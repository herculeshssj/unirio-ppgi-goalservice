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

import ie.deri.wsmx.core.WSMXKernel;
import ie.deri.wsmx.core.configuration.KernelConfiguration;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides Information on the server instance.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/kerneldata/ServerView.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.8 $ $Date: 2005-08-14 04:36:13 $
 */ 
public class ServerView extends AbstractView {

    public ServerView() throws ParserConfigurationException {
        super();
    }

    public ServerView(MBeanServer mbs) throws ParserConfigurationException {
        super(mbs);
    }
    
	public ServerView(MBeanServer mbs, DocumentBuilder b)  throws ParserConfigurationException {
		super(mbs, b);
	}
	

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.core.management.xmlviews.XMLView#getElement()
	 */
	@Override
	public Document getView(String ... args) throws JMException {
		Document document = builder.newDocument();

	      Element root = document.createElement("Server");
	      document.appendChild(root);
	      
	      //FIXME catch exception, log and build replacement document
	      root.setAttribute(
	      		"wsmx.startuptimestamp.long",
	      		mBeanServer.getAttribute(new ObjectName("core:name=WSMXKernel"), "StartupTimestamp").toString()	  
	      );

	      root.setAttribute(
	      		"wsmx.startuptimestamp.string",
	      		(String)mBeanServer.getAttribute(new ObjectName("core:name=WSMXKernel"), "FormattedStartupTimestamp")	  
	      );

	      root.setAttribute(
	      		"wsmx.uptime.long",
	      		mBeanServer.getAttribute(new ObjectName("core:name=WSMXKernel"), "Uptime").toString()
	      );

	      root.setAttribute(
	      		"wsmx.uptime.string",
	      		(String)mBeanServer.getAttribute(new ObjectName("core:name=WSMXKernel"), "FormattedUptime")	  
	      );

    
	      root.setAttribute(
	      		"wsmx.version",     		(String)mBeanServer.getAttribute(new ObjectName("core:name=WSMXKernel"), "Version")	  
	      );

	      root.setAttribute(
		   		"wsmx.domains",
		   		Integer.toString(mBeanServer.getDomains().length)
		  );      

		  root.setAttribute(
		   		"wsmx.mbeans",
		   		mBeanServer.getMBeanCount().toString()
		  );  
		  
	      root.setAttribute(
		  		"wsmx.hostname",
		   		WSMXKernel.HOSTNAME
		  );
	      
	      
	      root.setAttribute(
		   		"wsmx.ipaddress",
		   		WSMXKernel.IP_ADDRESS
		  );
	      
    	  KernelConfiguration kc = (KernelConfiguration)mBeanServer.invoke(WSMXKernel.OBJECT_NAME, "getKernelConfiguration", new Object[]{}, new String[]{});
    	  String spaceAddress;
    	  if (kc == null || (spaceAddress = kc.getSpaceAddress()) == null)
    		  spaceAddress = "unknown";
    	  
	      root.setAttribute(
		   		"wsmx.spaceaddress",
		   		spaceAddress
		  );
	      
	      root.setAttribute(
		   		"wsmx.federationpeers",
		   		"none"  
		  );
		      
	      root.setAttribute(
	      		"os.arch",
				System.getProperty("os.arch")	  
	      );

	      root.setAttribute(
	      		"os.name",
				System.getProperty("os.name")	  
	      );

	      root.setAttribute(
	      		"os.version",
				System.getProperty("os.version")	  
	      );
	      
	      root.setAttribute(
	      		"java.vm.vendor",
				System.getProperty("java.vm.vendor")	  
	      );
	      
	      root.setAttribute(
	      		"java.vm.name",
				System.getProperty("java.vm.name")	  
	      );

	      root.setAttribute(
	      		"java.vm.version",
				System.getProperty("java.vm.version")	  
	      );         

	      return document;
	}

}

