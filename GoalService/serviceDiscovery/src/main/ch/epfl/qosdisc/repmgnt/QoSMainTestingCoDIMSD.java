package ch.epfl.qosdisc.repmgnt;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.QueryManagerImpl;
import ch.epfl.codimsd.query.Request;
import ch.epfl.codimsd.query.RequestParameter;


import ch.epfl.qosdisc.repmgnt.datastructures.ReputationManagementConfig;

import ch.epfl.qosdisc.repmgnt.util.Constants;
import lhvu.qos.utils.InputSettingsReader;

public class QoSMainTestingCoDIMSD {
	
	private Logger projectLog;
	private InputSettingsReader inputSettingsReader;
	private ReputationManagementConfig repConf;
	//private BlackBoard blackBoard;
	
	private Vector<Integer> trustedAgentIDsList;
	private QueryManagerImpl discoveryQueryManagerImpl;
	private Request request;
	
	/**
	 * Internal method for testing the DishonestDetectionOperator
	 * @throws Throwable
	 */
	private void testDishonestDetectionOperator() throws Throwable{
		
		//copy the config file for testing DishonestDetectionOperator
//	    File testConfigFile = new File(System.getProperty("user.dir") + File.separator + "SystemConfigFile_testDishonestDetection.txt");
//	    File newConfigFile =new File(System.getProperty("user.dir") + File.separator + "SystemConfigFile.txt");
//	    
//	    String newConfigContent=ReadWriteTextFile.getContents(testConfigFile);
//	    ReadWriteTextFile.setContents(newConfigFile,newConfigContent);
//	    
//	    projectLog.debug("New config file content: "+ newConfigContent);
		
		//prepare the QEEF DiscoveryQueryManagerImpl for
		this.discoveryQueryManagerImpl =  QueryManagerImpl.getQueryManagerImpl();
		

		this.request=new Request();
		
		this.request.setRequestType(Constants.REQUEST_TYPE_DISHONEST_DETECTION);
		
		//put the reputationmangement object into the requestParameter
		RequestParameter  requestParameter=new RequestParameter();
		requestParameter.setParameter("ReputationManagementConfig", this.repConf);
		
	    BlackBoard bl = BlackBoard.getBlackBoard();
	    bl.put("RequestParameter",requestParameter);

		
		
		this.discoveryQueryManagerImpl.executeRequest(this.request);
		
	}
	
	/**
	 * Internal method for testing the QoSReportClusteringOperator
	 * @throws Throwable
	 */
	private void testQoSReportClusteringOperator() throws Throwable{
		
//		//copy the config file for testing DishonestDetectionOperator
//	    File testConfigFile = new File(System.getProperty("user.dir") + File.separator + "SystemConfigFile_testReportClustering.txt");
//	    File newConfigFile =new File(System.getProperty("user.dir") + File.separator + "SystemConfigFile.txt");
//	    
//	    ReadWriteTextFile.setContents(newConfigFile, ReadWriteTextFile.getContents(testConfigFile));
//	    projectLog.debug("New config file content: "+ ReadWriteTextFile.getContents(testConfigFile));

		//prepare the QEEF DiscoveryQueryManagerImpl for
		this.discoveryQueryManagerImpl =  QueryManagerImpl.getQueryManagerImpl();

		this.request=new Request();

		this.request.setRequestType(Constants.REQUEST_TYPE_REPORT_CLUSTERING);
		
		//put the reputationmangement object into the requestParameter
		RequestParameter  requestParameter=new RequestParameter();
		requestParameter.setParameter("ReputationManagementConfig", this.repConf);
		
	    BlackBoard bl = BlackBoard.getBlackBoard();
	    bl.put("RequestParameter",requestParameter);
		
		this.discoveryQueryManagerImpl.executeRequest(this.request);
	}
	
	
	/**
	 * Internal method for testing the PredictServiceQoSOperator
	 * @throws Throwable
	 */
	private void testPredictServiceQoSOperator() throws Throwable{
		
//		//copy the config file for testing DishonestDetectionOperator
//	    File testConfigFile = new File(System.getProperty("user.dir") + File.separator + "SystemConfigFile_testPredictQoS.txt");
//	    File newConfigFile =new File(System.getProperty("user.dir") + File.separator + "SystemConfigFile.txt");
//
//	    ReadWriteTextFile.setContents(newConfigFile, ReadWriteTextFile.getContents(testConfigFile));
//	    projectLog.debug("New config file content: "+ ReadWriteTextFile.getContents(testConfigFile));
//
		
		//prepare the QEEF DiscoveryQueryManagerImpl for
		this.discoveryQueryManagerImpl =  QueryManagerImpl.getQueryManagerImpl();
		
	
		this.request=new Request();

		this.request.setRequestType(Constants.REQUEST_TYPE_PREDICT_SERVICE_QOS);
		
		//put the reputationmangement object into the requestParameter
		RequestParameter  requestParameter=new RequestParameter();
		requestParameter.setParameter("ReputationManagementConfig", this.repConf);
		
	    BlackBoard bl = BlackBoard.getBlackBoard();
	    bl.put("RequestParameter",requestParameter);
		
	    discoveryQueryManagerImpl.executeRequest(this.request);
	}	
	
	
	/**
	 * Prepare environment for testing
	 * @throws Throwable
	 */
	private void initializeTesting() throws Throwable {
		 
		//default configuration for all loggers (every logger = INFO)
		//org.apache.log4j.BasicConfigurator.configure();
		
		//Use my configuration file for logging
		PropertyConfigurator.configure("log4j.properties.testing");
		//PropertyConfigurator.configure("log4j.properties.writelog");
		
		this.projectLog = Logger.getLogger("ch.epfl.codimsd.qos.testing.QoSMainTesting");
		
		this.projectLog.debug("Intialize testing environment...");
		
		//Preparing to read input parameters
		this.inputSettingsReader=
        				new InputSettingsReader(lhvu.qos.utils.Constants.DIRECTORY_NAME+lhvu.qos.utils.Constants.INPUT_SETTINGS_FILE_NAME);
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
		
	}
	
	/**
	 * Main testing method. Should be called 
	 * after calling LoadDatabase (Sebastian) and ReputationDataPreparation (Hung)
	 * to populate data appropriately.
	 *  
	 */
	public static void main(String[] args) {
		
		try {

			QoSMainTestingCoDIMSD test=new QoSMainTestingCoDIMSD();
			
			test.initializeTesting();
	
			test.testDishonestDetectionOperator();
			
			test.testQoSReportClusteringOperator();
			
			test.testPredictServiceQoSOperator();
			
			test.finalizeTesting();
			
		}catch (Throwable e) {
			e.printStackTrace();
		}
				
	}

	
	/**
	 * 
	 *
	 */
	private void finalizeTesting() throws Throwable {
		projectLog.info("Finished testing.");
	}

}
