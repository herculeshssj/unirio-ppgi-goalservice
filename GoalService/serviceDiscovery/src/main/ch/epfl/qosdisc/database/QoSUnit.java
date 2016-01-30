/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
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

package ch.epfl.qosdisc.database;


/**
 * Describes a specific QoS unit.
 * 
 * @author Sebastian Gerlach
 */
public class QoSUnit {

	/**
	 * IRI of the unit.
	 */
	public String iri;
	
	/**
	 * Description of the unit.
	 */
	public String description;
	
	/**
	 * Conversion factor for the unit.
	 */
	public double conversionFactor;
	
	/**
	 * Simple constructor.
	 * 
	 * @param iri IRI of this unit.
	 * @param desc Description of this unit.
	 */
	public QoSUnit(String iri, String desc, double cf) {
		
		this.iri = iri;
		this.description = desc;
		this.conversionFactor = cf;
	}
}
