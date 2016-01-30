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

package ie.deri.wsmx.core.management.commandprocessor;


import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * GetAttributeCommandProcessor, processes a request for getting one attribute
 * of a specific MBean. It also support some formats for types like Arrays
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/commandprocessor/GetAttributeCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-09-18 16:11:13 $
 */ 
public class GetAttributeCommandProcessor extends AbstractCommandProcessor {

    protected String name = null;
    protected String attributeVariable = null;
    protected String formatVariable = null;
    
    public GetAttributeCommandProcessor() {
        super();
    }

    @Override
	public Document execute() throws IOException,
            JMException {
        Document document = builder.newDocument();

        ObjectName objectName = null;
        MBeanAttributeInfo targetAttribute = null;
        // special case
        boolean validMBean = false;
        if (name != null) {
            objectName = new ObjectName(name);
            if (mBeanServer.isRegistered(objectName)) {
                validMBean = true;
            }
        }
        if (validMBean && attributeVariable != null) {
            validMBean = false;
            MBeanInfo info = mBeanServer.getMBeanInfo(objectName);
            MBeanAttributeInfo[] attributes = info.getAttributes();

            if (attributes != null) {
                for (int i = 0; i < attributes.length; i++) {
                    if (attributes[i].getName().equals(attributeVariable)) {
                        targetAttribute = attributes[i];
                        validMBean = true;
                        break;
                    }
                }
            }
        }
        if (validMBean) {
            Element root = document.createElement("MBean");
            document.appendChild(root);

            root.setAttribute("objectname", objectName.toString());
            MBeanInfo info = mBeanServer.getMBeanInfo(objectName);
            root.setAttribute("classname", info.getClassName());
            root.setAttribute("description", info.getDescription());

            Element attribute = document.createElement("Attribute");
            attribute.setAttribute("name", attributeVariable);
            attribute.setAttribute("classname", targetAttribute.getType());
            Object attributeValue = mBeanServer.getAttribute(objectName,
                    attributeVariable);
            attribute.setAttribute("isnull", (attributeValue == null) ? "true"
                    : "false");
            root.appendChild(attribute);

            if ("array".equals(formatVariable)
                    && attributeValue.getClass().isArray()) {
                Element array = document.createElement("Array");
                array.setAttribute("componentclass", attributeValue.getClass()
                        .getComponentType().getName());
                int length = Array.getLength(attributeValue);
                array.setAttribute("length", "" + length);
                for (int i = 0; i < length; i++) {
                    Element arrayElement = document.createElement("Element");
                    arrayElement.setAttribute("index", "" + i);
                    if (Array.get(attributeValue, i) != null) {
                        arrayElement.setAttribute("element", Array.get(
                                attributeValue, i).toString());
                        arrayElement.setAttribute("isnull", "false");
                    } else {
                        arrayElement.setAttribute("element", "null");
                        arrayElement.setAttribute("isnull", "true");
                    }
                    array.appendChild(arrayElement);
                }
                attribute.appendChild(array);
            } else if ("collection".equals(formatVariable)
                    && attributeValue instanceof Collection) {
                Collection collection = (Collection) attributeValue;
                Element collectionElement = document
                        .createElement("Collection");
                collectionElement
                        .setAttribute("length", "" + collection.size());
                Iterator i = collection.iterator();
                int j = 0;
                while (i.hasNext()) {
                    Element collectionEntry = document.createElement("Element");
                    collectionEntry.setAttribute("index", "" + j++);
                    Object obj = i.next();
                    if (obj != null) {
                        collectionEntry.setAttribute("elementclass", obj
                                .getClass().getName());
                        collectionEntry.setAttribute("element", obj.toString());
                    } else {
                        collectionEntry.setAttribute("elementclass", "null");
                        collectionEntry.setAttribute("element", "null");
                    }
                    collectionElement.appendChild(collectionEntry);
                }
                attribute.appendChild(collectionElement);
            } else if ("map".equals(formatVariable)
                    && attributeValue instanceof Map) {
                Map map = (Map) attributeValue;
                Element mapElement = document.createElement("Map");
                mapElement.setAttribute("length", "" + map.size());
                Iterator i = map.keySet().iterator();
                int j = 0;
                while (i.hasNext()) {
                    Element mapEntry = document.createElement("Element");
                    mapEntry.setAttribute("index", "" + j++);
                    Object key = i.next();
                    Object entry = map.get(key);
                    if (entry != null && key != null) {
                        mapEntry.setAttribute("keyclass", key.getClass()
                                .getName());
                        mapEntry.setAttribute("key", key.toString());
                        mapEntry.setAttribute("elementclass", entry.getClass()
                                .getName());
                        mapEntry.setAttribute("element", entry.toString());
                    } else {
                        mapEntry.setAttribute("keyclass", "null");
                        mapEntry.setAttribute("key", "null");
                        mapEntry.setAttribute("elementclass", "null");
                        mapEntry.setAttribute("element", "null");
                    }
                    mapElement.appendChild(mapEntry);
                }
                attribute.appendChild(mapElement);
            } else {
                attribute.setAttribute("value",
                        (attributeValue == null) ? "null" : attributeValue
                                .toString());
            }

        }
        
        name = null;
        attributeVariable = null;
        formatVariable = null;
        return document;
    }

    public void setAttributeVariable(String attributeVariable) {
        this.attributeVariable = attributeVariable;
    }

    public void setFormatVariable(String formatVariable) {
        this.formatVariable = formatVariable;
    }

    public void setName(String name) {
        this.name = name;
    }
}
