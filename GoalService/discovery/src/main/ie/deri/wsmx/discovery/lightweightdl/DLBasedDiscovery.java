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

package ie.deri.wsmx.discovery.lightweightdl;

import ie.deri.wsmx.discovery.*;
import ie.deri.wsmx.discovery.util.Discovery;

import java.util.*;

import org.apache.log4j.*;
import org.deri.wsmo4j.logicalexpression.util.*;
import org.deri.wsmo4j.validator.*;
import org.omwg.logicalexpression.*;
import org.omwg.logicalexpression.terms.*;
import org.omwg.ontology.*;
import org.wsml.reasoner.api.*;
import org.wsml.reasoner.api.inconsistency.*;
import org.wsml.reasoner.impl.*;
import org.wsmo.common.*;
import org.wsmo.common.exception.*;
import org.wsmo.execution.common.exception.*;
import org.wsmo.factory.*;
import org.wsmo.service.*;
import org.wsmo.validator.*;

/**
 * DL Based Discovery
 * 
 * <p>We interpret the conjunction of the postcondition and effects as
 * a concept description and then check for the different kinds of matches</p>
 * 
 * <p><b>Example:<b>
 * <pre>
 * webService ws1 capability capws1 sharedVariables ?x
 * effect ?x memberOf Car
 * 
 * WebService ws2 capability capws2 sharedVariables ?x
 * effect ?x memberOf Vehicle
 * 
 * Goal g1 capability capg1 sharedVariables ?x
 * effect ?x memberOf Car
 * 
 * Goal g2 capability capg1
 * importsOntology o1
 * 
 * Ontology o1
 * concept Car subConceptOf Vehicle
 * <pre>
 * </p>
 * 
 * 
 *
 * <pre>
 * Created on 14.09.2006
 * Committed by $Author: holgerlausen $
 * $Source: /cvsroot/wsmx/components/discovery/src/main/ie/deri/wsmx/discovery/lightweightdl/DLBasedDiscovery.java,v $,
 * </pre>
 *
 * @author Holger Lausen
 *
 * @version $Revision: 1.8 $ $Date: 2007/02/06 14:15:35 $
 */
public class DLBasedDiscovery extends  AbstractWSMODiscoveryImpl{
    
    private static int id = 0;
    private int getUniqueId(){return id++;}
    
    private IRI strategyIRI;
    private IRI typeOfMatchIRI;
    private IRI exactIRI;
    private IRI pluginIRI;
    private IRI intersectIRI;
    private IRI subsumesIRI;
    
    private IRI lightWeightIRI;
    
