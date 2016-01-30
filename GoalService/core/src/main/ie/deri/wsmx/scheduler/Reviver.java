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

package ie.deri.wsmx.scheduler;

import ie.deri.wsmx.exceptions.TransportException;
import ie.deri.wsmx.executionsemantic.AbstractExecutionSemantic;
import ie.deri.wsmx.scheduler.transport.Transport;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A kind of thread that drives a single component instance.
 * Defines the general plan of attack, which is to accept events,
 * process them, and emit new events.
 *
 * <pre>
 * Created on 23.02.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/Reviver.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.6 $ $Date: 2005-11-25 14:50:20 $
 */
public abstract class Reviver implements Runnable, Serializable {

    static Logger logger = Logger.getLogger(Reviver.class);
    protected boolean alive = true;
    protected Transport transport = null;
    protected transient Object component = null;
    protected DistributedScheduler wrapper = null;
	protected AbstractExecutionSemantic executionSemantic = null;
	
	protected static Map<Object, Reviver> instanceCache = new HashMap<Object, Reviver>();

    /**
     * @param transport
     */
    public Reviver(Transport transport, Object component, DistributedScheduler wrapper) {
        super();
        this.transport = transport;
        this.component = component;
        this.wrapper = wrapper;
		instanceCache.put(component, this);
    }
    
    public void start() {
        new Thread(this, "Reviver::" + component.getClass().getSimpleName() + "").start();
    }

    public void stop() {
        logger.debug("Reviver received stop signal.");
        alive = false;        
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
    	logger.debug("Reviver started in Thread " + Thread.currentThread()); 
        while(alive) {
        	try {
                Event job = null;
                while (job == null) {
                    //if we don't have a job we may die
                    //otherwise we must finish the job first
                    if (!alive) return;                    
                    job = transport.receiveIfExists(inputCriteria());                    
                }
	        	logger.debug("Received " + job); 
	        	Event result = handleEvent(job);
	        	logger.debug("Result " + result);
	        	if (result != null)
	        		transport.send(result, outputProperties());
        	} catch(IllegalArgumentException iae) {
        		logger.warn("Failed to operate the transport correctly.", iae);
        	} catch (TransportException te) {
        		logger.warn("Transportation failed.", te);
        	}
        }
        logger.debug("Reviver running in Thread " + Thread.currentThread() +
                     "ceases to exist."); 
    }     

	/**
	 * Specifies which kind of work which is accepted
	 * by the component that is driven by this
	 * <code>Reviver</code>.
	 * 
	 * @return a set of criteria that determines
	 * the kind of input data
	 * 
	 */
	public abstract Object[] inputCriteria();

	/**
	 * Specifies which kind of work is delivered
	 * by the component that is driven by this
	 * <code>Reviver</code>.

	 * @return
	 */
	public abstract Object[] outputProperties();

	/**
	 * Does the actual work.
	 * 
	 * @param event 
	 * @return 
	 */
	public abstract Event handleEvent(Event event);

	/**
	 * @return Returns the executionSematic.
	 */
	public AbstractExecutionSemantic getExecutionSemantic() {
		return executionSemantic;
	}
	

}
