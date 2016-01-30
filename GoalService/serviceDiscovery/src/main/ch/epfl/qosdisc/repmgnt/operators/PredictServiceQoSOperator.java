/*
 * Created on Apr 4, 2006
 */
package ch.epfl.qosdisc.repmgnt.operators;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;

import lhvu.qos.utils.Constants;

import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;

import ch.epfl.codimsd.connection.TransactionMonitor;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.OracleType;
import ch.epfl.codimsd.qep.OpNode;
import ch.epfl.qosdisc.repmgnt.datastructures.QoSConformanceInstance;
import ch.epfl.qosdisc.repmgnt.datastructures.QoSParameter;
import ch.epfl.qosdisc.repmgnt.datastructures.ReputationManagementConfig;
import ch.epfl.qosdisc.repmgnt.datastructures.ServiceInterfaceExt;
import ch.epfl.qosdisc.repmgnt.util.Utilities;
import ch.epfl.codimsd.query.RequestParameter;
import ch.epfl.qosdisc.database.Connection;
import ch.epfl.qosdisc.repmgnt.ReputationDataPreparation;

/**
 * @author Le-Hung Vu
 *
 * TODO
 *    
 */
public class PredictServiceQoSOperator extends ch.epfl.codimsd.qeef.Operator {


    /**
     * Operator configuration: configuration information for doing the
     * predicting service QoS   
     */
    protected ReputationManagementConfig reputationManagementConfig;

	private Logger projectLog; 
    private boolean testingMode=true;  
	
    /**
	 *  The pointer to the transaction monitor for the corresponding DBMS
	 *   
	 */
	@SuppressWarnings("unused")
	private TransactionMonitor transactionMonitor;

	
	/**
	 * Constructor
	 * @param id
	 * @param blackBoard
	 */
    public PredictServiceQoSOperator(int id, OpNode opNode) {
    	super(id);
		//get the Logger
        this.projectLog = Logger.getLogger(this.getClass());
	}

	
    /**
     * The Initialization method for the PredictServiceQoSOperator.
     * We read the following configuration inforamtion
     * 	 o	A ReputationManagementConfig object
     * <p>
     * @throws		Exception
     */
    
    public void open() throws  Exception {

    	//Let the super class do its initialization job
    	super.open();
    	
        	//Get the operator configuration reputationManagementConfig
	    BlackBoard bl = BlackBoard.getBlackBoard();
        RequestParameter requestParameter = (RequestParameter) bl.get("RequestParameter");
	    reputationManagementConfig=(ReputationManagementConfig) requestParameter.getParameter("ReputationManagementConfig");

    	//Get the transaction Monitor object 
	    transactionMonitor = TransactionMonitor.getTransactionMonitor();

    }
    //end method
    
    /**
     * Main processing method of the operator.
     * INPUT TUPLE CONTENT
     *  	o	a tuple containing interfaceID of the service whose QoS parameters will be estimated 
     * OUPUT TUPLE CONTENT:
     * 		o	the same tuple 
     * Implementation:
     * 		o	Read reputation data from the QoSReputationRepositoryWrapper for each 
     * 		service (for each ServiceInterface we need to read a list of ReportCluster from the DBMS).
     * 		o	The prediction is inherited from the existing implementation of the QoSPredictor 
     * class in the QoS Support Module (already realized)
     * <p>
     * @throws		Exception
     */
    public DataUnit getNext(int consumerID) throws  Exception {
    
    	if(testingMode==true) testingCode();
    	else {
	    	//Read tuples from each of the previous operators
			Tuple inputTuple=(Tuple)super.getNext(super.id); //id is the identifier of the current operator
			
			if (inputTuple != null) {
			    //Do the internal prediction processing for this serviceInterface object 
				doPredictingServiceQoS(buildServiceInterfaceExtFromTuple(inputTuple));
			}
			
		    return inputTuple;
    	}
    	return null;
    }
    //end method
    
