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
package ie.deri.wsmx.executionsemantic;

import ie.deri.wsmx.exceptions.TransportException;
import ie.deri.wsmx.scheduler.RoutingException;
import ie.deri.wsmx.scheduler.TypedEvent;
import ie.deri.wsmx.scheduler.transport.Transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.exception.SystemException;
import org.wsmo.execution.common.nonwsmo.Context;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 19.05.2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/executionsemantic/AbstractExecutionSemantic.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Maciej Zaremba
 * @author Michal Zaremba
 *
 * @version $Revision: 1.8 $ $Date: 2007-06-14 14:42:06 $
 */ 
public abstract class AbstractExecutionSemantic 
        implements ExecutionSemantic, Serializable {
                     
	protected Transport transport;
	public static final long serialVersionUID=223542;
	
	static Logger logger = Logger.getLogger(AbstractExecutionSemantic.class);
    public State state = null;
    
    public List<State> stateHistory = new ArrayList<State>();
	public List<WSMXExecutionSemantic.Event> terminateOnFailure = new ArrayList<WSMXExecutionSemantic.Event>();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
        if (obj instanceof AbstractExecutionSemantic) {
        	AbstractExecutionSemantic e = (AbstractExecutionSemantic)obj;
            if (this.getType().equals(e.getType()) && this.state.getContext().equals(e.getState().getContext()))
                return true;
        }
        return false;
	}

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.executionSemantics.ExecutionSemanticInterface#setJavaSpace(net.jini.space.JavaSpace)
	 */
	public void setTransport(Transport transport){
		this.transport = transport; 
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return getClass().getSimpleName() + " execution semantic with state " + state.getAssociatedEvent() +
               " in context " + state.getContext();
    }
    
    //method cleaning up (e.g. closing files, removing wsmo4j objects, etc.) execution semantics in case of failure 
    public void cleanUp(){};
       
	public String runState(Object component, Serializable... params) throws ComponentException, SystemException {
		try {
	    	logger.debug("In state: " + state.getClass().getSimpleName());
	        State successorState = state.handleState(component);
	        stateHistory.add(state);
	        state = successorState;
	        return state.getAssociatedEvent().toString();
	    } catch (ComponentException ce) {
	        logger.warn("Internal component failure.", ce);
	        cleanUp();
	        throw ce;
	    } catch (UnsupportedOperationException uoe) {
	        logger.warn("Component does not support needed functionality.",uoe);
	        cleanUp();
			throw new ComponentException ("Component does not support needed functionality.", uoe);
		} catch (RoutingException re) {
	        logger.warn("Routing failure: Expected " + re.getExpectedType().getSimpleName() +
	        		" but actual type is " + re.getActualType(), re);
	        cleanUp();
	        throw new SystemException("Routing failure: Expected " + re.getExpectedType().getSimpleName() +
	        		" but actual type is " + re.getActualType(), re);
		} catch (DataFlowException ese) {
	        logger.warn("Unrecoverable dataflow failure: " + ese.getMessage());
	        cleanUp();
	        throw new SystemException("Unrecoverable state in execution semantic.", ese);
		} catch (Throwable t) {
	        logger.warn("Failure during execution of state " + state.getClass().getSimpleName() + "."  , t);
	        cleanUp();
	        throw new SystemException("Failure during execution of state " + state.getClass().getSimpleName() + ".", t);
	    }
	}

	protected AbstractExecutionSemantic spawn(AbstractExecutionSemantic waiting, AbstractExecutionSemantic delegate) throws SystemException {
		try {			
			try {
				TypedEvent e = new TypedEvent(delegate.getState().getAssociatedEvent().name(),
											  null,
											  delegate);
				try {
					logger.debug("Sending " + e + " wrapping " + delegate);
					transport.send(e);
				} catch (TransportException te) {
					logger.fatal("Transportation failed.", te);
				}
			} catch (Exception e) {
				logger.fatal("Execution semantic instantiation failed.", e);

				throw new SystemException(
						"Execution semantic instantiation failed", e);
			}

			ie.deri.wsmx.scheduler.Event job = null;
			while (job == null) {
				Thread.sleep(2);
				job = transport
						.receiveIfExists(waiting.getState().getAssociatedEvent(),
										 waiting.getState().getContext());
			}
			logger.debug("Received " + job);
			TypedEvent e = (TypedEvent) job;
			AbstractExecutionSemantic executionSemantic = e.getExecutionSemantic();
			return executionSemantic;
		} catch (IllegalArgumentException iae) {
			logger.warn("Failed to operate the transport correctly.", iae);
		} catch (TransportException te) {
			logger.warn("Transportation failed.", te);

		} catch (InterruptedException ie) {
			logger.warn("Transportation failed.", ie);
		}
		throw new SystemException("Returned execution semantic is null.");
	}

	public State getState() {
		return state;
	}

	public Context getContext() {
		return state.getContext();
	}

	public WSMXExecutionSemantic.Event getType() {
		return state.getAssociatedEvent();
	}

	public List<State> getStateHistory() {
		return Collections.unmodifiableList(stateHistory);
	}

	public State getPredecessorState() {
		return stateHistory.get(stateHistory.size()-1);
	}

	public List<WSMXExecutionSemantic.Event> getTerminateOnFailure() {
		return terminateOnFailure;
	}

	protected void tagStateForTerminationOnFailure() {
		if (getState() != null) {
			logger.debug("Adding state to termination on failure list: " + getState().getAssociatedEvent());
	    	terminateOnFailure.add(getState().getAssociatedEvent());
		} else
			logger.warn("Attempted to tag state for termination on failure, but state is null.");
	}

	protected void untagStateForTerminationOnFailure() {
		if (getState() != null) {
			logger.debug("Removing state from termination on failure list: " + getState().getAssociatedEvent());
	    	terminateOnFailure.remove(getState().getAssociatedEvent());
		} else
			logger.warn("Attempted to untag state for termination on failure, but state is null.");
	}

}
