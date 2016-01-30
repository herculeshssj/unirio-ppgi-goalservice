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

package ch.epfl.qosdisc.ontologies;

/**
 * Representation of a fake concept.
 * 
 * @author sgerlach
 *
 */
public class Concept {
    /**
     * Concept identifier
     */
    public int id;
    
    /**
     * Superconcept 
     */
    public Concept parent;
    
    /**
     * Ontology in which this concept is defined 
     */
    public Ontology owner;
    
    /**
     * Constructor.
     * 
     * @param i Concept identifier.
     * @param p Superconcept, null if it is a root concept.
     * @param o Ontology in which this concept is to be defined.
     */
    public Concept(int i, Concept p, Ontology o) {
        id = i;
        owner = o;
        parent = p;
        
        // Add concept to ontology
        owner.addConcept(this);
    }
    
    /**
     * Recover a valid IRI for identifying this concept. Returns either a full
     * or partial IRI.
     * 
     * @param context Ontology in which this concept is being referenced.
     * @return Valid reference to this concept within specified context.
     */
    public String getIRI(Ontology context) {
        if(context != owner)
            return owner.getName()+"#Concept"+id;
        return "Concept"+id;
    }
    
    /**
     * Generates a declaration string for this concept.
     *  
     * @return A string defining this concept that can be used in a WSML ontology.
     */
    public String getDeclaration() {
        String decl = "concept "+getIRI(owner);
        if(parent != null)
            decl += " subConceptOf "+parent.getIRI(owner);
        return decl+"\n";
    }

}
