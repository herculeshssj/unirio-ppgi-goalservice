/*
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.deri.wsmx.mediation.ooMediator.wsml;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.wsmo4j.io.serializer.wsml.LogExprSerializerWSML;
import org.deri.wsmx.mediation.ooMediator.functoids.DefaultServiceResolver;
import org.deri.wsmx.mediation.ooMediator.functoids.ServiceResolver;
import org.deri.wsmx.mediation.ooMediator.logging.DataMediatorOutputStream;
import org.deri.wsmx.mediation.ooMediator.mapper.Mappings;
import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.terms.ConstructedTerm;
import org.omwg.logicalexpression.terms.NumberedAnonymousID;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.Axiom;
import org.omwg.ontology.Concept;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Type;
import org.omwg.ontology.Value;
import org.omwg.ontology.Variable;
import org.wsml.reasoner.api.WSMLReasoner;
import org.wsml.reasoner.api.WSMLReasonerFactory;
import org.wsml.reasoner.api.inconsistency.InconsistencyException;
import org.wsml.reasoner.impl.DefaultWSMLReasonerFactory;
import org.wsml.reasoner.impl.WSMO4JManager;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;
import org.wsmo.wsml.Serializer;

import com.ontotext.wsmo4j.ontology.InstanceImpl;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 29-Apr-2006
 * Committed by $Author: adrian.mocan $
 * 
 * $Source: /var/repository/wsmx-datamediator/src/main/java/org/deri/wsmx/mediation/ooMediator/wsml/MediationWSML2Reasoner.java,v $, 
 * @version $Revision: 1.5 $ $Date: 2008/02/26 03:20:26 $
 */

public class MediationWSML2Reasoner implements MediationReasoner {

    public static String ontologyName = "merged_ontology";
	public static String reasoningSpace = "http://www.wsmx.org/datamediator/runtime/merged_ontology";
	
	public static String transformationServicesSpace = "http://www.wsmx.org/datamediator/runtime/services";
	
    private IRI mergedOntologyIRI = null;
    private Ontology mergedOntology = null;
    
    private int evalMethod = 3;
    public static int allowImports = 0;
    private WSMLReasoner wsmlReasoner = null;
    
    private WSMLReasonerFactory.BuiltInReasoner reasoner =  WSMLReasonerFactory.BuiltInReasoner.IRIS;
    private LogExprSerializerWSML logExprSerializer = null;
    private WsmoFactory wsmoFactory = null;
    private LogicalExpressionFactory leFactory = null;
    private DataFactory dataFactory = null;
    private WSMO4JManager wsmoManager = null;
    
    private Parser wsmlparserimpl = null;
    private Serializer ontologySerializer = null;
    
    private HashMap <Pair<Term, Concept>, Value> processedTerms = new HashMap <Pair<Term, Concept>, Value>();

    private boolean printOntologies;
    private boolean printQueries;
    private boolean writeOntology;
    private boolean transformOnlyConnectedInstances;
    
    private Map <String, String> namespaceMap = new HashMap <String, String> ();
    private Map <String, String> reverseNamespaceMap = new HashMap <String, String> ();
    private Map <IRI, Instance> cloned = new HashMap <IRI, Instance> ();
    private DataMediatorOutputStream outputStream;

    /**
     * @param writeOntology 
     * @param printQueries 
     * @param printOntologies 
     * 
     */
    public MediationWSML2Reasoner(Map <String, Boolean> flags, DataMediatorOutputStream theOutputStream) {
        this.outputStream = theOutputStream;
        initReasoner();
        initMergedOntology();
        
        if (flags.containsKey(MediationFlags.PRINT_ONTOLOGIES_TO_CONSOLE)){
            printOntologies = flags.get(MediationFlags.PRINT_ONTOLOGIES_TO_CONSOLE);
        }
        if (flags.containsKey(MediationFlags.WRITE_MERGED_ONTOLOGY_TO_FILE)){
            writeOntology = flags.get(MediationFlags.WRITE_MERGED_ONTOLOGY_TO_FILE);
        }
        if (flags.containsKey(MediationFlags.PRINT_QUERIES_TO_CONSOLE)){
            printQueries = flags.get(MediationFlags.PRINT_QUERIES_TO_CONSOLE);
        }
        if (flags.containsKey(MediationFlags.TRANSFORM_ONLY_CONNECTED_INSTANCES)){
            transformOnlyConnectedInstances = flags.get(MediationFlags.TRANSFORM_ONLY_CONNECTED_INSTANCES);
        }
    }
    
