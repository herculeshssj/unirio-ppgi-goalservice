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

package com.swschallenge.adapter;

import ie.deri.wsmx.adapter.Adapter;


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

import com.superadapter.PackagerAdapter;

/**
 * Adapter for the VTA_UTC_Converter service as part of DIP project prototypes
 * 
 * <pre>
 *  Created on 15-Feb-2006
 *  Committed by $Author: maciejzaremba $
 *  $Source: /cvsroot/wsmx/components/communicationmanager/src/main/com/swschallenge/adapter/LegacyAdapter.java,v $,
 * </pre>
 * 
 * @author Matthew Moran
 * 
 * @version $Revision: 1.18 $ $Date: 2007/12/13 16:48:52 $
 */

public class LegacyAdapter extends Adapter {

	protected static Logger logger = Logger.getLogger(LegacyAdapter.class);
	
	public LegacyAdapter(String id) {
		super(id);
	}
	public LegacyAdapter() {
		super();
	}

	public org.w3c.dom.Document getXML(Instance instance){
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Set concepts = instance.listConcepts();
			LinkedHashMap attributes = (LinkedHashMap)instance.listAttributeValues();
			Set names = attributes.keySet();
			Iterator attribIterator = names.iterator();
			
			if (concepts.size() != 1) {
                throw new RuntimeException("The instance has more than one concept!");
            }
			Iterator conceptIter = concepts.iterator();
			Concept concept = (Concept) conceptIter.next();
			
			Identifier iri = concept.getIdentifier();
			String iristring = concept.getIdentifier().toString();
			
			String sDoc = "";
			
			// a flag to check if the iri has been matched
			boolean validInstanceFound = false; 
						
			WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
			

			// Handle searchCustomer
			if (iristring.equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#SearchCustomerRequest")){
				validInstanceFound = true;
				
				sDoc += "<SearchCustomer xmlns=\"mooncompany\">\n";
				while(attribIterator.hasNext()){

					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					String searchStringAttValue = values.iterator().next().toString();
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#searchString")){
						sDoc += "     <searchString>" + searchStringAttValue + "</searchString>\n";
					}
				}
				sDoc += "</SearchCustomer>";
			}
			

			// Handle createNewOrder
			if (iristring.equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#CreateOrderRequest")){
				validInstanceFound = true;
				
				sDoc += "<createNewOrder xmlns=\"mooncompany\">\n";
				sDoc += "   <Order>\n";
				while(attribIterator.hasNext()){

					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					String searchStringAttValue = values.iterator().next().toString();
					
					// Get the authentication token
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#authToken")){
						sDoc += "      <authToken>" + searchStringAttValue + "</authToken>\n";
					}
					
					// Expect to get the contact details, shipTo and billTo
					// details
					// First up is the contact
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#contact")){
						sDoc += "      <contact>\n";
						Contact theContact = new Contact();

						// extract the contact instance data
						Identifier contactInstanceID = ((Instance)(values.iterator().next())).getIdentifier();
						Instance contactInstance = (Instance) wsmoFactory.getInstance(contactInstanceID);
						
						// Get the attribute values for the date
						LinkedHashMap contactAttributes = (LinkedHashMap)contactInstance.listAttributeValues();
						Set contactAttibributeNames = contactAttributes.keySet();
						Iterator contactIterator = contactAttibributeNames.iterator();
						while(contactIterator.hasNext()){
							IRI contactAtt = (IRI) contactIterator.next();
							Set contactAttributeValues = (Set) contactAttributes.get((Object) contactAtt);
							String contactAttributeValue = contactAttributeValues.iterator().next().toString();
							if(contactAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#contactName")){
								theContact.setName(contactAttributeValue);
							}
							else if(contactAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#telephone")){
								theContact.setTelephone(contactAttributeValue);
							}
							else if(contactAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#email")){
								theContact.setEmail(contactAttributeValue);
							}
						}
						sDoc += "         <name>" + theContact.getName() + "</name>\n";
						sDoc += "         <telephone>" + theContact.getTelephone() + "</telephone>\n";
						sDoc += "         <email>" + theContact.getEmail() + "</email>\n";
						sDoc += "      </contact>\n";
					}
				
					// Next is the shipTo
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#shipTo")){
						sDoc += "      <shipTo>\n";
						Address shipTo = new Address();

