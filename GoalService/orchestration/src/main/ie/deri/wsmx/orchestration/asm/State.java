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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Variable;
import org.wsmo.common.Identifier;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.rule.CompoundFact;
import org.wsmo.service.rule.Condition;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.StateSignature;
import org.wsmo.wsml.ParserException;

/**
 * Keeps track of the current state of an machine instance.
 * May supply an ontology for feeding it to a reasoner.
 *
 * <pre>
 * Created on Dec 24, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/orchestration/src/main/ie/deri/wsmx/orchestration/asm/State.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.1 $ $Date: 2006-12-12 10:52:43 $
 */ 
public interface State {

	public abstract void add(CompoundFact fact);
	public abstract void addAll(Collection<CompoundFact> facts);
	public abstract void add(MembershipMolecule fact);
	public abstract void add(AttributeValueMolecule fact);
	public abstract void remove(CompoundFact fact);
	
	public abstract Grounding add(Instance instance) 
		throws SynchronisationException, InvalidModelException, UpdateIllegalException;	
	
	public abstract Ontology getOntology();
	public abstract boolean holds(Condition condition);
    public abstract Set<Map<Variable, Term>> retrieveBinding(Condition condition);  
    public CompoundFact copy(CompoundFact fact) throws ParserException;
	public Instance getInstance(Identifier id, Concept concept);
	public WsmoFactory getFactory();
	public StateSignature getSignature();

}