package ch.epfl.qosdisc.repmgnt.datastructures;

/**
 * A reported value for a certain QoS parameter. 
 * This class is designed such that we can further stored different tpyes of reported values in it
 * @author lhvu
 *
 */

public class QoSReportedParameter {

	private int qosConceptID;
    private double reportedConformanceValue;
    private double observedValue;

	/**
	 * @param qosConceptID
	 * @param reportedConformanceValue
	 */
	public QoSReportedParameter(int qosConceptID, double reportedValue, double advertisedValue) {
		this.qosConceptID=qosConceptID;
		this.reportedConformanceValue=reportedValue;
		this.observedValue=advertisedValue;
	}

	public int getQosConceptID() {
		return qosConceptID;
	}

	public void setQosConceptID(int qosConceptID) {
		this.qosConceptID = qosConceptID;
	}

	public double getReportedConfValue() {
		return reportedConformanceValue;
	}

	public void setReportedConfValue(double reportedConformanceValue) {
		this.reportedConformanceValue = reportedConformanceValue;
	}

	public double getObservedValue() {
		return observedValue;
	}

	public void setAdvertisedValue(double observedValue) {
		this.observedValue = observedValue;
	}

}
