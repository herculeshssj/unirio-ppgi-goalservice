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

package org.deri.wsmx.discovery.rule;

import ie.deri.wsmx.core.configuration.annotation.*;
import ie.deri.wsmx.discovery.*;
import ie.deri.wsmx.discovery.util.*;

import java.util.*;

import org.apache.log4j.*;
import org.deri.wsmx.discovery.rule.complete.HWRuleCompleteDiscovery;
import org.omwg.logicalexpression.*;
import org.omwg.logicalexpression.terms.*;
import org.omwg.ontology.*;
import org.wsml.reasoner.api.*;
import org.wsml.reasoner.api.inconsistency.*;
import org.wsml.reasoner.impl.*;
import org.wsmo.common.*;
import org.wsmo.common.exception.*;
import org.wsmo.execution.common.exception.*;
import org.wsmo.factory.*;
import org.wsmo.service.*;
import org.wsmo.wsml.*;

/**
 * A lightweight discovery engine for WSML Flight/Rule.
 * Three different configuration parameters can be set: the 
 * reasoning engine, the query containment to be used and 
 * the targeted match type.
 * 
 * TODO: check for constraints and negation!
 * 
 * @author Holger Lausen, STI Innsbruck
 * @author Nathalie Steinmetz, STI Innsbruck
 * @version $Revision: 1.6 $ $Date: 2007/09/27 14:23:08 $
 */

@WSMXComponent(
		name = "LightweightDiscovery",
		events = "DISCOVERY",
		description = "A lightweight discovery engine.")
		
public class LWRuleDiscovery extends AbstractWSMODiscoveryImpl{
	
	protected static Logger log = Logger.getLogger(LWRuleDiscovery.class);
	
    private WSMLFlightReasoner reasoner;
	
    private WsmoFactory wsmoFactory;
	
    private LogicalExpressionFactory leFactory;
    
//    private HWRuleCompleteDiscovery heavyWeightEngine;
	
    private Map<WebService, LWRuleServiceDescription> serviceDescriptions;
    
    private String ns = "urn:foobar/";
        
    private String matchType = null;
    
    private static int unique = 0;
    
    private Map<String, Object> reasonerParams = new HashMap<String, Object>();
    
    private Set<Map<Variable, Term>> resultingMapping = new HashSet<Map<Variable,Term>>();
    
    // Strings for query containment types
    public final static String QUERY_CONTAINMENT = Discovery.DISCOVERY_ONTOLOGY_NS + "queryContainment";
    
    public final static String IRIS_QC = Discovery.DISCOVERY_ONTOLOGY_NS + "IrisQC";
    
    public final static String DISCOVERY_ENGINE_QC = Discovery.DISCOVERY_ONTOLOGY_NS + "DiscoveryEngineQC";
    
    private String queryContainmentType = null;
    
    // IRIs for different strategy and match types
    private IRI strategyIRI;
    
    private IRI typeOfMatchIRI;
    
    private IRI exactIRI;
    
    private IRI pluginIRI;
    
    private IRI subsumesIRI;
    
    private IRI lightWeightIRI;
    
    /**
     * <p>
     * Method to create a lightweight discovery engine for WSML Flight/Rule.
     * If no options are specified, default parameters are chosen:
     * </p>
     * <ul>
     *   <li> reasoning engine: IRIS </li>
     *   <li> targeted match type: no preference </li>
     *   <li> query containment type: IRIS built-in reasoning task
     * </ul>
     */
	public LWRuleDiscovery() {
		this(new HashMap<String, Object>());
	}
	
