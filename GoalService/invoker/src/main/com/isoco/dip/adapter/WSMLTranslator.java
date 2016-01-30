package com.isoco.dip.adapter;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;


/**
 * @author Darek Kleczek
 *
 */
public class WSMLTranslator {

    private WsmoFactory factory;

    private LogicalExpressionFactory leFactory;

    private DataFactory dataFactory;

    private Parser parser;
    
	protected static Logger logger = Logger.getLogger(WSMLTranslator.class);
    
    public Instance parse(String fileName) throws InvalidModelException {
        
        try {
            // use default implementation for factory            
        	HashMap<String, Object> props = new HashMap<String, Object>();
            parser = Factory.createParser(props);
       	    
            InputStream buffer = getClass().getClassLoader().getResourceAsStream(fileName);
                    	
            TopEntity[] entities = parser.parse(new BufferedReader(new InputStreamReader(buffer)));
            Instance instance = null;
            
            logger.debug("Number of recognized top entities: " +entities.length);
            
            if (entities.length != 1) {
                throw new RuntimeException("The number of parsed entities is not 1!");
            }
            
            /*for(int i=0; i<entities.length;i++){*/
            	Set instances = ((Ontology) (entities[0])).listInstances();
            	Iterator iter = instances.iterator();
            	if (iter.hasNext()){
            		logger.debug("Recognized instance URI: ");
            		instance = (Instance) iter.next();
            		logger.debug(instance.getIdentifier());
            		
            /*	} */
            }
            
            
            return instance;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        catch (ParserException ex) {
            logger.error("Invalid WSML token encountered at line " + ex.getErrorLine()
                    + " position " + ex.getErrorPos());
            return null;
        }
    }
	
	
	
	
}
