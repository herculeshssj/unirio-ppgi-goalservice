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
 * WSML Translator for shuttle service input messages
 * 
 * @author James Scicluna
 * 
 * Created on 11 Dec 2006 Committed by $Author: maciejzaremba $
 * 
 * $Source: /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/ShuttleWSMLTranslator.java,v $,
 * @version $Revision: 1.15 $ $Date: 2007/12/13 16:48:52 $
 * 
 */

public class ShuttleWSMLTranslator extends AbstractWSMLTranslator {

	private ShuttleServiceAdapter shuttleAdapter = null;

	public ShuttleWSMLTranslator() {
		super("shuttleBooking_input.wsml","shuttleBooking_output.wsml");

		shuttleAdapter = new ShuttleServiceAdapter();
	}

	public void testWSMLShuttleBookingRequest() {
		Identifier id = factory
				.createIRI(ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS
						+ ShuttleServiceAdapter.BOOKING_REQUEST);
		Instance i = inputOnto.findInstance(id);
		if (i != null) {
			Document doc = shuttleAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.inputMsgsLocation, "shuttleBookingRequest.xml");
		} else
			System.out.println(id.toString() + " not found");
	}

	public void testWSMLShuttleRequest() {
		Identifier id = factory
				.createIRI(ShuttleServiceAdapter.SHUTTLE_BOOKING_INPUT_NS
						+ ShuttleServiceAdapter.SHUTTLE_SELECTION);
		Instance i = inputOnto.findInstance(id);
		if(i != null){
			Document doc = shuttleAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.inputMsgsLocation, "shuttleRequest.xml");
		}else
			System.out.println(id.toString() + " not found");
	}

	public void testWSMLAvailableShuttles() {
		Identifier id = factory
				.createIRI(ShuttleServiceAdapter.SHUTTLE_BOOKING_OUTPUT_NS
						+ ShuttleServiceAdapter.AVAILABLE_SHUTTLES);
		Instance i = outputOnto.findInstance(id);
		if(i != null){
			Document doc = shuttleAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.outputMsgsLocation, "availableShuttles.xml");
		}else
			System.out.println(id.toString() + " not found");
	}

	public void testWSMLConfirmShuttle() {
		Identifier id = factory
				.createIRI(ShuttleServiceAdapter.SHUTTLE_BOOKING_OUTPUT_NS
						+ ShuttleServiceAdapter.CONFIRM_SHUTTLE);
		Instance i = outputOnto.findInstance(id);
		if(i != null){
			Document doc = shuttleAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.outputMsgsLocation, "confirmShuttle.xml");
		}else
			System.out.println(id.toString() + " not found");
	}
	
	public void testXMLShuttleBookingRequest(){
		String doc = loadXMLAsString(this.inputMsgsLocation, "shuttleBookingRequest.xml");
		WSMLDocument wsml = shuttleAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}

	public void testXMLShuttleRequest(){
		String doc = loadXMLAsString(this.inputMsgsLocation, "shuttleRequest.xml");
		WSMLDocument wsml = shuttleAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}
	
	public void testXMLAvailableShuttles(){
		String doc = loadXMLAsString(this.outputMsgsLocation, "availableShuttles.xml");
		WSMLDocument wsml = shuttleAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}
	
	public void testXMLConfirmShuttle(){
		String doc = loadXMLAsString(this.outputMsgsLocation, "confirmShuttle.xml");
		WSMLDocument wsml = shuttleAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}

	public static void main(String[] args) {
		ShuttleWSMLTranslator test = new ShuttleWSMLTranslator();
		test.testWSMLShuttleBookingRequest();
		test.testWSMLShuttleRequest();
		test.testWSMLAvailableShuttles();
		test.testWSMLConfirmShuttle();
		test.testXMLShuttleBookingRequest();
		test.testXMLShuttleRequest();
		test.testXMLAvailableShuttles();
		test.testXMLConfirmShuttle();
	}
}
