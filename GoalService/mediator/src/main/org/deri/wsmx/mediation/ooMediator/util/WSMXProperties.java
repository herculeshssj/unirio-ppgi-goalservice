package org.deri.wsmx.mediation.ooMediator.util;

/**
 * Copyright (c) 2004 National University of Ireland, Galway
 * 
 * @author Michal Zaremba
 *
 * WSMXProperties.java, 14-Apr-2004, 17:07:53
 * 
 **/

import java.util.*;
import java.io.*;

public class WSMXProperties {
    private static final String MEDIATOR_PROPERTY_FILE_NAME = "mediationdb.properties";

    private static final String DEFAULT_WSMX_SYSTEM_PROPERTY_FILE = "ie/deri/agent/util/wsmx.properties";

    private Properties props = null;

    private static WSMXProperties instance = null;

    private static String toolPath = "";

    private WSMXProperties() {
        String propertyFileName = toolPath + File.separator + MEDIATOR_PROPERTY_FILE_NAME;
        //Properties tempProps = new Properties(defaultProperties());

        try {
            //propertyFileName = System.getProperty(MEDIATOR_PROPERTY_FILE_NAME, propertyFileName);
            InputStream is = new FileInputStream(propertyFileName);
            props = new Properties();
            props.load(is);
            //tempProps.load(is);
            is.close();

        }
        catch (Exception e) {
            System.out.println("Unable to read resource file to get data source");
            return;
        }
 
    }

    public String getProperty(String propertyName) {
        if (props == null) {
            return "";
        }
        return props.getProperty(propertyName);
    }
    
    public void setProperty(String propertyName, String value){
    	if (props == null)
    		return;
    	props.setProperty(propertyName, value);
    }

    public void saveProperties(){
    	String propertyFileName = toolPath + File.separator + MEDIATOR_PROPERTY_FILE_NAME;
    	OutputStream os = null;
		try {
			os = new FileOutputStream(propertyFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	try {
			props.store(os, "User defined");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			os.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
    }
    
    public String getProperty(String propertyName, String def) {
        if (props == null) {
            return "";
        }
        return props.getProperty(propertyName, def);
    }

    private Properties defaultProperties() {
        Properties defaultProps = new Properties();

        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(DEFAULT_WSMX_SYSTEM_PROPERTY_FILE);

            if (is == null) {
                throw new Exception("Property file " + DEFAULT_WSMX_SYSTEM_PROPERTY_FILE + " not found in classpath");
            }
            defaultProps.load(is);
            is.close();
        }
        catch (Exception e) {
            // e.printStackTrace();
            // System.err.println("Ignoring " +
            // DEFAULT_AGENT_SYSTEM_PROPERTY_FILE
            // + " due to exception " + e.toString());
        }
        return defaultProps;
    }

    public static void setPath(String theToolPath) {
        toolPath = theToolPath;
    }

    public static WSMXProperties getInstance() {
        if (instance == null) {
            instance = new WSMXProperties();
        }
        return instance;
    }

}
