/*
 * Copyright (c) 2008, University of Innsbruck, Austria.
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

package org.deri.wsmx.discovery.rule.complete;

import ie.deri.wsmx.core.configuration.annotation.*;
import ie.deri.wsmx.discovery.*;
import ie.deri.wsmx.discovery.util.*;

import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.*;
import org.deri.wsmx.discovery.rule.LWRuleDiscoveryException;
import org.deri.wsmx.discovery.rule.complete.HWRuleCompleteDiscovery;
import org.omwg.logicalexpression.*;
import org.omwg.logicalexpression.terms.*;
import org.omwg.ontology.*;
import org.semanticweb.kaon2.le;
import org.wsml.reasoner.api.*;
import org.wsml.reasoner.api.inconsistency.*;
import org.wsml.reasoner.impl.*;
import org.wsmo.common.*;
import org.wsmo.common.exception.*;
import org.wsmo.execution.common.exception.*;
import org.wsmo.factory.*;
import org.wsmo.service.*;
import org.wsmo.wsml.ParserException;


/**
 * A heavyweight discovery engine for WSML Flight/Rule.
 * One configuration parameter can be set: the targeted match type.
 * 
 * The discovery engine uses IRIS as underlying reasoning engine. It 
 * makes use of the IRIS query containment task.
 * 
 * TODO: check for constraints and negation!
 * 
 * @author Nathalie Steinmetz, STI Innsbruck
 * @version $Revision: $ $Date: $
 */

@WSMXComponent(
		name = "HeavyweightDiscovery",
		events = "DISCOVERY",
		description = "A heavyweight discovery engine.")
		
public class HWRuleCompleteDiscovery extends AbstractWSMODiscoveryImpl{
	
	protected static Logger log = Logger.getLogger(HWRuleCompleteDiscovery.class);
	
    private WSMLFlightReasoner reasoner;
	
    private WsmoFactory wsmoFactory;
	
    private LogicalExpressionFactory leFactory;
    
    private Map<WebService, HWRuleServiceDescription> servicePreconDescriptions;
    
    private Map<WebService, HWRuleServiceDescription> servicePostconDescriptions;
    
    private static int unique = 0;
    
    private Map<String, Object> reasonerParams = new HashMap<String, Object>();
    
    private Set<Map<Variable, Term>> resultingMapping = new HashSet<Map<Variable,Term>>();
    
    private Set<Map<Variable, Term>> finalContainmentMapping = new HashSet<Map<Variable, Term>>();
    
    // IRIs for different strategy and match types
    private IRI strategyIRI;
    
    private IRI typeOfMatchIRI;
    
    private IRI extendedPluginIRI;
    
    private IRI heavyWeightIRI;
    
    // used for evaluating heavyweight discovery
    private long a, b, c, d, e, f, g, h, i, j;
    
    /**
     * <p>
     * Method to create a heavyweight discovery engine for WSML Flight/Rule.
     * If no options are specified, default parameters are chosen:
     * </p>
     * <ul>
     *   <li> targeted match type: no preference </li>
     * </ul>
     */
	public HWRuleCompleteDiscovery() {
		this(new HashMap<String, Object>());
	}
	
	/**
	 * <p>
	 * Method to create a heavyweight discovery engine for WSML Flight/Rule.
	 * As parameter a map with possible configurations is accepted:
     * </p>
     * <ul>
     *   <li> targeted match type, e.g. SUBSUME, PLUGIN, EXACT 
     *        (all types per default)</li>
     * </ul>
     * <p>
     * There are two different possibilities to indicate the configurations:
     * </p>
     * <ul>
     *   <li> from the non functional property values of the goal</li>
     *   <li> from the parameter map given to the discovery engine at 
     *   creation time</li>
	 * </ul>
	 * <p>
	 * If both ways are used, then the values from the non functional properties 
	 * are used.
	 * </p>
	 * 
	 * @param options 
	 * 				Preferences for the discovery engine, e.g. reasoning 
	 * 				engine, targeted match type
	 */
	public HWRuleCompleteDiscovery(Map<String, Object> options) {
		servicePreconDescriptions = new HashMap<WebService, HWRuleServiceDescription>();
		servicePostconDescriptions = new HashMap<WebService, HWRuleServiceDescription>();
		
		reasonerParams.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER, 
					WSMLReasonerFactory.BuiltInReasoner.IRIS);

