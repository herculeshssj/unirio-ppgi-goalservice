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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.orchestration.OrchestrationFactoryRI;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.Molecule;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.Axiom;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.omwg.ontology.Variable;
import org.wsml.reasoner.ExternalToolException;
import org.wsml.reasoner.api.WSMLReasoner;
import org.wsml.reasoner.api.WSMLReasonerFactory;
import org.wsml.reasoner.api.inconsistency.InconsistencyException;
import org.wsml.reasoner.impl.DefaultWSMLReasonerFactory;
import org.wsml.reasoner.impl.WSMO4JManager;
import org.wsml.reasoner.transformation.AxiomatizationNormalizer;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.UnnumberedAnonymousID;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.OrchestrationFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.rule.CompoundFact;
import org.wsmo.service.rule.Condition;
import org.wsmo.service.rule.MoleculeFact;
import org.wsmo.service.signature.Controlled;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.In;
import org.wsmo.service.signature.Mode;
import org.wsmo.service.signature.NotGroundedException;
import org.wsmo.service.signature.Out;
import org.wsmo.service.signature.StateSignature;
import org.wsmo.wsml.ParserException;

import com.ontotext.wsmo4j.factory.WsmoFactoryImpl;
import com.ontotext.wsmo4j.ontology.ConceptImpl;

/**
 * A state implementation that is also a modification broadcaster.
 *
 * <pre>
 * Created on Aug 25, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/orchestration/src/main/ie/deri/wsmx/orchestration/asm/LogicalState.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.1 $ $Date: 2006-12-12 10:52:43 $
 */
public class LogicalState implements State, StateModificationBroadcaster {
    
	protected static Logger logger = Logger.getLogger(LogicalState.class);
	public static final String OASM_NAMESPACE = "http://www.wsmx.org/ontologies/oasm#";
	//used to speparate states withing this classloader domain
	private static int stateCounter = 0;

    protected WsmoFactory factory;
    protected LogicalExpressionFactory leFactory;
    protected OrchestrationFactory cFactory;
	protected DataFactory dataFactory;	

	protected Map<Molecule, Axiom> axioms = new HashMap<Molecule, Axiom>();
    protected Set<CompoundFact> facts = new HashSet<CompoundFact>();
    protected StateSignature signature;
    protected Ontology state;
    protected Map<String, Object> c = new HashMap<String, Object>();
    
    protected WSMLReasoner reasoner = null;
//    protected AxiomatizationNormalizer normalizer = new AxiomatizationNormalizer(new WSMO4JManager());   
    
    private List<StateModificationListener> listeners = new ArrayList<StateModificationListener>();

    private int attributeCounter = 0;
    private int membershipCounter = 0;
    
