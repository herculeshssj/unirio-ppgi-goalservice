/**
 * 
 */
package org.deri.wsmx.mediation.ooMediator.gui;

import java.util.HashSet;
import java.util.Set;

import org.deri.wsmx.mediation.ooMediator.logging.DataMediatorOutputStream;
import org.deri.wsmx.mediation.ooMediator.mapper.Mappings;
import org.deri.wsmx.mediation.ooMediator.storage.Loader;
import org.omwg.ontology.Ontology;


/**
 * @author adrmoc
 *
 */
public class DefaultLoader implements Loader {

	private Ontology sourceOntology;
	private Ontology targetOntology;
	private Mappings mappings;
    private DataMediatorOutputStream outputstream;
	
    public DefaultLoader(Ontology sourceOntology, Ontology targetOntology, Mappings mappings) {
        this(sourceOntology, targetOntology, mappings, new DataMediatorOutputStream(System.out));
    }
    
	public DefaultLoader(Ontology sourceOntology, Ontology targetOntology, Mappings mappings, DataMediatorOutputStream theOutputStream) {
		this.sourceOntology = sourceOntology;
		this.targetOntology = targetOntology;
		this.mappings = mappings;
        this.outputstream = theOutputStream;
	}

	public void setMappings(Mappings mappings) {
		this.mappings = mappings;
	}

	public void setSourceOntology(Ontology sourceOntology) {
		this.sourceOntology = sourceOntology;
	}

	public void setTargetOntology(Ontology targetOntology) {
		this.targetOntology = targetOntology;
	}

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.mediation.ooMediator.storage.Loader#getAvailableOntologies()
	 */
	public Set<Ontology> getAvailableOntologies() {
		Set<Ontology> availableOntologies = new HashSet<Ontology>();
		if (sourceOntology!=null)
			availableOntologies.add(sourceOntology);
		if (targetOntology!=null)
			availableOntologies.add(targetOntology);
		return availableOntologies;
	}

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.mediation.ooMediator.storage.Loader#loadOntology(org.omwg.ontology.Ontology)
	 */
	public Ontology loadOntology(Ontology ontology) {
		return ontology;
	}

	/* (non-Javadoc)
	 * @see ie.deri.wsmx.mediation.ooMediator.storage.Loader#loadMappings(org.omwg.ontology.Ontology, org.omwg.ontology.Ontology)
	 */
	public Mappings loadMappings(Ontology sourceOntology, Ontology targetOntology) {
		return mappings;
	}
	
	public boolean containsMappings(Ontology sourceOntology,
			Ontology targetOntology) {
		if (mappings == null || sourceOntology == null || targetOntology == null) {
			return false;
		} 
		return sourceOntology.equals(mappings.getSrcOntology())
			&& targetOntology.equals(mappings.getTgtOntology());
	}

	public boolean containsOntology(Ontology ontology) {
		if (ontology == null) {
			return false;
		}
		return ontology.equals(sourceOntology)
			|| ontology.equals(targetOntology);
	}

    public DataMediatorOutputStream getOutputStream() {
        return outputstream;
    }


}
