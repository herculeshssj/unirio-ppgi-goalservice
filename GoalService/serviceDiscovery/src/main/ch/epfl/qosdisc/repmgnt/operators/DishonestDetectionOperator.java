/*
 * Created on Apr 4, 2006
 */
package ch.epfl.qosdisc.repmgnt.operators;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;



import ch.epfl.codimsd.connection.TransactionMonitor;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.query.RequestParameter;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.OracleType;
import ch.epfl.codimsd.qep.OpNode;

import ch.epfl.qosdisc.repmgnt.datastructures.QoSMonitoringReport;
import ch.epfl.qosdisc.repmgnt.datastructures.QoSReportedParameter;
import ch.epfl.qosdisc.repmgnt.datastructures.ReputationManagementConfig;
import lhvu.qos.core.QoSReportsPreprocessor;
import lhvu.qos.datastructures.User;
import lhvu.qos.utils.Constants;
import ch.epfl.qosdisc.repmgnt.util.DataCompatibilityHandler;
import ch.epfl.qosdisc.repmgnt.util.Utilities;
import ch.epfl.qosdisc.database.Connection;


import org.apache.log4j.Logger;

/**
 * @author Le-Hung Vu
 *
 * This class is the implementation of the Trust-Distrust Propagation approach
 * based on a certain of trusted reports.
 * The implementation used the utility classes/methods that have been developed before
 * in the file lhvu-qos-rep.jar  
 *    
 */
public class DishonestDetectionOperator extends Operator {

    /**
     * Operator configuration   
     */
    protected ReputationManagementConfig reputationManagementConfig;


    /**
	 *  The pointer to the transaction monitor for the corresponding DBMS
	 *   
	 */
	private TransactionMonitor transactionMonitor;
    
    /**
     * Pointer to the logger of  the project
     */
    private Logger projectLog;

    
    /**
     * Internal variable for keeping the list of QoSMonitoringReport objects
     * 
     */
    private Vector<QoSMonitoringReport> qosMonitoringReportsList;

    

    /**
     * Internal variables for keeping the list of QoSMonitoringReport objects
     * with the discovered credibility
     * 
     */
    private Vector<QoSMonitoringReport> honestReportsList;
	private Vector<QoSMonitoringReport> dishonestReportsList;
	private Vector<QoSMonitoringReport> unknownCredibilityReportsList;

	private Vector<User> honestUsersList;
	private Vector<User> cheatingUsersList;
 
    /**
     * Internal variable for the status of processing
     * 
     * The  finishedProcessing=true means that the list qosReportClustersList
     * already has data. In the getNext() we just simply return one element of this qosReportClustersList.
     * 
     * The  finishedProcessing=false means that in the getNext() 
     * we must do still the trust-distrust processing to produce results and put them in qosReportClustersList
     * 
     */
    private boolean finishedProcessing;
    private boolean testingMode=true; //indicate whether we are in the testing mode or not


   
    

    /**
     * @param id Identification of the operator
     * @param blackBoard the pointer the shared space containing various common information
     */
    public DishonestDetectionOperator(int id, OpNode opNode) {
		super(id);

        //Initialize the list of QoSMonitoringReports
        this.qosMonitoringReportsList= new Vector<QoSMonitoringReport>();
        
        this.honestReportsList= new Vector<QoSMonitoringReport>();
        this.dishonestReportsList= new Vector<QoSMonitoringReport>();
        this.unknownCredibilityReportsList= new Vector<QoSMonitoringReport>();
        
        finishedProcessing=false;

		//get the Logger
        projectLog = Logger.getLogger(this.getClass());

	}
        

	/**
     * Clean the environment after executing the operator
     */
    public void close() throws Exception {
    	projectLog.info("DishonestDetectionOperator(" + id + ") close");

    }

