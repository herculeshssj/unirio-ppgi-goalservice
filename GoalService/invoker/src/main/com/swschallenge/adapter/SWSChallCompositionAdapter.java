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

public class SWSChallCompositionAdapter extends Adapter{
	
	protected static Logger logger = Logger.getLogger(SWSChallCompositionAdapter.class);
	private WebService lastWebService;
	private Instance mainInstance;
	WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);
	DataFactory dataFactory = Factory.createDataFactory(null);
	/*
	public BrokerServiceAdapter(String id) {
		super(id);
	}*/
	public SWSChallCompositionAdapter() {
		super();
	}

	
	
	public org.w3c.dom.Document getXML (List<Entity> instances, String id)
	{
		logger.debug("enter in getXML");
		logger.debug("Set size: "+instances.size());
		logger.debug("ID: "+id);
		mainInstance = (Instance) instances.get(0);
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
					
					if (conceptName.equalsIgnoreCase(SWSChallCompositionConstants.productOntoNS+"Notebook") ||
						conceptName.equalsIgnoreCase(SWSChallCompositionConstants.productOntoNS+"WebCam") ||
						conceptName.equalsIgnoreCase(SWSChallCompositionConstants.productOntoNS+"DockingStation") ||
						conceptName.equalsIgnoreCase(SWSChallCompositionConstants.productOntoNS+"Accessory"))
						
						
					{
						sDoc = getProductOrderReqXML(instance, id);
						found = true;
					}
					else if (conceptName.equalsIgnoreCase(SWSChallCompositionConstants.productOntoNS+"ProductCategory")) {
						sDoc = getListProductsReqXML(instance, id);
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
	
	public String getProductOrderReqXML(Instance instance, String id)
	{
		String result = "";
				
		String gtin = Helper.getInstanceAttribute(instance, loadIRI(SWSChallCompositionConstants.productOntoNS+"GTIN"));
		
		if (id.toString().contains(SWSChallCompositionConstants.WSBargainerOrder) ||  
			id.toString().contains(SWSChallCompositionConstants.WSHawkerOrder) ||
			id.toString().contains(SWSChallCompositionConstants.WSRummageOrder) )
		{
			result+= "<q0:OrderRequest xmlns:q0=\"http://sws-challenge.org/shops/products/\" >";
			result+= "  <productId>"+gtin+"</productId>";
			result+= "  <addressInformation/>";
			result+= "</q0:OrderRequest>";
			
		} else if (id.toString().contains(SWSChallCompositionConstants.WSBargainerGet) ||  
				id.toString().contains(SWSChallCompositionConstants.WSHawkerGet) ||
				id.toString().contains(SWSChallCompositionConstants.WSRummageGet) )
		{
			
		}
		return result;
	}
	
	public String getListProductsReqXML(Instance instance, String id)
	{
		String result = "";

		String typeWSML = Helper.getInstanceAttribute(instance, loadIRI(SWSChallCompositionConstants.productOntoNS+"type"));

		String typeForXML = "";
		if (typeWSML.equals("Notebook"))
			typeForXML = "Notebook";
		else if (typeWSML.equals("WebCam"))
			typeForXML = "Web_cam";
		else if (typeWSML.equals("DockingStation"))
			typeForXML = "Docking_station";
		else if (typeWSML.equals("Accessory"))
			typeForXML = "Accessory";
		else if (typeWSML.equals("Networking"))
			typeForXML = "Networking";
		
		if (id.toString().contains(SWSChallCompositionConstants.WSBargainerGet) ||  
			id.toString().contains(SWSChallCompositionConstants.WSHawkerGet) ||
			id.toString().contains(SWSChallCompositionConstants.WSRummageGet) )
		{
			result+= "<q0:ProductCategory xmlns:q0=\"http://sws-challenge.org/shops/products/\" >";
			result+= typeForXML;
			result+= "</q0:ProductCategory>";
			
		} else if (id.toString().contains(SWSChallCompositionConstants.WSBargainerGet) ||  
				id.toString().contains(SWSChallCompositionConstants.WSHawkerGet) ||
				id.toString().contains(SWSChallCompositionConstants.WSRummageGet) )
		{
			
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
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint, WebService webservice) {
		lastWebService = webservice;
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
		    
			nsContext.addNamespace("ns1", "http://sws-challenge.org/shops/products/" );
		    
			} catch (SOAPException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();		
			InputSource is = new InputSource(new StringReader(document));
			Document doc = builder.parse(is);
			
			Ontology temp = this.createTempOntology();
			ArrayList<Instance> instancesToTranslate = getInstanceToTransalate(doc, nsContext);
			for (Instance instanceToTranslate : instancesToTranslate) {
				
			//Set <Value> values = instanceToTranslate.listAttributeValues(loadIRI(SWSChallConstants.attributeInputMessage));
			ArrayList<Pair<String, String>> valueMapps = getValuesMappingsValues(instanceToTranslate);
			instResult = createInstance(getTargetConcept(instanceToTranslate));
			Ontology productOntology = Helper.getOntology(SWSChallCompositionConstants.productOntoIRI);
			for (Pair<String, String> pair : valueMapps) {
				XPath xpath = new DOMXPath(pair.getFirst());
				xpath.setNamespaceContext(nsContext);
				List<Node> nodes = xpath.selectNodes(doc);
				if (nodes.size() > 0)
					createAttribute(instResult, productOntology, pair.getSecond(), nodes.get(0).getTextContent());
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
	

	private void createAttribute(Instance inst, Ontology onto, String attrName, String value) throws SynchronisationException, InvalidModelException{
//		String regExpression = "(https?://[.[^#]]+)#{1,1}wsdl\\.interfaceMessageReference\\((\\w+)/(\\w+)/(\\w+)\\)";
//		attrName = "name(_string)"; 
//		value = "name";//(_fixed(ontology))";
		
//		String regExpression = "(\\w)( \\(((_string)/(_decimal)/(_int) )\\))?";
		String regExpression = "([^\\(]+) ( \\( (_string|_decimal|_fixed) ( \\( ([^\\)]+) \\) )? \\) )?"; //|_fixed
		regExpression = regExpression.replaceAll(" ", "");
//		\\({1,1}([^\\)])+\\){1,1}))\\){1,1})
//				"?\\{1,1}";
//		((_string|_float))?";
		
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

		IRI attributeIRI = wsmoFactory.createIRI(onto.getDefaultNamespace(),  targetAttr);

		try {
			
			if (dataType!=null && dataType.equals("_fixed") && (fixed!=null)){
				Ontology tempO = Helper.getOntology("http://www.wsmo.org/sws-challenge-composition/ProductOntology#ProductOntology");
				Instance tempI = tempO.findInstance(wsmoFactory.createIRI("http://www.wsmo.org/sws-challenge-composition/ProductOntology#"+fixed));
				inst.addAttributeValue(attributeIRI, tempI);
			} else if (dataType!=null && dataType.equals("_string")){
				inst.addAttributeValue(attributeIRI, dataFactory.createWsmlString(value));
			} else {			
				//try to create Float
				Float tempFloat = new Float(value);
				inst.addAttributeValue(attributeIRI, dataFactory.createWsmlDecimal(""+tempFloat));
			}	
		} catch (Exception e){
			//add as string
			inst.addAttributeValue(attributeIRI, dataFactory.createWsmlString(value));
			if (targetAttr.equals("status"))
				inst.addNFPValue(wsmoFactory.createIRI("http://www.wsmo.org/sws-challenge-composition/invocationOf"), mainInstance.getIdentifier());
				inst.addNFPValue(wsmoFactory.createIRI("http://www.wsmo.org/sws-challenge-composition/webService"), lastWebService.getIdentifier());
		}
	}
	
	public ArrayList<Instance> getInstanceToTransalate(Document soapMessage, SimpleNamespaceContext nsContext){
		
		Ontology adapterOntology = Helper.getOntology(SWSChallCompositionConstants.productAdapterIRI);
		Set<Instance> instances = adapterOntology.listInstances();
		
		ArrayList<Instance> instacesReturn = new ArrayList<Instance>();
		
    	for (Instance instance: instances)
    	{
    		Set <Value>values = instance.listAttributeValues(loadIRI(SWSChallCompositionConstants.attributeInputMessage));
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
		Ontology adapterOntology = Helper.getOntology(SWSChallCompositionConstants.productAdapterIRI);
		ArrayList<Triple<String, String,String>> result =new  ArrayList<Triple<String, String,String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(SWSChallCompositionConstants.attributeInstanceMappings));
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
		Ontology adapterOntology = Helper.getOntology(SWSChallCompositionConstants.productOntoIRI);
		ArrayList<Triple<String, String,String>> result =new  ArrayList<Triple<String, String,String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(SWSChallCompositionConstants.attributeConceptOutput));
		for (Value value : values) {
			return value.toString();
		 	}
		
	  	  return null;
		
	}		
	
	
	public Instance createInstance(String conceptName){
		Ontology productOntology = Helper.getOntology(SWSChallCompositionConstants.productOntoIRI);
		
		Concept concept = loadConcept(productOntology, SWSChallCompositionConstants.productOntoNS+conceptName);

		WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);

		IRI instanceIRI = wsmoFactory.createIRI(productOntology.getDefaultNamespace(),"tempInst"+(new Long ((new Date()).getTime())).toString());
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
		Set <Value> values = insts.listAttributeValues(loadIRI(SWSChallCompositionConstants.attributeValueMappings));
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
            IRI iriso = wsmoFactory.createIRI(SWSChallCompositionConstants.productOntoNS);
            Namespace poNamespace = wsmoFactory.createNamespace("po",iriso);
            anOntology.addNamespace(wsmoFactory.createNamespace("wsml", wsmoFactory.createIRI("http://www.wsmo.org/wsml/wsml-syntax#")));
            anOntology.addNamespace(poNamespace);
            // 3.2 attach some NFPs

            
            // 3.2 adds some references to mediators and imported ontologies            

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
	/* (non-Javadoc)
	 * @see ie.deri.wsmx.adapter.Adapter#getWSML(java.lang.String, org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding)
	 */
	@Override
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint) {
		// TODO Auto-generated method stub
		return null;
	}




}
