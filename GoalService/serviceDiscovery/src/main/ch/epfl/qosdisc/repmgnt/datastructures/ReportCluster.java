/*
 * Created on Apr 4, 2006
 */
package ch.epfl.qosdisc.repmgnt.datastructures;

import java.util.Vector;


/**
 * @author Le-Hung Vu
 *
 */
public class ReportCluster {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 6044232078679234017L;

	/**
     * ID of the service interface this report cluster relates to
     */
	public int serviceInterfaceID;
    
    public int clusterID;
    public int startTime;
    
    public int endTime;
    public double evaluatedCredibility;
    public double credibilityWeight;
    
    /**
     * The list of {@link QoSMonitoringReport} objects that
     * are member of this cluster
     */
    private Vector<QoSMonitoringReport> memberReportsList;
    
    /**
     * The centroid report of this cluster
     */
    private double centroidConformance;

	private int qosConceptID;
 
	/**
	 * 
	 * @param serviceInterfaceID
	 * @param qosConceptID
	 * @param startTime
	 * @param endTime
	 * @param clusterID
	 * @param centroidConformance
	 * @param evaluatedCredibility
	 * @param credibilityWeight
	 * @param memberReportsList
	 */
	public ReportCluster(int serviceInterfaceID, int qosConceptID, int startTime, int endTime, 
    		int clusterID, double centroidConformance, double evaluatedCredibility,
            double credibilityWeight, Vector<QoSMonitoringReport> memberReportsList) {
    	
        this.serviceInterfaceID = serviceInterfaceID;
        this.qosConceptID=qosConceptID;
        
        this.centroidConformance=centroidConformance;
        
        this.clusterID = clusterID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.evaluatedCredibility = evaluatedCredibility;
        this.credibilityWeight = credibilityWeight;
        this.memberReportsList = memberReportsList;
        
    }

//    /**
//     * 
//     * @param serviceInterfaceID
//     * @param qosConceptID
//     * @param clusterID
//     * @param startTime
//     * @param endTime
//     * @param evaluatedCredibility
//     * @param credibilityWeight
//     */
//	public ReportCluster(int serviceInterfaceID, int qosConceptID, 
//							int clusterID, 
//							int startTime, 
//							int endTime, 
//							double evaluatedCredibility, 
//							double credibilityWeight) {
//		
//		//only create the list if necessary
//		this.memberReportsList = new Vector<QoSMonitoringReport>();
//	}

    
    /**
     * Default constructor
     *
     */
	public ReportCluster() {
        this.memberReportsList = new Vector<QoSMonitoringReport>();
	}

	public Vector<QoSMonitoringReport> getMemberReportsList() {
		return memberReportsList;
	}

	public void setMemberReportsList(Vector<QoSMonitoringReport> memberReportsList) {
		this.memberReportsList = memberReportsList;
	}

	public double getCentroidConformance() {
		return centroidConformance;
	}

	public void setCentroidConformance(double centroidConformance) {
		this.centroidConformance = centroidConformance;
	}

	public int getQosConceptID() {
		return qosConceptID;
	}

	public void setQosConceptID(int qosConceptID) {
		this.qosConceptID = qosConceptID;
	}

	
}
