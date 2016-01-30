/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
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

package ch.epfl.qosdisc.database;

import ie.deri.wsmx.commons.Helper;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.io.serializer.wsml.VisitorSerializeWSMLTerms;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.*;
import org.wsml.reasoner.api.WSMLReasoner;
import org.wsml.reasoner.api.WSMLReasonerFactory;
import org.wsml.reasoner.impl.DefaultWSMLReasonerFactory;
import org.wsml.reasoner.impl.WSMO4JManager;
import org.wsmo.common.IRI;
import org.wsmo.common.TopEntity;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Serializer;

/**
 * A variant on the reasoning context, relying on the WSMLStore for
 * its ontologies.
 * 
 * @author Sebastian Gerlach
 */
public class Reasoner {

    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(Reasoner.class);
    
    /**
     * Logical expression factory used for creating the queries that are passed to
     * the reasoner.
     */
    private LogicalExpressionFactory leFactory;

    /**
     * The reasoner used for processing the queries.
     */
    private WSMLReasoner reasoner;
    
    /**
     * The ontology in which everything is grouped.
     */
    private Ontology commonOntology;
    
    /**
     * Indicates whether the current state of the common ontology is registered
     * with the reasoner.
     */
    private boolean registered = false;
    
    /**
     * Unique identifier for temporary ontologies. 
     */
    private static int useCount = 0;
    
    /**
     * A WSMO factory for creating the objects that will populate our common ontology. 
     */
    private WsmoFactory factory;
    
    /**
     * The ontologies that are currently loaded in our common ontology.
     */
    private HashSet<String> loadedOntologies;
    
    /**
     * Original axiom mapping.
     */
    private Map<Ontology,Vector<Axiom>> axiomMap;
    
    /**
     * Original concept mapping.
     */
    private Map<Ontology,Vector<Concept>> conceptMap;
    
    /**
     * Original instance mapping. 
     */
    private Map<Ontology,Vector<Instance>> instanceMap;
    
    /**
     * Indicates whether WSMLReasoner actually supports imported ontologies. 
     */
    public final boolean NuSchool = false;
    
    /**
     * Constructor. Private for singleton usage.
     */
    public Reasoner() {
        
        // Create our WSMO factory.
        factory = Factory.createWsmoFactory(null);
        
        // Create a logical expression factory.
        leFactory = new WSMO4JManager().getLogicalExpressionFactory();

        // Create a reasoner. We use KAON2 here, since MINS does not work very well :-(
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER, WSMLReasonerFactory.BuiltInReasoner.IRIS);
        reasoner = DefaultWSMLReasonerFactory.getFactory().createWSMLFlightReasoner(params);
        
        registered = false;
        loadedOntologies = new HashSet<String>();
        
