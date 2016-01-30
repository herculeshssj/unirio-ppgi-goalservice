/**
 * find a usable Web services for a goal instance 
 * - uses SDC graph 
 * - uses FOL matchmaker  
 * 
 * @author Michael Stollberg
 *
 * @version $Revision: 1.1 $ $Date: 2007-10-27 10:05:52 $
 */ 

package org.deri.wsmx.discovery.caching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmx.discovery.caching.matchmaking.Matchmaking4SWSC;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;


public class SDCRuntimeDiscoverer {
	
	protected static Logger logger;
	private GoalInstanceManager giManager; 
	private Matchmaking4SWSC matchmaker; 
	private SDCGraphCreatorHelper helper; 
	
	public SDCRuntimeDiscoverer() {
		giManager = new GoalInstanceManager(); 
		matchmaker = new Matchmaking4SWSC(); 
		helper = new SDCGraphCreatorHelper(); 
		
		logger = Logger.getLogger(SDCRuntimeDiscoverer.class);
		
	}

	/**
	 * finds a usable Web service for a goal instance 
	 * (main method for performing SDC enabled runtime discovery)  
	 * @param goalInstance
	 * @param sdcGraph
	 * @return the discovered Web service 
	 */
	public WebService discover(Instance goalInstance, Ontology sdcGraph) {
		logger.info("Web Service Discovery for Goal Instance: \n " + goalInstance.getIdentifier() );
		
		WebService theDiscoveredWS = null;
		
		logger.info("trying discovery by lookup .. " );
		// try to find a WS usable for corresp. GT under EXACT or PLUGIN		
		if ( !(giManager.getCorrespondingGT(goalInstance, sdcGraph) == null) ) {
			
			Goal correspGT = giManager.getCorrespondingGT(goalInstance, sdcGraph); 
			theDiscoveredWS = discoveryByLookup(goalInstance, correspGT, sdcGraph);
			
			if ( !(theDiscoveredWS == null) ) {

				logger.info("found usable WS: " + theDiscoveredWS.getIdentifier() );
				return theDiscoveredWS; 
			}			
		}

		Instance revisedGI = giManager.reviseGI4SWSC(goalInstance, sdcGraph); 
		
		// this captures the case if GI does not have a corresp. GT 
		if ( giManager.getCorrespondingGT(goalInstance, sdcGraph) == null ) {
			
			logger.info("the provided goal instance does not define a corresponding GT ... " );
			
			for (Goal aRoot : helper.getAllRootNodes(sdcGraph)) {

				if (matchmaker.instantiatonCheck(revisedGI,aRoot, sdcGraph)) {
				
					logger.info("setting corresponding GT: " + aRoot.getIdentifier() ); 
					revisedGI = giManager.setGT4goalInstance(revisedGI, aRoot, sdcGraph); 
				}
			}
			 
		}
		
		Instance giWithBestGT = null; 
		
		logger.info("setting best GT, then trying discovery by lookup .. " );
		// sets most appropriate GT, then try lookup discovery  
		if (theDiscoveredWS == null ) {
			
			giWithBestGT = goalTemplateSearch(revisedGI, sdcGraph);
			Goal correspGT = giManager.getCorrespondingGT(revisedGI, sdcGraph);
			theDiscoveredWS = discoveryByLookup(giWithBestGT, correspGT, sdcGraph);
		}
		if ( !(theDiscoveredWS == null) ) {
			
			logger.info("found usable WS: " + theDiscoveredWS.getIdentifier() );
			return theDiscoveredWS; 
		}
		
		logger.info("trying other WS usable for corresponding GT (best is already set) .. " );		
		// if lookup does not provide any result, then inspect other WS usable for GT 
		if (theDiscoveredWS == null ) { 
			
			theDiscoveredWS = discoveryGIwithMatchmaking(giWithBestGT, sdcGraph); 
		}
		if ( !(theDiscoveredWS == null) ) {
			logger.info("found usable WS: " + theDiscoveredWS.getIdentifier() );
		} else {
			logger.info("no usable WS found" );
		}

		return theDiscoveredWS ;
	
	}
	
