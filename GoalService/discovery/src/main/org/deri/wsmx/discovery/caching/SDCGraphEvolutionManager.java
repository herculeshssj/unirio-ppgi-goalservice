/**
 * Provides the methods for evolution of the SDC Graph in its dynamic environment 
 * - for Goal Templates: removal & modification  (insertion is part of @see SDCGraphCreator) 
 * - for Web Services: insertion, removal, update
 *  
 * @author Michael Stollberg
 * 
 * @version $Revision: 1.7 $ $Date: 2007-10-12 16:58:53 $
 */

package org.deri.wsmx.discovery.caching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmx.discovery.caching.matchmaking.Matchmaking4SWSC;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Identifier;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

public class SDCGraphEvolutionManager {
	
	protected static Logger logger;
	
 	private WsmoFactory wsmoFactory;
	private DataFactory dataFactory; 
	
	private SDCGraphManager graphManager; 
	private SDCResourceManager resourceManager;
	private SDCGraphCreatorHelper helper; 
	private SDCGraphCreator creator; 
	private Matchmaking4SWSC matchmaker; 
	
	
	public SDCGraphEvolutionManager(){
		
		logger = Logger.getLogger(SDCGraphEvolutionManager.class);
		
		wsmoFactory = Factory.createWsmoFactory(null);
		dataFactory = Factory.createDataFactory(new HashMap()); 

		
		graphManager = new SDCGraphManager(); 
		resourceManager = new SDCResourceManager();
		helper = new SDCGraphCreatorHelper();
		creator = new SDCGraphCreator(); 
		matchmaker = new Matchmaking4SWSC(); 
	}
	
	/**
	 * removes a goal template from the SDC Graph 
	 * @param goalTemplate
	 * @param sdcGraph
	 * @return the updated SDC Graph 
	 */
	public Ontology removeGoalTemplate(Goal goalTemplate, Ontology sdcGraph) {
		
		logger.info("removing goal template: " + goalTemplate.getIdentifier() ); 
				
		List<Goal> theChildren = helper.getChildrenFromGGM(sdcGraph, goalTemplate); 
		
		for ( Goal aChild : theChildren ) {
			
			if ( graphManager.isIntersectionGT(aChild, sdcGraph) ) {
				sdcGraph = removeIntersectGTofChild(goalTemplate, aChild, sdcGraph); 
			}

		}
		
		
		if ( helper.getAllRootNodes(sdcGraph).contains(goalTemplate) ) {

			sdcGraph = adjustmentRootGTremoval(goalTemplate,sdcGraph);
			
		} else {
			
			logger.info(" the GT is a child node ... " ); 
			
			sdcGraph = adjustmentChildIntersectGTremoval(goalTemplate,sdcGraph);
			
			logger.info("removing the outgoing goal graphs arcs of removed GT "); 
			
			for ( Instance outGGM : graphManager.getGoalGraphArcBySource(goalTemplate, sdcGraph) ) {
				
				sdcGraph = graphManager.deleteGoalGraphArc(sdcGraph, outGGM); 
				
			}

			
		}
				
		logger.info("deleting discovery cache for removed GT"); 
		
		List<Instance> allWGM4GT = graphManager.getDiscoveryCacheBySource(goalTemplate, sdcGraph);
		
		for (Instance aWGM : allWGM4GT) {
			sdcGraph = graphManager.deleteDiscoveryCacheArc(sdcGraph, aWGM); 
		}
		
		sdcGraph = graphManager.deleteGoalTemplate(goalTemplate, sdcGraph);
		
		

		
		return sdcGraph;
		
	}
	

	private Ontology removeIntersectGTofChild(Goal goalTemplate, Goal intersectionGT, Ontology sdcGraph) {
				
		sdcGraph = graphManager.deleteIntersectionGT(intersectionGT, sdcGraph); 

		return sdcGraph;
	}

