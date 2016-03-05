/*
 * Copyright (c) 2007 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  */

package ie.deri.wsmx.invoker;


import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.invoker.utility.Pair;
import ie.deri.wsmx.core.configuration.annotation.Exposed;
import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;
import ie.deri.wsmx.scheduler.Environment;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.deri.infrawebs.sfs.adapter.CarRentalServiceAdapter;
import org.deri.infrawebs.sfs.adapter.FlightServiceAdapter;
import org.deri.infrawebs.sfs.adapter.HotelServiceAdapter;
import org.deri.infrawebs.sfs.adapter.ShuttleServiceAdapter;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.TopEntity;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.execution.common.nonwsmo.grounding.HTTP_EndpointGrounding;
import org.wsmo.execution.common.nonwsmo.grounding.WSDL1_1EndpointGrounding;
import org.wsmo.execution.common.nonwsmo.grounding.WSDL1_1GroundingException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.WebService;
import org.wsmo.wsml.Parser;
import org.xml.sax.SAXException;

//import pl.telekomunikacja.portal.adapter.ContractPreparationAdapter;
//import pl.telekomunikacja.portal.adapter.CourierServiceAdapter;
import pl.telekomunikacja.portal.adapter.CustomerIdentificationAdapter;
//import pl.telekomunikacja.portal.adapter.FormalVerificationAdapter;
//import pl.telekomunikacja.portal.adapter.OrderConfirmAdapter;
//import pl.telekomunikacja.portal.adapter.OrderCreateAdapter;
import pl.telekomunikacja.portal.adapter.TP_Showcase_Constants;
//import pl.telekomunikacja.portal.adapter.TechnicalVerificationAdapter;

import br.uniriotec.aspect.logging.LoggingAdapter;

import com.isoco.dip.adapter.BrokerServiceAdapter;
import com.oms.dip.adapter.OMS_Adapter;
import com.superadapter.PackagerAdapter;
import com.superadapter.nexcom.NexcomAdapter;
import com.swing.adapter.SwingAdapter;
import com.swschallenge.adapter.LegacyAdapter;
import com.swschallenge.adapter.SWSChallAdapter;
import com.swschallenge.adapter.SWSChallCompositionAdapter;
import com.swschallenge.adapter.SWSChallPaymentAdapter;


/**
 * Interface or class description
 *
 * <pre>
 * Created on 06-Jul-2005
 * Committed by $Author: maciejzaremba $
 * </pre>
 *
 * @author Maciej Zaremba, Matthew Moran
 *
 * @version $Revision: 1.3 $ $Date: 2007-12-13 16:48:52 $
 */
@WSMXComponent(name   = "Invoker",
		       events = {"INVOKER"},
        	   description = "This version of the Invoker is for use with a specific use cases." +
        	   				 " Invokes the Web service using the list of entities as objects" +
        	   				 " and the specified operation as the WSDL operation to be used.")
public class Invoker implements org.wsmo.execution.common.component.Invoker {

    static Logger logger = Logger.getLogger(Invoker.class);
    
	private BrokerServiceAdapter isocoAdapter = new BrokerServiceAdapter();
	private PackagerAdapter packagerAdapter = new PackagerAdapter();
	private NexcomAdapter nexcomAdapter = new NexcomAdapter();
	private SWSChallAdapter swsChallAdapter = new SWSChallAdapter();
	private SWSChallCompositionAdapter swsChallCompositionAdapter = new SWSChallCompositionAdapter();
	private SWSChallPaymentAdapter swsChallPaymentAdapter = new SWSChallPaymentAdapter();
	
	//Infrawebs Adapters
	private CarRentalServiceAdapter carAdapter = new CarRentalServiceAdapter();
	private FlightServiceAdapter flightAdapter = new FlightServiceAdapter();
	private ShuttleServiceAdapter shuttleAdapter = new ShuttleServiceAdapter();
	private HotelServiceAdapter hotelAdapter = new HotelServiceAdapter();
    
	// Meu adapter
	private LoggingAdapter loggingAdapter = new LoggingAdapter();
	
