/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *                    Open University, Milton Keynes
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

package org.wsmo.execution.common.exception;

/**
 * Indicates a failure within in a component. Component should throw this or
 * subclasses of this class.
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/exception/ComponentException.java,v $,
 * </pre>
 *
 * @author Michal Zaremba
 * @author Liliana Cabral 
 * @author John Domingue
 * @author David Aiken
 * @author Emilia Cimpian
 * @author Thomas Haselwanter
 * @author Mick Kerrigan
 * @author Adrian Mocan
 * @author Matthew Moran
 * @author Brahmananda Sapkota
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.2 $ $Date: 2005-11-23 22:28:17 $
 */

public class ComponentException extends Exception {

	private static final long serialVersionUID = 8536663099794493891L;

	/**
     *  Constructs a plain component exception.
     */
    public ComponentException() {
        super();
    }

    /**
     * Constructs a component exception with a description
     * of the failure condition.
     * 
     * @param message description
     */
    public ComponentException(String message) {
        super(message);
    }

    /**
     * Constructs a component exception with a description
     * of the failure condition and a nested exception
     * that is the cause.
     * 
     * @param message description
     * @param cause nested exception
     */
    public ComponentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a component exception with a nested
     * exception that is the cause.
     * 
     * @param cause nested exception
     */
    public ComponentException(Throwable cause) {
        super(cause);
    }
}
