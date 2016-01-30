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
 * Created on 11 Dec 2006 Committed by $Author: maciejzaremba $
 * 
 * $Source:
 * /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/ShuttleServiceAdapter.java,v $,
 * @version $Revision: 1.20 $ $Date: 2007/12/13 16:48:52 $
 * 
 */

public class ShuttleServiceAdapter extends Adapter {

	/* carRentalBookingOntology Namespace */
	public static String SHUTTLE_BOOKING_NS = "http://www.infrawebs-eu.org/sfs/ontologies/shuttleBooking#";

	public static String SHUTTLE_BOOKING_INPUT_NS = "http://www.infrawebs-eu.org/sfs/ontologies/shuttleBooking_input#";

	public static String SHUTTLE_BOOKING_OUTPUT_NS = "http://www.infrawebs-eu.org/sfs/ontologies/shuttleBooking_output#";

	/* Real Web Service Namespace (TODO: to check this) */
	public static String WS_NS = "http://sfs.atos.es/shuttleBooking";

	/*
	 * Identifiers of Concepts - Note that static names for attributes are not
	 * defined since they are not yet fixed
	 */
	public static String BOOKING_REQUEST = "shuttleBookingRequest";

	public static String SHUTTLE_SELECTION = "shuttleRequest";

	public static String AVAILABLE_SHUTTLES = "availableShuttles";

	public static String CONFIRM_SHUTTLE = "shuttleBooking";

	public ShuttleServiceAdapter(String id) {
		super(id);
	}