	// TP Showcase
	private CustomerIdentificationAdapter ciAdapter = new CustomerIdentificationAdapter();
//	private FormalVerificationAdapter fvAdapter = new FormalVerificationAdapter();
//	private TechnicalVerificationAdapter tvAdapter = new TechnicalVerificationAdapter();
//	private OrderCreateAdapter ocAdapter = new OrderCreateAdapter();
//	private OrderConfirmAdapter ocoAdapter = new OrderConfirmAdapter();
//	private ContractPreparationAdapter cpAdapter = new ContractPreparationAdapter();
//	private CourierServiceAdapter csAdapter = new CourierServiceAdapter();
	
    //list of WSDL4j bindings for given URI, <URI, DynamicBinder>
    private HashMap<String,DynamicBinder> listOfDynamicBinders; 
	private WsmoFactory wsmoFactory;
	private Parser parser;
    
	private int context = 1;
	
	public Invoker() {
		super();
		//setup required variables
		listOfDynamicBinders = new  HashMap<String,DynamicBinder>();
		wsmoFactory = Factory.createWsmoFactory(null);
		
		HashMap<String, Object> props = new HashMap<String, Object>();
  		parser = Factory.createParser(props);

	}
	
	//WSDL4j caching method, each instance of Comm. Manager maintains a pool of analyzed WSDLs identifiable by URI   
	private DynamicBinder getDynamicBinder(String wsdlURIStr) throws ParserConfigurationException, SAXException, IOException{
		if (listOfDynamicBinders.containsKey(wsdlURIStr)){
			return listOfDynamicBinders. get(wsdlURIStr);
		}
		
		//no dynamic binding to WSDL URI exists
		DynamicBinder dBinder = new DynamicBinder(wsdlURIStr);
		listOfDynamicBinders.put(wsdlURIStr,dBinder);
		return dBinder;
	}
	
	// ------------------------------------------------------------------- //
	// Invoker interface methods
	// ------------------------------------------------------------------- //
	/**
	 * Invoke the Web service using the list of entities as objects and the
	 * specified operation as the WSDL operation to be used.
	 * 
	 */
	@Exposed(description = "Invoke the Web service using the list of entities as objects " +
						   "and the specified operation as the WSDL operation to be used.")
	public List<Entity> invoke(WebService service, 
			           List<Entity> data, 
			           String grounding) 
			throws ComponentException, UnsupportedOperationException {
		
		logger.info("service : " + service.getIdentifier());
		logger.info("data size : " + data.size());
		logger.info("grounding : " + grounding);
			try {
				return syncInvokeWS(service, data, grounding);
			} catch (WSDL1_1GroundingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return new ArrayList<Entity>();
			}
	}
	static int idCnt = 1;
	private int genID() {
		return idCnt++;
	}

	@Exposed(description = "Make a synchronous invocation of the Web service.")
	public List<Entity> syncInvoke(WebService sws, List<Entity> data, EndpointGrounding grounding, Ontology ontology) throws ComponentException 
	{	
		try {
			WSDL1_1EndpointGrounding theGrounding = (WSDL1_1EndpointGrounding) grounding; 
			Pair<SOAPElement,Document> soapParts = inlineWSMLtoXMLAdapter(sws, grounding , ontology, data);

			DynamicBinder dBinder = getDynamicBinder(theGrounding.getWsdlURI());
			dBinder.readDetailsFromWSDL(theGrounding.getPortType(), theGrounding.getOperation());
			
			logger.info("URI : " + theGrounding.getWsdlURI() + "\nporttype: "+theGrounding.getPortType() + " \noperation: " + theGrounding.getOperation() + "\nendpoint: " + dBinder.endPoint);

			QName serviceName = new QName(dBinder.targetNS,dBinder.serviceName);
			Service serv = Service.create(serviceName);
			
			QName portQName = new QName(dBinder.targetNS, theGrounding.getPortType());
			serv.addPort(portQName, SOAPBinding.SOAP11HTTP_BINDING, dBinder.endPoint);

			MessageFactory factory = MessageFactory.newInstance();
	        SOAPMessage message = factory.createMessage();	        
            message.getSOAPBody().addDocument(soapParts.getSecond());
            message.saveChanges();

            Dispatch<SOAPMessage> smDispatch = serv.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);
            
            smDispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
            smDispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, theGrounding.getOperation());