	/**
	 * handles SDC Graph adjustment for removal of a root GT 
	 * @param goalTemplate
	 * @param sdcGraph
	 * @return the updated SDC Graph 
	 */
	private Ontology adjustmentRootGTremoval(Goal goalTemplate, Ontology sdcGraph) {
		
		logger.info("removed GT is a root node ... adjusting SDC Graph"); 
		
		for ( Goal aChild : helper.getAllChildNodes(goalTemplate, sdcGraph) ) {
			
			logger.info("inspecting child node: " + aChild.getIdentifier()); 
					
			List<WebService> services = new ArrayList<WebService>();
			
			List<WebService> servicesExact = helper.getUsableWS4GT(sdcGraph, goalTemplate, "exact"); 
			List<WebService> servicesPlugin = helper.getUsableWS4GT(sdcGraph, goalTemplate, "plugin"); 
			
			for (WebService aWSexact : servicesExact ) {
				services.add(aWSexact); 
			}
			for (WebService aWSplugin: servicesPlugin ) {
				services.add(aWSplugin); 
			}
			
			// re-materialization of omitted plugin-WGM
			for ( WebService aWS : services) {
				sdcGraph = graphManager.addDiscoveryCacheArc(aChild, aWS, sdcGraph, "plugin"); 
			}
			
			// all child nodes become root nodes
			logger.info("position becomes 'root' "); 
			Instance theChildGTInstance = helper.setNewPosition(aChild, "root", sdcGraph);
		}
		
		
		// removing all outgoing discovery cache arcs 
		for ( Instance aWGM : graphManager.getDiscoveryCacheBySource(goalTemplate, sdcGraph)){
			sdcGraph = graphManager.deleteDiscoveryCacheArc(sdcGraph, aWGM); 
		}

		// removing all outgoing goal graph arcs 
		for ( Instance aGGM : graphManager.getGoalGraphArcBySource(goalTemplate, sdcGraph)){
			sdcGraph = graphManager.deleteGoalGraphArc(sdcGraph, aGGM); 
		}


		return sdcGraph;
	}

	/**
	 * handles the SDC Graph maintenance for removing a child GT 
	 * @param goalTemplate
	 * @param sdcGraph
	 * @return the updated SDC Graph 
	 */
	private Ontology adjustmentChildIntersectGTremoval(Goal goalTemplate, Ontology sdcGraph) {
		
		logger.info(" ... adjusting SDC Graph"); 

		/*
		 * for adjusting the discovery cache: 
		 * - explicate usability of WS under e / p degree for removed GT to its children 
		 * - all other usability degrees remain the same 
		 */  
				
		List<WebService> services = new ArrayList<WebService>();
		List<WebService> servicesExact = helper.getUsableWS4GT(sdcGraph, goalTemplate , "exact"); 
		List<WebService> servicesPlugin = helper.getUsableWS4GT(sdcGraph, goalTemplate , "plugin"); 

		for (WebService aWSexact : servicesExact ) {
			services.add(aWSexact); 
		}
		for (WebService aWSplugin: servicesPlugin ) {
			services.add(aWSplugin); 
		}
				

		
		for ( Goal aParentOfGT : helper.getParentsFromGGM(sdcGraph, goalTemplate) ) {
			
			logger.info("currently inspected parent: "+ aParentOfGT.getIdentifier()); 
			
			for ( Goal aChildOfGT : helper.getAllChildNodes(goalTemplate, sdcGraph) ) {

				logger.info("currently inspected child: "+ aChildOfGT.getIdentifier()); 
				
				// 1. explicate e / p usability to child nodes
				logger.info("explicate e / p usability to child node: "+ aChildOfGT.getIdentifier()); 
				
				for ( WebService ws : services ) {
					sdcGraph = graphManager.addDiscoveryCacheArc(aChildOfGT, ws, sdcGraph, "plugin"); 
				
				}
							
				
				// 2. all children of removed GT become direct children of the parent of the removed GT 
				logger.info("all children of removed GT become direct children of the parent of the removed GT" );
								
				sdcGraph = graphManager.addGoalGraphArc(aParentOfGT, aChildOfGT, sdcGraph, "subsume");
				
			}
			
			// deleting the incoming arcs of the removed GT
			
			logger.info("deleting the incoming arcs of the removed GT"); 
			
			sdcGraph = graphManager.deleteGoalGraphArc(sdcGraph,
					graphManager.getSingleGoalGraphArc(aParentOfGT, goalTemplate, sdcGraph) ); 

		}

		
						
		return sdcGraph;

	}

	
	/**
	 * handles the modification of a goal template in the SDC Graph 
	 * @param goalTemplate
	 * @param sdcGraph
	 * @return the updated SDC Graph 
	 */
	public Ontology modifyGoalTemplate(Goal GTpreviousVersion, Goal GTnewVersion, String wsDirectory, Ontology sdcGraph) {
		
		logger.info("modifying a goal template: " +
				"\n previous version:  " + GTpreviousVersion.getIdentifier() + 
				"\n new version:  " + GTnewVersion.getIdentifier());
		

		List<Goal> rootNodes = helper.getAllRootNodes(sdcGraph); 
		
		if ( rootNodes.contains(GTpreviousVersion) ) {
			
			boolean plugin4SingleRoot = false; 
			
			if ( matchmaker.similaritySubsume(GTnewVersion, GTpreviousVersion, sdcGraph) ) {
				plugin4SingleRoot = true; 
			}

			rootNodes.remove(GTpreviousVersion); 
			
			for ( Goal otherRoot : rootNodes ) {
				
				if ( matchmaker.similarityIntersect(GTnewVersion, otherRoot, sdcGraph) ) {
					
					plugin4SingleRoot = false; 
					
				}
			}
			
			if (plugin4SingleRoot) {
				
				logger.info("becomes a new root node ... "); 
								
				sdcGraph = graphManager.addGoalTemplate(GTnewVersion, sdcGraph, "root");
				
				for ( Goal aChild : helper.getAllChildNodes(GTpreviousVersion, sdcGraph)) {
					sdcGraph = graphManager.addGoalGraphArc(GTnewVersion, aChild, sdcGraph, "subsume"); 
				}
				
				
				for ( Instance outGGMprevious : graphManager.getGoalGraphArcBySource(GTpreviousVersion, sdcGraph) ) {
					sdcGraph = graphManager.deleteGoalGraphArc(sdcGraph, outGGMprevious); 
				}
			
				sdcGraph = graphManager.deleteGoalTemplate(GTpreviousVersion, sdcGraph); 
				
			}
			
			
		} 
		else if (
				helper.getChildrenFromGGM(sdcGraph, GTpreviousVersion).isEmpty() && 
				matchmaker.similaritySubsume(GTpreviousVersion, GTnewVersion, sdcGraph)
		) {

			logger.info("becomes a new lowest child node ... "); 
			
			sdcGraph = graphManager.addGoalTemplate(GTnewVersion, sdcGraph, "child");
			
			for ( Goal aParent : helper.getParentsFromGGM(sdcGraph, GTpreviousVersion) ) {
				sdcGraph = graphManager.addGoalGraphArc(aParent, GTnewVersion, sdcGraph, "subsume"); 
			}
			
			sdcGraph = creator.childNodeDiscovery(GTnewVersion, sdcGraph); 
				
			for ( Instance inGGMprevious : graphManager.getGoalGraphArcByTarget(GTpreviousVersion, sdcGraph) ) {
				sdcGraph = graphManager.deleteGoalGraphArc(sdcGraph,inGGMprevious); 
			}
			
			List<Instance> theWGMs4PreviousVersion = graphManager.getDiscoveryCacheBySource(GTpreviousVersion, sdcGraph); 
			
			for (Instance aWGM4PreviousVersion : theWGMs4PreviousVersion ) {
				sdcGraph = graphManager.deleteDiscoveryCacheArc(sdcGraph, aWGM4PreviousVersion); 
			} 
			
			sdcGraph = graphManager.deleteGoalTemplate(GTpreviousVersion, sdcGraph); 
						
		} else {
			
			logger.info("default situation: first remove previous, then add new version "); 
			
			logger.info("removing previous version .. "); 
			sdcGraph = removeGoalTemplate(GTpreviousVersion, sdcGraph);
			
			logger.info("inserting new version .. "); 
			sdcGraph = creator.insertGoalTemplate(sdcGraph, GTnewVersion, wsDirectory); 
			
		}
		
		
		return sdcGraph;
		
	}
	
