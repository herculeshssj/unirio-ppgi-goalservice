/**
 * Web Service implementation class
 * - invokes VAMPIRE for matchmaking
 * - returns matchmaking result
 * 
 * @author Michael Stollberg 
 * @version $Revision: 1.2 $ $Date: 2007-03-29 15:25:53 $
 */
package org.deri.wsmx.discovery.caching.matchmaking;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VampireInvoker {

	
    private final String PATH_TO_PROVER = "/home/michael/bin/vampire";

    private final String PROVER_OPTIONS = "--mode casc-19 --proof off";
    private final int PROVER_TIMEOUT = 5; // in seconds. 
    
    private final String path = "/home/michael/SWSCshipment/"; 

    private String proofObligationFile; 
    
    private String START_PROVER_COMMAND = PATH_TO_PROVER + " " +  PROVER_OPTIONS + " ";
    
    private final String SUCCESSFUL_PATTERN = "proved";

    private final String PO_DIR = path + "proofobligations/";
    
    /**
     * checks a TPTP proof obligation with VAMPIRE 
     * @param poContent
     * @return boolean (matchmaking result) 
     */
    public boolean check(String poContent) {

      boolean result = false;
      
      long startTime;
      long stopTime;

      
      File poFile = new File(PO_DIR + "poTest.p"); 

      try {
    	  	FileWriter fw = new FileWriter(poFile);
		    fw.write(poContent);
		    fw.close();

	  } catch (IOException io){
//	    	System.out.println("cannot write poFile");
		  io.printStackTrace();
	  }


    
      proofObligationFile = poFile.getAbsolutePath(); 
      
      if (proofObligationFile == null) {   	  
//    	System.out.println("proofObligationFile == null");
      	return false;
      }
      
      Runtime runt = java.lang.Runtime.getRuntime();
      StringBuffer proverOutputBuffer = new StringBuffer();
      Process newProver; 
      
            try {
	            startTime = System.currentTimeMillis();
				newProver = runt.exec(START_PROVER_COMMAND + " " + proofObligationFile);

				stopTime = startTime;
	        
			try {
              
              InputStream is = newProver.getInputStream();
              InputStreamReader sr = new java.io.InputStreamReader(is);
              
              boolean stop = false;
              
              while (!stop) {
                  int c;
					c = sr.read();
                  if (c != -1) {
                      proverOutputBuffer.append((char)c);
                  } else {
                      stop = true;
                  }
              }
              
              stopTime = System.currentTimeMillis();

     		 System.err.println("PROVER CALL OUTPUT: " + proverOutputBuffer.toString());
             result = (proverOutputBuffer.toString().toLowerCase().indexOf(this.SUCCESSFUL_PATTERN) != -1);

              
			} catch (IOException e) {
				e.printStackTrace();
				result = false;
			}
              
            long duration = (stopTime - startTime); // in ms.
              
		 System.err.println("PROVER CALL OUTPUT: " + proverOutputBuffer.toString());
              result = (proverOutputBuffer.toString().toLowerCase().indexOf(this.SUCCESSFUL_PATTERN) != -1);
		 
           
          
         System.err.println(" -- Done Checking PO: " + " [ "+(result? "match" : "no match")+" in ("+duration+" ms) ]");
      
			} catch (IOException e1) {
				e1.printStackTrace();
			}

	  poFile.delete(); 
			
      return result;
      
  }

}
      