/**
 * load and store management for SDC elements on local file system  
 * - load and store of SDC Graph as an ontology  
 * - loaders for WSML goal and Web service descriptions
 * 
 * <pre>
 * Committed by $Author: mstollberg $
 * </pre>
 * 
 * @author Michael Stollberg
 *
 * @version $Revision: 1.6 $ $Date: 2007-10-11 19:42:08 $
 */ 

package org.deri.wsmx.discovery.caching;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.common.ClearTopEntity;
import org.omwg.ontology.Ontology;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.Factory;
import org.wsmo.mediator.Mediator;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;
import org.wsmo.wsml.Serializer;

public class SDCResourceManager {
	
 	protected static Logger logger;

	private String resourcesDirectory; 
	private String ontologyDirectory; 
	private String gtDirectory; 
	private String wsDirectory; 
	private String SDCGraphLocation; 
	
	private Parser parser; 
	private Serializer serializer; 
	
	/**
	 * constructor with resoruce directories
	 * for domain ontologies, goals, Web services 
	 */
	public SDCResourceManager(){
		
		// local directory for "original" SWSC shipment sceanrio modelling 
		resourcesDirectory = "D:/DERI/phd-thesis/repository/stolle/usecases/SWS-challenge/own-modelling/"; 
		// local directory for extended SWSC shipment sceanrio modelling 
//		resourcesDirectory = "D:/DERI/phd-thesis/repository/stolle/usecases/SWS-challenge/evaluation/runtimeDiscoverySDCvsGTonly/SWSC-shipmentscenario-extended-modelling/"; 
		ontologyDirectory = resourcesDirectory + "WSML/ontologies/";
		gtDirectory = resourcesDirectory + "WSML/goals/goaltemplates/";
		wsDirectory = resourcesDirectory + "WSML/webservices/";
		
		SDCGraphLocation = "discovery\\src\\main\\org\\deri\\wsmx\\discovery\\caching\\";
		
    	parser = Factory.createParser(null); 
		serializer = Factory.createSerializer(null);
		logger = Logger.getLogger(SDCGraphManager.class);
	}
	
