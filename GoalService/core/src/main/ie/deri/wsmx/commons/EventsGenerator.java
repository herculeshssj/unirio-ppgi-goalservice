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

package ie.deri.wsmx.commons;

import java.util.Calendar;
import java.util.HashMap;

import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 16 Jan 2007
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/commons/EventsGenerator.java,v $, * @version $Revision: 1.1 $ $Date: 2007-02-05 15:25:30 $
 */
public class EventsGenerator {

	private final static WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap()); 
	private final static DataFactory dataFactory = Factory.createDataFactory(new HashMap());
	
	private final static String SEE_ONTOLOGY_NS = "http://irs.open.ac.uk/seeEvents#";
	private final static String SEE_ONTOLOGY = SEE_ONTOLOGY_NS + "seeEvents";

	private final static String MONITORING_ONTOLOGY_NS = "http://irs.open.ac.uk/monitoringEvents#";
	private final static String MONITORING_ONTOLOGY = MONITORING_ONTOLOGY_NS + "monitoringEvents";
	
	//main events
	private final static String STARTACHIEVEGOALEVENT = SEE_ONTOLOGY_NS+"startAchieveGoalEvent";
	private final static String ENDACHIEVEGOALEVENT = SEE_ONTOLOGY_NS+"endAchieveGoalEvent";

	private final static String STARTINVOKEWEBSERVICEEVENT = SEE_ONTOLOGY_NS+"startInvokeWebServiceEvent";
	private final static String ENDINVOKEWEBSERVICEEVENT = SEE_ONTOLOGY_NS+"endInvokeWebServiceEvent";
	
	//attribiutes of http://irs.open.ac.uk/seeEvents#seeEvent";
	private final static String HASSEESIONID_ATTR = SEE_ONTOLOGY_NS+"hasSessionId";

	//attribiute of http://irs.open.ac.uk/seeEvents#startInvokeWebServiceEvent and #startAchieveGoalEvent
	private final static String HASINPUTDATA_ATTR = SEE_ONTOLOGY_NS+"hasInputData";
	//attribiute of http://irs.open.ac.uk/seeEvents#endInvokeWebServiceEvent and #endAchieveGoalEvent
	private final static String HASOUTPUTDATA_ATTR = SEE_ONTOLOGY_NS+"hasOutputData";
	
	//attribiute of http://irs.open.ac.uk/seeEvents#achieveGoalEvent";
	private final static String HASGOAL_ATTR = SEE_ONTOLOGY_NS+"hasGoal";
	
	//attribiute of http://irs.open.ac.uk/seeEvents#invokeWebServiceEvent";
	private final static String HASWEBSERVICE_ATTR = SEE_ONTOLOGY_NS+"hasWebService";

	//attribiutes of http://irs.open.ac.uk/monitoringEvents#event";
	private final static String HASTIMESTAMP_ATTR = MONITORING_ONTOLOGY_NS+"hasTimestamp";
	private final static String HASPROCESSINSTANCEID_ATTR = MONITORING_ONTOLOGY_NS+"hasProcessInstanceId";
	private final static String GENERATEDBY_ATTR = MONITORING_ONTOLOGY_NS+"generatedBy";
	

	private static Ontology getSeeEventsOntology(){
		return Helper.getOntology(wsmoFactory.createIRI(SEE_ONTOLOGY));
	}
	
	private static Concept getConcept(Ontology onto, String iriOfConcept){
		return onto.findConcept(wsmoFactory.createIRI(iriOfConcept));
	}

	public static Instance createStartAchieveGoalEvent(String goal, String hasInputData) {
		Instance i = wsmoFactory.createInstance(wsmoFactory.createIRI("http://www.wsmo.org/seeEvents#instance"+Helper.getRandomLong()));
		try {
			i.addConcept(getConcept(getSeeEventsOntology(),STARTACHIEVEGOALEVENT));
			i.addAttributeValue(wsmoFactory.createIRI(HASINPUTDATA_ATTR), dataFactory.createWsmlString(hasInputData));
			i.addAttributeValue(wsmoFactory.createIRI(HASGOAL_ATTR), dataFactory.createWsmlString(goal));
			i.addAttributeValue(wsmoFactory.createIRI(HASSEESIONID_ATTR), dataFactory.createWsmlString("ID22332222"));
			i.addAttributeValue(wsmoFactory.createIRI(HASTIMESTAMP_ATTR), dataFactory.createWsmlDateTime(Calendar.getInstance()));
			i.addAttributeValue(wsmoFactory.createIRI(HASPROCESSINSTANCEID_ATTR), dataFactory.createWsmlString("ID2346344"));
			i.addAttributeValue(wsmoFactory.createIRI(GENERATEDBY_ATTR), dataFactory.createWsmlString("WSMX"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}
	
	public static Instance createEndAchieveGoalEvent(String goal, String hasOutputData) {
		Instance i = wsmoFactory.createInstance(wsmoFactory.createIRI("http://www.wsmo.org/seeEvents#instance"+Helper.getRandomLong()));
		try {
			i.addConcept(getConcept(getSeeEventsOntology(),ENDACHIEVEGOALEVENT));
			i.addAttributeValue(wsmoFactory.createIRI(HASOUTPUTDATA_ATTR), dataFactory.createWsmlString(hasOutputData));
			i.addAttributeValue(wsmoFactory.createIRI(HASGOAL_ATTR), dataFactory.createWsmlString(goal));
			i.addAttributeValue(wsmoFactory.createIRI(HASSEESIONID_ATTR), dataFactory.createWsmlString("ID22332222"));
			i.addAttributeValue(wsmoFactory.createIRI(HASTIMESTAMP_ATTR), dataFactory.createWsmlDateTime(Calendar.getInstance()));
			i.addAttributeValue(wsmoFactory.createIRI(HASPROCESSINSTANCEID_ATTR), dataFactory.createWsmlString("ID2346344"));
			i.addAttributeValue(wsmoFactory.createIRI(GENERATEDBY_ATTR), dataFactory.createWsmlString("WSMX"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}
	
	public static Instance createStartInvokeWSEvent(String webService, String hasInputData) {
		Instance i = wsmoFactory.createInstance(wsmoFactory.createIRI("http://www.wsmo.org/seeEvents#instance"+Helper.getRandomLong()));
		try {
			i.addConcept(getConcept(getSeeEventsOntology(),STARTINVOKEWEBSERVICEEVENT));
			i.addAttributeValue(wsmoFactory.createIRI(HASINPUTDATA_ATTR), dataFactory.createWsmlString(hasInputData));
			i.addAttributeValue(wsmoFactory.createIRI(HASWEBSERVICE_ATTR), dataFactory.createWsmlString(webService));
			i.addAttributeValue(wsmoFactory.createIRI(HASSEESIONID_ATTR), dataFactory.createWsmlString("ID22332222"));
			i.addAttributeValue(wsmoFactory.createIRI(HASTIMESTAMP_ATTR), dataFactory.createWsmlDateTime(Calendar.getInstance()));
			i.addAttributeValue(wsmoFactory.createIRI(HASPROCESSINSTANCEID_ATTR), dataFactory.createWsmlString("ID2346344"));
			i.addAttributeValue(wsmoFactory.createIRI(GENERATEDBY_ATTR), dataFactory.createWsmlString("WSMX"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}
	
	public static Instance createEndInvokeWSEvent(String webService, String hasOutputData) {
		Instance i = wsmoFactory.createInstance(wsmoFactory.createIRI("http://www.wsmo.org/seeEvents#instance"+Helper.getRandomLong()));
		try {
			i.addConcept(getConcept(getSeeEventsOntology(),ENDINVOKEWEBSERVICEEVENT));
			i.addAttributeValue(wsmoFactory.createIRI(HASOUTPUTDATA_ATTR), dataFactory.createWsmlString(hasOutputData));
			i.addAttributeValue(wsmoFactory.createIRI(HASWEBSERVICE_ATTR), dataFactory.createWsmlString(webService));
			i.addAttributeValue(wsmoFactory.createIRI(HASSEESIONID_ATTR), dataFactory.createWsmlString("ID22332222"));
			i.addAttributeValue(wsmoFactory.createIRI(HASTIMESTAMP_ATTR), dataFactory.createWsmlDateTime(Calendar.getInstance()));
			i.addAttributeValue(wsmoFactory.createIRI(HASPROCESSINSTANCEID_ATTR), dataFactory.createWsmlString("ID2346344"));
			i.addAttributeValue(wsmoFactory.createIRI(GENERATEDBY_ATTR), dataFactory.createWsmlString("WSMX"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}
}
 