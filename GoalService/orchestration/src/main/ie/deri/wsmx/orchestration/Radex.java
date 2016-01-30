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

package ie.deri.wsmx.orchestration;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.io.parser.wsml.ParserImpl;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Entity;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;
import org.wsmo.service.WebService;
import org.wsmo.service.orchestration.Orchestration;
import org.wsmo.service.signature.GroundedMode;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.Mode;
import org.wsmo.service.signature.NotGroundedException;
import org.wsmo.service.signature.StateSignature;

import ie.deri.wsmx.orchestration.asm.Machine;
import ie.deri.wsmx.orchestration.asm.OutstandingInstancesException;
import ie.deri.wsmx.orchestration.asm.OutstandingInstancesSuppliedException;
import ie.deri.wsmx.orchestration.asm.UpdateFailedException;
import ie.deri.wsmx.orchestration.asm.UpdateIllegalException;

import ie.deri.wsmx.core.configuration.annotation.Exposed;
import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;
//
@WSMXComponent(name = "RadexOrchestration",
			   events =	"ORCHESTRATION",
		       description = "An orchestration engine based on abstract state machines.")
/**
 * Executes orchestrations. An orchestration based on
 * OASMs has to be registered as the first step,
 * after which multiple calls to <code>updateState</code>,
 * step-wise execute the orchestration by eavaluation
 * the conditions of the rule set and executing the rule's
 * conclusion if appropriate.
 *
 */
public class Radex {

	public static enum  Validation {
		STRICT,
		BUFFERING
	}
	
	protected static Logger logger = Logger.getLogger(Radex.class);
	private Machine machine;
	private InvocationLinker linker;
	ModificationHistory history = new ModificationHistory();
	private Orchestration orchestration;
	
