/*
 * Created on Apr 4, 2006
 */
package ch.epfl.qosdisc.repmgnt.datastructures;

/**
 * @author Le-Hung Vu
 *
 */
public class QoSParameter {
    
	/**
	 * The corresponding ontological concept of the QoS parameter being mentioned
	 */
    private int qosConceptID;
    
     /**
     * Construct a QoS parameter for a certain qosConceptID
     * 
     */
    public QoSParameter(int qosConceptID) {
        this.qosConceptID = qosConceptID;
    }
    //end method
    
     
    public int getConceptID() {
		return qosConceptID;
	}
	public void setConceptID(int qosConceptID) {
		this.qosConceptID = qosConceptID;
	}
	    
}

