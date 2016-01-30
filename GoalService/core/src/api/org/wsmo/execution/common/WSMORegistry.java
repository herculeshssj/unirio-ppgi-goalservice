/*
 * Copyright (c) 2007 National University of Ireland, Galway
 *                    Open University, Milton Keynes
 *
 * Licensed under MIT License
 * 
 * Permission is hereby granted, free of charge, to any person 
 * obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons 
 * to whom the Software is furnished to do so, subject to the 
 * following conditions:
 *
 * The above copyright notice and this permission notice shall 
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS 
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES 
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT 
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.wsmo.execution.common;

import java.util.Set;

import org.wsmo.common.Entity;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.exception.SystemException;

/**
 * The WSMORegistry interface is a wrapper for all wsmo4j based resource manager
 * that handles the storing of the right entities in the right places. The interface
 * mimics all the interfaces of the resourcemanager class and calls each resourcemanager 
 * in turn amalgamating their results.
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/WSMORegistry.java,v $,
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
 * @version $Revision: 1.3 $ $Date: 2007-06-14 14:26:49 $
 */

public interface WSMORegistry {

    /**
     * Stores the given Entity in the registry 
     * 
     * @param theEntity The Entity to store
     * @throws SystemException
     */
    public void store(Entity theEntity) throws SystemException;
    
    /**
     * Stores the given Entities in the registry 
     * 
     * @param theEntities The Set of Entities to store
     * @throws SystemException
     */
    public void store(Set <Entity> theEntities) throws SystemException;
    
    /**
     * Removes the given Entity from the registry 
     * 
     * @param theEntity The Entity to remove 
     * @throws SystemException
     */
    public void remove(Entity theEntity) throws SystemException;
    
    /**
     * Removes the given Entities from the registry 
     * 
     * @param theEntities
     * @throws SystemException
     */
    public void remove(Set <Entity> theEntities) throws SystemException;
    
    /**
     * Retrieves the Entity with the given Identifier from the registry
     * 
     * @param identifier The Identifier of the requested Entity
     * @return The Entity with the given Identifier
     * @throws SystemException
     */
    public Entity retrieve(Identifier identifier) throws SystemException;
    
    /**
     * Retrieves all the Entities from the given Namespace
     * 
     * @param theNamespace The namespace of the requested Entities
     * @return The Entities with the given namespace
     * @throws SystemException
     */
    public Set <Entity> retrieve(Namespace theNamespace) throws SystemException;
    
    /**
     * Retrieves all the Namespaces of all the Entities in the registry
     * 
     * @return A Set of the Namespaces of the Entities in the registry
     * @throws SystemException
     */
    public Set<Namespace> getNamespaces() throws SystemException;
    
    /**
     * Retrieves all the Identifiers of all the Entities in the Registry
     * 
     * @return The Set of the all Identifiers in the Registry
     * @throws SystemException
     */
    public Set<Identifier> getIdentifiers() throws SystemException;

    /**
     * Lists the Identifiers of all the Entities in the Registry from the specified 
     * namespace.
     * 
     * @param namespace The namespace to retrieve from
     * @return a set of the Identifiers of the Entities from the specified namespace
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Identifier> getIdentifiers(Namespace namespace) throws SystemException;
    
    /**
     * This method can be used to tell you if the specified Identifier is 
     * contained within the Registry
     * 
     * @param identifier The identifier of the Entity to search for
     * @return a boolean denoting whether the Entity is present or not.
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public boolean contains(Identifier identifier) throws SystemException;
}
