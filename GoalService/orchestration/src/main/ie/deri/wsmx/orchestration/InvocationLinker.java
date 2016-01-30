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

package ie.deri.wsmx.orchestration;

import ie.deri.wsmx.orchestration.asm.State;
import ie.deri.wsmx.orchestration.asm.StateModificationListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.choreography.ChoreographyFactoryRI;
import org.omwg.logicalexpression.AttributeMolecule;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsml.reasoner.api.WSMLReasoner;
import org.wsml.reasoner.api.WSMLReasonerFactory;
import org.wsml.reasoner.api.inconsistency.InconsistencyException;
import org.wsml.reasoner.impl.DefaultWSMLReasonerFactory;
import org.wsml.reasoner.impl.WSMO4JManager;
import org.wsml.reasoner.transformation.AxiomatizationNormalizer;
import org.wsmo.common.IRI;
import org.wsmo.factory.ChoreographyFactory;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.signature.GroundedMode;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.Mode;
import org.wsmo.service.signature.NotGroundedException;
import org.wsmo.service.signature.StateSignature;
import org.wsmo.wsml.Parser;

/**
 * Link to the <code>Invoker</code>.
 *
 * <pre>
 * Created on Dec 24, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/orchestration/src/main/ie/deri/wsmx/orchestration/InvocationLinker.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.3 $ $Date: 2006-12-12 10:52:44 $
 */ 
public class InvocationLinker implements StateModificationListener {

	protected static Logger logger = Logger.getLogger(InvocationLinker.class);

    protected WsmoFactory factory;
    protected LogicalExpressionFactory leFactory;
    protected ChoreographyFactory cFactory;
	protected DataFactory dataFactory;
	
	private final StateSignature signature;
    protected Map<String, Object> c = new HashMap<String, Object>();
	protected WSMLReasoner reasoner = null;
	//	WSMLReasonerFactory.BuiltInReasoner.MINS);
//	protected AxiomatizationNormalizer normalizer = new AxiomatizationNormalizer(new WSMO4JManager());

//	protected WSMLReasoner reasoner = DefaultWSMLReasonerFactory.getFactory().getWSMLFlightReasoner(
//			WSMLReasonerFactory.BuiltInReasoner.MINS);

	Map<Instance, Set<Grounding>> updates = new HashMap<Instance, Set<Grounding>>();

	public InvocationLinker(StateSignature signature) {
		super();
		reasoner = DefaultWSMLReasonerFactory.getFactory().createWSMLFlightReasoner(c);
		setupFactories();
		this.signature = signature;
	}

	private void setupFactories() {
		leFactory = Factory.createLogicalExpressionFactory(null);
		factory = Factory.createWsmoFactory(null);
		cFactory = new ChoreographyFactoryRI();
		dataFactory = Factory.createDataFactory(null);
	}

	public void addedAttribute(State state, AttributeMolecule attribute) {
		//no action
	}

	public void addedMembership(State state, MembershipMolecule membership) {
		Instance modifiedInstance ;
		try {
			modifiedInstance = retrieveModifiedInstance(state, membership);
		} catch (NoMatchingInstanceException e) {
			logger.warn("The modified instance could not be retrieved.", e);
			return;
		}
		logger.debug("The instance to be passed to the invoker is " + modifiedInstance.getIdentifier());

		//TODO remove IRI limitation
		IRI concept;
		try {
			concept = (IRI)membership.getRightParameter();		
		} catch (ClassCastException  e) {
			logger.warn("Only IRIs as concept identifier are supported.", e);
			return;
		}

		Set<Grounding> grounding;
		try {
			grounding = getGrounding(concept);
		} catch (NotGroundedException e) {
			logger.warn("Concept" + concept + " is not grounded.");
			return;
		}

		updates.put(modifiedInstance, grounding);		
	}

	private Set<Grounding> getGrounding(IRI concept) throws NotGroundedException {
		Mode mode = getMode(concept);
		GroundedMode groundedMode;
		Set<Grounding> grounding = null;
		if (mode instanceof GroundedMode) {
			groundedMode = (GroundedMode) mode;
			grounding = groundedMode.getGrounding();
		}
		return grounding;
	}

	protected Mode getMode(IRI iri) {
		for (Mode mode : signature) {
			if (mode.getConcept().getIdentifier().equals(iri))
				return mode;
		}
		return null;
	}
	
	protected Instance retrieveModifiedInstance(State state, MembershipMolecule membership)
			throws NoMatchingInstanceException {
		Set<Instance> allInstancesOfConcept = retrieveInstances(state.getOntology(), membership.getRightParameter());
		for (Instance instance : allInstancesOfConcept) {
			if (instance.getIdentifier().equals(membership.getLeftParameter())) {
				return instance;
			}
		}
		throw new NoMatchingInstanceException(membership.getLeftParameter().toString());
	}

	private Set<Instance> retrieveInstances(Ontology o, Term t) {
		Concept c = factory.createConcept((IRI)t);
		reasoner.deRegisterOntology((IRI)(o.getIdentifier()));
		try {
			reasoner.registerOntology(o);
		} catch (InconsistencyException e) {
			logger.warn("Ontology registration failed.", e);
		}
		return reasoner.getInstances((IRI)(o.getIdentifier()), c);
	}

	public Map<Instance, Set<Grounding>> getUpdates() {
		return Collections.unmodifiableMap(updates);
	}

}
