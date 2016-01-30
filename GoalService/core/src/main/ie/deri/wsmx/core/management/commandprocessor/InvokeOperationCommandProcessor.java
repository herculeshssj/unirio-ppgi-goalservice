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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * InvokeOperationCommandProcessor, processes a request for unregistering an
 * MBean
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/commandprocessor/InvokeOperationCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.4 $ $Date: 2005-09-18 16:11:13 $
 */
public class InvokeOperationCommandProcessor extends AbstractCommandProcessor {
    
    static Logger logger = Logger.getLogger(InvokeOperationCommandProcessor.class);
	
    protected String objectname = null;
    protected String operationname = null;
    protected Map<String, String> parameterTypes = new HashMap<String, String>();
    protected Map<String, String> parameterValues = new HashMap<String, String>();

    
    public InvokeOperationCommandProcessor() {
        super();
    }

    @Override
	public Document execute() throws IOException,
            JMException {
        Document document = builder.newDocument();

        Element root = document.createElement("MBeanOperation");
        document.appendChild(root);
        Element operationElement = document.createElement("Operation");
        operationElement.setAttribute("operation", "invoke");
        root.appendChild(operationElement);

        if (objectname == null || objectname.equals("")
                || operationname == null || operationname.equals("")) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg",
                    "Incorrect parameters in the request");
            return document;
        }
        operationElement.setAttribute("objectname", objectname);
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
                }

                catch (Exception e) {
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
        if (objectname == null || objectname.equals("")
                || operationname == null || operationname.equals("")) {
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
            name = new ObjectName(objectname);
        } catch (MalformedObjectNameException e) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg", "Malformed object name");
            return document;
        }

        if (mBeanServer.isRegistered(name)) {
            MBeanInfo info = mBeanServer.getMBeanInfo(name);
            MBeanOperationInfo[] operations = info.getOperations();
            boolean match = false;
            if (operations != null) {
                for (int j = 0; j < operations.length; j++) {
                    if (operations[j].getName().equals(operationname)) {
                        MBeanParameterInfo[] parameters = operations[j]
                                .getSignature();
                        if (parameters.length != types.size()) {
                            continue;
                        }
                        Iterator k = types.iterator();
                        boolean signatureMatch = true;
                        for (int p = 0; p < types.size(); p++) {
                            if (!parameters[p].getType().equals(k.next())) {
                                signatureMatch = false;
                                break;
                            }
                        }
                        match = signatureMatch;
                    }
                    if (match) {
                        break;
                    }
                }
            }
            if (!match) {
                operationElement.setAttribute("result", "error");
                operationElement.setAttribute("errorMsg",
                        "Operation singature has no match in the MBean");
            } else {
                try {
                    Object[] params = values.toArray();
                    String[] signature = new String[types.size()];
                    types.toArray(signature);
                    Object returnValue = mBeanServer.invoke(name, operationname,
                            params, signature);
                    operationElement.setAttribute("result", "success");
                    if (returnValue != null) {
                        operationElement.setAttribute("returnclass",
                                returnValue.getClass().getName());
                        operationElement.setAttribute("return", returnValue
                                .toString());
                    } else {
                        operationElement.setAttribute("returnclass", null);
                        operationElement.setAttribute("return", null);
                    }
                } catch (Exception e) {
                    logger.warn("Execption during invocation.", e);
                    operationElement.setAttribute("result", "error");
                    String msg = e.getMessage();
                    Throwable cause = e;
                    while ((cause = cause.getCause()) != null)
                        msg = msg + ": " + cause.getMessage();
                    operationElement.setAttribute("errorMsg", msg);
                }
            }
        } else {
            if (name != null) {
                operationElement.setAttribute("result", "error");
                operationElement.setAttribute("errorMsg", new StringBuffer(
                        "MBean ").append(name).append(" not registered")
                        .toString());
            }
        }
        
        objectname = null;
        operationname = null;
        parameterTypes.clear();
        parameterValues.clear();
        
        return document;
    }

    public static void setLogger(Logger logger) {
        InvokeOperationCommandProcessor.logger = logger;
    }

    public void setObjectVariable(String objectVariable) {
        this.objectname = objectVariable;
    }

    public void setOperationVariable(String operationVariable) {
        this.operationname = operationVariable;
    }

    public void setParameterTypes(Map<String, String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setParameterValues(Map<String, String> parameterValues) {
        this.parameterValues = parameterValues;
    }

}
