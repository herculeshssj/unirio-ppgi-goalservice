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

import org.wsmo.service.*;

/**
 * Interface or class description
 * 
 * <pre>
 *  Created on 20.09.2006
 *  Committed by $Author: holgerlausen $
 *  $Source: /cvsroot/wsmx/components/discovery/src/main/ie/deri/wsmx/discovery/AbstractWSMODiscoveryImpl.java,v $,
 * </pre>
 * 
 * @author Holger Lausen
 * @version $Revision: 1.1 $ $Date: 2006/09/21 08:45:58 $
 */
public abstract class AbstractWSMODiscoveryImpl implements WSMODiscovery {
	
	public void addWebService(Set<WebService> services)
    			throws DiscoveryException {
		for (Iterator<WebService> i = services.iterator(); i.hasNext();) {
			WebService ws = (WebService) i.next();
			addWebService(ws);
		}
	}
				
    public void removeWebService(Set<WebService> services) {
        for (Iterator<WebService> i = services.iterator(); i.hasNext();) {
            WebService ws = (WebService) i.next();
            removeWebService(ws);
        }
    }
  
	public abstract void addWebService(WebService service) throws DiscoveryException;
	
	public abstract void removeWebService(WebService service);
 
}
