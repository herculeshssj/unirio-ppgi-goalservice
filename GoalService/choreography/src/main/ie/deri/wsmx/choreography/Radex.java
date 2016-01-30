/*
 * Copyright (c) 2006 University of Innsbruck, Austria
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ie.deri.wsmx.choreography;

import ie.deri.wsmx.asm.ContextInterface;
import ie.deri.wsmx.asm.LogicalState;
import ie.deri.wsmx.asm.Machine;
import ie.deri.wsmx.asm.OutstandingInstancesException;
import ie.deri.wsmx.asm.OutstandingInstancesSuppliedException;
import ie.deri.wsmx.asm.State;
import ie.deri.wsmx.asm.UpdateFailedException;
import ie.deri.wsmx.asm.UpdateIllegalException;
import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.core.configuration.annotation.Exposed;
import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.io.parser.wsml.ParserImpl;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.Molecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Axiom;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.TopEntity;
import org.wsmo.common.UnnumberedAnonymousID;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.component.ChoreographyEngine;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.ResponseModifierInterface;
import org.wsmo.factory.Factory;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;
import org.wsmo.service.WebService;
import org.wsmo.service.choreography.Choreography;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.WSDLGrounding;
import org.wsmo.wsml.Serializer;

import com.ontotext.wsmo4j.common.UnnumberedAnonymousIDImpl;

/**
 * Choreography engine.
 *
 * <pre>
 * Created on 30.05.2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/choreography/src/main/ie/deri/wsmx/choreography/Radex.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.37 $ $Date: 2007-06-14 16:12:24 $
 */
@WSMXComponent(name =	"RadexChoreography",
			   events =	"CHOREOGRAPHY",
			   description = "A choreography engine based on abstract state machines.")
public class Radex implements ChoreographyEngine {

	public static enum  Validation {
		STRICT,
		BUFFERING
	}
	
	protected static Logger logger = Logger.getLogger(Radex.class);
	private Machine provider, requester;
	private InvocationLinker providerLinker, requesterLinker;
	ModificationHistory providerHistory = new ModificationHistory(),
						requesterHistory = new ModificationHistory();

	@Exposed(description = "Human-interface-friendly wrapper around the operation " +
			"that allows to pass in a goal to register the requester's " +
			"choreography. Returns a human-readable confirmation or error description.")
	public String registerRequesterChoreography(String goal) {
		try {
			Set<Entity> entities;
			try {
				entities = parse(goal);
			} catch (ComponentException e) {
				logger.warn("Failed to parse goal.", e);
				return "Failed to parse goal: " + e.getMessage();
			}
			Goal parsedGoal = null;
			for (Entity entity : entities) {
				if (entity instanceof Goal) {
					parsedGoal = (Goal) entity;
				}
			}
			if (parsedGoal == null) {
				return "Failed to locate a goal in the parsed document.";
			}			
			try {
				registerChoreography(parsedGoal, null);
				requester.getModificationBroadcaster().registerListener(requesterHistory);
			} catch (UnsupportedOperationException e) {
				logger.warn("Operation not supported.", e);
				return "Operation not supported: " + e.getMessage();
			} catch (ComponentException e) {
				logger.warn("Failure during registration.", e);
				return "Failure during registration: " + e.getMessage();
			}	
			return "Requester choreography registered.";
		} catch (Throwable t) {
			logger.warn("Failed to register requester choreography.", t);
			return "Failed to register requester choreography: " + t.getMessage();
		}
	}	
	
	@Exposed(description = "Human-interface-friendly wrapper around the operation " +
			"that allows to pass in a webservice description to register the providers's " +
			"choreography. Returns a human-readable confirmation or error description.")
	public String registerProviderChoreography(String service) {
		try {
			Set<Entity> entities;
			try {
				entities = parse(service);
			} catch (ComponentException e) {
				logger.warn("Failed to parse service.", e);
				return "Failed to parse service: " + e.getMessage();
			}
			WebService parsedService = null;
			for (Entity entity : entities) {
				if (entity instanceof WebService) {
					parsedService = (WebService) entity;
				}
			}
			if (parsedService == null) {
				return "Failed to locate a webservice in the parsed document.";
			}			
			try {
				registerChoreography(parsedService, parsedService.listInterfaces().iterator().next());
				provider.getModificationBroadcaster().registerListener(providerHistory);
			} catch (UnsupportedOperationException e) {
				logger.warn("Operation not supported.", e);
				return "Operation not supported: " + e.getMessage();
			} catch (ComponentException e) {
				logger.warn("Failure during registration.", e);
				return "Failure during registration: " + e.getMessage();
			}	
			return "Provider choreography registered.";
		} catch (Throwable t) {
			logger.warn("Failed to register provider choreography.", t);
			return "Failed to register provider choreography: " + t.getMessage();
		}

	}
	
