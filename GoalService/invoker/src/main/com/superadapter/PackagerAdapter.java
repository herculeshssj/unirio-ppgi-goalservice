package com.superadapter;


import ie.deri.wsmx.adapter.Adapter;
import ie.deri.wsmx.commons.Helper;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.w3c.dom.DOMException;
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
import org.xml.sax.SAXException;

/**
 * 
 * @author based on DIP eBanking adapter by Silverster, Ozelin, Laurent, 
 * 		   further developed by Maciej Zaremba
 *
 */

public class PackagerAdapter extends Adapter{
	
	protected static Logger logger = Logger.getLogger(PackagerAdapter.class);
	private SimpleNamespaceContext nsContext = new SimpleNamespaceContext();

	public PackagerAdapter() {
		super();
	}

	
	
	public org.w3c.dom.Document getXML (List<Entity> instances, String id)
	{
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
					
					if (conceptName.equalsIgnoreCase(PackagerConstants.packagerOntoNS+"reqGetURL"))
					{
						sDoc = getRequestURLReqXML(instance, id);
						found = true;
					}
					if (found) break;
				}
				if (found) break;
			}
			
			InputSource is = new InputSource(new StringReader(sDoc));
			org.w3c.dom.Document doc = builder.parse(is);
			return doc;
		}
		catch (Exception e) {
			logger.error("Error in getXML");
			return null;
		}
	}


	
	

	
	public String getRequestURLReqXML(Instance instance, String id)
	{
		String result = "";
		
		String userID = Helper.getInstanceAttribute(instance, loadIRI(PackagerConstants.packagerOntoNS+"byUser"));
		String contents = Helper.getInstanceAttribute(instance, loadIRI(PackagerConstants.packagerOntoNS+"requestedContent"));
		
		if (id.toString().contains(PackagerConstants.WSPackagerRequest) )
		{
//			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:prer="">
//			   <soapenv:Header/>
//			   <soapenv:Body>
//			      <prer:generateURL>
//			         <!--Optional:-->
//			         <arg0>temp1</arg0>
//			         <!--Optional:-->
//			         <arg1>temp2</arg1>
//			      </prer:generateURL>
//			   </soapenv:Body>
//			</soapenv:Envelope>
			
			
			result+="<q0:generateURL xmlns:q0=\"http://ip-super.org/usecase/prereview/\">";
			result+="  <q0:arg0>"+userID+"</q0:arg0>";
			result+="  <q0:arg1>"+contents+"</q0:arg1>";
			result+="</q0:generateURL>";
		}
		return result;
	}
    
    public SOAPElement getHeader(List<Entity> instances, String id){
    	return null;
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
			//Define a namespaces used in response
			try {
				MessageFactory msgFactory = MessageFactory.newInstance();
				SOAPMessage message = msgFactory.createMessage();
				SOAPPart soapp = message.getSOAPPart();
				StreamSource source = new StreamSource(new StringReader(document));
				soapp.setContent(source);

				nsContext.addNamespace("ns", "http://ip-super.org/usecase/prereview/");
		    		    
			} catch (SOAPException e1) {
				e1.printStackTrace();
			}			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();		
			InputSource is = new InputSource(new StringReader(document));
			Document doc = builder.parse(is);
			
			DataFactory dataFactory = Factory.createDataFactory(null);
			Ontology temp = this.createTempOntology();
			ArrayList<Instance> instancesToTranslate = getInstanceToTransalate(doc);
			for (Instance instanceToTranslate : instancesToTranslate) {
				
			
			//Set <Value> values = instanceToTranslate.listAttributeValues(loadIRI(SWSChallConstants.attributeInputMessage));
			ArrayList<Triple<String, String, String>> instanceMapps= getInstanceMappingsValues(instanceToTranslate);
			ArrayList<Pair<String, String>> valueMapps = getValuesMappingsValues(instanceToTranslate);
			instResult = createInstance(getTargetConcept(instanceToTranslate));
			Ontology shipmentOntology = Helper.getOntology(PackagerConstants.packagerOntoIRI);
			WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);
			for (Pair<String, String> pair : valueMapps) {
				XPath xpath = new DOMXPath(pair.getFirst());
				xpath.setNamespaceContext(this.nsContext);
				List<Node> nodes = xpath.selectNodes(doc);
				IRI attributeIRI = wsmoFactory.createIRI(shipmentOntology.getDefaultNamespace(), pair.getSecond());

				try {
					String text = nodes.get(0).getTextContent();
					//try to create Double
					try {
						//try to create float value
						Float tempFloat = new Float(text);
						instResult.addAttributeValue(attributeIRI, dataFactory.createWsmlDecimal(""+tempFloat));
						
					} catch (Exception e){
						//add as string
						instResult.addAttributeValue(attributeIRI, dataFactory.createWsmlString(text));
					}
				} catch (IndexOutOfBoundsException e) {
					logger.error("Error en expression "+ pair.getFirst());
				}				
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
		Ontology adapterOntology = Helper.getOntology(PackagerConstants.packagerAdapterIRI);
		Set<Instance> instances = adapterOntology.listInstances();
		
		ArrayList<Instance> instacesReturn = new ArrayList<Instance>();
		
    	for (Instance instance: instances)
    	{
    		Set <Value>values = instance.listAttributeValues(loadIRI(PackagerConstants.attributeInputMessage));
    		instance.getIdentifier();
    		for (Value value: values)
    		{
    			List<Node> nodes;
				try {
					
					XPath xpath = new DOMXPath(value.toString());
					this.nsContext.addNamespace("pckg", "http://webservices.deri.ie");
					xpath.setNamespaceContext(this.nsContext);
					logger.debug(value.toString());
					nodes = xpath.selectNodes(soapMessage);
					logger.debug(nodes.size());
	    			if (nodes.size()>0)
	    				instacesReturn.add(instance);
	    				 						
				} catch (JaxenException e) {
					logger.error("Error: " + instance.getIdentifier().toString());
				}
	
    		}
    		
    	}
		return instacesReturn;

	}
	

	public ArrayList<Triple<String, String,String>> getInstanceMappingsValues(Instance insts){
		Ontology adapterOntology = Helper.getOntology(PackagerConstants.packagerAdapterIRI);
		ArrayList<Triple<String, String,String>> result =new  ArrayList<Triple<String, String,String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(PackagerConstants.attributeInstanceMappings));
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
		Ontology adapterOntology = Helper.getOntology(PackagerConstants.packagerOntoIRI);
		ArrayList<Triple<String, String,String>> result = new  ArrayList<Triple<String, String,String>>();
		Set <Value> values = insts.listAttributeValues(loadIRI(PackagerConstants.attributeConceptOutput));
		for (Value value : values) {
			return value.toString();
		 	}
		
	  	  return null;
		
	}		
	
	
	public Instance createInstance(String conceptName){
		Ontology packagerOntology = Helper.getOntology(PackagerConstants.packagerOntoIRI);
		
		Concept concept = loadConcept(packagerOntology, PackagerConstants.packagerOntoNS+conceptName);
		if (concept ==null )
			return null;

		WsmoFactory  wsmoFactory = Factory.createWsmoFactory(null);

		IRI instanceIRI = wsmoFactory.createIRI(packagerOntology.getDefaultNamespace(),"tempInst"+(new Long ((new Date()).getTime())).toString());
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
		Set <Value> values = insts.listAttributeValues(loadIRI(PackagerConstants.attributeValueMappings));
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
	        
	        if (uri.contains("http://webservices.deri.ie"))
	        	prefix="pckg";
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
            IRI irionto = wsmoFactory.createIRI(PackagerConstants.packagerOntoNS);
            Namespace smNamespace = wsmoFactory.createNamespace("onto",irionto);
            anOntology.addNamespace(wsmoFactory.createNamespace("wsml", wsmoFactory.createIRI("http://www.wsmo.org/wsml/wsml-syntax#")));
            anOntology.addNamespace(smNamespace);
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

}
