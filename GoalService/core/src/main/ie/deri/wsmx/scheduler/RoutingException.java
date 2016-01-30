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
 * Indicates a routing failure, which typically occurs
 * when an execution semantic gets a handle to an
 * unexpected component type.
 *
 * <pre>
 * Created on Sep 10, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/RoutingException.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.2 $ $Date: 2005-11-14 00:22:49 $
 */ 
public class RoutingException extends Exception {

	private static final long serialVersionUID = 2903675517044315445L;
	private Class expectedType;
	private Class actualType;
	
	public RoutingException() {
		super();
	}

	public RoutingException(Class expectedType, Class actualType) {
		super();
		this.expectedType = expectedType;
		this.actualType = actualType;
	}

	public RoutingException(Class expectedType, Class actualType, String message) {
		super(message);
		this.expectedType = expectedType;
		this.actualType = actualType;

	}

	public RoutingException(Class expectedType, Class actualType, String message, Throwable cause) {
		super(message, cause);
		this.expectedType = expectedType;
		this.actualType = actualType;
	}

	public RoutingException(Throwable cause) {
		super(cause);
	}

	public RoutingException(Class expectedType, Class actualType, Throwable cause) {
		super(cause);
		this.expectedType = expectedType;
		this.actualType = actualType;
	}

	public Class getActualType() {
		return actualType;
	}

	public Class getExpectedType() {
		return expectedType;
	}

}
