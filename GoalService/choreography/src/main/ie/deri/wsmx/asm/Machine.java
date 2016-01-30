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

package ie.deri.wsmx.asm;

import java.util.Collection;
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
import org.wsmo.common.Identifier;
import org.wsmo.common.UnnumberedAnonymousID;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.component.ChoreographyEngine.Direction;
import org.wsmo.service.choreography.rule.ChoreographyRules;
import org.wsmo.service.rule.Add;
import org.wsmo.service.rule.CompoundFact;
import org.wsmo.service.rule.Delete;
import org.wsmo.service.rule.MoleculeFact;
import org.wsmo.service.rule.Rule;
import org.wsmo.service.rule.Visitor;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.StateSignature;

/**
 * An ontologized abstract state machine, which executes collections of rules.
 * 
 *
 * <pre>
 * Created on Dec 24, 2005
 * Committed by $Author: maciejzaremba $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.28 $ $Date: 2007-06-14 16:12:25 $
 */ 
public class Machine {
	static int stepNo = 0;
	protected static Logger logger = Logger.getLogger(Machine.class);
    private LogicalState state;
//    private Choreography choreography;    
    Identifier id;
    ChoreographyRules rules;
    StateSignature signature;
    private Dialect activeDialect;
    private Map<Dialect, Class<? extends Visitor>> dialects = new HashMap<Dialect, Class<? extends Visitor>>();
    private Map<Class<? extends Rule>, Set<CompoundFact>> mostRecentUpdateSet;
    
    public static enum Dialect {
    	DEFAULT,
    	USER_DEFINED_DIALECT_ALPHA,
    	USER_DEFINED_DIALECT_BETA
    }
    
    public Machine(Identifier id, StateSignature signature, ChoreographyRules rules) {
        dialects.put(Dialect.DEFAULT, Executor.class);
        activeDialect = Dialect.DEFAULT;
        //this.choreography = choreography;
    	this.rules = rules;
    	this.signature = signature;
    	this.id = id;
    	state = new LogicalState(signature);
        logger.debug("Initializing the machine state with controlstate.");
        state.initializeWithControlState();
        logger.info("Init machine 4");
    }

    public void insert(CompoundFact fact) {
        if (fact != null) 
        	state.add(fact);
    }

    public void insert(Set<CompoundFact> facts) {
        if (facts != null)
            state.addAll(facts);
    }

    public Map<Instance, Grounding> updateState(Direction direction, Set<Instance> instances)
    		throws UpdateFailedException, UpdateIllegalException, OutstandingInstancesException,
    			   OutstandingInstancesSuppliedException {
    	Map<Instance, Grounding> groundings = new HashMap<Instance, Grounding>();

    	try {
			state.add(direction, instances, groundings);
		} catch (SynchronisationException e) {
			logger.warn("Instance update failed.", e);
			throw new UpdateFailedException("Instance update failed.", e);
		} catch (InvalidModelException e) {
			logger.warn("Instance update failed.", e);
			throw new UpdateFailedException("Instance update failed.", e);
		}
    	
//    	for (Instance instance : instances) {
//    		try {
//    			Grounding grounding = updateState(direction, instance);
//    			groundings.put(instance, grounding);
//    		}
//    		catch (UpdateIllegalException e) {
//    			logger.info("Machine found no matching inputs/outputs for " + 
//    					instance.getIdentifier().toString() + " with concept " + 
//    					((Concept) instance.listConcepts().iterator().next()).getIdentifier().toString());
//    		}
//    	}

//    	outstandingMolecules = new HashSet<Molecule>(); //FIXME
    	if (!outstandingMolecules.isEmpty()) {
    		outstandingMolecules.clear();
//    		for (Molecule molecule : outstandingMolecules) {
//   				logger.debug("Checking against whitelisted " + molecule);
//    			if (!state.holds(molecule)) FIXME LE rewriting needed here
//    				throw new OutstandingInstancesException("Instances outstanding that satisfy " + molecule);
//   				logger.debug("updateState() whitelisted against " + molecule);
//   				outstandingMolecules.remove(molecule);
//    		}
    		throw new OutstandingInstancesSuppliedException("All outstanding instances were supplied.");
    	}
    	return groundings;
    }
    