	/**
	 * adds a new Web service to the SDC Graph 
	 * @param newWS
	 * @param sdcGraph
	 * @return the updated SDC Graph 
	 */
	public Ontology insertWebService(WebService newWS, Ontology sdcGraph) {
		
		logger.info("inserting new WS: " + newWS.getIdentifier() );
		
		for (Goal aRoot: helper.getAllRootNodes(sdcGraph)) { 
			
			String usabilityDegree = creator.matchmakingUsability(aRoot, newWS, sdcGraph);

			if (!(usabilityDegree == "disjoint") ) {

				sdcGraph = graphManager.addDiscoveryCacheArc(aRoot, newWS, sdcGraph, usabilityDegree);
				
				if (usabilityDegree == "subsume" | usabilityDegree == "intersect" ) {
				
					sdcGraph = childNodeWSInsertion(aRoot,newWS,sdcGraph,usabilityDegree); 
				}
			}
		}
		
		return sdcGraph;
		
	}
	
	/**
	 * helper method for 'insertWebService' 
	 * @param theParent
	 * @param newWS
	 * @param sdcGraph
	 * @param usability4Parent
	 * @return the updated SDC Graph 
	 */
	private Ontology childNodeWSInsertion(Goal theParent, WebService newWS, Ontology sdcGraph, String usability4Parent) {
		
		String usability4Child = null;
				
		for (Goal aChild: helper.getAllChildNodes(theParent, sdcGraph)) {
			
			if ( graphManager.isIntersectionGT(aChild, sdcGraph) ) {
				
				logger.info("... is iGT " + aChild.getIdentifier());			
				
				for (Instance aDCarc : graphManager.getDiscoveryCacheBySource(aChild, sdcGraph)) {
					
					if ( graphManager.getDiscoveryCacheArcTarget(sdcGraph, aDCarc).equals(newWS) ) {
						
						logger.info("... a respective arc already exists: " +  aDCarc.getIdentifier() );
						return sdcGraph; 
					} 
				}
			}
			
			if (usability4Parent == "subsume") {
				
				usability4Child = creator.matchmakingUsability(aChild, newWS, sdcGraph);
				
				if (!(usability4Child == "disjoint") ) {
					
						sdcGraph = graphManager.addDiscoveryCacheArc(aChild, newWS, sdcGraph, usability4Child);						
			
				}
			}
				
			if (usability4Parent == "intersect") {
				
				if ( matchmaker.usabilityIntersect(aChild, newWS, sdcGraph)) {
					usability4Child = "intersect"; 
				}
				if ( matchmaker.usabilityPlugin(aChild, newWS, sdcGraph)) {
					usability4Child = "plugin"; 
				}

					sdcGraph = graphManager.addDiscoveryCacheArc(aChild, newWS, sdcGraph, usability4Child);					
				
			}
				
			childNodeWSInsertion(aChild, newWS, sdcGraph, usability4Child);
								
		}
		
		
		return sdcGraph;
	}


