/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */
package ie.deri.wsmx.resourcemanager;

import org.wsmo.execution.common.exception.ComponentException;

/**
 * This Exceeption is thrown by Resource Manager if the Identifier of
 * Identifiable is of type AnonnymousID which type is currently not
 * supported by Resource Manager implementation
 * @author Paweł Bugalski
 *
 */
public class UnsupportedIdentifierException extends ComponentException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2502155196568936203L;

	public UnsupportedIdentifierException() {
        super();
    }

    public UnsupportedIdentifierException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedIdentifierException(String message) {
        super(message);
    }

    public UnsupportedIdentifierException(Throwable cause) {
        super(cause);
    }
    
}
