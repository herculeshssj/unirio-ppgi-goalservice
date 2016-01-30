package com.isoco.dip.adapter;

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
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.omwg.ontology.Concept;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
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
import org.xml.sax.SAXException;

/**
 * 
 * @author Silverster, Ozelin, Laurent 
 *
 */

public class BrokerServiceAdapter extends Adapter{
	
	protected static Logger logger = Logger.getLogger(BrokerServiceAdapter.class);
	private SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
	/*
	public BrokerServiceAdapter(String id) {
		super(id);
	}*/
	public BrokerServiceAdapter() {
		super();
	}

	
	
	public org.w3c.dom.Document getXML (Instance instance, Identifier id)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			String sDoc = "";
			boolean found = false;

			Set <Concept> concepts = instance.listConcepts();
			for (Concept concept: concepts)
		  	{
				String conceptName = concept.getIdentifier().toString();
				if (conceptName.equalsIgnoreCase(BrokerConstants.conceptGetQuote))
				{
					sDoc = getGetQuoteXML(instance, id);
					found = true;
				}
				if (conceptName.equalsIgnoreCase(BrokerConstants.conceptGetRecommendation))
				{
					sDoc = getGetRecommendationXML(instance, id);
					found = true;
				}
				if (conceptName.equalsIgnoreCase(BrokerConstants.conceptGetIndex))
				{
					sDoc = getGetIndexXML(instance, id);
					found = true;
				}
				if (conceptName.equalsIgnoreCase(BrokerConstants.conceptPerformBuySell))
				{
					sDoc = getPerformBuySell(instance, id);
					found = true;
				}
				if (conceptName.equalsIgnoreCase(BrokerConstants.conceptGetNews))
				{
					sDoc = getGetNews(instance, id);
					found = true;
				}
				if (conceptName.equalsIgnoreCase(BrokerConstants.conceptSendAlert))
				{
					sDoc = getSendAlert(instance, id);
					found = true;
				}
				
				if (found) break;
		  	}
			
