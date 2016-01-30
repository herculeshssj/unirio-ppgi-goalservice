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

import org.wsmo.service.ServiceDescription;
import org.wsmo.service.Interface;
import org.omwg.ontology.*;

import ch.epfl.qosdisc.database.WSMLStore;

import java.util.*;

/**
 * Describes a web service interface. 
 * 
 * @author Sebastian Gerlach
 */
public class InterfaceExt {
    
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Pointer to the WMSO Goal or WebService description.
     */
    protected ServiceDescription serviceDescription;
    
    /**
     * Pointer to the matching QoS ontology.
     */
    private Vector<String> importedOntologies;
    
    /**
     * Identifier of the Interface.
     */
    private Interface theInterface;
    
    /**
     * Identifier of the interface in the database.
     */
    private int id;
    
    /**
     * The associated Bloom key computed from QoS concepts of this interface.
     */
    private BloomKey bloomFilter;
    
    /**
     * Correspondence between QoS requirements and service. This contains the
     * 'q' terms from the specification.
     */
    private Map<String,Double> qosMatching;
    
    /**
     * QoS estimates for the service. This contains the 'q^' terms from the
     * specification.
     */
    private Map<String,Double> qosEstimates;
    
    /**
     * The ranking score.
     */
    private double score = 0.0;
    
    /**
     * The ranking index. Set to -1 if the ranking is not yet determined or the
     * service has been disqualified. The reason for disqualification can be found in the
     * comments.
     */
    private int rank = -1;
    
    /**
     * A set of comments concerning the progress of discovery on this particular interface. 
     * The comments are prepared as HTML in order to allow simple display in a browser
     * interface.
     */
    private String comments = "";
    
    /**
     * Constructor.
     * 
     * @param serviceDescription Service description for this interface.
     * @param theInterface Identifier of this interface.
     * @param qosOntology Matching QoS ontology.
     */
    public InterfaceExt(ServiceDescription serviceDescription, Interface theInterface, int id) {
    	
    	// Copy input parameters.
        this.serviceDescription = serviceDescription;
        this.theInterface = theInterface;
        this.id=id;
        this.importedOntologies = new Vector<String>();
        for(Object o : theInterface.listOntologies()) {
            Ontology ont = (Ontology) o;
            importedOntologies.add(ont.getIdentifier().toString());
        }
        
        // Create QoS matching map.
        qosMatching = new HashMap<String,Double>();
        qosEstimates = new HashMap<String,Double>();
    }

    /**
     * @return Returns the Bloom filter.
     */
    public BloomKey getBloomKey() {
        return bloomFilter;
    }

    /**
     * @param bloomFilter The Bloom filter to set.
     */
    public void setBloomFilter(BloomKey bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    /**
     * @return Returns the interface.
     */
    public Interface getInterface() {
        return theInterface;
    }
    
    /**
     * @return Returns the identifier.
     */
    public int getId() {
    	return id;
    }

    /**
     * @return Returns the service description.
     */
    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }
    
    /**
     * @return Returns the QoS ontology.
     */
    public Collection<String> getImportedOntologies() {
        return importedOntologies;
    }
    
    /**
     * Set a QoS matching value.
     * 
     * @param parameter The IRI of the parameter.
     * @param value The value of the matching.
     */
    public void setQoSMatching(String parameter, double value) {
        qosMatching.put(parameter,new Double(value));
    }
    
    /**
     * Get a QoS matching value.
     * 
     * @param parameter The IRI of the parameter.
     * @return The value of the matching, or null if not found.
     */
    public Double getQoSMatching(String parameter) {
        return qosMatching.get(parameter);
    }
    
    /**
     * Set a QoS estimate value.
     * 
     * @param parameter The IRI of the parameter.
     * @param value The value of the estimate.
     */
    public void setQoSEstimate(String parameter, double value) {
        qosEstimates.put(parameter,new Double(value));
    }
    
    /**
     * Get a QoS estimate value.
     * 
     * @param parameter The IRI of the parameter.
     * @return The value of the estimate, or null if not found.
     */
    public Double getQoSEstimate(String parameter) {
        return qosEstimates.get(parameter);
    }

    /**
     * Set ranking score.
     * 
     * @param score The ranking score.
     */
    public void setRanking(double score) {
        this.score = score;
    }
    
    /**
     * @return The ranking score.
     */
    public double getRanking() {
        return score;
    }
    
    /**
     * Set the ranking index.
     * 
     * @param rank The ranking index.
     */
    public void setRank(int rank) {
    	this.rank = rank;
    }
    
    /**
     * Get the ranking index.
     * 
     * @return The ranking index.
     */
    public int getRank() {
    	return rank;
    }
    
    /**
     * Returns the current comments attached to this service.
     */
    public String getComments() {
    	return comments;
    }
    
    /**
     * Add a comment to this service. The comment is automatically postfixed with
     * a <br/> entity.
     * 
     * @param comment The comment to add.
     */
    public void addComment(String comment) {
    	comments = comments + comment +"<br/>";
    }
    
    /**
     * Clears the comment string. This must be called before starting a new discovery on an
     * existing list of interfaces.
     */
    public void clearComment() {
    	comments = "";
    }
    
    /**
     * Output the content of the fields required for passage between the CoDIMS operators to
     * a string. The fields are the service description IRI, the interface IRI, and the contents
     * of the qosMatching collection.
     * 
     * @return A string containing the required fields.
     */
    public String serializeToString() {
    	
    	StringBuffer ob = new StringBuffer();
    	
    	// Store interface IRI first.
    	ob.append(serviceDescription.getIdentifier().toString());
    	ob.append('\\');
    	ob.append(theInterface.getIdentifier().toString());
    	ob.append('\\');
    	
    	// Store the individual matching results.
    	for(Map.Entry<String,Double> e : qosMatching.entrySet()) {
    		
    		ob.append(e.getKey());
    		ob.append('\\');
    		ob.append(e.getValue().toString());
    		ob.append('\\');
    	}
    	
    	// Done, convert to string and return.
    	return ob.toString();
    }
    
    /**
     * Recreate an InterfaceExt from a serialized string.
     *  
     * @param ib The input string.
     * @return A new InterfaceExt object.
     */
    public static InterfaceExt deserializeFromString(String ib) throws Exception {
    	
    	String[] vals = ib.split("\\\\");
    	
    	ServiceDescription sdesc = (ServiceDescription)WSMLStore.getEntities(vals[0], WSMLStore.SERVICE).iterator().next();
    	InterfaceExt ie = null;
    	for(Object o : sdesc.listInterfaces()) {
    		
    		Interface iface = (Interface)o;    		
    		if(iface.getIdentifier().toString().equals(vals[1]))
    			ie = new InterfaceExt(sdesc,iface,-1);
    	}
    	if(ie == null)
    		throw new Exception("Could not recover service or interface");
    	
    	for(int i=2;i<vals.length;i+=2) {
    		
    		ie.setQoSMatching(vals[i],Double.parseDouble(vals[i+1]));
    	}
    	
    	return ie;
    }
}
