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

import ie.deri.wsmx.adapter.Adapter;

/**
 * Adapter for the Hotel Service
 * 
 * @author James Scicluna
 * 
 * Created on 12 Dec 2006 Committed by $Author: maciejzaremba $
 * 
 * $Source:
 * /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/HotelServiceAdapter.java,v $,
 * @version $Revision: 1.22 $ $Date: 2007/12/13 16:48:52 $
 * 
 */

public class HotelServiceAdapter extends Adapter {

	/* hotelBookingOntology Namespace */
	public static String HOTEL_BOOKING_NS = "http://www.infrawebs-eu.org/sfs/ontologies/hotelBooking#";

	public static String HOTEL_BOOKING_INPUT_NS = "http://www.infrawebs-eu.org/sfs/ontologies/hotelBooking_input#";

	public static String HOTEL_BOOKING_OUTPUT_NS = "http://www.infrawebs-eu.org/sfs/ontologies/hotelBooking_output#";

	/* Real Web Service Namespace (TODO: to check this) */
	public static String WS_NS = "http://sfs.atos.es/hotelBooking";

	/*
	 * Identifiers of Concepts - Note that static names for attributes are not
	 * defined since they are not yet fixed
	 */
	public static String BOOKING_REQUEST = "hotelRoomBookingRequest";

	public static String HOTEL_SELECTION = "hotelStayRequest";

	public static String AVAILABLE_STAYS = "availableHotelStays";

	public static String CONFIRM_BOOKING = "hotelRoomBooking";

	public HotelServiceAdapter(String id) {
		super(id);
	}

	public HotelServiceAdapter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ie.deri.wsmx.adapter.Adapter#getWSML(java.lang.String)
	 */
	@Override
	public WSMLDocument getWSML(String document, EndpointGrounding endpoint) {
		// NOTE: There's some hard-coding checking here

		String xslFileName = "";

		if (document.contains("<hotelRoomBookingRequest")
				|| document.contains("<hotelStayRequest")) {
			// Deal with inputs -> Use hotelInput.xsl
			xslFileName = "hotelInput.xsl";
		} else if (document.contains("<availableHotelStays")
				|| document.contains("<hotelRoomBooking")) {
			// Deal with outputs -> Use carRentalOutput.xsl
			xslFileName = "hotelOutput.xsl";
		}

		WSMLDocument wsmlDocument = new WSMLDocument("");
		XMLTranslator translator = new XMLTranslator();
		InputStream xsltFile = null;
		try {
			xsltFile = new FileInputStream("resources" + File.separator
					+ "communicationmanager" + File.separator + xslFileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wsmlDocument
				.setContent(translator.doTransformation(document, xsltFile));
		return wsmlDocument;
	}

	public WSMLDocument getWSML(String document, String fullXsltFilePath) {
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
					.equalsIgnoreCase(HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS)) {
				// Deal with inputs

				// Check for a booking request
				if (conceptIri
						.equalsIgnoreCase(HotelServiceAdapter.HOTEL_BOOKING_NS
								+ HotelServiceAdapter.BOOKING_REQUEST)) {
					foundMatch = true;
					sDoc = this.handleHotelBooking(instance, attributes,
							iterator);
				}

				if (conceptIri
						.equalsIgnoreCase(HotelServiceAdapter.HOTEL_BOOKING_NS
								+ HotelServiceAdapter.HOTEL_SELECTION)) {
					foundMatch = true;
					sDoc = this.handleHotelSelection(instance, attributes,
							iterator);
				}
			} else if (baseIri
					.equalsIgnoreCase(HotelServiceAdapter.HOTEL_BOOKING_OUTPUT_NS)) {
				// Deal with output messages

				if (conceptIri
						.equalsIgnoreCase(HotelServiceAdapter.HOTEL_BOOKING_NS
								+ HotelServiceAdapter.AVAILABLE_STAYS)) {
					foundMatch = true;
					sDoc = this.handleAvailableStays(instance, attributes,
							iterator);
				}

				if (conceptIri
						.equalsIgnoreCase(HotelServiceAdapter.HOTEL_BOOKING_NS
								+ HotelServiceAdapter.CONFIRM_BOOKING)) {
					foundMatch = true;
					sDoc = this.handleConfirmBooking(instance, attributes,
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

	private String handleHotelBooking(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<hotelRoomBookingRequest xmlns=\""
				+ HotelServiceAdapter.WS_NS + "\" instanceId=\""
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
					HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS
							+ "preferredHotel")) {
				Set hotels = i.listAttributeValues(att);
				if (!hotels.isEmpty()) {
					Iterator hotelIter = hotels.iterator();
					// get one by default
					Instance inst = (Instance) hotelIter.next();
					sDoc += SfsHelper.handleHotel(
							HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS, inst,
							"preferredHotel");
				}
			}

			if (att.toString().equalsIgnoreCase(
					HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS
							+ "preferredRoom")) {
				Set rooms = i.listAttributeValues(att);
				if (!rooms.isEmpty()) {
					Iterator roomIter = rooms.iterator();
					// get one by default
					Instance inst = (Instance) roomIter.next();
					sDoc += SfsHelper.handleRoom(
							HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS, inst,
							"preferredRoom");
				}
			}

			if (att.toString().equalsIgnoreCase(
					HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS + "buyerPoints")) {
				sDoc += "<buyerPoints>" + value + "</buyerPoints>";
			}

			if (att.toString().equalsIgnoreCase(
					HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS + "checkIn")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleDateTime(
							HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS, dt,
							"checkIn");
				}
			}

			if (att.toString().equalsIgnoreCase(
					HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS + "checkOut")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleDateTime(
							HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS, dt,
							"checkOut");
				}
			}
		}
		sDoc += "</hotelRoomBookingRequest>";
		return sDoc;
	}

