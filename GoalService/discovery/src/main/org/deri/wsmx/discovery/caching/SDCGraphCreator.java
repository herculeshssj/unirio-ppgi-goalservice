/**
 * for creation of SDC graph
 * - takes 1 goal template as input
 * - inserts this into goal graph 
 * - determines discovery cache (= usable Web services) 
 * 
 * NOTE: uses Matchmaking4SWSC, for other use cases 
 * another implementation of the Matchmaking interface
 * is needed 
 * 
 * @author Michael Stollberg
 *
 * @version $Revision: 1.9 $ $Date: 2007-10-11 19:42:08 $
 */ 

package org.deri.wsmx.discovery.caching;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.deri.wsmx.discovery.caching.matchmaking.Matchmaking4SWSC;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;


public class SDCGraphCreator {
	
 	protected static Logger logger;
	private WsmoFactory wsmoFactory;
	private SDCGraphManager sdcGraphManager;  
	private SDCResourceManager sdcResourceManager; 
	private Matchmaking4SWSC matchmaker; 
	private SDCGraphCreatorHelper helper; 
	
	public SDCGraphCreator(){
		wsmoFactory = Factory.createWsmoFactory(null);
		logger = Logger.getLogger(SDCGraphCreator.class);
		sdcGraphManager = new SDCGraphManager();
		sdcResourceManager = new SDCResourceManager();
		helper = new SDCGraphCreatorHelper(); 
		// here: the hardcoded matchmaker for the SWSC shipment use case 
		matchmaker = new Matchmaking4SWSC(); 
	}
	