        if(!NuSchool) {
	        
        	// Create the common ontology.
	        commonOntology = factory.createOntology(factory.createIRI("http://localhost/ontologies/test/Common"+useCount+".wsml"));
	        ++useCount;

	        // Create the original maps.
	        axiomMap = new HashMap<Ontology,Vector<Axiom>>();
	        conceptMap = new HashMap<Ontology,Vector<Concept>>();
	        instanceMap = new HashMap<Ontology,Vector<Instance>>();
        }
    }
    
    /**
     * Add an ontology in the current context. This is not efficient in its current state,
     * but it should not be there in the first place. The reasoners should be able to handle
     * referenced ontologies themselves without requiring this kludge.
     * The elements are actually moved from the source ontology to the common ontology, and
     * need to be returned there later by calling clean.
     * 
     * @param ontologies The IRIs/namespaces of the ontologies to add.
     */
    public void addOntology(Collection<String> ontologies) {
        for(String ontology : ontologies) {
            addOntology(ontology);
        }
    }
    
    /**
     * Add an ontology in the current context. This is not efficient in its current state,
     * but it should not be there in the first place. The reasoners should be able to handle
     * referenced ontologies themselves without requiring this kludge.
     * The elements are actually moved from the source ontology to the common ontology, and
     * need to be returned there later by calling clean.
     * 
     * @param ont The IRI/namespace of the ontology to add.
     */
    public void addOntology(String iri) {
    	
        // Check whether this ontology is already present in the reasoning context.
        String namespace = iri;
        int ndx = iri.lastIndexOf('#');
        if(ndx != -1)
            namespace = namespace.substring(0,ndx+1);
        else
        	namespace = namespace + '#';
        
        if(loadedOntologies.contains(namespace))
            return;
        loadedOntologies.add(namespace);
        
        Collection<TopEntity> entities = new HashSet<TopEntity>();
        
        if (Helper.getOntology(iri) != null){
        	entities.add(Helper.getOntology(iri));
        } else {
        	// Make sure we have the real ontology, and not some silly proxy.
        	entities = WSMLStore.getEntities(iri,WSMLStore.ONTOLOGY);
        }
        
        // Verify that the ontology exists, ignore it otherwise.
        if(entities == null) {
        	log.warn("Could not load ontology from "+iri+" into context.");
        	return;
        }
        
        if(NuSchool)
        	commonOntology = (Ontology)entities.iterator().next();
        
        for(TopEntity e : entities) {
	        try {
	            
		        // Add critical components of the ontology to our context.
	        	Ontology ontology = (Ontology) e;
		        log.debug("Adding ontology "+ontology.getIdentifier().toString());        
	            
		        if(!NuSchool) {        
	
		        	// Add all axioms.
		            Vector<Axiom> axioms = new Vector<Axiom>();
		            for(Object a : ontology.listAxioms()) {
		                axioms.add((Axiom)a);
		                commonOntology.addAxiom((Axiom)a);
		            }
		            axiomMap.put(ontology,axioms);
		
		            // Add all concepts.
		            Vector<Concept> concepts = new Vector<Concept>();
		            for(Object a : ontology.listConcepts()) {
		                concepts.add((Concept)a);
		                commonOntology.addConcept((Concept)a);
		            }
		            conceptMap.put(ontology,concepts);
		
		            // Add all instances.
		            Vector<Instance> instances = new Vector<Instance>();
		            for(Object a : ontology.listInstances()) {
		                instances.add((Instance)a);
		                commonOntology.addInstance((Instance)a);
		            }
		            instanceMap.put(ontology,instances);
	
		        } else {
					reasoner.registerOntology(ontology);
		        }
		        
		        // Import all related ontologies as well.
		        for(Object ro : ontology.listOntologies())
		            addOntology(((Ontology)ro).getIdentifier().toString());
		        
	        } catch(Exception ex) {
	            ex.printStackTrace();
	        }
        }
        registered = false;        
    }
    
    /**
     * Returns all elements to their original ontologies.
     */
    public void clean() {
    	if(!NuSchool) {
	        try {
	            
	            // Return all axioms.
	            for(Map.Entry<Ontology,Vector<Axiom>> e : axiomMap.entrySet()) {
	                for(Axiom a : e.getValue())
	                    e.getKey().addAxiom(a);
	            }
	            
	            // Return all concepts.
	            for(Map.Entry<Ontology,Vector<Concept>> e : conceptMap.entrySet()) {
	                for(Concept a : e.getValue())
	                    e.getKey().addConcept(a);
	            }
	            
	            // Return all instances.
	            for(Map.Entry<Ontology,Vector<Instance>> e : instanceMap.entrySet()) {
	                for(Instance a : e.getValue())
	                    e.getKey().addInstance(a);
	            }
	        } catch(Exception ex) {
	            ex.printStackTrace();
	        }
    	}
    }
    
    /**
     * Convert a term to a string representation.
     * 
     * @param t The term to convert.
     * @return The string representation.
     */
    public String termToString(Term t){
        
        // Create a serializing visitor and pass the term through it.
        VisitorSerializeWSMLTerms v = new VisitorSerializeWSMLTerms(commonOntology);
        t.accept(v);
        return v.getSerializedObject();
    }
    
    /**
     * Execute a query on the reasoner.
     * 
     * @param queryString The query to execute.
     * @return The results of the query.
     */
    public Vector<Map<String,Term>> execute(String queryString) {
        
        // Prepare result structure.
        Vector<Map<String,Term>> rv = new Vector<Map<String,Term>>();
                
        try {
            
            // Register ontology if required.
            if(!registered) {
            	if(!NuSchool) {
            		
//            		String str = Helper.serializeTopEntity(commonOntology);
//            		log.error(str);
            		
	                reasoner.registerOntology(commonOntology);
	                registered = true;
	                //debugDumpOntology("c:/tmp/dummy.wsml");
            	}
            }

            // Time execution.
            long st = System.currentTimeMillis();
            
            // Execute query request.
            log.debug("Query: "+queryString);
            LogicalExpression query = leFactory.createLogicalExpression(queryString, commonOntology);
            
//            System.out.println(Helper.serializeTopEntity(commonOntology));
            
            Set<Map<Variable, Term>> result = reasoner.executeQuery( (IRI) commonOntology.getIdentifier(), query);

            // Time execution.
            long et = System.currentTimeMillis();
            log.info("Query time "+(et-st)+" ms.");
            
            // Convert result to printable form.
            for (Map<Variable, Term> vBinding : result) {
                Map<String,Term> rvs = new HashMap<String,Term>();
                for (Variable var : vBinding.keySet()) {
                    rvs.put(var.toString(),vBinding.get(var));
                }
                rv.add(rvs);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        // Print the results to debug output.
        for(Map<String,Term> r : rv) {
            for(Map.Entry<String,Term> s : r.entrySet()) {
                log.debug(s.getKey() +" = "+termToString(s.getValue()));
            }
        }
        
        return rv;
    }
    
    /**
     * Write the common ontology to a file.
     * 
     * @param filename Name of the file to write.
     */
    public void debugDumpOntology(String filename) {
        
        // Create a serializer for dumping the WSML.
        Serializer ser = Factory.createSerializer(null);

        // Write the WSML to a string.
        StringBuffer str = new StringBuffer();
        TopEntity[] tops = {commonOntology};
        ser.serialize(tops, str);
        
        // And dump that string to a file.
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            out.write(str.toString());
            out.close();
        }
        catch (IOException ioe) {
            ioe.getStackTrace();
        }
    }
    
public String debugDumpOntology() {
        return Helper.serializeTopEntity(commonOntology);
    }
}
