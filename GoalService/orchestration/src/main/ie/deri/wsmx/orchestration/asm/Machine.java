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

package ie.deri.wsmx.orchestration.asm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.Molecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Instance;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.component.DataMediator;
import org.wsmo.service.orchestration.rule.OrchestrationRules;
import org.wsmo.service.rule.Add;
import org.wsmo.service.rule.CompoundFact;
import org.wsmo.service.rule.Delete;
import org.wsmo.service.rule.MoleculeFact;
import org.wsmo.service.rule.Rule;
import org.wsmo.service.rule.Visitor;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.StateSignature;

/**
 * An ontologized abstract state machine,
 * which executes collections of rules.
 * 
 *
 * <pre>
 * Created on Dec 24, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/orchestration/src/main/ie/deri/wsmx/orchestration/asm/Machine.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.1 $ $Date: 2006-12-12 10:52:43 $
 */ 
public class Machine {
	
	protected static Logger logger = Logger.getLogger(Machine.class);
    private LogicalState state;
    Identifier id;
    OrchestrationRules rules;
    StateSignature signature;
    private Dialect activeDialect;
    private Map<Dialect, Class<? extends Visitor>> dialects = new HashMap<Dialect, Class<? extends Visitor>>();
    private Map<Class<? extends Rule>, Set<CompoundFact>> mostRecentUpdateSet;
    private GoalAchiever achiever;
    private MachineBasedInvoker invoker;
    private DataMediator mediator;
    private PerformanceAccumulator accumulator = new PerformanceAccumulator();
    
    public static enum Dialect {
    	DEFAULT,
    	USER_DEFINED_DIALECT_ALPHA,
    	USER_DEFINED_DIALECT_BETA
    }
    
    public Machine(Identifier id, StateSignature signature, OrchestrationRules rules) {
        super();
    	dialects.put(Dialect.DEFAULT, Executor.class);
    	activeDialect = Dialect.DEFAULT;
        //this.choreography = choreography;
    	this.rules = rules;
    	this.signature = signature;
    	this.id = id;
    	state = new LogicalState(signature);
        logger.debug("Initializing the machine state with controlstate.");
        state.initializeWithControlState();
    	for (Rule rule : rules.listRules()) {
    		rule.accept(accumulator);
    	}
    }

    public void insert(CompoundFact fact) {
        if (fact != null) 
        	state.add(fact);
    }

    public void insert(Set<CompoundFact> facts) {
        if (facts != null)
            state.addAll(facts);
    }

    private Set<Instance> freshInstances;
	private Map<IRI, Instance> performances = new HashMap<IRI, Instance>();
	
    public Map<Instance, Grounding> updateState(Set<Instance> instances)
    		throws UpdateFailedException, UpdateIllegalException, OutstandingInstancesException,
    			   OutstandingInstancesSuppliedException {
    	Map<Instance, Grounding> groundings = new HashMap<Instance, Grounding>();
    	freshInstances = instances;
    	for (Instance instance : instances) {
    		Grounding grounding = updateState(instance);
    		groundings.put(instance, grounding);
    	}
    	if (!outstandingMolecules.isEmpty()) {
    		outstandingMolecules.clear();
    		throw new OutstandingInstancesSuppliedException("All outstanding instances were supplied.");
    	}
    	return groundings;
    }
    
    public Grounding updateState(Instance instance) 
    		throws UpdateFailedException, UpdateIllegalException {
        if (instance != null)
			try {
				return state.add(instance);
			} catch (SynchronisationException e) {
				logger.warn("Instance update failed.", e);
				throw new UpdateFailedException("Instance update failed.", e);
			} catch (InvalidModelException e) {
				logger.warn("Instance update failed.", e);
				throw new UpdateFailedException("Instance update failed.", e);
			}
		return null;
    }
    
    Set<Molecule> outstandingMolecules = new HashSet<Molecule>();
    
    public Set<Instance> step() throws OutstandingInstancesException {
    	Class<? extends Visitor> executorClass = dialects.get(activeDialect);
    	Visitor executor;
		try {
			executor = executorClass.getConstructor(State.class, GoalAchiever.class, MachineBasedInvoker.class, DataMediator.class,
					Set.class, Map.class, PerformanceAccumulator.class).newInstance(
							new Object[]{state, achiever, invoker, mediator, freshInstances, performances, accumulator});
		} catch (Throwable t) {
			logger.warn("Failed to step.", t);
			return new HashSet<Instance>();
		}
    	for (Rule rule : rules.listRules()) {
    		logger.debug("Executing " + rule.toString());
    		rule.accept(executor);
    		logger.debug("Finished execution of " + rule.toString());
    	}
    	Executor e = (Executor) executor;
    	Map<Class<? extends Rule>, Set<CompoundFact>> updateSet = e.getUpdateSet();
    	logger.info("Applying update set(" + updateSet.size() + "): " + updateSet); 	
    	applyControlledSubset(updateSet);
    	mostRecentUpdateSet = updateSet;
    	Set<Instance> toSend = e.getToSend();
    	e.clearToSend();
    	return toSend;
    }
    
