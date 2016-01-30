/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ch.epfl.qosdisc.operators;

import java.util.*;

import org.wsmo.common.IRI;

/**
 * Operator that keeps all services that match an input URI.
 * 
 * @author Sebastian Gerlach
 *
 */
public class NegHashJoinOperator extends Operator {

    /**
     * Set of interfaces against which to match.
     */
    private HashMap<IRI,InterfaceExt> refSet;
    
    /**
     * Constructor. Second argument is passed as input here.
     * 
     * @param refSet Set of interfaces against which to match.
     */
    public NegHashJoinOperator(Collection<InterfaceExt> refSet) {
        
        // Set up reference set.
        this.refSet = new HashMap<IRI, InterfaceExt>();
        for(Iterator<InterfaceExt> it = refSet.iterator(); it.hasNext(); ) {
            InterfaceExt e = it.next();
            this.refSet.put((IRI)e.getInterface().getIdentifier(), e);
        }
    }
    /**
     * Identifies whether the input object matches at least one of the reference
     * set items.
     * 
     * @return Next output element of operator.
     */
    public Object getNext() {
        
        // Get next object from superclass.
        InterfaceExt ie;
        while((ie = (InterfaceExt )super.getNext())!=null) {
            if(!refSet.containsKey((IRI)ie.getInterface().getIdentifier()))
                return ie;
        }
        while(true);
    }
}
