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

package ie.deri.wsmx.exceptions;

/**
 * Indicates that a requested component is not available
 * in this WSMX instance at this time.
 *
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * Created on 27.02.2005
 * Committed by $Author: mzaremba $
 *
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/exceptions/ComponentNotAvailableException.java,v $,
 * @version $Revision: 1.2 $ $Date: 2005-07-01 16:48:15 $
 */
public class ComponentNotAvailableException extends WSMXException {

	private static final long serialVersionUID = 3257852073457628213L;
	/**
	 * 
	 */
	public ComponentNotAvailableException() {
		super();
	}
	/**
	 * @param message
	 */
	public ComponentNotAvailableException(String message) {
		super(message);
	}
	/**
	 * @param message
	 * @param cause
	 */
	public ComponentNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param cause
	 */
	public ComponentNotAvailableException(Throwable cause) {
		super(cause);
	}
}
