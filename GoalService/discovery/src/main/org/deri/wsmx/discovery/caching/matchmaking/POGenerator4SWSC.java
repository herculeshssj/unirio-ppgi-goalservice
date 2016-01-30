/**
 * defines methods for generating TPTP proof obligations 
 * for SWSC Shipment Use Case with SDC  
 * 
 * @author Michael Stollberg 
 * @version $Revision: 1.10 $ $Date: 2007-10-11 14:37:54 $
 */

package org.deri.wsmx.discovery.caching.matchmaking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmx.discovery.caching.SDCGraphCreatorHelper;
import org.deri.wsmx.discovery.caching.SDCGraphManager;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;


public class POGenerator4SWSC implements POGenerator {
	
 	protected static Logger logger;

	private WsmoFactory wsmoFactory;
	private SDCGraphManager graphManager; 
	private SDCGraphCreatorHelper helper; 
	
	public POGenerator4SWSC(){

		wsmoFactory = Factory.createWsmoFactory(new HashMap());
		
		logger = Logger.getLogger(POGenerator4SWSC.class);
		
		graphManager = new SDCGraphManager(); 
		helper = new SDCGraphCreatorHelper(); 
	}
	
	/**
	 * gets TPTP filename of a goal template 
	 * @param goaltemplate
	 * @return String (filename)
	 */
	private String getGoalTemplateTPTPfileName(Goal goaltemplate){
		
		String tptpFileName = null;
		
		if (goaltemplate.getIdentifier().toString().contains("gtRoot") ) {
			tptpFileName = "gtRoot"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2world") ) {
			tptpFileName = "gtUS2world"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2AF") ) {
			tptpFileName = "gtUS2AF"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2AFlight") ) {
			tptpFileName = "gtUS2AFlight"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2AS") ) {
			tptpFileName = "gtUS2AS"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2ASlight") ) {
			tptpFileName = "gtUS2ASlight"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2EU") ) {
			tptpFileName = "gtUS2EU"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2EUlight") ) {
			tptpFileName = "gtUS2EUlight"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2NA") ) {
			tptpFileName = "gtUS2NA"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2OC") ) {
			tptpFileName = "gtUS2OC"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2OClight") ) {
			tptpFileName = "gtUS2OClight"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2SA") ) {
			tptpFileName = "gtUS2SA"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtUS2SAlight") ) {
			tptpFileName = "gtUS2SAlight"; 
		}
		if (goaltemplate.getIdentifier().toString().contains("gtNA2NAlight") ) {
			tptpFileName = "gtNA2NAlight"; 
		}
		
//		logger.info("using TPTP file: " + tptpFileName); 
		
		return tptpFileName; 
	}

	/**
	 * gets TPTP filename for a Web service description 
	 * @param ws
	 * @return String (filename)
	 */
	private String getWSTPTPfileName(WebService ws){
		
		String tptpFileName = null;
		
		if (ws.getIdentifier().toString().contains("wsMuller") ) {
			tptpFileName = "wsMuller"; 
		}
		if (ws.getIdentifier().toString().contains("wsRacer") ) {
			tptpFileName = "wsRacer"; 
		}
		if (ws.getIdentifier().toString().contains("wsRunner") ) {
			tptpFileName = "wsRunner"; 
		}
		if (ws.getIdentifier().toString().contains("wsWalker") ) {
			tptpFileName = "wsWalker"; 
		}
		if (ws.getIdentifier().toString().contains("wsWeasel") ) {
			tptpFileName = "wsWeasel"; 
		}
		if (ws.getIdentifier().toString().contains("wsOther") ) {
			tptpFileName = "wsOther"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment1") ) {
			tptpFileName = "wsShipment1"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment2") ) {
			tptpFileName = "wsShipment2"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment3") ) {
			tptpFileName = "wsShipment3"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment4") ) {
			tptpFileName = "wsShipment4"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment5") ) {
			tptpFileName = "wsShipment5"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment6") ) {
			tptpFileName = "wsShipment6"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment7") ) {
			tptpFileName = "wsShipment7"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment8") ) {
			tptpFileName = "wsShipment8"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment9") ) {
			tptpFileName = "wsShipment9"; 
		}
		if (ws.getIdentifier().toString().contains("wsShipment10") ) {
			tptpFileName = "wsShipment10"; 
		}
		
		return tptpFileName; 
	}
	