    private String SERVICE_PREFIX = "urn:serviceOntology#";
    private String SERVICECONCEPT_PREFIX = "urn:serviceConcept#";
    private WSMLReasoner wsmlReasoner = DefaultWSMLReasonerFactory.getFactory().createWSMLDLReasoner();
    private WsmlValidator validator = new WsmlValidatorImpl();
    private WsmoFactory wsmoFactory;
    private LogicalExpressionFactory leFactory = Factory.createLogicalExpressionFactory(null);
    private Map<WebService, Ontology> registeredService = new HashMap<WebService,Ontology>();
    private Logger log = Logger.getLogger(DLBasedDiscovery.class);


    
    /**
     * 
     */
    public DLBasedDiscovery() {
        wsmoFactory = Factory.createWsmoFactory(null);
        strategyIRI = wsmoFactory.createIRI(Discovery.STRATEGY);
        typeOfMatchIRI = wsmoFactory.createIRI(Discovery.TYPE_OF_MATCH);
        exactIRI = wsmoFactory.createIRI(Discovery.EXACT);
        pluginIRI = wsmoFactory.createIRI(Discovery.PLUGIN);
        intersectIRI = wsmoFactory.createIRI(Discovery.INTERSECT);
        subsumesIRI = wsmoFactory.createIRI(Discovery.SUBSUMES);
        lightWeightIRI = wsmoFactory.createIRI(Discovery.LEIGHTWEIGHT);
    }
    
    
    public List<WebService> discover(Goal goal, Set<WebService> searchSpace) throws DiscoveryException {
        log.info("DL Discovery with "+searchSpace.size()+
                " services in Repository");
        if (goal.getCapability()==null){
            throw new DLDiscoveryValidationException("To be used for DL based " +
                    "discovery goal must have a capability.");
        }
        
        //first convert GOAL.
        String ID = getUniqueId()+"";
        IRI goalID = wsmoFactory.createIRI("goal:"+ID);
        
        //building concept expression
        LogicalExpression rightHandSide = extractConceptDescription(goal.getCapability());
        Variable v = extractRootVariable(goal.getCapability().listSharedVariables(), rightHandSide);
        LogicalExpression leftHandSide = leFactory.createMemberShipMolecule(v, goalID);
        
        //adding expression and ontologies...
        Ontology goalOntology = wsmoFactory.createOntology(wsmoFactory.createIRI("goal:ontology#"+ID));
        Axiom a = wsmoFactory.createAxiom(wsmoFactory.createAnonymousID());
        try{goalOntology.addAxiom(a);}
        catch(Exception e){throw new RuntimeException("never will happen?",e);}
        a.addDefinition(leFactory.createEquivalence(leftHandSide, rightHandSide));
        
        //adding imports
        if (goal.listOntologies()!=null)
            for (Iterator i = goal.listOntologies().iterator(); i.hasNext();){
                goalOntology.addOntology((Ontology)i.next());
            }

        if (goal.getCapability().listOntologies()!=null)
            for (Iterator i = goal.getCapability().listOntologies().iterator(); i.hasNext();){
                goalOntology.addOntology((Ontology)i.next());
            }
        
        List errors = new ArrayList();
        if (!validator.isValid(goalOntology, WSML.WSML_DL, errors, null)){
            //System.out.println(errors.get(0));
            throw new DLDiscoveryValidationException("Goal is not WSML DL, first error: "+errors.get(0),errors);
        }
        
        List<WebService> result = new ArrayList<WebService>(); 
        
        //iterate over all registered Services
        for (Iterator i1 = searchSpace.iterator(); i1.hasNext();){
            WebService ws = (WebService)i1.next();
            Identifier id = ws.getIdentifier();
            log.info(" checking WS "+id);
            Ontology wsOntology = registeredService.get(ws);
            Ontology tmpOntology = wsmoFactory.createOntology(
                    wsmoFactory.createIRI("urn:temp/"+getUniqueId()));
            tmpOntology.addOntology(goalOntology);
            tmpOntology.addOntology(wsOntology);
//            printTE(goalOntology);
//            printTE(wsOntology);
            try{
                wsmlReasoner.registerOntology(tmpOntology); 
            }catch (InconsistencyException e){
                log.debug("  NO MATCH (WS is inconsistent with goal) ");
                //this pair does not match
                continue;
            }
            
            Concept goalConcept = wsmoFactory.createConcept(goalID);
            Concept wsConcept = wsmoFactory.createConcept(wsmoFactory.createIRI(
                    SERVICECONCEPT_PREFIX+wsOntology.getIdentifier().toString().substring(SERVICE_PREFIX.length())));
            
            log.debug("  comparing "+goalConcept.getIdentifier()+" with "+wsConcept.getIdentifier());
            Setrelation match = getRelationBetween(tmpOntology, goalConcept, wsConcept);
            if (match!=Setrelation.disjoint){
                addNFP(ws, match);
                result.add(ws);
            }

        }
        return result;    
    }

    
    private enum Setrelation {equivalent, subsume, plugin, intersect, disjoint};
    private Setrelation getRelationBetween(Ontology ontology, Concept c1, Concept c2){
        IRI iri = (IRI) ontology.getIdentifier();
        //EQUIVALENT
        if (wsmlReasoner.isEquivalentConcept(iri,c1,c2)){
            return Setrelation.equivalent;
        }
        //SUBSUMES
        else if (wsmlReasoner.isSubConceptOf(iri,c1,c2)){
            return Setrelation.plugin;
        }
        //PLUGIN
        else if (wsmlReasoner.isSubConceptOf(iri,c2,c1)){
            return Setrelation.subsume;
        }
        //INTERSECT
        else {
            Term instance = leFactory.createAnonymousID((byte)1);
            Molecule gm = leFactory.createMemberShipMolecule(instance, c1.getIdentifier());
            Molecule wsm = leFactory.createMemberShipMolecule(instance, c2.getIdentifier());
            Conjunction con = leFactory.createConjunction(gm, wsm);
            if (wsmlReasoner.entails(iri,con)){
                return Setrelation.intersect;
            }
        }
        return Setrelation.disjoint;
    }

    /**
     * extract root variable from expression and compares to sharedVariables in goal
     * @param goal
     * @param rightHandSide
     * @return
     */
    private Variable extractRootVariable(Set sharedVariables, LogicalExpression rightHandSide) 
    throws DLDiscoveryValidationException{
        //doesn't work!?
        Variable v = new GetRootUtil().getRootVariable(rightHandSide);
        
        if (sharedVariables.size()==0) {
            log.warn("sharedVariable should indicate which variable describes this concept.");
            if (v==null) throw new DLDiscoveryValidationException("require single sahred var");
        }else{
            if (sharedVariables.size()>1){
                log.warn("only one sharedVariable expected, ignoring the rest");
            }
            //take first as shared
            Variable temp = (Variable) sharedVariables.iterator().next();
            if (v==null) v=temp;
            if (!temp.equals(v)){
                log.warn("shared variable appears not to be valid wrt. expression - ignoring");
            }
        }
        return v;
    }

