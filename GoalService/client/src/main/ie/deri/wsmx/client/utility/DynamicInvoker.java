/*
 * Copyright (c) 2005 National University of Ireland, Galway
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */

package ie.deri.wsmx.client.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.wsmo.execution.common.exception.ComponentException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DynamicInvoker {

	private static Logger logger = Logger.getLogger(DynamicInvoker.class);
	
	static {
		logger.setLevel(Level.ALL);
	}
	
	public Document invokeMethod(DynamicBinder dBinder, Document inputDoc) throws ComponentException 
	{	
		try {
			logger.info("URI : " + dBinder.wsdlURI + "\n porttype: "+dBinder.portType + " \n operation: " + dBinder.operation + "\n endpoint: " + dBinder.endPoint);

			QName serviceName = new QName(dBinder.targetNS,dBinder.serviceName);
			Service serv = Service.create(serviceName);
			
			QName portQName = new QName(dBinder.targetNS, dBinder.portType);
			serv.addPort(portQName, SOAPBinding.SOAP11HTTP_BINDING, dBinder.endPoint);

			MessageFactory factory = MessageFactory.newInstance();
	        SOAPMessage message = factory.createMessage();
	        
	        //TODO: add namespaces declared on the SOAPBody level to the envelope
	        message.getSOAPPart().getEnvelope().addNamespaceDeclaration("tns1", dBinder.targetNS);
            message.getSOAPBody().addDocument(inputDoc);
            message.getMimeHeaders().addHeader("SOAPAction", dBinder.operation);
            message.saveChanges();

            Dispatch<SOAPMessage> smDispatch = serv.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);

            List<Handler> handlers =  smDispatch.getBinding().getHandlerChain();
	        handlers.add(new SOAPLoggingHandler());
	        smDispatch.getBinding().setHandlerChain(handlers);   
	        
	        SOAPMessage soapResponse = null;
	        try {
	        	soapResponse = smDispatch.invoke(message);
	        } catch (Exception e){
	        	logger.error("Fault has been returned in SOAP message!!!");
	        	return null;
	        }
	        
			return toDocument(soapResponse);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ComponentException(e.getMessage());
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