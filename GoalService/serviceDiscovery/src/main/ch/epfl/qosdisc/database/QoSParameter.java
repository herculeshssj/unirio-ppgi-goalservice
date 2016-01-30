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

import java.util.Vector;

/**
 * Describes a specific QoS parameter.
 * 
 * @author Sebastian Gerlach
 */
public class QoSParameter {
	
	/**
	 * Identifier of the parameter in the database.
	 */
	public int id = 0;
	
	/**
	 * IRI of the concept used for defining the parameter.
	 */
	public String iri = "";
	
	/**
	 * Description of the parameter (local name of the IRI).
	 */
	public String description = "";
	
	/**
	 * Comparator for values of this type.
	 */
	public String comparator = ""; 
	
	/**
	 * Valid units for this parameter.
	 */
	public Vector<QoSUnit> units = null;

	/**
	 * The default value for this parameter/requirement.
	 */
	public double defaultValue = 0;
}
