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

import org.apache.log4j.Logger;

/**
 * Selects only those interface definitions that expose the required QoS
 * attributes. The filtering is done based on a bloom key.
 * 
 * @author Sebastian Gerlach
 *
 */
public class BloomKeyRestrictQoSOperator extends Operator {
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(BloomKeyRestrictQoSOperator.class);

    /**
     * The goal we wish to satisfy. Only the Bloom key is actually used
     * here for matching.
     */
    private GoalInterfaceExt goal;
    
    /**
     * Constructor.
     * 
     * @param goal The goal to filter for.
     */
    public BloomKeyRestrictQoSOperator(GoalInterfaceExt goal) {
        this.goal = goal;
    }
    
    /**
     * Return the next output object for this operator.
     * 
     * @return Next output element of operator
     */
    public Object getNext() {
        
        // Get the next service description
        InterfaceExt desc;
        while((desc = (InterfaceExt)super.getNext()) != null) {
            
            // If it matches the goal Bloom key, keep it
            if(desc.getBloomKey().matches(goal.getBloomKey())) {
                log.debug("Keeping "+desc.getInterface().getIdentifier().toString());
                return desc;
            } else
                log.debug("Removing "+desc.getInterface().getIdentifier().toString());
        }
        return null;
    }
}
