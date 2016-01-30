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

import ie.deri.wsmx.commons.Helper;


import java.util.*;

import org.wsmo.common.*;
import org.wsmo.service.*;
import org.omwg.ontology.*;

import org.apache.log4j.*;

import ch.epfl.qosdisc.wsmx.QoSDiscovery;

/**
 * Test framework for QoS operators.
 * 
 * @author Sebastian Gerlach
 *
 */
public class TestFrame {
    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(QoSDiscovery.class);
    
    /**
     * List of candidate services.
     */
    private Vector<InterfaceExt> candidates;
    
    /**
     * Indicates whether a discovery is currently in progress.
     */
    public boolean discoveryRunning = true;
    
    /**
     * Inidcates whether functional discovery should be performed.
     */
    private boolean functionalDiscovery = false;
    
    /**
     * Constructor. 
     */
    public TestFrame(String refPath) {
        
        // Load properties.
    	PropertySet.setup(refPath);
        
        // Create our candidate vector.
        candidates = new Vector<InterfaceExt>();
    }
    
    /**
     * Add a web service to the list of candidates. 
     * 
     * @param service Service to add.
     */
    public void addService(ServiceDescription service) {
        
    	// Reject null services.
    	if(service == null)
    		return;
    	
        // Create interface descriptions.
        for(Object i : service.listInterfaces()) {
        	
        	// Check whether this interface is already loaded.
        	boolean found = false;
        	for(InterfaceExt ie : candidates) {
        		if(ie.getInterface().getIdentifier().equals(((Interface)i).getIdentifier()))
        			found = true;
        	}
        	
        	// If not, add it.
        	if(!found) {
        		InterfaceExt ie = new InterfaceExt(service, (Interface)i, -1); 
        		candidates.add(ie);
        	}
        }
    }
    
    /**
     * Get the list of currently loaded services.
     * 
     * @return The lsit of currently loaded services.
     */
    public Collection<InterfaceExt> getServices() {
    	
    	// Return a copy of the list of services.
        return new Vector<InterfaceExt>(candidates);
    }
    
    /**
     * Remove all currently loaded services.
     */
    public void removeAllServices() {
    	
    	// Clear the list of services.
    	candidates.clear();
    }
    
    /**
     * Create a goal description from a WSMO goal.
     * 
     * @param goal The WSMO goal.
     * @return The goal description.
     */
    private GoalInterfaceExt expandGoal(Goal goal) {
        
        // Create goal descriptions.
        assert(goal.listInterfaces().size() == 1);
        return new GoalInterfaceExt(goal, (Interface) goal.listInterfaces().iterator().next());
    }
    
    /**
     * Clear the inner state of all candidate services.
     */
    public void clearState() {
    	        
        // Remove any existing comment strings.
        for(InterfaceExt i : candidates) {
        	i.clearComment();
    		i.setRank(-1);
    		i.setRanking(0.0);
        }
    }
        
