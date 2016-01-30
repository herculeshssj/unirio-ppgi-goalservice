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

import ie.deri.wsmx.core.management.ManagementUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides information about all domains of a given <code>MBeanServer</code>.
 * 
 * <pre>
 *  Created on Feb 11, 2005
 *  Committed by $$Author: haselwanter $$
 *  $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/kerneldata/DomainView.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 * 
 * @version $Revision: 1.7 $ $Date: 2005-09-18 15:59:13 $
 */
public class DomainView extends AbstractView {

	public DomainView() throws ParserConfigurationException {
		super();
	}

	public DomainView(MBeanServer mbs) throws ParserConfigurationException {
		super(mbs);
	}

	public DomainView(MBeanServer mbs, DocumentBuilder b)
			throws ParserConfigurationException {
		super(mbs, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.core.management.xmlviews.XMLView#getElement()
	 */
	@Override
	public Document getView(String... args) throws JMException {
		String targetClass = null, queryNames = null;
		if (args.length > 0)
			targetClass = args[0];
		if (args.length > 1)
			queryNames = args[1];

		Document document = builder.newDocument();

		Element root = document.createElement("Server");
		document.appendChild(root);

		ObjectName query = null;
		if (queryNames != null) {
			try {
				query = new ObjectName(queryNames);
			} catch (MalformedObjectNameException e) {
				Element exceptionElement = document.createElement("Exception");
				exceptionElement.setAttribute("errorMsg", e.getMessage());
				root.appendChild(exceptionElement);
				return document;
			}
		}
		Set mbeans = mBeanServer.queryMBeans(query, null);
		Iterator i = mbeans.iterator();
		// this will order the domains
		Map<String, Set<ObjectName>> domains = new TreeMap<String, Set<ObjectName>>();
		while (i.hasNext()) {
			ObjectInstance instance = (ObjectInstance) i.next();
			ObjectName name = instance.getObjectName();
			String domain = name.getDomain();
			if (domains.containsKey(domain)) {
				domains.get(domain).add(name);
			} else {
				Set<ObjectName> objects = new TreeSet<ObjectName>(ManagementUtil
						.createObjectNameComparator());
				objects.add(name);
				domains.put(domain, objects);
			}
		}
		i = domains.keySet().iterator();
		while (i.hasNext()) {
			String domain = (String) i.next();
			Element domainElement = document.createElement("Domain");
			root.appendChild(domainElement);
			domainElement.setAttribute("name", domain);
			Set<ObjectName> names = domains.get(domain);
			Iterator j = names.iterator();
			while (j.hasNext()) {
				ObjectName targetName = (ObjectName) j.next();
				if (targetClass != null
						&& !mBeanServer.isInstanceOf(targetName, targetClass)) {
					continue;
				}
				Element mBeanElement = document.createElement("MBean");
				mBeanElement.setAttribute("objectname", targetName.toString());
				MBeanInfo info = mBeanServer.getMBeanInfo(targetName);
				mBeanElement.setAttribute("description", info.getDescription());
				mBeanElement.setAttribute("classname", info.getClassName());
				domainElement.appendChild(mBeanElement);
			}
		}
		return document;
	}

}
