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

import ie.deri.wsmx.scheduler.Proxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Entity;
import org.wsmo.execution.common.component.Parser;
import org.wsmo.execution.common.component.resourcemanager.GoalResourceManager;
import org.wsmo.execution.common.component.resourcemanager.NonWSMOResourceManager;
import org.wsmo.execution.common.component.resourcemanager.OntologyResourceManager;
import org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.MessageId;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

/**
 * Backups received WSML messages as String to the ResourceManager,
 * has it parsed by the Parser component and stores the resulting
 * objects in the ResourceManager by invoking different methods
 * dependant upon the type of the object deliverd from the parser.
 * 
 * <pre>
 *   Created on 19.05.2005
 *   Committed by $Author: maciejzaremba $
 *   $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/executionsemantic/StoreEntity.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 * 
 * @version $Revision: 1.6 $ $Date: 2006-10-03 14:45:18 $
 */
public class StoreEntity extends WSMXExecutionSemantic {
    private static final long serialVersionUID = 3256446889141418038L;
    static Logger logger = Logger.getLogger(StoreEntity.class);

    public StoreEntity() {
        super();
    }

    public StoreEntity(Context contextId, MessageId messageId, String message) {
        super();
		List<String> msgs = new ArrayList<String>();
		msgs.add(message);
        initialize(contextId, messageId, msgs);
    }
    
    /* (non-Javadoc)
	 * @see ie.deri.wsmx.executionsemantic.StoreEntity#initialize(org.wsmo.execution.common.nonwsmo.Context, org.wsmo.execution.common.nonwsmo.MessageId, java.lang.String)
	 */
    public void initialize(Context contextId, MessageId messageId, List<String> messages) {
    	state = new ReceivedMessageProcessing(contextId, messageId, messages.get(0));
        //FIXME assumption that we kick this one off with expecting a confirmation
        tagStateForTerminationOnFailure();
    }

    /* (non-Javadoc)
	 * @see ie.deri.wsmx.executionsemantic.StoreEntity#getProxy(java.lang.Class)
	 */
    public <E> Proxy<E> getProxy(Class<E> clazz) {
        return null;
    }

	class ReceivedMessageProcessing extends State {    
		private static final long serialVersionUID = -6915488944488044350L;
		public MessageId messageId = null;
	    public String message = null;
	
	    public ReceivedMessageProcessing(Context contextId, MessageId messageId, String message) {
	        super();
	        this.contextId = contextId;
	        this.messageId = messageId;
	        this.message = message;
	    }
	    
	    @Override
	    public State handleState(Object component)	    	
	            throws UnsupportedOperationException, ComponentException {
	        return new MessageBackup(contextId, messageId, message);
	    }
	
		@Override
		public Event getAssociatedEvent() {
			return Event.ENTRYPOINT;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
		
	}	
		
	class MessageBackup extends State {    
		private static final long serialVersionUID = -4397638933116562437L;
		public MessageId messageId = null;
	    public String message = null;

	    public MessageBackup(Context contextId, MessageId messageId, String message) {
	        super();
	        this.contextId = contextId;
	        this.messageId = messageId;
	        this.message = message;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException {
	    	logger.debug("Message backup.");
	        ((NonWSMOResourceManager)component).saveMessage(contextId, messageId,
	                message);
	        return new WSMLMessageParsing(contextId, new WSMLDocument(message));
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.RESOURCEMANAGER;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}


	class WSMLMessageParsing extends State {    
		private static final long serialVersionUID = -5023865882111569879L;
		public WSMLDocument message = null;

	    public WSMLMessageParsing(Context contextId, WSMLDocument message) {
	        super();
	        this.contextId = contextId;
	        this.message = message;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException {
	    	logger.debug("Parsing.");
	    	Set<Entity> data = ((Parser)component).parse(message);
	        return new StoreWSMOObject(contextId, data);
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.PARSER;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}

	class StoreWSMOObject extends State {
		private static final long serialVersionUID = -6827420664962295154L;
		public Set<Entity> data = null;

	    public StoreWSMOObject(Context contextId, Set<Entity> data) {
	        super();
	        this.contextId = contextId;
	        this.data = data;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException {
	    	logger.debug("Storing: " + data);
	    	for (Entity wsmoEntity : data) {
	    		if (wsmoEntity instanceof WebService)
	    			((WebServiceResourceManager)component).storeWebService((WebService)wsmoEntity);
	    		if (wsmoEntity instanceof Goal)
	    			((GoalResourceManager)component).storeGoal((Goal)wsmoEntity);
	    		if (wsmoEntity instanceof Ontology)
	    			((OntologyResourceManager)component).storeOntology((Ontology)wsmoEntity);	    		
	    	}
	    	Serializable sdata = null;
	    	try {
	    		sdata = (Serializable) data;
	    	} catch (ClassCastException e) {
	    		ComponentException ce = new ComponentException(
	    				"Stored data is not serializable and cannot be carried back for confirmation.", e);
	    		logger.warn(ce.getMessage(), ce);
	    		throw ce;
	    	}
	        return new Confirmation(contextId, sdata);
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.RESOURCEMANAGER;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}
	
	class Confirmation extends State {
		private static final long serialVersionUID = -2878343381562083978L;
		private Serializable confirmation;
		
	    public Confirmation(Context contextId, Serializable confirmation) {
	        super();
	        this.contextId = contextId;
	        this.confirmation = confirmation;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException {
	    	logger.debug("Confirmation.");

	        return new Exodus();
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.ENTRYPOINT;
		}
		
		@Override
		public Serializable getData() {
			return confirmation;
		}

		@Override
		public boolean isStatefulResponse() {
			return true;
		}
	}
	
	class Exodus extends State {
		private static final long serialVersionUID = 5417996174132567582L;

		@Override
		public State handleState(Object component) throws UnsupportedOperationException, ComponentException {
			return null;
		}

		@Override
		public Event getAssociatedEvent() {
			return Event.EXODUS;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}		
	}
	
}
