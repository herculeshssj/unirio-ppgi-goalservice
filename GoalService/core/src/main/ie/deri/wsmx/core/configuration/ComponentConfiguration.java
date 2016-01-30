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

package ie.deri.wsmx.core.configuration;

import ie.deri.wsmx.core.codebase.ComponentClassLoader;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Represent the configuration state of a single component.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/configuration/ComponentConfiguration.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.10 $ $Date: 2005-10-21 03:54:35 $
 */ 
public class ComponentConfiguration implements Serializable {

	private static final long serialVersionUID = 5942799784794937915L;
	private DomainConfiguration domain;
	private transient final ComponentClassLoader componentLoader;
	private final Class clazz;
	private final File codeBase;
	private transient final Method startMethod;
	private transient final Method stopMethod;	
	private final String name;
	private final Class notificationFilter;
	private final List<String> events;
    private final List<String> notifications;
    private final String description;
	private transient List<Method> exposedMethods = new ArrayList<Method>();


	private Map<String, String> properties = new LinkedHashMap<String, String>();	
	
	/**
	 * Constructs a configuration for a single MBean.
	 * 
	 * @param className the classname of the MBeans implementation
	 * @param codeBase the archive where the implementation can be found
	 * @param name the name under which we register at the MBeanServer
	 * @param eventFilter the filter used to filter notification for this MBean
	 * @param eventTypes atomic events or classes of event this MBean subscribes to
	 * @throws ClassNotFoundException if the given class can't be loaded using the given classloader
	 */
	public ComponentConfiguration(
			final String className,
			final String codeBase,
			final ComponentClassLoader componentLoader,
			final String name,
			final String eventFilter,
			final String[] events,
            final String[] notifications) throws ClassNotFoundException {
		this(className,
			 codeBase,
			 componentLoader,
			 name,
			 eventFilter,
			 events,
             notifications,
			 null
		);
	}

    /**
     * Constructs a configuration for a single MBean.
     * 
	 * @param className the classname of the MBeans implementation
	 * @param codeBase the archive where the implementation can be found
	 * @param name the name under which we register at the MBeanServer
	 * @param eventFilter the filter used to filter notification for this MBean
	 * @param eventTypes atomic events or classes of event this MBean subscribes to
     * @throws ClassNotFoundException if the given class can't be loaded using the given classloader
     */
    public ComponentConfiguration(
    		final String className,
    		final String codeBase,
			final ComponentClassLoader componentLoader,
    		final String name,
    		final String notificationFilter,
    		final List<String> events,
            final List<String> notifications
            ) throws ClassNotFoundException {
    	this(className,
    	     codeBase,
    	     componentLoader,
    	     name,
    	     notificationFilter,
    	     events,
             notifications,
    	     null,
    	     null
    	);
    }	
	
    /**
     * Constructs a configuration for a single MBean.
     * 
	 * @param className the classname of the MBeans implementation
	 * @param codeBase the archive where the implementation can be found
	 * @param componentLoader the ClassLoader used to load the component
	 * @param startMethod the operation used to start the component
	 * @param stopMethod the operation used to stop the component
	 * @param name the name under which we register at the MBeanServer
	 * @param eventFilter the filter used to filter notification for this MBean
	 * @param eventTypes atomic events or classes of event this MBean subscribes to
	 * @throws NoSuchMethodException if the start or stop method don't exist
	 * @throws ClassNotFoundException if the given class can't be loaded using the given classloader
	 * @throws SecurityException if a security manager is present and access is denied
     */
    public ComponentConfiguration(
    		final String className,
    		final String codeBase,
			final ComponentClassLoader componentLoader,
			final String startMethod,
			final String stopMethod,
    		final String name,
    		final String notificationFilter,
    		final List<String> events,
            final List<String> notifications
            ) throws SecurityException, ClassNotFoundException, NoSuchMethodException {
    	this(className,
    	     codeBase,
    	     componentLoader,
    	     startMethod,
    	     stopMethod,
    	     name,
    	     notificationFilter,
    	     events,
             notifications,
    	     null,
    	     null
    	);
    }	

