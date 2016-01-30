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

import java.util.HashSet;
import java.util.Set;

import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.terms.*;
import org.omwg.ontology.*;
import org.wsmo.common.IRI;
import org.wsmo.common.UnnumberedAnonymousID;


/**
 *   
 * @author Holger Lausen
 * @version $Revision: 1.3 $ $Date: 2007/09/25 08:46:11 $
 * @see org.omwg.logicalexpression.Visitor
 */
public class VariableCollector implements Visitor{
    
    private Set<Variable> s = new HashSet<Variable>();
    
    public void visitComplexDataValue(ComplexDataValue t) {
        for (int i=0; i<t.getArity();i++){
            Term term = t.getArgumentValue((byte)i);
            if (term instanceof Variable){
                s.add((Variable) term);
            }
        }
    }

    public void visitConstructedTerm(ConstructedTerm t) {
        for (int i=0; i<t.getArity();i++){
            Term term = t.getParameter(i);
            if (term instanceof Variable){
                s.add((Variable) term);
            }
        }
    }

    public void visitIRI(IRI t) {}

    public void visitNumberedID(NumberedAnonymousID t) {}

    public void visitSimpleDataValue(SimpleDataValue t) {}

    public void visitUnnumberedID(UnnumberedAnonymousID t) {}

    public void visitVariable(Variable t) {
        s.add(t);
    }
    
    public static Set<Variable>getVariables(LogicalExpression le){
        VariableCollector vcol = new VariableCollector();
        SimpleLEVisitor lev = new SimpleLEVisitor(vcol);
        le.accept(lev);
        return vcol.s;
    }
}