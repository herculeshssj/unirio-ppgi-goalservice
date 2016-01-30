/*
 * Copyright (c) 2007 National University of Ireland, Galway
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  */

package ie.deri.wsmx.communicationmanager;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.core.configuration.annotation.*;
import ie.deri.wsmx.executionsemantic.ExecutionSemanticsFinalResponse;
import ie.deri.wsmx.scheduler.*;

import java.util.*;

import org.apache.log4j.*;
import org.wsmo.common.*;
import org.wsmo.execution.common.*;
import org.wsmo.execution.common.exception.SystemException;
import org.wsmo.execution.common.nonwsmo.*;
import org.wsmo.service.*;

/**
 * Interface or class description
 *
 * <pre>
 * Created on 06-Jul-2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/communicationmanager/src/main/ie/deri/wsmx/communicationmanager/CommunicationManager.java,v $,
 * </pre>
 *
 * @author Matthew Moran
 *
 * @version $Revision: 1.53 $ $Date: 2007-12-09 22:14:55 $
 */
@WSMXComponent(name   = "CommunicationManager",
		       events = {"COMMUNICATIONMANAGER"},
        	   description = "Communication Manager implements WSMX EntryPoint functionality")
public class CommunicationManager implements EntryPoint {

    static Logger logger = Logger.getLogger(CommunicationManager.class);
    
    static {
    	logger.setLevel(Level.ALL);
    }

	private int context = 1;
	
	// ------------------------------------------------------------------- //
	// EntryPoint interface methods
	// ------------------------------------------------------------------- //

	@Exposed(description = "Wrapper around the achieveGoal entry point that returns " +
	" information regarding established communication between Goal and Web service. Synchronous method.")
	public ExecutionSemanticsFinalResponse achieveGoalFullResponse(String wsmlMessage) {
		logger.info("====achieveGoal full response entry ====\n" + wsmlMessage);

		Context context = new Context("achieveGoal" + this.context++);
		ExecutionSemanticsFinalResponse r = null;
		try {
			List<WSMLDocument> msgs = new ArrayList<WSMLDocument>();
			msgs.add(new WSMLDocument(wsmlMessage));
			r = (ExecutionSemanticsFinalResponse) blockingStartExecutionSemantics("ie.deri.wsmx.executionsemantic.AchieveGoalChor", msgs , context,
				 new MessageId("0"));
		} catch (ExecutionSemanticNotFoundException e) {
			logger.warn(e);
			return new ExecutionSemanticsFinalResponse((Goal)null, (List<WebService>)null, (Set<Entity>)null, (Set<Entity>)null, false, null, buildFailureMessage(e));
		} catch (SystemException e) {
			logger.warn(e);
			return new ExecutionSemanticsFinalResponse(null, null, null, null, false, null, buildFailureMessage(e));
		}

		return r;
	}

	@Exposed(description = "Wrapper around the achieveGoal entry point that works with" +
			" user interface friendly types and returns the result synchronously.")
	public String achieveGoal(String wsmlMessage) {
		logger.info("====achieveGoal entry ====\n"+wsmlMessage);

        Context context = new Context("achieveGoal" + this.context++);
        ExecutionSemanticsFinalResponse esResponse = null;
		try {
			List<WSMLDocument> msgs = new ArrayList<WSMLDocument>();
			msgs.add(new WSMLDocument(wsmlMessage));

			esResponse = (ExecutionSemanticsFinalResponse) blockingStartExecutionSemantics("ie.deri.wsmx.executionsemantic.AchieveGoalChor", msgs, context,
					new MessageId("0"));		
		} catch (ExecutionSemanticNotFoundException e) {
			logger.warn(e);
			return buildFailureMessage(e);
		} catch (SystemException e) {
			logger.warn(e);
			return buildFailureMessage(e);
		}
			
		//clean up after communication
		if (esResponse!=null && esResponse.getExecutionSemantic() != null)
			esResponse.getExecutionSemantic().cleanUp();
		
		String msg = "";
		if (esResponse != null) {
			msg = esResponse.getMsg();
		}
		return msg;
	}

