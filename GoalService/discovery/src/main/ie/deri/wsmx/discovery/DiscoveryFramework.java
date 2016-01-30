/*
 * Copyright (c) 2006, University of Innsbruck, Austria.
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

package ie.deri.wsmx.discovery;

import ie.deri.wsmx.commons.*;
import ie.deri.wsmx.core.configuration.annotation.*;

import ie.deri.wsmx.discovery.dummy.DummyWebServiceDiscovery;
import ie.deri.wsmx.discovery.util.*;
import ie.deri.wsmx.scheduler.*;

import java.util.*;

import org.apache.log4j.*;
import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.common.exception.*;
import org.wsmo.execution.common.component.Discovery;
import org.wsmo.execution.common.component.resourcemanager.*;
import org.wsmo.execution.common.exception.*;
import org.wsmo.execution.common.nonwsmo.DiscoveryType;
import org.wsmo.factory.*;
import org.wsmo.service.*;
import org.wsmo.wsml.*;

/**
 * A discovery framework supporting different discovery engines: 
 * keyword, lightweight rule and lightweight DL, instance-based 
 * and QoS discovery.
 * 
 * @author Adina Sirbu, STI-Innsbruck
 * @author Nathalie Steinmetz, STI-Innsbruck
 * @version $Revision: 1.10 $ $Date: 2007/06/14 15:08:50 $
 */

@WSMXComponent(
		name = "DiscoveryFramework",
		events = "WEBSERVICEDISCOVERY",
		description = "A discovery engine supporting keyword, " +
				"lightweight, lightweight rule, lightweight DL" +
				"and heavyweight discovery.")
public class DiscoveryFramework implements Discovery {
	
	protected static Logger logger = Logger.getLogger(DiscoveryFramework.class);
		
	private Parser parser;
	private WsmoFactory wsmoFactory;
	
	private DummyWebServiceDiscovery dummyDiscovery = new DummyWebServiceDiscovery(); 
	
	// different discovery engines (reflecting different types of discovery)
	private int[] types = {
			DiscoveryType.WEBSERVICE_KEYWORD_DISCOVERY,
			DiscoveryType.WEBSERVICE_LIGHTWEIGHT_RULE_DISCOVERY,
			DiscoveryType.WEBSERVICE_LIGHTWEIGHT_DISCOVERY,
//		    DiscoveryType.WEBSERVICE_LIGHTWEIGHT_DL_DISCOVERY,
		    DiscoveryType.WEBSERVICE_HEAVYWEIGHT_DISCOVERY}; 
	
	// list containing the discovery engines corresponding to the different 
	// types of discovery
	private Map<Integer, WSMODiscovery> discoveryEngines;

	public DiscoveryFramework() {
        
		// creating the wsml parser with the wsmo factory as parameter
		Map <String, Object> props = new HashMap<String, Object> ();
		wsmoFactory = Factory.createWsmoFactory(new HashMap<String, Object>());
		props.put(Factory.WSMO_FACTORY, wsmoFactory);
        parser = Factory.createParser(props);
        
        // get default locator
        Factory.getLocatorManager().addLocator(new DefaultLocator());     
        
        
        
        // create a discovery engine for each one of the different 
        // discovery types and add them to the discovery engine list
        discoveryEngines = new HashMap<Integer, WSMODiscovery>();
        for (int type : types)
        	discoveryEngines.put(type, DiscoveryFactory.createDiscoveryEngine(type));
	}
	
	/*
	 ******************************************************
	 *     Add Web Services to the Discovery Engines
	 ******************************************************
	 */
	
	/*
	 * Extract the web service to be added to the discovery engines 
	 * from the URL given as string.
	 */
	@Exposed(description = "Human-interface-friendly wrapper around the " +
			"operation of adding a Web service to the discovery engine from " +
			"a given Identifier. Returns a human-readable confirmation or " +
			"an error description in case of failure. ")
	public String addWebServiceByID(String ID) {
		WebService webService = this.getWebServiceByUrl(ID);
		
		if (webService == null)
			return "WARNING: Failed to locate a Web service";
		
		return addWebService(webService);
	}
	
