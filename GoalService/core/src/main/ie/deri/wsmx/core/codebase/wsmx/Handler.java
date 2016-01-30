/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */

package ie.deri.wsmx.core.codebase.wsmx;

import ie.deri.wsmx.core.WSMXKernel;
import ie.deri.wsmx.core.codebase.ComponentClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Custom protocolhandler. It is in a wsmx subpackage
 * since the name of the package must match the name of
 * the protocol. When asked for an URL instead of a stream the
 * <code>ComponentClassLoader</code> returns a URL that specifies
 * wsmx as its protocol. This allows this handler the be invoked
 * every time an URL with the wsmx protocol get resolved. In this way
 * we can take care of loading regular resource as well as the ones in
 * embedded jars. 
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/codebase/wsmx/Handler.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.4 $ $Date: 2005-10-28 06:50:42 $
 */ 
public class Handler extends URLStreamHandler {

	static Logger logger = Logger.getLogger(Handler.class);

    public static String PROTOCOL = "wsmx";

	protected int len = PROTOCOL.length()+1;
	
	/** 
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		
        final String resource = u.toString().substring(len);
        final String[] split = resource.split("!");
        
		return new URLConnection(u) {
			@Override
			public void connect() {
			}
			@Override
			public InputStream getInputStream() {
				Map<File, ComponentClassLoader> codebases = WSMXKernel.getCodebase().getComponentCodebases();
				ComponentClassLoader cl = codebases.get(new File(split[0]));
				if (cl == null) {
					codebases = WSMXKernel.getCodebase().getCandidateCodebases();
					cl = codebases.get(new File(split[0]));
				}
				if (cl == null) 
					return null;
				logger.debug("Handler uses classloader:" + cl);
				return cl.getByteStream(split[1]);
			}
		};
	}
    
}
