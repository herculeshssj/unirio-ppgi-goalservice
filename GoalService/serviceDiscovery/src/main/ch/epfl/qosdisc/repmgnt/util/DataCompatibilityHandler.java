package ch.epfl.qosdisc.repmgnt.util;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


import ch.epfl.codimsd.connection.TransactionMonitor;
import ch.epfl.qosdisc.repmgnt.datastructures.QoSMonitoringReport;
import ch.epfl.qosdisc.repmgnt.datastructures.QoSReportedParameter;
import ch.epfl.qosdisc.repmgnt.datastructures.ReportCluster;
import ch.epfl.qosdisc.database.Connection;
import ch.epfl.qosdisc.repmgnt.ReputationDataPreparation;
import lhvu.qos.datastructures.QoSConformance;
import lhvu.qos.datastructures.QoSTerm;
import lhvu.qos.datastructures.QoSTermIndexTable;
import lhvu.qos.datastructures.User;
import lhvu.qos.datastructures.UserQoSReport;
import lhvu.qos.datastructures.WebService;
import lhvu.qos.utils.Constants;
import lhvu.qos.utils.InputSettingsReader;
import org.apache.log4j.Logger;
/**
 * 
 * @author Le-Hung Vu
 * This utility class is implemented to transform and create data from
 * the format in current QoSServiceDiscoverySystem project
 * to those required by the package lhvu.qos.*, lhvu.qos.*.*, lhvu.qos.*.*.*
 *
 */
public final class DataCompatibilityHandler {

	/**
	 * The list of QoS reports and IDs of trusted agents
	 * to be transformed to the basic formats
	 */
	private Vector<QoSMonitoringReport> reportsList_extendedFormat;
	private Vector<Integer>   trustedAgentIDsList_ExtendedFormat;
	
	/**
	 *  The pointer to the transaction monitor for the corresponding DBMS
	 *   
	 */
	@SuppressWarnings("unused")
	private TransactionMonitor transactionMonitor;
	
	/**
	 * The list of QoS reports and users
	 * in basic formats
	 */
	private Vector<UserQoSReport> reportsList_basicFormat;
	private Vector<UserQoSReport> trustedReportsList_basicFormat;
	private InputSettingsReader inputSettingReader;
	
	private Vector<User> usersList_basicFormat;
	private QoSTermIndexTable qosTermsTable_basicFormat;

	private Logger projectLog;
	/**
	 * Default constructor
	 */
	public DataCompatibilityHandler(InputSettingsReader inputSettingReader,
									TransactionMonitor transactionMonitor,
									Vector<QoSMonitoringReport> reportsList_extendedFormat
									) 
									throws Exception {
		super();
		
		this.inputSettingReader=inputSettingReader;
		this.reportsList_extendedFormat = reportsList_extendedFormat;
		
		
		this.transactionMonitor=transactionMonitor;
		this.projectLog=Logger.getLogger(this.getClass());
		
		constructTrustedUserIDsFromDBMS();
		
		//populate the variable qosTermsTable_basicFormat
		constructQoSTermIndexTable_BasicFormat();
		
		//populate the variable usersList_basicFormat
		constructUsersList_BasicFormat();

		//construct the list of reports in basic format
		//and populate in the variable reportsList_basicFormat 
		//and trustedReportsList_basicFormat
		constructReportsList_BasicFormat();

	}

	/**
	 * Construct the list of trusted user IDs by querying the DBMS	 *
	 */
	private void constructTrustedUserIDsFromDBMS()throws Exception {
		this.trustedAgentIDsList_ExtendedFormat=new Vector<Integer>(); 
		
		String sqlString="Select id_user,name From QoSUser Where ds_realCredibility="+Constants.USER_BEHAVIOR_TRUSTED;
		ResultSet rs=Connection.executeQuery(sqlString);
    	while ( rs.next() ) {
    		int trustedUserID=rs.getInt("id_user");
    		String trustedUserName=rs.getString("name");
    		this.trustedAgentIDsList_ExtendedFormat.add(trustedUserID);
    		
    		projectLog.debug("Trusted user: name="+ trustedUserName +  ", id_user="+ trustedUserID);
    		
		}
    	rs.close();

		
	}