	/*
	 * Extract the web service to be added to the discovery engines 
	 * from a wsml service document given as string.
	 */
	@Exposed(description = "Human-interface-friendly wrapper around the " +
			"operation of adding a Web service to the discovery engine from " +
			"a given WSML document. Returns a human-readable confirmation " +
			"or an error description in case of failure. ")
	public String addWebServiceByContent(String serviceDocument) {
		WebService webService = null;
		try {
			webService = this.getWebServiceByContent(serviceDocument);
		} catch (ComponentException e) {
			logger.warn("WARNING: Failed to parse Web service.", e);
			return "WARNING: Failed to parse Web service: " + e.getMessage();
		}
		if (webService == null)
			return "WARNING: Failed to locate a Web service";
		
		return addWebService(webService);
	}
	
    /*
     * Add a list of web services to the discovery engines.
     */
    public String addWebServices(List<WebService> services) {
    	String message = "";
    	for (WebService webService: services)
    		message += "\n" + addWebService(webService) + "\n";
    	
    	return message;
	}
	
	/*
	 * Add a given web service to the different discovery engines, if 
	 * they have not been added before.
	 */
    public String addWebService(WebService webService) {
    	String message = "";
		// add the web service to each discovery engine	
		for (WSMODiscovery engine : discoveryEngines.values())
				message += "\n" + addWebService(webService, engine) + "\n";
		return message;
    }
    
    /*
     * Adds a given Web service to a given discovery engine. 
     * Returns a corresponding success/failure message 
     */
    private String addWebService(WebService webService, 
    		WSMODiscovery engine) {
    	String message;
    	
    	try {
			engine.addWebService(webService);
			message = "Successfully added Web service to " + 
				engine.getClass().getSimpleName() + " engine";
		} catch (DiscoveryException e) {
			String failed = "WARNING: Failed to add Web service to " + 
				engine.getClass().getSimpleName() + " engine";
			logger.warn(failed, e);
			message = failed + ":\n " + e.getMessage(); 
		}
		return message;
	}
    
    /*
	 ******************************************************
	 *   Remove Web Services from the Discovery Engines
	 ******************************************************
	 */
    
    /*
	 * Extract the web service to be removed from the discovery engines 
	 * from the URL given as string.
	 */
	@Exposed(description = "Human-interface-friendly wrapper around the " +
			"operation of removing a Web service from the discovery " +
			"engine. Returns a human-readable confirmation or an error " +
			"description in case of failure. ")
	public String removeWebServiceByID(String ID) {
		WebService webService = this.getWebServiceByUrl(ID);
		if (webService == null)
			return "WARNING: Failed to locate a Web service";
		
		try {
			this.removeWebService(webService);
		} catch (IllegalArgumentException e) {
			logger.warn("WARNING: Failed to remove Web service: ", e);
			return "WARNING: Failed to remove Web service: " + e.getMessage();
		}
		return "Successfully removed Web service.";
	}
	
	/*
	 * Extract the web service to be removed from the discovery engines 
	 * from a wsml service document given as string.
	 */
	@Exposed(description = "Human-interface-friendly wrapper around the " +
			"operation of removing a Web service from the discovery engine. " +
			"Returns a human-readable confirmation or an error description " +
			"in case of failure. ")
	public String removeWebServiceByContent(String serviceDocument) {
		WebService webService = null;
		try {
			webService = this.getWebServiceByContent(serviceDocument);
		} catch (ComponentException e) {
			logger.warn("WARNING: Failed to parse Web service.", e);
			return "WARNING: Failed to parse Web service: " + e.getMessage();
		}
		if (webService == null)
			return "WARNING: Failed to locate a Web service";
		
		return removeWebService(webService);
	}
    
