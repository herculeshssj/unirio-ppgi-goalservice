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

package ie.deri.wsmx.scheduler.transport;

import ie.deri.wsmx.exceptions.TransportException;
import ie.deri.wsmx.executionsemantic.WSMXExecutionSemantic;
import ie.deri.wsmx.scheduler.Event;

import java.io.Serializable;

import org.wsmo.execution.common.nonwsmo.Context;

/**
 * A <code>Transport</code> implementation encapsulates a  
 * transport mechanism such as a particular messaging
 * middleware or a tuplespace from the higher-level
 * concept of WSMX events.
 * This abstraction allows either of the two to change
 * indepently from the other, a well-tried quality of
 * protocol stacks which proved its worth. Thus this
 * interface resembles a service that a lower layer
 * provides to a higher layer.
 *
 * <pre>
 * Created on 15.02.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/transport/Transport.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.2 $ $Date: 2005-11-17 09:23:18 $
 */
public interface Transport extends Serializable {

    /**
     * Allows a higher layer to send events
     * according to the transport layers
     * protocol, unknown to higher layers.
     * There are two aspects in the arguments,
     * the Event is a unit of embedded 
     * application logic, while the optional 
     * and unbound number of modifier
     * arguments determine transport specific
     * operation behaviour.
     * 
     * @param event the <code>Event</code> to be sent
     * @param modifiers transport specific modifications
     * @throws IllegalArgumentException if the argument are not 
     * understood by the transport implementation
     * @throws TransportException if transportation itself failed
     */
    public void send(Event event, Object ... modifiers) 
    	throws IllegalArgumentException, TransportException;
    
    /**
     * Allows a higher layer to receive events
     * according to the transport layers
     * protocol, unknown to higher layers.
     * If no incomming events are available this
     * method blocks until the next arrives.
     * The arguments are optional and may be needed
     * for some transport implementations in which
     * case they narrow down the transport-specific
     * operation used to receive the 
     * application-level event. 
     * 
     * @param modifiers transport specific modifications
     * @return the received <code>Event</code>
     * @throws IllegalArgumentException if the argument are not 
     * understood by the transport implementation
     * @throws TransportException if transportation itself failed
     */
    public Event receive(Object ... modifiers) 
    	throws IllegalArgumentException, TransportException;
    
    /**
     * Allows a higher layer to receive events
     * according to the transport layers
     * protocol, unknown to higher layers.
     * If no incomming events are available this
     * method does not block and returns null.
     * The arguments are optional and may be needed
     * for some transport implementations in which
     * case they narrow down the transport-specific
     * operation used to receive the 
     * application-level event. 
     * 
     * @param modifiers transport specific modifications
     * @return the received <code>Event</code>
     * @throws IllegalArgumentException if the argument are not 
     * understood by the transport implementation
     * @throws TransportException if transportation itself failed
     */
    public Event receiveIfExists(Object ... modifiers) 
        throws IllegalArgumentException, TransportException;    

    public void send(Event event, WSMXExecutionSemantic.Event eventType, Context context) 
    	throws IllegalArgumentException, TransportException;
    
    public Event receive(WSMXExecutionSemantic.Event eventType, Context context) 
    	throws IllegalArgumentException, TransportException;
    
    public Event receiveIfExists(WSMXExecutionSemantic.Event eventType, Context context) 
        throws IllegalArgumentException, TransportException;
    
}
