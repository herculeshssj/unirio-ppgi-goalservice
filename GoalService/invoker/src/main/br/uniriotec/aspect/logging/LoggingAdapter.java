package br.uniriotec.aspect.logging;

import ie.deri.wsmx.adapter.Adapter;
import ie.deri.wsmx.commons.Helper;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPElement;

import org.apache.log4j.Logger;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.w3c.dom.Document;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Serializer;
import org.xml.sax.InputSource;

public class LoggingAdapter extends Adapter {
	
	//private SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
	protected static Logger logger = Logger.getLogger(LoggingAdapter.class);

	public Document getXML(List<Entity> instances, String id) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			String sDoc = "";
			boolean found = false;
			
			for (Entity entity : instances) {
				if (! (entity instanceof Entity) ) {
					break;
				}
				Instance instance = (Instance) entity;
				Set<Concept> concepts = instance.listConcepts();
				for (Concept concept : concepts) {
					String conceptName = concept.getIdentifier().toString();
					if (conceptName.equalsIgnoreCase("http://www.uniriotec.br/aspect#LogRequest")) {
						sDoc = getRequestURLReqXML(instance, id);
						found = true;
						logger.info("Instância " + instance.getIdentifier().toString() + " encontrada!");
					}
					if (found) break;
				}
				if (found) break;
			}
			
			InputSource is = new InputSource(new StringReader(sDoc));
			org.w3c.dom.Document doc = builder.parse(is);
			return doc;
		} catch (Exception e) {
			logger.error("**** Erro na geração do XML ****", e);
			return null;
		}
	}

	public String getRequestURLReqXML(Instance instance, String id) {
		StringBuilder result = new StringBuilder();
		
		result.append("<ser:simpleLogService xmlns:ser=\"http://service.logapp.uniriotec.br\" xmlns:mod=\"http://model.logapp.uniriotec.br\">");
		result.append("<ser:logRequest>");
		result.append("<mod:message>test message</mod:message>");
		result.append("<mod:severity>");
		result.append("<mod:level>2</mod:level>");
		result.append("<mod:name>INFO</mod:name>");
		result.append("</mod:severity>");
		result.append("</ser:logRequest>");
		result.append("</ser:simpleLogService>");

		return result.toString();
		
	/*
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
		*/
	}
	
	public SOAPElement getHeader(List<Entity> instances, String id){
    	return null;
    }