    /**
     * Perform a QoS-based selection, also serves as test for the operators.
     * 
     * @param goal Goal to achieve.
     */
    public List<WebService> achieveGoal(Goal goal) {
    	
    	// Set discovery flag.
    	discoveryRunning = true;
    	clearState();
    	
        List<InterfaceExt> pServices = new ArrayList<InterfaceExt>();
        List<WebService> services = new ArrayList<WebService>();
        
        // Create goal description.
        GoalInterfaceExt g = expandGoal(goal);
        
        // Skip all the static parts - this will actually be redesigned for the final
        // version to dynamically update the internal structures as new items are
        // encountered.
        
//        // Create vectors for the results of the operators that are being tested.
//        Vector<ConceptGroup> groups = new Vector<ConceptGroup>();
//        Vector<InterfaceExt> ifaces = new Vector<InterfaceExt>();
//        Vector<InterfaceExt> mifaces = new Vector<InterfaceExt>();
//        
//        // Test the SemanticClusteringOperator.
//        SemanticClusteringOperator scOp = new SemanticClusteringOperator();
//        scOp.setInput(ontologyInputSet);
//        for(ConceptGroup cg = (ConceptGroup)scOp.getNext(); cg != null; cg = (ConceptGroup)scOp.getNext()) {
//            groups.add(cg);
//            //cg.getConceptGroupKey().debugDump();
//        }
//        log.info("Number of groups: " + groups.size());
//
//        // Test the CreateBloomKeyOperator.
//        CreateBloomKeyOperator cbkOp = new CreateBloomKeyOperator(groups);
//        cbkOp.setInput(candidates);
//        for(InterfaceExt ci = (InterfaceExt)cbkOp.getNext(); ci != null; ci = (InterfaceExt)cbkOp.getNext()) {
//            ifaces.add(ci);
//            //ci.getBloomKey().debugDump();
//        }
//        log.info("Number of interfaces: " + ifaces.size());
//
//        // Run the CreateBloomKeyOperator again on the goal.
//        cbkOp = new CreateBloomKeyOperator(groups);
//        Vector<GoalInterfaceExt> tv = new Vector<GoalInterfaceExt>();
//        tv.add(g);
//        cbkOp.setInput(tv);
//        cbkOp.getNext();
//        //g.getBloomKey().debugDump();
//
//        // Test the BloomKeyRestrictQoSOperator 
//        BloomKeyRestrictQoSOperator bkrqOp = new BloomKeyRestrictQoSOperator(g);
//        bkrqOp.setInput(ifaces);
//        for(InterfaceExt ci = (InterfaceExt)bkrqOp.getNext(); ci != null; ci = (InterfaceExt)bkrqOp.getNext()) {
//            mifaces.add(ci);
//        }
//        log.info("Number of matching interfaces: " + mifaces.size());      
//        // Test the NegBloomKeyRestrictQoSOperator 
//        NegBloomKeyRestrictQoSOperator nbkrqOp = new NegBloomKeyRestrictQoSOperator(g);
//        nbkrqOp.setInput(ifaces);
//        mifaces.clear();
//        for(InterfaceExt ci = (InterfaceExt)nbkrqOp.getNext(); ci != null; ci = (InterfaceExt)nbkrqOp.getNext()) {
//            mifaces.add(ci);
//        }
//        System.out.println("Number of non-matching interfaces: " + mifaces.size());
        
        // Start timer
        long dt = System.currentTimeMillis();
        
        // Start QoS matching.
        log.info("Starting QoS matching operator.");
        QoSMatchingOperator mao = new QoSMatchingOperator(g);
        mao.setInput(candidates);
        
        for(InterfaceExt ci = (InterfaceExt)mao.getNext(); ci != null; ci = (InterfaceExt)mao.getNext()) {
            pServices.add(ci);
        }
        
        // Create ranking info. We assume that the goal ontology also references
        // the ranking ontology.
        RankingInfo ranking = new RankingInfo(g);

        // Run the ranking operator.
        log.info("Starting ranking operator.");
        ranking.performRanking(pServices);
        
        // Create sorted output list.
        log.info("Creating output list.");
        for(InterfaceExt i : pServices) {
            log.info(i.getRanking()+" "+i.getInterface().getIdentifier());
            services.add((WebService)i.getServiceDescription());
        }
        
        dt = System.currentTimeMillis() - dt;
        log.debug("Time taken: "+dt+" ms.");

        // Reset discovery flag.
        discoveryRunning = false;
        
        return services;
    }
    
