/**
 * DBMS sharing:
 *  * Hung: read from Concept, QoSParameter, AdvertiseUnderContext, ServiceInterface 
 * Note: 
 * 	Ignore QoSContext: 1 interface has only 1 context and one SLA
 *  QoSConformance: only the reported value (advertised value is a range) 
 *   --> predicted QoS value =predicted QoS conformance value
 *  Instance is no need  
 * 
 * Write to: QoSUser, QoSReport, ServiceUsage ReportServiceUsage  QoSParameterinstance
 *           Instance QoSParameterinstance QoSConformance HasReportedValue
 *           
 * 
 * Sebastian: read from
 * Write to:
 */
package ch.epfl.qosdisc.repmgnt;

import ie.deri.wsmx.scheduler.Environment;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ch.epfl.qosdisc.database.Connection;
import ch.epfl.qosdisc.operators.PropertySet;
import lhvu.qos.datastructures.QoSTerm;
import lhvu.qos.datastructures.QoSTermIndexTable;
import lhvu.qos.datastructures.User;
import lhvu.qos.datastructures.UserQoSReport;
import lhvu.qos.generator.QoSModuleInputsGenerator;
import lhvu.qos.utils.Constants;
import lhvu.qos.utils.InputSettingsReader;
import ch.epfl.qosdisc.repmgnt.util.Utilities;
import eduni.simjava.distributions.Sim_normal_obj;
import eduni.simjava.distributions.Sim_parameter_exception;


public class ReputationDataPreparation {
	private QoSModuleInputsGenerator inputGenerator;
	private InputSettingsReader inputSettingsReader;
	
    private Logger projectLog;
    private ResultSet rs=null;
	
	public ReputationDataPreparation(InputSettingsReader inputSettingsReader){
	 	this.inputSettingsReader=inputSettingsReader;	
	 	this.projectLog=Logger.getLogger(this.getClass());
	}

	public void close() throws Exception{
		if (rs!=null) rs.close();		
	}
	/**
	 * Create the script to delete the testing data set 
	 * produced by the Reputation management operators
	 */
	public void deleteTestingDataSet() throws Throwable {
        
		projectLog.info("Begin deleting testing dataset for reputation management operators...");
		
		String 	sqlString="delete from Reputation where 1=1";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);

