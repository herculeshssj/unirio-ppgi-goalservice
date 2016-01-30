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

/**
 * Selects only those interface definitions that satisfy the required QoS
 * attributes. This class has become quite meaningless and is scheduled
 * for removal.
 * 
 * @author Sebastian Gerlach
 */
public class QoSMatchingOperatorLite extends Operator {    
    
    /**
     * The matching processor.
     */
    private QoSMatchingLite qm;

    /**
     * Constructor.
     * 
     * @param goal The goal to filter for.
     */
    public QoSMatchingOperatorLite(GoalInterfaceExt goal) {

        // Setup matching processor.
        qm = new QoSMatchingLite(goal);
    }
    
    /**
     * Return the next output object for this operator.
     * 
     * @return Next output element of operator
     */
    public Object getNext() {
        
        // Get the next service description.
        InterfaceExt desc;
        while((desc = (InterfaceExt)super.getNext()) != null) {
        	
        	if(qm.matchService(desc))
        		return desc;
        }
        return null;
    }

}