    public LogicalState(StateSignature signature) {
        super();
        setupFactories();

        c.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER, WSMLReasonerFactory.BuiltInReasoner.KAON2);
        reasoner = DefaultWSMLReasonerFactory.getFactory().createWSMLFlightReasoner(c);
        this.signature = signature;
		if (signature == null)
			throw new IllegalArgumentException("Invalid signature for state initialisation.");
		initializeState(signature);
    }

	public void initializeWithControlState() {
		Concept controlState = factory.createConcept(factory.createIRI(OASM_NAMESPACE+"controlState"));
		Instance controlStateInstance = factory.createInstance(factory.createIRI(OASM_NAMESPACE+"cs"));
		try {
			controlStateInstance.addConcept(controlState);
			state.addInstance(controlStateInstance);
			AttributeValueMolecule avm = leFactory.createAttributeValue(
					factory.createIRI(OASM_NAMESPACE+"cs"),
					factory.createIRI(OASM_NAMESPACE+"value"),
					factory.createIRI(OASM_NAMESPACE+"initial"));		
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
		cFactory = new OrchestrationFactoryRI();
		dataFactory = Factory.createDataFactory(null);
	}	
	
	private void initializeState(StateSignature signature) {
        if (signature.listOntologies() == null || signature.listOntologies().isEmpty())
        	throw new IllegalArgumentException("Signature doesn't have an ontology.");
        state = factory.createOntology(
        		factory.createIRI("http://www.wsmx.org/choreography/state_id-" + stateCounter + "_at-" + System.currentTimeMillis()));
        logger.fatal("Initializing state " + state.getIdentifier());
        //TODO the default namespace of the ontology is selected somewhat arbitrarily
        state.setDefaultNamespace(signature.listOntologies().iterator().next().getDefaultNamespace());
        for (Ontology o : signature.listOntologies()) {
    		logger.debug("Transadding from " + o.getIdentifier());
        	for (Namespace ns : (Collection<Namespace>) signature.listOntologies().iterator().next().listNamespaces()) {
        		logger.debug("Transadding namespace " + ns.getPrefix());
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
		//FIXME this is a workaround for what should be clone() in wsmo4j
		//does not use factory to ensure that a real copy is created
		ConceptImpl conceptImpl = new ConceptImpl(concept.getIdentifier(), null);
		for (Concept c : (Collection<Concept>)concept.listSubConcepts())
			conceptImpl.addSubConcept(c);
		for (Concept c : (Collection<Concept>)concept.listSuperConcepts())
			conceptImpl.addSubConcept(c);
		for (Attribute a : (Collection<Attribute>)concept.listAttributes())
			conceptImpl.createAttribute(a.getIdentifier());
		for (Instance i : (Collection<Instance>)concept.listInstances())
			conceptImpl.addInstance(i);
		return conceptImpl;
	}

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.asm.StateModificationBroadcaster#registerListener(ie.deri.wsmx.asm.StateModificationListener)
	 */
	public void registerListener(StateModificationListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.asm.StateModificationBroadcaster#registerListeners(java.util.List)
	 */
	public void registerListeners(List<StateModificationListener> listeners) {
		listeners.addAll(listeners);
	}

    /* (non-Javadoc)
	 * @see ie.deri.wsmx.asm.State#add(org.wsmo.service.choreography.rule.CompoundFact)
	 */
    public void add(CompoundFact fact) {
		MoleculeFact mf = (MoleculeFact)fact; //FIXME handle relation facts);
        if (mf.listMembershipMolecules().size() > 0)
        	//TODO handle more than first
            add(mf.listMembershipMolecules().iterator().next());
        for (AttributeValueMolecule avm : mf.listAttributeValueMolecules())
        	add(avm);
    }

    
    /* (non-Javadoc)
	 * @see ie.deri.wsmx.asm.State#add(org.omwg.logicalexpression.MembershipMolecule)
	 */
    public void add(MembershipMolecule fact) {
    	logger.info("Adding membership molecule " + fact);
    	//TODO write this more elegant once factory convenience creators are fixed
    	Set<MembershipMolecule> mms = new HashSet<MembershipMolecule>();
    	mms.add(fact);
        facts.add(cFactory.facts.createMoleculeFact(mms, new HashSet<AttributeValueMolecule>()));
        Axiom axiom = factory.createAxiom(factory.createIRI("http://www.wsmx.org/choreography/state/membership" + membershipCounter++));
        axiom.addDefinition(fact);
        try {
        	logger.debug("Adding MM axiom: " + fact);
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
    
    public CompoundFact copy(CompoundFact fact) throws ParserException {
    	//FIXME reliance on toString()
    	MembershipMolecule mm = null;
		MoleculeFact mf = (MoleculeFact)fact; //FIXME handle relation facts);    	
    	if (mf.listMembershipMolecules().size() > 0)
    		mm = (MembershipMolecule)leFactory.createLogicalExpression(mf.listMembershipMolecules().iterator().next().toString());
    	Set<AttributeValueMolecule> avms = new HashSet<AttributeValueMolecule>();
    	for (AttributeValueMolecule avm : mf.listAttributeValueMolecules()) {
    		avms.add((AttributeValueMolecule)leFactory.createLogicalExpression(avm.toString()));
    	}
    	//TODO write this more elegant once factory convenience creators are fixed
    	Set<MembershipMolecule> mms = new HashSet<MembershipMolecule>();
    	if (mf.listMembershipMolecules().size() > 0)
   	    	mms.add(mm);
    	return cFactory.facts.createMoleculeFact(mms, avms);
    }
       
    /* (non-Javadoc)
	 * @see ie.deri.wsmx.asm.State#add(org.omwg.logicalexpression.AttributeValueMolecule)
	 */
    public void add(AttributeValueMolecule fact) {
        facts.add(cFactory.facts.createMoleculeFact(new HashSet<MembershipMolecule>(), new HashSet<AttributeValueMolecule>(Arrays.asList(new AttributeValueMolecule[]{fact}))));
        Axiom axiom = factory.createAxiom(factory.createIRI("http://www.wsmx.org/choreography/state/attribute" + attributeCounter++));
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
		for (StateModificationListener listener : listeners) {
			listener.addedAttribute(this, fact);
		}
    }

    public void remove(AttributeValueMolecule fact) {
        //facts.add(cFactory.facts.createCompoundFact(new HashSet<MembershipMolecule>(), new HashSet<AttributeValueMolecule>(Arrays.asList(new AttributeValueMolecule[]{fact}))));
        Axiom axiom = null;
    	for (Entry<Molecule, Axiom> e : axioms.entrySet()) {
        	if (e.getKey().equals(fact))
        		axiom = e.getValue();
		}
    	if (axiom == null)
    		return;
        try {
        	logger.debug("Removing AVM axiom: " + fact);
			state.removeAxiom(axiom);
			axioms.remove(fact);
		} catch (SynchronisationException e) {
			logger.warn("Failed to remove attribute value from state.", e);
		} catch (InvalidModelException e) {
			logger.warn("Failed to remove attribute value from state.", e);
		}
		//FIXME extend listener for removal events
//		for (StateModificationListener listener : listeners) {
//			listener.r(this, fact);
//		}
    }

    public void remove(MembershipMolecule fact) {
    	facts.remove(fact);
    }
    
    /* (non-Javadoc)
	 * @see ie.deri.wsmx.asm.State#add(org.omwg.ontology.Instance)
	 */
    public Grounding add(Instance instance)
    		throws SynchronisationException, InvalidModelException, UpdateIllegalException {
    	//FIXME check if instance is of correct mode and throw UpdateIllegal if not
    	
    	state.addInstance(instance);
    	try {
    		reasoner.deRegisterOntology((IRI)(state.getIdentifier()));
    	} catch (Throwable t) {
    		//silent
    		//TODO log warn
    	}
		try {
			reasoner.registerOntology(state);
		} catch (InconsistencyException e1) {
			logger.warn("Ontology registration failed:" + e1.getMessage(), e1);
		}
    	for (In mode : signature.listInModes()) {
    		if (reasoner.isMemberOf((IRI)state.getIdentifier(), instance, mode.getConcept()))
				try {
					return mode.getGrounding().iterator().next();
				} catch (NotGroundedException e) {
					//FIXME
					return null;
				}    				
		}
    	for (Out mode : signature.listOutModes()) {
    		if (reasoner.isMemberOf((IRI)state.getIdentifier(), instance, mode.getConcept()))
				try {
					return mode.getGrounding().iterator().next();
				} catch (NotGroundedException e) {
					return null;
				}    				
		}

    	throw new UpdateIllegalException();
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
		MoleculeFact mf = (MoleculeFact)fact; //FIXME handle relation facts);
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
		logger.debug("Executing ground query " + condition + " over " + state.getIdentifier() +
			" with " + state.listInstances().size() + " instances and " + state.listAxioms().size()
			+ " axioms.");
		try {
			reasoner.deRegisterOntology((IRI)(state.getIdentifier()));
		} catch (Throwable t) {
			//silent
			//TODO log warn
		}
		try {
			reasoner.registerOntology(state);
		} catch (InconsistencyException e) {
			logger.warn("Ontology registration failed.", e);
		}
		return reasoner.executeGroundQuery((IRI)state.getIdentifier(), condition.getRestrictedLogicalExpression());
	}

	public boolean anonymousToVariableRewrite(LogicalExpression condition) {
		if (condition instanceof MembershipMolecule) {
			MembershipMolecule mm = (MembershipMolecule)condition;
			Term leftParameter = mm.getLeftParameter();
			Term rightParameter = mm.getRightParameter();
			
		}
		return false;
	}
	
	public boolean holds(LogicalExpression condition) {
		logger.info("Executing ground query " + condition + " over " + state.getIdentifier() +
			" with " + state.listInstances().size() + " instances and " + state.listAxioms().size()
			+ " axioms.");
		try {
			reasoner.deRegisterOntology((IRI)(state.getIdentifier()));
		} catch (Throwable t) {
			//silent
			//TODO log warn
		}
		try {
			reasoner.registerOntology(state);
		} catch (InconsistencyException e) {
			logger.warn("Ontology registration failed.", e);
		}
		return reasoner.executeGroundQuery((IRI)state.getIdentifier(), condition);
	}

	public Set<Map<Variable, Term>> retrieveBinding(Condition condition) {
		logger.debug("Executing query " + condition + " over " + state.getIdentifier() +
			" with " + state.listInstances().size() + " instances and " + state.listAxioms().size()
			+ " axioms.");
		try {
			reasoner.deRegisterOntology((IRI)(state.getIdentifier()));
		} catch(Exception e) {
			//allow for kaon
		}
		
		try {
			reasoner.registerOntology(state);
		} catch (InconsistencyException e) {
			logger.warn("Ontology registration failed.", e);
		}
		Set<Map<Variable, Term>> instances = reasoner.executeQuery((IRI)state.getIdentifier(), condition.getRestrictedLogicalExpression());		
		return instances;
	}
	
	public Instance getInstance(Identifier id, Concept concept) {
		reasoner.deRegisterOntology((IRI)(state.getIdentifier()));
		try {
			reasoner.registerOntology(state);
		} catch (InconsistencyException e) {
			logger.warn("Ontology registration failed.", e);
		}
		Set<Instance> instances = reasoner.getInstances((IRI)state.getIdentifier(), concept);
		Instance instance = null;
		for (Instance i : instances)
			if (i.getIdentifier().equals(id))
				instance = i;
		for (Entry<Molecule, Axiom> e : axioms.entrySet()) {
			if (e.getKey() instanceof AttributeValueMolecule) {
				AttributeValueMolecule avm = (AttributeValueMolecule) e.getKey();
				Axiom axiom = e.getValue();
				
				if (avm.getLeftParameter().equals(instance.getIdentifier())) {
					try {
						instance.addAttributeValue((IRI)avm.getAttribute(),(Value)avm.getRightParameter());
					} catch (Exception e2) {
						logger.warn("Failed to pseudo-reason.", e2);
					}
				}
			}
		}
		
		return instance;
	}
	
    public boolean isControlled(Term instance) {
    	if (instance instanceof IRI)
	    	return isControlled(factory.createInstance((IRI)instance));			
    	if (instance instanceof UnnumberedAnonymousID)
	    	return isControlled(factory.createInstance((UnnumberedAnonymousID)instance));			
    	//FIXME?
    	return false;
    }
    
    public boolean isControlled(Instance instance) {
    	if (instance.getIdentifier() instanceof UnnumberedAnonymousID)
    		return false;
    	for (Controlled mode : signature.listControlledModes()) {
    		if (reasoner.isMemberOf((IRI)state.getIdentifier(), instance, mode.getConcept()))
    			return true;
		}
    	return false;
    }

    public boolean isControlledConcept(Term concept) {
    	if (concept instanceof IRI)
	    	return isControlledConcept(factory.createConcept((IRI)concept));			
    	if (concept instanceof UnnumberedAnonymousID)
	    	return isControlledConcept(factory.createConcept((UnnumberedAnonymousID)concept));			
    	//FIXME?
    	return false;
    }
    
    public boolean isControlledConcept(Concept concept) {
    	for (Controlled mode : signature.listControlledModes()) {
    		if (concept.equals(mode.getConcept()))
    			return true;
		}
    	return false;
    }    

    public boolean isOut(Term instance) {
    	if (instance instanceof IRI)
	    	return isOut(factory.createInstance((IRI)instance));			
    	if (instance instanceof UnnumberedAnonymousID)
	    	return isOut(factory.createInstance((UnnumberedAnonymousID)instance));			
    	//FIXME?
    	return false;
    }
    
    public boolean isOut(Instance instance) {
    	if (instance.getIdentifier() instanceof UnnumberedAnonymousID)
    		return false;    	
    	for (Out mode : signature.listOutModes()) {
    		if (reasoner.isMemberOf((IRI)state.getIdentifier(), instance, mode.getConcept()))
    			return true;
		}
    	return false;
    }

    public boolean isOutConcept(Term concept) {
    	if (concept instanceof IRI)
	    	return isOutConcept(factory.createConcept((IRI)concept));			
    	if (concept instanceof UnnumberedAnonymousID)
	    	return isOutConcept(factory.createConcept((UnnumberedAnonymousID)concept));			
    	//FIXME?
    	return false;
    }
    
    
    public boolean isOutConcept(Concept concept) {
    	for (Out mode : signature.listOutModes()) {
    		if (concept.equals(mode.getConcept()))
    			return true;
		}
    	return false;
    }

	public WsmoFactory getFactory() {
		return factory;
	}

	public void setFactory(WsmoFactory factory) {
		this.factory = factory;
	}

	public StateSignature getSignature() {
		return signature;
	}

	public void setSignature(StateSignature signature) {
		this.signature = signature;
	}    

	
}
