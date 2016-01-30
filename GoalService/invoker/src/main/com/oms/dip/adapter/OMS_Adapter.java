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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  */

package com.oms.dip.adapter;

import ie.deri.wsmx.adapter.Adapter;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.xml.sax.InputSource;

import com.isoco.dip.adapter.WSMLTranslator;
import com.isoco.dip.adapter.XMLTranslator;

/**
 * Adapter for the VTA_UTC_Converter service as part of DIP project prototypes
 *
 * <pre>
 * Created on 15-Feb-2006
 * Committed by $Author: maciejzaremba $
 * $Source: /cvsroot/wsmx/components/communicationmanager/src/main/com/oms/dip/adapter/OMS_Adapter.java,v $,
 * </pre>
 *
 * @author Matthew Moran
 *
 * @version $Revision: 1.16 $ $Date: 2007/12/13 16:48:53 $
 */

public class OMS_Adapter extends Adapter {
	
	protected static Logger logger = Logger.getLogger(OMS_Adapter.class);

	public OMS_Adapter(String id) {
		super(id);
	}
	public OMS_Adapter() {
		super();
	}
	
	public org.w3c.dom.Document getHeader(Instance instance){
		return null;
	}

	public org.w3c.dom.Document getXML(Instance instance){
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Set concepts = instance.listConcepts();
			LinkedHashMap attributes = (LinkedHashMap)instance.listAttributeValues();
			Set names = attributes.keySet();
			logger.debug(names.size());			
			Iterator iterator = names.iterator();
			
			if (concepts.size() != 1) {
                throw new RuntimeException("The instance has more than one concept!");
            }
			Iterator iter = concepts.iterator();
			Concept concept = (Concept) iter.next();
			
			Identifier iri = concept.getIdentifier();
			String iristring = concept.getIdentifier().toString();
			logger.debug("The instance belongs to: " + iristring);			
			
			String sDoc = "";
			
			//a flag to check if the iri has been matched
			boolean validInstanceFound = false; 
						
			WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
			String dateYearValue="", dateMonthValue= "", dateDayValue = "";
			String hourOfDayValue="", minuteOfHourValue= "", secondOfMinuteValue = "";
			
			if (iristring.equalsIgnoreCase("http://www.wsmo.org/ontologies/dateTime#dateAndTime")){
				validInstanceFound = true;
				
				while(iterator.hasNext()){
					IRI att = (IRI) iterator.next();
					Set values = (Set) attributes.get((Object) att);
					// String value = values.iterator().next().toString();

					// expect the first level attributes to be either 'hasDate' or 'hasTime'
					// here's the code to extract the date
					if(att.toString().equalsIgnoreCase("http://www.wsmo.org/ontologies/dateTime#hasDate")){
						sDoc += "<VTATime xmlns=\"http://oms_demo.src\">";

						// extract the date instance data
						Identifier dateInstanceID = ((Instance)(values.iterator().next())).getIdentifier();
						Instance dateInstance = (Instance) wsmoFactory.getInstance(dateInstanceID);
						
						// Get the attribute values for the date
						LinkedHashMap dateAttributes = (LinkedHashMap)dateInstance.listAttributeValues();
						Set dateAttibributeNames = dateAttributes.keySet();
						Iterator dateIterator = dateAttibributeNames.iterator();
						while(dateIterator.hasNext()){
							IRI dateAtt = (IRI) dateIterator.next();
							Set dateAttributeValues = (Set) dateAttributes.get((Object) dateAtt);
							String dateAttributeValue = dateAttributeValues.iterator().next().toString();
							if(dateAtt.toString().equalsIgnoreCase("http://www.wsmo.org/ontologies/dateTime#dayOfMonth")){
								dateDayValue = dateAttributeValue;
							}
							else if(dateAtt.toString().equalsIgnoreCase("http://www.wsmo.org/ontologies/dateTime#monthOfYear")){
								dateMonthValue = dateAttributeValue;
							}
							else if(dateAtt.toString().equalsIgnoreCase("http://www.wsmo.org/ontologies/dateTime#year")){
								dateYearValue = dateAttributeValue;
							}
						}
					}
						
					// here's the code to extract the time
					else if(att.toString().equalsIgnoreCase("http://www.wsmo.org/ontologies/dateTime#hasTime")){
						// extract the time instance data
						Identifier timeInstanceID = ((Instance)(values.iterator().next())).getIdentifier();
						Instance timeInstance = (Instance) wsmoFactory.getInstance(timeInstanceID);
						
						// Get the attribute values for the date
						LinkedHashMap timeAttributes = (LinkedHashMap)timeInstance.listAttributeValues();
						Set timeAttibributeNames = timeAttributes.keySet();
						Iterator timeIterator = timeAttibributeNames.iterator();
						while(timeIterator.hasNext()){
							IRI timeAtt = (IRI) timeIterator.next();
							Set timeAttributeValues = (Set) timeAttributes.get((Object) timeAtt);
							String timeAttributeValue = timeAttributeValues.iterator().next().toString();
							if(timeAtt.toString().equalsIgnoreCase("http://www.wsmo.org/ontologies/dateTime#hourOfDay")){
								hourOfDayValue = timeAttributeValue;
							}
							else if(timeAtt.toString().equalsIgnoreCase("http://www.wsmo.org/ontologies/dateTime#minuteOfHour")){
								minuteOfHourValue = timeAttributeValue;
							}
							else if(timeAtt.toString().equalsIgnoreCase("http://www.wsmo.org/ontologies/dateTime#secondOfMinute")){
								secondOfMinuteValue = timeAttributeValue;
							}
						}
					}
				}
			}
				
			
			//the iri has not been matched
			if (validInstanceFound == false){
				throw new RuntimeException("The type of the instance: " + iristring + " was not recognized. ");
			}
			
			else {
				sDoc+= dateYearValue + " " + dateMonthValue + " " + dateDayValue + " " + hourOfDayValue + " " + minuteOfHourValue + " " + secondOfMinuteValue;
				sDoc+= "</VTATime>";
			}
			
			InputSource is = new InputSource(new StringReader(sDoc));
			org.w3c.dom.Document doc = builder.parse(is);
						
			return doc;
			
		} catch (Exception e) {
			logger.error("Error in processing document");			
			return null;
		}
		
	}
	
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint){
		WSMLDocument wsmlDocument = new WSMLDocument("");
		String initialXML = document;
		
		String translatedXML;
		
		XMLTranslator translator = new XMLTranslator();
		InputStream xsltFile = getClass().getClassLoader().getResourceAsStream("..\\OMS_Adapter.xslt");
		translatedXML = translator.doTransformation(initialXML, xsltFile);
		
		wsmlDocument.setContent(translatedXML);
				
		return wsmlDocument;
	}

}

