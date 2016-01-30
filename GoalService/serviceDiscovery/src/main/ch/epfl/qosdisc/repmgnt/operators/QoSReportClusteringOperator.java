/*
 * Created on Apr 4, 2006
 */
package ch.epfl.qosdisc.repmgnt.operators;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Vector;


import org.apache.log4j.Logger;

import ch.epfl.codimsd.connection.TransactionMonitor;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.OracleType;
import ch.epfl.codimsd.qep.OpNode;
import ch.epfl.qosdisc.repmgnt.datastructures.QoSMonitoringReport;
import ch.epfl.qosdisc.repmgnt.datastructures.QoSReportedParameter;
import ch.epfl.qosdisc.repmgnt.datastructures.ReportCluster;
import ch.epfl.qosdisc.repmgnt.datastructures.ReputationManagementConfig;
import ch.epfl.qosdisc.repmgnt.datastructures.ServiceInterfaceExt;
import lhvu.qos.core.QoSReportsClusteringProcessor;
import lhvu.qos.utils.Constants;
import ch.epfl.codimsd.query.RequestParameter;
import ch.epfl.qosdisc.database.Connection;
import ch.epfl.qosdisc.repmgnt.util.DataCompatibilityHandler;

/**
 * @author Le-Hung Vu
 *
 */
public class QoSReportClusteringOperator extends ch.epfl.codimsd.qeef.Operator {

	/**
     * Operator configuration: configuration information for doing the
     * predicting service QoS   
     */
    protected ReputationManagementConfig reputationManagementConfig;
 
    /**
     * Pointer to the logger of  the project
     */
    private Logger projectLog;
    private boolean testingMode=true; //indicate whether we are in the testing mode or not

    /**
	 *  The pointer to the transaction monitor for the corresponding DBMS
	 *   
	 */
	private TransactionMonitor transactionMonitor;
	
    /**
     * Internal variable for keeping the list of QoSMonitoringReport objects
     * 
     */
    private Vector<QoSMonitoringReport> allQoSMonitoringReportsList;

    /**
     * Internal variable for keeping the list of ReportCluster objects
     * produced
     */
    private Vector<ReportCluster> reportClusterList;

  
    public QoSReportClusteringOperator(int id, OpNode opNode) {
		super(id);
	    
		//Initialize the list of incoming QoSMonitoringReports
        this.allQoSMonitoringReportsList= new Vector<QoSMonitoringReport>();
        
        //Initialize the list of outgoing ReportCluster
        this.reportClusterList= new Vector<ReportCluster>();
 		projectLog = Logger.getLogger(this.getClass());
	}
	/**
     * Operator configuration: the service interface of which we need to cluster the reports.  
     */
    protected ServiceInterfaceExt serviceInterface;    

    
    /**
     * The Initialization method for the QoSReportClusteringOperator.
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
     * 		o	a row of the ServiceInterface table
     * OUPUT TUPLE CONTENT:
     *   first we send tuples of type
     *   	o	ReportCluster object 
     *   after all newly create clusters have been sent, we will send other tuples of type:
     * 		o	QoSMonitoringReport object (with the clusterID)
     * Implementation:
     * 		o	Inherit from the existing implementation of the QoS Support Module (already realized in the QoSReportClusteringProcessor class)
     * 		o	For each ServiceInterface object we need to retrieve all related QoS reports before doing the clustering
     * <p>
     * @throws		Exception
     */
    public DataUnit getNext(int consumerID) throws  Exception {
 
    	if(testingMode==true) testingCode();
    	else {
	    	//Read tuples from each of the previous operators
			Tuple inputTuple=(Tuple)super.getNext(super.id); //id is the identifier of the current operator
					
	    	//read the metadata of the tuple from which to construct 
	    	//a list of QoSMonitoringReport
	    	OracleType tupleCol=((OracleType)inputTuple.getData(0));
			int serviceInterfaceID = ((BigDecimal)tupleCol.getObject()).intValue() ;
		
			//Do the internal clustering processing for reports on this serviceInterface object 
			//and write to DBMS the related data
			Vector<QoSMonitoringReport> qosReportsList= retrieveRelatedReportsOnServiceInterface(serviceInterfaceID);
			
			doReportClustering(serviceInterfaceID,qosReportsList);
			writeDBMS();

			this.allQoSMonitoringReportsList.addAll(qosReportsList);
			
			//Return the same tuple (i.e., we have finished clustering data for this service interface)
		    return inputTuple;
		 }
    	
    	 return null; //finish processing
    	 
  }
    
