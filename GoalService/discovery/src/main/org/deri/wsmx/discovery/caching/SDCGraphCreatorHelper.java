/**
 * provides helper methods for the SDCGraphCreator 
 * 
 * @author Michael Stollberg
 * @version $Revision: 1.9 $ $Date: 2007-10-08 22:33:45 $
 */

package org.deri.wsmx.discovery.caching;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.common.ClearTopEntity;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

public class SDCGraphCreatorHelper {
	
 	protected static Logger logger;

 	private WsmoFactory wsmoFactory;
	private DataFactory dataFactory; 
	
	private SDCGraphManager sdcGraphManager;  
	private SDCResourceManager sdcResourceManager; 
	
	public SDCGraphCreatorHelper(){
		logger = Logger.getLogger(SDCGraphCreator.class);

		wsmoFactory = Factory.createWsmoFactory(null);
		dataFactory = Factory.createDataFactory(new HashMap()); 

		sdcGraphManager = new SDCGraphManager();
		sdcResourceManager = new SDCResourceManager();
	}

	/**
	 * gets all available Web services (from a directory on the local machine) 
	 * @param directoryStr
	 * @return List of Web services 
	 */
	public List<WebService> getAvailableWebServices(String directoryStr) {
		
		logger.info("loading all Web Services from given directory "); 
		
		File wsmlWSDir = new File(directoryStr);	
		List<WebService> availableWS = new ArrayList<WebService>();

		if (!wsmlWSDir .exists()) {
			logger.info("Background "+ directoryStr + " directory does not exist. Skipping loading of background ontologies.");
			return availableWS;
		} 
		
		if (!wsmlWSDir .canRead()) {
			logger.info("Background " + directoryStr + " directory exist but is not readable. " +
					"Check file system permissions.");			
			return availableWS;
		}
		
		File[] wsmlFiles = wsmlWSDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith(".wsml"))
					return true;
				return false;
			}
		});
		
		for (File file : wsmlFiles) {
			availableWS.add(sdcResourceManager.loadWSfromDirectory(directoryStr, file.getName().toString())); 
		} 
				
		return availableWS;
	}

	/**
	 * gets all goal templates with position 'root' 
	 * @param sdcGraph
	 * @return List of Goal Templates 
	 */
	public List<Goal> getAllRootNodes(Ontology sdcGraph) {		
    	
		List<Instance> currentGTs = sdcGraphManager.getAllGoalTemplates(sdcGraph);
		
		List<Goal> theRootGTs = new ArrayList<Goal>();

		Identifier rootPosition = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "root");
		
		Goal aRoot = null; 
		
		for (Instance aGT : currentGTs) {
			
			Set<Instance> thePositionAttr = new HashSet<Instance>();
			Set<Value> temp = aGT.listAttributeValues(wsmoFactory.createIRI(
					sdcGraph.getDefaultNamespace(), "position"));
			for (Value v: temp)
				thePositionAttr.add((Instance)v);
			
//			Set<Instance> thePositionAttr = aGT.listAttributeValues(
//						wsmoFactory.createIRI(
//								sdcGraph.getDefaultNamespace(), "position"));
			for (Instance theOne : thePositionAttr ){
				if ( theOne.getIdentifier().equals(rootPosition) ){

					Set<String> theIDs = new HashSet<String>();
					Set<Value> temp2 = aGT.listAttributeValues(wsmoFactory.createIRI(
							sdcGraph.getDefaultNamespace(), "description"));
					for (Value v: temp2)
						theIDs.add(v.toString());
					
//		    		Set<String> theIDs = aGT.listAttributeValues(wsmoFactory.createIRI(
//		    				sdcGraph.getDefaultNamespace(), "description"));
		    		Iterator theIDsIterator = theIDs.iterator();
		    		String theGoalID = theIDsIterator.next().toString(); 
		    		aRoot = wsmoFactory.createGoal(wsmoFactory.createIRI(theGoalID)); 
					
					theRootGTs.add(aRoot);
				}
			}
		} 
			
		return theRootGTs;  
    	
	}
	
	/**
	 * gets all goal templates that are children of the input goal template 
	 * @param theParent
	 * @param sdcGraph
	 * @return List of Goal Templates
	 */
	public List<Goal> getAllChildNodes(Goal theParent,Ontology sdcGraph) {
		
		List<Instance> theGGMbySource = sdcGraphManager.getGoalGraphArcBySource(theParent, sdcGraph); 
		
		List<Goal> theChildNodes = new ArrayList<Goal>();
		
		Goal aChild = null; 

		for (Instance aChildInstance : theGGMbySource) {
			
			Set<String> theIDs = new HashSet<String>();
			Set<Value> temp2 = aChildInstance.listAttributeValues(wsmoFactory.createIRI(
					sdcGraph.getDefaultNamespace(), "targetGT"));
			for (Value v: temp2)
				theIDs.add(v.toString());

//    		Set<String> theIDs = aChildInstance.listAttributeValues(wsmoFactory.createIRI(
//    				sdcGraph.getDefaultNamespace(), "targetGT"));
    		Iterator theIDsIterator = theIDs.iterator();
    		String theGoalID = theIDsIterator.next().toString();
    		aChild = wsmoFactory.createGoal(wsmoFactory.createIRI(theGoalID)); 
    		theChildNodes.add(aChild); 
			
		}
    				
		return theChildNodes;  
    	
	}
	
	/**
	 * gets the goal templates that are parents of the input goal template  
	 * @param sdcGraph
	 * @param theChild
	 * @return List of Goal Templates
	 */
	public List<Goal> getParentsFromGGM(Ontology sdcGraph, Goal theChild) {
		
		List<Instance> existingGGM4newGT = sdcGraphManager.getGoalGraphArcByTarget(theChild, sdcGraph);
		
		List<Goal> theParents = new ArrayList<Goal>();
		
		Goal aParent = null; 
		
		for (Instance aGGM : existingGGM4newGT) {	
			
			Set<String> theSources = new HashSet<String>();
			Set<Value> temp2 = aGGM.listAttributeValues(wsmoFactory.createIRI(
					sdcGraph.getDefaultNamespace(), "sourceGT"));
			for (Value v: temp2)
				theSources.add(v.toString());
			
//    		Set<String> theSources = aGGM.listAttributeValues(
//					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "sourceGT")); 
    		
    		Iterator theIDsIterator = theSources.iterator();
    		String theGoalID = theIDsIterator.next().toString();
    		aParent = wsmoFactory.createGoal(wsmoFactory.createIRI(theGoalID)); 
			theParents.add(aParent);
			
		}
		
		return theParents; 

	}
	
	public Instance setNewPosition( Goal goalTemplate, String newPositionValue, Ontology sdcGraph ) {
		
		Instance theGTInstance = sdcGraphManager.getSingleGoalTemplate(goalTemplate, sdcGraph);
		
		Identifier positionAttrID = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "position");
		
		Identifier positionIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), newPositionValue); 
		
		try {

			theGTInstance.removeAttributeValues(positionAttrID);
			
			theGTInstance.addAttributeValue(positionAttrID,wsmoFactory.getInstance(positionIRI) ); 

			
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
						
		
		return theGTInstance; 
		
	}
	

	/**
	 * gets all goal templates that are children of the input goal template
	 * @param sdcGraph
	 * @param theParent
	 * @return List of Goal Templates
	 */
	public List<Goal> getChildrenFromGGM(Ontology sdcGraph, Goal theParent) {
		
		List<Instance> existingGGM4newGT = sdcGraphManager.getGoalGraphArcBySource(theParent, sdcGraph);
		
		List<Goal> theChildren = new ArrayList<Goal>();

		Goal aChild  = null; 
		
		for (Instance aGGM : existingGGM4newGT) {	
			Set<String> theSources = new HashSet<String>();
			Set<Value> temp = aGGM.listAttributeValues(wsmoFactory.createIRI(
					sdcGraph.getDefaultNamespace(), "targetGT"));
			for (Value v: temp)
				theSources.add(v.toString());
			
//    		Set<String> theSources = aGGM.listAttributeValues(
//					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "targetGT")); 
    		
    		Iterator theIDsIterator = theSources.iterator();
    		String theGoalID = theIDsIterator.next().toString();
    		aChild = wsmoFactory.createGoal(wsmoFactory.createIRI(theGoalID)); 
    		theChildren.add(aChild);
			
		}
		
		return theChildren; 

	}
	
	
	/**
	 * returns a WSMO Goal Object for an intersection goal template 
	 * @param parent1
	 * @param parent2
	 * @param sdcGraph
	 * @return Goal (for the intersection goal template) 
	 */
	public Goal getIntersectionGT(Goal parent1, Goal parent2, Ontology sdcGraph ) {
		
//		logger.info("the a Goal object for iGT of \n " + parent1.getIdentifier() + "\n" + parent2.getIdentifier() ); 
		
		Goal theIGT = null;
		
		List<Goal> childrenParent1 = getChildrenFromGGM(sdcGraph, parent1); 
		List<Goal> childrenParent2 = getChildrenFromGGM(sdcGraph, parent2);
		
		for (Goal parent1Child : childrenParent1 ) {
				
			for (Goal parent2Child : childrenParent2) {

				if (parent1Child.equals(parent2Child)) {
					theIGT = parent1Child; 
				}
			}
		}
		
//		logger.info("the created Goal object: " + theIGT.getIdentifier()); 
				

//		List<Instance> outgoingArcsParent1 = sdcGraphManager.getGoalGraphArcBySource(parent1, sdcGraph); 
//		List<Instance> outgoingArcsParent2 = sdcGraphManager.getGoalGraphArcBySource(parent2, sdcGraph);
//		
//		for (Instance anArcParent1: outgoingArcsParent1 ) {
//			
//    		Set<Object> theTargetsParent1 = anArcParent1.listAttributeValues(
//					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "targetGT"));
//    		
//    		for (Object aTargetParent1 : theTargetsParent1 ) {
//    			
//    			for (Instance anArcParent2: outgoingArcsParent2 ) {
//    				
//    	    		Set<Object> theTargetsParent2 = anArcParent2.listAttributeValues(
//    						wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "targetGT"));
//
//    	    		for (Object aTargetParent2 : theTargetsParent2 ) {
//    	    			
//    	    			if ( aTargetParent1.equals(aTargetParent2) ) {
//    	    				String theID = String.valueOf(aTargetParent1); 
//    	    				
//    	    				logger.info("the found ID is: " + theID); 
//    	    				
//    	    	    		theIGT = wsmoFactory.createGoal(wsmoFactory.createIRI(theID));
//    	    			}
//    	    		}
//    			}
//    		}
//		} 
		
		return theIGT; 
	}

	/**
	 * gets all Web services that are usable for a goal template under a certain usability degree 
	 * @param sdcGraph
	 * @param theGT
	 * @param usabilityDregree
	 * @return List of Web Services 
	 */
	public List<WebService> getUsableWS4GT(Ontology sdcGraph, Goal theGT, String usabilityDregree) {

		List<Instance> theWGMforTheGT = sdcGraphManager.getDiscoveryCacheBySource(theGT, sdcGraph);

		List<WebService> services = new ArrayList<WebService>();
		
		WebService aWS = null; 
		
		for (Instance aWGM : theWGMforTheGT) {
			
			Set<Instance> theUsabilityDegrees = new HashSet<Instance>();
			Set<Value> temp = aWGM.listAttributeValues(wsmoFactory.createIRI(
					sdcGraph.getDefaultNamespace(), "usability"));
			for (Value v: temp)
				theUsabilityDegrees.add((Instance)v);

//			Set<Instance> theUsabilityDegrees = aWGM.listAttributeValues(
//					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "usability")); 

			Set<String> theSources = new HashSet<String>();
			Set<Value> temp2 = aWGM.listAttributeValues(wsmoFactory.createIRI(
					sdcGraph.getDefaultNamespace(), "targetWS"));
			for (Value v: temp2)
				theSources.add(v.toString());

//    		Set<String> theSources = aWGM.listAttributeValues(
//					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "targetWS")); 

			for (Instance anInstance : theUsabilityDegrees) {
												
				Identifier usabilityIRI = wsmoFactory.createIRI(
						sdcGraph.getDefaultNamespace(), usabilityDregree);
				
				if (anInstance.getIdentifier().equals(usabilityIRI)) {
							    		
		    		Iterator theIDsIterator = theSources.iterator();
		    		String theWSID = theIDsIterator.next().toString();
		    		aWS = wsmoFactory.createWebService(wsmoFactory.createIRI(theWSID));
		    		services.add(aWS); 
					
				}
			}
		}
		
		return services; 
		
	}

	/**
	 * gets all Web services that have not yet been checked 
	 * (helper method for rootNodeDiscovery) 
	 * @param goaltemplate
	 * @param sdcGraph
	 * @param availableWS
	 * @return List of not yet checked Web services  
	 */
	public List<WebService> notYetCheckedWS(Goal goaltemplate, Ontology sdcGraph, List<WebService> availableWS) {
		
		
		List<Instance> allWGM4GT = sdcGraphManager.getDiscoveryCacheBySource(goaltemplate, sdcGraph);
		
		WebService aWS = null; 
		
		for (Instance aWGM : allWGM4GT) {
			
			Set<String> theSources = new HashSet<String>();
			Set<Value> temp2 = aWGM.listAttributeValues(wsmoFactory.createIRI(
					sdcGraph.getDefaultNamespace(), "targetWS"));
			for (Value v: temp2)
				theSources.add(v.toString());

//    		Set<String> theSources = aWGM.listAttributeValues(
//					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "targetWS")); 
    		
    		Iterator theIDsIterator = theSources.iterator();
    		String theWSID = theIDsIterator.next().toString();
    		aWS = wsmoFactory.createWebService(wsmoFactory.createIRI(theWSID));
    		availableWS.remove(aWS); 
				
			}

		return availableWS;
	}


//	/**
//	 * clears the WSMO4J cache for a goal 
//	 * @param theEntity
//	 */
//	public void cacheClearer(Goal theEntity) {
//		
//		IRI anIdentifier = wsmoFactory.createIRI("urn://unique/"+System.currentTimeMillis()); 
//		
//		Goal dummyHackByHolger = wsmoFactory.createGoal(anIdentifier); 
//
//		try {
//			ClearTopEntity.clearTopEntity(dummyHackByHolger);
//		} catch (SynchronisationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidModelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//		
//	}




}
