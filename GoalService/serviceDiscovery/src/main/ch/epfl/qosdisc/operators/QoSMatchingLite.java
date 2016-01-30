package ch.epfl.qosdisc.operators;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Ontology;
import org.wsmo.common.IRI;
import org.wsmo.common.TopEntity;

import ch.epfl.qosdisc.database.Reasoner;
import ch.epfl.qosdisc.database.WSMLStore;


/**
 * Selects only those interface definitions that satisfy the required QoS
 * attributes.
 * 
 * @author Sebastian Gerlach
 */
public class QoSMatchingLite {
    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(QoSMatchingOperatorLite.class);

    /**
     * The goal we wish to satisfy.
     */
    private GoalInterfaceExt goal;
    
    /**
     * The base QoS ontology.
     */
    private String qosBase = "file:///c:/WSMX/resources/qosdiscovery/ontologies/Common/QoSBase.wsml";
    
	public QoSMatchingLite(GoalInterfaceExt goal) {
		
        // Store the goal.
        this.goal = goal;
        
        // Get the base QoS ontology.
        Collection<TopEntity> base = WSMLStore.getEntities(PropertySet.getProperty("qosbase"));
        if(base != null && base.size() > 0)
        	qosBase = ((IRI)base.iterator().next().getIdentifier()).getNamespace();
        else
        	log.warn("Could not recover QoS base ontology. Things might get hairy. Very hairy.");
	}
	
	public boolean matchService(InterfaceExt desc) {
		        
        // Set up our local reasoning context.
        Reasoner cm = new Reasoner();

        // Inform about what we are doing.
        log.info("Checking "+desc.getServiceDescription().getIdentifier().toString());
        
        // Constitute a fake ontology.
        for(Object o : desc.getInterface().listOntologies())
        	cm.addOntology(((Ontology)o).getIdentifier().toString());
        for(Object o : goal.getInterface().listOntologies())
        	cm.addOntology(((Ontology)o).getIdentifier().toString());
        
//        log.fatal("--Dumped--"+cm.debugDumpOntology()+"-----");
        
        // Execute the query and store the service requirement terms.
        Vector<Map<String,Term>> rv;            
        Vector<IRI> terms = new Vector<IRI>();
        Vector<IRI> foundTerms = new Vector<IRI>();
        rv = cm.execute("_\""+qosBase+"ServiceList\"(?x)");
        for(Map<String,Term> r : rv)
        	terms.add((IRI)r.get("?x"));

        String comment = "";
        
        // Now perform the query to see what matches
        rv = cm.execute("_\""+qosBase+"ServiceSatisfied\"(?x)");
        for(Map<String,Term> r : rv) {
        	IRI i =(IRI)r.get("?x");
        	if(!terms.contains(i)) {
        		
        		// The term was not returned by the ServiceList query. This should not happen.
        		log.warn("Inconsistent term "+i.toString()+" returned from ServiceSatisfied query.");
        		desc.addComment("WARNING: Service matching criteria returned unexpected parameter "+i.getLocalName());
        	} else {
        	
        		// Move the term from the unsatisfied to the satisfied list. 
        		terms.remove(i);
        		foundTerms.add(i);
        	}
        }

        // Add comment string.
    	for(IRI i : terms)
            comment = comment + ", " + i.getLocalName()+ "(no)";                    	
    	for(IRI i : foundTerms)
            comment = comment + ", " + i.getLocalName() +"(yes)";                    	
    	
        boolean keep = terms.size() == 0; 
        if(!keep) {
            
            // The service does not match.
        	comment = "Service client environment requirements satisfaction: "+comment.substring(2);

            // Set negative ranking score for display.
            desc.setRanking(-1.0);
        } else {
        	
        	if(comment.length()>0)
        		comment = "Service client environment requirements satisfied: "+comment.substring(2);
        	else
        		comment = "Service has no client environment requirements.(yes)";
        	log.debug(comment);
        	desc.addComment(comment);
        	comment = "";
        	
        	// Execute the goal queries.
            rv = cm.execute("_\""+qosBase+"GoalList\"(?x)");
            terms.clear();
            for(Map<String,Term> r : rv){
            	terms.add((IRI)r.get("?x"));
            	String query = "?y["+"_\""+qosBase+"value\" hasValue ?value]" + " memberOf _\""+qosBase + "GoalRequirement\" and ?y memberOf _\"" + ( (IRI)r.get("?x") ).toString()+"\""; 
            	Vector<Map<String,Term>> temp = cm.execute(query);
            	Map<String,Term> rt = temp.elementAt(0);
            	goal.setQoSEstimate( ((IRI)r.get("?x")).toString(), new Double(rt.get("?value").toString()).doubleValue());            	
            }

            // Now perform the goal query to see what matches
            rv = cm.execute("_\""+qosBase+"GoalSatisfied\"(?x)");
            
            foundTerms.clear();
            for(Map<String,Term> r : rv) {
            	IRI i =(IRI)r.get("?x");
            	if(!terms.contains(i)) {
            		
            		// The term was not returned by the ServiceList query. This should not happen.
            		log.warn("Inconsistent term "+i.toString()+" returned from ServiceSatisfied query.");
            		desc.addComment("WARNING: Goal matching criteria returned unexpected parameter "+i.getLocalName());
            	} else {
            	
            		// Move the term from the unsatisfied to the satisfied list. 
            		terms.remove(i);
            		foundTerms.add(i);
            	}
            }
        	
            //get estimates for both
            
            // Unmatched terms.
            for(IRI i : terms) {
            	
                // Store matching results in service description.
                desc.setQoSMatching(i.toString(),0);
                comment = comment + ", "+i.getLocalName()+ " (no)";
            }
        	
            // Matched terms.
            for(IRI i : foundTerms) {
            	
                // Store matching results in service description.
                desc.setQoSMatching(i.toString(),1);
                comment = comment + ", "+i.getLocalName()+ " (yes)";
//                rv = cm.execute("_\""+qosBase+"GoalList\"(?x)");
                
                //goal.setQoSEstimate(parameter, value)            
            }
            
            // Create output comment.
            if(comment.length() > 0)
            	comment = "Goal QoS requirement matching results: "+comment.substring(2);
            else
            	comment = "No goal QoS requirement matching results were produced.";
        }
        
        // Add comment for UI output.
        log.debug(comment);
    	desc.addComment(comment);                            
        
        // Cleanup the reasoning context.
        cm.clean();

        // Return the interface if matching was successful.
        return keep;
	}
}
