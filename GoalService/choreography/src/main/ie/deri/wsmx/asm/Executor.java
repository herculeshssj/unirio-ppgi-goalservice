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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.choreography.ChoreographyFactoryRI;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.Constants;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.SimpleDataType;
import org.omwg.ontology.SimpleDataValue;
import org.omwg.ontology.Variable;
import org.omwg.ontology.WsmlDataType;
import org.wsml.reasoner.transformation.AnonymousIdTranslator;
import org.wsmo.common.IRI;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.factory.ChoreographyFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.choreography.rule.ChoreographyChoose;
import org.wsmo.service.choreography.rule.ChoreographyForAll;
import org.wsmo.service.choreography.rule.ChoreographyIfThen;
import org.wsmo.service.choreography.rule.ChoreographyPipedRules;
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
import org.wsmo.wsml.ParserException;

import com.ontotext.wsmo4j.ontology.SimpleDataValueImpl;

/**
 * Recursively executes rules.
 *
 * <pre>
 * Created on Aug 25, 2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/choreography/src/main/ie/deri/wsmx/asm/Executor.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.27 $ $Date: 2007-06-14 16:12:25 $
 */ 
public class Executor implements Visitor {

    protected static Logger logger = Logger.getLogger(Executor.class);

    private State state;
    private Map<Variable, Term> bindings = new HashMap<Variable, Term>();
    private Set<Term> triggers = new HashSet<Term>();
    private Map<Class<? extends Rule>, Set<CompoundFact>> updateSet = new HashMap<Class<? extends Rule>, Set<CompoundFact>>();
    private LogicalExpressionFactory leFactory = Factory.createLogicalExpressionFactory(null);
    private ChoreographyFactory cFactory = new ChoreographyFactoryRI();

	private AnonymousIdTranslator anonymousIDTranslator;
	WsmoFactory wsmoFactory;
	
	Set<ContextInterface> context;
	Context currentContext;
  
    public Executor(State state) {
        super();
        this.state = state;
		wsmoFactory = Factory.createWsmoFactory(null);
        anonymousIDTranslator = new AnonymousIdTranslator(wsmoFactory);
        context = new HashSet<ContextInterface>();
    }

