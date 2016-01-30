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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  */

package org.wsmo.execution.common.nonwsmo.grounding;

/**
 * Exception thrown by WSDL1_1EndpointGrouning class
 *
 * <pre>
 * Created on 07-Feb-2006
 * Committed by $Author: maitiu_moran $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/nonwsmo/grounding/WSDL1_1GroundingException.java,v $,
 * </pre>
 *
 * @author Matthew Moran
 *
 * @version $Revision: 1.1 $ $Date: 2006-02-07 12:45:57 $
 */

public class WSDL1_1GroundingException extends Exception {

	private static final long serialVersionUID = 1L;

	public WSDL1_1GroundingException() {
		super();
	}
	
    /**
     * Constructs an exception with a description
     * of the failure condition.
     * 
     * @param message description
     */
    public WSDL1_1GroundingException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with a description
     * of the failure condition and a nested exception
     * that is the cause.
     * 
     * @param message description
     * @param cause nested exception
     */
    public WSDL1_1GroundingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception with a nested
     * exception that is the cause.
     * 
     * @param cause nested exception
     */
    public WSDL1_1GroundingException(Throwable cause) {
        super(cause);
    }
	
}

