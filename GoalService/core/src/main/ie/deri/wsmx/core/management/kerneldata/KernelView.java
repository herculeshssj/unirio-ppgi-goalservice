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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Pulls together information about all the various
 * parts of the microkernel and structures it coherently.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/kerneldata/KernelView.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.6 $ $Date: 2005-08-14 04:36:01 $
 */ 
public class KernelView extends AbstractView {

    public KernelView() throws ParserConfigurationException {
        super();
    }
    public KernelView(MBeanServer mbs) throws ParserConfigurationException {
        super(mbs);
    }
	public KernelView(MBeanServer mbs, DocumentBuilder b) throws ParserConfigurationException {
		super(mbs, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.core.management.xmlviews.XMLView#getElement()
	 */
	@Override
	public Document getView(String... args) throws JMException, ParserConfigurationException {
		Document document = new ServerView(mBeanServer, builder).getView();
		Node root = document.getFirstChild();
		
		//import domains
	    Document d = new DomainView(mBeanServer, builder).getView();
	    NodeList domainList = d.getFirstChild().getChildNodes();
	    for(int i = 0; i < domainList.getLength() ; i++) {   		
	    	Node newDomain = root.appendChild(document.importNode(domainList.item(i), true));
		    //replace shallow MBean information with detailed attributes/operation/constructors information
	      	NodeList mBeanList = newDomain.getChildNodes();
	      		for(int j = 0; j < mBeanList.getLength() ; j++) {
	      			String objectName = mBeanList.item(j).getAttributes().getNamedItem("objectname").getNodeValue();
	      			newDomain.replaceChild(
		      			document.importNode(
		      				new MBeanView(
		      						mBeanServer,
		      						builder
		      				).getView(objectName).getFirstChild(),
							true
					    ),
						mBeanList.item(j)
	      			);
	      		}	
	      	}
	    return document;
	}
	
}

