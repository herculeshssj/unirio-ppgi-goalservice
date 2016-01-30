/*
 * Created on Apr 4, 2006
 */
package ch.epfl.qosdisc.repmgnt.datastructures;


import lhvu.qos.utils.Constants;



/**
 * @author Le-Hung Vu
 *
 *    
 */
public class QoSMonitoringReport {



	/**
	 * 
	 */
	private static final long serialVersionUID = -7377095783461864421L;


	/**
     * The key of the user ({@link lhvu.qos.datastructures.User}) of the report (as from the DBMS)
     */
	public int userID;
	
    
    /**
     * The key of the service interface ({@link ch.epfl.codimsd.qos.datastructures.ServiceInterfaceExt}) 
     * of the report (as from the DBMS)
     */
    public int serviceInterfaceID;
	
	
    /**
     * The ID of the corresponding cluster
     * The value would be Constants.UNDEFINED_INTEGER if we do have any other information about
     * the cluster of this report.
     */
	private int clusterID;
    
    /**
     * The reported values of a report. We use one QoSMonitoringReport to collect
     * the feedback value on only one QoS attribute
     */    
    private QoSReportedParameter qosReportedParameter;    
    
    public int startTime;
    public int endTime;
     
    /* the evaluate credibility of this report, which values are defined as 
     * lhvu.qos.util.Constants.REPORT_UNCERTAINED
     * lhvu.qos.util.Constants.REPORT_TRUSTED
     * lhvu.qos.util.Constants.REPORT_CREDIBLE
     * lhvu.qos.util.Constants.REPORT_INCREDIBLE
     * */
    public int evaluatedReportCredibility;
    public int realReportCredibility; /*for simulation purpose */
    
    /* the ID (key) of this report*/
    public int reportID;
    
       /**
     * @param userID
     * @param serviceInterfaceID
     * @param reportID
     * @param qosReportedParameter
     * @param startTime
     * @param evaluatedReportCredibility
     */
    public QoSMonitoringReport(int userID,
    		int serviceInterfaceID,
            int reportID,
            QoSReportedParameter qosReportedParameter, int startTime, int endTime,
            int evaluatedReportCredibility,
            int realReportCrediblity) {
        
    	this.userID = userID;
        this.serviceInterfaceID= serviceInterfaceID;
        this.qosReportedParameter = qosReportedParameter;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reportID=reportID;
        this.evaluatedReportCredibility=evaluatedReportCredibility;
        this.realReportCredibility=realReportCrediblity;
        this.clusterID=Constants.UNDEFINED_INTEGER;
        
    }


    /**
     * Default constructor
     *
     */
	public QoSMonitoringReport() {
		super();
	}
	
	/**
	 * Return the reported parameter value of this report
	 * 
	 */
	public QoSReportedParameter getReportedParameter(){
		return this.qosReportedParameter;		
	}

	/**
	 * Setter for the list of the reported parameter values of this report
	 * 
	 */
	public void setReportedParameters(QoSReportedParameter qosReportedParameter){
		this.qosReportedParameter=qosReportedParameter;		
	}
	

	
	public int getClusterID() {
		return clusterID;
	}

	public void setClusterID(int clusterID) {
		this.clusterID = clusterID;
	}



}
