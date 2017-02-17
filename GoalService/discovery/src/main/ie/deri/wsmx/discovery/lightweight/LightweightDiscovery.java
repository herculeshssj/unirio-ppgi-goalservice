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

package ie.deri.wsmx.discovery.lightweight;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.core.configuration.annotation.*;
import ie.deri.wsmx.discovery.*;
import ie.deri.wsmx.discovery.util.*;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;
import org.omwg.logicalexpression.*;
import org.omwg.ontology.*;
import org.wsml.reasoner.api.*;
import org.wsml.reasoner.api.inconsistency.*;
import org.wsml.reasoner.impl.*;
import org.wsml.reasoner.transformation.*;
import org.wsmo.common.*;
import org.wsmo.common.exception.*;
import org.wsmo.execution.common.exception.*;
import org.wsmo.factory.*;
import org.wsmo.service.*;
import org.wsmo.wsml.*;

import br.uniriotec.aspect.simulation.SimulationHelper;

/**
 * A lightweight discovery engine.
 * 
 * @author Adina Sirbu
 * @version $Revision: 1.12 $ $Date: 2007-01-30 14:49:00 $
 */
@WSMXComponent(
		name = "LightweightDiscovery",
		events = "DISCOVERY",
		description = "A lightweight discovery engine.")
public class LightweightDiscovery extends  AbstractWSMODiscoveryImpl{
	
	protected static Logger logger = Logger.getLogger(LightweightDiscovery.class);
	
	private WSMLFlightReasoner flightReasoner;
	
	private WsmoFactory wsmoFactory;
	
	private LogicalExpressionFactory leFactory;
	
	private DataFactory dataFactory;
	
	private Map<WebService, Ontology> lightweightDescriptions;
	
	private InstantiatingVisitor visitor;
	
