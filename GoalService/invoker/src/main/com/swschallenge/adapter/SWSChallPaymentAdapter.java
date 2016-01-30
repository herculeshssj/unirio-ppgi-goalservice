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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.wsmo.service.WebService;
import org.wsmo.wsml.Serializer;
import org.xml.sax.InputSource;

/**
 * 
 * @author based on DIP eBanking adapter by Silverster, Ozelin, Laurent, 
 * 		   further developed by Maciej Zaremba
 *
 */

public class SWSChallPaymentAdapter extends Adapter{
	protected static Logger logger = Logger.getLogger(SWSChallPaymentAdapter.class);
	WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);
	DataFactory dataFactory = Factory.createDataFactory(null);

	public SWSChallPaymentAdapter() {
		super();
	}

	public org.w3c.dom.Document getXML (List<Entity> instances, String id)
	{
		logger.debug("Get Instances, ID: "+id);
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
					
					if (conceptName.equalsIgnoreCase(SWSChallPaymentAdapterConstants.moonOntoNS+"BankingInformationRequest"))
					{
						sDoc = getBankingInformationRequestXML(instance, id);
						found = true;
					}
					else if (conceptName.equalsIgnoreCase(SWSChallPaymentAdapterConstants.paymentOntoNS+"PaymentAuthorizationMDRequest")||
							conceptName.equalsIgnoreCase(SWSChallPaymentAdapterConstants.paymentOntoNS+"PaymentInitiationFDRequest"))
					{
						sDoc = getUNIFIReqXML(instance, id);
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
	
	public String getBankingInformationRequestXML(Instance instance, String id)
	{
		String result = "";
				
		String token = Helper.getInstanceAttribute(instance, loadIRI(SWSChallPaymentAdapterConstants.moonOntoNS+"hasRequestId"));
		
		if (id.toString().contains(SWSChallPaymentAdapterConstants.WSMoonFIPRequest))
		{
			result+= "<BankingInformationRequest xmlns=\"mooncompany\">";
			result+= "   <requestId>"+token+"</requestId>";
			result+= "</BankingInformationRequest>";
		}
		return result;
	}
	
	public String getUNIFIReqXML(Instance instance, String id)
	{
		String res = "";
		String ns = SWSChallPaymentAdapterConstants.paymentOntoNS;
		
		Instance header					= Helper.getInstanceAttribute(instance, loadIRI(ns+"hasHeader"), loadIRI(ns+"Header"));
		Instance payInf 				= Helper.getInstanceAttribute(instance, loadIRI(ns+"hasPaymentInformation"),loadIRI(ns+"PaymentInformation"));

		Instance debtor 				= Helper.getInstanceAttribute(payInf, 	loadIRI(ns+"hasDebtor"),loadIRI(ns+"PaymentPartner"));
		Instance debtorAddr				= Helper.getInstanceAttribute(debtor, 	loadIRI(ns+"hasAddress"),loadIRI(ns+"PostalAddress"));
		Instance debtorAccount			= Helper.getInstanceAttribute(payInf, 	loadIRI(ns+"hasDebtorAccount"),loadIRI(ns+"PaymentPartnerAccount"));
		Instance debtorAgent			= Helper.getInstanceAttribute(payInf, 	loadIRI(ns+"hasDebtorAgent"),loadIRI(ns+"PaymentPartnerAgent"));

		Instance creditTTI				= Helper.getInstanceAttribute(payInf, 	loadIRI(ns+"hasCreditTransferTransactionInformation"),loadIRI(ns+"CreditTransferTransactionInformation"));
		
		Instance creditor				= Helper.getInstanceAttribute(creditTTI,loadIRI(ns+"hasCreditor"),loadIRI(ns+"PaymentPartner"));
		Instance creditorAddr			= Helper.getInstanceAttribute(creditor,	loadIRI(ns+"hasAddress"),loadIRI(ns+"PostalAddress"));
		Instance creditorAccount		= Helper.getInstanceAttribute(creditTTI,loadIRI(ns+"hasCreditorAccount"),loadIRI(ns+"PaymentPartnerAccount"));
		Instance creditorAgent			= Helper.getInstanceAttribute(creditTTI,loadIRI(ns+"hasCreditorAgent"),loadIRI(ns+"PaymentPartnerAgent"));

		res+= "   <iso20022:pain.001.001.02>";
		res+= "   <iso20022:GrpHdr>";
		res+= "     <iso20022:MsgId>"+Helper.getAttribute(header,loadIRI(ns+"hasMessageId"))+"</iso20022:MsgId>";
		res+= "     <iso20022:CreDtTm>"+Helper.getAttribute(header,loadIRI(ns+"hasCreationDateTime"))+"</iso20022:CreDtTm>";
		res+= "     <iso20022:NbOfTxs>"+Helper.getAttribute(header,loadIRI(ns+"hasNumberOfTransactions"))+"</iso20022:NbOfTxs>";
		res+= "     <iso20022:CtrlSum>"+Helper.getAttribute(header,loadIRI(ns+"hasContolSum"))+"</iso20022:CtrlSum>";
		res+= "     <iso20022:Grpg>"+Helper.getAttribute(header,loadIRI(ns+"hasGrouping"))+"</iso20022:Grpg>";
		res+= "   </iso20022:GrpHdr>";
		
		res+= "   <iso20022:PmtInf>";
		res+= "     <iso20022:PmtMtd>"+Helper.getAttribute(payInf,loadIRI(ns+"hasPaymentMethod"))+"</iso20022:PmtMtd>";
		res+= "     <iso20022:ReqdExctnDt>"+Helper.getAttribute(payInf,loadIRI(ns+"hasRequestedExecutionDate"))+"</iso20022:ReqdExctnDt>";

		res+= "     <iso20022:Dbtr>";
		res+= "       <iso20022:Nm>"+Helper.getAttribute(debtor,loadIRI(ns+"hasName"))+"</iso20022:Nm>";
		res+= "       <iso20022:PstlAdr>";
		res+= "           <iso20022:StrtNm>"+Helper.getAttribute(debtorAddr,loadIRI(ns+"hasStreetName"))+"</iso20022:StrtNm>";
		res+= "           <iso20022:PstCd>"+Helper.getAttribute(debtorAddr,loadIRI(ns+"hasPostalCode"))+"</iso20022:PstCd>";
		res+= "           <iso20022:TwnNm>"+Helper.getAttribute(debtorAddr,loadIRI(ns+"hasTownName"))+"</iso20022:TwnNm>";
		res+= "           <iso20022:Ctry>"+Helper.getAttribute(debtorAddr,loadIRI(ns+"hasCountry"))+"</iso20022:Ctry>";
		res+= "       </iso20022:PstlAdr>";
		res+= "       <iso20022:CtryOfRes>"+Helper.getAttribute(debtor,loadIRI(ns+"hasCountryOfResidence"))+"</iso20022:CtryOfRes>";
		res+= "     </iso20022:Dbtr>";

		res+= "     <iso20022:DbtrAcct>";
		res+= "       <iso20022:Id>";
		res+= "         <iso20022:IBAN>"+Helper.getAttribute(debtorAccount,loadIRI(ns+"hasIBAN"))+"</iso20022:IBAN>";
		res+= "       </iso20022:Id>";
		res+= "       <iso20022:Tp>";
		res+= "         <iso20022:Cd>"+Helper.getAttribute(debtorAccount,loadIRI(ns+"hasCode"))+"</iso20022:Cd>";
		res+= "       </iso20022:Tp>";
		res+= "       <iso20022:Ccy>"+Helper.getAttribute(debtorAccount,loadIRI(ns+"hasCurrency"))+"</iso20022:Ccy>";
		res+= "     </iso20022:DbtrAcct>";

		res+= "     <iso20022:DbtrAgt>";
		res+= "       <iso20022:FinInstnId>";
		res+= "         <iso20022:BIC>"+Helper.getAttribute(debtorAgent,loadIRI(ns+"hasBIC"))+"</iso20022:BIC>";
		res+= "       </iso20022:FinInstnId>";
		res+= "     </iso20022:DbtrAgt>";

		res+= "     <iso20022:CdtTrfTxInf>";
		res+= "       <iso20022:Amt>";
		res+= "         <iso20022:InstdAmt>";
		res+= "           <iso20022:Ccy>"+Helper.getAttribute(creditTTI,loadIRI(ns+"hasCurrency"))+"</iso20022:Ccy>";
		res+= "           <iso20022:Amount>"+Helper.getAttribute(creditTTI,loadIRI(ns+"hasAmount"))+"</iso20022:Amount>";
		res+= "         </iso20022:InstdAmt>";
		res+= "       </iso20022:Amt>";

		res+= " 	  <iso20022:CdtrAgt>";
		res+= "         <iso20022:FinInstnId>";
		res+= "           <iso20022:BIC>"+Helper.getAttribute(creditorAgent,loadIRI(ns+"hasBIC"))+"</iso20022:BIC>";
		res+= "         </iso20022:FinInstnId>";
		res+= "       </iso20022:CdtrAgt>";
		
		res+= "       <iso20022:CdtrAgtAcct>";
		res+= "         <iso20022:Id>";
		res+= "           <iso20022:IBAN>"+Helper.getAttribute(creditorAccount,loadIRI(ns+"hasIBAN"))+"</iso20022:IBAN>";
		res+= "         </iso20022:Id>";
		res+= "         <iso20022:Tp>";
		res+= "           <iso20022:Cd>"+Helper.getAttribute(creditorAccount,loadIRI(ns+"hasCode"))+"</iso20022:Cd>";
		res+= "         </iso20022:Tp>";
		res+= "         <iso20022:Ccy>"+Helper.getAttribute(creditorAccount,loadIRI(ns+"hasCurrency"))+"</iso20022:Ccy>";
		res+= "       </iso20022:CdtrAgtAcct>";
		
		res+= "       <iso20022:Cdtr>";
		res+= "         <iso20022:Nm>"+Helper.getAttribute(creditor,loadIRI(ns+"hasName"))+"</iso20022:Nm>";
		res+= "         <iso20022:PstlAdr>";
		res+= "             <iso20022:StrtNm>"+Helper.getAttribute(creditorAddr,loadIRI(ns+"hasStreetName"))+"</iso20022:StrtNm>";
		res+= "             <iso20022:PstCd>"+Helper.getAttribute(creditorAddr,loadIRI(ns+"hasPostalCode"))+"</iso20022:PstCd>";
		res+= "             <iso20022:TwnNm>"+Helper.getAttribute(creditorAddr,loadIRI(ns+"hasTownName"))+"</iso20022:TwnNm>";
		res+= "             <iso20022:Ctry>"+Helper.getAttribute(creditorAddr,loadIRI(ns+"hasCountry"))+"</iso20022:Ctry>";
		res+= "         </iso20022:PstlAdr>";
		res+= "         <iso20022:CtryOfRes>"+Helper.getAttribute(creditor,loadIRI(ns+"hasCountryOfResidence"))+"</iso20022:CtryOfRes>";
		res+= "       </iso20022:Cdtr>";
		res+= "     </iso20022:CdtTrfTxInf>";		
		res+= "   </iso20022:PmtInf>";
		res+= " </iso20022:pain.001.001.02>";

		if (id.toString().contains(SWSChallPaymentAdapterConstants.WSBlueMDRequest)){
		res=  "<AuthorizationRequest xmlns:iso20022=\"http://www.sws-challenge.org/schemas/iso20022\""+
		      " xmlns=\"http://www.sws-challenge.org/schemas/ManagementDepartment\""+
		      " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"+
		      "  <Authority>" +
		      "    <FirstName>"+Helper.getAttribute(instance,loadIRI(ns+"hasAuthorityFirstname"))+"</FirstName>"+
		      "    <LastName>"+Helper.getAttribute(instance,loadIRI(ns+"hasAuthorityLastname"))+"</LastName>"+
		      "  </Authority>"+
		      res + 
		      "</AuthorizationRequest>";
		} else if (id.toString().contains(SWSChallPaymentAdapterConstants.WSBlueFDRequest)){
			res=  "<fin:PaymentInitiationRequest " +
				  " xmlns:fin=\"http://www.sws-challenge.org/schemas/FinancialDepartment\""+ 
				  " xmlns:iso20022=\"http://www.sws-challenge.org/schemas/iso20022\">" +
		      "  <fin:AuthorizationCode>"+Helper.getAttribute(instance,loadIRI(ns+"hasAuthorizationCode"))+ "</fin:AuthorizationCode>" +
		      res + 
		      "  </fin:PaymentInitiationRequest>";
		}
		return res;
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
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint, WebService webservice) {
		Instance instResult=null;
		try {
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
		    
			nsContext.addNamespace("ns1", "mooncompany" );
			nsContext.addNamespace("ns2", "http://www.sws-challenge.org/schemas/ManagementDepartment");
			nsContext.addNamespace("ns3", "http://www.sws-challenge.org/schemas/FinancialDepartment");
			nsContext.addNamespace("ns4", "http://www.sws-challenge.org/schemas/iso20022");
		    
			} catch (SOAPException e1) {
				e1.printStackTrace();
			}			
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();		
			InputSource is = new InputSource(new StringReader(document));
			Document doc = builder.parse(is);
			
			Ontology respOnto = this.createTempOntology();
			
			ArrayList<Instance> instancesToTranslate = getInstanceToTransalate(doc, nsContext, SWSChallPaymentAdapterConstants.paymentAdapterNS+"xml2wsmlmapping");
			for (Instance instanceToTranslate : instancesToTranslate) {
				
			//Set <Value> values = instanceToTranslate.listAttributeValues(loadIRI(SWSChallConstants.attributeInputMessage));
				ArrayList<Pair<String, String>> valueMapps = getValuesMappingsValues(instanceToTranslate);
				instResult = createInstance(getTargetConcept(instanceToTranslate));
				for (Pair<String, String> pair : valueMapps) {
					XPath xpath = new DOMXPath(pair.getFirst());
					xpath.setNamespaceContext(nsContext);
					List<Node> nodes = xpath.selectNodes(doc);
					if (nodes.size() > 0)
						createAttribute(instResult, pair.getSecond(), nodes.get(0).getTextContent(), doc, nsContext, respOnto);
				}
				respOnto.addInstance(instResult);
			}
			 
			String strTemp = writeOntology(respOnto);
			//clear this ontology
			for (Instance i : (Set<Instance>)respOnto.listInstances() ){
				Map attr = i.listAttributeValues();
				Set<IRI> keys = attr.keySet();
				
				for (IRI iri : keys){
					Object values = attr.get(iri);
					i.removeAttributeValues(iri);
				}
			}		
			respOnto.removeOntology(respOnto);
						
			return new WSMLDocument(strTemp);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Instance generateInstance(String conceptId, Document doc, SimpleNamespaceContext docNSs, Ontology respOnto){
		Instance instResult = null;
		
		Ontology adapterOntology = Helper.getOntology(SWSChallPaymentAdapterConstants.paymentAdapterIRI);
		
		Set<Entity> entities = 	Helper.getInstancesOfConceptAsSet(new HashSet<Entity>(Helper.getInstances(adapterOntology)), 
								SWSChallPaymentAdapterConstants.paymentAdapterNS+"generateInstance");
		
		Instance mappingInstance = null;
		
		for (Entity e: entities)
		{
			String outputAttrValue = Helper.getAttribute((Instance) e, SWSChallPaymentAdapterConstants.paymentAdapterNS+"conceptOutput");
			if (conceptId.equals(outputAttrValue)){
				mappingInstance = (Instance) e;
				break;
			}
		}
		if (mappingInstance == null)
			return null;
		
		ArrayList<Pair<String, String>> valueMapps = getValuesMappingsValues(mappingInstance);
		instResult = createInstance(getTargetConcept(mappingInstance));
		for (Pair<String, String> pair : valueMapps) {
			XPath xpath;
			try {
				xpath = new DOMXPath(pair.getFirst());
				xpath.setNamespaceContext(docNSs);
				List<Node> nodes = xpath.selectNodes(doc);
				if (nodes.size() > 0)
					createAttribute(instResult, pair.getSecond(), nodes.get(0).getTextContent(), doc, docNSs, respOnto);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		return instResult;
	}
	

	private void createAttribute(Instance inst, String attrName, String value, Document doc, SimpleNamespaceContext docNSs, Ontology respOnto) throws SynchronisationException, InvalidModelException{		
		String regExpression = "([^\\(]+) ( \\( (_string|_decimal|_fixed|_generateInstance) ( \\( ([^\\)]+) \\) )? \\) )?"; //|_fixed
		regExpression = regExpression.replaceAll(" ", "");
		
		logger.debug("Reg exp = " + regExpression);
		logger.debug("input = " + attrName);
		
		Pattern pattern = Pattern.compile(regExpression);
	    Matcher matcher = pattern.matcher(attrName);

	    String targetAttr = "", dataType = "", fixed = "";
	    if (matcher.find()) {
	    	targetAttr = matcher.group(1);
	    	dataType = matcher.group(3); 
	    	fixed = matcher.group(5);
	    }

		IRI attributeIRI = wsmoFactory.createIRI(targetAttr);

		try {
			if (dataType!=null && dataType.equals("_string")){
				inst.addAttributeValue(attributeIRI, dataFactory.createWsmlString(value));
			} else if (dataType!=null && dataType.equals("_generateInstance")){
				Instance newInst = generateInstance(fixed, doc, docNSs, respOnto);
				inst.addAttributeValue(attributeIRI,newInst);
				respOnto.addInstance(newInst);
			} else {			
				//try to create decimal
				Float tempFloat = new Float(value);
				inst.addAttributeValue(attributeIRI, dataFactory.createWsmlDecimal(""+tempFloat));
			}	
		} catch (Exception e){
			//add as string
			inst.addAttributeValue(attributeIRI, dataFactory.createWsmlString(value));
		}
	}
	
	public ArrayList<Instance> getInstanceToTransalate(Document soapMessage, SimpleNamespaceContext nsContext, String ofConcept){
		
		Ontology adapterOntology = Helper.getOntology(SWSChallPaymentAdapterConstants.paymentAdapterIRI);
		Set<Entity> entities = Helper.getInstancesOfConceptAsSet(new HashSet<Entity>(Helper.getInstances(adapterOntology)), ofConcept);

		ArrayList<Instance> instacesReturn = new ArrayList<Instance>();
		
    	for (Entity e: entities)
    	{
    		Instance instance = (Instance) e;
    		Set <Value>values = instance.listAttributeValues(loadIRI(SWSChallPaymentAdapterConstants.attributeInputMessage));
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
	    				 						
				} catch (JaxenException ex) {
					logger.error("Error in: " + instance.getIdentifier().toString());
				}
    		}
    	}
		return instacesReturn;
	}
	

	public String getTargetConcept(Instance insts){
		Set <Value> values = insts.listAttributeValues(loadIRI(SWSChallPaymentAdapterConstants.attributeConceptOutput));
		for (Value value : values) {
			return value.toString();
	 	}
		return null;
	}		
	
	
	public Instance createInstance(String conceptName){
		WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);

		IRI instanceIRI = wsmoFactory.createIRI("http://www.example.org/ontologies/example#inst"+Helper.getRandomLong());
        Instance instanceResult = null;
		try {
			instanceResult = wsmoFactory.createInstance(instanceIRI);
			instanceResult.addConcept(wsmoFactory.createConcept(wsmoFactory.createIRI(conceptName)));
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return instanceResult;
	}	
	
	public ArrayList<Pair<String, String>> getValuesMappingsValues(Instance insts){
		
		ArrayList<Pair<String, String>> result =new  ArrayList<Pair<String, String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(SWSChallPaymentAdapterConstants.attributeValueMappings));
		for (Value value : values) {
			String valueStr = value.toString();
			try{
				int index = valueStr.lastIndexOf("=");
				String first = valueStr.substring(0,index);
	  		  	String second = valueStr.substring(index+1, valueStr.length());
	  		  	
	  		  	Pair<String, String> tr = new Pair(first,second);
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
	        context.addNamespace( prefix, uri);
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
    
    public Ontology createTempOntology() {

        try {
            Ontology anOntology = wsmoFactory.createOntology(wsmoFactory.createIRI("http://www.example.org/ontologies/example#"+Helper.getRandomLong())); 	
            anOntology.addNamespace(wsmoFactory.createNamespace("pay",wsmoFactory.createIRI(SWSChallPaymentAdapterConstants.paymentOntoNS)));
            anOntology.addNamespace(wsmoFactory.createNamespace("moon",wsmoFactory.createIRI(SWSChallPaymentAdapterConstants.moonOntoNS)));
            anOntology.addNamespace(wsmoFactory.createNamespace("wsml", wsmoFactory.createIRI("http://www.wsmo.org/wsml/wsml-syntax#")));

            return anOntology;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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
	@Override
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint) {
		// TODO Auto-generated method stub
		return null;
	}
}
