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
 * Indicates a problem with retrieving a view from the kernel.
 *
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * Created on Feb 11, 2005
 * Committed by $Author: mzaremba $
 *
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/exceptions/KernelDataException.java,v $,
 * @version $Revision: 1.5 $ $Date: 2005-07-01 16:48:15 $
 */ 
public class KernelDataException extends WSMXException {

	private static final long serialVersionUID = 3546075848345007929L;
	/**
	 * 
	 */
	public KernelDataException() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param message
	 */
	public KernelDataException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param message
	 * @param cause
	 */
	public KernelDataException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param cause
	 */
	public KernelDataException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
