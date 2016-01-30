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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.omwg.ontology.ComplexDataValue;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Value;
import org.wsmo.common.IRI;

/**
 * Interface or class description
 * 
 * @author James Scicluna
 * 
 * Created on 11 Dec 2006 Committed by $Author: maciejzaremba $
 * 
 * $Source:
 * /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/SfsHelper.java,v $,
 * @version $Revision: 1.20 $ $Date: 2007/12/13 16:48:52 $
 * 
 */

public class SfsHelper {

	/*
	 * Note: the baseNS is needed since it may be either in the input or output
	 * namespace.
	 * 
	 * TODO: Handle Locations
	 */
	public static String handleCarRentalBookingInfo(String baseNS, Instance i,
			String attributeName) {
		String sDoc = "<" + attributeName + " instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName()
				+ "\" conceptName=\"carRentalBookingInfo\">";

		LinkedHashMap attributes = (LinkedHashMap) i.listAttributeValues();
		Set names = attributes.keySet();
		Iterator iterator = names.iterator();

		while (iterator.hasNext()) {
			IRI att = (IRI) iterator.next();
			Set values = (Set) attributes.get((Object) att);
			String value = values.iterator().next().toString();

			if (att.toString().equalsIgnoreCase(baseNS + "driversName")) {
				sDoc += "<driversName>" + value + "</driversName>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "pickupLocation")) {
				Set locations = i.listAttributeValues(att);
				if (!locations.isEmpty()) {
					Iterator locIter = locations.iterator();
					// get one by default
					Instance inst = (Instance) locIter.next();
					sDoc += handleLocation(baseNS, inst, "pickupLocation");
				}
			}
			if (att.toString().equalsIgnoreCase(baseNS + "returnLocation")) {
				Set locations = i.listAttributeValues(att);
				if (!locations.isEmpty()) {
					Iterator locIter = locations.iterator();
					// get one by default
					Instance inst = (Instance) locIter.next();
					sDoc += handleLocation(baseNS, inst, "returnLocation");
				}
			}
			if (att.toString().equalsIgnoreCase(baseNS + "pickupDateAndTime")) {
				Set dates = i.listAttributeValues(att);
				if (!dates.isEmpty()) {
					Iterator datIter = dates.iterator();
					// get one by default
					Instance inst = (Instance) datIter.next();
					sDoc += handleDateTime(baseNS, inst, "pickupDateAndTime");
				}
			}
			if (att.toString().equalsIgnoreCase(baseNS + "returnDateAndTime")) {
				Set dates = i.listAttributeValues(att);
				if (!dates.isEmpty()) {
					Iterator datIter = dates.iterator();
					// get one by default
					Instance inst = (Instance) datIter.next();
					sDoc += handleDateTime(baseNS, inst, "returnDateAndTime");
				}
			}
			if (att.toString().equalsIgnoreCase(baseNS + "hasCar")) {
				Set cars = i.listAttributeValues(att);
				if (!cars.isEmpty()) {
					Iterator carIter = cars.iterator();
					// get one by default
					Instance car = (Instance) carIter.next();
					sDoc += handleCar(baseNS, car, "hasCar");
				}
			}
		}
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}

	public static String handleDateTime(String baseNS, Instance i,
			String attributeName) {
		String sDoc = "<" + attributeName + " instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName()
				+ "\" conceptName=\"dateTime\">";

		LinkedHashMap attributes = (LinkedHashMap) i.listAttributeValues();
		Set names = attributes.keySet();
		Iterator iterator = names.iterator();

		while (iterator.hasNext()) {
			IRI att = (IRI) iterator.next();
			Set values = (Set) attributes.get((Object) att);
			String value = values.iterator().next().toString();

			if (att.toString().equalsIgnoreCase(baseNS + "year")) {
				sDoc += "<year>" + value + "</year>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "month")) {
				sDoc += "<month>" + value + "</month>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "day")) {
				sDoc += "<day>" + value + "</day>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "hour")) {
				sDoc += "<hour>" + value + "</hour>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "minute")) {
				sDoc += "<minute>" + value + "</minute>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "second")) {
				sDoc += "<second>" + value + "</second>";
			}
		}
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}

