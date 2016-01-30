/*
 * Copyright (c) 2008, University of Galway, Ireland.
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

package ie.deri.wsmx.servicediscovery.instancebased;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.scheduler.Environment;

import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.Molecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.*;
import org.wsml.reasoner.api.WSMLFlightReasoner;
import org.wsml.reasoner.api.WSMLReasonerFactory;
import org.wsml.reasoner.api.inconsistency.InconsistencyException;
import org.wsml.reasoner.impl.DefaultWSMLReasonerFactory;
import org.wsmo.common.*;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.component.ChoreographyEngine;
import org.wsmo.execution.common.component.Invoker;
import org.wsmo.execution.common.component.ChoreographyEngine.Direction;
import org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.ResponseModifierInterface;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.execution.common.nonwsmo.grounding.WSDL1_1GroundingException;
import org.wsmo.factory.*;
import org.wsmo.service.*;
import org.wsmo.service.signature.*;

/**
 * Instance based discovery engine.
 * 
 * @author Maciej Zaremba
 * @version $Revision: 1.1 $ $Date: 2007-10-24 15:19:50 $
 */
public class InstanceBasedDiscovery {

	protected static Logger logger = Logger.getLogger(InstanceBasedDiscovery.class);

	private WSMLFlightReasoner flightReasoner;
	private WsmoFactory wsmoFactory;
	private ChoreographyEngine cEngine;
	private Invoker invoker;
	private Map<String, Object> params;

