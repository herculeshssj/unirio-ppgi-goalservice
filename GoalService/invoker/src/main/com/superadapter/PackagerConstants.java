package com.superadapter;

public class PackagerConstants {

	static final String packagerOntoNS = "http://www.ip-super.org/ontologies/prereview#"; 
	static final String packagerAdapterNS = "http://www.wsmo.org/ontologies/PackagerAdapterOntology#";
	
	static final String packagerOntoIRI = packagerOntoNS+"sbpelProcess"; 
	static final String packagerAdapterIRI = packagerAdapterNS+"AdapterOntology";
	
	static final String WSPackagerRequest = "http://localhost:8001/Packager?wsdl#wsdl.interfaceMessageReference(PackagerPort/generateURL/in0)";
	
	static final String attributeInputMessage = packagerAdapterNS + "inputMessage";
	static final String attributeInstanceMappings = packagerAdapterNS + "instanceMappings";
	static final String attributeValueMappings = packagerAdapterNS + "valueMappings";
	static final String attributeConceptOutput = packagerAdapterNS + "conceptOutput";	
	
}