	private String handleHotelSelection(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<hotelStayRequest xmlns=\"" + HotelServiceAdapter.WS_NS
				+ "\" instanceId=\"" + ((IRI) i.getIdentifier()).getLocalName()
				+ "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();
			Set values = (Set) attributes.get((Object) att);

			// Get only one value (?)
			String value = values.iterator().next().toString();

			// NOTE: to look for attributes, the input ontology namespace must
			// be used (kinda wierd)

			if (att.toString().equalsIgnoreCase(
					HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS + "buyer")) {
				sDoc += "<buyer>" + value + "</buyer>";
			}

			if (att.toString()
					.equalsIgnoreCase(
							HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS
									+ "selectedStay")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					System.out.println(((IRI) dt.getIdentifier()).toString());
					sDoc += SfsHelper.handleHotelStay(
							HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS, dt,
							"selectedStay");
				}
			}
		}
		sDoc += "</hotelStayRequest>";
		return sDoc;
	}

	private String handleAvailableStays(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<availableHotelStays xmlns=\""
				+ HotelServiceAdapter.WS_NS + "\" instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName() + "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();

			if (att.toString().equalsIgnoreCase(
					HotelServiceAdapter.HOTEL_BOOKING_OUTPUT_NS
							+ "availableStays")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance availInst;
					while (valIter.hasNext()) {
						availInst = (Instance) valIter.next();
						sDoc += SfsHelper.handleHotelStay(
								HotelServiceAdapter.HOTEL_BOOKING_OUTPUT_NS,
								availInst, "availableStays");
					}
				}
			}
		}
		sDoc += "</availableHotelStays>";
		return sDoc;
	}

	private String handleConfirmBooking(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<hotelRoomBooking xmlns=\"" + HotelServiceAdapter.WS_NS
				+ "\" instanceId=\"" + ((IRI) i.getIdentifier()).getLocalName()
				+ "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();
			Set values = (Set) attributes.get((Object) att);

			// Get only one value (?)
			String value = values.iterator().next().toString();

			// NOTE: to look for attributes, the input ontology namespace must
			// be used (kinda wierd)

			if (att.toString().equalsIgnoreCase(
					HotelServiceAdapter.HOTEL_BOOKING_OUTPUT_NS + "seller")) {
				sDoc += "<seller>" + value + "</seller>";
			}

			if (att.toString().equalsIgnoreCase(
					HotelServiceAdapter.HOTEL_BOOKING_OUTPUT_NS + "bookingId")) {
				sDoc += "<bookingId>" + value + "</bookingId>";
			}

			if (att.toString().equalsIgnoreCase(
					HotelServiceAdapter.HOTEL_BOOKING_OUTPUT_NS
							+ "hasHotelStay")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					System.out.println(((IRI) dt.getIdentifier()).toString());
					sDoc += SfsHelper.handleHotelStay(
							HotelServiceAdapter.HOTEL_BOOKING_OUTPUT_NS, dt,
							"hasHotelStay");
				}
			}
		}
		sDoc += "</hotelRoomBooking>";
		return sDoc;
	}
}
