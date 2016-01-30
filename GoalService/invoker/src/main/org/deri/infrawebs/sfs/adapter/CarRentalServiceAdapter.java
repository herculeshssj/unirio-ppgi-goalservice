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

import ie.deri.wsmx.adapter.Adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Value;
import org.wsmo.common.IRI;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.xml.sax.InputSource;

/**
 * Adapter for the Car Rental Service
 * 
 * @author James Scicluna
 * 
 * Created on 10 Dec 2006 Committed by $Author: maciejzaremba $
 * 
 * $Source:
 * /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/CarRentalServiceAdapter.java,v $,
 * @version $Revision: 1.22 $ $Date: 2007/12/13 16:48:52 $
 * 
 */

public class CarRentalServiceAdapter extends Adapter {

	/* carRentalBookingOntology Namespace */
	public static String CAR_RENTAL_BOOKING_NS = "http://www.infrawebs-eu.org/sfs/ontologies/carRentalBooking#";

	public static String CAR_RENTAL_BOOKING_INPUT_NS = "http://www.infrawebs-eu.org/sfs/ontologies/carRentalBooking_input#";

	public static String CAR_RENTAL_BOOKING_OUTPUT_NS = "http://www.infrawebs-eu.org/sfs/ontologies/carRentalBooking_output#";

	/* Real Web Service Namespace (TODO: to check this) */
	public static String WS_NS = "http://sfs.atos.es/carRentalBooking";

	/*
	 * Identifiers of Concepts - Note that static names for attributes are not
	 * defined since they are not yet fixed
	 */
	public static String BOOKING_REQUEST = "carRentalBookingRequest";

	public static String CAR_SELECTION = "carRentalSelection";

	public static String AVAILABLE_RENTALS = "availableCarRentals";

	public static String BOOKING_CONFIRM = "carRentalBooking";

	public CarRentalServiceAdapter(String id) {
		super(id);
	}

