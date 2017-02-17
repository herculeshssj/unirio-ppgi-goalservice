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
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Capability;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;
import org.wsmo.wsml.Serializer;

public class CreateWsmoFile {
	
	private WsmoFactory factory;
	
	private LogicalExpressionFactory leFactory;
	
	private DataFactory dataFactory;

	private String wsmoName;
	
	private List<String> wsmoParameters = new ArrayList<>();
	
	private int index = 0;
	
	private String[] alphabet = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
	
	public CreateWsmoFile(String swsName, List<String> parameterList, int index) {
		this.wsmoName = swsName;
		this.wsmoParameters = parameterList;
		this.index = index;
	}
	
	public void createWebService() throws IOException {
		factory = Factory.createWsmoFactory(null);
		leFactory = Factory.createLogicalExpressionFactory(null);
		dataFactory = Factory.createDataFactory(null);
		
		IRI serviceIRI = factory.createIRI("http://www.uniriotec.br/wsmo/services/" + wsmoName);
		WebService service = factory.createWebService(serviceIRI);
		service.setWsmlVariant("http://www.wsmo.org/wsml/wsml-syntax/wsml-flight");
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
		Capability capability = factory.createCapability(factory.createIRI(targetNamespace, wsmoName + "Capability"));
		Axiom axiom = null; 
		LogicalExpression logExp = null;
		         
		// add capability post-condition
		axiom = factory.createAxiom(factory.createIRI(targetNamespace, wsmoName + "Axiom"));
		
		StringBuilder logicalExpression = new StringBuilder();
		
		int i = 0;
		do {
			if (i != 0) 
				logicalExpression.append(" and ");
			
			logicalExpression.append("?");
			logicalExpression.append(alphabet[i]);
			logicalExpression.append(" memberOf _\"");
			logicalExpression.append(wsmoParameters.get(i));
			logicalExpression.append("\"");
			
			i++;
		} while (i < wsmoParameters.size());
		logicalExpression.append(".\n");
		
		logExp = leFactory.createLogicalExpression(logicalExpression.toString(), service);
		axiom.addDefinition(logExp);
		capability.addPostCondition(axiom);
		service.setCapability(capability);
		
		Serializer serializer = Factory.createSerializer(new HashMap<String, Object>(0));
		serializer.serialize(new TopEntity[] {service}, new FileWriter("C:\\CustomServiceWorkspace\\unirio-ppgi-webservices\\SWS-WSML\\Services\\WebService_" + index + ".wsml"));
	}
	
	public void createGoal() throws IOException {
		factory = Factory.createWsmoFactory(null);
		leFactory = Factory.createLogicalExpressionFactory(null);
		dataFactory = Factory.createDataFactory(null);
		
		IRI serviceIRI = factory.createIRI("http://www.uniriotec.br/wsmo/goals/" + wsmoName);
		Goal goal = factory.createGoal(serviceIRI);
		goal.setWsmlVariant("http://www.wsmo.org/wsml/wsml-syntax/wsml-flight");
		Namespace targetNamespace = factory.createNamespace("targetnamespace", factory.createIRI("http://www.uniriotec.br/wsmo/goals#"));
		  
		// set namespaces
		goal.setDefaultNamespace(factory.createNamespace("", factory.createIRI("http://www.uniriotec.br/wsmo/goals#")));
		goal.addNamespace(factory.createNamespace("dc", factory.createIRI("http://purl.org/dc/elements/1.1/")));
		goal.addNamespace(factory.createNamespace("wsml", factory.createIRI("http://www.wsmo.org/wsml/wsml-syntax#")));
		goal.addNamespace(factory.createNamespace("xsd", factory.createIRI("http://www.w3.org/2001/XMLSchema#")));
		goal.addNamespace(targetNamespace);
		          
		// set NFP values
		goal.addNFPValue(factory.createIRI(NFP.DC_TITLE), dataFactory.createWsmlString("Goal " + separatedNumber(index)));
		goal.addNFPValue(factory.createIRI(NFP.DC_DESCRIPTION), dataFactory.createWsmlString(createStringFromParameters()));
		 
		// import used ontologies
		goal.addOntology(factory.getOntology(factory.createIRI("http://www.uniriotec.br/wsmo/ontology/Concepts.owl")));
		
		// create capability
		Capability capability = factory.createCapability(factory.createIRI(targetNamespace, wsmoName + "Capability"));
		Axiom axiom = null; 
		LogicalExpression logExp = null;
		         
		// add capability post-condition
		axiom = factory.createAxiom(factory.createIRI(targetNamespace, wsmoName + "Axiom"));
		
		StringBuilder logicalExpression = new StringBuilder();
		
		int i = 0;
		do {
			if (i != 0) 
				logicalExpression.append(" and ");
			
			logicalExpression.append("?");
			logicalExpression.append(alphabet[i]);
			logicalExpression.append(" memberOf _\"");
			logicalExpression.append(wsmoParameters.get(i));
			logicalExpression.append("\"");
			
			i++;
		} while (i < wsmoParameters.size());
		logicalExpression.append(".\n");
		
		logExp = leFactory.createLogicalExpression(logicalExpression.toString(), goal);
		axiom.addDefinition(logExp);
		capability.addPostCondition(axiom);
		goal.setCapability(capability);
		
		Serializer serializer = Factory.createSerializer(new HashMap<String, Object>(0));
		serializer.serialize(new TopEntity[] {goal}, new FileWriter("C:\\CustomServiceWorkspace\\unirio-ppgi-webservices\\SWS-WSML\\Goals\\Goal_" + index + ".wsml"));
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
		for (String s : wsmoParameters) {
			String[] temp = s.split("#");
			sb.append(temp[1]);
			sb.append(" ");
		}
		return sb.toString();
	}
}
