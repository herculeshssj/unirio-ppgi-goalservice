/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ch.epfl.qosdisc.wsmx;

import ch.epfl.qosdisc.database.*;
import ch.epfl.qosdisc.operators.PropertySet;

public class TestReasoner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

    	PropertySet.setup(".");
	
		Reasoner rc = new Reasoner();
		rc.addOntology("file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/QoSBase.wsml");
		rc.addOntology("file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/HotelQoSBase.wsml");
		//rc.addOntology("file:///c:/Data/LSIR/ontologies/DemoSets/Lite/Service0.wsml#I0Param");
		//rc.addOntology("file:///c:/Data/LSIR/ontologies/DemoSets/Lite/Goal0.wsml");
		
		rc.execute("_\"file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/QoSBase.wsml#QualityRangeMin\"(?x,?y)");
		rc.execute("_\"file:///c:/Data/LSIR/Workspace/QoSDiscovery/ontologies/Lite/QoSBase.wsml#QualityRangeDefault\"(?x,?y)");
		//rc.execute("_\"file:///c:/Data/LSIR/ontologies/DemoSets/Lite/QoSBase.wsml#GoalList\"(?x)");
		//rc.execute("_\"file:///c:/Data/LSIR/ontologies/DemoSets/Lite/QoSBase.wsml#GoalSatisfied\"(?x)");
		//rc.execute("_\"file:///c:/Data/LSIR/ontologies/DemoSets/Lite/QoSBase.wsml#ServiceList\"(?x)");
		//rc.execute("_\"file:///c:/Data/LSIR/ontologies/DemoSets/Lite/QoSBase.wsml#ServiceSatisfied\"(?x)");
		//rc.execute("_\"file:///c:/Data/LSIR/ontologies/DemoSets/Lite/QoSBase.wsml#SelectQuality\"(?x,?y,?z)");
		//rc.execute("_\"file:///c:/Data/LSIR/ontologies/DemoSets/Lite/QoSBase.wsml#SelectUnit\"(?x,?y)");
		//rc.execute("_\"file:///c:/Data/LSIR/ontologies/DemoSets/Lite/QoSBase.wsml#SelectSpecQuality\"(?x)");
		//rc.execute("_\"file:///c:/Data/LSIR/ontologies/DemoSets/Lite/QoSBase.wsml#SelectQualityUnit\"(?x,?y)");
		//rc.execute("?x subConceptOf ?y");
		//rc.execute("?x memberOf ?y");

		rc.clean();

	}

}
