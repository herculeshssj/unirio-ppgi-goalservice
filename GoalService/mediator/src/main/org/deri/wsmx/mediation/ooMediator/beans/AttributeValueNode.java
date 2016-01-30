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

package org.deri.wsmx.mediation.ooMediator.beans;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Type;
import org.omwg.ontology.Value;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;


/** 
 * Interface or class description
 * 
 * @author FirstName LastName, FirstName LastName
 *
 * Created on 17-Mar-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/beans/AttributeValueNode.java,v $, 
 * @version $Revision: 1.1 $ $Date: 2007-09-27 06:48:56 $
 */

public class AttributeValueNode<E extends Value> implements Entity {

	private E value = null;
	private Attribute attribute = null;
	
	public AttributeValueNode(Attribute attr, E inst){
		this.attribute = attr;
		this.value = inst;
	}
	
	public Set listTypes(){
	    if (value instanceof Instance)
            return ((Instance)value).listConcepts();
        Set<Type> set = new HashSet<Type>();
        set.add(((DataValue)value).getType());
        return set;
    }
    
	public Attribute getAttribute() {
		return attribute;
	}
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	public E getValue() {
		return value;
	}
	public void setValue(E instance) {
		this.value = instance;
	}
	public String toString(){
		String attributeName = WSMOUtil.getAttributeName(attribute);
        String instanceName = WSMOUtil.getValueName(value);
        if (value instanceof AnonymousInstance)
            instanceName = "_#:" + instanceName;
		return attributeName + " -> " + instanceName;
	}
    
    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#getIdentifier()
     */
    public Identifier getIdentifier() {
        return attribute.getIdentifier();
    }


    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#listNFPValues(org.wsmo.common.IRI)
     */
    public Set listNFPValues(IRI key) throws SynchronisationException {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#listNFPValues()
     */
    public Map listNFPValues() throws SynchronisationException {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#addNFPValue(org.wsmo.common.IRI, org.wsmo.common.Identifier)
     */
    public void addNFPValue(IRI key, Identifier value) throws SynchronisationException, InvalidModelException {
        // TODO Auto-generated method stub
        
    }


    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#addNFPValue(org.wsmo.common.IRI, org.omwg.ontology.Value)
     */
    public void addNFPValue(IRI key, Value value) throws SynchronisationException, InvalidModelException {
        // TODO Auto-generated method stub
        
    }


    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#removeNFPValue(org.wsmo.common.IRI, org.wsmo.common.Identifier)
     */
    public void removeNFPValue(IRI key, Identifier value) throws SynchronisationException, InvalidModelException {
        // TODO Auto-generated method stub
        
    }


    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#removeNFPValue(org.wsmo.common.IRI, org.omwg.ontology.Value)
     */
    public void removeNFPValue(IRI key, Value value) throws SynchronisationException, InvalidModelException {
        // TODO Auto-generated method stub
        
    }


    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#removeNFP(org.wsmo.common.IRI)
     */
    public void removeNFP(IRI key) throws SynchronisationException, InvalidModelException {
        // TODO Auto-generated method stub
        
    }

}