    public void visitChoreographyIfThen(ChoreographyIfThen rule) {
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
       		String value = "";
       		Term eVal = e.getValue();
       		if(le.indexOf("?" + e.getKey().getName()) != -1) {
       			if(eVal instanceof IRI){
       				value = "_\"" + eVal.toString() + "\"";
       			} else {
       				if(eVal instanceof SimpleDataValue){
       					WsmlDataType type = ((SimpleDataValue)eVal).getType();
       					String iri = type.getIRI().toString();
       					if(iri.equals(WsmlDataType.WSML_STRING)) {
       						value = "\"" + eVal.toString() + "\"";
       					} else {
       						if(iri.equals(WsmlDataType.WSML_STRING)) {
       							value = "\"" + eVal.toString() + "\"";
       						} else {
       							if(iri.equals(WsmlDataType.WSML_DECIMAL) || iri.equals(WsmlDataType.WSML_INTEGER)) {
       								value = eVal.toString();
       							} else {
       								logger.error("Not treated value  " + eVal.toString() + " of type " + eVal.getClass() + " for variable " + e.getKey().getName(), new Exception());
       							}
       						}
       					}
       				} else {
       					logger.error("Not treated value  " + eVal.toString() + " of type " + eVal.getClass() + " for variable " + e.getKey().getName(), new Exception());
       				}
       			}
       			le = le.replaceAll("\\?" + e.getKey().getName(), value);
       		}
       		
       	}
        LogicalExpression bound;
        try {
			bound = leFactory.createLogicalExpression(le, state.getOntology());
		} catch (ParserException e1) {
			logger.warn("Failure during variable binding.");
			return;
		}
		//binding
//        try {
//			condition = cFactory.transitionRules.createConditionFromLogicalExpression(bound);
//		} catch (InvalidModelException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        logger.debug("After if-then rule binding: " + condition);
        if ( ((LogicalState)state).holds(bound)) {
//        if (state.holds(condition)) {
        	logger.debug("Condition holds: " + condition);
	        for (Rule nestedRule : rule.listNestedRules()) {
	        	nestedRule.accept(this);
			}
        } else 
        	logger.debug("Condition doesn't hold: " + condition);
        //unbinding
        condition = unboundCondition;       
    }
    
    public void visitChoreographyForAll(ChoreographyForAll rule) {
        Set<Map<Variable, Term>> retrievedTerms = state.retrieveBinding(rule.getCondition());
        for (Map<Variable, Term> binding : retrievedTerms) {
			for (Term term : binding.values()) {
				logger.info("add term " + term);
				triggers.add(term);
			}
		}
        
        if (retrievedTerms.size() == 0) {
        	logger.debug("No bindings for " + rule.getCondition());
        } 
        for (Map<Variable, Term> binding : retrievedTerms) {
        	logger.info("Firing " + rule.getCondition());
        	logger.debug("Activate bindings: " + binding);
        	this.bindings.putAll(binding);
        	currentContext = new Context(state);
        	        	
        	// TODO need method to clear the map of numbered annonymous ids
        	anonymousIDTranslator = new AnonymousIdTranslator(wsmoFactory);
        	
        	for (Rule nestedRule : rule.listNestedRules())
	            nestedRule.accept(this);
        	logger.debug("Passivate bindings: " + binding);
        	for (Term key : binding.keySet())
        		this.bindings.remove(key);
        	
        	context.add(currentContext);
        }
    }

    public void visitChoreographyChoose(ChoreographyChoose rule) {
    	Map<Class, Set<Variable>> variables = new HashMap<Class, Set<Variable>>();
    	variables.put(ChoreographyChoose.class, rule.listVariables());
        for (Rule nestedRule : rule.listNestedRules())
            nestedRule.accept(this);
    }
    
    public void visitAdd(Add rule) {
    	
    	logger.info("In visit ADD ");
    	CompoundFact compoundFact = rule.getFact();
		MoleculeFact mf = (MoleculeFact)compoundFact; //FIXME handle relation facts);
		
		
		/*
		 * Left translated term is stored for _# [.. hasValue ?x] memberOf ..
		 * Membership molecule gets the left term translated and the same identifier hould be used for attribute value molecules.
		 */
		Term leftTranslatedTerm = null;
		
		Set<MembershipMolecule> membershipMolecules = mf.listMembershipMolecules();
		Set<AttributeValueMolecule> attributeValueMolecules = mf.listAttributeValueMolecules();
		
		//FIXME handle more than first membershipmolecule
		MembershipMolecule membershipMolecule = null;
		
		boolean isOutput = false;
		if (membershipMolecules.size() > 0) {
	    	membershipMolecule = membershipMolecules.iterator().next();
	    	Term lhs = membershipMolecule.getLeftParameter();    	
	    	if (lhs.toString().startsWith(Constants.ANONYMOUS_ID_NOTATION)) {
	    		leftTranslatedTerm = anonymousIDTranslator.translate(lhs);
	    		if(lhs.toString().equals(Constants.ANONYMOUS_ID_NOTATION)) {
	    			isOutput = true;
	    		}
	    	}
		}
		
		if(leftTranslatedTerm != null) {
	    	Term rhs = membershipMolecule.getRightParameter();
	    	
	    	List<AttributeValueMolecule> avms = new ArrayList<AttributeValueMolecule>();
	    	List<Term> rhsAVMs = new ArrayList<Term>();
	    	if(attributeValueMolecules.size() > 0) { 
	    		for (Iterator iter = attributeValueMolecules.iterator(); iter.hasNext();) {
					AttributeValueMolecule avm = (AttributeValueMolecule) iter.next();
					avms.add(avm);
					Term rhsTerm = avm.getRightParameter();
					if (rhsTerm.toString().startsWith(Constants.ANONYMOUS_ID_NOTATION)) {
			    		Term translatedTerm = anonymousIDTranslator.translate(rhsTerm);
			    		avm.setRightOperand(translatedTerm);
			    	}
					rhsAVMs.add(rhsTerm);
					
				}
	    	}
	        logger.info("In visitAdd membershipMolecule: Before add-rule binding: " + rule);
	        Term term;
	        for (Variable v : bindings.keySet()) {
	        	term = bindings.get(v);
	        	logger.info("BINDING-VALUE : " + term.toString());
	        	if (rhs instanceof Variable && ((Variable)rhs).equals(v)) {
	        		membershipMolecule.setRightOperand(term);
	        	}
	        	for (int i = 0; i < rhsAVMs.size(); i++) {
	        		Term rhsAVM = (Term) rhsAVMs.get(i);
	        		if (rhsAVM != null && rhsAVM instanceof Variable && ((Variable)rhsAVM).equals(v)) {
	        			AttributeValueMolecule avm = (AttributeValueMolecule) avms.get(i);
	        			avm.setRightOperand(term);
	        		}
				}
	        }
	        
	        currentContext.addTerm(leftTranslatedTerm, rhs, isOutput);
	        
	        logger.info("In visitAdd membershipMolecule: After add-rule binding: " + rule);
	        try {
	        	Set<CompoundFact> cfs = new HashSet<CompoundFact>();
	        	logger.info("fact " + rule.getFact());
	        	
	        	cfs.add(state.copy(rule.getFact(), leftTranslatedTerm));
	        	put(Add.class, cfs);
			} catch (ParserException e) {
				logger.info("In visitAdd membershipMolecule: Could not copy fact for addition to state.", e);
			}
	        membershipMolecule.setRightOperand(rhs);
	        if(avms.size() > 0) {
	        	for (int i = 0; i < avms.size(); i++) {
					AttributeValueMolecule avm = (AttributeValueMolecule) avms.get(i);
					Term rhsAVM = (Term) rhsAVMs.get(i);
					avm.setRightOperand(rhsAVM);
				}
	        }
	        
		} else {			
			if (membershipMolecules.size() > 0) {
				Term lhs = membershipMolecule.getLeftParameter();
				Term rhs = membershipMolecule.getRightParameter();
				logger.info("In visitAdd membershipMolecule: Before add-rule binding: " + rule);
				Term term;
				for (Variable v : bindings.keySet()) {
					term = bindings.get(v);
					logger.info("BINDING-VALUE : " + term.toString());
					if (lhs instanceof Variable && 
							((Variable)lhs).equals(v))
						membershipMolecule.setLeftOperand(term);
					if (rhs instanceof Variable && ((Variable)rhs).equals(v))
						membershipMolecule.setRightOperand(term);
				}
				logger.info("In visitAdd membershipMolecule: After add-rule binding: " + rule);
				try {
					Set<CompoundFact> cfs = new HashSet<CompoundFact>();
					logger.info("fact " + rule.getFact());
					
					cfs.add(state.copy(rule.getFact()));
					put(Add.class, cfs);
				} catch (ParserException e) {
					logger.info("In visitAdd membershipMolecule: Could not copy fact for addition to state.", e);
				}
				membershipMolecule.setLeftOperand(lhs);
				membershipMolecule.setRightOperand(rhs);
			}
			//FIXME handle more than first avm
			if (attributeValueMolecules.size() > 0) {
				for (AttributeValueMolecule avm : attributeValueMolecules) {
					
					Term lhs = avm.getLeftParameter();
					
					Term rhs = avm.getRightParameter();
					logger.info("In visitAdd AVM: Before add-rule binding: " + rule);
					Term term;
					for (Variable v : bindings.keySet()) {
						term = bindings.get(v);
						if (lhs instanceof Variable && ((Variable)lhs).equals(v))
							avm.setLeftOperand(term);
						if (rhs instanceof Variable && ((Variable)rhs).equals(v))
							avm.setRightOperand(term);
					}
					logger.info("In visitAdd AVM: After add-rule binding: " + rule);
					try {
						Set<CompoundFact> cfs = new HashSet<CompoundFact>();
						cfs.add(state.copy(rule.getFact()));
						
						
//					Term bind = bindings.get(lhs);
						
//					state.getOntology().findInstance(
						put(Add.class, cfs);
					} catch (ParserException e) {
						logger.info("In visitAdd AVM: Could not copy fact for addition to state.", e);
					}
					avm.setLeftOperand(lhs);
					avm.setRightOperand(rhs);
				}
			}
		}
		
		
    }
    
    
    private void put(Class<? extends Rule> c, Set<CompoundFact> cfs) {
    	if (updateSet.get(c) == null)
    		updateSet.put(c, cfs);
    	Set<CompoundFact> t = updateSet.get(c);
    	
    	t.addAll(cfs);
    	logger.info("put in update set " + c + " \nfacts " + t);
    	
    	updateSet.put(c, t);    	
    }

    public void visitDelete(Delete rule) {
    	logger.info("In visit DELETE ");
    	CompoundFact compoundFact = rule.getFact();
		MoleculeFact mf = (MoleculeFact)compoundFact; //FIXME handle relation facts);
		
    	//FIXME handle more than first membershipmolecule
    	if (mf.listMembershipMolecules().size() > 0) {
	    	MembershipMolecule membershipMolecule = mf.listMembershipMolecules().iterator().next();
	    	Term lhs = membershipMolecule.getLeftParameter();
	    	Term rhs = membershipMolecule.getRightParameter();
	        logger.debug("Before delete-rule binding: " + rule);
	        Term term;
	        for (Variable v : bindings.keySet()) {
	        	term = bindings.get(v);
	        	if (lhs instanceof Variable && ((Variable)lhs).equals(v))
	        		membershipMolecule.setLeftOperand(term);
	        	if (rhs instanceof Variable && ((Variable)rhs).equals(v))
	        		membershipMolecule.setRightOperand(term);
	        }
	        logger.debug("After delete-rule binding: " + rule);
	        try {
	        	Set<CompoundFact> cfs = new HashSet<CompoundFact>();
	        	cfs.add(state.copy(rule.getFact()));
	        	put(Delete.class, cfs);
			} catch (ParserException e) {
				logger.fatal("Could not copy fact for deletion from state.", e);
			}
	        membershipMolecule.setLeftOperand(lhs);
	        membershipMolecule.setRightOperand(rhs);
    	}
    	//FIXME handle more than first avm
    	if (mf.listAttributeValueMolecules().size() > 0) {
	    	AttributeValueMolecule avm = mf.listAttributeValueMolecules().iterator().next();
	    	Term lhs = avm.getLeftParameter();
	    	Term rhs = avm.getRightParameter();
	        logger.debug("Before delete-rule binding: " + rule);
	        Term term;
	        for (Variable v : bindings.keySet()) {
	        	term = bindings.get(v);
	        	if (lhs instanceof Variable && ((Variable)lhs).equals(v))
	        		avm.setLeftOperand(term);
	        	if (rhs instanceof Variable && ((Variable)rhs).equals(v))
	        		avm.setRightOperand(term);
	        }
	        logger.debug("After delete-rule binding: " + rule);
	        try {
	        	Set<CompoundFact> cfs = new HashSet<CompoundFact>();
	        	cfs.add(state.copy(rule.getFact()));
	        	put(Delete.class, cfs);
			} catch (ParserException e) {
				logger.fatal("Could not copy fact for deletion from state.", e);
			}
	        avm.setLeftOperand(lhs);
	        avm.setRightOperand(rhs);
    	}                
    }

    public void visitUpdate(Update rule) {
        state.remove(rule.getOldFact());
        state.add(rule.getNewFact());
    }

	public void visitCondition(Condition arg0) {
	}

	public void visitCompoundFact(CompoundFact arg0) {
	}

	public Set<Term> getTriggers() {
		return triggers;
	}

	public Map<Class<? extends Rule>, Set<CompoundFact>> getUpdateSet() {
		return updateSet;
	}

	public void visitChoreographyPipedRules(ChoreographyPipedRules rules) {
		// TODO handle piped rules
	}

	/**
	 * @return The list of contexts created by the rules that fired. Each context contains the in and out terms for the corresponding rule.
	 */
    public Set<ContextInterface> getContextForStep(){
    	return context;
    }



	// TODO implement
	public void visitOrchestrationAchieveGoal(OrchestrationAchieveGoal arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitOrchestrationApplyMediation(OrchestrationApplyMediation arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitOrchestrationChoose(OrchestrationChoose arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitOrchestrationForAll(OrchestrationForAll arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitOrchestrationIfThen(OrchestrationIfThen arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitOrchestrationInvokeService(OrchestrationInvokeService arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitOrchestrationPipedRules(OrchestrationPipedRules arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitReceive(Receive arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitSend(Send arg0) {
		// TODO Auto-generated method stub
		
	}
}
