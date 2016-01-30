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

import ie.deri.wsmx.choreography.Radex;
import ie.deri.wsmx.commons.Helper;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.choreography.ChoreographyFactoryRI;
import org.deri.wsmo4j.logicalexpression.AttributeValueMoleculeImpl;
import org.deri.wsmo4j.logicalexpression.MemberShipMoleculeImpl;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.Molecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.*;
import org.wsml.reasoner.api.WSMLReasoner;
import org.wsml.reasoner.api.WSMLReasonerFactory;
import org.wsml.reasoner.api.inconsistency.ConsistencyViolation;
import org.wsml.reasoner.api.inconsistency.InconsistencyException;
import org.wsml.reasoner.impl.DefaultWSMLReasonerFactory;
import org.wsmo.common.*;
import org.wsmo.common.exception.*;
import org.wsmo.execution.common.component.ChoreographyEngine.Direction;
import org.wsmo.factory.*;
import org.wsmo.service.rule.*;
import org.wsmo.service.signature.*;
import org.wsmo.wsml.ParserException;

import com.ontotext.wsmo4j.common.IRIImpl;
import com.ontotext.wsmo4j.ontology.*;

/**
 * A state implementation that is also a modification broadcaster.
 * 
 * <pre>
 * Created on Aug 25, 2005
 * Committed by $Author: maciejzaremba $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * 
 * @version $Revision: 1.48 $ $Date: 2007-06-14 16:12:25 $
 */
public class LogicalState implements State, StateModificationBroadcaster {

	protected static Logger logger = Logger.getLogger(LogicalState.class);

	// used to speparate states withing this classloader domain
	private static int stateCounter = 0;

	protected WsmoFactory factory;
	protected LogicalExpressionFactory leFactory;
	protected ChoreographyFactory cFactory;
	protected DataFactory dataFactory;

	protected Map<Molecule, Axiom> axioms = new HashMap<Molecule, Axiom>();
	protected Set<CompoundFact> facts = new HashSet<CompoundFact>();
	protected StateSignature signature;
	protected Ontology state;
	protected Map<String, Object> c = new HashMap<String, Object>();

	protected WSMLReasoner reasoner = null;
	// protected AxiomatizationNormalizer normalizer = new AxiomatizationNormalizer(new WSMO4JManager(), null);

	private List<StateModificationListener> listeners = new ArrayList<StateModificationListener>();

	private int attributeCounter = 0;
	private int membershipCounter = 0;
	public boolean isEndState = false;
	LogicalExpression le = null;

	public LogicalState(StateSignature signature) {
		super();
		setupFactories();

		c.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER, WSMLReasonerFactory.BuiltInReasoner.IRIS);
		reasoner = DefaultWSMLReasonerFactory.getFactory().createWSMLFlightReasoner(c);
		this.signature = signature;
		if (signature == null) throw new IllegalArgumentException("Invalid signature for state initialisation.");
		initializeState(signature);
		cacheGroundings = new HashMap<Concept, Grounding>();

		for (In mode : signature.listInModes()) {
			try {
				cacheGroundings.put(mode.getConcept(), mode.getGrounding().iterator().next());
			} catch (NotGroundedException e) {
				logger.warn("In mode " + mode.getConcept().getIdentifier() + " not grounded.");
			}
		}

		for (Out mode : signature.listOutModes()) {
			try {
				cacheGroundings.put(mode.getConcept(), mode.getGrounding().iterator().next());
			} catch (NotGroundedException e) {
				logger.warn("Out mode " + mode + " not grounded.");
			}
		}

		String leStr = "?x[_\"http://www.wsmo.org/ontologies/choreography/oasm#value\" hasValue _\"http://www.wsmo.org/ontologies/choreography/oasm#EndState\"] memberOf _\"http://www.wsmo.org/ontologies/choreography/oasm#ControlState\"";

