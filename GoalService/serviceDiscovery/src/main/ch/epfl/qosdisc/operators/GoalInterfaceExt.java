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

import org.wsmo.service.ServiceDescription;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;

/**
 * Describes a goal for a web service interface. This class is identical
 * to InterfaceExt, since a goal uses the same semantics as a web service.
 * 
 * @author Sebastian Gerlach
 */
public class GoalInterfaceExt extends InterfaceExt {
    
    /**
     * Constructor.
     * 
     * @param g The goal to achieve.
     * @param i The interface on which to achieve the goal.
     * @param o The ontology describing the goal.
     */
    public GoalInterfaceExt(Goal g, Interface i) {
        super((ServiceDescription) g, i,-1);
    }
}
