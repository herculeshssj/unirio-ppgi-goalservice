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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

/**
 * Representation of a fake web service.
 * 
 * @author sgerlach
 *
 */
public class WebService {
    /**
     * Identifier for this web service
     */
    public int id;
    
    /**
     * Interfaces implemented by this web service
     */
    public Vector<Vector<Concept>> interfaces;
    
    /**
     * Ontologies referenced by the definition of this web service.  
     */
    public Vector<Ontology> refs;
    
    /**
     * Concepts used by the axioms defining the current interface 
     */
    private Vector<Concept> currentInterface;
    
    /**
     * Constructor.
     * 
     * @param i Identifier for this web service.
     */
    public WebService(int i) {
        id = i;
        interfaces = new Vector<Vector<Concept>>();
        refs = new Vector<Ontology>();
        currentInterface = null;
    }
    
    /**
     * Add a new interface to the web service.
     */
    public void newInterface() {
        currentInterface = new Vector<Concept>();
        interfaces.add(currentInterface);
    }
    
    /**
     * Add a concept to the current interface of the web service.
     * 
     * @param c The concept to add.
     */
    public void addConcept(Concept c) {
        currentInterface.add(c);
        if(!refs.contains(c.owner))
            refs.addElement(c.owner);
    }
    
    /**
     * Get the name of the web service.
     * 
     * @return The name of the web service.
     */
    public String getName() {
        return "ws"+id;
    }
    
    /**
     * Get the URL to the WSML file representing this web service.
     * 
     * @return The URL to the WSML file representing this web service.
     */
    public String getURL() {
        return OntologyCreator.ROOT_URL+"service"+id+".wsml";
    }
    
    /**
     * Get the URL to the WSML file representing the axioms referenced by this 
     * web service.
     * 
     * @return The URL to the WSML file representing the axioms.
     */
    public String getAxiomURL() {
        return OntologyCreator.ROOT_URL+"axiom"+id+".wsml";
    }
    
    /**
     * Get a string containing the complete WSML code for this web service.
     * 
     * @return A string containing the complete WSML code for this web service.
     */
    public String getDeclaration() {
        String decl = "";

        // Start with a comment
        decl+="/*\n";
        decl+=" * Web service file created by OntologyCreator\n";
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
        decl+="            wsml _\"http://www.wsmo.org/wsml/wsml-syntax#\",\n";
        decl+="            ws"+id+"axiom"+id+" _\""+getAxiomURL()+"#\"\n";
        decl+="          }\n";
        decl+="\n";
        
        // And now our web service name
        decl+="webService "+getName()+"\n";
        decl+="\n";
        decl+="capability dummyCap"+id+"\n";
        decl+="\n";
    
        // And finally all the interfaces
        for(int i = 0; i<interfaces.size(); ++i) {
            decl+="interface dummyInterface"+(i+1)+"\n";
            decl+=" importsOntology {\n";
            decl+="                     _\""+getAxiomURL()+"\"\n";
            decl+="                 }\n";
            decl+=" nonFunctionalProperties\n";
            decl+="  dc#relation hasValue {\n";
            for(int j = 0; j<interfaces.get(i).size(); ++j) {
                decl+="                         ws"+id+"axiom"+id+"#ws"+id+"int"+(i+1)+"QoSProp"+(j+1)+(j<interfaces.get(i).size()-1?",":"")+"\n";
            }
            decl+="                       }\n";
            decl+=" endNonFunctionalProperties\n";
            decl+=" choreography dummyChor"+(i+1)+"\n";
            decl+=" orchestration dummyOrch"+(i+1)+"\n";
        }

        return decl;
    }
    
    /**
     * Get a string containing the complete WSML code for the axioms referenced by
     * this web service.
     * 
     * @return A string containing the complete WSML code for the axioms.
     */
    public String getAxioms() {
        String decl = "";
        decl+="/*\n";
        decl+=" * Web service axiom file created by OntologyCreator\n";
        decl+=" *\n";
        decl+=" * Copyright (c) 2006 Sebastian Gerlach EPFL-IC-LSIR\n";
        decl+=" *\n";
        decl+=" * Root URL: "+getAxiomURL()+"\n";
        decl+=" */\n";
        decl+="\n";
        
        // The WSML variant we use
        decl+="wsmlVariant _\"http://www.wsmo.org/wsml/wsml-syntax/wsml-rule\"\n";
        
        // The imported namespaces
        decl+="namespace { _\""+getAxiomURL()+"#\",\n";
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
        
        // And now our web service name
        decl+="ontology axiom"+id+"\n";
        decl+="\n";
    
        for(int i = 0; i<interfaces.size(); ++i) {
            for(int j = 0; j<interfaces.get(i).size(); ++j) {
                decl+="axiom ws"+id+"int"+(i+1)+"QoSProp"+(j+1)+"\n";
                decl+="definedBy \n";
                decl+="    forall ?testItem (\n";
                decl+="    ?testItem memberOf "+interfaces.get(i).get(j).getIRI(null)+" and\n";
                decl+="    ?testItem[hasMeanValue hasValue 5.0] ).\n"; 
            }
        }
        return decl;
    }
    
    /**
     * Write this ontology and associated axioms to WSML files.
     */
    public void writeFile() {
        try {
            Writer writer;
            // Create service output file 
            writer = new FileWriter(OntologyCreator.ROOT_DIRECTORY+"service"+id+".wsml");
            
            // Write the declaration to the output file
            writer.write(getDeclaration());
                
            // Close the file
            writer.close();
            
            // Create axiom output file 
            writer = new FileWriter(OntologyCreator.ROOT_DIRECTORY+"axiom"+id+".wsml");
            
            // Write the declaration to the output file
            writer.write(getAxioms());
                
            // Close the file
            writer.close();
            
            writer = null;
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    

}
