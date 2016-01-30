/**
 * this provides methods for all matchmaking tasks necessary within SDC 
 * uses: 
 * - POGenerator4SWSC for generating the proof obligations 
 * - VampireInvokerStub to invoke the WS to use VAMPIRE for matchmaking 
 * 
 * @author Michael Stollberg 
 * @version $Revision: 1.5 $ $Date: 2007-10-11 19:42:31 $
 */

package org.deri.wsmx.discovery.caching.matchmaking;

import java.rmi.RemoteException;

import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.deri.wsmx.discovery.caching.SDCGraphManager;
import org.deri.wsmx.discovery.caching.SDCResourceManager;
import org.deri.wsmx.discovery.caching.matchmaking.VampireInvokerStub.*;

/*
 * needs to implement the client functionality 
 */

public class Matchmaking4SWSC implements Matchmaking {
	
 	protected static Logger logger;

	private POGenerator4SWSC theGenerator; 
	
	public Matchmaking4SWSC(){
		theGenerator = new POGenerator4SWSC(); 
		logger = Logger.getLogger(Matchmaking4SWSC.class);
		logger.setLevel(Level.INFO);
	}
	
	/**
	 * invokes VAMPIRE for matchmaking, via Web Service 
	 * @see VampireInvoker 
	 * @param po
	 * @return boolean (matchmaking result) 
	 */
	private boolean invokeMatchmaker(String po) {
		boolean result = false; 
		
		VampireInvokerStub theInvoker = null;
		
		
		try {
			theInvoker = new VampireInvokerStub();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	//Create the request
    	VampireInvokerStub.Check request = new VampireInvokerStub.Check(); 
    	request.setPoContent(po); 

        //Invoke the service
        long startTime;
        long stopTime;

        startTime = System.currentTimeMillis();
        stopTime = startTime;

    	CheckResponse response = null;
		try {
			response = theInvoker.check(request);
		} catch (RemoteException e) {
			logger.info("vampire timeout ... assume TRUE"); 
			boolean assumedResult = true;
			response = new CheckResponse(); 
			response.set_return(assumedResult); 
//			e.printStackTrace();
		}
    	
        stopTime = System.currentTimeMillis();

        long duration = (stopTime - startTime); // in ms.
        
    	logger.info("matchmaking result : " + response.get_return() + " [ "+(response.get_return()? "match" : "no match")+" in ("+duration+" ms incl. WS invocation) ]");

        result = response.get_return(); 

		return result; 
	}
	
	

	public boolean similarityExact(Goal source, Goal target, Ontology sdcGraph) {
		logger.info("checking for similarity degree EXACT");
		boolean result = false; 		
		String thePO = theGenerator.generatePOsimilarity(source, target, "exact", sdcGraph); 
		result = invokeMatchmaker(thePO);
		return result;
	}

	public boolean similarityPlugin(Goal source, Goal target, Ontology sdcGraph) {
		logger.info("checking for similarity degree PLUGIN");
		boolean result = false; 		
		String thePO = theGenerator.generatePOsimilarity(source, target, "plugin", sdcGraph); 
		result = invokeMatchmaker(thePO);
		return result;
	}

	public boolean similaritySubsume(Goal source, Goal target, Ontology sdcGraph) {
		logger.info("checking for similarity degree SUBSUME");
		boolean result = false; 		
		String thePO = theGenerator.generatePOsimilarity(source, target, "subsume", sdcGraph); 
		result = invokeMatchmaker(thePO);
		return result;
	}

	public boolean similarityIntersect(Goal source, Goal target, Ontology sdcGraph) {
		logger.info("checking for similarity degree INTERSECT");
		boolean result = false; 		
		String thePO = theGenerator.generatePOsimilarity(source, target, "intersect", sdcGraph); 
		result = invokeMatchmaker(thePO);
		return result;
	}


	public boolean usabilityPlugin(Goal goaltemplate, WebService webservice, Ontology sdcGraph) {
		logger.info("checking for usability degree PLUGIN");
		boolean result = false; 		
		String thePO = theGenerator.generatePOusability(goaltemplate, webservice, "plugin", sdcGraph);  
		result = invokeMatchmaker(thePO);
		return result;
	}

	public boolean usabilitySubsume(Goal goaltemplate, WebService webservice, Ontology sdcGraph) {
		logger.info("checking for usability degree SUBSUME");
		boolean result = false; 		
		String thePO = theGenerator.generatePOusability(goaltemplate, webservice, "subsume", sdcGraph);  
		result = invokeMatchmaker(thePO);
		return result;
	}

	public boolean usabilityIntersect(Goal goaltemplate, WebService webservice, Ontology sdcGraph) {
		logger.info("checking for usability degree INTERSECT");
		boolean result = false; 		
		String thePO = theGenerator.generatePOusability(goaltemplate, webservice, "intersect", sdcGraph);  
		result = invokeMatchmaker(thePO);
		return result;
	}

	public boolean instantiatonCheck(Instance goalInstance, Goal goaltemplate, Ontology sdcGraph) {
		logger.info("goal instantiation check");
		boolean result = false; 		
		String thePO = theGenerator.generatePOInstantiatonCheck(goalInstance, goaltemplate, sdcGraph);  
		result = invokeMatchmaker(thePO);
		return result;
	}
	

	public boolean giLevelUsabilitySubsume(Instance goalinstance, WebService ws) {
		logger.info("GI level Subsume Matchmaking");
		boolean result = false; 		
		String thePO = theGenerator.generatePOgilevelSubsume(goalinstance, ws);   
		result = invokeMatchmaker(thePO);
		return result;
	}

	public boolean giLevelUsabilityIntersect(Instance goalinstance, Goal goaltemplate, WebService ws, Ontology sdcGraph) {
		logger.info("GI level Intersect Matchmaking");
		boolean result = false; 		
		String thePO = theGenerator.generatePOgilevelIntersect(goalinstance, goaltemplate, ws, sdcGraph);
		result = invokeMatchmaker(thePO);
		return result;
	}

	/*
	 * for testing during development only 
	 */
//	public static void main(String[] args) throws RemoteException {
//		System.out.println("moin, this is the matchmaker4SWSC");
//		
//		Matchmaking4SWSC theMatchmaker = new Matchmaking4SWSC(); 
//		
//		SDCGraphManager sdcGraphManager = new SDCGraphManager();
//		SDCResourceManager theResoruceManager = new SDCResourceManager();
//
//    	Ontology theSDCGraph = theResoruceManager.loadSDCGraphSchema();
//    	Goal gt1 = theResoruceManager.loadGoalTemplate("gtRoot.wsml");	
//    	Goal gt2 = theResoruceManager.loadGoalTemplate("gtUS2world.wsml");
//    	
//    	System.out.println(theMatchmaker.theGenerator.generatePOsimilarity(gt1, gt2, "subume")); 
////    	does not work yet ... null pointer exception 
//    	boolean similarity1 = theMatchmaker.similarityExact(gt1, gt2);
//    	System.out.print("result for gt1, gt2, exact: "); 
//    	System.out.println(similarity1); 
////    	OK, now it works  
//    	
//    	boolean similarity2 = theMatchmaker.similarityPlugin(gt1, gt2);
//    	System.out.print("result for gt1, gt2, plugin: "); 
//    	System.out.println(similarity2); 
//    	
//    	boolean similarity3 = theMatchmaker.similaritySubsume(gt1, gt2);
//    	System.out.print("result for gt1, gt2, subsume: "); 
//    	System.out.println(similarity3); 
//
//    	boolean similarity4 = theMatchmaker.similarityIntersect(gt1, gt2);
//    	System.out.print("result for gt1, gt2, intersect: "); 
//    	System.out.println(similarity4); 
//    	
//    	WebService  ws1 = theResoruceManager.loadWS("wsMuller.wsml");
//    	WebService  ws2 = theResoruceManager.loadWS("wsRacer.wsml");
//    	WebService  ws3 = theResoruceManager.loadWS("wsRunner.wsml");
//    	WebService  ws4 = theResoruceManager.loadWS("wsWalker.wsml");
//    	WebService  ws5 = theResoruceManager.loadWS("wsWeasel.wsml");
//
//    	
//    	Goal gt3 = theResoruceManager.loadGoalTemplate("gtUS2AF.wsml");	
//    	System.out.println("loaded: " + gt3.getIdentifier().toString()); 
//    	
//    	String myPO = theMatchmaker.theGenerator.generatePOusability(gt3, ws1, "intersect");
//    	System.out.println(myPO); 

/* 
 * OK, the get TPTP file name stuff should work now     	
    	boolean wsFileName = false;  
    	wsFileName = ws1.getIdentifier().toString().contains("wsMuller");  
    	System.out.print("does WS filename detection work? ");
    	System.out.println(wsFileName); 
    	
    	System.out.println(ws1.getIdentifier());

		String tptpFileName = null; 

		if (ws1.getIdentifier().toString().contains("wsMuller") ) {
			tptpFileName = "wsMuller";
			System.out.println(tptpFileName); 
		}
		
		String res = "include('/home/michael/SWSCshipment/webservices/" + tptpFileName + ".ax').\n";
		System.out.println(res); 

    	
    	String aPO = theMatchmaker.theGenerator.generatePOusability(gt1, ws1, "subsume"); 
    	System.out.println("PO for gt1, wsMuller, subsume: \n " + aPO); 
    	
    	System.out.println(ws2.getIdentifier());

    	String aPO3 = theMatchmaker.theGenerator.generatePOusability(gt1, ws2, "subsume"); 
    	System.out.println("PO for gt1, wsMuller, subsume: \n " + aPO3); 

    	
    	String aPO2 = theMatchmaker.theGenerator.generatePOusability(gt2, ws3, "subsume"); 
    	System.out.println("PO for gt1, wsRacer, subsume: \n " + aPO2); 
*/    	

/*
 * OK, now it works (for whatever stupid reason), even with multiple invocations  
    	boolean usability1 = theMatchmaker.usabilityPlugin(gt1,ws1); 
    	System.out.print("result for gt1, wsMuller, plugin: "); 
    	System.out.println(usability1); 

    	boolean usability2 = theMatchmaker.usabilitySubsume(gt1,ws1); 
    	System.out.print("result for gt1, wsMuller, subsume: "); 
    	System.out.println(usability2); 


    	boolean usability3 = theMatchmaker.usabilityIntersect(gt1,ws1); 
    	System.out.print("result for gt1, wsMuller, intersect: "); 
    	System.out.println(usability3); 
 */
//	}


}