	public InstanceBasedDiscovery() {
		params = new HashMap<String, Object>();
		params.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER,	WSMLReasonerFactory.BuiltInReasoner.IRIS);
		wsmoFactory = Factory.createWsmoFactory(new HashMap<String, Object>());
	}

	/**
	 * Returns the list of Web services that match the requester goal. A match
	 * is an intersection of the sets of concrete objects associated to the
	 * semantic descriptions of the requester and provider of the service.
	 */
	public List<WebService> discover(Goal goal) throws ComponentException,
			UnsupportedOperationException {
		Set<WebService> searchSpace = new HashSet<WebService>();
		Properties config = Environment.getConfiguration();
		// by default core is present
		if (config != null) {
			try {
				// obtains Web services from ResourceManager if available
				WebServiceResourceManager wsRM = Environment.getComponentProxy(
						WebServiceResourceManager.class, this);
				if (wsRM != null) {
					searchSpace.addAll(wsRM.retrieveWebServices());
					logger
							.debug("WS fetched from WSMO Resource Manager. Number of WS: "
									+ searchSpace.size());
				}
			} catch (Throwable e) {
				// RM is not available - use internal Web Services
				logger.debug("NO WSMX Environment found");
			}
		} else {
			// local mode - no WSMX core
		}

		return discover(goal, searchSpace);
	}

	/*
	 * Selects from the input search space the list of Web services that match
	 * the requester goal.
	 */
	public List<WebService> discover(Goal goal, Set<WebService> searchSpace) {
		List<WebService> matchingServices = new ArrayList<WebService>();

		// reference to sorting criteria and additional instances
		String goalCriteria = null;
		List<Entity >goalInstances = null;

		Set<Object> theNFPs = goal.getCapability().listNFPValues(wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/instancebased"));
		if (theNFPs.isEmpty() || ! theNFPs.iterator().next().toString().toLowerCase().equals("true")) {
			// return empty list
			return matchingServices;
		}
		
		theNFPs = goal.getCapability().listNFPValues(wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/instancebased/rankingcriteria"));
		if (!theNFPs.isEmpty()) {
			goalCriteria = theNFPs.iterator().next().toString();
			if (goalCriteria.equals(""))
				goalCriteria = null;
		}

		theNFPs = goal.getCapability().listNFPValues(wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/instancebased/ontology"));
		if (!theNFPs.isEmpty()) {
			Ontology goalOntology = Helper.getOntology(wsmoFactory.createIRI(theNFPs.iterator().next().toString()));
			goalInstances = Helper.getInstances(goalOntology);			
		} else return matchingServices;
		
		Axiom goalQuery = (Axiom) goal.getCapability().listPostConditions().iterator().next();
		
		List<DiscoveryResult> discResults = new ArrayList<DiscoveryResult>();  
		
		for (WebService service : searchSpace) {

			// create temporary ontology
			Ontology matchOnt = getMatchOntology((Set<Ontology>)goal.listOntologies(), 
						(Set<Ontology>)service.listOntologies());
			Set<Namespace> namespaces = goal.listNamespaces();
			for (Namespace namespace : namespaces){
				matchOnt.addNamespace(namespace);
			}

			// check if there is contracting ontology, if so invoke it
			List<Entity> contrInstances = findAndInvokeContractingInterfaces(goalInstances, service);

			for (Entity entity : contrInstances){
				if (entity instanceof Instance){
					try {
						matchOnt.addInstance( (Instance) entity);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		
			logger.debug("Temporary Ontology: \n" + Helper.serializeTopEntity(matchOnt));
			
			try {
				LogicalExpression lx = (LogicalExpression) goalQuery.listDefinitions().iterator().next();

				Helper.visualizerLog(Helper.FILTER_DISCOVERY, "Testing Goal with respect to: "+service.getIdentifier().toString());
				logger.debug("Testing goal with respect to Web service");
				flightReasoner = DefaultWSMLReasonerFactory.getFactory().createWSMLFlightReasoner(params);
				flightReasoner.registerOntology(matchOnt);
				boolean flag = flightReasoner.entails((IRI)matchOnt.getIdentifier(), lx);
				Helper.visualizerLog(Helper.FILTER_DISCOVERY, "WS entails Goal: "+flag);
				logger.debug("Entails: " +flag);
				
				DiscoveryResult discResult = new DiscoveryResult(service, flag, goalCriteria);
				
				if (flag) {
					Set<Map<Variable,Term>> resp = flightReasoner.executeQuery((IRI)matchOnt.getIdentifier(), lx);
					Float value = null;

					for (Map<Variable,Term> map : resp){
						for (Entry<Variable, Term> entry: map.entrySet()){
							String variableName = entry.getKey().toString();
							Helper.visualizerLog(Helper.FILTER_DISCOVERY, "variableName" + " - "+entry.getValue().toString());
							logger.debug(variableName+" - "+entry.getValue().toString());
							if (variableName.equals(goalCriteria))
								discResult.setVariableValues(variableName, Float.valueOf(entry.getValue().toString()));
						}
					}
				}
				discResults.add(discResult);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
		//sort results
		DiscoveryResult[] temp = discResults.toArray(new DiscoveryResult[discResults.size()]);
		Arrays.sort(temp, new DiscoveryResultComparator());
		discResults.clear();
		for (DiscoveryResult r : temp)
			discResults.add(r);
		
		logger.info("Discovery results:");
		Helper.visualizerLog(Helper.FILTER_DISCOVERY, "Discovery results:");
	
		for ( DiscoveryResult discRes : discResults){
			String msg = discRes.ws.getIdentifier() + " - " + discRes.entails;
			Helper.visualizerLog(Helper.FILTER_DISCOVERY,msg);
			logger.info(msg);
			
			if (discRes.entails && goalCriteria!= null && discRes.getVariableValue(goalCriteria) != null) {
				msg = goalCriteria + " = " + discRes.getVariableValue(goalCriteria);
				Helper.visualizerLog(Helper.FILTER_DISCOVERY,msg);
				logger.info(msg);
			}
			
			if (discRes.entails)
				matchingServices.add(discRes.ws);
		}
		return matchingServices;	
	}

	/*
	 * Selects from the input search space the list of Web services that match
	 * the requester goal.
	 */
	public List<Map<WebService, List<Entity>>> discoverWithComposition(
			Goal goal, Set<WebService> searchSpace) {
		List<Map<WebService, List<Entity>>> matchingServices = new ArrayList<Map<WebService, List<Entity>>>();

		// reference to sorting criteria and additional instances
		List<Map<String, String>> goalRankingCriteria = new ArrayList<Map<String, String>>();
		List<String> goalRankingList = new ArrayList<String>();
		List<Entity> goalInstances = null;
		List<String> mainElements = new ArrayList<String>();

		Set<Object> theNFPs = goal
				.getCapability()
				.listNFPValues(
						wsmoFactory
								.createIRI("http://www.wsmo.org/goal/discovery/instancebased/composition"));
		if (theNFPs.isEmpty()
				|| !theNFPs.iterator().next().toString().toLowerCase().equals(
						"true")) {
			// return empty list
			return matchingServices;
		}

		theNFPs = goal
				.getCapability()
				.listNFPValues(
						wsmoFactory
								.createIRI("http://www.wsmo.org/goal/discovery/instancebased/rankingcriteria"));
		Iterator iter = theNFPs.iterator();
		while (iter.hasNext()) {
			String val = iter.next().toString();
			String variableName = val.substring(0, val.indexOf("-"));
			String criterion = val
					.substring(val.indexOf("-") + 1, val.length());
			Map<String, String> temp = new HashMap<String, String>();
			temp.put(variableName, criterion);
			goalRankingCriteria.add(temp);
			goalRankingList.add(variableName);
		}

		theNFPs = goal
				.getCapability()
				.listNFPValues(
						wsmoFactory
								.createIRI("http://www.wsmo.org/goal/discovery/instancebased/ontology"));
		if (!theNFPs.isEmpty()) {
			Ontology goalOntology = Helper.getOntology(wsmoFactory
					.createIRI(theNFPs.iterator().next().toString()));
			goalInstances = Helper.getInstances(goalOntology);
		} else
			return matchingServices;

		theNFPs = goal
				.getCapability()
				.listNFPValues(
						wsmoFactory
								.createIRI("http://www.wsmo.org/goal/discovery/instancebased/mainElements"));
		iter = theNFPs.iterator();
		while (iter.hasNext())
			mainElements.add(iter.next().toString());

		Axiom goalQuery = (Axiom) goal.getCapability().listPostConditions()
				.iterator().next();

		SortedMap<Float, WebService> sortedResults = new TreeMap<Float, WebService>();

		Ontology matchOnt = wsmoFactory.createOntology(wsmoFactory
				.createIRI("http://www.wsmx.org/discovery/matchOntology-"
						+ Math.abs(Helper.getRandomLong())));
		List<TopEntity> matchOntTopEnts = new ArrayList<TopEntity>();
		matchOntTopEnts.add(matchOnt);

		for (Ontology ont : (Set<Ontology>) goal.listOntologies()) {
			matchOnt.addOntology(ont);
			matchOntTopEnts.add(ont);
		}

		for (Namespace namespace : goal.listNamespaces())
			matchOnt.addNamespace(namespace);

		for (WebService service : searchSpace) {
			for (Ontology ont : (Set<Ontology>) service.listOntologies()) {
				if (!matchOnt.listOntologies().contains(ont)) {
					matchOnt.addOntology(ont);
					matchOntTopEnts.add(ont);
				}
			}

			// check if there is contracting ontology, if so invoke it
			List<Entity> contrInstances = findAndInvokeContractingInterfaces(
					goalInstances, service);

			for (Entity entity : contrInstances) {
				if (entity instanceof Instance) {
					try {
						matchOnt.addInstance((Instance) entity);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		TopEntity[] matchOntTopEntsArray = (TopEntity[]) matchOntTopEnts
				.toArray(new TopEntity[matchOntTopEnts.size()]);
		List<Entity> matchOntInstances = Helper
				.getInstances(matchOntTopEntsArray);

		List<InstanceLevelService> instLevelServList = new ArrayList<InstanceLevelService>();
		// Helper.getInstances();
		logger.debug("Temporary Ontology: \n"
				+ Helper.serializeTopEntity(matchOnt));
		try {
			LogicalExpression lx = (LogicalExpression) goalQuery
					.listDefinitions().iterator().next();

			// Helper.visualizerLog(Helper.FILTER_DISCOVERY, "Testing Goal with
			// respect to: "+service.getIdentifier().toString());
			logger.debug("Testing goal with respect to Web service");
			flightReasoner = DefaultWSMLReasonerFactory.getFactory()
					.createWSMLFlightReasoner(params);
			flightReasoner.registerOntology(matchOnt);
			boolean flag = flightReasoner.entails((IRI) matchOnt
					.getIdentifier(), lx);
			Helper.visualizerLog(Helper.FILTER_DISCOVERY, "WS entails Goal: "
					+ flag);
			logger.debug("Entails: " + flag);

			Set<Map<Variable, Term>> resp = flightReasoner.executeQuery(
					(IRI) matchOnt.getIdentifier(), lx);

			for (Map<Variable, Term> map : resp) {
				InstanceLevelService instLevelServ = new InstanceLevelService();

				for (Entry<Variable, Term> entry : map.entrySet()) {
					String variableName = entry.getKey().toString();
					Helper.visualizerLog(Helper.FILTER_DISCOVERY,
							"variableName" + " - "
									+ entry.getValue().toString());
					logger.debug(variableName + " - "
							+ entry.getValue().toString());
					if (goalRankingList.contains(variableName)) {
						Float value = Float
								.valueOf(entry.getValue().toString());
						instLevelServ.putRankingVariableValue(variableName,
								value);
					} else if (mainElements.contains(variableName)) {
						String iriStr = entry.getValue().toString();
						// get service reference
						for (Entity e : matchOntInstances) {
							logger.debug("---" + e.getIdentifier() + " - "
									+ iriStr);
							Helper.visualizerLog(Helper.FILTER_DISCOVERY, "---"
									+ e.getIdentifier() + " - " + iriStr);
							if (e.getIdentifier().toString().equals(iriStr)) {
								Instance inst = (Instance) e;

								Namespace ns = Helper.getOntology(
										inst.getOntology().getIdentifier()
												.toString())
										.getDefaultNamespace();

								Set<WebService> services = Helper
										.getWebServices(ns);
								instLevelServ.putMainElementWebService(
										variableName, inst, services.iterator()
												.next());
							}
						}
					}

				}
				instLevelServList.add(instLevelServ);
				Helper.visualizerLog(Helper.FILTER_DISCOVERY, "--------");
				logger.debug("--------");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// do ranking
		iter = goalRankingCriteria.iterator();

		List<List<InstanceLevelService>> listOfLists = new ArrayList<List<InstanceLevelService>>();
		listOfLists
				.add((List<InstanceLevelService>) new ArrayList<InstanceLevelService>(
						instLevelServList));

		while (iter.hasNext()) {
			Map<String, String> map = (Map<String, String>) iter.next();
			Entry<String, String> e = map.entrySet().iterator().next();
			String variableName = e.getKey();
			List<List<InstanceLevelService>> listOfListsTemp = new ArrayList<List<InstanceLevelService>>();
			for (List<InstanceLevelService> list : listOfLists) {
				if (list.size() > 1) {
					List<InstanceLevelService> sortedSubset = sortServices(
							list, variableName, e.getValue());
					Float f = sortedSubset.get(0).getRankingVariableValue(
							variableName);
					int indexBeg = 0, indexEnd = 0;
					for (InstanceLevelService inst : sortedSubset) {
						if (!inst.getRankingVariableValue(variableName).equals(
								f)) {
							List newList = sortedSubset.subList(indexBeg,
									indexEnd);
							listOfListsTemp.add(newList);
							indexBeg = indexEnd;
							f = sortedSubset.get(indexBeg)
									.getRankingVariableValue(variableName);
						}
						indexEnd++;
					}
					if (indexBeg < sortedSubset.size()) {
						List newList = sortedSubset.subList(indexBeg, indexEnd);
						listOfListsTemp.add(newList);
					}
				} else
					listOfListsTemp.add(list);
			}
			listOfLists = listOfListsTemp;
		}

		instLevelServList.removeAll(instLevelServList);
		for (List<InstanceLevelService> list : listOfLists) {
			instLevelServList.addAll(list);
		}

		// now print out results
		for (InstanceLevelService instServ : instLevelServList) {
			String comment = "\n";
			Map<WebService, List<Entity>> mapOutput = new HashMap<WebService, List<Entity>>();
			for (String mainElem : mainElements) {
				Map<Instance, WebService> instWS = instServ
						.getMainElementWebService(mainElem);
				Entry entry = instWS.entrySet().iterator().next();
				Instance inst = (Instance) entry.getKey();

				List<Entity> entities = new ArrayList<Entity>();
				entities.add(inst);
				WebService ws = (WebService) entry.getValue();
				comment += mainElem + " = " + inst.getIdentifier().toString()
						+ " - WS: " + ws.getIdentifier().toString() + "\n";
				comment += Helper.printInstance(inst) + "\n";
				if (mapOutput.containsKey(ws)) {
					List<Entity> temp = mapOutput.get(ws);
					temp.addAll(entities);
					mapOutput.put(ws, temp);
				} else
					mapOutput.put(ws, entities);
			}
			matchingServices.add(mapOutput);

			iter = goalRankingCriteria.iterator();
			while (iter.hasNext()) {
				Map<String, String> map = (Map<String, String>) iter.next();
				Entry<String, String> e = map.entrySet().iterator().next();
				String variableName = e.getKey();
				Float f = instServ.getRankingVariableValue(variableName);
				comment += variableName + " = " + f + "\n";
			}

			Helper.visualizerLog(Helper.FILTER_DISCOVERY, comment);
			logger.debug(comment);

			logger.debug("--------");
		}
		return matchingServices;
	}

	// direction: HigherBetter or LowerBetter
	List<InstanceLevelService> sortServices(
			List<InstanceLevelService> services, String variableName,
			String direction) {

		List<InstanceLevelService> result = new ArrayList<InstanceLevelService>();
		SortedSet<Float> sortedSet = new TreeSet<Float>();

		for (InstanceLevelService inst : services) {
			sortedSet.add(inst.getRankingVariableValue(variableName));
		}

		// now, we have sorted list, but some of the values might have been lost
		Float[] sortedArray = sortedSet.toArray(new Float[sortedSet.size()]);

		if (direction.equals("HigherBetter")) {
			// flip the table
			int endIndex = sortedSet.size() - 1;
			int beginIndex = 0;
			while (beginIndex < endIndex) {
				Float temp = sortedArray[endIndex];
				sortedArray[endIndex] = sortedArray[beginIndex];
				sortedArray[beginIndex] = temp;
				beginIndex++;
				endIndex--;
			}
		}

		for (Float f : sortedArray) {
			for (InstanceLevelService inst : services) {
				if (f.equals(inst.getRankingVariableValue(variableName))) {
					result.add(inst);
				}
			}
		}

		// for ( Float value : sortedResults.entrySet()){
		// logger.info(variableName + " = " + entry.getKey());
		// result.add(entry.getValue());
		// }
		// } else if (direction.equals("LowerBetter")){
		// for ( Entry <Float,InstanceLevelService> entry :
		// sortedResults.entrySet()){
		// logger.info(variableName + " = " + entry.getKey());
		// result.add(entry.getValue());
		// }
		// }

		return result;
	}

	private class InstanceLevelService {
		// map which holds pairs of instance main elements and WebService pairs
		Map<String, Map<Instance, WebService>> mainElementsWebService = new HashMap<String, Map<Instance, WebService>>();
		Map<String, Float> rankingVariablesValues = new HashMap<String, Float>();

		public Map<Instance, WebService> getMainElementWebService(
				String variableName) {
			return mainElementsWebService.get(variableName);
		}

		public void putMainElementWebService(String variableName,
				Instance inst, WebService webService) {
			Map<Instance, WebService> temp = new HashMap<Instance, WebService>();
			temp.put(inst, webService);
			mainElementsWebService.put(variableName, temp);
		}

		public Float getRankingVariableValue(String variableName) {
			return rankingVariablesValues.get(variableName);
		}

		public void putRankingVariableValue(String variableName, Float value) {
			rankingVariablesValues.put(variableName, value);
		}
	}

	public List<Entity> findAndInvokeContractingInterfaces(
			List<Entity> goalInstances, WebService service) {
		List<Entity> contrInsts = new ArrayList<Entity>();
		for (Interface interf : (Set<Interface>) service.listInterfaces()) {
			String wsInterface = interf.getIdentifier().toString();
			if (wsInterface.contains("http://www.wsmo.org/webservice/contracting#")) {
				ensureComponentExists();
				contrInsts.addAll(runChoreography(null, goalInstances, service, interf));
			}
		}
		return contrInsts;
	}

	private void ensureComponentExists() {
		if (invoker != null && cEngine != null)
			return;

		if (Environment.isCore()) {
			cEngine = new ChoreographyEngineMBeanProxy();
			invoker = new InvokerMBeanProxy();
		} else {
//			 injectComponents(..) method should be called beforehand
		}
	}

	public boolean injectComponents(ChoreographyEngine cEngine, Invoker invoker) {
		if (!Environment.isCore()) {
			this.cEngine = cEngine;
			this.invoker = invoker;
			return true;
		} else
			return false;
	}

	private boolean entails(Ontology baseOntology, Ontology lightweightDescr,
			Set<LogicalExpression> queries) {

		boolean match = false;

		// logger.debug("Lightweight description is: \n" +
		// this.prettyPrint(lightweightDescr));
		try {
			Set<Axiom> axioms = (Set<Axiom>) lightweightDescr.listAxioms();
			for (Axiom a : axioms)
				baseOntology.addAxiom(a);

			flightReasoner = DefaultWSMLReasonerFactory.getFactory()
					.createWSMLFlightReasoner(params);
			flightReasoner.registerOntology(baseOntology);

			for (LogicalExpression query : queries) {
				logger.debug("Query is: \n" + query);
				if (!flightReasoner.executeQuery(
						(IRI) baseOntology.getIdentifier(), query).isEmpty()) {
					match = true;
					logger.debug("Match!");
					break;
				}
			}
			flightReasoner.deRegisterOntology((IRI) baseOntology
					.getIdentifier());
			for (Axiom a : axioms) {
				baseOntology.removeAxiom(a);
				// FIXME - possible bug
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
	 * Creates a temporary ontology importing the ontologies from both sets
	 */
	private Ontology getMatchOntology(Set<Ontology> set1, Set<Ontology> set2) {

		Ontology matchOnt = wsmoFactory.createOntology(wsmoFactory
				.createIRI("http://www.wsmx.org/discovery/matchOntology-"
						+ Math.abs(Helper.getRandomLong())));

		for (Ontology ont1 : set1)
			matchOnt.addOntology(ont1);
		for (Ontology ont2 : set2)
			matchOnt.addOntology(ont2);
		return matchOnt;
	}

	public Map<Map<WebService, Interface>, Identifier> discover(Goal goal,
			Ontology rankingOntology) throws ComponentException,
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	class ChoreographyEngineMBeanProxy implements ChoreographyEngine {
		boolean envFlag = Environment.isCore();

		public boolean isProviderChorInEndState() throws ComponentException,
				UnsupportedOperationException {
			if (!envFlag)
				return false;
			Object response = invokeComponent("RadexChoreography",
					"isProviderChorInEndState", new Object[] {},
					new String[] {});
			return ((Boolean) response).booleanValue();
		}

		public void registerChoreography(Goal goal, Interface inter)
				throws ComponentException, UnsupportedOperationException {
			if (!envFlag)
				return;
			Object response = invokeComponent("RadexChoreography",
					"registerChoreography", new Object[] { goal, inter },
					new String[] { "org.wsmo.service.Goal",
							"org.wsmo.service.Interface" });
		}

		public void registerChoreography(WebService webService, Interface inter)
				throws ComponentException, UnsupportedOperationException {
			if (!envFlag)
				return;
			Object response = invokeComponent("RadexChoreography",
					"registerChoreography", new Object[] { webService, inter },
					new String[] { "org.wsmo.service.WebService",
							"org.wsmo.service.Interface" });
		}

		public void updateState(URI origin, Entity message)
				throws ComponentException, UnsupportedOperationException {
			if (!envFlag)
				return;
			Object response = invokeComponent("RadexChoreography",
					"updateState", new Object[] { origin, message },
					new String[] { "java.net.URI", "org.wsmo.common.Entity" });
		}

		public Map<Instance, ResponseModifierInterface> updateState(
				Direction direction, Set<Instance> data)
				throws ComponentException, UnsupportedOperationException {
			if (!envFlag)
				return new HashMap<Instance, ResponseModifierInterface>();

			logger.debug("-------" + direction.getClass().toString()
					+ "-----------");
			Object response = invokeComponent(
					"RadexChoreography",
					"updateState",
					new Object[] { direction, data },
					new String[] {
							"org.wsmo.execution.common.component.ChoreographyEngine$Direction",
							"java.util.Set" });
			return (Map<Instance, ResponseModifierInterface>) response;
		}
	}

	class InvokerMBeanProxy implements Invoker {
		boolean envFlag = Environment.isCore();

		public List<Entity> syncInvoke(WebService service, List<Entity> data,
				EndpointGrounding grounding, Ontology ontology)
				throws ComponentException, UnsupportedOperationException {
			if (!envFlag)
				return new ArrayList<Entity>();

			Object response = invokeComponent(
					"Invoker",
					"syncInvoke",
					new Object[] { service, data, grounding, ontology },
					new String[] {
							"org.wsmo.service.WebService",
							"java.util.List",
							"org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding",
							"org.omwg.ontology.Ontology" });
			return (List<Entity>) response;
		}

		public List<Entity> invoke(WebService service, List<Entity> data,
				String grounding) throws ComponentException,
				UnsupportedOperationException, WSDL1_1GroundingException {
			if (!envFlag)
				return new ArrayList<Entity>();

			Object response = invokeComponent(
					"Invoker",
					"invoke",
					new Object[] { service,	data, grounding },
					new String[] {
							"org.wsmo.service.WebService",
							"java.util.List",
							"java.lang.String"	});
			return (List<Entity>) response;
		}
	}

	private Object invokeComponent(String componentName, String operationName,
			Object[] params, String[] paramsDataTypes)
			throws ComponentException, UnsupportedOperationException {

		logger.debug("-----" + operationName);

		// get a reference to MBeanService
		MBeanServer mBeanServer = Environment.getMBeanServer();

		ObjectName componentObjectName;
		try {
			componentObjectName = new ObjectName("components:name="
					+ componentName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ComponentException(e);
		}

		// check if mBean has been registered
		boolean flag = mBeanServer.isRegistered(componentObjectName);
		if (!flag) {
			throw new ComponentException();
		}

		Object response;
		try {
			response = mBeanServer.invoke(componentObjectName, operationName,
					params, paramsDataTypes);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
		return response;
	}

	public List<Entity> runChoreography(Goal goalR,
			List<Entity> goalInstancesR, WebService webServiceR,
			Interface interR) {

		try {
			// now read Web service
			List<Entity> cacheInstancesForProvider = goalInstancesR;// new
																	// HashSet<Entity>(Helper.getInstances(goalOnto));
			List<Entity> allInstancesReceivedFromProvider = new ArrayList<Entity>();

			try {
				cEngine.registerChoreography(goalR, null);
			} catch (Exception e) {
				// ignore
			}
			cEngine.registerChoreography(webServiceR, interR);

			printOutInstances("\n ----------- Updating CE ---REQ_TO_PROV--- "+ cacheInstancesForProvider.size()+ " instances -----------",
					cacheInstancesForProvider);
			Map<Instance, ResponseModifierInterface> chorResp = cEngine.updateState(Direction.REQUESTER_TO_PROVIDER, new HashSet(cacheInstancesForProvider));
			logger.info("CE returned:" + Helper.printSetShort(chorResp.keySet()));

			// list of messages returned from the WSDL endpoints after the WSML lifting
			List<Entity> msgForRequester = new ArrayList<Entity>();
			List<Entity> msgForRequesterToBeCleaned = new ArrayList<Entity>();
			Set<Instance> createdByExecution = new HashSet<Instance>();
			int instancesSent;
			while (true) {
				instancesSent = 0;
				for (Instance inst : chorResp.keySet()) {
					if (cacheInstancesForProvider.contains(inst))
						cacheInstancesForProvider.remove(inst);

					List<Entity> localInsts = new ArrayList<Entity>();
					localInsts.add(inst);

					ResponseModifierInterface responseModifier = chorResp
							.get(inst);
					WSDLGrounding grounding = (WSDLGrounding) responseModifier
							.getGrounding();

					if (grounding != null) {
						instancesSent++;
						// FIXME reliance on to string
						String groundingIRI = grounding.getIRI().toString();

						String logMsg = "----------- Instance Discovery-->>Invoker: Sending WSML to service -----------\n" +
										"Service grounding: >>> " + groundingIRI + " <<< \n" +
										Helper.printSetShort(new HashSet(localInsts));
						logger.info(logMsg);
						Helper.visualizerLog(Helper.FILTER_OUTGOING,logMsg);
						
						List<Entity> resp = invoker.invoke(webServiceR,	localInsts, groundingIRI);
						
						logMsg =		"----------- Instance Discovery<<--Invoker: Received WSML from service -----------\n"+ 
										Helper.printSetShort(new HashSet(resp));
						logger.info(logMsg);
						Helper.visualizerLog(Helper.FILTER_INCOMING,logMsg);
						
						Set<Molecule> molecules = responseModifier.getMolecules();

						List<Instance> instances = null;
						List<AttributeValueMolecule> avms = new ArrayList<AttributeValueMolecule>();

						logger.debug("Before Molecule:\n " + Helper.printSetShort(new HashSet<Entity>(allInstancesReceivedFromProvider)));

						for (Molecule molecule : molecules) {
							if (molecule instanceof AttributeValueMolecule) {
								avms.add((AttributeValueMolecule) molecule);
							} else {
								Term concept = molecule.getRightParameter();
								instances = getInstancesForConcept(resp,
										concept);
							}
						}

						if (instances != null) {
							for (Instance instance : instances) {
								for (AttributeValueMolecule molecule : avms) {
									instance.addAttributeValue(
											(Identifier) molecule
													.getAttribute(),
											(Value) molecule
													.getRightParameter());
									logger.info("Response instance "
											+ instance.getIdentifier()
											+ "\n            added "
											+ molecule.getRightParameter());
								}
							}
						}

						allInstancesReceivedFromProvider.addAll(resp);
						msgForRequester.addAll(resp);
						msgForRequesterToBeCleaned.addAll(resp);

					} else {
						createdByExecution.add(inst);
					}

				}

				// now, feed back obtained instances to Chor. Engine
				printOutInstances(
						"\n----------- Updating CE ---PROV_TO_REQ--- "
								+ allInstancesReceivedFromProvider.size()
								+ " instances -----------",
						new ArrayList(allInstancesReceivedFromProvider));
				logger
						.debug("Before PROVIDER_TO_REQUESTER update:\n "
								+ Helper.printSetFull(new HashSet<Entity>(
										msgForRequester)));

				chorResp = cEngine.updateState(Direction.PROVIDER_TO_REQUESTER,
						new HashSet(msgForRequester));

				// check if in the endEtate
				if (cEngine.isProviderChorInEndState()) {
					logger.debug("provider chor in end state"); // [mh]
					break;
				}

				printOutInstances(
						"\n----------- Updating CE ---REQ_TO_PROV--- "
								+ cacheInstancesForProvider.size()
								+ " instances -----------",
						cacheInstancesForProvider);
				chorResp = cEngine.updateState(Direction.REQUESTER_TO_PROVIDER,
						new HashSet(cacheInstancesForProvider));

				if (instancesSent == 0)
					break;
				else {
					msgForRequester.clear();
					createdByExecution.clear();
				}

			}
			// delete goal and goal ontology
			// clear goal ontology

			// for (Entity ent : goalInstancesR ){
			// if (ent instanceof Instance) {
			// Instance i = (Instance) ent;
			// Map attr = i.listAttributeValues();
			// Set<IRI> keys = attr.keySet();
			//				
			// for (IRI iri : keys){
			// Object values = attr.get(iri);
			// i.removeAttributeValues(iri);
			// }
			// }
			// }

			String respOntoNS = "http://wsmx.org//responseOntology"
					+ Helper.getRandomLong();
			Ontology respOnto = wsmoFactory.createOntology(wsmoFactory
					.createIRI(respOntoNS));
			// create response ontology and clone the relevant instances to it

			// First check non functional properties of the service
			Set theNFPs = webServiceR.listNFPValues(wsmoFactory
					.createIRI("http://response"));

			if (!theNFPs.isEmpty()) {
				IRI lastConcept = (IRI) theNFPs.iterator().next();

				logger.debug("Response concept of the Web service: "
						+ lastConcept + "\n");
				for (Instance i : createdByExecution) {
					for (Concept concept : i.listConcepts()) {
						if (concept.getIdentifier().toString().equals(
								lastConcept.toString())) {
							// the response ontology will contain this instance
							Helper.clone(i, respOntoNS, respOnto,
									new HashMap<IRI, Instance>(),
									new HashMap<Instance, Instance>());
						} else {
							logger.info("Instance " + i.getIdentifier()
									+ " not added to the response ontology.");
						}
					}
				}
			} else {
				// Otherwise return last instance
				if (msgForRequesterToBeCleaned.size() > 0) {
					Instance lastInstance = (Instance) msgForRequesterToBeCleaned
							.get(msgForRequesterToBeCleaned.size() - 1);
					Helper.clone(lastInstance, respOntoNS, respOnto,
							new HashMap<IRI, Instance>(),
							new HashMap<Instance, Instance>());
				}
			}
			// clean up all the original messages from memory (so it doesn't
			// break subsequent runs)
			Helper.cleanUpInstances(msgForRequesterToBeCleaned);
			return Helper.getInstances(respOnto);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<Entity>();
	}

 	protected Instance clone(Instance theInstance, String theNamespace,
			Ontology theOntology, Map<IRI, Instance> iri2Cloned,
			Map<Instance, Instance> original2Cloned)
			throws SynchronisationException, InvalidModelException {

		// create new IRI
		IRI cloneIRI = wsmoFactory.createIRI(theNamespace + "#"
				+ ((IRI) theInstance.getIdentifier()).getLocalName() + "_"
				+ theInstance.getIdentifier().toString().hashCode());

		// logger.debug("Fixing instance with IRI \"" +
		// theInstance.getIdentifier().toString() + "\" to clone with IRI \"" +
		// cloneIRI.toString()+ "\"");

		if (iri2Cloned.containsKey(cloneIRI)) {
			return iri2Cloned.get(cloneIRI);
		}

		// create clone instance
		Instance clone = wsmoFactory.createInstance(cloneIRI);
		iri2Cloned.put(cloneIRI, clone);
		original2Cloned.put(theInstance, clone);

		// add concepts
		for (Concept concept : theInstance.listConcepts()) {
			Concept nonProxyConcept = wsmoFactory.getConcept(concept
					.getIdentifier());
			clone.addConcept(nonProxyConcept);
		}

		// add attributes
		for (Identifier attributeID : theInstance.listAttributeValues()
				.keySet()) {
			for (Value value : theInstance.listAttributeValues(attributeID)) {
				if (value instanceof Instance) {
					clone.addAttributeValue(attributeID, clone(
							(Instance) value, theNamespace, theOntology,
							iri2Cloned, original2Cloned));
				} else {
					clone.addAttributeValue(attributeID, value);
				}
			}
		}
		// set ontology
		clone.setOntology(theOntology);
		theOntology.addInstance(clone);

		return clone;
	}

	private void printOutInstances(String msg, List<Entity> instances) {
		logger.debug(msg);
		for (Entity e : instances) {
			Instance i = (Instance) e;

			logger.debug(((Instance) e).getIdentifier().toString()
					+ " memberOf " + Helper.printSetShort(i.listConcepts()));
		}
	}

	/**
	 * Find the instances corresponding to the specified concept in the
	 * response.
	 * 
	 * @param resp
	 *            A list of entities.
	 * @param concept
	 *            The concept to look for.
	 * @return The list of instances.
	 */
	private List<Instance> getInstancesForConcept(List<Entity> resp,
			Term concept) {
		List<Instance> instances = new ArrayList<Instance>();
		for (Entity entity : resp) {
			if (entity instanceof Instance) {
				Instance instance = (Instance) entity;
				logger.info("INSTANCE " + instance.getIdentifier());
				Set<Concept> concepts = instance.listConcepts();
				for (Concept c : concepts) {
					if (c.getIdentifier().toString().equals(concept.toString())) {
						instances.add(instance);
					}
				}
			}
		}
		return instances;
	}
}