	/**
	 * From the list of QoSMonitoringReport (new format),
	 * create the two list reportsList_basicFormat and trustedReportsList_basicFormat
	 *
	 */
	private void constructReportsList_BasicFormat() {

		//Browse the list this.reportsList_extendedFormat, build a corresponding list of 
		//reports in basic format in which the list of trusted reports in put in the front of the list
		this.reportsList_basicFormat=new Vector<UserQoSReport>();
		this.trustedReportsList_basicFormat=new Vector<UserQoSReport>();
		
		for (int i = 0; i < this.reportsList_extendedFormat.size(); i++) {
			QoSMonitoringReport qosReport_NewFormat = this.reportsList_extendedFormat.elementAt(i);
			
			//Create a corresponding UserQoS report object
			QoSReportedParameter reportedParam= qosReport_NewFormat.getReportedParameter();
				
			//get the string identifier (key) of this QoS instance 
			int qosTermID=reportedParam.getQosConceptID();

			//create a QoSTerm based on the Identifier of the qos instance
			QoSTerm qosTerm= new QoSTerm(qosTermID,Integer.toString(qosTermID));
				
			double observedValue=reportedParam.getObservedValue();
			double normalizedConformanceValue=reportedParam.getReportedConfValue()-observedValue;
			if (Double.compare(observedValue,0.0)!=0)
				normalizedConformanceValue=normalizedConformanceValue/observedValue;
					
			//create a QoSConformance object to reflect what user report
			QoSConformance reportedConformance= new QoSConformance(qosTerm,normalizedConformanceValue,qosReport_NewFormat.startTime);
			reportedConformance.setConformanceValueDifference(normalizedConformanceValue);
				
			int userID=qosReport_NewFormat.userID;
			User user=null;
			
			//Get the User object from the list of users
			for (int j = 0; j < this.usersList_basicFormat.size(); j++) {
				User existingUser=this.usersList_basicFormat.elementAt(j);
				if(existingUser.userID==userID){
					user=existingUser;
					break;
				}
			}
			
			if (user.userBehavior==Constants.USER_BEHAVIOR_TRUSTED){//trusted user
				qosReport_NewFormat.evaluatedReportCredibility=Constants.REPORT_CREDIBLE;
			}
			
			int serviceInterfaceID=qosReport_NewFormat.serviceInterfaceID;
			UserQoSReport report_basicFormat=new UserQoSReport(qosReport_NewFormat.reportID,
															qosReport_NewFormat.evaluatedReportCredibility,
															user,
															new WebService(serviceInterfaceID),
															reportedConformance,
															ch.epfl.qosdisc.repmgnt.util.Constants.DEFAULT_TRANSACTION_PURPOSE);
				
			//add this report to the list of reports in basic format
			//such that the list of trusted reports is in the head
			if (report_basicFormat.evaluatedReportCredibility==Constants.REPORT_CREDIBLE)
				this.trustedReportsList_basicFormat.add(report_basicFormat);				
			else
				this.reportsList_basicFormat.add(report_basicFormat);

		}//end for i

		//add the list of trusted reports in the front of the list of all reports
		this.reportsList_basicFormat.addAll(0,this.trustedReportsList_basicFormat);
		
	}
	//end method
	
	/**
	 * Check whether a user with a specific id is a trusted agent or not
	 * @param userID the id of the user
	 * @return true if the user is a trusted agent and false otherwise
	 */	
	
	private boolean isTrustedUser(int userID) {
		for (Iterator iter = this.trustedAgentIDsList_ExtendedFormat.iterator();iter.hasNext();) {
			Integer trustedID = (Integer) iter.next();
			if (trustedID.intValue()==userID) return true;
		}
		return false;
	}


	/**
	 * From the list this.trustedAgentIDsList_ExtendedFormat, compute and 
	 * return the number of trusted users.
	 */
	public int getNumberOfTrustedUsers() throws Exception  {
		return this.trustedAgentIDsList_ExtendedFormat.size();
	}