	/**
	 * removes a Web service from the SDC Graph 
	 * @param webService
	 * @param sdcGraph
	 * @return the updated SDC Graph
	 */
	public Ontology removeWebService(WebService webService, Ontology sdcGraph) {
		
		logger.info("removing WS: " + webService.getIdentifier() );
		
		if ( graphManager.getDiscoveryCacheArcByTarget(webService, sdcGraph).isEmpty() ) {
			logger.info("the WS is not in the SDC Graph"); 
		}
		
		logger.info("removing all discovery cache arcs for WS"); 
		
		for ( Instance aWGM4WS : graphManager.getDiscoveryCacheArcByTarget(webService, sdcGraph)) {
			sdcGraph = graphManager.deleteDiscoveryCacheArc(sdcGraph, aWGM4WS); 
		}
		
		List<Goal> GTsWithoutWS = new ArrayList<Goal>();
		
		for ( Goal aRoot: helper.getAllRootNodes(sdcGraph) ) {
			if ( graphManager.getDiscoveryCacheBySource(aRoot, sdcGraph).isEmpty() ) {
				GTsWithoutWS.add(aRoot); 
			}
			
		logger.info("\n WARNING: due to the deletion, there are not more usable Web serices " +
				"\n for the following root goal templates and, in consequence, all their children: " ); 
		
		for (Goal aRootWithoutWS: GTsWithoutWS ) {
			logger.info(aRoot.getIdentifier()); 
		}
						
		
		}

		
		return sdcGraph;
		
	}
	
	/**
	 * handles the modification of a Web service in the SDC Graph 
	 * @param webService
	 * @param sdcGraph
	 * @return the updated SDC Graph
	 */
	public Ontology modifyWebService(WebService WSpreviousVersion, WebService WSnewVersion, Ontology sdcGraph) {
		
		logger.info("modifying a Web Service: " +
				"\n previous version: " + WSpreviousVersion.getIdentifier()  + 
				"\n new version: " + WSnewVersion.getIdentifier() );
		
		List<Goal> rootNodes = helper.getAllRootNodes(sdcGraph); 
		
		for ( Goal aRoot : rootNodes ) {
			
			logger.info("checking whether new version is usable under e / p degree for a root GT ") ; 
			
			if ( matchmaker.usabilityPlugin(aRoot, WSnewVersion, sdcGraph) ) {
				
				if ( matchmaker.usabilitySubsume(aRoot, WSnewVersion, sdcGraph) ) {
					
					sdcGraph = graphManager.addDiscoveryCacheArc(aRoot, WSnewVersion, sdcGraph, "exact"); 
					
				} else {
					
					sdcGraph = graphManager.addDiscoveryCacheArc(aRoot, WSnewVersion, sdcGraph, "plugin"); 
			
				}
				
				if ( graphManager.getSingleDiscoveryCacheArc(aRoot, WSpreviousVersion, sdcGraph) != null ) {

					sdcGraph = graphManager.deleteDiscoveryCacheArc(sdcGraph,
							graphManager.getSingleDiscoveryCacheArc(aRoot, WSpreviousVersion, sdcGraph) );

				}
				
				

			} else {
				
				logger.info("default behavior: first remove previous version, then insert new version ... ");
				
				logger.info(" ... remove previous version of WS "); 
				sdcGraph = removeWebService(WSpreviousVersion, sdcGraph); 
				
				logger.info(" ... adding new version of WS ");
				sdcGraph = insertWebService(WSnewVersion, sdcGraph); 
				
			}
		
			
		}
		
	
		return sdcGraph;
		
	}
	

}
