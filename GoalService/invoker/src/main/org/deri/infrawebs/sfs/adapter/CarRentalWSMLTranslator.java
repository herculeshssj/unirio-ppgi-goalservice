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
 * Tester class for WSML Messages
 * 
 * @author James Scicluna
 * 
 * Created on 11 Dec 2006 Committed by $Author: maciejzaremba $
 * 
 * $Source: /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/CarRentalWSMLTranslator.java,v $,
 * @version $Revision: 1.17 $ $Date: 2007/12/13 16:48:52 $
 * 
 */

public class CarRentalWSMLTranslator extends AbstractWSMLTranslator{

	private CarRentalServiceAdapter carRentalAdapter = null;

	public CarRentalWSMLTranslator() {
		super("carRentalBooking_input.wsml","carRentalBooking_output.wsml");		
		carRentalAdapter = new CarRentalServiceAdapter();
	}
	
	public void testWSMLCarRentalBookingRequest(){
		Identifier id = factory.createIRI(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS + "carRentalBookingRequest");
		Instance i = inputOnto.findInstance(id);
		if(i != null){
			Document doc = carRentalAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.inputMsgsLocation, "carRentalBookingRequest.xml");
		}else System.out.println(id.toString() + " not found");
	}
	
	public void testWSMLCarRentalSelection(){
		Identifier id = factory.createIRI(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_INPUT_NS + "carRentalSelection");
		Instance i = inputOnto.findInstance(id);
		if(i != null){
			Document doc = carRentalAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.inputMsgsLocation, "carRentalSelection.xml");
		}else System.out.println(id.toString() + " not found");
	}
	
	public void testWSMLAvailableRentals(){
		Identifier id = factory.createIRI(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS + "availableCarRentals");
		Instance i = outputOnto.findInstance(id);
		if(i != null){
			Document doc = carRentalAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.outputMsgsLocation,"availableCars.xml");
		}else System.out.println(id.toString() + " not found");
		
	}
	
	public void testWSMLConfirmedBooking(){
		Identifier id = factory.createIRI(CarRentalServiceAdapter.CAR_RENTAL_BOOKING_OUTPUT_NS + "carRentalBooking");
		Instance i = outputOnto.findInstance(id);
		if(i != null){
			Document doc = carRentalAdapter.getXML(i);
			printDocument(doc);
			storeDocument(doc, this.outputMsgsLocation,"confirmCar.xml");
		}else System.out.println(id.toString() + " not found");
	}
	
	public void testXMLCarRentalBookingRequest(){
		String doc = loadXMLAsString(this.inputMsgsLocation, "carRentalBookingRequest.xml");
		WSMLDocument wsml = carRentalAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}
	
	public void testXMLCarRentalSelection(){
		String xmlSelection = loadXMLAsString(this.inputMsgsLocation, "carRentalSelection.xml");
		WSMLDocument wsmlSelection = carRentalAdapter.getWSML(xmlSelection, (EndpointGrounding)null);
		System.out.println(wsmlSelection.getContent());
	}
	
	public void testXMLAvailableRentals(){
		String doc = loadXMLAsString(this.outputMsgsLocation, "availableCars.xml");
		WSMLDocument wsml = carRentalAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}
	
	public void testXMLConfirmedBooking(){
		String doc = loadXMLAsString(this.outputMsgsLocation, "confirmCar.xml");
		WSMLDocument wsml = carRentalAdapter.getWSML(doc, (EndpointGrounding)null);
		System.out.println(wsml.getContent());
	}
	
	public static void main(String[] args) {
		CarRentalWSMLTranslator test = new CarRentalWSMLTranslator();
		test.testWSMLCarRentalBookingRequest();
		test.testWSMLCarRentalSelection();
		test.testWSMLAvailableRentals();
		test.testWSMLConfirmedBooking();
		test.testXMLCarRentalBookingRequest();
		test.testXMLCarRentalSelection();
		test.testXMLAvailableRentals();
		test.testXMLConfirmedBooking();
	}

}