	/**
	 *	Write predicted QoS information from a ServiceInterfaceExt object 
	 *  to the the PredictedQoS and QoSConformance tables 
	 *	@param 		ServiceInterfaceExt the object with the predicted QoS 
	 */
	private void writeDBMS(ServiceInterfaceExt serviceInterface) throws Exception{

		for (int i = 0; i < serviceInterface.getPredictedQoSParametersList().size(); i++) {

			projectLog.debug("Predicting service QoS for interface id_interfac="+serviceInterface.getServiceInterfaceKeyID());
			

			//write each qos conformance instance to DBMS
			QoSConformanceInstance qosConformanceInstance=
						serviceInterface.getPredictedQoSParametersList().elementAt(i);

			//Note that we have predicted the absolute value, not the conformance.
			double predictedQoSValue=qosConformanceInstance.getMeanAbosulteValue();
    	    
			
			//Ask if the existing data
			String sqlString="Select id_parameter, id_interface From EstimatedValue " +
							 " Where id_parameter= " + qosConformanceInstance.getQoSConceptID()+
							 " and id_interface=" + serviceInterface.getServiceInterfaceKeyID();
			
			ResultSet rs=Connection.executeQuery(sqlString);
			if (rs.next()) {//data exists, need to do update
				sqlString= "Update EstimatedValue Set value= " +predictedQoSValue+
				" Where id_parameter= " + qosConformanceInstance.getQoSConceptID()+
				" and id_interface=" + serviceInterface.getServiceInterfaceKeyID();
			}
			else{//do a new insert
				sqlString="Insert into EstimatedValue(id_parameter,id_interface,value) " +
				 " values( " +
				 " " +qosConformanceInstance.getQoSConceptID()+
				 "," +serviceInterface.getServiceInterfaceKeyID()+
				 "," + predictedQoSValue + ")";
			}
			rs.close();
			projectLog.info(sqlString);
			Connection.execute(sqlString);
			
    		//Get the expect QoS value of this interface and this parameter (for the latest advertisement) in the DBMS
			sqlString="Select  vl_expectedValue From AdvertisedValue " +
    							" Where id_interface= " +serviceInterface.getServiceInterfaceKeyID()+
    							" And id_parameter= " + qosConformanceInstance.getQoSConceptID();
    		
    		rs=Connection.executeQuery(sqlString);
    		double vl_expectedValue=lhvu.qos.utils.Constants.UNDEFINED_DOUBLE;
    		if (rs.next()) 
    			vl_expectedValue=rs.getDouble("vl_expectedValue");
    		rs.close();
            projectLog.info("Real QoS Value: "+ vl_expectedValue);

		}//end for i
	
	}
	//end method
	

	/**
	 * Internal method to do the predicting QoS procedure
	 * @param serviceInteface the interface to be predict QoS
	 * @throws Exception 
	 * @throws QoSServiceDiscoveryException 
	 * 	   
	 */
    private void doPredictingServiceQoS(ServiceInterfaceExt serviceInteface) throws Exception {
    	
    	if (serviceInteface==null) return;
    	
    	Vector<QoSConformanceInstance> predictedQoSConformanceList=new Vector<QoSConformanceInstance>();
    	
     	//For each QoS parameter advertised in the interface  
    	//we retrieve the list of related ReportCluster from the DBMS
    	for (int i = 0; i < serviceInteface.getProvidedQoSParametersList().size(); i++) {
    		QoSParameter qosAdvertisedParam=serviceInteface.getProvidedQoSParametersList().elementAt(i);
    	
    		int qosConceptID=qosAdvertisedParam.getConceptID();

    		//perform the prediction of future conformance of the parameter qosConcept 
    		QoSConformanceInstance predictedQoSConformance=predictIndividualQoSParameters(serviceInteface,
    																			 qosConceptID);
    		
    		predictedQoSConformanceList.add(predictedQoSConformance);
    		
		}//end i

    	ServiceInterfaceExt processedInterface=serviceInteface;
    	processedInterface.setPredictedQoSParametersList(predictedQoSConformanceList);
    	
    	writeDBMS(processedInterface);
    	
	}