	/*
     * Remove a list of web services from the discovery engines.
     */
    public String removeWebServices(List<WebService> services) {
    	String message = "";
    	for (WebService ws: services)
    		message += "\n" + removeWebService(ws) + "\n";
    	
    	return message;
	}
    
    /*
	 * Remove a given web service from the different discovery engines, if 
	 * they have not been removed before.
	 */
    public String removeWebService(WebService webService) {
    	String message = "";
   		// remove the web service from each discovery engine	
   		for (WSMODiscovery engine : discoveryEngines.values())
   			message += "\n" + removeWebService(webService, engine) + "\n";
    	return message;	
    }
    
    /*
     * Removes a Web service from a discovery engine. 
     * Returns a corresponding success/failure message
     */
    private String removeWebService(WebService webService, 
    		WSMODiscovery engine) {
    	String message;
    	
    	try {
			engine.removeWebService(webService);
			message = "Successfully removed Web service from " + 
				engine.getClass().getSimpleName() + " engine";
		} catch (IllegalArgumentException e) {
			String failed = "WARNING: Failed to remove Web service from " + 
				engine.getClass().getSimpleName() + " engine";
			logger.warn(failed, e);
			message = failed + ":\n " + e.getMessage(); 
		}
		return message;
    }

    /*
	 ******************************************************
	 *                   Discover Goals
	 ******************************************************
	 */
    
    /*
	 * Extract the goal to be discovered by the discovery engines 
	 * from the URL given as string.
	 */
	@Exposed(description = "Human-interface-friendly wrapper around the " +
			"operation of discovery that allows to pass in a goal. Returns " +
			"the list of matching web services or an error description in " +
			"case of failure.")
	public String discoverByGoalUrl(String URL) {
		Goal goal = this.getGoalByUrl(URL);
		if (goal == null)
			return "WARNING: Failed to locate a goal";
		
		return this.getDiscoveryOutput(goal);
	}
	
	/*
	 * Extract the goal to be discovered by the discovery engines 
	 * from a wsml service document given as string.
	 */
	@Exposed(description = "Human-interface-friendly wrapper around the " +
			"operation of discovery that allows to pass in a goal. Returns " +
			"the list of matching web services or an error description in " +
			"case of failure.")
	public String discoverByGoalContent(String goalDoc) {
		Goal goal = null;
		try {
			goal = this.getGoalByContent(goalDoc);
		} catch (ComponentException e) {
			logger.warn("Failed to parse goal: ", e);
			return "WARNING: Failed to parse goal: " + e.getMessage();
		}
		if (goal == null)
			return "WARNING: Failed to locate a goal";
		
		String result = this.getDiscoveryOutput(goal); 

		return result; 
	}

	private String getDiscoveryOutput(Goal goal) {
		List<WebService> wsList;
		try {
			wsList = this.discover(goal);
		} catch (UnsupportedOperationException e) {
			logger.warn("WARNING: Operation not supported.", e);
			return "WARNING: Operation not supported: " + e.getMessage();
		} catch (ComponentException e) {
			logger.warn("WARNING: Failure during discovery: ", e);
			return "WARNING: Failure during discovery: " + e.getMessage();
		} 
		
		String result = "";
		if (wsList.isEmpty()) {
			result = "No matching web services";
		} else {
			for (WebService ws : wsList)
				result += ws.getIdentifier().toString() + "\n";
		}
		return result;
	}
	