    /**
     * Write output of the clustering operator to 
     * the ReportedValue and the ReportedValueGroup tables
     */
    private void writeDBMS() throws Exception{

  	 	ReportCluster choosenCluster;
    	
 		//insert to the ReportedValueGroup table
			
	 	//for each ReportCluster object, 
 		//write a corresponding record in the ReportedValueGroup 
	 	for (int i = 0; i < this.reportClusterList.size(); i++) {
	 		choosenCluster = this.reportClusterList.elementAt(i);

	 		int qosConceptID= choosenCluster.getQosConceptID();
	 		double centroidConformance= choosenCluster.getCentroidConformance();
			
	 		//write to the ReportGroup table
	 		String insertString="Insert into ReportedValueGroup (id_interface, id_parameter, vl_centroidconformance, vl_estimatedclustercredibility, vl_credibilityweight) " +
				"values(" + choosenCluster.serviceInterfaceID+ "," +
							qosConceptID+ "," +
							centroidConformance + "," +
							choosenCluster.evaluatedCredibility + "," + 
				            choosenCluster.credibilityWeight + ")";
		    
	 		Connection.execute(insertString);
	 		
	 		//Get back the id_reportedvaluegroup for using later
	 		String sqlString="Select id_reportedvaluegroup from ReportedValueGroup " +
	 						 " Where  id_interface= " + choosenCluster.serviceInterfaceID+
	 						 " and id_parameter=" + qosConceptID;
	 		
	 		ResultSet rs=Connection.executeQuery(sqlString);
	 		int id_reportedvaluegroup;
	 		if (rs.next()){
	 			id_reportedvaluegroup=rs.getInt("id_reportedvaluegroup");
		 		projectLog.debug("Inserted cluster: id_reportedvaluegroup="+id_reportedvaluegroup 
							+", id_interface="+ choosenCluster.serviceInterfaceID
							+", id_parameter="+ qosConceptID
							+", centroidConformance="+ centroidConformance
							+", evaluatedCredibility="+ choosenCluster.evaluatedCredibility
							+", credibilityWeight="+ choosenCluster.credibilityWeight );

				//for each member report of the above choosenCluster, 
				//update the ReportedValue tables with the id_reportedvaluegroup value 
				for (int j = 0; j < choosenCluster.getMemberReportsList().size(); j++) {
				
				QoSMonitoringReport memberReport=choosenCluster.getMemberReportsList().elementAt(j);
				
				//write to the ReportGroup table
				sqlString="Update ReportedValue " +
						   "Set id_reportedvaluegroup= " + id_reportedvaluegroup +
						   "Where id_report=" + memberReport.reportID;
				Connection.execute(sqlString);
				
				projectLog.debug(sqlString);
				
				}//end for each member report

	 		}//end if rs.next()
	 		rs.close();
	 		
    	}//end for each report cluster

	}
	//end method

    /**
     * Retrieves the related reports on a certain ServiceInterface from the DBMS
     */
	private Vector<QoSMonitoringReport> retrieveRelatedReportsOnServiceInterface(int serviceInterfaceID) throws Exception{
		Vector<QoSMonitoringReport> reportsList= new Vector<QoSMonitoringReport>();
		
		projectLog.debug("Retrieving QoS reports on service interface id="+serviceInterfaceID);

		String sqlString= "Select SU.id_user, U.name, RV.id_parameter, RV.id_report, " +
								 "RV.vl_value, RV.vl_estimatedCredibility, RV.vl_realCredibility, " +
								 "SU.dt_timestart , SU.dt_timestart, " +
								 "A.vl_lowerbound, A.vl_upperbound " +
								 
						  " From ReportedValue RV, ServiceUsage SU, QoSUser U, AdvertisedValue A  " +
						  
						  " Where SU.id_interface= " + serviceInterfaceID +
						  " And A.id_interface="+ serviceInterfaceID +
						  " And A.id_parameter= RV.id_parameter"+
						  //TODO HUNG: more generalized " And A.dt_timestart <= SU.dt_timestart"+
						  //TODO HUNG: more generalized " And A.dt_timeend >= SU.dt_timeend"+
						  " And SU.id_serviceUsage=RV.id_serviceUsage" +
						  " And SU.id_user=U.id_user";
		
		
    	ResultSet rs=Connection.executeQuery(sqlString);

		while ( rs.next() ) {
			
			int userID=rs.getInt("id_user");
			projectLog.debug("userID="+userID);
			
			String userName=rs.getString("name");
			projectLog.debug("username="+userName);
			
			int qosConceptID= rs.getInt("id_parameter");
			projectLog.debug("qosConceptID="+qosConceptID);
			
			int reportID=rs.getInt("id_report");		
			projectLog.debug("reportID="+reportID);
			
			double reportedValue= rs.getDouble("vl_value");
			projectLog.debug("reportedValue="+reportedValue);
			
			//only the lowerbound or the upperbound is present and this would be the advertised value
			double advertisedValue= rs.getDouble("vl_lowerbound");
			if(rs.wasNull()){
				advertisedValue= rs.getDouble("vl_upperbound");
				if (rs.wasNull()) throw new Exception("Advertised value for QoS parameter id=" +qosConceptID+
														", service id= " +serviceInterfaceID+
														"not found");
			}
			projectLog.debug("advertisedValue="+advertisedValue);
			
			
			int evaluatedReportCredibility=rs.getInt("vl_estimatedCredibility");
			projectLog.debug("evaluatedReportCredibility="+evaluatedReportCredibility);
			
			int reportRealCredibility=rs.getInt("vl_realCredibility");
			projectLog.debug("reportRealCredibility="+reportRealCredibility);
			
			int startTimePoint=ch.epfl.qosdisc.repmgnt.util.Utilities.getTimePoint(rs.getTimestamp("dt_timestart"));
			projectLog.debug("startTimePoint="+startTimePoint);
			
			int endTimePoint=ch.epfl.qosdisc.repmgnt.util.Utilities.getTimePoint(rs.getTimestamp("dt_timestart"));
			projectLog.debug("endTimePoint="+endTimePoint);
			
			QoSMonitoringReport qosReport=new QoSMonitoringReport(userID,serviceInterfaceID,
																  reportID,
																  new QoSReportedParameter(qosConceptID,reportedValue,advertisedValue),
																  startTimePoint,endTimePoint,
																  evaluatedReportCredibility,
																  reportRealCredibility);
			reportsList.add(qosReport);
			
		}//end rs.next()
		rs.close();
		projectLog.debug("Retrieved "+reportsList.size()+" QoS reports on interface id ="+serviceInterfaceID);

		
		return reportsList;
	}
	/**
	 * Internal method to do the clustering procedure
	 * based on the list of QoSMonitoringReport objects in the variable allQoSMonitoringReportsList. 
	 * The result of this method is that we have the list qosReportClustersList, each element of which
	 * is an ReportCluster which holds the group of reports with the evaluated credibility
	 * @throws Exception 
	 * 	   
	 */

