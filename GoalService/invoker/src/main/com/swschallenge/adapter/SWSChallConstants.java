package com.swschallenge.adapter;

public class SWSChallConstants {

	static final String swsPath = "http://www.wsmo.org/sws-challenge/";

	static final String shipmentOntoNS = swsPath+"ShipmentOntology#"; 
	static final String shipmentOntoProcessNS = swsPath+"ShipmentOntologyProcess#";
	static final String shipmentOntoInstancesNS = swsPath+"ShipmentOntologyInstances#";
	static final String shipmentAdapterNS = swsPath+"AdapterOntology#";
	
	static final String shipmentOntoIRI = shipmentOntoNS+"ShipmentOntology"; 
	static final String shipmentOntoProcessIRI = shipmentOntoProcessNS+"ShipmentOntologyProcess";
	static final String shipmentOntoInstancesIRI = shipmentOntoInstancesNS+"ShipmentOntologyInstances";
	static final String shipmentAdapterIRI = shipmentAdapterNS+"AdapterOntology";
	
	static final String WSMullerOrderRequest = "http://sws-challenge.org/shipper/v2/muller.wsdl#wsdl.interfaceMessageReference(mullerSOAP/ShipmentOrder/in0)";
	static final String WSMullerGetQuote = "http://sws-challenge.org/shipper/v2/muller.wsdl#wsdl.interfaceMessageReference(mullerSOAP/invokePrice/in0)";
	static final String WSRacerOrderRequest  = "http://sws-challenge.org/shipper/v2/racer.wsdl#wsdl.interfaceMessageReference(racer/OrderOperation/in0)";
	static final String WSRunnerOrderRequest = "http://sws-challenge.org/shipper/v2/runner.wsdl#wsdl.interfaceMessageReference(RunnerOrderSOAP/OrderCollection/in0)";
	static final String WSWalkerOrderRequest = "http://sws-challenge.org/shipper/v2/walker.wsdl#wsdl.interfaceMessageReference(walkerOrderSOAP/Order/in0)";
	static final String WSWeaselOrderRequest = "http://sws-challenge.org/shipper/v2/weasel.wsdl#wsdl.interfaceMessageReference(weasel/weaselOrder/in0)";
	
	static final String attributeInputMessage = shipmentAdapterNS + "inputMessage";
	static final String attributeInstanceMappings = shipmentAdapterNS + "instanceMappings";
	static final String attributeValueMappings = shipmentAdapterNS + "valueMappings";
	static final String attributeConceptOutput = shipmentAdapterNS + "conceptOutput";	
	
}