	private void applyControlledSubset(Map<Class<? extends Rule>, Set<CompoundFact>> updateSet) {
    	for (Entry<Class<? extends Rule>, Set<CompoundFact>> e : updateSet.entrySet()) {
    		//membership
    		for (CompoundFact cf : e.getValue()) {
    			MoleculeFact mf = (MoleculeFact)cf; //FIXME handle relation facts
	    		for (MembershipMolecule mm : mf.listMembershipMolecules()) {
	    			logger.debug("Checking for controlledness: " + mm);
	    			if (state.isControlledConcept(mm.getRightParameter())) {
	    				logger.debug("Is controlled: " + mm);
	    				if (e.getKey().equals(Add.class))
	    					state.add(mm);
	    				else if (e.getKey().equals(Delete.class))
	    					state.remove(mm);
	    			}
				}
    		}
    		//attribute values
    		for (CompoundFact cf : e.getValue()) {
    			MoleculeFact mf = (MoleculeFact)cf; //FIXME handle relation facts
	    		for (AttributeValueMolecule avm : mf.listAttributeValueMolecules()) {
	    			logger.debug("Checking for controlledness: " + avm);
//	    			if (state.isControlled(avm.getLeftParameter())) {
	        			logger.debug("Is controlled: " + avm);
	    				if (e.getKey().equals(Add.class))
	    					state.add(avm);
	    				else if (e.getKey().equals(Delete.class))
	    					state.remove(avm);
//	    			}
				}
    		}
		}
	}
	
	private Set<Molecule> determineOutstandingSubset(Map<Class<? extends Rule>, Set<CompoundFact>> updateSet) {
		Set<MembershipMolecule> outstandingMembershipSubset = new HashSet<MembershipMolecule>();
    	Set<AttributeValueMolecule> outstandingAttributeValueSubset = new HashSet<AttributeValueMolecule>();
    	for (Entry<Class<? extends Rule>, Set<CompoundFact>> e : updateSet.entrySet()) {
    		for (CompoundFact cf : e.getValue()) {
    			MoleculeFact mf = (MoleculeFact)cf; //FIXME handle relation facts
	    		for (MembershipMolecule mm : mf.listMembershipMolecules()) {
	    			logger.debug("Checking for outstanding subset: " + mm);
	    			if (state.isOutConcept(mm.getRightParameter())) {
	    				outstandingMembershipSubset.add(mm);
	    				logger.debug("Is outstanding: " + mm);
	    			}
				}
    		}
    		for (CompoundFact cf : e.getValue()) {
    			MoleculeFact mf = (MoleculeFact)cf; //FIXME handle relation facts
	    		for (AttributeValueMolecule avm : mf.listAttributeValueMolecules()) {
	    			logger.debug("Checking for outstanding subset: " + avm);
	    			if (state.isOut(avm.getLeftParameter())) {
	    				outstandingAttributeValueSubset.add(avm);
	        			logger.info("Is outstanding: " + avm);
	    			}
				}
    		}
		}    	
    	Set<Molecule> outstandingMolecules = new HashSet<Molecule>();
    	outstandingMolecules.addAll(outstandingAttributeValueSubset);
    	outstandingMolecules.addAll(outstandingMembershipSubset);
    	return outstandingMolecules;
	}
    
    public State getState() {
        return state;
    }

    public StateModificationBroadcaster getModificationBroadcaster() {
        return state;
    }
    
	public Dialect getActiveDialect() {
		return activeDialect;
	}

	public void setActiveDialect(Dialect activeDialect) {
		this.activeDialect = activeDialect;
	}

	public Set<Dialect> getAvailableDialects() {
		return dialects.keySet();
	}

	public void addUserDefinedDialect(Dialect dialect, Class<? extends Visitor> visitor) {
		dialects.put(dialect, visitor);
	}

	public Map<Class<? extends Rule>, Set<CompoundFact>> getMostRecentUpdateSet() {
		return mostRecentUpdateSet;
	}

	public GoalAchiever getAchiever() {
		return achiever;
	}

	public void setAchiever(GoalAchiever achiever) {
		this.achiever = achiever;
	}

	public Map<IRI, Instance> getPerformances() {
		return performances;
	}

	public DataMediator getMediator() {
		return mediator;
	}

	public void setMediator(DataMediator mediator) {
		this.mediator = mediator;
	}

	public MachineBasedInvoker getInvoker() {
		return invoker;
	}

	public void setInvoker(MachineBasedInvoker invoker) {
		this.invoker = invoker;
	}
	
}
