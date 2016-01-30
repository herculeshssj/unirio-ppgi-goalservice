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

import org.omwg.ontology.Instance;
import org.w3c.dom.Document;
import org.wsmo.common.Identifier;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;

/**
 * Flight Service WSML to XML Translator
 * 
 * @author James Scicluna
 * 
 * Created on 12 Dec 2006 
 * Committed by $Author: maciejzaremba $ 
 *
 * $Source: /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/FlightWSMLTranslator.java,v $, 
 * @version $Revision: 1.15 $ $Date: 2007/12/13 16:48:52 $
 * 
 **/

public class FlightWSMLTranslator extends AbstractWSMLTranslator {
	
	private FlightServiceAdapter flightAdapter = null;

	public FlightWSMLTranslator(){
		super("flightBooking_input.wsml","flightBooking_output.wsml");
		flightAdapter = new FlightServiceAdapter();
	}
	
	public void testWSMLFlightBookingRequest() {
		Identifier id = factory
				.createIRI(FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS
						+ FlightServiceAdapter.BOOKING_REQUEST);
		Instance i = inputOnto.findInstance(id);
		if (i != null) {
			Document doc = flightAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc,this.inputMsgsLocation, "flightBookingRequest.xml");
		} else
			System.out.println(id.toString() + " not found");
	}

	public void testWSMLFlightSelection() {
		Identifier id = factory
				.createIRI(FlightServiceAdapter.FLIGHT_BOOKING_INPUT_NS
						+ FlightServiceAdapter.FLIGHT_SELECTION);
		Instance i = inputOnto.findInstance(id);
		if(i != null){
			Document doc = flightAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc,this.inputMsgsLocation, "flightSelection.xml");
		}else
			System.out.println(id.toString() + " not found");
	}

	public void testWSMLAvailableFlights() {
		Identifier id = factory
				.createIRI(FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS
						+ FlightServiceAdapter.AVAILABLE_FLIGHTS);
		Instance i = outputOnto.findInstance(id);
		if(i != null){
			Document doc = flightAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.outputMsgsLocation, "availableFlights.xml");
		}else
			System.out.println(id.toString() + " not found");
	}

	public void testWSMLConfirmBooking() {
		Identifier id = factory
				.createIRI(FlightServiceAdapter.FLIGHT_BOOKING_OUTPUT_NS
						+ FlightServiceAdapter.CONFIRM_FLIGHT);
		Instance i = outputOnto.findInstance(id);
		if(i != null){
			Document doc = flightAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.outputMsgsLocation, "confirmFlight.xml");
		}else
			System.out.println(id.toString() + " not found");
	}
	
	public void testXMLFlightBookingRequest(){
		String doc = loadXMLAsString(this.inputMsgsLocation, "flightBookingRequest.xml");
		WSMLDocument wsml = flightAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}

	public void testXMLFlightSelection(){
		String doc = loadXMLAsString(this.inputMsgsLocation, "flightSelection.xml");
		WSMLDocument wsml = flightAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}
	
	public void testXMLAvailableFlights(){
		String doc = loadXMLAsString(this.outputMsgsLocation, "availableFlights.xml");
		WSMLDocument wsml = flightAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}

	public void testXMLConfirmBooking(){
		String doc = loadXMLAsString(this.outputMsgsLocation, "confirmFlight.xml");
		WSMLDocument wsml = flightAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}

	public static void main(String[] args) {
		FlightWSMLTranslator test = new FlightWSMLTranslator();
		test.testWSMLFlightBookingRequest();
		test.testWSMLFlightSelection();
		test.testWSMLAvailableFlights();
		test.testWSMLConfirmBooking();
		test.testXMLFlightBookingRequest();
		test.testXMLFlightSelection();
		test.testXMLAvailableFlights();
		test.testXMLConfirmBooking();
	}
}
