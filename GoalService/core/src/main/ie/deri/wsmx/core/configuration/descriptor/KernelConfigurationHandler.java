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

import ie.deri.wsmx.core.configuration.ComponentConfiguration;
import ie.deri.wsmx.core.configuration.DomainConfiguration;
import ie.deri.wsmx.core.configuration.KernelConfiguration;
import ie.deri.wsmx.exceptions.WSMXConfigurationException;

import java.io.File;
import java.io.IOException;
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
 * with the Kernel Configuration Format.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: maciejzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/configuration/descriptor/KernelConfigurationHandler.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2006-01-25 16:24:09 $
 */ 
public class KernelConfigurationHandler extends DefaultHandler {

	private static final String KERNEL = "kernel";
	private static final String DOMAIN = "domain";
	private static final String NAME = "name";
	private static final String MBEAN = "mbean";
	private static final String SYSTEMCODEBASE = "systemcodebase";
	private static final String COMPONENTCODEBASE = "componentcodebase";
	private static final String HTTP_PORT = "httpport";
	private static final String AXIS_PORT = "httpport";
	private static final String SSH_PORT = "sshport";
    private static final String SPACEADDRESS = "spaceaddress";
    private static final String NOTIFICATIONFILTER = "notificationfilter";
	private static final String EVENTS = "events";
    private static final String NOTIFICATIONS = "notifications";
    private static final String PROPERTY = "property";
	private static final String CLASS = "class";
	private static final String WSMX_HOME_ENVVAR = "WSMX_HOME";

	static Logger logger = Logger.getLogger(KernelConfigurationHandler.class);

