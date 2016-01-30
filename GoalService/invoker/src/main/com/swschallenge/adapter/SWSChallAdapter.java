package com.swschallenge.adapter;

import ie.deri.wsmx.adapter.Adapter;
import ie.deri.wsmx.commons.Helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;

import org.omwg.ontology.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Serializer;
import org.xml.sax.InputSource;

/**
 * 
 * @author based on DIP eBanking adapter by Silverster, Ozelin, Laurent, 
 * 		   further developed by Maciej Zaremba
 *
 */

public class SWSChallAdapter extends Adapter{
	
	protected static Logger logger = Logger.getLogger(SWSChallAdapter.class);

	public SWSChallAdapter() {
		super();
	}

	
	
	public org.w3c.dom.Document getXML (List<Entity> instances, String id)
	{
		logger.debug("enter in getXML");
		logger.debug("Set size: "+instances.size());
		logger.debug("ID: "+id);
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			String sDoc = "";
			boolean found = false;

			for (Entity entity : instances) {
				if (! (entity instanceof Entity) )
					break;
				Instance instance = (Instance) entity;
				Set <Concept> concepts = instance.listConcepts();
				for (Concept concept: concepts)
				{
					String conceptName = concept.getIdentifier().toString();
					
					if (conceptName.equalsIgnoreCase(SWSChallConstants.shipmentOntoProcessNS+"ShipmentOrderReq"))
					{
						sDoc = getShipmentOrderReqXML(instance, id);
						found = true;
					}
					if (found) break;
				}
				if (found) break;
			}
			
			logger.debug("---------obtained XML pefore parsing--------- \n"+ sDoc + "\n------------------");
			InputSource is = new InputSource(new StringReader(sDoc));
			org.w3c.dom.Document doc = builder.parse(is);
			return doc;
		}
		catch (Exception e) {
			logger.error("Error in getXML");
			return null;
		}
	}

	//returns ConctacInfo data in following form:
	//

	
	private class ContactInfo{
		String company = "";
		String firstname = "";
		String lastname = "";
		String title = "";
		String address = "";
		String postalCode = "";
		String streetAddress = "";
		String city = "";
		String state = "";
		String country = "";
		String phone = "";
		String email = "";
		String fax = "";
		
		ContactInfo(){
			super();	
		}
		
		ContactInfo(Instance contactInfo){

			this.company = Helper.getInstanceAttribute(contactInfo,loadIRI(SWSChallConstants.shipmentOntoNS+"company"));
			this.firstname = Helper.getInstanceAttribute(contactInfo,loadIRI(SWSChallConstants.shipmentOntoNS+"firstname"));
			this.lastname = Helper.getInstanceAttribute(contactInfo,loadIRI(SWSChallConstants.shipmentOntoNS+"lastname"));
			this.title = Helper.getInstanceAttribute(contactInfo,loadIRI(SWSChallConstants.shipmentOntoNS+"title"));
			this.phone = Helper.getInstanceAttribute(contactInfo,loadIRI(SWSChallConstants.shipmentOntoNS+"phone"));
			this.fax = Helper.getInstanceAttribute(contactInfo,loadIRI(SWSChallConstants.shipmentOntoNS+"fax"));
			this.email = Helper.getInstanceAttribute(contactInfo,loadIRI(SWSChallConstants.shipmentOntoNS+"email"));
			
			Instance addressInst = Helper.getInstanceAttribute(contactInfo, loadIRI(SWSChallConstants.shipmentOntoNS+"address"), 
								   loadIRI(SWSChallConstants.shipmentOntoNS+"Address"));

			if (addressInst!=null){
				this.streetAddress = Helper.getInstanceAttribute(addressInst,loadIRI(SWSChallConstants.shipmentOntoNS+"streetAddress"));  
				this.state = Helper.getInstanceAttribute(addressInst,loadIRI(SWSChallConstants.shipmentOntoNS+"stateProvinceCounty"));
				this.postalCode = Helper.getInstanceAttribute(addressInst,loadIRI(SWSChallConstants.shipmentOntoNS+"postalCode"));
				
				Instance cityInst = Helper.getInstanceAttribute(addressInst, loadIRI(SWSChallConstants.shipmentOntoNS+"city"), 
						   loadIRI(SWSChallConstants.shipmentOntoNS+"City"));
				
				if (cityInst!=null) {
					this.city = Helper.getInstanceAttribute(cityInst,loadIRI(SWSChallConstants.shipmentOntoNS+"name"));
					Instance countryInst = Helper.getInstanceAttribute(cityInst, loadIRI(SWSChallConstants.shipmentOntoNS+"country"), 
							   loadIRI(SWSChallConstants.shipmentOntoNS+"Country"));
					if (countryInst!=null){
						this.country = Helper.getInstanceAttribute(countryInst,loadIRI(SWSChallConstants.shipmentOntoNS+"name"));
					}
				}
				
				this.address = streetAddress+"; " +city+", "+state+", "+postalCode+", "+ country;
			}
		}
	}
	
	
	private ContactInfo getContactInfo(Instance instance){
	
		return null;
		
	}
	
