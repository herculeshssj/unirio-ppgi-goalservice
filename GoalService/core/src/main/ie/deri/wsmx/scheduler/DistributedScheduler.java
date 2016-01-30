/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *
 * Licensed under MIT License
 * 
 * Permission is hereby granted, free of charge, to any person 
 * obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons 
 * to whom the Software is furnished to do so, subject to the 
 * following conditions:
 *
 * The above copyright notice and this permission notice shall 
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS 
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES 
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT 
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ie.deri.wsmx.scheduler;

import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;
import ie.deri.wsmx.exceptions.TransportException;
import ie.deri.wsmx.scheduler.transport.JavaSpaceTransport;
import ie.deri.wsmx.scheduler.transport.LocalTransport;

import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.apache.log4j.Logger;

/**
 * A generic scheduler that can be customized with
 * a <code>Reviver</code> implementation and a
 * range of proxies.
 *
 * <pre>
 * Created on 06.03.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/DistributedScheduler.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.11 $ $Date: 2005-11-25 14:46:57 $
 */
public class DistributedScheduler extends AbstractScheduler {
    
	private static final long serialVersionUID = 7563097560205197892L;

	static Logger logger = Logger.getLogger(DistributedScheduler.class);
	
    private transient Object component = null;    
	private transient RequiredModelMBean schedulerMBean = null;
    private int scheduledTasks = 0;

    public DistributedScheduler() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    @Override
	public void handleNotification(Notification notification, Object object) {
        //TODO
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.scheduler.AbstractScheduler#getProxy(java.lang.String)
     */
    @Override
	public AbstractProxy getProxy(String name) {        
        return null;
    }

    @Override
	public void start(String spaceLocation, String eventType) {
        try {
			transport = new JavaSpaceTransport(spaceLocation);
		} catch (TransportException e) {
			logger.warn("Failed to get a transport handle on a space for " + component.getClass().getSimpleName() + ", falling back to a local transport.", e);
	        transport = new LocalTransport();
		}
        reviver = new GenericReviver(transport, component, this);  
		Object[] inputCriteria;
		if (eventType != null)
			inputCriteria = new Object[]{eventType};
		else
			inputCriteria = new Object[]{};
        ((GenericReviver)reviver).setInputCriteria(inputCriteria);
        reviver.start();
        new Thread(this, "Scheduler::"+ component.getClass().getSimpleName()).start();
    }

    public void logSchedule() {
        scheduledTasks += 1;
        try {
        	//FIXME how we get the objectname here, is not generic enough
        	//String name = "schedulers:name=" + component.getClass().getAnnotation(WSMXComponent.class).name();        	
        	schedulerMBean.sendNotification(new Notification("TASK_SCHEDULED", 
					this,
					1,
					component.getClass().getAnnotation(WSMXComponent.class).name()));
		} catch (NullPointerException e) {
			logger.warn("Failed to log a task schedule.", e);
		} catch (RuntimeOperationsException e) {
			logger.warn("Failed to log a task schedule.", e);
		} catch (MBeanException e) {
			logger.warn("Failed to log a task schedule.", e);
		}
    }
    
    /* (non-Javadoc)
     * @see ie.deri.wsmx.wrapper.GenericWrapperMBean#registerComponent(ie.deri.wsmx.infomodel.AbstractComponent)
     */
    public void registerComponent(Object component) {
        this.component = component;
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.wrapper.GenericWrapperMBean#removeComponent()
     */
    public void removeComponent() {
        this.component = null;
    }	
	
	@Override
	public String toString() {
		return "scheduler for " + component.getClass().getSimpleName();
	}

    public ModelMBeanInfo getModelMBeanInfo(ObjectName objectName) {
        Descriptor attributedesc =
            new DescriptorSupport(
                new String[] {
                    "name=ScheduledTasks",
                    "descriptorType=attribute",
                    "displayName=ScheduledTasks",
                    "getMethod=getScheduledTasks",
                    "currencyTimeLimit=-1"  });
                       
        ModelMBeanAttributeInfo attributeInfo[] = new ModelMBeanAttributeInfo[] { 
                    new ModelMBeanAttributeInfo(
                    "ScheduledTasks",
                    "int",
                    "The number of task that this scheduler has scheduled since instatiation.",
                    true,
                    false,
                    false,
                    attributedesc)
                };
        
        Descriptor operationdesc =
            new DescriptorSupport(
                new String[] {
                    "name=getScheduledTasks",
                    "descriptorType=operation",
                    "class=" + DistributedScheduler.class.getCanonicalName(),
                    "role=operation"});
        
        MBeanParameterInfo[] parameters = new MBeanParameterInfo[0];
               
        ModelMBeanOperationInfo operationInfo[] = new ModelMBeanOperationInfo[] { 
                    new ModelMBeanOperationInfo(
                    "getScheduledTasks",
                    "Returns the number of tasks that this scheduler has scheduled since instatiation.",
                    parameters,
                    "int",
                    MBeanOperationInfo.ACTION,
                    operationdesc)
                };
        
        Descriptor wrapperDescription =
            new DescriptorSupport(
                new String[] {
                    ("name=" + objectName),
                    "descriptorType=mbean",
                    ("displayName=DistributedScheduler"),
                    "type=ie.deri.wsmx.wrapper.DistributedScheduler",
                    "log=T",
                    "logFile=wrapper.log",
                    "currencyTimeLimit=10" });
        
        ModelMBeanConstructorInfo[] wConstructors =
            new ModelMBeanConstructorInfo[0];

/*        MBeanParameterInfo[] constructorParms =
            new MBeanParameterInfo[] {};

        Descriptor wBeanDesc =
            new DescriptorSupport(
                new String[] {
                    "name=DistributedScheduler",
                    "descriptorType=operation",
                    "role=constructor" });

        wConstructors[0] =
            new ModelMBeanConstructorInfo(
                "DistributedScheduler",
                "Creates a new DistributedScheduler instance.",
                constructorParms,
                wBeanDesc);
*/
        ModelMBeanInfo cmMBeanInfo =
            new ModelMBeanInfoSupport(
                "DistributedScheduler",
                "The local scheduling fragment for " + component.getClass().getSimpleName() +
                " of the distributed, instance-global scheduler.",
                attributeInfo,
                wConstructors,
                operationInfo,
                new ModelMBeanNotificationInfo[0]);

        try {
            cmMBeanInfo.setMBeanDescriptor(wrapperDescription);
        } catch (Exception e) {
            logger.warn("CreateMBeanInfo failed with " + e.getMessage());
        }
        return cmMBeanInfo;
    }

	public RequiredModelMBean getSchedulerMBean() {
		return schedulerMBean;
	}

	public void setSchedulerMBean(RequiredModelMBean schedulerMBean) {
		this.schedulerMBean = schedulerMBean;
	}

    public int getScheduledTasks() {
        return scheduledTasks;
    }


}
