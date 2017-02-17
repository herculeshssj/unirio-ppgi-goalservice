package br.uniriotec.aspect.convert;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.ontology.Axiom;
import org.wsmo.common.IRI;
import org.wsmo.common.NFP;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Capability;
import org.wsmo.service.WebService;
import org.wsmo.wsml.ParserException;
import org.wsmo.wsml.Serializer;

public class CreateWsmoFile {
	
	private WsmoFactory factory;
	
	private LogicalExpressionFactory leFactory;
	
	private DataFactory dataFactory;

	private String wsmoSWSName;
	
	private List<String> wsmoSWSParameters = new ArrayList<>();
	
	private int index = 0;
	
	private String[] alphabet = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
	
	public CreateWsmoFile(String swsName, List<String> parameterList, int index) {
		this.wsmoSWSName = swsName;
		this.wsmoSWSParameters = parameterList;
		this.index = index;
	}
	
	public void create() throws IOException {
		WebService service = showExample();
		
		Serializer serializer = Factory.createSerializer(new HashMap<String, Object>(0));
		serializer.serialize(new TopEntity[] {service}, new FileWriter("C:\\CustomServiceWorkspace\\unirio-ppgi-webservices\\SWS-WSML\\WebService_" + index + ".wsml"));
	}

	public WebService showExample() throws InvalidModelException, ParserException {
		this.createFactory();
		return this.createWebservice();
	}
	
	private void createFactory() {
		factory = Factory.createWsmoFactory(null);
		leFactory = Factory.createLogicalExpressionFactory(null);
		dataFactory = Factory.createDataFactory(null);
	}
	
	private WebService createWebservice() throws InvalidModelException, ParserException {
		IRI serviceIRI = factory.createIRI("http://www.uniriotec.br/wsmo/services/" + wsmoSWSName);
		WebService service = factory.createWebService(serviceIRI);
		Namespace targetNamespace = factory.createNamespace("targetnamespace", factory.createIRI("http://www.uniriotec.br/wsmo/services#"));
		  
		// set namespaces
		service.setDefaultNamespace(factory.createNamespace("", factory.createIRI("http://www.uniriotec.br/wsmo/services#")));
		service.addNamespace(factory.createNamespace("dc", factory.createIRI("http://purl.org/dc/elements/1.1/")));
		service.addNamespace(factory.createNamespace("wsml", factory.createIRI("http://www.wsmo.org/wsml/wsml-syntax#")));
		service.addNamespace(factory.createNamespace("xsd", factory.createIRI("http://www.w3.org/2001/XMLSchema#")));
		service.addNamespace(targetNamespace);
		          
		// set NFP values
		service.addNFPValue(factory.createIRI(NFP.DC_TITLE), dataFactory.createWsmlString("Web Service " + separatedNumber(index)));
		service.addNFPValue(factory.createIRI(NFP.DC_DESCRIPTION), dataFactory.createWsmlString(createStringFromParameters()));
		 
		// import used ontologies
		service.addOntology(factory.getOntology(factory.createIRI("http://www.uniriotec.br/wsmo/ontology/Concepts.owl")));
		
		// create capability
		Capability capability = factory.createCapability(factory.createIRI(targetNamespace, wsmoSWSName + "Capability"));
		Axiom axiom = null; 
		LogicalExpression logExp = null;
		         
		// add capability post-condition
		axiom = factory.createAxiom(factory.createIRI(targetNamespace, "axiom"));
		
		StringBuilder logicalExpression = new StringBuilder();
		
		int i = 0;
		do {
			if (i != 0) 
				logicalExpression.append(" and ");
			
			logicalExpression.append("?");
			logicalExpression.append(alphabet[i]);
			logicalExpression.append(" memberOf _\"");
			logicalExpression.append(wsmoSWSParameters.get(i));
			logicalExpression.append("\"");
			
			i++;
		} while (i < wsmoSWSParameters.size());
		logicalExpression.append(".\n");
		
		logExp = leFactory.createLogicalExpression(logicalExpression.toString(), service);
		axiom.addDefinition(logExp);
		capability.addPostCondition(axiom);
		service.setCapability(capability);
		         
		return service;
	}

	private String separatedNumber(int number) {
		String separated = Integer.toString(number);
		
		if (separated.length() == 1) {
			return separated;
		} else {
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < separated.length(); i++) {
				sb.append(separated.charAt(i));
				sb.append(" ");
			}
			return sb.toString();
		}
	}
	
	private String createStringFromParameters() {
		
		StringBuilder sb = new StringBuilder();
		for (String s : wsmoSWSParameters) {
			String[] temp = s.split("#");
			sb.append(temp[1]);
			sb.append(" ");
		}
		return sb.toString();
	}
}