	/**
	 * <p>
	 * Matches the goal with the available Web service descriptions in two 
	 * steps. 
	 * </p>
	 * <p>
	 * The first (optional) step is a non-semantic pre-filtering that 
	 * reduces the set of Web services. The technique used in this step is 
	 * keyword-based matching, the filtering parameters being set such as to 
	 * ensure that no potentially matching Web services are filtered out. 
	 * </p>
	 * <p>
	 * The second step corresponds to lightweight semantic discovery. The 
	 * resulting set of matching Web services is then returned to the 
	 * framework.
	 * </p>
	 */
	public List<WebService> discover(Goal goal) 
		throws ComponentException, UnsupportedOperationException {

		List<WebService> webservices = new ArrayList<WebService>();
		if ( Environment.isCore())
		    try {
				// obtains Web services from ResourceManager if available
				WebServiceResourceManager wsResourceManager = 
					Environment.getComponentProxy(WebServiceResourceManager.class,this);
				if (wsResourceManager != null){
					webservices.addAll(wsResourceManager.retrieveWebServices());
					logger.debug("Web Services fetched from WSMO Resource Manager. " +
							"Number of Web Services: " + webservices.size());
				}
			} catch (Throwable e) {
				// Web Service Resource Manager is not available - 
				// use internal Web Services
				logger.debug("NO WSMX Environment found");
			}
		else {
			//local mode - no WSMX core, i.e. no web services from resource manager
		}
		
		return discover(goal);
	}