    /**
     * The Initialization method for the DishonestDetectionOperator.
     * We need to read the various configuration information for the
     * reputation management mechanism via the {@link ch.epfl.codimsd.qos.datastructures.ReputationManagementConfig} object 
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
     * Main processing method of the operator DishonestDetectionOperator.
     * 
     * OperatorConfigurationParameter:
     *      o	A ReputationManagementConfig object
     * INPUT TUPLE CONTENT
     *      o	a QoSReport tuple
     * OUPUT TUPLE CONTENT:
     * 		o	a HasReportedValue tuple object with estimated credibility written to the DBMS
     * Implementation:
     * 		o	Inherit from the existing implementation of the QoS Support Module 
     * (already realized in the QoSReportsPreprocessor class). 
     * The getNext() of this operator may need to retrieve all tuples 
     * from its producers before doing the dishonest detection)
     * <p>
     * @throws		Exception
     */
    public DataUnit getNext(int consumerID) throws  Exception {
        
    	if(finishedProcessing==false){
    	//The first time the getNext() of this operator is called
    	//we need to do all the processing
    		
    		if(testingMode) testingCode();
    		else{//real mode
    		
		    		//Get list of previous operator
		    		Collection producers=super.getProducers();
		    		
		    		//Continously read tuples from each of the previous operators
		    		for (Iterator iter = producers.iterator(); iter.hasNext();) {
		    			Operator producer = (Operator) iter.next();
		    		   
		    			Tuple inputTuple;
		    			
		    			do{//read till have all data from this producer
		    				
		    				inputTuple = (Tuple)producer.getNext(id); //id is the identifier of the current operator
		    				
		    				//each of the read tuple should be consist of one QoSMonitoringReport object
		    				QoSMonitoringReport qosReport = buildQoSReportFromTuple(inputTuple); 
		    				
		    				this.qosMonitoringReportsList.add(qosReport);
		    				
		    			} while (inputTuple != null);

	    	    		//Do the trust & distrust propagation on the list of the obtained QoSMonitoringReport
	    	    		//to get a list of ReportCluster objects
	    				doTrustDistrustPropagation();

		    		}//end for iter
    		}//end if not in testing mode

			//Update the processing status
			finishedProcessing=true;
			
    	}//end if finishedProcessing==false 
    		
    	//Here the trust-distrust processing has already finished, 
		
    	//We just write data from the lists
    	//this.honestReportsList, this.dishonestReportsList, this.unknownReportsList
    	//to the DBMS
    	QoSMonitoringReport choosenReport;
    	projectLog.info("Writting list of honest report list...");
    	for (int i = 0; i < this.honestReportsList.size(); i++) {
    		choosenReport = this.honestReportsList.elementAt(i);
    		writeDBMS(choosenReport);
    	}
    	projectLog.info("Writting list of dishonest report list...");
    	for (int i = 0; i < this.dishonestReportsList.size(); i++) {
    		choosenReport = this.dishonestReportsList.elementAt(i);
    		writeDBMS(choosenReport);
    	}
    	projectLog.info("Writting list of unknown crediblity report list...");
    	for (int i = 0; i < this.unknownCredibilityReportsList.size(); i++) {
    		choosenReport = this.unknownCredibilityReportsList.elementAt(i);
    		writeDBMS(choosenReport);
    	}

    	//Update estimated behavior for users in the DBMS
    	User choosenUser;
    	projectLog.info("Update list of honest userst...");
    	for (int i = 0; i < this.honestUsersList.size(); i++) {
    		choosenUser = this.honestUsersList.elementAt(i);
    		writeDBMS(choosenUser);
    	}
    	projectLog.info("Update list of dishonest users...");
    	for (int i = 0; i < this.cheatingUsersList.size(); i++) {
    		choosenUser = this.cheatingUsersList.elementAt(i);
    		writeDBMS(choosenUser);
    	}
    	
    	
    	return (DataUnit)null; 
    }
    
    /* Code for testing the operator*/
	private void testingCode() throws Exception {

    	//Dummy method for reading from DBMS to get a list of Tuple itself.
    	Vector<Tuple> inputTuplesList=readQoSReportTuples();
    	
    	for (int i = 0; i < inputTuplesList.size(); i++) {
			
    		Tuple inputTuple =inputTuplesList.elementAt(i);
			//each of the read tuple should be consist of one QoSMonitoringReport object
			QoSMonitoringReport qosReport = buildQoSReportFromTuple(inputTuple); 
			this.qosMonitoringReportsList.add(qosReport);
    	}	
    	
    	//Do the trust & distrust propagation on the list of the obtained QoSMonitoringReport
    	//to get a list of ReportCluster objects
		doTrustDistrustPropagation();
		
	}


	//end method