	public String generatePOsimilarity(Goal source, Goal target, String degree, Ontology sdcGraph) {
		
	    String res = "";
	    
        res += "%loading ontologies \n";
        res += "include('/home/michael/SWSCshipment/ontologies/locationNoAxioms.ax').\n";
        res += "include('/home/michael/SWSCshipment/ontologies/shipment.ax').\n";
        
        if ( graphManager.isIntersectionGT(source, sdcGraph) & graphManager.isIntersectionGT(target, sdcGraph) ) {
//        logger.info(" case: 2 iGTS"); 
        
        List<Goal> sourceParents = helper.getParentsFromGGM(sdcGraph, source);
        Goal sourceParent1 = sourceParents.get(0);
		String TPTPsourceParent1 = getGoalTemplateTPTPfileName(sourceParent1);  
        Goal sourceParent2 = sourceParents.get(1); 
		String TPTPsourceParent2 = getGoalTemplateTPTPfileName(sourceParent2);  
        
        List<Goal> targetParents = helper.getParentsFromGGM(sdcGraph, target);
        Goal targetParent1 = targetParents.get(0);
        String TPTPtargetParent1 = getGoalTemplateTPTPfileName(targetParent1);
        Goal targetParent2 = targetParents.get(1);
        String TPTPtargetParent2 = getGoalTemplateTPTPfileName(targetParent2);

        res += "%loading source goal templates \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPsourceParent1 + ".ax').\n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPsourceParent2 + "3.ax').\n";
        res += "%loading target goal templates \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPtargetParent1 + "4Sim.ax').\n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPtargetParent2 + "4.ax').\n";

