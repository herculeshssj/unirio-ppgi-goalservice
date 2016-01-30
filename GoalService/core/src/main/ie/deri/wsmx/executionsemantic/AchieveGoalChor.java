/*
 * Copyright (c) 2008 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */

package ie.deri.wsmx.executionsemantic;

import ie.deri.wsmx.commons.EventsGenerator;
import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.scheduler.Environment;
import ie.deri.wsmx.scheduler.Proxy;
import ie.deri.wsmx.scheduler.RoutingException;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.Molecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.execution.common.component.ChoreographyEngine;
import org.wsmo.execution.common.component.DataMediator;
import org.wsmo.execution.common.component.Discovery;
import org.wsmo.execution.common.component.Invoker;
import org.wsmo.execution.common.component.NonFunctionalSelector;
import org.wsmo.execution.common.component.Parser;
import org.wsmo.execution.common.component.ServiceDiscovery;
import org.wsmo.execution.common.component.ChoreographyEngine.Direction;
import org.wsmo.execution.common.component.resourcemanager.NonWSMOResourceManager;
import org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.exception.SystemException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.DiscoveryType;
import org.wsmo.execution.common.nonwsmo.MessageId;
import org.wsmo.execution.common.nonwsmo.ResponseModifierInterface;
import org.wsmo.execution.common.nonwsmo.grounding.WSDL1_1GroundingException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.mediator.OOMediator;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;
import org.wsmo.service.ServiceDescription;
import org.wsmo.service.WebService;
import org.wsmo.service.signature.WSDLGrounding;

/**
 * Execution Semantics with support for choreography, discovery and data mediation.
 *
 * <pre>
 * Created on Nov 5, 2005
 * Committed by $Author: maciejzaremba $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.16 $ $Date: 2007-10-11 14:33:41 $
 */ 
public class AchieveGoalChor extends WSMXExecutionSemantic {

	private static final long serialVersionUID = 5414183431355320580L;
	static Logger logger = Logger.getLogger(AchieveGoalChor.class);
	
	//global variables for whole execution semantics
	public MessageId messageId = null;
	public String wsmlMessage = null;
	long dt = System.currentTimeMillis();

	private WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
	
	//after parsing
	public Goal goal = null;
	public Ontology goalOnto = null;
	public String goalOntoIRIString = "";
	public List<Ontology> allOntos = null;
	public int requiredDiscovery = -1;
	//after discovery
	public WebService discoveredWebService = null;
	
	public List<WebService> discoveredWSSimple = new ArrayList<WebService>();
	public List<Map<WebService, List<Entity>>> discoveredWSComplex = new ArrayList<Map<WebService, List<Entity>>>();
	
	public List<WebService> candidateWebServices = null;
	
	/** data to be sent to Web service after data mediation */
	public List<Entity> mediatedEntities = null;
	/** relevant output of the choreography, for input to final data mediation */
	public List<Entity> choreographyOutput = null; 
	
	public List<Entity> msgsReceivedFromWS = null;		
	
	public AbstractExecutionSemantic executionSemantic;
	
	private boolean internalIRI = false;
	
	public enum DataMediationDirection { UNDEFINED, GOAL_TO_WEBSERVICE, WEBSERVICE_TO_GOAL }
	
	public DataMediationDirection dataMediationDirection = DataMediationDirection.UNDEFINED;

	public Invoker wsmxInvoker = null;
	
