package pl.telekomunikacja.portal.adapter;

public class TP_Showcase_Constants {

	private static final String swsPath = "http://www.gsmo.org/dip/sws-challenge/";

	
	public static final String tpscOwnerPath = "http://TP_Showcase/";
	
	public static final String wsCustomerIdentificationName = "CustomerIdentification";
	public static final String wsFormalVerificationName = "FormalVerification";
	public static final String wsTechnicalVerificationName = "TechnicalVerification";
	public static final String wsOrderCreateName = "OrderCreate";
	public static final String wsOrderConfirmName = "OrderConfirm";
	public static final String wsContractPreparationName = "ContractPreparation";
	public static final String wsCourierServiceName = "CourierService";
	
	
	public static final String tpscOntologyPath = "http://org.ipsuper.composition.tp/";
	public static final String tpscInstancesPath = "http://org.ipsuper.composition.tp/";
	
	
	public static final String webServicesLocation = "http://localhost:8888/";
	
	public static final String wsCustomerIdentificationLocation = webServicesLocation+wsCustomerIdentificationName;
	
	
	public static final String CustomerIdentificationRequest = "http://myhost/myservice?WSDL#wsdl.interfaceMessageReference(myservice/myoperation/in0)";
	
	
//	xsltFile = new FileInputStream("resources" + File.separator
//	+ "communicationmanager" + File.separator
//	+ xslFileName)
	public static final String xlstLocation = "resources/resourcemanager/TP/xslt/";
	
	public static final String WebServiceNamespace="http://webservices.portal.telekomunikacja.pl/";
	public static final String WebServiceTypesNamespace="http://webservices.portal.telekomunikacja.pl/types/";
	
	
	
	public static String getWebServiceTypesNamespace(String webService){
		return "http://"+webService+".webservices.portal.telekomunikacja.pl/types/";
	}
	
	/*
	private static final String shipmentOntoNS = swsPath+"ShipmentOntology#"; 
	private static final String shipmentOntoProcessNS = swsPath+"ShipmentOntologyProcess#";
	private static final String shipmentOntoInstancesNS = swsPath+"ShipmentOntologyInstances#";
	private static final String shipmentAdapterNS = swsPath+"AdapterOntology#";
	
	private static final String shipmentOntoIRI = shipmentOntoNS+"ShipmentOntology"; 
	private static final String shipmentOntoProcessIRI = shipmentOntoProcessNS+"ShipmentOntologyProcess";
	private static final String shipmentOntoInstancesIRI = shipmentOntoInstancesNS+"ShipmentOntologyInstances";
	private static final String shipmentAdapterIRI = shipmentAdapterNS+"AdapterOntology";
	
	private static final String WSMullerOrderRequest = "http://sws-challenge.org/shipper/v2/muller.wsdl#wsdl.interfaceMessageReference(mullerSOAP/ShipmentOrder/in0)";
	private static final String WSMullerGetQuote = "http://sws-challenge.org/shipper/v2/muller.wsdl#wsdl.interfaceMessageReference(mullerSOAP/invokePrice/in0)";
	private static final String WSRacerOrderRequest  = "http://sws-challenge.org/shipper/v2/racer.wsdl#wsdl.interfaceMessageReference(racer/OrderOperation/in0)";
	private static final String WSRunnerOrderRequest = "http://sws-challenge.org/shipper/v2/runner.wsdl#wsdl.interfaceMessageReference(RunnerOrderSOAP/OrderCollection/in0)";
	private static final String WSWalkerOrderRequest = "http://sws-challenge.org/shipper/v2/walker.wsdl#wsdl.interfaceMessageReference(walkerOrderSOAP/Order/in0)";
	private static final String WSWeaselOrderRequest = "http://sws-challenge.org/shipper/v2/weasel.wsdl#wsdl.interfaceMessageReference(weasel/weaselOrder/in0)";
	
	private static final String attributeInputMessage = shipmentAdapterNS + "inputMessage";
	private static final String attributeInstanceMappings = shipmentAdapterNS + "instanceMappings";
	private static final String attributeValueMappings = shipmentAdapterNS + "valueMappings";
	private static final String attributeConceptOutput = shipmentAdapterNS + "conceptOutput";	
	*/
}
