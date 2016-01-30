/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian <br>
 *
 * Mediator.java, Apr 29, 2004, 11:02:04 AM
 *
 **/
package org.deri.wsmx.mediation.ooMediator;

import org.omwg.ontology.Ontology;


/**
 * Defines the interface all the mediators have to implement
 *
 **/
public interface Mediator {
	
	/**
	 * Calls the mediator and returns back the mediated payload 
	 * @param sourceOntologyID the source ontology ID
	 * @param targetOntologyID the target ontology ID
	 * @param payload the subject of the mediation (instances from the source ontology)
	 * @return the mediated payload (instances from the target ontology)
	 */
	public StringBuffer mediate(Ontology sourceOntologyID, Ontology targetOntologyID, StringBuffer payload);

}
