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
import ie.deri.wsmx.scheduler.Event;

import org.apache.log4j.Logger;
import org.wsmo.execution.common.nonwsmo.Context;

/**
 * A Transport that prefers locally available components
 * and attempt to publish the task to a space only
 * if no local components can handle it.
 *
 * <pre>
 * Created on Sep 3, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/transport/OptimizedSpaceTransport.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2006-02-17 11:38:15 $
 */ 
public class OptimizedSpaceTransport implements Transport {

	private static final long serialVersionUID = 8336579666241420198L;
	static Logger logger = Logger.getLogger(JavaSpaceTransport.class);
	JavaSpaceTransport spaceTransport;	
	LocalTransport localTransport;
	
	
    public OptimizedSpaceTransport(String spaceLocation) throws TransportException {
        super();
        spaceTransport = new JavaSpaceTransport(spaceLocation);
        localTransport = new LocalTransport();
    }
    
	public void send(Event event, Object... modifiers)
			throws IllegalArgumentException, TransportException {
		

	}

	public Event receive(Object... modifiers) throws IllegalArgumentException,
			TransportException {
		// TODO Auto-generated method stub
		return null;
	}

	public Event receiveIfExists(Object... modifiers)
			throws IllegalArgumentException, TransportException {
		// TODO Auto-generated method stub
		return null;
		
	}

	public void send(Event event, ie.deri.wsmx.executionsemantic.WSMXExecutionSemantic.Event eventType, Context context) throws IllegalArgumentException, TransportException {
		// TODO Auto-generated method stub
		
	}

	public Event receive(ie.deri.wsmx.executionsemantic.WSMXExecutionSemantic.Event eventType, Context context) throws IllegalArgumentException, TransportException {
		// TODO Auto-generated method stub
		return null;
	}

	public Event receiveIfExists(ie.deri.wsmx.executionsemantic.WSMXExecutionSemantic.Event eventType, Context context) throws IllegalArgumentException, TransportException {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
