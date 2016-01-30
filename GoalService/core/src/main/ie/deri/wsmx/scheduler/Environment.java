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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;

import ie.deri.wsmx.core.WSMXKernel;
import ie.deri.wsmx.exceptions.TransportException;
import ie.deri.wsmx.executionsemantic.AbstractExecutionSemantic;
import ie.deri.wsmx.executionsemantic.Killer;
import ie.deri.wsmx.executionsemantic.State;
import ie.deri.wsmx.scheduler.transport.JavaSpaceTransport;
import ie.deri.wsmx.scheduler.transport.LocalTransport;
import ie.deri.wsmx.scheduler.transport.Transport;

import org.apache.log4j.Logger;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.exception.SystemException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.MessageId;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;

/**
 * Used by components to get proxy references of local or remote components
 * that allow them to transparently invoke components.
 * 
 * <pre>
 * Created on 11.05.2005
 * Committed by $$Author: maciejzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/Environment.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * Created on 11.05.2005
 * Committed by $Author: maciejzaremba $
 *
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/Environment.java,v $,
 * @version $Revision: 1.11 $ $Date: 2007-10-11 14:34:19 $
 */ 
public class Environment {
	
	static Logger logger = Logger.getLogger(Environment.class);	   
	private static Transport transport = null;
	private static String spaceAddress = "localhost";
	private static Properties configuration;
	private static MBeanServer mBeanServer = null;
	//flag indicating presence of core 
	private static boolean isCore = true;
	
	public static boolean isCore() {
		return isCore;
	}
	
	public static void setIsCore(boolean isCoreFlag){
		isCore = isCoreFlag;
	}
	
	private static void setupTransport () {
		isCore = true;
        logger.debug("Looking for JavaSpace at " + spaceAddress + ".");
        try {
			transport = new JavaSpaceTransport(spaceAddress);
		} catch (TransportException e) {
			logger.debug("Failed to get a transport handle on a space, falling back to a local transport.");
	        transport = new LocalTransport();
		}
	}
	
    /**
     * Returns a reference to a proxy that behaves exactely like the
     * requested component and that transparently relays to a local or remote
     * implementation of that component. The components reference is requested
     * by specifying a class - usually of an interface that is implemented by the
     * requested component.
     * 
     * @param <T>
     * @param clazz the interface of the requested component.
     * @param caller the reference of the caller (this)
     * @return
     */
    public static <T> T getComponentProxy(Class<T> clazz, Object caller) {        
        Proxy<T> proxy = GenericReviver.getExecutionSemantic(caller).getProxy(clazz);
        return proxy.muteToComponent();
    }
    
    public static void spawn(String executionSemantic) throws ExecutionSemanticNotFoundException {
    	throw new ExecutionSemanticNotFoundException("Unsupported.");
    }

    /**
     * Kicks off an execution semantic and blocks until the
     * result is available. In order to guarantee proper operation
     * it is vital that the component that requests that a certain
     * execution semantic is kicked off, ensures that this is a valid
     * execution semantic for this particular component, ie. that the
     * first state of the execution semantic is associated with the
     * component that kicks it off.
     * 
     * @param executionSemantic a <code>String</code> that identifies the execution semantic that is to be kicked off
     * @return the result of the run, whatever that may be
     * @throws ExecutionSemanticNotFoundException if the requested execution semantic is not available
     * @throws SystemException if requested execution semantic is available but something else, like transportation hase gone wrong
     */
    @SuppressWarnings("unchecked")
	public static Object blockingSpawn(String executionSemantic,
			                           Context context,
			                           MessageId messageId,
			                           List<WSMLDocument> messages) 
    		throws ExecutionSemanticNotFoundException, SystemException {
    	AbstractExecutionSemantic es = instanstiateExecutionSemantic(executionSemantic);
    	
    	List<String> msgsStr = new ArrayList<String>();
    	for (WSMLDocument msg : messages)
    		msgsStr.add(msg.getContent());
    	
		es.initialize(context, messageId, msgsStr);		
		String type = runInitState(es);
		AbstractExecutionSemantic returnedExecutionSemantic = kickOffExecutionSemantic(es, type);
		if (returnedExecutionSemantic instanceof Killer) {
			Killer killer = (Killer)returnedExecutionSemantic;
			SystemException e = new SystemException("Execution semantic was terminated under a" +
					" controlled rollback due to a failure during execution.", killer.getCause());
			logger.warn(e.getMessage(), e);
			throw e;
		}
		if (returnedExecutionSemantic != null && returnedExecutionSemantic.getState() != null)
			return returnedExecutionSemantic.getState().getData();
		SystemException e =new SystemException("Failure during execution semantic execution, null return.");
		logger.warn(e.getMessage(), e);
		throw e;
    }