	/**
	 * main method for SDC graphy creation 
	 * inserts a new goal template into the SDC graph
	 * - at the right place in the goal graph + goal graph revision 
	 * - creates & maintains minimal discovery cache 
	 * @param sdcGraph
	 * @param newGT
	 * @param wsDirectory
	 * @return the updated SDC Graph 
	 */
	public Ontology insertGoalTemplate(Ontology sdcGraph, Goal newGT, String wsDirectory){

		logger.info("inserting new goal template: " + newGT.getIdentifier());
		
		if ( sdcGraphManager.getAllGoalTemplates(sdcGraph).isEmpty()) {
			logger.info("inserting as first GT ");
			sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "root");
			sdcGraph = discoveryCacheArcCreation(sdcGraph, newGT, wsDirectory);
		} 
		else {
		  for (Goal theExistingGT : helper.getAllRootNodes(sdcGraph)) {
			  
			  logger.info("for root nodes ...");  
			  
			if ( matchmaker.similarityExact(theExistingGT, newGT, sdcGraph)) {
				logger.info("this or an identical GT already exists !!");
			}
			else if ( matchmaker.similarityPlugin(theExistingGT, newGT, sdcGraph)) { 
				sdcGraph = rootNodeInsertion(newGT,theExistingGT,sdcGraph);
				sdcGraph = discoveryCacheArcCreation(sdcGraph, newGT, wsDirectory); 
			}
			else if ( matchmaker.similaritySubsume(theExistingGT, newGT, sdcGraph)) { 
				sdcGraph = childNodeInsertion(newGT,theExistingGT,sdcGraph);
				sdcGraph = discoveryCacheArcCreation(sdcGraph, newGT, wsDirectory); 
			}
			else if ( matchmaker.similarityIntersect(theExistingGT, newGT, sdcGraph)) {
				sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "root"); 
				sdcGraph = iArcResolution(newGT,theExistingGT,sdcGraph);
				sdcGraph = discoveryCacheArcCreation(sdcGraph, newGT, wsDirectory);
			}
			else {
				sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "root");
				sdcGraph = discoveryCacheArcCreation(sdcGraph, newGT, wsDirectory);
			}
			
		  }
		}
		return sdcGraph;
		
	}
	
	




	/**
	 * inserts the new goal template as a root node in the goal graph 
	 * @param newGT
	 * @param theExistingGT
	 * @param sdcGraph
	 * @return the updated SDC graph 
	 */
	private Ontology rootNodeInsertion(Goal newGT, Goal theExistingGT, Ontology sdcGraph) {			
		logger.info("inserting as a new root node ");
		
		sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "root");
		
		sdcGraph = sdcGraphManager.deleteGoalTemplate(theExistingGT, sdcGraph); 
		sdcGraph = sdcGraphManager.addGoalTemplate(theExistingGT, sdcGraph, "child"); 
		
		sdcGraph = sdcGraphManager.addGoalGraphArc(newGT, theExistingGT, sdcGraph, "subsume"); 
		
		return sdcGraph;
	}

	/**
	 * inserts the new goal template as a child node in the goal graph
	 * @param newGT
	 * @param theExistingGT
	 * @param sdcGraph
	 * @return the updated SDC graph 
	 */
	private Ontology childNodeInsertion(Goal newGT, Goal theExistingGT, Ontology sdcGraph) {
		logger.info("inserting as a child node of " + theExistingGT.getIdentifier());
		
		
		if ( helper.getAllChildNodes(theExistingGT, sdcGraph).isEmpty() ) {
			
			if ( sdcGraphManager.isIntersectionGT(newGT, sdcGraph) ) {
				
				List<Goal> parentsOfExistingGT = helper.getParentsFromGGM(sdcGraph, theExistingGT); 
				
				for (Goal aParent : parentsOfExistingGT) {
					sdcGraph = sdcGraphManager.deleteGoalGraphArc(sdcGraph,
							sdcGraphManager.getSingleGoalGraphArc(aParent, newGT, sdcGraph) ); 		
				}
					
				
			} else if ( !(sdcGraphManager.getGoalGraphArcByTarget(newGT, sdcGraph) == null ) ) {
				List<Instance> theRedundantGGM = sdcGraphManager.getGoalGraphArcByTarget(newGT, sdcGraph); 
				for (Instance aGGM : theRedundantGGM) {
					sdcGraph = sdcGraphManager.deleteGoalGraphArc(sdcGraph, aGGM); 
				}
			}
			
			sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "child"); 
			sdcGraph = sdcGraphManager.addGoalGraphArc(theExistingGT, newGT, sdcGraph, "subsume");
		}
		else {
			logger.info("for child nodes of " + theExistingGT.getIdentifier()); 
			for ( Goal theExistingChild : helper.getAllChildNodes(theExistingGT, sdcGraph)) {
				
				logger.info("for child node " + theExistingChild.getIdentifier()); 
				
				if ( matchmaker.similarityExact(theExistingChild, newGT, sdcGraph)) {
					logger.info("this or an identical GT already exists !!");
					return sdcGraph;
				}
	 
				else if ( matchmaker.similarityPlugin(theExistingChild, newGT, sdcGraph)) {
					logger.info("inserting as a new inner level");
					
					sdcGraph = sdcGraphManager.deleteGoalGraphArc(sdcGraph,
							sdcGraphManager.getSingleGoalGraphArc(theExistingGT, theExistingChild, sdcGraph));
					sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "child");					
					sdcGraph = sdcGraphManager.addGoalGraphArc(newGT, theExistingChild, sdcGraph, "subsume");
					sdcGraph = sdcGraphManager.addGoalGraphArc(theExistingGT, newGT, sdcGraph, "subsume");			

				}
				
				else if ( matchmaker.similaritySubsume(theExistingChild, newGT, sdcGraph)) {
					logger.info("going down the goal graph hierarchy");
					sdcGraph = childNodeInsertion(newGT,theExistingChild,sdcGraph); 
				}
				else if ( matchmaker.similarityIntersect(theExistingChild, newGT, sdcGraph)) {
					logger.info("detecting an i-arc ...");
	
					sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "child");
					sdcGraph = sdcGraphManager.addGoalGraphArc(helper.getParentsFromGGM(sdcGraph, theExistingChild).get(0), newGT, sdcGraph, "subsume"); 
	
					sdcGraph = iArcResolution(newGT,theExistingChild,sdcGraph); 
				}
				else {
					sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "child"); 
					sdcGraph = sdcGraphManager.addGoalGraphArc(theExistingGT, newGT, sdcGraph, "subsume");			
					
				}
				
			}
			
		}
		
		

		return sdcGraph;
	}


		
