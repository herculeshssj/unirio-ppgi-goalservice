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

import org.apache.log4j.Logger;
import org.wsmo.common.*;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.omwg.logicalexpression.terms.Term;
import ch.epfl.qosdisc.database.*;


/**
 * Selects only those interface definitions that satisfy the required QoS
 * attributes.
 * 
 * @author Sebastian Gerlach
 *
 */
public class QoSMatchingOperator extends Operator {    
    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(QoSMatchingOperator.class);

    /**
     * The goal we wish to satisfy.
     */
    private GoalInterfaceExt goal;
    
    /**
     * The name of the goal axiom.
     */
    private IRI goalAxiom;
    
    /**
     * The dc#relation IRI.
     */
    private IRI relation;
    
    /**
     * Constructor.
     * 
     * @param goal The goal to filter for.
     */
    public QoSMatchingOperator(GoalInterfaceExt goal) {

        // First of all we need a WSMO factory in order to create IRI objects
        WsmoFactory factory = Factory.createWsmoFactory(null);
        
        // Create IRI for the desired relationship.
        relation = factory.createIRI("http://purl.org/dc/elements/1.1#relation");
        
        // Collect the relation in the goal (the QoS axiom).
        Collection props = (Collection)goal.getInterface().listNFPValues(relation);        

        // Should be only one requirement in there.
        assert(props.size()==1);

        // Get the axioms describing the goal we want to achieve.
        goalAxiom = (IRI)props.iterator().next();
        
        // Store the goal.
        this.goal = goal;
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
            
            // Set up our local reasoning context.
            Reasoner cm = new Reasoner();

            // Inform about what we are doing.
            log.info("Checking "+desc.getServiceDescription().getIdentifier().toString());
            
            // Constitute a fake ontology.
            cm.addOntology(desc.getImportedOntologies());
            cm.addOntology(goal.getImportedOntologies());
            
            // Collect the relation in the service (the QoS axiom).
            Collection props = (Collection)desc.getInterface().listNFPValues(relation);     
            
            // Should be only one requirement in there.
            if(props.size()<1) {
            	desc.addComment("No relation found in interface description, skipping");
                log.warn("No relation found in interface description of "+desc.getInterface().getIdentifier()+", skipping.");
                continue;
            }

            // Get that requirement.
            IRI serviceAxiom = (IRI)props.iterator().next();
            
            // Execute the query.
            Vector<Map<String,Term>> rv;            
            rv = cm.execute("_\""+serviceAxiom.toString()+"\"(?x,?y)");

            // Find the matching term.
            boolean keep = false; 
            for(Map<String,Term> r : rv) {
                if(cm.termToString(r.get("?y")).contains("#yes") && cm.termToString(r.get("?x")).contains("#any"))
                    keep=true;
            }
            
            String comment;
            
            if(!keep) {
                
                // The service does not match.
                log.info("Rejecting interface "+desc.getInterface().getIdentifier()+", client does not meet service specs.");
            	comment = "REJECT: Service client environment requirements not satisfied";

                // Print out the matching results (they should contain the reason of failure).
                if(rv.size()==0) {                	
                    log.debug("No service spec results were found.");
                	comment = comment + " because no service client environment was found.";                
                } else {
                    for(Map<String,Term> r : rv) {
                        
                        // Make pretty versions of the strings for debug output.
                        String result = cm.termToString(r.get("?y"));
                        result = result.substring(result.lastIndexOf('#')+1);
                        result = result.substring(0, result.length()-1);
                        String value = cm.termToString(r.get("?x"));
                        value = value.substring(value.lastIndexOf('#')+1);
                        value = value.substring(0, value.length()-1);
                        
                        // Add comment string.
                        comment = comment + ", " + value +" ("+result+")";
                    	
                        log.debug(value +" is "+result);
                    }
                }
                
                // Set negative ranking score for display.
                desc.setRanking(-1.0);
            } else {
            	
            	comment = "Service client environment requirements satisfied.";
            	desc.addComment(comment);
            	comment = "";
                
                // Execute goal axiom in order to find out whether service meets our QoS specs.
                rv = cm.execute("_\""+goalAxiom.toString()+"\"(?x,?y)");
                for(Map<String,Term> r : rv) {
                    
                    // Make pretty versions of the strings for debug output.
                    String result = cm.termToString(r.get("?y"));
                    result = result.substring(result.lastIndexOf('#')+1);
                    result = result.substring(0, result.length()-1);
                    String value = cm.termToString(r.get("?x"));
                    String niceValue = value.substring(value.lastIndexOf('#')+1);
                    niceValue = niceValue.substring(0, niceValue.length()-1);

                    // Store matching results in service description.
                    int res;
                    if(result.contains("yes")) {
                        log.debug(niceValue+" matches.");
                        res=1;
                    } else {
                        log.debug(niceValue+" does not match ("+result+").");
                        res=0;
                    }
                    desc.setQoSMatching(((IRI)r.get("?x")).toString(),res);
                    
                    // Add comment string.
                    comment = comment + ", "+niceValue + " ("+result+"="+res+")";
                }
                
                // Create output comment.
                if(rv.size() > 0)
                	comment = "Goal QoS requirement matching results: "+comment.substring(2);
                else
                	comment = "No goal QoS requirement matching results were produced.";
            }
            
            // Add comment for UI output.
        	desc.addComment(comment);                            
            
            // Cleanup the reasoning context.
            cm.clean();

            // Return the interface if matching was successful.
            if(keep)
                return desc;
        }
        return null;
    }
}