	private static AbstractExecutionSemantic kickOffExecutionSemantic(AbstractExecutionSemantic es, String type) throws SystemException {
		if (transport == null)
			setupTransport();
		
		AbstractExecutionSemantic returnedExecutionSemantic = null;
		State predecessor = es.getPredecessorState();
		try {
			try {
				TypedEvent e = new TypedEvent(type, es);
				try {
					logger.debug("Sending " + e);
					transport.send(e);
				} catch (TransportException te) {
					logger.debug("Transportation failed.");
				}
			} catch (Exception e) {
				SystemException se = new SystemException("Execution semantic instantiation failed", e);
				logger.warn(se.getMessage(), se);
				throw se;
			}

			ie.deri.wsmx.scheduler.Event job = null;
			while (job == null) {
				Thread.sleep(5);
				job = transport
						.receiveIfExists(predecessor.getAssociatedEvent().name(), es.getContext().getId());
			}
			logger.debug("Received " + job);
			TypedEvent e = (TypedEvent) job;
			returnedExecutionSemantic = e.getExecutionSemantic();			
		} catch (IllegalArgumentException iae) {
			logger.warn("Failed to operate the transport correctly.", iae);
		} catch (TransportException te) {
			logger.warn("Transportation failed.", te);
		} catch (InterruptedException ie) {
			logger.warn("Transportation failed.", ie);
		}
		return returnedExecutionSemantic;
	}

	@SuppressWarnings("unchecked")
	private static AbstractExecutionSemantic instanstiateExecutionSemantic(String executionSemantic) throws ExecutionSemanticNotFoundException {
		AbstractExecutionSemantic es = null;
    	try {
			Class<AbstractExecutionSemantic> esc = (Class<AbstractExecutionSemantic>)
					Environment.class.getClassLoader().loadClass(executionSemantic);
			es = esc.newInstance(); 
		} catch (ClassCastException e) {
			logger.warn("Requested execution semantic " + executionSemantic + " not loadable.");
			throw new ExecutionSemanticNotFoundException(
					"Requested execution semantic " + executionSemantic + " not loadable.");
		} catch (InstantiationException e) {
			logger.warn("Requested execution semantic " + executionSemantic + " not instantiatable.");
			throw new ExecutionSemanticNotFoundException(
					"Requested execution semantic " + executionSemantic + " not instantiatable.");
		} catch (IllegalAccessException e) {
			logger.warn("Requested execution semantic " + executionSemantic + " not accessible.");
			throw new ExecutionSemanticNotFoundException(
					"Requested execution semantic " + executionSemantic + " not accessible.");
		} catch (ClassNotFoundException e) {
			logger.warn("Requested execution semantic " + executionSemantic + " not available.");
			throw new ExecutionSemanticNotFoundException(
					"Requested execution semantic " + executionSemantic + " not available.");
		} catch (Throwable t) {
			ExecutionSemanticNotFoundException e = new ExecutionSemanticNotFoundException(
					"Unexpected failure. Requested execution semantic " + executionSemantic + " not available.");
			logger.warn(e.getMessage(), e);
			throw e;			
		}
		return es;
	}

	private static String runInitState(AbstractExecutionSemantic es) throws SystemException {
		if (transport == null)
			setupTransport();
	
		es.setTransport(transport);
        
        String type;
		try {
			//The first state of an execution semantic is initialisation, we don't need a reference
			//to the associated component which is the one that kicks off the execution semantics.
			//During initialisation the execution semantic determines the first non-init state.
			type = es.runState(null);
		} catch (ComponentException ce) {
			logger.warn("Internal component failure in component that kicked off execution semantics." +
					" This possibly indicates an ill-written initialisation state in the execution semantics." +
					" Killing active execution sematic.", ce);
			//TODO notification
			type = null;
			throw new SystemException("Internal component failure in component that kicked off execution semantics." +
					" This possibly indicates an ill-written initialisation state in the execution semantics." +
					" Killing active execution sematic.");
		} catch (SystemException se) {
			//TODO notification
			type = null;
			throw new SystemException("System failure during initialisation state of execution semantics." +
					" This possibly indicates an ill-written initialisation state in the execution semantics." +
					" Killing active execution sematic.");
		}
		if (type == null)
			throw new SystemException("Invalid state after initialisation state of execution semantics." +
					" This possibly indicates an ill-written initialisation state in the execution semantics." +
					" Killing active execution sematic.");
		if (type.equalsIgnoreCase("EXODUS"))
			throw new SystemException("Exodus state after initialisation state of execution semantics." +
					" This possibly indicates an ill-written initialisation state in the execution semantics." +
					" Killing active execution sematic.");
		return type;
	}

	public static String getSpaceAddress() {
		return spaceAddress;
	}

	public static void setSpaceAddress(String spaceAddress) {
		Environment.spaceAddress = spaceAddress;
	}
	
	public static File getKernelLocation() {
		return WSMXKernel.KERNEL_LOCATION;
	}
	
	public static Properties getConfiguration(){
		if (configuration == null)
			return new Properties();
		return configuration; 
	}

	public static void setConfiguration(Properties properties){
		configuration = properties;
	}
	
	synchronized public static MBeanServer getMBeanServer() {
		return mBeanServer;
	}

	synchronized public static void setMBeanServer(MBeanServer beanServer) {
		mBeanServer = beanServer;
	}    
}