		// create the reasoner and the wsmo factories
        reasoner = DefaultWSMLReasonerFactory.getFactory().
        	createWSMLFlightReasoner(reasonerParams);
        wsmoFactory = Factory.createWsmoFactory(null);
        leFactory = Factory.createLogicalExpressionFactory(null);
        
        //TODO: check difference with default wsmo4j locator
        Factory.getLocatorManager().addLocator(new DefaultLocator());
        
        // build IRIs for different strategy and match types
        strategyIRI = wsmoFactory.createIRI(Discovery.STRATEGY);
        typeOfMatchIRI = wsmoFactory.createIRI(Discovery.TYPE_OF_MATCH);
        extendedPluginIRI = wsmoFactory.createIRI(Discovery.EXTENDED_PLUGIN);
        heavyWeightIRI = wsmoFactory.createIRI(Discovery.HEAVYWEIGHT);
	}
	
	/**
	 * Adds a Web service to the discovery engine
	 * 
	 * @throws DiscoveryException if the service description does not contain 
	 * a proper description
	 */
	public void addWebService(WebService service) throws DiscoveryException {
		if (!servicePostconDescriptions.containsKey(service)) {
			log.debug("Adding to " + getClass().getSimpleName() + 
					" - " + service.getIdentifier());
            servicePostconDescriptions.put(service, getServicePostconDescr(service));
            servicePreconDescriptions.put(service, getServicePreconDescr(service));
		}
	}
	
	/**
	 * Removes a Web service from the discovery engine.
	 * 
	 */
	public void removeWebService(WebService service) {
		if (!servicePostconDescriptions.containsKey(service))
			throw new IllegalArgumentException("Web service not found");
        servicePostconDescriptions.remove(service);
        servicePreconDescriptions.remove(service);
	}

	/**
	 * Selects from the input search space the list of Web services 
	 * that match the requester goal. 
	 */
	public List<WebService> discover(Goal goal, Set<WebService> searchSpace) 
			throws DiscoveryException {
		addWebService(searchSpace);
		try {
			return discover(goal);
		} catch (ComponentException e) {
			throw new DiscoveryException(e);
		}
	}
    
	public Map<Map<WebService, Interface>, Identifier> discover(Goal goal, Ontology rankingOntology) 
			throws ComponentException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns the list of Web services that match the requester goal. 
	 * A match is an intersection of the sets of concrete objects 
	 * associated to the semantic descriptions of the requester and 
	 * provider of the service.
	 */
	public List<WebService> discover(Goal goal) throws ComponentException {
		// get time before starting discovery process and 
    	// before transforming web services
    	a = System.currentTimeMillis();
    	
        HWRuleServiceDescription pre = getServicePreconDescr(goal);
        HWRuleServiceDescription post = getServicePostconDescr(goal);
        
    	// get time after transforming web services
    	b = System.currentTimeMillis();
    	
        return discover(pre, post);
	}
	
    private List<WebService> discover(
    		HWRuleServiceDescription pre, HWRuleServiceDescription post) 
    		throws ComponentException {
    	
    	List<WebService> preConditionResult = new ArrayList<WebService>();
    	List<WebService> finalResult = new ArrayList<WebService>();
        Set<WebService> services = servicePreconDescriptions.keySet();      
        
        for (WebService service:services){	
        	
        	/* get containment mapping for preconditions */

//        	log.info("Comparing goal with Web service: " + service.getIdentifier());
        	HWRuleServiceDescription serviceObject = servicePreconDescriptions.get(service);      	
        	
        	resultingMapping.clear();
        	Set<Map<Variable, Term>> subsumeSet = getContainmentMapping(serviceObject, pre);
//        	for (Map<Variable, Term> assignment : resultingMapping) {
//        		System.out.println("After subsume: " + assignment);
//        	}
        	boolean subsume = subsumeSet.size() > 0;
        	Set<Map<Variable, Term>> pluginSet = getContainmentMapping(pre, serviceObject);
        	resultingMapping.addAll(pluginSet);
//        	for (Map<Variable, Term> assignment : resultingMapping) {
//        		System.out.println("After plugin: " + assignment);
//        	}
        	boolean plugin = pluginSet.size() > 0;
        		
        	if (subsume && plugin){
        		preConditionResult.add(service);
        	}
        	else if (plugin){
        		preConditionResult.add(service);
        	}
        	
        	/*
        	 * if the preconditions did not match we can interrupt the 
        	 * discovery step here
        	 */
//        	System.out.println(preConditionResult.size());
        	if (preConditionResult.isEmpty())
        		return finalResult;
        	
        	// get time before building final, injective containment mapping
        	e = System.currentTimeMillis();
        	
        	/* 
        	 * further transform the containment mapping that had been 
        	 * returned from the reasoner and build the final mapping from 
        	 * description2's variables to the variables of description1
        	 */
        	for (Map<Variable, Term> assignment : resultingMapping) {
        		Set<Variable> keySet = assignment.keySet();
        		HashMap<Variable, Term> tmpMap = new HashMap<Variable, Term>();
        		Set<Variable> vars = new HashSet<Variable>();
        		// if the mapping is not injective we will not add it to the final mappings
        		boolean injective = true;
        		for (Variable v : keySet) {
        			String term = assignment.get(v).toString();
        			Variable tmp = leFactory.createVariable(term.substring(16));
        			if(vars.add(tmp)) 
        				tmpMap.put(v, tmp);
        			else
        				injective = false;
        		}
        		if (injective)
        			finalContainmentMapping.add(tmpMap);
        	}
        	
        	// get time for building final, injective containment mapping
        	f = System.currentTimeMillis();
	 	
        	for (Map<Variable, Term> assignment : finalContainmentMapping) {
//   	     	System.out.println("assignment: " + assignment);
        		
        		// get time before building extended plugin match specific queries
        		g = System.currentTimeMillis();
        		
        		/* build extended plugin match specific queries */
        		
        		List<Variable> variables = new ArrayList<Variable>();

        		/* build goal query */
        		LogicalExpression goal = buildExtendedPluginQuery(post, assignment, true);
        		
        		HWRuleServiceDescription goalPostDescription = 
        			new HWRuleServiceDescription(post.listOntologies(), post.getVariables(), goal);
//        		System.out.println("Goal: " + goal.toString());
        		
    			/* build service query */
        		HWRuleServiceDescription servicePostDescription = null;
        		HWRuleServiceDescription serviceDesc = getServicePostconDescr(service);
        		LogicalExpression servic = buildExtendedPluginQuery(serviceDesc, assignment, false);
        		servicePostDescription = 
        			new HWRuleServiceDescription(serviceDesc.listOntologies(), serviceDesc.getVariables(), servic);
//        		System.out.println("Service: " + servic.toString());	
        		
        		// get time for building extended plugin match specific queries
        		h = System.currentTimeMillis();
        		
                /* check for query containment with extended plugin queries */
    			subsume = checkQueryContainment(servicePostDescription, goalPostDescription);
        		if (subsume) {
    				addNFP(service, extendedPluginIRI);
            		finalResult.add(service);
    			}
    			
    			// get time for doing query containment at reasoner
    			i = System.currentTimeMillis();
        	}
        }
        // get time before returning final result
        j = System.currentTimeMillis();

		return finalResult;
    }
    
    /**
     * Build a query for the extended plugin match, as developed in D2.3 in SWING, and 
     * explained in D2.4 in SWING as well.
     * 
     * @param expr 
     * @param mapping
     * @param goal whether this logical expression is built for the goal description (true) 
     * 		  or for the service (false)
     * @return LogicalExpression 
     * @throws ComponentException 
     */
    private LogicalExpression buildExtendedPluginQuery(
    		HWRuleServiceDescription desc, Map<Variable, Term> assignment, boolean goal) 
    		throws ComponentException {
    	LogicalExpression le;
    	String leString = "";
				
    	// add order part
    	leString = leString.concat("_\"" + Discovery.ORDER + "\"(");
    	int i=0;
    	for (Entry<Variable, Term> entrySet : assignment.entrySet()) {
    		if (i!=0)
    			leString = leString.concat(", ");   
    		// for the goal we use the values of the mapping
    		if (goal)
    			leString = leString.concat(entrySet.getValue().toString());
    		// for the services we use the key values of the mapping
    		else  
    			leString = leString.concat(entrySet.getKey().toString());
    		i++;
    	}	
    	leString = leString.concat(")");
//		System.out.println("leString: " + leString);

    	// add shared part
    	for (Variable var : desc.getVariables())
    		leString = leString.concat(" and _\"" + Discovery.SHARED + "\"(" + var.toString() + ")");
//		System.out.println("leString: " + leString);
			
    	// add dif part 			
    	Set<Vector<Variable>> vecSet = new HashSet<Vector<Variable>>();
    	List<Variable> vars = new ArrayList<Variable>();
    	for (Entry<Variable, Term> entrySet : assignment.entrySet()) {
    		// for the goal we use the values of the mapping
    		if (goal)
    			vars.add((Variable) entrySet.getValue());
    		// for the services we use the key values of the mapping
    		else 
    			vars.add((Variable) entrySet.getKey());
    	}
    	for (int x=0; x<vars.size(); x++) {
    		for (int y=0; y<vars.size(); y++) {
    			Vector<Variable> tmpVec = new Vector<Variable>();
    			Vector<Variable> tmpVec2 = new Vector<Variable>();
    			if (!vars.get(x).equals(vars.get(y))) {
    				tmpVec.add(0, vars.get(x));
    				tmpVec.add(1, vars.get(y));
    				tmpVec2.add(0, vars.get(y));
    				tmpVec2.add(1, vars.get(x));
    				if (!vecSet.contains(tmpVec2))
    					vecSet.add(tmpVec);
    			}
    		}
    	}
    	for (Vector<Variable> vec : vecSet) {     				
    		leString = leString.concat(" and _\"" + Discovery.DIF + "\"(" + vec.get(0) + ", " + vec.get(1) + ")");
		}	
//		System.out.println("leString: " + leString);

		try {
			// build final goal query
			LogicalExpression expr = desc.getExpressions();
			le = leFactory.createLogicalExpression(
					expr.toString().replace(". ", " and ") + leString + ".");
		} catch (ParserException e) {
			throw new ComponentException(e);
		}
		return le;
	}
    
	/**
	 * Check for whether description1 is contained within description2, 
	 * i.e. whether description1 implies description2 (i.e. subsume or 
	 * plugin match). This check is done using the query containment reasoning 
	 * task of the IRIS reasoning engine.
	 * 
	 * @param description1
	 * @param description2
	 * @return true if description1 is contained within description2, false otherwise
	 */
	private boolean checkQueryContainment (HWRuleServiceDescription description1, 
			HWRuleServiceDescription description2) {
		log.debug("IRIS Check: does description1 imply description2? " +
                "\ndescription1: " + description1 +
                "\ndescription2: " + description2 +
                "\n IRIS Return containment mapping");
		Ontology o = wsmoFactory.createOntology(
                wsmoFactory.createIRI("urn:foo#" + unique++));
		
		// add all service imports
        for (Ontology ontology:description1.listOntologies()){
            o.addOntology(ontology);
        }
        
        // add all goal imports
        for (Ontology ontology:description2.listOntologies()){
            o.addOntology(ontology);
        }

        // register ontology at reasoner
        try {
			reasoner.registerOntology(o);
		} catch (InconsistencyException e) {
			// no match
			return false;
		}

		// check for query containment
		return reasoner.checkQueryContainment(
				description1.getExpressions(), description2.getExpressions());
		
	}
	
	/**
	 * Check for whether description1 implies description2 (i.e. subsume or 
	 * plugin match). This check is done using the query containment reasoning 
	 * task of the IRIS reasoning engine.
	 * 
	 * @param description1
	 * @param description2
	 * @return true if description1 implies description2, false otherwise
	 */
	private Set<Map<Variable, Term>> getContainmentMapping (HWRuleServiceDescription description1, 
			HWRuleServiceDescription description2) {
		log.debug("IRIS Check: does description1 imply description2? " +
                "\ndescription1: " + description1 +
                "\ndescription2: " + description2 +
                "\n IRIS Return containment mapping");
		Ontology o = wsmoFactory.createOntology(
                wsmoFactory.createIRI("urn:foo#" + unique++));
		
		// add all service imports
        for (Ontology ontology:description1.listOntologies()){
            o.addOntology(ontology);
        }
        
        // add all goal imports
        for (Ontology ontology:description2.listOntologies()){
            o.addOntology(ontology);
        }
        
        // register ontology at reasoner
        try {
			reasoner.registerOntology(o);
		} catch (InconsistencyException e) {
			// no match
			return new HashSet<Map<Variable, Term>>();
		}
		
		// get time after registering ontologies at the reasoner
    	c = System.currentTimeMillis();

		// check for query containment and get corresponding containment
		// mapping
		Set<Map<Variable, Term>> result =  reasoner.getQueryContainment(
				description1.getExpressions(), description2.getExpressions());
		
		// get time after getting containment mapping from reasoner
		d = System.currentTimeMillis();
		
		return result;
	}
    
	/**
	 * <p> Get containment mapping that results from check whether one 
	 * description implies another (i.e. subsume or plugin match). 
	 * </p>
	 * <p>
	 * If the map is null, then no discovery check has been executed 
	 * before. Otherwise the map is either empty or filled with the 
	 * variable mapping.
	 * </p>
	 * 
	 * @return Set<Map<Variable, Term>>, the resulting variable mapping 
	 * 		  or an empty set if no discovery has been executed so far
	 */
	public Set<Map<Variable, Term>> getContainmentMapping() {
		log.debug("Return containment mapping");
		return resultingMapping;
	}
	
	private HWRuleServiceDescription getServicePreconDescr(ServiceDescription service) 
			throws DiscoveryException {
    
		Capability capability = service.getCapability();

		Set<LogicalExpression> result = extractPreExpressions(capability);
		if (result.isEmpty()){
			String str = "no expressions in precondition or assumption";
			log.error(str);
			throw new LWRuleDiscoveryException(str);
		}
    
		LogicalExpression expression = null;
		for(LogicalExpression le : result){
			if (expression == null){
				expression = le;
			}else{
				expression = leFactory.createConjunction(expression, le);
			}
		}
		List<String> errors = new ArrayList<String>();
		HWRuleServiceDescriptionValidator val = new HWRuleServiceDescriptionValidator(errors);
		expression.accept(val);
		if (!errors.isEmpty()){
			throw new LWRuleDiscoveryException("invalid expression(s) found: "
					+errors, errors);
		}
    
		Set<Ontology> onts = new HashSet<Ontology>();
		onts.addAll((Set<Ontology>)service.listOntologies());
		onts.addAll((Set<Ontology>)service.getCapability().listOntologies());
		HWRuleServiceDescription ret = new HWRuleServiceDescription(
				onts, capability.listSharedVariables(), expression);
		ret.setDefaultNS(service.getDefaultNamespace().getIRI().toString());

		return ret;
	}

	private HWRuleServiceDescription getServicePostconDescr(ServiceDescription service) 
			throws DiscoveryException {

		Capability capability = service.getCapability();

		Set<LogicalExpression> result = extractPostExpressions(capability);
		if (result.isEmpty()){
			String str = "no expressions in postcondition or effect";
			log.error(str);
			throw new LWRuleDiscoveryException(str);
		}

		LogicalExpression expression = null;
		for(LogicalExpression le : result){
			if (expression == null){
				expression = le;
			}else{
				expression = leFactory.createConjunction(expression, le);
			}
		}
		List<String> errors = new ArrayList<String>();
		HWRuleServiceDescriptionValidator val = new HWRuleServiceDescriptionValidator(errors);
		expression.accept(val);
		if (!errors.isEmpty()){
			throw new LWRuleDiscoveryException("invalid expression(s) found: "
					+errors, errors);
		}

		Set<Ontology> onts = new HashSet<Ontology>();
		onts.addAll((Set<Ontology>)service.listOntologies());
		onts.addAll((Set<Ontology>)service.getCapability().listOntologies());
		HWRuleServiceDescription ret = new HWRuleServiceDescription(
				onts, capability.listSharedVariables(), expression);
		ret.setDefaultNS(service.getDefaultNamespace().getIRI().toString());

		return ret;
	}	
	
	/*
	 * Extract all logical expressions from the assumptions and preconditions 
	 * of the given capability.
	 */
	private Set<LogicalExpression> extractPreExpressions(Capability capability) throws LWRuleDiscoveryException{
		if (capability==null){
			log.warn("service description was empty");
			return null;
		}

		Set<LogicalExpression> result = new HashSet<LogicalExpression>(); 
		if(capability.listAssumptions()!=null){
			for (Axiom a:(Set<Axiom>)capability.listAssumptions()){
				result.addAll((Set<LogicalExpression>)a.listDefinitions());
			}
		}
		if(capability.listPreConditions()!=null){
			for (Axiom a:(Set<Axiom>)capability.listPreConditions()){
				result.addAll((Set<LogicalExpression>)a.listDefinitions());
			}
		}
		return result;
	}

	/*
	 * Extract all logical expressions from the effects and postconditions 
	 * of the given capability.
	 */
	private Set<LogicalExpression> extractPostExpressions(Capability capability) throws LWRuleDiscoveryException{
		if (capability==null){
			log.warn("service description was empty");
			return null;
		}

		Set<LogicalExpression> result = new HashSet<LogicalExpression>(); 
		if(capability.listEffects()!=null){
			for (Axiom a:(Set<Axiom>)capability.listEffects()){
				result.addAll((Set<LogicalExpression>)a.listDefinitions());
			}
		}
		if(capability.listPostConditions()!=null){
			for (Axiom a:(Set<Axiom>)capability.listPostConditions()){
				result.addAll((Set<LogicalExpression>)a.listDefinitions());
			}
		}
		return result;
	}
    
    /*
     * add type of match and used strategy to the non functional 
     * properties of the web service
     */
    private void addNFP(WebService ws, IRI typeOfMatch){
        removeNFP(ws);
        try{
            ws.addNFPValue(typeOfMatchIRI, typeOfMatch);
            ws.addNFPValue(strategyIRI, heavyWeightIRI);
        }catch (InvalidModelException e){
            throw new RuntimeException("should never happen",e);
        }
    }
    
    /*
     * remove type of match and used strategy from the non functional
     * properties of the web services
     */
    private void removeNFP(WebService ws){
        try{
            ws.removeNFP(typeOfMatchIRI);
            ws.removeNFP(strategyIRI);
        }catch (InvalidModelException e){
            throw new RuntimeException("should never happen",e);
        }
    }
    
    /**
     * Measures runtime of heavyweight discovery.
     */
    public void printEvaluation() {
    	System.out.println("\n---------------------------------------------------");
    	System.out.println("--------------- Runtime Measurement ---------------");
    	System.out.println("---------------------------------------------------\n");

    	System.out.println("Time for internally transforming web services: " + 
    			(b-a));
    	System.out.println("Time for registering ontologies at the reaoner: " + 
    			(c-b));
    	System.out.println("Time for getting containment mapping from reasonre: " +
    			(d-c));
    	System.out.println("Time for building final, injective containment " +
    			"mapping: " + (f-e));
    	System.out.println("Time for building extended plugin match specific " +
    			"queries: " + (h-g));
    	System.out.println("Time for doing query containment at reasoner: " +
    			(i-h));
    	System.out.println("Time for complete heavyweight discovery process: " + 
    			(j-a));
    	System.out.println("");
    }
}