	/**
	 * Constructs a configuration for a single MBean and
	 * attaches it to a <code>DomainConfiguration</code>.
	 * 
	 * @param className the classname of the MBeans implementation
	 * @param codeBase the archive where the implementation can be found
	 * @param componentLoader the ClassLoader used to load the component
	 * @param name the name under which we register at the MBeanServer
	 * @param eventFilter the filter used to filter notification for this MBean
	 * @param eventTypes atomic events or classes of event this MBean subscribes to
	 * @param domainConfiguration the domain to attach this MBean to
	 * @throws ClassNotFoundException if the given class can't be loaded using the given classloader
	 */
	public ComponentConfiguration(
			final String className,
			final String codeBase,
			final ComponentClassLoader componentLoader,
			final String name,
			final String notificationFilter,
			final String[] events,
            final String[] notifications,
			final DomainConfiguration domainConfiguration) throws ClassNotFoundException {
		this(className,
		     codeBase,
		     componentLoader,
		     name,
		     notificationFilter,
			 //the ArrayList instantiation is needed because Arrays.asList() returns a fixed size list
		     new ArrayList<String>(Arrays.asList(events)),
             new ArrayList<String>(Arrays.asList(notifications)),
		     domainConfiguration,
		     null
		);
	}
	
	/**
	 * Constructs a configuration for a single MBean and
	 * attaches it to a <code>DomainConfiguration</code>.
	 * 
	 * @param className the classname of the MBeans implementation
	 * @param codeBase the archive where the implementation can be found
 	 * @param componentLoader the ClassLoader used to load the component
	 * @param name the name under which we register at the MBeanServer
	 * @param eventFilter the filter used to filter notification for this MBean
	 * @param eventTypes atomic events or classes of event this MBean subscribes to
	 * @param domainConfiguration the domain to attach this MBean to
	 * @throws ClassNotFoundException if the given class can't be loaded using the given classloader
	 */
	public ComponentConfiguration(
			final String className,
			final String codeBase,
			final ComponentClassLoader componentLoader,
			final String name,
			final String notificationFilter,
			final List<String> events,
            final List<String> notifications,
			final DomainConfiguration domainConfiguration,
			final String description) throws ClassNotFoundException {
		this(getClass(componentLoader, className),
			     new File(codeBase),
				 componentLoader,
				 null,
				 null,
				 name,
				 getClass(componentLoader, notificationFilter),
				 events,
				 notifications,
				 domainConfiguration,
				 description);
	}

	
	/**
	 * Constructs a configuration for a single MBean and
	 * attaches it to a <code>DomainConfiguration</code>.
	 * 
	 * @param className the classname of the MBeans implementation
	 * @param codeBase the archive where the implementation can be found
	 * @param componentLoader the ClassLoader used to load the component
	 * @param startMethod the operation used to start the component
	 * @param stopMethod the operation used to stop the component
	 * @param name the name under which we register at the MBeanServer
	 * @param eventFilter the filter used to filter notification for this MBean
	 * @param eventTypes atomic events or classes of event this MBean subscribes to
	 * @param domainConfiguration the domain to attach this MBean to
	 * @throws NoSuchMethodException if the start or stop method don't exist
	 * @throws ClassNotFoundException if the given class can't be loaded using the given classloader
	 * @throws SecurityException if a security manager is present and access is denied
	 */
	public ComponentConfiguration(
			final String className,
			final String codeBase,
			final ComponentClassLoader componentLoader,
			final String startMethod,
			final String stopMethod,
			final String name,
			final String notificationFilter,
			final List<String> events,
            final List<String> notifications,
			final DomainConfiguration domainConfiguration,
	        final String description) throws SecurityException, ClassNotFoundException, NoSuchMethodException {
		this(getClass(componentLoader, className),
		     new File(codeBase),
			 componentLoader,
			 getMethod(className, componentLoader, startMethod),
			 getMethod(className, componentLoader, stopMethod),
			 name,
			 getClass(componentLoader, notificationFilter),
			 events,
			 notifications,
			 domainConfiguration,
			 description);
	}