	public LightweightDiscovery() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER, WSMLReasonerFactory.BuiltInReasoner.IRIS);
        flightReasoner = DefaultWSMLReasonerFactory.getFactory().
        	createWSMLFlightReasoner(params);
        wsmoFactory = Factory.createWsmoFactory(new HashMap());
        leFactory = Factory.createLogicalExpressionFactory(new HashMap());
        dataFactory = Factory.createDataFactory(new HashMap());
        
        Factory.getLocatorManager().addLocator(new DefaultLocator());
        
        visitor = new InstantiatingVisitor(wsmoFactory, leFactory, dataFactory);
        lightweightDescriptions = new HashMap<WebService, Ontology>();
	}
	
	/**
	 * Adds a Web service to the discovery engine
	 * @throws DiscoveryException if the service description does not contain 
	 * a functional description
	 */
	public void addWebService(WebService service) throws DiscoveryException {
		if (!lightweightDescriptions.containsKey(service)) {
			logger.debug("Adding to " + getClass().getSimpleName() + 
					" - " + service.getIdentifier());
			lightweightDescriptions.put(service, getServiceLightDescr(service));
		}
	}
	
	
	public void removeWebService(WebService service) {
		if (!lightweightDescriptions.containsKey(service))
			throw new IllegalArgumentException("Web service not found");
		lightweightDescriptions.remove(service);
	}
	
	
	/**
	 * Returns the list of Web services that match the requester goal. 
	 * A match is an intersection of the sets of concrete objects 
	 * associated to the semantic descriptions of the requester and 
	 * provider of the service.
	 */
	public List<WebService> discover(Goal goal) 
			throws ComponentException, UnsupportedOperationException {
		List<WebService> webServices = new ArrayList<WebService>();
		try {
			webServices.addAll(
					discover(goal, new HashSet<WebService>(lightweightDescriptions.keySet())));
		} catch (DiscoveryException e) {
			throw new ComponentException(e);
		}
		return webServices;	
	}
	
	/*
	 * Selects from the input search space the list of Web services 
	 * that match the requester goal. 
	 */
	public List<WebService> discover(Goal goal, Set<WebService> searchSpace) 
			throws DiscoveryException {
		
		/*
		 * Code for operational goal simulation
		 */
		List<String> webServices = new ArrayList<>();
		for (WebService ws : searchSpace) {
			String[] temp = ws.getIdentifier().toString().split("/");
			webServices.add(temp[5]);
		}
		SimulationHelper.saveIdentifiedSWS(webServices);
		/* End of code for operationa goal simulation */
		
		List<WebService> matchingServices = new ArrayList<WebService>();
		
		Set<LogicalExpression> goalQueries = this.getServiceQueries(goal);
		Ontology goalDescr = this.getServiceLightDescr(goal);
		
		for (WebService service : lightweightDescriptions.keySet()) {
			if (!searchSpace.contains(service))
				continue;
			logger.info("Testing Web service " + service.getIdentifier() + "\n");
			
			Ontology matchOnt = createTempOntology((Set<Ontology>)goal.listOntologies(), 
						(Set<Ontology>)service.listOntologies());
			
			logger.debug("Testing goal with respect to Web service");
			boolean pluginMatch = entails(matchOnt, lightweightDescriptions.get(service), goalQueries);
			
			logger.debug("Testing Web service with respect to goal");
			boolean subsumesMatch = entails(matchOnt, goalDescr, getServiceQueries(service));
			
			if (pluginMatch && subsumesMatch) {
				// quickfix: the exact match is always the first in the return set
				logger.debug(service.getIdentifier() + " is an exact match");
				matchingServices.add(0, service);
			} else
				if (pluginMatch || subsumesMatch) {
					logger.debug(service.getIdentifier() + " matches");
					matchingServices.add(service);
			} else
				logger.debug(service.getIdentifier() + " doesn't match");
		}	
		return matchingServices;	
	}
	
	private boolean entails(Ontology baseOntology, Ontology lightweightDescr, 
			Set<LogicalExpression> queries) {
		boolean match = false;
		
		logger.debug("Lightweight description is: \n" + this.prettyPrint(lightweightDescr));
		try {
			Set<Axiom> axioms = (Set<Axiom>)lightweightDescr.listAxioms();
			for (Axiom a : axioms)
				baseOntology.addAxiom(a);
			
			this.prettyPrint(baseOntology);
			
			flightReasoner.registerOntology(baseOntology);
			
			for (LogicalExpression query : queries) {
				logger.debug("Query is: \n" + query);
				if (!flightReasoner.executeQuery(
						(IRI)baseOntology.getIdentifier(), query).isEmpty()) {
					match = true;
					logger.debug("Match!");
					break;
				}
			}
			flightReasoner.deRegisterOntology((IRI)baseOntology.getIdentifier());
			for (Axiom a : axioms) {
				baseOntology.removeAxiom(a);
				//FIXME - possible bug
				lightweightDescr.addAxiom(a);
			}
		} catch (InvalidModelException e) {
			logger.error("Invalid resulting ontology", e);
			e.printStackTrace();
			return false;
		} catch (InconsistencyException e) {
			logger.error("Resulting ontology is inconsistent", e);
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			logger.error("Other ontology error", e);
			e.printStackTrace();
			return false;
		}
		return match;
	}
	
	/*
	 * Builds an ontology from the postconditions and effects 
	 * of the input Web service or goal
	 */
	private Ontology getServiceOntology(ServiceDescription serviceDescr) 
			throws UnsupportedOperationException {
		//fixing unique identifiers
		Ontology descr = wsmoFactory.createOntology(
				wsmoFactory.createIRI("http://www.wsmx.org/discovery/ontology-" + 
						Math.abs(Helper.getRandomLong())));
		try {
			for (Axiom axiom : 
					(Set<Axiom>)serviceDescr.getCapability().listPostConditions())
				descr.addAxiom(axiom);
				
			for (Axiom axiom : 
					(Set<Axiom>)serviceDescr.getCapability().listEffects())
				descr.addAxiom(axiom);
		} catch (InvalidModelException e) {
			logger.error("Invalid model found on service or goal " + serviceDescr.getIdentifier());
			e.printStackTrace();
		}
		
		return descr;
	}
	
	/*
	 * Builds set of logical expressions from the input service description 
	 */
	private Set<LogicalExpression> getServiceQueries(ServiceDescription serviceDescr) 
			throws DiscoveryException {
		Ontology ont = this.getServiceOntology(serviceDescr);
		if (ont.listAxioms().isEmpty())
			throw new DiscoveryException(
					serviceDescr.getIdentifier().toString() + 
					" has no functional description");

		Set<LogicalExpression> sdExprs = this.split(ont); 	
		//logger.debug("Service description query:\n" + sdQuery.toString() + "\n");
		return sdExprs;
	}
	
	private Ontology getServiceLightDescr(ServiceDescription serviceDescr) 
		throws DiscoveryException {
		
		if (!serviceDescr.getCapability().listPreConditions().isEmpty() ||
				!serviceDescr.getCapability().listAssumptions().isEmpty()) {
			logger.debug("Preconditions and assumptions are not supported");
			//ignore preconditions/effects		
//			throw new DiscoveryException(
//					serviceDescr.getIdentifier().toString() + 
//					" - preconditions and assumptions are not supported");
		}
		
		Ontology descr = this.getServiceOntology(serviceDescr);
		if (descr.listAxioms().isEmpty())
			throw new DiscoveryException(
					serviceDescr.getIdentifier().toString() + 
					" - has no functional description");
		
		Ontology newOnt = wsmoFactory.createOntology(wsmoFactory.createIRI(
				"http://www.wsmx.org/discovery/newOntology-" + 
				(Math.abs(Helper.getRandomLong()))));
		
		for (LogicalExpression expr : split(descr))
			this.addExprToOntology(expr, newOnt);
			
		return visitor.getProjection(newOnt);
	}
	
	/* 
	 * Splits the axioms in the input ontology into the their corresponding 
	 * disjunctive parts. Currently only one disjunction is supported
	 */
	private Set<LogicalExpression> split(Ontology ont) {
		Set<LogicalExpression> exprs = new HashSet<LogicalExpression>();
		
		OntologyNormalizer normalizer = new ConstructReductionNormalizer(
				new WSMO4JManager(wsmoFactory, leFactory, dataFactory));
		Ontology normalized = ont;
		Set<Axiom> axioms = normalizer.normalizeAxioms(ont.listAxioms());
		try {
			for (Axiom axiom : normalized.listAxioms())
				normalized.removeAxiom(axiom);
			for (Axiom axiom : axioms)
				normalized.addAxiom(axiom);
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Ontology normalized = normalizer.normalize(ont);
		
		Axiom first = ((Set<Axiom>)normalized.listAxioms()).iterator().next();
		LogicalExpression firstExpr = 
			((Set<LogicalExpression>)first.listDefinitions()).iterator().next();
		
		if (firstExpr instanceof Disjunction) {
			logger.debug("Found disjunction");
			Disjunction expr = (Disjunction)firstExpr;
			exprs.add(expr.getLeftOperand());	
			exprs.add(expr.getRightOperand());
		}
		else 
			exprs.add(firstExpr);
		
		return exprs;
	}
	
	/*
	 * Wraps the expression into a new axiom and adds it to the ontology 
	 */
	private Ontology addExprToOntology(LogicalExpression expr, Ontology ont) {
		
		//fixed ontology marging bug
		Axiom axiom = wsmoFactory.createAxiom(wsmoFactory.createIRI("http://www.wsmx.org/discovery/axiom-"+Math.abs(Helper.getRandomLong())));
		axiom.addDefinition(expr);
		
		try {
			ont.addAxiom(axiom);
		} catch (InvalidModelException e) {
			e.printStackTrace();
		}
		return ont;
	}
	
	/*
	 * Creates a temporary ontology importing the ontologies from both sets
	 */
//	private Ontology getMatchOntology(Set<Ontology> set1, Set<Ontology> set2) {
//		Ontology matchOnt = wsmoFactory.createOntology(wsmoFactory.createIRI(
//				"http://www.wsmx.org/discovery/matchOntology-" + 
//				Math.abs(Helper.getRandomLong())));
//
//		for (Ontology ont1 : set1)
//			matchOnt.addOntology(ont1);
//		for (Ontology ont2 : set2)
//			matchOnt.addOntology(ont2);	
//		return matchOnt;		
//	}	
	
	/*
	 * Returns the reunion of the two sets of ontologies as a new, 
	 * temporary ontology 
	 */
	private Ontology createTempOntology(Set<Ontology> set1, Set<Ontology> set2) {

	    //fixed support for unique identifiers
		String uri = "http://www.wsmx.org/discovery/tempOntology-" +
				Math.abs(Helper.getRandomLong());;
		Ontology temp = wsmoFactory.createOntology(
				wsmoFactory.createIRI(uri + ".wsml"));
		
		Set<Ontology> reunion = new HashSet<Ontology>();
		reunion.addAll(set1);
		reunion.addAll(set2);
		
		for (Ontology ont : reunion) {
			IRI ontID = (IRI) ont.getIdentifier();
			logger.debug("Fetching ontology: " + ontID);
			
			Ontology helperOnt = Helper.getOntology(ontID);
			if (helperOnt != null)
				temp.addOntology(helperOnt);
			else {
				// try to fetch the ontology from its location
				Ontology locatedOnt = (Ontology) Factory.getLocatorManager().
					lookup(ontID, Ontology.class);
				if (locatedOnt != null)
					temp.addOntology(locatedOnt);
				else
					logger.error("Could not find ontology " + ontID);
			}	
		}
		return temp;		
	}
	
	/*
	 * Preety-prints the contents of the input ontology
	 */
	private String prettyPrint(Ontology ont) {
		Serializer ontologySerializer = Factory.createSerializer(new HashMap<String, Object>());
		String ontContent = "";
		try {
			StringWriter sw = new StringWriter();
			ontologySerializer.serialize(new TopEntity[] {ont}, sw);
			ontContent = sw.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ontContent;
	}
	
	public Map<Map<WebService, Interface>, Identifier> discover(Goal goal, Ontology rankingOntology) 
			throws ComponentException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}