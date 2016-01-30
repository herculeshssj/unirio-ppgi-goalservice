package org.deri.wsmx.discovery.caching.matchmaking;



import org.deri.wsmx.discovery.caching.matchmaking.VampireInvokerStub.*;

public class ClientTest {
	
	public ClientTest() {
		
	}
	
	private String generatePO(){
	    String res = "";
	    
        res += "%loading ontologies \n";
        res += "include('/home/michael/SWSCshipment/ontologies/locationNoAxioms.ax').\n";
        res += "include('/home/michael/SWSCshipment/ontologies/shipment.ax').\n";
        res += "%loading goal template \n";
        res += "include('/home/michael/SWSCshipment/goals/goaltemplates/gt1.ax').\n";
        res += "%loading Web Service \n";
        res += "include('/home/michael/SWSCshipment/webservices/wsMuller.ax').\n";
        res += "%loading proof obligation \n";
        res += "include('/home/michael/SWSCshipment/matchmaking/usabilitySubsume.ax').\n";
        res += "\n";

	    return res;  
	}


    public static void main(String[] args) throws Exception {

    	VampireInvokerStub myInvoker = new VampireInvokerStub();
		
    	ClientTest theTest = new ClientTest(); 
        
    	//Create the request
    	VampireInvokerStub.Check request = new VampireInvokerStub.Check(); 
    	request.setPoContent(theTest.generatePO()); 
    	System.out.println(theTest.generatePO());

        //Invoke the service
        long startTime;
        long stopTime;

        startTime = System.currentTimeMillis();
        stopTime = startTime;

    	CheckResponse result = myInvoker.check(request);
    	
        stopTime = System.currentTimeMillis();

        long duration = (stopTime - startTime); // in ms.

            	

    	System.out.println("Response : " + result.get_return() + " [ "+(result.get_return()? "match" : "no match")+" in ("+duration+" ms incl. WS invocation) ]");
    }

}