	public static String handleLocation(String baseNS, Instance i,
			String attributeName) {
		String sDoc = "<" + attributeName + " instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName()
				+ "\" conceptName=\"location\">";

		LinkedHashMap attributes = (LinkedHashMap) i.listAttributeValues();
		Set names = attributes.keySet();
		Iterator iterator = names.iterator();

		while (iterator.hasNext()) {
			IRI att = (IRI) iterator.next();
			Set values = (Set) attributes.get((Object) att);
			String value = values.iterator().next().toString();

			if (att.toString().equalsIgnoreCase(baseNS + "continent")) {
				sDoc += "<continent>" + value + "</continent>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "country")) {
				sDoc += "<country>" + value + "</country>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "city")) {
				sDoc += "<city>" + value + "</city>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "street")) {
				sDoc += "<street>" + value + "</street>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "number")) {
				sDoc += "<number>" + value + "</number>";
			}
		}
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}

	public static String handleShuttleBookingInfo(String baseNS, Instance i,
			String attributeName) {
		String sDoc = "<" + attributeName + " instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName()
				+ "\" conceptName=\"shuttleBookingInfo\">";

		LinkedHashMap attributes = (LinkedHashMap) i.listAttributeValues();
		Set names = attributes.keySet();
		Iterator iterator = names.iterator();

		while (iterator.hasNext()) {
			IRI att = (IRI) iterator.next();
			Set values = (Set) attributes.get((Object) att);
			String value = values.iterator().next().toString();

			if (att.toString().equalsIgnoreCase(baseNS + "pickupAddress")) {
				Set locations = i.listAttributeValues(att);
				if (!locations.isEmpty()) {
					Iterator locIter = locations.iterator();
					// get one by default
					Instance inst = (Instance) locIter.next();
					sDoc += handleLocation(baseNS, inst, "pickupAddress");
				}
			}
			if (att.toString().equalsIgnoreCase(baseNS + "dropoffAddress")) {
				Set locations = i.listAttributeValues(att);
				if (!locations.isEmpty()) {
					Iterator locIter = locations.iterator();
					// get one by default
					Instance inst = (Instance) locIter.next();
					sDoc += handleLocation(baseNS, inst, "dropoffAddress");
				}
			}
			if (att.toString().equalsIgnoreCase(baseNS + "pickupDateAndTime")) {
				Set dates = i.listAttributeValues(att);
				if (!dates.isEmpty()) {
					Iterator datIter = dates.iterator();
					// get one by default
					Instance inst = (Instance) datIter.next();
					sDoc += handleDateTime(baseNS, inst, "pickupDateAndTime");
				}
			}
			if (att.toString().equalsIgnoreCase(baseNS + "transportType")) {
				sDoc += "<transportType>" + value + "</transportType>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "numberOfPersons")) {
				sDoc += "<numberOfPersons>" + value + "</numberOfPersons>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "hasShuttleId")) {
				sDoc += "<hasShuttleId>" + value + "</hasShuttleId>";
			}
		}
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}

