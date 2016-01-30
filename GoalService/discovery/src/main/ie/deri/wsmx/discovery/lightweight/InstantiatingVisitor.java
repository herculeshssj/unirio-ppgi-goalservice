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

import java.util.*;

import org.apache.log4j.*;
import org.omwg.logicalexpression.*;
import org.omwg.logicalexpression.Visitor;
import org.omwg.logicalexpression.terms.*;
import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.common.exception.*;
import org.wsmo.factory.*;

/**
 * Replaces variables with dummy instances
 *   
 * @author Adina Sirbu
 * 
 * @version $Revision: 1.5 $ $Date: 2007-01-08 14:45:11 $
 * @see org.omwg.logicalexpression.Visitor
 */
public class InstantiatingVisitor implements Visitor {
	
	protected static Logger logger = Logger.getLogger(InstantiatingVisitor.class);
	
	private WsmoFactory wsmoFactory;
	
	private LogicalExpressionFactory leFactory;
		
	private Stack<LogicalExpression> stack;
	
	private Map<Ontology, Map<Variable, Instance>> variableBinding; 
	
	private Set<Concept> classesOfObjects;
	
	private int instanceNr;
	
	private Ontology currentOntology;
	
	private Identifier defaultId;
	
	private int axiomNr;
	
	public InstantiatingVisitor(WsmoFactory wsmoFactory, 
			LogicalExpressionFactory leFactory,
			DataFactory dataFactory) {
		this.wsmoFactory = wsmoFactory;
		this.leFactory = leFactory;
		this.axiomNr = 0;
		reset();
	}
	
	private void reset() {
		stack = new Stack<LogicalExpression>();
		classesOfObjects = new HashSet<Concept>();
		variableBinding = new HashMap<Ontology, Map<Variable, Instance>>();
		currentOntology = null;
		instanceNr = 0;
	}
	
	/**
	 * Constructs an ontology with all the variables bounded to dummy instances
	 * @param inputOnt - input ontology
	 * @return Returns an ontology containing all disjunctive variants of the 
	 * input ontology, with variables bounded to dummy instances 
	 */
	public Ontology getProjection(Ontology inputOnt) {
		reset();
		
		String uri = "http://www.wsmx.org/discovery/ontology-" +
			Math.abs(Helper.getRandomLong());
		Ontology newOnt = wsmoFactory.createOntology(
			wsmoFactory.createIRI(uri + ".wsml"));
		newOnt.setDefaultNamespace(wsmoFactory.createIRI(uri));
		defaultId = newOnt.getDefaultNamespace().getIRI();
		
		currentOntology = inputOnt;
		try {
			for (Axiom axiom : (Set<Axiom>)currentOntology.listAxioms()) {
				Axiom newAxiom = createAxiom();
				Iterator exprs = axiom.listDefinitions().iterator();
				while (exprs.hasNext()) {
					LogicalExpression le = (LogicalExpression)exprs.next();
					le.accept(this);
					LogicalExpression outLe = stack.pop();
					newAxiom.addDefinition(outLe);
				}
				newOnt.addAxiom(newAxiom);
			}
//			for (Instance i : this.getInstances(currentOntology))
//				newOnt.addInstance(i);
		} catch (InvalidModelException e) {
			e.printStackTrace();
		}
		
		return newOnt;
	}
	
	/*
	 * Creates an axiom
	 */
	private Axiom createAxiom() {
		Identifier newAxiomId = wsmoFactory.createIRI("http://www.wsmx.org/discovery/axiom-"
					+ axiomNr + Math.abs(Helper.getRandomLong()));
		return wsmoFactory.createAxiom(newAxiomId);
	}
	
	/**
     * @param expr Atom
     */
	public void visitAtom(Atom expr) {
		logger.debug("Visiting atom " + expr.getIdentifier() + 
				"\n\t with params: " + expr.listParameters());
		
		Iterator it = expr.listParameters().iterator();
		List<Term> params = new ArrayList<Term>();
		while (it.hasNext()) {
			Term param = (Term)it.next();
			if (param instanceof Variable) {
				Instance instance = getInstance((Variable)param);
				params.add(instance.getIdentifier());
			} else
				params.add(param);
		}
		stack.push(leFactory.createAtom(
				expr.getIdentifier(), params));
	}
	