		sqlString="delete from ReportedValueGroup where 1=1";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);
		
		sqlString="delete from ReportedValue where 1=1";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);

		sqlString="delete from ServiceUsage where 1=1";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);

		sqlString="delete from EstimatedValue where 1=1";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);
		
		sqlString="delete from QoSUser where 1=1";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);
		
		projectLog.info("Finished deleting testing dataset for reputation management operators...");
		
	}



	/**
	 * This method should be called after running LoadDatabase to populate the other tables
	 * 	 * @throws Throwable 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void createTestingDataSet() throws Throwable {

		projectLog.info("Begin creating testing data set for reputation management operators...");
		
		//generate the list of QoS terms		
		QoSTermIndexTable qosTermsTable= getQoSTermsListFromDBMS();
		
		Vector<Integer> interfaceKeyIDsList=getInterfaceIDsFromDBMS();
		
		writeExpectedQoSValue();
		
		//Generate in-memory data
		this.inputGenerator = new QoSModuleInputsGenerator( 1, /*The 1-st run*/ 
                        			  	inputSettingsReader,
		        					  qosTermsTable,
		        					  interfaceKeyIDsList,
		        					  lhvu.qos.utils.Constants.GENERATED_QOS_USER_QUERY_FILE_NAME,
		        					  lhvu.qos.utils.Constants.GENERATED_QOS_SERVICE_MATCHING_INFO_FILE_NAME,
		        					  lhvu.qos.utils.Constants.GENERATED_QOS_SERVICE_REAL_INFO_FILE_NAME,
		        					  lhvu.qos.utils.Constants.GENERATED_USER_QOS_REPORTS_FILE_NAME);
		
		
        inputGenerator.initializeGenerators();				
		projectLog.info("Generated "+ inputGenerator.getAllUserQoSReports().size()+ " reports of " +inputGenerator.getAllUsersList().size() +" users");
		
		writeGeneratedUsers(inputGenerator.getAllUsersList());
		writeGeneratedQoSReports(inputGenerator.getAllUserQoSReports());

		projectLog.info("Finished creating testing data set for reputation management operators.");
	}
	
	/**
	 * Generate and write a list of expected real QoS values 
	 * for each service from the upperbound and lowerbound of each QoS parameter
	 * @param interfaceKeyIDsList
	 */
	private void writeExpectedQoSValue() throws Throwable{

		//Retrieve upperbound, lowerbound value, is higher better for each QoS parameter of each service
		String sqlString="Select id_interface , id_parameter, dt_timestart, dt_timeend, " +
								"vl_lowerbound, vl_upperbound, vl_higherbetter, " +
								"vl_absmin, vl_absmax" +
						 " From AdvertisedValue A, QoSParameter Q " +
						 " Where A.id_parameter=Q.id_concept";

		ResultSet rs=Connection.executeQuery(sqlString);

		Random serviceBehaviorSelector=new Random(Calendar.getInstance().getTimeInMillis());
		Sim_normal_obj serviceRealValueGenerator= new Sim_normal_obj("serviceRealValueGenerator",/*mean*/0.0,0.0/*no variance*/);
        serviceRealValueGenerator.set_seed(Calendar.getInstance().getTimeInMillis());

        Vector<String> batchInsertString=new Vector<String>();
        
        while (rs.next()){
			int id_interface=rs.getInt("id_interface");
			int id_parameter=rs.getInt("id_parameter");

			Timestamp dt_timestart=rs.getTimestamp("dt_timestart");
			if (rs.wasNull())
				dt_timestart=new Timestamp(ch.epfl.qosdisc.repmgnt.util.Constants.STARTING_TIME_POINT);
			
			Timestamp dt_timeend=rs.getTimestamp("dt_timeend");
			if (rs.wasNull())
				dt_timeend=ch.epfl.qosdisc.repmgnt.util.Utilities.getTimeStamp(inputSettingsReader.DEFAULT_TIMEWINDOW);

			double vl_lowerbound=rs.getDouble("vl_lowerbound");
			if (rs.wasNull())
				vl_lowerbound=Constants.UNDEFINED_DOUBLE;
			
			double vl_upperbound=rs.getDouble("vl_upperbound");
			if (rs.wasNull())
				vl_upperbound=Constants.UNDEFINED_DOUBLE;
			
			boolean vl_higherbetter=(rs.getInt("vl_higherbetter")==1);
			if (rs.wasNull())//by default, higher QoS value is better
				vl_higherbetter=true;
			
			double vl_absmin=rs.getDouble("vl_absmin");
			if (rs.wasNull())
				vl_absmin=Constants.UNDEFINED_DOUBLE;
			
			double vl_absmax=rs.getDouble("vl_absmax");
			if (rs.wasNull())
					vl_absmax=Constants.UNDEFINED_DOUBLE;
			
			
			//the number of bad, good, stable services are equal
			byte serviceBehavior=Constants.SERVICE_QUALITY_TENDENCY_STABLE; 
			serviceBehavior = (serviceBehaviorSelector.nextInt()%3 ==0)?Constants.SERVICE_QUALITY_TENDENCY_BETTER:
																	   Constants.SERVICE_QUALITY_TENDENCY_WORSE;
			//FIXME: now all expected values are the same as advertised by the service
//			double expectedQoSValue=generateQoSvalue(serviceRealValueGenerator,serviceBehavior,
//													 vl_lowerbound,vl_upperbound,vl_absmin,vl_absmax,
//													 dt_timestart,dt_timeend,vl_higherbetter);
			double expectedQoSValue = 0.0;
			
	        if(vl_higherbetter){
	        	expectedQoSValue = vl_lowerbound;
	        }else{
	        	expectedQoSValue = vl_upperbound;
	        }

			String insertString="Update AdvertisedValue " +
								" Set vl_expectedValue=" + expectedQoSValue +
								" Where id_interface=" + id_interface + 
								" And id_parameter=" + id_parameter +
								" And dt_timeStart=" + "'"+ dt_timestart +"'"+ 
								" And dt_timeEnd=" + "'"+ dt_timeend + "'";
			
			batchInsertString.add(insertString);
			
		}
		rs.close();

		//Update the generated qos values for all services...
		for (int i = 0; i < batchInsertString.size(); i++) {
			String insertString=batchInsertString.elementAt(i);
			Connection.execute(insertString);
			projectLog.debug("Generated QoS:"+ insertString);
		}

	}

	/**
	 * Generate the expected QoS value for a certain QoS parameter
	 * @param vl_lowerbound
	 * @param vl_upperbound
	 * @param vl_absmin absolute minimum value of this parameter
	 * @param vl_absmax absolute maximum value of this parameter
	 * @param dt_timestart
	 * @param dt_timeend
	 * @param vl_higherbetter
	 * @return
	 * @throws Sim_parameter_exception 
	 */
	static private double generateQoSvalue(Sim_normal_obj serviceRealValueGenerator, byte serviceBehavior, 
									double vl_lowerbound, double vl_upperbound, 
									double vl_absmin, double vl_absmax,
									Timestamp dt_timestart, Timestamp dt_timeend, 
									boolean vl_higherbetter) throws Throwable {
		
		double advertisedValue;
		
		if ((vl_lowerbound==Constants.UNDEFINED_DOUBLE) && 
			(vl_upperbound==Constants.UNDEFINED_DOUBLE))
		{
			//TODO Hung: Verify that this case is only for my testing case where the GUI is not attached 
			//In reality the GUI must assure this should not happen ever
			advertisedValue=100; //some stupid junk for testing
		}
		else if( (vl_lowerbound!=Constants.UNDEFINED_DOUBLE) && (vl_upperbound ==Constants.UNDEFINED_DOUBLE)){
			//only the lowerbound be present  
			advertisedValue=vl_lowerbound;
		}
		else if( (vl_lowerbound==Constants.UNDEFINED_DOUBLE) && (vl_upperbound !=Constants.UNDEFINED_DOUBLE)){
			//only the upperbound be present  
			advertisedValue=vl_upperbound;			
		}
		else{//both upper and lower bounds are present
			advertisedValue=(vl_lowerbound+vl_upperbound)/2.0;
		}
        
		return  getRealQoSValueFromAdvertisedValue(serviceRealValueGenerator,serviceBehavior,advertisedValue,
												   vl_absmin,vl_absmax,vl_higherbetter);
	
	}
