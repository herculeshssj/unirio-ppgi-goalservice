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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.Molecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Axiom;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Variable;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.component.ChoreographyEngine.Direction;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.rule.CompoundFact;
import org.wsmo.service.rule.Condition;
import org.wsmo.service.signature.Grounding;
import org.wsmo.wsml.ParserException;

/**
 * Keeps track of the current state of an machine instance.
 * May supply an ontology for feeding it to a reasoner.
 *
 * <pre>
 * Created on Dec 24, 2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/choreography/src/main/ie/deri/wsmx/asm/State.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.14 $ $Date: 2007-06-14 16:12:25 $
 */ 
public interface State {

	public abstract void add(CompoundFact fact);
	public abstract void addAll(Collection<CompoundFact> facts);
	public abstract void add(MembershipMolecule fact);
	public abstract void add(AttributeValueMolecule fact);
	public abstract void remove(CompoundFact fact);
	
	public abstract Grounding add(Direction direction, Instance instance) 
		throws SynchronisationException, InvalidModelException, UpdateIllegalException;	
	
	public abstract Ontology getOntology();
	public abstract boolean holds(Condition condition);
    public abstract Set<Map<Variable, Term>> retrieveBinding(Condition condition);  
    public CompoundFact copy(CompoundFact fact) throws ParserException;
    /**
     * 
     * @param fact The fact to be copied.
     * @param leftTranslatedTerm It is either a unique id in case the fact is '_# [..] memnerOf ..' or null.
     * @return a copy of the fact where _# is replaced by a unique id
     * @throws ParserException
     */
    public CompoundFact copy(CompoundFact fact, Term leftTranslatedTerm) throws ParserException;
	public Instance getInstance(Identifier id, Concept concept);
	public Instance getInstance(Identifier id, IRI concept);
	public Set<Instance> getInstances(Identifier id, Concept concept) throws SynchronisationException, InvalidModelException;
	public Set<Instance> getInstances(Identifier id, IRI concept) throws SynchronisationException, InvalidModelException;
	public Grounding getGroundingFromInstance(Instance instance) throws UpdateIllegalException;
	void registerListener(StateModificationListener listener);
	void refreshOntology();
	Map<Concept, Grounding> getCacheGroundings();
	WsmoFactory getFactory();
	boolean isInConcept(Term concept);
	boolean isOutConcept(Term concept);
	Set<Molecule> getMoleculesForInstance(String id) throws SynchronisationException, InvalidModelException;
	Map<Molecule, Axiom> getAxioms();

		
}