//		if ( helper.getAllChildNodes(theExistingGT, sdcGraph).isEmpty() )  {
//			
//			sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "child"); 
//			sdcGraph = sdcGraphManager.addGoalGraphArc(theExistingGT, newGT, sdcGraph, "subsume");
//		}
//		
//		else {
//
////			logger.info("for child nodes of " + theExistingGT.getIdentifier()); 
//			for ( Goal theExistingChild : helper.getAllChildNodes(theExistingGT, sdcGraph)) {
//				
//				logger.info("for child node " + theExistingChild.getIdentifier()); 
//				
//				if ( matchmaker.similarityExact(theExistingChild, newGT)) {
//					logger.info("this or an identical GT already exists !!");
//				}
//	 
//				else if ( matchmaker.similarityPlugin(theExistingChild, newGT)) {
//					logger.info("inserting as a new inner level");
//					
//					sdcGraph = sdcGraphManager.deleteGoalGraphArc(sdcGraph,
//							sdcGraphManager.getSingleGoalGraphArc(theExistingGT, theExistingChild, sdcGraph));
//					sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "child");					
//					sdcGraph = sdcGraphManager.addGoalGraphArc(newGT, theExistingChild, sdcGraph, "subsume");
//					sdcGraph = sdcGraphManager.addGoalGraphArc(theExistingGT, newGT, sdcGraph, "subsume");			
//
//				}
//				
//				else if ( matchmaker.similaritySubsume(theExistingChild, newGT)) {
//					logger.info("going down the goal graph hierarchy");
//					sdcGraph = childNodeInsertion(newGT,theExistingChild,sdcGraph); 
//				}
//				else if ( matchmaker.similarityIntersect(theExistingChild, newGT)) {
//					logger.info("detecting an i-arc ...");
//					sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "child");
//					sdcGraph = iArcResolution(newGT,theExistingChild,sdcGraph); 
//				}
//				else  {
//					sdcGraph = sdcGraphManager.addGoalTemplate(newGT, sdcGraph, "child");
//					
////					if ( ! (sdcGraphManager.getSingleGoalGraphArc(theExistingGT, newGT, sdcGraph) == null) ) {
////						sdcGraph = sdcGraphManager.deleteGoalGraphArc(sdcGraph,
////								sdcGraphManager.getSingleGoalGraphArc(theExistingGT, newGT, sdcGraph));						
////					}
//					
//					sdcGraph = sdcGraphManager.addGoalGraphArc(theExistingChild, newGT, sdcGraph, "subsume");
//				} 
//			}
//		}
//		
//		return sdcGraph; 
//		
//	}
					
		

	/**
	 * implements the en-route i-arc resolution in the goal graph 
	 * @param newGT: one of the orginal goal templates 
	 * @param theExistingGT: the other orginal goal templates 
	 * @param sdcGraph
	 * @return the updated SDC Graph 
	 */
	private Ontology iArcResolution(Goal oneGT, Goal otherGT, Ontology sdcGraph) {
		logger.info("commencing the i-arc resolution ...");
		
		sdcGraph = sdcGraphManager.addIntersectionGT(oneGT, otherGT, sdcGraph);
		
//		List<Instance> allGGMfromOneGT = sdcGraphManager.getGoalGraphArcBySource(oneGT, sdcGraph); 
//		
//		for (Instance aGGMoneGT : allGGMfromOneGT ) {
//			logger.info("ggm from " + oneGT.getIdentifier() + aGGMoneGT.getIdentifier()); 
//		}
//
//		List<Instance> allGGMfromOtherGT = sdcGraphManager.getGoalGraphArcBySource(otherGT, sdcGraph); 
//		
//		for (Instance aGGMotherGT : allGGMfromOtherGT) {
//			logger.info("ggm from " + otherGT.getIdentifier() + aGGMotherGT.getIdentifier()); 
//		}

		
		Goal theIGT = helper.getIntersectionGT(oneGT, otherGT, sdcGraph);
		
		logger.info("the iGT is: " + theIGT.getIdentifier()); 
		
		List<Goal> childrenOfParents = helper.getChildrenFromGGM(sdcGraph, oneGT);
		List<Goal> childrenOtherGT = helper.getChildrenFromGGM(sdcGraph, otherGT);
		
		for ( Goal aChild : childrenOtherGT) {
						
			if ( ! childrenOfParents.contains(aChild) ) {
				childrenOfParents.add(aChild); 
			}
		}
		
		childrenOfParents.remove(theIGT); 
					
		logger.info("checking similarity degree for nodes on the same level ...  "); 

		for ( Goal theChild: childrenOfParents) {
			
			logger.info("checking for child " + theChild.getIdentifier() ); 
			
			if ( matchmaker.similarityExact(theChild, theIGT, sdcGraph) ) {
				
				logger.info("found identical GT .. deleting iGT and adjusting the goal graph "); 


				sdcGraph = sdcGraphManager.deleteIntersectionGT(theIGT, sdcGraph); 
				
				if ( sdcGraphManager.getSingleGoalGraphArc(oneGT, theChild, sdcGraph) == null ) {
					sdcGraph = sdcGraphManager.addGoalGraphArc(oneGT, theChild, sdcGraph, "subsume"); 
				}
				if ( sdcGraphManager.getSingleGoalGraphArc(otherGT, theChild, sdcGraph) == null ) {
					sdcGraph = sdcGraphManager.addGoalGraphArc(otherGT, theChild, sdcGraph, "subsume"); 
				}
				
			} else if ( matchmaker.similarityPlugin(theChild, theIGT, sdcGraph)) {
				
				logger.info("PLUGIN similarity ... adjusting the goal graph "); 

				sdcGraph = sdcGraphManager.addGoalGraphArc(theIGT, theChild, sdcGraph, "subsume"); 
				
				if ( !(sdcGraphManager.getSingleGoalGraphArc(oneGT, theChild, sdcGraph) == null) ) {
					sdcGraph = sdcGraphManager.deleteGoalGraphArc(sdcGraph,
							sdcGraphManager.getSingleGoalGraphArc(oneGT, theChild, sdcGraph) ); 
				}
				if ( !(sdcGraphManager.getSingleGoalGraphArc(otherGT, theChild, sdcGraph) == null) ) {
					sdcGraph = sdcGraphManager.deleteGoalGraphArc(sdcGraph,
							sdcGraphManager.getSingleGoalGraphArc(otherGT, theChild, sdcGraph) );
				}
				
				
			} else if ( matchmaker.similaritySubsume(theChild, theIGT, sdcGraph)) {
				
				logger.info("SUBSUME: add the iGT as a child node of " + theChild.getIdentifier()); 
				
				childNodeInsertion(theIGT, theChild, sdcGraph); 				
				
			} else if ( matchmaker.similarityIntersect(theChild, theIGT, sdcGraph)) {
				
				logger.info("found another i-arc in the goal graph .. "); 

				iArcResolution(theIGT, theChild, sdcGraph); 
				
			}
			
		}		
		
		childNodeDiscovery(theIGT, sdcGraph); 

		return sdcGraph;
	}

	/**
	 * creates the discovery cache (= WGMs) for a newly inserted goal template
	 * ensures that the created discovery cache is minimal  
	 * @param sdcGraph
	 * @param newGT
	 * @return the updated SDC Graph 
	 */
	private Ontology discoveryCacheArcCreation(Ontology sdcGraph, Goal newGT, String wsDirectory) {
		
		logger.info("creating the discovery cache ...");
				
//		Instance newGTInstance = sdcGraphManager.getSingleGoalTemplate(newGT, sdcGraph);
		List<Goal> allRootNodes = helper.getAllRootNodes(sdcGraph);
				
		if (allRootNodes.contains(newGT)) {
			sdcGraph = rootNodeDiscovery(newGT, sdcGraph, wsDirectory);			
		} else {
			sdcGraph = childNodeDiscovery(newGT, sdcGraph);
		}
		
		sdcGraph = removeRedundantDiscoveryCacheArcs(newGT, sdcGraph); 


		return sdcGraph;
	}
		
	

	/**
	 * removes redundant WGMs in order to maintain minimality of the discovery cache 
	 * (invoked after root node discovery)  
	 * @param newRoot
	 * @param sdcGraph
	 * @return the updated SDC graph 
	 */
	private Ontology removeRedundantDiscoveryCacheArcs(Goal newRoot, Ontology sdcGraph) {
		
		logger.info("removing redundant WGMs");
		
		List<WebService> theWS4RootExact = helper.getUsableWS4GT(sdcGraph, newRoot, "exact");
		List<WebService> theWS4RootPlugin = helper.getUsableWS4GT(sdcGraph, newRoot, "plugin");
		
		List<WebService> theWS4RootExactPlugin = new ArrayList<WebService>();		
		for (WebService aWS4RootExact : theWS4RootExact) {
			theWS4RootExactPlugin.add(aWS4RootExact); 
		}
		for (WebService aWS4RootPlugin: theWS4RootPlugin) {
			theWS4RootExactPlugin.add(aWS4RootPlugin); 
		}
		
		for (Goal theChild : helper.getAllChildNodes(newRoot, sdcGraph)) {

			logger.info("the investigated child : " + theChild.getIdentifier()); 

			for (WebService aWS : theWS4RootExactPlugin) {
				
	    		logger.info("investigated WS: " + aWS.getIdentifier() );
	    		
	    		List<Instance> DCArcs4WS = sdcGraphManager.getDiscoveryCacheArcByTarget(aWS, sdcGraph);
	    		for ( Instance aDCarc : DCArcs4WS ) {
		    		List<Instance> DCArcs4Child = sdcGraphManager.getDiscoveryCacheBySource(theChild, sdcGraph); 
		    		for (Instance aDCarc2 : DCArcs4Child) {
		    			if ( aDCarc2.equals(aDCarc) ) {
		    				sdcGraphManager.deleteDiscoveryCacheArc(sdcGraph,aDCarc2); 
		    			}
		    		}
	    		}
	    		
			}
					
		}
		
		return sdcGraph; 

/*
		
		for (Goal theChild : helper.getAllChildNodes(newRoot, sdcGraph)) {
						
//			IRI theChildID = (IRI)aChild.getIdentifier(); 
//			Goal theChild = wsmoFactory.createGoal(theChildID ); 
			
	    	for (WebService wsPlugin : theWS4RootPlugin) {
	    		logger.info("investigated WS: " + wsPlugin.getIdentifier() ); 

	    		List<Instance> theWGMs = sdcGraphManager.getDiscoveryCacheArcByTarget(wsPlugin, sdcGraph);
	    		List<Instance> theWGMs2 = sdcGraphManager.getDiscoveryCacheBySource(theChild, sdcGraph);
	    		
	    		
	    		for (Instance aWGM : theWGMs) {
	    			
	    			for (Instance aWGM2 : theWGMs2 ) {
	    				if ( aWGM.getIdentifier().equals(aWGM2.getIdentifier()) ) {
	    	    			System.out.println("the WGM ID: " + aWGM.getIdentifier().toString());
	    					sdcGraph = sdcGraphManager.deleteDiscoveryCacheArc(sdcGraph,aWGM);  

	    				}
	    			}
	    		}
	    	}
	    	
	    	for (WebService wsExact : theWS4RootExact) {
	    		List<Instance> theWGMsExact = sdcGraphManager.getDiscoveryCacheArcByTarget(wsExact, sdcGraph);
	    		List<Instance> theWGMs2Exact = sdcGraphManager.getDiscoveryCacheBySource(theChild, sdcGraph);
	    		
	    		// see explanation above 	    		
	    		for (Instance aWGM : theWGMsExact) {
	    			for (Instance aWGM2 : theWGMs2Exact ) {
	    				if ( aWGM.getIdentifier().equals(aWGM2.getIdentifier()) ) {
	    	    			System.out.println("the WGM ID: " + aWGM.getIdentifier().toString());
	    					sdcGraph = sdcGraphManager.deleteDiscoveryCacheArc(sdcGraph,aWGM);  
	    				}
	    			}
	    		}			
	    	}
	    	
		}

		return sdcGraph;
*/
	}


	/**
	 * Web Service usability determination for a goal template that is a root nodes
	 * @param newGT
	 * @param sdcGraph
	 * @param wsDirectory: the local file directory where all .wsml WS files are stored  
	 * @return the updated SDC Graph (with new WGMs)
	 */
	public Ontology rootNodeDiscovery(Goal newGT, Ontology sdcGraph, String wsDirectory) {
		
		logger.info(" ... for new root node "); 
		
		List<WebService> availableWS = helper.getAvailableWebServices(wsDirectory);
		
		List<Instance> existingGGM4newGT = sdcGraphManager.getGoalGraphArcBySource(newGT, sdcGraph);
		String usabilityDegree = null; 

		if (existingGGM4newGT.isEmpty()) {
			for (WebService theWS : availableWS) {
				logger.info("checking WS: " + theWS.getIdentifier()); 
				usabilityDegree = matchmakingUsability(newGT, theWS, sdcGraph);
				if (!(usabilityDegree == "disjoint") ) {
					sdcGraph = sdcGraphManager.addDiscoveryCacheArc(newGT, theWS, sdcGraph, usabilityDegree); 
				}
			}		
		}
		else {
		

		for (Goal theChild : helper.getChildrenFromGGM(sdcGraph, newGT)) {
						
			logger.info("using knowledge for child: " + theChild.getIdentifier());
			
			logger.info("checking for WS usable under degree EXACT "); 
			for ( WebService ws1 : helper.getUsableWS4GT(sdcGraph, theChild, "exact")) {
				sdcGraph = sdcGraphManager.addDiscoveryCacheArc(newGT, ws1, sdcGraph, "exact");
			}

			logger.info("checking for WS usable under degree SUBSUME"); 
			for ( WebService ws2 : helper.getUsableWS4GT(sdcGraph, theChild, "subsume")) {
				sdcGraph = sdcGraphManager.addDiscoveryCacheArc(newGT, ws2, sdcGraph, "subsume");				
			}
			
			logger.info("checking for WS usable under degree PLUGIN "); 
			for ( WebService ws3 : helper.getUsableWS4GT(sdcGraph, theChild, "plugin")) {
				
				boolean pluginFlag = false;  
				boolean subsumeFlag = false;
				
				usabilityDegree = "intersect"; 
				
				if ( matchmaker.usabilityPlugin(newGT, ws3, sdcGraph) ) {
					pluginFlag = true; 
					usabilityDegree = "plugin";  
				}
				
				if ( matchmaker.usabilitySubsume(newGT, ws3, sdcGraph) ) {
					subsumeFlag = true; 
					usabilityDegree = "subsume"; 
				}
				
				if ( pluginFlag && subsumeFlag ) {
					usabilityDegree  = "exact";
				} 
				
				sdcGraph = sdcGraphManager.addDiscoveryCacheArc(newGT, ws3, sdcGraph, usabilityDegree); 
								
			}
			
			logger.info("checking for WS usable under degree INTERSECT "); 
			for ( WebService ws4 : helper.getUsableWS4GT(sdcGraph, theChild, "intersect")) {
								
				if ( matchmaker.usabilityIntersect(newGT, ws4, sdcGraph) ) {
					usabilityDegree = "intersect";
				}
				if ( matchmaker.usabilitySubsume(newGT, ws4, sdcGraph) ) {
					usabilityDegree = "subsume";
				}

				sdcGraph = sdcGraphManager.addDiscoveryCacheArc(newGT, ws4, sdcGraph, usabilityDegree);
			 	
			}
			
			for ( WebService aNotYetCheckedWS : helper.notYetCheckedWS(newGT, sdcGraph, availableWS) ) {
				logger.info("not yet checked WS: " + aNotYetCheckedWS.getIdentifier() ); 
				usabilityDegree = matchmakingUsability(newGT, aNotYetCheckedWS, sdcGraph);
				if (!(usabilityDegree == "disjoint") ) {
					sdcGraph = sdcGraphManager.addDiscoveryCacheArc(newGT, aNotYetCheckedWS, sdcGraph, usabilityDegree); 
				}

			}
		
		}
		
		}
		
		return sdcGraph;
	}

	/**
	 * Web Service usability determination for a goal templates that is a child node 
	 * @param newGT
	 * @param sdcGraph 
	 * @return the updated SDC Graph (with new WGMs) 
	 */
	public Ontology childNodeDiscovery(Goal newGT, Ontology sdcGraph) {
		
		logger.info(" ... for new child node "); 
		
		String usabilityDegree = null; 

		for (Goal theParent : helper.getParentsFromGGM(sdcGraph, newGT) ) { 
			
			logger.info("using knowledge from parent : " + theParent.getIdentifier()); 

			logger.info("WS usable under EXACT or PLUGIN are omitted "); 

			logger.info("checking for WS usable under degree SUBSUME "); 
			for ( WebService ws1 : helper.getUsableWS4GT(sdcGraph,theParent, "subsume") ) {
				
				logger.info("checking WS: " + ws1.getIdentifier()); 
				usabilityDegree = matchmakingUsability(newGT,ws1, sdcGraph); 
				
				if (!(usabilityDegree == "disjoint") ) {
					/*
					 * NOTE: only works when disabling "WGM exists already" check in Graph Manager 
					 */
					sdcGraph = sdcGraphManager.addDiscoveryCacheArc(newGT, ws1, sdcGraph, usabilityDegree); 
				}
			}
			
			logger.info("checking for WS usable under degree INTERSECT"); 
			for (WebService ws2 : helper.getUsableWS4GT(sdcGraph,theParent, "intersect") ) {

				logger.info("checking WS: " + ws2.getIdentifier()); 
				
				usabilityDegree = "disjoint"; 

				if ( matchmaker.usabilityIntersect(newGT, ws2, sdcGraph) ) {
					usabilityDegree = "intersect"; 
				}
				if ( matchmaker.usabilityPlugin(newGT, ws2, sdcGraph) ) {
					usabilityDegree = "plugin"; 
				}

				if (!(usabilityDegree == "disjoint") ) {
					sdcGraph = sdcGraphManager.addDiscoveryCacheArc(newGT, ws2, sdcGraph, usabilityDegree);
				}
			}	
		}

		return sdcGraph;
	}

	
	
	/**
	 * determines usability degree without any pre-knowledge 
	 * @param goaltemplate
	 * @param webservice
	 * @return String - the usability degree 
	 */
	public String matchmakingUsability(Goal goaltemplate, WebService webservice, Ontology sdcGraph) {
		
		String degree = "disjoint";
		
		boolean pluginFlag = false;  
		boolean subsumeFlag = false;  
		
		if ( matchmaker.usabilityIntersect(goaltemplate, webservice, sdcGraph) ) {
			degree = "intersect";
		}
		if ( matchmaker.usabilityPlugin(goaltemplate, webservice, sdcGraph) ) {
			pluginFlag = true; 
			degree = "plugin";  
		}
		
		if ( matchmaker.usabilitySubsume(goaltemplate, webservice, sdcGraph) ) {
			subsumeFlag = true; 
			degree = "subsume"; 
		}
		
		if ( pluginFlag && subsumeFlag ) {
			degree = "exact";
		} 
		
		
		return degree;
	}


	


	/*
	 * for testing during development only 
	 */