	/**
	 * find the most appropriate goal template for a goal instance 
	 * @param goalInstance
	 * @param sdcGraph
	 * @return the goal instance (with the most appropriate goal template)  
	 */
	public Instance goalTemplateSearch (Instance goalInstance, Ontology sdcGraph) {
		logger.info("searching most appropriate corresponding GT ... "); 
		
		if ( giManager.getCorrespondingGT(goalInstance, sdcGraph) == null ) {
			
			logger.info("the provided goal instance does not define a corresponding GT ... " ); 
			
			for (Goal aRoot : helper.getAllRootNodes(sdcGraph)) {
				if (matchmaker.instantiatonCheck(goalInstance,aRoot, sdcGraph)) {
					logger.info("setting corresponding GT: " + aRoot.getIdentifier() ); 
					goalInstance = giManager.setGT4goalInstance(goalInstance, aRoot, sdcGraph); 
				}
			}
			 
		}
		
		Goal theCurrentGT = giManager.getCorrespondingGT(goalInstance, sdcGraph); 
		logger.info("current corresponding GT: " + theCurrentGT.getIdentifier() );
		
		
		List<Goal> children = helper.getAllChildNodes(theCurrentGT, sdcGraph); 
//		randomizing the order under which children are inspected 
//		java.util.Collections.shuffle(children);
		
		
		for (Goal childOfCurrentGT : children) {

			logger.info("cecking for child: " + childOfCurrentGT.getIdentifier() );

			if (matchmaker.instantiatonCheck(goalInstance,childOfCurrentGT, sdcGraph)) {

				logger.info("setting new corresponding GT: " + childOfCurrentGT.getIdentifier() ); 				
				goalInstance = giManager.setGT4goalInstance(goalInstance, childOfCurrentGT, sdcGraph);
				
				logger.info("going down the goal graph hierachy ... " ); 
				goalTemplateSearch(goalInstance, sdcGraph);
				
				break; 
			}
			
		}

		return goalInstance;
		
	}
	
	
	/**
	 * performs dicovery by lookup, i.e. find a WS that is usable for the 
	 * corresponding goal template under degree EXACT or PLUGIN (and thus 
	 * is usable for goal instance) 
	 * @param goalInstance
	 * @param goalTemplate
	 * @param sdcGraph
	 * @return
	 */	
	private WebService discoveryByLookup (Instance goalInstance, Goal goalTemplate, Ontology sdcGraph) {
		logger.info("discovery by lookup with GT: " + goalTemplate.getIdentifier() );
		
		WebService theDiscoveredWS = null; 
		
		List<WebService> services = new ArrayList<WebService>();
		
		List<WebService> servicesExact = helper.getUsableWS4GT(sdcGraph, goalTemplate, "exact"); 
		List<WebService> servicesPlugin = helper.getUsableWS4GT(sdcGraph, goalTemplate, "plugin"); 
		
		for (WebService aWSexact : servicesExact ) {
			services.add(aWSexact); 
		}
		for (WebService aWSplugin: servicesPlugin ) {
			services.add(aWSplugin); 
		}
		
		if ( services.isEmpty() ) {
			
			for (Goal aParent : helper.getParentsFromGGM(sdcGraph, goalTemplate)) {
				discoveryByLookup(goalInstance, aParent, sdcGraph); 
			}
			
		} else {

			theDiscoveredWS = services.get(0); 
		}
		
		
		return theDiscoveredWS;
		
	}
	
