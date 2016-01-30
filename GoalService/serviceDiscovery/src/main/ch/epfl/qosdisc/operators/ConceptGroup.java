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
import org.wsmo.common.Identifier;
import org.omwg.ontology.Concept;

/**
 * Container for a group of related concepts.
 *
 * @author Sebastian Gerlach
 */
public class ConceptGroup {

    /**
     * Key for this concept group used for locating it within
     * the Bloom filter.
     */
    private BloomKey conceptGroupKey;
    
    /**
     * The root concept of the group.
     */
    private Concept rootConcept;
    
    /**
     * Map containing all the concepts that are members of this group.  
     */
    private Map<Identifier, Concept> memberConcepts;
    
    /**
     * Bloom filter bit count
     */
    public static final int BLOOM_BIT_COUNT = 256; 
    
    /**
     * Constructor.
     */
    public ConceptGroup() {
        memberConcepts = new HashMap<Identifier, Concept>();
    }
    
    /**
     * Check whether a specific {@link Concept} is in this group or not.  
     * 
     * @param concept The {@link Concept} that needs to be checked.
     * @return true if concept is a member of this {@link ConceptGroup}.  
     */
    public boolean checkMembership(Concept concept) {
        
        // Attempt to find the concept in the map
        return memberConcepts.containsKey(concept.getIdentifier());
    }
    
    /**
     * Check whether the {@link Concept} identified by the passed identifier
     * is in this group or not.  
     * 
     * @param id The {@link Identifier} of the concept that needs to be checked.
     * @return true if concept is a member of this {@link ConceptGroup}.  
     */
    public boolean checkMembership(Identifier id) {
        
        // Attempt to find the concept in the map
        return memberConcepts.containsKey(id);
    }
    
    /**
     * Compute a hash value for a string.
     * 
     * @param s The string to hash
     * @param hk A hashing key
     * @return A hash value
     */
    private int hashString(String s, int hk) {
        
        // This is a fairly stupid hash function. This could certainly be improved.
        int hv = hk;
        for(int i = 0; i<s.length(); ++i) {
            hv = (hv * hk) ^ s.charAt(i) + hk;
        }
        return hv;
    }
    
    /**
     * Add a new concept to the group.
     * 
     * @param concept The concept to add
     */
    public void addConcept(Concept concept) {
        
        // Ensure the concept is not already a member of the group.
        assert(!checkMembership(concept));
        
        if(memberConcepts.size() == 0) {
            
            // If this is the first concept added, create a Bloom Filter key.
            conceptGroupKey = new BloomKey(BLOOM_BIT_COUNT);

            // Compute a certain number of hashes, and insert them in the key.
            int hash1 = hashString(concept.getIdentifier().toString(),793);
            int hash2 = hashString(concept.getIdentifier().toString(),981);
            int hash3 = hashString(concept.getIdentifier().toString(),37);
            int hash4 = hashString(concept.getIdentifier().toString(),3803);
            int hashes[] = new int[] { hash1, hash2, hash3, hash4 };
            conceptGroupKey.set(hashes);
            
            rootConcept = concept;
        }
        
        // Add the concept to the map.
        memberConcepts.put(concept.getIdentifier(), concept);
    }
 
    /**
     * Print the current contents of the concept group to the console. 
     */
    public void debugDump() {
        System.out.println("Concept group debug dump:");      
        
        // Iterate through all concepts in the group.
        for(Iterator<Concept> it = memberConcepts.values().iterator(); it.hasNext();) {
            Concept concept = it.next();
            
            // Print concept identifier.
            System.out.println("Concept: "+concept.getIdentifier().toString());       
        }
    }

    /**
     * Returns the Bloom filter key for this concept group.
     * 
     * @return The Bloom filter key.
     */
    public BloomKey getConceptGroupKey() {
        return conceptGroupKey;
    }
    
    /**
     * @return The name of the root concept of the group.
     */
    public String getGroupName() {
        return rootConcept.getIdentifier().toString();
    }
}