    /**
     * Perform a QoS-based selection, also serves as test for the operators.
     * 
     * @param goal Goal to achieve.
     */
    public List<WebService> achieveGoalDatabase(Goal goal) {
    	
    	// Set discovery flag.
    	discoveryRunning = true;
    	clearState();
    	
        List<InterfaceExt> pServices = new ArrayList<InterfaceExt>();
        List<WebService> services = new ArrayList<WebService>();

    	for(InterfaceExt ie : candidates)
    		ie.setRanking(0);
        
        // Perform functional discovery if required.
//        if(functionalDiscovery) {
//        	
//        	log.info("Starting functional discovery operator.");
//        	List<WebService> fs = new ArrayList<WebService>();
//        	List<WebService> ks = new ArrayList<WebService>();
//        	for(InterfaceExt ie : candidates) {
//        		
//        		ie.setRanking(0);
//        		if(!fs.contains((WebService)ie.getServiceDescription()))
//        			fs.add((WebService)ie.getServiceDescription());
//        	}
//        	
//            for(WebService s : fs) {
//            	
//                try {
//                    	
//	                // Add the service to the functional discovery component.
//	                LightweightDiscovery lwd = new LightweightDiscovery();
//	                lwd.addWebService(s);
//                
//	                // Perform the functional discovery.
//	                List<WebService> funcServices = lwd.discover(goal);
//	                if(funcServices.size()==0)
//	                	log.info("Functional discovery removed "+s.getIdentifier().toString());
//	                else 
//	                	ks.addAll(funcServices);
//	                
//	                Thread.sleep(10);
//                } catch(Exception ex) {
//	            	
//	            	// Print the stack trace.
//	                ex.printStackTrace();
//	            }
//            }
//            
//            for(InterfaceExt ie : candidates) {
//            	
//            	if(!ks.contains((WebService)ie.getServiceDescription())) {
//            		
//            		ie.setRanking(-1);
//            		ie.addComment("REJECT: Does not satisfy functional criteria.");
//            	}
//            }
//        } else {
//        	
//        	//log.info("Starting fake functional discovery operator.");
//        	List<InterfaceExt> ks = new ArrayList<InterfaceExt>();
//
//        	for(InterfaceExt ie : candidates) {
//        		
//        		ie.setRanking(0);
//        		ks.add(ie);
//        	}
//        	
//            for(InterfaceExt s : candidates) {
//            	
//            	Object go = ((Ontology)goal.listOntologies().iterator().next()).getIdentifier();
//                try {
//                    	
//	                // Add the service to the functional discovery component.
//                	Object so = ((Ontology)s.getServiceDescription().listOntologies().iterator().next()).getIdentifier();
//                	if(!go.equals(so)) {
//	                	log.info("Functional discovery removed "+s.getInterface().getIdentifier().toString());
//	                	ks.remove(s);
//                	}
//
//                } catch(Exception ex) {
//	            	
//	            	// Print the stack trace.
//	                //ex.printStackTrace();
//	            }
//            }
//            
//            for(InterfaceExt ie : candidates) {
//            	
//            	if(!ks.contains(ie)) {
//            		
//            		ie.setRanking(-1);
//            		ie.addComment("Does not satisfy functional criteria.(no)");
//            	}
//            }
//            	
//        }
        	
        
        // Create goal description.
        GoalInterfaceExt g = expandGoal(goal);
        
        // Start timer
        long dt = System.currentTimeMillis();
        
        // Start QoS matching.
        log.info("Starting QoS matching operator.");
        QoSMatchingLite mao = new QoSMatchingLite(g);
        for(InterfaceExt ie : candidates) {
        	
        	if(ie.getRanking()==0 && mao.matchService(ie))
        		pServices.add(ie);
        }
        
        // Create ranking info. We assume that the goal ontology also references
        // the ranking ontology.
        RankingInfo ranking = new RankingInfo(g);

        // Run the ranking operator.
        log.info("Starting ranking operator.");
        ranking.performRanking(pServices);
        
        // Create sorted output list.
        log.info("Creating output list.");
        
		Helper.visualizerLog(Helper.FILTER_DISCOVERY,"QoSDiscovery discovered " + pServices.size() + " Web services");
        
        for(InterfaceExt i : pServices) {
            log.info(i.getRanking()+" "+i.getInterface().getIdentifier());
    		Helper.visualizerLog(Helper.FILTER_DISCOVERY,i.getRanking()+" "+i.getInterface().getIdentifier());
            services.add((WebService)i.getServiceDescription());
        }
        
        dt = System.currentTimeMillis() - dt;
        log.debug("Time taken: "+dt+" ms.");

        // Reset discovery flag.
        discoveryRunning = false;
        
        return services;
    }
    
    /**
     * Indicates whether functional discovery should be performed.
     * 
     * @param enabled true to perform functional discovery.
     */
    public void enableFunctionalDiscovery(boolean enabled) {
    	
    	functionalDiscovery = enabled;
    }
}
