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

package ie.deri.wsmx.wrapper;

import ie.deri.wsmx.core.configuration.ComponentConfiguration;
import ie.deri.wsmx.core.configuration.annotation.Exposed;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.management.Descriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.apache.log4j.Logger;

/**
 * This wrapper is used to abstract a component, that wishes to
 * expose its managability, away from JMX. Using reflection
 * we can extract most information automatically and create
 * the necessary MBean descriptors instead of having the component
 * manually create them.
 *
 * <pre>
 * Created on 06.03.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/wrapper/ManagabilityWrapper.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2006-01-25 02:07:29 $
 */
public class ManagabilityWrapper {
	
	static Logger logger = Logger.getLogger(ManagabilityWrapper.class);	    
    private transient Object component = null;
    private transient List<ModelMBeanOperationInfo> wrapperOperations = new ArrayList<ModelMBeanOperationInfo>();
    String description = "";
    
    public ManagabilityWrapper() {
        super();
    }

    public void registerComponent(Object component, ComponentConfiguration configuration) {
        this.component = component;
        description = configuration.getDescription();
    }

    public void removeComponent() {
        this.component = null;        
    }	
	
	@Override
	public String toString() {
		return "ManagabilityWrapper for " + component.getClass();
	}

	/**
	 * Add a method to the set of exposed methods of the component
	 * that underlies this wrapper. This is done by extracting information
	 * out of a given method to create a JMX compliant instrumentation for
	 * this method.
	 * 
	 * @param method the method to be exposed
	 */
	public void addExposedMethod(Method method) {
		Descriptor desc =
            new DescriptorSupport(
                new String[] {
                    "name=" + method.getName(),
                    "descriptorType=operation",
                    "class=" + component.getClass().getCanonicalName(),
                    "role=operation"});
		
		MBeanParameterInfo[] parameters = new MBeanParameterInfo[method.getParameterTypes().length];
		
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			Class clazz = method.getParameterTypes()[i];
			parameters[i] = 
				new MBeanParameterInfo("parameter",
									   clazz.getCanonicalName(),
								       "");
		}
		
		wrapperOperations.add(
            new ModelMBeanOperationInfo(
                method.getName(),
                method.getAnnotation(Exposed.class).description(),
                parameters,
                method.getReturnType().getName(),
                MBeanOperationInfo.ACTION,
                desc)
        );                
	}	
	
	public void addExposedMethods(List<Method> methods) {
		for(Method m : methods)
			addExposedMethod(m);
	}
	
    public ModelMBeanInfo getModelMBeanInfo(ObjectName objectName) {

        Descriptor wrapperDescription =
            new DescriptorSupport(
                new String[] {
                    ("name=" + objectName),
                    "descriptorType=mbean",
                    ("displayName=ManagabilityWrapper"),
                    "type=ie.deri.wsmx.wrapper.ManagabilityWrapper",
                    "log=T",
                    "logFile=wrapper.log",
                    "currencyTimeLimit=-1" });
        
        ModelMBeanConstructorInfo[] wConstructors =
            new ModelMBeanConstructorInfo[0];

        ModelMBeanInfo cmMBeanInfo =
            new ModelMBeanInfoSupport(
                "ManagabilityWrapper",
                description,
                new ModelMBeanAttributeInfo[0],
                wConstructors,
                wrapperOperations.toArray(new ModelMBeanOperationInfo[]{}),
                new ModelMBeanNotificationInfo[0]);

        try {
            cmMBeanInfo.setMBeanDescriptor(wrapperDescription);
        } catch (Exception e) {
            logger.warn("CreateMBeanInfo failed with " + e.getMessage());
        }
        return cmMBeanInfo;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


}