        res += "%loading proof obligation \n";
        	if (degree == "exact") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similarityExact2iGT.ax').\n";
        	}
        	if (degree == "plugin") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similarityPlugin2iGT.ax').\n";
        	}
        	if (degree == "subsume") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similaritySubsume2iGT.ax').\n";
        	}
        	if (degree == "intersect") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similarityIntersect2iGT.ax').\n";
        	}
        
        	
        } else if ( graphManager.isIntersectionGT(source, sdcGraph) ) {
//            logger.info(" case: source is a iGT"); 
        	
        List<Goal> sourceParents = helper.getParentsFromGGM(sdcGraph, source);
        Goal sourceParent1 = sourceParents.get(0); 
    	String TPTPsourceParent1 = getGoalTemplateTPTPfileName(sourceParent1);  
        Goal sourceParent2 = sourceParents.get(1); 
    	String TPTPsourceParent2 = getGoalTemplateTPTPfileName(sourceParent2);
    	
		String TPTPtarget = getGoalTemplateTPTPfileName(target);

        res += "%loading source goal templates \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPsourceParent1 + ".ax').\n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPsourceParent2 + "3.ax').\n";

        res += "%loading target goal template \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPtarget + "4Sim.ax').\n";
        
        res += "%loading proof obligation \n";
        	if (degree == "exact") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similarityExactSourceiGT.ax').\n";
        	}
        	if (degree == "plugin") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similarityPluginSourceiGT.ax').\n";
        	}
        	if (degree == "subsume") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similaritySubsumeSourceiGT.ax').\n";
        	}
        	if (degree == "intersect") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similarityIntersectSourceiGT.ax').\n";
        	}

        	
        } else if ( graphManager.isIntersectionGT(target, sdcGraph) ) {
//            logger.info(" case: target is a iGT"); 

  		String TPTPsource = getGoalTemplateTPTPfileName(source);

        List<Goal> targetParents = helper.getParentsFromGGM(sdcGraph, target);
        Goal targetParent1 = targetParents.get(0); 
        String TPTPtargetParent1 = getGoalTemplateTPTPfileName(targetParent1);
        Goal targetParent2 = targetParents.get(1); 
        String TPTPtargetParent2 = getGoalTemplateTPTPfileName(targetParent2);

		
        res += "%loading source goal template \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPsource + ".ax').\n";
        res += "%loading target goal templates \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPtargetParent1 + "4Sim.ax').\n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPtargetParent2 + "4.ax').\n";

        res += "%loading proof obligation \n";
    		if (degree == "exact") {
    			res += "include('/home/michael/SWSCshipment/matchmaking/similarityExactTargetiGT.ax').\n";
    		}
    		if (degree == "plugin") {
    			res += "include('/home/michael/SWSCshipment/matchmaking/similarityPluginTargetiGT.ax').\n";
    		}
    		if (degree == "subsume") {
    			res += "include('/home/michael/SWSCshipment/matchmaking/similaritySubsumeTargetiGT.ax').\n";
    		}
    		if (degree == "intersect") {
    			res += "include('/home/michael/SWSCshipment/matchmaking/similarityIntersectTargetiGT.ax').\n";
    		}


        } else {
        	  		
		String TPTPgt1 = getGoalTemplateTPTPfileName(source);  
		String TPTPgt2 = getGoalTemplateTPTPfileName(target);

        res += "%loading goal template 1 \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPgt1 + ".ax').\n";
        res += "%loading goal template 2 \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPgt2 + "4Sim.ax').\n";
        
        res += "%loading proof obligation \n";
        	if (degree == "exact") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similarityExact.ax').\n";
        	}
        	if (degree == "plugin") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similarityPlugin.ax').\n";
        	}
        	if (degree == "subsume") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similaritySubsume.ax').\n";
        	}
        	if (degree == "intersect") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/similarityIntersect.ax').\n";
        	}
        
        }
        
        res += "\n";
        
