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

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.scheduler.Environment;
import ie.deri.wsmx.scheduler.Proxy;
import ie.deri.wsmx.scheduler.RoutingException;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.deri.wsmo4j.io.serializer.wsml.SerializerImpl;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.component.ChoreographyEngine;
import org.wsmo.execution.common.component.DataMediator;
import org.wsmo.execution.common.component.Discovery;
import org.wsmo.execution.common.component.Invoker;
import org.wsmo.execution.common.component.NonFunctionalSelector;
import org.wsmo.execution.common.component.Parser;
import org.wsmo.execution.common.component.ChoreographyEngine.Direction;
import org.wsmo.execution.common.component.resourcemanager.NonWSMOResourceManager;
import org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.exception.SystemException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.MessageId;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.execution.common.nonwsmo.grounding.WSDL1_1EndpointGrounding;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;
import org.wsmo.service.signature.Grounding;
import org.wsmo.service.signature.WSDLGrounding;
import org.wsmo.wsml.Serializer;


/**
 * Execution Semantics with support for choreography, discovery and data mediation.
 *
 * <pre>
 * Created on Nov 5, 2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/executionsemantic/DiscoverWebServices.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.6 $ $Date: 2007-10-11 14:33:53 $
 */ 
public class DiscoverWebServices extends WSMXExecutionSemantic {

	private static final long serialVersionUID = 54141834313553200L;
	static Logger logger = Logger.getLogger(DiscoverWebServices.class);
	
	//global variables for whole execution semantics
	public MessageId messageId = null;
	public String wsmlMessage = null;

	//after parsing
	public Goal goal = null;
	public Ontology goalOnto = null;
	//after discovery
	List<WebService> discoveredWebServices = null;
	
	//after data mediation
	public List<Entity> mediatedEntities = null;
	
	public AbstractExecutionSemantic executionSemantic;
	
	static private int counter = 0;
	
