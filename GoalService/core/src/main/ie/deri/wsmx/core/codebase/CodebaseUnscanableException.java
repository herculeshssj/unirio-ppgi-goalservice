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
 * Indicates that a codebase could not be scanned due to
 * I/O errors, invalid path or for other reasons.
 *
 * <pre>
 * Created on Sep 4, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/codebase/CodebaseUnscanableException.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-09-16 19:05:49 $
 */ 
public class CodebaseUnscanableException extends Exception {

	private static final long serialVersionUID = -4398067702744124907L;

	public CodebaseUnscanableException() {
		super();
	}

	public CodebaseUnscanableException(String s) {
		super(s);
	}

	public CodebaseUnscanableException(String message, Throwable cause) {
		super(message, cause);
	}

	public CodebaseUnscanableException(Throwable cause) {
		super(cause);
	}

}
