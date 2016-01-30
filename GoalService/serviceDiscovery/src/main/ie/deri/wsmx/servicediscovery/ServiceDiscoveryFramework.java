/*
 * Copyright (c) 2007, University of Galway, Ireland.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ie.deri.wsmx.servicediscovery;

import ie.deri.wsmx.commons.*;
import ie.deri.wsmx.core.configuration.annotation.*;
import ie.deri.wsmx.servicediscovery.dummy.*;
import ie.deri.wsmx.servicediscovery.instancebased.InstanceBasedDiscovery;

import ie.deri.wsmx.scheduler.*;

import java.util.*;

import org.apache.log4j.*;
import org.wsmo.common.*;
import org.wsmo.execution.common.component.*;
import org.wsmo.execution.common.component.resourcemanager.*;
import org.wsmo.execution.common.exception.*;
import org.wsmo.execution.common.nonwsmo.DiscoveryType;
import org.wsmo.factory.*;
import org.wsmo.service.*;
import org.wsmo.wsml.Parser;

import ch.epfl.qosdisc.wsmx.LoadDatabase;
import ch.epfl.qosdisc.wsmx.QoSDiscovery;

/**
 * Service level discovery - an instance-based and qos discovery.
 * 
 * @author Maciej Zaremba
 * @version $Revision: 1.1 $ $Date: 2007-10-24 15:19:50 $
 */
@WSMXComponent(
		name = "ServiceDiscoveryFramework",
		events = "SERVICEDISCOVERY",
		description = "A Service discovery engine supports instance-based and qos discovery.")
public class ServiceDiscoveryFramework implements ServiceDiscovery {
	
	protected static Logger logger = Logger.getLogger(ServiceDiscoveryFramework.class);
		
	private Parser parser;
	private WsmoFactory wsmoFactory;
	
	private QoSDiscovery qosDiscovery;
	public InstanceBasedDiscovery instanceBasedDiscovery;
	private DummyDiscovery dummyDiscovery;
	
	private IRI goalQoSIRI = null;
	private IRI goalInstanceBasedIRI = null;
	private IRI goalInstanceBasedCompositionIRI = null;
	