	/**
	 * <p>
	 * Method to create a lightweight discovery engine for WSML Flight/Rule.
	 * As parameter a map with possible configurations is accepted:
     * </p>
     * <ul>
     *   <li> reasoning engine, e.g. IRIS (default), MINS, KAON2 </li>
     *   <li> targeted match type, e.g. SUBSUME, PLUGIN, EXACT, INTERSECT 
     *        (all types per default)</li>
     *   <li> query containment type, e.g. IRIS built-in reasoning task 
     *   	  (default, if reasoning engine = IRIS), Discovery engine 
     *   	  query containment</li>
     * </ul>
     * <p>
	 * At the moment only Subsume, Plugin and Exact matches are supported by 
	 * this engine.
	 * </p>
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
	public LWRuleDiscovery(Map<String, Object> options) {
		serviceDescriptions = new HashMap<WebService, LWRuleServiceDescription>();
		
		// default values: reasoning engine IRIS, no match type preference
		if (options.containsKey(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER)) {
			reasonerParams.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER, 
					options.get(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER));
		}
		else {
			reasonerParams.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER, 
					WSMLReasonerFactory.BuiltInReasoner.IRIS);
		}
		if (options.containsKey(Discovery.TYPE_OF_MATCH)) {
			matchType = (String) options.get(Discovery.TYPE_OF_MATCH);
		}
		if (options.containsKey(QUERY_CONTAINMENT)) {
			queryContainmentType = (String) options.get(QUERY_CONTAINMENT);
		}
		else if (reasonerParams.get(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER).equals(
				WSMLReasonerFactory.BuiltInReasoner.IRIS)){
			queryContainmentType = IRIS_QC;
		}
		else {
			queryContainmentType = DISCOVERY_ENGINE_QC;
		}

		// create the reasoner and the wsmo factories
        reasoner = DefaultWSMLReasonerFactory.getFactory().
        	createWSMLFlightReasoner(reasonerParams);
        wsmoFactory = Factory.createWsmoFactory(null);
        leFactory = Factory.createLogicalExpressionFactory(null);
        
//        // create an instance of the HWRuleCompleteDiscovery
//        heavyWeightEngine = new HWRuleCompleteDiscovery(options);
        
        //TODO: check difference with default wsmo4j locator
        Factory.getLocatorManager().addLocator(new DefaultLocator());
        
        // build IRIs for different strategy and match types
        strategyIRI = wsmoFactory.createIRI(Discovery.STRATEGY);
        typeOfMatchIRI = wsmoFactory.createIRI(Discovery.TYPE_OF_MATCH);
        exactIRI = wsmoFactory.createIRI(Discovery.EXACT);
        pluginIRI = wsmoFactory.createIRI(Discovery.PLUGIN);
        subsumesIRI = wsmoFactory.createIRI(Discovery.SUBSUMES);
        lightWeightIRI = wsmoFactory.createIRI(Discovery.LEIGHTWEIGHT);
	}
	
	/**
	 * Adds a Web service to the discovery engine
	 * 
	 * @throws DiscoveryException if the service description does not contain 
	 * a proper description
	 */
	public void addWebService(WebService service) throws DiscoveryException {
//		// check whether heavyweight discovery is targeted and forward 
//		// request eventually
//		// !! only needed for building wsmt and eclipse plugins
//    	Set<Object> values = service.getCapability().listNFPValues(
//    			wsmoFactory.createIRI("http://www.wsmo.org/webservice/discovery/rule/extendedPlugin"));
//    	if ((values.size() > 0) && 
//    			(values.iterator().next().toString().toLowerCase().equals("true"))) {
//    		heavyWeightEngine.addWebService(service);
//    	}	
//    	else {
    		if (!serviceDescriptions.containsKey(service)) {
			log.debug("Adding to " + getClass().getSimpleName() + 
					" - " + service.getIdentifier());
            serviceDescriptions.put(service, getServiceLightDescr(service));
    		}
//    	}
	}
	
	/**
	 * Removes a Web service from the discovery engine.
	 * 
	 */
	public void removeWebService(WebService service) {
		if (!serviceDescriptions.containsKey(service))
			throw new IllegalArgumentException("Web service not found");
        serviceDescriptions.remove(service);
	}
	
