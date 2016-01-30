package org.deri.wsmx.mediation.ooMediator.wsmx;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.scheduler.Environment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.deri.wsmx.mediation.ooMediator.logging.DataMediatorOutputStream;
import org.deri.wsmx.mediation.ooMediator.mapper.MappingDocument2Mappings;
import org.deri.wsmx.mediation.ooMediator.mapper.Mappings;
import org.deri.wsmx.mediation.ooMediator.storage.Loader;
import org.deri.wsmx.mediation.ooMediator.wsml.Pair;
import org.omwg.mediation.language.objectmodel.api.MappingDocument;
import org.omwg.mediation.language.objectmodel.api.OntologyId;
import org.omwg.mediation.parser.alignment.XpathParser;
import org.omwg.mediation.parser.hrsyntax.lexer.LexerException;
import org.omwg.ontology.Ontology;
import org.wsmo.common.IRI;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.ParserException;
import org.xml.sax.SAXException;

/**
 * Loader that uses the file system for storage.
 * 
 * Works basically like the resource manager component, 
 * but doesn't actually delegate to it like the RMLoader.
 * 
 * @see {@link RMLoader}
 * @see {@link ie.deri.wsmx.resourcemanager.inmemory.InMemoryRM}
 * 
 * @author Max
 * 
 * @deprecated  This class may be still used for testing purposes, 
 * 				otherwise use {@link RMLoader} instead.
 */
public class FileLoader implements Loader {
	
	static Logger logger = Logger.getLogger(FileLoader.class);
	
	protected final static String SUPPORTED_FORMALISM_IRI = "http://www.wsmo.org/wsml";
	protected final static String DEFAULT_MAPPINGFILE_DIR = "\"$(resources)/resourcemanager/datamediator/mappings\";";
	//protected final static String DEFAULT_MAPPINGFILE_DIR = "\"files\";";
	
	protected WsmoFactory wsmoFactory;
	
	protected DataMediatorOutputStream outputStream;
	
	/** (sourceOntIRI,targetOntIRI) -> loaded mapping document */
	protected Map<Pair<IRI, IRI>, MappingDocument> availableMappingDocuments;
	
	
	public FileLoader() {				
		
		outputStream = new DataMediatorOutputStream(new Log4jOutputStream(logger));
		
		wsmoFactory = Factory.createWsmoFactory(null);		
//		this.callingWSMXComponent = callingWSMXComponent;
		availableMappingDocuments = new HashMap<Pair<IRI,IRI>, MappingDocument>();
		
		logger.info("Loading mappings:");					
		
		try {
			Properties config = Environment.getConfiguration(); 
			
			String mappings = config.getProperty("wsmx.resourcemanager.mappings");
			
			if (mappings==null) {
				logger.info("No configuration found for mappings, looking in default dir: " + DEFAULT_MAPPINGFILE_DIR);
				mappings = DEFAULT_MAPPINGFILE_DIR;							
			} 
							
			loadDirectories(getDirectories(mappings));

			logger.info("Loaded Mappings: " + availableMappingDocuments.size());		
			
		} catch (Throwable t) {
			t.printStackTrace();
			logger.warn("Background resources loading failed.", t);
		}
	}
	
	public boolean containsOntology(Ontology ontology) {
		// TODO delegate directly to RM for performance reasons
		return Helper.getAllOntologies().contains(ontology);
	}
	
	public boolean containsMappings(Ontology sourceOntology, Ontology targetOntology) {
		return (sourceOntology != null) && (targetOntology != null) 
				&& availableMappingDocuments.containsKey(
						new Pair<IRI,IRI>(
								(IRI)sourceOntology.getIdentifier(), 
								(IRI)targetOntology.getIdentifier()));
	}

	public Set<Ontology> getAvailableOntologies() {		
			logger.debug("Retrieving ontologies...");

			Set<Ontology> result = Helper.getAllOntologies();
			if (logger.isDebugEnabled()) {
				logger.debug("All ontologies size: " + result.size());
			}
			
			return result;
	}


	public Mappings loadMappings(Ontology sourceOntology,
			Ontology targetOntology) {
		logger.info("Trying to load mappings for mediation...");
		try {
			Pair<IRI,IRI> requested = new Pair<IRI,IRI>(
					(IRI)sourceOntology.getIdentifier(), 
					(IRI)targetOntology.getIdentifier());
			
			if (availableMappingDocuments.containsKey(requested)) {	
				logger.debug("Found mapping document, creating mappings for mediation...");
				MappingDocument mapdoc = availableMappingDocuments.get(requested);
				return new MappingDocument2Mappings().
					mappingDocument2Mappings(sourceOntology, targetOntology, mapdoc);
			} else {
				logger.info("No mapping documents found, no mappings for mediation created.");
				return new Mappings();
			}
		} catch (Exception e) {
			throw new RuntimeException("Problem while trying to load mappings.", e);
		}
	}

