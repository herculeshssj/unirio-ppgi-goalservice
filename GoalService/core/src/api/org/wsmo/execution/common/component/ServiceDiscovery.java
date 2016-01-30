/*
 * Copyright (c) 2007 National University of Ireland, Galway
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
import java.util.Map.Entry;

import org.wsmo.common.Entity;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.service.Goal;
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
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/ServiceDiscovery.java,v $,
 * </pre>
 *
 * @author Maciej Zaremba
 *  *
 * @version $Revision: 1.1 $ $Date: 2007-06-14 14:29:34 $
 */

public interface ServiceDiscovery {

    /**
     * Searches the repository for those Web services that match the goal of the requester. 
     * The returned Web services will be ordered according to the degree of confidence that 
     * characterize the match between each Web service and the requester goal. 
     * @param goal the requester’s goal
     * @return a list of ranked and sorted services able to satisfy the requester’s goal  
     *         The Web service that represents the most probable match to the requester goal will be on the 
     *         position 0 in the list.
     * @throws ComponentException throws an exception if there is a problem reported during the mediation process
     * @throws UnsupportedOperationException
     */
    public List<WebService> discoverService(Goal goal, Set<WebService> searchSpace)
        throws ComponentException, UnsupportedOperationException;

    /**
     * Discovery supporting composition of the services as a result of the discovery based on the 
     * single Goal  
     * @return a list of service able to satisfy the requester’s goal
     * 		   for each Web service number of the instances to be used for this 
     * 		   service invocation  
     */
	public List<Map<WebService, List<Entity>>> discoverServiceCompositon(Goal goal, Set<WebService> searchSpace)
    	throws ComponentException, UnsupportedOperationException;

	//discovers services among all known services 
    public List<WebService> discoverService(Goal goal)
    	throws ComponentException, UnsupportedOperationException;

	//discovers services among all known services
	public List<Map<WebService, List<Entity>>> discoverServiceCompositon(Goal goal)
    	throws ComponentException, UnsupportedOperationException;
	
}