	/**
	 * @param className
	 * @param componentLoader
	 * @param startMethod
	 * @return
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	private static Method getMethod(final String className, final ComponentClassLoader componentLoader, final String startMethod) throws NoSuchMethodException, ClassNotFoundException {
		if (startMethod != null)
			return getClass(componentLoader, className).getMethod(startMethod, new Class[]{});
		return null;
	}

	/**
	 * Constructs a configuration for a single MBean and
	 * attaches it to a <code>DomainConfiguration</code>.
	 * 
	 * @param class the class of the MBeans implementation
	 * @param codeBase the archive where the implementation can be found
	 * @param componentLoader the ClassLoader used to load the component
	 * @param startMethod the operation used to start the component
	 * @param stopMethod the operation used to stop the component
	 * @param name the name under which we register at the MBeanServer
	 * @param eventFilter the filter used to filter notification for this MBean
	 * @param eventTypes atomic events or classes of event this MBean subscribes to
	 * @param domainConfiguration the domain to attach this MBean to
	 * @throws ClassNotFoundException if the class of the notificationf filter cannot be loaded using the given classloader
	 */
	public ComponentConfiguration(
			final Class componentClass,
			final File codeBase,
			final ComponentClassLoader componentLoader,
			final Method startMethod,
			final Method stopMethod,
			final String name,
			final String notificationFilter,
			final List<String> events,
            final List<String> notifications,
			final DomainConfiguration domainConfiguration,
	        final String description) throws ClassNotFoundException {
		this(componentClass,
			 codeBase,
			 componentLoader,
			 startMethod,
			 stopMethod,
			 name,
			 getClass(componentLoader, notificationFilter),
			 events,
			 notifications,
			 domainConfiguration,
			 description);
	}

	private static Class<?> getClass(final ComponentClassLoader componentLoader, final String notificationFilter) throws ClassNotFoundException {
		if (notificationFilter == null)
			return null;
		return componentLoader.loadClass(notificationFilter);
	}
	
	/**
	 * Constructs a configuration for a single MBean and
	 * attaches it to a <code>DomainConfiguration</code>.
	 * 
	 * @param class the class of the MBeans implementation
	 * @param codeBase the archive where the implementation can be found
	 * @param componentLoader the ClassLoader used to load the component
	 * @param startMethod the operation used to start the component
	 * @param stopMethod the operation used to stop the component
	 * @param name the name under which we register at the MBeanServer
	 * @param eventFilter the filter used to filter notification for this MBean
	 * @param eventTypes atomic events or classes of event this MBean subscribes to
	 * @param domainConfiguration the domain to attach this MBean to
	 */
	public ComponentConfiguration(
			final Class componentClass,
			final File codeBase,
			final ComponentClassLoader componentLoader,
			final Method startMethod,
			final Method stopMethod,
			final String name,
			final Class notificationFilter,
			final List<String> events,
            final List<String> notifications,
			final DomainConfiguration domainConfiguration,
	        final String description) {
		super();
		this.clazz = componentClass;
		this.codeBase = codeBase;
		this.componentLoader = componentLoader;
		this.startMethod = startMethod;
		this.stopMethod = stopMethod;
		this.name = name;
		this.notificationFilter = notificationFilter;
		if (events != null)
		    this.events = events;
		else
		    // we prefer to be empty rather than null
		    this.events = new ArrayList<String>(); 
        if (notifications != null)
            this.notifications = notifications;
        else
            // we prefer to be empty rather than null
            this.notifications = new ArrayList<String>();
        appendToDomainConfiguration(domainConfiguration);
        if (description == null)
        	this.description = "";
        else 
        	this.description = description;
	}

	
	/**
	 * Returns the fully qualified java classname of this MBean.
	 * 
	 * @return the classname
	 */
	public Class<?> getComponentClass() {
		return clazz;
	}
	/**
	 * Returns the codebase for this MBean.
	 * 
	 * @return the codebase
	 */
	public File getCodeBase() {
		return codeBase;
	}

	/**
	 * @return Returns the domain.
	 */
	public DomainConfiguration getDomain() {
		return domain;	
	}
	
	
	/**
	 * Returns the name of this MBean, which is the
	 * key/value pair of the <code>ObjectName</code>
	 * with the key 'name'.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the <code>Map</code> of properties (name/value pairs)
	 * of the currently loaded configuration. Note that
	 * the returned <code>Map</code> is unmodifyable in order to
	 * properly encapsulate the internal implementation.
	 * 
	 * @return a <code>Map</code> of properties
	 */
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Appends a property to this
	 * <code>MBeanConfiguration</code>. As a delegator
	 * this method ensures encapsulation of the internal
	 * datastructure implementation that holds the properties.
	 * Note that the added properties will be part of the 
	 * <code>ObjectName</code>.
	 * 
	 * @param key the 
	 * @param value the <code>DomainConfiguration</code> to append
	 */
	public void appendProperty(String key, String value) {
		properties.put(key, value);
	}
	
	/**
	 * @return Returns the eventFilter.
	 */
	public Class getNotificationFilter() {
		return notificationFilter;
	}
	
