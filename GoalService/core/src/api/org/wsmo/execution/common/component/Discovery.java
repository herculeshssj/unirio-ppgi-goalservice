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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omwg.ontology.Ontology;
import org.wsmo.common.Identifier;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;
import org.wsmo.service.WebService;

/**
 * The Discovery interface provides two methods by which Web services matching a specific Goal
 * description can be obtained. 
 * The first method returns a possibly empty list of Web services that match a Goal description. 
 * Services are returned in descending order of how well they match
 * the supplied Goal. The algorithm that determines the matching is part of the implementation 
 * of the method itself.
 * The second method allows a ranking mechanism described as an ontology to be used in the discovery
 * process. The ordering of results is in descending order as in the first method.
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/Discovery.java,v $,
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
 * @author Adina Sirbu
 *
 * @version $Revision: 1.7 $ $Date: 2007-06-14 14:29:34 $
 */

public interface Discovery {
    
    /**
     * Searches the repository for those Web services that match the goal of the requester. 
     * The returned Web services will be ordered according to the degree of confidence that 
     * characterize the match between each Web service and the requester goal. 
     * @param goal the requester’s goal
     * @return a list of Web service able to satisfy the requester’s goal. 
     *         The Web service that represents the most probable match to the requester goal will be on the 
     *         position 0 in the list.
     * @throws ComponentException throws an exception if there is a problem reported during the mediation process
     * @throws UnsupportedOperationException
     */
    public List<WebService> discover(Goal goal)
        throws ComponentException, UnsupportedOperationException;

    
    public List<WebService> discover(Goal goal, Set<WebService> searchSpace)
    	throws ComponentException, UnsupportedOperationException;

    /**
     * This is the interface for QOS discovery within WSMX. The service requester supplies a WSMO Goal and WSMO ontology for search ranking.
     * The QOS discovery locates and matches services to the Goal and uses the ranking ontology to order the results.
     * The interface return parameter is a nested map. The inner map pairs WSMO Web services and WSMO Interfaces that match the supplied WSMO Goal.
     * The outer map pairs each inner map with an identifier of the concept used from the ranking ontology to rank the results.   
     * @param goal the requester’s goal (Java interface)
     * @param rankingOntology The ontology describing the ranking criteria (Java interface)
     * @return Map<Map<WebService, Interface>, Identifier> 
     * 			The inner map contains pairs of Web services and interfaces that satisfy the goal.
     * 			The outer map pairs the inner map with the instance of a concept from the QOS  
     * 			used by the discovery component to order the results.
     * 			For example it might be an instance of a concept representing 'cost-of-use' or 'speed'. 
     * @throws ComponentException throws an exception if there is a problem reported during the mediation process
     * @throws UnsupportedOperationException
     */
    public Map<Map<WebService, Interface>, Identifier> discover(Goal goal, Ontology rankingOntology)
        throws ComponentException, UnsupportedOperationException;
    
    
    
}