	/**
	 * Return the number of trusted reports
	 */
	public int getNumberOfTrustedReports() throws Exception {
		
		return this.trustedReportsList_basicFormat.size();
	}

	/**
	 * Return the list of all UserQoSReport objects corresponding to the 
	 * list of reports this.reportsList_extendedFormat	
	 * where the list of trusted reports is put the head of this returned list
	 */
	public Vector<UserQoSReport> getAllUserQoSReports_BasicFormat() throws Exception {

		return this.reportsList_basicFormat;
	}

	/**
     * Return the list of keys of distinguished QoS concepts that appear in the set of all QoS reports
     * by querying the DBMS. 
	 */
	private void constructQoSTermIndexTable_BasicFormat() throws Exception {

		//create the QoSTermIndexTable
		Vector<QoSTerm> qosTermsList=new Vector<QoSTerm>();
		
		String sqlString="Select distinct(id_parameter) From ReportedValue order by id_parameter asc";
		ResultSet rs=Connection.executeQuery(sqlString);
    	while ( rs.next() ) {
    		int qosTermID=rs.getInt("id_parameter");
			QoSTerm qosTerm = new QoSTerm(qosTermID,String.valueOf(qosTermID));
			qosTermsList.add(qosTerm);
		}
    	rs.close();
		
		this.qosTermsTable_basicFormat=new QoSTermIndexTable(qosTermsList);
	}

	/**
	 * Construct the list of users who submitted reports (this.usersList_basicFormat)
	 * by querying the DBMS (table ReportedValue, ServiceUsage and QoSUser).
	 */
	private void constructUsersList_BasicFormat() throws Exception {
		
		//Get list of Users in the system who have at least a report
    	this.usersList_basicFormat=new Vector<User>();

    	String sqlString = "SELECT distinct(U.id_user), U.name, U.ds_realCredibility , U.ds_estimatedCredibility  " +
	    				   "FROM QoSUser U, ServiceUsage SU, ReportedValue RV " +
	    				   "WHERE U.id_user = SU.id_user AND " +
	    				   "SU.id_serviceUsage = RV.id_serviceUsage";
	    	
	    ResultSet rs=Connection.executeQuery(sqlString);
	    projectLog.debug("Read a reporting user from DBMS:");
		
	    while ( rs.next() ) {

	    		int userID=rs.getInt("id_user");
	    		projectLog.debug("id_user="+userID);
				
	    		String userName=rs.getString("name");
	    		projectLog.debug("name="+userName);
				
	    		int userEstimatedBehavior=rs.getInt("ds_estimatedCredibility");
	    		projectLog.debug("ds_estimatedCredibility="+userEstimatedBehavior);

	    		int userRealBehavior=rs.getInt("ds_realCredibility");
	    		projectLog.debug("ds_realCredibility="+userRealBehavior);
				
				User user =new User(userID,userRealBehavior,userEstimatedBehavior,inputSettingReader.DEFAULT_TIMEWINDOW);
				
				this.usersList_basicFormat.add(user);
		}
		rs.close();
		
	}//end method

	
	/**
	 * Return the QoSTermIndexTable object which comprises
	 * all QoS parameter appears in the all reports	
	 */
	public QoSTermIndexTable getQoSTable_BasicFormat() throws Exception {
		return this.qosTermsTable_basicFormat;
	}
	
	/**
	 * Return the list of all User those produced reports	
	 */
	public Vector<User> getAllUsersList_BasicFormat() throws Exception {
		return this.usersList_basicFormat;
	}

