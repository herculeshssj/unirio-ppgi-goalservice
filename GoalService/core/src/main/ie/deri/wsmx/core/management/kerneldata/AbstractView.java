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

package ie.deri.wsmx.core.management.kerneldata;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;


/**
 * A <code>Viewable</code> that uses a <code>DocumentBuilder</code>
 * and a <code>MBeanServer</code> as sink and source of information,
 * respectively. If the <code>DocumentBuilder</code> reference is null
 * a new one will be created. If the <code>MBeanServer</code> reference
 * is null a <code>MBeanServer</code> will we arbitrarily selected from
 * all <code>MBeanServers</code> registered with the JVM.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: mzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/kerneldata/AbstractView.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2005-07-01 16:45:19 $
 */ 
public abstract class AbstractView implements Viewable {

	protected DocumentBuilder builder;
	protected MBeanServer mBeanServer;
	
	
	public AbstractView() throws ParserConfigurationException {
	    this(null);		
	}
	
	public AbstractView(MBeanServer mBeanServer)  throws ParserConfigurationException {
		this(mBeanServer, null);
	}
	
	public AbstractView(MBeanServer mBeanServer, DocumentBuilder builder) throws ParserConfigurationException {
		super();
		this.builder = builder;
		if (mBeanServer == null)
		    //get the first MBeanServer in this JVM, no matter what it's agent id is
		    this.mBeanServer = (MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
		else
		    this.mBeanServer = mBeanServer;
		if (builder == null) {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	       	this.builder = factory.newDocumentBuilder();
		} else
		    this.builder = builder;
	}

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.core.management.xmlviews.XMLView#getElement()
	 */
	public abstract Document getView(String ... args) throws JMException, ParserConfigurationException;
}
