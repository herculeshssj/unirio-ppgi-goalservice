/*
 * Copyright (c) 2006 University of Innsbruck, Austria
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.deri.infrawebs.sfs.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.LineSeparator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.omwg.ontology.Ontology;
import org.w3c.dom.Document;
import org.wsmo.common.TopEntity;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Parser;

/**
 * Provides common methods for testing translations from WSML to XML
 * 
 * @author James Scicluna
 * 
 * Created on 11 Dec 2006 Committed by $Author: maciejzaremba $
 * 
 * $Source: /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/AbstractWSMLTranslator.java,v $,
 * @version $Revision: 1.15 $ $Date: 2007/12/13 16:48:52 $
 * 
 */

public abstract class AbstractWSMLTranslator {
	protected DocumentBuilderFactory domFactory = null;
	Document xmlDocument = null;
	protected WsmoFactory factory = null;;

	protected Parser parser;

	protected String inputMsgsLocation = "resources" + File.separator
			+ "resourcemanager" + File.separator + "messages" + File.separator
			+ "infrawebs" + File.separator + "input" + File.separator;
	
	protected String outputMsgsLocation = "resources" + File.separator
	+ "resourcemanager" + File.separator + "messages" + File.separator
	+ "infrawebs" + File.separator + "output" + File.separator;

	protected String inputOntoName = "";
	protected String outputOntoName = "";

	protected Ontology inputOnto = null;
	protected Ontology outputOnto = null;

	public AbstractWSMLTranslator(String inputOntoName, String outputOntoName) {
		this.inputOntoName = inputOntoName;
		this.outputOntoName = outputOntoName;
		factory = Factory.createWsmoFactory(null);
		parser = Factory.createParser(null);
		domFactory = DocumentBuilderFactory.newInstance();

		try {
			TopEntity[] in = parser.parse(new FileReader(inputMsgsLocation
					+ inputOntoName));
			inputOnto = (Ontology) in[0];
			
			TopEntity[] out = parser.parse(new FileReader(outputMsgsLocation
					+ outputOntoName));
			outputOnto = (Ontology) out[0];
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected void printDocument(Document doc) {
		//NOTE: formatting is not working with System.out
		OutputFormat format = new OutputFormat();
		format.setLineSeparator(LineSeparator.Windows);
		format.setIndenting(true);
		format.setLineWidth(0);
		format.setIndent(3);
		try {
			XMLSerializer serializer = new XMLSerializer(System.out, format);
			serializer.asDOMSerializer();
			serializer.serialize(doc);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected void storeDocument(Document doc, String location, String fName){
		//NOTE: formatting is not working with System.out
		OutputFormat format = new OutputFormat();
		format.setLineSeparator(LineSeparator.Windows);
		format.setIndenting(true);
		format.setLineWidth(0);
		format.setIndent(3);
		format.setEncoding("UTF-8");
		try {
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File(location + fName)), format);
			serializer.asDOMSerializer();
			serializer.serialize(doc);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	protected Document loadDocument(String location, String fName){
		try{
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			xmlDocument = builder.parse(new File(location + fName));
		}catch(Exception ex){
			System.out.println(ex.toString());
		}
		return xmlDocument;
	}
	
	protected String loadXMLAsString(String location, String fName){
		String xml = "";
		try{
			BufferedReader reader = new BufferedReader(new FileReader(location + fName));
			String buffer = "";
			while((buffer=reader.readLine()) != null){
				xml += buffer;
			}
			reader.close();
			xml = xml.trim();
		}catch(Exception ex){
			System.out.println(ex.toString());
		}
		return xml;
	}
}
