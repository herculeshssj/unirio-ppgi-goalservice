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

package ie.deri.wsmx.core;

import java.io.File;


/**
 * Indicates that an incetion failed and carries
 * information about the component archive.
 * 
 * <pre>
 * Created on Sep 10, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/InjectionFailedException.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-09-16 19:05:49 $
 */ 
public class InjectionFailedException extends Exception {

	private static final long serialVersionUID = 3090698347825842674L;
	private File codebase;
	
	public InjectionFailedException() {
		super();
	}

	public InjectionFailedException(File codebase) {
		super();
		this.codebase = codebase;		
	}

	public InjectionFailedException(String s) {
		super(s);
	}

	public InjectionFailedException(File codebase, String s) {
		super(s);
		this.codebase = codebase;
	}

	public InjectionFailedException(File codebase, String message, Throwable cause) {
		super(message, cause);
		this.codebase = codebase;
	}

	public InjectionFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	
	public InjectionFailedException(Throwable cause) {
		super(cause);
	}

	public InjectionFailedException(File codebase, Throwable cause) {
		super(cause);
		this.codebase = codebase;
	}

	public File getCodebase() {
		return codebase;
	}

}
