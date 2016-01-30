/*
 * Copyright (c) 2008 National University of Ireland, Galway
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

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DynamicBinder {

	private static Logger logger = Logger.getLogger(DynamicBinder.class);
	private Document xmlModel;
	public String wsdlURI = "";
	public String portType = "";
	public String operation = "";
	public String endPoint = "";
	public String serviceName = "";
	public String targetNS = "";
	public String soapAction = "";
	
	public DynamicBinder(String wsdlURIStr)
			throws ParserConfigurationException, SAXException, IOException {

		this.wsdlURI = wsdlURIStr;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		
		xmlModel = docBuilder.parse(wsdlURIStr);
	}
		
	public void readDetailsFromWSDL(String portType, String operation)
	{
		this.portType = portType;
		this.operation = operation;
		
		NodeList nodes; 

		nodes = xmlModel.getElementsByTagName("wsdlsoap:address");
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("soap:address") : nodes;
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("address") : nodes;
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("wsdl:address") : nodes;
		this.endPoint = nodes.item(0).getAttributes().item(0).getNodeValue().toString();

		nodes = xmlModel.getElementsByTagName("wsdl:service");
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("service") : nodes;
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("soap:service") : nodes;
		this.serviceName = nodes.item(0).getAttributes().item(0).getNodeValue().toString();
		
		nodes = xmlModel.getElementsByTagName("wsdl:definitions");
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("definitions") : nodes;
		nodes = (nodes.item(0)==null) ? xmlModel.getElementsByTagName("soap:definitions") : nodes;
		this.targetNS = nodes.item(0).getAttributes().getNamedItem("targetNamespace").getNodeValue();
	}
}