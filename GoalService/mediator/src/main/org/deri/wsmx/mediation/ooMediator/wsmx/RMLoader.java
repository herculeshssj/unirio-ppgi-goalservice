package org.deri.wsmx.mediation.ooMediator.wsmx;

import ie.deri.wsmx.commons.Helper;

import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmx.mediation.ooMediator.logging.DataMediatorOutputStream;
import org.deri.wsmx.mediation.ooMediator.mapper.MappingDocument2Mappings;
import org.deri.wsmx.mediation.ooMediator.mapper.Mappings;
import org.deri.wsmx.mediation.ooMediator.storage.Loader;
import org.omwg.mediation.language.objectmodel.api.MappingDocument;
import org.omwg.ontology.Ontology;
import org.wsmo.common.IRI;
import org.wsmo.execution.common.component.DataMediator;

/**
 * Loader that delegates loading to WSMX Resource Manager component.
 * 
 * (..actually to the Helper - true delegation to RM is yet to be implemented)
 * 
 * @author Max
 *
 */
public class RMLoader implements Loader {
	
	static Logger logger = Logger.getLogger(RMLoader.class);
	
	protected DataMediatorOutputStream outputStream;
	
	protected DataMediator callingWSMXComponent;
	
	public RMLoader(DataMediator callingWSMXComponent) {				
		logger.debug("Creating RMLoader with calling WSMX component: " + callingWSMXComponent);
		this.callingWSMXComponent = callingWSMXComponent;
		outputStream = new DataMediatorOutputStream(new Log4jOutputStream(logger));
	}
	
	public boolean containsOntology(Ontology ontology) {
		// TODO delegate directly to RM for performance reasons
		return Helper.getAllOntologies().contains(ontology);
	}
	
	public boolean containsMappings(Ontology sourceOntology, Ontology targetOntology) {
		boolean res = (sourceOntology!=null) && (targetOntology != null)
			&& Helper.containsMapping( (IRI)sourceOntology.getIdentifier(), (IRI)targetOntology.getIdentifier() );
		logger.debug("containsMapping? " + res);
		return res;
	}

	// TODO error handling
	public Set<Ontology> getAvailableOntologies() {
//		try {			
//			logger.debug(" (core?=" + Environment.isCore() + ")");
			
//			OntologyResourceManager oRM = (OntologyResourceManager) 
//				Environment.getComponentProxy(OntologyResourceManager.class, callingWSMXComponent);
//			
//			return oRM.retrieveOntologies();
			Set<Ontology> result = Helper.getAllOntologies();							
			
			return result;
			
//		} catch (ComponentException e) {
//			throw new RuntimeException("Problem getting available ontologies from store.", e);
//		}
	}


	public Mappings loadMappings(Ontology sourceOntology,
			Ontology targetOntology) {		
		try {
			logger.debug("Trying to load mappings for mediation between \"" 
					+ sourceOntology.getIdentifier().toString() + "\" and \""
					+ targetOntology.getIdentifier().toString() + "\"...");

			IRI srcOntoIRI = (IRI) sourceOntology.getIdentifier();
			IRI tgtOntoIRI = (IRI) targetOntology.getIdentifier();			
			
			MappingDocument mapdoc = Helper.getMapping(srcOntoIRI, tgtOntoIRI);
			if (mapdoc != null) {
				logger.debug("Found mapping document, creating mappings for mediation...");
				return new MappingDocument2Mappings().mappingDocument2Mappings(
						sourceOntology, targetOntology, mapdoc);
			} else {
				logger.debug("No mapping documents found, no mappings for mediation created.");
				return new Mappings();
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Problem while trying to load mappings.", e);
		}
	}

	public Ontology loadOntology(Ontology ontology) {			
//		try {			
			Ontology result = ontology;
			if (ontology.listConcepts().isEmpty()) {
				logger.debug("Retrieving ontology...");
//				OntologyResourceManager oRM = (OntologyResourceManager) 
//					Environment.getComponentProxy(OntologyResourceManager.class, callingWSMXComponent);
//				result = oRM.retrieveOntology( ontology.getIdentifier() );	
				Helper.getOntology( (IRI) ontology.getIdentifier() );
			}
			return result;
//		} catch (ComponentException e) {
//			throw new RuntimeException("Problem while trying to load ontology.", e);
//		}		
	}
	
	public DataMediatorOutputStream getOutputStream() {
		return outputStream;
	}

	
	
    
	



}
