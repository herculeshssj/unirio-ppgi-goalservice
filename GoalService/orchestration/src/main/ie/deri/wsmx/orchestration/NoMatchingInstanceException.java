/*
 * Copyright (c) 2006 University of Innsbruck, Austria
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

package ie.deri.wsmx.orchestration;

/**
 * TODO Comment this type.
 *
 * <pre>
 * Created on Dec 31, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/orchestration/src/main/ie/deri/wsmx/orchestration/NoMatchingInstanceException.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.2 $ $Date: 2006-09-01 02:13:24 $
 */ 
public class NoMatchingInstanceException extends Exception {

	private static final long serialVersionUID = 2258993472456238071L;

	public NoMatchingInstanceException() {
		super();
	}

	public NoMatchingInstanceException(String message) {
		super(message);
	}

	public NoMatchingInstanceException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoMatchingInstanceException(Throwable cause) {
		super(cause);
	}

}
