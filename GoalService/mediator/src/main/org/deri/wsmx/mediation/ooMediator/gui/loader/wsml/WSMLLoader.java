 package org.deri.wsmx.mediation.ooMediator.gui.loader.wsml;



import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deri.wsmx.mediation.ooMediator.gui.loader.Loader;
import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Type;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Parser;

/*
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 *   2005 Digital Enterprise Research Insitute (DERI) Galway
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 **/
public class WSMLLoader implements Loader{
	
	private Ontology ontology = null;
    private WsmoFactory wsmoFactory;
    private LogicalExpressionFactory leFactory;	
	
	public WSMLLoader(File f){
        wsmoFactory = WSMOUtil.wsmoFactory;
        leFactory = WSMOUtil.leFactory;
		processWSML(parse(f));	
		loadAllWMSLFiles(f);
	}
	
	public WSMLLoader(Ontology ontology){
		Entity[] a = new Entity[1];
		a[0] = ontology;
		processWSML(a);
	}
	
	private Entity[] parse(File f){
        /*HashMap <String, Object> props = new HashMap <String, Object> ();
        props.put(Parser.PARSER_WSMO_FACTORY, wsmoFactory);
        props.put(Parser.PARSER_LE_FACTORY, leFactory);*/
        //Parser parser = null; 
        /*props.put(Factory.PROVIDER_CLASS, parser);
               parser = Factory.createParser(props);*/
        Parser parser = WSMOUtil.parser; 
		Entity[] holders = null;
		try{
			FileReader reader = new FileReader(f);
			holders = parser.parse(reader);
		}
		catch(Exception e){
			e.printStackTrace();
			return new Entity[1];
		}
		return holders;
	}
	
	private void processWSML(Entity[] holders){
        ontology = (Ontology)holders[0];
		
		//the inheritance relationships is considered
		//considerInheritanceForConcepts(ontology); //TODO RUN SCREAMING!!!!!
	}

	private IRI newIRI(String string) {
        return wsmoFactory.createIRI(string);
    }

    private int indexOfConcept(Object concept, ArrayList concepts){
		int size = concepts.size();
		for (int i=0; i<size; i++)
			if (concepts.get(i).equals(concept))
				return i;
		return -1;
	}
	
	public Ontology getOntology(){
		return ontology;
	}
	
	/**
	 * Takes in consideration the inheritance relationshis. That is, the inherited attributes will be added to the specific sub-concept
	 */
	private void considerInheritanceForConcepts(Ontology theOntology){
        Iterator itC = theOntology.listConcepts().iterator();
        while (itC.hasNext()){
            Concept c = (Concept)itC.next();
            for (Concept sc : getSuperConcepts(c)){
                for (Attribute a : (Set <Attribute>) sc.listAttributes()){
                    if (a.getIdentifier() instanceof IRI){
                        //Attribute newA = c.createAttribute((IRI) a.getIdentifier());
                        Attribute newA = WSMOUtil.createAttribute((IRI)a.getIdentifier(), c);
                        for (Type t: (Set <Type>) a.listTypes()){
                            try {
                                newA.addType(t);
                            } catch (InvalidModelException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else{
                        // TODO run away screaming
                    }
                }
            }
		}
	}
    
    private List <Concept> getSuperConcepts(Concept theConcept) {
        List <Concept> sconcepts = new ArrayList <Concept>();

        for (Concept sc : (Set <Concept>) theConcept.listSuperConcepts()){
            sconcepts.add(sc);
            sconcepts.addAll(getSuperConcepts(sc));
        }
        
        return sconcepts;
    }
    
    private void loadAllWMSLFiles(File f){
    	File parentDirectory = f.getParentFile();
    	if (parentDirectory == null)
    		return;
    	File[] otherWSMLFiles = parentDirectory.listFiles(new FilenameFilter(){

			public boolean accept(File dir, String name) {
				if (name.endsWith(".wsml"))
					return true;
				return false;
			}});
    	if (otherWSMLFiles==null)
    		return;
        	
    	for (int i=0; i<otherWSMLFiles.length; i++){
    		parse(otherWSMLFiles[i]);
    	}
    	
    }
}
