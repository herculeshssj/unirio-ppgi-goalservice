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

package org.deri.wsmx.eclipse.datamediation.ooMediator.items;


import java.util.Iterator;


import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.WsmlDataType;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 10-Oct-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/eclipse/datamediation/ooMediator/items/PrimitiveItemNode.java,v $, 
 * @version $Revision: 1.2 $ $Date: 2007-09-27 06:48:59 $
 */

public class PrimitiveItemNode<E> implements PrimitiveItem<E> {

    private E wsmoElement = null;
    
    /**
     * @param wsmoElement
     */
    public PrimitiveItemNode(E wsmoElement) {
        this.wsmoElement = wsmoElement;
    }
    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.Item#getWSMOElement()
     */
    public E getWSMOElement() {
        return wsmoElement;
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.Item#setWSMOElement(java.lang.Object)
     */
    public void setWSMOElement(E element) {
        wsmoElement = element;        
    }
    
    public String toString(){
        if (wsmoElement instanceof WsmlDataType)
            return ((WsmlDataType)wsmoElement).getIRI().getLocalName();
        if (wsmoElement instanceof Concept)
            return WSMOUtil.getConceptName((Concept)wsmoElement);
        if (wsmoElement instanceof Instance)
            return WSMOUtil.getInstanceName((Instance)wsmoElement) + " : " + getConceptsAsString((Instance)wsmoElement);
        return wsmoElement.toString();
        
    }
    
    private String getConceptsAsString(Instance instance){
        String result = "";
        Iterator it = instance.listConcepts().iterator();
        while (it.hasNext()){
            result = result + WSMOUtil.getConceptName((Concept)it.next());
            if (it.hasNext())
                result = result + ", ";
        }
        return result;
    }
}

