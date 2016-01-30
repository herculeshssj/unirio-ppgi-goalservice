
package org.deri.wsmx.mediation.ooMediator.beans;


import org.deri.wsmx.mediation.beans.AttributesPairDefinition;
import org.omwg.ontology.Attribute;

/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian
 *
 * AttributesPair.java, May 18, 2004, 7:04:26 PM
 *
 **/
public class AttributesPair implements AttributesPairDefinition {

	private Attribute srcAttribute = null;
	
	private Attribute tgtAttribute = null;
	
	public AttributesPair(Attribute srcAttribute, Attribute tgtAttribute){
		this.srcAttribute = srcAttribute;
		this.tgtAttribute = tgtAttribute;
	}
	/**
	 * @return
	 */
	public Attribute getSrcAttribute() {
		return srcAttribute;
	}

	/**
	 * @return
	 */
	public Attribute getTgtAttribute() {
		return tgtAttribute;
	}

	/**
	 * @param definition
	 */
	public void setSrcAttribute(Attribute definition) {
		srcAttribute = definition;
	}

	/**
	 * @param definition
	 */
	public void setTgtAttribute(Attribute definition) {
		tgtAttribute = definition;
	}

}
