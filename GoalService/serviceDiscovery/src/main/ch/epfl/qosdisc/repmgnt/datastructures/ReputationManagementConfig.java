/*
 * Created on Apr 4, 2006
 */
package ch.epfl.qosdisc.repmgnt.datastructures;

import java.util.Vector;

import lhvu.qos.utils.InputSettingsReader;


/**
 * @author Le-Hung Vu
 *
 */
public class ReputationManagementConfig {
    
    /**
     * List of requests (of type {@link QoSMonitoringRequest}) to the trusted agents 
     * for monitoring QoS of certain services.
     * This list is to be produced internally based on the other configuration information
     */
    private Vector<QoSMonitoringRequest> qosMonitoringRequestsList;
    
    /**
     * Configuration for the reputation management algorithm, 
     * re-used from the package lhvu.qos.util.InputSettingReader
     */
    private InputSettingsReader reputationConfigInputSettingReader;

    /**
     * The a Commas Separated Values (CSV) file from which 
     * to read the InputSettingsReader object for the reputation management algorithm
     */
    private String reputationConfigInputSettingFileName;

    
    /**
     * List of IDs of the trusted agents (keys of the User table in DBMS)
     */
    private Vector<Integer> trustedAgentIDsList;
    
    
    public ReputationManagementConfig(InputSettingsReader reputationConfigInputSettingReader, Vector<Integer> trustedAgentIDsList) {
		super();
		this.reputationConfigInputSettingReader = reputationConfigInputSettingReader;
		this.trustedAgentIDsList = trustedAgentIDsList;
		
	}


	/**
     * @param trustedAgentIDsList the list of Integer values containing 
     * 								the list of Identifier of trusted agent IDs  
     * 							  
     * @param reputationConfigInputSettingFileName the CSV file from which to read 
     * 												internal configuration for the trust and reputation algorithm 
     * @param qosReasonerWrapper	pointer to the qosReasonerWrapper
     */
    public ReputationManagementConfig(Vector<Integer> trustedAgentIDsList, 
    								  String reputationConfigInputSettingFileName)
    								throws Exception {
       
    	this.trustedAgentIDsList = trustedAgentIDsList;
        
        this.reputationConfigInputSettingFileName = reputationConfigInputSettingFileName;
        
        
        //We must read the config CVS file here 
        //to construct the InputSettingReader object,
        //as the ReputationMangementConfig object is constructed at
        //local machine where the file path is valid.
        try {
        		this.reputationConfigInputSettingReader = new InputSettingsReader(reputationConfigInputSettingFileName);
        }catch (Throwable e){
        	throw new Exception(e);
        }		
        
        
    }

    
    public Vector<QoSMonitoringRequest> getQosMonitoringRequestsList() {
        return qosMonitoringRequestsList;
    }
    public void setQosMonitoringRequestsList(Vector<QoSMonitoringRequest> qosMonitoringRequestsList) {
        this.qosMonitoringRequestsList = qosMonitoringRequestsList;
    }
    
    public Vector<Integer> getTrustedAgentIDsList() {
        return trustedAgentIDsList;
    }
    public void setTrustedAgentIDsList(Vector<Integer> trustedAgentIDsList) {
        this.trustedAgentIDsList = trustedAgentIDsList;
    }

    public InputSettingsReader getReputationConfigInputSettingReader() {
		return reputationConfigInputSettingReader;
	}

	public void setReputationConfigInputSettingReader(
			InputSettingsReader reputationConfigInputSettingReader) {
		this.reputationConfigInputSettingReader = reputationConfigInputSettingReader;
	}


	public String getReputationConfigInputSettingFileName() {
		return reputationConfigInputSettingFileName;
	}


	public void setReputationConfigInputSettingFileName(
			String reputationConfigInputSettingFileName) {
		this.reputationConfigInputSettingFileName = reputationConfigInputSettingFileName;
	}
}
