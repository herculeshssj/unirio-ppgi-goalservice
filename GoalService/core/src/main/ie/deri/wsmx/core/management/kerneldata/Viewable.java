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

package ie.deri.wsmx.core.management.kerneldata;

import javax.management.JMException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

/**
 * Any part of the microkernel that wants to expose
 * its underlying data or a representation of its state
 * as XML document implements this interface.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: mzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/kerneldata/Viewable.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2005-07-01 16:45:19 $
 */ 
public interface Viewable {
	//TODO do we really want to use DOM specific return values here?
    //FIXME exception wrapper + localised exceptions
	/**
	 * Returns a XML representation, of a certain
	 * part of the microkernel. Implementations
	 * may be parameterized with an arbitrary number
	 * of <code>String</code> arguments.
	 * 
	 * @param args variable number of strings
	 * @return a DOM Document
	 * @throws JMException if retrieving data from the MBeanServer failed
	 * @throws ParserConfigurationException
	 */
	public Document getView(String ... args) throws JMException, ParserConfigurationException;
		
}
