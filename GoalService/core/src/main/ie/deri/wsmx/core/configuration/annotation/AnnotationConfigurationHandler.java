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

package ie.deri.wsmx.core.configuration.annotation;

import ie.deri.wsmx.core.codebase.ComponentClassLoader;
import ie.deri.wsmx.core.configuration.ComponentConfiguration;
import ie.deri.wsmx.core.configuration.DomainConfiguration;
import ie.deri.wsmx.exceptions.WSMXConfigurationException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 18.04.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/configuration/annotation/AnnotationConfigurationHandler.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-09-21 16:28:55 $
 */
public class AnnotationConfigurationHandler  {

	ComponentClassLoader classLoader = null;
	Class<?> componentClass = null;
    
    static Logger logger = Logger.getLogger(AnnotationConfigurationHandler.class);

	public AnnotationConfigurationHandler(ComponentClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}

	public List<ComponentConfiguration> load() throws WSMXConfigurationException {
		List<ComponentConfiguration> components = new ArrayList<ComponentConfiguration>();
		Manifest manifest = classLoader.getManifest();		
		Attributes attributes = manifest.getMainAttributes();	

		if (!attributes.containsKey(Attributes.Name.MAIN_CLASS))
			throw new WSMXConfigurationException("Manifest does not point to the main class of the component archive.");
		String componentClassName = attributes.getValue(Attributes.Name.MAIN_CLASS);
		componentClassName = componentClassName.replace('/','.'); //support slashstyle notation as well as dotstyle
		try {
			componentClass = classLoader.loadClass(componentClassName);
		} catch (ClassNotFoundException cnfe){
			throw new WSMXConfigurationException("Main class of component cannot be loaded.", cnfe);
		}
		if (!componentClass.isAnnotationPresent(WSMXComponent.class))
			throw new WSMXConfigurationException("Main class of the component is not annotated.");
		
		WSMXComponent annotation = componentClass.getAnnotation(WSMXComponent.class);
		DomainConfiguration domain = new DomainConfiguration(annotation.domain());
		
		String filter = annotation.notificationFilter();
		if (filter.equals(WSMXComponent.NO_NOTIFICATION_FILTER))
			filter = null;

		List<String> events = Arrays.asList(annotation.events());
		if (events.get(0).equals(WSMXComponent.NO_EVENTS))
			events = new ArrayList<String>();
			
		List<String> notifications= Arrays.asList(annotation.notifications());
		if (notifications.get(0).equals(WSMXComponent.NO_NOTIFICATIONS))
			notifications = new ArrayList<String>();	

		Method startMethod = getStartupMethod(componentClass);
		Method stopMethod = getStopMethod(componentClass);
		List<Method> exposedMethods = getExposedMethods(componentClass);
		
        logger.debug(componentClassName + " annotates " + startMethod + " as startup and " + stopMethod + " as shutdown procedures.");
		logger.debug(componentClassName + " annotates " + exposedMethods + " as exposed methods.");
        
		String description = annotation.description();
		
		ComponentConfiguration mbean = null;
		try {
			mbean = new ComponentConfiguration(componentClass,
									   classLoader.getCodebase(),
									   classLoader,
									   startMethod,
									   stopMethod,
									   annotation.name(),
									   filter,	   
									   events,
									   notifications,
									   domain,
									   description);
		} catch (ClassNotFoundException e) {
			logger.warn("Invalid notification filter class.", e);
			throw new WSMXConfigurationException("Invalid notification filter class.", e);
		}

		if (exposedMethods.size() > 0)
			mbean.addExposedMethods(exposedMethods);
		
		components.add(mbean);
		return components;
	}

	private List<Method> getExposedMethods(Class clazz) {
		List<Method> exposedMethods = new ArrayList<Method>();
		for (Method m : clazz.getMethods()) {
			if (m.getAnnotation(Exposed.class) != null){
                exposedMethods.add(m);
            }
        }
		return exposedMethods;
	}

	private Method getStopMethod(Class clazz) {
		for (Method m : clazz.getMethods()) {
            if (m.getAnnotation(Stop.class) != null){
                return m;
            }
        }
		return null;
	}

	private Method getStartupMethod(Class clazz) {
		for (Method m : clazz.getMethods()) {
            if (m.getAnnotation(Start.class) != null)
                return m;
        }
		return null;
	}

}
