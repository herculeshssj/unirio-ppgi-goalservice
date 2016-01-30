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
import org.omwg.mediation.language.objectmodel.api.InstanceExpr.InstanceId;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Value;
import org.omwg.ontology.WsmlDataType;
import org.wsmo.common.Identifier;
import com.ontotext.wsmo4j.ontology.InstanceImpl;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 27-Mar-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/mapper/mappings/IndividualId.java,v $, 
 * @version $Revision: 1.1 $ $Date: 2007-09-27 06:48:56 $
 */

public class IndividualId extends InstanceId {

	private Value value;
	
	public IndividualId(Value instance){
		super(WSMOUtil.getIdentifier(instance).toString());
		this.value = instance;
	}

	/**
	 * @return Returns the instance.
	 */
	public Value getValue() {
		return value;
	}
	/**
	 * @param instance The instance to set.
	 */
	public void setInstance(Instance instance) {
		this.value = instance;
	}
	
	public IndividualId clone(){
        if (value instanceof Instance)
            return new IndividualId(new InstanceImpl(((Instance)value).getIdentifier()));
        if (value instanceof WsmlDataType)
            return new IndividualId(value);
        return this;
	}
	
	public String toString(){
		return WSMOUtil.getValueName(value);
	}
	
	public Identifier getIdentifier(){
        return WSMOUtil.getIdentifier(value);
	}
	
    public boolean equals(Object object){
        
        if (this==object)
            return true;
        
        if ((object == null) || (object.getClass() != this.getClass()))
            return false;
            
        IndividualId id = (IndividualId)object;
        
        return ((value == id.value) || (value!=null && value.equals(id.value)));
    }
    
    public int hashCode(){
        int hash = 7;
        hash = 31 * hash + (null == value ? 0 : value.hashCode());
        return hash;
    }

	@Override
	public String plainText() {
		return getIdentifier().toString();
	}
    
}

