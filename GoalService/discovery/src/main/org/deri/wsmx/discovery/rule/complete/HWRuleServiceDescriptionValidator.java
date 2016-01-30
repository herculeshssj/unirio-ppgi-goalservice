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

package org.deri.wsmx.discovery.rule.complete;

import java.util.ArrayList;
import java.util.List;

import org.deri.wsmx.discovery.rule.SimpleLEVisitor;
import org.omwg.logicalexpression.*;


/**
 *   
 * @author Holger Lausen
 * @version $Revision: 1.3 $ $Date: 2007/09/25 08:46:11 $
 * @see org.omwg.logicalexpression.Visitor
 */
public class HWRuleServiceDescriptionValidator extends SimpleLEVisitor{
   
    @Override
    public void visitConstraint(Constraint expr) {
        errors.add("no constraints in postcondition allowed:"+ expr);
    }

    @Override
    public void visitEquivalence(Equivalence expr) {
        errors.add("no equivalence implication allowed:"+ expr);
    }

    @Override
    public void visitImplication(Implication expr) {
        errors.add("no implication allowed:"+ expr);
    }

    @Override
    public void visitInverseImplication(InverseImplication expr) {
        errors.add("no implication allowed:"+ expr);
    }

    @Override
    public void visitLogicProgrammingRule(LogicProgrammingRule expr) {
        errors.add("no LP Rules allowed:"+ expr);
    }

    @Override
    public void visitNegation(Negation expr) {
        errors.add("no negation allowed:"+ expr);
    }

    List<String> errors;
    
    public HWRuleServiceDescriptionValidator(List<String> errors){
        super(null);
        this.errors=errors;
        if (this.errors==null){
            errors = new ArrayList<String>();
        }
    }
    
    public List<String> getErrors(){
        return errors;
    }
    
    public void visitQuantification(Quantified expr){
        //allowed!
    }

    public void visitAtom(Atom expr) {
        //allowed!
    }
    
    public void visitAttributeMolecule(AttributeMolecule m){
        //allowed!
    }
    
    public void visitMolecule(Molecule m){
        //allowed!
    }
}