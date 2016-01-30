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

import ie.deri.wsmx.core.management.ManagementUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SetAttributesCommandProcessor, processes a request for setting one or more
 * attributes in one MBean. it uses th facility of havin multiple submit buttons
 * in a web page if the set_all=Set variable is passed all attributes will be
 * set, if a set_XXX variable is passed only the specific attribute will be set
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/commandprocessor/SetAttributesCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-09-18 16:11:13 $
 */ 
public class SetAttributesCommandProcessor extends AbstractCommandProcessor {

    protected String objectVariable = null;
    protected Map variables = null;
    
    public SetAttributesCommandProcessor() {
        super();
    }

    @Override
	public Document execute() throws IOException,
            JMException {
        Document document = builder.newDocument();

        Element root = document.createElement("MBeanOperation");
        document.appendChild(root);
        Element operationElement = document.createElement("Operation");
        operationElement.setAttribute("operation", "setattributes");
        root.appendChild(operationElement);

        if (objectVariable == null || objectVariable.equals("")) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg",
                    "Missing objectname in the request");
            return document;
        }
        operationElement.setAttribute("objectname", objectVariable);
        ObjectName name = null;
        try {
            name = new ObjectName(objectVariable);
        } catch (MalformedObjectNameException e) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg", "Malformed object name");
            return document;
        }
        if (mBeanServer.isRegistered(name)) {
            if (variables.containsKey("setall")) {
                Iterator keys = variables.keySet().iterator();
                SortedMap<String, Element> allAttributes = new TreeMap<String, Element>();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (key.startsWith("value_")) {
                        String attributeVariable = key.substring(6, key
                                .length());
                        String valueVariable = getVariable(key);
                        Element attributeElement = setAttribute(document,
                                attributeVariable, valueVariable, name);
                        allAttributes.put(attributeVariable, attributeElement);
                        operationElement.appendChild(attributeElement);
                    }
                }
                keys = allAttributes.keySet().iterator();
                while (keys.hasNext()) {
                    Element attributeElement = allAttributes.get(keys.next());
                    operationElement.appendChild(attributeElement);
                }
            } else {
                Iterator keys = variables.keySet().iterator();
                SortedMap<String, Element> allAttributes = new TreeMap<String, Element>();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (key.startsWith("set_")) {
                        String attributeVariable = key.substring(4, key
                                .length());
                        String valueVariable = getVariable("value_"
                                + attributeVariable);
                        Element attributeElement = setAttribute(document,
                                attributeVariable, valueVariable, name);
                        allAttributes.put(attributeVariable, attributeElement);
                    }
                }
                keys = allAttributes.keySet().iterator();
                while (keys.hasNext()) {
                    Element attributeElement = allAttributes.get(keys
                            .next());
                    operationElement.appendChild(attributeElement);
                }
            }
            // operationElement.setAttribute("result", "success");
        } else {
            if (name != null) {
                operationElement.setAttribute("result", "error");
                operationElement.setAttribute("errorMsg", "MBean " + name
                        + " not registered");
            }
        }
        return document;
    }
    
    public String getVariable(String name) {
        if (variables.containsKey(name)) {
            Object variable = variables.get(name);
            if (variable instanceof String) {
                return (String) variable;
            } else if (variable instanceof String[]) {
                return ((String[]) variable)[0];
            }
        }
        
        objectVariable = null;
        variables = null;
        
        return null;
    }

    private Element setAttribute(Document document, String attributeVariable,
            String valueVariable, ObjectName name) throws JMException {
        Element attributeElement = document.createElement("Attribute");
        attributeElement.setAttribute("attribute", attributeVariable);
        MBeanInfo info = mBeanServer.getMBeanInfo(name);
        MBeanAttributeInfo[] attributes = info.getAttributes();
        MBeanAttributeInfo targetAttribute = null;
        if (attributes != null) {
            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i].getName().equals(attributeVariable)) {
                    targetAttribute = attributes[i];
                    break;
                }
            }
        }
        if (targetAttribute != null) {
            String type = targetAttribute.getType();
            Object value = null;
            if (valueVariable != null) {
                try {
                    value = ManagementUtil.createParameterValue(type,
                            valueVariable);
                } catch (Exception e) {
                    attributeElement.setAttribute("result", "error");
                    attributeElement.setAttribute("errorMsg", "Value: "
                            + valueVariable + " could not be converted to "
                            + type);
                }
                if (value != null) {
                    try {
                        mBeanServer.setAttribute(name, new Attribute(
                                attributeVariable, value));
                        attributeElement.setAttribute("result", "success");
                        attributeElement.setAttribute("value", valueVariable);
                    } catch (Exception e) {
                        attributeElement.setAttribute("result", "error");
                        attributeElement.setAttribute("errorMsg", e
                                .getMessage());
                    }
                }
            }
        } else {
            attributeElement.setAttribute("result", "error");
            attributeElement.setAttribute("errorMsg", "Attribute "
                    + attributeVariable + " not found");
        }
        return attributeElement;
    }

    public void setObjectVariable(String objectVariable) {
        this.objectVariable = objectVariable;
    }

    public void setVariables(Map variables) {
        this.variables = variables;
    }

}