	public ShuttleServiceAdapter() {
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

		if (document.contains("<shuttleBookingRequest")
				|| document.contains("<shuttleRequest")) {
			// Deal with inputs -> Use carRentalInput.xsl
			xslFileName = "shuttleInput.xsl";
		} else if (document.contains("<availableShuttles")
				|| document.contains("<shuttleBooking")) {
			// Deal with outputs -> Use carRentalOutput.xsl
			xslFileName = "shuttleOutput.xsl";
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
					.equalsIgnoreCase(ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS)) {
				// Deal with Inputs

				// Check for a booking request
				if (conceptIri
						.equalsIgnoreCase(ShuttleServiceAdapter.SHUTTLE_BOOKING_NS
								+ ShuttleServiceAdapter.BOOKING_REQUEST)) {
					foundMatch = true;
					sDoc = this.handleShuttleBooking(instance, attributes,
							iterator);
				}

				if (conceptIri
						.equalsIgnoreCase(ShuttleServiceAdapter.SHUTTLE_BOOKING_NS
								+ ShuttleServiceAdapter.SHUTTLE_SELECTION)) {
					foundMatch = true;
					sDoc = this.handleShuttleRequest(instance, attributes,
							iterator);
				}
			} else if (baseIri
					.equalsIgnoreCase(ShuttleServiceAdapter.SHUTTLE_BOOKING_OUTPUT_NS)) {
				// Deal with outputs

				if (conceptIri
						.equalsIgnoreCase(ShuttleServiceAdapter.SHUTTLE_BOOKING_NS
								+ ShuttleServiceAdapter.AVAILABLE_SHUTTLES)) {
					foundMatch = true;
					sDoc = this.handleAvailableShuttles(instance, attributes,
							iterator);
				}

				if (conceptIri
						.equalsIgnoreCase(ShuttleServiceAdapter.SHUTTLE_BOOKING_NS
								+ ShuttleServiceAdapter.CONFIRM_SHUTTLE)) {
					foundMatch = true;
					sDoc = this.handleConfirmShuttle(instance, attributes,
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

	private String handleShuttleBooking(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<shuttleBookingRequest xmlns=\""
				+ ShuttleServiceAdapter.WS_NS + "\" instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName() + "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();
			Set values = (Set) attributes.get((Object) att);

			// Get only one value (?)
			String value = values.iterator().next().toString();

			// NOTE: to look for attributes, the input ontology namespace must
			// be used (kinda wierd)

			if (att.toString().equalsIgnoreCase(
					ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS
							+ "pickupAddress")) {
				Set locations = i.listAttributeValues(att);
				if (!locations.isEmpty()) {
					Iterator locIter = locations.iterator();
					// get one by default
					Instance inst = (Instance) locIter.next();
					sDoc += SfsHelper.handleLocation(
							ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS,
							inst, "pickupAddress");
				}
			}

			if (att.toString().equalsIgnoreCase(
					ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS
							+ "dropoffAddress")) {
				Set locations = i.listAttributeValues(att);
				if (!locations.isEmpty()) {
					Iterator locIter = locations.iterator();
					// get one by default
					Instance inst = (Instance) locIter.next();
					sDoc += SfsHelper.handleLocation(
							ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS,
							inst, "dropoffAddress");
				}
			}

			if (att.toString().equalsIgnoreCase(
					ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS
							+ "pickupDateAndTime")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleDateTime(
							ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS, dt,
							"pickupDateAndTime");
				}
			}

			if (att.toString().equalsIgnoreCase(
					ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS
							+ "buyerPoints")) {
				sDoc += "<buyerPoints>" + value + "</buyerPoints>";
			}
			if (att.toString().equalsIgnoreCase(
					ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS
							+ "numberOfPersons")) {
				sDoc += "<numberOfPersons>" + value + "</numberOfPersons>";
			}
		}
		sDoc += "</shuttleBookingRequest>";
		return sDoc;
	}

	private String handleShuttleRequest(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<shuttleRequest xmlns=\"" + ShuttleServiceAdapter.WS_NS
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
					ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS + "buyer")) {
				sDoc += "<buyer>" + value + "</buyer>";
			}

			if (att.toString().equalsIgnoreCase(
					ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS
							+ "selectedShuttle")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					sDoc += SfsHelper.handleShuttleBookingInfo(
							ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS, dt,
							"selectedShuttle");
				}
			}

		}
		sDoc += "</shuttleRequest>";
		return sDoc;
	}

	private String handleAvailableShuttles(Instance i,
			LinkedHashMap attributes, Iterator attrIterator) {
		String sDoc = "<availableShuttles xmlns=\""
				+ ShuttleServiceAdapter.WS_NS + "\" instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName() + "\">";
		while (attrIterator.hasNext()) {
			IRI att = (IRI) attrIterator.next();

			if (att.toString().equalsIgnoreCase(
					ShuttleServiceAdapter.SHUTTLE_BOOKING_OUTPUT_NS
							+ "availableShuttlesInfo")) {
				Set instValues = i.listAttributeValues(att);
				if (!instValues.isEmpty()) {
					Iterator valIter = instValues.iterator();
					Instance availInst;
					while (valIter.hasNext()) {
						availInst = (Instance) valIter.next();
						sDoc += SfsHelper
								.handleShuttleBookingInfo(
										ShuttleServiceAdapter.SHUTTLE_BOOKING_OUTPUT_NS,
										availInst, "availableShuttlesInfo");
					}
				}
			}
		}
		sDoc += "</availableShuttles>";
		return sDoc;
	}

	private String handleConfirmShuttle(Instance i, LinkedHashMap attributes,
			Iterator attrIterator) {
		String sDoc = "<shuttleBooking xmlns=\"" + ShuttleServiceAdapter.WS_NS
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
					ShuttleServiceAdapter.SHUTTLE_BOOKING_OUTPUT_NS + "seller")) {
				sDoc += "<seller>" + value + "</seller>";
			}

			if (att.toString().equalsIgnoreCase(
					ShuttleServiceAdapter.SHUTTLE_BOOKING_OUTPUT_NS
							+ "bookingId")) {
				sDoc += "<bookingId>" + value + "</bookingId>";
			}

			if (att.toString().equalsIgnoreCase(
					ShuttleServiceAdapter.SHUTTLE_BOOKING_OUTPUT_NS
							+ "hasShuttleBookingInfo")) {
				Set dtValues = i.listAttributeValues(att);
				if (!dtValues.isEmpty()) {
					Instance dt = (Instance) dtValues.iterator().next();
					System.out.println(((IRI) dt.getIdentifier()).toString());
					sDoc += SfsHelper.handleShuttleBookingInfo(
							ShuttleServiceAdapter.SHUTTLE_BOOKING_OUTPUT_NS,
							dt, "hasShuttleBookingInfo");
				}
			}
		}
		sDoc += "</shuttleBooking>";
		return sDoc;
	}

}