	/**
	 * <p>
	 * Matches the given goal with the given Web service descriptions in 
	 * two steps. 
	 * </p>
	 * <p>
	 * The first (optional) step is a non-semantic pre-filtering that 
	 * reduces the set of Web services. The technique used in this step is 
	 * keyword-based matching, the filtering parameters being set such as to 
	 * ensure that no potentially matching Web services are filtered out. 
	 * </p>
	 * <p>
	 * The second step corresponds to lightweight semantic discovery. The 
	 * resulting set of matching Web services is then returned to the 
	 * framework.
	 * </p>
	 */
    public List<WebService> discover(Goal goal, Set<WebService> searchSpace){
    	// list for the resulting web services
    	List<WebService> discoveredWS = new ArrayList<WebService>();  	
    	
    	Map<Integer, WSMODiscovery> discEnginesTemp = new HashMap<Integer, WSMODiscovery>(discoveryEngines);

    	// temporary search space, equal to given search space
    	Set<WebService> tempSearchSpace = new HashSet<WebService>(searchSpace);
    	
        // create a keyword discovery engine and a lightweight rule 
        int dt = DiscoveryType.getRequiredDiscoveryType(goal);
        // if the lightweight rule engine is targeted by nfp
        if (dt == DiscoveryType.WEBSERVICE_LIGHTWEIGHT_RULE_DISCOVERY) {
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_HEAVYWEIGHT_DISCOVERY);
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_LIGHTWEIGHT_DISCOVERY);
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_LIGHTWEIGHT_DL_DISCOVERY);
        }
        // if the extended plugin rule (heavyweight) engine is targeted by nfp
        else if (dt == DiscoveryType.WEBSERVICE_HEAVYWEIGHT_DISCOVERY) {
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_LIGHTWEIGHT_DISCOVERY);
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_LIGHTWEIGHT_DL_DISCOVERY);
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_LIGHTWEIGHT_RULE_DISCOVERY);
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_KEYWORD_DISCOVERY);
        }
        else {
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_LIGHTWEIGHT_RULE_DISCOVERY);
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_LIGHTWEIGHT_DL_DISCOVERY);
        	discEnginesTemp.remove(DiscoveryType.WEBSERVICE_HEAVYWEIGHT_DISCOVERY);
        }

	    // discover in each one of the discovery engines
		Iterator<WSMODiscovery> engineIt = discEnginesTemp.values().iterator();
		while (engineIt.hasNext()) {
			WSMODiscovery engine = engineIt.next();
			try {
				engine.addWebService(searchSpace);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			// log messages
			Helper.visualizerLog(Helper.FILTER_DISCOVERY, 
					"Discovery on " + engine.getClass().getSimpleName());
			logger.debug("Testing discovery on " + 
					engine.getClass().getSimpleName());

			try {
				// narrow down search scope to the results from the given 
				// discovery step, i.e. from keyword-based search first 
				// and then from set-based search
				discoveredWS = engine.discover(goal, tempSearchSpace);
				tempSearchSpace.clear();
				tempSearchSpace.addAll(discoveredWS);
				
				// log messages
				Helper.visualizerLog(Helper.FILTER_DISCOVERY, 
						engine.getClass().getSimpleName() + " discovered " + 
						discoveredWS.size() + " Web services:");
				logger.debug(engine.getClass().getSimpleName() + 
						" discovered: " + discoveredWS.size());
				for (WebService resultWS : discoveredWS){
					Helper.visualizerLog(Helper.FILTER_DISCOVERY, 
							"" + resultWS.getIdentifier());
					logger.debug(resultWS.getIdentifier());
				}
			// this exception needs to be caught when either rule-based 
		    // goals are tried to be discovered by lightweight DL discovery 
			// or inverted. The discovery needs to be continued in this case, 
			// as either lightweight DL or lightweight rule discovery should 
			// succeed.
			} catch (Exception e) {
				logger.warn("Discovery failed on " + 
						engine.getClass().getSimpleName() + 
						" engine\n " + e.getMessage());
				continue;
			}
		}
		if (discoveredWS== null || discoveredWS.size() == 0)
			discoveredWS = dummyDiscovery.discover(goal, new ArrayList<WebService>(searchSpace));
		
		// log messages
		Helper.visualizerLog(Helper.FILTER_DISCOVERY, "Final result:");
		logger.debug("Discovered WS:");
		for (WebService w : discoveredWS){
			Helper.visualizerLog(Helper.FILTER_DISCOVERY, 
					"" + w.getIdentifier());	
			logger.debug(w.getIdentifier());
		}
		
		return discoveredWS;		
    }
	
	/* (non-Javadoc)
	* @see org.wsmo.execution.common.component.Discovery#discover(
	* 			org.wsmo.service.Goal, org.omwg.ontology.Ontology)
	*/
	public Map<Map<WebService, Interface>, Identifier> discover(
			Goal goal, Ontology rankingOntology) 
	throws ComponentException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/*
	 ******************************************************
	 *                   Helper methods
	 ******************************************************
	 */
	
    /*
     * Returns a Web service object from the given URL
     */
    private WebService getWebServiceByUrl(String url) {
		return (WebService) Factory.getLocatorManager().lookup(
					wsmoFactory.createIRI(url), 
					WebService.class);
    }
    
    /*
     * Returns a Web service object from the given WSMLDocument
     */
    private WebService getWebServiceByContent(String serviceDoc) 
		throws ComponentException {
    	TopEntity[] topEntities = parse(serviceDoc);
    	Set<WebService> webservices = Helper.getWebServiceSet(topEntities);

    	if (webservices.size()!= 0)
    		return (WebService) webservices.toArray()[0];
    	else
    		return null;
    }

    /*
     * Returns a goal object from the given URL
     */
    private Goal getGoalByUrl(String url) {
    	return (Goal) Factory.getLocatorManager().lookup(
    				wsmoFactory.createIRI(url.toString()), 
    				Goal.class);
    }
    
    /*
     * Returns a Goal object from the given WSMLDocument
     */
    private Goal getGoalByContent(String goalDoc) throws ComponentException {
    	TopEntity[] topEntities = parse(goalDoc);
    	Set<Goal> goals = Helper.getGoalSet(topEntities);

    	if (goals.size()!= 0)
    		return (Goal) goals.toArray()[0];
    	else
    		return null;
    }
    
    private TopEntity[] parse(String wsmlDocument) throws ComponentException {
        TopEntity[] parsed = null;
        try {
            parsed = parser.parse( new StringBuffer(wsmlDocument));
        } catch (ParserException e) {
        	throw new ComponentException("Parsing failed:" + e.getMessage(), e);
        } catch (InvalidModelException e) {
        	throw new ComponentException("Parsing failed:" + e.getMessage(), e);
        }
        
        return parsed;
    }

}