						// extract the contact instance data
						Identifier shipToInstanceID = ((Instance)(values.iterator().next())).getIdentifier();
						Instance shipToInstance = (Instance) wsmoFactory.getInstance(shipToInstanceID);
						
						// Get the attribute values for the date
						LinkedHashMap shipToAttributes = (LinkedHashMap)shipToInstance.listAttributeValues();
						Set shipToAttibributeNames = shipToAttributes.keySet();
						Iterator shipToIterator = shipToAttibributeNames.iterator();
						while(shipToIterator.hasNext()){
							IRI shipToAtt = (IRI) shipToIterator.next();
							Set shipToAttributeValues = (Set) shipToAttributes.get((Object) shipToAtt);
							String shipToAttributeValue = shipToAttributeValues.iterator().next().toString();
							if(shipToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#businessName")){
								shipTo.setBusinessName(shipToAttributeValue);
							}
							else if(shipToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#street")){
								shipTo.setStreet(shipToAttributeValue);
							}
							else if(shipToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#city")){
								shipTo.setCity(shipToAttributeValue);
							}
							else if(shipToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#state")){
								shipTo.setState(shipToAttributeValue);
							}
							else if(shipToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#countryCode")){
								shipTo.setCountryCode(shipToAttributeValue);
							}
							else if(shipToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#postalCode")){
								shipTo.setPostalCode(shipToAttributeValue);
							}
						}
						sDoc += "         <name>" + shipTo.getBusinessName() + "</name>\n";
						sDoc += "         <street>" + shipTo.getStreet() + "</street>\n";
						sDoc += "         <city>" + shipTo.getCity() + "</city>\n";
						sDoc += "         <postalCode>" + shipTo.getPostalCode() + "</postalCode>\n";
						sDoc += "         <country>" + shipTo.getCountryCode() + "</country>\n";
						sDoc += "      </shipTo>\n";
					}
				
					// Next is the billTo
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#billTo")){
						sDoc += "      <billTo>\n";
						Address billTo = new Address();

						// extract the contact instance data
						Identifier billToInstanceID = ((Instance)(values.iterator().next())).getIdentifier();
						Instance billToInstance = (Instance) wsmoFactory.getInstance(billToInstanceID);
						
						// Get the attribute values for the date
						LinkedHashMap billToAttributes = (LinkedHashMap)billToInstance.listAttributeValues();
						Set billToAttibributeNames = billToAttributes.keySet();
						Iterator billToIterator = billToAttibributeNames.iterator();
						while(billToIterator.hasNext()){
							IRI billToAtt = (IRI) billToIterator.next();
							Set billToAttributeValues = (Set) billToAttributes.get((Object) billToAtt);
							String billToAttributeValue = billToAttributeValues.iterator().next().toString();
							if(billToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#businessName")){
								billTo.setBusinessName(billToAttributeValue);
							}
							else if(billToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#street")){
								billTo.setStreet(billToAttributeValue);
							}
							else if(billToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#city")){
								billTo.setCity(billToAttributeValue);
							}
							else if(billToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#state")){
								billTo.setState(billToAttributeValue);
							}
							else if(billToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#countryCode")){
								billTo.setCountryCode(billToAttributeValue);
							}
							else if(billToAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#postalCode")){
								billTo.setPostalCode(billToAttributeValue);
							}
						}
						sDoc += "         <name>" + billTo.getBusinessName() + "</name>\n";
						sDoc += "         <street>" + billTo.getStreet() + "</street>\n";
						sDoc += "         <city>" + billTo.getCity() + "</city>\n";
						sDoc += "         <postalCode>" + billTo.getPostalCode() + "</postalCode>\n";
						sDoc += "         <country>" + billTo.getCountryCode() + "</country>\n";
						sDoc += "      </billTo>\n";
					}

				}
				sDoc += "   </Order>\n";
				sDoc += "</createNewOrder>\n";
			}	
			

			
			// Handle addLineItem
			if (iristring.equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#AddLineItemRequest")){
				validInstanceFound = true;

				sDoc += "<addLineItem xmlns=\"mooncompany\">\n";
				sDoc += "   <LineItem>\n";

				Set lineItemAttValueSet = null;
				while(attribIterator.hasNext()){

					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					String attValue = values.iterator().next().toString();
					
					// Get the authentication token
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#authToken")){
						sDoc += "      <authToken>" + attValue + "</authToken>\n";
					}
					
					// Get the orderId
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#orderId")){
						sDoc += "      <orderId>" + attValue + "</orderId>\n";
					}
					
					// Get the Item details
					// Look for the articleId
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#lineItem"))
						lineItemAttValueSet = values;
				}
				
				sDoc += "      <item>\n";
				LineItem theLineItem = new LineItem();

				// extract the contact instance data
				Identifier itemInstanceID = ((Instance)(lineItemAttValueSet.iterator().next())).getIdentifier();
				Instance itemInstance = (Instance) wsmoFactory.getInstance(itemInstanceID);
				
				// Get the attribute values for the date
				LinkedHashMap itemAttributes = (LinkedHashMap)itemInstance.listAttributeValues();
				Set itemAttibributeNames = itemAttributes.keySet();
				Iterator itemIterator = itemAttibributeNames.iterator();
				while(itemIterator.hasNext()){
					IRI itemAtt = (IRI) itemIterator.next();
					Set itemAttributeValues = (Set) itemAttributes.get((Object) itemAtt);
					String itemAttributeValue = itemAttributeValues.iterator().next().toString();
					if(itemAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#articleId")){
						theLineItem.setArticleId(itemAttributeValue);
					}
					else if(itemAtt.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#quantity")){
						theLineItem.setQuantity(Integer.parseInt(itemAttributeValue));
					}
				}
				sDoc += "         <articleId>" + theLineItem.getArticleId() + "</articleId>\n";
				sDoc += "         <quantity>" + theLineItem.getQuantity() + "</quantity>\n";
				sDoc += "      </item>\n";
				sDoc += "   </LineItem>\n";
				sDoc += "</addLineItem>";

			}
			
			// Handle closeCustomer
			if (iristring.equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#CloseOrderRequest")){
				validInstanceFound = true;
				
				sDoc += "<closeOrder xmlns=\"mooncompany\">\n"+
						"  <CloseOrder>\n";
				while(attribIterator.hasNext()){

					IRI att = (IRI) attribIterator.next();
					Set values = (Set) attributes.get((Object) att);
					String attValue = values.iterator().next().toString();
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#authToken")){
						sDoc += "     <authToken>" + attValue + "</authToken>\n";
					}
					if(att.toString().equalsIgnoreCase("http://www.example.org/ontologies/sws-challenge/MOON#orderId")){
						sDoc += "     <orderId>" + Integer.parseInt(attValue) + "</orderId>\n";
					}
				}
				sDoc += "  </CloseOrder>\n"+
						"</closeOrder>";
			}
			

			// the iri has not been matched
			if (validInstanceFound == false){
				throw new RuntimeException("The type of the instance: " + iristring + " was not recognized. ");
			}

			InputSource is = new InputSource(new StringReader(sDoc));
			org.w3c.dom.Document doc = builder.parse(is);
						
			return doc;
			
		} catch (Exception e) {
			logger.error("Error in getXML");
			return null;
		}
		
	}
	
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint){
		return null;
	}
	
	// Internal class for the OMS ontology contact concept
	class Contact {
		private String name;
		private String telephone;
		private String email;
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getTelephone() {
			return telephone;
		}
		public void setTelephone(String telephone) {
			this.telephone = telephone;
		}
		
	}

	// Internal class for the OMS ontology address concept
	class Address {
		private String businessName;
		private String street;
		private String city;
		private String state;
		private String countryCode;
		private String postalCode;
		public String getBusinessName() {
			return businessName;
		}
		public void setBusinessName(String businessName) {
			this.businessName = businessName;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getCountryCode() {
			return countryCode;
		}
		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}
		public String getPostalCode() {
			return postalCode;
		}
		public void setPostalCode(String postalCode) {
			this.postalCode = postalCode;
		}
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public String getStreet() {
			return street;
		}
		public void setStreet(String street) {
			this.street = street;
		}
	}

	class LineItem {
		private String articleId;
		private int quantity;
		public String getArticleId() {
			return articleId;
		}
		public void setArticleId(String articleId) {
			this.articleId = articleId;
		}
		public int getQuantity() {
			return quantity;
		}
		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}

	
}