	/**
	 * finds a usable WS for a goal instance out of those usable for the 
	 * corresponding goal template under the degrees SUBSUME or INTERSECT 
	 * @param goalInstance
	 * @param sdcGraph
	 * @return
	 */
	private WebService discoveryGIwithMatchmaking (Instance goalInstance, Ontology sdcGraph) {
		logger.info("discovery with GI level matchmaking ");

		WebService theDiscoveredWS = null;
		
		Goal correspGT = giManager.getCorrespondingGT(goalInstance, sdcGraph); 
		
		logger.info("checking WS under SUBUME"); 
		for (WebService aWSsubsume : helper.getUsableWS4GT(sdcGraph, correspGT, "subsume")) {
			
			logger.info("checking for WS: " + aWSsubsume.getIdentifier() ); 

			if (matchmaker.giLevelUsabilitySubsume(goalInstance, aWSsubsume)) {
				
//				theDiscoveredWS = aWSsubsume;
				return aWSsubsume; 
				
				 
			}
		}

		logger.info("checking WS under INTERSECT"); 
		for (WebService aWSintersect : helper.getUsableWS4GT(sdcGraph, correspGT, "intersect")) {

			logger.info("checking for WS: " + aWSintersect.getIdentifier() ); 

			if (matchmaker.giLevelUsabilityIntersect(goalInstance, correspGT, aWSintersect, sdcGraph)) {
				
//				theDiscoveredWS = aWSintersect;
				return aWSintersect; 
			}
			
		}

		return theDiscoveredWS; 
	}


	
	/**
	 * discovers all usable Web services for a given goal instances 
	 * @param goalInstance for which WS shall be discovered  
	 * @param sdcGraph - used SDC Graph 
	 * @return List<WebService> - the set of discovered WS 
	 */
	public List<WebService> discoverAllWS(Instance goalInstance, Ontology sdcGraph) {
		logger.info("discovery all Web Services for Goal Instance: \n " + goalInstance.getIdentifier() );
		
		List<WebService> theDiscoveredWS = new ArrayList<WebService>();

		// lift goal instance definition to GT level description
		// .. includes the goal instantiation condition check 
		Instance revisedGI = giManager.reviseGI4SWSC(goalInstance, sdcGraph);

		// this captures the case if GI does not have a corresp. GT 
		if ( giManager.getCorrespondingGT(goalInstance, sdcGraph) == null ) {
			
			logger.info("the provided goal instance does not define a corresponding GT ... " );
			
			for (Goal aRoot : helper.getAllRootNodes(sdcGraph)) {
				if (matchmaker.instantiatonCheck(revisedGI,aRoot, sdcGraph)) {
					logger.info("setting corresponding GT: " + aRoot.getIdentifier() ); 
					revisedGI = giManager.setGT4goalInstance(revisedGI, aRoot, sdcGraph); 
				}
			}
		}
		
		// refine goal instance to most appropriate goal template 
		
		Instance giWithBestGT = goalTemplateSearch(revisedGI, sdcGraph);
		Goal mostAppropGT = giManager.getCorrespondingGT(giWithBestGT, sdcGraph); 
		
		// discover Web services by lookup 
		logger.info("checking WS under EXACT and PLUGIN usability degree"); 
		
		theDiscoveredWS = lookupAllWS(mostAppropGT, sdcGraph, theDiscoveredWS);
		
		// investigate WS under with SUBSUME(mostAppropGT,W) 
		logger.info("checking WS under SUBUME usability degree"); 
		
		for (WebService aWSsubsume : helper.getUsableWS4GT(sdcGraph, mostAppropGT, "subsume")) {
			
			logger.info("checking for WS: " + aWSsubsume.getIdentifier() ); 

			if (matchmaker.giLevelUsabilitySubsume(giWithBestGT, aWSsubsume)) {
				
				theDiscoveredWS.add(aWSsubsume); 
			 
			}
		}

		// investigate WS under with intersect(mostAppropGT,W) 
		logger.info("checking WS under INTERSECT usability degree"); 

		for (WebService aWSintersect : helper.getUsableWS4GT(sdcGraph, mostAppropGT, "intersect")) {

			logger.info("checking for WS: " + aWSintersect.getIdentifier() ); 

			if (matchmaker.giLevelUsabilityIntersect(giWithBestGT, mostAppropGT, aWSintersect, sdcGraph)) {
				
				theDiscoveredWS.add(aWSintersect); 

			}
		}
	 
	
		if ( ! theDiscoveredWS.isEmpty() ) {
			logger.info("discovered the following Web services:");
			for ( WebService discoveredWS : theDiscoveredWS) {
				logger.info("  " + discoveredWS.getIdentifier());
			}
		} else {
			logger.info("no usable WS found" );
		}
		
		return theDiscoveredWS ;
	
	}