            List<Handler> handlers =  smDispatch.getBinding().getHandlerChain();
	        handlers.add(new SOAPLoggingHandler());
	        smDispatch.getBinding().setHandlerChain(handlers);   
	        
	        SOAPMessage soapResponse = null;
	        try {
	        	soapResponse = smDispatch.invoke(message);
	        } catch (Exception e){
	        	e.printStackTrace();
	        	logger.error("Fault has been returned in SOAP message!!!", e);
	        	return new ArrayList<Entity>();
	        }
	        
        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Document doc = soapResponse.getSOAPBody().getOwnerDocument();
            
            Element counter = doc.createElement("counter");
			
			counter.appendChild(doc.createTextNode(Integer.toString(genID())));
			doc.getDocumentElement().appendChild(counter);
			
			DOMSource domSource = new DOMSource(doc);
			
			StreamResult responseXMLStream = new StreamResult(bos);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();			
			transformer.transform(domSource, responseXMLStream);
			String wsXMLResponse = bos.toString();
	        //local name, envelope
	        WSMLDocument wsmlDoc = inlineXMLtoWSMLAdapter(sws, grounding, wsXMLResponse);
			
			TopEntity[] topEnt = parser.parse(new StringReader(wsmlDoc.getContent()));
			List<Entity> instances = Helper.getInstances(topEnt);