/*
	public SOAPElement getHeader(Instance instance, Identifier id) {
		SOAPElement soapElement = null;
		/
		try {			
			SOAPFactory soapFactory = SOAPFactory.newInstance();
			Name service = soapFactory.createName("service", "ser", "http://service.logapp.uniriotec.br");
			Name model = soapFactory.createName("model", "mod", "http://model.logapp.uniriotec.br");
			
			SOAPElement serviceElem = soapFactory.createElement(service);
			SOAPElement modelElem = soapFactory.createElement(model);
			
			soapElement = serviceElem;
			soapElement.addChildElement(modelElem);
			
			// <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://service.logapp.uniriotec.br" xmlns:mod="http://model.logapp.uniriotec.br">			
			
    	} catch (Exception e) {
    		logger.error("Erro ao processo XML", e);
    	}
		return soapElement;
		
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
	*/
	
	@Override
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint) {
		// Por enquanto o documento WSML será hard-coded. Mais pra frente este adaptador
		// será refatorado para poder obter dinamicamente as instâncias que representam
		// a mensagem de retorno do envelope SOAP
		
		try {
			WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
			DataFactory dataFactory = Factory.createDataFactory(null);
			
			Ontology tempOntology = this.createTempOntology();
			
			Ontology loggingOntology = Helper.getOntology("http://www.uniriotec.br/aspect#LogOntology");
			Concept concept = loggingOntology.findConcept(loadIRI("http://www.uniriotec.br/aspect#LogEffect"));
			 
			Instance instanceResult = createInstance(loggingOntology, concept);
			
			IRI attributeIRI = wsmoFactory.createIRI(loggingOntology.getDefaultNamespace(), "messageLogged");
			instanceResult.addAttributeValue(attributeIRI, dataFactory.createWsmlBoolean("true"));
			
			attributeIRI = wsmoFactory.createIRI(loggingOntology.getDefaultNamespace(), "mailSent");
			instanceResult.addAttributeValue(attributeIRI, dataFactory.createWsmlBoolean("false"));
			
			attributeIRI = wsmoFactory.createIRI(loggingOntology.getDefaultNamespace(), "logPersisted");
			instanceResult.addAttributeValue(attributeIRI, dataFactory.createWsmlBoolean("false"));
			
			attributeIRI = wsmoFactory.createIRI(loggingOntology.getDefaultNamespace(), "messageShowed");
			instanceResult.addAttributeValue(attributeIRI, dataFactory.createWsmlBoolean("false"));
			
			tempOntology.addInstance(instanceResult);
			
			String temp = writeOntology(tempOntology);
			
			return new WSMLDocument(temp);
			
		} catch (Exception e) {
			logger.error("Error in generate WSML doc", e);
		}
		
		return null;
	}
	
	private String writeOntology(Ontology inputOntology) throws IOException {
		Serializer serializer = Factory.createSerializer(null);
		StringWriter writer = new StringWriter();
		TopEntity[] entitiesOntology = new TopEntity[1];
		entitiesOntology[0] = inputOntology;
		serializer.serialize(entitiesOntology, writer);
		return writer.getBuffer().toString();
	}

	private Instance createInstance(Ontology ontology, Concept concept) throws InvalidModelException {
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		
		IRI instanceIRI = wsmoFactory.createIRI(ontology.getDefaultNamespace(), "tempInst"+( new Long (( new Date()).getTime())).toString());
		
		Instance instanceResult = wsmoFactory.createInstance(instanceIRI, concept);
		
		return instanceResult;
	}
	
	private IRI loadIRI(String value) {
		WsmoFactory factory = Factory.createWsmoFactory(null);
		IRI result = factory.createIRI(value);
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Ontology createTempOntology() {
		// 1. Initialize the factory with the wsmo4j provider
		HashMap factoryParams = new HashMap();
		factoryParams.put(Factory.WSMO_SERIALIZER, "com.ontotext.wsmo4j.common.WSMOFactoryImpl");
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		
		try {
			// 2. Create a ontology
			Ontology anOntology = wsmoFactory.createOntology(wsmoFactory.createIRI("http://www.example.org/ontologies/example#"));
			
			// 3. Setup a namespace
			IRI iriOnto = wsmoFactory.createIRI("http://www.uniriotec.br/aspect#");
			Namespace nameSpace = wsmoFactory.createNamespace("onto", iriOnto);
			anOntology.addNamespace(wsmoFactory.createNamespace("wsml", wsmoFactory.createIRI("http://www.wsmo.org/wsml/wsml-syntax#")));
			anOntology.addNamespace(nameSpace);
			
			return anOntology;
			
		} catch (Exception e) {
			logger.error("Error in create temp ontology", e);
		}
		return null;
	}
		
	/*
		try {
			Instance instanceResult = null;
			
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage message = messageFactory.createMessage();
			SOAPPart soapPart = message.getSOAPPart();
			StreamSource source = new StreamSource(new StringReader(document));
			soapPart.setContent(source);
			
			nsContext.addNamespace("ns", "http://service.logapp.uniriotec.br");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();		
			InputSource is = new InputSource(new StringReader(document));
			Document doc = builder.parse(is);
			
			DataFactory dataFactory = Factory.createDataFactory(null);
			/*
			Ontology tempOntology = this.createTempOntology();
			ArrayList<Instance> instancesToTranslate = getInstanceToTranslate(doc);
			for (Instance instanceToTranslate : instancesToTranslate) {
				
			}
			
			
		} catch (Exception e) {
			logger.error("Erro ao gerar o documento WSML", e);
		}
		/*
			
		
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
	/*
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
            IRI irionto = wsmoFactory.createIRI("http://www.uniriotec.br/aspect/logging#");
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
	
	public ArrayList<Instance> getInstanceToTransalate(Document soapMessage){
		Ontology adapterOntology = Helper.getOntology("http://www.uniriotec.br/aspect/logging#LogOntology");
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
	*/
}
