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



import java.util.ArrayList;
import java.util.Collection;


import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.ontology.Concept;


/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 09-Oct-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/eclipse/datamediation/ooMediator/items/CompoundItemNode.java,v $, 
 * @version $Revision: 1.2 $ $Date: 2007-09-27 06:48:59 $
 */

public class CompoundItemNode<E, D, S> implements CompoundItem<E, D, S> {

    private E wsmoElement = null;
    private Collection<DescriptionItem<E, D, S>> descriptionItems = new ArrayList<DescriptionItem<E, D, S>>();
    
    /**
     * @param wsmoElement
     */
    public CompoundItemNode(E wsmoElement) {
        this.wsmoElement = wsmoElement;
    }
    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.CompoundItem#getDescriptionItems()
     */
    public Collection<DescriptionItem<E, D, S>> listDescriptionItems() {
        return descriptionItems;
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.Item#setWSMOElement(java.lang.Object)
     */
    public void setWSMOElement(E element) {
        wsmoElement = element;       
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.Item#getWSMOElement()
     */
    public E getWSMOElement() {
        return wsmoElement;
    }
    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.CompoundItem#setDescriptionItems(java.util.Collection)
     */
    public void addDescriptionItems(Collection<DescriptionItem<E, D, S>> descriptionItems) {
       this.descriptionItems.addAll(descriptionItems);         
    }
    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.CompoundItem#addDescriptionItem(ie.deri.wsmx.mediation.ooMediator.items.DescriptionItem)
     */
    public void addDescriptionItem(DescriptionItem<E, D, S> descriptionItem) {
       this.descriptionItems.add(descriptionItem);       
    }
    
    public String toString(){
        if (wsmoElement instanceof Concept)
            return WSMOUtil.getConceptName((Concept)wsmoElement);
        return wsmoElement.toString();
    }

}

