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

import java.util.*;

import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.factory.Factory;

import com.ontotext.wsmo4j.common.TopEntityImpl;

/**
 * <pre>
 * Committed by $Author: $
 * $Source: $,
 * </pre>
 *
 * @author Nathalie Steinmetz, STI Innsbruck
 *
 * @version $Revision: $ $Date: $
 */
public class HWRuleServiceDescription {
    private Set<Variable> variables;
    private LogicalExpression le;
    private Set<Ontology> imports;
    private TopEntity defaultNS =null;
    
    public HWRuleServiceDescription(
            Set<Ontology> imports,
            Set<Variable> variables,
            LogicalExpression le ){
        this.variables=variables;
        this.le=le;
        this.imports=imports;
    }
    
    /*
     * just for pretty printing...
     */
    public void setDefaultNS(String ns){
    	defaultNS = new TopEntityImpl(
    			Factory.createWsmoFactory(null).createIRI("http://foo#topEntity"));
    	defaultNS.setDefaultNamespace(Factory.createWsmoFactory(null).createIRI(ns));
    }
    
    public Set<Variable> getVariables(){
        return variables;
    }

    public LogicalExpression getExpressions(){
        return le;
    }
    
    public void replaceExpressions(LogicalExpression le) {
    	this.le = le;
    }
    
    public Set<Ontology> listOntologies(){
        return imports;
    }
    
    public String toString(){
        return le.toString(defaultNS);
    }
    
}