	@Stop()
	public void stop(){
		if (qosDiscovery != null){
			try {
				ch.epfl.qosdisc.database.Connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public ServiceDiscoveryFramework() {
		wsmoFactory = Factory.createWsmoFactory(new HashMap());
        LogicalExpressionFactory leFactory = Factory.createLogicalExpressionFactory(new HashMap());
        
        this.goalQoSIRI 				  		= wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/qos");
        this.goalInstanceBasedIRI 				= wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/instancebased");
        this.goalInstanceBasedCompositionIRI	= wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/instancebased/composition");
        
        Map <String, Object> props = new HashMap <String, Object> ();
        props.put(Factory.WSMO_FACTORY, wsmoFactory);
        props.put(Factory.LE_FACTORY, leFactory);
        parser = Factory.createParser(props);
        
        Properties config = Environment.getConfiguration();
        if (config != null){
        	String qosStr = config.getProperty("wsmx.discovery.qosdiscovery");
        	String qosCreateDBStr = config.getProperty("wsmx.discovery.qosdiscovery.createDB");
        	if (qosStr != null && qosStr.toLowerCase().equals("true") ){
        		if (qosCreateDBStr != null && qosCreateDBStr.toLowerCase().equals("true") ){                 
        			//load DB first
        			LoadDatabase.main(null);
        		}
        		qosDiscovery  = new QoSDiscovery();
        	}
        }
        
        instanceBasedDiscovery = new InstanceBasedDiscovery(); 
        dummyDiscovery = new DummyDiscovery();
	}

	public List<Map<WebService, List<Entity>>> discoverServiceCompositon(Goal goal) 
		throws ComponentException, UnsupportedOperationException {

		int discoveryType = DiscoveryType.getRequiredDiscoveryType(goal);
		Set<WebService> searchSpace = getWebServicesFromRM(discoveryType);
		return discoverServiceCompositon(goal, searchSpace);
	}
	
		
	/**
	 * Matches the goal with the available Web service descriptions in two steps. 
	 * The first (optional) step is a non-semantic pre-filtering that reduces 
	 * the set of Web services. The technique used in this step is keyword-based matching, 
	 * the filtering parameters being set such as to ensure that no potentially matching 
	 * Web services are filtered out. 
	 * The second step corresponds to lightweight semantic discovery. The resulting set 
	 * of matching Web services is then returned to the framework.
	 */
	public List<Map<WebService, List<Entity>>> discoverServiceCompositon(Goal goal, Set<WebService> searchSpace) 
	throws ComponentException, UnsupportedOperationException {
		List<WebService> discoveredWSSimple = new ArrayList<WebService>();
		List<Map<WebService, List<Entity>>> discoveredWSComplex = new ArrayList<Map<WebService, List<Entity>>>();
	
		int dt = DiscoveryType.getRequiredDiscoveryType(goal);
		
		if (dt == DiscoveryType.SERVICE_INSTANCEBASED_COMPOSITION_DISCOVERY) {
			logger.debug("Before instance based Discovery with composition support");
			logger.debug("Goal: "+goal.getIdentifier().toString());
			logger.debug("Size: "+searchSpace.size());
			logger.debug("Disc: "+searchSpace.toString());
			
			discoveredWSComplex = instanceBasedDiscovery.discoverWithComposition(goal,searchSpace);
			return discoveredWSComplex;
		} else if (dt == DiscoveryType.SERVICE_INSTANCEBASED_COMPOSITION_DISCOVERY || dt == DiscoveryType.SERVICE_QOS_DISCOVERY){
			discoveredWSSimple = discoverService(goal, searchSpace);
		} else
			return discoveredWSComplex;
			
		//fall back settings - if no Web services has been found
		if (discoveredWSSimple.isEmpty())
			discoveredWSSimple = dummyDiscovery.discover(goal, new ArrayList<WebService>(searchSpace));
		
		logger.debug("Discovered WS:");
		Helper.visualizerLog(Helper.FILTER_DISCOVERY,"Final result:");

//		for (WebService w : discoveredWSSimple){
//			Helper.visualizerLog(Helper.FILTER_DISCOVERY,""+w.getIdentifier());	
//			logger.debug(w.getIdentifier());
//		}
		
		//map simple list to complex list
		for (WebService ws: discoveredWSSimple){
			Map<WebService, List<Entity>> map = new HashMap<WebService, List<Entity>>();
			map.put(ws, new ArrayList<Entity>());
			discoveredWSComplex.add(map);
		}
		return discoveredWSComplex;		
	}
	
	public List<WebService> discoverService(Goal goal) 
		throws ComponentException, UnsupportedOperationException {
		
		int discoveryType = DiscoveryType.getRequiredDiscoveryType(goal);
		Set<WebService> searchSpace = getWebServicesFromRM(discoveryType);
		
		return discoverService(goal, searchSpace);
	}
	
	public List<WebService> discoverService(Goal goal, Set<WebService> searchSpace)
		throws ComponentException, UnsupportedOperationException {
		List<WebService> discoveredWSSimple = new ArrayList<WebService>();
		
		int dt = DiscoveryType.getRequiredDiscoveryType(goal);

		if (dt == DiscoveryType.SERVICE_QOS_DISCOVERY && qosDiscovery != null){
			
			Helper.visualizerLog(Helper.FILTER_DISCOVERY,"Discovery on QoSDiscovery");			discoveredWSSimple = qosDiscovery.discover(goal, new ArrayList<WebService>(searchSpace));

		} else if (dt == DiscoveryType.SERVICE_INSTANCEBASED_DISCOVERY){
			//do instance based discovery
			discoveredWSSimple = instanceBasedDiscovery.discover(goal, searchSpace);
		}

		//fall back settings - if no Web services has been found
		if (discoveredWSSimple.isEmpty())
			discoveredWSSimple = dummyDiscovery.discover(goal, new ArrayList<WebService>(searchSpace));

		return discoveredWSSimple;
	}
	
	private Set<WebService> getWebServicesFromRM(int discoveryType)
	{
		Set<WebService> searchSpace = new HashSet<WebService>();
		Properties config = Environment.getConfiguration();
		if ( Environment.isCore())
		{
			try {
				// obtains Web services from ResourceManager if available
				WebServiceResourceManager wsRM = Environment.getComponentProxy(WebServiceResourceManager.class,this);
				if (wsRM != null){
					searchSpace.addAll(wsRM.retrieveWebServices(discoveryType));
					logger.debug("WS fetched from WSMO Resource Manager. Number of WS: " + searchSpace.size());
				}
			} catch (Throwable e) {
				//RM is not available - use internal Web Services
				logger.debug("NO WSMX Environment found");
			}
		} else {
			//local mode - no WSMX core
			}
		return searchSpace;
	}

}

