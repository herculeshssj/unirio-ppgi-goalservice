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

//import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.scheduler.Environment;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

import org.apache.log4j.Logger;
import org.omwg.ontology.Ontology;
import org.wsmo.common.*;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.mediator.Mediator;
import org.wsmo.service.*;

import ch.epfl.qosdisc.database.*;
import ch.epfl.qosdisc.operators.PropertySet;
import ch.epfl.qosdisc.operators.TestFrame;


/**
 * Standalone testing class. This class allows testing of the new
 * optimized ontologies and the database back-end. The configuration
 * file is set up for this type of run by default.
 * Before running this, it is necessary to populate the database by
 * running LoadDatabase once.
 * 
 * @author Sebastian Gerlach
 */
public class TestStandaloneDatabase {

    /**
     * A logger for outputting silly comments, and also some potentially useful
     * information. 
     */
    static Logger log = Logger.getLogger(TestStandaloneDatabase.class);
	
    /**
     * A main function for standalone testing.
     * 
     * @param args The command line arguments.
     */
	public static void main(String[] args) {

        // Run the stand-alone test.
//        TestFrame tf = new TestFrame(System.getProperty("user.dir")+File.separator+"resources"+File.separator+"qosdiscovery");
		TestFrame tf = new TestFrame(System.getProperty("user.dir")+File.separator+"resources"+File.separator+"qosdiscovery");
        
        try {

        	// Open database connection.
        	Connection.open(PropertySet.props);
        
        	// Load a goal.
        	//String goalIRI = PropertySet.getProperty("goal");
//        	String goalIRI = "file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/3-GoalDoConversion.wsml#GoalDoConversion";
//        	String goalIRI = "file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetCurrencyRate.wsml#GoalGetCurrencyRate";

//        	String goalIRI = "file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetCurrencyRate.wsml#GoalGetCurrencyRate";
//        	String goalIRI = "file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetQuote.wsml#GoalGetQuote";

        	Helper.parse(new File(PropertySet.getPath()+"/ontologies/bankinter/GoalsQoS/Goal-QoS1.wsml"));
        	Helper.getTopEntity("file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/GoalsQoS/Goal-QoS1.wsml#QoSRequiredParams1");
        	
        	Helper.parse(new File(PropertySet.getPath()+"/ontologies/bankinter/Goals/GoalGetNews.wsml"));
        	
        	
        	String goalIRI = "file:///c:/WSMX/resources/qosdiscovery/ontologies/bankinter/Goals/GoalGetNews.wsml#GoalGetNews";
        	
        	//EPFL goals
//			String goalIRI = "file:///c:/WSMX/resources/qosdiscovery/ontologies/BankInter-Lite/GoalGetQuote.wsml#GoalGetQuote";
//        	String goalIRI = "file:///c:/WSMX/resources/qosdiscovery/ontologies/BankInter-Lite/GoalGetQuote-Generated.wsml#InteractiveDefinedGoal";
        	
        	System.out.println(goalIRI);
        	Collection<TopEntity> e = WSMLStore.getEntities(goalIRI, WSMLStore.GOAL, true);
        	
        	// Check that we have only a single goal specified. 
        	if(e.size() != 1) {
        		
        		throw new Exception("The provided IRI specifies "+e.size()+" goals, a single one was expected.");
        	}
        	Goal goal = (Goal)e.iterator().next();
        	
        	// Get all web services.
        	Collection<WebService> services = WSMLStore.getAllWebServices();
        	log.debug("Services in candidates list: "+services.size());
        
            // Add the services to the test frame.
            for(WebService s : services)
                tf.addService(s);
            
            // And find the matching services.
        	log.debug("Starting QoS discovery.");
            tf.achieveGoalDatabase(goal);
            
            // Remove the goal from the WSMLStore memory cache.
        	WSMLStore.removeEntities(((IRI)goal.getIdentifier()).getNamespace());
        	
        	// Close database connection.
        	Connection.close();

        } catch(Exception ex) {
        
        	// Print the stack trace.
        	ex.printStackTrace();
        }

    }


}