	@Exposed(description = "Wrapper around the discoverWebServices entry point that works with" +
	" user interface friendly types and returns the result synchronously.")
	public String[] discoverWebServices(String wsmlGoal) {
		
		String response[] = {};
		
		logger.info("====discoverWebServices entry ====\n" + wsmlGoal);

		Context context = new Context("discoverWebServices" + this.context++);
		ExecutionSemanticsFinalResponse esResponse = null;
		try {
			List<WSMLDocument> msgs = new ArrayList<WSMLDocument>();
			msgs.add(new WSMLDocument(wsmlGoal));
			esResponse = (ExecutionSemanticsFinalResponse) blockingStartExecutionSemantics("ie.deri.wsmx.executionsemantic.DiscoverWebServices", msgs, context,
						new MessageId("0"));
		} catch (ExecutionSemanticNotFoundException e) {
			logger.warn(e);
			return response;
		} catch (SystemException e) {
			logger.warn(e);
			return response;
		}

		//serialize discovered services
		if (esResponse.getWebservices()!= null && esResponse.getWebservices().size()!=0)		
			response = Helper.serializeTopEntities(new HashSet<TopEntity>(esResponse.getWebservices()));
		
		// clean up after communication
		if (esResponse != null && esResponse.getExecutionSemantic() != null)
			esResponse.getExecutionSemantic().cleanUp();

		return response;
	}
	
	/* Execute provided Web service
	 */
	
	@Exposed(description = "Wrapper around the invokeWebService entry point that works with" +
	" user interface friendly types and returns the result synchronously.")
	public String invokeWebService(String wsmlWebService, String wsmlOntology) {
		String msg = "";
		logger.info("====invokeWebService entry ====");

		Context context = new Context("invokeWebService" + this.context++);
		ExecutionSemanticsFinalResponse esResponse = null;
		try {
			List<WSMLDocument> msgs = new ArrayList<WSMLDocument>();
			msgs.add(new WSMLDocument(wsmlWebService));
			msgs.add(new WSMLDocument(wsmlOntology));
			esResponse = (ExecutionSemanticsFinalResponse) blockingStartExecutionSemantics("ie.deri.wsmx.executionsemantic.ExecuteWebService", msgs, context,
						new MessageId("0"));
		} catch (ExecutionSemanticNotFoundException e) {
			logger.warn(e);
			return msg;
		} catch (SystemException e) {
			logger.warn(e);
			return msg;
		}

		//clean up after communication
		if (esResponse!=null && esResponse.getExecutionSemantic() != null)
			esResponse.getExecutionSemantic().cleanUp();
		
		if (esResponse != null) {
			msg = esResponse.getMsg();
		}
		return msg;
	}
	
	//attaches messages of all nested exception to the failure message
	private String buildFailureMessage(Exception e) {
		String msg = "Failure:\n" + e.getMessage();
		Throwable re = e;
		while (re.getCause() != null) {
			re = re.getCause();
			msg = msg + "\n" + re.getMessage();
		}
		return msg;
	}

	/**
	 * AchieveGoal is an end-to-end execution semantic that allows for 
	 * goal based service invocation
	 */
	public Context achieveGoal(WSMLDocument wsmlMessage) throws SystemException {
		Context c = null; //TODO create context here
		logger.debug("In achievGoal entrypoint");
        Context context = new Context("achieveGoal" + this.context++);
        //TODO MessageID
        try {
			List<WSMLDocument> msgs = new ArrayList<WSMLDocument>();
			msgs.add(wsmlMessage);
			blockingStartExecutionSemantics("ie.deri.wsmx.executionsemantic.AchieveGoalChor", msgs, context, new MessageId("0"));
		} catch (ExecutionSemanticNotFoundException e) {
			throw new SystemException("Execution semantic not found.", e);
		} catch (SystemException e) {
			throw e;
		}
        return c;
	}
	
	
	private Object blockingStartExecutionSemantics(String exSemClass, List<WSMLDocument> wsmlMessage, Context context, MessageId msgId) 
			throws ExecutionSemanticNotFoundException, SystemException {
		Object r = null;
        try {
			r = Environment.blockingSpawn(exSemClass,
					                      context,
					                      msgId,
					                      wsmlMessage);
		} catch (ExecutionSemanticNotFoundException e) {
			logger.warn(e);
			throw e;
		} catch (SystemException e) {
			logger.warn(e);
			throw e;
		}
		return r;
	}
    