    /**
     * 
     */
    private void initMergedOntology() {
        mergedOntologyIRI = WSMOUtil.createIRI(reasoningSpace + "#" + ontologyName + "_" + WSMOUtil.generateUniqueID());
        
        mergedOntology = WSMOUtil.wsmoFactory.createOntology(mergedOntologyIRI);
        mergedOntology.setDefaultNamespace(WSMOUtil.wsmoFactory.createNamespace("", WSMOUtil.createIRI(reasoningSpace)));
        mergedOntology.setWsmlVariant("http://www.wsmo.org/wsml/wsml-syntax/wsml-rule");
    }

    private void initReasoner(){
        //wsmoManager = new WSMO4JManager();
        leFactory = WSMOUtil.leFactory;//wsmoManager.getLogicalExpressionFactory();
        wsmoFactory = WSMOUtil.wsmoFactory;//wsmoManager.getWSMOFactory();
        dataFactory = WSMOUtil.dataFactory;//wsmoManager.getDataFactory();
        // Set up WSML parser
        wsmlparserimpl = WSMOUtil.parser;//org.wsmo.factory.Factory.createParser(null);
        // Set up serializer
        ontologySerializer = /*WSMOUtil.serializer;*/org.wsmo.factory.Factory.createSerializer(null);

        // Create reasoner
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER, reasoner);
        //params.put(WSMLReasonerFactory.PARAM_EVAL_METHOD,evalMethod);
        params.put(WSMLReasonerFactory.PARAM_ALLOW_IMPORTS,allowImports);
        wsmlReasoner = DefaultWSMLReasonerFactory.getFactory()
                .createWSMLRuleReasoner(params); 
    }
    
    public void refreshKB(){
        //wsmlReasoner.deRegisterOntology(mergedOntologyIRI);
        try {

            Set<Ontology> ontologiesNeededForReasoning = getOntologiesNeededForReasoning(mergedOntology);
            if (printOntologies){
                serialize(ontologiesNeededForReasoning, outputStream);
            }
            if (writeOntology){
                serializeWSMLRules(mergedOntology);
            }
            
            wsmlReasoner.registerOntologies(ontologiesNeededForReasoning);
//            for (Ontology o: ontologiesNeededForReasoning){
//            	wsmlReasoner.registerOntology(o);
//            }
            
        } catch (InconsistencyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }
    
       
    private Set<Ontology> getOntologiesNeededForReasoning(Ontology theOntology) {
        Set <Ontology> ontologiesNeededForReasoning = new HashSet <Ontology> ();
        ontologiesNeededForReasoning.addAll(getImportedOntologies(theOntology));
        ontologiesNeededForReasoning.add(theOntology);
        return ontologiesNeededForReasoning;
    }

    private Set <Ontology> getImportedOntologies(Ontology theOntology){
        Set<Ontology> importedOntologies = new HashSet <Ontology> ();
        getImportedOntologies(theOntology, importedOntologies);
        return importedOntologies;
    }
    
    private void getImportedOntologies(Ontology theOntology, Set<Ontology> theImportedOntologies){
        for (Ontology importedOntology : (Set<Ontology>) theOntology.listOntologies()) {
            if (!theImportedOntologies.contains(importedOntology)) {
                theImportedOntologies.add(importedOntology);
                getImportedOntologies(importedOntology, theImportedOntologies);
            }
        }
    }
	public List<Set<Instance>> getInstances(Set<Concept> targetConceptsSet) throws ParserException{
        List<Set<Instance>> result = new ArrayList<Set<Instance>>();
        Iterator<Concept> itC = targetConceptsSet.iterator();
        while(itC.hasNext()){
            result.add(getInstance(itC.next()));
        }
        
        return result;
    }
    
    public List<Set<Instance>> getInstances(Set<Concept> targetConceptsSet, Set<Concept> sourceConceptsSet) throws ParserException{
        List<Set<Instance>> result = new ArrayList<Set<Instance>>();
        Iterator<Concept> itC = targetConceptsSet.iterator();
        while(itC.hasNext()){
            result.add(getInstance(itC.next(), sourceConceptsSet));
        }
        
        return result;
    }
    
    
    public Set<Instance> getInstance(Concept c, Set<Concept> sourceConceptsSet) throws ParserException{
        //String query = "?x memberOf " + itC.next().getIdentifier();
        Set<Instance> result = new HashSet<Instance>();
        Variable x = WSMOUtil.leFactory.createVariable("x");
        LogicalExpression le = WSMOUtil.leFactory.createMemberShipMolecule(x, c.getIdentifier());
        Set<Map<Variable, Term>> queryResult = performQuery(le);
        Iterator<Map<Variable, Term>> itBindings = queryResult.iterator();
        while (itBindings.hasNext()){
            Term crtTerm = itBindings.next().get(x);
            Instance resultInstance = buildInstance(crtTerm, c, sourceConceptsSet);
            result.add(resultInstance);
        }        
        return result;
    }

    public Set<Instance> getInstance(Concept c) throws ParserException{
        //String query = "?x memberOf " + itC.next().getIdentifier();
        Set<Instance> result = new HashSet<Instance>();
        Variable x = WSMOUtil.leFactory.createVariable("x");
        LogicalExpression le = WSMOUtil.leFactory.createMemberShipMolecule(x, c.getIdentifier());
        Set<Map<Variable, Term>> queryResult = performQuery(le);
        Iterator<Map<Variable, Term>> itBindings = queryResult.iterator();
        while (itBindings.hasNext()){
            Term crtTerm = itBindings.next().get(x);
            processedTerms = new HashMap<Pair<Term, Concept>, Value>();
            Instance resultInstance = buildInstance(crtTerm, c);
            if (resultInstance!=null)
                result.add(resultInstance);
        }        
        return result;
    }

    /**
     * @param term
     * @return
     * @throws ParserException 
     */
    private Instance buildInstance(Term term, Concept concept) throws ParserException {
        
        Instance result = null;
        if (term instanceof ConstructedTerm){
            ConstructedTerm constructedTerm = (ConstructedTerm)term;
            
            String parameters = "";
            Iterator itP = constructedTerm.listParameters().iterator();
            while (itP.hasNext()){
               Term crtP = (Term)itP.next();

               String aParameter = crtP.toString();
               String prefix = aParameter.substring(0, aParameter.indexOf('#') + 1);
               if (reverseNamespaceMap.containsKey(prefix)){
                   aParameter = aParameter.replaceFirst(prefix, reverseNamespaceMap.get(prefix));
               }
               aParameter = aParameter.replace('#', '/');
                             
               parameters = parameters + aParameter;
               if (itP.hasNext())
                   parameters = parameters + ", ";
            }
            String instanceIRI = constructedTerm.getFunctionSymbol().toString() + "('" + parameters + "')";
            result = WSMOUtil.createInstance(instanceIRI, concept);
            processedTerms.put(new Pair<Term, Concept> (term, concept), result);  
        }
        
        if (term instanceof DataValue || term instanceof NumberedAnonymousID || term instanceof Variable){
            //return null;        
            String instanceIRIString = "";
            if (concept.getIdentifier() instanceof IRI)
                instanceIRIString = ((IRI)concept.getIdentifier()).getNamespace().toString();
            instanceIRIString = instanceIRIString + toAplphaNumeric(term.toString());
            return WSMOUtil.wsmoFactory.createInstance(WSMOUtil.createIRI(instanceIRIString));
        }
            
        return buildQuery(term, concept, result);
    }

    private Instance buildQuery(Term term, Concept concept, Instance result) throws ParserException{
        if (term instanceof Identifier )
            try {
                return WSMOUtil.wsmoFactory.createInstance((Identifier)term, concept);
            } catch (SynchronisationException e) {
                // TODO Auto-generated catch block
                return WSMOUtil.wsmoFactory.createInstance((Identifier)term);
            } catch (InvalidModelException e) {
                // TODO Auto-generated catch block
                return WSMOUtil.wsmoFactory.createInstance((Identifier)term);
            }
        
         Variable y = WSMOUtil.leFactory.createVariable("y");
         Variable z = WSMOUtil.leFactory.createVariable("z");
         Variable avC = WSMOUtil.leFactory.createVariable("avC");
         
         AttributeValueMolecule attrMolecule = WSMOUtil.leFactory.createAttributeValue(term, y, z);
         MembershipMolecule membershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(term, concept.getIdentifier());
         List moleculesList = new ArrayList();
         moleculesList.add(attrMolecule);
         moleculesList.add(membershipMolecule);
         LogicalExpression le = WSMOUtil.leFactory.createCompoundMolecule(moleculesList);
                  
         le = WSMOUtil.leFactory.createConjunction(le, WSMOUtil.leFactory.createMemberShipMolecule(z, avC));
         
         
         
         Set<Map<Variable, Term>> queryResult = performQuery(le);
         Iterator<Map<Variable, Term>> itBindings = queryResult.iterator();
         while (itBindings.hasNext()){
             Map<Variable, Term> crtBinding = itBindings.next();
             Identifier attributeId = (Identifier)crtBinding.get(y);
             Term valueTerm = crtBinding.get(z);
             
             Set attributesSet = concept.findAttributes(attributeId);
             if (attributesSet != null){
                 Iterator attrIt = attributesSet.iterator();
                 while (attrIt.hasNext()){
                     Attribute crtAttr = (Attribute)attrIt.next();
                     //Iterator rangeIt = crtAttr.listTypes().iterator();
                     ///----------------------------HERE!-------------------------
                     Iterator rangeIt = WSMOUtil.getSubRanges(crtAttr).iterator();
                     
                     Term avCTerm = crtBinding.get(avC);
                     Concept crtType = WSMOUtil.getConcept(avCTerm.toString());
                     
                     ///-------------------END_HERE---------------------------
                     //while (rangeIt.hasNext()){
                     if (crtType != null){                         
                         if (WSMOUtil.isAllowedRangeOfAttribute(crtAttr, crtType)){
//                             processedTerms = new HashMap<Term, Value>();
                             Value attributeValue = processedTerms.get(new Pair<Term, Concept>(valueTerm, crtType));
                             if (attributeValue == null){                                 
                                 attributeValue = buildInstance(valueTerm, crtType);
                             }
                             if (attributeValue!=null){
                                 try {
                                    result.addAttributeValue(attributeId, attributeValue);
                                } catch (SynchronisationException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                } catch (InvalidModelException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                             }                                 
                         }
                     }
                     else{
                    	 if (!avCTerm.toString().equals("http://www.wsmo.org/wsml/wsml-syntax#iri")){
	                         try {
	                        	//if the range is a type we can have as a value a simple DataValue or a ConstructedTerm 
	                        	//(the last one representing the transformation service call) 
	                        	 
	                        	if (valueTerm instanceof DataValue){ 
	                        		result.addAttributeValue(attributeId, (DataValue)valueTerm);
	                        	}
	                        	else
	                        		if (valueTerm instanceof ConstructedTerm){
	                        			//this is where the postprocessing step should be included
	                        			
	                        			ServiceResolver sr = DefaultServiceResolver.getServiceResolver();
	                        			ConstructedTerm cValueTerm = (ConstructedTerm)valueTerm;
	                        			
	                        			Object[] parameters = new Object[cValueTerm.listParameters().size()];
	                        			for (int i=0; i<cValueTerm.listParameters().size(); i++){
	                        				parameters[i] = cValueTerm.listParameters().get(i);
	                        			}                            			
	                        			//result.addAttributeValue(attributeId, WSMOUtil.createDataValue(((ConstructedTerm)valueTerm).toString()));
                                        Object value = sr.invokeService(cValueTerm.getFunctionSymbol().toString(), parameters);
                                        if (value != null){
                                            result.addAttributeValue(attributeId, WSMOUtil.createDataValueFromMediationService(value.toString()));
                                        }
	                        		}
	                        } catch (Exception e1) {
	                            // TODO Auto-generated catch block
	                            e1.printStackTrace();
	                        }
                    	 }
                    	 //the case where actual IRI are sued in the ontology as ranges for some attributes
                    	 else{
                    		 boolean hasIRIRange = false; 
                    		 for (Type type : crtAttr.listTypes()){
                    			 if (type.toString().equals("http://www.wsmo.org/wsml/wsml-syntax#iri")){
                    				 hasIRIRange = true; 
                     }
                 }
                    		 if (hasIRIRange){
                    			 try {
									result.addAttributeValue(attributeId, new InstanceImpl((IRI)valueTerm));
								} catch (SynchronisationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvalidModelException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
             }
         }
                    	 }
                     }
                 }
             }
         }
         return result;
    }

    /**
     * @param string
     * @return
     */
    private String toAplphaNumeric(String string) {
        int size = string.length();
        for (int i=0; i<size; i++){
            if (!Character.isLetterOrDigit(string.charAt(i))){
                string.replace(string.charAt(i), '_');
            }
        }
        return string;
    }


    private Instance buildInstance(Term term, Concept concept, Set<Concept> sourceConceptsSet) throws ParserException {
        
        Instance result = null;
        if (term instanceof ConstructedTerm){
            ConstructedTerm constructedTerm = (ConstructedTerm)term;
            
            String parameters = "";
            Iterator itP = constructedTerm.listParameters().iterator();
            int i = 0;
            while (itP.hasNext()){
               Term crtP = (Term)itP.next();
               //test if the first parameter is one of thr root instances from the source
               if (i==0){
                   if (crtP instanceof Identifier){
                       Instance inst = WSMOUtil.wsmoFactory.getInstance((Identifier)crtP);
                       //to be determined if it is necessary to test containsAll or just an intersection is sufficient
                       if (!sourceConceptsSet.containsAll(inst.listConcepts()))
                           return null; 
                   }
                       
               }                   
               
               String aParameter = crtP.toString();
               String prefix = aParameter.substring(0, aParameter.indexOf('#') + 1);
               if (reverseNamespaceMap.containsKey(prefix)){
                   aParameter = aParameter.replaceFirst(prefix, reverseNamespaceMap.get(prefix));
               }
               aParameter = aParameter.replace('#', '/');
               
               parameters = parameters + aParameter;
               if (itP.hasNext())
                   parameters = parameters + ", ";
            }
            String instanceIRI = constructedTerm.getFunctionSymbol().toString() + "('" + parameters + "')";
            result = WSMOUtil.createInstance(instanceIRI, concept);            
        }
        
        if (term instanceof DataValue || term instanceof NumberedAnonymousID || term instanceof Variable)
            return null;
            
        return buildQuery(term, concept, result);
    }    
    
    private Set<Map<Variable, Term>> performQuery(String query) throws ParserException{
        if (printQueries){
            outputStream.println("\n\nStarting reasoner with query '" + query + "'");
            outputStream.flush();            
        }
        //LogicalExpression qExpression = leFactory.createLogicalExpression(query, o);
        LogicalExpression qExpression = WSMOUtil.leFactory.createLogicalExpression(query);
        return performQuery(qExpression);
    }
    
    
    private Set<Map<Variable, Term>> performQuery(LogicalExpression query) throws ParserException{
        if (printQueries){
            outputStream.println("WSML Query LE:");
        }
        logExprSerializer = new LogExprSerializerWSML(mergedOntology);
        if (printQueries){
            outputStream.println(query.toString());
            outputStream.println("QQQQQQQQQQQQQQQQ");
        }
        
        Set<Map<Variable, Term>> result = wsmlReasoner.executeQuery((IRI)mergedOntology.getIdentifier(), query);
        
        if (printQueries){
            outputStream.println("Found < " + result.size()+ " > results to the query:");
            int i = 0;
            for (Map<Variable, Term> vBinding : result) {
                outputStream.print("(" + (++i) + ") -- ");
                boolean first = true;
                for (Variable v : vBinding.keySet()){
                    if (!first){
                        outputStream.print(", ");
                    }
                    first = false;
                    
                    Term t = vBinding.get(v);
                    outputStream.print(v + "=" + t);
                }
                outputStream.println();
            }
            outputStream.flush();
        }
        
        return result;     
       
    }
 
    public void addSourceInstances(Set<Instance> instances, Mappings theMappings) throws SynchronisationException, InvalidModelException{
        Iterator<Instance> itI = instances.iterator();
        while (itI.hasNext()){
            addInstance(itI.next(), theMappings);
        }
    }
    
    public void addInstance(Instance theSourceInstance, Mappings theMappings) throws SynchronisationException, InvalidModelException{
        String namespace = ((IRI) theSourceInstance.getIdentifier()).getNamespace();
        String replacementNS = namespace.replaceAll("#", "") + "_cloned#";
        
        if (namespaceMap.containsKey(namespace)){
            replacementNS = namespaceMap.get(namespace);
        }
        else{
            namespaceMap.put(namespace, replacementNS);
            reverseNamespaceMap.put(replacementNS, namespace);
        }
        InstanceCloner.clone(theSourceInstance, replacementNS, mergedOntology, theMappings, transformOnlyConnectedInstances, cloned);
    }
    
    public void addAxiom(Axiom axiom) throws SynchronisationException, InvalidModelException{
        mergedOntology.addAxiom(axiom);
        if (printOntologies){
            logExprSerializer = new LogExprSerializerWSML(mergedOntology);
            Iterator<LogicalExpression> itLE  = axiom.listDefinitions().iterator();
            while (itLE.hasNext()){
                outputStream.println("\n" + logExprSerializer.serialize(itLE.next()));
            }
            outputStream.flush();
        }
    }
    
    public void addAxioms(Set<Axiom> axioms) throws SynchronisationException, InvalidModelException{
        Iterator<Axiom> itA = axioms.iterator();
        while (itA.hasNext()){
            addAxiom(itA.next());
        }
    }
    
    public void addNamespace(Namespace ns){
        mergedOntology.addNamespace(ns);
    }
    
    public void addNamespaces(Set<Namespace> namespaces){
        Iterator<Namespace> it =  namespaces.iterator();
        while (it.hasNext()){
            addNamespace(it.next());
        }
    }
    
    public void addOntologies(Set<Ontology> ontologies){
    	for (Ontology ontology : ontologies){
    		addOntology(ontology);
    	}
    }
    
    public void addOntology(Ontology ontology){
    	mergedOntology.addOntology(ontology);
    }
    
    public static void serialize(Set<Ontology> ontologies, DataMediatorOutputStream theOutputStream){
    	for (Ontology ontology: ontologies){
            theOutputStream.println("-----------------------------------------------------------");
	        try {
				Factory.createSerializer(new HashMap()).serialize(new TopEntity[]{ontology}, new OutputStreamWriter(theOutputStream.getOutputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            theOutputStream.println("-----------------------------------------------------------");
            theOutputStream.flush();
        }
    }
    
    public static void serializeWSMLRules(Ontology o){
    	OutputStreamWriter osw = null;
    	File f = null;
    	try {
    		f = new File("wsml-rules.wsml.tmp");
			osw = new OutputStreamWriter(new FileOutputStream(f));
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			return;
		}
		//outputStream.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@: " + f.getAbsolutePath());
		try {
			Factory.createSerializer(new HashMap()).serialize(new TopEntity[]{o},osw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			osw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public Set<Namespace> getNamespaces(){
    	Set<Namespace> result = new HashSet<Namespace>();
    	
        if (mergedOntology.listNamespaces()!=null)
        	result.addAll(mergedOntology.listNamespaces());
        
        if (mergedOntology.getDefaultNamespace()!=null)
        	result.add(mergedOntology.getDefaultNamespace());
    	
    	return result;
    	
    }

    public Set<Instance> getSourceInstances() {
        return mergedOntology.listInstances();
    }
}
