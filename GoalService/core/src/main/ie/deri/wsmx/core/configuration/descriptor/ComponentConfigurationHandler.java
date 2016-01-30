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

package ie.deri.wsmx.core.configuration.descriptor;

import ie.deri.wsmx.core.codebase.ComponentClassLoader;
import ie.deri.wsmx.core.configuration.ComponentConfiguration;
import ie.deri.wsmx.core.configuration.DomainConfiguration;
import ie.deri.wsmx.exceptions.WSMXConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX ContentHandler for WSMX XML configuration files
 * with the Component Configuration Format.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/configuration/descriptor/ComponentConfigurationHandler.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-09-21 16:28:56 $
 */ 
public class ComponentConfigurationHandler extends DefaultHandler {

	private static final String COMPONENT = "component";
	private static final String DOMAIN = "domain";
	private static final String NAME = "name";
	private static final String MBEAN = "mbean";
    private static final String NOTIFICATIONFILTER = "notificationfilter";
	private static final String EVENTS = "events";
    private static final String NOTIFICATIONS = "notifications";
    private static final String PROPERTY = "property";
	private static final String CLASS = "class";
	private static final String START = "start";
	private static final String STOP = "stop";

	static Logger logger = Logger.getLogger(ComponentConfigurationHandler.class);

	private String contextCodebase;
	private ComponentClassLoader componentLoader;
	private List<ComponentConfiguration> components;
	private DomainConfiguration domainConfiguration;
	private ComponentConfiguration mBeanConfiguration;
	private String propertyName;
	private StringBuffer buffer = new StringBuffer();
	
	/**
	 * @param contextCodebase
	 */
	public ComponentConfigurationHandler(ComponentClassLoader componentLoader, String contextCodebase) {
		super();
		this.componentLoader = componentLoader;
		this.contextCodebase = contextCodebase;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String lName, String qName,
			Attributes attributes) throws SAXException {
		if (COMPONENT.equals(lName)) {
			components = new ArrayList<ComponentConfiguration>();
			
		} else if (DOMAIN.equals(lName)) {
			String name = attributes.getValue(NAME);
			if (name == null)
				throw new SAXException("Domain name invalid.");
			domainConfiguration = new DomainConfiguration(name);

		} else if (MBEAN.equals(lName)) {
			String name = attributes.getValue(NAME);
			String className = attributes.getValue(CLASS);
			String startName = attributes.getValue(START);
			String stopName = attributes.getValue(STOP);
			String filter = attributes.getValue(NOTIFICATIONFILTER);
			String eventString = attributes.getValue(EVENTS);
            String notificationString = attributes.getValue(NOTIFICATIONS);
			List<String> eventClasses = new ArrayList<String>();
			List<String> notificationClasses = new ArrayList<String>();
            
            try {
                eventClasses = extractValuesFromCSV(eventString);
            } catch(WSMXConfigurationException wce){
                logger.warn("MBean " + name + " has an invalid events attribute. Use comma-separated strings in dot notation.");
            }
            
            try {
                notificationClasses = extractValuesFromCSV(notificationString);
            } catch(WSMXConfigurationException wce){
                logger.warn("MBean " + name + " has an invalid events attribute. Use comma-separated strings in dot notation.");
            }

			
			if (name == null)
				throw new SAXException("Component name invalid.");
			if (className == null)
				throw new SAXException("Component class invalid.");

			// filter may be null and eventClasses may be empty
			try {
				mBeanConfiguration = new ComponentConfiguration(className, contextCodebase, componentLoader, startName, stopName, name, filter, eventClasses, notificationClasses);
			} catch (SecurityException e) {
				throw new SAXException("Failed to create component configuration due to security restrictions.", e);
			} catch (ClassNotFoundException e) {
				throw new SAXException("Failed to create component configuration because the component class could not be found.", e);
			} catch (NoSuchMethodException e) {
				throw new SAXException("Failed to create component configuration because taged method couldn't be found.", e);
			}
		} else if (PROPERTY.equals(lName)) {
			propertyName = attributes.getValue(NAME);
			buffer.delete(0, buffer.length());
		}
	}



    /**
     * Extract the values from a list of comma seperated values(CVS).
     * 
     * @param csv the CSV
     */
    private List<String> extractValuesFromCSV(String csv) throws WSMXConfigurationException {
        List<String> values = new ArrayList<String>();
        if (csv != null) {
            //event types may contain [a-zA-Z_0-9] and . and have to start with one of [a-zA-Z_0-9]
            //the events entry may contains multiple, comma separated event types, whitespace is ignored
            Matcher m0 = Pattern.compile("\\s*(\\w[\\w\\.]*)((\\s*,\\s*\\w[\\w\\.]*)*)\\s*").matcher(csv);
            if (m0.matches()) {
                values.add(m0.group(1));
                //possessive quantifier
        	    Matcher m1 = Pattern.compile("\\s*,\\s*(\\w[\\w\\.]*+)").matcher(m0.group(2));
        	    while (m1.find()) {
                    values.add(m1.group(1));
        	    }
            } else
                throw new WSMXConfigurationException("Invalid CSV. Use comma-separated strings in dot notation.");
        }
        return values;
    }


    /* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String lName, String qName) 
			throws SAXException {
		if (DOMAIN.equals(lName)) {
			domainConfiguration = null;
		} else if (MBEAN.equals(lName)) {
			domainConfiguration.appendMBean(mBeanConfiguration);
			components.add(mBeanConfiguration);
			mBeanConfiguration = null;
		} else if (PROPERTY.equals(lName)) {
			mBeanConfiguration.appendProperty(propertyName , buffer.toString().trim());
			propertyName = null;
		}
	}


	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) {
		int lhs, rhs;
		//trim whitespace on both sides
		for (lhs = start; lhs < start + length; lhs++) {
			if (!Character.isWhitespace(ch[lhs]))
				break;
		}
		for (rhs = start + length; rhs > lhs; rhs--) {
			if (!Character.isWhitespace(ch[rhs-1]))
				break;
		}
			
		if (lhs != rhs) {
			String piece = new String(ch, lhs, rhs - lhs);
			if (lhs != start)
				buffer.append(' ');
			buffer.append(piece);
			if (rhs != start + length)
				buffer.append(' ');
		}			
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(SAXParseException e) throws SAXException {
		// for now we tread validation errors as fatal
		// TODO recover when possible
		throw e;
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	@Override
	public void warning(SAXParseException e) throws SAXException {
		logger.warn("Warning: Line: " +  e.getLineNumber() +
				" SID: " + e.getSystemId() + "\n" + e.getMessage());
	}
	
	/**
	 * @return Returns the KernelConfiguration.
	 */
	public List<ComponentConfiguration> getComponentConfigurations() {
		return components;
	}
	

}