	/**
	 * @param expr AttributeConstraintMolecule
	 */
	public void visitAttributeContraintMolecule(AttributeConstraintMolecule expr) {
		stack.push(expr);
	}
	
	/**
	 * @param expr AttributeInferenceMolecule
	 */
	public void visitAttributeInferenceMolecule(AttributeInferenceMolecule expr) {
		stack.push(expr);
	}
	
	/**
	 * @param expr AttributeValueMolecule
	 */
	public void visitAttributeValueMolecule(AttributeValueMolecule expr) {
		Term right;
		Instance rInstance = null;
	
		if (expr.getRightParameter() instanceof Variable) {
			rInstance = getInstance((Variable) expr.getRightParameter());
			right = rInstance.getIdentifier();
		} else
			right = expr.getRightParameter();
		
		Term left;
		if (expr.getLeftParameter() instanceof Variable) {
			Instance lInstance = getInstance((Variable) expr.getLeftParameter());
			left = lInstance.getIdentifier();
			try {
				if (rInstance != null)
					lInstance.addAttributeValue((Identifier)expr.getAttribute(), rInstance);
//				else
//					lInstance.addAttributeValue((Identifier)expr.getAttribute(),
//							(Value)right);
			} catch (InvalidModelException e) {
				e.printStackTrace();
			}
		} else
			left = expr.getLeftParameter();
		
		stack.push(leFactory.createAttributeValue(left, expr.getAttribute(), right));
	}
	
	/**
	 * @param expr Binary with operator AND
	 */
	public void visitConjunction(Conjunction expr) {
		expr.getLeftOperand().accept(this);
		LogicalExpression newLeft = stack.pop();
		
		expr.getRightOperand().accept(this);
		LogicalExpression newRight = stack.pop();
		
		stack.push(leFactory.createConjunction(newLeft, newRight));
	}
	
	/**
	 * @param expr Binary with operator OR
	 */
	public void visitDisjunction(Disjunction expr) {
		expr.getLeftOperand().accept(this);
		LogicalExpression newLeft = stack.pop();
		
		expr.getRightOperand().accept(this);
		LogicalExpression newRight = stack.pop();
		
		stack.push(leFactory.createDisjunction(newLeft, newRight));
	}    

	/**
	 * @param expr CompoundMolecule
	 */
	public void visitCompoundMolecule(CompoundMolecule expr) {
		List<Molecule> newOperands = new ArrayList<Molecule>();
		Iterator i = expr.listOperands().iterator();
		
		while(i.hasNext()){
			((Molecule)i.next()).accept(this);
			newOperands.add((Molecule)stack.pop());
		}
		stack.push(leFactory.createCompoundMolecule(newOperands));
	}
		
	public void visitMemberShipMolecule(MembershipMolecule expr) {
//		logger.debug("Handling membership molecule with " +
//		"\n\tleft expr: " + expr.getLeftParameter() + 
//		"\n\tright expr: " + expr.getRightParameter());
		
		if ((expr.getLeftParameter() instanceof Variable) && 
				(expr.getRightParameter() instanceof Identifier)){
			
			Variable var = (Variable) expr.getLeftParameter();
			Instance instance = getInstance(var);
			
			Concept concept = wsmoFactory.getConcept(
					(Identifier) expr.getRightParameter()); 
			classesOfObjects.add(concept);
			
			try {
				instance.addConcept(concept);
				variableBinding.get(currentOntology).put(var, instance);
				stack.push(leFactory.createMemberShipMolecule(instance.getIdentifier(), 
						expr.getRightParameter()));
			} catch (InvalidModelException ex) {
				ex.printStackTrace();
			}
		} 
		else 
			stack.push(expr);
	}
	
	public void visitSubConceptMolecule(SubConceptMolecule expr) {
		stack.push(leFactory.createSubConceptMolecule(expr.getLeftParameter(), 
				expr.getRightParameter()));
	}
	
	public Set<Concept> getClassesOfObjects() {
		return classesOfObjects;
	}
	
