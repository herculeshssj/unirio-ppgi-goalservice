/*
 * Copyright (c) 2008 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */

package org.deri.wsmx.unittest.util;

import ie.deri.wsmx.choreography.Radex;
import ie.deri.wsmx.core.logging.CleanPatternLayout;
import ie.deri.wsmx.discovery.DiscoveryFramework;
import ie.deri.wsmx.executionsemantic.AchieveGoalChor;
import ie.deri.wsmx.executionsemantic.ExecutionSemanticsFinalResponse;
import ie.deri.wsmx.executionsemantic.State;
import ie.deri.wsmx.executionsemantic.WSMXExecutionSemantic.Event;
import ie.deri.wsmx.invoker.Invoker;
import ie.deri.wsmx.resourcemanager.inmemory.InMemoryRM;
import ie.deri.wsmx.scheduler.Environment;
import ie.deri.wsmx.servicediscovery.ServiceDiscoveryFramework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.xml.ws.Endpoint;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.deri.wsmx.mediation.ooMediator.wsml.WSMLDataMediator;
import org.ipsuper.nexcom.services.CEOApprovalWebService;
import org.ipsuper.nexcom.services.LegalDepartmentWebService;
import org.ipsuper.nexcom.services.VoIPSEEEntryPoint;
import org.ipsuper.nexcom.services.WholesaleSupplierWebService;
import org.ipsuper.prereview.PackagerWebService;
import org.omwg.ontology.Ontology;
import org.wsmo.execution.common.component.DataMediator;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.DiscoveryType;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

/* 
 * WSMX execution utility class
 */

public class WSMXExecution {
	
	protected static Logger logger = Logger.getLogger(WSMXExecution.class);
	private Radex cEngine;
	private Invoker invoker;
	private DiscoveryFramework webServiceDiscovery;
	private ServiceDiscoveryFramework serviceDiscovery;
	private DataMediator dataMediator;
	private InMemoryRM resourceManager;
	private WsmoFactory wsmoFactory;
	
	static {
		int port = 8001;
		try {
			ServerSocket sock = new ServerSocket(port);
			sock.close();

			//publish Web services
			Endpoint.publish("http://localhost:8001/VoIPProviders", new WholesaleSupplierWebService());
			Endpoint.publish("http://localhost:8001/VoIPSEEEntryPoint", new VoIPSEEEntryPoint());
			Endpoint.publish("http://localhost:8001/CEOApproval", new CEOApprovalWebService());
			Endpoint.publish("http://localhost:8001/LegalDepartment", new LegalDepartmentWebService());
			Endpoint.publish("http://localhost:8001/Packager", new PackagerWebService());
		}catch(BindException b) {
			logger.info(port + " port is not available");
		}catch (IOException e) {
			//ignore
		}

    	Environment.setIsCore(false);
		
    	String configPath = "dist"+File.separator+"config.properties";
    	File configFile = new File(configPath);
    	
        boolean defaultLogging = false;
        URL propertiesURL = null;
        try {
			if (!configFile.canRead())
				throw new FileNotFoundException();
        	propertiesURL =  configFile.toURI().toURL();
        } catch (Throwable t) {
        	defaultLogging = true;
		}
        if (!defaultLogging) {
        	PropertyConfigurator.configure(propertiesURL);
        	
           	//make properties available to the components
	    	Properties p;
			try {
				p = new Properties();
				InputStream stream = new FileInputStream("dist"+File.separator+"config.properties");
				p.load(stream);
				stream.close();
				
				//shallow copy properties
				Environment.setConfiguration((Properties)p.clone());
			} catch (Exception e) {
				e.printStackTrace();
			}
        } else {
        	PatternLayout layout = new CleanPatternLayout("%-5p %-25c{1}: %m%n");
        	BasicConfigurator.configure(new ConsoleAppender(layout));
	    	Logger.getRootLogger().setLevel(Level.FATAL);
	    	Logger.getLogger("org.deri").setLevel(Level.DEBUG);
	    	Logger.getLogger("ie.deri").setLevel(Level.DEBUG);
	    	Logger.getLogger("at.deri").setLevel(Level.INFO);
	    	Logger.getLogger("com").setLevel(Level.FATAL);
	    	Logger.getLogger("org").setLevel(Level.FATAL);
	    	Logger.getLogger("net").setLevel(Level.FATAL); 	
        	logger.warn("Property configuration of log4j failed, falling back to default configuration.");        
        }
        logger.info("Repositório WSML: " + configFile.getAbsolutePath());
	}	