    /**
     * This method return the predicted QoSConformance of a QoS concept of a given service interface 
     * given a list of historical performance 
     * @return a QoSConformanceInstance object represented the predicted value of the qosConcept
     * @throws Exception 
     */
    private QoSConformanceInstance predictIndividualQoSParameters(ServiceInterfaceExt serviceInteface, int qosConceptID) throws Exception {

    	//Get the advertised and min, max value for this QoS parameter
    	String sqlString="Select vl_expectedValue, vl_upperbound, vl_lowerbound, vl_higherbetter, vl_absmin, vl_absmax " +
    			"	from AdvertisedValue A, QoSParameter Q " +
    			  " Where A.id_interface= " +serviceInteface.getServiceInterfaceKeyID()+
    			  " And A.id_parameter="+qosConceptID+
    			  " And Q.id_concept="+qosConceptID;
    	
    	ResultSet rs=Connection.executeQuery(sqlString);
    	boolean vl_higherbetter=true;
    	
    	double  minValue=Constants.UNDEFINED_DOUBLE, maxValue=Constants.UNDEFINED_DOUBLE,
    			advertisedValue=Constants.UNDEFINED_DOUBLE;
		if(rs.next()){

			advertisedValue=rs.getDouble("vl_lowerbound");
			if (rs.wasNull()){
				advertisedValue=rs.getDouble("vl_upperbound");
				if (rs.wasNull()) throw new Exception("No advertised value found");
			}
				
			
			vl_higherbetter=(rs.getInt("vl_higherbetter")==1)?true:false;
			minValue=rs.getDouble("vl_absmin");
			if (rs.wasNull()) minValue=Constants.UNDEFINED_DOUBLE;
			maxValue=rs.getDouble("vl_absmax");
			if (rs.wasNull()) maxValue=Constants.UNDEFINED_DOUBLE;

		}
    	
    	rs.close();

    	
        //Using the simple regression class from Jarkarta Math 1.0 package to predict
        //future value
        SimpleRegression simpleRegression= new SimpleRegression();
        
        
        //Retrieve a list of time points of the reports on this service
        sqlString="Select distinct(dt_timeStart) from ServiceUsage where id_interface="+serviceInteface.getServiceInterfaceKeyID()
                  +" order by dt_timeStart asc";
        
        rs=Connection.executeQuery(sqlString);

        int intervalIndex=0;//each time interval is number from 0,1,2,...
        
        Vector<Integer> timePointsList=new Vector<Integer>();
        //For each possible timeInterval 
        while (rs.next()){
        	
        	int currentStartTimePoint=Utilities.getTimePoint(rs.getTimestamp("dt_timeStart"));
        	timePointsList.add(currentStartTimePoint);        	
        }
        rs.close();

        //this service has no user so far, simply return the advertised value
        if(timePointsList.size()==0)
        	return new QoSConformanceInstance(qosConceptID,advertisedValue);
        
        //otherwise, perform the predicting given the historic performance statistics
        for (int i = 0; i < timePointsList.size(); i++) {
        	int currentStartTimePoint=timePointsList.elementAt(i);
        	
        	
			//Retrieve the list of conformance values representing for the historical perfomance of the parameter qosConcept
    		//by considering only the trusted or credible reports
        	projectLog.debug("Retrieving credible reports...");
        	double evaluatedPastQoSConformanceValue = this.retrieveCredibleReportedValues(serviceInteface.getServiceInterfaceKeyID(),
    																					qosConceptID,minValue,maxValue,
    																					currentStartTimePoint);

    		//If there are no honest reports, then we retrieve the related conformance value 
    		//from the clusters of normal reports and do the weighted average of them 
    		if(evaluatedPastQoSConformanceValue==lhvu.qos.utils.Constants.UNDEFINED_DOUBLE){
    			
    			projectLog.debug("Retrieving weighted centroid conformances from clusters of reports...");
    	    	evaluatedPastQoSConformanceValue = this.retrieveClusteredWeightedQoSCentroidValues(serviceInteface.getServiceInterfaceKeyID(),
    																					qosConceptID,minValue,maxValue,
    																					currentStartTimePoint);
    		}
    		
    		//(DEFAULT REPORTING VALUE IS Based ON number of users)
    		//if there are no cluster of reports for the given (qosConcept, qosContext) of this service interface
    		//during the specific timeperiod, we compute the default reported value of this web service
    		//based on its number of users
    		if(evaluatedPastQoSConformanceValue==lhvu.qos.utils.Constants.UNDEFINED_DOUBLE){
    			projectLog.debug("Computing default reporting value...");
    	    	evaluatedPastQoSConformanceValue= getQoSValueNoReporting(serviceInteface.getServiceInterfaceKeyID(),
    	    															 qosConceptID,advertisedValue,minValue,maxValue,
    	    															 vl_higherbetter,
    	    															 currentStartTimePoint);
    		}//end if
    		
   			//add it to the regression prediction
   			simpleRegression.addData(intervalIndex,evaluatedPastQoSConformanceValue);
    		
   			intervalIndex++;
        }
    
        //From past values, use Least Squared Method to predict the next value
        double predictedValue=ReputationDataPreparation.validate(simpleRegression.predict(intervalIndex),
        														 minValue,maxValue);
    	
        return new QoSConformanceInstance(qosConceptID,predictedValue);
        
	}

