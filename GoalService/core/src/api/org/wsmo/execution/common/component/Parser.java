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

package org.wsmo.execution.common.component;

import java.util.Set;

import org.wsmo.common.Entity;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;

/**
 * Parse WSML written in human readable-syntax into the WSMO4J object model which is the 
 * internal representation used by the WSMX architecture.
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maitiu_moran $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/Parser.java,v $,
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
 * @version $Revision: 1.2 $ $Date: 2005-11-29 13:52:57 $
 */

public interface Parser {
 
	/**
	 * Parse the WSML contained in the WSMLDocument class as a string in human-readable form
	 * into WSMO4j objects that can be used across all components in the WSMX architecture. 
     * @param the WSMLDocument object containing the WSML to be parsed
     * @return a set of WSMO4j objects representing the WSML data in terms of the WSMO4j object model
     * @throws ComponentException throws an exception if there is a problem reported during the parsing process
     * @throws UnsupportedOperationException throws an exception if an unexpected exception occurs 
	 */
    public Set<Entity> parse(WSMLDocument wsmlDocument)
        throws ComponentException, UnsupportedOperationException;
}
