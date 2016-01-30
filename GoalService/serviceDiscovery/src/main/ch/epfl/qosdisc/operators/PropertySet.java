/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ch.epfl.qosdisc.operators;

import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The global properties used by all components. These are typically read from the qosdisc.properties
 * file.
 * 
 * @author Sebastian Gerlach
 */
public class PropertySet {
    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(PropertySet.class);
    
	/**
	 * The global property set.
	 */
	public static Properties props = null;
	
	/**
	 * Reference path to use as a last resort.
	 */
	private static String refPath = ".";
	
	/**
	 * Open a file using various search paths.
	 * 
	 * @param file Base filename to look for.
	 * @return An InputStream for the file if it could be opened, null otherwise.
	 */
	public static InputStream openFile(String file) {
		
        try {
        	
        	// Print something for fun.
        	log.debug("Attempting to load file "+file);
            
            // First try from our class loader.
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
            
            // And then from the reference path.
            if(is == null)
                is = new FileInputStream(refPath+"/"+file);
            
            // And finally die with an error message - does not go to log4j since it is not configured here yet.
            if(is == null) {
                log.warn("Could not load "+file+". This will lead to dubious functionality.");
            }
            else
            	return is;
        } catch(Exception ex) {
        	
        	log.warn("Could not open file "+file+".");
            //ex.printStackTrace();
        }
        
        return null;
	}
	
    /**
     * Constructor. 
     */
    public static void setup(String refPath) {
        
    	// Check whether this has already been done.
    	if(props != null)
    		return;
    	
    	// Store reference path.
    	PropertySet.refPath = refPath;
    	
        // Load properties.
        props = new Properties();
       	InputStream is = openFile("qosdisc.properties");
       	if(is != null) {
       		try {
       			props.load(is);
       		} catch(IOException ex) {
       			ex.printStackTrace();
       		}
       	} else 
        	log.warn("Could not load property file.");
        
        // Configure log4j.
        PropertyConfigurator.configure(props);
    }
    
    /**
     * Get path to class root.
     * 
     * @return The path, with a trailing /.
     */
    public static String getPath() {
		
//    	if( Thread.currentThread()==null || Thread.currentThread().getContextClassLoader()==null ||
//    		Thread.currentThread().getContextClassLoader().getResource("./")==null ||
//    		Thread.currentThread().getContextClassLoader().getResource("./").getPath()==null)
    		return PropertySet.refPath+"/";
//    	String path = Thread.currentThread().getContextClassLoader().getResource("./").getPath();
//		if(path.charAt(0)=='/')
//			path = path.substring(1);
//		return path;
    }
    
    /**
     * Shorthand for props.getProperty().
     * 
     * @param propName Name of property to query.
     * @return Value of property, or null if it doesn't exist.
     */
    public static String getProperty(String propName) {
    	
    	return props.getProperty(propName);
    }
    
    /**
     * Shorthand for props.getProperty().
     * 
     * @param propName Name of property to query.
     * @param defaultValue Default value to return if property is not set.
     * @return Value of property, or defaultValue if it doesn't exist.
     */
    public static String getProperty(String propName, String defaultValue) {
    	
    	return props.getProperty(propName, defaultValue);
    }
    
    /**
     * Add all properties from the property set to the global properties.
     * 
     * @param props Properties to add.
     */
    public static void setProperties(Properties props) {
    	
    	for(Map.Entry<Object, Object> p : props.entrySet())
    		PropertySet.props.setProperty((String)p.getKey(), (String)p.getValue());
    }
	
}