		try {
			le = leFactory.createLogicalExpression(leStr);
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initializeWithControlState() {
		Concept controlState = factory.createConcept(factory
				.createIRI("http://www.wsmo.org/ontologies/choreography/oasm#ControlState"));
		Instance controlStateInstance = factory.createInstance(factory
				.createIRI("http://www.wsmo.org/ontologies/choreography/oasm#controlstate"));
		try {
			controlStateInstance.addConcept(controlState);
			state.addInstance(controlStateInstance);
			AttributeValueMolecule avm = leFactory.createAttributeValue(factory
					.createIRI("http://www.wsmo.org/ontologies/choreography/oasm#controlstate"), factory
					.createIRI("http://www.wsmo.org/ontologies/choreography/oasm#value"), factory
					.createIRI("http://www.wsmo.org/ontologies/choreography/oasm#InitialState"));
			add(avm);
		} catch (SynchronisationException e) {
			logger.warn("Failure during insertion of control state.", e);
		} catch (InvalidModelException e) {
			logger.warn("Failure during insertion of control state.", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void setupFactories() {
		leFactory = Factory.createLogicalExpressionFactory(null);
		factory = Factory.createWsmoFactory(null);
		cFactory = new ChoreographyFactoryRI();
		dataFactory = Factory.createDataFactory(null);
	}

	private void initializeState(StateSignature signature) {
		if (signature.listOntologies() == null || signature.listOntologies().isEmpty())
			throw new IllegalArgumentException("Signature doesn't have an ontology.");
		String iriStr = "http://www.wsmx.org/choreography/state_id-" + stateCounter+ "at" + Helper.getRandomLong();
		state = factory.createOntology(factory.createIRI(iriStr+"#onto"));
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap<String,Object>());
		
		state.setDefaultNamespace(wsmoFactory.createIRI(iriStr));
		Ontology oasm = Helper.getOntology("http://www.wsmo.org/ontologies/choreography/oasm");
		if (oasm != null) {
			state.addOntology(oasm);
			state.addNamespace(oasm.getDefaultNamespace());
		}
		logger.info("Initializing state " + state.getIdentifier());
		int counter = 1;
		for (Ontology o : signature.listOntologies()){
			state.addNamespace(wsmoFactory.createNamespace("ns"+counter++, o.getDefaultNamespace().getIRI()));
		}
		
//		state.setDefaultNamespace(signature.listOntologies().iterator().next().getDefaultNamespace());
		for (Ontology o : signature.listOntologies()) {
			state.addOntology(o);
			logger.info("Transadding from " + o.getIdentifier());
			for (Namespace ns : (Collection<Namespace>) signature.listOntologies().iterator().next().listNamespaces()) {
				logger.info("Transadding namespace " + ns.getPrefix());
				state.addNamespace(ns);
			}
		}
		for (Mode mode : signature) {
			try {
				Concept concept = mode.getConcept();
				Concept copy = copy(concept);
				state.addConcept(copy);
			} catch (SynchronisationException e) {
				try {
					logger.warn("Failed to initialize state with " + mode.getConcept().getIdentifier(), e);
				} catch (Throwable t) {
					logger.warn("Failed to initialize state.", e);
				}
			} catch (InvalidModelException e) {
				try {
					logger.warn("Failed to initialize state with " + mode.getConcept().getIdentifier(), e);
				} catch (Throwable t) {
					logger.warn("Failed to initialize state.", e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private ConceptImpl copy(Concept concept) throws InvalidModelException {
		// FIXME this is a workaround for what should be clone() in wsmo4j
		// does not use factory to ensure that a real copy is created
		ConceptImpl conceptImpl = new ConceptImpl(concept.getIdentifier(), null);
		for (Concept c : (Collection<Concept>) concept.listSubConcepts())
			conceptImpl.addSubConcept(c);
		for (Concept c : (Collection<Concept>) concept.listSuperConcepts())
			conceptImpl.addSuperConcept(c);
		for (Attribute a : (Collection<Attribute>) concept.listAttributes())
			conceptImpl.createAttribute(a.getIdentifier());
		for (Instance i : (Collection<Instance>) concept.listInstances())
			conceptImpl.addInstance(i);
		return conceptImpl;
	}

	public List<Instance> copy(Instance instance) {
		// FIXME this is a workaround for what should be clone() in wsmo4j
		// does not use factory to ensure that a real copy is created
		//first on the list is the root instance
		List<Instance> response = new ArrayList<Instance>();		
		InstanceImpl instanceImpl = new InstanceImpl(instance.getIdentifier());
		response.add(instanceImpl);
		
		try {
			for (Concept c : (Collection<Concept>) instance.listConcepts())
				instanceImpl.addConcept(c);
			Map map = instance.listAttributeValues();

			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				Object key = pairs.getKey();
				Object values = pairs.getValue();
				Set valueSet = (Set) values;

				for (Object value : valueSet) {
					if (value instanceof Instance) {
						List<Instance> instances = copy((Instance) value);
						instanceImpl.addAttributeValue(factory.createIRI(key.toString()), instances.get(0));
						response.addAll(instances);
					} else if (value instanceof DataValue) {
						DataValue dv = (DataValue) value;
						instanceImpl.addAttributeValue(factory.createIRI(key.toString()), dv);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.asm.StateModificationBroadcaster#registerListener(ie.deri.wsmx.asm.StateModificationListener)
	 */
	public void registerListener(StateModificationListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.asm.StateModificationBroadcaster#registerListeners(java.util.List)
	 */
	public void registerListeners(List<StateModificationListener> listeners) {
		listeners.addAll(listeners);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.asm.State#add(org.wsmo.service.choreography.rule.CompoundFact)
	 */
	public void add(CompoundFact fact) {
		MoleculeFact mf = (MoleculeFact) fact; // FIXME handle relation facts);
		if (mf.listMembershipMolecules().size() > 0)
		// TODO handle more than first
			add(mf.listMembershipMolecules().iterator().next());
		for (AttributeValueMolecule avm : mf.listAttributeValueMolecules())
			add(avm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.asm.State#add(org.omwg.logicalexpression.MembershipMolecule)
	 */
	public void add(MembershipMolecule fact) {
		// TODO write this more elegant once factory convenience creators are
		// fixed
		Set<MembershipMolecule> mms = new HashSet<MembershipMolecule>();
		mms.add(fact);
		facts.add(cFactory.facts.createMoleculeFact(mms, new HashSet<AttributeValueMolecule>()));
		Axiom axiom = factory.createAxiom(factory.createIRI("http://www.wsmx.org/choreography/state/membership"
				+ membershipCounter++));
		removeCachedDefinitions(axiom);
		axiom.addDefinition(fact);
		try {
			logger.debug("Adding MM axiom: " + fact);
			axioms.put(fact, axiom);
			state.addAxiom(axiom);
		} catch (SynchronisationException e) {
			logger.warn("Failed to add membership molecule to state.", e);
		} catch (InvalidModelException e) {
			logger.warn("Failed to add membership molecule to state.", e);
		}
		for (StateModificationListener listener : listeners) {
			listener.addedMembership(this, fact);
		}
	}

	public CompoundFact copy(CompoundFact fact, Term leftTranslatedTerm) throws ParserException {
		CompoundFact copyFact = null;
		if (leftTranslatedTerm != null) {
			// The fact is like '_#[..] memberOf ...'

			// FIXME reliance on toString()
			MembershipMolecule mm = null;
			MoleculeFact mf = (MoleculeFact) fact; // FIXME handle relation
			// facts);
			if (mf.listMembershipMolecules().size() > 0) {
				MembershipMolecule membershipMolecule = mf.listMembershipMolecules().iterator().next();
				MembershipMolecule translatedMembershipMolecule = new MemberShipMoleculeImpl(leftTranslatedTerm,
						membershipMolecule.getRightParameter());
				mm = (MembershipMolecule) leFactory.createLogicalExpression(translatedMembershipMolecule.toString());
			}
			Set<AttributeValueMolecule> avms = new HashSet<AttributeValueMolecule>();
			for (AttributeValueMolecule avm : mf.listAttributeValueMolecules()) {
				AttributeValueMolecule avmTranslated = new AttributeValueMoleculeImpl(leftTranslatedTerm, avm
						.getAttribute(), avm.getRightParameter());
				;
				avms.add((AttributeValueMolecule) leFactory.createLogicalExpression(avmTranslated.toString()));
			}
			// TODO write this more elegant once factory convenience creators
			// are fixed
			Set<MembershipMolecule> mms = new HashSet<MembershipMolecule>();
			if (mf.listMembershipMolecules().size() > 0) mms.add(mm);
			copyFact = cFactory.facts.createMoleculeFact(mms, avms);

		} else {
			copyFact = copy(fact);
		}
		return copyFact;
	}

	public CompoundFact copy(CompoundFact fact) throws ParserException {
		// FIXME reliance on toString()
		MembershipMolecule mm = null;
		MoleculeFact mf = (MoleculeFact) fact; // FIXME handle relation facts);
		if (mf.listMembershipMolecules().size() > 0)
			mm = (MembershipMolecule) leFactory.createLogicalExpression(mf.listMembershipMolecules().iterator().next()
					.toString());
		Set<AttributeValueMolecule> avms = new HashSet<AttributeValueMolecule>();
		for (AttributeValueMolecule avm : mf.listAttributeValueMolecules()) {
			avms.add((AttributeValueMolecule) leFactory.createLogicalExpression(avm.toString()));
		}
		// TODO write this more elegant once factory convenience creators are
		// fixed
		Set<MembershipMolecule> mms = new HashSet<MembershipMolecule>();
		if (mf.listMembershipMolecules().size() > 0) mms.add(mm);
		return cFactory.facts.createMoleculeFact(mms, avms);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.asm.State#add(org.omwg.logicalexpression.AttributeValueMolecule)
	 */
	public void add(AttributeValueMolecule fact) {
		facts.add(cFactory.facts.createMoleculeFact(new HashSet<MembershipMolecule>(),
			new HashSet<AttributeValueMolecule>(Arrays.asList(new AttributeValueMolecule[] { fact }))));
		
		boolean addAsAxiom = true;
		if((!fact.getLeftParameter().toString().startsWith("http://www.wsmo.org/reasoner/anonymous")) && (!fact.getLeftParameter().toString().startsWith("http://www.wsmo.org/ontologies/choreography/oasm#controlstate"))){
			Instance instance = state.findInstance((Identifier) fact.getLeftParameter());
			if(instance != null) {
				addAsAxiom = false;
				Set<Attribute> attributeDefinitions = instance.findAttributeDefinitions((Identifier) fact.getAttribute());
				
				if(attributeDefinitions.size() == 1){
					Attribute attr = attributeDefinitions.iterator().next();
					Type type = attr.listTypes().iterator().next();
					int maxCardinality = attr.getMaxCardinality();
					if(maxCardinality == 1){
						try {
							instance.removeAttributeValues((Identifier) fact.getAttribute());
						} catch (SynchronisationException e) {
							logger.warn("Failed to remove attribute values.", e);
						} catch (InvalidModelException e) {
							logger.warn("Failed to remove attribute values.", e);
						}
					}
					Value value = null;
					if(type instanceof Concept){
						value = new InstanceImpl((Identifier) fact.getRightParameter());
					} else if(type instanceof SimpleDataType) {
						SimpleDataType simple = (SimpleDataType) type;
						IRI iri = simple.getIRI();
						if(iri.toString().equals(WsmlDataType.WSML_DECIMAL)){
							value = new SimpleDataValueImpl((SimpleDataType)type, new BigDecimal(fact.getRightParameter().toString()));
						} else if(iri.toString().equals(WsmlDataType.WSML_INTEGER)){
							value = new SimpleDataValueImpl((SimpleDataType)type, new BigInteger(fact.getRightParameter().toString()));
						} else if(iri.toString().equals(WsmlDataType.WSML_STRING)){
							value = new SimpleDataValueImpl((SimpleDataType)type, fact.getRightParameter().toString());
						} 
					} else if(type instanceof ComplexDataType) {
						value = new ComplexDataValueImpl((ComplexDataType)type, (SimpleDataValue)fact.getRightParameter());
					}
					try {
						instance.addAttributeValue((Identifier)fact.getAttribute(), value);
					} catch (SynchronisationException e) {
						logger.warn("Failed to remove attribute values.", e);
					} catch (InvalidModelException e) {
						logger.warn("Failed to remove attribute values.", e);
					}
				} else if(attributeDefinitions.size() == 0) {
					try {
						instance.removeAttributeValues((Identifier) fact.getAttribute());
						Value value = new SimpleDataValueImpl(new SimpleDataTypeImpl(new IRIImpl(WsmlDataType.WSML_STRING)), fact.getRightParameter().toString());
						instance.addAttributeValue((Identifier)fact.getAttribute(), value);
					} catch (SynchronisationException e) {
						logger.warn("Failed to remove attribute values.", e);
					} catch (InvalidModelException e) {
						logger.warn("Failed to remove attribute values.", e);
					}
				}
			}
		}
		if(addAsAxiom){
			Axiom axiom = factory.createAxiom(factory.createIRI("http://www.wsmx.org/choreography/state/attribute"
					+ attributeCounter++));
			removeCachedDefinitions(axiom);
			axiom.addDefinition(fact);
			try {
				logger.debug("Adding AVM axiom: " + fact);
				axioms.put(fact, axiom);
				state.addAxiom(axiom);
			} catch (SynchronisationException e) {
				logger.warn("Failed to add attribute value to state.", e);
			} catch (InvalidModelException e) {
				logger.warn("Failed to add attribute value to state.", e);
			}
		}
		for (StateModificationListener listener : listeners) {
			listener.addedAttribute(this, fact);
		}
	}

	public void remove(AttributeValueMolecule fact) {
		// facts.add(cFactory.facts.createCompoundFact(new
		// HashSet<MembershipMolecule>(), new
		// HashSet<AttributeValueMolecule>(Arrays.asList(new
		// AttributeValueMolecule[]{fact}))));
		Axiom axiom = null;
		for (Entry<Molecule, Axiom> e : axioms.entrySet()) {
			if (e.getKey().equals(fact)) axiom = e.getValue();
		}
		if (axiom == null) return;
		try {
			logger.debug("Removing AVM axiom: " + fact);
			state.removeAxiom(axiom);
			axioms.remove(fact);
		} catch (SynchronisationException e) {
			logger.warn("Failed to remove attribute value from state.", e);
		} catch (InvalidModelException e) {
			logger.warn("Failed to remove attribute value from state.", e);
		}
		// FIXME extend listener for removal events
		// for (StateModificationListener listener : listeners) {
		// listener.r(this, fact);
		// }
	}

	public void remove(MembershipMolecule fact) {
		// TODO
	}

    /* (non-Javadoc)
	 * @see ie.deri.wsmx.asm.State#add(org.omwg.ontology.Instance)
	 */
	public Grounding add(Direction direction, Instance instance) throws SynchronisationException,
			InvalidModelException, UpdateIllegalException {
		// FIXME check if instance is of correct mode and throw UpdateIllegal if
		List<Instance> copyInstances = copy(instance);
		for (Instance copyInstance : copyInstances )
			state.addInstance(copyInstance);

		refreshOntology();

		Grounding groundingFromInstance = null;
		for (Instance copyInstance : copyInstances) {
			Concept concept = (Concept) copyInstance.listConcepts().iterator().next();
			if (cacheGroundings.keySet().contains(concept)) {
				// If the concept was previously encountered, but no grounding was
				// found for it, write message
				groundingFromInstance = cacheGroundings.get(concept);
				logger.info("Instance " + copyInstance.getIdentifier().toString() + " matches  "
						+ concept.getIdentifier().toString());
			} else {
				// This is a new encountered concept, cache the grounding.
				groundingFromInstance = getGroundingFromInstance(copyInstance);
				cacheGroundings.put(concept, groundingFromInstance);
			}
		}

		return groundingFromInstance;
		// return getGroundingFromInstance(instance);
	}

	public Grounding getGroundingFromInstance(Instance instance) throws UpdateIllegalException {
		for (In mode : signature.listInModes()) {
			if (reasoner.isMemberOf((IRI) state.getIdentifier(), instance, mode.getConcept()))
				try {
					logger.info("Instance " + instance.getIdentifier().toString() + " matches input "
							+ mode.getConcept().getIdentifier().toString());
					return mode.getGrounding().iterator().next();
				} catch (NotGroundedException e) {
					// FIXME
					return null;
				}
		}
		for (Out mode : signature.listOutModes()) {
			if (reasoner.isMemberOf((IRI) state.getIdentifier(), instance, mode.getConcept()))
				try {
					logger.info("Instance " + instance.getIdentifier().toString() + " matches output "
							+ mode.getConcept().getIdentifier().toString());
					return mode.getGrounding().iterator().next();
				} catch (NotGroundedException e) {
					return null;
				}
		}
		throw new UpdateIllegalException();
	}

	private Grounding getGroundingFromInstanceFast(Instance instance) throws UpdateIllegalException {
		for (In mode : signature.listInModes()) {
			if (((Concept) instance.listConcepts().iterator().next()).getIdentifier().equals(
				mode.getConcept().getIdentifier()))
				try {
					logger.info("Instance " + instance.getIdentifier().toString() + " matches input "
							+ mode.getConcept().getIdentifier().toString());
					return mode.getGrounding().iterator().next();
				} catch (NotGroundedException e) {
					// FIXME
					return null;
				}
		}
		for (Out mode : signature.listOutModes()) {
			if (((Concept) instance.listConcepts().iterator().next()).getIdentifier().equals(
				mode.getConcept().getIdentifier()))
				try {
					logger.info("Instance " + instance.getIdentifier().toString() + " matches output "
							+ mode.getConcept().getIdentifier().toString());
					return mode.getGrounding().iterator().next();
				} catch (NotGroundedException e) {
					return null;
				}
		}
		throw new UpdateIllegalException();
	}

	public void add(Direction direction, Set<Instance> instances, Map<Instance, Grounding> groundings)
			throws SynchronisationException, InvalidModelException, UpdateIllegalException {
		for (Instance instance : instances) {
			List<Instance> copyInstances = copy(instance);
			for (Instance copyInstance : copyInstances )
				state.addInstance(copyInstance);
			
			
//			Instance copyInstance = copy(instance);
//			state.addInstance(copyInstance);
		}
		refreshOntology();

		// for (Instance instance : instances) {
		// try {
		// Grounding groundingFromInstance = getGroundingFromInstance(instance);
		// groundings.put(instance, groundingFromInstance);
		// } catch (UpdateIllegalException e) {
		// logger.info("Machine found no matching inputs/outputs for " +
		// instance.getIdentifier().toString() + " with concept " +
		// ((Concept)
		// instance.listConcepts().iterator().next()).getIdentifier().toString());
		// }
		// }

		for (Instance instance : instances) {
			// logger.info("instance " + instance.getIdentifier());

			Concept concept = (Concept) instance.listConcepts().iterator().next();
			// logger.info("concept " + concept.getIdentifier());
			try {
				if (cacheGroundings.keySet().contains(concept)) {
					// If the concept was previously encountered, but no
					// grounding was found for it, write message
					Grounding groundingFromInstance = cacheGroundings.get(concept);
					if (groundingFromInstance == null) {
						logger.info("Machine found no matching inputs/outputs for "
								+ instance.getIdentifier().toString() + " with concept "
								+ ((Concept) instance.listConcepts().iterator().next()).getIdentifier().toString());
					}
					groundings.put(instance, groundingFromInstance);
					// logger.info("grounding " + groundingFromInstance);

				} else {
					// This is a new encountered concept, cache the grounding.
					Grounding groundingFromInstance = getGroundingFromInstance(instance);
					cacheGroundings.put(concept, groundingFromInstance);
					groundings.put(instance, groundingFromInstance);
					logger.info("Instance " + instance.getIdentifier().toString() + " matches  "
							+ concept.getIdentifier().toString());

				}

			} catch (UpdateIllegalException e) {
				logger.info("Machine found no matching inputs/outputs for " + instance.getIdentifier().toString()
						+ " with concept "
						+ ((Concept) instance.listConcepts().iterator().next()).getIdentifier().toString());
			}
		}

		// print out the ontology

		// logger.debug("\n----------------"+Helper.serializeTopEntity(state)+"\n----------------");

		// check if in the end state
		isEndState = reasoner.entails((IRI) state.getIdentifier(), le);
		logger.debug("EndState: " + isEndState);
	}

	private Map<Concept, Grounding> cacheGroundings;

	boolean myIsMemberOf(Instance instance, Concept concept) {
		Set concepts = instance.listConcepts();
		Iterator i = concepts.iterator();
		logger.info("Checking instance : " + instance.getIdentifier().toString() + " against "
				+ concept.getIdentifier().toString());
		while (i.hasNext()) {
			Concept iconcept = (Concept) i.next();
			if (myIsEqualOrSubConceptOf(iconcept, concept, "  ")) {
				logger.info("Found Instance Match !");
				return true;
			}
		}
		return false;
	}

	boolean myIsEqualOrSubConceptOf(Concept iconcept, Concept concept, String indent) {
		logger.info(indent + "Checking concept : " + iconcept.getIdentifier().toString() + " against "
				+ concept.getIdentifier().toString());
		if (iconcept.equals(concept)) return true;

		Set supers = iconcept.listSuperConcepts();
		Iterator is = supers.iterator();
		while (is.hasNext()) {
			Concept isconcept = (Concept) is.next();
			if (myIsEqualOrSubConceptOf(isconcept, concept, indent + "  ")) {
				logger.info(indent + "Found Concept Match !");
				return (true);
			}
		}

		return false;
	}

    /* (non-Javadoc)
	 * @see ie.deri.wsmx.asm.State#addAll(java.util.Collection)
	 */
	public void addAll(Collection<CompoundFact> facts) {
		for (CompoundFact fact : facts) {
			add(fact);
		}
	}

	/* (non-Javadoc) 
	 * @see ie.deri.wsmx.asm.State#remove(org.wsmo.service.choreography.rule.CompoundFact)
	 */
	public void remove(CompoundFact fact) {
		facts.remove(fact);
		MoleculeFact mf = (MoleculeFact) fact; // FIXME handle relation facts);
		for (AttributeValueMolecule avm : mf.listAttributeValueMolecules())
			remove(avm);
	}

	public Set<CompoundFact> getFacts() {
		return Collections.unmodifiableSet(facts);
	}

	public Ontology getOntology() {
		return state;
	}

	public boolean holds(Condition condition) {
		return holds(condition.getRestrictedLogicalExpression());
	}

	public boolean anonymousToVariableRewrite(LogicalExpression condition) {
		throw new RuntimeException("not implemrnted");
		// if (condition instanceof MembershipMolecule) {
		// MembershipMolecule mm = (MembershipMolecule)condition;
		// Term leftParameter = mm.getLeftParameter();
		// Term rightParameter = mm.getRightParameter();
		//			
		// }
		// return false;
	}

	public boolean holds(LogicalExpression condition) {
		logger.info("Executing ground query " + condition + " over " + state.getIdentifier() + " with "
				+ state.listInstances().size() + " instances and " + state.listAxioms().size() + " axioms.");
		refreshOntology();
		return reasoner.executeGroundQuery((IRI) state.getIdentifier(), condition);
	}

	public Set<Map<Variable, Term>> retrieveBinding(Condition condition) {
		logger.info("Executing query " + condition + " over " + state.getIdentifier() + " with "
				+ state.listInstances().size() + " instances and " + state.listAxioms().size() + " axioms.");
		refreshOntology();
		Set<Map<Variable, Term>> instances = reasoner.executeQuery((IRI) state.getIdentifier(), condition
				.getRestrictedLogicalExpression());
		return instances;
	}

	public Instance getInstance(Identifier id, IRI conceptIRI) {
		Concept concept = factory.createConcept(conceptIRI);
		return getInstance(id, concept);
	}

	public Instance getInstance(Identifier id, Concept concept) {
		refreshOntology();
		Set<Instance> instances = reasoner.getInstances((IRI) state.getIdentifier(), concept);
		Instance instance = null;
		for (Instance i : instances)
			if (i.getIdentifier().equals(id)) instance = i;
		if (instance != null) {
			addAttributesFromAxioms(instance);

		}

		return instance;
	}

	private void addAttributesFromAxioms(Instance instance) {
		for (Entry<Molecule, Axiom> e : axioms.entrySet()) {
			if (e.getKey() instanceof AttributeValueMolecule) {
				AttributeValueMolecule avm = (AttributeValueMolecule) e.getKey();
				Axiom axiom = e.getValue();

				if (avm.getLeftParameter().equals(instance.getIdentifier())) {
					try {
						IRI iri = (IRI) avm.getAttribute();
						Value val = null;
						Object o = avm.getRightParameter();
						if (o instanceof Value) {
							val = (Value) o;
						} else if (o instanceof IRI) {
							val = factory.getInstance((IRI) o);
						} else {
							logger.info("Can't assign " + o.toString() + " to " + iri.toString());
						}
						/*
						 * try { val = (Value) avm.getRightParameter(); } catch
						 * (ClassCastException f) { logger.info("Dealing with
						 * Problem - 2 has class:" +
						 * avm.getRightParameter().getClass()); val =
						 * factory.getInstance((IRI) avm.getRightParameter()); }
						 * 
						 * logger.info("Problem 2 - passed");
						 */
						instance.addAttributeValue(iri, val);
					} catch (Exception e2) {
						logger.warn("Failed to pseudo-reason: " + e2);
					}
				}
			}
		}
		Map<Identifier, Set<Value>> listAttributeValues = instance.listAttributeValues();
		Set<Identifier> attributes = listAttributeValues.keySet();
		for (Identifier identifier : attributes) {
			Object o = listAttributeValues.get(identifier).iterator().next();

			if (o instanceof Instance) {
				Instance attribute = (Instance) o;
				addAttributesFromAxioms(attribute);
				logger.debug(Helper.printInstance(attribute));
			}
		}
	}

	public Set<Instance> getInstances(Identifier id, IRI conceptIRI) throws SynchronisationException,
			InvalidModelException {
		Concept concept = factory.createConcept(conceptIRI);
		return getInstances(id, concept);
	}

	public void refreshOntology() {
		try {
			reasoner.registerOntology(state);
		} catch (InconsistencyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		reasoner.registerOntologyNoVerification(state);
		
//		Set<Ontology> ontos = state.listOntologies();
//		for (Ontology o : ontos)
//			reasoner.registerOntologyNoVerification(o);
//		
//
//		//even if inconsistencies has been detected Choreography should run anyways
//		Set<ConsistencyViolation> violations = reasoner.checkConsistency();
//		if (violations.size() > 0){
//			logger.fatal("Ontology violations has been detected!");
//			for (ConsistencyViolation violation : violations){
//				logger.fatal(violation.toString());
//			}
//		}
	}

	public Set<Instance> getInstances(Identifier id, Concept concept) throws SynchronisationException,
			InvalidModelException {
		refreshOntology();
		logger.debug(Helper.serializeTopEntity(state));

		Set<Instance> instances = reasoner.getInstances((IRI) state.getIdentifier(), concept);
		Set<Instance> r = new HashSet<Instance>();

		for (Instance instance : instances) {

			// TODO proxy instance - fill it with data !!!
			if (instance instanceof Proxy) {
				instance = factory.createInstance(instance.getIdentifier(), concept);
			}
			r.add(fillFromAxiomsrecursivellyProxy(instance));
		}
		return r;
	}

	private Instance fillFromAxiomsrecursivellyProxy(Instance instance) {
		for (Entry<Molecule, Axiom> e : axioms.entrySet()) {
			if (e.getKey() instanceof AttributeValueMolecule) {
				AttributeValueMolecule avm = (AttributeValueMolecule) e.getKey();
				if (avm.getLeftParameter().equals(instance.getIdentifier())) {
					try {
						IRI iri = (IRI) avm.getAttribute();
						Value val = null;
						Object o = avm.getRightParameter();
						if (o instanceof Value) {
							val = (Value) o;
						} else if (o instanceof IRI) {
							IRI iri1 = (IRI) o;
							// TODO continue recursivelly because althow the
							// attribute value is an instance,
							// it is possible the some of its attributes were
							// modified
							val = state.findInstance(iri1);
							if (val != null) {
								val = fillFromAxiomsRecursivelly((Instance) val);
							} else {
								val = factory.getInstance(iri1);
								if (val instanceof Proxy) {
									val = factory.createInstance(((Instance) val).getIdentifier());
									val = fillFromAxiomsrecursivellyProxy((Instance) val);
								}
							}
						} else {
							logger.info("Can't assign " + o.toString() + " to " + iri.toString());
						}
						instance.addAttributeValue(iri, val);
					} catch (Exception e2) {
						logger.warn("Failed to pseudo-reason: " + e2);
					}
				}
			} else if (e.getKey() instanceof MembershipMolecule) {
				MembershipMolecule molecule = (MembershipMolecule) e.getKey();
				if (molecule.getLeftParameter().equals(instance.getIdentifier())) {
					try {
						Concept conceptOfMolecule = factory.getConcept((IRI) molecule.getRightParameter());
						instance.addConcept(conceptOfMolecule);
					} catch (Exception e2) {
						logger.warn("Failed to pseudo-reason: " + e2);
					}
				}
			}

		}
		return instance;
	}

	/**
	 * Find the membership and attribute value molecules for the instance with
	 * the specified id.
	 * @param id The id of the instance.
	 * @return The list of molecules corresponding to the id.
	 * @throws InvalidModelException
	 * @throws SynchronisationException
	 */
	public Set<Molecule> getMoleculesForInstance(String id) throws SynchronisationException, InvalidModelException {
		Map<Molecule, Axiom> toReturn = new HashMap<Molecule, Axiom>();
		for (Entry<Molecule, Axiom> e : axioms.entrySet()) {
			Molecule molecule = e.getKey();
			if (molecule.getLeftParameter().toString().equals(id)) {
				toReturn.put(molecule, e.getValue());
			}
		}
		// The ids of the response instances will not be annonimous.
		// The axioms should be removed so that there won't be duplicates.
		for (Entry<Molecule, Axiom> e : toReturn.entrySet()) {
			state.removeAxiom(e.getValue());
			axioms.remove(e.getKey());
		}
		return toReturn.keySet();
	}

	private Instance fillFromAxiomsRecursivelly(Instance instance) {
		for (Entry<Molecule, Axiom> e : axioms.entrySet()) {
			if (e.getKey() instanceof AttributeValueMolecule) {
				AttributeValueMolecule avm = (AttributeValueMolecule) e.getKey();
				if (avm.getLeftParameter().equals(instance.getIdentifier())) {
					try {
						IRI iri = (IRI) avm.getAttribute();
						Value val = null;
						Object o = avm.getRightParameter();
						if (o instanceof Value) {
							val = (Value) o;
						} else if (o instanceof IRI) {
							IRI iri1 = (IRI) o;
							val = state.findInstance(iri1);
						} else {
							logger.info("Can't assign " + o.toString() + " to " + iri.toString());
						}
						instance.addAttributeValue(iri, val);
						// TODO when numbered annonymous instances are available
						// or it is possible to create the request, remove break
						break;
					} catch (Exception e2) {
						logger.warn("Failed to pseudo-reason: " + e2);
					}
				}
			}
		}
		Map attributes = instance.listAttributeValues();
		Set<IRI> attributeNames = attributes.keySet();
		for (IRI attrName : attributeNames) {
			Set value = (Set) attributes.get(attrName);
			for (Object object : value) {
				if (object instanceof Instance) {

					Instance attribute = state.findInstance((Identifier) ((Instance) object).getIdentifier());
					if (attribute != null) {
						attribute = fillFromAxiomsRecursivelly(attribute);
					}
				}
			}

		}

		return instance;
	}
	
	 /**
     * Removes all definitions from axiom
     * @param axiom
     */
    protected void removeCachedDefinitions(Axiom axiom) {
    	for (LogicalExpression le : axiom.listDefinitions()) 
			axiom.removeDefinition(le);
    }

	public boolean isControlled(Term instance) {
		if (instance instanceof IRI) return isControlled(factory.createInstance((IRI) instance));
		if (instance instanceof UnnumberedAnonymousID)
			return isControlled(factory.createInstance((UnnumberedAnonymousID) instance));
		// FIXME?
		return false;
	}

	public boolean isControlled(Instance instance) {
		if (instance.getIdentifier() instanceof UnnumberedAnonymousID) return false;
		for (Controlled mode : signature.listControlledModes()) {
			if (reasoner.isMemberOf((IRI) state.getIdentifier(), instance, mode.getConcept())) return true;
		}
		return false;
	}

	public boolean isControlledConcept(Term concept) {
		if (concept instanceof IRI) return isControlledConcept(factory.createConcept((IRI) concept));
		if (concept instanceof UnnumberedAnonymousID)
			return isControlledConcept(factory.createConcept((UnnumberedAnonymousID) concept));
		// FIXME?
		return false;
	}

	public boolean isControlledConcept(Concept concept) {
		for (Controlled mode : signature.listControlledModes()) {
			if (concept.equals(mode.getConcept())) return true;
		}
		return false;
	}

	public boolean isOut(Term instance) {
		if (instance instanceof IRI) return isOut(factory.createInstance((IRI) instance));
		if (instance instanceof UnnumberedAnonymousID)
			return isOut(factory.createInstance((UnnumberedAnonymousID) instance));
		// FIXME?
		return false;
	}

	public boolean isOut(Instance instance) {
		if (instance.getIdentifier() instanceof UnnumberedAnonymousID) return false;
		for (Out mode : signature.listOutModes()) {
			if (reasoner.isMemberOf((IRI) state.getIdentifier(), instance, mode.getConcept())) return true;
		}
		return false;
	}

	public boolean isOutConcept(Term concept) {
		if (concept instanceof IRI) return isOutConcept(factory.createConcept((IRI) concept));
		if (concept instanceof UnnumberedAnonymousID)
			return isOutConcept(factory.createConcept((UnnumberedAnonymousID) concept));
		// FIXME?
		return false;
	}

	public boolean isInConcept(Term concept) {
		if (concept instanceof IRI) return isInConcept(factory.createConcept((IRI) concept));
		if (concept instanceof UnnumberedAnonymousID)
			return isInConcept(factory.createConcept((UnnumberedAnonymousID) concept));
		// FIXME?
		return false;
	}

	public boolean isOutConcept(Concept concept) {
		for (Out mode : signature.listOutModes()) {
			if (concept.equals(mode.getConcept())) return true;
		}
		return false;
	}

	public boolean isInConcept(Concept concept) {
		for (In mode : signature.listInModes()) {
			if (concept.equals(mode.getConcept())) return true;
		}
		return false;
	}

	/**
	 * @return the cacheGroundings
	 */
	public Map<Concept, Grounding> getCacheGroundings() {
		return cacheGroundings;
	}

	/**
	 * @param cacheGroundings the cacheGroundings to set
	 */
	public void setCacheGroundings(Map<Concept, Grounding> cacheGroundings) {
		this.cacheGroundings = cacheGroundings;
	}

	/**
	 * @return the wsmo factory
	 */
	public WsmoFactory getFactory() {
		return factory;
	}

	public Map<Molecule, Axiom> getAxioms() {
		return axioms;
	}

}
