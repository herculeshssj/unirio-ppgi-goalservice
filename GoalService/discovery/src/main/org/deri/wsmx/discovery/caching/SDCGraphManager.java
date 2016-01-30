/**
 * management for SDC Graph as an ontology, providing 
 * - adding of all four element types 
 * - getter methods   of all four element types
 * 
 * <pre>
 * Committed by $Author: maciejzaremba $
 * </pre>
 * 
 * @author Michael Stollberg
 *
 * @version $Revision: 1.13 $ $Date: 2007-10-11 14:37:53 $
 */ 


package org.deri.wsmx.discovery.caching;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.common.ClearTopEntity;
import org.omwg.ontology.Concept;
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
import org.wsmo.mediator.GGMediator;
import org.wsmo.mediator.Mediator;
import org.wsmo.mediator.WGMediator;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;



public class SDCGraphManager {

 	protected static Logger logger;

	private WsmoFactory wsmoFactory;
	private DataFactory dataFactory; 
	
	
	public SDCGraphManager() {
		wsmoFactory = Factory.createWsmoFactory(new HashMap());
		dataFactory = Factory.createDataFactory(new HashMap()); 
		
		logger = Logger.getLogger(SDCGraphManager.class);
	}

	/**
	 * adds a goal template as an instance to the SDC graph 
	 * @param theGoalTemplate
	 * @param sdcGraph
	 * @param positionValue
	 * @return the updated SDC graph 
	 */
	public Ontology addGoalTemplate(Goal theGoalTemplate, Ontology sdcGraph, String positionValue) {
		
		if (getSingleGoalTemplate(theGoalTemplate, sdcGraph) != null ) {
			logger.info("goal template exists already"); 
		} else {

		logger.info("adding new " + positionValue + " goal template to goal store " + theGoalTemplate.getIdentifier());
		
		// generation of unique ID 
		List<Instance> currentGTs = getAllGoalTemplates(sdcGraph);  
		
		Integer noOfGTs = currentGTs.size();
		Identifier instanceID = goalTemplateIDGenerator(noOfGTs, sdcGraph); 

		
		Identifier positionIRI = null;  
		if (positionValue == "root") {
			positionIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "root"); 			
		} else if (positionValue == "child") {
			positionIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "child"); 			
		} else if (positionValue == "disconnected") {
			positionIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "disconnected"); 			
		} else {
			logger.error("The provided position value is not valid !!");
		}
						
		Instance theNewGoalTemplate = null;
			
						
		try {
			theNewGoalTemplate = wsmoFactory.createInstance(
					instanceID, 
					sdcGraph.findConcept(wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "goalTemplate")));
			theNewGoalTemplate.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "description"),
					dataFactory.createWsmlString(theGoalTemplate.getIdentifier().toString()) ); 
			theNewGoalTemplate.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "position"),
					wsmoFactory.getInstance(positionIRI));
			sdcGraph.addInstance(theNewGoalTemplate);
			logger.info("adding successful");
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
				
		}

		return sdcGraph;

	}
	
	/**
	 * creates a unique Identifier for a goal template 
	 * @param noOfGTs
	 * @param sdcGraph
	 * @return the generated identifier 
	 */
	private Identifier goalTemplateIDGenerator(Integer noOfGTs, Ontology sdcGraph){

    	Integer numberID = noOfGTs + 1;  	
		String theID = "goalTemplate" + numberID.toString();
		Identifier theInstanceID = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), theID);
		
		if (sdcGraph.findInstance(theInstanceID) != null) { 
			theInstanceID = goalTemplateIDGenerator(numberID, sdcGraph); 
		} 
		return theInstanceID;
			
	}


	/**
	 * deletes a specified goal template from the SDC graph 
	 * @param goalTemplate
	 * @param sdcGraph
	 * @return the updated SDC graph 
	 */
	public Ontology deleteGoalTemplate(Goal goalTemplate, Ontology sdcGraph) {
		logger.info("deleting goal template from goal store " + goalTemplate.getIdentifier());
		
    	List<Instance> theGTs = getAllGoalTemplates(sdcGraph); 
    	
    	Identifier idOfInstanceToDelete = null; 
    	for (Instance aGT : theGTs) {
    		Set<String> theIDs = new HashSet<String>();
    		Set<Value> temp = aGT.listAttributeValues(wsmoFactory.createIRI(
    				sdcGraph.getDefaultNamespace(), "description"));
    		for (Value v: temp)
    			theIDs.add(v.toString());

//    		Set<String> theIDs = aGT.listAttributeValues(wsmoFactory.createIRI(
//    				sdcGraph.getDefaultNamespace(), "description"));
    		Iterator theIDsIterator = theIDs.iterator();
    		if ( theIDsIterator.next().toString().equals(goalTemplate.getIdentifier().toString()) ) {
    			idOfInstanceToDelete = aGT.getIdentifier(); 
    			try {
    				Instance instanceToDelete = sdcGraph.findInstance(idOfInstanceToDelete);
    				sdcGraph.removeInstance(instanceToDelete);
    				cacheClearer(instanceToDelete); 			
    				logger.info("deletion succsessful");
    			} catch (SynchronisationException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (InvalidModelException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
				
		return sdcGraph;
		
	}

	/**
	 * returns all existing goal templates 
	 * @param sdcGraph
	 * @return list of goal templates 
	 */
	public List<Instance> getAllGoalTemplates(Ontology sdcGraph) {		
    	Set<Instance> theInstances = sdcGraph.listInstances();
    	List <Instance> theGoalTemplates = new ArrayList<Instance>();
    	for (Instance anInstance : theInstances ) {
    		if (anInstance.listConcepts().contains(
    				sdcGraph.findConcept(wsmoFactory.createIRI(
    						sdcGraph.getDefaultNamespace(), "goalTemplate")))) {
        		theGoalTemplates.add(anInstance);
    		}
    	}
    	
    	return theGoalTemplates;  
    	
	}

	/**
	 * gets a single Goal Template instance 
	 * @param goalTemplate
	 * @param sdcGraph
	 * @return the SDC graph instance 
	 */
	public Instance getSingleGoalTemplate(Goal goalTemplate, Ontology sdcGraph) {	

		List<Instance> currentGTS = getAllGoalTemplates(sdcGraph);

		Instance theGoalTemplate = null;

    	Identifier idOfInstance = null; 
    	for (Instance aGT : currentGTS) {
    		Set<String> theIDs = new HashSet<String>();
    		Set<Value> temp = aGT.listAttributeValues(wsmoFactory.createIRI(
    				sdcGraph.getDefaultNamespace(), "description"));
    		for (Value v: temp)
    			theIDs.add(v.toString());

//    		Set<String> theIDs = aGT.listAttributeValues(wsmoFactory.createIRI(
//    				sdcGraph.getDefaultNamespace(), "description"));
    		Iterator theIDsIterator = theIDs.iterator();
    		if ( theIDsIterator.next().toString().equals(goalTemplate.getIdentifier().toString()) ) {
    			idOfInstance = aGT.getIdentifier(); 
    			theGoalTemplate = sdcGraph.findInstance(idOfInstance);
    		}
    	}

		
    	return theGoalTemplate;  
    	
	}

/* 
 * not needed any longer ... 
*/ 

//	/**
//	 * adds a Web service as an instance to the SDC graph 
//	 * @param webservice
//	 * @param sdcGraph
//	 * @return the updated SDC graph 
//	 */
//	public Ontology addWebService(WebService webservice, Ontology sdcGraph) {
//		
//		if (getSingleWebService(webservice, sdcGraph) != null ) {
//			logger.info("Web Service exists already"); 
//		} else {
//
//		
//		logger.info("adding new available Web Service:" + webservice.getIdentifier());
//		
//		Instance theNewWS = null;
//				
//		try {
//			theNewWS = wsmoFactory.createInstance(
//					webservice.getIdentifier(),
//					sdcGraph.findConcept(wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "webServiceDescription")));
//			sdcGraph.addInstance(theNewWS);
//			logger.info("adding successful");
//		} catch (SynchronisationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidModelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		}
//		
//		return sdcGraph;
//		
//	}
//
//	/**
//	 * deletes a specified Web Service from the SDC graph
//	 * @param webservice
//	 * @param sdcGraph
//	 * @return the updated SDC graph 
//	 */
//	public Ontology deleteWebService(WebService webservice, Ontology sdcGraph) {
//		logger.info("deleting Web Service:" + webservice.getIdentifier());
//
//		try {
//			Instance instanceToDelete = sdcGraph.findInstance(webservice.getIdentifier());
//			sdcGraph.removeInstance(webservice.getIdentifier());
//			cacheClearer(instanceToDelete); 			
//
//			logger.info("deletion successful");
//		} catch (SynchronisationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidModelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return sdcGraph;
//		
//	}
//	
//	/**
//	 * gets all WS registered in the SDC Graph 
//	 * @param sdcGraph
//	 * @return List of Web Service Instances 
//	 */
//	public List<Instance> getAllWebServices(Ontology sdcGraph) {		
//    	Set<Instance> theInstances = sdcGraph.listInstances();
//    	List <Instance> theWebServices = new ArrayList<Instance>();
//    	for (Instance anInstance : theInstances ) {
//    		if (anInstance.listConcepts().contains(
//    				sdcGraph.findConcept(wsmoFactory.createIRI(
//    						sdcGraph.getDefaultNamespace(), "webServiceDescription")))) {
//    			theWebServices.add(anInstance);
//    		}
//    	}
//    	
//    	return theWebServices;  
//    	
//	}
//
//	/**
//	 * gets the instance of a single WS 
//	 * @param webservice
//	 * @param sdcGraph
//	 * @return the Instance of the WS 
//	 */
//	public Instance getSingleWebService(WebService webservice, Ontology sdcGraph) {	
//
//		List<Instance> currentWS = getAllWebServices(sdcGraph);
//		
//		Instance theWS = null;
//		
//		for (Instance aWS : currentWS) {
//			if (aWS.getIdentifier().equals(webservice.getIdentifier())){
//				theWS = aWS;		
//			}
//		}
//    	
//    	return theWS;  
//    	
//	}

	
	/**
	 * add a goal graph arc to the SDC graph 
	 * @param source: the source goal template 
	 * @param target: the target goal template 
	 * @param sdcGraph
	 * @param degree: similarity degree of soruce and target 
	 * @return the updated SDC graph 
	 */
	public Ontology addGoalGraphArc(Goal source, Goal target, Ontology sdcGraph, String degree ) {

		if ( getSingleGoalGraphArc(source,target,sdcGraph) != null ) {
			logger.info("goal graph arc exists already, ID: " + getSingleGoalGraphArc(source,target,sdcGraph).getIdentifier() 
					+ "\n source: " + source.getIdentifier() + "\n target: " + target.getIdentifier() );
				
		} else {

		logger.info("adding a new goal graph arc with \n" +
				"   source = " + source.getIdentifier() + "\n" +
				"   target = " + target.getIdentifier() + "\n" +
				"   similarity degree = " + degree); 
		

		// generation of unique ID 
		List<Instance> currentGGM = getAllGoalGraphArcs(sdcGraph);			
		
		Integer noOfGGM = currentGGM.size();
		Identifier arcID = goalGraphArcIDGenerator(noOfGGM,sdcGraph);
		
		Identifier degreeIRI = null;
		if (degree == "exact") {
			degreeIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "exact");
		} else if (degree == "plugin") {
			degreeIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "plugin");
		} else if (degree == "subsume") {
			degreeIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "subsume");
		} else if (degree == "intersect") {
			degreeIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "intersect");
		} else {
			logger.error("The provided degree value is not valid !!");
		}
		
		Instance theNewArc= null;
		
		try {			
			theNewArc = wsmoFactory.createInstance(
						arcID,
						sdcGraph.findConcept(wsmoFactory.createIRI(
								sdcGraph.getDefaultNamespace(), "goalGraphArc")));
			theNewArc.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "sourceGT"),
					dataFactory.createWsmlString(source.getIdentifier().toString()) ); 
			theNewArc.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "targetGT"),
					dataFactory.createWsmlString(target.getIdentifier().toString()) ); 
			theNewArc.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "similarity"),
					wsmoFactory.getInstance(degreeIRI)); 
			sdcGraph.addInstance(theNewArc);
			logger.info("adding successful: " + arcID.toString());
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
		}
		
		return sdcGraph;
	}
	
	
	/**
	 * generates a unique, not yet existing ID for a GMM 
	 * @param noOfGGM
	 * @param sdcGraph
	 * @return the new Identifier 
	 */
	private Identifier goalGraphArcIDGenerator(Integer noOfGGM, Ontology sdcGraph){

    	Integer numberID = noOfGGM + 1;  	
		String theID = "ggm" + numberID.toString();
		Identifier theArcID = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), theID);
		
		if (sdcGraph.findInstance(theArcID) != null) { 
			theArcID = goalGraphArcIDGenerator(numberID, sdcGraph); 
		} 
		return theArcID;
			
	}

	/**
	 * removes a GGM from the SDC graph 
	 * @param sdcGraph
	 * @param arc
	 * @return the updated SDC Graph 
	 */
	public Ontology deleteGoalGraphArc(Ontology sdcGraph, Instance arc) {
		logger.info("deleting goal graph arc: " + arc.getIdentifier());
		

		try {
			Instance instanceToDelete = sdcGraph.findInstance(arc.getIdentifier());
			sdcGraph.removeInstance(arc.getIdentifier());
			cacheClearer(instanceToDelete); 			
			logger.info("deletion succsessful");
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
	 * gets all GGM from the SDC graph 
	 * @param sdcGraph
	 * @return
	 */
	public List<Instance> getAllGoalGraphArcs(Ontology sdcGraph) {
    	
		Set<Instance> theInstances = sdcGraph.listInstances();
		List<Instance> currentGGM = new ArrayList<Instance>();
    	for (Instance aGGM : theInstances ) {
    		if (aGGM.listConcepts().contains(
    				sdcGraph.findConcept(wsmoFactory.createIRI(
    						sdcGraph.getDefaultNamespace(), "goalGraphArc")))) {
    			currentGGM.add(aGGM);
    		}
    	}

		return currentGGM;		
	}

	/**
	 * gets a single GGM by source and target 
	 * @param source
	 * @param target
	 * @param sdcGraph
	 * @return the GGM instance 
	 */
	public Instance getSingleGoalGraphArc(Goal source,Goal target,Ontology sdcGraph) {
				
		List<Instance> arcsWithSource = getGoalGraphArcBySource(source,sdcGraph);
				
		List<Instance> arcsWithTarget = getGoalGraphArcByTarget(target,sdcGraph);

		List<Instance> theRelevantArcs = new ArrayList<Instance>();

		Instance theGGM = null;

		for (Instance aGGM : arcsWithSource) {
			
			for (Instance aGGM2 : arcsWithTarget){
				
				if (aGGM.equals(aGGM2)) {
					theRelevantArcs.add(aGGM);
				}
			}
		}
		
		if (! theRelevantArcs.isEmpty() ) {
			theGGM = theRelevantArcs.get(0);
		}
					
		return theGGM;
		
	}
	
	/**
	 * gets all GGM by the source goal template 
	 * @param source
	 * @param sdcGraph
	 * @return List of Instances 
	 */
	public List<Instance> getGoalGraphArcBySource(Goal source,Ontology sdcGraph) {

		List<Instance> currentGGM = getAllGoalGraphArcs(sdcGraph);
				
		List<Instance> theArcs = new ArrayList<Instance>();

		for (Instance theGGM : currentGGM) {
			
    		Set<String> theSourceAttr = new HashSet<String>();
    		Set<Value> temp = theGGM.listAttributeValues(wsmoFactory.createIRI(
    				sdcGraph.getDefaultNamespace(), "sourceGT"));
    		for (Value v: temp)
    			theSourceAttr.add(v.toString());

//			Set<String> theSourceAttr = theGGM.listAttributeValues(
//						wsmoFactory.createIRI(
//								sdcGraph.getDefaultNamespace(), "sourceGT"));
			
			Iterator theIDsIterator = theSourceAttr.iterator();
    		if ( theIDsIterator.next().toString().equals(source.getIdentifier().toString()) ) {
    			theArcs.add(theGGM);
    		}
		}
				
		return theArcs;
		
	}

	/**
	 * gets all GGM by the target goal template 
	 * @param target
	 * @param sdcGraph
	 * @return List of Instances 
	 */
	public List<Instance> getGoalGraphArcByTarget(Goal target,Ontology sdcGraph) {

		List<Instance> currentGGM = getAllGoalGraphArcs(sdcGraph);
		
		List<Instance> theArcs = new ArrayList<Instance>();
		
		for (Instance theGGM : currentGGM) {
    		Set<String> theSourceAttr = new HashSet<String>();
    		Set<Value> temp = theGGM.listAttributeValues(wsmoFactory.createIRI(
    				sdcGraph.getDefaultNamespace(), "targetGT"));
    		for (Value v: temp)
    			theSourceAttr.add(v.toString());

//			Set<String> theSourceAttr = theGGM.listAttributeValues(
//						wsmoFactory.createIRI(
//								sdcGraph.getDefaultNamespace(), "targetGT"));
			
			Iterator theIDsIterator = theSourceAttr.iterator();
    		if ( theIDsIterator.next().toString().equals(target.getIdentifier().toString()) ) {
    			theArcs.add(theGGM);
    		}
		}

				
		return theArcs;
		
	}
	
	/**
	 * gets the source goal template 
	 * @param sdcGraph
	 * @param goalGraphArc
	 * @return the source goal template 
	 */
	public Goal getGoalGraphArcSource(Ontology sdcGraph, Instance goalGraphArc) {
		//from string to value otherwise compile error
		//Holger 25.09.2007
		Set<Value> theSources = goalGraphArc.listAttributeValues(
				wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "sourceGT")); 
		
		Iterator theSourcesIterator = theSources.iterator();
		String theGoalID = theSourcesIterator.next().toString();
		
		Goal theSourceGoal = wsmoFactory.createGoal(wsmoFactory.createIRI(theGoalID)); 
		
		return theSourceGoal; 
	}

	/**
	 * gets the target goal template 
	 * @param sdcGraph
	 * @param goalGraphArc
	 * @return the target goal template
	 */
	public Goal getGoalGraphArcTarget(Ontology sdcGraph, Instance goalGraphArc) {
		//from string to value otherwise compile error
		//Holger 25.09.2007
		Set<Value> theTargets = goalGraphArc.listAttributeValues(
				wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "targetGT")); 
		
		Iterator theTargetsIterator = theTargets.iterator();
		String theGoalID = theTargetsIterator.next().toString();
		
		Goal theTargetGoal = wsmoFactory.createGoal(wsmoFactory.createIRI(theGoalID)); 
		
		return theTargetGoal; 
	}
	
	/**
	 * generates a WSMO GG Mediator that corresponds to a SDC Goal Graph Arcs 
	 * @param sdcGraph
	 * @param goalGraphArc
	 * @param mediatorID
	 * @return the generated GG Mediator
	 */
	public GGMediator generateGGMediator(Ontology sdcGraph, Instance goalGraphArc, IRI mediatorID){
		
		Goal theSourceGoal = getGoalGraphArcSource(sdcGraph, goalGraphArc); 
		Goal theTargetGoal = getGoalGraphArcTarget(sdcGraph, goalGraphArc);
		
		logger.info("generating GG Mediator with: " +
				"\n ID: " + mediatorID + 
				"\n source: " + theSourceGoal.getIdentifier() + 
				"\n target: " + theTargetGoal.getIdentifier() ); 
		
		GGMediator theGGM = wsmoFactory.createGGMediator(mediatorID); 
		
		Namespace nsDC = wsmoFactory.createNamespace("dc", wsmoFactory.createIRI("http://purl.org/dc/elements/1.1"));
		theGGM.addNamespace(nsDC); 
		Namespace nsSDC = wsmoFactory.createNamespace("sdc", (IRI) sdcGraph.getIdentifier());
		theGGM.addNamespace(nsSDC); 
		
		Concept SDCgoalGraphArc =  sdcGraph.findConcept(wsmoFactory.createIRI(
				sdcGraph.getDefaultNamespace(), "goalGraphArc") ); 

		
		try {
			theGGM.addSource((IRI) theSourceGoal.getIdentifier());
			theGGM.setTarget((IRI) theTargetGoal.getIdentifier());
			theGGM.addNFPValue(
					wsmoFactory.createIRI(nsDC, "description"), SDCgoalGraphArc.getIdentifier() );  
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
		return theGGM; 
	}




	/**
	 * adds a new discovery cache arcs to the SCD graph 
	 * @param source
	 * @param target
	 * @param sdcGraph
	 * @param degree
	 * @return the updated SCD graph 
	 */
	public Ontology addDiscoveryCacheArc(Goal source, WebService target, Ontology sdcGraph, String degree ) {

//		if ( getSingleDiscoveryCacheArc(source,target,sdcGraph) != null ) {
//			logger.info("discovery cache arc exists already"); 
//		} else {

		logger.info("adding a new discovery cache graph arc with \n" +
				"   source = " + source.getIdentifier() + "\n" +
				"   target = " + target.getIdentifier() + "\n" +
				"   usability degree = " + degree); 
		

		// generation of unique ID 
		List<Instance> currentWGM = getAllDiscoveryCacheArcs(sdcGraph);			
		
		Integer noOfWGM = currentWGM.size();
		Identifier arcID = discoveryCacheArcIDGenerator(noOfWGM,sdcGraph);
		
		Identifier degreeIRI = null;
		if (degree == "exact") {
			degreeIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "exact");
		} else if (degree == "plugin") {
			degreeIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "plugin");
		} else if (degree == "subsume") {
			degreeIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "subsume");
		} else if (degree == "intersect") {
			degreeIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "intersect");
		} else {
			logger.error("The provided degree value is not valid !!");
		}
		
		Instance theNewArc= null;
		
		try {			
			theNewArc = wsmoFactory.createInstance(
						arcID,
						sdcGraph.findConcept(wsmoFactory.createIRI(
								sdcGraph.getDefaultNamespace(), "discoveryCacheArc")));
			theNewArc.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "sourceGT"),
					dataFactory.createWsmlString(source.getIdentifier().toString()) );
			theNewArc.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "targetWS"),
					dataFactory.createWsmlString(target.getIdentifier().toString()) );
			theNewArc.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "usability"),
					wsmoFactory.getInstance(degreeIRI)); 
			sdcGraph.addInstance(theNewArc);
			logger.info("adding successful: " + arcID);
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
//		}
		
		return sdcGraph;
	}

	/**
	 * generates a unique, not existing Identifier for a WGM 
	 * @param noOfWGM
	 * @param sdcGraph
	 * @return the generated Identifier 
	 */
	private Identifier discoveryCacheArcIDGenerator(Integer noOfWGM, Ontology sdcGraph){

    	Integer numberID = noOfWGM + 1;  	
		String theID = "wgm" + numberID.toString();
		Identifier theArcID = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), theID);
		
		if (sdcGraph.findInstance(theArcID) != null) { 
			theArcID = goalGraphArcIDGenerator(numberID, sdcGraph); 
		} 
		return theArcID;
			
	}

	/**
	 * deletes a instance of WGM from the SDC graph 
	 * @param sdcGraph
	 * @param arc
	 * @return the updated SCD graph 
	 */
	public Ontology deleteDiscoveryCacheArc(Ontology sdcGraph, Instance arc) {
		logger.info("deleting discovery cache arc: " + arc.getIdentifier());

		try {
			Instance instanceToDelete = sdcGraph.findInstance(arc.getIdentifier());
			sdcGraph.removeInstance(arc.getIdentifier());
			cacheClearer(instanceToDelete); 			
			logger.info("deletion successful");
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
	 * gets all WGM 
	 * @param sdcGraph
	 * @return list of Instances 
	 */
	public List<Instance> getAllDiscoveryCacheArcs(Ontology sdcGraph) {

    	Set<Instance> theInstances = sdcGraph.listInstances();
		List<Instance> currentWGM = new ArrayList<Instance>();
    	for (Instance aWGM : theInstances ) {
    		if (aWGM.listConcepts().contains(
    				sdcGraph.findConcept(wsmoFactory.createIRI(
    						sdcGraph.getDefaultNamespace(), "discoveryCacheArc")))) {
    			currentWGM.add(aWGM);
    		}
    	}

		return currentWGM;		
	}

	/**
	 * gets a single WGM by source goal and target WS 
	 * @param source
	 * @param target
	 * @param sdcGraph
	 * @return the Instance of the WGM 
	 */
	public Instance getSingleDiscoveryCacheArc(Goal source,WebService target,Ontology sdcGraph) {
		
		List<Instance> arcsWithSource = getDiscoveryCacheBySource(source,sdcGraph);
		List<Instance> arcsWithTarget = getDiscoveryCacheArcByTarget(target,sdcGraph);

		List<Instance> theRelevantArcs = new ArrayList<Instance>();

		Instance theWGM = null;

		for (Instance aWGM : arcsWithSource) {
			for (Instance aWGM2 : arcsWithTarget){
				
				if (aWGM.equals(aWGM)) {
					theRelevantArcs.add(aWGM);
				}
			}
		}
				
		for (Instance aRelevantWGM : theRelevantArcs) {
			theWGM = aRelevantWGM; 
		}
		
						
		
		if (! theRelevantArcs.isEmpty() ) {
			theWGM = theRelevantArcs.get(0);
		}
					
		return theWGM;
		
	}
	
	/**
	 * gets all WGM by source goal template 
	 * @param source
	 * @param sdcGraph
	 * @return List of Instances 
	 */
	public List<Instance> getDiscoveryCacheBySource(Goal source,Ontology sdcGraph) {

		List<Instance> currentWGM = getAllDiscoveryCacheArcs(sdcGraph);
				
		List<Instance> theArcs = new ArrayList<Instance>();
		
		for (Instance theGGM : currentWGM) {

//    		Set<String> theSourceAttr = new HashSet<String>();
//    		Set<Value> temp = theGGM.listAttributeValues(wsmoFactory.createIRI(
//    				sdcGraph.getDefaultNamespace(), "sourceGT"));
//    		for (Value v: temp)
//    			theSourceAttr.add(v.toString());

			Set<Value> theSourceAttr = theGGM.listAttributeValues(
						wsmoFactory.createIRI(
								sdcGraph.getDefaultNamespace(), "sourceGT"));
			
			Iterator theIDsIterator = theSourceAttr.iterator();
    		if ( theIDsIterator.next().toString().equals(source.getIdentifier().toString()) ) {
    			theArcs.add(theGGM);
    		}
		}

				
		return theArcs;
		
	}

	/**
	 * gets all WGM by target WS 
	 * @param target
	 * @param sdcGraph
	 * @return List of WGM Instances 
	 */
	public List<Instance> getDiscoveryCacheArcByTarget(WebService target,Ontology sdcGraph) {

		List<Instance> currentWGM = getAllDiscoveryCacheArcs(sdcGraph);
		
		List<Instance> theArcs = new ArrayList<Instance>();
		
		for (Instance theGGM : currentWGM) {
			
//    		Set<String> theSourceAttr = new HashSet<String>();
//    		Set<Value> temp = theGGM.listAttributeValues(wsmoFactory.createIRI(
//    				sdcGraph.getDefaultNamespace(), "sourceGT"));
//    		for (Value v: temp)
//    			theSourceAttr.add(v.toString());

			
			Set<Value> theSourceAttr = theGGM.listAttributeValues(
						wsmoFactory.createIRI(
								sdcGraph.getDefaultNamespace(), "targetWS"));
			
			Iterator theIDsIterator = theSourceAttr.iterator();
    		if ( theIDsIterator.next().toString().equals(target.getIdentifier().toString()) ) {
    			theArcs.add(theGGM);
    		}
		}

				
		return theArcs;
	}
	
	/**
	 * gets the source goal of a discovery cache arc 
	 * @param sdcGraph
	 * @param discoveryCacheArc
	 * @return the source goal 
	 */
	public Goal getDiscoveryCacheArcSource(Ontology sdcGraph, Instance discoveryCacheArc) {
		//from string to value otherwise compile error
		//Holger 25.09.2007
		Set<Value> theSources = discoveryCacheArc.listAttributeValues(
				wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "sourceGT")); 
		
		Iterator theSourcesIterator = theSources.iterator();
		String theGoalID = theSourcesIterator.next().toString();
		
		Goal theSourceGoal = wsmoFactory.createGoal(wsmoFactory.createIRI(theGoalID)); 
		
		return theSourceGoal; 
	}

	/**
	 * gets the target Web service goal of a discovery cache arc
	 * @param sdcGraph
	 * @param discoveryCacheArc
	 * @return the target Web service 
	 */
	public WebService getDiscoveryCacheArcTarget(Ontology sdcGraph, Instance discoveryCacheArc) {
		//from string to value otherwise compile error
		//Holger 25.09.2007
		Set<Value> theTargets = discoveryCacheArc.listAttributeValues(
				wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "targetWS")); 
		
		Iterator theTargetsIterator = theTargets.iterator();
		String theWsID = theTargetsIterator.next().toString();
				
		WebService theTargetWS  = wsmoFactory.createWebService(wsmoFactory.createIRI(theWsID)); 
		
		return theTargetWS; 
	}

	/**
	 * gets the usability degree explicated in a discovery cache arc 
	 * @param sdcGraph
	 * @param discoveryCacheArc
	 * @return the usability degree (IRI) 
	 */
	public IRI getDiscoveryCacheArcDegree(Ontology sdcGraph, Instance discoveryCacheArc) {
		//from string to value otherwise compile error
		//Holger 25.09.2007
		Set<Value> theDegrees = discoveryCacheArc.listAttributeValues(
				wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "usability"));
		
		String degreeInstanceID = theDegrees.iterator().next().toString(); 
		IRI theDegreeID = wsmoFactory.createIRI(degreeInstanceID); 
			
		return theDegreeID; 
	}
	
	/**
	 * generates a WG Mediator from a discovery cache arc 
	 * @param sdcGraph
	 * @param discoveryCacheArc
	 * @param mediatorID
	 * @return the generated WG Mediator 
	 */
	public WGMediator generateWGMediator(Ontology sdcGraph, Instance discoveryCacheArc, IRI mediatorID){
		
		String theArcID = discoveryCacheArc.getIdentifier().toString(); 
		
		Goal theSourceGoal = getDiscoveryCacheArcSource(sdcGraph, discoveryCacheArc); 
		WebService theTargetWS = getDiscoveryCacheArcTarget(sdcGraph, discoveryCacheArc); 
		IRI theUsabilityDegree = (IRI) getDiscoveryCacheArcDegree(sdcGraph, discoveryCacheArc);
		
		
		logger.info("generating WG Mediator with: " +
				"\n ID: " + mediatorID + 
				"\n source: " + theSourceGoal.getIdentifier() + 
				"\n target: " + theTargetWS.getIdentifier() + 
				"\n usability degree: " + theUsabilityDegree); 

		
		WGMediator theWGM = wsmoFactory.createWGMediator(mediatorID); 
		
		Namespace nsDC = wsmoFactory.createNamespace("dc", wsmoFactory.createIRI("http://purl.org/dc/elements/1.1"));
		theWGM.addNamespace(nsDC); 
		Namespace nsSDC = wsmoFactory.createNamespace("sdc", (IRI) sdcGraph.getIdentifier());
		theWGM.addNamespace(nsSDC); 
		
		try {
			theWGM.addSource((IRI) theSourceGoal.getIdentifier());
			theWGM.setTarget((IRI) theTargetWS.getIdentifier());
			theWGM.addNFPValue(
					wsmoFactory.createIRI(nsDC, "description"), theUsabilityDegree ); 
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
		return theWGM; 
	}
	
	
	/**
	 * creates an intersection goal template out of 2 given goal templates 
	 * @param parent1
	 * @param parent2
	 * @param sdcGraph
	 * @return the updated SDC Graph (incl. the 2 new goal graph arcs) 
	 */
	public Ontology addIntersectionGT(Goal parent1,Goal parent2, Ontology sdcGraph){
		logger.info("adding new intersection goal template to goal store with \n " +
				"parent1: " + parent1.getIdentifier() + 
				"\n parent2: " + parent2.getIdentifier() ); 
		
		// generation of unique ID 
		List<Instance> currentGTs = getAllGoalTemplates(sdcGraph);  
		
		Integer noOfGTs = currentGTs.size();
		Identifier arcID = goalTemplateIDGenerator(noOfGTs, sdcGraph);
		
		// create a WSMO Goal for the intersection GT 
		
		String parent1localID = parent1.getIdentifier().toString();
//		parent1localID.replace(parent1.getDefaultNamespace().toString(), ""); 
//		parent1localID.replace(".wsml", ""); 
		String parent2localID = parent2.getIdentifier().toString();
//		parent2localID.replace(parent2.getDefaultNamespace().toString(), ""); 
//		parent2localID.replace(".wsml", ""); 
		
		Goal intersectGT = wsmoFactory.createGoal(
				wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), 
						"intersectionGT_" + parent1localID + "_"+ parent2localID) ); 


		// create the new GT instance 
		
		Identifier positionIRI = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "intersectionGT");
		
		Instance theNewIntersectionGT = null;
				
		try {
			theNewIntersectionGT = wsmoFactory.createInstance(
					arcID, 
					sdcGraph.findConcept(wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "goalTemplate")));
			theNewIntersectionGT.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "description"),
					dataFactory.createWsmlString(intersectGT.getIdentifier().toString()) ); 
			theNewIntersectionGT.addAttributeValue(
					wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "position"),
					wsmoFactory.getInstance(positionIRI) );
			sdcGraph.addInstance(theNewIntersectionGT);
			logger.info("adding successful" + arcID);
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// create the goal graph arcs 
							
		sdcGraph = addGoalGraphArc(parent2, intersectGT, sdcGraph, "subsume"); 
		sdcGraph = addGoalGraphArc(parent1, intersectGT, sdcGraph, "subsume"); 
		


		return sdcGraph;
		
	}
	

		
	/** 
	 * deletes an intersection goal templates (also the 2 incoming goal graph arcs) 
	 * @param intersectionGT
	 * @param sdcGraph
	 * @return the updated SDC Graph 
	 */
	public Ontology deleteIntersectionGT(Goal intersectionGT, Ontology sdcGraph){
		
		deleteGoalTemplate(intersectionGT, sdcGraph); 
		
		List<Instance> iGTarcs = getGoalGraphArcByTarget(intersectionGT, sdcGraph);
		
		for (Instance aIGTarcs : iGTarcs) {
			deleteGoalGraphArc(sdcGraph, aIGTarcs); 
		}
		
		return sdcGraph;
	}
	
	/** 
	 * tests whether the provided goal template is an intersection goal template 
	 * @param goalTemplate
	 * @param sdcGraph
	 * @return boolean 
	 */
	public boolean isIntersectionGT(Goal goalTemplate, Ontology sdcGraph) {
		
		boolean checkResult = false;
		
		if ( goalTemplate.getIdentifier().toString().contains("intersectionGT_") ) {
			checkResult = true; 
		}
		
//		Instance theGTInstance = getSingleGoalTemplate(goalTemplate, sdcGraph); 
//		
//		Identifier intersectGTPosition = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "intersectionGT");
//
//		Set<Instance> thePositionAttr = theGTInstance.listAttributeValues(
//						wsmoFactory.createIRI(
//								sdcGraph.getDefaultNamespace(), "position"));
//			for (Instance theOne : thePositionAttr ){
//				if ( theOne.getIdentifier().equals(intersectGTPosition) ){
//					checkResult = true; 
//				}
//			}
		
		return checkResult; 
		
	}
	
	/**
	 * test whether a SDC Graph instance is an intersection goal template 
	 * @param goalTemplate (an ontology instance)  
	 * @param sdcGraph
	 * @return boolean 
	 */
	public boolean isInstanceIntersectionGT(Instance goalTemplate, Ontology sdcGraph) {
		
		boolean checkResult = false;
				
		Identifier intersectGTPosition = wsmoFactory.createIRI(sdcGraph.getDefaultNamespace(), "intersectionGT");
		
		Set<Instance> thePositionAttr = new HashSet<Instance>();
		Set<Value> temp = goalTemplate.listAttributeValues(wsmoFactory.createIRI(
				sdcGraph.getDefaultNamespace(), "position"));
		for (Value v: temp)
			thePositionAttr.add((Instance)v);

//		Set<Instance> thePositionAttr = goalTemplate.listAttributeValues(
//						wsmoFactory.createIRI(
//								sdcGraph.getDefaultNamespace(), "position"));
		
			for (Instance theOne : thePositionAttr ){
				if ( theOne.getIdentifier().equals(intersectGTPosition) ){
					checkResult = true; 
				}
			}
		
		return checkResult; 
		
	}

	/** 
	 * lists all existing intersection goal templates 
	 * @param sdcGraph
	 * @return List of instances of the SDC Graph 
	 */
	public List<Instance> getAllIntersectionGT(Ontology sdcGraph) {
		
		List<Instance> allGTs = getAllGoalTemplates(sdcGraph);
				
		List<Instance> allIntersectionGTs = new ArrayList<Instance>();
		
		for (Instance aGT : allGTs ) {
			if ( isInstanceIntersectionGT(aGT, sdcGraph) ) {
				allIntersectionGTs.add(aGT); 
			}
		}
		
		return allIntersectionGTs; 
	}
	
	/**
	 * helper method for deletions 
	 * clears the WSMO4J cache for a specific instance 
	 * @param instanceToDelete
	 */
	public void cacheClearer(Instance instanceToDelete){
		
		Ontology dummyHackByHolger = wsmoFactory.createOntology(wsmoFactory.createIRI("urn://unique/"+System.currentTimeMillis()));
		try {
			dummyHackByHolger.addInstance(instanceToDelete);
			ClearTopEntity.clearTopEntity(dummyHackByHolger);
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	/*
	 * for testing during development only 
	 */
//	public static void main(String[] args) throws Exception{
//		
//		SDCGraphManager theTest = new SDCGraphManager(); 
//		
//		SDCResourceManager theResoruceManager = new SDCResourceManager();
//
//		// Loading the predefined SDC Graph Ontology Schema 
//    	Ontology sdcGraphTest = theResoruceManager.loadSDCGraphSchema(); 
//
//		Goal gt1 = theResoruceManager.loadGoalTemplate("gtRoot.wsml");		
//		theTest.addGoalTemplate(gt1, sdcGraphTest, "root"); 
//
//		Goal gt2 = theResoruceManager.loadGoalTemplate("gtUS2world.wsml");		
//		theTest.addGoalTemplate(gt2, sdcGraphTest, "child"); 
//
//    	List<Instance> theGTs = theTest.getAllGoalTemplates(sdcGraphTest); 
//    	System.out.print("all GTs: \n"); 
//    	for (Instance aGT : theGTs) {
//    		System.out.println(aGT.getIdentifier());
//    	}
//		
//		sdcGraphTest = theTest.deleteGoalTemplate(gt2, sdcGraphTest); 
//
//    	List<Instance> theGTs2 = theTest.getAllGoalTemplates(sdcGraphTest); 
//    	System.out.print("all GTs: \n"); 
//    	for (Instance aGT2 : theGTs2) {
//    		System.out.println(aGT2.getIdentifier());
//    	}
//    	
//    	Instance theGTtoFind = theTest.getSingleGoalTemplate(gt1, sdcGraphTest); 
//    	System.out.println(theGTtoFind.getIdentifier()); 
//    	
//		theTest.addGoalTemplate(gt2, sdcGraphTest, "child"); 
//		
//		theTest.addGoalGraphArc(gt1, gt2, sdcGraphTest, "subsume"); 
//		
//		System.out.println("testing get GGM by source"); 
//		List<Instance> theGGMs = theTest.getGoalGraphArcBySource(gt1, sdcGraphTest);
//    	for (Instance aGGM : theGGMs ) {
//    		System.out.println(aGGM.getIdentifier());
//    	}
//		
//		System.out.println("testing get GGM by target"); 
//		List<Instance> theGGMs2 = theTest.getGoalGraphArcByTarget(gt2, sdcGraphTest);
//    	for (Instance aGGM : theGGMs2 ) {
//    		System.out.println(aGGM.getIdentifier());
//    	}
//
//		System.out.println("testing get single GGM "); 
//		Instance aSingleGGM = theTest.getSingleGoalGraphArc(gt1, gt2, sdcGraphTest);
//    		System.out.println(aSingleGGM.getIdentifier());
//
//		// storing the current SDC Graph
////    	String SDCGraphDirectory = "D:/DERI/phd-thesis/repository/stolle/usecases/SDC-tests/";  
////    	theResoruceManager.storeSDCGraph(SDCGraphDirectory, "SDCgraphTestNew.wsml", sdcGraphTest); 
//    	
////    	List<Instance> theGTs = theTest.getAllGoalTemplates(sdcGraphTest); 
////    	
////    	System.out.print("all GTs: \n"); 
////    	for (Instance aGT2 : theGTs) {
////    		System.out.println(aGT2.getIdentifier());
////    	}
////
////    	System.out.print("get GT for gt1: \n"); 
////    	for (Instance aGT : theGTs) {
////    		Set<String> theIDs = aGT.listAttributeValues(theTest.wsmoFactory.createIRI(
////    				sdcGraphTest.getDefaultNamespace(), "description"));
////    		
////    		System.out.println(theIDs);
////    		
////    		Iterator theIDsIterator = theIDs.iterator();
////    		if ( theIDsIterator.next().toString().equals(gt1.getIdentifier().toString()) ) {
////    			System.out.println(aGT.getIdentifier()); 
////    		}
////    		System.out.println(theIDsIterator.next().toString().equals(gt1.getIdentifier().toString())); 
//    		
////    		for (String theID : theIDs) {
////    			System.out.println(theID); 
////    		}
//    		
////    		Object[] theIDsArray = theIDs.toArray(); 
////    		
////    		System.out.println(theIDsArray); 
////    		
////    		String theOne = (String) theIDsArray[0];  
////    		
////    		System.out.println(theOne); 
//    		
////    		System.out.println(theIDs.toArray().toString()); 
//    		
////    		String theOneINeed = theIDs.toString(); 
////    		
////    		System.out.println(theOneINeed); 
//    		
////    		Iterator theIDsIterator = theIDs.iterator();
//    		
////    		theIDsIterator.
////    		
////    		System.out.println(theIDs.equals(gt1.getIdentifier().toString())); 
//    		
//    		
//    		
////    		for (String anID : theIDs) {
////        		if (theIDs.contains(gt1.getIdentifier().toString())) {
////            		System.out.println(aGT.getIdentifier());
////        			
////        		}	
////    		}
//    		
////    	}
//		
///*
//		Parser parser = Factory.createParser(null); 
//		Serializer serializer = Factory.createSerializer(null);
//
//
//
//		// Loading the predefined SDC Graph Ontology Schema 
//    	Ontology sdcGraphTest = theResoruceManager.loadSDCGraphSchema(); 
//    	
//    	// reading instances 
//    	
////    	Set<Instance> theInstances = sdcGraphTest.listInstances();
////    	System.out.println("all possible position values: "); 	
////    	for (Instance anInstance : theInstances) {
////    		if (anInstance.listConcepts().contains(
////    				sdcGraphTest.findConcept(theTest.wsmoFactory.createIRI(
////    						sdcGraphTest.getDefaultNamespace(), "goalGraphPosition")))) {
////        			
////        		System.out.println(anInstance.getIdentifier()); 
////    			
////    		}
////    	}
//    	
//    	// test the GT adding 
//
//    	String path = "D:/DERI/phd-thesis/repository/stolle/usecases/SDC-tests/";
//    	String fileName = "goal-bestEurope.wsml"; 
//		File file = new File(path + fileName);
//		TopEntity[] te = parser.parse(new FileReader(file));
//		Goal theGTtoAdd = (Goal)te[0];
//    	System.out.print("the goal template for testing: "); 	
//    	System.out.println(theGTtoAdd.getIdentifier()); 	
//    	System.out.println("adding the goal template"); 	    	
//    	theTest.addGoalTemplate(theGTtoAdd, sdcGraphTest, "root");
//
//    	System.out.println("adding another goal template as a child node"); 	    	
//    	String fileName2 = "goal-bestAT.wsml"; 
//		File file2 = new File(path + fileName2);
//		TopEntity[] te2 = parser.parse(new FileReader(file2));
//		Goal theGTtoAdd2 = (Goal)te2[0];
//    	theTest.addGoalTemplate(theGTtoAdd2, sdcGraphTest, "child");
//
//    	
//    	System.out.println("adding another goal template as a child node"); 	    	
//    	String fileName3 = "goal-bestDE.wsml"; 
//		File file3 = new File(path + fileName3);
//		TopEntity[] te3 = parser.parse(new FileReader(file3));
//		Goal theGTtoAdd3 = (Goal)te3[0];
//    	theTest.addGoalTemplate(theGTtoAdd3, sdcGraphTest, "child");
//
//    	// test for getAllgoaltemplates 
//    	List<Instance> theCurrentGoalTemplates = theTest.getAllGoalTemplates(sdcGraphTest);
//    	System.out.println("the retrieved goal templates: ");
//    	for (Instance aGT : theCurrentGoalTemplates ) {
//    		System.out.println(aGT .getIdentifier()); 
//    	}
//
//    	// test for removeGoaltemplate 
////    	System.out.println("removing the best DE goal template");
////    	theTest.deleteGoalTemplate(theGTtoAdd3, sdcGraphTest);
////    	List<Instance> theCurrentGoalTemplates2 = theTest.getAllGoalTemplates(sdcGraphTest);
////    	System.out.println("now existing goal templates: ");
////    	for (Instance aGT : theCurrentGoalTemplates2 ) {
////    		System.out.println(aGT.getIdentifier()); 
////    	}
//
//    	// test for Web service addition and deletion 
//    	System.out.println("now testing WS addition & deletion");
//    	WebService ws1 = theTest.wsmoFactory.createWebService(
//    			theTest.wsmoFactory.createIRI("http:www.stolle.com#ws1"));
//    	WebService ws2 = theTest.wsmoFactory.createWebService(
//    			theTest.wsmoFactory.createIRI("http:www.stolle.com#ws2"));
//    	
//    	theTest.addWebService(ws1, sdcGraphTest); 
//    	theTest.addWebService(ws2, sdcGraphTest); 
//
//    	theTest.deleteWebService(ws2, sdcGraphTest); 
//    	
//    	// test for Arc ID Generation 
////    	Identifier anID = theTest.goalGraphArcIDGenerator(sdcGraphTest);
////    	System.out.println(anID.toString());
//    	
//    	theTest.addGoalGraphArc(theGTtoAdd, theGTtoAdd2, sdcGraphTest, "subsume");
//    	System.out.println(theTest.getAllGoalTemplates(sdcGraphTest).size());
//
////    	Identifier anID2 = theTest.goalGraphArcIDGenerator(sdcGraphTest);
////    	System.out.println(anID2.toString());
//
//    	theTest.addGoalGraphArc(theGTtoAdd2, theGTtoAdd3, sdcGraphTest, "subsume"); 
//    	theTest.addGoalGraphArc(theGTtoAdd, theGTtoAdd3, sdcGraphTest, "subsume");
//    	
//    	
//    	Instance theGGM = sdcGraphTest.findInstance(
//    			theTest.wsmoFactory.createIRI(sdcGraphTest.getDefaultNamespace(), "ggm2")); 
//    	System.out.println(theGGM.getIdentifier()); 
//    	
//    	theTest.deleteGoalGraphArc(sdcGraphTest, theGGM);
//    	// works 
//    	
//    	theTest.addGoalGraphArc(theGTtoAdd2, theGTtoAdd, sdcGraphTest, "plugin");
//    	theTest.addGoalGraphArc(theGTtoAdd3, theGTtoAdd, sdcGraphTest, "plugin");
//
//    	Instance theGGM2 = sdcGraphTest.findInstance(
//    			theTest.wsmoFactory.createIRI(sdcGraphTest.getDefaultNamespace(), "ggm3")); 
//
//    	theTest.deleteGoalGraphArc(sdcGraphTest, theGGM2);
//    	
//    	theTest.addGoalGraphArc(theGTtoAdd2, theGTtoAdd3, sdcGraphTest, "plugin");
//    	    	   	  	
//    	for (Instance aGGMofInterest : theTest.getGoalGraphArcBySource(theGTtoAdd2,sdcGraphTest)) {
//    		System.out.print("a GGM with bestAT as source: ");
//    		System.out.println(aGGMofInterest.getIdentifier());    		
//    	}
//
//    	for (Instance aGGMofInterest : theTest.getGoalGraphArcByTarget(theGTtoAdd,sdcGraphTest)) {
//    		System.out.print("a GGM with bestEurope as target: ");
//    		System.out.println(aGGMofInterest.getIdentifier());    		
//    	}
//
//		System.out.print("finding a single GGM with s = bestEU and t = bestAT: ");
//    	Instance theSingleGGM = theTest.getSingleGoalGraphArc(theGTtoAdd, theGTtoAdd2, sdcGraphTest);  	
//    	System.out.println(theSingleGGM.getIdentifier());
//
//    	theTest.addGoalGraphArc(theGTtoAdd, theGTtoAdd2, sdcGraphTest, "subsume");
//
////    	System.out.println(theGTtoAdd.getIdentifier());
//
//    	Instance aGT = theTest.getSingleGoalTemplate(theGTtoAdd,sdcGraphTest); 
//    	System.out.println(aGT.getIdentifier());
//    	
//    	theTest.addGoalTemplate(theGTtoAdd, sdcGraphTest, "child");
//    	
//    	// now for WS and WGMs 
//    	theTest.addDiscoveryCacheArc(theGTtoAdd, ws1, sdcGraphTest, "subsume"); 
//    	theTest.addDiscoveryCacheArc(theGTtoAdd, ws2, sdcGraphTest, "plugin"); 
//
//
//    	theTest.addDiscoveryCacheArc(theGTtoAdd2, ws1, sdcGraphTest, "plugin"); 
//    	
//    	theTest.deleteDiscoveryCacheArc(sdcGraphTest,
//    			theTest.getSingleDiscoveryCacheArc(theGTtoAdd, ws2, sdcGraphTest)); 
//
//    	theTest.addDiscoveryCacheArc(theGTtoAdd2, ws1, sdcGraphTest, "exact");
//    	
//    	theTest.addDiscoveryCacheArc(theGTtoAdd3, ws1, sdcGraphTest, "plugin"); 
//    	
//    	
//		// storing the current SDC Graph
//    	String SDCGraphDirectory = "D:/DERI/phd-thesis/repository/stolle/usecases/SDC-tests/";  
//    	theResoruceManager.storeSDCGraph(SDCGraphDirectory, "SDCgraphTest.wsml", sdcGraphTest); 
//*/ 	
//	}


}
