/*
 * Copyright (c) 20058 National University of Ireland, Galway
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

package ie.deri.wsmx.client;

import ie.deri.wsmx.client.utility.DynamicBinder;
import ie.deri.wsmx.client.utility.DynamicInvoker;
import ie.deri.wsmx.commons.Helper;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.deri.wsmo4j.io.parser.wsml.ParserImpl;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.execution.common.component.resourcemanager.GoalResourceManager;
import org.wsmo.execution.common.component.resourcemanager.MediatorResourceManager;
import org.wsmo.execution.common.component.resourcemanager.OntologyResourceManager;
import org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.mediator.Mediator;
import org.wsmo.mediator.OOMediator;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.Serializer;
import org.xml.sax.InputSource;

/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 23-Jun-2006
 * Committed by $Author: maciejzaremba $
 */

public class WSMXWebServiceClient implements OntologyResourceManager, WebServiceResourceManager, GoalResourceManager, MediatorResourceManager{

	static WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap<String, Object>());
	static Serializer wsmlSerializer = Factory.createSerializer(new HashMap<String, Object>());
	static Parser wsmlParser = new ParserImpl(new HashMap<String, Object>());
    private HashMap<String,DynamicBinder> listOfDynamicBinders; 
	private static Logger logger = Logger.getLogger(WSMXWebServiceClient.class);
	
	static {
		logger.setLevel(Level.ALL);
	}

	//need to provide reference for WSDL
	String wsdlLocation;
	
	public WSMXWebServiceClient(String wsdlLocation) {
		super();
		//setup required variables
		this.listOfDynamicBinders = new  HashMap<String,DynamicBinder>();
		this.wsdlLocation = wsdlLocation;
	}
	
	//WSDL4j caching method, each instance of Comm. Manager maintains a pool of analyzed WSDLs ide7ntifiable by URI   
	private DynamicBinder getDynamicBinder(String wsdlURIStr){
		if (listOfDynamicBinders.containsKey(wsdlURIStr)){
			return listOfDynamicBinders. get(wsdlURIStr);
		}
		//no dynamic binding to WSDL URI exists
		DynamicBinder dBinder = null;
		try {
			dBinder = new DynamicBinder(wsdlURIStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		listOfDynamicBinders.put(wsdlURIStr,dBinder);
		return dBinder;
	}

	private List<String> invokeWebService(String portType, String operation, String[] input){
		try {
			logger.debug("porttype: "+portType+" operation: " + operation);
			String xmlStr = "";
			DynamicBinder dBinder = getDynamicBinder(wsdlLocation);
			dBinder.readDetailsFromWSDL(portType, operation);

			xmlStr += "<tns1:"+dBinder.operation +" xmlns:tns1=\""+dBinder.targetNS+"\">";
			if (input!=null || input.length==0)
			{
				for (int i=0; i < input.length; i++) {
					xmlStr += "<tns1:param"+i+">";
					xmlStr += 	input[i];
					xmlStr += "</tns1:param"+i+">";
				}
			}
			xmlStr += "</tns1:"+dBinder.operation +">";

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(true);
			DocumentBuilder builder = docFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlStr));
			org.w3c.dom.Document doc = builder.parse(is);
			
			DynamicInvoker dInvoker = new DynamicInvoker();
			Document respDoc = dInvoker.invokeMethod(dBinder,doc);
			
			SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
			nsContext.addNamespace("tns1", "http://webservices.deri.ie");

			XPath xpath = new DOMXPath("//tns1:return");
			xpath.setNamespaceContext(nsContext);
			
			List<Node> nodes = xpath.selectNodes(respDoc);
			List<String> list = new ArrayList<String>();
			for (Node n: nodes){
				list.add(n.getTextContent());
			}

			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
		
	private Set<TopEntity> getTopEntitySet(List<String> input){
		Set<TopEntity> response = new HashSet<TopEntity>();

		for (String s : input){
			//parse it as WSML
			try {
				TopEntity[] topEntity = wsmlParser.parse(new StringReader(s));
				for (int a=0; a<topEntity.length; a++)
					response.add(topEntity[a]);
			} catch (Exception e) {
				logger.error("Exception occured during parsing response WSML.");
			}
		}
		return response;
	}
	
	private boolean toBoolean(List<String> input){
		return (input.get(0).equals("true")) ? true : false;
	}

	private Set<Ontology> toOntologySet(Set<TopEntity> topEntities){
		Set<Ontology> response = new HashSet<Ontology>();
		for (TopEntity topEntity : topEntities)
			if (topEntity instanceof Ontology)
				response.add((Ontology)topEntity);
		return response;
	}
	private Set<WebService> toWebServiceSet(Set<TopEntity> topEntities){
		Set<WebService> response = new HashSet<WebService>();
		for (TopEntity topEntity : topEntities)
			if (topEntity instanceof WebService) 
				response.add((WebService)topEntity);
		return response;
	}
	private Set<Goal> toGoalSet(Set<TopEntity> topEntities){
		Set<Goal> response = new HashSet<Goal>();
		for (TopEntity topEntity : topEntities)
			if (topEntity instanceof Goal) 
				response.add((Goal)topEntity);
		return response;
	}
	private Set<Mediator> toMediatorSet(Set<TopEntity> topEntities){
		Set<Mediator> response = new HashSet<Mediator>();
		for (TopEntity topEntity : topEntities)
			if (topEntity instanceof Mediator)
				response.add((Mediator)topEntity);
		return response;
	}
	
	private Set<Namespace> toNamespaceSet(String[] namespaces) {
		Set<Namespace> response = new HashSet<Namespace>();
		for (int i=0; i<namespaces.length; i++)
			response.add(wsmoFactory.createNamespace("_",wsmoFactory.createIRI(namespaces[i])));
		return response;
	}

	private Set<Identifier> toIdentifierSet(String[] identifiers) {
		Set<Identifier> response = new HashSet<Identifier>();
		for (int i=0; i<identifiers.length; i++)
			response.add(wsmoFactory.createIRI(identifiers[i]));
		return response;
	}
	
	public String achieveGoal(String wsmlMessageGoal) {
		List<String> response = invokeWebService("WSMXEntryPoints","achieveGoal",new String[]{wsmlMessageGoal});
		return response.get(0);
	}
	
	public String achieveGoal(String wsmlGoal, String wsmlOntology) {
		
		//merge Goal and Ontology
		//FIXME reliance on "ontology" string
		String wsmlMessageGoal = wsmlGoal + "\n" + wsmlOntology.substring(wsmlOntology.indexOf("ontology")-1); 
		
		List<String> response = invokeWebService("WSMXEntryPoints","achieveGoal",new String[]{wsmlMessageGoal});
		return response.get(0);
	}
	
	public Set<Instance> achieveGoal(Goal g, Set <Instance> instances) {
		Ontology tempOnto = wsmoFactory.createOntology(wsmoFactory.createIRI("http://www.wsmo.org/examples/temp"+Helper.getRandomLong()));
		try {
			for (Instance i : instances)
				tempOnto.addInstance(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String wsmlGoal = Helper.serializeTopEntity(g);
		String wsmlOntology = Helper.serializeTopEntity(tempOnto);
		
		//merge Goal and Ontology
		//FIXME reliance on "ontology" string
		String wsmlMessageGoal = wsmlGoal + "\n" + wsmlOntology.substring(wsmlOntology.indexOf("ontology")-1); 
		
		System.out.println("Sending out to WSMX:"+wsmlMessageGoal);
		
		List<String> response = invokeWebService("WSMXEntryPoints","achieveGoal",new String[]{wsmlMessageGoal});
		Ontology respOnto = (Ontology) Helper.parse(new StringReader(response.get(0)))[0];
		
		System.out.println("Response from WSMX: "+Helper.serializeTopEntity(respOnto));
		
		return new HashSet(Helper.getInstances(respOnto));
	}
	

	public Set<WebService> discoverWebServices(Goal goal) {
		StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)goal}, sb);
		String goalStr = sb.toString();		
		List<String> response = invokeWebService("WSMXEntryPoints","discoverWebServices",new String[]{goalStr});
		return toWebServiceSet(getTopEntitySet(response));
	}

	public String store(String wsmlMessage) {
		List<String> response = invokeWebService("WSMXEntryPoints","store",new String[]{wsmlMessage});
		return response.get(0);
	}
	
	public void storeOntology(Ontology ontology) throws ComponentException, UnsupportedOperationException {
		StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)ontology}, sb);
		List<String> response = invokeWebService("WSMXEntryPoints","storeOntology",new String[]{sb.toString()});
	}
	public void storeWebService(WebService webservice) throws ComponentException, UnsupportedOperationException {
		StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)webservice}, sb);
		List<String> response = invokeWebService("WSMXEntryPoints","storeWebService",new String[]{sb.toString()});
	}
	public void storeGoal(Goal goal) throws ComponentException, UnsupportedOperationException {
		StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)goal}, sb);
		List<String> response = invokeWebService("WSMXEntryPoints","storeGoal",new String[]{sb.toString()});
	}
	public void storeMediator(Mediator mediator) throws ComponentException, UnsupportedOperationException {
		StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)mediator}, sb);
		List<String> response = invokeWebService("WSMXEntryPoints","storeMediator",new String[]{sb.toString()});
	}

	public void removeOntology(Ontology ontology) throws ComponentException, UnsupportedOperationException {
		StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)ontology}, sb);
		List<String> response = invokeWebService("WSMXEntryPoints","removeOntology",new String[]{sb.toString()});
	}
	public void removeWebService(WebService webservice) throws ComponentException, UnsupportedOperationException {
		StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)webservice}, sb);
		List<String> response = invokeWebService("WSMXEntryPoints","removeWebService",new String[]{sb.toString()});
	}
	public void removeGoal(Goal goal) throws ComponentException, UnsupportedOperationException {
		StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)goal}, sb);
		List<String> response = invokeWebService("WSMXEntryPoints","removeGoal",new String[]{sb.toString()});
	}
	public void removeMediator(Mediator mediator) throws ComponentException, UnsupportedOperationException {
		StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)mediator}, sb);
		List<String> response = invokeWebService("WSMXEntryPoints","removeMediator",new String[]{sb.toString()});
	}
	
	public Set<Ontology> retrieveOntologies() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveOntologies",new String[]{});
		return toOntologySet(getTopEntitySet(response));
	}
	public Set<WebService> retrieveWebServices() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveWebServices",new String[]{});
		return toWebServiceSet(getTopEntitySet(response));
	}
	public Set<Goal> retrieveGoals() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveGoals",new String[]{});
		return toGoalSet(getTopEntitySet(response));
	}
	public Set<Mediator> retrieveMediators() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveOntologies",new String[]{});
		return toMediatorSet(getTopEntitySet(response));
	}
	
	public Set<Ontology> retrieveOntologies(Namespace namespace) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveOntologiesByNamespace",new String[]{namespace.getIRI().toString()});
		return toOntologySet(getTopEntitySet(response));
	}
	public Set<WebService> retrieveWebServices(Namespace namespace) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveWebServicesByNamespace",new String[]{namespace.getIRI().toString()});
		return toWebServiceSet(getTopEntitySet(response));
	}
	public Set<Goal> retrieveGoals(Namespace namespace) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveGoalsByNamespace",new String[]{namespace.getIRI().toString()});
		return toGoalSet(getTopEntitySet(response));
	}
	public Set<Mediator> retrieveMediators(Namespace namespace) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveMediatorsByNamespace",new String[]{namespace.getIRI().toString()});
		return toMediatorSet(getTopEntitySet(response));
	}

	public Ontology retrieveOntology(Identifier identifier) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveOntologyByIdentifier",new String[]{identifier.toString()});
		return (Ontology)toOntologySet(getTopEntitySet(response)).toArray()[0];
	}
	public WebService retrieveWebService(Identifier identifier) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveWebServiceByIdentifier",new String[]{identifier.toString()});
		return (WebService)toWebServiceSet(getTopEntitySet(response)).toArray()[0];
	}
	public Goal retrieveGoal(Identifier identifier) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveGoalByIdentifier",new String[]{identifier.toString()});
		return (Goal)toGoalSet(getTopEntitySet(response)).toArray()[0];
	}
	public Mediator retrieveMediator(Identifier identifier) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveMediatorIdentifier",new String[]{identifier.toString()});
		return (Mediator)toMediatorSet(getTopEntitySet(response)).toArray()[0];
	}
	
	public Set<Namespace> getOntologyNamespaces() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getOntologyNamespaces",new String[]{});
		return (Set<Namespace>)toNamespaceSet(response.toArray(new String[0]));
	}
	public Set<Namespace> getWebServiceNamespaces() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getWebServiceNamespaces",new String[]{});
		return (Set<Namespace>)toNamespaceSet(response.toArray(new String[0]));
	}
	public Set<Namespace> getGoalNamespaces() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getGoalNamespaces",new String[]{});
		return (Set<Namespace>)toNamespaceSet(response.toArray(new String[0]));
	}
	public Set<Namespace> getMediatorNamespaces() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getMediatorNamespaces",new String[]{});
		return (Set<Namespace>)toNamespaceSet(response.toArray(new String[0]));
	}
	
	public Set<Identifier> getOntologyIdentifiers() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getOntologyIdentifiers",new String[]{});
		return (Set<Identifier>)toIdentifierSet(response.toArray(new String[0]));
	}
	public Set<Identifier> getWebServiceIdentifiers() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getWebServiceIdentifiers",new String[]{});
		return (Set<Identifier>)toIdentifierSet(response.toArray(new String[0]));
	}
	public Set<Identifier> getGoalIdentifiers() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getGoalIdentifiers",new String[]{});
		return (Set<Identifier>)toIdentifierSet(response.toArray(new String[0]));
	}
	public Set<Identifier> getMediatorIdentifiers() throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getMediatorIdentifiers",new String[]{});
		return (Set<Identifier>)toIdentifierSet(response.toArray(new String[0]));
	}
	
	public Set<Identifier> getOntologyIdentifiers(Namespace namespace) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getOntologyIdentifiersByNamespace",new String[]{namespace.getIRI().toString()});
		return (Set<Identifier>)toIdentifierSet(response.toArray(new String[0]));
	}
	public Set<Identifier> getWebServiceIdentifiers(Namespace namespace) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getWebServiceIdentifiersByNamespace",new String[]{namespace.getIRI().toString()});
		return (Set<Identifier>)toIdentifierSet(response.toArray(new String[0]));
	}
	public Set<Identifier> getGoalIdentifiers(Namespace namespace) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getGoalIdentifiersByNamespace",new String[]{namespace.getIRI().toString()});
		return (Set<Identifier>)toIdentifierSet(response.toArray(new String[0]));
	}
	public Set<Identifier> getMediatorIdentifiers(Namespace namespace) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","getMediatorIdentifiersByNamespace",new String[]{namespace.getIRI().toString()});
		return (Set<Identifier>)toIdentifierSet(response.toArray(new String[0]));
	}
	
	public boolean containsOntology(Identifier identifier) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","containsOntologyByIdentifier",new String[]{identifier.toString()});
		return toBoolean(response);
	}
	public boolean containsWebService(Identifier identifier) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","containsWebServiceByIdentifier",new String[]{identifier.toString()});
		return toBoolean(response);
	}
	public boolean containsGoal(Identifier identifier) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","containsGoalByIdentifier",new String[]{identifier.toString()});
		return toBoolean(response);
	}
	public boolean containsMediator(Identifier identifier) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","containsMediatorByIdentifier",new String[]{identifier.toString()});
		return toBoolean(response);
	}

	public Set<WebService> retrieveWebServices(int discoveryType) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveWebServices",new String[]{""+discoveryType});
		return toWebServiceSet(getTopEntitySet(response));
	}

	public Set<WebService> retrieveWebServicesReferringOntology(Identifier ontoIdentifer) throws ComponentException, UnsupportedOperationException {
		List<String> response = invokeWebService("WSMXEntryPoints","retrieveWebServicesReferringOntology",new String[]{ontoIdentifer.toString()});
		return toWebServiceSet(getTopEntitySet(response));
	}

	@Override
	public boolean containsOOMediator(IRI sourceOntologyIRI,
			IRI targetOntologyIRI) throws ComponentException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OOMediator retrieveOOMediator(IRI sourceOntologyIRI,
			IRI targetOntologyIRI) throws ComponentException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}
}
 