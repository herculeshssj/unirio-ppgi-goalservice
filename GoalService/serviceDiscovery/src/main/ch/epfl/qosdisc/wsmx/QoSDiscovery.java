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

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;
import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.datastore.WsmoRepository;
import org.wsmo.execution.common.exception.ComponentException;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.core.configuration.annotation.*;
import org.wsmo.service.*;


import ie.deri.wsmx.scheduler.Environment;
import org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager;

//import ie.deri.wsmx.discovery.lightweight.LightweightDiscovery;

import ch.epfl.qosdisc.operators.*;
import ch.epfl.qosdisc.database.*;

/**
 * Interface to WSMX discovery.
 * 
 * @author Sebastian Gerlach
 */
@WSMXComponent
(
    name =   "QoSDiscovery",
    events = "DISCOVERY"
)
public class QoSDiscovery implements IQoSDiscovery {

	/**
     * A logger for outputting silly comments, and also some potentially useful
     * information. 
     */
    static Logger logger = Logger.getLogger(QoSDiscovery.class);
	private TestFrame tf;
	private boolean isDB = false;
		
	public QoSDiscovery() {
		super();
		try {
			PropertySet.setup(System.getProperty("user.dir")+File.separator+"resources"+File.separator+"qosdiscovery");
			//Connection.open(PropertySet.props);
			
			String path = "resources"+File.separator+ "qosdiscovery";
			Properties conf = Environment.getConfiguration();
		
			if ( Environment.isCore())	
				path = Environment.getKernelLocation().getPath() + File.separator + path;
			else 
				path = System.getProperty("user.dir") + File.separator + path;
			
			tf = new TestFrame(path);
			Connection.open(PropertySet.props);
			isDB = true;
		} catch (Exception e) {
			e.printStackTrace();
			//ignore db connection error, run without database 
		}    	
	}
	
    /**
     * Old-style discovery entry point.
     *  
     * @see org.wsmo.execution.common.component.Discovery#discover(org.wsmo.service.Goal)
     */
    //@Exposed(description = "Find webservices matching a goal.")
    public List<WebService> discover(Goal goal) throws ComponentException, UnsupportedOperationException {

        // Print out a small debug message.
        logger.debug("old discover(Goal) invoked with " + goal.getIdentifier());
        try {
            
            // Create the test framework.
            TestFrame tf = new TestFrame(Environment.getKernelLocation().getAbsolutePath());
            Connection.open(PropertySet.props);

            // Get the web services from WSMX.
            WebServiceResourceManager rm = Environment.getComponentProxy(WebServiceResourceManager.class, this);
            Set<WebService> services = rm.retrieveWebServices();
            logger.debug("Retrieved services from resource manager");
                
            // Do we interface with functional discovery?
            String func = PropertySet.getProperty("functional");
            boolean functional = func!=null ? func.equals("true") : false;
            if(functional) {
                
//                logger.info("Performing functional discovery");
//
//                // Add the services to the functional discovery component.
//                LightweightDiscovery lwd = new LightweightDiscovery();
//                lwd.addWebService(services);
//                
//                // Perform the functional discovery.
//                List<WebService> funcServices = lwd.discover(goal);
//                
//                // Copy only the remaining services.
//                services.clear();
//                services.addAll(funcServices);
            } 
  
            // Add the services to our list.
            for(WebService s : services) {
                tf.addService(s);
                logger.debug("Added service "+s.getIdentifier().toString());
            }
            
            // Execute discovery component.
            List<WebService> list = tf.achieveGoalDatabase(goal);
                        
            // Return the result.
            return list;
            
        } catch (Exception ex) {
             ex.printStackTrace();
        } 
        
        return new ArrayList<WebService>();
    }

    /**
     * New-style discovery entry point.
     * 
     * @see org.wsmo.execution.common.component.Discovery#discover(org.wsmo.service.Goal, org.omwg.ontology.Ontology)
     */
    public Map<Map<WebService, Interface>, Identifier> discover(Goal goal, Ontology ont) throws ComponentException, UnsupportedOperationException {
        
        // Print out a small debug message.
        logger.debug("new discover(Goal, Ontology) invoked with " + goal.getIdentifier());
        
        return null;
    }

    
    public static void main(String[] args){
    	QoSDiscovery qos = new QoSDiscovery();
    	TopEntity[] te = Helper.parse(new File("C:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/0-GoalBuy.wsml"));
    	Goal goal = (Goal) te[0];
    	List<WebService> webservices = new ArrayList<WebService>();
    	te = Helper.parse(new File("C:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/0-WSBuy.wsml"));
    	webservices.add((WebService)te[0]);
//    	te = Helper.parse(new File("C:/WSMX/resources/qosdiscovery/ontologies/bankinter/SWS/2-WSSendSMS.wsml"));
//    	webservices.add((WebService)te[0]);
    	
    	qos.discover(goal, webservices);
    	
    }
    
    /**
     * Web-friendly entry point.
     *  
     * @see org.wsmo.execution.common.component.Discovery#discover(org.wsmo.service.Goal)
     */
    @Exposed(description = "Perform the same task as the standalone run, but invoked from WSMX. Cheap hack to get around the huge dependency list.")
    public void discover() {
        
        // Run the stand-alone test.
        TestStandalone.main(null);
    }
//    List<Interface>
    public List<WebService> discover(Goal goal, List<WebService> webservices){
		tf.removeAllServices();
    	
    	for (WebService ws : webservices){
        	tf.addService(ws);    		
    	}
    	
    	List<WebService> wss = tf.achieveGoalDatabase(goal);
    	
//    	if (isDB)
//			try {
//				Connection.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//				//ignore closing problem
//			}
    	
    	return wss;
    }
    
    
    /**
     * Add a WSMO repository for querying in addition to our internal datasource.
     * 
     * @param repository The repository to add.
     */
    public void setWsmoRepository(WsmoRepository repository) {
    	
    	WSMLStore.addWsmoRepository(repository);
    }

	/* (non-Javadoc)
	 * @see org.wsmo.execution.common.component.Discovery#discover(org.wsmo.service.Goal, java.util.Set)
	 */
	public List<WebService> discover(Goal goal, Set<WebService> searchSpace) throws ComponentException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}
    
}
