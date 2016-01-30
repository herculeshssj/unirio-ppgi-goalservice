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

package org.wsmo.execution.common.component.resourcemanager;

import java.util.Set;

import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.service.WebService;

/**
 * This interface is used to implement a store within WSMX for WSMO4J WebService
 * objects.
 * 
 * The main functionality includes, store, retrieve, remove and search.
 * 
 * <pre>
 *  Created on 10-May-2005
 *  Committed by $Author: maciejzaremba $
 *  $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/resourcemanager/WebServiceResourceManager.java,v $,
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
 * @version $Revision: 1.4 $ $Date: 2007-06-14 14:30:44 $
 */

public interface WebServiceResourceManager {

    /**
     * This method takes a WSMO4J WebService object and stores it within the
     * Resource Manager
     * 
     * @param webService The WebService object to store
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public void storeWebService(WebService webService) throws ComponentException, UnsupportedOperationException;

    /**
     * This method takes a WSMO4J WebService object already within the Resource
     * Manager and removes it from the Resource Manager.
     * 
     * @param webService The WebService object to remove
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public void removeWebService(WebService webService) throws ComponentException, UnsupportedOperationException;
    
    /**
     * Returns the WebService object for all WebServices stored in the Resource
     * Manager
     * 
     * --NOTE: This method should be used sparingly as it may return a huge
     * result set , consider using getIdentifiers() followed by
     * retrieve(Identifier theIdentifier) iteratively--
     * 
     * @return a Set of all the WebService objects in the Resource Manager
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<WebService> retrieveWebServices() throws ComponentException, UnsupportedOperationException;

    public Set<WebService> retrieveWebServices(int discoveryType) throws ComponentException, UnsupportedOperationException;
    
    /**
    * Returns the WebService object for all WebServices in the specified namespace
    * 
    * @param namespace The Namespace of the WebServices to retrieve
    * @return a Set of all the WebService objects in the Resource Manager from the specified Namespace
    * @throws ComponentException
    * @throws UnsupportedOperationException
    */
    public Set<WebService> retrieveWebServices(Namespace namespace) throws ComponentException, UnsupportedOperationException;

    /**
     * Returns the WebServices object for all WebServices which refer (import) specified Ontology
     * 
     * @param Identifier of the ontology
     * @return a Set of all the WebService objects in the Resource Manager
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<WebService> retrieveWebServicesReferringOntology(Identifier ontoIdentifer) throws ComponentException, UnsupportedOperationException;

    
   /**
    * Returns the WebService object for the specified Identifier
    * 
    * @param identifier The Identifier of the WebService to load
    * @return the WebService object of the specified Identifier
    * @throws ComponentException
    * @throws UnsupportedOperationException
    */
    public WebService retrieveWebService(Identifier identifier) throws ComponentException, UnsupportedOperationException;
   
   /**
    * Retrieves all the Namespaces of all the WebServices in the Resource Manager
    * 
    * @return A Set of the Namespaces of the WebServices in the Resource Manager
    * @throws ComponentException
    * @throws UnsupportedOperationException
    */
    public Set<Namespace> getWebServiceNamespaces() throws ComponentException, UnsupportedOperationException;
   
    /**
     * Lists the Identifiers of all the WebServices in the Resource Manager
     * 
     * @return a set of the Identifiers of the WebServices in the Resource Manager
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Identifier> getWebServiceIdentifiers() throws ComponentException, UnsupportedOperationException;

    /**
     * Lists the Identifiers of all the WebServices in the Resource Manager from
     * the specified namespace.
     * 
     * @param namespace The namespace to retrieve from
     * @return a set of the Identifiers of the WebServices in the Resource Manager from the specified namespace
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Identifier> getWebServiceIdentifiers(Namespace namespace) throws ComponentException, UnsupportedOperationException;

    /**
     * This method can be used to tell you if the specified WebService's
     * Identifier is contained within the Resource Manager
     * 
     * @param identifier The identifier of the WebService to search for
     * @return a boolean denoting whether the WebService is present or not.
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public boolean containsWebService(Identifier identifier) throws ComponentException, UnsupportedOperationException;
}
