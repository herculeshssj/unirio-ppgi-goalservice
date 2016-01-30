/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian
 *
 * ConceptsPair.java, May 18, 2004, 6:12:50 PM
 *
 **/
package org.deri.wsmx.mediation.ooMediator.beans;


import org.deri.wsmx.mediation.beans.ConceptsPairDefinition;
import org.omwg.ontology.Concept;

/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian
 *
 * ConceptsPair.java, May 18, 2004, 6:12:50 PM
 *
 **/
public class ConceptsPair implements ConceptsPairDefinition {

	private Concept srcConcept=null;
	
	private Concept tgtConcept=null;
	
	public ConceptsPair(Concept srcConcept, Concept tgtConcept){
		this.srcConcept = srcConcept;
		this.tgtConcept = tgtConcept;
	}
    
	/**
	 * @return
	 */
	public Concept getSrcConcept() {
		return srcConcept;
	}

	/**
	 * @return
	 */
	public Concept getTgtConcept() {
		return tgtConcept;
	}

	/**
	 * @param string
	 */
	public void setSrcConcept(Concept conceptName) {
		srcConcept = conceptName;
	}

	/**
	 * @param string
	 */
	public void setTgtConcept(Concept conceptName) {
		tgtConcept = conceptName;
	}
}