	/**
	 * loads the SDC Graph Ontology Schema 
	 * @return Ontology: the object for the working SDC graph 
	 */
	public Ontology loadSDCGraphSchema() {
 		
		logger.info("loading pre-defined SDC Graph Schema");

    	String localFile = SDCGraphLocation + "SDCgraphSchema.wsml";
    	File file = new File(localFile);
    	
        Ontology sdcGraph = null;
		TopEntity[] te;
		try {
			te = parser.parse(new FileReader(file));
			sdcGraph = (Ontology)te[0]; 
			logger.info("load successful: " + sdcGraph.getIdentifier());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sdcGraph;
	}

	/**
	 * stores the SDC graph as a WSML ontology on the local machine 
	 * @param directory: where to store 
	 * @param theFile
	 * @param theSDCgraph
	 */
	public void storeSDCGraph(String directory, String theFile, Ontology theSDCgraph) {
		
		logger.info("serializing the current SDC Graph");

        StringWriter writer = new StringWriter();
        try {
			serializer.serialize(new TopEntity[]{theSDCgraph}, writer);
			logger.info("serialization successful");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("storing as a local file: " + directory + theFile);

		File theFileToDelete  = new File(theFile); 
		if (theFileToDelete.canRead()) 
			theFileToDelete.delete(); 
		
        try {
			writeStringBufferToFile(writer.getBuffer(),directory + theFile);
			logger.info("storing successful");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			ClearTopEntity.clearTopEntity(theSDCgraph);
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


	}
	
	public void storeMediator(String directory, String theFile, Mediator theMediator) {
		
		logger.info("serializing mediator: " + theMediator.getIdentifier());

        StringWriter writer = new StringWriter();
        try {
			serializer.serialize(new TopEntity[]{theMediator}, writer);
			logger.info("serialization successful");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("storing as a local file: " + directory + theFile);

		File theFileToDelete  = new File(theFile); 
		if (theFileToDelete.canRead()) 
			theFileToDelete.delete(); 
		
        try {
			writeStringBufferToFile(writer.getBuffer(),directory + theFile);
			logger.info("storing successful");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			ClearTopEntity.clearTopEntity(theMediator);
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


	}
	
	
	/*
	 * copied from the Web 
	 */
	
    static final int STRING_LENGTH = 1000 * 1000 * 10;
    static final int ROUNDS = 1;
    
    private static void writeStringBufferToFile(StringBuffer buf, String filename)
	throws IOException
    {
        long start = System.currentTimeMillis();
        for (int n = 0; n < ROUNDS; n++) {
	    FileOutputStream stream = new FileOutputStream(filename);
	    Writer writer = new OutputStreamWriter(stream);
	    char chunk[] = new char[8192];
	    for (int m = 0, pos = 0; ; ) {
		if (m == chunk.length || pos == buf.length()) {
		    writer.write(chunk, 0, m);
		    if (pos == buf.length())
			break;
		    m = 0;
		}
		chunk[m++] = buf.charAt(pos++);
	    }
	    writer.close();
	    stream.close();
        }
        long end = System.currentTimeMillis();
 
        System.out.println("chunk time " + (end - start) + " ms");
    }

	/**
	 * loads an existing SDC Graph knowledge base 
	 * @param directory
	 * @param filename
	 * @return the SDC Graph (ontology) 
	 */
    public Ontology loadSDCGraph(String directory, String filename) {
 		
		logger.info("loading an SDC Graph from " + directory + filename); 
		File sdcGraphFile = new File(directory + filename);
		TopEntity[] entities;
		Ontology theOntology = null;
		try {
			entities = parser.parse(new FileReader(sdcGraphFile));
			theOntology  = (Ontology)entities[0];
			logger.info("loading successful");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return theOntology;

	}
	
    /**
     * loads an ontology from the local directory 
     * @param filename
     * @return Ontology 
     */
    public Ontology loadOntology(String filename){
		logger.info("loading ontology: " + filename); 
		File gtFile = new File(ontologyDirectory + filename);
		TopEntity[] entities;
		Ontology theOntology = null;
		try {
			entities = parser.parse(new FileReader(gtFile));
			theOntology  = (Ontology)entities[0];
			logger.info("loading successful");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return theOntology;
		
	}

	/**
	 * loads a goal template from the local directory
	 * @param filename
	 * @return Goal: the loaded goal template 
	 */
    public Goal loadGoalTemplate(String filename){
		logger.info("loading goal template: " + filename); 
		File gtFile = new File(gtDirectory + filename);
		TopEntity[] entities;
		Goal theGoal = null;
		try {
			entities = parser.parse(new FileReader(gtFile));
			theGoal = (Goal)entities[0];
			logger.info("loading successful");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return theGoal;
		
	}

	/**
	 * loads a Web service description from the local directory
	 * @param filename
	 * @return WebService 
	 */
    public WebService loadWS(String filename){
		logger.info("loading Web service description: " + filename); 
		File gtFile = new File(wsDirectory + filename);
		TopEntity[] entities;
		WebService theWS = null;
		try {
			entities = parser.parse(new FileReader(gtFile));
			theWS = (WebService)entities[0];
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return theWS;
		
	}

    public WebService loadWSfromDirectory(String directory, String filename){
		logger.info("loading Web service description: " + filename + " from " + directory); 
		File wsFile = new File(directory + filename);
		TopEntity[] entities;
		WebService theWS = null;
		try {
			entities = parser.parse(new FileReader(wsFile));
			theWS = (WebService)entities[0];
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return theWS;
		
	}

    
	public void storeWebService(String directory, String theFile, WebService theWS) {
		
		logger.info("serializing WSMO Web service description: " + theWS.getIdentifier());

        StringWriter writer = new StringWriter();
        try {
			serializer.serialize(new TopEntity[]{theWS}, writer);
			logger.info("serialization successful");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("storing as a local file: " + directory + theFile);

		File theFileToDelete  = new File(theFile); 
		if (theFileToDelete.canRead()) 
			theFileToDelete.delete(); 
		
        try {
			writeStringBufferToFile(writer.getBuffer(),directory + theFile);
			logger.info("storing successful");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			ClearTopEntity.clearTopEntity(theWS);
		} catch (SynchronisationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


	}
    
    
    

}
