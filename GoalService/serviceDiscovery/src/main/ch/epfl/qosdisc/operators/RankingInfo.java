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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.omwg.logicalexpression.terms.Term;
import org.wsmo.factory.*;
import org.wsmo.common.*;
import org.wsmo.wsml.Serializer;
import org.omwg.ontology.*;

import ch.epfl.qosdisc.database.*;


/**
 * The ranking info object. This object contains not only the parameters for the
 * ranking algorithm, but also the ranking algorithm itself.
 * 
 * @author Sebastian Gerlach
 *
 */
public class RankingInfo {
    
    /**
     * Output log for debug info.
     */
    private static Logger log = Logger.getLogger(RankingInfo.class);

    /**
     * The ranking score threshold xi_h for the ranking algorithms.
     */
    private double xiH = 0.0;

    /**
     * The ranking score threshold xi_l for the ranking algorithms.
     */
    private double xiL = 0.0;
    
    /**
     * The partial ranking score rho_h given for a service interface with higher partial
     * ranking. 
     */
    private double rhoH = 0.0;

    /**
     * The partial ranking score rho_e given for a service interface with equal partial
     * ranking. 
     */
    private double rhoE = 0.0;
    
    /**
     * The partial ranking score rho_l given for a service interface with lower partial
     * ranking. 
     */
    private double rhoL = 0.0;
    
    /**
     * Configuration information for individual concepts.
     */
    private HashMap<String,ConceptRankingInfo> conceptRanking;
    
    private GoalInterfaceExt goal;
        
    /**
     * Constructor.
     * 
     * @param goal The goal from which to retrieve the ranking parameters.
     */
    public RankingInfo(GoalInterfaceExt goal) {
        this.goal = goal; 
    	
        // Set up our local reasoning context.
        Reasoner cm = new Reasoner();
        
        Collection<String> tr = goal.getImportedOntologies();
        
        // Constitute a fake ontology.
        cm.addOntology(goal.getImportedOntologies());
        
        // Get the ranking ontology IRI from the configuration file.
        Collection<TopEntity> base = WSMLStore.getEntities(PropertySet.getProperty("ranking"));
        String rankIRI = null;
        if(base != null && base.size() > 0)
        	rankIRI = ((IRI)base.iterator().next().getIdentifier()).getNamespace();

        if(rankIRI == null) {
            log.error("Could not get ranking ontology from configuration, all rankings will be zero.");
            return;
        }
        
        // Query for the global QoS ranking parameters.
        Vector<Map<String,Term>> rv;
        rv = cm.execute("?threshold memberOf _\""+rankIRI+"QoSRankingScoreThreshold\" and " +
                "?threshold[_\""+rankIRI+"hasHigherRankThreshold\" hasValue ?xiH] and "+
                "?threshold[_\""+rankIRI+"hasLowerRankThreshold\" hasValue ?xiL] and "+ 
                "?threshold[_\""+rankIRI+"hasHigherPartialScore\" hasValue ?rhoH] and "+ 
                "?threshold[_\""+rankIRI+"hasEqualPartialScore\" hasValue ?rhoE] and "+
                "?threshold[_\""+rankIRI+"hasLowerPartialScore\" hasValue ?rhoL]");
        
        // Store the results.
        if(rv.size()==1) {
            xiH = Double.parseDouble(cm.termToString(rv.get(0).get("?xiH")));
            xiL = Double.parseDouble(cm.termToString(rv.get(0).get("?xiL")));
            rhoH = Double.parseDouble(cm.termToString(rv.get(0).get("?rhoH")));
            rhoL = Double.parseDouble(cm.termToString(rv.get(0).get("?rhoL")));
            rhoE = Double.parseDouble(cm.termToString(rv.get(0).get("?rhoE")));
        } else
            log.error("Could not load ranking ontology "+rankIRI);
        
        // Query for the per-concept ranking parameters.
        rv = cm.execute("?concept memberOf _\""+rankIRI+"QoSConceptConfiguration\" and "+
                "?concept[_\""+rankIRI+"hasQoSConceptIRI\" hasValue ?iri] and "+
                "?concept[_\""+rankIRI+"hasWeight\" hasValue ?weight] and "+
                "?concept[_\""+rankIRI+"hasReputationScore\" hasValue ?repScore] and"+ 
                "?concept[_\""+rankIRI+"hasMatchingThreshold\" hasValue ?threshold]");
        if(rv.size()==0)
            log.error("Could not find any ranking configuration for QoS concepts");
        
        // Parse the results.
        conceptRanking = new HashMap<String,ConceptRankingInfo>();
        for(Map<String,Term> r : rv) {
        	String iri = ((IRI)r.get("?iri")).toString();
            ConceptRankingInfo cri = new ConceptRankingInfo(
                Double.parseDouble(cm.termToString(r.get("?weight"))),
                Double.parseDouble(cm.termToString(r.get("?threshold"))),
                Double.parseDouble(cm.termToString(r.get("?repScore"))),
                WSMLStore.getComparisonOperator(iri));
            conceptRanking.put(iri,cri);
            
            log.debug(((IRI)r.get("?iri")).toString()+" w: "+cri.getWeight()+" t: "+cri.getThreshold()+" d: "+cri.getDefaultReputation());
        }
        
        // Clear the query execution context.
        cm.clean();        
    }
    
