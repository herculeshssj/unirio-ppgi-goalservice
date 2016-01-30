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
import java.util.Iterator;

import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.ontology.Attribute;


/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 10-Oct-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/eclipse/datamediation/ooMediator/items/DescriptionItemNode.java,v $, 
 * @version $Revision: 1.2 $ $Date: 2007-09-27 06:48:59 $
 */

public class DescriptionItemNode<P, D, S> implements DescriptionItem<P, D, S> {

    private D wsmoElement = null;
    private Collection<RootItem<? extends S>> successors = new ArrayList<RootItem<? extends S>>();
    private RootItem<P> parent = null;
    
    
    /**
     * @param wsmoElement
     */
    public DescriptionItemNode(D wsmoElement) {
        this.wsmoElement = wsmoElement;
    }
    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.DescriptionItem#getSuccessorItems()
     */
    public Collection<RootItem<? extends S>> listSuccessorItems() {
        return successors;
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.Item#getWSMOElement()
     */
    public D getWSMOElement() {
        return wsmoElement;
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.Item#setWSMOElement(java.lang.Object)
     */
    public void setWSMOElement(D element) {
        wsmoElement = element;
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.DescriptionItem#addSuccessorItems(java.util.Collection)
     */
    public void addSuccessorItems(Collection<RootItem<? extends S>> succesorItems) {
        successors.addAll(succesorItems);
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.DescriptionItem#addSuccessorItem(ie.deri.wsmx.mediation.ooMediator.items.Item)
     */
    public void addSuccessorItem(RootItem<? extends S> succesorItem) {
        successors.add(succesorItem);
    }
    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.DescriptionItem#getParent()
     */
    public RootItem<P> getParent() {
        return parent;
    }
    /* (non-Javadoc)
     * @see ie.deri.wsmx.mediation.ooMediator.items.DescriptionItem#setParent(ie.deri.wsmx.mediation.ooMediator.items.RootItem)
     */
    public void setParent(RootItem<P> parent) {
        this.parent = parent;
    }

    public String toString(){
        if (wsmoElement instanceof Attribute)
            return WSMOUtil.getAttributeName((Attribute)wsmoElement) + " => " + getSuccessorsAsString(); 
        return wsmoElement.toString();
    }
    
    private String getSuccessorsAsString(){
        String result = "";
        Iterator<RootItem<? extends S>> it = listSuccessorItems().iterator();
        while (it.hasNext()){
            result = result + it.next().toString();
            if (it.hasNext())
                result = result + ", ";
        }
        return result;
    }

}

