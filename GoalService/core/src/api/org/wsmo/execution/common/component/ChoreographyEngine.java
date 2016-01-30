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

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.omwg.ontology.Instance;
import org.wsmo.common.Entity;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.ResponseModifierInterface;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;
import org.wsmo.service.WebService;

/**
 * In a conversation the requesters choreography will be registered and an unbound number of provider choreographies 
 * may be registered if suitable web service are discovered.   
 * If we receive messages during an ongoing conversation, either from the provider of a service or from the 
 * requestor of the service, the ChoreographyEngine has to update its internal state and determine if the
 * resulting state is valid. For this purpose the upstateState method is used.
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/ChoreographyEngine.java,v $,
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
 * @version $Revision: 1.7 $ $Date: 2007-06-14 14:29:34 $
 */
public interface ChoreographyEngine {
    
    /**
     * When a goal is received, the choreography instance of the requestor needs to be registered.
     * 
     * @param goal whose choreography is to be registered
     * @throws ComponentException throws an exception if there is a problem reported during the mediation process
     * @throws UnsupportedOperationException
     */
    public void registerChoreography(Goal goal, Interface inter)
        throws ComponentException, UnsupportedOperationException;

    
    /**
     * When a WS that satisfy a certain goal is discovered, an instance of 
     * its choreography needs to be registered.
     * 
     * @param webService whose choreography is to be registered
     * @throws ComponentException throws an exception if there is a problem reported during the mediation process
     * @throws UnsupportedOperationException
     */
    public void registerChoreography(WebService webService, Interface inter)
        throws ComponentException, UnsupportedOperationException;
    
    /**
     * Attempt to update the internal state of the choreography engine
     * and determines if the received message from a given origin result
     * in a valid next state. If it doesn't this method throws a subclass
     * of ComponentExcetion that carries more information why the resulting
     * state is not a valid one i.e. if we expect to get creditcard data from the
     * client next but receive location information instead.
     * 
     * @param origin where this message came from
     * @param message the content of the message
     * @throws ComponentException if this transition is not a possible one according to the choreography
     * @throws UnsupportedOperationException if the operation is not supported by the implementation
     */
    public void updateState(URI origin, Entity message)
        throws ComponentException, UnsupportedOperationException;

	public Map<Instance, ResponseModifierInterface> updateState(Direction direction, Set<Instance> data) 
		throws ComponentException, UnsupportedOperationException;
	
	public boolean isProviderChorInEndState()
		throws ComponentException, UnsupportedOperationException;;
	
	public enum Direction {
		PROVIDER_TO_REQUESTER,
		REQUESTER_TO_PROVIDER
	}

}