    /**
     * Performs an execution call in blocking mode.
     * 
     * @param wsmlMessage Message which is sent as an input to the operation
     * @param context Context of the conversation
     * @param msgId Identifier of the Message
     * @return Context object
     * @throws ExecutionSemanticNotFoundException
     * @throws SystemException
     */
    @Exposed(description = "Wrapper for Context executeWebService(wsmlMessage)")
    private Object blockingExecuteWebService(List<WSMLDocument> wsmlMessages, Context context, MessageId msgId)
        throws ExecutionSemanticNotFoundException, SystemException{
        Object r = null;
        try {
            r = Environment.blockingSpawn("ie.deri.wsmx.executionsemantic.ExecuteWebService",
                                          context,
                                          msgId,
                                          wsmlMessages);
        } catch (ExecutionSemanticNotFoundException e) {
            logger.warn(e);
            throw e;
        } catch (SystemException e) {
            logger.warn(e);
            throw e;
        }
        return r;
    }
	
	
	/* (non-Javadoc)
	 * @see org.wsmo.execution.common.EntryPoint#getWebService(org.wsmo.execution.common.nonwsmo.WSMLDocument)
	 */
	public Context getWebService(WSMLDocument wsmlMessage) throws SystemException {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see org.wsmo.execution.common.EntryPoint#invokeWebService(org.wsmo.execution.common.nonwsmo.WSMLDocument, org.wsmo.execution.common.nonwsmo.Context)
	 */
	public Context invokeWebService(WSMLDocument wsmlMessage, Context context) throws SystemException {
      Context c = null; //TODO create context here
      logger.debug("In invokeWebService(wsmlMessage, context) Entry Point");
      //TODO MessageID
      try {
 		  List<WSMLDocument> msgs = new ArrayList<WSMLDocument>();
		  msgs.add(wsmlMessage);
          blockingExecuteWebService(msgs, context, new MessageId("0"));
      } catch (ExecutionSemanticNotFoundException e) {
          throw new SystemException("Execution semantic not found.", e);
      } catch (SystemException e) {
          throw e;
      }
      return c;
	}


	/* (non-Javadoc)
	 * @see org.wsmo.execution.common.EntryPoint#invokeWebService(org.wsmo.execution.common.nonwsmo.WSMLDocument)
	 */
	public Context invokeWebService(WSMLDocument wsmlMessage) throws SystemException {
        Context c = null; //TODO create context here
        logger.debug("In invokeWebService(wsmlMessage) Entry Point");
        Context context = new Context("executeWebService" + this.context++);
        //TODO MessageID
        try {
			List<WSMLDocument> msgs = new ArrayList<WSMLDocument>();
			msgs.add(wsmlMessage);
            blockingExecuteWebService(msgs, context, new MessageId("0"));
        } catch (ExecutionSemanticNotFoundException e) {
            throw new SystemException("Execution semantic not found.", e);
        } catch (SystemException e) {
            throw e;
        }
        //TODO: shouldn't it be "return context;" ??
        return c;
	}


	// ------------------------------------------------------------------- //
	// Receiver interface methods
	// ------------------------------------------------------------------- //

	@Exposed(description = "Wrapper for the Store entrypoint that accepts " +
			" user interface friendly types and return a human-readable confirmation.")
	public String store(String wsmlStringMessage) throws SystemException {
		WSMLDocument doc = new WSMLDocument(wsmlStringMessage);
		logger.debug("in store (string) entry point " + doc.getContent());
		Object r = null;
		try {
			r = confirmingStore(doc);
		} catch (ExecutionSemanticNotFoundException e) {
			logger.warn(e);
			return buildFailureMessage(e);
		} catch (SystemException e) {
			logger.warn(e);
			return buildFailureMessage(e);
		}
		String msg = "Execution semantic finished. Stored:\n";
		if (r != null) {
			msg += r.toString();
		}
		return msg;
	}
	
	/**
	 * Store a WSML object in WSMX
	 * 
	 */
	public Context store(WSMLDocument wsmlMessage) throws SystemException {
        logger.debug("storeEntity entrypoint invocation");
        //TODO create context here
        try {
			confirmingStore(wsmlMessage);
		} catch (SystemException e) {
			throw e;
		} catch (ExecutionSemanticNotFoundException e) {
			throw new SystemException(e);
		}
        return null;
	}
	
	private Object confirmingStore(WSMLDocument wsmlMessage) 
			throws SystemException, ExecutionSemanticNotFoundException {
        logger.debug("storeEntity entrypoint invocation");
        Context context = new Context("store" + this.context++);
        //TODO MessageID
        Object c = null;
        try {
			List<WSMLDocument> msgs = new ArrayList<WSMLDocument>();
			msgs.add(wsmlMessage);
			c = Environment.blockingSpawn("ie.deri.wsmx.executionsemantic.StoreEntity",
									  context,
									  new MessageId("0"),
									  msgs);
		} catch (ExecutionSemanticNotFoundException e) {
	        logger.warn("storeEntity entrypoint invocation failed.", e);
	        throw e;
		} catch (SystemException e) {
	        logger.warn("storeEntity entrypoint invocation failed.", e);
	        throw e;
		}
		return c;
	}
}