	public void cleanUp(){
		logger.error("---Cleaning active execution semantic---");		
		if (goalOnto == null)
			return;
		
		List<Entity> goalInstances = Helper.getInstances(goalOnto);
		//clean up after communication
		//clean up Goal + Goal ontology
		try {
			for (Entity i : goalInstances ){
				Map attr = ((Instance) i).listAttributeValues();
				Set<IRI> keys = attr.keySet();
					
				for (IRI iri : keys){
					Object values = attr.get(iri);
					((Instance) i).removeAttributeValues(iri);
				}
				goalOnto.removeInstance(((Instance) i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (goal != null)
			goal.removeOntology(goalOnto);
		goalOnto.removeOntology(goalOnto);

		goal = null;
		goalOnto = null;
	};
	
	static {
		logger.setLevel(Level.ALL);
	}

	public DiscoverWebServices() {
		super();
	}
	
    public DiscoverWebServices(Context contextId, MessageId messageId, String wsmlMessage) {
        super();
		List<String> msgs = new ArrayList<String>();
		msgs.add(wsmlMessage);
        initialize(contextId, messageId, msgs);
    }

    public void initialize(Context contextId, MessageId messageId, List<String> wsmlMessages) {
    	this.messageId = messageId;
    	this.wsmlMessage = wsmlMessages.get(0);
    	executionSemantic = this;
    	
        state = new ReceivedMessageProcessing(contextId);
        tagStateForTerminationOnFailure();
    }
    
    public DiscoverWebServices(State state) {
        super();
        this.state = state;
    }

    @SuppressWarnings("unchecked")
	public <E> Proxy<E> getProxy(Class<E> clazz) {
        return (Proxy<E>) new PerformWebServiceResourceRetrieval(state.getContext());
    }
    
	class ReceivedMessageProcessing extends State {    
		private static final long serialVersionUID = -6915488944488044350L;
	
	    public ReceivedMessageProcessing(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }
	
	    @Override
	    public State handleState(Object component)	    	
	            throws UnsupportedOperationException, ComponentException {
	        return new MessageBackup(contextId);
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
		
	    public MessageBackup(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException {
	    	logger.debug("Message backup.");
	        ((NonWSMOResourceManager)component).saveMessage(contextId, messageId, wsmlMessage);
	        return new WSMLMessageParsing(contextId);
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

	    public WSMLMessageParsing(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException, DataFlowException {
	    	logger.debug("Parsing.");
	    	
	    	try {
		    	Parser parser = ((Parser)component);
	    		Set<Entity> topEntities = parser.parse(new WSMLDocument(wsmlMessage));

				//get Goal, get Ontology
				for (Entity ent : topEntities){
					if (ent instanceof Goal)
						goal = (Goal) ent;
					if (ent instanceof Ontology)
						goalOnto = (Ontology) ent;
				}
		    	
		    	if (goal == null)
		    		throw new DataFlowException("Execution semantics must be given a valid goal.");
		    	logger.info("Parsing completed.");
	    	} catch (ClassCastException cce) {
	    		throw new RoutingException(Parser.class, component.getClass());
	    	} catch (Throwable t) {
    			throw new ComponentException("Unexpected failure inside parser component.", t);
	    	}
	    	
	        return new PerformDiscovery(contextId);
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

	class PerformDiscovery extends State {
		private static final long serialVersionUID = -8126105336257532540L;
		
	    public PerformDiscovery(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException, DataFlowException {
	    	logger.debug("Discovering suitable webservices for: " + goal);
	    	Discovery discovery;
	    	try {
	    		discovery = ((Discovery)component);
	    	} catch (ClassCastException cce) {
			   	throw new RoutingException(Discovery.class, component.getClass());
			}
			List<WebService> discoveredWebServices = discovery.discover(goal);
			
			String info = "";
			if (discoveredWebServices.size()==0)
				info="Discovery did not result in any candidate services.";
				
			ExecutionSemanticsFinalResponse response = new ExecutionSemanticsFinalResponse(goal,discoveredWebServices, 
					   new HashSet(), 
					   new HashSet(), 
					   true, 
					   executionSemantic,
					   info);
			return new EntryPoint(contextId,response);				
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.WEBSERVICEDISCOVERY;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}
	}


	public class EntryPoint extends State {
		private static final long serialVersionUID = 7305173447500494311L;
		Serializable r;

	    public EntryPoint(Context contextId, Serializable r) {
	        super();
	        this.contextId = contextId;
	        this.r = r;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException {
	    	logger.debug("Entrypoint state.");
	    	//not needed
	        return new Exodus();
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.ENTRYPOINT;
		}

		@Override
		public Serializable getData() {
			return r;
		}

		@Override
		public boolean isStatefulResponse() {
			return true;
		}
	}
	
	class Exodus extends State {
		private static final long serialVersionUID = 5417996174132567582L;

		String output = null;

		public Exodus(){
			super();
		}
		
	    public Exodus(Context contextId, String output) {
	        super();
	        this.contextId = contextId;
	        this.output = output;
	    }
		
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

	class PerformWebServiceResourceRetrieval extends State implements WebServiceResourceManager, Proxy<WebServiceResourceManager> {
		private static final long serialVersionUID = 2826724910732574204L;
		Set<WebService> data = null;

	    public PerformWebServiceResourceRetrieval(Context contextId) {
	        super();
	        this.contextId = contextId;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException {
	    	logger.debug("Resource retrival.");
	    	WebServiceResourceManager rm;
	    	try {
	    		rm = ((WebServiceResourceManager)component);
	    	} catch (ClassCastException cce) {
			   	throw new RoutingException(DataMediator.class, component.getClass());
			}
	    	Set<WebService> data = rm.retrieveWebServices();
	    	return new WebServiceCarrier(contextId, data);
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.RESOURCEMANAGER;
		}

		public WebServiceResourceManager muteToComponent() {
			return this;
		}

		public void removeWebService(WebService arg0) throws ComponentException, UnsupportedOperationException {
			// TODO Auto-generated method stub
			
		}

		public Set<Identifier> getWebServiceIdentifiers() throws ComponentException, UnsupportedOperationException {
			// TODO Auto-generated method stub
			return null;
		}

		public Set<Identifier> getWebServiceIdentifiers(Namespace arg0) throws ComponentException, UnsupportedOperationException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean containsWebService(Identifier arg0) throws ComponentException, UnsupportedOperationException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}

        public void storeWebService(WebService webService) throws ComponentException, UnsupportedOperationException {
            // TODO Auto-generated method stub
        }

        public Set<WebService> retrieveWebServices() throws ComponentException, UnsupportedOperationException {
        	tagStateForTerminationOnFailure();
			Set<WebService> s = null;
			try {
				DiscoverWebServices delegate = new DiscoverWebServices(new PerformWebServiceResourceRetrieval(getContext()));
				AbstractExecutionSemantic carrier = spawn(DiscoverWebServices.this, delegate);
				s = (Set<WebService>) carrier.getState().getData();
			} catch (SystemException e) {
				throw new ComponentException("Unable to retrieve webservices due to system failure.", e);
			} catch (Throwable t) {
				throw new ComponentException("Unable to retrieve webservices due to unexpected failure.", t);
			}
			untagStateForTerminationOnFailure();
			return s;
        }

        public Set<WebService> retrieveWebServices(Namespace namespace) throws ComponentException, UnsupportedOperationException {
            // TODO Auto-generated method stub
            return null;
        }

        public WebService retrieveWebService(Identifier identifier) throws ComponentException, UnsupportedOperationException {
            // TODO Auto-generated method stub
            return null;
        }

        public Set<Namespace> getWebServiceNamespaces() throws ComponentException, UnsupportedOperationException {
            // TODO Auto-generated method stub
            return null;
        }

		/* (non-Javadoc)
		 * @see org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager#retrieveWebServices(int)
		 */
		public Set<WebService> retrieveWebServices(int discoveryType) throws ComponentException, UnsupportedOperationException {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager#retrieveWebServicesReferringOntology(org.wsmo.common.Identifier)
		 */
		public Set<WebService> retrieveWebServicesReferringOntology(
				Identifier ontoIdentifer) throws ComponentException,
				UnsupportedOperationException {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class WebServiceCarrier extends State {
		private static final long serialVersionUID = -2669305420557478782L;
		Set<WebService> s;

	    public WebServiceCarrier(Context contextId, Set<WebService> services) {
	        super();
	        this.contextId = contextId;
	        this.s = services;
	    }

	    @Override
	    public State handleState(Object component)
	            throws UnsupportedOperationException, ComponentException, RoutingException {
	    	logger.debug("RetrievedWebServices state.");
	    	//branch dies
	        return new Exodus();
	    }

		@Override
		public Event getAssociatedEvent() {
			return Event.WEBSERVICEDISCOVERY;
		}

		@Override
		public Serializable getData() {
			return (Serializable) s;
		}

		@Override
		public boolean isStatefulResponse() {
			return true;
		}
	}
}