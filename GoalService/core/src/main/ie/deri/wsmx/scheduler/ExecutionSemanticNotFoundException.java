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

package ie.deri.wsmx.scheduler;

/**
 * Indicates that a requested execution semantic is not available.
 *
 * <pre>
 * Created on Nov 15, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/ExecutionSemanticNotFoundException.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-11-17 09:29:14 $
 */ 
public class ExecutionSemanticNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7115806613012282790L;

	public ExecutionSemanticNotFoundException() {
		super();
	}

	public ExecutionSemanticNotFoundException(String message) {
		super(message);
	}

	public ExecutionSemanticNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecutionSemanticNotFoundException(Throwable cause) {
		super(cause);
	}

}
