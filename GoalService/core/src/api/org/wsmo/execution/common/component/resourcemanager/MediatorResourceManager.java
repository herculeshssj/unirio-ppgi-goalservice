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

import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.mediator.Mediator;
import org.wsmo.mediator.OOMediator;

/**
 * This interface is used to implement a store within WSMX for WSMO4J Mediator
 * objects.
 * 
 * <pre>
 *  Created on 10-May-2005
 *  Committed by $Author: maitiu_moran $
 *  $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/resourcemanager/MediatorResourceManager.java,v $,
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
 * @version $Revision: 1.4 $ $Date: 2005-11-29 16:00:59 $
 */

public interface MediatorResourceManager {

    /**
     * This method takes a WSMO4J Mediator object and stores it within the
     * Resource Manager
     * 
     * @param mediator The Mediator object to store
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public void storeMediator(Mediator mediator) throws ComponentException, UnsupportedOperationException;

    /**
     * This method takes a WSMO4J Mediator object already within the Resource
     * Manager and removes it from the Resource Manager.
     * 
     * @param mediator The Mediator object to remove
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public void removeMediator(Mediator mediator) throws ComponentException, UnsupportedOperationException;

    /**
     * Returns the Mediator object for all Mediators stored in the Resource
     * Manager
     * 
     * --NOTE: This method should be used sparingly as it may return a huge
     * result set , consider using getMediatorIdentifiers() followed by
     * retrieveMediator(Identifier theIdentifier) iteratively--
     * 
     * @return a Set of all the Mediator objects in the Resource Manager
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Mediator> retrieveMediators() throws ComponentException, UnsupportedOperationException;

    /**
     * Returns the Mediator object for all Mediators in the specified namespace
     * 
     * @param namespace The Namespace of the Mediators to retrieve
     * @return a Set of all the Mediator objects in the Resource Manager from the specified Namespace
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Mediator> retrieveMediators(Namespace namespace) throws ComponentException, UnsupportedOperationException;
    
    /**
     * Returns the Mediator object for the specified Identifier
     * 
     * @param identifier The Identifier of the Mediator to load
     * @return the Mediator object of the specified Identifier
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Mediator retrieveMediator(Identifier identifier) throws ComponentException, UnsupportedOperationException;
    
    /**
     * Retrieves all the Namespaces of all the Mediators in the Resource Manager
     * 
     * @return A Set of the Namespaces of the Mediators in the Resource Manager
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Namespace> getMediatorNamespaces() throws ComponentException, UnsupportedOperationException;

    /**
     * Lists the Identifiers of all the Mediators in the Resource Manager
     * 
     * @return a set of the Identifiers of the Mediators in the Resource Manager
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Identifier> getMediatorIdentifiers() throws ComponentException, UnsupportedOperationException;

    /**
     * Lists the Identifiers of all the Mediators in the Resource Manager from
     * the specified namespace.
     * 
     * @param namespace The namespace to retrieve from
     * @return a set of the Identifiers of the Mediators in the Resource Manager from the specified namespace
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Identifier> getMediatorIdentifiers(Namespace namespace) throws ComponentException, UnsupportedOperationException;

    /**
     * This method can be used to tell you if the specified Mediator's
     * Identifier is contained within the Resource Manager
     * 
     * @param identifier The identifier of the Mediator to search for
     * @return a boolean denoting whether the Mediator is present or not.
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public boolean containsMediator(Identifier identifier) throws ComponentException, UnsupportedOperationException;
    
    public OOMediator retrieveOOMediator(IRI sourceOntologyIRI, IRI targetOntologyIRI) throws ComponentException, UnsupportedOperationException;
    public boolean containsOOMediator(IRI sourceOntologyIRI, IRI targetOntologyIRI) throws ComponentException, UnsupportedOperationException;
    
}