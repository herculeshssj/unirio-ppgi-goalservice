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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * CreateMBeanCommandProcessor, processes a request for creating and registering
 * an MBean
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/commandprocessor/CreateMBeanCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-09-18 16:11:13 $
 */ 
public class CreateMBeanCommandProcessor extends AbstractCommandProcessor {

	static Logger logger = Logger.getLogger(CreateMBeanCommandProcessor.class);
    protected String objectVariable = null;
    protected String classVariable = null;
    protected Map<String, String> parameterTypes = new HashMap<String, String>();
    protected Map<String, String> parameterValues = new HashMap<String, String>();
    
    public CreateMBeanCommandProcessor() {
        super();
    }

    @Override
	public Document execute() throws IOException,
            JMException {
        Document document = builder.newDocument();

        Element root = document.createElement("MBeanOperation");
        document.appendChild(root);
        Element operationElement = document.createElement("Operation");
        operationElement.setAttribute("name", "create");
        root.appendChild(operationElement);

        if (objectVariable == null || objectVariable.equals("")
                || classVariable == null || classVariable.equals("")) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg",
                    "Incorrect parameters in the request");
            
            return document;
        }
        operationElement.setAttribute("objectname", objectVariable);
        List<String> types = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        int i = 0;
        boolean unmatchedParameters = false;
        boolean valid = false;
        do {
            String parameterType = parameterTypes.get("type" + i);
            String parameterValue = parameterValues.get("value" + i);
            valid = (parameterType != null && parameterValue != null);
            if (valid) {
                types.add(parameterType);
                Object value = null;
                try {
                    value = ManagementUtil.createParameterValue(parameterType,
                            parameterValue);
                } catch (Exception e) {
                    operationElement.setAttribute("result", "error");
                    operationElement.setAttribute("errorMsg", "Parameter " + i
                            + ": " + parameterValue
                            + " cannot be converted to type " + parameterType);
                    return document;
                }
                if (value != null) {
                    values.add(value);
                }
            }
            if (parameterType == null ^ parameterValue == null) {
                unmatchedParameters = true;
                break;
            }
            i++;
        } while (valid);
        if (objectVariable == null || objectVariable.equals("")) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg",
                    "Incorrect parameters in the request");
            return document;
        }
        if (unmatchedParameters) {
            operationElement.setAttribute("result", "error");
            operationElement
                    .setAttribute("errorMsg",
                            "count of parameter types doesn't match count of parameter values");
            return document;
        }
        ObjectName name = null;
        try {
            name = new ObjectName(objectVariable);
        } catch (MalformedObjectNameException e) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg", "Malformed object name");
            return document;
        }

        if (mBeanServer.isRegistered(name)) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg", "A MBean with name "
                    + name + " is already registered");
            return document;
        }
		try {
		    if (types.size() > 0) {
		        Object[] params = values.toArray();
		        String[] signature = new String[types.size()];
		        types.toArray(signature);
		        mBeanServer.createMBean(classVariable, name, null, params,
		                signature);
		    } else {
		        mBeanServer.createMBean(classVariable, name, null);
		    }
		    operationElement.setAttribute("result", "success");
		} catch (Exception e) {
		    operationElement.setAttribute("result", "error");
		    operationElement.setAttribute("errorMsg", e.getMessage());
		}
        
        objectVariable = null;
        classVariable = null;
        parameterTypes.clear();
        parameterValues.clear();
        
        return document;
    }

    public void setClassVariable(String classVariable) {
        this.classVariable = classVariable;
    }

    public void setObjectVariable(String objectVariable) {
        this.objectVariable = objectVariable;
    }

    public void setParameterTypes(Map<String, String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setParameterValues(Map<String, String> parameterValues) {
        this.parameterValues = parameterValues;
    }

}
