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

package ch.epfl.qosdisc.wsmx;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import ch.epfl.codimsd.qeef.*;
import ch.epfl.codimsd.qeef.relational.*;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.query.Request;
import ch.epfl.codimsd.query.RequestParameter;
import ch.epfl.codimsd.query.RequestResult;
import ch.epfl.qosdisc.database.Connection;
import ch.epfl.qosdisc.database.WSMLStore;
import ch.epfl.qosdisc.operators.*;


public class TestCoDIMS {
    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(TestCoDIMS.class);
    
    /**
     * Load the contents of the file pointed by the URL into a string.
     * 
     * @param url The URL to query.
     * @return String containing the file, or null if some error occured.
     */
    private static String loadStringFromURL(String url) {

    	try {
    		
			// Change iri as required.
			url = WSMLStore.fixIRI(url);
			
			// Read the WSML text from the file.
			BufferedReader r = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			StringBuffer buffer = new StringBuffer();
			String line;
			while((line = r.readLine()) != null) {
				buffer.append(line);
				buffer.append("\n");
			}
			r.close();
			
			return buffer.toString();
    	} catch(Exception ex) {
    		
    		ex.printStackTrace();
    	}
    	return null;
    }
    
    /**
     * Run CoDIMS test case.
     * 
     * @param goalText The goal URL
     */
    public static void runTest(String goalURL, TestFrame tf) {

		try {
			
			// Get the QueryManagerImpl singleton.
			long codimsInitTime = System.currentTimeMillis();
			QueryManagerImpl queryManagerImpl = QueryManagerImpl.getQueryManagerImpl();
			log.debug("codimsInitTime : " + (System.currentTimeMillis() - codimsInitTime));
						
			// Check configuration.
			log.debug("CODIMS_HOME: "+ch.epfl.codimsd.qeef.SystemConfiguration.getSystemConfigInfo("CODIMS_HOME"));
			
			// Specify your goal parameters here.
			int requestType = Constants.REQUEST_TYPE_SERVICE_DISCOVERY;
			RequestParameter requestParameter = new RequestParameter();
			requestParameter.setParameter(Constants.LOG_EXECUTION_PROFILE, "TRUE");
			requestParameter.setParameter(Constants.NO_DISTRIBUTION, "FALSE");
			
			// Load the goal WSML and store it in the properties
			requestParameter.setParameter("goalstring", loadStringFromURL(goalURL));
			
			// Fill requestParameter with initial properties.
			for (Map.Entry<Object, Object> p : PropertySet.props.entrySet())
				requestParameter.setParameter((String) p.getKey(), (String) p.getValue());
			
			Request request = new Request(null, requestType, requestParameter);
			@SuppressWarnings("unused")
			RequestResult result = null;

			// Execute a request.
			@SuppressWarnings("unused")
			RequestResult finalRequestResult = queryManagerImpl.executeRequest(request);
			
			finalRequestResult.getResultSet().open();
			log.debug("Found "+finalRequestResult.getResultSet().linkedList.size()+" items.");
			Tuple t = (Tuple) finalRequestResult.getResultSet().next();
			String s = t.getData(0).toString();
	    	String[] vals = s.split("\\\\");
	        // Create sorted output list.
	        log.info("Output list:");
	        Collection<InterfaceExt> srv = tf.getServices();
	    	for(int i=0; i<vals.length; i+=3) {

	    		for(InterfaceExt ie : srv) {
	    			if(ie.getInterface().getIdentifier().toString().equals(vals[i+1])) {
	    				ie.setRank((i/3)+1);
	    				ie.setRanking(Double.parseDouble(vals[i+2]));
	    			}
	    		}
	    		log.debug(""+vals[i+2]+" "+vals[i+1]);	    		
	    	}

			// Close the QueryManagerImpl.
//			long codimsShutDownTime = System.currentTimeMillis();
//			queryManagerImpl.shutdown();
//			log.debug("codimsShutDownTime : " + (System.currentTimeMillis() - codimsShutDownTime));
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }

	/**
	 * Main entry point for the codims test.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
        
		// Load the properties.
		PropertySet.setup(".");
		
		try {
			
			// Get the QueryManagerImpl singleton.
			long codimsInitTime = System.currentTimeMillis();
			QueryManagerImpl queryManagerImpl = QueryManagerImpl.getQueryManagerImpl();
			log.debug("codimsInitTime : " + (System.currentTimeMillis() - codimsInitTime));
						
			// Specify your goal parameters here.
			int requestType = Constants.REQUEST_TYPE_SERVICE_DISCOVERY;
			RequestParameter requestParameter = new RequestParameter();
			requestParameter.setParameter(Constants.LOG_EXECUTION_PROFILE, "TRUE");
			requestParameter.setParameter(Constants.NO_DISTRIBUTION, "TRUE");
			
			// Load the goal WSML and store it in the properties
			requestParameter.setParameter("goalstring", loadStringFromURL(PropertySet.props.getProperty("goal")));
			
			// Fill requestParameter with initial properties.
			for (Map.Entry<Object, Object> p : PropertySet.props.entrySet())
				requestParameter.setParameter((String) p.getKey(), (String) p.getValue());
			
			Request request = new Request(null, requestType, requestParameter);
			@SuppressWarnings("unused")
			RequestResult result = null;

			// Open connection.
			Connection.open(PropertySet.props);

			// Execute a request.
			@SuppressWarnings("unused")
			RequestResult finalRequestResult = queryManagerImpl.executeRequest(request);
			
			// Execute a second request.
			//@SuppressWarnings("unused")
//			long codimsExecTime = System.currentTimeMillis();
//			RequestResult finalRequestResult2 = queryManagerImpl.executeRequest(request);
//			log.debug("codimsExecTime : " + (System.currentTimeMillis() - codimsExecTime));
			
			// Execute a third request.
			//@SuppressWarnings("unused") 
//			RequestResult finalRequestResult3 = queryManagerImpl.executeRequest(request);
//			
//			System.out.println("ElapsedTime : " + finalRequestResult.getElapsedTime());
//			System.out.println("ElapsedTime : " + finalRequestResult2.getElapsedTime());
//			System.out.println("ElapsedTime : " + finalRequestResult3.getElapsedTime());
			
			finalRequestResult.getResultSet().open();
			log.debug("Found "+finalRequestResult.getResultSet().linkedList.size()+" items.");
			Tuple t = (Tuple) finalRequestResult.getResultSet().next();
			String s = t.getData(0).toString();
	    	String[] vals = s.split("\\\\");
	        // Create sorted output list.
	        log.info("Output list:");
	    	for(int i=0; i<vals.length; i+=3) {

	    		log.debug(""+vals[i+2]+" "+vals[i+1]);	    		
	    	}

			// Close database connection.
			// Connection.close();
			
			// Close the QueryManagerImpl.
			long codimsShutDownTime = System.currentTimeMillis();
			queryManagerImpl.shutdown();
			log.debug("codimsShutDownTime : " + (System.currentTimeMillis() - codimsShutDownTime));
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
