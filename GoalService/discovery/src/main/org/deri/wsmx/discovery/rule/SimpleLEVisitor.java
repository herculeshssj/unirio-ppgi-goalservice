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

package org.deri.wsmx.discovery.rule;

import java.util.List;
import java.util.Set;

import org.omwg.logicalexpression.*;
import org.omwg.ontology.Variable;

/**
 * Default left to right depth first walker...
 *   
 * @author Holger Lausen
 * @version $Revision: 1.3 $ $Date: 2007/09/25 08:46:12 $
 * @see org.omwg.logicalexpression.Visitor
 */
public class SimpleLEVisitor implements Visitor {
    
    org.omwg.logicalexpression.terms.Visitor termvisitor;
    
    public SimpleLEVisitor(org.omwg.logicalexpression.terms.Visitor termvisitor){
        this.termvisitor=termvisitor;
    }
    
    public void visitQuantification(Quantified expr){
        Set<Variable> s = expr.listVariables();
        for (Variable v:s){
            v.accept(termvisitor);
        }
        visitUnary(expr);
    }

    public void visitAtom(Atom expr) {
        expr.getIdentifier().accept(termvisitor);
        for (int i=0;i<expr.getArity();i++){
            expr.getParameter(i).accept(termvisitor);
        }
    }
    
    public void visitAttributeMolecule(AttributeMolecule m){
        visitMolecule(m);
        m.getAttribute().accept(termvisitor);
    }
    
    public void visitMolecule(Molecule m){
        m.getLeftParameter().accept(termvisitor);
        m.getRightParameter().accept(termvisitor);
    }

    public void visitBinary(Binary expr){
        expr.getLeftOperand().accept(this);
        expr.getRightOperand().accept(this);
    }

    public void visitUnary(Unary expr){
        expr.getOperand().accept(this);
    }

    //thos below are only the specialized that we do not have to worry about
    
    public void visitAttributeContraintMolecule(AttributeConstraintMolecule expr) {
        visitAttributeMolecule(expr);
    }

    public void visitAttributeInferenceMolecule(AttributeInferenceMolecule expr) {
        visitAttributeMolecule(expr);
    }

    public void visitAttributeValueMolecule(AttributeValueMolecule expr) {
        visitAttributeMolecule(expr);
    }

    public void visitCompoundMolecule(CompoundMolecule expr) {
        List<LogicalExpression> list = (List<LogicalExpression>) expr.listOperands();
        for (LogicalExpression m: list){
            visitMolecule((Molecule)m);
        }
    }

    public void visitConjunction(Conjunction expr) {
        visitBinary(expr);
    }

    public void visitConstraint(Constraint expr) {
        visitUnary(expr);
    }
    
    public void visitDisjunction(Disjunction expr) {
        visitBinary(expr);
    }

    public void visitEquivalence(Equivalence expr) {
        visitBinary(expr);
    }
    
    public void visitExistentialQuantification(ExistentialQuantification expr) {
        visitUnary(expr);
    }

    public void visitImplication(Implication expr) {
        visitBinary(expr);
    }

    public void visitInverseImplication(InverseImplication expr) {
        visitBinary(expr);
    }

    public void visitLogicProgrammingRule(LogicProgrammingRule expr) {
        visitBinary(expr);
    }

    public void visitMemberShipMolecule(MembershipMolecule expr) {
        visitMolecule(expr);
    }

    public void visitNegation(Negation expr) {
        visitUnary(expr);
    }

    public void visitNegationAsFailure(NegationAsFailure expr) {
        visitUnary(expr);
    }

    public void visitSubConceptMolecule(SubConceptMolecule expr) {
        visitMolecule(expr);
    }

    public void visitUniversalQuantification(UniversalQuantification expr) {
        visitUnary(expr);
    }
}