    /**
     * Compute the past conformance value of a service interface based on its number of usage 
     * based on assumption that if there are no cluster of reports for the given (qosConcept, qosContext) of this service interface
     * during the specific timeperiod, the conformance should be acceptable
     * @param serviceInterfaceKeyID
     * @param startTimeInterval
     * @param endTimeInterval
     * @return
     * @throws SQLException 
     */
    private double getQoSValueNoReporting(int serviceInterfaceKeyID, 
    									 int qosConceptID, 
    									 double advertisedValue, double minValue, double maxValue,
    									 boolean vl_higherbetter, 
    									 int startTimeInterval) throws Exception {
    
    	double evaluatePastConfPercentage=lhvu.qos.utils.Constants.DEFAULT_QOS_CONFORMANCE;

    	String sqlString="SELECT COUNT(*) FROM ServiceUsage SU";
		
    	ResultSet rs=Connection.executeQuery(sqlString);
    	int totalUsages=0;
    	if (rs.next()) totalUsages=rs.getInt(1);//there should be at most one result in the result set

    	//if there is no user, just return the default value
    	if (totalUsages==0) return evaluatePastConfPercentage;
		
    	//otherwise count the number of users of this service
    	sqlString= "Select count(distinct id_user) From ServiceUsage Where id_interface=" + serviceInterfaceKeyID; 

    	rs=Connection.executeQuery(sqlString);
    	
    	//the past conformance is increased propotionally to the percentage of user of this services over time
    	//but this should be less than a certain fraction
    	if(rs.next())
    		evaluatePastConfPercentage= rs.getInt(1)/(double)totalUsages;
    	
    	if (evaluatePastConfPercentage>lhvu.qos.utils.Constants.MAX_PERCENTAGE_CONF_DEFAULT_NOREPORTING)
    		evaluatePastConfPercentage=	lhvu.qos.utils.Constants.MAX_PERCENTAGE_CONF_DEFAULT_NOREPORTING;
		
    	rs.close();
    	
    	
    	if (vl_higherbetter) 
    		return ReputationDataPreparation.validate(advertisedValue *(1+evaluatePastConfPercentage),minValue,maxValue);
    	else 
    		return ReputationDataPreparation.validate(advertisedValue *(1-evaluatePastConfPercentage),minValue,maxValue);
	}


