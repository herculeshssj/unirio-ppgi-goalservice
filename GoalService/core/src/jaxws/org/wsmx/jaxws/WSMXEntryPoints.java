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

package org.wsmx.jaxws;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.executionsemantic.ExecutionSemanticsFinalResponse;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.io.parser.wsml.ParserImpl;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.mediator.Mediator;
import org.wsmo.service.Goal;
import org.wsmo.wsml.Parser;

/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 1 Apr 2008
 * Committed by $Author$
 * * $Source$, * @version $Revision$ $Date$
 */

@WebService(name="WSMXEntryPointsPortType",serviceName="WSMXEntryPoints", targetNamespace = "http://www.wsmx.org/jaxws/entrypoint/")
@SOAPBinding(style = Style.RPC, use = Use.LITERAL)
public class WSMXEntryPoints {

	protected static Logger logger = Logger.getLogger(WSMXEntryPoints.class);
	
	static WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap<String, Object>());
	static Parser wsmlParser = new ParserImpl(new HashMap<String, Object>());
		
	public String getRMIString(){
		String strRMI = (String) Helper.getAttribute("strRMI");
		return strRMI;
	}
	
	@WebMethod
	public String achieveGoal(String wsmlMessageGoal)
	{
		try {
			Object response = invokeComponent("CommunicationManager", "achieveGoalFullResponse", 
					new Object[]{wsmlMessageGoal}, new String[]{"java.lang.String"});

			ExecutionSemanticsFinalResponse esResponse = (ExecutionSemanticsFinalResponse) response; 

			//clean up after communication
			if (esResponse!=null && esResponse.getExecutionSemantic() != null)
				esResponse.getExecutionSemantic().cleanUp();
			
			return esResponse.getMsg();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}
	
	@WebMethod
	public String[] discoverWebServices (String goalMessage){
		
		String[] responseStr = {};

		try {
			Object response = invokeComponent("CommunicationManager", "discoverWebServices", 
					new Object[]{goalMessage}, new String[]{"java.lang.String"});
			responseStr = (String[]) response; 

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return responseStr;
	}
	
	@WebMethod
	public String invokeWebService(
			@WebParam(name="webServiceAndMessageAsString") StringPair webServiceAndMessageAsString)
	{
		try {
			Object response = invokeComponent("CommunicationManager", "invokeWebService", 
					new Object[]{webServiceAndMessageAsString.getFirst(), webServiceAndMessageAsString.getSecond()}, 
					new String[]{"java.lang.String","java.lang.String"});
			String esResponse = (String) response; 

			return esResponse;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}
	
	@WebMethod
	public String registerRequesterChoreography(String wsmlGoal)
	{
		try {
			Object response = invokeComponent("RadexChoreography", "registerRequesterChoreography", 
					new Object[]{wsmlGoal}, new String[]{"java.lang.String"});
			String esResponse = (String) response; 

			return esResponse;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}
	
	@WebMethod
	public String registerProviderChoreography(String wsmlWebService)
	{
		try {
			Object response = invokeComponent("RadexChoreography", "registerProviderChoreography", 
					new Object[]{wsmlWebService}, new String[]{"java.lang.String"});
			String esResponse = (String) response; 

			return esResponse;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}
	
	@WebMethod
	public String updateState(@WebParam(name="directionAndWsmlMessage") StringPair directionAndWsmlMessage)
	{
		try {
			Object response = invokeComponent("RadexChoreography", "updateState", 
					new Object[]{directionAndWsmlMessage.getFirst(), directionAndWsmlMessage.getSecond()}, 
					new String[]{"java.lang.String","java.lang.String"});
			String esResponse = (String) response; 

			return esResponse;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}

	@WebMethod
	public String store(String wsmlMessage)
	{
    	try {
    		
    		logger.fatal("Store received:" + wsmlMessage);
    		
			TopEntity[] topEntity = Helper.parse(new StringReader(wsmlMessage));
			
			Set<Ontology> ontos = Helper.getOntologySet(topEntity);
			Set<Goal> goals = Helper.getGoalSet(topEntity);
			Set<org.wsmo.service.WebService> webservices = Helper.getWebServiceSet(topEntity);
			
			for (Ontology o : ontos)
				invokeComponent("ResourceManager", "storeOntology", 
						  new Object[]{o}, new String[]{"org.omwg.ontology.Ontology"});
			for (Goal g : goals)
				invokeComponent("ResourceManager", "storeGoal", 
						new Object[]{g}, new String[]{"org.wsmo.service.Goal"});
			for (org.wsmo.service.WebService ws : webservices)
				invokeComponent("ResourceManager", "storeWebService", 
						new Object[]{ws}, new String[]{"org.wsmo.service.WebService"});
			logger.fatal("Store has been carried out successfully.");
			return "Store has been carried out successfully.";
		} catch (Exception e) {
			logger.fatal("Store error");
			return "Store error";
		}
		
//		try {
//			Object response = invokeComponent("CommunicationManager", "store", 
//					new Object[]{wsmlMessage}, new String[]{"java.lang.String"});
//			return ((ExecutionSemanticsFinalResponse)response).getMsg();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "Error";
//		}
	}

	@WebMethod
    public void storeOntology(String wsmlOntology){
    	try {
			TopEntity[] topEntity = wsmlParser.parse(new StringReader(wsmlOntology));
	    	Object response = invokeComponent("ResourceManager", "storeOntology", 
	    									  new Object[]{Helper.getOntologies(topEntity).get(0)}, new String[]{"org.omwg.ontology.Ontology"});			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	@WebMethod
    public void storeWebService(String wsmlWebService){
    	try {
			TopEntity[] topEntity = wsmlParser.parse(new StringReader(wsmlWebService));
	    	Object response = invokeComponent("ResourceManager", "storeWebService", 
	    									  new Object[]{Helper.getWebServices(topEntity).get(0)}, new String[]{"org.wsmo.service.WebService"});			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	@WebMethod
    public void storeGoal(String wsmlGoal){
    	try {
			TopEntity[] topEntity = wsmlParser.parse(new StringReader(wsmlGoal));
	    	Object response = invokeComponent("ResourceManager", "storeGoal", 
	    									  new Object[]{Helper.getGoals(topEntity).get(0)}, new String[]{"org.wsmo.service.Goal"});			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	@WebMethod
    public void storeMediator(String wsmlMediator){
    	try {
			TopEntity[] topEntity = wsmlParser.parse(new StringReader(wsmlMediator));
	    	Object response = invokeComponent("ResourceManager", "storeMediator", 
	    									  new Object[]{Helper.getGoals(topEntity).get(0)}, new String[]{"org.wsmo.mediator.Mediator"});			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	@WebMethod
	public void storeMapping(String mapdocAsString){
		try {	
			Object response = invokeComponent("ResourceManager", "storeMapping", 
											  new Object[]{mapdocAsString}, new String[]{"java.lang.String"});//"org.omwg.mediation.language.objectmodel.api.MappingDocument"});
		} catch (Exception e) {	
			e.printStackTrace();
		}		
	}
    
	@WebMethod
    public void removeOntology(String wsmlOntology){
    	try {
			TopEntity[] topEntity = wsmlParser.parse(new StringReader(wsmlOntology));
	    	Object response = invokeComponent("ResourceManager", "removeOntology", 
	    									  new Object[]{Helper.getOntologies(topEntity).get(0)}, new String[]{"org.omwg.ontology.Ontology"});			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	@WebMethod
    public void removeWebService(String wsmlWebService){
    	try {
			TopEntity[] topEntity = wsmlParser.parse(new StringReader(wsmlWebService));
	    	Object response = invokeComponent("ResourceManager", "removeWebService", 
	    									  new Object[]{Helper.getWebServices(topEntity).get(0)}, new String[]{"org.wsmo.service.WebService"});			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	@WebMethod
    public void removeGoal(String wsmlGoal){
    	try {
			TopEntity[] topEntity = wsmlParser.parse(new StringReader(wsmlGoal));
	    	Object response = invokeComponent("ResourceManager", "removeGoal", 
	    									  new Object[]{Helper.getOntologies(topEntity).get(0)}, new String[]{"org.wsmo.service.Goal"});			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	@WebMethod
    public void removeMediator(String wsmlMediator){
    	try {
			TopEntity[] topEntity = wsmlParser.parse(new StringReader(wsmlMediator));
	    	Object response = invokeComponent("ResourceManager", "removeMediator", 
	    									  new Object[]{Helper.getOntologies(topEntity).get(0)}, new String[]{"org.wsmo.mediator.Mediator"});			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	@WebMethod
	public void removeMapping(String mapdocIriAsString) {
		try {
	    	Object response = invokeComponent("ResourceManager", "removeMapping", 
					  new Object[]{mapdocIriAsString}, new String[]{"java.lang.String"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @WebMethod
    public String[] retrieveOntologies(){
    	try {
	    	Object response = invokeComponent("ResourceManager", "retrieveOntologies", new Object[]{}, new String[]{});
	    	return Helper.serializeTopEntities((Set<Ontology>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
    @WebMethod
    public String[] retrieveWebServices(){
    	try {
	    	Object response = invokeComponent("ResourceManager", "retrieveWebServices", new Object[]{}, new String[]{});
	    	return Helper.serializeTopEntities((Set<WebService>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
    @WebMethod
    public String[] retrieveWebServicesReferringOntology(String ontoIdentifer) {
    	try {
	    	Object response = invokeComponent("ResourceManager", "retrieveWebServicesReferringOntology", 
	    			new Object[]{(Identifier)wsmoFactory.createIRI(ontoIdentifer)}, new String[]{"org.wsmo.common.Identifier"});
	    	return Helper.serializeTopEntities((Set<WebService>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }

    @WebMethod
    public String[] retrieveGoals(){
    	try {
	    	Object response = invokeComponent("ResourceManager", "retrieveGoals", new Object[]{}, new String[]{});
	    	return Helper.serializeTopEntities((Set<Goal>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] retrieveMediator(){
    	try {
	    	Object response = invokeComponent("ResourceManager", "retrieveMediator", new Object[]{}, new String[]{});
	    	return Helper.serializeTopEntities((Set<Mediator>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
	@WebMethod
	public String retrieveMapping(String mapdocIriAsString) {
		try {			
			Object response = invokeComponent("ResourceManager", "retrieveMapping", 
					  new Object[]{mapdocIriAsString}, new String[]{"java.lang.String"});
			return (String) response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
		
    @WebMethod
	public String retrieveMappingByOntologyIDs(
			@WebParam(name="sourceAndTargetOntoIRIAsString") StringPair sourceAndTargetOntoIRIAsString) {
		try {			
			Object response = invokeComponent("ResourceManager", "retrieveMapping", 
					  new Object[]{sourceAndTargetOntoIRIAsString.getFirst(), sourceAndTargetOntoIRIAsString.getSecond()}, 
					  new String[]{"java.lang.String", "java.lang.String"});				
			return (String) response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
    @WebMethod
    public String[] retrieveOntologiesByNamespace(String namespace){
    	try {
    		Namespace n = wsmoFactory.createNamespace("_",wsmoFactory.createIRI(namespace));
	    	Object response = invokeComponent("ResourceManager", "retrieveOntologies", 
	    									  new Object[]{n}, new String[]{"org.wsmo.common.Namespace"});
	    	return Helper.serializeTopEntities((Set<Ontology>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
    @WebMethod
    public String[] retrieveWebServicesByNamespace(String namespace){
    	try {
    		Namespace n = wsmoFactory.createNamespace("_",wsmoFactory.createIRI(namespace));
	    	Object response = invokeComponent("ResourceManager", "retrieveWebServices", 
	    									  new Object[]{n}, new String[]{"org.wsmo.common.Namespace"});
	    	return Helper.serializeTopEntities((Set<WebService>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] retrieveGoalByNamespace(String namespace){
    	try {
    		Namespace n = wsmoFactory.createNamespace("_",wsmoFactory.createIRI(namespace));
	    	Object response = invokeComponent("ResourceManager", "retrieveGoals", 
	    									  new Object[]{n}, new String[]{"org.wsmo.common.Namespace"});
	    	return Helper.serializeTopEntities((Set<Goal>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;    	
    }

    @WebMethod
    public String[] retrieveMediatorByNamespace(String namespace){
    	try {
    		Namespace n = wsmoFactory.createNamespace("_",wsmoFactory.createIRI(namespace));
	    	Object response = invokeComponent("ResourceManager", "retrieveMediator", 
	    									  new Object[]{n}, new String[]{"org.wsmo.common.Namespace"});
	    	return Helper.serializeTopEntities((Set<Mediator>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
    @WebMethod
    public String retrieveOntologyByIdentifier(String identifier){
    	try {
	    	Object response = invokeComponent("ResourceManager", "retrieveOntology", 
	    									  new Object[]{(Identifier)wsmoFactory.createIRI(identifier)}, new String[]{"org.wsmo.common.Identifier"});
	    	return Helper.serializeTopEntity((TopEntity)response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
    @WebMethod
    public String retrieveWebServiceByIdentifier(String identifier){
    	try {
	    	Object response = invokeComponent("ResourceManager", "retrieveWebService", 
	    									  new Object[]{(Identifier)wsmoFactory.createIRI(identifier)}, new String[]{"org.wsmo.common.Identifier"});
	    	return Helper.serializeTopEntity((TopEntity)response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
    @WebMethod
    public String retrieveGoalByIdentifier(String identifier)  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "retrieveGoal", 
	    									  new Object[]{(Identifier)wsmoFactory.createIRI(identifier)}, new String[]{"org.wsmo.common.Identifier"});
	    	return Helper.serializeTopEntity((TopEntity)response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String retrieveMediatorByIdentifier(String identifier)  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "retrieveMediator", 
	    									  new Object[]{(Identifier)wsmoFactory.createIRI(identifier)}, new String[]{"org.wsmo.common.Identifier"});
	    	return Helper.serializeTopEntity((TopEntity)response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	  	
    }

    @WebMethod
    public String[] getOntologyNamespaces()  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "getOntologyNamespaces", new Object[]{}, new String[]{});
	    	return Helper.serializeNamespaces((Set<Namespace>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
    @WebMethod
    public String[] getWebServiceNamespaces()  { //Namespace[]
    	try {
	    	Object response = invokeComponent("ResourceManager", "getWebServiceNamespaces", new Object[]{}, new String[]{});
	    	return Helper.serializeNamespaces((Set<Namespace>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] getGoalNamespaces()  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "getGoalNamespaces", new Object[]{}, new String[]{});
	    	return Helper.serializeNamespaces((Set<Namespace>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] getMediatorNamespaces()  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "getMediatorNamespaces", new Object[]{}, new String[]{});
	    	return Helper.serializeNamespaces((Set<Namespace>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] getOntologyIdentifiers()  { //Identifier[]
    	try {
	    	Object response = invokeComponent("ResourceManager", "getOntologyIdentifiers", new Object[]{}, new String[]{});
	    	return Helper.serializeIdentifiers((Set<Identifier>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] getWebServiceIdentifiers()  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "getWebServiceIdentifiers", new Object[]{}, new String[]{});
	    	return Helper.serializeIdentifiers((Set<Identifier>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] getGoalIdentifiers()  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "getGoalIdentifiers", new Object[]{}, new String[]{});
	    	return Helper.serializeIdentifiers((Set<Identifier>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] getMediatorIdentifiers()  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "getMediatorIdentifiers", new Object[]{}, new String[]{});
	    	return Helper.serializeIdentifiers((Set<Identifier>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
  
    @WebMethod
    public String[] getOntologyIdentifiersByNamespace(String namespace)  { //Identifier[]
    	try {
    		Namespace n = wsmoFactory.createNamespace("_",wsmoFactory.createIRI(namespace));
    		Object response = invokeComponent("ResourceManager", "getOntologyIdentifiers", 
    										  new Object[]{n}, new String[]{"org.wsmo.common.Namespace"});
    		return Helper.serializeIdentifiers((Set<Identifier>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
    @WebMethod
    public String[] getWebServiceIdentifiersByNamespace(String namespace)  {
    	try {
    		Namespace n = wsmoFactory.createNamespace("_",wsmoFactory.createIRI(namespace));
    		Object response = invokeComponent("ResourceManager", "getWebServiceIdentifiers", 
    										  new Object[]{n}, new String[]{"org.wsmo.common.Namespace"});
    		return Helper.serializeIdentifiers((Set<Identifier>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] getGoalIdentifiersByNamespace(String namespace)  {
    	try {
    		Namespace n = wsmoFactory.createNamespace("_",wsmoFactory.createIRI(namespace));
    		Object response = invokeComponent("ResourceManager", "getGoalIdentifiers", 
    										  new Object[]{n}, new String[]{"org.wsmo.common.Namespace"});
    		return Helper.serializeIdentifiers((Set<Identifier>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public String[] getMediatorIdentifiersByNamespace(String namespace)  {
    	try {
    		Namespace n = wsmoFactory.createNamespace("_",wsmoFactory.createIRI(namespace));
    		Object response = invokeComponent("ResourceManager", "getMediatorIdentifiers", 
    										  new Object[]{n}, new String[]{"org.wsmo.common.Namespace"});
    		return Helper.serializeIdentifiers((Set<Identifier>) response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;    	
    }

    @WebMethod
    public boolean containsOntologyByIdentifier(String identifier)  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "containsOntology", 
	    						new Object[]{(Identifier)wsmoFactory.createIRI(identifier)}, new String[]{"org.wsmo.common.Identifier"});
	    	return ((Boolean) response).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;    	  	
    }

    @WebMethod
    public boolean containsWebServiceByIdentifier(String identifier)  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "containsWebService", 
	    						new Object[]{(Identifier)wsmoFactory.createIRI(identifier)}, new String[]{"org.wsmo.common.Identifier"});
	    	return ((Boolean) response).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;  	
    }

    @WebMethod
    public boolean containsGoalByIdentifier(String identifier)  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "containsGoal", 
	    						new Object[]{(Identifier)wsmoFactory.createIRI(identifier)}, new String[]{"org.wsmo.common.Identifier"});
	    	return ((Boolean) response).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;     	
    }
    
    @WebMethod
    public boolean containsMediatorByIdentifier(String identifier)  {
    	try {
	    	Object response = invokeComponent("ResourceManager", "containsMediator", 
	    						new Object[]{(Identifier)wsmoFactory.createIRI(identifier)}, new String[]{"org.wsmo.common.Identifier"});
	    	return ((Boolean) response).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;    	
    }
    
    private Object invokeComponent(String componentName, String operationName, Object[] params, String[] paramsDataTypes){
		//get a reference to MBeanService
		MBeanServer mBeanServer = (MBeanServer) Helper.getAttribute("MBeanServer");
			
		ObjectName componentObjectName=null;
		try {
			componentObjectName = new ObjectName("components:name="+componentName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//check if mBean has been registered
		if (!mBeanServer.isRegistered(componentObjectName)) 
			return null;
		
		if (logger.isDebugEnabled ())
			logger.debug("Invoking MBeanServer object=" + componentName 
					+ ", operation: " + operationName 
					+ ", params=" + Arrays.toString(params) 
					+ ", paramsDataTypes=" + Arrays.toString(paramsDataTypes));
		
		Object response;
		try {			
			response = mBeanServer.invoke(componentObjectName, operationName, params, paramsDataTypes);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
		return response;
    }
    
//	public static void main(String[] args) {
//		WSMXEntryPoint wsmxEntryPoint = new WSMXEntryPoint();
//		try {
//			wsmxEntryPoint.invokeComponent(null,null,null, null);
//		} catch (UnsupportedOperationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ComponentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	
//	public void store(String wsmlMessage){
//
//		ServletContext sc = ((HttpServlet) MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLET)).getServletContext();
//		MBeanServer mBeanServer = (MBeanServer) sc.getAttribute("MBeanServer");
//		//component is availible as an MBean
//		
//		MBeanInfo info = mBeanServer.getMBeanInfo(commManagerName);
//    MBeanOperationInfo[] operations = info.getOperations();
//    boolean match = false;
//    if (operations != null) {
//            for (int j = 0; j < operations.length; j++) {
//            	response += operations[j].getName() +"-params:-";
//            	MBeanParameterInfo[] parameters = operations[j].getSignature();
//                
//            	for (int p = 0; p < parameters.length; p++) {
//            		response += parameters[p].getType();
//                }
//            	response += "##"; 
//            }
//    }
} 