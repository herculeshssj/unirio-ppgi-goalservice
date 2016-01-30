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

import java.util.*;
import java.io.*;

/**
 * Representation of a fake ontology.
 * 
 * @author sgerlach
 *
 */
public class Ontology {
    /**
     * Ontology identifier. 
     */
    public int id;
    
    /**
     * Concepts that are members of this ontology 
     */
    public Vector<Concept> concepts;
    
    /**
     * Other ontologies that are referenced in the definition of the concepts 
     * contained in this ontology. 
     */
    public Vector<Ontology> refs;
    
    /**
     * Constructor.
     * 
     * @param i Identifier of this ontology.
     */
    public Ontology(int i) {
        id = i;
        concepts = new Vector<Concept>();
        refs = new Vector<Ontology>();
    }
    
    /**
     * Add a new concept to this ontology. This is called from within the constructor
     * of the concept.
     * 
     * @param c Concept to add to this ontology.
     */
    public void addConcept(Concept c) {
        concepts.add(c);
        if(c.parent!=null && c.parent.owner!=this && (!refs.contains(c.parent.owner)))
            refs.addElement(c.parent.owner);
    }
    
    /**
     * Get the name of this ontolgy generated from the identifier.
     * 
     * @return The name of this ontology.
     */
    public String getName() {
        return "ont"+id;
    }
    
    /**
     * Get the URL to the WSML file of this ontology.
     * 
     * @return The URL to the WSML file of this ontology.
     */
    public String getURL() {
        return OntologyCreator.ROOT_URL+"ontology"+id+".wsml";
    }
    
    /**
     * Get a string containing the complete WSML code for this ontology.
     * 
     * @return A string containing the complete WSML code for this ontology.
     */
    public String getDeclaration() {
        String decl = "";
        
        // Start with a comment
        decl+="/*\n";
        decl+=" * Ontology file created by OntologyCreator\n";
        decl+=" *\n";
        decl+=" * Copyright (c) 2006 Sebastian Gerlach EPFL-IC-LSIR\n";
        decl+=" *\n";
        decl+=" * Root URL: "+getURL()+"\n";
        decl+=" */\n";
        decl+="\n";
        
        // The WSML variant we use
        decl+="wsmlVariant _\"http://www.wsmo.org/wsml/wsml-syntax/wsml-rule\"\n";
        
        // The imported namespaces
        decl+="namespace { _\""+getURL()+"#\",\n";
        decl+="            dc _\"http://purl.org/dc/elements/1.1#\",\n";
        decl+="            wsml _\"http://www.wsmo.org/wsml/wsml-syntax#\"";
        if(refs.size()>0) {
            decl+=",\n";
            for(int i = 0; i<refs.size(); ++i) {
                decl+="            "+refs.elementAt(i).getName()+" _\""+refs.elementAt(i).getURL()+"#\"";
                if(i<refs.size()-1)
                    decl+=",\n";
                else
                    decl+="\n";
            }
        }
        else
            decl+="\n";
        decl+="          }\n";
        decl+="\n";
        
        // And now our ontology name
        decl+="ontology "+getName()+"\n";
        decl+="\n";
    
        // And finally all the concepts
        for(int i = 0; i<concepts.size(); ++i)
            decl+=concepts.elementAt(i).getDeclaration();

        return decl;
    }
    
    /**
     * Write this ontology to a WSML file.
     */
    public void writeFile() {
        try {
            // Create output file 
            Writer writer = new FileWriter(OntologyCreator.ROOT_DIRECTORY+"ontology"+id+".wsml");
            
            // Write the declaration to the output file
            writer.write(getDeclaration());
                
            // Close the file
            writer.close();
            writer = null;
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}
