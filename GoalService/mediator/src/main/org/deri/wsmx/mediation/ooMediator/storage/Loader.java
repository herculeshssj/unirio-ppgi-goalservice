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
 */
package org.deri.wsmx.mediation.ooMediator.storage;



import java.util.Set;

import org.deri.wsmx.mediation.ooMediator.logging.DataMediatorOutputStream;
import org.deri.wsmx.mediation.ooMediator.mapper.Mappings;
import org.omwg.ontology.Ontology;


/**
 * Definess the necessary operations for loading from a persistent storage (e.g. a database) the required data: ontologies and mappings.
 **/
public interface Loader{
	
	/**
	 * Returns the ontologies available in the storage. 
	 * @return a set of ontologies
	 */
	public Set<Ontology> getAvailableOntologies();
	/**
	 * Returns an ontology corresponding to the ontology object given as parameter. If the given object contains only the identifier of the ontology the ontoloy 
	 * entities will be loaded from the storage. If the object represents a full ontology (ontology containing at least one concept) it will be return as it is.    
	 * @param ontology An object representing the ontology.
	 * @return the populted ontology  
	 */
	public Ontology loadOntology(Ontology ontology);

	/**
	 * Returns the mappings between the given source and target.
	 * @param sourceOntology
	 * @param targetOntology
	 * @return The mappings, or null if they aren't available in store.
	 */
	public Mappings loadMappings(Ontology sourceOntology, Ontology targetOntology);
	
	/** 
	 * Returns whether the ontology is available in storage.
	 * @param ontology
	 * @return
	 */
	public boolean containsOntology(Ontology ontology);
	
	/**
	 * Returns whether there is a mappings in storage for the given source and target.
	 * @param sourceOntology
	 * @param targetOntology
	 * @return
	 */
	public boolean containsMappings(Ontology sourceOntology, Ontology targetOntology);
    
    public DataMediatorOutputStream getOutputStream(); 
    
}
