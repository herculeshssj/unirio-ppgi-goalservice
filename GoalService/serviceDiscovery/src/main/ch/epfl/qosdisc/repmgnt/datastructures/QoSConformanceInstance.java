package ch.epfl.qosdisc.repmgnt.datastructures;
import org.omwg.ontology.Instance;

/**
 * 
 * @author Le Hung Vu
 * This class is used to hold the absolute value of a QoS INSTANCE,
 *
 */
public class QoSConformanceInstance {

	/**
	 * The key of this QoS parameter in the DBMS (table QoSParameter)
	 *
	 */
	public int qosConceptID;
	
	/**
	 * The mean value & standard deviation of this QoS conformance instance
	 */
	private double mean;
	private double standardDeviation;
	
	/**
	 * @param qosInstanceIdentifier
	 * @param normalizedConf
	 */
	public QoSConformanceInstance(int qosConceptID, double normalizedConf) {
		super();
		this.qosConceptID=qosConceptID;
		this.mean=normalizedConf;
		this.standardDeviation=0.0;
	}
 
	public QoSConformanceInstance(int qosConceptID, double meanConf,double standardDevConf) {
		super();
		this.qosConceptID=qosConceptID;
		this.mean=meanConf;
		this.standardDeviation=standardDevConf;
	}
	
	/**
	 * @return the property corresponding to the mean value of the QoS parameter 
	 */
	public double getMeanAbosulteValue() {
		return this.mean;
	}

	/**
	 * @return the property corresponding to the standard deviation value of the QoS parameter 
	 */
	public double getStdDeviationValue() {
		return this.standardDeviation;
	}

	/**
	 * TODO Query the containing ontology to get the measurement units
	 * of the QoS instance
	 * @return the property corresponding to the measurement unit of the QoS parameter
	 */
	public Instance getMeasurementUnit() {
		return null;
	}

	/**
	 * @return the Identifier of the corresponding QoS concept
	 */
	public int  getQoSConceptID() {
		return this.qosConceptID;
	}
	
	

}