	public CarRentalServiceAdapter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.adapter.Adapter#getWSML(java.lang.String)
	 */
	@Override
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint) {
		
		String xslFileName = "";
		
		if(document.contains("<carRentalBookingRequest") || document.contains("<carRentalSelection")){
			//Deal with inputs -> Use carRentalInput.xsl
			xslFileName = "carRentalInput.xsl";
		}else if(document.contains("<availableCarRentals") || document.contains("<carRentalBooking")){
			//Deal with outputs -> Use carRentalOutput.xsl
			xslFileName = "carRentalOutput.xsl";
		}
		
		
		WSMLDocument wsmlDocument = new WSMLDocument("");
		XMLTranslator translator = new XMLTranslator();
		InputStream xsltFile = null;
		try {
			xsltFile = new FileInputStream("resources" + File.separator
					+ "communicationmanager" + File.separator
					+ xslFileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wsmlDocument.setContent(translator.doTransformation(document, xsltFile));
		return wsmlDocument;
	}
	
	public WSMLDocument getWSML(String document, String fullXsltFilePath){
		WSMLDocument wsmlDocument = new WSMLDocument("");
		XMLTranslator translator = new XMLTranslator();
		InputStream xsltFile = null;
		try {
			xsltFile = new FileInputStream(fullXsltFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		wsmlDocument.setContent(translator.doTransformation(document, xsltFile));
		return wsmlDocument;
		
	}

	public org.w3c.dom.Document getXML(Instance instance) {
		org.w3c.dom.Document doc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();

			Set concepts = instance.listConcepts();
			LinkedHashMap attributes = (LinkedHashMap) instance
					.listAttributeValues();
			Set names = attributes.keySet();
			Iterator iterator = names.iterator();

			if (concepts.size() != 1) {
				throw new RuntimeException(
						"The instance has more than one concept!");
			}
			// Get only the first membership by default
			Iterator iter = concepts.iterator();
			Concept concept = (Concept) iter.next();

			String conceptIri = concept.getIdentifier().toString();
			System.out.println("This instance is a member of: " + conceptIri);

			String baseIri = ((IRI) instance.getIdentifier()).getNamespace();

			String sDoc = "";

			// a flag to check if the iri has been matched
			boolean foundMatch = false;

			if (baseIri
					.equalsIgnoreCase(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS)) {
				// Deal with Inputs

				// Check for a booking request
				if (conceptIri
						.equalsIgnoreCase(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_NS
								+ CarRentalServiceAdapter.BOOKING_REQUEST)) {
					foundMatch = true;
					sDoc = this.handleCarRentalBooking(instance, attributes,
							iterator);
				}

				if (conceptIri
						.equalsIgnoreCase(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_NS
								+ CarRentalServiceAdapter.CAR_SELECTION)) {
					foundMatch = true;
					sDoc = this.handleCarRentalSelection(instance, attributes,
							iterator);
				}
			} else if (baseIri
					.equalsIgnoreCase(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS)) {
				// Deal with outputs

				if (conceptIri
						.equalsIgnoreCase(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_NS
								+ CarRentalServiceAdapter.AVAILABLE_RENTALS)) {
					foundMatch = true;
					sDoc = this.handleAvailableRentals(instance, attributes,
							iterator);
				}

				if (conceptIri
						.equalsIgnoreCase(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_NS
								+ CarRentalServiceAdapter.BOOKING_CONFIRM)) {
					foundMatch = true;
					sDoc = this.handleBookingConfirm(instance, attributes,
							iterator);
				}

			} else {
				throw new RuntimeException(
						"Unable to recongnize namespace of messages");
			}

			// Parse the xml string message to a Document
			if (foundMatch == false) {
				throw new RuntimeException("The type of the instance: "
						+ conceptIri + " was not recognized. ");
			}

			InputSource is = new InputSource(new StringReader(sDoc));
			doc = builder.parse(is);

			return doc;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return doc;
	}

	private String handleCarRentalBooking(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<carRentalBookingRequest xmlns=\""
				+ CarRentalServiceAdapter.WS_NS + "\" instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName() + "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();
			Set values = (Set) attributes.get((Object) att);

			// Get only one value (?)
			Value v = (Value) values.iterator().next();
			String value = v.toString();

			// NOTE: to look for attributes, the input ontology namespace must
			// be used (kinda wierd)

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "allowSendToHome")) {
				sDoc += SfsHelper.handleBoolean(
						CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS, v,
						"allowSendToHome");
			}

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "allowOutlineReturn")) {
				sDoc += SfsHelper.handleBoolean(
						CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS, v,
						"allowOutlineReturn");
			}

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "pickupCity")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance locationInst = (Instance) valIter.next();
					sDoc += SfsHelper
							.handleLocation(
									CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS,
									locationInst, "pickupCity");
				}
			}

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "returnCity")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance locationInst = (Instance) valIter.next();
					sDoc += SfsHelper
							.handleLocation(
									CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS,
									locationInst, "returnCity");
				}
			}

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "pickupDate")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance dateInst = (Instance) valIter.next();
					sDoc += SfsHelper
							.handleDateTime(
									CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS,
									dateInst, "pickupDate");
				}
			}
			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "returnDate")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance dateInst = (Instance) valIter.next();
					sDoc += SfsHelper
							.handleDateTime(
									CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS,
									dateInst, "returnDate");
				}
			}
			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "preferredCar")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance carInst = (Instance) valIter.next();
					sDoc += SfsHelper
							.handleCar(
									CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS,
									carInst, "preferredCar");
				}
			}
			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "buyerPoints")) {
				sDoc += "<buyerPoints>" + value + "</buyerPoints>";
			}
		}
		sDoc += "</carRentalBookingRequest>";
		return sDoc;
	}

	private String handleCarRentalSelection(Instance i,
			LinkedHashMap attributes, Iterator attrIterator) {
		String sDoc = "<carRentalSelection xmlns=\""
				+ CarRentalServiceAdapter.WS_NS + "\" instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName() + "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();
			Set values = (Set) attributes.get((Object) att);

			// Get only one value (?)
			String value = values.iterator().next().toString();

			// NOTE: to look for attributes, the input ontology namespace must
			// be used (kinda wierd)

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "buyer")) {
				sDoc += "<buyer>" + value + "</buyer>";
			}

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS
							+ "selectedCarRental")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance selectionInst = (Instance) valIter.next();
					sDoc += SfsHelper
							.handleCarRentalBookingInfo(
									CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS,
									selectionInst, "selectedCarRental");
				}
			}
		}
		sDoc += "</carRentalSelection>";
		return sDoc;
	}

	private String handleAvailableRentals(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<availableCarRentals xmlns=\""
				+ CarRentalServiceAdapter.WS_NS + "\" instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName() + "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS
							+ "availableCarRentalsInfo")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance availInst;
					while (valIter.hasNext()) {
						availInst = (Instance) valIter.next();
						sDoc += SfsHelper
								.handleCarRentalBookingInfo(
										CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS,
										availInst, "availableCarRentalsInfo");
					}
				}
			}
		}
		sDoc += "</availableCarRentals>";
		return sDoc;
	}

	private String handleBookingConfirm(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<carRentalBooking xmlns=\""
				+ CarRentalServiceAdapter.WS_NS + "\" instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName() + "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();
			Set values = (Set) attributes.get((Object) att);

			// Get only one value (?)
			String value = values.iterator().next().toString();

			// NOTE: to look for attributes, the input ontology namespace must
			// be used (kinda wierd)

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS
							+ "seller")) {
				sDoc += "<seller>" + value + "</seller>";
			}

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS
							+ "buyer")) {
				sDoc += "<buyer>" + value + "</buyer>";
			}

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS
							+ "hasCarRentalBookingInfo")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance selectionInst = (Instance) valIter.next();
					sDoc += SfsHelper
							.handleCarRentalBookingInfo(
									CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS,
									selectionInst, "hasCarRentalBookingInfo");
				}
			}

			if (att.toString().equalsIgnoreCase(
					CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS
							+ "bookingId")) {
				sDoc += "<bookingId>" + value + "</bookingId>";
			}
		}
		sDoc += "</carRentalBooking>";
		return sDoc;
	}
}