//	/**
//	 * Return the generated QoS value for the case we have only a minimum advertised value
//	 * @param serviceRealValueGenerator
//	 * @param serviceBehavior
//	 * @param vl_upperbound
//	 * @param vl_higherbetter
//	 * @return
//	 */
//	static private double getQoSValueWithLowerBound(Sim_normal_obj serviceRealValueGenerator, 
//											byte serviceBehavior, 
//											double vl_lowerbound, 
//											boolean vl_higherbetter) 
//				   							throws Throwable {
//
//		//In this case of having only the lowerbound, it is apparent that vl_higherbetter must be true
//		if (!vl_higherbetter) throw new Exception("Data semantic constraints vilolated: " +
//													"with only the lower bound provided, " +
//													"higher values must represent for better QoS.");
//		
//		//default settings for serviceBehavior ==Constants.SERVICE_QUALITY_TENDENCY_STABLE							
//		double mean = vl_lowerbound + 0.1*vl_lowerbound; //1 is a standard unit of QoS value
//		double variance = 0.25*vl_lowerbound; //stand dev =0.5
//
//		//mean and variances for good services
//		double mean1 = vl_lowerbound + 0.2*vl_lowerbound; //2 is 2*standard unit of QoS
//		double variance1 = 0.25*vl_lowerbound;
//
//		//mean and variances for bad services
//		double mean2= vl_lowerbound - 0.1*vl_lowerbound;
//		double variance2 = 0.25*vl_lowerbound;
//		
//		double generatedQoSvalue= vl_lowerbound;
//		
//		switch(serviceBehavior){
//			case  Constants.SERVICE_QUALITY_TENDENCY_BETTER:
//		        	mean=mean1;
//		        	variance=variance1;
//				break;
//			case  Constants.SERVICE_QUALITY_TENDENCY_WORSE:
//		        	mean=mean2;
//		        	variance=variance2;
//				break;
//		}
//		
//        serviceRealValueGenerator.setMean(mean);
//        serviceRealValueGenerator.setVariance(variance);
//        
//        generatedQoSvalue= serviceRealValueGenerator.sample();
//        
//        if(generatedQoSvalue <0.0) generatedQoSvalue= 0.0;
//        
//        return generatedQoSvalue;
//	}
//
	/**
	 * Given an advertised QoS value, generate real QoS value of the service
	 * @param serviceRealValueGenerator
	 * @param serviceBehavior
	 * @param advertisedValue
	 * @param minValue min physical limitation of this QoS parameter
	 * @param maxValue max physical limitation of this QoS parameter
	 * @param vl_higherbetter
	 * @return
	 */
	static  private double getRealQoSValueFromAdvertisedValue(Sim_normal_obj serviceRealValueGenerator, 
					byte serviceBehavior, double advertisedValue, 
					double minValue, double maxValue, boolean vl_higherbetter) throws Throwable {
		
		//default settings for serviceBehavior ==Constants.SERVICE_QUALITY_TENDENCY_STABLE							
		double mean = advertisedValue;
		double variance = (advertisedValue)*(advertisedValue)*0.01;//variance =10% of advertisedValue

		//mean and variances for good/bad services
		double mean1 = advertisedValue + advertisedValue*0.25;
		double variance1 = mean1*mean1*0.01;//variance =10% of mean;
		
		double mean2= advertisedValue - advertisedValue*0.25;;
		double variance2 = mean2*mean2*0.01;//variance =10% of mean;
        

		switch(serviceBehavior){
			case  Constants.SERVICE_QUALITY_TENDENCY_BETTER:
		        if(vl_higherbetter){
		        	mean=mean1;
		        	variance=variance1;
		        }else{
		        	mean=mean2;
		        	variance=variance2;
		        }
				break;
			case  Constants.SERVICE_QUALITY_TENDENCY_WORSE:
		        if(vl_higherbetter){
		        	mean=mean2;
		        	variance=variance2;
		        }else{
		        	mean=mean1;
		        	variance=variance1;
		        }
				break;
		}
		
        serviceRealValueGenerator.setMean(mean);
        serviceRealValueGenerator.setVariance(variance);
        
        double generatedQoSValue=serviceRealValueGenerator.sample();
        
        
        return ReputationDataPreparation.validate(generatedQoSValue,minValue,maxValue);
        
	}

	/**
	 * 
	 * @param qosValue
	 * @param minValue
	 * @param maxValue
	 * @return
	 */
	public static double validate(double qosValue, double minValue, double maxValue) {
		//restrict 
        if (minValue != Constants.UNDEFINED_DOUBLE && qosValue< minValue) 
        	qosValue=minValue;
        else if (maxValue != Constants.UNDEFINED_DOUBLE  && qosValue > maxValue ) 
        	qosValue =maxValue;
        
        return qosValue;
    }
	
	/**
	 * Get a list of id_interface from the Interface table
	 * @return
	 */
	private Vector<Integer> getInterfaceIDsFromDBMS() throws Exception {
	   	String sqlString="Select id_interface from Interface order by id_interface asc";
    	rs=Connection.executeQuery(sqlString);
    	
    	Vector<Integer> interfaceKeyIDsList=new Vector<Integer>();
		while(rs.next()){
			int id_interface=rs.getInt("id_interface");
			interfaceKeyIDsList.add(id_interface);
		}
		return interfaceKeyIDsList;
	
		
	}

	/**
	 * Write the list of generated reports to the DBMS	 
	 * @param allUserQoSReports
	 */
	private void writeGeneratedQoSReports(Vector<UserQoSReport> userQoSReportsList) throws Exception {
		
		int numReportRowsInserted=0;
		for (int i = 0; i < userQoSReportsList.size(); i++) {
			UserQoSReport report =userQoSReportsList.elementAt(i);

			//get back the id_user of this report
			String sqlString="Select id_user from QoSUser where name= " + "'user_"+report.user.userID + "'";
			rs= Connection.executeQuery(sqlString);
			rs.next();
			int id_user=rs.getInt("id_user");//should return only one row
			rs.close();

			//Get the advertise and min, max value for this QoS parameter
			String queryAdvString="Select vl_expectedValue, vl_absmin, vl_absmax " +
								  " From AdvertisedValue A, QoSParameter Q" + 
								  " Where A.id_interface= " + report.service.serviceID + 
								  " and A.id_parameter=" + report.reportedQoSConformance.qattr.qosTermID+
								  " and Q.id_concept=" + report.reportedQoSConformance.qattr.qosTermID;
			
			rs= Connection.executeQuery(queryAdvString);
			double expectedValue=Constants.UNDEFINED_DOUBLE, minValue=Constants.UNDEFINED_DOUBLE, maxValue=Constants.UNDEFINED_DOUBLE;
			if(!rs.next()) continue;
			
			//the advertised value must be either the lower bound or the lowerbound of the qos parameter
			expectedValue=rs.getDouble("vl_expectedValue");
			minValue=rs.getDouble("vl_absmin");
			if (rs.wasNull()) minValue=Constants.UNDEFINED_DOUBLE;
			maxValue=rs.getDouble("vl_absmax");
			if (rs.wasNull()) maxValue=Constants.UNDEFINED_DOUBLE;
			
			rs.close();	
			
			//do the select to see if the quaruple (id_user,id_interface,dt_timestart,dt_timeend) has been used
			String queryString= "select id_serviceUsage from ServiceUsage where id_user= " + id_user +
																		" and id_interface= " + report.service.serviceID + 
																		" and dt_timestart= " + Utilities.timeStampStr(report.reportedQoSConformance.timePoint)+
																		" and dt_timeend= " +  Utilities.timeStampStr(report.reportedQoSConformance.timePoint);
			rs= Connection.executeQuery(queryString);
			
			int id_serviceUsage;			
			if (!rs.next()){//we can do the insert since this is new data
	
	 			rs.close();
	 			
				String insertSQLstring = "insert into ServiceUsage(id_user,id_interface,dt_timestart,dt_timeend) values("
					+ id_user + ","
	    			+ report.service.serviceID + ","
	    			+ Utilities.timeStampStr(report.reportedQoSConformance.timePoint)+ ","											    			
	    			+ Utilities.timeStampStr(report.reportedQoSConformance.timePoint)
	    			+ ")";
				//write this statement to the script file.
				Connection.execute(insertSQLstring);
				
				
				//get back the id_serviceUsage for later use;
				rs= Connection.executeQuery(queryString);
				if (rs.next())	{
					id_serviceUsage=rs.getInt("id_serviceUsage");//should return only one row
					
					projectLog.debug("Inserted id_serviceUsage=" + id_serviceUsage+ ", query=" + insertSQLstring);

					
					//the reported value stored in the DBMS should be the absolute value,
					//i.e., equal to the conformance + advertisedValue
					//since we define conformance=realValue - advetisedValue
					double absoluteReportedValue= expectedValue*(1+ report.reportedQoSConformance.conformanceValueDifference) ;
					
					absoluteReportedValue= ReputationDataPreparation.validate(absoluteReportedValue,minValue,maxValue);
					
					insertSQLstring = "Insert into ReportedValue (id_serviceUsage,id_parameter,vl_value, vl_estimatedcredibility, vl_realcredibility) " +
														"values ("
															+ id_serviceUsage + ","
															+ report.reportedQoSConformance.qattr.qosTermID + ","	
															+ absoluteReportedValue+ ","
											    			+ report.evaluatedReportCredibility + "," 
															+ report.user.userBehavior  
											    			+ ")";
					//write this statement to the script file.
					projectLog.debug(insertSQLstring);
					Connection.execute(insertSQLstring);
					numReportRowsInserted++;
					
				}//end if (rs.next())
				rs.close();
			
			}//end if (!rs.next())
			
		}//end for browsing userQoSReportsList
		
		projectLog.info("Inserted to database "+numReportRowsInserted+" rows in ReportedValue table");
		
	}//end method
	
	

	/**
	 * Create the script for writting to the tables QoSParameter and QoSContext 
	 * the rows with map uniquely a qosTermID of the qosTable <--> (conceptID, contextID)
	 * @return the QoSIndexingTermsTable corresponding with the generated QoSParameter and QoSContext 	
	 * @throws Exception
	 */
	private QoSTermIndexTable getQoSTermsListFromDBMS() throws Exception{

    	String sqlString="Select id_concept from QoSParameter order by id_concept asc";
    	rs=Connection.executeQuery(sqlString);
    	
    	Vector<QoSTerm> qosTermsList=new Vector<QoSTerm>();
		while(rs.next()){
			int qosTermID=rs.getInt("id_concept");
			QoSTerm qosTerm=new QoSTerm(qosTermID,null);
			qosTermsList.add(qosTerm);
		}
		return new QoSTermIndexTable(qosTermsList);
		
	}

	/**
	 * Write a list of users with various behaviors to the DBMS table QoSUser
	 * @throws Exception
	 */
	private void writeGeneratedUsers(Vector<User> usersList) throws Exception{
		
		int numUserInserted=0;
		
		//write the i-th user to the DBMS		
		for (int i = 0; i < usersList.size(); i++) {
			User user=usersList.elementAt(i);
			//only write the well-defined users to the DBMS
			if(user.userID!=lhvu.qos.utils.Constants.UNDEFINED_INTEGER){
				String insertQuery = "Insert into QoSUser(name,ds_realCredibility , ds_estimatedCredibility ) values("
										+ "'user_"+user.userID + "',"	
										+ Integer.toString(user.userBehavior)+ ","
	      								+ Integer.toString(user.evaluatedUserBehavior)+")";

				//write this statement to the script file.
				Connection.execute(insertQuery);
				
				//get back the id_user for later use;
				String sqlString="Select id_user from QoSUser where name= " + "'user_"+user.userID  + "'";
				rs= Connection.executeQuery(sqlString);
				int id_user=lhvu.qos.utils.Constants.UNDEFINED_INTEGER;
				if (rs.next()){
				  id_user=rs.getInt("id_user");//should return only one row
  				  projectLog.debug("Inserted id_user=" + id_user+ ", query=" + insertQuery);
  				  numUserInserted++;
				}

			}//end if
		}//end for i
		
		projectLog.info("Inserted to database "+numUserInserted+" users");
	}


	/**
	 * This class is to generate data for the simulation of reputation management operators
	 * according to the list of parameters specified in the InputSettings.csv file
	 * The main function should be called after running ch.epfl.qosdisc.wsmx.LoadDatabase  
	 * @author lhvu
	 *
	 */