	/**
	 * From the received tuple, which is a row of the table QoSReport,
	 * build a list of QoSMonitoringReport object from that tuple.
	 * This requires the reading of predefined views from DBMS
	 * @throws Exception 
	 * 	   
	 */
    private QoSMonitoringReport buildQoSReportFromTuple(Tuple tuple) throws Exception {
		
    	/* The order of data is
		tuple.addData(reportID);
		tuple.addData(qosTermID);
		tuple.addData(reportedValue);
		tuple.addData(vl_expectedValue);
		tuple.addData(estimatedCredibility);
		tuple.addData(realCredibility);

		tuple.addData(userID);
		tuple.addData(userName);
		tuple.addData(interfaceID);

		tuple.addData(timeStart);
		tuple.addData(timeEnd);
		*/

		projectLog.debug("Build a report from tuple:");
    	//read the metadata of the tuple from which to construct 
    	//a list of QoSMonitoringReport
    	OracleType tupleCol=((OracleType)tuple.getData(0));
		int reportID = ((Integer)tupleCol.getObject()).intValue() ;
		projectLog.debug("id_report="+reportID);
    	
    	tupleCol=((OracleType)tuple.getData(1));
		int qosTermID = ((Integer)tupleCol.getObject()).intValue() ;
		projectLog.debug("id_parameter="+qosTermID);
    	
    	tupleCol=((OracleType)tuple.getData(2));
		double reportedValue = ((Double)tupleCol.getObject()).doubleValue() ;
		projectLog.debug("vl_value="+reportedValue);
    	
    	tupleCol=((OracleType)tuple.getData(3));
		double vl_expectedValue = ((Double)tupleCol.getObject()).doubleValue() ;
		projectLog.debug("vl_expectedValue="+vl_expectedValue);
		
		tupleCol=((OracleType)tuple.getData(4));
		int estimatedCredibility = ((Integer)tupleCol.getObject()).intValue() ;
		projectLog.debug("vl_estimatedCredibility="+estimatedCredibility);
    	
    	tupleCol=((OracleType)tuple.getData(5));
		int realCredibility = ((Integer)tupleCol.getObject()).intValue() ;
		projectLog.debug("vl_realCredibility="+realCredibility);
    	
		tupleCol=((OracleType)tuple.getData(6));
		int userID = ((Integer)tupleCol.getObject()).intValue() ;
		projectLog.debug("id_user="+userID);
    	
		tupleCol=((OracleType)tuple.getData(7));
		String userName = ((String)tupleCol.getObject()) ;
		projectLog.debug("user name="+userName);
    	
		tupleCol=((OracleType)tuple.getData(8));
		int interfaceID = ((Integer)tupleCol.getObject()).intValue() ;
		projectLog.debug("id_interface="+interfaceID);
    	
		//the reportStartTime and reportEndTime is supposed to be formatted
		tupleCol=((OracleType)tuple.getData(9));
		Timestamp reportStartTime=((Timestamp)tupleCol.getObject());
		projectLog.debug("dt_timestart="+reportStartTime);
    	
		tupleCol=((OracleType)tuple.getData(10));
		Timestamp reportEndTime=((Timestamp)tupleCol.getObject());
		projectLog.debug("dt_timeend="+reportStartTime);
    	
				
		int startTimePoint=Utilities.getTimePoint(reportStartTime);
		int endTimePoint=Utilities.getTimePoint(reportEndTime);
		
		QoSReportedParameter qosReportedParameter=new QoSReportedParameter(qosTermID,reportedValue,vl_expectedValue);
	      
      
		QoSMonitoringReport qosMonitoringReport=new QoSMonitoringReport(userID,interfaceID,
		    		  													 reportID,
		    		  													 qosReportedParameter, 
		    		  													 startTimePoint,endTimePoint,
		    		  													 estimatedCredibility,
		    		  													 realCredibility);
		      
		return qosMonitoringReport;
	}

    
	/**
	 * Internal method to do the trust-distrust propagation 
	 * based on the list of QoSMonitoringReport objects in the variable qosMonitoringReportsList. 
	 * The result of this method is that we have the 3 lists honestReportsList, dishonestReportsList,
	 * and unknownCredibilityReportsList each element of which is an QoSMonitoringReport 
	 * with the evaluated credibility
	 * @throws Exception 
	 * 	   
	 */
	@SuppressWarnings("unchecked")
	private void doTrustDistrustPropagation() throws Exception {

		projectLog.info("DishonestDetectionOperator(" + id + ") begin the trust-distrust propagation");
		
		

		//Set input data for the handler for transforming to the according formats
		//required by my previous implemented packages lhvu.qos.*.*
		DataCompatibilityHandler dataCompatibilityHandler=new DataCompatibilityHandler(
													this.reputationManagementConfig.getReputationConfigInputSettingReader(),
													this.transactionMonitor,
													this.qosMonitoringReportsList);
		
		projectLog.info("Performing the report preprocessing step for "+dataCompatibilityHandler.getAllUserQoSReports_BasicFormat().size()+	" reports " +
										"with "+dataCompatibilityHandler.getNumberOfTrustedReports()+" trusted reports...");
		
        QoSReportsPreprocessor qosReportsPreprocessor= 
			new QoSReportsPreprocessor(
			        1, /*for one get next we run the algorithm only one time*/
			        this.reputationManagementConfig.getReputationConfigInputSettingReader(),
			        dataCompatibilityHandler.getAllUsersList_BasicFormat(),
			        dataCompatibilityHandler.getAllUserQoSReports_BasicFormat(),
			        dataCompatibilityHandler.getQoSTable_BasicFormat(),
			        dataCompatibilityHandler.getNumberOfTrustedUsers(),
			        dataCompatibilityHandler.getNumberOfTrustedReports()
			        );
        
        qosReportsPreprocessor.preProcessAllUserReports();
        
        //write the evaluated user behaviors to output file 
        qosReportsPreprocessor.writeData(Constants.EVALUATED_USER_REPORT_BEHAVIOR_FILE_NAME);

        //Now the reports have been processed and have results containing in 
        //the qosReportsPreprocessor object according to lhvu.qos.*.* datastructure format
        
        //We ask the DataCompatibilityHandler to translate these results back to our format
        this.honestReportsList = dataCompatibilityHandler.createReportsListWithMatchedCredibility(
        														qosReportsPreprocessor.credibleReportsList);
        
        this.dishonestReportsList = dataCompatibilityHandler.
        												 createReportsListWithMatchedCredibility(qosReportsPreprocessor.incredibleReportsList);
        
        this.unknownCredibilityReportsList = dataCompatibilityHandler.
        												createReportsListWithMatchedCredibility(qosReportsPreprocessor.unknownCredibilityReportsList);
        
        this.honestUsersList=qosReportsPreprocessor.credibleUsersList;
        this.cheatingUsersList=qosReportsPreprocessor.incredibleUsersList;
        
		projectLog.debug("Total reports preprocessed: honest: "+ this.honestReportsList.size()+ 
								", dishonest: " +this.dishonestReportsList.size()+ 
								", uncertain: " +this.unknownCredibilityReportsList.size());
       
	}
	//end method