//	public String getPriceQuoteReqXML(Instance instance, Identifier id)
//	{
//		String result = "";
//		
//		ContactInfo to = new ContactInfo();
//		String package_quantity = "";
//		String package_length = "";
//		String package_width  = "";
//		String package_height = "";
//		String package_weight = "";	
//
//		Instance toInst = Helper.getInstanceAttribute(instance, loadIRI(SWSChallConstants.shipmentOntoProcessNS+"to"), 
//				loadIRI(SWSChallConstants.shipmentOntoNS+"ContactInfo"));
//		if (toInst != null){
//			to = new ContactInfo(toInst);
//		}
//
//		Instance packageInst = Helper.getInstanceAttribute(instance, loadIRI(SWSChallConstants.shipmentOntoProcessNS+"package"), 
//				loadIRI(SWSChallConstants.shipmentOntoNS+"Package"));
//		if (packageInst != null){
//			package_quantity = Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"quantity"));
//			package_length = Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"length"));
//			package_width  = Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"width"));
//			package_height = Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"height"));
//			package_weight = Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"weight"));
//		}
//
//		if (id.toString().contains(SWSChallConstants.WSMuller) )
//		{
//			result+="<q0:invokePriceRequest xmlns:q0=\"http://www.example.org/muller/\">";
//			result+="  <q0:country>"+to.country+"</q0:country>";
//			result+="  <q0:packageInformation>";
//			result+="     <q0:quantity>"+package_quantity+"</q0:quantity>";
//			result+="     <q0:weight>"+package_weight+"</q0:weight>";
//			result+="     <q0:length>"+package_length+"</q0:length>";
//			result+="     <q0:height>"+package_height+"</q0:height>";
//			result+="     <q0:width>"+package_width+"</q0:width>";
//			result+="  </q0:packageInformation>";
//			result+="</q0:invokePriceRequest>";
//		}
//		return result;
//	}
	
	public String getShipmentOrderReqXML(Instance instance, String id)
	{
		String result = "";
		
		ContactInfo from = new ContactInfo();
		ContactInfo to   = new ContactInfo();
		String package_quantity = "";
		String package_length = "";
		String package_width  = "";
		String package_height = "";
		String package_weight = "";
		String shipmentDate_earliest = "";
		String shipmentDate_latest    = "";
		
		Instance fromInst = Helper.getInstanceAttribute(instance, loadIRI(SWSChallConstants.shipmentOntoProcessNS+"from"), 
				loadIRI(SWSChallConstants.shipmentOntoNS+"ContactInfo"));
		if (fromInst != null){
			from = new ContactInfo(fromInst);
		}

		Instance toInst = Helper.getInstanceAttribute(instance, loadIRI(SWSChallConstants.shipmentOntoProcessNS+"to"), 
				loadIRI(SWSChallConstants.shipmentOntoNS+"ContactInfo"));
		if (toInst != null){
			to = new ContactInfo(toInst);
		}

		Instance packageInst = Helper.getInstanceAttribute(instance, loadIRI(SWSChallConstants.shipmentOntoProcessNS+"package"), 
				loadIRI(SWSChallConstants.shipmentOntoNS+"Package"));
		if (packageInst != null){
			package_quantity = ""+ new Double(Math.ceil(new Double(Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"quantity"))))).intValue(); 
			package_length 	 = ""+ new Double(Math.ceil(new Double(Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"length"))))).intValue(); 
			package_width  = ""+ new Double(Math.ceil(new Double(Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"width"))))).intValue();
			package_height = ""+ new Double(Math.ceil(new Double(Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"height"))))).intValue(); 
			package_weight = ""+ new Double(Math.ceil(new Double(Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+"weight"))))).intValue(); 
		}

		Instance shipmentDateInst = Helper.getInstanceAttribute(instance, loadIRI(SWSChallConstants.shipmentOntoProcessNS+"shipmentDate"), 
				loadIRI(SWSChallConstants.shipmentOntoNS+"ShipmentDate"));
		if (shipmentDateInst != null){
			shipmentDate_earliest = Helper.getInstanceAttribute(shipmentDateInst,loadIRI(SWSChallConstants.shipmentOntoNS+"earliest"));
			shipmentDate_latest   = Helper.getInstanceAttribute(shipmentDateInst,loadIRI(SWSChallConstants.shipmentOntoNS+"latest"));
		}
		
		if (id.toString().contains(SWSChallConstants.WSMullerGetQuote) )
		{
			result+="<q0:invokePriceRequest xmlns:q0=\"http://www.example.org/muller/\">";
			result+="  <q0:country>"+to.country+"</q0:country>";
			result+="  <q0:packageInformation>";
			result+="     <q0:quantity>"+package_quantity+"</q0:quantity>";
			result+="     <q0:weight>"+package_weight+"</q0:weight>";
			result+="     <q0:length>"+package_length+"</q0:length>";
			result+="     <q0:height>"+package_height+"</q0:height>";
			result+="     <q0:width>"+package_width+"</q0:width>";
			result+="  </q0:packageInformation>";
			result+="</q0:invokePriceRequest>";
		} else if (id.toString().contains(SWSChallConstants.WSMullerOrderRequest) )
		{
			result += "<q0:shipmentOrderRequest xmlns:q0=\"http://www.example.org/muller/\">"; 
			result += "  <q0:addressFrom>";
			result += "     <q0:firstname>" + from.firstname + "</q0:firstname>";
			result += "     <q0:lastname>" + from.lastname+ "</q0:lastname>";
			result += "     <q0:address>" + from.address+ "</q0:address>";
			result += "      <q0:location>";
			result += "         <q0:postalCode>" + from.postalCode + "</q0:postalCode>";
			result += "         <q0:country>" + from.country + "</q0:country>";
			result += "         <q0:state>" + from.state + "</q0:state>";
			result += "      </q0:location>";
			result += "      <q0:contactInformation>";
			result += "        <q0:phone>" + from.phone +"</q0:phone>";
			result += "        <q0:EMail>" + from.email +"</q0:EMail>";
			result += "        <q0:fax>" + from.fax +"</q0:fax>";
			result += "      </q0:contactInformation>";
			result += "  </q0:addressFrom>";
			result += "  <q0:shipmentDate>";
			result += "    <q0:earliestPickupDate>"+shipmentDate_earliest+"</q0:earliestPickupDate>";
			result += "    <q0:latestPickupDate>"+shipmentDate_latest+"</q0:latestPickupDate>";
			result += "  </q0:shipmentDate>";
			result += "  <q0:packageInformation>";
			result += "    <q0:quantity>"+package_quantity+"</q0:quantity>";
			result += "    <q0:weight>"+package_weight+"</q0:weight>";
			result += "    <q0:length>"+package_length+"</q0:length>";
			result += "    <q0:height>"+package_height+"</q0:height>";
			result += "    <q0:width>"+package_width+"</q0:width>";
			result += "  </q0:packageInformation>";
			result += "  <q0:addressTo>";
			result += "     <q0:firstname>" + to.firstname + "</q0:firstname>";
			result += "     <q0:lastname>" + to.lastname+ "</q0:lastname>";
			result += "     <q0:address>" + to.address+ "</q0:address>";
			result += "      <q0:location>";
			result += "         <q0:postalCode>" + to.postalCode + "</q0:postalCode>";
			result += "         <q0:country>" + to.country + "</q0:country>";
			result += "         <q0:state>" + to.state + "</q0:state>";
			result += "      </q0:location>";
			result += "  </q0:addressTo>";
			result += "</q0:shipmentOrderRequest>";
		} else if (id.toString().contains(SWSChallConstants.WSRacerOrderRequest) ){
			
			if (from.country.contains("United Kingdom"))
				from.country = "United Kingdom(Great Britain)";
			if (to.country.contains("United Kingdom"))
				to.country = "United Kingdom(Great Britain)";
			
			result += "<q0:OrderOperationRequest xmlns:q0=\"http://www.example.org/racer/\">";
			result += "  <q0:from>"; 
	        result += "    <q0:Company>"+from.company+"</q0:Company>";
			result += "    <q0:FirstName>" + from.firstname + "</q0:FirstName>";
			result += "    <q0:MiddleInitial></q0:MiddleInitial>";
			result += "    <q0:LastName>" + from.lastname + "</q0:LastName>";
			result += "    <q0:PhoneNumber>" + from.phone + "</q0:PhoneNumber>";
			result += "    <q0:Address1>" + from.streetAddress + "</q0:Address1>";
			result += "    <q0:Address2></q0:Address2>";
			result += "    <q0:City>" + from.city + "</q0:City>";
			result += "    <q0:State>" + from.state + "</q0:State>";
			result += "    <q0:ZipCode>" + from.postalCode + "</q0:ZipCode>";
			result += "    <q0:SpecialInstructions></q0:SpecialInstructions>";
			result += "  </q0:from>";
			result += "  <q0:to>";
			result += "    <q0:Company>"+to.company+"</q0:Company>";
			result += "    <q0:FirstName>" + to.firstname + "</q0:FirstName>";
			result += "    <q0:LastName>" + to.lastname + "</q0:LastName>";
			result += "    <q0:Address1>" + to.streetAddress + "</q0:Address1>";
			result += "    <q0:City>" + to.city + "</q0:City>";
			result += "    <q0:State>" + to.state + "</q0:State>";
			result += "    <q0:ZipCode>" + to.postalCode + "</q0:ZipCode>";
			result += "    <q0:Country>" + to.country + "</q0:Country>";
			result += "  </q0:to>";
			result += "  <q0:quantity>"+ package_quantity +"</q0:quantity>";
			result += "  <q0:packageWeight>"+ package_weight +"</q0:packageWeight>";
			result += "  <q0:collectionTime>";
			result += "    <q0:readyPickup>" + shipmentDate_earliest + "</q0:readyPickup>";
			result += "    <q0:latestPickup>"+ shipmentDate_latest + "</q0:latestPickup>";
			result += "  </q0:collectionTime>";
			result += "</q0:OrderOperationRequest>";
		} else if (id.toString().contains(SWSChallConstants.WSRunnerOrderRequest)){
			if (from.country.contains("United Kingdom"))
				from.country = "United Kingdom";
			if (to.country.contains("United Kingdom"))
				to.country = "United Kingdom";
			
			result += "<q0:OrderCollectionRequest xmlns:q0=\"http://www.example.org/runner/\">";
			result += "  <q0:from>";
			result += "    <q0:Name>"+from.firstname+" "+from.lastname+"</q0:Name>";
			result += "    <q0:Title>"+from.title+"</q0:Title>";
			result += "    <q0:StreetAddress>"+from.streetAddress+"</q0:StreetAddress>";
			result += "    <q0:City>"+from.city+"</q0:City>";
			result += "    <q0:StateProvinceCounty>"+from.state+"</q0:StateProvinceCounty>";
			result += "    <q0:PostalCode>"+from.postalCode+"</q0:PostalCode>";
			result += "    <q0:Country>"+from.country+"</q0:Country>";
			result += "    <q0:Telephone>"+from.phone+"</q0:Telephone>";
			result += "    <q0:EMail>"+from.email+"</q0:EMail>";
			result += "  </q0:from>";
			result += "  <q0:to>";
			result += "    <q0:Name>"+to.firstname+" "+to.lastname+"</q0:Name>";
			result += "    <q0:Title>"+to.title+"</q0:Title>";
			result += "    <q0:StreetAddress>"+to.streetAddress+"</q0:StreetAddress>";
			result += "    <q0:City>"+to.city+"</q0:City>";
			result += "    <q0:StateProvinceCounty>"+to.state+"</q0:StateProvinceCounty>";
			result += "    <q0:PostalCode>"+to.postalCode+"</q0:PostalCode>";
			result += "    <q0:Country>"+to.country+"</q0:Country>";
			result += "    <q0:Telephone>"+to.phone+"</q0:Telephone>";
			result += "    <q0:EMail>"+to.email+"</q0:EMail>";
			result += "  </q0:to>";
			result += "  <q0:packageWeight>"+package_weight+"</q0:packageWeight>";
			result += "  <q0:collectionTime>";
			result += "    <q0:start>"+shipmentDate_earliest+"</q0:start>";
			result += "    <q0:end>"+shipmentDate_latest+"</q0:end>";
			result += "  </q0:collectionTime>";
			result += "  </q0:OrderCollectionRequest>";
		} else if (id.toString().contains(SWSChallConstants.WSWalkerOrderRequest)){
			if (from.country.contains("United Kingdom"))
				from.country = "United Kingdom(England)";
			if (to.country.contains("United Kingdom"))
				to.country = "United Kingdom(England)";
			
			result += "<q0:OrderRequest xmlns:q0=\"http://www.example.org/walker/\">";
			result += "  <q0:from>";
			result += "	   <q0:AddressInformation>";
			result += "	     <q0:ContactName>"+from.firstname+" "+from.lastname+"</q0:ContactName>";
			result += "	     <q0:Telephone>"+from.phone+"</q0:Telephone>";
			result += "	     <q0:Email>"+from.email+"</q0:Email>";
			result += "    </q0:AddressInformation>";
			result += "   <q0:ContactInformation>";
			result += "     <q0:Company>"+from.company+"</q0:Company>";
			result += "     <q0:Address1>"+from.streetAddress+"</q0:Address1>";
			result += "     <q0:Address2></q0:Address2>";
			result += "     <q0:City>"+from.city+"</q0:City>";
			result += "     <q0:StateProvince>"+from.state+"</q0:StateProvince>";
			result += "     <q0:PickupLocation>front</q0:PickupLocation>";
			result += "     </q0:ContactInformation>";
			result += "  </q0:from>";
			result += "  <q0:to>";
			result += "     <q0:Name>"+to.firstname+" "+to.lastname+"</q0:Name>";
			result += "     <q0:Address>"+to.streetAddress+"</q0:Address>";
			result += "     <q0:City>"+to.city+"</q0:City>";
			result += "     <q0:StateProvinceCounty>"+to.state+"</q0:StateProvinceCounty>";
			result += "     <q0:PostalCode>"+to.postalCode+"</q0:PostalCode>";
			result += "     <q0:Country>"+to.country+"</q0:Country>";
			result += "     <q0:Telephone>"+to.phone+"</q0:Telephone>";
			result += "     <q0:EMail>"+to.email+"</q0:EMail>";
			result += "  </q0:to>";
			result += "  <q0:collectionTime>";
			result += "    <q0:readyPickup>"+shipmentDate_earliest+"</q0:readyPickup>";
			result += "    <q0:latestPickup>"+shipmentDate_latest+"</q0:latestPickup>";
			result += "  </q0:collectionTime>";
			result += "  <q0:packageInformation>";
			result += "    <q0:length>"+package_length+"</q0:length>";
			result += "    <q0:height>"+package_height+"</q0:height>";
			result += "    <q0:width>"+package_width+"</q0:width>";
			result += "    <q0:packageWeight>"+package_weight+"</q0:packageWeight>";
			result += "  </q0:packageInformation>";
			result += "</q0:OrderRequest>";
		} else if (id.toString().contains(SWSChallConstants.WSWeaselOrderRequest)){
			result += "<q0:weaselOrderRequest xmlns:q0=\"http://www.example.org/weasel/\">";
			result += "  <q0:pickupAddress>";
			result += "    <q0:Name>"+from.firstname+" "+from.lastname+"</q0:Name>";
			result += "    <q0:Address>"+from.address+"</q0:Address>";
			result += "    <q0:City>"+from.city+"</q0:City>";
			result += "    <q0:PostalCode>"+from.postalCode+"</q0:PostalCode>";
			result += "    <q0:State>"+from.state+"</q0:State>";
			result += "  </q0:pickupAddress>";
			result += "  <q0:deliveryAddress>";
			result += "    <q0:Name>"+to.firstname+" "+to.lastname+"</q0:Name>";
			result += "    <q0:Address>"+to.address+"</q0:Address>";
			result += "    <q0:City>"+to.city+"</q0:City>";
			result += "    <q0:PostalCode>"+to.postalCode+"</q0:PostalCode>";
			result += "    <q0:State>"+to.state+"</q0:State>";
			result += "  </q0:deliveryAddress>";
			result += "  <q0:FirstPickupDate>"+shipmentDate_earliest+"</q0:FirstPickupDate>";
			result += "  <q0:LatestPickupDate>"+shipmentDate_latest+"</q0:LatestPickupDate>";
			result += "  <q0:quantity>"+package_quantity+"</q0:quantity>";
			result += "  <q0:weight>"+package_weight+"</q0:weight>";
			result += "  <q0:width>"+package_width+"</q0:width>";
			result += "  <q0:height>"+package_height+"</q0:height>";
			result += "  <q0:length>"+package_length+"</q0:length>";
			result += "</q0:weaselOrderRequest>";
		}
		return result;
	}
    
    public SOAPElement getHeader(List<Entity> instances, String id){
    	return null;
    }
        
    public static String loadURL2(URL url) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(url.
                openStream()));
        String pagina = new String("");
        char[] buffer = new char[512];
        int leidos = -1;
        while ((leidos = in.read(buffer)) != 0) {
            logger.debug(".");
            pagina += new String(buffer, 0, leidos);
            if (!in.ready() == true)
                break;
        }
        return pagina;
    }

  
  public IRI loadIRI(String value)
  {
	  WsmoFactory factory = Factory.createWsmoFactory(null);
	  IRI result = factory.createIRI(value);
	  return result;
  }
  
  
  //***********************************************************++
	@Override
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint ) {
		Instance instResult=null;
		try {
			//			Define a namespaces used in response
			Iterator itera;
			SimpleNamespaceContext nsContext = new SimpleNamespaceContext();

			try {
				MessageFactory msgFactory = MessageFactory.newInstance();
				SOAPMessage message = msgFactory.createMessage();
				SOAPPart soapp = message.getSOAPPart();
				StreamSource source = new StreamSource(new StringReader(document));
				soapp.setContent(source);

				SOAPEnvelope enve = soapp.getEnvelope();
				SOAPBody body = enve.getBody();
				itera = body.getChildElements();

		    while(itera.hasNext()) {
		        SOAPElement element = (SOAPElement)itera.next();
		        addNamespaces(nsContext, element);
		    }
		    
		    String eStr = endpoint.toString();
		    
			if ( (eStr.contains(SWSChallConstants.WSMullerGetQuote)) || (eStr.toString().contains(SWSChallConstants.WSMullerOrderRequest)) ) 
				nsContext.addNamespace("ns1", "http://www.example.org/muller/" );
			else if (eStr.toString().contains(SWSChallConstants.WSRacerOrderRequest) )
				nsContext.addNamespace("ns1", "http://www.example.org/racer/" );
			else if (eStr.toString().contains(SWSChallConstants.WSRunnerOrderRequest))
				nsContext.addNamespace("ns1", "http://www.example.org/runner/" );
			else if (eStr.toString().contains(SWSChallConstants.WSWalkerOrderRequest))
				nsContext.addNamespace("ns1", "http://www.example.org/walker/" );
			else if (eStr.toString().contains(SWSChallConstants.WSWeaselOrderRequest))
				nsContext.addNamespace("ns1", "http://www.example.org/weasel/" );		    
		    
			} catch (SOAPException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();		
			InputSource is = new InputSource(new StringReader(document));
			Document doc = builder.parse(is);
			
			DataFactory dataFactory = Factory.createDataFactory(null);
			//TODO ver como identificamos instancia de mappeo
			Ontology temp = this.createTempOntology();
			ArrayList<Instance> instancesToTranslate = getInstanceToTransalate(doc, nsContext);
			for (Instance instanceToTranslate : instancesToTranslate) {
				
			
			//Set <Value> values = instanceToTranslate.listAttributeValues(loadIRI(SWSChallConstants.attributeInputMessage));
			ArrayList<Triple<String, String, String>> instanceMapps= getInstanceMappingsValues(instanceToTranslate);
			ArrayList<Pair<String, String>> valueMapps = getValuesMappingsValues(instanceToTranslate);
			instResult = createInstance(getTargetConcept(instanceToTranslate));
			Ontology shipmentOntology = Helper.getOntology(SWSChallConstants.shipmentOntoIRI);
			Ontology shipmentOntologyProcess = Helper.getOntology(SWSChallConstants.shipmentOntoProcessIRI);
			Ontology shipmentInstances = Helper.getOntology(SWSChallConstants.shipmentOntoInstancesIRI);
			WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);
			for (Pair<String, String> pair : valueMapps) {
				XPath xpath = new DOMXPath(pair.getFirst());
				xpath.setNamespaceContext(nsContext);
				List<Node> nodes = xpath.selectNodes(doc);
				IRI attributeIRI = wsmoFactory.createIRI(shipmentOntologyProcess.getDefaultNamespace(), pair.getSecond());

				try {
					//try to create Double
					try {
						//try to create float value
						Float tempFloat = new Float(nodes.get(0).getTextContent());
						instResult.addAttributeValue(attributeIRI, dataFactory.createWsmlDecimal(""+tempFloat));
						
					} catch (Exception e){
						//add as string
						instResult.addAttributeValue(attributeIRI, dataFactory
								.createWsmlString(nodes.get(0).getTextContent()));
					}
				} catch (IndexOutOfBoundsException e) {
					logger.error("Error in expression "+ pair.getFirst());
				}				
			}
			for (Triple<String,String,String> triple : instanceMapps) {
				XPath xpath = new DOMXPath(triple.getFirst());
				xpath.setNamespaceContext(nsContext);
				List<Node> nodes = xpath.selectNodes(doc);
				IRI storedInstanceIRI = wsmoFactory.createIRI(shipmentOntology.getDefaultNamespace(), triple.getSecond());
				Instance storedInstance = findInstance(shipmentInstances, storedInstanceIRI, nodes.get(0).getTextContent());
				IRI attributeIRI = wsmoFactory.createIRI(shipmentOntology.getDefaultNamespace(), triple.getThird());
				instResult.addAttributeValue(attributeIRI,storedInstance);				
			}
			temp.addInstance(instResult);
			}
			
			 
			String strTemp = writeOntology(temp);
			//clear this ontology
			for (Instance i : (Set<Instance>)temp.listInstances() ){
				Map attr = i.listAttributeValues();
				Set<IRI> keys = attr.keySet();
				
				for (IRI iri : keys){
					Object values = attr.get(iri);
					i.removeAttributeValues(iri);
				}
			}		
			temp.removeOntology(temp);
						
			return new WSMLDocument(strTemp);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public ArrayList<Instance> getInstanceToTransalate(Document soapMessage, SimpleNamespaceContext nsContext){
		
		Ontology adapterOntology = Helper.getOntology(SWSChallConstants.shipmentAdapterIRI);
		Set<Instance> instances = adapterOntology.listInstances();
		
		ArrayList<Instance> instacesReturn = new ArrayList<Instance>();
		
    	for (Instance instance: instances)
    	{
    		Set <Value>values = instance.listAttributeValues(loadIRI(SWSChallConstants.attributeInputMessage));
    		instance.getIdentifier();
    		for (Value value: values)
    		{
    			List<Node> nodes;
				try {
					
					XPath xpath = new DOMXPath(value.toString());
					xpath.setNamespaceContext(nsContext);
					logger.debug(value.toString());
					nodes = xpath.selectNodes(soapMessage);
					logger.debug(nodes.size());
	    			if (nodes.size()>0)
	    				instacesReturn.add(instance);
	    				 						
				} catch (JaxenException e) {
					logger.error("Error in: " + instance.getIdentifier().toString());
				}
	
    		}
    		
    	}
		return instacesReturn;

	}
	

	public ArrayList<Triple<String, String,String>> getInstanceMappingsValues(Instance insts){
		Ontology adapterOntology = Helper.getOntology(SWSChallConstants.shipmentAdapterIRI);
		ArrayList<Triple<String, String,String>> result =new  ArrayList<Triple<String, String,String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(SWSChallConstants.attributeInstanceMappings));
		for (Value value : values) {
			String valueStr = value.toString();
			try{
				String stringSplit[] = valueStr.split("=",3);
	  		  	String frist = stringSplit[0];
	  		  	String second = stringSplit[1];
	  		  	String third = stringSplit[2];
	  		  	Triple<String, String, String> tr = new Triple(frist,second,third);
	  		  	result.add(tr);	
			}
			catch (Exception e){
				logger.error("Error adding expression");
	  		  	}	
		 	}
		
	  	  return result;
		
	}		

	public String getTargetConcept(Instance insts){
		Ontology adapterOntology = Helper.getOntology(SWSChallConstants.shipmentOntoProcessIRI);
		ArrayList<Triple<String, String,String>> result =new  ArrayList<Triple<String, String,String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(SWSChallConstants.attributeConceptOutput));
		for (Value value : values) {
			return value.toString();
		 	}
		
	  	  return null;
		
	}		
	
	
	public Instance createInstance(String conceptName){
		Ontology shipmentOntology = Helper.getOntology(SWSChallConstants.shipmentOntoIRI);
		Ontology shipmentOntologyProcess = Helper.getOntology(SWSChallConstants.shipmentOntoProcessIRI);
		
		Concept concept = loadConcept(shipmentOntology, SWSChallConstants.shipmentOntoNS+conceptName);
		if (concept ==null )
			concept = loadConcept(shipmentOntologyProcess, SWSChallConstants.shipmentOntoProcessNS+conceptName);

		WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);

		IRI instanceIRI = wsmoFactory.createIRI(shipmentOntologyProcess.getDefaultNamespace(),"tempInst"+(new Long ((new Date()).getTime())).toString());
        Instance instanceResult = null;
		try {
			instanceResult = wsmoFactory.createInstance(instanceIRI,concept);
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return instanceResult;
	}	
	
	  public Concept loadConcept(Ontology ont, String value)
	  {
		  Concept concepto = null;
		  //WsmoFactory factory = Factory.createWsmoFactory(null);
		  concepto = ont.findConcept(loadIRI(value));
		  return concepto;
	  }	
	public ArrayList<Pair<String, String>> getValuesMappingsValues(Instance insts){
		
		ArrayList<Pair<String, String>> result =new  ArrayList<Pair<String, String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(SWSChallConstants.attributeValueMappings));
		for (Value value : values) {
			String valueStr = value.toString();
			try{
				String stringSplit[] = valueStr.split("=",3);
	  		  	String frist = stringSplit[0];
	  		  	String second = stringSplit[1];
	  		  	
	  		  	Pair<String, String> tr = new Pair(frist,second);
	  		  	result.add(tr);	
			}
			catch (Exception e){
				logger.error("Error adding expression");
	  		  	}	
		 	}
	  	  return result;
	}
	
	void addNamespaces(SimpleNamespaceContext context,
	        SOAPElement element) {
	    Iterator namespaces = element.getNamespacePrefixes();

	    while(namespaces.hasNext()) {
	        String prefix = (String)namespaces.next();
	        String uri = element.getNamespaceURI(prefix);
	        
	        if (uri.contains("www.xignite.com"))
	        	prefix = "xg";
	        if (uri.contains("swanandmokashi.com"))
	        	prefix="sti";
	        if (uri.contains("www.strikeiron.com"))
	        	prefix="sti";
	        context.addNamespace( prefix, uri );
	        logger.debug("prefix " + prefix + " " + uri );
	    }
	}
    
    public String writeOntology(Ontology theInputOnto){
    	try {
			Serializer serializer = Factory.createSerializer(null);
			StringWriter writer = new StringWriter();
			 TopEntity[] entitiesOnto = new TopEntity[1];
			 entitiesOnto[0] = theInputOnto;
			 serializer.serialize(entitiesOnto,writer);
			 return writer.getBuffer().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
    public Instance findInstance(Ontology ontology, String iriattribute, String compareTo)
    {
  	Instance instanceResult = null;
  	boolean found = false;
    	Set <Instance>instances = ontology.listInstances();
    	for (Instance instance: instances)
    	{
    		Set <Value>values = instance.listAttributeValues(loadIRI(iriattribute));
    		instance.getIdentifier();
    		for (Value value: values)
    		{
    			if (value.toString().equalsIgnoreCase(compareTo))
    			{
    				instanceResult = instance;
    				found = true;
    			}
    			if (found) break;
    		}
    		if (found) break;
    	}
    	return instanceResult;
    }	
    
    public Instance findInstance(Ontology ontology, IRI iriattribute, String compareTo)
    {
  	Instance instanceResult = null;
  	boolean found = false;
    	Set <Instance>instances = ontology.listInstances();
    	for (Instance instance: instances)
    	{
    		Set <Value>values = instance.listAttributeValues(iriattribute);
    		for (Value value: values)
    		{
    			if (value.toString().equalsIgnoreCase(compareTo))
    			{
    				instanceResult = instance;
    				found = true;
    			}
    			if (found) break;
    		}
    		if (found) break;
    	}
    	return instanceResult;
    }
    
    public Value getAttributeValue(Instance instance, String attributeName)
    {
  	  Value value = null;
  	  Set <Value> values = instance.listAttributeValues(loadIRI(attributeName));
  	  for (Value myValue : values)
  	  {
  		  value = myValue;
  		  break;
  		  //This method only return the first value of the attribute;
  	  }
  	  
  	  return value;
    }
    
    
    public Ontology createTempOntology() {

        //1. initialise the factory with the wsmo4j provider
        HashMap factoryParams = new HashMap();
        factoryParams.put(Factory.WSMO_SERIALIZER,
                "com.ontotext.wsmo4j.common.WSMOFactoryImpl");
        WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);

        try {

            //3. create an ontology
        	
            Ontology anOntology =wsmoFactory.createOntology(wsmoFactory.createIRI("http://www.example.org/ontologies/example#")); 	
            //     3.1 setup a namespace
            IRI iriso = wsmoFactory.createIRI(SWSChallConstants.shipmentOntoNS);
            IRI irisop = wsmoFactory.createIRI(SWSChallConstants.shipmentOntoProcessNS);
            IRI irisoi	= wsmoFactory.createIRI(SWSChallConstants.shipmentOntoInstancesNS);
            Namespace smNamespace = wsmoFactory.createNamespace("so",iriso);
            Namespace smpNamespace = wsmoFactory.createNamespace("sop", irisop); 
            Namespace smpiNamespace = wsmoFactory.createNamespace("soi", irisoi);
            anOntology.addNamespace(wsmoFactory.createNamespace("wsml", wsmoFactory.createIRI("http://www.wsmo.org/wsml/wsml-syntax#")));
            anOntology.addNamespace(smNamespace);
            anOntology.addNamespace(smpNamespace);
            anOntology.addNamespace(smpiNamespace);
            // 3.2 attach some NFPs

            
            // 3.2 adds some references to mediators and imported ontologies
            
            anOntology.addNamespace(smpNamespace);            
            

            return anOntology;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            
        }
        return null;
    }

    
    public class Triple <E, F, G> {

        private E e;
        private F f;
        private G g;
        
        public Triple (E theFirst, F theSecond, G theThird){
            this.e = theFirst;
            this.f = theSecond;
            this.g = theThird;
        }
        
        public E getFirst(){
            return e;
        }
        
        public F getSecond(){
            return f;
        }
        public G getThird(){
        	return g;
        }
    }
    public class Pair <E, F> {

        private E e;
        private F f;

        
        public Pair (E theFirst, F theSecond){
            this.e = theFirst;
            this.f = theSecond;

        }
        
        public E getFirst(){
            return e;
        }
        
        public F getSecond(){
            return f;
        }

    }
}