public static void main(String[] args) {
		
	try {
			boolean fullReset=true;

			// Load the properties.
			PropertySet.setup(".");

			// Open connection to the embedded Derby data server.
			Connection.open(PropertySet.props);
			
		    //default configuration for all loggers
			//org.apache.log4j.BasicConfigurator.configure();
		    PropertyConfigurator.configure(PropertySet.getPath()+"log4j.ReputationDataPreparation.writelog");


			//Preparing to read input parameters
			InputSettingsReader inputSettingsReader=
	        				new InputSettingsReader(PropertySet.getPath()+lhvu.qos.utils.Constants.INPUT_SETTINGS_FILE_NAME);
			//Read input data for this experiment
			inputSettingsReader.readInputSettingsData(1/*numExperiment*/);

			
			//Generate scripts for insert data to database
			ReputationDataPreparation dbPreparation
					= new ReputationDataPreparation(inputSettingsReader);
		    
			if(fullReset)
				dbPreparation.deleteTestingDataSet();//delete testing data of the previous experiments
			
		    dbPreparation.createTestingDataSet(); //create new testing dataset
		
		    dbPreparation.close();
		    
		    //Shutdown the Derby database server
		    Connection.close();
		    
		}catch (Throwable e) {
			e.printStackTrace();
		}				
	}

