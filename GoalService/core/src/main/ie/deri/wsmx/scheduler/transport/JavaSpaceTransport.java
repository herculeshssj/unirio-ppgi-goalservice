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
import ie.deri.wsmx.scheduler.TypedEvent;

import java.io.IOException;
import java.rmi.RemoteException;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import org.apache.log4j.Logger;
import org.wsmo.execution.common.nonwsmo.Context;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on Feb 19, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/transport/JavaSpaceTransport.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-11-17 09:24:45 $
 */
public class JavaSpaceTransport implements Transport {

    private static final long serialVersionUID = 3258128063761297971L;
    
    JavaSpace space;
	static Logger logger = Logger.getLogger(JavaSpaceTransport.class);

    public JavaSpaceTransport(String spaceLocation) throws TransportException {
        super();
        logger.debug("Looking for JavaSpace.");
        try {
        	this.space = (JavaSpace)SpaceLocator.getService(spaceLocation,
                                                        JavaSpace.class);
            logger.debug("Got space: " + spaceLocation);
        } catch (ClassNotFoundException cnfe) {
            logger.warn("Space lookup operation failed for " + spaceLocation, cnfe);
            throw new TransportException("Space not available.");
        } catch (IOException ioe) {
            logger.warn("Space lookup operation failed for " + spaceLocation, ioe);
            throw new TransportException("Space not available.");
        }
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.infomodel.Transport#send(ie.deri.wsmx.nonwsmodatamodel.Event)
     */
    public void send(Event event, Object ... modifiers) throws IllegalArgumentException, TransportException {        
        if (space == null) throw new TransportException("Space not available.");
        logger.debug("Writing entry " + event);
    	try {
	        space.write((Entry)event, null, Long.MAX_VALUE);
        } catch(TransactionException te) {
            logger.debug("Tuplespace write operation failed.", te);
        } catch(RemoteException re) {
            logger.debug("Tuplespace write operation failed.", re);
        }
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.infomodel.Transport#receive()
     */
    public Event receive(Object ... modifiers) throws IllegalArgumentException, TransportException {
        if (space == null) throw new TransportException("Space not available.");
    	if (modifiers == null ||
    			modifiers[0] == null ||
    			!(modifiers[0] instanceof String))
    		throw new IllegalArgumentException("JavaSpaces Transport expects a String modifier.");
        TypedEvent e = null;
    	if (modifiers != null &&
    			modifiers[0] != null) {
            e = new TypedEvent((String)modifiers[0]);
            if (modifiers.length > 1 && modifiers[1] != null)
                e.setContext((String)modifiers[1]);
        }        
        Event result = null;             
        logger.debug("Attempting to take an entry for template " + e);
        try {
            //FIXME In the case that a RemoteException occurs,
            //an entry can be taken from the space and never returned
            //to the client that performed the take, effectively losing
            //the entry in between. We need to wrap the take inside a
            //transaction that is committed  when we have received 
            //the requested entry.
            while (result == null) {
                //FIXME use notifiy instead of checking every second
                Thread.sleep(1000);
                result = (Event)space.take(e, null, JavaSpace.NO_WAIT);
            }
            logger.debug("Took " + result);
        } catch (RemoteException re) {
            logger.warn("Space take operation failed.", re); 
        } catch (TransactionException te) {
            logger.warn("Space take operation failed.", te); 
        } catch (InterruptedException ie) {
            logger.warn("Space take operation failed.", ie); 
        } catch (UnusableEntryException uee) {
            logger.warn("Space take operation failed.", uee); 
        }
        return result;
    }

    public Event receiveIfExists(Object... modifiers) throws IllegalArgumentException, TransportException {
        if (space == null) throw new TransportException("Space not available.");
        if (modifiers != null &&
                modifiers[0] != null &&
                !(modifiers[0] instanceof String))
            throw new IllegalArgumentException("JavaSpaceTransport expects a String modifier.");
        TypedEvent e = null;
        if (modifiers != null &&
                modifiers[0] != null) {
            e = new TypedEvent((String)modifiers[0]);
            if (modifiers.length > 1 && modifiers[1] != null)
                e.setContext((String)modifiers[1]);
        }        
        Event result = null;             
        try {
            //FIXME In the case that a RemoteException occurs,
            //an entry can be taken from the space and never returned
            //to the client that performed the take, effectively losing
            //the entry in between. We need to wrap the take inside a
            //transaction that is committed  when we have received 
            //the requested entry.
            result = (Event)space.take(e, null, JavaSpace.NO_WAIT);
        } catch (RemoteException re) {
            logger.warn("Space take operation failed.", re); 
        } catch (TransactionException te) {
            logger.warn("Space take operation failed.", te); 
        } catch (InterruptedException ie) {
            logger.warn("Space take operation failed.", ie); 
        } catch (UnusableEntryException uee) {
            logger.warn("Space take operation failed.", uee); 
        }
        return result;


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
