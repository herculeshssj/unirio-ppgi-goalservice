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

package org.deri.wsmx.mediation.ooMediator.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.omwg.ontology.Attribute;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Value;
import org.omwg.ontology.WsmlDataType;
import org.wsmo.common.Entity;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 18-Sep-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/util/Serializer.java,v $, 
 * @version $Revision: 1.2 $ $Date: 2007-12-12 12:52:26 $
 */

public class Serializer {

    /**
     * @param array
     * @param result
     */
    public void serialize(Entity[] array, StringBuffer result) {
        List<StringBuffer> moreResults = new ArrayList<StringBuffer>();
        for (int i=0; i<array.length; i++){
            if (array[i] instanceof Instance){
                Instance crtInstance = (Instance)array[i];
                result.append("\ninstance " + crtInstance.getIdentifier().toString());
                if (crtInstance.listConcepts() != null && !crtInstance.listConcepts().isEmpty()){
                    result.append(" memberOf ");
                    Iterator it = crtInstance.listConcepts().iterator();
                    while(it.hasNext()){
                        result.append(((Concept)it.next()).getIdentifier().toString());
                        if (it.hasNext())
                            result.append(", ");
                    }
                }
                if (crtInstance.listAttributeValues() !=null && !crtInstance.listAttributeValues().isEmpty()){
                    Iterator<Identifier> it = crtInstance.listAttributeValues().keySet().iterator();
                    while (it.hasNext()){
                        Identifier crtAttribute = it.next();
                        Iterator<Value> valueIt = crtInstance.listAttributeValues(crtAttribute).iterator();
                        if (valueIt.hasNext())
                            result.append("\n\t" + crtAttribute.toString() +" hasValue ");//TODO Check the syntax
                        while (valueIt.hasNext()){
                            Value crtValue = valueIt.next();
                            if (crtValue instanceof Instance){
                                result.append(((Instance)crtValue).getIdentifier().toString());
                                StringBuffer addOns = new StringBuffer();
                                Entity[] arrayAddOns = new Entity[1];arrayAddOns[0]=(Instance)crtValue;
                                serialize(arrayAddOns, addOns);
                                //result.append("\n");
                                //result.append(addOns);
                                moreResults.add(addOns);
                            }
                            else
                                result.append(crtValue.toString());
                            if (valueIt.hasNext())
                                result.append(", ");
                        }
                        if (!it.hasNext())
                            result.append("\n");
                    }
                    
                }
            }
        }
        
        Iterator<StringBuffer> itSB = moreResults.iterator();
        while (itSB.hasNext()){
            result.append("\n");
            result.append(itSB.next()); 
        }
        
    }
    
    public void serialize(Entity[] array, StringBuffer result, Set<Namespace> namespaces) {
    	serialize(array, result);    
    	if (namespaces != null) {
    		for (Namespace ns : namespaces)
    			replaceURI(ns, result);
    	}
    }
    
    private void replaceURI(Namespace ns, StringBuffer buffer){
    	
    	int indexOf = 0;
    	String toReplace = "";
    	String replacement = "";
    	if (ns.getIRI().getNamespace()!=null){
    		toReplace =  ns.getIRI().getNamespace();
    	}
    	else{
    		toReplace = ns.getIRI().toString();    		
    	}    	
    	
    	if (!ns.getPrefix().equals(""))
    		toReplace = toReplace.substring(0, toReplace.length()-1); 
    	
    	indexOf = buffer.indexOf(toReplace);
    	
    	
    	
    	if (indexOf!=-1){
    		
    		buffer = buffer.replace(indexOf, indexOf + toReplace.length(), ns.getPrefix());    		
    		replaceURI(ns, buffer);
    	}
    }
    
}