	/**
	 * sub-procedure for detecting all usable Web services by lookup 
	 * @param correspondingGT 
	 * @param sdcGraph
	 * @param resultSet - the set of discovered WS 
	 * @return List<WebService> 
	 */
	private List<WebService> lookupAllWS(Goal correspondingGT, Ontology sdcGraph, List<WebService> resultSet) {
		
		for (WebService aWSexact : helper.getUsableWS4GT(sdcGraph, correspondingGT, "exact") ) {
			resultSet.add(aWSexact); 
		}

		for (WebService aWSplugin: helper.getUsableWS4GT(sdcGraph, correspondingGT, "plugin") ) {
			resultSet.add(aWSplugin); 
		}

		for (Goal aParent : helper.getParentsFromGGM(sdcGraph, correspondingGT)) {
			lookupAllWS(aParent, sdcGraph, resultSet); 
		}
		
		return resultSet;
	}

	
	public WebService discoverGTonly(Instance goalInstance, Ontology sdcGraph) {
		logger.info("Web Service Discovery by GT-filtering only for Goal Instance: \n " + goalInstance.getIdentifier() );
		
		WebService theDiscoveredWS = null;
		
		Instance revisedGI = giManager.reviseGI4SWSC(goalInstance, sdcGraph); 
		
		// this captures the case if GI does not have a corresp. GT 
		if ( giManager.getCorrespondingGT(goalInstance, sdcGraph) == null ) {
			
			logger.info("the provided goal instance does not define a corresponding GT ... " );
			
			for (Goal aRoot : helper.getAllRootNodes(sdcGraph)) {

				if (matchmaker.instantiatonCheck(revisedGI,aRoot, sdcGraph)) {
				
					logger.info("setting corresponding GT: " + aRoot.getIdentifier() ); 
					revisedGI = giManager.setGT4goalInstance(revisedGI, aRoot, sdcGraph); 
				}
			}
			 
		}
		
		Goal correspondingGT = giManager.getCorrespondingGT(revisedGI, sdcGraph); 
		
		// try discovery by lookup (for initially defined corresponding GT) 
		theDiscoveredWS = discoveryByLookup(revisedGI, correspondingGT, sdcGraph);
		
		if ( !(theDiscoveredWS == null) ) {
			
			logger.info("found usable WS: " + theDiscoveredWS.getIdentifier() );
			return theDiscoveredWS; 
		} else {
			// if lookup does not provide any result, then inspect other WS usable for GT 
			logger.info("trying other WS usable for corresponding GT  .. " );		

			theDiscoveredWS = discoveryGIwithMatchmaking(revisedGI, sdcGraph); 
			
		}
		
		if ( !(theDiscoveredWS == null) ) {
			logger.info("found usable WS: " + theDiscoveredWS.getIdentifier() );
		} else {
			logger.info("no usable WS found" );
		}

		return theDiscoveredWS ;
	
	}

	public List<WebService> discoverGTonlyAllWS(Instance goalInstance, Ontology sdcGraph) {
		logger.info("discovery all Web Services for Goal Instance: \n " + goalInstance.getIdentifier() );
		
		List<WebService> theDiscoveredWS = new ArrayList<WebService>();

		// lift goal instance definition to GT level description
		// .. includes the goal instantiation condition check 
		Instance revisedGI = giManager.reviseGI4SWSC(goalInstance, sdcGraph);

		// this captures the case if GI does not have a corresp. GT 
		if ( giManager.getCorrespondingGT(goalInstance, sdcGraph) == null ) {
			
			logger.info("the provided goal instance does not define a corresponding GT ... " );
			
			for (Goal aRoot : helper.getAllRootNodes(sdcGraph)) {
				if (matchmaker.instantiatonCheck(revisedGI,aRoot, sdcGraph)) {
					logger.info("setting corresponding GT: " + aRoot.getIdentifier() ); 
					revisedGI = giManager.setGT4goalInstance(revisedGI, aRoot, sdcGraph); 
				}
			}
		}
		
		Goal correspondingGT = giManager.getCorrespondingGT(revisedGI, sdcGraph); 
				
		// discover Web services by lookup 
		logger.info("checking WS under EXACT and PLUGIN usability degree"); 
		
		theDiscoveredWS = lookupAllWS(correspondingGT, sdcGraph, theDiscoveredWS);
		
		// investigate WS under with SUBSUME(mostAppropGT,W) 
		logger.info("checking WS under SUBUME usability degree"); 
		
		for (WebService aWSsubsume : helper.getUsableWS4GT(sdcGraph, correspondingGT, "subsume")) {
			
			logger.info("checking for WS: " + aWSsubsume.getIdentifier() ); 

			if (matchmaker.giLevelUsabilitySubsume(revisedGI, aWSsubsume)) {
				
				theDiscoveredWS.add(aWSsubsume); 
			 
			}
		}

		// investigate WS under with intersect(mostAppropGT,W) 
		logger.info("checking WS under INTERSECT usability degree"); 

		for (WebService aWSintersect : helper.getUsableWS4GT(sdcGraph, correspondingGT, "intersect")) {

			logger.info("checking for WS: " + aWSintersect.getIdentifier() ); 

			if (matchmaker.giLevelUsabilityIntersect(revisedGI, correspondingGT, aWSintersect, sdcGraph)) {
				
				theDiscoveredWS.add(aWSintersect); 

			}
		}
	 
	
		if ( ! theDiscoveredWS.isEmpty() ) {
			logger.info("discovered the following Web services:");
			for ( WebService discoveredWS : theDiscoveredWS) {
				logger.info("  " + discoveredWS.getIdentifier());
			}
		} else {
			logger.info("no usable WS found" );
		}
		
		return theDiscoveredWS ;
	
	}

	
}