	/*
	 * Initiation of WSMX components. Starting internally hosted JAX-WS Web services. 
	 * It should be commented out when WSMX is running in Server mode since WSMX starts 
	 * the same Web services anyways and there is a clash of ports.
	 * Tests share set-up dependency due to the components initialization.
	 */
	public WSMXExecution () {
		cEngine = new Radex();
		invoker = new Invoker();
		resourceManager = new InMemoryRM();
		webServiceDiscovery = new DiscoveryFramework();
		serviceDiscovery = new ServiceDiscoveryFramework();
		serviceDiscovery.instanceBasedDiscovery.injectComponents(cEngine, invoker);
		dataMediator = new WSMLDataMediator(); // MoonDataMediator();
		wsmoFactory = Factory.createWsmoFactory(null);
	}
	
	public List<String> runDiscoveryDataMediationAndChoreography(String goalIRIStr, String goalOntoIRIStr) throws UnsupportedOperationException, ComponentException {
		TimeMeasure tm = new TimeMeasure();
		List<String> finalResult =  new ArrayList<String>();
		Goal goal = null;
		Ontology goalOnto = null;
		
		if (goalIRIStr!= null && !goalIRIStr.equals(""))
			goal = resourceManager.retrieveGoal(wsmoFactory.createIRI(goalIRIStr));
		
		if (goal != null){
			logger.info("Retrieved Goal: " +goalIRIStr);
		} else if (goalIRIStr== null || goalIRIStr.equals("")){
			logger.error("Goal has not been provided.");
			tm.finish();
			return finalResult;
		} else {
			logger.error("Goal has not been found: " +goalIRIStr);
			tm.finish();
			return finalResult;
		}
		
		if (goalOntoIRIStr!= null && !goalOntoIRIStr.equals(""))
			goalOnto = resourceManager.retrieveOntology(wsmoFactory.createIRI(goalOntoIRIStr));
		
		if (goalOnto != null){
			logger.info("Retrieved Goal Ontology: " + goalIRIStr);
		} else if (goalOntoIRIStr== null || goalOntoIRIStr.equals("")){
			logger.info("Goal Ontology has not been provided.");
		} else {
			logger.error("Goal Ontology has not been found: " +goalIRIStr);			
		}
		
		//initialize Execution Semantics
		AchieveGoalChor achieveGoalES = new AchieveGoalChor();
		achieveGoalES.wsmxInvoker = invoker;
		
		achieveGoalES.goal = goal;
		achieveGoalES.goalOnto = goalOnto;
		achieveGoalES.requiredDiscovery = DiscoveryType.getRequiredDiscoveryType(goal);
		
		State esState = achieveGoalES.new GetWebServiceFromRM(null);
		
		ExecutionSemanticsFinalResponse esResponse = runExecutionSemantics(esState, achieveGoalES);

		if (esResponse.getReceivedMessages()!= null && esResponse.getReceivedMessages().size() != 0)
			//return non-empty list if some data was received
			finalResult.add(esResponse.getMsg());
		
		achieveGoalES.cleanUp();

		if (finalResult.size()>0)
			logger.debug("THE FINAL RESULT:\n" + finalResult.get(0));
		else
			logger.debug("THE FINAL RESULT: EMPTY");
		
		tm.finish();
		return finalResult;
	}
	
