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
import org.wsmo.service.Goal;

/**
 * This interface is used to implement a store within WSMX for WSMO4J Goal
 * objects. 
 * 
 * <pre>
 *  Created on 10-May-2005
 *  Committed by $Author: maitiu_moran $
 *  $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/resourcemanager/GoalResourceManager.java,v $,
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
 * @version $Revision: 1.4 $ $Date: 2005-11-29 16:00:02 $
 */

public interface GoalResourceManager {

    /**
     * This method takes a WSMO4J Goal object and stores it within the
     * Resource Manager
     * 
     * @param goal The Goal object to store
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public void storeGoal(Goal goal) throws ComponentException, UnsupportedOperationException;

    /**
     * This method takes a WSMO4J Goal object already within the Resource
     * Manager and removes it from the Resource Manager.
     * 
     * @param goal The Goal object to remove
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public void removeGoal(Goal goal) throws ComponentException, UnsupportedOperationException;
    
    /**
     * Returns the Goal object for all Goals stored in the Resource
     * Manager
     * 
     * --NOTE: This method should be used sparingly as it may return a huge
     * result set , consider using getGoalIdentifiers() followed by
     * retrieveGoal(Identifier theIdentifier) iteratively--
     * 
     * @return a Set of all the Goal objects in the Resource Manager
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Goal> retrieveGoals() throws ComponentException, UnsupportedOperationException;
    
    /**
     * Returns the Goal object for all Goals in the specified namespace
     * 
     * @param namespace The Namespace of the Goals to retrieve
     * @return a Set of all the Goal objects in the Resource Manager from the specified Namespace
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Goal> retrieveGoals(Namespace namespace) throws ComponentException, UnsupportedOperationException;
    
    /**
     * Returns the Goal object for the specified Identifier
     * 
     * @param identifier The Identifier of the Goal to load
     * @return the Goal object of the specified Identifier
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Goal retrieveGoal(Identifier identifier) throws ComponentException, UnsupportedOperationException;

    /**
     * Retrieves all the Namespaces of all the Goals in the Resource Manager
     * 
     * @return A Set of the Namespaces of the Goals in the Resource Manager
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Namespace> getGoalNamespaces() throws ComponentException, UnsupportedOperationException;

    /**
     * Lists the Identifiers of all the Goals in the Resource Manager
     * 
     * @return a set of the Identifiers of the Goals in the Resource Manager
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Identifier> getGoalIdentifiers() throws ComponentException, UnsupportedOperationException;

    /**
     * Lists the Identifiers of all the Goals in the Resource Manager from
     * the specified namespace.
     * 
     * @param namespace The namespace to retrieve from
     * @return a set of the Identifiers of the Goals in the Resource Manager from the specified namespace
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Identifier> getGoalIdentifiers(Namespace namespace) throws ComponentException, UnsupportedOperationException;

    /**
     * This method can be used to tell you if the specified Goal's
     * Identifier is contained within the Resource Manager
     * 
     * @param identifier The identifier of the Goal to search for
     * @return a boolean denoting whether the Goal is present or not.
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public boolean containsGoal(Identifier identifier) throws ComponentException, UnsupportedOperationException;
}