	// TODO: Probably to handle Locations
	public static String handleAirTripInfo(String baseNS, Instance i,
			String attributeName) {
		String sDoc = "<" + attributeName + " instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName()
				+ "\" conceptName=\"airTripInfo\">";

		LinkedHashMap attributes = (LinkedHashMap) i.listAttributeValues();
		Set names = attributes.keySet();
		Iterator iterator = names.iterator();

		while (iterator.hasNext()) {
			IRI att = (IRI) iterator.next();
			Set values = (Set) attributes.get((Object) att);
			String value = values.iterator().next().toString();

			if (att.toString()
					.equalsIgnoreCase(baseNS + "airTripId")) {
				sDoc += "<airTripId>" + value
						+ "</airTripId>";
			}

			if (att.toString().equalsIgnoreCase(baseNS + "startLocation")) {
				Set locations = i.listAttributeValues(att);
				if (!locations.isEmpty()) {
					Iterator locIter = locations.iterator();
					// get one by default
					Instance inst = (Instance) locIter.next();
					sDoc += handleLocation(baseNS, inst, "startLocation");
				}
			}

			if (att.toString().equalsIgnoreCase(baseNS + "endLocation")) {
				Set locations = i.listAttributeValues(att);
				if (!locations.isEmpty()) {
					Iterator locIter = locations.iterator();
					// get one by default
					Instance inst = (Instance) locIter.next();
					sDoc += handleLocation(baseNS, inst, "endLocation");
				}
			}

			if (att.toString().equalsIgnoreCase(baseNS + "startAirport")) {
				sDoc += "<startAirport>" + value + "</startAirport>";
			}

			if (att.toString().equalsIgnoreCase(baseNS + "endAirport")) {
				sDoc += "<endAirport>" + value + "</endAirport>";
			}

			if (att.toString().equalsIgnoreCase(baseNS + "departureTime")) {
				Set dates = i.listAttributeValues(att);
				if (!dates.isEmpty()) {
					Iterator datIter = dates.iterator();
					// get one by default
					Instance inst = (Instance) datIter.next();
					sDoc += handleDateTime(baseNS, inst, "departureTime");
				}
			}

			if (att.toString().equalsIgnoreCase(baseNS + "arrivalTime")) {
				Set dates = i.listAttributeValues(att);
				if (!dates.isEmpty()) {
					Iterator datIter = dates.iterator();
					// get one by default
					Instance inst = (Instance) datIter.next();
					sDoc += handleDateTime(baseNS, inst, "arrivalTime");
				}
			}
			if (att.toString().equalsIgnoreCase(baseNS + "numberOfPersons")) {
				sDoc += "<numberOfPersons>" + value + "</numberOfPersons>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "seatClass")) {
				sDoc += "<seatClass>" + value + "</seatClass>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "flightCompany")) {
				sDoc += "<flightCompany>" + value + "</flightCompany>";
			}
		}
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}

	public static String handleHotelStay(String baseNS, Instance i,
			String attributeName) {
		String sDoc = "<" + attributeName + " instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName()
				+ "\" conceptName=\"hotelStay\">";

		LinkedHashMap attributes = (LinkedHashMap) i.listAttributeValues();
		Set names = attributes.keySet();
		Iterator iterator = names.iterator();

		while (iterator.hasNext()) {
			IRI att = (IRI) iterator.next();
			Set values = (Set) attributes.get((Object) att);
			String value = values.iterator().next().toString();

			if (att.toString().equalsIgnoreCase(baseNS + "stayId")) {
				sDoc += "<stayId>" + value + "</stayId>";
			}

			if (att.toString().equalsIgnoreCase(baseNS + "hotel")) {
				Set hotels = i.listAttributeValues(att);
				if (!hotels.isEmpty()) {
					Iterator hotelIter = hotels.iterator();
					// get one by default
					Instance inst = (Instance) hotelIter.next();
					sDoc += handleHotel(baseNS, inst, "hotel");
				}
			}

			if (att.toString().equalsIgnoreCase(baseNS + "room")) {
				Set hotels = i.listAttributeValues(att);
				if (!hotels.isEmpty()) {
					Iterator hotelIter = hotels.iterator();
					// get one by default
					Instance inst = (Instance) hotelIter.next();
					sDoc += handleRoom(baseNS, inst, "room");
				}
			}

			if (att.toString().equalsIgnoreCase(baseNS + "checkIn")) {
				Set hotels = i.listAttributeValues(att);
				if (!hotels.isEmpty()) {
					Iterator hotelIter = hotels.iterator();
					// get one by default
					Instance inst = (Instance) hotelIter.next();
					sDoc += handleDateTime(baseNS, inst, "checkIn");
				}
			}

			if (att.toString().equalsIgnoreCase(baseNS + "checkOut")) {
				Set hotels = i.listAttributeValues(att);
				if (!hotels.isEmpty()) {
					Iterator hotelIter = hotels.iterator();
					// get one by default
					Instance inst = (Instance) hotelIter.next();
					sDoc += handleDateTime(baseNS, inst, "checkOut");
				}
			}
}
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}

	public static String handleBoolean(String baseNS, Value v,
			String attributeName) {
		String sDoc = "<" + attributeName + ">";
		sDoc += ((ComplexDataValue) v).getValue().toString();
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}