			InputSource is = new InputSource(new StringReader(sDoc));
			org.w3c.dom.Document doc = builder.parse(is);
			return doc;
		}
		catch (Exception e) {
			logger.error("Error in getXML BrokerServiceAdapter");
			return null;
		}
	}
	
	public String getGetQuoteXML(Instance instance, Identifier id)
	{
		String result = "";
		String instanceName = ""; 
		Set <Value>values = instance.listAttributeValues(loadIRI(BrokerConstants.conceptProcessStock));
  		for (Value value: values)
  		{
  			if(value instanceof Instance){
  				String identifier = ((Instance)value).getIdentifier().toString(); 
  	  			instanceName = identifier.substring(identifier.indexOf('#')+1);
  			}
  			
  		}
		Ontology ontology = loadOntology(BrokerConstants.stockMarketInstancesOntoIRI);
		Instance stockInstance = ontology.findInstance(loadIRI(BrokerConstants.stockMarketInstancesNS+instanceName));
		
		String stockTicker = getAttributeValue(stockInstance, BrokerConstants.attributeStockTicker).toString();
		String stockName = getAttributeValue(stockInstance, BrokerConstants.attributeStockName).toString();

		if (id.toString().contains(BrokerConstants.Bankinter) )
		{
			String marketCode = "";
			Instance marketInstance = ontology.findInstance(loadIRI(BrokerConstants.nasdaq));
			marketCode = getAttributeValue(marketInstance, BrokerConstants.attributeMarketCode).toString();
			result += "<searchValue xmlns=\"https://aia.ebankinter.com/wsBrokerService/\">";
			result += "<name>"+stockName+"</name>";
			result += "<market>"+marketCode+"</market>";
			result += "</searchValue>";
		}
		if (id.toString().contains(BrokerConstants.StrikeIron) )
		{			
			result += "<tns:GetQuotes xmlns:tns=\"http://swanandmokashi.com\" >";
            result += "<tns:QuoteTicker xsi:type=\"xsd:string\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >" + stockTicker + "</tns:QuoteTicker>"; 
            result += "</tns:GetQuotes>";
		}
		if (id.toString().contains(BrokerConstants.XIgnite))
		{
			result += "<tns:GetQuote xmlns:tns=\"http://www.xignite.com/services/\">";
            result += "<tns:Symbol xsi:type=\"xsd:string\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >" + stockTicker + "</tns:Symbol>"; 
            result += "</tns:GetQuote>";
		}
		return result;
	}
	
	
	public String getGetIndexXML(Instance instance, Identifier id)
	{
		String result = "";
		Ontology ontology = loadOntology(BrokerConstants.stockMarketInstancesOntoIRI);
		Instance marketInstance = ontology.findInstance(loadIRI(BrokerConstants.nasdaq));

		if (id.toString().contains(BrokerConstants.Bankinter))
		{
			String marketCode = getAttributeValue(marketInstance, BrokerConstants.attributeMarketCode).toString();
			String marketISIN = getAttributeValue(marketInstance, BrokerConstants.attributeMarketISIN).toString();
			marketISIN = marketISIN.replaceAll("\\^","");
			result += "<cotizacionIndice xmlns=\"https://aia.ebankinter.com/wsBrokerService/\">";
			result += "<isin>"+marketISIN+"</isin>";
			result += "<market>"+marketCode+"</market>";
			result += "</cotizacionIndice>";
		}
		if (id.toString().contains(BrokerConstants.StrikeIron))
		{	
			String marketISIN = getAttributeValue(marketInstance, BrokerConstants.attributeMarketISIN).toString();
			result+= "<si:GetDelayedValue xmlns:si=\"http://www.strikeiron.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
			result+= "<si:Identifier xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xsd:string\">"+marketISIN+"</si:Identifier>";
			result+= "<si:IdentifierType>Symbol</si:IdentifierType>";
			result+= "</si:GetDelayedValue>";
		}
		if (id.toString().contains(BrokerConstants.XIgnite))
		{//does not work. I think that it is because of the definition of xsd. it is deleted somewhere
			String marketISIN = getAttributeValue(marketInstance, BrokerConstants.attributeMarketISIN).toString();
			result += "<tns:GetDelayedValue xmlns:tns=\"http://www.xignite.com/services/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">";
			result+= "<tns:Identifier xsi:type=\"xsd:string\">^IUXX</tns:Identifier>";
			result+= "<tns:IdentifierType>Symbol</tns:IdentifierType>";
			result+= "</tns:GetDelayedValue>";
		}
		return result;
	}
	
	public String getPerformBuySell(Instance instance, Identifier id)
	{
		String result = "";
		String instanceNameStock = ""; 
		Ontology ontology = loadOntology(BrokerConstants.stockMarketInstancesOntoIRI);
		Instance marketInstance = ontology.findInstance(loadIRI(BrokerConstants.nasdaq));

		Set <Value>valuesStock = instance.listAttributeValues(loadIRI(BrokerConstants.conceptProcessStock));
  		for (Value value: valuesStock)
  		{
  			if(value instanceof Instance){
  				String identifier = ((Instance)value).getIdentifier().toString(); 
  				instanceNameStock = identifier.substring(identifier.indexOf('#')+1);
  			}
  		}
  		String instanceNameActionType = ""; 
  		Set <Value>valuesActionType = instance.listAttributeValues(loadIRI(BrokerConstants.conceptProcessActionType));
  		for (Value value: valuesActionType)
  		{
  			if(value instanceof Instance){
  				String identifier = ((Instance)value).getIdentifier().toString(); 
  				instanceNameActionType = identifier.substring(identifier.indexOf('#')+1);
  			}
  		}
  		
  		String instanceNameUser = ""; 
  		Set <Value>valuesUser = instance.listAttributeValues(loadIRI(BrokerConstants.conceptProcessUser));
  		for (Value value: valuesUser)
  		{
  			if(value instanceof Instance){
  				String identifier = ((Instance)value).getIdentifier().toString(); 
  				instanceNameUser = identifier.substring(identifier.indexOf('#')+1);
  			}
  		}
  		
  		String amount = ""; 
  		Set <Value>valuesAmount = instance.listAttributeValues(loadIRI(BrokerConstants.conceptProcessAmount));
  		for (Value value: valuesAmount)
  		{
  			if(value instanceof DataValue){
  				amount = ((DataValue)value).getValue().toString();
  			}
  		}
  		
		if (id.toString().contains(BrokerConstants.Bankinter))
		{
			Instance stockInstance = ontology.findInstance(loadIRI(BrokerConstants.stockMarketInstancesNS+instanceNameStock));
			Instance actionTypeInstance = ontology.findInstance(loadIRI(BrokerConstants.stockMarketInstancesNS+instanceNameActionType));
			//Instance userInstance = ontology.findInstance(loadIRI(BrokerConstants.stockMarketInstances+instanceNameUser));
			
			/*String portfolio="";
			Set<Value> portfolioValues = userInstance.listAttributeValues(loadIRI(BrokerConstants.attributeIdPortfolio));
			for (Value value : portfolioValues) {
				if(value instanceof DataValue){
					portfolio = ((DataValue) value).getValue().toString();
				}
			}*/
			
			String marketCode = getAttributeValue(marketInstance, BrokerConstants.attributeMarketCode).toString();
			String stockISIN = getAttributeValue(stockInstance, BrokerConstants.attributeStockISIN).toString();
			//String user = getAttributeValue(userInstance, BrokerConstants.attributeUser).toString();
			String actionType = getAttributeValue(actionTypeInstance, BrokerConstants.attributeActionType).toString();
			
			result += "<performBuySell xmlns=\"https://aia.ebankinter.com/wsBrokerService/\">";
			result += "<isin>"+stockISIN+"</isin>";
			result += "<market>"+marketCode+"</market>";
			result += "<numStocks>"+amount+"</numStocks>";
			result += "<buyOrSell>"+actionType+"</buyOrSell>";
			result += "<portFolioId>p1</portFolioId>";
			result += "<userID>ozelin</userID>";
			result += "</performBuySell>";
		}
		return result;
	}
	
	public String getGetNews(Instance instance, Identifier id)
	{
		String result = "";
		String instanceName = ""; 
		Set <Value>values = instance.listAttributeValues(loadIRI(BrokerConstants.conceptProcessStock));
  		for (Value value: values)
  		{
  			if(value instanceof Instance){
  				String identifier = ((Instance)value).getIdentifier().toString(); 
  	  			instanceName = identifier.substring(identifier.indexOf('#')+1);
  			}
  			
  		}
		Ontology ontology = loadOntology(BrokerConstants.stockMarketInstancesOntoIRI);
		Instance marketInstance = ontology.findInstance(loadIRI(BrokerConstants.nasdaq));
		Instance stockInstance = ontology.findInstance(loadIRI(BrokerConstants.stockMarketInstancesNS+instanceName));

		String stockTicker = getAttributeValue(stockInstance, BrokerConstants.attributeStockTicker).toString();
		if (id.toString().contains(BrokerConstants.Bankinter))
		{
			String marketCode = getAttributeValue(marketInstance, BrokerConstants.attributeMarketCode).toString();
			
			String stockISIN = getAttributeValue(stockInstance, BrokerConstants.attributeStockISIN).toString();
			
			result += "<cotizacionIndice xmlns=\"https://aia.ebankinter.com/wsBrokerService/\">";
			result += "<isin>"+stockISIN+"</isin>";
			result += "<market>"+marketCode+"</market>";
			result += "</cotizacionIndice>";
		}
		if (id.toString().contains(BrokerConstants.XIgnite))
		{
			result += "<tns:GetStockHeadlines xmlns:tns=\"http://www.xignite.com/services/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">";
			result+= "<tns:Symbols xsi:type=\"xsd:string\">"+stockTicker+"</tns:Symbols>";
			result += "<tns:HeadlineCount xsi:type=\"xsd:int\">1</tns:HeadlineCount>";
			result+= "</tns:GetStockHeadlines>";
		}
		return result;
	}
	
	public String getSendAlert(Instance instance, Identifier id)
	{
		String result = "";
		String instanceNameUser = ""; 
		Set <Value>values = instance.listAttributeValues(loadIRI(BrokerConstants.conceptProcessUser));
  		for (Value value: values)
  		{
  			if(value instanceof Instance){
  				String identifier = ((Instance)value).getIdentifier().toString(); 
  				instanceNameUser = identifier.substring(identifier.indexOf('#')+1);
  			}  			
  		}
  		Ontology ontology = loadOntology(BrokerConstants.stockMarketInstancesOntoIRI);
  		Instance userInstance = ontology.findInstance(loadIRI(BrokerConstants.stockMarketInstancesNS+instanceNameUser));
		
		String mobileNumber="";
		Set<Value> mobileNumberValues = userInstance.listAttributeValues(loadIRI(BrokerConstants.attributeMobileNumber));
		for (Value value : mobileNumberValues) {
			if(value instanceof DataValue){
				mobileNumber = ((DataValue) value).getValue().toString();
			}
		}
		
		if (id.toString().contains(BrokerConstants.StrikeIron))
		{
			
			result += "<si:SendMessage  xmlns:si=\"http://www.strikeiron.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
			result += "<si:ToNumber xsi:type=\"xsd:string\">34646965630</si:ToNumber>";
			result += "<si:FromNumber xsi:type=\"xsd:string\">900601601</si:FromNumber>";
			result += "<si:FromName xsi:type=\"xsd:string\">Stockbroker</si:FromName>";
			result += "<si:MessageText xsi:type=\"xsd:string\">Las condiciones de tu alerta se han cumplido</si:MessageText>";
			result +="</si:SendMessage>";
		}		
		return result;
	}
	
	
	public String getGetRecommendationXML(Instance instance, Identifier id)
	{
		String result = "";
		String instanceName = ""; 
		Set <Value>values = instance.listAttributeValues(loadIRI(BrokerConstants.conceptProcessStock));
  		for (Value value: values)
  		{
  			if(value instanceof Instance){
  				String identifier = ((Instance)value).getIdentifier().toString(); 
  	  			instanceName = identifier.substring(identifier.indexOf('#')+1);
  			}
  		}
		Ontology ontology = loadOntology(BrokerConstants.stockMarketInstancesOntoIRI);
		
		Instance stockInstance = ontology.findInstance(loadIRI(BrokerConstants.stockMarketInstancesNS+instanceName));
		Instance marketInstance = ontology.findInstance(loadIRI(BrokerConstants.nasdaq));

		String marketCode = getAttributeValue(marketInstance, BrokerConstants.attributeMarketCode).toString();
		String stockISIN = getAttributeValue(stockInstance, BrokerConstants.attributeStockISIN).toString();
		result += "<getRecommendations xmlns=\"https://aia.ebankinter.com/wsBrokerService/\">";
		result += "<isin>"+stockISIN+"</isin>";
		result += "<market>"+marketCode+"</market>";
		result += "</getRecommendations>";
		return result;
	}
	
    
    public SOAPElement getHeader(Instance theInst, Identifier id){
        
        try {
        	SOAPElement soapElement = null;
        	
            String iri = theInst.getIdentifier().toString();
            boolean a = false; 
            if (id.toString().contains(BrokerConstants.XIgnite))
            {            	
            	SOAPFactory soapFactory = SOAPFactory.newInstance();
    			Name header = soapFactory.createName("Header", "s1","http://www.xignite.com/services/");
    			Name username = soapFactory.createName("Username", "s1","http://www.xignite.com/services/");
    			Name password = soapFactory.createName("Password", "s1","http://www.xignite.com/services/");
    			Name tracer = soapFactory.createName("Tracer", "s1","http://www.xignite.com/services/");
    			
    			SOAPElement headerElem = soapFactory.createElement(header);
    			SOAPElement usernameElem = soapFactory.createElement(username);
    			SOAPElement passwordElem = soapFactory.createElement(username);
    			SOAPElement tracerElem = soapFactory.createElement(tracer);
    			
    			passwordElem.addTextNode("password");
    			usernameElem.addTextNode("ozelin@isoco.com");
    			tracerElem.addTextNode("");

    			headerElem.appendChild(usernameElem);
    			headerElem.appendChild(passwordElem);
    			headerElem.appendChild(tracerElem);
    			
            	soapElement = headerElem;
                a = true;       
            }
            if (id.toString().contains(BrokerConstants.StrikeIron))
            {
    			SOAPFactory soapFactory = SOAPFactory.newInstance();

    			Name licensceInfoName = soapFactory.createName("LicenseInfo", "s1", "http://ws.strikeiron.com");
    			Name registredUserName = soapFactory.createName("RegisteredUser");
    			Name userIDName = soapFactory.createName("UserID", "s1", "http://ws.strikeiron.com");
    			Name passwordName = soapFactory.createName("Password", "s1", "http://ws.strikeiron.com");

    			SOAPElement licensceInfoElem = soapFactory.createElement(licensceInfoName);
    			SOAPElement registredUserElem = soapFactory.createElement(registredUserName);
    			SOAPElement userIDElem = soapFactory.createElement(userIDName);
    			SOAPElement passwordElem = soapFactory.createElement(passwordName);
    			
    			userIDElem.addTextNode("ozelin@isoco.com");
    			passwordElem.addTextNode("plinplin");
    			
    			registredUserElem.appendChild(userIDElem);			
    			registredUserElem.appendChild(passwordElem);
    			licensceInfoElem.appendChild(registredUserElem);

    			soapElement = licensceInfoElem; 
                a = true;  
            }  
            
            
            return soapElement;
            
        } catch (Exception e) {
            logger.error("Error in processing document");           
            return null;
        }
    }
    
    public Ontology loadOntology(String ontIRI)
    {
    	return Helper.getOntology(ontIRI);
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
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint) {
						
		Instance instResult=null;
		try {
			//			Define a namespaces used in response
			Iterator itera;

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
		        addNamespaces(this.nsContext, element);
		    }
		    
			} catch (SOAPException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();		
			InputSource is = new InputSource(new StringReader(document));
			Document doc = builder.parse(is);
			String inputMessageID ="bankinterQuote";
			DataFactory dataFactory = Factory.createDataFactory(null);
			//TODO ver como identificamos instancia de mappeo
			Ontology temp = this.createTempOntology();
			ArrayList<Instance> instancesToTranslate = getInstanceToTransalate(doc);
			for (Instance instanceToTranslate : instancesToTranslate) {
				
			
			//Set <Value> values = instanceToTranslate.listAttributeValues(loadIRI(BrokerConstants.attributeInputMessage));
			ArrayList<Triple<String, String, String>> instanceMapps= getInstanceMappingsValues(instanceToTranslate);
			ArrayList<Pair<String, String>> valueMapps = getValuesMappingsValues(instanceToTranslate);
			instResult = cretateInstance(getTargetConcept(instanceToTranslate));
			Ontology stockMarketOntology = loadOntology(BrokerConstants.stockMarketOntoIRI);
			Ontology stockMarketInstances = loadOntology(BrokerConstants.stockMarketInstancesOntoIRI);
			WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);
			for (Pair<String, String> pair : valueMapps) {
				XPath xpath = new DOMXPath(pair.getFirst());
				xpath.setNamespaceContext(this.nsContext);
				List<Node> nodes = xpath.selectNodes(doc);
				IRI attributeIRI = wsmoFactory.createIRI(stockMarketOntology.getDefaultNamespace(), pair.getSecond());

				try {
					instResult.addAttributeValue(attributeIRI, dataFactory
							.createWsmlString(nodes.get(0).getTextContent()));
				} catch (IndexOutOfBoundsException e) {
					logger.error("Error in expression "+ pair.getFirst());
				}				
			}
			for (Triple<String,String,String> triple : instanceMapps) {
				XPath xpath = new DOMXPath(triple.getFirst());
				xpath.setNamespaceContext(nsContext);
				List<Node> nodes = xpath.selectNodes(doc);
				IRI storedInstanceIRI = wsmoFactory.createIRI(stockMarketOntology.getDefaultNamespace(), triple.getSecond());
				Instance storedInstance = findInstance(stockMarketInstances, storedInstanceIRI, nodes.get(0).getTextContent());
				IRI attributeIRI = wsmoFactory.createIRI(stockMarketOntology.getDefaultNamespace(), triple.getThird());
				instResult.addAttributeValue(attributeIRI,storedInstance);				
			}
			temp.addInstance(instResult);
			}

			return new WSMLDocument(writeOntology(temp));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JaxenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
		
		
	}
	
	
	public ArrayList<Instance> getInstanceToTransalate(Document soapMessage){
		
		Ontology adapterOntology = loadOntology(BrokerConstants.adapterOntoIRI);
		Set<Instance> instances = adapterOntology.listInstances();
		
		ArrayList<Instance> instacesReturn = new ArrayList<Instance>();
		
    	for (Instance instance: instances)
    	{
    		Set <Value>values = instance.listAttributeValues(loadIRI(BrokerConstants.attributeInputMessage));
    		instance.getIdentifier();
    		for (Value value: values)
    		{
    			List<Node> nodes;
				try {
					
					XPath xpath = new DOMXPath(value.toString());
					xpath.setNamespaceContext(this.nsContext);
					logger.debug(value.toString());
					nodes = xpath.selectNodes(soapMessage);
					logger.debug(nodes.size());
	    			if (nodes.size()>0)
	    				instacesReturn.add(instance);
	    				 						
				} catch (JaxenException e) {
					logger.error("Error in:" + instance.getIdentifier().toString());
				}
    		}
    		
    	}
		return instacesReturn;

	}
	

	public ArrayList<Triple<String, String,String>> getInstanceMappingsValues(Instance insts){
		Ontology adapterOntology = loadOntology(BrokerConstants.adapterOntoIRI);
		ArrayList<Triple<String, String,String>> result =new  ArrayList<Triple<String, String,String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(BrokerConstants.attributeInstanceMappings));
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
				logger.error("Error in getInstanceMapping");
	  		  	}	
		 	}
	  	  return result;
	}		

	public String getTargetConcept(Instance insts){
		Ontology adapterOntology = loadOntology(BrokerConstants.adapterOntoIRI);
		ArrayList<Triple<String, String,String>> result =new  ArrayList<Triple<String, String,String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(BrokerConstants.attributeConceptOutput));
		for (Value value : values) {
			return value.toString();
		 	}
		
	  	  return null;
		
	}		
	
	
	public Instance cretateInstance(String conceptName){
		Ontology stockMarketOntology = loadOntology(BrokerConstants.stockMarketOntoIRI);
		Ontology stockMarketProcess = loadOntology(BrokerConstants.stockMarketProcessOntoIRI);
		
		Concept concept = loadConcept(stockMarketOntology, BrokerConstants.stockMarketNS+ conceptName);
		if (concept ==null )
			concept = loadConcept(stockMarketProcess, BrokerConstants.stockMarketProcessNS+conceptName);
			

		WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);

		IRI instanceIRI = wsmoFactory.createIRI(stockMarketOntology.getDefaultNamespace(), "tempInst"+(new Long ((new Date()).getTime())).toString());
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
		  Concept concept = null;
		  concept = ont.findConcept(loadIRI(value));
		  return concept;
	  }	
	public ArrayList<Pair<String, String>> getValuesMappingsValues(Instance insts){
		
		ArrayList<Pair<String, String>> result =new  ArrayList<Pair<String, String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(BrokerConstants.attributeValueMappings));
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
				logger.error("Error in adding expression");
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
            IRI irism = wsmoFactory.createIRI(BrokerConstants.stockMarketNS);
            IRI irismp = wsmoFactory.createIRI(BrokerConstants.stockMarketProcessNS);
            IRI irismpi	= wsmoFactory.createIRI(BrokerConstants.stockMarketInstancesNS);
            Namespace smNamespace = wsmoFactory.createNamespace("sm",irism);
            Namespace smpNamespace = wsmoFactory.createNamespace("smp", irismp); 
            Namespace smpiNamespace = wsmoFactory.createNamespace("smpi", irismpi);
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
