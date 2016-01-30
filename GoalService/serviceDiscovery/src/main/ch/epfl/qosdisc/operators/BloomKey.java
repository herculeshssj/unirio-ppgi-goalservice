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
 * Bloom key of arbitrary size.
 * 
 * @author Sebastian Gerlach
 */
public class BloomKey {
    /**
     * The Bloom key bit vector
     */
    private int[] bitVector;
    
    /**
     * Number of bits in vector.
     */
    private int bits;
    
    /**
     * Constructor.
     * 
     * @param bits Number of bits in the Bloom key.
     */
    public BloomKey(int bits) {
        
        // Allocate bit vector
        this.bits = bits;
        bitVector = new int[(bits+31)>>5];
    }
    
    /**
     * Set bits in the bit vector.
     * 
     * @param hashes The hashes for the bits to set.
     */
    public void set(int[] hashes) {
        
        // Insert all hashes.
        for(int i = 0; i < hashes.length; ++i ) {
            
            // Get rid of sign bit if it is there
            int v = hashes[i] & ((1<<31)-1);
            
            // Compute offset and bit position for the hash.
            int off = (v%bits) >> 5;
            int bit = 1 << (v&31);
            
            // Set corresponding bit.
            bitVector[off] |= bit;
        }
    }
    
    /**
     * Set bits in the bit vector from another key.
     * 
     * @param mask The key from which to copy the bits.
     */
    public void set(BloomKey mask) {
        assert(mask.bits == bits);

        // Copy all bits
        for(int i = 0; i < bitVector.length; ++i) 
            bitVector[i] |= mask.bitVector[i];
    }
    
    /**
     * Check whether the current Bloom filter fits the passed mask.
     * 
     * @param mask The mask against which to check.
     * @return true if all mask bits are set in this filter key.
     */
    public boolean matches(BloomKey mask) {
        assert(mask.bits == bits);

        // Compare all bits of both vectors.
        for(int i = 0; i < bitVector.length; ++i) {
            
            // If it does not match, exit.
            if((bitVector[i] & mask.bitVector[i]) != mask.bitVector[i])
                return false;
        }
        return true;
    }
    
    /**
     * Convert to string.
     * 
     * @return The string representation of the Bloom key
     */
    public String toString() {
        
        // Convert to a long sequence of hex chars.
        StringBuffer rv = new StringBuffer();
        for(int i = 0; i < bitVector.length; ++i)
            rv.append(String.format("%08x",bitVector[i]));
        return rv.toString();
    }
}
