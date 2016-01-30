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

package org.wsmo.execution.common.nonwsmo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.wsmo.common.IRI;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;

public class DiscoveryType {
	
	public static final int WEBSERVICE_DISCOVERY = 0;
	public static final int WEBSERVICE_KEYWORD_DISCOVERY = 1;
    public static final int WEBSERVICE_LIGHTWEIGHT_DISCOVERY = 2;
    public static final int WEBSERVICE_LIGHTWEIGHT_RULE_DISCOVERY = 3;
    public static final int WEBSERVICE_LIGHTWEIGHT_DL_DISCOVERY = 4;
    public static final int WEBSERVICE_HEAVYWEIGHT_DISCOVERY = 5;

    public static final int SERVICE_DISCOVERY = 6;
	public static final int SERVICE_INSTANCEBASED_DISCOVERY = 7;
	public static final int SERVICE_INSTANCEBASED_COMPOSITION_DISCOVERY = 8;
	public static final int SERVICE_QOS_DISCOVERY = 9;
	
	private static final WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap());
	private static final IRI goalLightweightRule 				= wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/rule");
	private static final IRI goalHeavyweightRule 				= wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/rule/extendedPlugin");
	
	private static final IRI goalQoSIRI 				  		= wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/qos");
	private static final IRI goalInstanceBasedIRI 				= wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/instancebased");
	private static final IRI goalInstanceBasedCompositionIRI	= wsmoFactory.createIRI("http://www.wsmo.org/goal/discovery/instancebased/composition");
	
	public static int getRequiredDiscoveryType(Goal goal){
		int discoveryType = -1;

		//Lightweight Rule Discovery - NFP _"http://www.wsmo.org/goal/discovery/rule" hasValue "true"
		Set theNFPs = goal.getCapability().listNFPValues(goalLightweightRule);
		if (!theNFPs.isEmpty() && theNFPs.iterator().next().toString().toLowerCase().equals("true") ) {
			discoveryType = DiscoveryType.WEBSERVICE_LIGHTWEIGHT_RULE_DISCOVERY;
		} 
		
		//Heavyweight Rule Discovery - NFP _"http://www.wsmo.org/goal/discovery/rule/extendedPlugin" hasValue "true"
		theNFPs = goal.getCapability().listNFPValues(goalHeavyweightRule);
		if (!theNFPs.isEmpty() && theNFPs.iterator().next().toString().toLowerCase().equals("true") ) {
			discoveryType = DiscoveryType.WEBSERVICE_HEAVYWEIGHT_DISCOVERY;
		} 
		
		//QoSInstance based Discovery - NFP _"http://www.wsmo.org/goal/discovery/qos" hasValue "true"
		theNFPs = goal.getCapability().listNFPValues(goalQoSIRI);
		if (!theNFPs.isEmpty() && theNFPs.iterator().next().toString().toLowerCase().equals("true") ) {
			discoveryType = DiscoveryType.SERVICE_QOS_DISCOVERY;
		} 
		
		//Instance based Discovery - NFP _"http://www.wsmo.org/goal/discovery/instancebased" hasValue "true"
		theNFPs = goal.getCapability().listNFPValues(goalInstanceBasedIRI);
		if (!theNFPs.isEmpty() && theNFPs.iterator().next().toString().toLowerCase().equals("true") ) {
			discoveryType = DiscoveryType.SERVICE_INSTANCEBASED_DISCOVERY;
		}
		
		//Instance based Discovery - NFP _"http://www.wsmo.org/goal/discovery/instancebased/composition" hasValue "true"
		theNFPs = goal.getCapability().listNFPValues(goalInstanceBasedCompositionIRI);
		if (!theNFPs.isEmpty() && theNFPs.iterator().next().toString().toLowerCase().equals("true") ) {
			discoveryType = DiscoveryType.SERVICE_INSTANCEBASED_COMPOSITION_DISCOVERY;
		} 
		return discoveryType;
	}

}