	private KernelConfiguration kernelConfiguration;
	private DomainConfiguration domainConfiguration;
	private ComponentConfiguration mBeanConfiguration;
	private String systemCodebase;
	private String spaceAddress;
    private int httpPort;
    private int axisPort;
    private int sshPort;
	private String propertyName;
	private StringBuffer buffer = new StringBuffer();
	

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String lName, String qName,
			Attributes attributes) throws SAXException {
		if (KERNEL.equals(lName)) {
			handleKernelConfiguration(attributes);
		} else if (DOMAIN.equals(lName)) {
			handleDomainConfiguration(attributes);
		} else if (MBEAN.equals(lName)) {
			handleMBeanConfiguration(attributes);
		} else if (PROPERTY.equals(lName)) {
			handlePropertyConfiguration(attributes);
		}
	}

	private void handlePropertyConfiguration(Attributes attributes) {
		propertyName = attributes.getValue(NAME);
		buffer.delete(0, buffer.length());
	}



	private void handleDomainConfiguration(Attributes attributes) throws SAXException {
		String name = attributes.getValue(NAME);
		if (name == null)
			throw new SAXException("Domain name invalid.");
		domainConfiguration = new DomainConfiguration(name);
	}



	private void handleMBeanConfiguration(Attributes attributes) throws SAXException {
		String name = attributes.getValue(NAME);
		String className = attributes.getValue(CLASS);
		String codeBase = attributes.getValue(COMPONENTCODEBASE);
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
			throw new SAXException("MBean name invalid.");
		if (className == null)
			throw new SAXException("MBean class invalid.");
		if (codeBase == null) {
			logger.warn("Componentcodebase of " + name + " is null. Fallback to systemcodebase.");
		    codeBase = systemCodebase;
		}
		// filter may be null and eventClasses may be empty
		try {
			mBeanConfiguration = new ComponentConfiguration(className, codeBase,null, name, filter, eventClasses, notificationClasses);
		} catch (ClassNotFoundException e) {
			throw new SAXException("The given classes are not valid because not loadable.", e);
		}
	}



	private void handleKernelConfiguration(Attributes attributes) throws SAXException {
		systemCodebase = attributes.getValue(SYSTEMCODEBASE);
		spaceAddress = attributes.getValue(SPACEADDRESS);

		if (systemCodebase == null) {
		    // if we don't have an explicit systemcodebase, we attempt to fallback to a defaultcodebase
		    logger.warn("Explicit systemcodebase is not specified.");
		    systemCodebase = fallback();
		} else if (!new File(systemCodebase).canRead()) {
		    // same if we can't read from it
		    logger.warn("Explicit systemcodebase " + systemCodebase +" is not readable.");
		    systemCodebase = fallback();			    
		}

		if (spaceAddress == null) {
		    // if we don't have an explicit systemcodebase, we attempt to fallback to a defaultcodebase
		    logger.warn("Spaceaddress not specified. Fallback to localhost.");
		    spaceAddress = "localhost";
		}
		
		logger.info("Systemcodebase is \"" + systemCodebase + "\".");
		    
		String portString = attributes.getValue(HTTP_PORT);
		
		if (portString == null) {
		    logger.warn("HTTP port not specified. Fallback to defaultport 8080.");
			httpPort = 8080;
		} else {
		    try {
		    	httpPort = Integer.parseInt(portString);
			} catch (NumberFormatException nfe) {
			    logger.warn("Specified HTTP port " + portString + " is invalid. Fallback to default port 8080.", nfe);	
			    httpPort = 8080;
			}
		}
		
		String portAxisString = attributes.getValue(AXIS_PORT);
		
		if (portAxisString == null) {
		    logger.warn("Axis port not specified. Fallback to defaultport 8050.");
			axisPort = 8050;
		} else {
		    try {
		    	axisPort = Integer.parseInt(portAxisString);
			} catch (NumberFormatException nfe) {
			    logger.warn("Specified Axis port " + portString + " is invalid. Fallback to default port 8050.", nfe);	
			    axisPort = 8050;
			}
		}		
		
		String sshPortString = attributes.getValue(SSH_PORT);
		
		if (sshPortString == null) {
		    logger.warn("SSH port not specified. Fallback to defaultport 22.");
			sshPort = 22;
		} else {
		    try {
		    	sshPort = Integer.parseInt(sshPortString);
			} catch (NumberFormatException nfe) {
			    logger.warn("Specified SSH port " + sshPortString + " is invalid. Fallback to default port 22.", nfe);	
			    sshPort = 22;
			}
		}
		kernelConfiguration = new KernelConfiguration(new File(systemCodebase), spaceAddress, httpPort, axisPort, sshPort);
	}



    /**
     * Extract the values from a list of comma seperated values(CSV).
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



    /**
     * Attempts to get a valid systemcodebase
     * that is not <code>null</code> and readable.
     * It tries the WSMX_HOME environment variable first
     * and the current path next.
     * 
     * @return the systemcodebase path
     * @throws SAXException if all backfalling fails
     */
    private String fallback() throws SAXException {
        String systemCodebase;
        if ((systemCodebase = primaryFallback()) == null)
            if((systemCodebase = secondaryFallback()) == null)
    			throw new SAXException("There's no explicit systemcodebase specified, WSMX_HOME was not set and automatic recovery failed");
        return systemCodebase;
    }


    private String primaryFallback() {
        logger.warn("Attempting to recover and fall back to the WSMX_HOME environment variable.");
        
        if(System.getProperties().containsKey(WSMX_HOME_ENVVAR)) {
            //if the environment variable WSMX_HOME is there we use it
        	//this needs an environment variable that is handed to the 
            //JVM at startup using the -D parameter:
            //java -DWSMX_HOME=$WSMX_HOME [...]
        	File environment = new File(System.getProperty(WSMX_HOME_ENVVAR));
        	if (!environment.canRead()) {
        	    logger.warn("WSMX_HOME does not point to a valid location that is readable.");
        	    return null;
        	}           
        	try {
        	    return environment.getCanonicalPath();
        	} catch (IOException ioe) {
                logger.warn("Primary fallback failed.", ioe);
        	    return null;
        	}
        }
		logger.warn("Primary fallback failed: WSMX_HOME environment variable is not set. " +
					"The JVM needs to be handed OS specific environment variables using -D.");
		return null;     					
    }

    private String secondaryFallback() {
        //if we don't have WSMX_HOME, we try to use to current path
        logger.warn("Attempting to recover and fall back to the current path.");
        String current = null;
        try {
            File currentFile = new File(".");
            current = currentFile.getCanonicalPath();
            if (!currentFile.canRead()) {
        	    logger.warn("The current path is not readable.");
        	    return null;                
            }
            
        } catch (IOException ioe) {
            logger.fatal("Secondary fallback failed.", ioe);
            return null;
        }
        return current;
    }

    /* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String lName, String qName) 
			throws SAXException {
		if (DOMAIN.equals(lName)) {
			kernelConfiguration.appendDomain(domainConfiguration);
			domainConfiguration = null;
		} else if (MBEAN.equals(lName)) {
			domainConfiguration.appendMBean(mBeanConfiguration);
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
	public KernelConfiguration getKernelConfiguration() {
		return kernelConfiguration;
	}
	
	/**
	 * @param configuration The KernelConfiguration to set.
	 */
	public void setKernelConfiguration(KernelConfiguration configuration) {
		kernelConfiguration = configuration;
	}
}

