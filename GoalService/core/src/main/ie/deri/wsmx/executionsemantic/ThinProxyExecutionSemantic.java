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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Entity;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.execution.common.component.DataMediator;
import org.wsmo.execution.common.component.Discovery;
import org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.MessageId;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;
import org.wsmo.service.WebService;

/**
 * Thin proxies.
 *
 * <pre>
 * Created on Nov 5, 2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/executionsemantic/ThinProxyExecutionSemantic.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2007-06-14 14:42:06 $
 */ 
public abstract class ThinProxyExecutionSemantic extends WSMXExecutionSemantic {

	static Logger logger = Logger.getLogger(ThinProxyExecutionSemantic.class);

	public ThinProxyExecutionSemantic() {
		super();
	}
	
    public ThinProxyExecutionSemantic(Context contextId, MessageId messageId, String message) {
        super();
        initialize(contextId, messageId, message);
    }

    public void initialize(Context contextId, MessageId messageId, String message) {
        //state = new ReceivedMessageProcessing(contextId, messageId, message);
        //tagStateForTerminationOnFailure();
    }
    
    public ThinProxyExecutionSemantic(State state) {
        super();
        this.state = state;
    }

    @SuppressWarnings("unchecked")
	public <E> Proxy<E> getProxy(Class<E> clazz) {
//        return (Proxy<E>) new P();
        return null;
    }
	
	//Proxies follow below this line	

	class PerformDiscovery implements Discovery, Proxy<Discovery> {				
		public Discovery d;
		
		public List<WebService> discover(Goal arg0) throws ComponentException, UnsupportedOperationException {
			return d.discover(arg0);
		}

		public List<WebService> discover(Goal goal, Set<WebService> searchSpace) throws ComponentException, UnsupportedOperationException {
			return d.discover(goal, searchSpace);
		}

		public Map<Map<WebService, Interface>, Identifier> discover(Goal arg0, Ontology arg1) throws ComponentException, UnsupportedOperationException {
			return d.discover(arg0, arg1);
		}
		
		public Discovery muteToComponent() {
			return this;
		}

	}    
    
	class PerformDataMediation implements DataMediator, Proxy<DataMediator> {				
		public DataMediator m;
		
		public Map<Entity, List<Entity>> mediate(Ontology arg0, Ontology arg1, Set<Entity> arg2) throws ComponentException, UnsupportedOperationException {
			return m.mediate(arg0, arg1, arg2);
		}

		public List<Entity> mediate(Ontology arg0, Ontology arg1, Entity arg2) throws ComponentException, UnsupportedOperationException {
			return m.mediate(arg0, arg1, arg2);
		}

		public DataMediator muteToComponent() {
			return this;
		}		
	}    
    
	class PerformWebServiceResourceRetrieval implements WebServiceResourceManager, Proxy<WebServiceResourceManager> {
		public WebServiceResourceManager rm;
		
		public PerformWebServiceResourceRetrieval(WebServiceResourceManager rm) {
			super();
			this.rm = rm;
		}

		public WebServiceResourceManager muteToComponent() {
			return this;
		}

		public void removeWebService(WebService arg0) throws ComponentException, UnsupportedOperationException {
			rm.removeWebService(arg0);
		}

		public Set<Identifier> getWebServiceIdentifiers() throws ComponentException, UnsupportedOperationException {
			return rm.getWebServiceIdentifiers();
		}

		public Set<Identifier> getWebServiceIdentifiers(Namespace arg0) throws ComponentException, UnsupportedOperationException {
			return rm.getWebServiceIdentifiers(arg0);
		}

		public boolean containsWebService(Identifier arg0) throws ComponentException, UnsupportedOperationException {
			return rm.containsWebService(arg0);
		}

        public void storeWebService(WebService webService) throws ComponentException, UnsupportedOperationException {
        	rm.storeWebService(webService);
        }

        public Set<WebService> retrieveWebServices() throws ComponentException, UnsupportedOperationException {
        	return rm.retrieveWebServices();
        }

        public Set<WebService> retrieveWebServices(Namespace namespace) throws ComponentException, UnsupportedOperationException {
        	return rm.retrieveWebServices(namespace);
        }

        public WebService retrieveWebService(Identifier identifier) throws ComponentException, UnsupportedOperationException {
        	return rm.retrieveWebService(identifier);
        }

        public Set<Namespace> getWebServiceNamespaces() throws ComponentException, UnsupportedOperationException {
            return rm.getWebServiceNamespaces();
        }

        public Set<WebService> retrieveWebServices(int discoveryType) throws ComponentException, UnsupportedOperationException {
			return rm.retrieveWebServices(discoveryType);
		}

		public Set<WebService> retrieveWebServicesReferringOntology(
				Identifier ontoIdentifer) throws ComponentException,
				UnsupportedOperationException {
			return rm.retrieveWebServicesReferringOntology(ontoIdentifer);		}
	}
	
}