	/**
	 * Return the list of QoSMonitoringReport in the variable this.reportsList_extendedFormat with the 
	 * credibility and IDs as in the list comparedReportsList
	 * 
	 * @param comparedReportsList the list of UserQoSReport objects to be compared
	 * @return the list of QoSMonitoringReport
	 */
	public Vector<QoSMonitoringReport> createReportsListWithMatchedCredibility(Vector<UserQoSReport> comparedReportsList)
			throws Exception 
	{
		
		Vector<QoSMonitoringReport> matchedReportsList=new Vector<QoSMonitoringReport>();
		
		//browse the report in the basic format
		for (int i = 0; i < comparedReportsList.size(); i++) {
			
			UserQoSReport report_basicFormat= comparedReportsList.elementAt(i);

			//browse the report in extended format 
			for (int j = 0; j < this.reportsList_extendedFormat.size(); j++) {
					
				QoSMonitoringReport report_extendedFormat=this.reportsList_extendedFormat.elementAt(j);

				//compare the reportID of the two reports
				if(report_extendedFormat.reportID==report_basicFormat.report_id){
					
					//copy the evaluated credibility
					report_extendedFormat.evaluatedReportCredibility=report_basicFormat.evaluatedReportCredibility;

					matchedReportsList.add(report_extendedFormat);
				}//end if
					
			}//end j
			
		}//end i
		
		return matchedReportsList;
		
	}//end method

	/**
	 * Construct the list of services which have been in the QoS reports 
	 * by querying the system
	 */
	public Vector<WebService> getServicesList_BasicFormatFromDBMS() throws Exception{
		
		//Get list of ServiceInterfaces in the system which have at least a report
		Collection<Integer> serviceInterfaceIDsList = getAllServiceInterfaceIDsInReportsFromDBMS();
		
		Vector<WebService> servicesList=new Vector<WebService>();

		//with each ServiceInterfaceExt return, create a lhvu.qos.datastructures.WebService object
		for (Iterator iter = serviceInterfaceIDsList.iterator(); iter.hasNext();) {
			Integer serviceInterfaceID = (Integer) iter.next();
			
			servicesList.add(new WebService(serviceInterfaceID.intValue()));
			
		}//end for
		
		return servicesList;
		
	}

	/**
	 * Construct the list of ReportCluster in the new format 
	 * from the list of ReportCluster of the old format
	 */
	@SuppressWarnings("unchecked")
	public Vector<ReportCluster> createReportCluster_ExtendedFormat(Vector reportClustersList) throws Exception {
		Vector<ReportCluster> reportClustersList_Ext=new Vector<ReportCluster>();
		
		for (int i = 0; i < reportClustersList.size(); i++) {
			
			lhvu.qos.datastructures.ReportCluster reportCluster_basicFormat
					=(lhvu.qos.datastructures.ReportCluster)reportClustersList.elementAt(i);
			
			//the evaluated credibility is propotional to the size of the cluster
			double evaluatedCredibility=reportCluster_basicFormat.memberReportsList.size();
			
			//the evaluated weight is not yet determined (temporarily to be as the credibility)
			double credibilityWeight=evaluatedCredibility ;
				
			int serviceInterfaceID= reportCluster_basicFormat.reportedService.serviceID;
			QoSConformance qosConformance= reportCluster_basicFormat.centroidQoSConformance;
			int qosConceptID=qosConformance.qattr.qosTermID;
			double centroidConf=qosConformance.normalizedConformanceValue;

			//Get the advertise value for this QoS parameter
			String queryAdvString="Select vl_expectedValue, vl_absmin, vl_absmax " +
								  "From AdvertisedValue A, QoSParameter Q " + 
								  " Where A.id_interface= " + serviceInterfaceID+
								  " and A. id_parameter=" + qosConceptID+
								  " and Q. id_concept=" + qosConceptID;
			
			ResultSet rs= Connection.executeQuery(queryAdvString);
			double vl_expectedValue=Constants.UNDEFINED_DOUBLE, minValue=Constants.UNDEFINED_DOUBLE, maxValue=Constants.UNDEFINED_DOUBLE;
			if(rs.next()){
				//the advertised value must be either the lower bound or the lowerbound of the qos parameter
				vl_expectedValue=rs.getDouble("vl_expectedValue");
				
				minValue=rs.getDouble("vl_absmin");
				if (rs.wasNull()) minValue=Constants.UNDEFINED_DOUBLE;
				maxValue=rs.getDouble("vl_absmax");
				if (rs.wasNull()) maxValue=Constants.UNDEFINED_DOUBLE;

			}
			rs.close();
			
			double absoluteCentroidValue=ReputationDataPreparation.validate((centroidConf+1)*vl_expectedValue,minValue,maxValue);
			
			//create the member report list in extended format
			Vector<QoSMonitoringReport> memberReportsList_extFormat=createReportsListWithMatchedClusterID(reportCluster_basicFormat.memberReportsList);

			ReportCluster reportCluster_extFormat= new ReportCluster(serviceInterfaceID,
																	 qosConceptID,
																	 reportCluster_basicFormat.startReportTimePoint,
																	 reportCluster_basicFormat.endReportTimePoint,
																	 reportCluster_basicFormat.clusterID,
																	 absoluteCentroidValue,
																	 evaluatedCredibility,
																	 credibilityWeight,
																	 memberReportsList_extFormat
																	 );	
			
			reportClustersList_Ext.add(reportCluster_extFormat);
			
		}
		
		return reportClustersList_Ext;
	}

