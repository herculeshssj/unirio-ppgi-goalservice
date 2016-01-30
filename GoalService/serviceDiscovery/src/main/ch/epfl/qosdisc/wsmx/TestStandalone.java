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
import org.wsmo.service.*;
import org.wsmo.common.*;

//import ie.deri.wsmx.discovery.lightweight.LightweightDiscovery;

import ie.deri.wsmx.scheduler.Environment;

import java.io.File;
import java.util.*;

import ch.epfl.qosdisc.database.*;
import ch.epfl.qosdisc.operators.TestFrame;
import ch.epfl.qosdisc.operators.PropertySet;

/**
 * Standalone testing class. This class allows testing of the orginial 
 * D4.18 specification for the first prototype. The configuration file
 * needs to be updated according to the comments it contains.
 * 
 * @author Sebastian Gerlach
 */
public class TestStandalone {

	static Logger log = Logger.getLogger(TestStandalone.class);
	
    /**
     * A main function for standalone testing.
     * 
     * @param args The command line arguments.
     */
    public static void main(String[] args) {

        // Run the stand-alone test.
        TestFrame tf = new TestFrame(System.getProperty("user.dir")+File.separator+"resources"+File.separator+"qosdiscovery");
//        System.setProperty("user.dir", System.getProperty("user.dir") + File.separator+"resources"+File.separator+"qosdiscovery");
//    	TestFrame tf = new TestFrame(Environment.getKernelLocation()+File.separator+"resources"+File.separator+"qosdiscovery");
        
        Goal goal = null;
    	// Open database connection.
    	try {
			Connection.open(PropertySet.props);
//			String goalIRI = PropertySet.getProperty("goal");
	    	String goalIRI = "file:///E:/work/EclipseWorkspace/WSMX/bin/resources/qosdiscovery/ontologies/Lite/ebankingGoals/0-GoalBuy.wsml";

	    	System.out.println(goalIRI);
	    	
	    	//remover goal from cache
//	    	WSMLStore.removeEntities(((IRI)goal.getIdentifier()).getNamespace());
	    	
	    	// Load a goal
	    	Collection<TopEntity> ents = WSMLStore.getEntities(goalIRI, WSMLStore.GOAL, true);
	    	// Check that we have only a single goal specified. 
	    	if(ents.size() != 1) {
	    		throw new Exception("The provided IRI specifies "+ents.size()+" goals, a single one was expected.");
	    	}
	    	goal = (Goal)ents.iterator().next();


//	    	 Add the services.
//	        Set<WebService> services = new HashSet<WebService>();
//	        for(int i = 1;;i++) {
//	            String sn = PropertySet.getProperty("service"+i);
//	            if(sn == null)
//	                break;
//	            
//	            Collection<TopEntity> e = WSMLStore.getEntities(sn);
//	            if(e != null)
//	            	services.add((WebService)e.iterator().next());
//	        }
	        
        	Collection<WebService> services = WSMLStore.getAllWebServices();
        	log.debug("Services in candidates list: "+services.size());
            for(WebService s : services)
                tf.addService(s);
	        
	        // Do we interface with functional discovery?
//	        if(PropertySet.getProperty("functional","false").equals("true")) 
//	        {
//	            try {
//	                // Add the services to the functional discovery component.
//	                LightweightDiscovery lwd = new LightweightDiscovery();
//	                lwd.addWebService(services);
//	                
//	                // Perform the functional discovery.
//	                List<WebService> funcServices = lwd.discover(goal);
//	                
//	                // Copy only the remaining services.
//	                services.clear();
//	                services.addAll(funcServices);
//	            } catch(Exception ex) {
//	                ex.printStackTrace();
//	            }
//	        }
	        
	        // Add the services to the test frame.
	        for(WebService s : services)
	            tf.addService(s);
	        
	        // And find the matching services.
	        tf.achieveGoalDatabase(goal);
	    	
	        Connection.close();
	        
    	} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
}

}
