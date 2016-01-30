
package org.deri.wsmx.mediation.beans;

import org.omwg.ontology.Concept;

/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian
 *
 * ConceptPairDefinition.java, May 18, 2004, 2:10:24 PM
 *
 **/
public interface ConceptsPairDefinition {
	
	public Concept getSrcConcept();
    
	public void setSrcConcept(Concept conceptName);
	
	public Concept getTgtConcept();
	
	public void setTgtConcept(Concept conceptName);

}
