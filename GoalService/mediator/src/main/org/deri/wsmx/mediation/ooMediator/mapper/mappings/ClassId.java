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
import org.omwg.ontology.Type;
import org.wsmo.common.Identifier;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 25-Mar-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/mapper/mappings/ClassId.java,v $, 
 * @version $Revision: 1.1 $ $Date: 2007-09-27 06:48:56 $
 */

public class ClassId extends org.omwg.mediation.language.objectmodel.api.ClassExpr.ClassId {

	private Type type;
	
	public ClassId(Type type){
		super(WSMOUtil.getIdentifier(type).toString());
		this.type = type;
	}

	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	
	public String toString(){
		return WSMOUtil.getTypeName(type);
	}
	
	public Identifier getIdentifier(){
	    return WSMOUtil.getIdentifier(type);
	}

	public ClassId clone(){
		return new ClassId(type);
	}
	

    public boolean equals(Object object){
        
        if (this==object)
            return true;
        
        if ((object == null) || (object.getClass() != this.getClass()))
            return false;
            
        ClassId id = (ClassId)object;
        
        return ((type == id.type) || (type!=null && type.equals(id.type)));
    }
    
    public int hashCode(){
        int hash = 7;
        hash = 31 * hash + (null == type ? 0 : type.hashCode());
        return hash;
    }

	@Override
	public String plainText() {		
		return getIdentifier().toString();
	}
    
}

