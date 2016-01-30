/**
 * creation and management of Goal Instances for a SDC graph
 * 
 * @author Michael Stollberg
 * @version $Revision: 1.7 $ $Date: 2007-10-11 14:37:53 $
 */ 

package org.deri.wsmx.discovery.caching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmx.discovery.caching.matchmaking.Matchmaking4SWSC;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.wsmo.common.Identifier;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

public class GoalInstanceManager {
	
 	protected static Logger logger;

	private WsmoFactory wsmoFactory;
	private DataFactory dataFactory; 
	
	private Matchmaking4SWSC matchmaker;
	private SDCGraphManager graphManager; 
	private SDCGraphCreatorHelper helper; 
	private SDCResourceManager resoruceManager; 

	
	public GoalInstanceManager () {
		wsmoFactory = Factory.createWsmoFactory(new HashMap());
		dataFactory = Factory.createDataFactory(new HashMap());
		
		resoruceManager = new SDCResourceManager(); 
		graphManager = new SDCGraphManager(); 
		matchmaker = new Matchmaking4SWSC();
		helper = new SDCGraphCreatorHelper(); 
		
		logger = Logger.getLogger(GoalInstanceManager.class);
		
	}
	
	/**
	 * creates a goal instance 
	 * @param inputs: set of Strings (3 are required for SWSC scenario: 1 = SenderLocation, 2 = ReceiverLocation, 3 = weight ) 
	 * @param goalTemplate: the corresponsing goal template (can also be null) 
	 * @param sdcGraph
	 * @return the created Goal Instance 
	 */
	public Instance createGoalInstance(String[] inputs, Goal goalTemplate, Ontology sdcGraph) {
		
		logger.info("creating a goal instance ... " ); 
		// generation of unique ID 
		List<Instance> currentGTs = getAllGoalInstances(sdcGraph);   
		
		Integer noOfGIs = currentGTs.size();
		Identifier goalInstanceID = goalInstanceIDGenerator(noOfGIs, sdcGraph);  

		// distinguishing inputs 
		String input1 = inputs[0];
		String input2 = inputs[1]; 
		String input3 = inputs[2]; 
		
		Instance theNewGoalInstance = null;	
						
		try {
			theNewGoalInstance = wsmoFactory.createInstance(
					goalInstanceID, 
					sdcGraph.findConcept(wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "goalInstance")));
			if (goalTemplate == null ) {
				theNewGoalInstance.addAttributeValue(
						wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "correspondingGoalTemplate"),
						dataFactory.createWsmlString("not specified") ); 
			} else {
				theNewGoalInstance.addAttributeValue(
						wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "correspondingGoalTemplate"),
						dataFactory.createWsmlString(goalTemplate.getIdentifier().toString()) ); 				
			}
			theNewGoalInstance.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "inputs"),
					dataFactory.createWsmlString(input1 ) );
			theNewGoalInstance.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "inputs"),
					dataFactory.createWsmlString(input2 ) );
			theNewGoalInstance.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "inputs"),
					dataFactory.createWsmlString(input3 ) );
			logger.info("successfully created: " + theNewGoalInstance.getIdentifier());
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
				
		
		addGItoSDCGraph(theNewGoalInstance, sdcGraph); 

		return theNewGoalInstance;

	}
	
	/**
	 * creates a unique Identifier for a goal instance 
	 * @param noOfGIs
	 * @param sdcGraph
	 * @return the generated identifier 
	 */
	private Identifier goalInstanceIDGenerator(Integer noOfGIs, Ontology sdcGraph){

    	Integer numberID = noOfGIs + 1;  	
		String theID = "goalInstance" + numberID.toString();
		Identifier theArcID = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), theID);
		
		if (sdcGraph.findInstance(theArcID) != null) { 
			theArcID = goalInstanceIDGenerator(numberID, sdcGraph); 
		} 
		return theArcID;
			
	}
	
	/**
	 * storing a Goal Instance in the SDC Graph 
	 * @param goalInstance
	 * @param sdcGraph
	 * @return the updated SDC Graph
	 */
	public Ontology addGItoSDCGraph(Instance goalInstance, Ontology sdcGraph) {
		
		try {
			sdcGraph.addInstance(goalInstance);
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sdcGraph; 

	}


	/**
	 * gives List of all goal instances in a SDC Graph 
	 * @param sdcGraph
	 * @return List of Instances 
	 */
	public List<Instance> getAllGoalInstances(Ontology sdcGraph) {		
    	Set<Instance> theInstances = sdcGraph.listInstances();
    	List <Instance> theGoalInstances = new ArrayList<Instance>();
    	for (Instance anInstance : theInstances ) {
    		if (anInstance.listConcepts().contains(
    				sdcGraph.findConcept(wsmoFactory.createIRI(
    						sdcGraph.getDefaultNamespace(), "goalInstance")))) {
    			theGoalInstances.add(anInstance);
    		}
    	}
    	
    	return theGoalInstances;  
    	
	}

	
	/**
	 * gives the corresponsing Goal Template of a Goal Instance 
	 * @param goalInstance
	 * @param sdcGraph
	 * @return Goal, i.e. the corresponsing goal template 
	 */
	public Goal getCorrespondingGT(Instance goalInstance, Ontology sdcGraph) {
		
		Goal theGT = null; 
		
		Set<String> theCorrespondingGTs = new HashSet<String>();
		Set<Value> temp = goalInstance.listAttributeValues(wsmoFactory.createIRI(
				sdcGraph.getDefaultNamespace(), "correspondingGoalTemplate"));
		for (Value v: temp)
			theCorrespondingGTs.add(v.toString());
		
//		Set<String> theCorrespondingGTs = goalInstance.listAttributeValues(
//				wsmoFactory.createIRI(
//						sdcGraph.getDefaultNamespace(), "correspondingGoalTemplate")); 
						
		Iterator theIDsIterator = theCorrespondingGTs.iterator();
		String theGoalID = theIDsIterator.next().toString();
		if (! theGoalID.equals("not specified") ) {
			theGT = wsmoFactory.createGoal(wsmoFactory.createIRI(theGoalID)); 			
		}
		
		return theGT; 
	}
	
	/**
	 * updates the corresponsing Goal Template of a Goal Instance
	 * @param goalInstance
	 * @param goalTemplate
	 * @param sdcGraph
	 * @return the updated Goal Instance
	 */
	public Instance setGT4goalInstance(Instance goalInstance, Goal goalTemplate, Ontology sdcGraph ) {
				
		try {
			goalInstance.removeAttributeValues(wsmoFactory.createIRI(
							sdcGraph.getDefaultNamespace(), "correspondingGoalTemplate"));
			goalInstance.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "correspondingGoalTemplate"),
					dataFactory.createWsmlString(goalTemplate.getIdentifier().toString()) ); 
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return goalInstance;
		
	}
	

	/**
	 * lifts the weight for a Goal Instance to a Weight Class, and creates a new Goal Instance
	 * @param goalInstance
	 * @param sdcGraph
	 * @return the revised Goal Instance 
	 */public Instance reviseGI4SWSC(Instance goalInstance, Ontology sdcGraph) {
		 logger.info("revising goal instance .."); 
		
		Goal theCorrespondingGT = getCorrespondingGT(goalInstance, sdcGraph); 
		
//		Set<String> theInputs = new HashSet<String>();
//		Set<Value> temp = goalInstance.listAttributeValues(wsmoFactory.createIRI(
//				sdcGraph.getDefaultNamespace(), "inputs"));
//		for (Value v: temp)
//			theInputs.add(v.toString());
		
		Set<Value> theInputs = goalInstance.listAttributeValues(
				wsmoFactory.createIRI(
						sdcGraph.getDefaultNamespace(), "inputs"));
		
		Iterator myIterator = theInputs.iterator(); 
		
		String SenderLoc = myIterator.next().toString(); 
		String ReceiverLoc = myIterator.next().toString(); 
		String theWeightAsString  = myIterator.next().toString(); 
		
		
		float theWeight = Float.valueOf(theWeightAsString).floatValue();
		
		int theWeightInt = (int) theWeight;  
		
		String weightClass = null; 
		
		if (  theWeight < 10 ) {weightClass = "light"; }
		else if (  theWeight < 20 ) {weightClass = "w20lq"; }
		else if (  theWeight < 30 ) {weightClass = "w30lq"; }
		else if (  theWeight < 40 ) {weightClass = "w40lq"; }
		else if (  theWeight < 50 ) {weightClass = "w50lq"; }
		else if (  theWeight < 60 ) {weightClass = "w60lq"; }
		else if (  theWeight < 70 ) {weightClass = "w70lq"; }
		else if (  theWeight > 70 ) {weightClass = "heavy"; }
		else {
			System.out.println("input is not a float, cannot be converted to weight class "); 
		}
		
		String[] inputs4RevisedGI = {SenderLoc,ReceiverLoc,weightClass};
		
		Instance revisedGI = createGoalInstance(inputs4RevisedGI, theCorrespondingGT, sdcGraph);
		
		try {
			revisedGI.addNFPValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "title"), 
					dataFactory.createWsmlString("revision of " + goalInstance.getIdentifier().toString()) );
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return revisedGI;	
	}
	
	 /**
	  * checks whether a given goal instance is defined properly 
	  * @param goalInstance
	  * @param sdcGraph
	  * @return boolean 
	  */
	 public boolean GIValidation(Instance goalInstance, Goal correspGT, Ontology sdcGraph) {
		   
	    	boolean result = false;
	    	
	    	// checks if GI properly instantiates defined GT  
	    	boolean instantiationTest = false;     	
	    	
	    	if ( !(correspGT == null) ) {
	    		
	    		Instance revisedGI = reviseGI4SWSC(goalInstance, sdcGraph); 
	        	
	    		if (matchmaker.instantiatonCheck(revisedGI, correspGT, sdcGraph)) {
	        	
	    			instantiationTest = true; 
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
	    		
	    		
	    		
	    	} else {
	    		instantiationTest = true;
	    	}
	    	
	    	// checks if 3. input can be converted to float 
	    	
	    	boolean weightTest = false;  
	    	
//			Set<String> theInputs = new HashSet<String>();
//			Set<Value> temp = goalInstance.listAttributeValues(wsmoFactory.createIRI(
//					sdcGraph.getDefaultNamespace(), "inputs"));
//			for (Value v: temp)
//				theInputs.add(v.toString());
	    	
			Set<Value> theInputs = goalInstance.listAttributeValues(
					wsmoFactory.createIRI(
							sdcGraph.getDefaultNamespace(), "inputs"));
			
			Iterator myIterator = theInputs.iterator(); 
			String SenderLoc = myIterator.next().toString(); 
			String ReceiverLoc = myIterator.next().toString(); 
			String theWeightAsString  = myIterator.next().toString();
			
			try {
				Float.valueOf(theWeightAsString).floatValue(); 
				weightTest = true; 
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			result = (instantiationTest && weightTest ); 
	    	
	    	return result; 
	    	
	    }
	 
	 /**
	  * finds all usable Web services for a Goal Instance 
	  * @param goalInstance
	  * @param sdcGraph
	  * @return List of usable Web services 
	  */	
	 public List<WebService> findAllWS4GI (Instance goalInstance, Ontology sdcGraph) {
	 		
	 		List<WebService> usableWS = new ArrayList<WebService>();
	 		
	 		String theSWSCwsDirectory = "D:/DERI/phd-thesis/repository/stolle/usecases/SWS-challenge/own-modelling/WSML/webservices/";
	 		List<WebService> availableWS = helper.getAvailableWebServices(theSWSCwsDirectory);
	 		
	 		Goal correspGT = null; 

	 		if (getCorrespondingGT(goalInstance, sdcGraph) == null ) {
	 			correspGT = resoruceManager.loadGoalTemplate("gtRoot.wsml"); 
	 		} else {
	 			correspGT = getCorrespondingGT(goalInstance, sdcGraph);	 			
	 		}
	 		
	 		Instance revisedGI = reviseGI4SWSC(goalInstance, sdcGraph); 
	 		
	 		for (WebService aWS : availableWS) {
	 			
	 			if ( matchmaker.giLevelUsabilityIntersect(revisedGI, correspGT, aWS, sdcGraph) ) {
	 				usableWS.add(aWS); 
	 			}
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
    		

	 		
	 		return usableWS; 
	 		
	 	}

	 public List<WebService> findAllWS4GIextendedSWSC(Instance goalInstance, Ontology sdcGraph) {
	 		
	 		List<WebService> usableWS = new ArrayList<WebService>();
	 		
	 		String theSWSCwsDirectory = "D:/DERI/phd-thesis/repository/stolle/usecases/SWS-challenge/evaluation/runtimeDiscoverySDCvsGTonly/SWSC-shipmentscenario-extended-modelling/WSML/webservices/";
	 		List<WebService> availableWS = helper.getAvailableWebServices(theSWSCwsDirectory);
	 		
	 		Goal correspGT = null; 

	 		if (getCorrespondingGT(goalInstance, sdcGraph) == null ) {
	 			correspGT = resoruceManager.loadGoalTemplate("gtRoot.wsml"); 
	 		} else {
	 			correspGT = getCorrespondingGT(goalInstance, sdcGraph);	 			
	 		}
	 		
	 		Instance revisedGI = reviseGI4SWSC(goalInstance, sdcGraph); 
	 		
	 		for (WebService aWS : availableWS) {
	 			
	 			if ( matchmaker.giLevelUsabilityIntersect(revisedGI, correspGT, aWS, sdcGraph) ) {
	 				usableWS.add(aWS); 
	 			}
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
 		

	 		
	 		return usableWS; 
	 		
	 	}
	



}
