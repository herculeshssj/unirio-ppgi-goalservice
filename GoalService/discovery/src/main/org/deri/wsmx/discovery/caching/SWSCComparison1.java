/**
 * provides the methods for running comparison tests for runtime WS discovery 
 * - with SDC, incl. discovery time 
 * - without SDC, incl. discovery time
 * 
 * @author Michael Stollberg
 *
 * @version $Revision: 1.2 $ $Date: 2007-04-25 17:12:05 $
 * 
 */
package org.deri.wsmx.discovery.caching;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.deri.wsmx.discovery.caching.matchmaking.Matchmaking4SWSC;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

public class SWSCComparison1 {
	
	protected static Logger logger;
	
	private GoalInstanceSDCDiscoverer sdcDiscoverer; 
	private GoalInstanceManager giManager; 
	private SDCResourceManager resourceMgr; 
	private Matchmaking4SWSC matchmaker; 
	private SDCGraphManager graphManager; 
	
	public SWSCComparison1() {
		sdcDiscoverer = new GoalInstanceSDCDiscoverer();
		giManager = new GoalInstanceManager();
		resourceMgr = new SDCResourceManager();
		matchmaker = new Matchmaking4SWSC();
		graphManager = new SDCGraphManager(); 
		
		logger = Logger.getLogger(SWSCComparison1.class);
		
	}
	
	/**
	 * invokes the SDC runtime discoverer (@see GoalInstanceSDCDiscoverer) 
	 * - for a specific goal instance 
	 * - measures time for completing the discovery task
	 * @param goalInstance
	 * @param sdcGraph
	 * @return String (discovery result & time in ms) 
	 */
	public String runSDCDiscoverer(Instance goalInstance, Ontology sdcGraph ) {
		
		logger.info("running SDC enabled runtime discovery for GI: " + goalInstance.getIdentifier() ); 
		
		String output = null;
		
        long startTime;
        long stopTime;

        startTime = System.currentTimeMillis();
        stopTime = startTime;
		
		WebService theDiscoveredWS = sdcDiscoverer.discover(goalInstance, sdcGraph); 

        stopTime = System.currentTimeMillis();

        long duration = (stopTime - startTime); // in ms.
        
        if ( theDiscoveredWS == null ) {
        	output = "no usable WS found, time: " + duration + " ms"; 
        } else {
            output = "found WS: " + theDiscoveredWS.getIdentifier().toString() + 
            	"\n time: " + duration + " ms";
        }
       
		return output; 
	}
	
	
	/**
	 * implements the goal instance level matchmaking without using an SDC Graph  
	 * @param goalInstance
	 * @param sdcGraph: the one wherein the goal instance is defined 
	 * @param noOfWS
	 * @return String (discovery result + needed time) 
	 */
	public String nonSDCDiscovery(Instance goalInstance, Ontology sdcGraph, int noOfWS) {
		
		logger.info("running runtime discovery without SDC for GI: " + goalInstance.getIdentifier() + "\n with no. of WS = " +noOfWS ); 
		
		String output = null;

		WebService theDiscoveredWS = null;
				
		// loading Web service descriptions 
		WebService wsMuller = resourceMgr.loadWS("wsMuller.wsml"); 
		WebService wsRacer = resourceMgr.loadWS("wsRacer.wsml"); 
		WebService wsRunner = resourceMgr.loadWS("wsRunner.wsml"); 
		WebService wsWalker = resourceMgr.loadWS("wsWalker.wsml"); 
		WebService wsWeasel = resourceMgr.loadWS("wsWeasel.wsml"); 
		WebService wsOther = resourceMgr.loadWS("wsOther.wsml"); 
		
		
		List<Integer> availableWS = new ArrayList<Integer>();
		
	    for (int i = 1; i < noOfWS ; i++) {
	    	availableWS.add(i); 
	      } 
	    
	    // randomizing order 
	    java.util.Collections.shuffle(availableWS);
	    
	    logger.info("number of available WS: " + (availableWS.size() +1) ); 

	    for (Integer number : availableWS) {
	    	System.out.print(number + ", "); 
	      } 
	    
	    System.out.println(); 

	    WebService theWStoCheck = null; 
	    
        long startTime;
        long stopTime;

        startTime = System.currentTimeMillis();
        stopTime = startTime;
		
        stopTime = System.currentTimeMillis();
        
 		Goal correspGT = null;
 		
 		Instance revisedGI = giManager.reviseGI4SWSC(goalInstance, sdcGraph); 
 		
 		if (giManager.getCorrespondingGT(goalInstance, sdcGraph) == null ) {
 			revisedGI = sdcDiscoverer.goalTemplateSearch(revisedGI, sdcGraph);
 			correspGT = giManager.getCorrespondingGT(revisedGI, sdcGraph); 
 		} else {
 			correspGT = giManager.getCorrespondingGT(revisedGI, sdcGraph);	 			
 		}



        int counter = 0; 
	    for (Integer number : availableWS) {
	    	counter += 1; 
	    	if (number == 1) { theWStoCheck = wsMuller; } 
	    	else if (number == 2) { theWStoCheck = wsRacer; } 
//			delete this line !! 
	    	else if (number == 3) { theWStoCheck = wsRacer; } 
	    	else if (number == 4) { theWStoCheck = wsRunner; } 
	    	else if (number == 5) { theWStoCheck = wsWalker; } 
	    	else if (number == 6) { theWStoCheck = wsWeasel; } 
	    	else { theWStoCheck = wsOther; }
	    	
	    	logger.info("checking for WS: " + theWStoCheck.getIdentifier()); 
	    	
 			if ( matchmaker.giLevelUsabilityIntersect(revisedGI, correspGT, theWStoCheck, sdcGraph) ) {
 				
 				theDiscoveredWS = theWStoCheck; 
 				break; 
 			
 			}	
	    }
	    
        stopTime = System.currentTimeMillis();

        long duration = (stopTime - startTime); // in ms.
        
        if ( theDiscoveredWS == null ) {
        	output = "no usable WS found, time: " + duration + " ms"; 
        } else {
            output = "found WS: " + theDiscoveredWS.getIdentifier().toString() + 
            	"\n time: " + duration + " ms, number of matchmaking operations: " + counter ;
        }

		try {
			sdcGraph.removeInstance(revisedGI);
			graphManager.cacheClearer(revisedGI); 
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	    		
		return output;
	}
	

}
