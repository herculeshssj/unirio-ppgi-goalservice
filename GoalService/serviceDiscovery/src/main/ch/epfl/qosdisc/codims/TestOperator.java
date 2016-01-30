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

package ch.epfl.qosdisc.codims;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.*;
import ch.epfl.codimsd.qeef.types.*;
import ch.epfl.codimsd.qeef.relational.*;
import ch.epfl.codimsd.qep.OpNode;


/**
 * A simple testing operator.
 * 
 * @author Sebastian Gerlach
 */
public class TestOperator extends ch.epfl.codimsd.qeef.Operator {
	
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(TestOperator.class);
    
    /**
     * Number of objects that have been output.
     */
    private int outputCount = 0;

	/**
	 * Constructor.
	 * 
	 * @param id Operator id.
	 * @param opNode Operatore node.
	 */
	public TestOperator(int id, OpNode opNode) {
		
		// Call superconstructor.
		super(id);
	}
	
	/**
     * Operator cleanup code.
     */
    public void close() throws Exception {

    	log.debug("Closing operator.");
    }
    
    /**
     * Operator startup code.
     * 
     * @throws Exception
     */
    public void open() throws  Exception {
 
    	log.debug("Opening operator.");
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.codimsd.qeef.Operator#getNext(int)
     */
    public DataUnit getNext(int consumerID) throws  Exception {
    	
    	// Output 5 objects.
    	if(outputCount<5) {
    		
    		outputCount++;
    		Tuple t = new Tuple();
    		t.addData(new FloatType(outputCount));
    		return t;
    	}
    	
    	return null;
    }
    
	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.Operator#setMetadata(ch.epfl.codimsd.qeef.Metadata[])
	 */
	@Override
	public void setMetadata(Metadata[] metadata) {

		// This just seems to be required by CoDIMS?
		log.debug("Setting metadata.");
        this.metadata[0] = (Metadata)metadata[0].clone();
	}

}
