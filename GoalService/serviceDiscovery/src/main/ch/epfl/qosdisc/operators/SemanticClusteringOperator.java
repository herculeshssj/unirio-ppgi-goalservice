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

package ch.epfl.qosdisc.operators;

import java.util.*;

import org.apache.log4j.Logger;
import org.omwg.ontology.*;

/**
 * This is the semantic clustering operator. It groups together all concepts that share
 * a common superconcept. This basically means that all concepts that share the same top-level
 * superconcept are grouped together.  
 * 
 * @author sgerlach
 */
public class SemanticClusteringOperator extends Operator {
    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(SemanticClusteringOperator.class);

    /**
     * List of concept groups derived from input ontolgies. 
     */
    Vector<ConceptGroup> conceptGroups;
    
    /**
     * Current output group.
     */
    Iterator<ConceptGroup> currentGroup;
    
    /**
     * Constructor.
     */
    public SemanticClusteringOperator() {
        conceptGroups = new Vector<ConceptGroup>();
        currentGroup = null;
    }
    
    /**
     * Prints out the sub- and superconcepts of a concept. Used for
     * debugging.
     * 
     * @param concept Concept to display
     */
    public void printConcept(Concept concept) {
        // Print concept identifier
        System.out.println("Concept: "+concept.getIdentifier().toString());
        // And print that as well
        System.out.println("Top-level: "+getTopLevelSuperConcept(concept).getIdentifier().toString());

        // Print list of subconcepts
        System.out.println("Subconcepts:");
        for(Iterator it = concept.listSubConcepts().iterator();it.hasNext();) {
            Concept sc = (Concept)it.next();
            System.out.println(" "+sc.getIdentifier().toString());
        }
        // Print list of superconcepts
        System.out.println("Superconcepts:");
        for(Iterator it = concept.listSuperConcepts().iterator();it.hasNext();) {
            Concept sc = (Concept)it.next();
            System.out.println(" "+sc.getIdentifier().toString());
        }
    }
    
    /**
     * Gets the highest-level superconcept of a concept.
     * 
     * @param concept The concept for which to find the top-level superconcept 
     * @return The top-level superconcept of the argument.
     */
    private Concept getTopLevelSuperConcept(Concept concept) {
        // Find top-level superconcept
        Concept tlc = concept;
        while(tlc.listSuperConcepts().iterator().hasNext()) {
            // Check that each concept only has one superconcept.
            // TODO: Concepts are a graph, not a tree. Therefore we should be a bit more careful here.
            if(tlc.listSuperConcepts().size()!=1)
                log.warn("Concept hierarchy is not a tree. This is currently unsupported and may lead to unpredictable results.");
            tlc = (Concept)tlc.listSuperConcepts().iterator().next();
        }
        return tlc;   
    }
    
    /**
     * Return the next output object for this operator.
     * 
     * @return Next output element of operator
     */
    public Object getNext() {
        if(currentGroup==null) {
            
            // This is the first time we are called. Therefore we collect all the
            // input ontologies and group the related concepts.            
            Ontology ontology;
            
            // Collect all input objects, in this case ontologies.
            while((ontology = (Ontology)super.getNext()) != null) {
                
                // Print out all the concepts contained in the ontology.
                for(Iterator it = ontology.listConcepts().iterator();it.hasNext();) {
                    Concept concept = (Concept)it.next();
                    
                    // Ignore the two spec collection concepts.
                    String conceptName = concept.getIdentifier().toString();
                    if(conceptName.contains("SpecCollection"))
                        continue;
    
                    // Find top-level superconcept.
                    Concept tlc = getTopLevelSuperConcept(concept);
                    
                    // Find the concept group containing the top-level superconcept.
                    boolean found = false;  // Set this to true if a group was found for the current concept.
                    for(Iterator<ConceptGroup> cg=conceptGroups.iterator();cg.hasNext();) {
                        ConceptGroup group = cg.next();
                        
                        if(group.checkMembership(tlc))
                        {
                            // A group already contains the top-level concept, join it.
                            group.addConcept(concept);
                            found = true;
                        }
                    }
                    if(!found) {
                        
                        // If a top-level superconcept is encountered for the first 
                        // time, it must necessarily be equal to itself.
                        assert(concept==tlc);
                        
                        // Now add a new concept group with this concept as a root.
                        ConceptGroup cg = new ConceptGroup();
                        conceptGroups.add(cg);
                        cg.addConcept(concept);
                    }
                    //printConcept(concept);
                }
            }
            currentGroup = conceptGroups.iterator();
        }

        // Return next concept group in list.
        if(currentGroup.hasNext())
            return currentGroup.next();
        
        // Return null when all groups have been returned.
        return null;
    }

}
