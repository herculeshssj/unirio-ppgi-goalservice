/**
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

import org.deri.wsmx.discovery.rule.LWRuleDiscovery;
import org.deri.wsmx.discovery.rule.complete.HWRuleCompleteDiscovery;
import org.wsmo.execution.common.nonwsmo.DiscoveryType;

import ie.deri.wsmx.discovery.keyword.*;
import ie.deri.wsmx.discovery.lightweight.LightweightDiscovery;
import ie.deri.wsmx.discovery.lightweightdl.*;

/**
 * Offers the factory method as a way of creating discovery engines.
 * 
 * @author Adina Sirbu
 * @version $Revision: 1.6 $ $Date: 2007/06/14 14:55:02 $
 */
public class DiscoveryFactory {
	
	/**
	 * Returns a discovery engine of the specified type, if the type is known/supported.
	 * @throws UnsupportedOperationException
	 */
	public static WSMODiscovery createDiscoveryEngine(int type) 
			throws UnsupportedOperationException {
		
		WSMODiscovery discoveryEngine;
        switch (type) {
        case DiscoveryType.WEBSERVICE_KEYWORD_DISCOVERY:
            discoveryEngine = new KeywordDiscovery();
            break;
        case DiscoveryType.WEBSERVICE_LIGHTWEIGHT_DISCOVERY:
            discoveryEngine = new LightweightDiscovery();
            break;
        case DiscoveryType.WEBSERVICE_LIGHTWEIGHT_RULE_DISCOVERY:
            discoveryEngine = new LWRuleDiscovery();
            break;
        case DiscoveryType.WEBSERVICE_LIGHTWEIGHT_DL_DISCOVERY:
            discoveryEngine = new DLBasedDiscovery();
            break;
        case DiscoveryType.WEBSERVICE_HEAVYWEIGHT_DISCOVERY:
        	discoveryEngine = new HWRuleCompleteDiscovery();
        	break;
        default:
            throw new UnsupportedOperationException("Type of engine does not exist");
        }
		return discoveryEngine;
	}
}