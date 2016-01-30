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

/**
 * Fake base class for operators. This is a placeholder for the CODIMS operator
 * class in order to enable standalone execution while keeping a CODIMS-like
 * object model.
 * 
 * @author Sebastian Gerlach
 */
public class Operator {
    
    /**
     * Current index in input set.
     */
    private Iterator currentInput;
    
    /**
     * Set of input objects.
     */
    private Collection inputSet;
    
    /**
     * Set the input set for the operator.
     * 
     * @param inputSet Set of input values that are to be returned to the operator being tested.
     */
    public void setInput(Collection inputSet) {
        this.inputSet = inputSet;
        currentInput = inputSet.iterator();
    }
    
    /**
     * Get the next element from the input set.
     * 
     * @return The next object in the input set.
     */
    public Object getNext() {
        if(currentInput.hasNext())
            return currentInput.next();
        return null;
    }
    
    /**
     * Reset the input set to its beginning.
     */
    public void reset() {
        
        // Construct a new iterator.
        currentInput = inputSet.iterator();
    }
}
