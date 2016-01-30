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
import org.wsmo.common.IRI;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.xml.sax.InputSource;

import ie.deri.wsmx.adapter.Adapter;

/**
 * Interface or class description
 * 
 * @author James Scicluna
 * 
 * Created on 12 Dec 2006 Committed by $Author: maciejzaremba $
 * 
 * $Source:
 * /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/FlightServiceAdapter.java,v $,
 * @version $Revision: 1.21 $ $Date: 2007/12/13 16:48:52 $
 * 
 */

public class FlightServiceAdapter extends Adapter {

	/* flightBookingOntology Namespace */
	public static String FLIGHT_BOOKING_NS = "http://www.infrawebs-eu.org/sfs/ontologies/flightBooking#";

	public static String FLIGHT_BOOKING_INPUT_NS = "http://www.infrawebs-eu.org/sfs/ontologies/flightBooking_input#";

	public static String FLIGHT_BOOKING_OUTPUT_NS = "http://www.infrawebs-eu.org/sfs/ontologies/flightBooking_output#";

	/* Real Web Service Namespace (TODO: to check this) */
	public static String WS_NS = "http://sfs.atos.es/flightBooking";

	/*
	 * Identifiers of Concepts - Note that static names for attributes are not
	 * defined since they are not yet fixed
	 */
	public static String BOOKING_REQUEST = "flightBookingRequest";

	public static String FLIGHT_SELECTION = "airTripRequest";

	public static String AVAILABLE_FLIGHTS = "availableAirTrips";

	public static String CONFIRM_FLIGHT = "flightBooking";

	public FlightServiceAdapter(String id) {
		super(id);
	}

	public FlightServiceAdapter() {
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

		if (document.contains("<flightBookingRequest")
				|| document.contains("<airTripRequest")) {
			// Deal with inputs -> Use flightInput.xsl
			xslFileName = "flightInput.xsl";
		} else if (document.contains("<availableAirTrips")
				|| document.contains("<flightBooking")) {
			// Deal with outputs -> Use flightOutput.xsl
			xslFileName = "flightOutput.xsl";
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
					.equalsIgnoreCase(FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS)) {
				// Check for a booking request
				if (conceptIri
						.equalsIgnoreCase(FlightServiceAdapter.FLIGHT_BOOKING_NS
								+ FlightServiceAdapter.BOOKING_REQUEST)) {
					foundMatch = true;
					sDoc = this.handleFlightBookingRequest(instance,
							attributes, iterator);
				}

				if (conceptIri
						.equalsIgnoreCase(FlightServiceAdapter.FLIGHT_BOOKING_NS
								+ FlightServiceAdapter.FLIGHT_SELECTION)) {
					foundMatch = true;
					sDoc = this.handleFlightSelection(instance, attributes,
							iterator);
				}
			} else if (baseIri
					.equalsIgnoreCase(FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS)) {
				// Deal with output messages

				if (conceptIri
						.equalsIgnoreCase(FlightServiceAdapter.FLIGHT_BOOKING_NS
								+ FlightServiceAdapter.AVAILABLE_FLIGHTS)) {
					foundMatch = true;
					sDoc = this.handleAvailableFlights(instance, attributes,
							iterator);
				}

				if (conceptIri
						.equalsIgnoreCase(FlightServiceAdapter.FLIGHT_BOOKING_NS
								+ FlightServiceAdapter.CONFIRM_FLIGHT)) {
					foundMatch = true;
					sDoc = this.handleConfirmFlight(instance, attributes,
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

	private String handleFlightBookingRequest(Instance i,
			LinkedHashMap attributes, Iterator attrIterator) {
		String sDoc = "<flightBookingRequest xmlns=\""
				+ FlightServiceAdapter.WS_NS + "\" instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName() + "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();
			Set values = (Set) attributes.get((Object) att);

			// Get only one value (?)
			String value = values.iterator().next().toString();

			// NOTE: to look for attributes, the input ontology namespace must
			// be used (kinda wierd)

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS
							+ "startLocation")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance locationInst = (Instance) valIter.next();
					sDoc += SfsHelper.handleLocation(
							FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS,
							locationInst, "startLocation");
				}
			}

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS
							+ "endLocation")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance locationInst = (Instance) valIter.next();
					sDoc += SfsHelper.handleLocation(
							FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS,
							locationInst, "endLocation");
				}
			}

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS + "departure")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleDateTime(
							FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS, dt,
							"departure");
				}
			}

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS + "arrival")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleDateTime(
							FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS, dt,
							"arrival");
				}
			}
			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS
							+ "buyerPoints")) {
				sDoc += "<buyerPoints>" + value + "</buyerPoints>";
			}
			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS
							+ "numberOfPersons")) {
				sDoc += "<numberOfPersons>" + value + "</numberOfPersons>";
			}
			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS + "seatClass")) {
				sDoc += "<seatClass>" + value + "</seatClass>";
			}
			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS
							+ "preferredCompany")) {
				sDoc += "<preferredCompany>" + value + "</preferredCompany>";
			}
		}
		sDoc += "</flightBookingRequest>";
		return sDoc;
	}

	private String handleFlightSelection(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<airTripRequest xmlns=\"" + FlightServiceAdapter.WS_NS
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
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS + "buyer")) {
				sDoc += "<buyer>" + value + "</buyer>";
			}

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS
							+ "selectedOutgoingAirTrip")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleAirTripInfo(
							FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS, dt,
							"selectedOutgoingAirTrip");
				}
			}

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS
							+ "selectedReturnAirTrip")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleAirTripInfo(
							FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS, dt,
							"selectedReturnAirTrip");
				}
			}
		}
		sDoc += "</airTripRequest>";
		return sDoc;
	}

	private String handleAvailableFlights(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<availableAirTrips xmlns=\""
				+ FlightServiceAdapter.WS_NS + "\" instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName() + "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS
							+ "availableAirTripInfo")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance availInst;
					while (valIter.hasNext()) {
						availInst = (Instance) valIter.next();
						sDoc += SfsHelper.handleAirTripInfo(
								FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS,
								availInst, "availableAirTripInfo");
					}
				}
			}
		}
		sDoc += "</availableAirTrips>";
		return sDoc;
	}

	private String handleConfirmFlight(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<flightBooking xmlns=\"" + FlightServiceAdapter.WS_NS
				+ "\" instnaceId=\"" + ((IRI) i.getIdentifier()).getLocalName()
				+ "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();
			Set values = (Set) attributes.get((Object) att);

			// Get only one value (?)
			String value = values.iterator().next().toString();

			// NOTE: to look for attributes, the input ontology namespace must
			// be used (kinda wierd)

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS + "seller")) {
				sDoc += "<seller>" + value + "</seller>";
			}

			if (att.toString()
					.equalsIgnoreCase(
							FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS
									+ "bookingId")) {
				sDoc += "<bookingId>" + value + "</bookingId>";
			}

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS
							+ "outgoingAirTripInfo")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleAirTripInfo(
							FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS, dt,
							"outgoingAirTripInfo");
				}
			}

			if (att.toString().equalsIgnoreCase(
					FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS
							+ "returnAirTripInfo")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleAirTripInfo(
							FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS, dt,
							"returnAirTripInfo");
				}
			}

		}
		sDoc += "</flightBooking>";
		return sDoc;
	}

}
