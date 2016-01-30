/*
 * Created on Apr 4, 2006
 */
package ch.epfl.qosdisc.repmgnt.datastructures;

import java.util.Vector;

/**
 * @author Le-Hung Vu
 *
 * TODO
 *    
 */

public class ServiceInterfaceExt{



	/**
	 * 
	 */
	private static final long serialVersionUID = 2494705060477982184L;

	/**
	 * The ID of the service interface object (primary key) in the table
	 * 
	 */
	private int serviceInterfaceKeyID; 
	
	/** 
     * 
     * The provided QoS of the corresponding service interface as a list of {@link QoSParameter} objects
     * This is computed from the analysis of the member variable serviceDescription. 
     */
    private Vector<QoSParameter> providedQoSParametersList;
    
    /*Time of the advertisement*/
    private int timeStart;
    private int timeEnd;


    /** 
     * 
     * The estimated QoS of the corresponding service interface as a list of {@link QoSConformanceInstance} objects
     * This is evaluated from the QoS prediction processing step. 
     * The i-th instance of this vector corresponds to the predicted value of the i-th QoSParameter in the
     * list providedQoSParametersList
     */
    private Vector<QoSConformanceInstance> predictedQoSParametersList;

    
   /**
    * 
    * @param serviceInterfaceKeyID the primary key of this service in the table ServiceInterface
    * 
    */
   public ServiceInterfaceExt(int serviceInterfaceKeyID, Vector<QoSParameter> providedQoSParametersList,
		   					  int timeStart, int timeEnd) {
		this.serviceInterfaceKeyID=serviceInterfaceKeyID;
		this.providedQoSParametersList=providedQoSParametersList;
		this.predictedQoSParametersList=new Vector<QoSConformanceInstance>();
	    this.timeStart=timeStart;
		this.timeEnd=timeEnd;

	}



/**
     * The getter for the providedQoSParametersList member variable
     */
    public Vector<QoSParameter> getProvidedQoSParametersList() {
        return providedQoSParametersList;
    }
    /**
     * The setter for the providedQoSParametersList member variable
     */
    public void setProvidedQoSParametersList(Vector<QoSParameter> providedQoSParametersList) {
        this.providedQoSParametersList = providedQoSParametersList;
    }

    /**
     * The getter for the predictedQoSParametersList member variable
     */
    public Vector<QoSConformanceInstance> getPredictedQoSParametersList() {
        return predictedQoSParametersList;
    }
    /**
     * The setter for the predictedQoSParametersList member variable
     */
    public void setPredictedQoSParametersList(Vector<QoSConformanceInstance> predictedQoSParametersList) {
        this.predictedQoSParametersList = predictedQoSParametersList;
    }



	public int getServiceInterfaceKeyID() {
		return serviceInterfaceKeyID;
	}



	public void setServiceInterfaceKeyID(int serviceInterfaceKeyID) {
		this.serviceInterfaceKeyID = serviceInterfaceKeyID;
	}


	public int getTimeEnd() {
		return timeEnd;
	}


	public void setTimeEnd(int timeEnd) {
		this.timeEnd = timeEnd;
	}


	public int getTimeStart() {
		return timeStart;
	}


	public void setTimeStart(int timeStart) {
		this.timeStart = timeStart;
	}

}
