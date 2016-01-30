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

package ie.deri.wsmx.core.management.sshadapter;

import ie.deri.wsmx.core.WSMXKernel;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.sshtools.daemon.platform.NativeAuthenticationProvider;
import com.sshtools.daemon.platform.PasswordChangeException;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 02.05.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/sshadapter/GlobalUserAuthenticationProvider.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2006-03-07 13:34:57 $
 */ 
public class GlobalUserAuthenticationProvider extends
        NativeAuthenticationProvider {

	protected static Logger logger = Logger.getLogger(GlobalUserAuthenticationProvider.class);
	private Map<String, String> validLogins = new HashMap<String, String>();
	
    public GlobalUserAuthenticationProvider() {
        super();
        initialize();
    }
    
    public void initialize() {
        try {
	    	Properties p = new Properties();
	        InputStream stream = WSMXKernel.PROPERTIES_CONFIG_LOCATION.toURI().toURL().openStream();
			p.load(stream);
	        stream.close();
	        addLogin("root", "dedication"); //default
	    	String rpw = p.getProperty("wsmx.ssh.rootpassword");
	    	if (rpw != null)
	    		validLogins.put("root", new String(rpw));
	    	logger.debug("valid ssh logins:" + validLogins);
		} catch (Throwable t) {
			logger.warn("Failed to initialize SSH authentication.", t);
		}
    }

    public void addLogin(String loginname, String password) {
    	validLogins.put(new String(loginname), new String(password));
    }

    @Override
    public String getHomeDirectory(String username) throws IOException {
        return "/home/wsmx";
    }

    @Override
    public boolean logonUser(String username, String password)
            throws PasswordChangeException, IOException {
    	if (validLogins.containsKey(username))
   			if (validLogins.get(username).equals(password))
   				return true;
    	return false;
    }

    @Override
    public boolean logonUser(String username) throws IOException {
        return true;
    }

    @Override
    public void logoffUser() throws IOException {
        //nothing
    }

    @Override
    public boolean changePassword(String username, String oldpassword,
            String newpassword) {
        return false;
    }

}