	@Exposed(description = "Human-interface-friendly wrapper around the operation " +
			"that allows to update the state. The first parameter is either a '<' or a " +
			"'>' which stands for provider to requester or requester to provider, respectively, indicating" +
			"the direction of the data flow.")
	public String updateState(String direction, String instances) {		
		try {
			Set<Entity> entities;
			try {
				entities = parse(instances);
			} catch (ComponentException e) {
				logger.warn("Failed to parse document.", e);
				return "Failed to parse document: " + e.getMessage();
			}
			Set<Instance> parsedInstances = new HashSet<Instance>();
			//TODO warn about non-instances
			for (Entity entity : entities) {
				if (entity instanceof Ontology) {
					//TODO check if instance present
					((Ontology)entity).getDefaultNamespace();
					parsedInstances.add((Instance) ((Ontology)entity).listInstances().iterator().next());
				}
			}
			Map<Instance, ResponseModifierInterface> g = null;
			try {
				if (direction.equals("<"))	
					g = updateState(Direction.PROVIDER_TO_REQUESTER, parsedInstances);
				else if (direction.equals(">"))	
					g = updateState(Direction.REQUESTER_TO_PROVIDER, parsedInstances);
				else {
					logger.warn("Invalid direction, use either '<' or '>'.");
					return "Invalid direction, use either '<' or '>' " +
					"for provider to requester or requester to provider, respectively.";
				}
			} catch (UnsupportedOperationException e) {
				logger.warn("Operation not supported.", e);
				return "Operation not supported: " + e.getMessage();
			} catch (ComponentException e) {
				logger.warn("Failure during state update.", e);
				return "Failure during state update: " + e.getMessage();
			}
			String msg = "State updated.";
			if (requester == null)
				msg = msg + "\nRequester choreography not registered, skipped requester update.";
			else
				msg = msg + "\nRequester choreography stepped.";				
			if (provider == null)
				msg = msg + "\nProvider choreography not registered, skipped provider update.";
			else
				msg = msg + "\nProvider choreography stepped.";

			msg = msg + "\nGroundings: " + g;
			return msg;
		} catch (Throwable t) {
			logger.warn("State update failed", t);
			return "State update failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();			
		}
	}	

	@Exposed(description = "Returns a textual description of the provider's machine state modification history.")
	public String getProviderMachineHistory() {
		return providerHistory.getHistory();
	}

	@Exposed(description = "Returns a textual description of the requester's machine state modification history.")
	public String getRequesterMachineHistory() {
		return requesterHistory.getHistory();
	}

