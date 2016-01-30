package ie.deri.webservices;



public class MoonRNEntryPoint {
	
}
///*
// * Copyright (c) 2005 National University of Ireland, Galway
// *
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 2 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
// */
//
//package ie.deri.webservices;
//
//import ie.deri.wsmx.commons.Helper;
//import ie.deri.wsmx.executionsemantic.ExecutionSemanticsFinalResponse;
//import ie.deri.wsmx.scheduler.Environment;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.io.StringReader;
//import java.io.StringWriter;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.management.MBeanServer;
//import javax.management.ObjectName;
//import javax.servlet.ServletContext;
//import javax.servlet.http.HttpServlet;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.stream.StreamResult;
//import javax.xml.transform.stream.StreamSource;
//
//import org.apache.axis.MessageContext;
//import org.apache.axis.transport.http.HTTPConstants;
//import org.apache.log4j.Logger;
//import org.apache.xml.serialize.OutputFormat;
//import org.apache.xml.serialize.XMLSerializer;
//import org.deri.wsmo4j.io.parser.wsml.ParserImpl;
//import org.omwg.ontology.Instance;
//import org.omwg.ontology.Ontology;
//import org.omwg.ontology.Value;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.wsmo.common.Entity;
//import org.wsmo.common.IRI;
//import org.wsmo.common.TopEntity;
//import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
//import org.wsmo.execution.common.nonwsmo.grounding.WSDL1_1EndpointGrounding;
//import org.wsmo.factory.Factory;
//import org.wsmo.factory.WsmoFactory;
//import org.wsmo.service.Goal;
//import org.wsmo.service.Interface;
//import org.wsmo.service.WebService;
//import org.wsmo.service.choreography.Choreography;
//import org.wsmo.service.signature.Grounding;
//import org.wsmo.service.signature.Out;
//import org.wsmo.service.signature.WSDLGrounding;
//import org.wsmo.wsml.Parser;
//import org.xml.sax.InputSource;
//
///** * Interface or class description
// * * @author Maciej Zaremba
// *
// * Created on 2006-04-24
// * Committed by $Author: maciejzaremba $
// * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/webservices/src/main/ie/deri/webservices/MoonRNEntryPoint.java,v $, * @version $Revision: 1.7 $ $Date: 2007-10-11 14:53:38 $
// */
//public class MoonRNEntryPoint {
//
//	protected static Logger logger = Logger.getLogger(MoonRNEntryPoint.class);
//	
//	static {
////		Logger.getRootLogger().removeAllAppenders();
////    	BasicConfigurator.configure(new ConsoleAppender());
//		
////    	Logger.getRoot().setLevel(Level.ALL);
//	}
//	
//    public Element[] receivePO(Element [] elems) {
//    	
//    	//recognize the message
//    	Element rootElement = elems[0];    	
//    	if (rootElement.getOwnerDocument().getElementsByTagName("OrderLineItemConfirmation").getLength() > 0)
//    	{
//    		// extract order number
//			int orderId = Integer.parseInt(rootElement.getOwnerDocument().getElementsByTagName("orderId").item(0).getTextContent());
//			ServletContext sc = ((HttpServlet) MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLET)).getServletContext();
//			// check if order object is availible
//
//			try {
//				LineOrderConfimation lineOrderConfimations = null;
//				//attempt to get lineOrderConf. object from app server context 
//				for (int i = 0; i < 10; i++) {
//					lineOrderConfimations = (LineOrderConfimation) sc.getAttribute("LineOrderConfirmation" + orderId);
//					if (lineOrderConfimations != null)
//						break;
//
//					Thread.sleep(400); 
//				}
//
//	    		//if no LineOrderConfimation object created
//				if (lineOrderConfimations == null)
//					return elems;
//
//				//if (true) return elems;
//				
//				// make wsml from received xml
//				StringWriter sw = new StringWriter();
//				OutputFormat format = new OutputFormat(rootElement.getOwnerDocument());
//				format.setIndenting(true);
//				XMLSerializer serializer = new XMLSerializer(sw, format);
//				logger.fatal(sw.toString());
//
//				serializer.serialize(rootElement.getOwnerDocument());
//				String xsltPath = "resources" + File.separator+ "communicationmanager" + File.separator+ "moon2WSML.xsl";
//				File xsltFile = new File(Environment.getKernelLocation()+ File.separator + xsltPath);
//				String translatedWSML = doTransformation(sw.toString(),new FileInputStream(xsltFile));
//
//				// it will add instances automatically to existing wsmo4j ontology
//				Parser parser = new ParserImpl(new HashMap<String, Object>());
//				TopEntity[] topEntities = parser.parse(new StringReader(translatedWSML));
//
//				lineOrderConfimations.addLineItemConfimation();
//				
//				logger.fatal("-- Received LineOrderConfirmation(s) --");
//
//				if (lineOrderConfimations.isReady()){
//					logger.fatal("-- Received all LineOrderConfirmation(s) --");
//					
//					AsynchronousInvoker aInvoker = lineOrderConfimations.getAsynchronousInvoker();
//					sc.removeAttribute("LineOrderConfirmation" + orderId);
//					
//					sw = new StringWriter();
//					format = new OutputFormat(aInvoker.POMsg.getOwnerDocument());
//					format.setIndenting(true);
//					serializer = new XMLSerializer(sw, format);
//					serializer.serialize(aInvoker.POMsg.getOwnerDocument());
//					
//					xsltPath = "resources"+File.separator+"communicationmanager"+File.separator+"por2poc.xsl";
//					xsltFile = new File(Environment.getKernelLocation() + File.separator + xsltPath);
//					String translatedXML = doTransformation(sw.toString(), new FileInputStream(xsltFile));
//	
//					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//					factory.setNamespaceAware(true);
//					DocumentBuilder builder = factory.newDocumentBuilder();
//					
//					//Parse the documents		
//					Document documentPOC = builder.parse(new InputSource(new StringReader(translatedXML)));
//					Element POCMsg = documentPOC.getDocumentElement();
//					
//					String dateAndTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//					//generate POC.core:thisDocumentGenerationDateTime
//					Node targetNode = POCMsg.getElementsByTagName("core:thisDocumentGenerationDateTime").item(0);
//					targetNode.getChildNodes().item(0).setTextContent(dateAndTime);
//	
//					//generate POC.core:thisDocumentIdentifier
//					targetNode = POCMsg.getElementsByTagName("core:thisDocumentIdentifier").item(0);
//					targetNode.getChildNodes().item(0).setTextContent(""+this.hashCode()+dateAndTime);
//					
//					sw = new StringWriter();
//					format = new OutputFormat(POCMsg.getOwnerDocument());
//					format.setIndenting(true);
//					serializer = new XMLSerializer(sw, format);
//					serializer.serialize(POCMsg.getOwnerDocument());
//					
//					List<Entity> goalInstances = Helper.getInstances(aInvoker.goalOnto);
//		    		//clean up after communication
//					//clean up Goal + Goal ontology
//					for (Entity i : goalInstances ){
//						Map attr = ((Instance) i).listAttributeValues();
//						Set<IRI> keys = attr.keySet();
//							
//						for (IRI iri : keys){
//							Object values = attr.get(iri);
//							((Instance) i).removeAttributeValues(iri);
//						}
//						aInvoker.goalOnto.removeInstance(((Instance) i));
//					}
//					aInvoker.goal.removeOntology(aInvoker.goalOnto);
//					aInvoker.goalOnto.removeOntology(aInvoker.goalOnto);
//					
//					Ontology goalMediatedOnto = ((Instance)aInvoker.esResponse.getSendMessages().toArray()[0] ).getOntology();
//					List<Entity> goalMediatedInstances = Helper.getInstances(goalMediatedOnto);
//					
//					//clean up mediatedGoalOntology
//					for (Entity i : goalMediatedInstances ){
//						Map attr = ((Instance) i).listAttributeValues();
//						Set<IRI> keys = attr.keySet();
//							
//						for (IRI iri : keys){
//							Object values = attr.get(iri);
//							((Instance) i).removeAttributeValues(iri);
//						}
//						goalMediatedOnto.removeInstance(((Instance) i));
//					}
//					goalMediatedOnto.removeOntology(goalMediatedOnto);		
//	
//					ObjectName commManagerName = new ObjectName("components:name=CommunicationManager");
//					Object returnData = aInvoker.mBeanServer.invoke(commManagerName, "syncInvoke",
//										new Object[]{(WebService)aInvoker.esResponse.getWebservice(),documentPOC,(EndpointGrounding)aInvoker.endpointGrounding}, 
//										new String[]{"org.wsmo.service.WebService",
//													 "org.w3c.dom.Document",
//													 "org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding"});
//	
////					Document documentPOC_Ack = (Document) returnData;
////					serialize the response
////					sw = new StringWriter();
////					format = new OutputFormat(documentPOC_Ack);
////					format.setIndenting(true);
////					serializer = new XMLSerializer(sw, format);
////					serializer.serialize(documentPOC_Ack);
////					
////					logger.fatal(sw.toString());
//				}
//
//    		} catch (Exception e) {
//				e.printStackTrace();
//			}        		
//
//    		return elems;
//    	}
//    	
//    	//read and parse XML response
//    	String xmlPORAcknPath = "resources"+File.separator+"webservices"+File.separator+"templates"+File.separator+"Ack_POR.xml"; 
//    	File xmlPORAckn = new File(Environment.getKernelLocation() + "/" + xmlPORAcknPath);
//		
//		try {
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			factory.setNamespaceAware(true);
//			DocumentBuilder builder = factory.newDocumentBuilder();
//			
//			// Parse the document		
//			Document documentPORAckn = builder.parse(xmlPORAckn);
//			
//			Element rootPOR = rootElement;
//			Element rootPORAckn = documentPORAckn.getDocumentElement();
//			
//			Node targetNode = documentPORAckn.getElementsByTagName("ack:OriginalMessageDigest").item(0);
//			Node sourceNode = documentPORAckn.importNode(rootPOR.getOwnerDocument().getFirstChild(), true);
//
//			//add contents of POR document to Ackn			 
//			targetNode.appendChild(sourceNode);
//			
//			Element[] elem = new Element[]{rootPORAckn};
//			//response is ready
//			
//			//make wsml from received xml
//			StringWriter sw = new StringWriter();
//			OutputFormat format = new OutputFormat(documentPORAckn);
//			format.setIndenting(true);
//			XMLSerializer serializer = new XMLSerializer(sw, format);
//			logger.fatal(sw.toString());
//			
//			serializer.serialize(documentPORAckn);
//			String xsltPath = "resources"+File.separator+"communicationmanager"+File.separator+"po2WSML3_NoNamespaceDef.xsl";
//			File xsltFile = new File(Environment.getKernelLocation() + File.separator + xsltPath);
//			String translatedXML = doTransformation(sw.toString(), new FileInputStream(xsltFile));
//			
//			//read Goal definition
//	    	String tempPath = "resources"+File.separator+"resourcemanager"+File.separator+"goals"+File.separator+"MoonGoal.wsml";
//	    	File tempFile = new File(Environment.getKernelLocation() + File.separator + tempPath);
//
//	    	BufferedReader br = new BufferedReader(new FileReader(tempFile));
//	    	String line, wsmlGoalString = "";
//	    	while((line = br.readLine())!=null)
//	    		wsmlGoalString += line + "\n";
//			
//			//combine Goal with messages
//	    	String finalGoal = wsmlGoalString + "\n " +translatedXML;  
//
//	    	//get reference to MBeanServer
//	    	ServletContext sc = ((HttpServlet) MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLET)).getServletContext();
//			MBeanServer mBeanServer = (MBeanServer) sc.getAttribute("MBeanServer");
//			//create a second thread
//	    	AsynchronousInvoker ainvoker = new AsynchronousInvoker(finalGoal, rootPOR, mBeanServer, sc);
//	    	
//	    	return elem;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return null;
//    }
//    
//    public class AsynchronousInvoker implements Runnable
//    {
//    	String wsmlMessageGoal;
//    	MBeanServer mBeanServer;
//    	ServletContext sc;
//    	Element POMsg;
//    	Thread t;
//    	Parser parser;
//    	public Goal goal;
//    	public Ontology goalOnto;
//    	public WSDL1_1EndpointGrounding endpointGrounding;
//    	public ExecutionSemanticsFinalResponse esResponse;
//    	
//    	public AsynchronousInvoker(String wsmlMessageGoal, Element POMsg, MBeanServer mBeanServer, ServletContext sc){
//    		this.wsmlMessageGoal = wsmlMessageGoal;
//    		this.mBeanServer = mBeanServer;
//    		this.POMsg = POMsg;
//    		this.sc = sc;
//    		
//    		this.parser = new ParserImpl(new HashMap<String, Object>());
//
//    		//read Goal + Ontology (instances)
//			try {
//				TopEntity[] topEntities = parser.parse(new StringReader(wsmlMessageGoal));
//	    		//get Goal
//	    		for (int i = 0; i < topEntities.length; i++){
//	    			if (topEntities[i] instanceof Goal)
//	    				goal = (Goal) topEntities[i];
//	    			if (topEntities[i] instanceof Ontology)
//	    				goalOnto = (Ontology) topEntities[i];
//	    		}
//	    		Interface inf = (Interface) goal.listInterfaces().iterator().next();
//	    		Choreography goalChoreography = (Choreography) inf.getChoreography();
//	    		
//	    		Set<Out> goalOUTs = goalChoreography.getStateSignature().listOutModes();
//
//	    		for (Out out : goalOUTs){
//	    			Set<Grounding> groundings = out.getGrounding();
//	    			for (Grounding grounding : groundings){
//
//	    				if (grounding!= null) {
//	    					 
//	    					// FIXME reliance on to string
//	    					this.endpointGrounding = new WSDL1_1EndpointGrounding(((WSDLGrounding)grounding).getIRI().toString());
////	    					logger.fatal("Grounding: " + this.endpointGrounding.getWsdlURI());
//	    				}
//	    			}
//	    		}
//
//	    		
////				FIXME - remove it; read Goal definition
////		    	String tempPath = "resources"+File.separator+"resourcemanager"+File.separator+"goals"+File.separator+"goalMediateTime.wsml";
////		    	File tempFile = new File(Environment.getKernelLocation() + File.separator + tempPath);
////	    		
////		    	BufferedReader br = new BufferedReader(new FileReader(tempFile));
////		    	String line;
////		    	this.wsmlMessageGoal = "";
////		    	while((line = br.readLine())!=null)
////		    		this.wsmlMessageGoal += line + "\n";	    		
//	    		
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			t = new Thread(this);
//    		t.start();
//    	}
//        
//    	public void run() {                           
//			try {				
//				ObjectName commManagerName = new ObjectName("components:name=CommunicationManager");
//
//				//check if registered
//				boolean flag = mBeanServer.isRegistered(commManagerName);
//				if (!flag) {
//					return;
//				} 
//
//				//invoke achieveGoal from Communication Manager MBean
//				Object returnData = mBeanServer.invoke(commManagerName, "achieveGoalFullResponse",
//									new Object[]{wsmlMessageGoal}, new String[]{"java.lang.String"});
//				this.esResponse = (ExecutionSemanticsFinalResponse) returnData;
//				
//				WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
//					
//				//register final answer with the system
//				List<Entity> addLineItemsResp = Helper.getInstancesOfConcept(new ArrayList(esResponse.getReceivedMessages()),
//																			 "http://www.example.org/ontologies/sws-challenge/Moon#AddLineItemResponse");
//				Set attrValues = ((Instance) addLineItemsResp.get(0)).listAttributeValues(wsmoFactory.createIRI("http://www.example.org/ontologies/sws-challenge/Moon#orderId"));
//				Value v = ( (Value) attrValues.toArray()[0] );
//				int orderId = Integer.parseInt(v.toString());
//				LineOrderConfimation lineOrderConfirmation = new LineOrderConfimation(orderId,addLineItemsResp.size(), this);				
//				sc.setAttribute("LineOrderConfirmation" + orderId, lineOrderConfirmation);
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			} 
//        }
//    	
//    	 public void stop() {
//    	        t = null;
//    	    }
//    	    	
//    }
//    
//    class LineOrderConfimation {
//    	
//    	private int orderId;
//    	private int receivedLineOrdersConf;
//    	private int totalLineOrdersConf;
//    	private AsynchronousInvoker asynchronousInvoker;
//    	
//		public LineOrderConfimation(int orderId, int totalLineOrdersConf, AsynchronousInvoker asynchronousInvoker) {
//			super();
//			this.receivedLineOrdersConf = 0;
//			this.orderId = orderId;
//			this.totalLineOrdersConf = totalLineOrdersConf;
//			this.asynchronousInvoker = asynchronousInvoker;
//		}
//		
//		public synchronized void addLineItemConfimation(){
//			receivedLineOrdersConf++;
//    	}
//		
//		//function returns true when all 
//		public synchronized boolean isReady(){
//			return (receivedLineOrdersConf == totalLineOrdersConf) ? true : false;
//		}
//
//		public AsynchronousInvoker getAsynchronousInvoker() {
//			return asynchronousInvoker;
//		}
//
//    }
//    
//
//    
//	private String doTransformation(String initialXML, InputStream xsltFileName){
//		
//		String wsmlFile = new String();
//		StringWriter writer = new StringWriter();
//		
//		try
//		{
//			TransformerFactory tf = TransformerFactory.newInstance();
//			Transformer t1 = tf.newTransformer(new StreamSource(xsltFileName));
//			t1.transform(new StreamSource(new StringReader(initialXML)), new StreamResult(writer));
//			//write the results of the transformation to the wsml document
//			wsmlFile = writer.toString();
//			
//		}
//		catch(Exception e)
//		{System.out.println(e.getMessage());
//		}
//				
//		return wsmlFile;
//	}
//    
//}
// 