	public Set<Instance> getInstances(Ontology ont) {
		return new HashSet<Instance>(variableBinding.get(ont).values());
	}
	
	private Instance getInstance(Variable variable) {
		Map<Variable, Instance> vars = variableBinding.get(currentOntology);
		if (vars == null)
			vars = new HashMap<Variable, Instance>();
		
		Instance instance = vars.get(variable);
		if (instance == null) {
			instance = wsmoFactory.createInstance(
					wsmoFactory.createIRI(defaultId + 
							"instance" + instanceNr++));
			vars.put((Variable)variable, instance);
			variableBinding.put(currentOntology, vars);
		}
		return instance;
	}
	
	/**
	 * @param expr Unary with operator NEG
	 * @see org.omwg.logicalexpression.Visitor#visitNegation(Negation)
	 */
	public void visitNegation(Negation expr) {
		throw new IllegalArgumentException("Classical negation is not allowed: " + expr);
	}
	
	/**
	 * @param expr Unary with operator NAF
	 * @see org.omwg.logicalexpression.Visitor#visitNegationAsFailure(NegationAsFailure)
	 */
	public void visitNegationAsFailure(NegationAsFailure expr) {
		expr.getOperand().accept(this);
	}
	
	/**
	 * @param expr Unary with operator CONSTRAINT
	 * @see org.omwg.logicalexpression.Visitor#visitConstraint(Constraint)
	 */
	public void visitConstraint(Constraint expr) {
		throw new IllegalArgumentException("Constraints are not allowed: " + expr);
	}
	
	/**
	 * @param expr Binary with operator IMPLIEDBY
	 * @see org.omwg.logicalexpression.Visitor#visitInverseImplication(InverseImplication)
	 */
	public void visitInverseImplication(InverseImplication expr) {
		expr.getLeftOperand().accept(this);
		LogicalExpression newLeft = stack.pop();
		
		expr.getRightOperand().accept(this);
		LogicalExpression newRight = stack.pop();
		
		stack.push(leFactory.createInverseImplication(newLeft, newRight));
	}
	
	/**
	 * @param expr Binary with operator IMPLIES
	 * @see org.omwg.logicalexpression.Visitor#visitImplication(Implication)
	 */
	public void visitImplication(Implication expr) {
		expr.getLeftOperand().accept(this);
		LogicalExpression newLeft = stack.pop();
		
		expr.getRightOperand().accept(this);
		LogicalExpression newRight = stack.pop();
		
		stack.push(leFactory.createImplication(newLeft, newRight));
	}
	
	/**
	 * @param expr Binary with operator EQUIVALENT
	 * @see org.omwg.logicalexpression.Visitor#visitEquivalence(Equivalence)
	 */
	public void visitEquivalence(Equivalence expr) {
		expr.getLeftOperand().accept(this);
		LogicalExpression newLeft = stack.pop();
		
		expr.getRightOperand().accept(this);
		LogicalExpression newRight = stack.pop();
		
		stack.push(leFactory.createEquivalence(newLeft, newRight));
	}
	
	/**
	 * @param expr Binary with operator IMPLIESLP
	 * @see org.omwg.logicalexpression.Visitor#visitLogicProgrammingRule(LogicProgrammingRule)
	 */
	public void visitLogicProgrammingRule(LogicProgrammingRule expr) {
		expr.getLeftOperand().accept(this);
		LogicalExpression newLeft = stack.pop();
		
		expr.getRightOperand().accept(this);
		LogicalExpression newRight = stack.pop();
		
		stack.push(leFactory.createLogicProgrammingRule(newLeft, newRight));
	}
	
	/**
	 * @param expr Quantified with operator EXISTS
	 * @see org.omwg.logicalexpression.Visitor#visitExistentialQuantification(ExistentialQuantification)
	 */
	public void visitExistentialQuantification(ExistentialQuantification expr) {
		stack.push(expr);
	}
	
	/**
	 * @param expr Quantified with operator FORALL
	 * @see org.omwg.logicalexpression.Visitor#visitUniversalQuantification(UniversalQuantification)
	 */
	public void visitUniversalQuantification(UniversalQuantification expr) {
		stack.push(expr);
	}
}