	@Exposed (description = "")
	public void registerChoreography(Goal goal, Interface inter) throws ComponentException, UnsupportedOperationException {
		String logMsg = "Registering requester choreography"; 
		logger.info(logMsg);
		Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,logMsg);
//		
//		logger.info("goal " + goal.getIdentifier());
//		logger.info("has interfaces " + ((InterfaceImpl)goal.listInterfaces().iterator().next()).getIdentifier());
//		if (goal.listInterfaces().isEmpty()) {
//			logger.info("Interface of web service does not specify a choreography.");
//			throw new ComponentException("Interface of goal does not specify a cheography.");
//		}
//		logger.info("Registering requester choreography - 1");
//		Interface i = (Interface) goal.listInterfaces().iterator().next();
//		logger.info("Registering requester choreography - 2");
//		Choreography choreography = (Choreography) i.getChoreography();
//		logger.info("Registering requester choreography - 3");
//		//requester = new Machine(choreography.getIdentifier(), choreography.getStateSignature(), choreography.getRules());
//		//requesterLinker = new InvocationLinker(choreography.getStateSignature());
//		//((StateModificationBroadcaster)requester.getState()).registerListener(requesterLinker);
//		logger.info("Finished Registering requester choreography");
	}

	@Exposed (description = "")
	public void registerChoreography(WebService service, Interface inter) throws ComponentException, UnsupportedOperationException {
		String logMsg = "Registering provider choreography"; 
		logger.info(logMsg);
		Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,logMsg);

		if (service.listInterfaces().isEmpty()) {
			logger.info("Interface of web service does not specify a choreography.");
			throw new ComponentException("Interface of web service does not specify a choreography.");
		}
		Interface i = inter;
		Choreography choreography = (Choreography) i.getChoreography();
		logger.info("id:" + choreography.getIdentifier());
		Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,"id:" + choreography.getIdentifier());
		logger.info("signature id:" + choreography.getStateSignature().getIdentifier());
		Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,"signature id:" + choreography.getStateSignature().getIdentifier());
		logger.info("rules id:" + choreography.getRules().getIdentifier());
		Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,"rules id:" + choreography.getRules().getIdentifier());
		provider = new Machine(choreography.getIdentifier(), choreography.getStateSignature(), choreography.getRules());
	}

	public void updateState(URI source, Entity data) throws ComponentException, UnsupportedOperationException {
		//TODO obsolete?
	}

	public Grounding updateState(Direction direction, Instance data) 
			throws ComponentException, UnsupportedOperationException, UpdateIllegalException {
		logger.info("Updating state in " + direction + " direction with " + data);
		Set<Instance> set = new HashSet<Instance>();
		set.add(data);
		Map<Instance, ResponseModifierInterface> g = updateState(direction, set);
		if (g.values().size() > 0)
			return g.values().iterator().next().getGrounding();
		return null;
	}
	
	private Map<Instance, Grounding> filterTriggers(Map<Instance, Grounding> g, Set<Term> t) {
		Map<Instance, Grounding> r = new HashMap<Instance, Grounding>();
		for (Entry<Instance, Grounding> e: g.entrySet()) {
			if (t.contains(e.getKey().getIdentifier()) )
//					|| e.getKey().getIdentifier() instanceof UnnumberedAnonymousID 
//					|| e.getKey().getIdentifier().toString().startsWith("http://www.wsmo.org/reasoner/anonymous_")) //FIXME later
				r.put(e.getKey(), e.getValue());
		}
		return r;
	}
	
	private Map<Instance, Grounding> clean(Map<Instance, Grounding> dirty) {
		//be nice and don't modify the passed in map
		HashMap<Instance, Grounding> clean = new HashMap<Instance, Grounding>(dirty);
		Set <Entry<Instance, Grounding>> eSet = clean.entrySet();
		List<Instance> toRemove = new ArrayList<Instance>();
		
		for (Entry<Instance, Grounding> e : eSet) {
			if (e.getValue() == null)
				toRemove.add(e.getKey());
		}
		
		for (Instance i: toRemove)
			clean.remove(i);
		
		return clean;
	}
	
	@Exposed (description = "")
	public boolean isProviderChorInEndState(){
		return ((LogicalState) provider.getState()).isEndState;
	}
	
	
	@Exposed (description = "")
	public Map<Instance, ResponseModifierInterface> updateState(Direction direction, Set<Instance> data) 
			throws ComponentException, UnsupportedOperationException {
		Map response = null;
		String logMsg = "Updating state in " + direction + " direction with " + 
						Helper.printSetShort(data);
		logger.debug(logMsg);
		Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,logMsg);
		
		Map<Instance, Grounding> g = null;
		Map<Instance, Grounding> triggers = null;
		if (direction.equals(Direction.REQUESTER_TO_PROVIDER)) {
				try {
					requester = null; 
					//FIXME
					if (requester == null)
						logger.warn("Requester's choreography was not registered, skipping requester update.");
					else {
						try {
							logger.debug("Before requester update");
							g = requester.updateState(direction, data);
							logger.debug("After requester update");
						} catch (OutstandingInstancesSuppliedException e) {
							return Collections.EMPTY_MAP;
						}
//						logger.info("R-TO-P: Requester state ontology:" + prettyPrint(requester.getState().getOntology()));
						logger.info("Requester state ontology:");
						requester.getState().getOntology().listInstances();
						logger.info("Requester machine groundings:" + g.size());
						Set<Term> t = requester.step();
						
						reintroduceAnonymousInstances(g);
				    	
						logger.info("Requester machine trigger groundings(" + t.size() + ")" + ":" + t);
						Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,"Requester machine trigger groundings(" + t.size() + ")" + ":" + t);
						for (Term tt : t) {
							logger.info("Term : " + tt.toString());
							Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,"Term : " + tt.toString());
						}
						//some instance might have been modfied through axiom inside an update set
						g = refresh(g);
						triggers = filterTriggers(g, t);
						triggers = clean(triggers);
						logger.debug("Requester machine merged groundings: " + triggers.size());
						response = createResponse(triggers, false);
					}
				} catch (UpdateFailedException e) {
					logger.warn("State update failed.", e);
					//TODO recovery
				} catch (UpdateIllegalException e) {
					logger.warn("State update illegal.", e);
				} catch (OutstandingInstancesException e) {
					logger.warn("Outstanding instances.", e);
				} catch (SynchronisationException e) {
					logger.warn("Synchronisation exception.", e);
				} catch (InvalidModelException e) {
					logger.warn("Invalid model.", e);
				}
				//TODO sync
				try {
					if (provider == null)
						logger.warn("Provider's choreography was not registered, skipping provider update.");
					else {
						try {
							logger.debug("Before provider update");
							g = provider.updateState(direction, data);
							for (Iterator iter = data.iterator(); iter.hasNext();) {
								Instance element = (Instance) iter.next();
								logger.info("element " + element.getIdentifier());
							}
							logger.debug("After provider update");
						} catch (OutstandingInstancesSuppliedException e) {
							return Collections.EMPTY_MAP;
						}
						logger.debug("R-TO-P: Provider state ontology: \n" + Helper.serializeTopEntity(provider.getState().getOntology()));
						
						logger.info("Provider machine groundings:" + g.size());
						Set<Term> t = provider.step();
						logger.info("Provider machine trigger groundings(" + t.size() + ")" + ":" + t);
						
						reintroduceAnonymousInstances(g);

						//some instance might have been modified through axiom inside an update set
						logger.debug("Provider state ontology after step: \n" + Helper.serializeTopEntity(provider.getState().getOntology()));

						logger.info("R-TO-P Groundings:");
						Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,"R-TO-P Groundings:");
						for (Instance gg : g.keySet()) {
							Object tt = g.get(gg);
							if (tt != null) {
								logger.info(gg.getIdentifier().toString() + " has " + ((WSDLGrounding)tt).getIRI().toString());
								Helper.visualizerLog(Helper.FILTER_CHOREOGRAPHY,gg.getIdentifier().toString() + " has " + ((WSDLGrounding)tt).getIRI().toString());
							} else 
								logger.info(gg.getIdentifier().toString() + " has no grounding");
						}
						g = refresh(g);
						logger.info("Groundings after refresh:");
						for (Instance gg : g.keySet()) {
							Object tt = g.get(gg);
							if (tt != null)
								logger.info(gg.getIdentifier().toString() + " has " + ((WSDLGrounding)tt).getIRI().toString());
						}
						triggers = filterTriggers(g, t);
						logger.info("Groundings after filterTriggers:");
						for (Instance gg : triggers.keySet()) {
							Object tt = g.get(gg);
							if (tt != null)
								logger.info(gg.getIdentifier().toString() + " has " + ((WSDLGrounding)tt).getIRI().toString());
						}
						triggers = clean(triggers);
						logger.info("Groundings after clean:");
						for (Instance gg : triggers.keySet()) {
							Object tt = g.get(gg);
							if (tt != null)
								logger.info(gg.getIdentifier().toString() + "has " + ((WSDLGrounding)tt).getIRI().toString());
						}
						logger.info("Provider machine merged groundings: " + triggers.size());
						response = createResponse(triggers, true);
					}
				} catch (UpdateFailedException e) {
					logger.warn("State update failed.", e);
					//TODO recovery
				} catch (UpdateIllegalException e) {
					logger.warn("State update illegal.", e);
				} catch (OutstandingInstancesException e) {
					logger.warn("Outstanding instances.", e);
				} catch (SynchronisationException e) {
					logger.warn("Synchronisation exception.", e);
				} catch (InvalidModelException e) {
					logger.warn("Invalid model.", e);
				}
				
				return response;
		}
		else if (direction.equals(Direction.PROVIDER_TO_REQUESTER)) {
				try {
					if (provider == null)
						logger.warn("Provider's choreography was not registered, skipping provider update.");
					else {
						
						try {
							g = provider.updateState(direction, data);
						} catch (OutstandingInstancesSuppliedException e) {
							return Collections.EMPTY_MAP;
						}
//						logger.info("P-TO-R: Provider state ontology:" + prettyPrint(provider.getState().getOntology()));
						logger.debug("Provider machine groundings:" + g.size());
//						Set<Term> t = provider.step();
//
//						reintroduceAnonymousInstances(g);
//						logger.debug("P-TO-R machine trigger groundings(" + t.size() + ")" + ":" + t);
//						triggers = filterTriggers(g, t);
//						logger.info("Groundings after filterTriggers:");
//						for (Instance gg : triggers.keySet()) {
//							Object tt = g.get(gg);
//							if (tt != null)
//								logger.info(gg.getIdentifier().toString() + " has " + ((WSDLGrounding)tt).getIRI().toString());
//						}
//						triggers = clean(triggers);
//						logger.info("Groundings after clean:");
//						for (Instance gg : triggers.keySet()) {
//							Object tt = g.get(gg);
//							if (tt != null)
//								logger.info(gg.getIdentifier().toString() + "has " + ((WSDLGrounding)tt).getIRI().toString());
//						}
//						logger.debug("Provider machine merged groundings: " + triggers.size());
					}
				} catch (UpdateFailedException e) {
					logger.warn("State update failed.", e);
					//TODO recovery
				} catch (OutstandingInstancesException e) {
					logger.warn("Outstanding instances.", e);
				} catch (UpdateIllegalException e) {
					e.printStackTrace();
					logger.warn("State update illegal.", e);
				}
				try {
					if (requester == null)
						logger.warn("Requester's choreography was not registered, skipping requester update.");
					else {
						try {
							g = requester.updateState(direction, data);
						} catch (OutstandingInstancesSuppliedException e) {
							return Collections.EMPTY_MAP;
						}
						logger.debug("Requester machine groundings:" + g.size());
						Set<Term> t = requester.step();
						for (Term tt : t) {
							logger.info("Term : " + tt.toString());
						}
						logger.debug("Requester machine trigger groundings(" + t.size() + ")" + ":" + t);
						triggers = filterTriggers(g, t);
						triggers = clean(triggers);
						logger.debug("Requester machine merged groundings: " + triggers.size());
						response = createResponse(triggers,false);
					}
				} catch (UpdateFailedException e) {
					logger.warn("State update failed.", e);
					//TODO recovery
				} catch (OutstandingInstancesException e) {
					logger.warn("Outstanding instances.", e);
				} catch (UpdateIllegalException e) {
					logger.warn("State update illegal.", e);
				} catch (SynchronisationException e) {
					logger.warn("Sync exception.", e);
				} catch (InvalidModelException e) {
					logger.warn("Invalid model.", e);
				}
				return response;
		} else
			throw new ComponentException("Invalid direction.");
	}
	
	
	private Map createResponse(Map<Instance, Grounding> triggers, boolean modify) throws SynchronisationException, InvalidModelException {
		Map response = null;
		Set<ContextInterface> contextAfterStep = provider.getContextAfterStep();
		Set<Instance> instances = triggers.keySet();
		if(instances != null) {
			response = new HashMap<Instance, ResponseModifierInterface>();
			for (Iterator iter = instances.iterator(); iter
					.hasNext();) {
				Instance instance = (Instance) iter.next();
				Grounding grounding = triggers.get(instance);
				
				Set molecules = null;
				if(modify){
					molecules = getMolecules(contextAfterStep, instance, provider.getState());
				}
				
				ResponseModifierInterface rm = new ResponseModifier(grounding, molecules);
				//in case of instance attribute value modification, we read instance from the state ontology which has latest change  
				if (instance.getIdentifier().toString().startsWith("http://www.wsmo.org/reasoner/anonymous")){
					response.put(instance, rm);
				} else {
					Instance stateInstance = provider.getState().getOntology().findInstance(instance.getIdentifier());
					response.put(stateInstance, rm);
				}
			}
		}
		// if the response instance is created by the choreography, add the instance, but without any grounding
		for (Molecule m : provider.getOutstandingMolecules()) {
			if(m instanceof MembershipMolecule){
				Set<Instance> set = provider.getState().getInstances(new UnnumberedAnonymousIDImpl(), ((IRI)((MembershipMolecule) m).getRightParameter()));
				for (Instance instance : set) {
					response.put(instance, new ResponseModifier(null));
				}
			}
		}
		
		
		return response;
	}

	private Set<Molecule> getMolecules(Set<ContextInterface> context, Instance inst, State state) throws SynchronisationException, InvalidModelException{
		Map<Molecule, Axiom> toReturn = new HashMap<Molecule, Axiom>();
		
		// Rules that modify the response
		for (ContextInterface c : context) {
			List<String> outTerms = c.getOutTerms(inst.getIdentifier().toString());
			if(outTerms != null && outTerms.size() > 0) {
				logger.info("For instance " + inst.getIdentifier() + " found " + outTerms.size() + " terms.");
				
				// Get the molecules for the out terms.
				for (String id : outTerms) {
					logger.info("id " + id);
					
					for (Entry<Molecule, Axiom> e : state.getAxioms().entrySet()) {
						Molecule molecule = e.getKey();
						if(molecule.getLeftParameter().toString().equals(id)) {
							toReturn.put(molecule, e.getValue());
						}
					}
					// The ids of the response instances will not be annonimous.
					// The axioms should be removed so that there won't be duplicates.
					for (Entry<Molecule, Axiom> e : toReturn.entrySet()) {
						state.getOntology().removeAxiom(e.getValue());
						state.getAxioms().remove(e.getKey());			
					}
				}
			}
		}
		return toReturn.keySet();
	}
	

	private void reintroduceAnonymousInstances(Map<Instance, Grounding> g) throws UpdateIllegalException, SynchronisationException, InvalidModelException {
		Set<Instance> instances;
		Set<Term> anonymousConcepts = provider.getAnonymousConcepts();
		
		State state = provider.getState();
//		logger.info("SERIALIZED ONTOLOGY: \n" + Helper.serializeTopEntity(state.getOntology()));
		
		state.refreshOntology();
		
		Map<Concept, Grounding> cacheGroundings = state.getCacheGroundings();
		
		for (Term term : anonymousConcepts) {
			if (term instanceof IRI) {
				IRI iri = (IRI)term;
//				logger.info(Radex.prettyPrint(state.getOntology()));
				
				Concept concept = state.getFactory().createConcept(iri);
				instances = state.getInstances(new UnnumberedAnonymousIDImpl(), concept);
				
				for (Instance instance : instances) {
//					Grounding grounding = state.getGroundingFromInstance(instance);
					Grounding groundingFromInstance = null;
					if(cacheGroundings.keySet().contains(concept)) {
						//  If the concept was previously encountered, but no grounding was found for it, write message    				
						groundingFromInstance = cacheGroundings.get(concept);
						logger.info("Instance " + instance.getIdentifier().toString() + " matches  " + concept.getIdentifier().toString());
						
					} else {
						// This is a new encountered concept, cache the grounding.
						groundingFromInstance = state.getGroundingFromInstance(instance);
						cacheGroundings.put(concept, groundingFromInstance);
					}
					g.put(instance, groundingFromInstance);
				}
			}
		}
	}

	private Map<Instance, Grounding> refresh(Map<Instance, Grounding> g) {
		Map<Instance, Grounding> refreshed = new HashMap<Instance, Grounding>();
		for (Entry<Instance, Grounding> e : g.entrySet()) {
			Instance old = e.getKey();
			Concept c;
			if (old.listConcepts().size() > 0) {
				c = (Concept) old.listConcepts().iterator().next();
				Instance fresh = provider.getState().getInstance(old.getIdentifier(), c);
				refreshed.put(fresh, e.getValue());
			} else {
				refreshed.put(old, e.getValue());
				logger.warn("Could not find grounded instance " + old +" in state, skipped grounding refresh.");
			}
		}
		return refreshed;
	}

	public InvocationLinker getProviderLinker() {
		return providerLinker;
	}

	public InvocationLinker getRequesterLinker() {
		return requesterLinker;
	}
	
	private Set<Entity> parse(String wsmlDocument) throws ComponentException {
        //instantiate to choreography enabled parser directly because the
        //factory creates the non-choreography aware parser
        org.wsmo.wsml.Parser parser = new ParserImpl(new HashMap<String, String>());        
        Entity[]  parsed = null;
        try {
            StringReader reader = new StringReader(wsmlDocument);
            parsed = parser.parse(reader);
        } catch (Exception e) {
        	throw new ComponentException("Parsing failed:" + e.getMessage(), e);
        }
        
        Set<Entity> set = new HashSet<Entity>();
        
        for (int i=0; i < parsed.length; i++) {
            set.add(parsed[i]);
        }  
        logger.debug("parsing finished");
        return set;
    }
	
	public Set<Molecule> getProviderMoleculesForInstance(String id) throws SynchronisationException, InvalidModelException {
		return provider.getState().getMoleculesForInstance(id);
	}
	}