	/**
     * Retrieve the set of report clusters which contain reports 
     * on a certaint QoS concept of a serviceInterface during a time period.
     * Then return the list of <conformance values*crediblity*crediblityweight> of the centroid reports 
     * of those above clusters.
     *
     */
    private double retrieveClusteredWeightedQoSCentroidValues(int serviceInterfaceID, 
    													int qosConceptID,double minValue,double maxValue, 
    													int startTime) throws Exception{
    	
    	
    	String sqlString="Select RG.vl_centroidconformance, RG.vl_estimatedclustercredibility, RG.vl_credibilityweight" +
    					 " From ReportedValueGroup RG, ReportedValue RV, ServiceUsage SU" +
    					 " Where RG.id_interface=" + serviceInterfaceID+
    					 " And   RG.id_parameter=" +qosConceptID+
    					 " And   RG.id_reportedValueGroup=RV.id_reportedValueGroup" +
    					 " And   RV.id_serviceUsage=SU.id_serviceUsage" +
    					 " And   SU.dt_timeStart <=" + Utilities.timeStampStr(startTime)+
    					 " And   SU.dt_timeEnd   >="+ Utilities.timeStampStr(startTime);
    	
		projectLog.debug(sqlString);

		//Retrieve the centroid data of report in the above list
    	ResultSet rs=Connection.executeQuery(sqlString);
    	double sumConformance=0.0; 
    	double sumWeight=0.0;

		while ( rs.next() ) {
			double meanCentroid=rs.getDouble("vl_centroidconformance"); 
			double clusterCrediblity=rs.getDouble("vl_estimatedclustercredibility");
			double clusterCrediblityWeight=rs.getDouble("vl_credibilityweight");
			projectLog.debug("meanCentroid="+meanCentroid);
			projectLog.debug("clusterCrediblity="+clusterCrediblity);
			projectLog.debug("clusterCrediblityWeight="+clusterCrediblityWeight);
			
			sumWeight +=clusterCrediblityWeight;
			
			sumConformance += meanCentroid*clusterCrediblity*clusterCrediblityWeight ;//the result set should have only one column
		}
		
		rs.close();

		if(sumWeight==0.0) return lhvu.qos.utils.Constants.UNDEFINED_DOUBLE;
		
		else return ReputationDataPreparation.validate((double)sumConformance/sumWeight,minValue,maxValue);
	}


	/**
     * Given a certain service interface id, a qos parameter concept id, 
     * and a start timestamp, retrieve the list of reported QoS conformance values which were shown as HONEST 
     * after the previous trust-distrust propagation phase. 
     *  
     * @param serviceInteface
     * @param qosConceptID
     * @param startTime
     * @return
     * 
     */
    private double retrieveCredibleReportedValues(int serviceInterfaceID, int qosConceptID,
    											  double minValue, double maxValue,
    											  int startTime) throws Exception{

    	    double conformance=	lhvu.qos.utils.Constants.UNDEFINED_DOUBLE;//the default value to return
    		
    	    String sqlString ="Select RV.vl_value" +
    						  " From ServiceUsage SU, ReportedValue RV" +
    						  " Where RV.id_parameter= " + qosConceptID+
    						  " And RV.vl_estimatedcredibility = " + lhvu.qos.utils.Constants.REPORT_CREDIBLE +
    						  " And SU.id_interface= " + serviceInterfaceID+
    						  " And SU.dt_timeStart <=" + Utilities.timeStampStr(startTime)+
    						  " And SU.dt_timeEnd   >=" + Utilities.timeStampStr(startTime)+ 
     						  " And SU.id_serviceUsage = RV.id_serviceUsage";
    		
    						
        	projectLog.debug(sqlString);

        	//Take average of all honest report values
        	ResultSet rs=Connection.executeQuery(sqlString);
        	int count=0;
        	double sumConformance=0.0;
    		while ( rs.next() ) {
    			sumConformance += rs.getDouble("vl_value") ;
        	    projectLog.debug("vl_value="+rs.getDouble("vl_value"));
    			count++;
    		}
    		rs.close();
    		
    		if (count==0) return lhvu.qos.utils.Constants.UNDEFINED_DOUBLE;
    		conformance=sumConformance/count;
    		
    		return ReputationDataPreparation.validate(conformance,minValue,maxValue);
    }