	public static String handleCar(String baseNS, Instance i,
			String attributeName) {
		String sDoc = "<" + attributeName + " instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName()
				+ "\" conceptName=\"car\">";

		LinkedHashMap attributes = (LinkedHashMap) i.listAttributeValues();
		Set names = attributes.keySet();
		Iterator iterator = names.iterator();

		while (iterator.hasNext()) {
			IRI att = (IRI) iterator.next();
			Set values = (Set) attributes.get((Object) att);
			Value v = (Value) values.iterator().next();
			String value = v.toString();

			if (att.toString().equalsIgnoreCase(baseNS + "carIdentificator")) {
				sDoc += "<carIdentificator>" + value + "</carIdentificator>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "rentalRate")) {
				sDoc += "<rentalRate>" + value + "</rentalRate>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "doorCount")) {
				sDoc += "<doorCount>" + value + "</doorCount>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "carSize")) {
				sDoc += "<carSize>" + value + "</carSize>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "mileageLimited")) {
				sDoc += handleBoolean(baseNS, v, "mileageLimited");
			}
			if (att.toString().equalsIgnoreCase(baseNS + "hasChildSeat")) {
				sDoc += handleBoolean(baseNS, v, "hasChildSeat");
			}
			if (att.toString().equalsIgnoreCase(baseNS + "hasAirConditioning")) {
				sDoc += handleBoolean(baseNS, v, "hasAirConditioning");
			}
		}
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}

	public static String handleHotel(String baseNS, Instance i,
			String attributeName) {
		String sDoc = "<" + attributeName + " instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName()
				+ "\" conceptName=\"hotel\">";

		LinkedHashMap attributes = (LinkedHashMap) i.listAttributeValues();
		Set names = attributes.keySet();
		Iterator iterator = names.iterator();

		while (iterator.hasNext()) {
			IRI att = (IRI) iterator.next();
			Set values = (Set) attributes.get((Object) att);
			Value v = (Value) values.iterator().next();
			String value = v.toString();

			if (att.toString().equalsIgnoreCase(baseNS + "name")) {
				sDoc += "<name>" + value + "</name>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "stars")) {
				sDoc += "<stars>" + value + "</stars>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "hasRestaurant")) {
				sDoc += handleBoolean(baseNS, v, "hasRestaurant");
			}
			if (att.toString().equalsIgnoreCase(baseNS + "hasLocation")) {
				Set locations = i.listAttributeValues(att);
				if (!locations.isEmpty()) {
					Iterator locIter = locations.iterator();
					// get one by default
					Instance locInst = (Instance) locIter.next();
					sDoc += handleLocation(baseNS, locInst, "hasLocation");
				}
			}
		}
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}

	public static String handleRoom(String baseNS, Instance i,
			String attributeName) {
		String sDoc = "<" + attributeName + " instanceId=\""
				+ ((IRI) i.getIdentifier()).getLocalName()
				+ "\" conceptName=\"room\">";

		LinkedHashMap attributes = (LinkedHashMap) i.listAttributeValues();
		Set names = attributes.keySet();
		Iterator iterator = names.iterator();

		while (iterator.hasNext()) {
			IRI att = (IRI) iterator.next();
			Set values = (Set) attributes.get((Object) att);
			Value v = (Value) values.iterator().next();
			String value = v.toString();

			if (att.toString().equalsIgnoreCase(baseNS + "roomNumber")) {
				sDoc += "<roomNumber>" + value + "</roomNumber>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "pointsPerNight")) {
				sDoc += "<pointsPerNight>" + value + "</pointsPerNight>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "numberOfPersons")) {
				sDoc += "<numberOfPersons>" + value + "</numberOfPersons>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "numberOfBeds")) {
				sDoc += "<numberOfBeds>" + value + "</numberOfBeds>";
			}
			if (att.toString().equalsIgnoreCase(baseNS + "smoking")) {
				sDoc += handleBoolean(baseNS, v, "smoking");
			}
			if (att.toString().equalsIgnoreCase(baseNS + "internet")) {
				sDoc += handleBoolean(baseNS, v, "internet");
			}
		}
		sDoc += "</" + attributeName + ">";
		return sDoc;
	}
}