public static void runWithin(boolean fullReset) {
	
	try {
	    //default configuration for all loggers
		//org.apache.log4j.BasicConfigurator.configure();
//	    PropertyConfigurator.configure(PropertySet.getPath()+"/"+"log4j.ReputationDataPreparation.writelog");

	    String inputSettingStr = PropertySet.getProperty("inputsettings");
	    
    	String localDirStr = "";
    	if (Environment.isCore()){
    		localDirStr = Environment.getKernelLocation().getPath();
    	} else {
    		localDirStr = System.getProperty("user.dir");
    	}
    	inputSettingStr = inputSettingStr.replace("$(local)", localDirStr);
	    
		//Preparing to read input parameters
		InputSettingsReader inputSettingsReader=
        				new InputSettingsReader(inputSettingStr);
		//Read input data for this experiment
		inputSettingsReader.readInputSettingsData(1/*numExperiment*/);

		
		//Generate scripts for insert data to database
		ReputationDataPreparation dbPreparation
				= new ReputationDataPreparation(inputSettingsReader);
	    
		if(fullReset)
			dbPreparation.deleteTestingDataSet();//delete testing data of the previous experiments
		
	    dbPreparation.createTestingDataSet(); //create new testing dataset
	
	    dbPreparation.close();
	    
		} catch(Throwable tw) {
				tw.printStackTrace();
		}
	}

	/**
	 * OBSOLETE METHOD
	 * Get the expected QoS value of one specific service, given the lowerbound and upperbound of the QoS parameters
	 * vl_lowerbound and vl_upperbound should be equal to lhvu.qos.utils.Constants.UNDEFINED_DOUBLE if not provided  
	 * @param interfaceKeyIDsList
	 */
//	static public double getExpectedQoSValueForOneService(double vl_lowerbound,double vl_upperbound,
//											   Timestamp dt_timestart, Timestamp dt_timeend, 
//												boolean vl_higherbetter) throws Throwable{
//
//	Random serviceBehaviorSelector=new Random(Calendar.getInstance().getTimeInMillis());
//
//	Sim_normal_obj serviceRealValueGenerator= new Sim_normal_obj("serviceRealValueGenerator",/*mean*/0.0,0.0/*no variance*/);
//    serviceRealValueGenerator.set_seed(Calendar.getInstance().getTimeInMillis());
//
//    //the number of bad, good, stable services are equal
//	byte serviceBehavior=Constants.SERVICE_QUALITY_TENDENCY_STABLE; 
//	serviceBehavior= (serviceBehaviorSelector.nextInt()%3 ==0)?Constants.SERVICE_QUALITY_TENDENCY_BETTER:
//																   Constants.SERVICE_QUALITY_TENDENCY_WORSE;
//
//	
//	return generateQoSvalue(serviceRealValueGenerator,serviceBehavior,
//												 vl_lowerbound,vl_upperbound,
//												 dt_timestart,dt_timeend,vl_higherbetter);
//	
//	}
}