    /**
     * Rank the provided services according to their QoS.
     * 
     * @param services The services to rank.
     */
    public void performRanking(Collection<InterfaceExt> services) {
        
        // Reject services that do not satisfy basic requirements.
        Vector<InterfaceExt> reject = new Vector<InterfaceExt>();
        for(InterfaceExt i : services) {
        	
        	// Check the mandatory parameters.
            for(Map.Entry<String,ConceptRankingInfo> k : conceptRanking.entrySet()) {
                
                // Fetch matching result.
                Double uiv = i.getQoSMatching(k.getKey());
                double ui = (uiv==null) ? 0 : uiv.doubleValue();
                
                // If matching is mandatory, remove service if it does not satisfy criteria.
                //FIXME - removing threshold
//              double threshold = k.getValue().getThreshold();
//              if(ui==0 && threshold!=0.0) {
                if(ui==0) {
                    log.debug("Rejecting "+i.getInterface().getIdentifier()+" since it does not satisfy mandatory params");
                    i.addComment("Does not satisfy mandatory QoS parameters.(no)");
                    i.setRanking(-1.0);
                    reject.add(i);
                    break;
                }
            }
            
            // Query for the QoS estimates.
            HashMap<String,Double> ests = WSMLStore.queryEstimates(i.getInterface().getIdentifier().toString());
            for(Map.Entry<String,Double> e: ests.entrySet())
            	i.setQoSEstimate(e.getKey(),e.getValue().doubleValue());
        }
        
        // Remove all rejected services.
        for(InterfaceExt i : reject)
            services.remove(i);
        
        //alternative ranking algorithm by Maciej Zaremba
        for(InterfaceExt i : services) {
        	double score = 0.0;

        	for(Map.Entry<String,ConceptRankingInfo> k : conceptRanking.entrySet()) {
              Double qServ = i.getQoSEstimate(k.getKey());
              Double qGoal = goal.getQoSEstimate(k.getKey());
              if (qServ != null && qGoal != null) {
					// Do the comparison with the parameter-specific operator.
					if (k.getValue().getComparison().equals("HigherBetter")){
						score += k.getValue().getWeight() * ((qServ-qGoal)/qGoal);
						int a = 4;
					}
					else if (k.getValue().getComparison().equals("LowerBetter")){
						score += k.getValue().getWeight() * ((qGoal-qServ)/qGoal);
						int a = 4;
					}
					else
						log.warn("Unknown estimated parameter values "+  k.getKey());
              }
        	}
            // And the final comment.
            i.addComment("Ranking score is "+score+"(yes)");
            i.setRanking(score);

        }
        
        // Implementation of the ranking algorithm. 
//        for(InterfaceExt i : services) {
//            int rH = 0, rE = 0, rL = 0;
//
//            for(InterfaceExt j : services) {
//                if(i == j)
//                    continue;
//                
//                // Perform the comparison for every pair of services (i,j).
//                double rank = 0, weight = 0;
//
//                log.debug("Comparing "+i.getInterface().getIdentifier()+" with "+j.getInterface().getIdentifier());
//                
//                // Compare every QoS concept k.
//                for(Map.Entry<String,ConceptRankingInfo> k : conceptRanking.entrySet()) {
//                    
//                    Double qiv = null, qjv = null;
//                    double compVal = 0.0;
//                    
//                    // Query for the per-concept parameter estimates for Qik^, Qjk^.
//                    qiv = i.getQoSEstimate(k.getKey());
//                    qjv = j.getQoSEstimate(k.getKey());
//                    if(qiv != null && qjv != null) {
//
//                    	// Do the comparison with the parameter-specific operator.
//                    	if(k.getValue().getComparison().equals("HigherBetter"))
//                    		compVal = qiv.doubleValue() > qjv.doubleValue() ? 1.0 : 0.0;
//                    	else if(k.getValue().getComparison().equals("LowerBetter"))
//                    		compVal = qiv.doubleValue() < qjv.doubleValue() ? 1.0 : 0.0;
//                		else
//                			log.warn("Unknown comparison operator "+k.getValue().getComparison());
//                    }
//                    
//                    // Get default values if no estimates are found.
//                    double rval;
//                    if(qiv==null && qjv==null)
//                        rval = 0;
//                    else if(qjv == null)
//                        rval = k.getValue().getDefaultReputation();
//                    else if (qiv == null)
//                        rval = 1-k.getValue().getDefaultReputation();
//                    else 
//                        rval = compVal;
//                    
//                    // Get matching result.
//                    Double uiv = i.getQoSMatching(k.getKey());
//                    Double ujv = j.getQoSMatching(k.getKey());
//                    double ui = (uiv==null) ? 0 : uiv.doubleValue();
//                    double uj = (ujv==null) ? 0 : ujv.doubleValue();
//                    if(ui>uj)
//                        rval += 1.0;
//                    
//                    rank += k.getValue().getWeight() * rval;
//                    weight += k.getValue().getWeight();
//                }
//                rank /= weight;
//                
//                if(rank>xiH)
//                    rH++;
//                else if(rank<xiL)
//                    rL++;
//                else
//                    rE++;
//            }
//            
//            // Add 1 to the score in order to remove any zeroes :-)
//            double score = 1 + rhoH * rH + rhoE * rE + rhoL * rL;
//            i.setRanking(score);
//
//            // And the final comment.
//            i.addComment("Ranking score is "+score+"(yes)");
//        }
        
        // Now we want to do some sorting.
        Vector<InterfaceExt> tv = new Vector<InterfaceExt>();
        tv.addAll(services);
        services.clear();
        int currentRank = 1;
        double normalizedRanking = 0.0;
        while(tv.size() > 0) {
            InterfaceExt best = tv.firstElement();
            double cr = best.getRanking();
            for(InterfaceExt c : tv) {
                if(c.getRanking() > cr) {
                    cr = c.getRanking();
                    best = c;
                }
            }
            if(currentRank==1)
            	normalizedRanking = cr;
            best.setRanking(best.getRanking()/normalizedRanking);
            best.setRank(currentRank);
            services.add(best);
            tv.remove(best);
            
            currentRank++;
        }
        
        // And now the really ugly part - dump the ranking in the input ranking ontology.
        String out = PropertySet.getProperty("output");
        if(out != null) {
            
            // Create factories.
            WsmoFactory factory = Factory.createWsmoFactory(null);
            DataFactory dataFactory = Factory.createDataFactory(null);
            
            // Create ontology.
            Ontology rank = factory.createOntology(factory.createIRI(out));
            for(InterfaceExt i : services) {
                try {
                    // Create one instance for each service.
                    Instance in = factory.createInstance(factory.createIRI(out+"#rank"+i.getRank()));
                    in.addAttributeValue(factory.createIRI(out+"#service"),dataFactory.createWsmlString(i.getInterface().getIdentifier().toString()));
                    in.addAttributeValue(factory.createIRI(out+"#rank"),dataFactory.createWsmlDouble(i.getRanking()));                    
                    rank.addInstance(in);
                } catch(Exception ex) {
                    ex.printStackTrace();               
                }
            }
            
            // Create a serializer for dumping the WSML.
            Serializer ser = Factory.createSerializer(null);

            // Write the WSML to a string.
            StringBuffer str = new StringBuffer();
            TopEntity[] tops = {rank};
            ser.serialize(tops, str);
            
            // And dump that string to a file.
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(out));
                bw.write(str.toString());
                bw.close();
            }
            catch (IOException ioe) {
                ioe.getStackTrace();
            }
            
        } else 
            log.warn("No output ontology specified in configuration.");
    }

    /**
     * @return Returns the partial ranking score rho_e.
     */
    public double getRhoE() {
        return rhoE;
    }

    /**
     * @return Returns the partial ranking score rho_h.
     */
    public double getRhoH() {
        return rhoH;
    }

    /**
     * @return Returns the partial ranking score rho_l.
     */
    public double getRhoL() {
        return rhoL;
    }

    /**
     * @return Returns the ranking score threshold xi_h for the ranking algorithms..
     */
    public double getXiH() {
        return xiH;
    }

    /**
     * @return Returns the ranking score threshold xi_l for the ranking algorithms..
     */
    public double getXiL() {
        return xiL;
    }

    /**
     * Get concept ranking info for given concept.
     * @param iri
     * @return
     */
    public ConceptRankingInfo getConceptRanking(String iri) {
        return conceptRanking.get(iri);
    }
    
}