	/**
	 * Return the list of QoSMonitoringReport in the variable this.reportsList_extendedFormat with the 
	 * clusterID as in the list comparedReportsList
	 * 
	 * @param comparedReportsList the list of UserQoSReport objects to be compared
	 * @return the list of QoSMonitoringReport
	 */

	public Vector<QoSMonitoringReport> createReportsListWithMatchedClusterID(Vector<UserQoSReport> comparedReportsList) {
		
		Vector<QoSMonitoringReport> reportsWithClusterIDsList =new Vector<QoSMonitoringReport>();
		
		//browse the report in the basic format
		for (int i = 0; i < comparedReportsList.size(); i++) {
			
			UserQoSReport report_basicFormat= comparedReportsList.elementAt(i);

			//browse the report in extended format 
			for (int j = this.reportsList_extendedFormat.size(); 
						j < this.reportsList_extendedFormat.size(); j++) {
					
				QoSMonitoringReport report_extendedFormat=this.reportsList_extendedFormat.elementAt(j);

				//compare the reportID of the two reports
				if(report_extendedFormat.reportID==report_basicFormat.report_id){
					
					//copy the clusterID 
					report_extendedFormat.setClusterID(report_basicFormat.currentClusterID);

					reportsWithClusterIDsList.add(report_extendedFormat);
					
				}//end if
					
			}//end j
			
		}//end i
		
		return reportsWithClusterIDsList;

	}

	/**
	 * Get the list of IDs for the ServiceInterfaces in the system which have at least a report
	 * by querying the DBMS tables ReportServiceUsage 
	 */
	public Collection<Integer> getAllServiceInterfaceIDsInReportsFromDBMS() throws Exception {
    	Vector<Integer> serviceInterfaceIDsList = new Vector<Integer>();
	
    	String sqlString = "Select distinct(SU.id_interface) " +
    					   "From ServiceUsage SU, ReportedValue RV " +
    					   "Where SU.id_serviceUsage=RV.id_serviceUsage " +
    					   "Order by id_interface asc";
    	
    	ResultSet rs=Connection.executeQuery(sqlString);
    	
		while ( rs.next() ) {
			int serviceInterfaceID=rs.getInt("id_interface");
			serviceInterfaceIDsList.add(new Integer(serviceInterfaceID));
		}
    	
		rs.close();
    	return serviceInterfaceIDsList;

	}

	/**
	 * Create the list of Web services in basic format 
	 * from the set of input reports in the variable this.reportsList_extendedFormat 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Vector getServicesList_BasicFormatFromInputReports() {
		Vector serviceList=new Vector();
		for (int i = 0; i < this.reportsList_extendedFormat.size(); i++) {
			QoSMonitoringReport report_extFormat=this.reportsList_extendedFormat.elementAt(i);
			int serviceID=report_extFormat.serviceInterfaceID;
			WebService service_basicFormat=new WebService(serviceID);
			
			//do not add duplicate service ids
			int n=0;
			for (int j = 0; j < serviceList.size(); j++) {
				int currentID=((WebService)serviceList.elementAt(j)).serviceID;
				if (currentID==serviceID) break;
				n++;
			}
			if (n==serviceList.size())serviceList.add(service_basicFormat);			
		}
		return serviceList;
	}


    


	
	
}
 