			return instances;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ComponentException(e);
		}
	}	
	

	public class SOAPLoggingHandler implements SOAPHandler<SOAPMessageContext> {
		public boolean handleMessage(SOAPMessageContext smc) {
			 logToSystemOut(smc);
			 return true;
		}

		public boolean handleFault(SOAPMessageContext smc) {
			logToSystemOut(smc);
			return true;
		}

		public Set<QName> getHeaders() {
			return null;
		}

		public void close(MessageContext context) {
		}
		
	    private void logToSystemOut(SOAPMessageContext smc) {
	        boolean direction = ((Boolean) smc.get (javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();
	        String directionStr = direction ? "\n---------Outgoing SOAP message---------" : "\n---------Incoming SOAP message---------";
	        
	        SOAPMessage message = smc.getMessage();
	        
	        try {
	        	//XML pretty print
	        	Document doc = toDocument(message);
	        	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        	OutputFormat format = new OutputFormat(doc);
				format.setIndenting(true);
				XMLSerializer serializer = new XMLSerializer(stream, format);
				serializer.serialize(doc);
	            logger.info(directionStr + "\n" + stream.toString() + "----------End of SOAP message----------\n");
	        } catch (Exception e) {
	            logger.info(directionStr + "\n Exception was thrown  \n----------End of SOAP message----------\n");
	        }
	    }
		
	}

	/**
	 * Inline adaptor for the Invoker 
	 * 
	 * @param ws
	 *            The WSMO4j Web Service object of the service being invoked
	 * @param data
	 *            The data that needs to be adapted
	 * @return
	 */
	//Pair<SOAPHeader Document, SOAPBody Document> 
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private Pair<SOAPElement,Document> inlineWSMLtoXMLAdapter(WebService ws, EndpointGrounding grounding, Ontology onto, List<Entity> data) {
		// Determine the owner of the WebService and use this to get the correct adapter for lowering
		// A NFP with identifier "http://owner" is used to determine which adaptation code to use
		String theOwner = "";
		try{
			Set theNFPs = ws.listNFPValues(wsmoFactory.createIRI("http://owner"));
			
			if (!theNFPs.isEmpty()) {
				IRI ownerValue = (IRI) theNFPs.iterator().next();
				theOwner = ownerValue.toString(); 
				logger.debug("Owner of the Web service: " +theOwner + "\n");
			} else 
				return new Pair(null,null);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// THE TP SHOWCASE
		if (theOwner.contains(TP_Showcase_Constants.tpscOwnerPath)) {
			Identifier id = ws.getIdentifier();
			
			logger.info("TP SHOWCASE identified: "+id.toString());
			
			if(theOwner.contains(TP_Showcase_Constants.wsCustomerIdentificationName)) {					
				org.w3c.dom.Document doc  = ciAdapter.getXML(data, id.toString());
				SOAPElement headerElement = ciAdapter.getHeader(data,id.toString());				
				
				return new Pair(headerElement,doc);
			}
//				else if(theOwner.contains(TP_Showcase_Constants.wsFormalVerificationName)){
//				org.w3c.dom.Document doc  = fvAdapter.getXML(data, id.toString());
//				SOAPElement headerElement = fvAdapter.getHeader(data,id.toString());				
//					
//				return new Pair(headerElement,doc);	
//					
//			}else if(theOwner.contains(TP_Showcase_Constants.wsTechnicalVerificationName)){
//				org.w3c.dom.Document doc  = tvAdapter.getXML(data, id.toString());
//				SOAPElement headerElement = tvAdapter.getHeader(data,id.toString());				
//					
//				return new Pair(headerElement,doc);	
//			}else if(theOwner.contains(TP_Showcase_Constants.wsContractPreparationName)){
//				org.w3c.dom.Document doc  = cpAdapter.getXML(data, id.toString());
//				SOAPElement headerElement = cpAdapter.getHeader(data,id.toString());				
//					
//				return new Pair(headerElement,doc);
//			}else if(theOwner.contains(TP_Showcase_Constants.wsCourierServiceName)){
//				org.w3c.dom.Document doc  = csAdapter.getXML(data, id.toString());
//				SOAPElement headerElement = csAdapter.getHeader(data,id.toString());				
//					
//				return new Pair(headerElement,doc);
//					
//			}else if(theOwner.contains(TP_Showcase_Constants.wsOrderCreateName)){
//				org.w3c.dom.Document doc  = ocAdapter.getXML(data, id.toString());
//				SOAPElement headerElement = ocAdapter.getHeader(data,id.toString());				
//					
//				return new Pair(headerElement,doc);
//					
//			}else if(theOwner.contains(TP_Showcase_Constants.wsOrderConfirmName)){
//				org.w3c.dom.Document doc  = ocoAdapter.getXML(data, id.toString());
//				SOAPElement headerElement = ocoAdapter.getHeader(data,id.toString());				
//					
//				return new Pair(headerElement,doc);
//			}
			return new Pair(null,null);
		// Do the adaptation for iSOCO case study
		}else if (theOwner.equals("http://iSOCO")) {
			// do iSOCO adaptation here
			try {
				logger.debug("Now translating instance to XML: ");
				Identifier id = ws.getIdentifier();
				System.out.println(id.toString());
				
				org.w3c.dom.Document doc  = isocoAdapter.getXML((Instance)data.get(0), id);
				SOAPElement headerElement = isocoAdapter.getHeader((Instance)data.get(0),id);				
				
				return new Pair(headerElement,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		} else if (theOwner.equals("http://SWS-Challenge-Shipping")) {
			// do SWS-Challenge shipping adaptation in here
			try {
				logger.debug("Now translating instance to XML: ");
								
				org.w3c.dom.Document doc =  swsChallAdapter.getXML(data, grounding.toString());
				SOAPElement headerElement = swsChallAdapter.getHeader(data, grounding.toString());				
				
				return new Pair(headerElement,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		} else if (theOwner.equals("http://SWS-Challenge-Composition")) {
			// do SWS-Challenge simple composition 
			try {
				logger.debug("Now translating instance to XML: ");
								
				org.w3c.dom.Document doc =  swsChallCompositionAdapter.getXML(data, grounding.toString());
				SOAPElement headerElement = swsChallCompositionAdapter.getHeader(data, grounding.toString());				
				
				return new Pair(headerElement,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		} else if (theOwner.equals("http://www.wsmx.org/SWS/MoonPaymentSWS")) {
			// do SWS-Challenge mediation payment scenario 
			try {
				logger.debug("Now translating instance to XML: ");
								
				org.w3c.dom.Document doc =  swsChallPaymentAdapter.getXML(data, grounding.toString());
				SOAPElement headerElement = swsChallPaymentAdapter.getHeader(data, grounding.toString());				
				
				return new Pair(headerElement,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		} else if (theOwner.equals("http://www.wsmo.org/ws/packager")) {
			// do SUPER pre-review Packager service 
			try {
				logger.debug("Now translating instance to XML: ");
				org.w3c.dom.Document doc =  packagerAdapter.getXML(data, grounding.toString());
				SOAPElement headerElement = packagerAdapter.getHeader(data, grounding.toString());
				
				return new Pair(headerElement,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		}
		//Do the adaptation for Moon
		else if (theOwner.equals("http://Moon")) {
			try {
		        Set theInstances = null;
				
            	LegacyAdapter adapter = new LegacyAdapter();
				logger.debug("Now translating instance to XML: ");
				org.w3c.dom.Document doc = adapter.getXML((Instance)data.get(0));
					
				return new Pair(null,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		} 
		//Do the adaptation for OMS
		else if (theOwner.equals("http://oms")) {
			try {
		        Set theInstances = null;
				OMS_Adapter omsAdapter = new OMS_Adapter();
				
				logger.debug("Now translating instance to XML: ");
				org.w3c.dom.Document doc = omsAdapter.getXML((Instance)data.get(0));
					
				return new Pair(null,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		} 
		//Do the adaptation for SUPER Nexcom
		else if(theOwner.equalsIgnoreCase("http://www.super-ip.org/nexcom")){
			try {
				logger.debug("Now translating instance to XML: ");
				org.w3c.dom.Document doc = nexcomAdapter.getXML(data, grounding.toString());
					
				return new Pair(null,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		}
		//Do the adaptation for Infrawebs Euro Car Service
		else if(theOwner.equalsIgnoreCase("http://atos.EuroCarWS")){
			try {

				logger.debug("Now translating instance to XML: ");
				org.w3c.dom.Document doc = carAdapter.getXML((Instance)data.get(0));
					
				return new Pair(null,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		}
		//Do the adaptation for Infrawebs Iberia Flight Service
		else if(theOwner.equalsIgnoreCase("http://atos.IBFlightWS")){
			try {
				
				logger.debug("Now translating instance to XML: ");
				org.w3c.dom.Document doc = flightAdapter.getXML((Instance)data.get(0));
					
				return new Pair(null,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		}
		//Do the adaptation for Infrawebs Hotel Service
		else if(theOwner.equalsIgnoreCase("http://atos.MHotelWS")){
			try {
				
				logger.debug("Now translating instance to XML: ");
				org.w3c.dom.Document doc = hotelAdapter.getXML((Instance)data.get(0));
					
				return new Pair(null,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		}
		//Do the adaptation for Infrawebs Shuttle Service
		else if(theOwner.equalsIgnoreCase("http://atos.ShuttleManWS")){
			try {
				
				logger.debug("Now translating instance to XML: ");
				org.w3c.dom.Document doc = shuttleAdapter.getXML((Instance)data.get(0));
					
				return new Pair(null,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		} else if (theOwner.startsWith("http://Swing")) {
			try {
				Set theInstances = null;
		
				SwingAdapter adapter = new SwingAdapter();
				logger.debug("Now translating instance to XML: ");
				//org.w3c.dom.Document doc = adapter.getXML((Instance)data.get(0));
				org.w3c.dom.Document doc = adapter.getXML(data);
				
				return new Pair(null,doc);
			} catch (Exception e) {
				e.printStackTrace();
				return new Pair(null,null);
			}
		} else if (theOwner.equalsIgnoreCase("http://www.uniriotec.br/aspect/logging")) {
			logger.info("Achou meu Adapter! :D");
			
			try {
				logger.debug("Now translating instance to XML: ");
				logger.info("Transformando as instâncias em XML");
				
				Identifier id = ws.getIdentifier();
				logger.info("Identifier: " + id.toString());
				
				org.w3c.dom.Document doc = loggingAdapter.getXML(data, grounding.toString());
				SOAPElement headerElement = loggingAdapter.getHeader(data, grounding.toString());
				
				return new Pair(headerElement,doc);
				
			} catch (Exception e) {
				logger.error("Não foi possível transformar para XML... :~( ");
				e.printStackTrace();
				return new Pair(null,null);
			}
		} else {
			return new Pair(null,null);
		}
	}

	public WSMLDocument inlineXMLtoWSMLAdapter(WebService ws, EndpointGrounding endpoint, String xmlString) {
		
		// Determine the owner of the WebService and use this to get the correct adapter for lowering
		// A NFP with identifier "http://owner" is used to determine which adaptation code to use
		String theOwner = "";
		try{
			Set theNFPs = ws.listNFPValues(wsmoFactory.createIRI("http://owner"));
			
			if (!theNFPs.isEmpty()) {
				IRI ownerValue = (IRI) theNFPs.iterator().next();
				theOwner = ownerValue.toString(); 
				logger.info("Owner of the Web service: " +theOwner + "\n");
			} else 
				return null;

			String xsltPath = "";

			if(theOwner.contains(TP_Showcase_Constants.tpscOwnerPath)){
				if(theOwner.contains(TP_Showcase_Constants.wsCustomerIdentificationName)){
					return ciAdapter.getWSML(xmlString, endpoint);
//				}else if(theOwner.contains(TP_Showcase_Constants.wsFormalVerificationName)){
//					return fvAdapter.getWSML(xmlString, endpoint);
//				}else if(theOwner.contains(TP_Showcase_Constants.wsTechnicalVerificationName)){
//					return tvAdapter.getWSML(xmlString, endpoint);
//				}else if(theOwner.contains(TP_Showcase_Constants.wsOrderCreateName)){
//					return ocAdapter.getWSML(xmlString, endpoint);
//				}else if(theOwner.contains(TP_Showcase_Constants.wsOrderConfirmName)){
//					return ocoAdapter.getWSML(xmlString, endpoint);
//				}else if(theOwner.contains(TP_Showcase_Constants.wsContractPreparationName)){
//					return cpAdapter.getWSML(xmlString, endpoint);
//				}else if(theOwner.contains(TP_Showcase_Constants.wsCourierServiceName)){
//					return csAdapter.getWSML(xmlString, endpoint);
				}else{
					return null;
				}
			}else if (theOwner.equals("http://iSOCO")) {
				WSMLDocument result = isocoAdapter.getWSML(xmlString, endpoint); 
				return result;
			} else if (theOwner.equals("http://SWS-Challenge-Shipping")) {
				WSMLDocument result = swsChallAdapter.getWSML(xmlString, endpoint); 
				return result;
			} else if (theOwner.equals("http://SWS-Challenge-Composition")) {
				WSMLDocument result = swsChallCompositionAdapter.getWSML(xmlString, endpoint, ws); 
				return result;
			} else if (theOwner.equals("http://www.wsmx.org/SWS/MoonPaymentSWS")) {
				WSMLDocument result = swsChallPaymentAdapter.getWSML(xmlString, endpoint, ws); 
				return result;
			} else if (theOwner.equals("http://www.wsmo.org/ws/packager")) {
				WSMLDocument result = packagerAdapter.getWSML(xmlString, endpoint); 
				return result;
			} else if (theOwner.equals("http://www.super-ip.org/nexcom")){
				WSMLDocument result = nexcomAdapter.getWSML(xmlString, endpoint); 
				return result;
			} else if (theOwner.equals("http://oms")) {
				xsltPath = "resources"+File.separator+"communicationmanager"+File.separator+"OMSAdapter.xslt";
			} else if (theOwner.equals("http://Moon")) {
				xsltPath = "resources"+File.separator+"communicationmanager"+File.separator+"moon2WSML.xsl";
			} else if (theOwner.equalsIgnoreCase("http://atos.EuroCarWS")){
				return carAdapter.getWSML(xmlString, endpoint);
			} else if (theOwner.equalsIgnoreCase("http://atos.IBFlightWS")){
				return flightAdapter.getWSML(xmlString, endpoint);
			} else if (theOwner.equalsIgnoreCase("http://atos.MHotelWS")){
				return hotelAdapter.getWSML(xmlString, endpoint);
			} else if (theOwner.equalsIgnoreCase("http://atos.ShuttleManWS")){
				return shuttleAdapter.getWSML(xmlString, endpoint);
			} else if (theOwner.equals("http://Swing")) {
				xsltPath = "resources"+File.separator+"communicationmanager"+File.separator+"SWING2WSML.xsl";
			} else if (theOwner.equalsIgnoreCase("http://www.uniriotec.br/aspect/logging")) {
				return loggingAdapter.getWSML(xmlString, endpoint);
			} else
				return null;
			
			InputStream xsltFile = new FileInputStream(xsltPath);
			if (xsltFile == null) {
				xsltFile = new FileInputStream(Environment.getKernelLocation()+File.separator+"resources"+File.separator+"communicationmanager"+File.separator+"moon2WSML.xslt");
			}
			
			WSMLDocument wsmlDocument = new WSMLDocument("");
			StringWriter writer = new StringWriter();
	
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t1 = tf.newTransformer(new StreamSource(xsltFile));
			
			
			t1.transform(new StreamSource(new StringReader(xmlString)), new StreamResult(writer));
			//write the results of the transformation to the wsml document
			wsmlDocument.setContent(writer.toString());
			return wsmlDocument;
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public WSMLDocument inlineXMLtoWSMLAdapterHttp(WebService ws, String xmlString) {
		
		// Determine the owner of the WebService and use this to get the correct adapter for lowering
		// A NFP with identifier "http://owner" is used to determine which adaptation code to use
		String theOwner = "";
		try{
			Set theNFPs = ws.listNFPValues(wsmoFactory.createIRI("http://owner"));
		
			if (!theNFPs.isEmpty()) {
				IRI ownerValue = (IRI) theNFPs.iterator().next();
				theOwner = ownerValue.toString(); 
				logger.debug("Owner of the Web service: " +theOwner + "\n");
			} else 
				return null;

			WSMLDocument wsmlDocument = new WSMLDocument("");
			if (theOwner.startsWith("http://Swing")) {
				SwingAdapter adapter = new SwingAdapter();
				logger.debug("Now translating instance to WSML: ");
				wsmlDocument = adapter.getWSML(xmlString, null);
			} 
			else 
				return null;

			return wsmlDocument;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Make a synchronous invocation of the Web service using the list of entities as objects and the
	 * specified operation as the WSDL operation to be used.
	 * 
	 */
	public List<Entity> syncInvokeHTTP(WebService service, List<Entity> data, EndpointGrounding grounding) 
	           throws ComponentException, UnsupportedOperationException {
		try {
			HTTP_EndpointGrounding theGrounding = (HTTP_EndpointGrounding) grounding;
			String UrlStr = theGrounding.getURI();
			logger.info("URI : " + UrlStr);
			
			URLConnection connection = null;
			Set theNFPs = service.listNFPValues(wsmoFactory.createIRI("http://owner"));
			if (!theNFPs.isEmpty()) {
				IRI ownerValue = (IRI) theNFPs.iterator().next();
				if("http://Swing2".equals(ownerValue.toString())) {
					connection = doGet(UrlStr, data, ownerValue);
				} else {
					connection = doPost(UrlStr, service, grounding, data);
				}
			}			

			// Read from URL
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			StringBuffer sb = new StringBuffer("");
						
			while ((line = in.readLine()) != null) {
				sb.append(line + "\n");
			}
			in.close();
			String result = sb.toString();		
			//now get back to WSML
			WSMLDocument wsmlDoc = inlineXMLtoWSMLAdapterHttp(service, result);
			
			TopEntity[] topEnt = null;
			
			try {
				topEnt = parser.parse(new StringReader(wsmlDoc.getContent()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			List<Entity> instances = Helper.getInstances(topEnt);

			return instances;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ComponentException(e);
		}
	}
	/**
	 * Create a GET HTTP connection
	 * 
	 * @param UrlStr The URL of the service
	 * @param data Request instances
	 * @return The connection
	 * @throws IOException
	 */
	private URLConnection doGet(String UrlStr, List<Entity> data, IRI ownerWS) throws IOException {
		String requestString = UrlStr + inlineWSMLtoGETAdapter(UrlStr, data, ownerWS);		
		logger.info("GET request : " + requestString);

		//Open Connection
		URL myUrl = new URL(requestString);				
		URLConnection connection = myUrl.openConnection();
		((HttpURLConnection) connection).setRequestMethod("GET");

		connection.setDoOutput(true);
		connection.setDoInput(true); 
		connection.setUseCaches(false);

		return connection;
	}
	private String inlineWSMLtoGETAdapter(String urlStr, List<Entity> data, IRI ownerWS) {
		String requestString = "";
		if (ownerWS.toString().equals("http://Swing2")) {
		
				Set theInstances = null;
		
				SwingAdapter adapter = new SwingAdapter();
				logger.debug("Now translating instance to GET parameters: ");
				requestString = adapter.getURL(urlStr, data);
		}
		return requestString;
	}

	/**
	 * Create a POST HTTP connection
	 * @param UrlStr
	 * @param service 
	 * @param grounding 
	 * @param data 
	 * @return The connection 
	 * @throws TransformerException
	 * @throws IOException
	 */
	private URLConnection doPost(String UrlStr, WebService service, EndpointGrounding grounding, List<Entity> data) throws TransformerException, IOException {
		// Get the XML document for provided WSML instances
		Pair<SOAPElement,Document> soapParts = inlineWSMLtoXMLAdapter(service, grounding, null, data);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		Document bodyDoc = soapParts.getSecond();
		DOMSource domSource = new DOMSource(bodyDoc);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult inputXMLStream = new StreamResult(bos);
		transformer.transform(domSource, inputXMLStream);
		String wsInput = bos.toString();
		logger.info("POST request : " + wsInput);

		//Open Connection
		URL myUrl = new URL(UrlStr);				
		URLConnection connection = myUrl.openConnection();
		((HttpURLConnection) connection).setRequestMethod("POST");

		connection.addRequestProperty("Content-Type", "text/xml");

		connection.setDoOutput(true);
		connection.setDoInput(true); 
		connection.setUseCaches(false);

		// Write to URL by POST
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
						connection.getOutputStream()));

		output.print(wsInput);
		output.flush();
		output.close();
		return connection;
	}

	public List<Entity> syncInvokeWS(WebService webService, List<Entity> localInsts, String groundingIRI) throws UnsupportedOperationException, ComponentException, WSDL1_1GroundingException {
		if( (groundingIRI.toLowerCase().indexOf("?wsdl") != -1) || (groundingIRI.toLowerCase().indexOf(".wsdl") != -1) ) {
			return syncInvoke(webService, localInsts, new WSDL1_1EndpointGrounding(groundingIRI),null);
		} else {
			return syncInvokeHTTP(webService, localInsts, new HTTP_EndpointGrounding(groundingIRI));
		}
	}

	//utility method converting SOAPMessage to W3C dom Document
	public static Document toDocument(SOAPMessage soapMsg)
			throws ParserConfigurationException, SAXException, SOAPException,
			IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		soapMsg.writeTo(outputStream);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(inputStream);
		return doc;
	}
}

class DynamicBinder {

	private Document xmlModel;
	public String endPoint = "";
	public String serviceName = "";
	public String targetNS = "";
	public String soapAction = "";
	

	public DynamicBinder(String wsdlURIStr)
			throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		
		xmlModel = docBuilder.parse(wsdlURIStr);
	}
		
	public void readDetailsFromWSDL(String PortType, String Operation)
	{
		NodeList nodes; 

		nodes = xmlModel.getElementsByTagName("wsdlsoap:address");
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("soap:address") : nodes;
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("address") : nodes;
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("wsdl:address") : nodes;
		endPoint = nodes.item(0).getAttributes().item(0).getNodeValue().toString();

		nodes = xmlModel.getElementsByTagName("wsdl:service");
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("service") : nodes;
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("soap:service") : nodes;
		serviceName = nodes.item(0).getAttributes().item(0).getNodeValue().toString();
		
		nodes = xmlModel.getElementsByTagName("wsdl:definitions");
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("definitions") : nodes;
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("soap:definitions") : nodes;
		targetNS = nodes.item(0).getAttributes().getNamedItem("targetNamespace").getNodeValue();
	}
	
		
}