	/**
	 *	Update the DBMS by writing a QoSMonitoringReport object to the DBMS by updating the ReportedValue table 
	 *	@param 		qosMonitoringReport the report object with the evaluated credibility. 
	 */
	public void writeDBMS(QoSMonitoringReport qosMonitoringReport) throws Exception {

		String sqlString = "Update ReportedValue set vl_estimatedcredibility= "+qosMonitoringReport.evaluatedReportCredibility+
						   " where id_report="+qosMonitoringReport.reportID; 
	
		Connection.execute(sqlString);
		projectLog.debug(sqlString);
		projectLog.debug("Real credibility="+qosMonitoringReport.realReportCredibility);
	}

	/**
	 *	Update the DBMS by writing a User object to the DBMS by updating the QoSUser table 
	 *	@param 		user The User object with the evaluated credibility. 
	 */
	public void writeDBMS(User user) throws Exception {

		String sqlString = "Update QoSUser set ds_estimatedCredibility = "+user.evaluatedUserBehavior+
						   " where id_user="+user.userID; 
	
		Connection.execute(sqlString);
		projectLog.debug(sqlString);
		projectLog.debug("Real credibility="+user.userBehavior);
   	}


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
	


    /**
     * Dummy method for reading a list of tuples from DBMS from the QoSReport table
     *  
     * @return the list of Tuple containing the result set
     * @throws Exception 
     */
    private Vector<Tuple> readQoSReportTuples() throws Exception {
		
    	String sqlString ="Select RV.id_report, RV.id_parameter, RV.vl_value, RV.vl_estimatedcredibility, RV.vl_realcredibility," +
    							  "U.id_user,U.name, " +
    							  "SU.id_interface, SU.dt_timestart, SU.dt_timeend," +
    							  "A.vl_expectedValue" +
    							  
    					  " From ReportedValue RV, ServiceUsage SU, QoSUser U, AdvertisedValue A Where " +
    					  
    					  " RV.id_serviceUsage=SU.id_serviceUsage and " +
    					  " SU.id_user=U.id_user and" +
    					  " SU.id_interface=A.id_interface and" +
    					  " A.id_parameter=RV.id_parameter";
						  //TODO HUNG: more generalized " And A.dt_timestart <= SU.dt_timestart"+
						  //TODO HUNG: more generalized " And A.dt_timeend >= SU.dt_timeend";
    					  
		Vector<Tuple> tuplesList=new Vector<Tuple>();

    	ResultSet rs=Connection.executeQuery(sqlString);

		//for each result set build a corresponding Tuple
    	//TODO Does Othman still use OracleType object for storing column data values?
		while ( rs.next() ) {
	    	
			OracleType reportID= new OracleType();
	    	OracleType qosTermID= new OracleType();
	    	OracleType reportedValue= new OracleType();
	    	OracleType vl_expectedValue= new OracleType();
	    	
	    	OracleType estimatedCredibility= new OracleType();
	    	OracleType realCredibility= new OracleType();
	    	
	    	OracleType userID= new OracleType();
	    	OracleType userName= new OracleType();
			OracleType interfaceID= new OracleType();
			OracleType timeStart= new OracleType();
			OracleType timeEnd= new OracleType();
			
			projectLog.debug("Read report from DBMS:");
			
			reportID.setValue(rs.getInt("id_report"));
			projectLog.debug("id_report="+((Integer)reportID.getObject()).intValue());

			qosTermID.setValue(rs.getInt("id_parameter"));
			projectLog.debug("id_parameter="+((Integer)qosTermID.getObject()).intValue());

			reportedValue.setValue(rs.getDouble("vl_value"));
			projectLog.debug("vl_value="+((Double)reportedValue.getObject()).doubleValue());

			vl_expectedValue.setValue(rs.getDouble("vl_expectedValue"));
			projectLog.debug("vl_expectedValue="+((Double)vl_expectedValue.getObject()).doubleValue());

			estimatedCredibility.setValue(rs.getInt("vl_estimatedcredibility"));
			projectLog.debug("vl_estimatedcredibility="+((Integer)estimatedCredibility.getObject()).intValue());

			realCredibility.setValue(rs.getInt("vl_realcredibility"));
			projectLog.debug("vl_realcredibility="+((Integer)realCredibility.getObject()).intValue());

			userID.setValue(rs.getInt("id_user"));
			projectLog.debug("id_user="+((Integer)userID.getObject()).intValue());

			userName.setValue(rs.getString("name"));
			projectLog.debug("userName="+((String)userName.getObject()));

	    	interfaceID.setValue(rs.getInt("id_interface"));
			projectLog.debug("id_interface="+((Integer)interfaceID.getObject()).intValue());

			timeStart.setValue(rs.getTimestamp("dt_timestart"));
			projectLog.debug("dt_timestart="+((Timestamp)timeStart.getObject()).toString());

			timeEnd.setValue(rs.getTimestamp("dt_timeend"));
			projectLog.debug("dt_timeend="+((Timestamp)timeEnd.getObject()).toString());
			
			
			
			Tuple tuple= new Tuple();
			
			tuple.addData(reportID);
			tuple.addData(qosTermID);
			tuple.addData(reportedValue);
			tuple.addData(vl_expectedValue);
			
			tuple.addData(estimatedCredibility);
			tuple.addData(realCredibility);

			tuple.addData(userID);
			tuple.addData(userName);
			tuple.addData(interfaceID);

			tuple.addData(timeStart);
			tuple.addData(timeEnd);
			
			tuplesList.add(tuple);
			
		}
			
		rs.close();
		
		projectLog.info("Reading total "+ tuplesList.size() + " reports from the database");
		
		return tuplesList;
	}

	
}
//end classs DishonestDetectionOperator