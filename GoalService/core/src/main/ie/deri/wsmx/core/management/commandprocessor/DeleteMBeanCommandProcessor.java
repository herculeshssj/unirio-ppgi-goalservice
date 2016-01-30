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

import ie.deri.wsmx.core.WSMXKernel;

import java.io.IOException;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * DeleteMBeanCommandProcessor, processes a request for unregistering an MBean
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/commandprocessor/DeleteMBeanCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.4 $ $Date: 2005-10-21 03:53:59 $
 */ 
public class DeleteMBeanCommandProcessor extends AbstractCommandProcessor {

    protected String objectVariable = null;
    
    static Logger logger = Logger.getLogger(DeleteMBeanCommandProcessor.class);

    public DeleteMBeanCommandProcessor() {
        super();
    }

    @Override
	public Document execute() throws IOException,
            JMException {
        Document document = builder.newDocument();

        Element root = document.createElement("MBeanOperation");
        document.appendChild(root);
        Element operationElement = document.createElement("Operation");
        operationElement.setAttribute("operation", "delete");
        root.appendChild(operationElement);

        operationElement.setAttribute("objectname", objectVariable);
        if (objectVariable == null || objectVariable.equals("")) {
            operationElement.setAttribute("result", "error");
            operationElement.setAttribute("errorMsg",
                    "Incorrect parameters in the request");
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
            try {
                String result = (String)mBeanServer.invoke(WSMXKernel.OBJECT_NAME,
                              "undeploy",
                              new Object[]{name},
                              new String[]{"javax.management.ObjectName"}
                );
                operationElement.setAttribute("result", "success");
                operationElement.setAttribute("errorMsg", result);
            } catch (Exception e) {
                logger.warn("Undeployment failed.", e);
                operationElement.setAttribute("result", "error");
                operationElement.setAttribute("errorMsg", e.getMessage());
            }
        } else {
            if (name != null) {
                operationElement.setAttribute("result", "error");
                operationElement.setAttribute("errorMsg", "MBean " + name
                        + " not registered");
            }
        }
        
        objectVariable = null;
        return document;
    }

    public void setObjectVariable(String objectVariable) {
        this.objectVariable = objectVariable;
    }

}
