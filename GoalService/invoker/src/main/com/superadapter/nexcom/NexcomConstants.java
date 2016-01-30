package com.superadapter.nexcom;

public class NexcomConstants {

	static final String nexcomOntoNS = "http://www.ip-super.org/ontologies/Nexcom/20070514#"; 
	static final String nexcomAdapterNS = "http://www.ip-super.org/ontologies/Nexcom/20070514/AdapterOntology#";
	
	static final String nexcomOntoIRI = nexcomOntoNS+"Nexcom"; 
	static final String nexcomAdapterIRI = nexcomAdapterNS+"AdapterOntology";
	
	static final String WSNexcomRequest = "http://localhost:8050/axis/services/Packager?wsdl#wsdl.interfaceMessageReference(Packager/generateURL/in0)";
	
	static final String attributeInputMessage = nexcomAdapterNS + "inputMessage";
	static final String attributeInstanceMappings = nexcomAdapterNS + "instanceMappings";
	static final String attributeValueMappings = nexcomAdapterNS + "valueMappings";
	static final String attributeConceptOutput = nexcomAdapterNS + "conceptOutput";	
	
}
