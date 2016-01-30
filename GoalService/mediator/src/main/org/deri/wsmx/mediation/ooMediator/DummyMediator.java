/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian <br>
 *
 * DummyMediator.java, Apr 29, 2004, 11:17:25 AM
 * 
 **/
package org.deri.wsmx.mediation.ooMediator;

import org.omwg.ontology.Ontology;


/**
 * A dummy mediator that does nothing more that adding in the end of the given payload the string: "__ADDED_BY_DUMMYMEDIATOR".
 *
 **/
public class DummyMediator implements Mediator {
	
	DummyMediator(){
	}
			
	public StringBuffer mediate(Ontology sourceOntologyID, Ontology targetOntologyId, StringBuffer payload){
		return payload.append("__ADDED_BY_DUMMYMEDIATOR");
	}
}