	/**
	 * Appends this <code>MBeanConfiguration</code> to the
	 * specified <code>DomainConfiguration</code>. In order to
	 * avoid code duplication a ease maintenance this method
	 * actually calls <code>appendDomain</code> on its parameter,
	 * allowing the bidirectional link logic to be in one place.
	 * 
	 * @param domainConfiguration the <code>DomainConfiguration</code> to append to
	 */
	public void appendToDomainConfiguration(DomainConfiguration domainConfiguration) {
	    if (domainConfiguration != null)
	        domainConfiguration.appendMBean(this);
	}
	
	/**
	 * This method is used by the logic that maintains
	 * the bidirectional links between
	 * <code>DomainConfiguration</code> and
	 * <code>MBeanConfiguration</code>. It is not 
	 * intented for general use, therefore package access.
	 * 
	 * @param domainConfiguration the <code>DomainConfiguration</code> to set
	 */
    void setDomain(DomainConfiguration domain) {
        this.domain = domain;
    }

    /**
	 * Returns the <code>List</code> of event types 
	 * of the currently loaded configuration. Event types
	 * may be atomic events of classes of events. Note that
	 * the returned <code>List</code> is unmodifyable in order to
	 * properly encapsulate the internal implementation.
	 * 
	 * @return a <code>Map</code> of event types 
	 */
    public List<String> getEvents() {
        return Collections.unmodifiableList(events);
    } 
    
    /**
     * Returns the <code>List</code> of notification types 
     * of the currently loaded configuration. Notification types
     * may be atomic notification of classes of notification. Note that
     * the returned <code>List</code> is unmodifyable in order to
     * properly encapsulate the internal implementation.
     * 
     * @return a <code>Map</code> of notification types
     */
    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

	/**
	 * @return Returns the classloader.
	 */
	public ComponentClassLoader getClassloader() {
		return componentLoader;
	}

	/**
	 * @return Returns the startMethod.
	 */
	public Method getStartMethod() {
		return startMethod;
	}	

	/**
	 * @return Returns the stopMethod.
	 */
	public Method getStopMethod() {
		return stopMethod;
	}

    /**
     * Returns the <code>List</code> of exposed <code>Methods</code>
     * of the currently loaded configuration. Exposed
     * methods are made available to external managers and
     * are also invokable from within management consoles. Note that
     * the returned <code>List</code> is unmodifyable in order to
     * properly encapsulate the internal implementation.
     * 
     * @return a <code>List</code> of <code>Methods</code>
     */
	public List<Method> getExposedMethods() {
		return Collections.unmodifiableList(exposedMethods);
	}	

	/**
    * Appends a <code>Method</code> to the current <code>List</code>
    * of exposed methods. Exposed methods are made available to
    * external managers and are also invokable from within
    * management consoles.
    * 
    * @param exposedMethod a <code>Method</code> to be appended
    */
	public void addExposedMethod(Method exposedMethod) {
		this.exposedMethods.add(exposedMethod);
	}
    /**
     * Appends a list of <code>List</code> of <code>Methods</code>
     * to the current <code>List</code> of exposed methods. Exposed
     * methods are made available to external managers and
     * are also invokable from within management consoles.
     * 
     * @param exposedMethods a <code>List</code> of <code>Methods</code> to be appended
	 */
	public void addExposedMethods(List<Method> exposedMethods) {
		this.exposedMethods.addAll(exposedMethods);
	}
	    
    /**
     * Computes the object name from information in this
     * component configuration and from the context, like
     * the hosting domain configuration.
     * 
     * @return the objectname under which this component is registered at an <code>MBeanServer</code>
     * @throws MalformedObjectNameException
     */
    public ObjectName getObjectName() throws MalformedObjectNameException {
        String assembledName = getDomain().getDomainName() + ":name=" + getName();
        for(String key : getProperties().keySet())
            assembledName += "," + key + "=" + getProperties().get(key);
        ObjectName objectName = new ObjectName(assembledName);
        return objectName;
    }

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComponentConfiguration) {
			ComponentConfiguration cc = (ComponentConfiguration)obj;
			try {
				if(!getObjectName().equals(cc.getObjectName()))
					return false;
			} catch (MalformedObjectNameException e) {
				return false;
			}
			if(!getCodeBase().equals(cc.getCodeBase()))
				return false;
			return true;
		}
		return false;
	}	

}

