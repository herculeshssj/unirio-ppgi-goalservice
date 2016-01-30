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

package ie.deri.wsmx.core.codebase;

/**
 * Indicates that a component archive has an invalid manifest,
 * which could either mean that it is missing or that it does not
 * include all required key/value pairs.
 *
 * <pre>
 * Created on Sep 16, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/codebase/InvalidComponentManifestException.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-09-16 19:05:49 $
 */ 
public class InvalidComponentManifestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidComponentManifestException() {
		super();
	}

	public InvalidComponentManifestException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidComponentManifestException(String message) {
		super(message);
	}

	public InvalidComponentManifestException(Throwable cause) {
		super(cause);
	}

}