	// TODO error handling
	public Ontology loadOntology(Ontology ontology) {				
			Ontology result = ontology;
			if (ontology.listConcepts().isEmpty()) {
				logger.debug("Retrieving ontology...");
				Helper.getOntology( (IRI) ontology.getIdentifier() );
			}
			return result;	
	}
	
	
    private void loadDirectories(List<String> directoriesStr) {
    	for (String dirStr : directoriesStr){
    		loadDirectory(dirStr);
    	}
    }
    
    private void loadDirectory(String directoryStr) {
    	String resourceStr = "";
    	if (Environment.isCore()){
    		resourceStr = Environment.getKernelLocation().getPath()+File.separator+"resources";
    	} else {
    		resourceStr = "resources";
    	}
    	
    	directoryStr = directoryStr.replace("$(resources)", resourceStr);
		logger.info(directoryStr);
	
		File mapDir = new File(directoryStr);
				
		if (!mapDir.exists()) {
			logger.debug("Background "+ mapDir.getAbsolutePath() + " directory does not exist. Skipping loading of background mappings.");
			return;
		} 
		
		if (!mapDir.canRead()) {
			logger.warn("Background " + directoryStr + " directory exist but is not readable. " +
					"Check file system permissions.");			
			return;
		}
		
		File[] mapFiles = mapDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".map");
			}
		});
		
		for (File file : mapFiles) {
			try {
				
				MappingDocument mapdoc = XpathParser.parse(file);
								
				OntologyId sourceOID = mapdoc.getSource();
				OntologyId targetOID = mapdoc.getTarget();
				
				if (sourceOID == null || targetOID == null
					|| !SUPPORTED_FORMALISM_IRI.equals(sourceOID.getFormalismUri().toString())					
					|| !SUPPORTED_FORMALISM_IRI.equals(targetOID.getFormalismUri().toString()))
				{
					logger.debug( sourceOID == null ? "null" : sourceOID.getFormalismUri().toString() );
					logger.debug( targetOID == null ? "null" : targetOID.getFormalismUri().toString() );
					throw new UnsupportedOperationException(
							"Source and target ontology formalism must both be \""+SUPPORTED_FORMALISM_IRI+"\".");
				}
				
				Pair<IRI,IRI> iris = new Pair<IRI,IRI>(
						wsmoFactory.createIRI(sourceOID.getUri().toString()), 
						wsmoFactory.createIRI(targetOID.getUri().toString()));
				
				if (availableMappingDocuments.containsKey(iris)) {
					logger.warn("Duplicate mapping file found for source \"" + iris.getFirst().toString() 
							+ "\" and target \"" + iris.getSecond().toString() + "\", discarding current.");
				}
				
				availableMappingDocuments.put(iris, mapdoc);				
			
			} catch (UnsupportedOperationException e) {
				logger.warn("Failed to store " + file.getAbsolutePath() + ": Operation not supported by Loader.", e);
			} catch (IOException e) {
				logger.warn("Failed to store " + file.getAbsolutePath() + ".", e);
			} catch (SAXException e) {
				logger.warn("Failed to store " + file.getAbsolutePath() + ": Error during parsing.", e);
			}
		}
    }
    
    protected MappingDocument parseMappingDocument(String theFileName) throws 
 	ParserException, LexerException, IOException, SAXException {
        MappingDocument md = null;
        try {               
            md = XpathParser.parse(getInputStreamForFile(theFileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (XPathExpressionException e) {
			e.printStackTrace();
		}
        
        return md;
    }
    
    
    /* Tokenizes String according to the following convention:
     * "dir1"; "dir2"; ...  
    */
    private List<String> getDirectories(String propertyString){
    	ArrayList<String> dirs = new ArrayList<String>();
    	StringTokenizer st = new StringTokenizer(propertyString,";");
    	while (st.hasMoreElements()){
    		String dir = st.nextToken();
    		if (dir == null || dir.equals("") || dir.indexOf("\"") == -1) 
    			continue;
    		dir = dir.substring(dir.indexOf("\"")+1);
    		if (dir == null || dir.equals("") || dir.indexOf("\"") == -1)
    			continue;
    		dir = dir.substring(0, dir.indexOf("\""));
    		dirs.add(dir);
    	}
    	return dirs; 
    }
 
    
    // TODO refactor to utilities
	 /**
	 * Utiltiy to get an InputStream, tries first FileInputStream and then to load from
	 * class path, helps avoiding FileNotFound exception during automated
	 * testing
	 * @param location
	 * @return
	 */
	public static InputStream getInputStreamForFile(String location) {
		InputStream is = null;
		try {
	        is = new FileInputStream(location);
	    } catch (FileNotFoundException e) {
	        // get current class loader and try to load from there...
	        is = FileLoader.class.getClassLoader()
	                .getResourceAsStream(location);
	        if (is==null) logger.warn("Could not load file from class path: " + location);
	    }
	    if (is==null) logger.warn("Could not load file from file system: " + location);
	    return is;
	}


	public DataMediatorOutputStream getOutputStream() {
		return outputStream;
	}

	



}