	public List<String> runDataMediationAndChoreography(String goalIRIStr, String goalOntoIRIStr, String wsIRIStr) throws UnsupportedOperationException, ComponentException {
		TimeMeasure tm = new TimeMeasure();
		
		List<String> finalResult =  new ArrayList<String>();
		Goal goal = null;
		Ontology goalOnto = null;
		WebService ws = null;
		
		if (goalIRIStr!= null && !goalIRIStr.equals(""))
			goal = resourceManager.retrieveGoal(wsmoFactory.createIRI(goalIRIStr));
		
		if (goal != null){
			logger.info("Retrieved Goal: " +goalIRIStr);
		} else if (goalIRIStr== null || goalIRIStr.equals("")){
			logger.error("Goal has not been provided.");
			tm.finish();
			return finalResult;
		} else {
			logger.error("Goal has not been found: " +goalIRIStr);
			tm.finish();
			return finalResult;
		}
		
		if (goalOntoIRIStr!= null && !goalOntoIRIStr.equals(""))
			goalOnto = resourceManager.retrieveOntology(wsmoFactory.createIRI(goalOntoIRIStr));
		
		if (goalOnto != null){
			logger.info("Retrieved Goal Ontology: " + goalIRIStr);
		} else if (goalOntoIRIStr== null || goalOntoIRIStr.equals("")){
			logger.error("Goal Ontology has not been provided.");
			tm.finish();
			return finalResult;
		} else {
			logger.error("Goal Ontology has not been found: " +goalIRIStr);
			tm.finish();
			return finalResult;
		}

		if (wsIRIStr!= null && ! wsIRIStr.equals(""))
			ws = resourceManager.retrieveWebService(wsmoFactory.createIRI(wsIRIStr));
		
		if (ws != null){
			logger.info("Retrieved Web service: " + wsIRIStr);
		} else if (wsIRIStr== null || wsIRIStr.equals("")){
			logger.error("Web Service has not been provided.");
			tm.finish();
			return finalResult;
		} else {
			logger.error("Web Service has not been found: " +wsIRIStr);
			tm.finish();
			return finalResult;
		}

		//initialize Execution Semantics
		AchieveGoalChor achieveGoalES = new AchieveGoalChor();
		achieveGoalES.wsmxInvoker = invoker;
		
		achieveGoalES.goal = goal;
		achieveGoalES.goalOnto = goalOnto;
		achieveGoalES.requiredDiscovery = DiscoveryType.getRequiredDiscoveryType(goal);
		achieveGoalES.discoveredWebService = ws;
		achieveGoalES.dataMediationDirection = AchieveGoalChor.DataMediationDirection.GOAL_TO_WEBSERVICE;
		
		State esState = achieveGoalES.new PerformDataMediation(null);
		
		ExecutionSemanticsFinalResponse esResponse = runExecutionSemantics(esState, achieveGoalES);

		if (esResponse.getReceivedMessages()!= null && esResponse.getReceivedMessages().size() != 0)
			//return non-empty list if some data was received
			finalResult.add(esResponse.getMsg());
		
		achieveGoalES.cleanUp();

		if (finalResult.size()>0)
			logger.debug("THE FINAL RESULT:\n" + finalResult.get(0));
		else
			logger.debug("THE FINAL RESULT: EMPTY");
		
		tm.finish();
		
		return finalResult;
	}
	
