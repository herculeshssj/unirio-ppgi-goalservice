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
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides information on a single MBeans attributes, operations and
 * constructors.
 * 
 * <pre>
 *  Created on Feb 11, 2005
 *  Committed by $$Author: haselwanter $$
 *  $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/kerneldata/MBeanView.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 * 
 * @version $Revision: 1.8 $ $Date: 2005-09-18 15:59:12 $
 */
public class MBeanView extends AbstractView {

	public MBeanView() throws ParserConfigurationException {
		super();
	}

	public MBeanView(MBeanServer mbs) throws ParserConfigurationException {
		super(mbs);
	}

	public MBeanView(MBeanServer mbs, DocumentBuilder b)
			throws ParserConfigurationException {
		super(mbs, b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.core.management.xmlviews.XMLView#getElement()
	 */
	//FIXME remove SupressWarnings once MBeanServer.queryNames() returns a parameterized set
	@SuppressWarnings("unchecked")
	@Override
	public Document getView(String... args) throws JMException {
		String name = null;
		if (args.length > 0)
			name = args[0];

		Document document = builder.newDocument();

		ObjectName objectName = null;

		if (name != null) {
			objectName = new ObjectName(name);
			if (!objectName.isPattern()) {
				// not a pattern - assume a single MBean
				if (mBeanServer.isRegistered(objectName)) {
					Element mb = createMBeanElement(document, objectName);
					document.appendChild(mb);
				}
			} else {
				// A pattern - return all matching MBeans
				Set<ObjectName> names = new TreeSet<ObjectName>(ManagementUtil
						.createObjectNameComparator());
				names.addAll(mBeanServer.queryNames(objectName, null));
				Element root = document.createElement("Server");
				root.setAttribute("pattern", objectName.toString());
				for (Iterator it = names.iterator(); it.hasNext();) {
					Element mb = createMBeanElement(document, (ObjectName) it
							.next());
					root.appendChild(mb);
				}
				document.appendChild(root);
			}
		}
		return document;
	}

	private Element createMBeanElement(Document document, ObjectName objectName)
			throws JMException {
		Element root = document.createElement("MBean");

		MBeanInfo info = mBeanServer.getMBeanInfo(objectName);
		root.setAttribute("description", info.getDescription());
		root.setAttribute("classname", info.getClassName());
		root.setAttribute("objectname", objectName.toString());

		if (info instanceof ModelMBeanInfo) {
			root.setAttribute("model", "true");
		}
		MBeanAttributeInfo[] attributes = info.getAttributes();
		if (attributes != null) {
			SortedMap<String, Element> sortedAttributes = new TreeMap<String, Element>();
			for (int i = 0; i < attributes.length; i++) {
				Element attribute = document.createElement("Attribute");
				attribute.setAttribute("name", attributes[i].getName());
				attribute.setAttribute("type", attributes[i].getType());
				attribute.setAttribute("description", attributes[i]
						.getDescription());
				attribute.setAttribute("strinit", String.valueOf(ManagementUtil
						.canCreateParameterValue(attributes[i].getType())));
				if (attributes[i].isReadable() && attributes[i].isWritable()) {
					attribute.setAttribute("availability", "RW");
				}
				if (attributes[i].isReadable() && !attributes[i].isWritable()) {
					attribute.setAttribute("availability", "RO");
				}
				if (!attributes[i].isReadable() && attributes[i].isWritable()) {
					attribute.setAttribute("availability", "WO");
				}
				try {
					Object attributeValue = mBeanServer.getAttribute(
							objectName, attributes[i].getName());
					attribute.setAttribute("isnull",
							(attributeValue == null) ? "true" : "false");
					if (attributeValue != null) {
						attribute.setAttribute("value", attributeValue
								.toString());
						if (attributeValue.getClass().isArray()) {
							attribute.setAttribute("aggregation", "array");
						}
						if (attributeValue instanceof java.util.Collection) {
							attribute.setAttribute("aggregation", "collection");
						}
						if (attributeValue instanceof java.util.Map) {
							attribute.setAttribute("aggregation", "map");
						}
					} else {
						attribute.setAttribute("value", "null");
					}

				} catch (JMException e) {
					attribute.setAttribute("value", e.getMessage());
				}
				sortedAttributes.put(attributes[i].getName(), attribute);
			}
			Iterator keys = sortedAttributes.keySet().iterator();
			while (keys.hasNext()) {
				root.appendChild(sortedAttributes.get(keys.next()));
			}
		}

		MBeanConstructorInfo[] constructors = info.getConstructors();
		if (constructors != null) {
			// How to order contructors?
			for (int i = 0; i < constructors.length; i++) {
				Element constructor = document.createElement("Constructor");
				constructor.setAttribute("name", constructors[i].getName());
				constructor.setAttribute("description", constructors[i]
						.getDescription());
				addParameters(constructor, document, constructors[i]
						.getSignature());
				root.appendChild(constructor);
			}
		}

		MBeanOperationInfo[] operations = info.getOperations();
		if (operations != null) {
			for (int i = 0; i < operations.length; i++) {
				Element operation = document.createElement("Operation");
				operation.setAttribute("name", operations[i].getName());
				operation.setAttribute("description", operations[i]
						.getDescription());
				operation.setAttribute("return", operations[i].getReturnType());
				switch (operations[i].getImpact()) {
				case MBeanOperationInfo.UNKNOWN:
					operation.setAttribute("impact", "unknown");
					break;
				case MBeanOperationInfo.ACTION:
					operation.setAttribute("impact", "action");
					break;
				case MBeanOperationInfo.INFO:
					operation.setAttribute("impact", "info");
					break;
				case MBeanOperationInfo.ACTION_INFO:
					operation.setAttribute("impact", "action_info");
					break;
				}
				addParameters(operation, document, operations[i].getSignature());
				root.appendChild(operation);
			}
		}

		MBeanNotificationInfo[] notifications = info.getNotifications();
		if (notifications != null) {
			for (int i = 0; i < notifications.length; i++) {
				Element notification = document.createElement("Notification");
				notification.setAttribute("name", notifications[i].getName());
				notification.setAttribute("description", notifications[i]
						.getDescription());
				String[] types = notifications[i].getNotifTypes();
				for (int j = 0; j < types.length; j++) {
					Element type = document.createElement("Type");
					type.setAttribute("name", types[j]);
					notification.appendChild(type);
				}
				root.appendChild(notification);
			}
		}

		return root;
	}

	protected void addParameters(Element node, Document document,
			MBeanParameterInfo[] parameters) {
		for (int j = 0; j < parameters.length; j++) {
			Element parameter = document.createElement("Parameter");
			parameter.setAttribute("name", parameters[j].getName());
			parameter.setAttribute("description", parameters[j]
					.getDescription());
			parameter.setAttribute("type", parameters[j].getType());
			parameter.setAttribute("strinit", String.valueOf(ManagementUtil
					.canCreateParameterValue(parameters[j].getType())));
			// add id since order is relevant
			parameter.setAttribute("id", "" + j);
			node.appendChild(parameter);
		}
	}

}