	/**
	 * Returns the list of Web services that match the requester goal. 
	 * A match is an intersection of the sets of concrete objects 
	 * associated to the semantic descriptions of the requester and 
	 * provider of the service.
	 */
	public List<WebService> discover(Goal goal) throws ComponentException {
//		// check whether heavyweight discovery is targeted and forward 
//		// request eventually
//		// !! only needed for building wsmt and eclipse plugins
//    	Set<Object> values = goal.getCapability().listNFPValues(
//    			wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/rule/extendedPlugin"));
//    	if ((values.size() > 0) && 
//    			(values.iterator().next().toString().toLowerCase().equals("true"))) {
//    		heavyWeightEngine.addWebService(serviceDescriptions.keySet());
//    		return heavyWeightEngine.discover(goal);
//    	}
//    	else {
    		LWRuleServiceDescription desc = getServiceLightDescr(goal);
    			 
    		/*
    		 * Get targeted match type, if any. There are two ways to get it:
    		 * - from the non functional values of the goal
    		 * - from the parameter map given to the discovery engine at 
    		 *   creation time
    		 */
    		getNFPConfigurationProperties(goal);
    		        
    		return discover(desc);
//    	}
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
	
    private List<WebService> discover(LWRuleServiceDescription goal) 
    		throws ComponentException {
        List<WebService> result = new ArrayList<WebService>();
        Set<WebService> services = serviceDescriptions.keySet();      
        
        for (WebService service:services){
        	log.info("Comparing goal with Web service: " + service.getIdentifier());
        	LWRuleServiceDescription serviceObject = serviceDescriptions.get(service);
        	
        	// if no targeted match type is indicated
        	if (matchType == null){
	        	boolean subsume = checkQueryImplication(serviceObject, goal);
	            boolean plugin = checkQueryImplication(goal, serviceObject);

	            if (subsume && plugin){
	                addNFP(service,exactIRI);
	                result.add(service);
	                log.info("Exact match");
	            }
	            else if (subsume){
	                addNFP(service,subsumesIRI);
	                result.add(service);
	                log.info("Subsumes match");
	            }
	            else if (plugin){
	                addNFP(service,pluginIRI);
	                result.add(service);
	                log.info("Plugin match");
	            }
        	}
        	
        	// if targeted match type is given, check for targeted match type
        	else {
        		if (matchType.equals(Discovery.SUBSUMES)) {
	        		boolean subsume = checkQueryImplication(serviceObject, goal);
	        		if (subsume){
	                    addNFP(service,subsumesIRI);
	                    result.add(service);
	                    log.info("Subsumes match");
	                }
	        	}
	        	else if (matchType.equals(Discovery.PLUGIN)) {
	        		boolean plugin = checkQueryImplication(goal, serviceObject);
	        		if (plugin){
	                    addNFP(service,pluginIRI);
	                    result.add(service);
	                    log.info("Plugin match");
	                }
	        	}
	        	else if (matchType.equals(Discovery.EXACT)) {
	        		boolean subsume = checkQueryImplication(serviceObject, goal);
	                boolean plugin = checkQueryImplication(goal, serviceObject);
	                if (subsume && plugin){
	                    addNFP(service,exactIRI);
	                    result.add(service);
	                    log.info("Exact match");
	                }
	        	}
	        	else {
	       		 throw new UnsupportedOperationException("only support for exact, " +
	       		 		"subsume and plugin match so far");
	        	}
        	}
        }
        return result;
    }
    
	private LWRuleServiceDescription getServiceLightDescr(ServiceDescription service) 
		throws DiscoveryException {
        
        Capability capability = service.getCapability();

        Set<LogicalExpression> result = extractPostExpressions(capability);
        if (result.isEmpty()){
            String str = "no expressions in postcondition or effect";
            log.error(str);
            throw new LWRuleDiscoveryException(str);
        }
        
        Variable serviceObject = extractVariable(result,
                capability.listSharedVariables());
        
        //now just build conjunction of the expression
        //FIXME: variables not in sharedVar are now in one scope should check for it!
        LogicalExpression expression = null;
        for(LogicalExpression le : result){
            if (expression == null){
                expression = le;
            }else{
                expression = leFactory.createConjunction(expression, le);
            }
        }
        List<String> errors = new ArrayList<String>();
        LWRuleServiceDescriptionValidator val = new LWRuleServiceDescriptionValidator(errors);
        expression.accept(val);
        if (!errors.isEmpty()){
            throw new LWRuleDiscoveryException("invalid expression(s) found: "
                    +errors, errors);
        }
        
        Set<Ontology> onts = new HashSet<Ontology>();
        onts.addAll((Set<Ontology>)service.listOntologies());
        onts.addAll((Set<Ontology>)service.getCapability().listOntologies());
        LWRuleServiceDescription ret = new LWRuleServiceDescription(onts, serviceObject, expression);
        ret.setDefaultNS(service.getDefaultNamespace().getIRI().toString());

        return ret;
	}
	
    private Variable extractVariable(
            Set<LogicalExpression> expressions,
            Set<Variable> candidates) throws LWRuleDiscoveryException{
        Variable result=null;
        //lets collect all variables in post expression to check if the thin is valid
        Set<Variable> allVarsInPost = new HashSet<Variable>();
        for (LogicalExpression le : expressions){
            allVarsInPost.addAll(VariableCollector.getVariables(le));
        }
        //we need at least one variable
        if (allVarsInPost.isEmpty()){
            String str = "none of the expressions in postcondition did contain a variable";
            log.error(str);
            throw new LWRuleDiscoveryException(str);
        }

        if (candidates==null || candidates.isEmpty()){
            log.warn("shared variable must indicate object delivered by service");
            //OK lets guess that there is only one variable...
            if (allVarsInPost.size()==1){
                result = allVarsInPost.iterator().next();
            }else{
                String str = "no shared variable specified and post expressions contain more the one variable";
                log.error(str);
                throw new LWRuleDiscoveryException(str);
            }
        }else{
            if (candidates.size()==1){
                result = candidates.iterator().next();
            }else{
                String str = "more then one shared variable specified, only one allowed!";
                log.error(str);
                throw new LWRuleDiscoveryException(str);
            }
        }
        //make sure variable is in postCondition!
        if(allVarsInPost.contains(result)){
            return result;
        }else{
            String str = "shared Variable "+result+" not present in expression!";
            log.error(str);
            throw new LWRuleDiscoveryException(str);
        }
    }
    
    private Set<LogicalExpression> extractPostExpressions(Capability capability) throws LWRuleDiscoveryException{
        if (capability==null){
            log.warn("service description was empty");
            return null;
        }

        if (!capability.listPreConditions().isEmpty() ||
                !capability.listAssumptions().isEmpty()) {
            log.warn("Preconditions and assumptions will be ingored supported");
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
	
	public Map<Map<WebService, Interface>, Identifier> discover(Goal goal, Ontology rankingOntology) 
			throws ComponentException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
    
	/**
	 * Check for whether description1 implies description2 (i.e. subsume or 
	 * plugin match).
	 * 
	 * @param description1
	 * @param description2
	 * @return true if description1 implies description2, false otherwise
	 */
	private boolean checkQueryImplication(LWRuleServiceDescription description1, 
			LWRuleServiceDescription description2) {
		// check for query containment/implication
		if (queryContainmentType.equals(IRIS_QC)) {
			return getIRISQC(description1, description2);
		}
		else {
			return doesDesc1ImplyDesc2(description1, description2);
		}
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
	 * 		  or null if no discovery has been executed so far
	 */
	public Set<Map<Variable, Term>> getContainmentMapping() {
		log.debug("Return containment mapping");
		if (resultingMapping == null) {
			log.debug("No discovery executed so far, mapping is null");
			return null;
		}
		else {
//			System.out.println(resultingMapping);
			return resultingMapping;
		}
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
	private boolean getIRISQC (LWRuleServiceDescription description1, 
			LWRuleServiceDescription description2) {
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
		
		// check for query containment and get corresponding containment
		// mapping
		
		/* check for query containment */
		return reasoner.checkQueryContainment(
				description1.getExpressions(), description2.getExpressions());
	}
	
    /**
	 * Check for whether description1 implies description2 (i.e. subsume or 
	 * plugin match). This check is done using the methods own query containment 
	 * algorithm implementation.
     * 
     * @param description1
     * @param description2
     * @return true if description1 implies description2, false otherwise
     */
    private boolean doesDesc1ImplyDesc2(
            LWRuleServiceDescription description1,
            LWRuleServiceDescription description2){  
        try {
            log.debug("Discovery engine check: does description1 imply description2? " +
                    "\ndescription1: " + description1+
                    "\ndescription2: " + description2);

            Ontology o = wsmoFactory.createOntology(
                    wsmoFactory.createIRI("urn:foo#"+unique++));
            Axiom a = wsmoFactory.createAxiom(wsmoFactory.createAnonymousID());
            o.addAxiom(a);
            
            // create copy of logical expression
            LogicalExpression serviceExpression = 
                leFactory.createLogicalExpression(description1.getExpressions().toString());
            Term serviceTerm=wsmoFactory.createIRI(ns+unique++);
            
            // build frozen logical expression
            VariableTermReplacer visitor = new VariableTermReplacer(
                    description1.getVariable(), serviceTerm);
//            System.out.println("desc: "+serviceExpression);
            serviceExpression.accept(visitor);
//            System.out.println("desc: "+serviceExpression);
            
            a.addDefinition(serviceExpression);
            
            // add all service imports
            for (Ontology ontology:description1.listOntologies()){
                o.addOntology(ontology);
            }
            
            // add all goal imports
            for (Ontology ontology:description2.listOntologies()){
                o.addOntology(ontology);
            }
            
            // register ontology at reasoner and 
            // execute query to check for matches
            try {
                reasoner.registerOntology(o);
                Set<Map<Variable, Term>> result =reasoner.executeQuery(
                        description2.getExpressions());
                
                for (Map<Variable,Term> assignment : result){
                    resultingMapping.add(assignment);
                    Term t = assignment.get(description2.getVariable());
                    if (t!=null && t.equals(serviceTerm)){
                        return true;
                    }
                }
            } catch (InconsistencyException e) {
                // NO MATCH!
                return false;
            }
            return false;
        } catch (SynchronisationException e) {
            String err = "should never happen";
            log.error(err);
            throw new RuntimeException(err,e);
        } catch (InvalidModelException e) {
            String err = "should never happen";
            log.error(err);
            throw new RuntimeException(err,e);
        } catch (ParserException e) {
            String err = "should never happen";
            log.error(err);
            throw new RuntimeException(err,e);
        }
    }
    
    /*
     * Extract configuration properties from the non functional properties 
     * of the goal, if any.
     */
    private void getNFPConfigurationProperties(Goal goal) {
    	
    	// get type of match
    	Set<Object> values = goal.listNFPValues(typeOfMatchIRI);
    	if (values.size() > 0) {
    		Object value = values.iterator().next();
    		if (value instanceof IRI) {
    			matchType = ((IRI) value).toString();
    		}
    	}
    	
		// get choice for query containment
    	values = goal.listNFPValues(wsmoFactory.createIRI(QUERY_CONTAINMENT));
    	if (values.size() > 0) {
    		Object value = values.iterator().next();
    		if (value instanceof IRI) {
    			queryContainmentType = ((IRI) value).toString();
    		}
    	}
    }
    
    private void removeNFP(WebService ws){
        try{
            ws.removeNFP(typeOfMatchIRI);
            ws.removeNFP(strategyIRI);
        }catch (InvalidModelException e){
            throw new RuntimeException("should never happen",e);
        }
    }

    /*
     * add type of match and used strategy to the non functional 
     * properties of the web service
     */
    private void addNFP(WebService ws, IRI typeOfMatch){
        removeNFP(ws);
        try{
            ws.addNFPValue(typeOfMatchIRI, typeOfMatch);
            ws.addNFPValue(strategyIRI, lightWeightIRI);
        }catch (InvalidModelException e){
            throw new RuntimeException("should never happen",e);
        }
    }
}