	public List<String> runDiscovery(String goalIRIStr, String goalOntoIRIStr) throws UnsupportedOperationException, ComponentException {
		TimeMeasure tm = new TimeMeasure();
		List<String> finalResult =  new ArrayList<String>();
		
		Goal goal = null;
		Ontology goalOnto = null;
		
		if (goalIRIStr!= null && !goalIRIStr.equals(""))
			goal = resourceManager.retrieveGoal(wsmoFactory.createIRI(goalIRIStr));
		
		if (goal != null){
			logger.info("Retrieved Goal: " +goalIRIStr);			
		} else {
			logger.error("Goal has not been found: " +goalIRIStr);
			tm.finish();
			return finalResult;
		}
		
		if (goalOntoIRIStr!= null && !goalOntoIRIStr.equals(""))
			goalOnto = resourceManager.retrieveOntology(wsmoFactory.createIRI(goalOntoIRIStr));
		
		if (goalOnto != null){
			logger.info("Retrieved Goal Ontology: " + goalIRIStr);
		} else if (goalOntoIRIStr== null || goalOntoIRIStr.equals("")){
			logger.error("Goal Ontology has not been provided.");
			tm.finish();
			return finalResult;
		} else {
			logger.error("Goal Ontology has not been found: " +goalIRIStr);
			tm.finish();
			return finalResult;
		}
				
		//initialize Execution Semantics
		AchieveGoalChor achieveGoalES = new AchieveGoalChor();
		achieveGoalES.wsmxInvoker = invoker;
		
		achieveGoalES.goal = goal;
		//discovery only
		achieveGoalES.goalOnto = null;
		achieveGoalES.requiredDiscovery = DiscoveryType.getRequiredDiscoveryType(goal);
		State esState = achieveGoalES.new GetWebServiceFromRM(null);
		
		ExecutionSemanticsFinalResponse esResponse = runExecutionSemantics(esState, achieveGoalES);

		if (esResponse.getWebservices()!= null && esResponse.getWebservices().size() > 0){
			for (WebService ws : esResponse.getWebservices()){
				finalResult.add(ws.getIdentifier().toString());	
			}
			
		}
		
		achieveGoalES.cleanUp();

		if (finalResult.size()>0)
			logger.debug("THE FINAL RESULT:\n" + esResponse.getMsg());
		else
			logger.debug("THE FINAL RESULT: EMPTY");
		
		tm.finish();
		return finalResult;
	}

	private ExecutionSemanticsFinalResponse runExecutionSemantics(State esState, AchieveGoalChor es) {
		ExecutionSemanticsFinalResponse esResponse = null;
		try {
			boolean finished = false;
			while (!finished){
				if (esState.getAssociatedEvent().equals(Event.RESOURCEMANAGER))
					//retrieve Web services from Repository
					esState = esState.handleState(resourceManager);
				else if (esState.getAssociatedEvent().equals(Event.WEBSERVICEDISCOVERY)) 
					//perform Web service Discovery
					esState = esState.handleState(webServiceDiscovery);
				else if (esState.getAssociatedEvent().equals(Event.SERVICEDISCOVERY)) 
					//perform Service Discovery
					esState = esState.handleState(serviceDiscovery);
				else if (esState.getAssociatedEvent().equals(Event.DATAMEDIATOR)) 
					//perform Data Mediation
					esState = esState.handleState(dataMediator);
				else if (esState.getAssociatedEvent().equals(Event.CHOREOGRAPHY))
					//run choreography
					esState = esState.handleState(cEngine);
				else if (esState.getAssociatedEvent().equals(Event.ENTRYPOINT)){
					esResponse = (ExecutionSemanticsFinalResponse) ((AchieveGoalChor.EntryPoint) esState).getData(); 
					finished = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
		return esResponse;
	}
	
	private class TimeMeasure {
		long dt;
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");;
		
		public TimeMeasure() {
			super();
			Calendar calendar = Calendar.getInstance();
			logger.info("STARTED AT " + sdf.format(calendar.getTime()));
			dt = System.currentTimeMillis();
		}

		public void finish(){
			dt = System.currentTimeMillis() - dt;
		    logger.info("TIME TAKEN: "+dt+" ms.");
		
			Calendar calendar = Calendar.getInstance();
			logger.info("FINISHED AT " + sdf.format(calendar.getTime()));
		}		
	}
	
 }
