package org.wsmo.execution.common.component.resourcemanager;

import java.util.Collection;

import org.omwg.mediation.language.objectmodel.api.IRI;
import org.omwg.mediation.language.objectmodel.api.MappingDocument;
import org.wsmo.execution.common.exception.ComponentException;

/**
 * This interface is used to implement a store within WSMX for mappings 
 * that are used for data mediation.
 * 
 * (Note: This resource manager is specific to one mapping language.)
 * 
 * @author Maximilian Herold
 * 
 */
public interface MappingResourceManager {

	// TODO method documentation
	
	/**
	 * 
	 * @param mapdoc
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public void storeMapping(MappingDocument mapdoc) 
		throws ComponentException, UnsupportedOperationException;
	
	/**
	 * 
	 * @param iri
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public void removeMapping(IRI iri) 
		throws ComponentException, UnsupportedOperationException;
	
	/**
	 * 
	 * @param iri
	 * @return
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public MappingDocument retrieveMapping(IRI iri) 
		throws ComponentException, UnsupportedOperationException;
	
	/**
	 * 
	 * @param sourceOntologyIRI
	 * @param targetOntologyIRI
	 * @return
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public MappingDocument retrieveMapping(
			org.wsmo.common.IRI sourceOntologyIRI, 
			org.wsmo.common.IRI targetOntologyIRI)
	 	throws ComponentException, UnsupportedOperationException;
	
	/**
	 * 
	 * @return
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public Collection<MappingDocument> retrieveMappings() 
		throws ComponentException, UnsupportedOperationException;
	
	/**
	 * 
	 * @param iri
	 * @return
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public boolean containsMapping(IRI iri) 
		throws ComponentException, UnsupportedOperationException;
	
	/**
	 * 
	 * @param sourceOntologyIRI
	 * @param targetOntologyIRI
	 * @return
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public boolean containsMapping(
			org.wsmo.common.IRI sourceOntologyIRI, 
			org.wsmo.common.IRI targetOntologyIRI)
		throws ComponentException, UnsupportedOperationException;
	

}
