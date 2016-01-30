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

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SetAttributeCommandProcessor, processes a request for setting one attribute
 * in one MBean
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/commandprocessor/SetAttributeCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-09-18 16:11:13 $
 */ 
public class SetAttributeCommandProcessor extends AbstractCommandProcessor {

    protected String objectVariable = null;
    protected String attributeVariable = null;
    protected String valueVariable = null;
    
    
    public SetAttributeCommandProcessor() {
        super();
    }

    @Override
	public Document execute() throws IOException,
            JMException {
        Document document = builder.newDocument();

        Element root = document.createElement("MBeanOperation");
        document.appendChild(root);
        Element operationElement = document.createElement("Operation");
        operationElement.setAttribute("operation", "setattribute");
        root.appendChild(operationElement);

        if (objectVariable == null || objectVariable.equals("")
                || attributeVariable == null || attributeVariable.equals("")
                || valueVariable == null) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg",
                    "Incorrect parameters in the request");
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
                        operationElement.setAttribute("result", "error");
                        operationElement.setAttribute("errorMsg", "Value: "
                                + valueVariable + " could not be converted to "
                                + type);
                    }
                    if (value != null) {
                        try {
                            mBeanServer.setAttribute(name, new Attribute(
                                    attributeVariable, value));
                            operationElement.setAttribute("result", "success");
                        } catch (Exception e) {
                            operationElement.setAttribute("result", "error");
                            operationElement.setAttribute("errorMsg", e
                                    .getMessage());
                        }
                    }
                }
            } else {
                operationElement.setAttribute("result", "error");
                operationElement.setAttribute("errorMsg", "Attribute "
                        + attributeVariable + " not found");
            }
        } else {
            if (name != null) {
                operationElement.setAttribute("result", "error");
                operationElement.setAttribute("errorMsg", "MBean " + name
                        + " not registered");
            }
        }
        
        objectVariable = null;
        attributeVariable = null;
        valueVariable = null;
        return document;
    }

    public void setAttributeVariable(String attributeVariable) {
        this.attributeVariable = attributeVariable;
    }

    public void setObjectVariable(String objectVariable) {
        this.objectVariable = objectVariable;
    }

    public void setValueVariable(String valueVariable) {
        this.valueVariable = valueVariable;
    }

}