    @SuppressWarnings("unchecked")
	private void doReportClustering(int serviceInterfaceID,Vector <QoSMonitoringReport> qosReportsList) throws Exception{
		
		
		//Set input data for the handler for transforming to the according formats
		//required by my previous implemented packages lhvu.qos.*.*
		DataCompatibilityHandler dataCompatibilityHandler= 
						new DataCompatibilityHandler(this.reputationManagementConfig.getReputationConfigInputSettingReader(),
													this.transactionMonitor,
													qosReportsList);
		
		
		projectLog.info("Begin the clustering process of "+dataCompatibilityHandler.getAllUserQoSReports_BasicFormat().size()+" reports..." +
						 " for interface ID="+serviceInterfaceID);
		
		
		QoSReportsClusteringProcessor qosReportsClusteringProcessor=
				new QoSReportsClusteringProcessor(
						1, /*for one get next we run the algorithm only one time*/
				        this.reputationManagementConfig.getReputationConfigInputSettingReader(),
				        dataCompatibilityHandler.getAllUserQoSReports_BasicFormat(),
				        dataCompatibilityHandler.getQoSTable_BasicFormat().qosTermsList,
				        dataCompatibilityHandler.getServicesList_BasicFormatFromInputReports()
				        );

		
		qosReportsClusteringProcessor.performKMeansClustering();
		
		//write the clustere report to output file 
        qosReportsClusteringProcessor.writeData(Constants.REPORTS_CLUSTERING_OUTPUTS_FILE_NAME);

        //create the list of ReportCluster
        this.reportClusterList = dataCompatibilityHandler.
				createReportCluster_ExtendedFormat(qosReportsClusteringProcessor.reportClustersList);
          	
        projectLog.info("Created totally "+ this.reportClusterList.size()+" clusters...");
		
	}//end method
    
	/**
     * Clean the environment after executing the operator
     */
    public void close() throws Exception {
    	projectLog.info("QoSReportClusteringOperator(" + id + ") close");
    	if (!testingMode) super.close();
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


    /* Code for testing the operator*/
	private void testingCode() throws Exception {

    	//Read from DBMS to get a list of Tuple containing service interfaces.
    	Vector<Tuple> inputTuplesList=readTuplesFromServiceInterfaceTable();
    	
    	for (int i = 0; i < inputTuplesList.size(); i++) {
			
    		Tuple inputTuple =inputTuplesList.elementAt(i);

	    	//read the serviceinterface ID from the tuple
    		OracleType colData=(OracleType)inputTuple.getData(0);
			int serviceInterfaceID = ((Integer)colData.getObject()).intValue();
	
			
			//retrieve the list of related reports
			Vector<QoSMonitoringReport> qosReportsList= retrieveRelatedReportsOnServiceInterface(serviceInterfaceID);
			
			//Do the internal clustering processing for all reports 
			//and write to DBMS the related data
			doReportClustering(serviceInterfaceID,qosReportsList);
			writeDBMS();

			this.allQoSMonitoringReportsList.addAll(qosReportsList);
    	}	
    	
	}
	
	/**
	 * Read the Interface tables to get the list of serviceInterface that appears in all reports
	 *  
	 */
	private Vector<Tuple> readTuplesFromServiceInterfaceTable() throws Exception {
	
		    String sqlString ="Select distinct(SU.id_interface)" +
 							 "From ReportedValue RV, ServiceUsage SU " +
 							 "Where RV.id_serviceUsage=SU.id_serviceUsage " +
 							 "Order by id_interface asc";
			
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

	
	

}//end class
