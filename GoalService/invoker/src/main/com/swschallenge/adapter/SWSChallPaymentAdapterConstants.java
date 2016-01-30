package com.swschallenge.adapter;

public class SWSChallPaymentAdapterConstants {

	static final String paymentOntoNS = "http://www.wsmx.org/ontologies/UNIFIISO20022PaymentInitiation#"; 
	static final String moonOntoNS = "http://www.wsmx.org/ontologies/MoonPaymentOntology#";
	static final String paymentAdapterNS = "http://www.wsmo.org/sws-challenge-payment/AdapterOntology#";
	
	static final String paymentOntoIRI = paymentOntoNS+"ShipmentOntology";
	static final String moonOntIRI = moonOntoNS + "MoonPaymentOntology";
	static final String paymentAdapterIRI = paymentAdapterNS+"AdapterOntology";	
	
	static final String WSMoonFIPRequest = "http://sws-challenge.org/moon_v3/services/FIPService?wsdl#wsdl.interfaceMessageReference(FIPService/getBankingData/in0)";
	static final String WSBlueMDRequest = "http://sws-challenge.org/moon_v3/services/MDService?wsdl#wsdl.interfaceMessageReference(MDService/authorize/in0)";
	static final String WSBlueFDRequest = "http://sws-challenge.org/moon_v3/services/FDService?wsdl#wsdl.interfaceMessageReference(FDService/processPayment/in0)";
		
	static final String attributeInputMessage = paymentAdapterNS + "inputMessage";
	static final String attributeInstanceMappings = paymentAdapterNS + "instanceMappings";
	static final String attributeValueMappings = paymentAdapterNS + "valueMappings";
	static final String attributeConceptOutput = paymentAdapterNS + "conceptOutput";
}
