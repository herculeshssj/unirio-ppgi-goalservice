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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.choreography.ChoreographyFactoryRI;
import org.deri.wsmo4j.orchestration.OrchestrationFactoryRI;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Variable;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.component.DataMediator;
import org.wsmo.factory.ChoreographyFactory;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.OrchestrationFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;
import org.wsmo.service.choreography.rule.ChoreographyChoose;
import org.wsmo.service.choreography.rule.ChoreographyForAll;
import org.wsmo.service.choreography.rule.ChoreographyIfThen;
import org.wsmo.service.choreography.rule.ChoreographyPipedRules;
import org.wsmo.service.orchestration.PpMediator;
import org.wsmo.service.orchestration.rule.OrchestrationAchieveGoal;
import org.wsmo.service.orchestration.rule.OrchestrationApplyMediation;
import org.wsmo.service.orchestration.rule.OrchestrationChoose;
import org.wsmo.service.orchestration.rule.OrchestrationForAll;
import org.wsmo.service.orchestration.rule.OrchestrationIfThen;
import org.wsmo.service.orchestration.rule.OrchestrationInvokeService;
import org.wsmo.service.orchestration.rule.OrchestrationPipedRules;
import org.wsmo.service.orchestration.rule.Receive;
import org.wsmo.service.orchestration.rule.Send;
import org.wsmo.service.rule.Add;
import org.wsmo.service.rule.CompoundFact;
import org.wsmo.service.rule.Condition;
import org.wsmo.service.rule.Delete;
import org.wsmo.service.rule.MoleculeFact;
import org.wsmo.service.rule.Rule;
import org.wsmo.service.rule.Update;
import org.wsmo.service.rule.Visitor;
import org.wsmo.service.signature.GroundedMode;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.Mode;
import org.wsmo.service.signature.NotGroundedException;
import org.wsmo.service.signature.WSDLGrounding;
import org.wsmo.wsml.ParserException;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

/**
 * Recursively executes rules.
 *
 * <pre>
 * Created on Aug 25, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/orchestration/src/main/ie/deri/wsmx/orchestration/asm/Executor.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.2 $ $Date: 2007-02-05 16:12:56 $
 */ 
public class Executor implements Visitor {

    protected static Logger logger = Logger.getLogger(Executor.class);

    private State state;
    private Map<Variable, Term> bindings = new HashMap<Variable, Term>();
    private Set<Term> triggers = new HashSet<Term>();
    private Map<Class<? extends Rule>, Set<CompoundFact>> updateSet = new HashMap<Class<? extends Rule>, Set<CompoundFact>>();
    private LogicalExpressionFactory leFactory = Factory.createLogicalExpressionFactory(null);
    private OrchestrationFactory cFactory = new OrchestrationFactoryRI();
    private GoalAchiever achiever;
    private DataMediator mediator;
	private Map<IRI, Instance> performances = new HashMap<IRI, Instance>();
    private Set<Instance> freshInstances;
    private PerformanceAccumulator accumulator;
    private MachineBasedInvoker invoker;
	protected WsmoFactory factory;
	protected OrchestrationFactory oFactory;
	protected DataFactory dataFactory;
	private Set<Instance> toSend = new HashSet<Instance>();
	private boolean strict = false;
	
    public Executor(State state, GoalAchiever achiver, MachineBasedInvoker invoker, DataMediator mediator, Set<Instance> instances,
    		Map<IRI, Instance> performances, PerformanceAccumulator accumulator) {
        super();
        this.state = state;
        this.achiever = achiver;
        this.invoker = invoker;
        this.mediator = mediator;
        this.freshInstances = instances;
        this.performances = performances;
        this.accumulator = accumulator;
        setupFactories();
    }

	@SuppressWarnings("unchecked")
	private void setupFactories() {
		leFactory = Factory.createLogicalExpressionFactory(null);
		factory = Factory.createWsmoFactory(null);
		oFactory = new OrchestrationFactoryRI();
		dataFactory = Factory.createDataFactory(null);
	}
	
