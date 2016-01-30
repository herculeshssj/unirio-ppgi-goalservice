/*
 * Copyright (c) 2006, University of Innsbruck, Austria.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ie.deri.wsmx.discovery;

import java.util.*;

import org.wsmo.execution.common.component.*;
import org.wsmo.service.*;

/**
 * Defines an interface that all discovery engines 
 * to be included in the framework must implement.
 * 
 * @author Adina Sirbu
 * @version $Revision: 1.3 $ $Date: 2006/09/21 08:45:58 $
 */
public interface WSMODiscovery  extends Discovery {
	
	/**
	 * Adds a Web service to the discovery engine
	 * @throws DiscoveryException 
	 */
	public void addWebService(WebService service) throws DiscoveryException;
	
	/**
	 * Adds the set of Web services to the discovery engine
	 * @throws DiscoveryException
	 */
	public void addWebService(Set<WebService> services) throws DiscoveryException;
	
	/**
	 * Removes a Web service from the discovery engine
	 */
	public void removeWebService(WebService service);
	
	/**
	 * Removes the set of Web services from the discovery engine
	 */
	public void removeWebService(Set<WebService> services);
    
}