//	public static void main(String[] args){
//		
//		System.out.println("moin, this is the SDCGraphCreator"); 
//		
//		SDCGraphCreator theCreator = new SDCGraphCreator(); 
///* 		
//		// Testing get all root nodes 
//		Ontology theGraph = theCreator.sdcResourceManager.loadSDCGraphSchema(); 
//		
//		Goal gt1 = theCreator.wsmoFactory.createGoal(theCreator.wsmoFactory.createIRI("http://me.org.org/gt1"));
//		Goal gt2 = theCreator.wsmoFactory.createGoal(theCreator.wsmoFactory.createIRI("http://me.org.org/gt2"));
//		Goal gt3 = theCreator.wsmoFactory.createGoal(theCreator.wsmoFactory.createIRI("http://me.org.org/gt3"));
//		
//		theCreator.sdcGraphManager.addGoalTemplate(gt1, theGraph, "root"); 
//		theCreator.sdcGraphManager.addGoalTemplate(gt2, theGraph, "child"); 
//		theCreator.sdcGraphManager.addGoalTemplate(gt3, theGraph, "root"); 
//
//		System.out.println("all goal templates"); 
//		for (Instance i : theCreator.sdcGraphManager.getAllGoalTemplates(theGraph)) {
//			System.out.println(i.getIdentifier()); 
//		}
//
//		System.out.println("all root nodes"); 
//		for (Instance i : theCreator.getAllRootNodes(theGraph)) {
//			System.out.println(i.getIdentifier()); 
//		}
//*/
//
//		Ontology theSDCGraph = theCreator.sdcResourceManager.loadSDCGraphSchema(); 
//
//    	Goal gtRoot = theCreator.sdcResourceManager.loadGoalTemplate("gtRoot.wsml"); 
//    	Goal gt2 = theCreator.sdcResourceManager.loadGoalTemplate("gtUS2world.wsml");
//
///*
//    	theCreator.sdcGraphManager.addGoalTemplate(gtRoot, theSDCGraph, "root");
//    	
//    	for (Instance i : theCreator.sdcGraphManager.getAllGoalTemplates(theSDCGraph)) {
//    		System.out.println("all existing GTs: \n" + i.getIdentifier().toString());
//    	}
//
//    	theCreator.sdcGraphManager.deleteGoalTemplate(gtRoot, theSDCGraph); 
//
//    	for (Instance i : theCreator.sdcGraphManager.getAllGoalTemplates(theSDCGraph)) {
//    		System.out.println("all existing GTs: \n" + i.getIdentifier().toString());
//    	}
//    		
//*/
//    	
//    	String theWSdirectory = "D:/DERI/phd-thesis/repository/stolle/usecases/SWS-challenge/own-modelling/WSML/webservices/"; 
//    	
//    	List<WebService> theWSs = theCreator.helper.getAvailableWebServices(theWSdirectory); 
//    	
//    	for ( WebService ws : theWSs ) {
//    		System.out.println(ws.getIdentifier()); 
//    	}
//    	 
//		 
//	}

}
