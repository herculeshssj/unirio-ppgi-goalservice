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

package ie.deri.wsmx.executionsemantic.proxy;

import java.util.Set;

import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.execution.common.component.resourcemanager.WebServiceResourceManager;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.service.WebService;

import ie.deri.wsmx.scheduler.Proxy;

public class WebServiceResourceManagerProxy implements WebServiceResourceManager, Proxy<WebServiceResourceManager> {

	public WebServiceResourceManagerProxy() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void storeWebService(WebService arg0) throws ComponentException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		
	}

	public void removeWebService(WebService arg0) throws ComponentException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		
	}

	public Set<WebService> retrieveWebServices() throws ComponentException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<WebService> retrieveWebServices(Namespace arg0) throws ComponentException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	public WebService retrieveWebService(Identifier arg0) throws ComponentException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Namespace> getWebServiceNamespaces() throws ComponentException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
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

	public WebServiceResourceManager muteToComponent() {
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
	@Override
	public Set<WebService> retrieveWebServicesReferringOntology(
			Identifier ontoIdentifer) throws ComponentException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

}