    public List<WebService> discover(Goal goal) throws ComponentException,
            UnsupportedOperationException {
        return discover(goal, registeredService.keySet());
    }

    public Map<Map<WebService, Interface>, Identifier> discover(Goal arg0,
            Ontology arg1) throws ComponentException,
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    private void addNFP(WebService ws,Setrelation match){
        removeNFP(ws);
        if (match==Setrelation.disjoint)return;
        IRI typeOfMatch = intersectIRI;
        if (match==Setrelation.equivalent) typeOfMatch=exactIRI;
        if (match==Setrelation.subsume) typeOfMatch=subsumesIRI;
        if (match==Setrelation.plugin) typeOfMatch=pluginIRI;
        try{
            ws.addNFPValue(typeOfMatchIRI, typeOfMatch);
            ws.addNFPValue(strategyIRI, lightWeightIRI);
        }catch (InvalidModelException e){
            throw new RuntimeException("should never happen",e);
        }
    }
    
    private void removeNFP(WebService ws){
        try{
            ws.removeNFP(typeOfMatchIRI);
            ws.removeNFP(strategyIRI);
        }catch (InvalidModelException e){
            throw new RuntimeException("should never happen",e);
        }
    }

    /**
     * taking out the postcondition only (musst be concept expression)
     * using all importing ontologies 
     * creating remporary ontology
     */
    public void addWebService(WebService service) throws DiscoveryException {
        String ID = getUniqueId()+"";
        if (service==null) 
            throw new DLDiscoveryValidationException("No service given to register");
        if (service.getCapability()==null) 
            throw new DLDiscoveryValidationException("tried to register service without capability");
        
        IRI serviceID = wsmoFactory.createIRI(SERVICECONCEPT_PREFIX+ID);
        
        Ontology wsOntology = wsmoFactory.createOntology(
                wsmoFactory.createIRI(SERVICE_PREFIX+ID));
        for (Iterator i = service.listOntologies().iterator(); i.hasNext();){
            wsOntology.addOntology((Ontology)i.next());
        }
        for (Iterator i = service.getCapability().listOntologies().iterator(); i.hasNext();){
            wsOntology.addOntology((Ontology)i.next());
        }
        Axiom a = wsmoFactory.createAxiom(wsmoFactory.createAnonymousID());
        try{wsOntology.addAxiom(a);}
        catch(Exception e){throw new RuntimeException("never will happen?",e);}
        
        LogicalExpression rightHandSide = extractConceptDescription(service.getCapability());
        Variable v = extractRootVariable(service.getCapability().listSharedVariables(), rightHandSide);
        LogicalExpression leftHandSide = leFactory.createMemberShipMolecule(v, serviceID);
        a.addDefinition(leFactory.createEquivalence(leftHandSide, rightHandSide));

        List errors = new ArrayList();
        if (!validator.isValid(wsOntology, WSML.WSML_DL, errors, null)){
            throw new DLDiscoveryValidationException("Web Service is not WSML DL: "+errors.get(0),errors);
        }
        
        registeredService.put(service, wsOntology);
    }

    public void removeWebService(WebService service) {
        registeredService.remove(service);
    }
    
    private LogicalExpression extractConceptDescription(Capability cap) throws DiscoveryException{
        LogicalExpression object = null;
        //make a conjunction of all postconditions in capability
        for (Iterator i1 = cap.listEffects().iterator(); i1.hasNext();){
            Axiom capAx = (Axiom) i1.next();
            for (Iterator i2 = capAx.listDefinitions().iterator(); i2.hasNext();){
                LogicalExpression le = (LogicalExpression) i2.next();
                if (object == null){
                    object=le;
                }else{
                    object=leFactory.createConjunction(object, le);
                }
            }
        }
        for (Iterator i1 = cap.listPostConditions().iterator(); i1.hasNext();){
            Axiom capAx = (Axiom) i1.next();
            for (Iterator i2 = capAx.listDefinitions().iterator(); i2.hasNext();){
                LogicalExpression le = (LogicalExpression) i2.next();
                if (object == null){
                    object=le;
                }else{
                    object=leFactory.createConjunction(object, le);
                }
            }
        }
        if (object==null){
            throw new DiscoveryException("Tried to register capbility without postcondition and effects");
        }
        return object;
    }
    
    private void printTE(TopEntity te){
        StringBuffer buf = new StringBuffer();
        Factory.createSerializer(null).serialize(new TopEntity[]{te}, buf);
        System.out.println(buf);
    }
}
