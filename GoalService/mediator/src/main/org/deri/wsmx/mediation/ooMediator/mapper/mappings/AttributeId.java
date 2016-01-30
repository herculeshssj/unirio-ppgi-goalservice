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

package org.deri.wsmx.mediation.ooMediator.mapper.mappings;


import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.ontology.Attribute;
import org.wsmo.common.Identifier;

/** 
 * Interface or class description
 * 
 * @author FirstName LastName, FirstName LastName
 *
 * Created on 25-Mar-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/mapper/mappings/AttributeId.java,v $, 
 * @version $Revision: 1.1 $ $Date: 2007-09-27 06:48:56 $
 */

public class AttributeId extends org.omwg.mediation.language.objectmodel.api.AttributeExpr.AttributeId {

	private Attribute attribute;
	
	public AttributeId(Attribute attribute){
		super(attribute.getIdentifier().toString());
		this.attribute = attribute;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public AttributeId clone(){
		return new AttributeId(attribute);
	}
	
	public String toString(){
		return "[(" + WSMOUtil.getConceptName(attribute.getConcept()) + ") " +  WSMOUtil.getAttributeName(attribute) + " => " + 
                                                                            WSMOUtil.getRangesAsString(attribute) + "]";
	}
	
	public Identifier getIdentifier(){
		return attribute.getIdentifier();
	}
	
   
    public boolean equals(Object object){
        
        if (this==object)
            return true;
        
        if ((object == null) || (object.getClass() != this.getClass()))
            return false;
            
        AttributeId id = (AttributeId)object;
        
        return ((attribute == id.attribute) || (attribute!=null && attribute.equals(id.getAttribute())));
    }
    
    public int hashCode(){
        int hash = 7;
        hash = 31 * hash + (null == attribute ? 0 : attribute.hashCode());
        return hash;
    }

	@Override
	public String plainText() {
		return getIdentifier().toString() + "@" + attribute.getConcept().getIdentifier();
	}
	
}