	//clean up after communication
	public void cleanUp(){
		logger.debug("---Cleaning active execution semantic---");		
		if (goalOnto == null)
			return; 

		try {
			//clean up Goal + Goal ontology
			if (allOntos != null) {
				for (Ontology ont : allOntos){
					if (ont != null && internalIRI == false) {
						Helper.cleanUpInstances( Helper.getInstances(ont) );
					}
				} 
			}
		
			//clean mediated instances
			//FIXME - current assumption - if there is not mediation involved, mediated instance = goal intances 
			if (mediatedEntities != null && internalIRI == false) 
				Helper.cleanUpInstances(mediatedEntities);

			//clean instances obtained from the web service
			Helper.cleanUpInstances(msgsReceivedFromWS);	
			
			//FIXME check whether more cleaning is required (-> choreographyOutput)
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (internalIRI == false) { 
			if (goal != null)
				goal.removeOntology(goalOnto);
			goalOnto.removeOntology(goalOnto);
		}

		goal = null;
		goalOnto = null;
		dataMediationDirection = DataMediationDirection.UNDEFINED;
	};
	
	public AchieveGoalChor() {
		super();
	}
	
    public AchieveGoalChor(Context contextId, MessageId messageId, String wsmlMessage) {
        super();
		List<String> msgs = new ArrayList<String>();
		msgs.add(wsmlMessage);
        initialize(contextId, messageId, msgs);
    }

    public void initialize(Context contextId, MessageId messageId, List<String> wsmlMessages) {
//    	this.contextId = contextId;
    	this.messageId = messageId;
    	this.wsmlMessage = wsmlMessages.get(0);
    	executionSemantic = this;
    	
    	mediatedEntities = new ArrayList<Entity>();
//    	msgsReceivedFromWS = new ArrayList<Entity>();
    	
        state = new ReceivedMessageProcessing(contextId);
        tagStateForTerminationOnFailure();
    }
    
    public AchieveGoalChor(State state) {
        super();
        this.state = state;
    }

    @SuppressWarnings("unchecked")
	public <E> Proxy<E> getProxy(Class<E> clazz) {
        return (Proxy<E>) new PerformWebServiceResourceRetrieval(state.getContext());
    }
    
	class ReceivedMessageProcessing extends State {    
		private static final long serialVersionUID = -6915488944488044350L;
	
	    public ReceivedMessageProcessing(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }
	
	    @Override
	    public State handleState(Object component)	    	
	            throws UnsupportedOperationException, ComponentException {
	        return new MessageBackup(contextId);
	    }
	
		@Override
		public Event getAssociatedEvent() {
			return Event.ENTRYPOINT;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}	
		
	class MessageBackup extends State {    
		private static final long serialVersionUID = -4397638933116562437L;
		
	    public MessageBackup(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException {
	    	logger.debug("Message backup.");
	        ((NonWSMOResourceManager)component).saveMessage(contextId, messageId, wsmlMessage);
	        return new WSMLMessageParsing(contextId);
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.RESOURCEMANAGER;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}

	class WSMLMessageParsing extends State {    
		private static final long serialVersionUID = -5023865882111569879L;

	    public WSMLMessageParsing(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException, DataFlowException {
	    	logger.debug("Parsing.");
	    	
	    	try {
		    	Parser parser = ((Parser)component);
				//check if IRI or WSML contents 
				if (wsmlMessage.length() < 300) {
					try { 
						IRI goalIRI = wsmoFactory.createIRI(wsmlMessage);
						
						if (goalIRI != null) {
							internalIRI = true;
							goal = Helper.getGoal(goalIRI);
					    	if (goal == null)
					    		throw new DataFlowException("Execution semantics must be given a valid goal.");
							requiredDiscovery = DiscoveryType.getRequiredDiscoveryType(goal);
							//try to get reference to the attached ontology
							//attached ontology - NFP _"http://www.wsmo.org/goal/attachedOnto"
							Set theNFPs = goal.getCapability().listNFPValues(wsmoFactory.createIRI("http://www.wsmo.org/goal/attachedOnto"));
							if (!theNFPs.isEmpty()) {
								String ontoIRIStr = theNFPs.iterator().next().toString();
								try {
									IRI ontoIRI = wsmoFactory.createIRI(ontoIRIStr);
									if (ontoIRI != null) {
										goalOnto = Helper.getOntology(ontoIRI);
										goalOntoIRIString = goalOnto.toString();
									}
								} catch (Exception e){
									//ignore
								}
							} 
							
					        Helper.visualizerLog(Helper.FILTER_INCOMING,"----------- CM received Goal from the Requester -----------\n"+Helper.serializeTopEntity(goalOnto));
							Instance inst = EventsGenerator.createStartAchieveGoalEvent(goal.getIdentifier().toString(), goalOntoIRIString);
							Helper.generateMonitoringEvent(inst);
							return new GetWebServiceFromRM(contextId);
						}
					} catch (Exception e) {
						//ignore
					}
				}
		    	
		    	TopEntity[] topEntities = Helper.parse(new StringReader(wsmlMessage));
		    	
		    	Set topEnt = new HashSet<TopEntity>();
		    	for (TopEntity t: topEntities){
			    	topEnt.add(t);	    		
		    	}

		    	allOntos = new ArrayList<Ontology>();
				//get Goal, get Ontology
	    		logger.debug("Parsed entities");
				for (Entity ent : topEntities){
					if (ent instanceof Goal)
						goal = (Goal) ent;
					if (ent instanceof Ontology) {
						allOntos.add((Ontology)ent);
						goalOnto = (Ontology) ent;
						goalOntoIRIString = goalOnto.toString();						
					}
				}
				
		    	if (goal == null)
		    		throw new DataFlowException("Execution semantics must be given a valid goal.");
		    	
		        Helper.visualizerLog(Helper.FILTER_INCOMING,"----------- CM received Goal from the Requester -----------\n"+Helper.serializeTopEntity(goalOnto));
				Instance inst = EventsGenerator.createStartAchieveGoalEvent(goal.getIdentifier().toString(), goalOntoIRIString);
				Helper.generateMonitoringEvent(inst);

				logger.info("Parsing completed.");
	    	} catch (ClassCastException cce) {
	    		throw new RoutingException(Parser.class, component.getClass());
	    	} catch (Throwable t) {
    			throw new ComponentException("Unexpected failure inside parser component.", t);
	    	}
	    	
	        return new GetWebServiceFromRM(contextId);
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.PARSER;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}
	
	public class GetWebServiceFromRM extends State {    

		private static final long serialVersionUID = 243819999209296262L;

		public GetWebServiceFromRM(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException, DataFlowException {
	    	logger.debug("Getting Web services from RM.");
	    	
	    	try {
	    		requiredDiscovery = DiscoveryType.getRequiredDiscoveryType(goal);
	    		WebServiceResourceManager rm = ((WebServiceResourceManager)component);
		    	candidateWebServices = new ArrayList<WebService>(rm.retrieveWebServices(requiredDiscovery));
	    	} catch (ClassCastException cce) {
			   	throw new RoutingException(Discovery.class, component.getClass());
			}
	    
	    	if (requiredDiscovery == DiscoveryType.SERVICE_INSTANCEBASED_DISCOVERY || 
	    		requiredDiscovery == DiscoveryType.SERVICE_INSTANCEBASED_COMPOSITION_DISCOVERY)
	    		//skip Web service discovery, perform service discovery
	    		return new PerformServiceDiscovery(contextId);
	    	else
	    		return new PerformWebServiceDiscovery(contextId);
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.RESOURCEMANAGER;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}
	

	public class PerformWebServiceDiscovery extends State {
		private static final long serialVersionUID = -8126105336257532540L;
		
	    public PerformWebServiceDiscovery(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException, DataFlowException {
	    	logger.info("Discovering suitable webservices for: " + goal.getIdentifier());
	    	Discovery discovery;
	    	try {
	    		discovery = ((Discovery)component);
	    	} catch (ClassCastException cce) {
			   	throw new RoutingException(Discovery.class, component.getClass());
			}
	    	
	    	logger.debug("Before Web service discovery.");
	    	discoveredWSSimple = discovery.discover(goal, new HashSet<WebService>(candidateWebServices));
	    	logger.debug("After Web service discovery.");

			if (discoveredWSSimple.size() == 0){
				String finalMsg = "Discovery did not result in any candidate services.";
				logger.info(finalMsg);
				Helper.visualizerLog(Helper.FILTER_OUTGOING,"----------- CM response to the Requester -----------\n"+finalMsg);
				ExecutionSemanticsFinalResponse response = new ExecutionSemanticsFinalResponse(goal,discoveredWSSimple, 
						   null, 
						   null, 
						   true, 
						   executionSemantic,
						   finalMsg);
				return new EntryPoint(contextId,response);	
			}

			if (requiredDiscovery == DiscoveryType.SERVICE_QOS_DISCOVERY)
				return new PerformServiceDiscovery(contextId);
			
			if ( (goalOnto == null) || (Helper.getInstances(goalOnto) == null) ){
				//there is discovery only
				String finalMsg = "Discovered Web Services:\n";
				for (WebService w : discoveredWSSimple) 
					finalMsg += w.getIdentifier().toString()+" \n";
				
				Helper.visualizerLog(Helper.FILTER_OUTGOING,"----------- CM response to the Requester -----------\n"+finalMsg);
				ExecutionSemanticsFinalResponse response = new ExecutionSemanticsFinalResponse(goal,discoveredWSSimple, 
						   null, 
						   null, 
						   true, 
						   executionSemantic,
						   finalMsg);
				return new EntryPoint(contextId,response);			
			}
				
			//no service discovery 
			if (discoveredWSSimple.size() > 0)
				discoveredWebService = discoveredWSSimple.get(0);
				
			// prepare for data mediation
			dataMediationDirection = DataMediationDirection.GOAL_TO_WEBSERVICE;
			return new PerformDataMediation(contextId);
	    }
	    
		@Override
		public Event getAssociatedEvent() {
			return Event.WEBSERVICEDISCOVERY;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}

	public class PerformServiceDiscovery extends State {
		private static final long serialVersionUID = -8126105336257532540L;
			
	    public PerformServiceDiscovery(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }
		    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException, DataFlowException {
	    	logger.info("Discovering suitable webservices for: " + goal.getIdentifier());
	    	ServiceDiscovery serviceDiscovery;
	    	try {
	    		serviceDiscovery = ((ServiceDiscovery)component);
	    	} catch (ClassCastException cce) {
			   	throw new RoutingException(Discovery.class, component.getClass());
			}
	    				
			if (requiredDiscovery == DiscoveryType.SERVICE_INSTANCEBASED_COMPOSITION_DISCOVERY){
				discoveredWSComplex = serviceDiscovery.discoverServiceCompositon(goal, new HashSet(candidateWebServices));
			} else if (requiredDiscovery == DiscoveryType.SERVICE_INSTANCEBASED_DISCOVERY){
				discoveredWSSimple = serviceDiscovery.discoverService(goal, new HashSet(candidateWebServices));
			} else {
				//check if QoS
				if (requiredDiscovery == DiscoveryType.SERVICE_QOS_DISCOVERY){
					discoveredWSSimple = serviceDiscovery.discoverService(goal, new HashSet<WebService>(discoveredWSSimple));
				}
			}

			if (discoveredWSSimple.size() == 0 && requiredDiscovery != DiscoveryType.SERVICE_INSTANCEBASED_COMPOSITION_DISCOVERY){
				String finalMsg = "Discovery did not result in any candidate services."; 
				logger.info(finalMsg);
				Helper.visualizerLog(Helper.FILTER_OUTGOING,"----------- CM response to the Requester -----------\n"+finalMsg);
				ExecutionSemanticsFinalResponse response = new ExecutionSemanticsFinalResponse(goal,discoveredWSSimple, 
						   null, 
						   null, 
						   true, 
						   executionSemantic,
						   finalMsg);
				return new EntryPoint(contextId,response);	
			}
			
			if ( (goalOnto == null) || (Helper.getInstances(goalOnto) == null) ){
				//there is discovery only
				String finalMsg = "Discovered Services:\n";
				for (WebService w : discoveredWSSimple) 
					finalMsg += w.getIdentifier().toString()+" \n";
				
				Helper.visualizerLog(Helper.FILTER_OUTGOING,"----------- CM response to the Requester -----------\n"+finalMsg);	
				ExecutionSemanticsFinalResponse response = new ExecutionSemanticsFinalResponse(goal,discoveredWSSimple, 
						   null, 
						   null, 
						   true, 
						   executionSemantic,
						   finalMsg);
				return new EntryPoint(contextId,response);			
			}
			
			if (discoveredWSSimple.size()>0)
				discoveredWebService = discoveredWSSimple.get(0);
			dataMediationDirection = DataMediationDirection.GOAL_TO_WEBSERVICE;
			return new PerformDataMediation(contextId);
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.SERVICEDISCOVERY;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}

	public class PerformDataMediation extends State {

		private static final long serialVersionUID = 1768511278264887782L;
	
	    public PerformDataMediation(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }	    	    

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException, DataFlowException {
	    	logger.info("--- Performing Data Mediation; direction: " + dataMediationDirection);
	    	DataMediator dataMediator;	    	
	    	try {
	    		dataMediator = ((DataMediator)component);
	    	} catch (ClassCastException cce) {
			   	throw new RoutingException(DataMediator.class, component.getClass());
			}
	    	
	    	// initialize source, target and input depending on direction
	    	ServiceDescription dataMediationSource, dataMediationTarget;
	    	List<Entity> dataMediationInput;	    	
	    	switch(dataMediationDirection) {
		    	case GOAL_TO_WEBSERVICE: 
		    		dataMediationSource = goal;
		    		dataMediationTarget = discoveredWebService;
		    		dataMediationInput = Helper.getInstances(goalOnto);
		    		break;
		    	case WEBSERVICE_TO_GOAL:
		    		dataMediationSource = discoveredWebService;
			    	dataMediationTarget = goal;
			    	dataMediationInput = choreographyOutput;
					break;
		    	default:
		    		throw new DataFlowException("Unexpected state, no information on data mediation direction present.");
	    	}
	    	
	    	logger.debug("----------- Before mediation -----------");
	    	for (Entity i : dataMediationInput)
	    		logger.debug(i.getIdentifier());
	    	
	    	//perform Data Mediation
			mediatedEntities = 
				mediateData(dataMediator, dataMediationSource, dataMediationTarget, new HashSet<Entity>(dataMediationInput));
			// if no mediation could be performed, use the non-mediated instances
			if (mediatedEntities.isEmpty()) 
				mediatedEntities = new ArrayList<Entity>(dataMediationInput); 
			
	    	logger.info("----------- Mediated instances -----------");
	    	for (Entity e : mediatedEntities)
	    		logger.info(e.getIdentifier() +" memberOf "+((Instance)e).listConcepts().iterator().next().getIdentifier().toString());
			
			// prepare and execute next step depending on direction
			switch(dataMediationDirection) {	
			
		    	case GOAL_TO_WEBSERVICE: 
		    		logger.debug("Data mediation done, next action: choreography execution.");
		    		dataMediationDirection = DataMediationDirection.UNDEFINED;	    		    		
		    		return new ChoreographyExecution(contextId);
		    		
		    	case WEBSERVICE_TO_GOAL:
		    		logger.debug("Data mediation done, finalizing...");
		    		dataMediationDirection = DataMediationDirection.UNDEFINED;		    		
			    	return generateFinalResponse();
		    		
		    	default:
		    		throw new DataFlowException("Unexpected state, no information on data mediation direction present.");
	    	}
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.DATAMEDIATOR;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
		
		private List<Entity> mediateData(DataMediator dm, ServiceDescription source, ServiceDescription target, Set<Entity> data) throws ComponentException {

			List<Entity> result = new ArrayList<Entity>();
			
			if (data==null || data.isEmpty()) {
				logger.warn("Will not perform data mediation: No input given.");
				return result;
			}
			
			// get possible source / target ontologies for mediation
			Set<Ontology> possibleSrcOntos = Helper.getOntologiesFromStateSignature(source);
			Set<Ontology> possibleTgtOntos = Helper.getOntologiesFromStateSignature(target);	
			
			if ((possibleSrcOntos == null) || (possibleTgtOntos == null)) {
				logger.info("Will not perform data mediation: No ontologies imported in choreography's state signature of goal or web service.");
				return result;
			}
					
			// try to mediate for each possible source / target ontology pair
			Map<Entity, List<Entity>> mediatedData = new HashMap<Entity, List<Entity>>();		 		    
	    	for (Ontology srcOnto : possibleSrcOntos) {
	    		Identifier srcId = srcOnto.getIdentifier();
		    	for (Ontology tgtOnto : possibleTgtOntos) {		    		
		    		Identifier tgtId = tgtOnto.getIdentifier();	 
		    		logger.debug("----looking for OOMediator from source ontology \"" + srcId + "\" to target ontology \"" + tgtId + "\"" );
		    		if ( !srcId.equals(tgtId) && Helper.containsOOMediator((IRI)srcId, (IRI)tgtId) ) {
		    			
		    			// get OO Mediator
	    				OOMediator ooMediator = Helper.retrieveOOMediator((IRI)srcId, (IRI)tgtId);	        				
	    				
	    				// get root instances from OOMediator NFP
						Set<Entity> rootInstances = Helper.getMediationRootInstances(ooMediator, data);
						if (rootInstances.isEmpty()) // if no root instances are explicitly specified, use all
							rootInstances.addAll(data);
						logger.debug("number of ROOT INSTANCES for mediation : " + rootInstances.size());						
						
						// perform mediation 
						Map<Entity, List<Entity>> m = dm.mediate(srcOnto, tgtOnto, rootInstances);
						    		
			    		if ((!m.isEmpty()) && logger.isDebugEnabled()) {
				    		Set<Instance> input = new HashSet<Instance>();
				    		for (Entity e: rootInstances) {
								input.add((Instance)e);
							}
				    		Set<Instance> resultInstances = new HashSet <Instance> ();
				            for (List <Entity> entities : m.values()){
				                for (Entity e : entities) {
				                    if (e instanceof Instance)
				                    	resultInstances.add((Instance) e);
				                }
				            }			
				            logger.debug("\n\n\n**********************INPUT**********************");
				    		logger.debug(Helper.serialize(input));
				    		logger.debug("**********************RESULT**********************");
				    		logger.debug(Helper.serialize(resultInstances));
				    		logger.debug("**************************************************");
			    		}		    					    					    					    		
			    		if (logger.isDebugEnabled()) {
			    			int resultCount = 0;
			    			for (Entity k : m.keySet()) {
			    				// warn if any entity in current mediation step has already been mediated previously
								if (mediatedData.containsKey( k )){								
									logger.debug("Previous mediation result is overwritten for source entity: " 
											+ ((k!=null) ? k.getIdentifier() : k) );
								}
								for(Entity e : m.get(k)) {
									if (e instanceof Instance)
				                        resultCount++;
								}
							}
				    		logger.debug("----mediation from source ontology \"" + srcId + "\" to target ontology \"" 
				    				+ tgtId + "\" returned " + resultCount + " instances");
			    		}
			    		
			    		mediatedData.putAll(m);
			    		
		    		}
				}
	    	}
	    	
			for (List<Entity> l: mediatedData.values())
				result.addAll(l);
			return result;
		}
		
		protected State generateFinalResponse() {
			Ontology respOnto = Helper.createOntology(mediatedEntities);	    				    	
	    	String receivedMsgStr = Helper.serializeTopEntity(respOnto); 
	    	
	    	logger.debug("=== FINAL RESPONSE AFTER MEDIATION: \n\n" + receivedMsgStr);

			Instance inst = EventsGenerator.createEndAchieveGoalEvent(goal.getIdentifier().toString(), goalOntoIRIString);
			Helper.generateMonitoringEvent(inst);
			
			Helper.visualizerLog(Helper.FILTER_OUTGOING,"----------- CM response to the Requester -----------\n"+receivedMsgStr);
    		ExecutionSemanticsFinalResponse response = new ExecutionSemanticsFinalResponse(goal,discoveredWSSimple, 
					   new HashSet<Entity>(Helper.getInstances(respOnto)), 
					   new HashSet<Entity>(msgsReceivedFromWS), 
					   true, 
					   executionSemantic,
					   receivedMsgStr);
    		return new EntryPoint(contextId,response);
		}
		
	}
	
	public class ChoreographyExecution extends State {
		private static final long serialVersionUID = -8558131230791663782L;
    	private ChoreographyEngine cEngine;
		
	    public ChoreographyExecution(Context contextId) {
	        super();
	        this.contextId = contextId;
	        logger.debug("Constructing choreography execution...");
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException {
	    try{
	    	try {
	    		cEngine = ((ChoreographyEngine)component);
		    	logger.debug("Choreography component has been retrieved");
	    	} catch (ClassCastException cce) {
	    		logger.debug("Choreography component has not been retrieved !");
			   	throw new RoutingException(NonFunctionalSelector.class, component.getClass());
			}

	    	if (requiredDiscovery == DiscoveryType.SERVICE_INSTANCEBASED_COMPOSITION_DISCOVERY){
	    		logger.debug("choreography: instancebased composition");
				List<Entity> overallResp = new ArrayList<Entity>();
				//get highest ranked WS
				Map<WebService,  List<Entity>> map = discoveredWSComplex.get(0);
				
				logger.info("--Invoke:--");
				for (Entry<WebService,  List<Entity>> entry: map.entrySet()){
					WebService ws = entry.getKey();
					discoveredWebService = ws;
					List<Entity> instances = entry.getValue();
					for (Entity inst: instances) {
						List<Entity> goalInst = new ArrayList<Entity>();
						goalInst.add(inst);
						overallResp.addAll(runChoreography(goal, goalInst, ws, ws.listInterfaces().iterator().next()));
					}
				}
				
				choreographyOutput = overallResp;
				msgsReceivedFromWS = overallResp; // <- need to do that for generating final response later
				dataMediationDirection = DataMediationDirection.WEBSERVICE_TO_GOAL;
				
				return new PerformDataMediation(contextId);

	    	}
	    	//else
	    	logger.debug("Starting choreography run");
	    		    	
	    	msgsReceivedFromWS = runChoreography(goal, mediatedEntities, discoveredWebService, discoveredWebService.listInterfaces().iterator().next());
	    	
	    	choreographyOutput = new ArrayList<Entity>();
	    	
	    	// First check non functional properties of the service
	    	Set theNFPs = discoveredWebService.listNFPValues(wsmoFactory.createIRI("http://response"));
			
	    	if (msgsReceivedFromWS.size() > 0) {
	    		if (!theNFPs.isEmpty()) {
	    			choreographyOutput.addAll(msgsReceivedFromWS);
	    		} else {
	    			choreographyOutput.add( msgsReceivedFromWS.get( msgsReceivedFromWS.size()-1 ) );
	    		}
	    	}
	    	
			dataMediationDirection = DataMediationDirection.WEBSERVICE_TO_GOAL;
			
			return new PerformDataMediation(contextId);
		
	    } catch (Exception e){
			e.printStackTrace();
			logger.error(e);
		}
	    return new Exodus();
		}
	    
	    /**
		 * Find the instances corresponding to the specified concept in the response.
		 * @param resp A list of entities.
		 * @param concept The concept to look for.
		 * @return The list of instances.
		 */
		private List<Instance> getInstancesForConcept(List<Entity> resp, Term concept) {
			List<Instance> instances = new ArrayList<Instance>();
			for (Entity entity : resp) {
				if(entity instanceof Instance) {
					Instance instance = (Instance) entity;
					Set<Concept> concepts = instance.listConcepts();
					for (Concept c : concepts) {
						if(c.getIdentifier().toString().equals(concept.toString())) {
							instances.add(instance);
						}
					}
				}
			}
			return instances;
		}

		@Override
		public Event getAssociatedEvent() {
			return Event.CHOREOGRAPHY;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
		
		private List<Entity> wsInvocation(Context contextId, WebService service, List<Entity> instances, String grounding) { 
			List<Entity> resp = new ArrayList<Entity>();
			
			String logMsg = "----------- Invoker: Sending WSML to service -----------\n" +
							"Service grounding: >>> " + grounding + " <<< \n" +
							Helper.printSetFull(new HashSet(instances));
			logger.info(logMsg);
			Helper.visualizerLog(Helper.FILTER_OUTGOING,logMsg);
			Helper.generateMonitoringEvent(EventsGenerator.createStartInvokeWSEvent(discoveredWebService.getIdentifier().toString(), Helper.getEntitiesAsString(instances)));
			
			if (Environment.isCore()){
			
				AchieveGoalChor delegate = new AchieveGoalChor(new PerformInvocation(getContext(), service, instances, grounding));
				AbstractExecutionSemantic carrier;
				try {
					carrier = spawn(AchieveGoalChor.this, delegate);
					resp = (List<Entity>) carrier.getState().getData();
				} catch (SystemException e) {
					e.printStackTrace();
				}
			} else {
				//testing, no-server mode
				try {
					resp = wsmxInvoker.invoke(service, instances, grounding);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}			
			logMsg =		"----------- Invoker: Received WSML from service -----------\n"+ 
							Helper.printSetFull(new HashSet(resp));
			logger.info(logMsg);
			Helper.visualizerLog(Helper.FILTER_INCOMING,logMsg);

			if ((resp != null) && (!resp.isEmpty())){
				Helper.generateMonitoringEvent(EventsGenerator.createEndInvokeWSEvent(discoveredWebService.getIdentifier().toString(), Helper.getEntitiesAsString(resp)));
			} else {
				Helper.generateMonitoringEvent(EventsGenerator.createEndInvokeWSEvent(discoveredWebService.getIdentifier().toString(), "empty document"));
			}
			return resp;
		}
		

		public List<Entity> runChoreography(Goal goalR, List<Entity> goalInstancesR, WebService webServiceR, Interface interR){
			
			try {
				//now read Web service
				List<Entity> cacheInstancesForProvider = goalInstancesR;//new HashSet<Entity>(Helper.getInstances(goalOnto));
				List<Entity> allInstancesReceivedFromProvider = new ArrayList<Entity>();
				
				try {
					cEngine.registerChoreography(goalR, null);
				} catch (Exception e){
					//ignore
				}
				cEngine.registerChoreography(webServiceR, interR);
							
				printOutInstances("\n----------- Updating CE ---REQ_TO_PROV--- "+cacheInstancesForProvider.size()+" instances -----------", cacheInstancesForProvider);
				Map<Instance, ResponseModifierInterface> chorResp = cEngine.updateState(Direction.REQUESTER_TO_PROVIDER, new HashSet(cacheInstancesForProvider));
				
				logger.info("CE returned:" + Helper.printSetShort(chorResp.keySet()));
				
				//list of messages returned from the WSDL endpoints after the WSML lifting
				List<Entity> msgForRequester = new ArrayList<Entity>();
				List<Entity> msgForRequesterToBeCleaned = new ArrayList<Entity>();
				Set<Instance> createdByExecution = new HashSet<Instance>();
				int instancesSent;
				while (true){
					instancesSent = 0;
					for (Instance inst : chorResp.keySet()) {
						if (cacheInstancesForProvider.contains(inst))
							cacheInstancesForProvider.remove(inst);

						List<Entity> localInsts = new ArrayList<Entity>();
						localInsts.add(inst);
						
						ResponseModifierInterface responseModifier = chorResp.get(inst);
						WSDLGrounding grounding = (WSDLGrounding) responseModifier.getGrounding();
						
						
						if (grounding!= null) {
							instancesSent++; 
							logger.debug("----------- Instance to send -----------\n" + inst.getIdentifier());					
							// FIXME reliance on to string
							String groundingIRI = grounding.getIRI().toString();
							logger.debug("Grounding: >>>" + groundingIRI + "<<<");
						
							List<Entity> resp = wsInvocation(getContext(), webServiceR, localInsts, groundingIRI);
							
							Set<Molecule> molecules = responseModifier.getMolecules();
							
							List<Instance> instances = null;
							List<AttributeValueMolecule> avms = new ArrayList<AttributeValueMolecule>();
							
							logger.debug("Before Molecule:\n " + Helper.printSetShort(new HashSet<Entity>(allInstancesReceivedFromProvider)));
							
							for (Molecule molecule : molecules) {
								if(molecule instanceof AttributeValueMolecule) {
									avms.add((AttributeValueMolecule) molecule);
								} else {
									Term concept = molecule.getRightParameter();
									instances = getInstancesForConcept(resp,concept);
								}
							}
							
							if(instances != null) {
								for (Instance instance : instances) {
									for (AttributeValueMolecule molecule : avms) {
										instance.addAttributeValue((Identifier)molecule.getAttribute(), (Value)molecule.getRightParameter());
										logger.info("Response instance "+ instance.getIdentifier() + "\n            added " + molecule.getRightParameter());
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
					
					//now, feed back obtained instances to Chor. Engine
					printOutInstances("\n----------- Updating CE ---PROV_TO_REQ--- "+allInstancesReceivedFromProvider.size()+" instances -----------", new ArrayList(allInstancesReceivedFromProvider));
					logger.debug("Before PROVIDER_TO_REQUESTER update:\n " + Helper.printSetShort(new HashSet<Entity>(msgForRequester)));
					
					chorResp = cEngine.updateState(Direction.PROVIDER_TO_REQUESTER, new HashSet(msgForRequester));
				
					//check if in the endEtate
					if (cEngine.isProviderChorInEndState()) {
						logger.debug("provider chor in end state"); // [mh]
						break;
					}
					
					printOutInstances("\n----------- Updating CE ---REQ_TO_PROV--- "+cacheInstancesForProvider.size()+" instances -----------", cacheInstancesForProvider);				
					chorResp = cEngine.updateState(Direction.REQUESTER_TO_PROVIDER, new HashSet(cacheInstancesForProvider));

					if (instancesSent == 0)
						break;
					else {
						msgForRequester.clear();
						createdByExecution.clear();
					}
					
				}				
				//delete goal and goal ontology
				//clear goal ontology

				for (Entity ent : goalInstancesR ){
					if (ent instanceof Instance) {
						Instance i = (Instance) ent;
						Map attr = i.listAttributeValues();
						Set<IRI> keys = attr.keySet();
					
						for (IRI iri : keys){
							Object values = attr.get(iri);
							i.removeAttributeValues(iri);
						}
					}
				}
				
				String respOntoNS = "http://wsmx.org//responseOntology" + Helper.getRandomLong();
				Ontology respOnto = wsmoFactory.createOntology(wsmoFactory.createIRI(respOntoNS));
		    	// create response ontology and clone the relevant instances to it
			    	
			   	// First check non functional properties of the service
			   	Set theNFPs = webServiceR.listNFPValues(wsmoFactory.createIRI("http://response"));
					
			   	if (!theNFPs.isEmpty()) {
			   		IRI lastConcept = (IRI) theNFPs.iterator().next();

			   		logger.debug("Response concept of the Web service: " + lastConcept + "\n");
			   		for (Instance i : createdByExecution) {
			   			for (Concept concept : i.listConcepts()) {
			   				if(concept.getIdentifier().toString().equals(lastConcept.toString())) {
			   					// the response ontology will contain this instance
			   					Helper.clone(i, respOntoNS, respOnto, new HashMap<IRI, Instance>(), new HashMap<Instance, Instance>());		    				    		
			   				} else {
			   					logger.info("Instance " + i.getIdentifier() + " not added to the response ontology.");
			   				}
			   			}
			   			
			   			if (i.getOntology()!=null && i.getOntology().listNamespaces()!=null){
			   				//add namespaces
			   				for (Namespace ns: i.getOntology().listNamespaces()){
			   					if (!respOnto.listNamespaces().contains(ns))
			   						respOnto.addNamespace(ns);
			   				}
			   			}
			   		}
			   	} else {
			   		// Otherwise return last instance
			   		if (msgForRequesterToBeCleaned.size() > 0) {		    		
			   			Instance lastInstance = (Instance)msgForRequesterToBeCleaned.get(msgForRequesterToBeCleaned.size()-1);
			   			Helper.clone(lastInstance, respOntoNS, respOnto, new HashMap<IRI, Instance>(), new HashMap<Instance, Instance>());
			   			//add namespaces
			   			for (Namespace ns: lastInstance.getOntology().listNamespaces()){
			   				respOnto.addNamespace(ns);
			   			}
			   		}
			   	}
			   	// clean up all the original messages from memory (so it doesn't break subsequent runs)
			   	Helper.cleanUpInstances(msgForRequesterToBeCleaned);
			   	
			   	logger.info("----------- Final response to requestor -----------\n" + Helper.serializeTopEntity(respOnto));
			   	return Helper.getInstances(respOnto);			    	
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return new ArrayList<Entity>();
		}		
	}
	
	private void printOutInstances(String msg, List<Entity> instances){
		// [mh] changed to only print out 1st and last instance if size is very large
		if (logger.isDebugEnabled()) {
			logger.debug(msg);			
			if (instances.size() <= 50) {
				for (Entity e : instances) {
					Instance i = (Instance) e;
					logger.debug(((Instance)e).getIdentifier().toString() +  " memberOf " + Helper.printSetShort(i.listConcepts()));
				}
			} else {
				logger.debug("Many instances, only printing out first and last: ");
				Instance first = (Instance)instances.get(0);
				Instance last = (Instance)instances.get(instances.size()-1);
				logger.debug(first.getIdentifier().toString() +  " memberOf " + Helper.printSetShort(first.listConcepts()));
				logger.debug("[...]");
				logger.debug(last.getIdentifier().toString() +  " memberOf " + Helper.printSetShort(last.listConcepts()));
			}
		}
	}
	

	public class EntryPoint extends State {
		private static final long serialVersionUID = 7305173447500494311L;
		Serializable r;

	    public EntryPoint(Context contextId, Serializable r) {
	        super();
	        this.contextId = contextId;
	        this.r = r;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException {
	    	logger.debug("Entrypoint state.");
	    	//not needed
	        return new Exodus();
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.ENTRYPOINT;
		}

		@Override
		public Serializable getData() {
			return r;
		}

		@Override
		public boolean isStatefulResponse() {
			return true;
		}
	}
	
	class Exodus extends State {
		private static final long serialVersionUID = 5417996174132567582L;

		String output = null;

		public Exodus(){
			super();
		}
		
	    public Exodus(Context contextId, String output) {
	        super();
	        this.contextId = contextId;
	        this.output = output;
	    }
		
		@Override
		public State handleState(Object component) throws UnsupportedOperationException, ComponentException {
			return null;
		}

		@Override
		public Event getAssociatedEvent() {
			return Event.EXODUS;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
		
	}

	//Proxies follow below this line
	
	class PerformInvocation extends State {
		private static final long serialVersionUID = 2826724910732574204L;
		WebService service = null;
		List<Entity> instances = null;
		String grounding = null;

	    public PerformInvocation(Context contextId, WebService service, List<Entity> instances, String grounding) {
	        super();
	        this.contextId = contextId;
	        this.service = service;
	        this.instances = instances;
	        this.grounding = grounding;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException {
	    	Invoker invoker;
	    	
	    	try {
	    		invoker = ((Invoker)component);
	    	} catch (ClassCastException cce) {
			   	throw new RoutingException(Invoker.class, component.getClass());
			}
			List<Entity> respData = null;
			try {
				respData = invoker.invoke(service, instances, grounding);
			} catch (WSDL1_1GroundingException e) {
				logger.error(e,e);
			}
			
	    	return new InvocationResponseCarrier(contextId, respData);
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.INVOKER;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}

	}
	
	public class InvocationResponseCarrier extends State {
		private static final long serialVersionUID = -269305420557478782L;
		List<Entity> s;

	    public InvocationResponseCarrier(Context contextId, List<Entity> instances) {
	        super();
	        this.contextId = contextId;
	        this.s = instances;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException {
	    	logger.debug("InvocationResponse state.");
	    	//branch dies
	        return new Exodus();
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.CHOREOGRAPHY;
		}

		@Override
		public Serializable getData() {
			return (Serializable) s;
		}

		@Override
		public boolean isStatefulResponse() {
			return true;
		}
	}
	
	class PerformWebServiceResourceRetrieval extends State implements WebServiceResourceManager, Proxy<WebServiceResourceManager> {
		private static final long serialVersionUID = 2826724910732574204L;
		Set<WebService> data = null;
		int discoveryType = -1;

	    public PerformWebServiceResourceRetrieval(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }
		
	    public PerformWebServiceResourceRetrieval(Context contextId, int discoveryType) {
	        super();
	        this.contextId = contextId;
	        this.discoveryType = discoveryType;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException {
	    	WebServiceResourceManager rm;
	    	try {
	    		rm = ((WebServiceResourceManager)component);
	    	} catch (ClassCastException cce) {
			   	throw new RoutingException(WebServiceResourceManager.class, component.getClass());
			}
	    	Set<WebService> data = rm.retrieveWebServices(discoveryType);
	    	return new WebServiceCarrier(contextId, data);
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.RESOURCEMANAGER;
		}

		public WebServiceResourceManager muteToComponent() {
			return this;
		}

		public void removeWebService(WebService arg0) throws ComponentException, UnsupportedOperationException {
			// TODO Auto-generated method stub
			
		}

		public Set<Identifier> getWebServiceIdentifiers() throws ComponentException, UnsupportedOperationException {
			// TODO Auto-generated method stub
			return null;
		}

		public Set<Identifier> getWebServiceIdentifiers(Namespace arg0) throws ComponentException, UnsupportedOperationException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean containsWebService(Identifier arg0) throws ComponentException, UnsupportedOperationException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}

        public void storeWebService(WebService webService) throws ComponentException, UnsupportedOperationException {
            // TODO Auto-generated method stub
        }

        public Set<WebService> retrieveWebServices() throws ComponentException, UnsupportedOperationException {
        	tagStateForTerminationOnFailure();
			Set<WebService> s = null;
			try {
				AchieveGoalChor delegate = new AchieveGoalChor(new PerformWebServiceResourceRetrieval(getContext()));
				AbstractExecutionSemantic carrier = spawn(AchieveGoalChor.this, delegate);
				s = (Set<WebService>) carrier.getState().getData();
			} catch (SystemException e) {
				throw new ComponentException("Unable to retrieve webservices due to system failure.", e);
			} catch (Throwable t) {
				throw new ComponentException("Unable to retrieve webservices due to unexpected failure.", t);
			}
			untagStateForTerminationOnFailure();
			return s;
        }

        public Set<WebService> retrieveWebServices(Namespace namespace) throws ComponentException, UnsupportedOperationException {
            // TODO Auto-generated method stub
            return null;
        }

        public WebService retrieveWebService(Identifier identifier) throws ComponentException, UnsupportedOperationException {
            // TODO Auto-generated method stub
            return null;
        }

        public Set<Namespace> getWebServiceNamespaces() throws ComponentException, UnsupportedOperationException {
            // TODO Auto-generated method stub
            return null;
        }

		/* (non-Javadoc)
		 * @see org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager#retrieveWebServices(int)
		 */
		public Set<WebService> retrieveWebServices(int discoveryType) throws ComponentException, UnsupportedOperationException {
        	tagStateForTerminationOnFailure();
			Set<WebService> s = null;
			try {
				AchieveGoalChor delegate = new AchieveGoalChor(new PerformWebServiceResourceRetrieval(getContext(), discoveryType));
				AbstractExecutionSemantic carrier = spawn(AchieveGoalChor.this, delegate);
				s = (Set<WebService>) carrier.getState().getData();
			} catch (SystemException e) {
				throw new ComponentException("Unable to retrieve webservices due to system failure.", e);
			} catch (Throwable t) {
				throw new ComponentException("Unable to retrieve webservices due to unexpected failure.", t);
			}
			untagStateForTerminationOnFailure();
			return s;
		}

		/* (non-Javadoc)
		 * @see org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager#retrieveWebServicesReferringOntology(org.wsmo.common.Identifier)
		 */
		public Set<WebService> retrieveWebServicesReferringOntology(
				Identifier ontoIdentifer) throws ComponentException,
				UnsupportedOperationException {
			return null;
		}
		
	}

	public class WebServiceCarrier extends State {
		private static final long serialVersionUID = -2669305420557478782L;
		Set<WebService> s;

	    public WebServiceCarrier(Context contextId, Set<WebService> services) {
	        super();
	        this.contextId = contextId;
	        this.s = services;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException {
	    	logger.debug("RetrievedWebServices state.");
	    	//branch dies
	        return new Exodus();
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.SERVICEDISCOVERY;
		}

		@Override
		public Serializable getData() {
			return (Serializable) s;
		}

		@Override
		public boolean isStatefulResponse() {
			return true;
		}
	}
}