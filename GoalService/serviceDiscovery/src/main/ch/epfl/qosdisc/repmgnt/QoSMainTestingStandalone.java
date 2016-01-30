package ch.epfl.qosdisc.repmgnt;

import java.sql.SQLException;
import java.util.Vector;
import java.io.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ch.epfl.qosdisc.repmgnt.datastructures.ReputationManagementConfig;


import ch.epfl.qosdisc.database.Connection;
import ch.epfl.qosdisc.operators.PropertySet;
import ch.epfl.qosdisc.repmgnt.operators.DishonestDetectionOperator;
import ch.epfl.qosdisc.repmgnt.operators.PredictServiceQoSOperator;
import ch.epfl.qosdisc.repmgnt.operators.QoSReportClusteringOperator;
import lhvu.qos.utils.InputSettingsReader;
import lhvu.qos.utils.Constants;

public class QoSMainTestingStandalone {
	
	private Logger projectLog;
	private InputSettingsReader inputSettingsReader;
	private ReputationManagementConfig repConf;
	
	private Vector<Integer> trustedAgentIDsList;
	
	/**
	 * Internal method for testing the DishonestDetectionOperator
	 * @throws Throwable
	 */
	public void testDishonestDetectionOperator() throws Throwable{
	    DishonestDetectionOperator dishonestDetectionOperator=new DishonestDetectionOperator(0,null);
	    
	    dishonestDetectionOperator.setReputationManagementConfig(this.repConf);
	    dishonestDetectionOperator.setTestingMode(true);
	    dishonestDetectionOperator.getNext(1);
	}
	
	/**
	 * Internal method for testing the QoSReportClusteringOperator
	 * @throws Throwable
	 */
	public void testQoSReportClusteringOperator() throws Throwable{
	    QoSReportClusteringOperator clusteringOperator=new QoSReportClusteringOperator(1,null);
	    clusteringOperator.setReputationManagementConfig(this.repConf);
	    clusteringOperator.setTestingMode(true);
	    clusteringOperator.getNext(2);
	}
	
	
	/**
	 * Internal method for testing the PredictServiceQoSOperator
	 * @throws Throwable
	 */
	public void testPredictServiceQoSOperator() throws Throwable{
		PredictServiceQoSOperator predictQoSOperator=new PredictServiceQoSOperator(1,null);
		predictQoSOperator.setReputationManagementConfig(this.repConf);
		predictQoSOperator.setTestingMode(true);
		predictQoSOperator.getNext(3);
	}	
	
	
	/**
	 * Prepare environment for testing
	 * @throws Throwable
	 */
	public void initializeTesting() throws Throwable {
		 
		//default configuration for all loggers (every logger = INFO)
		//org.apache.log4j.BasicConfigurator.configure();
		
		//Use my configuration file for logging
		//PropertyConfigurator.configure("log4j.properties.testing");
		PropertyConfigurator.configure(PropertySet.getPath()+"log4j.properties.writelog");
		
		this.projectLog = Logger.getLogger(this.getClass());
		
		this.projectLog.debug("Intialize testing environment...");
		
		//Preparing to read input parameters
		this.inputSettingsReader=
        				new InputSettingsReader(new FileInputStream(PropertySet.getProperty("inputsettings")));

		//Read input data for this experiment
		this.inputSettingsReader.readInputSettingsData(1/*numExperiment*/);

		//prepare the trusted agent ID list
		this.trustedAgentIDsList=new Vector<Integer>();
		for (int i = 0; i < this.inputSettingsReader.PERCENTAGE_TRUSTED_USERS 
							* this.inputSettingsReader.TOTAL_NUMBER_USERS ; i++) {
			this.trustedAgentIDsList.add(i);
		}
		
		//prepare the reputationManagementConfig
		this.repConf= new ReputationManagementConfig(this.inputSettingsReader,this.trustedAgentIDsList);		
		
		//clear data of the previous runs
		this.clearPreviousRunData();
		
		
	}
	
	/**
	 * Clear the previous data collected by previous runs of the reputation managements 
	 */
	private void clearPreviousRunData() throws SQLException{
		
		projectLog.info("Begin clearing results of previous runs...");
		
		String 	sqlString="Update QoSUser set ds_estimatedCredibility="+ Constants.USER_BEHAVIOR_UNKNOWN +
		                   " where (ds_realCredibility is null) OR (ds_realCredibility <> "+Constants.USER_BEHAVIOR_TRUSTED +")";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);
		
		sqlString="Update ReportedValue set vl_estimatedcredibility="+ Constants.REPORT_UNCERTAINED +
		          " where 1=1";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);
		
		sqlString="delete from ReportedValueGroup where 1=1";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);
		
		sqlString="delete from EstimatedValue where 1=1";
		Connection.execute(sqlString);
		projectLog.debug(sqlString);
		
		projectLog.info("Finished clearing stale dataset.");
		
	}

	/**
	 * Main testing method in standalone mode. 
	 * This should be run after executing the LoadDatabase (Sebastian) 
	 * and ReputationDataPreparation (Hung) to populate the database with appropriate values.
	 *  
	 */
	public static void main(String[] args) {
		
		try {

			// Load the properties.
			PropertySet.setup(".");

			// Open connection to the embedded Derby data server.
			Connection.open(PropertySet.props);

			QoSMainTestingStandalone test=new QoSMainTestingStandalone();
			
			test.initializeTesting();
	
			test.testDishonestDetectionOperator();
			test.testQoSReportClusteringOperator();
			test.testPredictServiceQoSOperator();
			
			test.finalizeTesting();
			
			Connection.close();
			
		}catch (Throwable e) {
			e.printStackTrace();
		}
				
	}

	
	/**
	 * 
	 *
	 */
	public void finalizeTesting() throws Throwable {
		projectLog.info("Finished testing.");
	}

}
