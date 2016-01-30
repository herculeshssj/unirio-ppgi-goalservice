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
import java.lang.reflect.Constructor;
import java.util.Arrays;

import javax.management.JMException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * ConstructorsCommandProcessor, processes a request to query the available
 * constructors for a classname
 * 
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/commandprocessor/ConstructorsCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-09-03 17:10:17 $
 */ 
public class ConstructorsCommandProcessor extends AbstractCommandProcessor {
    
	static Logger logger = Logger.getLogger(ConstructorsCommandProcessor.class);
    protected String classname = null;
    
    public ConstructorsCommandProcessor() {
        super();
    }    

    @Override
	public Document execute() throws IOException,
            JMException {
        logger.debug("Attempting to load " + classname);
        Document document = builder.newDocument();

        if (classname == null || classname.trim().length() == 0) {
            return createException(document, "", "classname parameter required");
        }
		// look class in default classloader
		Class targetClass = null;
		//FIXME use component classloaders
		try {
			targetClass = mBeanServer.getClassLoaderRepository().loadClass(classname);
		} catch (ClassNotFoundException e) {
			logger.debug("Class not found using the MBeanServers classloader repository.");
		}
		try {
		    if (targetClass == null) {
		        targetClass = ClassLoader.getSystemClassLoader().loadClass(
		                classname);
		    }
		} catch (ClassNotFoundException e) {
			logger.debug("Class not found using the systemclassloader.");
		}
		try {
		    if (targetClass == null) {
		        targetClass = getClass().getClassLoader().loadClass(
		                classname);
		    }
		} catch (ClassNotFoundException e) {
			logger.debug("Class not found using the current classloader.");
		}

		if (targetClass == null) {
		    return createException(document, classname, "class "
		            + classname + " not found");
		}

		Element root = document.createElement("Class");
		root.setAttribute("classname", classname);
		document.appendChild(root);
		Constructor[] constructors = targetClass.getConstructors();
		Arrays.sort(constructors, ManagementUtil
		        .createConstructorComparator());
		for (int i = 0; i < constructors.length; i++) {
		    System.out.println("Constructor " + constructors[i]);
		    Element constructor = document.createElement("Constructor");
		    constructor.setAttribute("name", constructors[i].getName());
		    addParameters(constructor, document, constructors[i]
		            .getParameterTypes());
		    root.appendChild(constructor);
		}
        
        classname = null;
        
        return document;
    }

    protected void addParameters(Element node, Document document,
            Class[] parameters) {
        for (int j = 0; j < parameters.length; j++) {
            Element parameter = document.createElement("Parameter");
            parameter.setAttribute("type", parameters[j].getName());
            parameter.setAttribute("strinit", String.valueOf(ManagementUtil
                    .canCreateParameterValue(parameters[j].getName())));
            // add id since order is relevant
            parameter.setAttribute("id", "" + j);
            node.appendChild(parameter);
        }
    }

    private Document createException(Document document, String classname,
            String message) {
        Element exceptionElement = document.createElement("Exception");
        document.appendChild(exceptionElement);
        exceptionElement.setAttribute("classname", classname);
        exceptionElement.setAttribute("errorMsg", message);
        return document;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

}
