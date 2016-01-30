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

import java.util.*;

import org.omwg.logicalexpression.*;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.logicalexpression.terms.Visitor;
import org.omwg.ontology.Variable;

import com.ontotext.wsmo4j.common.IRIImpl;


/**
 *   
 * @author Holger Lausen
 * @version $Revision: 1.3 $ $Date: 2007/09/25 08:46:12 $
 * @see org.omwg.logicalexpression.Visitor
 */
public class VariableTermReplacer extends SimpleLEVisitor{
    Variable toReplace;
    Term newTerm;
    Visitor termVisitor;
    Map<Variable,Term> otherVariablesBeeingSubstituted = new HashMap<Variable,Term>();
    
    public VariableTermReplacer(Variable toReplace, Term newTerm){
        super(null);
        this.toReplace=toReplace;
        this.newTerm=newTerm;
    }
    
    public void visitQuantification(Quantified expr){
        Set<Variable> s = expr.listVariables();
        for (Variable v:s){
            if (v.equals(toReplace)){
                s.remove(toReplace);
            }
        }
        visitUnary(expr);
    }

    public void visitAtom(Atom expr) {
        List<Term> params = expr.listParameters();
        List<Term> newParams = new ArrayList<Term>();
        for (Term t: params){
            if(t instanceof Variable){
                newParams.add(replace((Variable)t));
            }else{
                newParams.add(t);
            }
        }
        expr.setParameters(newParams);
    }
    
    public void visitAttributeMolecule(AttributeMolecule m){
        visitMolecule(m);
        if (m.getAttribute()instanceof Variable){
            m.setAttribute(replace((Variable)m.getAttribute()));
        }
    }
    
    public void visitMolecule(Molecule m){
        if (m.getLeftParameter() instanceof Variable){
            m.setLeftOperand(replace((Variable)m.getLeftParameter()));
        }
        if (m.getRightParameter() instanceof Variable){
            m.setRightOperand(replace((Variable)m.getRightParameter()));
        }
    }
    
    Term replace(Variable var) {
    	Term term;
    	if (var.equals(toReplace)) {
    		term=newTerm;
    	}else {
	    	term = otherVariablesBeeingSubstituted.get(var);
	    	if (term==null) {
	    		//FIXME pass factory
	    		term = new IRIImpl("http://boesby.de#unieq"+uniq++);
	    		otherVariablesBeeingSubstituted.put(var, term);
	    	}
    	}
    	System.out.println("for var "+var+" returnning "+term);
    	return term;
    }
    
    static int uniq = 0;
}