    public void visitOrchestrationIfThen(OrchestrationIfThen rule) {
    	Condition condition = rule.getCondition();
        if (bindings.isEmpty()) {
	        if (state.holds(condition))
	            for (Rule nestedRule : rule.listNestedRules())
	                nestedRule.accept(this);
	        return;
        }
        logger.debug("Before if-then rule binding: " + condition);
        Condition unboundCondition = condition;
        String le = new String(condition.getRestrictedLogicalExpression().toString(state.getOntology()));
        Entry[] sorted = bindings.entrySet().toArray(new Entry[0]);
        Arrays.sort(sorted, new Comparator<Entry>(){
        		public int compare(Entry e0, Entry e1) {
        			Variable v0 = (Variable)e0.getKey();
        			Variable v1 = (Variable)e1.getKey();
        			int l0 = v0.getName().length();
        			int l1 = v1.getName().length();
        			if (l0 < l1 )
        				return -1;
        			else if (l0 > l1)
        				return 1;
        			return 0;
        		}
        	}
        );
        List<Entry<Variable, Term>> lengthSortedBindings = new ArrayList<Entry<Variable,Term>>();
        for (int i = 0; i < sorted.length; i++) {
        	//add in front and shift everything right so that we get the variable
        	//with the most character in front
            lengthSortedBindings.add(0, sorted[i]);		
		}
       	for (Entry<Variable, Term> e : lengthSortedBindings) {
	        le = le.replaceAll("\\?" + e.getKey().getName(), e.getValue().toString());
       	}
        LogicalExpression bound;
        try {
			bound = leFactory.createLogicalExpression(le, state.getOntology());
		} catch (ParserException e1) {
			logger.warn("Failure during variable binding.");
			return;
		}
		//binding
        try {
			condition = cFactory.transitionRules.createConditionFromLogicalExpression(bound);
		} catch (InvalidModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        logger.debug("After if-then rule binding: " + condition);
        if (state.holds(condition)) {
        	logger.debug("Condition holds: " + condition);
	        for (Rule nestedRule : rule.listNestedRules()) {
	        	nestedRule.accept(this);
			}
        } else 
        	logger.debug("Condition doesn't hold: " + condition);
        //unbinding
        condition = unboundCondition;       
    }
    
    public void visitOrchestrationForAll(OrchestrationForAll rule) {
        Set<Map<Variable, Term>> retrievedTerms = state.retrieveBinding(rule.getCondition());
        for (Map<Variable, Term> binding : retrievedTerms) {
			for (Term term : binding.values()) {
				triggers.add(term);
			}
		}
        if (retrievedTerms.size() == 0)
        	logger.debug("No bindings for " + rule.getCondition());
        for (Map<Variable, Term> binding : retrievedTerms) {
        	logger.info("Firing " + rule.getCondition());
        	logger.debug("Activate bindings: " + binding);
        	this.bindings.putAll(binding);
        	for (Rule nestedRule : rule.listNestedRules())
	            nestedRule.accept(this);
        	logger.debug("Passivate bindings: " + binding);
        	for (Term key : binding.keySet())
        		this.bindings.remove(key);
        }
    }

    public void visitOrchestrationChoose(OrchestrationChoose rule) {
    	Map<Class, Set<Variable>> variables = new HashMap<Class, Set<Variable>>();
    	variables.put(OrchestrationChoose.class, rule.listVariables());
        for (Rule nestedRule : rule.listNestedRules())
        	nestedRule.accept(this);
    }
    
    public void visitAdd(Add rule) {
    	String ruleString = rule.toString();
    	CompoundFact compoundFact = rule.getFact();
		addFact(compoundFact, ruleString);
    }

	private void addFact(CompoundFact compoundFact, String ruleString) {
		MoleculeFact mf = (MoleculeFact)compoundFact; 
    	if (mf.listMembershipMolecules().size() > 0) {
	    	MembershipMolecule membershipMolecule = mf.listMembershipMolecules().iterator().next();
	    	Term lhs = membershipMolecule.getLeftParameter();
	    	Term rhs = membershipMolecule.getRightParameter();
	        logger.debug("Before add-rule M molecule binding: " + ruleString);
	        Term term;
	        for (Variable v : bindings.keySet()) {
	        	term = bindings.get(v);
	        	if (lhs instanceof Variable && ((Variable)lhs).equals(v))
	        		membershipMolecule.setLeftOperand(term);
	        	if (rhs instanceof Variable && ((Variable)rhs).equals(v))
	        		membershipMolecule.setRightOperand(term);
	        }
	        logger.debug("After add-rule M molecule binding: " + ruleString);
	        try {
	        	Set<CompoundFact> cfs = new HashSet<CompoundFact>();
	        	cfs.add(state.copy(compoundFact));
	        	put(Add.class, cfs);
			} catch (ParserException e) {
				logger.fatal("Could not copy fact for addition to state.", e);
			}
	        membershipMolecule.setLeftOperand(lhs);
	        membershipMolecule.setRightOperand(rhs);
    	}
    	if (mf.listAttributeValueMolecules().size() > 0) {
	    	AttributeValueMolecule avm = mf.listAttributeValueMolecules().iterator().next();
	    	Term lhs = avm.getLeftParameter();
	    	Term rhs = avm.getRightParameter();
	        logger.debug("Before add-rule AV molecule binding: " + ruleString);
	        Term term;
	        for (Variable v : bindings.keySet()) {
	        	term = bindings.get(v);
	        	if (lhs instanceof Variable && ((Variable)lhs).equals(v))
	        		avm.setLeftOperand(term);
	        	if (rhs instanceof Variable && ((Variable)rhs).equals(v))
	        		avm.setRightOperand(term);
	        }
	        logger.debug("After add-rule AV molecule binding: " + ruleString);
	        try {
	        	Set<CompoundFact> cfs = new HashSet<CompoundFact>();
	        	cfs.add(state.copy(compoundFact));
	        	put(Add.class, cfs);
			} catch (ParserException e) {
				logger.fatal("Could not copy fact for addition to state.", e);
			}
	        avm.setLeftOperand(lhs);
	        avm.setRightOperand(rhs);
    	}
	}
    
    private void put(Class<? extends Rule> c, Set<CompoundFact> cfs) {
    	if (updateSet.get(c) == null)
    		updateSet.put(c, cfs);
    	Set<CompoundFact> t = updateSet.get(c);
    	t.addAll(cfs);
    	updateSet.put(c, t);    	
    }

    public void visitDelete(Delete rule) {
    	CompoundFact compoundFact = rule.getFact();
    	String ruleString = rule.toString();
    	deleteFact(compoundFact, ruleString);                
    }

	private void deleteFact(CompoundFact compoundFact, String ruleString) {
		MoleculeFact mf = (MoleculeFact)compoundFact;
    	if (mf.listMembershipMolecules().size() > 0) {
	    	MembershipMolecule membershipMolecule = mf.listMembershipMolecules().iterator().next();
	    	Term lhs = membershipMolecule.getLeftParameter();
	    	Term rhs = membershipMolecule.getRightParameter();
	        logger.debug("Before delete-rule M moleculebinding: " + ruleString);
	        Term term;
	        for (Variable v : bindings.keySet()) {
	        	term = bindings.get(v);
	        	if (lhs instanceof Variable && ((Variable)lhs).equals(v))
	        		membershipMolecule.setLeftOperand(term);
	        	if (rhs instanceof Variable && ((Variable)rhs).equals(v))
	        		membershipMolecule.setRightOperand(term);
	        }
	        logger.debug("After delete-rule M molecule binding: " + ruleString);
	        try {
	        	Set<CompoundFact> cfs = new HashSet<CompoundFact>();
	        	cfs.add(state.copy(compoundFact));
	        	put(Delete.class, cfs);
			} catch (ParserException e) {
				logger.fatal("Could not copy fact for deletion from state.", e);
			}
	        membershipMolecule.setLeftOperand(lhs);
	        membershipMolecule.setRightOperand(rhs);
    	}
    	if (mf.listAttributeValueMolecules().size() > 0) {
	    	AttributeValueMolecule avm = mf.listAttributeValueMolecules().iterator().next();
	    	Term lhs = avm.getLeftParameter();
	    	Term rhs = avm.getRightParameter();
	        logger.debug("Before delete-rule AV molecule binding: " + ruleString);
	        Term term;
	        for (Variable v : bindings.keySet()) {
	        	term = bindings.get(v);
	        	if (lhs instanceof Variable && ((Variable)lhs).equals(v))
	        		avm.setLeftOperand(term);
	        	if (rhs instanceof Variable && ((Variable)rhs).equals(v))
	        		avm.setRightOperand(term);
	        }
	        logger.debug("After delete-rule AV molecule binding: " + ruleString);
	        try {
	        	Set<CompoundFact> cfs = new HashSet<CompoundFact>();
	        	cfs.add(state.copy(compoundFact));
	        	put(Delete.class, cfs);
			} catch (ParserException e) {
				logger.fatal("Could not copy fact for deletion from state.", e);
			}
	        avm.setLeftOperand(lhs);
	        avm.setRightOperand(rhs);
    	}
	}

    public void visitUpdate(Update rule) {
    	deleteFact(rule.getOldFact(), rule.toString());
    	addFact(rule.getNewFact(), rule.toString());
    }

	public Set<Term> getTriggers() {
		return triggers;
	}

	public Map<Class<? extends Rule>, Set<CompoundFact>> getUpdateSet() {
		return updateSet;
	}

	public void visitOrchestrationPipedRules(OrchestrationPipedRules rules) {
		logger.debug("Pipedrule");
		Set<Rule> ruleset = rules.listPipedRules();
		for (Rule rule : ruleset) {
			rule.accept(this);			
		}
	}

	public void visitChoreographyChoose(ChoreographyChoose rule) {
		logger.warn("Choreography rule in orchestration.");
	}

	public void visitChoreographyForAll(ChoreographyForAll rule) {
		logger.warn("Choreography rule in orchestration.");
	}

	public void visitChoreographyIfThen(ChoreographyIfThen rule) {
		logger.warn("Choreography rule in orchestration.");
	}

	public void visitChoreographyPipedRules(ChoreographyPipedRules rules) {
		logger.warn("Choreography rule in orchestration.");
	}

	public void visitOrchestrationAchieveGoal(OrchestrationAchieveGoal rule) {
		logger.debug("Executing: " + rule);
		List<Instance> instances = achiever.achieve(rule.getGoal());
		if (instances.size() < 1)
			logger.warn("Failed to achieve goal, goal resolution returned no instances.");		
		if (instances.size() > 1)
			logger.warn("During goal achievement: goal resolution returned more than one instance, ignoring all except first one.");		
		performances.put(rule.getPerformIRI(), instances.get(0));
	}

	public void visitOrchestrationApplyMediation(OrchestrationApplyMediation rule) {
		logger.debug("Executing: " + rule);
		Instance instance = performances.get(rule.getPpMediator().listSources().iterator().next());

		if (rule.getPpMediator().getMediationService() == null) { //use local mediator via SEE API
			logger.debug("Mediation via local mediator.");
			Ontology sourceOntology = instance.getOntology();
			Ontology targetOntology = findOntologyOfMediationTarget(rule.getPpMediator());
			Instance trgt = null;
			List<Entity> mediated;
			try {
				mediated = mediator.mediate(sourceOntology, targetOntology, instance);
				for (Entity entity : mediated) {
					if (entity instanceof Instance)
						trgt = (Instance) entity;
				}
				if (trgt == null)
					throw new Exception();
			} catch (Exception e) {
				logger.fatal("Failed to mediate.",e);
				//TODO rethrow
			}
			performances.put(rule.getPpMediator().getTarget(), trgt);
		} else if (isGoal(rule.getPpMediator().getMediationService())) { //use mediator via goal
			logger.debug("Mediation via goal achievement for: " + rule.getPpMediator().getMediationService());
			List<Instance> instances = achiever.achieve((Goal)factory.getGoal(rule.getPpMediator().getMediationService()));
			if (instances.size() < 1)
				logger.warn("Failed to mediate, goal resolution returned no instances.");		
			if (instances.size() > 1)
				logger.warn("During PP mediation: goal resolution returned more than one instance, ignoring all except first one.");
	        logger.debug("Associating with mediators target performance " + rule.getPpMediator().getTarget());
			performances.put(rule.getPpMediator().getTarget(), instances.get(0));
		} else if (isService(rule.getPpMediator().getMediationService())) { //use mediator via service	
			logger.warn("Mediation via service discarded.");		
		} else 
			logger.warn("Unexpeceted usesService usage in PpMediator. Either goal, service or nonexistant.");		
	}

	private boolean isGoal(IRI iri) {
		Goal goal = factory.getGoal(iri);
		if(goal != null && goal instanceof Goal)
			return true;
		return false;
	}

	private boolean isService(IRI iri) {
		WebService service = factory.getWebService(iri);
		if(service != null && service instanceof WebService)
			return true;
		return false;
	}
	
	private Ontology findOntologyOfMediationTarget(PpMediator m) {
		m.getTarget();
		Set<OrchestrationInvokeService> invokes = accumulator.getInvokes();
		for (OrchestrationInvokeService invoke : invokes) {
			//TODO pick the service from the accumulator
			//this is only needed for a semantic that supports
			//*local* mediators that are not accessing via a goal indirection
		}
		return null; 
	}

	public void visitOrchestrationInvokeService(OrchestrationInvokeService rule) {
		logger.debug("Executing: " + rule);
		WebService service = rule.getService();
		Instance request = performances.get(rule.getPerformIRI());
        logger.debug("Invoking service with: " + rule.getService());
		Instance response = invoker.invoke(service, request);
        logger.debug("Service response: " + response.getIdentifier());
        logger.debug("Associating with performance " + rule.getPerformIRI());
		performances.put(rule.getPerformIRI(), response);
	}

	public void visitReceive(Receive rule) {
		logger.debug("Executing: " + rule);
		for (Instance i : freshInstances) {
			for (Concept c : (Collection<Concept>)i.listConcepts()) {
				if (c.getIdentifier().equals(rule.getSourceIRI()))
			        logger.debug("Associating with performance " + rule.getPerformIRI());
					performances.put(rule.getPerformIRI(), i);
			}	
		}
		if (!performances.containsKey(rule.getPerformIRI())) {
			logger.fatal("Expected to receive an instance but didn't receive one.");
			//TODO throw new UpdateIllegalException("Expected to receive an instance but didn't receive one.");
		}
	}

	public void visitSend(Send rule) {
		logger.debug("Executing: " + rule);
		IRI performIRI = rule.getPerformIRI();
		Instance instance = performances.get(performIRI);
		if (instance == null) {
			logger.warn("Attempted to send instance under performance " + performIRI + ", but performance is not associated to any instances.");
			return;
		}
		if (strict && !checkWitinMode(instance, rule.getTargetIRI())) {
			logger.warn("Attempted to send instance " + instance.getIdentifier() + ", but it is not of the specified mode.");		
		} else {
			logger.info("Sending instance " + instance.getIdentifier() + " under performance " + performIRI);
			toSend.add(instance);
			performances.put(rule.getPerformIRI(), instance);
		}
	}
	
	
	private boolean checkWitinMode(Instance instance, IRI mode) {
		for (Concept c : (Collection<Concept>)instance.listConcepts()) {
			if (mode.equals(c.getIdentifier()))
					return true;	
		}
		return false;
	}

	public Set<Instance> getToSend() {
		return new HashSet<Instance>(toSend);
	}

	public void clearToSend() {
		toSend.clear();
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
}