	/**
     * Build a Service Interface object from tuple, which is a row of the ServiceInterface table 
     * by retriving related data from DBMS 
	 * This requires the reading of predefined views from DBMS
	 * @throws Exception 
     */
	private ServiceInterfaceExt buildServiceInterfaceExtFromTuple(Tuple tuple) throws Exception{
    
		if (tuple==null) return null;
		
    	//read the metadata of the tuple from which to construct 
    	//a list of QoSMonitoringReport
    	OracleType tupleCol=((OracleType)tuple.getData(0));
		int serviceInterfaceID = ((Integer)tupleCol.getObject()).intValue() ;
	
		//Retrieve data from the constructed view:
		String sqlString = "Select Q.id_concept, A.dt_timeStart, A.dt_timeEnd" +
						   " From AdvertisedValue A, QosParameter Q" +
						   " Where A.id_interface=" +serviceInterfaceID +
						   " And A.id_parameter=Q.id_concept";

    	ResultSet rs=Connection.executeQuery(sqlString);
		
		//prepare the predicted QoS paramter list
		Vector<QoSParameter> qosParamsList=new Vector<QoSParameter>();
		int startTimePoint=0;
		int endTimePoint=0;

		while ( rs.next() ) {

			  int qosConceptID=rs.getInt("id_concept");
		      Timestamp advertisementStartTimeStamp=rs.getTimestamp("dt_timeStart");
		      Timestamp advertisementEndTimeStamp=rs.getTimestamp("dt_timeEnd");
			     
			  startTimePoint=Utilities.getTimePoint(advertisementStartTimeStamp);
			  if (advertisementEndTimeStamp==null)
				  endTimePoint=reputationManagementConfig.getReputationConfigInputSettingReader().DEFAULT_TIMEWINDOW;
			  else
				  endTimePoint=Utilities.getTimePoint(advertisementEndTimeStamp);
			  
			  QoSParameter qosParam = new QoSParameter(qosConceptID); 
			  
			  qosParamsList.add(qosParam);	
		      
		}//end rs.next()
		
		rs.close();
		
		return new ServiceInterfaceExt(serviceInterfaceID,
									   qosParamsList,
									   startTimePoint,
									   endTimePoint);
		
	}//end method


	
	public ReputationManagementConfig getReputationManagementConfig() {
		return reputationManagementConfig;
	}


	public void setReputationManagementConfig(
			ReputationManagementConfig reputationManagementConfig) {
		this.reputationManagementConfig = reputationManagementConfig;
	}


	@Override
	public void setMetadata(Metadata[] metadata) {
		// TODO Auto-generated method stub
		
	}


	public boolean isTestingMode() {
		return testingMode;
	}


	public void setTestingMode(boolean testingMode) {
		this.testingMode = testingMode;
	}
	
	
    /* Code for testing the operator*/
	private void testingCode() throws Exception {

    	//Read from DBMS to get a list of Tuple containing service interface IDs.
    	Vector<Tuple> inputTuplesList=readTuplesFromServiceInterfaceTable();
    	
    	for (int i = 0; i < inputTuplesList.size(); i++) {
			
    		Tuple inputTuple =inputTuplesList.elementAt(i);

		    //Do the internal prediction processing for this serviceInterface object 
			doPredictingServiceQoS(buildServiceInterfaceExtFromTuple(inputTuple));
	
		}	
    	
	}

	/**
	 * Read the Interface tables to get the list of serviceInterface in the DBMS 
	 * This functions is slightly different from that in the ReportClusteringOperator
	 *  
	 */
	private Vector<Tuple> readTuplesFromServiceInterfaceTable() throws Exception {
	
		    String sqlString ="Select id_interface from Interface order by id_interface asc";
			
			Vector<Tuple> tuplesList=new Vector<Tuple>();
			
			ResultSet rs=Connection.executeQuery(sqlString);
			
			//for each result set build a corresponding Tuple
			//TODO Does Othman still use OracleType object for storing column data values?
			while ( rs.next() ) {
			
				OracleType interfaceID= new OracleType();
				
				interfaceID.setValue(rs.getInt("id_interface"));
				projectLog.debug("Read service interface from DBMS: id_interface="+((Integer)interfaceID.getObject()).intValue());
				
				Tuple tuple= new Tuple();
				tuple.addData(interfaceID);
				
				tuplesList.add(tuple);
			
			}//end rs.next()
			rs.close();
			
			return tuplesList;
		
	}//end method
	
	
	/**
     * Clean the environment after executing the operator
     */
    public void close() throws Exception {
    	projectLog.info("PredictServiceQoS(" + id + ") close");
    	if (!testingMode) super.close();
    }

	
}
