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
package org.wsmo.execution.common.component.resourcemanager;

import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.WSDLDocument;
import org.wsmo.service.WebService;

/**
 * This interface is used to implement a store within WSMX for mappings between WSMO4J WebService objects and WSML Documents. 
 * 
 * The main functionality includes store, retrieve, remove
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: morcen $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/resourcemanager/WSDLResourceManager.java,v $,
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
 * @version $Revision: 1.2 $ $Date: 2005-11-22 16:49:18 $
 */

public interface WSDLResourceManager {
    
    /**
     * Stores the given WebService WSDLDocument mapping in the Resource Manager
     * 
     * @param webService The WebService as key
     * @param wsdlDocument The WSDLDocument as value
     * 
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public void registerWSDL(WebService webService, WSDLDocument wsdlDocument)
        throws ComponentException, UnsupportedOperationException; 

    /**
     * Retrieves the WSDLDocument corresponding the specified WSDL Document
     * 
     * @param webService The WebService as key
     * @return The WSDLDocument registered against the specified WebService
     * 
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public WSDLDocument getWSDL (WebService webService)
        throws ComponentException, UnsupportedOperationException; 
    
    /**
     * Removes the mapping between the specified WebService and the WSDLDocument 
     * it is mapped against.
     * 
     * @param webService The WebService as key
     * @throws ComponentException 
     * @throws UnsupportedOperationException
     */
    public void deregisterWSDL(WebService webService)
        throws ComponentException, UnsupportedOperationException; 
}
