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

import org.wsmo.common.*;
import org.apache.log4j.Logger;
import java.util.*;
import org.omwg.logicalexpression.terms.Term;
import ch.epfl.qosdisc.database.*;

/**
 * Creates Bloom Keys for Web Service descriptions.
 * 
 * @author Sebastian Gerlach
 */
public class CreateBloomKeyOperator extends Operator {
    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(CreateBloomKeyOperator.class);

    /**
     * Set of concept groups against which we match the web services.
     */
    private Collection<ConceptGroup> conceptGroups;
    
    /**
     * Constructor.
     * 
     * @param conceptGroups The concept groups against which we want to match
     * the web services.
     */
    public CreateBloomKeyOperator(Collection<ConceptGroup> conceptGroups) {
        this.conceptGroups = conceptGroups;
    }
    
    /**
     * Return the next output object for this operator.
     * 
     * @return Next output element of operator.
     */
    public Object getNext() {
        
        // Get the next service description.
        InterfaceExt desc = (InterfaceExt)super.getNext();
        if(desc == null)
            return null;
        
        // Create Bloom filter key.
        BloomKey bloomFilter = new BloomKey(ConceptGroup.BLOOM_BIT_COUNT);
        
        // Get all QoS axioms referenced by the interface.
        Reasoner rc = new Reasoner();
       	rc.addOntology(desc.getImportedOntologies());
        
        log.debug("Testing interface "+desc.getInterface().getIdentifier().toString());
        
        // Perform membership query.
        Vector<Map<String,Term>> rv = rc.execute("?x memberOf ?y");
        
        // Walk through results.
        for(Map<String,Term> r : rv) {
            for(Map.Entry<String,Term> s : r.entrySet()) {
                if(IRI.class.isAssignableFrom(s.getValue().getClass())) {
                    
                    // And keep all IRIs.
                    IRI id = (IRI)s.getValue();
                    for(ConceptGroup cg : conceptGroups) {
                        if(cg.checkMembership(id)) {
                            
                            // Set bits in the bloom filter key.
                            bloomFilter.set(cg.getConceptGroupKey());
                            log.debug("Using concept "+s.getValue());
                        }
                    }
                }
            }
        }
        
        // Clean up reasoning context.
        rc.clean();

        // Set the Bloom filter key.
        desc.setBloomFilter(bloomFilter);
        
        // Return the input object.
        return desc;
    }

}