	@Exposed(description = "Human-interface-friendly wrapper around the operation " +
			"that allows to pass in a webservice description to register the providers's " +
			"orchestration. Returns a human-readable confirmation or error description.")
	public String registerOrchestration(String service) {
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
				registerOrchestration(parsedService);
				machine.getModificationBroadcaster().registerListener(history);
			} catch (UnsupportedOperationException e) {
				logger.warn("Operation not supported.", e);
				return "Operation not supported: " + e.getMessage();
			} catch (ComponentException e) {
				logger.warn("Failure during registration.", e);
				return "Failure during registration: " + e.getMessage();
			}	
			return "Orchestration registered.";
		} catch (Throwable t) {
			logger.warn("Failed to register orchestration.", t);
			return "Failed to register orchestration: " + t.getMessage();
		}

	}
	
	@Exposed(description = "Human-interface-friendly wrapper around the operation " +
			"that allows to update the state. The first parameter is either a '<' or a " +
			"'>' which stands for provider to requester or requester to provider, respectively, indicating" +
			"the direction of the data flow.")
	public String updateState(String instances) {
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
			Map<Instance, Set<Grounding>> g = null;
			try {
				g = updateState(parsedInstances);
			} catch (UnsupportedOperationException e) {
				logger.warn("Operation not supported.", e);
				return "Operation not supported: " + e.getMessage();
			} catch (ComponentException e) {
				logger.warn("Failure during state update.", e);
				return "Failure during state update: " + e.getMessage();
			}
			String msg = "State updated.";
			if (machine == null)
				msg = msg + "\nOrchestration not registered, skipped provider update.";
			else
				msg = msg + "\nOrchestration stepped.";

			msg = msg + "\nGroundings: " + g;
			return msg;
		} catch (Throwable t) {
			logger.warn("State update failed", t);
			return "State update failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();			
		}
	}	

	@Exposed(description = "Returns a textual description of the machine state modification history.")
	public String getMachineHistory() {
		return history.getHistory();
	}

	/**
	 * Register a webservices orchestration description.
	 * 
	 * @param service
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public void registerOrchestration(WebService service) throws ComponentException, UnsupportedOperationException {
		logger.debug("Registering orchestration");
		if (service.listInterfaces().isEmpty())
			throw new ComponentException("Interface of web service does not specify an orchestration.");
		Interface i = (Interface) service.listInterfaces().iterator().next();
		orchestration = (Orchestration) i.getOrchestration();
		machine = new Machine(orchestration.getIdentifier(), orchestration.getStateSignature(), orchestration.getRules());
	}

	/**
	 * Convenience method for not container updates.
	 * 
	 * @param data
	 * @return
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 * @throws UpdateIllegalException
	 */
	public Set<Grounding> updateState(Instance data) 
			throws ComponentException, UnsupportedOperationException, UpdateIllegalException {
		logger.debug("Updating state with " + data);
		Set<Instance> set = new HashSet<Instance>();
		set.add(data);
		Map<Instance, Set<Grounding>> g = updateState(set);
		if (g.values().size() > 0)
			return g.values().iterator().next();
		return null;
	}
	
	private Map<Instance, Grounding> filterTriggers(Map<Instance, Grounding> g, Set<Instance> t) {
		Map<Instance, Grounding> r = new HashMap<Instance, Grounding>();
		for (Entry<Instance, Grounding> e: g.entrySet()) {
			for (Instance instance : t) {
				if (instance.getIdentifier().equals(e.getKey().getIdentifier()))
					r.put(e.getKey(), e.getValue());				
			}
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
	
	/**
	 * Takes input data, calls the stepper function of the underlying
	 * ASM and returns output if there is any. For the output 
	 * the grounding is determined and returned along with the instances.
	 * 
	 * @param data
	 * @return
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public Map<Instance, Set<Grounding>> updateState(Set<Instance> data) 
			throws ComponentException, UnsupportedOperationException {		
		Map<Instance, Grounding> g = null;
		Map<Instance, Set<Grounding>> triggers = null;
		try {
			if (machine == null)
				logger.warn("Orchestration was not registered, skipping provider update.");
			else {
				try {
					g = machine.updateState(data);
				} catch (OutstandingInstancesSuppliedException e) {
					return Collections.EMPTY_MAP;
				}
				Set<Instance> t = machine.step();
				triggers = findGrounding(orchestration.getStateSignature(), t);
			}
		} catch (UpdateFailedException e) {
			logger.warn("State update failed.", e);
			//TODO recovery
		} catch (UpdateIllegalException e) {
			logger.warn("State update illegal.", e);
		} catch (OutstandingInstancesException e) {
			logger.warn("Outstanding instances.", e);
		}
		return triggers;
	}

	private Map<Instance, Set<Grounding>> findGrounding(StateSignature s, Set<Instance> t) {
		Map<Instance, Set<Grounding>> r = new HashMap<Instance, Set<Grounding>>();
		for (Instance instance : t) {
			for (Mode mode : s) {
				if (mode.getConcept().getIdentifier().equals(instance.getIdentifier()))
					if (mode instanceof GroundedMode) {
						GroundedMode gm = (GroundedMode) mode;
						try {
							r.put(instance, gm.getGrounding());
							t.remove(instance);
						} catch (NotGroundedException e) {
							logger.warn("Attempted to send instance " + instance.getIdentifier() + " but is not grounded.");
						}
					} else {
						logger.warn("Attempted to send instance " + instance.getIdentifier() + " but is of ungrounded mode.");
					}
			}	
		}
		for (Instance instance : t) {
			logger.warn("Attempted to send instance " + instance.getIdentifier() + ", which is not found in grounded state signature. Assuming empty grounding and continuing.");
			r.put(instance, Collections.EMPTY_SET);
			t.remove(instance);			
		}
		return r;
	}

	private Map<Instance, Grounding> refresh(Map<Instance, Grounding> g) {
		Map<Instance, Grounding> refreshed = new HashMap<Instance, Grounding>();
		for (Entry<Instance, Grounding> e : g.entrySet()) {
			Instance old = e.getKey();
			Concept c;
			if (old.listConcepts().size() > 0) {
				c = (Concept) old.listConcepts().iterator().next();
				Instance fresh = machine.getState().getInstance(old.getIdentifier(), c);
				refreshed.put(fresh, e.getValue());
			} else {
				refreshed.put(old, e.getValue());
				logger.warn("Could not find grounded instance in state, skipped grounding refresh.");
			}
		}
		return refreshed;
	}

	public InvocationLinker getLinker() {
		return linker;
	}
	
	private Set<Entity> parse(String wsmlDocument) throws ComponentException {
        //instantiate to asm enabled parser directly because the
        //factory creates the non-asm aware parser
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

	public Machine getMachine() {
		return machine;
	}

	public void setMachine(Machine machine) {
		this.machine = machine;
	}

}
