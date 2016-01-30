package com.swschallenge.adapter;

public class SWSChallCompositionConstants {

	static final String swsPath = "http://www.wsmo.org/sws-challenge-composition/";

	static final String productOntoNS = swsPath+"ProductOntology#"; 
//	static final String shipmentOntoProcessNS = swsPath+"ShipmentOntologyProcess#";
//	static final String shipmentOntoInstancesNS = swsPath+"ShipmentOntologyInstances#";
	static final String productAdapterNS = swsPath+"AdapterOntology#";
	
	static final String productOntoIRI = productOntoNS+"ProductOntology"; 
//	static final String productOntoProcessIRI = productOntoProcessNS+"ShipmentOntologyProcess";
//	static final String productOntoInstancesIRI = productOntoInstancesNS+"ShipmentOntologyInstances";
	static final String productAdapterIRI = productAdapterNS+"AdapterOntology";

	
	static final String WSBargainerGet	= "http://sws-challenge.org/shops/Bargainer.wsdl#wsdl.interfaceMessageReference(BargainerPort/listProducts/in0)";
	static final String WSHawkerGet		= "http://sws-challenge.org/shops/Hawker.wsdl#wsdl.interfaceMessageReference(HawkerPort/listProducts/in0)";
	static final String WSRummageGet	= "http://sws-challenge.org/shops/Rummage.wsdl#wsdl.interfaceMessageReference(RummagePort/listProducts/in0)";
	
	static final String WSBargainerOrder =  "http://sws-challenge.org/shops/Bargainer.wsdl#wsdl.interfaceMessageReference(BargainerPort/order/in0)";
	static final String WSHawkerOrder    = 	"http://sws-challenge.org/shops/Hawker.wsdl#wsdl.interfaceMessageReference(HawkerPort/order/in0)";
	static final String WSRummageOrder   = 	"http://sws-challenge.org/shops/Rummage.wsdl#wsdl.interfaceMessageReference(RummagePort/order/in0)";
	
	static final String attributeInputMessage = productAdapterNS + "inputMessage";
	static final String attributeInstanceMappings = productAdapterNS + "instanceMappings";
	static final String attributeValueMappings = productAdapterNS + "valueMappings";
	static final String attributeConceptOutput = productAdapterNS + "conceptOutput";	
	
}
