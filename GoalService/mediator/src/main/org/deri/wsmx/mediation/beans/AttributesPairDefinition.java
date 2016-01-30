
package org.deri.wsmx.mediation.beans;

import org.omwg.ontology.Attribute;

/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian
 *
 * AttributePairDefinition.java, May 18, 2004, 2:12:40 PM
 *
 **/
public interface AttributesPairDefinition {

	public void setSrcAttribute(Attribute attribute);
	
	public void setTgtAttribute(Attribute attribute);
	
	public Attribute getSrcAttribute();
	
	public Attribute getTgtAttribute();
}