    public Grounding updateState(Direction direction, Instance instance) 
    		throws UpdateFailedException, UpdateIllegalException {
        if (instance != null)
			try {
				return state.add(direction, instance);
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
    
    public Set<Term> step() throws OutstandingInstancesException {
    	logger.info("STEP " + (++stepNo));
    	
    	
    	if (!outstandingMolecules.isEmpty()) {
    		//FIXME not concurrent
    		throw new OutstandingInstancesException("Can't step, there are instances outstanding: " + outstandingMolecules);
    	}
    	Class<? extends Visitor> executorClass = dialects.get(activeDialect);
    	Visitor executor;
		try {
			executor = executorClass.getConstructor(State.class).newInstance(new Object[]{state});
		} catch (Throwable t) {
			logger.warn("Failed to step.", t);
			return new HashSet<Term>();
		}
    	for (Rule rule : rules.listRules()) {
    		logger.info("Executing " + rule.toString());
    		rule.accept(executor);
    		logger.info("Finished execution of " + rule.toString());
    	}
    	//FIXME this type assumption breaks the dialect support
    	Executor e = (Executor) executor;
    	Map<Class<? extends Rule>, Set<CompoundFact>> updateSet = e.getUpdateSet();
    	logger.info("Applying update set(" + updateSet.size() + "): " + updateSet); 	

    	outstandingMolecules = applyToUpdateSet(updateSet);
    	
    	mostRecentUpdateSet = updateSet;
    	
    	anonymousConcepts.clear();
    	
    	Collection<Set<CompoundFact>> facts = updateSet.values();
    	Set<Term> triggers = e.getTriggers();
    	for (Set<CompoundFact> compundFact : facts) {
    		for (CompoundFact fact : compundFact) {
    			if (fact instanceof MoleculeFact) {
    				MoleculeFact molecule = (MoleculeFact)fact;
    				Set<MembershipMolecule> mms = molecule.listMembershipMolecules();
    				if(mms.size() > 0){
    					for (MembershipMolecule mm : mms) {
        					Term leftParameter = mm.getLeftParameter();
        					   					
        					if (leftParameter instanceof UnnumberedAnonymousID 
        							// TODO
        							|| leftParameter.toString().startsWith("http://www.wsmo.org/reasoner/anonymous")) {
        						Term concept = mm.getRightParameter();
        						if (state.isInConcept(mm.getRightParameter())) {
        							triggers.add(leftParameter);
        							anonymousConcepts.add(concept);
        						}
        					}
    					}
    				} else {
    					Set<AttributeValueMolecule> attributeValueMolecules = molecule.listAttributeValueMolecules();
    					for (AttributeValueMolecule avm : attributeValueMolecules) {
        					Term leftParameter = avm.getLeftParameter();
        					   					
        					if (leftParameter instanceof UnnumberedAnonymousID 
        							// TODO
        							|| leftParameter.toString().startsWith("http://www.wsmo.org/reasoner/anonymous")) {
        						
        						// Find the axiom that defines the concept of this instance
        						Set<Molecule> molecules = state.getAxioms().keySet();
        						for (Molecule ml : molecules) {
									if(ml instanceof MembershipMolecule && ml.getLeftParameter().equals(leftParameter)){
										Term concept = ml.getRightParameter();
										if (state.isInConcept(concept)) {
											triggers.add(leftParameter);
											anonymousConcepts.add(concept);
										}
									}
								}
        					}
    					}
    				}
    				
    			}
			}		
		}
    	
    	context = e.getContextForStep();
    	
		return triggers;
    }

    Set<ContextInterface> context;
    Set<Term> anonymousConcepts = new HashSet<Term>();
    
    private Set<Molecule> applyToUpdateSet(Map<Class<? extends Rule>, Set<CompoundFact>> updateSet) {
    	Set<MembershipMolecule> outstandingMembershipSubset = new HashSet<MembershipMolecule>();
    	Set<AttributeValueMolecule> outstandingAttributeValueSubset = new HashSet<AttributeValueMolecule>();

    	for (Entry<Class<? extends Rule>, Set<CompoundFact>> e : updateSet.entrySet()) {
    		//membership
    		for (CompoundFact cf : e.getValue()) {
    			boolean isOut = false;
    			MoleculeFact mf = (MoleculeFact)cf; //FIXME handle relation facts
    			for (MembershipMolecule mm : mf.listMembershipMolecules()) {
    				if (
    						mm.getLeftParameter() instanceof UnnumberedAnonymousID || 
    						mm.getLeftParameter().toString().startsWith("http://www.wsmo.org/reasoner/anonymous_") ||
    						state.isControlledConcept(mm.getRightParameter())) {
    					if (e.getKey().equals(Add.class))
    						state.add(mm);
    					else if (e.getKey().equals(Delete.class))
    						state.remove(mm);
    				}
    				if (state.isOutConcept(mm.getRightParameter())) {
    					isOut = true;
    					outstandingMembershipSubset.add(mm);
    					logger.debug("Is outstanding: " + mm);
    				}
    			}
    			//attribute values
    			for (AttributeValueMolecule avm : mf.listAttributeValueMolecules()) {
    				if (e.getKey().equals(Add.class))
    					state.add(avm);
    				else if (e.getKey().equals(Delete.class))
    					state.remove(avm);


    				if((mf.listMembershipMolecules().size()>0)) {
    					if(isOut) {
    						outstandingAttributeValueSubset.add(avm);
    						logger.info("Is outstanding: " + avm);
    					}
    				} else if (state.isOut(avm.getLeftParameter())) {
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
    
    
	private void applyAnonymousSubset(Map<Class<? extends Rule>, Set<CompoundFact>> updateSet) {
    	for (Entry<Class<? extends Rule>, Set<CompoundFact>> e : updateSet.entrySet()) {
    		//membership
    		for (CompoundFact cf : e.getValue()) {
    			MoleculeFact mf = (MoleculeFact)cf; //FIXME handle relation facts
	    		for (MembershipMolecule mm : mf.listMembershipMolecules()) {
	    			logger.debug("Checking for anonymity: " + mm);
	    			if (mm.getLeftParameter() instanceof UnnumberedAnonymousID || mm.getLeftParameter().toString().startsWith("http://www.wsmo.org/reasoner/anonymous_")) {
	    				logger.debug("Is anonymous: " + mm);
	    				if (e.getKey().equals(Add.class))
	    					state.add(mm);
	    				else if (e.getKey().equals(Delete.class))
	    					state.remove(mm);
	    			}
				}
	    		//attribute values
	    		for (AttributeValueMolecule avm : mf.listAttributeValueMolecules()) {
	    			logger.debug("Checking for anonymity: " + avm);
	    			if (avm.getLeftParameter() instanceof UnnumberedAnonymousID || avm.getLeftParameter().toString().startsWith("http://www.wsmo.org/reasoner/anonymous_")) {
	    				logger.debug("Is anonymous: " + avm);
	    				if (e.getKey().equals(Add.class))
	    					state.add(avm);
	    				else if (e.getKey().equals(Delete.class))
	    					state.remove(avm);
	    			}
				}
    		}
		}
	}

    
	private void applyControlledSubset(Map<Class<? extends Rule>, Set<CompoundFact>> updateSet) {
    	for (Entry<Class<? extends Rule>, Set<CompoundFact>> e : updateSet.entrySet()) {
    		//membership
    		for (CompoundFact cf : e.getValue()) {
    			MoleculeFact mf = (MoleculeFact)cf; //FIXME handle relation facts
	    		for (MembershipMolecule mm : mf.listMembershipMolecules()) {
	    			logger.debug("Checking for controlledness: " + mm);
	    			if ((! mm.getLeftParameter().toString().startsWith("http://www.wsmo.org/reasoner/anonymous_")) && state.isControlledConcept(mm.getRightParameter())) {
	    				logger.debug("Is controlled: " + mm);
	    				if (e.getKey().equals(Add.class))
	    					state.add(mm);
	    				else if (e.getKey().equals(Delete.class))
	    					state.remove(mm);
	    			}
				}
	    		//attribute values
	    		for (AttributeValueMolecule avm : mf.listAttributeValueMolecules()) {
	    			logger.debug("Checking for controlledness: " + avm);
//	    			if (state.isControlled(avm.getLeftParameter())) {
	    			logger.debug("Is controlled: " + avm);
	    			if ((!avm.getLeftParameter().toString().startsWith("http://www.wsmo.org/reasoner/anonymous_")) && e.getKey().equals(Add.class))
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

	public Set<Term> getAnonymousConcepts() {
		return anonymousConcepts;
	}

	public void setAnonymousConcepts(Set<Term> anonymousConcepts) {
		this.anonymousConcepts = anonymousConcepts;
	}

	/**
	 * @return the context for the rules.
	 */
	public Set<ContextInterface> getContextAfterStep() {
		return context;
	}
	public Set<Molecule> getOutstandingMolecules(){
		return outstandingMolecules;
	}
}
