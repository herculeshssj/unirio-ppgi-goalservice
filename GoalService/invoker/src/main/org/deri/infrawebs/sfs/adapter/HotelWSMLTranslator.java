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
 * Tester for WSML to XML Translation of the Hotel service inputs.
 * 
 * @author James Scicluna
 * 
 * Created on 12 Dec 2006 Committed by $Author: maciejzaremba $
 * 
 * $Source: /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/HotelWSMLTranslator.java,v $,
 * @version $Revision: 1.16 $ $Date: 2007/12/13 16:48:52 $
 * 
 */

public class HotelWSMLTranslator extends AbstractWSMLTranslator {

	private HotelServiceAdapter hotelAdapter = null;

	public HotelWSMLTranslator() {
		super("hotelBooking_input.wsml","hotelBooking_output.wsml");
		hotelAdapter = new HotelServiceAdapter();
	}

	public void testWSMLHotelBookingRequest() {
		Identifier id = factory
				.createIRI(HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS
						+ HotelServiceAdapter.BOOKING_REQUEST);
		Instance i = inputOnto.findInstance(id);
		if (i != null) {
			Document doc = hotelAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.inputMsgsLocation, "hotelBookingRequest.xml");
		} else
			System.out.println(id.toString() + " not found");
	}

	public void testWSMLHotelSelection() {
		Identifier id = factory
				.createIRI(HotelServiceAdapter.HOTEL_BOOKING_INPUT_NS
						+ HotelServiceAdapter.HOTEL_SELECTION);
		Instance i = inputOnto.findInstance(id);
		if (i != null) {
			Document doc = hotelAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.inputMsgsLocation, "hotelSelection.xml");
		} else
			System.out.println(id.toString() + " not found");
	}

	public void testWSMLAvailableStays() {
		Identifier id = factory
				.createIRI(HotelServiceAdapter.HOTEL_BOOKING_OUTPUT_NS
						+ HotelServiceAdapter.AVAILABLE_STAYS);
		Instance i = outputOnto.findInstance(id);
		if (i != null) {
			Document doc = hotelAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.outputMsgsLocation, "availableStays.xml");
		} else
			System.out.println(id.toString() + " not found");
	}

	public void testWSMLConfirmBooking() {
		Identifier id = factory
				.createIRI(HotelServiceAdapter.HOTEL_BOOKING_OUTPUT_NS
						+ HotelServiceAdapter.CONFIRM_BOOKING);
		Instance i = outputOnto.findInstance(id);
		if (i != null) {
			Document doc = hotelAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.outputMsgsLocation, "confirmHotel.xml");
		} else
			System.out.println(id.toString() + " not found");
	}
	
	public void testXMLHotelBookingRequest(){
		String doc = loadXMLAsString(this.inputMsgsLocation, "hotelBookingRequest.xml");
		WSMLDocument wsml = hotelAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}
	
	public void testXMLHotelSelection(){
		String doc = loadXMLAsString(this.inputMsgsLocation, "hotelSelection.xml");
		WSMLDocument wsml = hotelAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}

	public void testXMLAvailableStays(){
		String doc = loadXMLAsString(this.outputMsgsLocation, "availableStays.xml");
		WSMLDocument wsml = hotelAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}

	public void testXMLConfirmBooking(){
		String doc = loadXMLAsString(this.outputMsgsLocation, "confirmHotel.xml");
		WSMLDocument wsml = hotelAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}

	
	public static void main(String[] args) {
		HotelWSMLTranslator test = new HotelWSMLTranslator();
		test.testWSMLHotelBookingRequest();
		test.testWSMLHotelSelection();
		test.testWSMLAvailableStays();
		test.testWSMLConfirmBooking();
		test.testXMLHotelBookingRequest();
		test.testXMLHotelSelection();
		test.testXMLAvailableStays();
		test.testXMLConfirmBooking();
	}

}