//        logger.info("generated proof obligation: \n " + res); 

		return res;
	}

	public String generatePOusability(Goal gt, WebService ws, String degree, Ontology sdcGraph) {
	
		String tptpFileNamews = getWSTPTPfileName(ws); 

		String res = "";

        res += "%loading ontologies \n";
        res += "include('/home/michael/SWSCshipment/ontologies/locationNoAxioms.ax').\n";
        res += "include('/home/michael/SWSCshipment/ontologies/shipment.ax').\n";

        res += "%loading Web service \n";
        res += "include('/home/michael/SWSCshipment/webservices/" + tptpFileNamews + ".ax').\n";
		
	    
	    if ( graphManager.isIntersectionGT(gt, sdcGraph) ) {
	    	
	    List<Goal> theParents = helper.getParentsFromGGM(sdcGraph, gt);
	    Goal parent1 = theParents.get(0); 
	    String TPTPparent1 = getGoalTemplateTPTPfileName(parent1);  
	    Goal parent2 = theParents.get(1); 
	    String TPTPparent2 = getGoalTemplateTPTPfileName(parent2);  

        res += "%loading parent goal templates \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPparent1 + ".ax').\n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPparent2 + "3.ax').\n";

        res += "%loading proof obligation \n";
        	if (degree == "plugin") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/usabilityPlugin4iGT.ax').\n";
        	}
        	if (degree == "subsume") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/usabilitySubsume4iGT.ax').\n";
        	}
        	if (degree == "intersect") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/usabilityIntersect4iGT.ax').\n";
        	}

	    	
	    } else { 
		
		String TPTPgt = getGoalTemplateTPTPfileName(gt);  

        res += "%loading goal template \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPgt + ".ax').\n";
        
        res += "%loading proof obligation \n";
        	if (degree == "plugin") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/usabilityPlugin.ax').\n";
        	}
        	if (degree == "subsume") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/usabilitySubsume.ax').\n";
        	}
        	if (degree == "intersect") {
        		res += "include('/home/michael/SWSCshipment/matchmaking/usabilityIntersect.ax').\n";
        	}
        
	    }
        
        res += "\n";

		return res;
	}

	public String generatePOInstantiatonCheck(Instance goalInstance, Goal goalTemplate, Ontology sdcGraph) {

		String res = "";
		
	    // getting input values from goal instance 
	    
//		Set<String> theInputs = new HashSet<String>();
//		Set<Value> temp = goalInstance.listAttributeValues(wsmoFactory.createIRI(
//				sdcGraph.getDefaultNamespace(), "inputs"));
//		for (Value v: temp)
//			theInputs.add(v.toString());
		
		Set<Value> theInputs = goalInstance.listAttributeValues(
				wsmoFactory.createIRI(goalInstance.getOntology().getDefaultNamespace(), "inputs"));
		
		Iterator myIterator = theInputs.iterator(); 
		
		String SenderLoc = myIterator.next().toString(); 
		String ReceiverLoc = myIterator.next().toString(); 
		String theWeightClass = myIterator.next().toString(); 

		
        res += "%loading ontologies \n";
        res += "include('/home/michael/SWSCshipment/ontologies/locationNoAxioms.ax').\n";
        res += "include('/home/michael/SWSCshipment/ontologies/shipment.ax').\n";
                
     			    
		if ( graphManager.isIntersectionGT(goalTemplate, sdcGraph) ) {
			
		List<Goal> theParents = helper.getParentsFromGGM(sdcGraph, goalTemplate); 
		Goal parent1 = theParents.get(0); 
		String TPTPparent1 = getGoalTemplateTPTPfileName(parent1);  
		Goal parent2 = theParents.get(1); 
		String TPTPparent2 = getGoalTemplateTPTPfileName(parent2);  

	    res += "%loading parent goal templates \n";
	    res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPparent1 + ".ax').\n";
	    res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPparent2 + "3.ax').\n";
	    
        res += "input_formula(po, conjecture,( \n"; 
        res += "? [I3,O] : ( gt(" + SenderLoc + "," + ReceiverLoc + ",I3," + theWeightClass + ",O) & gt3(" + 
        SenderLoc + "," + ReceiverLoc + ",I3," + theWeightClass + ",O) ) \n"; 
        res += ")). \n";


		} else {
			
		String TPTPgt = getGoalTemplateTPTPfileName(goalTemplate);

		res += "%loading goal template \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPgt + ".ax').\n";
        
        res += "input_formula(po, conjecture,( \n"; 
        res += "? [I3,O] : ( gt(" + SenderLoc + "," + ReceiverLoc + ",I3," + theWeightClass + ",O) ) \n"; 
        res += ")). \n";

		}

        res += "\n";

		return res;
	}

	public String generatePOgilevelSubsume(Instance goalinstance, WebService ws) {

		String res = "";
		
		String tptpFileNamews = getWSTPTPfileName(ws); 
		
	    // getting input values from goal instance 
	    
//		Set<String> theInputs = new HashSet<String>();
//		Set<Value> temp = goalinstance.listAttributeValues(wsmoFactory.createIRI(
//				goalinstance.getOntology().getDefaultNamespace(), "inputs"));
//		for (Value v: temp)
//			theInputs.add(v.toString());
		
		Set<Value> theInputs = goalinstance.listAttributeValues(
				wsmoFactory.createIRI(goalinstance.getOntology().getDefaultNamespace(), "inputs"));
		
		Iterator myIterator = theInputs.iterator(); 
		
		String SenderLoc = myIterator.next().toString(); 
		String ReceiverLoc = myIterator.next().toString(); 
		String theWeightClass = myIterator.next().toString(); 


        res += "%loading ontologies \n";
        res += "include('/home/michael/SWSCshipment/ontologies/locationNoAxioms.ax').\n";
        res += "include('/home/michael/SWSCshipment/ontologies/shipment.ax').\n";
        res += "%loading Web service \n";
        res += "include('/home/michael/SWSCshipment/webservices/" + tptpFileNamews + ".ax').\n";
        
        res += "% proof obligation \n";
        
        res += "input_formula(po, conjecture,( \n"; 
        res += "? [I3,O] : ( ws(" + SenderLoc + "," + ReceiverLoc + ",I3," + theWeightClass + ",O) ) \n"; 
        res += ")). \n";

        
        res += "\n";

		return res;
	}

	public String generatePOgilevelIntersect(Instance goalInstance, Goal goalTemplate, WebService ws, Ontology sdcGraph) {

		String res = "";
		
//		Set<String> theInputs = new HashSet<String>();
//		Set<Value> temp = goalInstance.listAttributeValues(wsmoFactory.createIRI(
//				goalInstance.getOntology().getDefaultNamespace(), "inputs"));
//		for (Value v: temp)
//			theInputs.add(v.toString());

		Set<Value> theInputs = goalInstance.listAttributeValues(
				wsmoFactory.createIRI(goalInstance.getOntology().getDefaultNamespace(), "inputs"));
		
		Iterator myIterator = theInputs.iterator(); 
		
		String SenderLoc = myIterator.next().toString(); 
		String ReceiverLoc = myIterator.next().toString(); 
		String theWeightClass = myIterator.next().toString(); 


		String tptpFileNamews = getWSTPTPfileName(ws); 
		
        res += "%loading ontologies \n";
        res += "include('/home/michael/SWSCshipment/ontologies/locationNoAxioms.ax').\n";
        res += "include('/home/michael/SWSCshipment/ontologies/shipment.ax').\n";
        res += "%loading Web service \n";
        res += "include('/home/michael/SWSCshipment/webservices/" + tptpFileNamews + ".ax').\n";

        if ( graphManager.isIntersectionGT(goalTemplate, sdcGraph) ) {

   		List<Goal> theParents = helper.getParentsFromGGM(sdcGraph, goalTemplate);
   		Goal parent1 = theParents.get(0); 
   		String TPTPparent1 = getGoalTemplateTPTPfileName(parent1);  
   		Goal parent2 = theParents.get(1); 
   		String TPTPparent2 = getGoalTemplateTPTPfileName(parent2);  

   	    res += "%loading parent goal templates \n";
   	    res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPparent1 + ".ax').\n";
   	    res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPparent2 + "3.ax').\n";
   	    
        res += "input_formula(po, conjecture,( \n";
        
        res += "? [I3,O] : ( gt(" + SenderLoc + "," + ReceiverLoc + ",I3," + theWeightClass + ",O) " +
        		"& gt3(" + SenderLoc + "," + ReceiverLoc + ",I3," + theWeightClass + ",O)" +
        		"& ws(" + SenderLoc + "," + ReceiverLoc + ",I3," + theWeightClass + ",O) ) \n"; 
        res += ")). \n";


        } else {


   		String TPTPgt = getGoalTemplateTPTPfileName(goalTemplate);  

        res += "%loading goal template \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/" + TPTPgt + ".ax').\n";
        
        res += "% proof obligation \n";
        
        res += "input_formula(po, conjecture,( \n"; 
        res += "? [I3,O] : ( gt(" + SenderLoc + "," + ReceiverLoc + ",I3," + theWeightClass + ",O) & ws(" 
        			+ SenderLoc + "," + ReceiverLoc + ",I3," + theWeightClass + ",O) ) \n"; 
        res += ")). \n";
        
        }

        
        res += "\n";

		return res;
	}
	
	

}
