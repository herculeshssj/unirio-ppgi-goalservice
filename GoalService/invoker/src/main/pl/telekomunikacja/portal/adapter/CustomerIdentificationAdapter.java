package pl.telekomunikacja.portal.adapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.wsmo.common.Identifier;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.deri.infrawebs.sfs.adapter.CarRentalServiceAdapter;
import org.deri.infrawebs.sfs.adapter.XMLTranslator;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Value;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.execution.common.nonwsmo.grounding.WSDL1_1EndpointGrounding;
import org.wsmo.execution.common.nonwsmo.grounding.WSDL1_1GroundingException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Interface;
import org.wsmo.service.WebService;
import org.wsmo.service.choreography.Choreography;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.In;
import org.wsmo.service.signature.NotGroundedException;
import org.wsmo.service.signature.WSDLGrounding;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ie.deri.wsmx.adapter.Adapter;
import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.invoker.Invoker;


public class CustomerIdentificationAdapter extends TP_Showcase_Adapter {
	
	static Logger logger = Logger.getLogger(CustomerIdentificationAdapter.class);
	public org.w3c.dom.Document getXML(List<Entity> instances, String id) {
		
		String customerName="";
		String customerID="";
		String caseID="";
		String caseOrderConfirmed="false";
		String caseContractPrintedOut="false";
		String caseContractConfirmedByCourier="false";
		
		String result = "";
		
		org.w3c.dom.Document doc=null;
		try {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			boolean found = false;
			
			logger.info("Instances: "+instances.size());
			
			for (Entity entity : instances) {
				if (! (entity instanceof Entity) )
					break;
				
				
				Instance instance = (Instance) entity;
				logger.info("Instance identifier: "+instance.getIdentifier().toString());
				
				
				Set <Concept> concepts = instance.listConcepts();
				
				logger.info("Instance data contains "+concepts.size()+" concept(s)");
				

				String conceptName;
				conceptName=concepts.iterator().next().getIdentifier().toString();
				if(true/*conceptName.contains("TP_Customer")*/){
					customerName=Helper.getInstanceAttribute(instance,loadIRI("http://org.ipsuper.composition.tp/tpOntology#hasName"));
					customerID=Helper.getInstanceAttribute(instance,loadIRI("http://org.ipsuper.composition.tp/tpOntology#hasCustomerID"));
				}
				
			}
			// 
			
			result += "<identifyCustomer xmlns='http://crm.portal.telekomunikacja.pl/xsd'>" +"\n";
			result += " <customerLastName >"+customerName+"</customerLastName>"+"\n";
			result += "</identifyCustomer>";
			
			//result += "<q0:MCustomerIdentificationRequestElement xmlns:q0=\""+TP_Showcase_Constants.getWebServiceTypesNamespace("customerIdentification")+"\">"; 
			//result += "<q0:customerLastName>"+customerName + "</q0:customerLastName>";
			//result += "  <q0:customerName>"+ customerName + "</q0:customerName>";
			//result += "</q0:MCustomerIdentificationRequestElement>";
			
			/*
	    	result="";
	    	result += "<ns1:identifyCustomer xmlns='http://crm.portal.telekomunikacja.pl/xsd' xmlns:ns1='http://crm.portal.telekomunikacja.pl/xsd'>" +"\n";
			result += " <ns1:customerLastName >"+"Laurie"+"</ns1:customerLastName>"+"\n";
			result += "</ns1:identifyCustomer>";*/
			//result = "<q0:MCustomerIdentificationRequestElement xmlns:q0=\"http://crm.portal.telekomunikacja.pl\"><q0:customerLastName>LaurieA</q0:customerLastName></q0:MCustomerIdentificationRequestElement>";
			System.out.println("SOAP REQUEST::: "+result);
			
			
			
			
			InputSource is = new InputSource(new StringReader(result));
			logger.info("Parsing started");
			doc = builder.parse(is);
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    	OutputFormat format = new OutputFormat(doc);
			format.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(stream, format);
			serializer.serialize(doc);
			System.out.println("::"+stream.toString());
			
	
		
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return doc;
	}
	
	

	
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint) {
		
		String xslFileName = "customerIdentification.xsl";
		
		
		WSMLDocument wsmlDocument = new WSMLDocument("");
		XMLTranslator translator = new XMLTranslator();
		InputStream xsltFile = null;
		try {

			xsltFile = new FileInputStream(TP_Showcase_Constants.xlstLocation
					+ xslFileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wsmlDocument.setContent(translator.doTransformation(document, xsltFile));
		return wsmlDocument;
	}
	
}
