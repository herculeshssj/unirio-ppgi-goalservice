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

import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.factory.Factory;

import com.ontotext.wsmo4j.common.*;

/**
 * <pre>
 * Created on 01.12.2006
 * Committed by $Author: holgerlausen $
 * $Source: /cvsroot/wsmx/components/discovery/src/main/org/deri/wsmx/discovery/rule/LWRuleServiceDescription.java,v $,
 * </pre>
 *
 * @author Holger Lausen
 *
 * @version $Revision: 1.3 $ $Date: 2007/09/25 08:46:12 $
 */
public class LWRuleServiceDescription {
    private Variable v;
    private Set<Variable> variables;
    private LogicalExpression le;
    private Set<Ontology> imports;
    private TopEntity defaultNS =null;
    
    public LWRuleServiceDescription(
            Set<Ontology> imports,
            Variable v,
            LogicalExpression le ){
        this.v=v;
        this.le=le;
        this.imports=imports;
    }
    
    public LWRuleServiceDescription(
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
    
    public Variable getVariable(){
        return v;
    }

    public Set<Variable> getVariables() {
    	return variables;
    }
    
    public LogicalExpression getExpressions(){
        return le;
    }
    
    public Set<Ontology> listOntologies(){
        return imports;
    }
    
    public String toString(){
        return le.toString(defaultNS);
    }
    
}
