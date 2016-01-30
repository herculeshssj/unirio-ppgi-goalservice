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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.wsmo.execution.common.nonwsmo.Context;

/**
 * A local transport that is intended for 
 * single machine communication.
 *
 * <pre>
 * Created on Nov 15, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/transport/LocalTransport.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2005-11-27 23:10:08 $
 */ 
public class LocalTransport implements Transport {

	private static final long serialVersionUID = 5094104229447281057L;
	static Logger logger = Logger.getLogger(LocalTransport.class);
	private static Map<String, Map<String, Event>> availableEvents = null;
	public static final String STATELESS_EVENT_KEY = "statelessevent";
	
	static {
		availableEvents = Collections.synchronizedMap(new HashMap<String, Map<String, Event>>());
		availableEvents.put(STATELESS_EVENT_KEY, Collections.synchronizedMap(new HashMap<String, Event>()));
	}
	
	public void send(Event event, Object... modifiers)
			throws IllegalArgumentException, TransportException {
		if(event.getType() == null || event.getType().equals(""))
			throw new IllegalArgumentException("Event doesn't have a valid type.");
		synchronized (availableEvents) {
			if (!availableEvents.containsKey(event.getContext()) &&
					event.getContext() != null)
				availableEvents.put(event.getContext(), 
						Collections.synchronizedMap(new HashMap<String, Event>()));
			if(event.getContext() == null ||
					event.getContext().equals("") ||
					event.getContext().equals(STATELESS_EVENT_KEY))
				availableEvents.get(STATELESS_EVENT_KEY).put(event.getType(), event);
			else
				availableEvents.get(event.getContext()).put(event.getType(), event);
		}
	}

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.scheduler.transport.Transport#receive(java.lang.Object[])
	 */
	public Event receive(Object... modifiers) throws IllegalArgumentException,
			TransportException {
		logger.warn("Blocking receive is currently not supported by local transport.");
		throw new TransportException("Blocking receive is currently not supported by local transport.");
	}

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.scheduler.transport.Transport#receiveIfExists(java.lang.Object[])
	 */
	public Event receiveIfExists(Object... modifiers)
			throws IllegalArgumentException, TransportException {
		if (modifiers == null ||
    			modifiers[0] == null ||
    			!(modifiers[0] instanceof String))
    		throw new IllegalArgumentException("Local Transport expects a String as first modifier.");
        String type = (String)modifiers[0];
        String context = STATELESS_EVENT_KEY;
        if (modifiers.length > 1)
            context = (String)modifiers[1];
        Event result = null;             
		synchronized (availableEvents) {
			Map<String, Event> contextEvents = availableEvents.get(context);
			if (contextEvents != null)
				result = contextEvents.remove(type);
		}
		return result;
	}

	public void send(Event event, WSMXExecutionSemantic.Event eventType, Context context) 
			throws IllegalArgumentException, TransportException {
		send(event, new Object[]{eventType.name(), context.getId()});
	}

	public Event receive(WSMXExecutionSemantic.Event eventType, Context context) 
			throws IllegalArgumentException, TransportException {
		return receive(new Object[]{eventType.name(), context.getId()});
	}

	public Event receiveIfExists(WSMXExecutionSemantic.Event eventType, Context context) 
			throws IllegalArgumentException, TransportException {
		return receiveIfExists(new Object[]{eventType.name(), context.getId()});
	}

	
}
