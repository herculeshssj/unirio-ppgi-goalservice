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
import ie.deri.wsmx.executionsemantic.Killer;
import ie.deri.wsmx.executionsemantic.WSMXExecutionSemantic;
import ie.deri.wsmx.scheduler.transport.Transport;

import org.apache.log4j.Logger;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.exception.SystemException;

/**
 * TODO comment
 *
 * <pre>
 * Created on 15.03.2005
 * Committed by $$Author: maciejzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/GenericReviver.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.17 $ $Date: 2007-06-14 14:42:57 $
 */
public class GenericReviver extends Reviver {
    
	private static final long serialVersionUID = 7629260710886694774L;
	Object[] inputCriteria = null;
    Object[] outputProperties = null;
    
    static Logger logger = Logger.getLogger(GenericReviver.class);

    boolean transportationFailed = false;
    
    /**
     * @param transport
     * @param component
     */
    public GenericReviver(Transport transport, Object component, DistributedScheduler wrapper) {
        super(transport, component, wrapper);
    }
    
    /* (non-Javadoc)
     * @see ie.deri.wsmx.wrapper.Reviver#inputCriteria()
     */
    @Override
	public Object[] inputCriteria() {
        return inputCriteria;
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.wrapper.Reviver#outputProperties()
     */
    @Override
	public Object[] outputProperties() {
        return outputProperties;
    }

    /* (non-Javadoc)
     * @see ie.deri.wsmx.wrapper.Reviver#handleEvent(ie.deri.wsmx.nonwsmodatamodel.Event)
     */
    @Override
	public Event handleEvent(Event event) {        
        return null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
	public void run() {
		//if there are no events for this component we don't need a running reviver
		if (inputCriteria().length == 0) {
			logger.warn("Reviver start skipped due to insufficient events for component " + component.getClass());
			return;
		}
		logger.debug("Reviver started in Thread " + Thread.currentThread());
		while(alive) {  
            try {
            	if (!transportationFailed)
            		logger.debug("Reviver of " + component.getClass().getSimpleName() +
            				" attempts to retrieve tuple matching " + inputCriteria()[0]);
                Event job = null;
                while (job == null) {
                    //if we don't have a job we may die
                    //otherwise we must finish the job first
                    if (!alive) {
                        logger.debug("Reviver running in Thread " + Thread.currentThread() +
                        " ceases to exist."); 
                        return;
                    }
                    Thread.sleep(2);
                    job = transport.receiveIfExists(inputCriteria());
                }
                logger.debug("Reviver of " + component.getClass().getSimpleName() + " received " + job);
                wrapper.logSchedule();
                TypedEvent e = (TypedEvent)job;
                executionSemantic = e.getExecutionSemantic();
                logger.debug("Reviver of " + component.getClass().getSimpleName() +
                		" unwrapped " + executionSemantic);
                executionSemantic.setTransport(transport);
				                       
                String type;
				try {
					type = executionSemantic.runState(component);
				} catch (ComponentException ce) {
					logger.warn("Internal component failure in " + component.getClass() + ", killing active execution sematic.", ce);
					executionSemantic.cleanUp();
					//TODO notification
					type = null;
					rollback(e, ce);
				} catch (SystemException se) {
					logger.warn("Failure during execution of state, killing active execution semantic.", se);
					executionSemantic.cleanUp();
					//TODO notification
					type = null;
					rollback(e, se);
				}
				//FIXME string comparison
                if (type != null && !type.equalsIgnoreCase("EXODUS"))  {
                    e.setType(type);                    
                    if (e.getExecutionSemantic().getState().isStatefulResponse())
                    	e.setContext(e.getExecutionSemantic().getState().getContext().getId());
                    else
                    	e.setContext(null);
                    e.setExecutionSemantic(executionSemantic);
                    logger.debug("Reviver of " + component.getClass().getSimpleName() +
                    			 " sends " + e + " wrapping " + executionSemantic);
                    transport.send(e);
                } else
                    logger.debug("Reviver of " + component.getClass().getSimpleName() +
                    		" permits execution semantics exodus.");
            } catch(IllegalArgumentException iae) {
                logger.warn("Failed to operate the transport correctly.", iae);
            } catch (TransportException te) {
            	if (!transportationFailed) {
            		logger.warn("Transportation failed.", te);
            		transportationFailed = true;
            	}
            } catch (InterruptedException ie) {
                logger.warn("Transportation failed.", ie);                              
            }
        }
        logger.debug("Reviver running in Thread " + Thread.currentThread() +
        " ceases to exist."); 
    }

	private void rollback(TypedEvent e, Exception cause) throws TransportException {
		for (WSMXExecutionSemantic.Event event : executionSemantic.getTerminateOnFailure()) {
			e.setType(event.name());
			e.setContext(executionSemantic.getState().getContext().getId());
			e.setExecutionSemantic(new Killer(executionSemantic.getContext(), cause));
		    transport.send(e);
		}
	}
    
	//TODO eliminate argument by inspecting the stackframe (not possible? instance versus class)
	public static AbstractExecutionSemantic getExecutionSemantic(Object component) {
		return instanceCache.get(component).getExecutionSemantic();
	}
    
    /**
     * Takes the current stack-frame and searches bottom-up 
     * for a class that is is not equal to this class,
     * that is the caller.
     * 
     * @return
     */
    protected String getCaller() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String caller = null;
        for (int i = 0; i < stack.length; i++) {
            if (!stack[i].getClassName().equals(this.getClass().getName())) {
                caller = stack[i].getClassName();
                break;
            }
        }
        return caller;
    }
	
    /**
     * @return Returns the inputCriteria.
     */
    public Object[] getInputCriteria() {
        return inputCriteria;
    }
    /**
     * @param inputCriteria The inputCriteria to set.
     */
    public void setInputCriteria(Object[] inputCriteria) {
        this.inputCriteria = inputCriteria;
    }
    /**
     * @return Returns the outputProperties.
     */
    public Object[] getOutputProperties() {
        return outputProperties;
    }
    /**
     * @param outputProperties The outputProperties to set.
     */
    public void setOutputProperties(Object[] outputProperties) {
        this.outputProperties = outputProperties;
    }
}
