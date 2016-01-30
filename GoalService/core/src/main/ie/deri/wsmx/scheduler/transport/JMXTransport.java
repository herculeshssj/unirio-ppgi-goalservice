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

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;

import org.wsmo.execution.common.nonwsmo.Context;

/**
 * TODO comment
 *
 * <pre>
 * Created on 15.02.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/transport/JMXTransport.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.2 $ $Date: 2005-11-17 09:24:45 $
 */
public class JMXTransport extends NotificationBroadcasterSupport
                          implements NotificationListener, Transport {


    private static final long serialVersionUID = 3690755115967590709L;

    /* (non-Javadoc)
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    public void handleNotification(Notification arg0, Object arg1) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.infomodel.Transport#send(ie.deri.wsmx.nonwsmodatamodel.Event)
     */
    public void send(Event event, Object ... modifiers) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.infomodel.Transport#receive()
     */
    public Event receive(Object ... modifiers) {
        // TODO Auto-generated method stub
        return null;
    }

    public Event receiveIfExists(Object... modifiers) throws IllegalArgumentException, TransportException {
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
