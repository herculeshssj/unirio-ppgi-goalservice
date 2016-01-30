/*
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 *   2005 Digital Enterprise Research Insitute (DERI) Galway
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 **/

package org.deri.wsmx.mediation.ooMediator.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.omwg.ontology.Attribute;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 03-Nov-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/beans/AnonymousInstance.java,v $, 
 * @version $Revision: 1.1 $ $Date: 2007-09-27 06:48:56 $
 */

public class AnonymousInstance implements Instance {

    private Set<Concept> concepts = new HashSet<Concept>();
    private Ontology ontology = null;
    private Identifier identifier = null;
    private Map<Identifier, Set<Value>> attributeValues = new HashMap<Identifier, Set<Value>>();
    /* (non-Javadoc)
     * @see org.omwg.ontology.Instance#addConcept(org.omwg.ontology.Concept)
     */
    
    public AnonymousInstance(Identifier identifier){
        this.identifier = identifier;
    }
    
    public void addConcept(Concept concept) throws SynchronisationException, InvalidModelException {
        concepts.add(concept);        
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.Instance#removeConcept(org.omwg.ontology.Concept)
     */
    public void removeConcept(Concept concept) throws SynchronisationException, InvalidModelException {
        concepts.remove(concept);
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.Instance#listConcepts()
     */
    public Set listConcepts() throws SynchronisationException {        
        return concepts;
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.Instance#addAttributeValue(org.wsmo.common.Identifier, org.omwg.ontology.Value)
     */
    public void addAttributeValue(Identifier attributeId, Value value) throws SynchronisationException, InvalidModelException {
        Set<Value> valuesForAttr = attributeValues.get(attributeId);
        if (valuesForAttr == null)
            valuesForAttr = new HashSet<Value>();
        valuesForAttr.add(value);
        attributeValues.put(attributeId, valuesForAttr);
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.Instance#removeAttributeValue(org.wsmo.common.Identifier, org.omwg.ontology.Value)
     */
    public void removeAttributeValue(Identifier attributeId, Value value) throws SynchronisationException, InvalidModelException {
        Set<Value> valuesForAttr = attributeValues.get(attributeId);
        if (valuesForAttr == null)
            return;
        valuesForAttr.remove(value);
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.Instance#removeAttributeValues(org.wsmo.common.Identifier)
     */
    public void removeAttributeValues(Identifier attributeId) throws SynchronisationException, InvalidModelException {
        attributeValues.put(attributeId, null);
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.Instance#listAttributeValues(org.wsmo.common.Identifier)
     */
    public Set listAttributeValues(Identifier attributeId) throws SynchronisationException {
        Set<Value> result = attributeValues.get(attributeId);
        if (result == null)
            result = new HashSet<Value>();
        return result;
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.Instance#listAttributeValues()
     */
    public Map listAttributeValues() throws SynchronisationException {
        return attributeValues;
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.OntologyElement#getOntology()
     */
    public Ontology getOntology() throws SynchronisationException {
        return ontology;
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.OntologyElement#setOntology(org.omwg.ontology.Ontology)
     */
    public void setOntology(Ontology ontology) throws SynchronisationException, InvalidModelException {
        this.ontology = ontology;
    }

    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#listNFPValues(org.wsmo.common.IRI)
     */
    public Set listNFPValues(IRI arg0) throws SynchronisationException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#listNFPValues()
     */
    public Map listNFPValues() throws SynchronisationException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#addNFPValue(org.wsmo.common.IRI, org.wsmo.common.Identifier)
     */
    public void addNFPValue(IRI arg0, Identifier arg1) throws SynchronisationException, InvalidModelException {
        throw new InvalidModelException("This is a placeholder implementation of a an Instance, with limited functionality"); 
        
    }

    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#addNFPValue(org.wsmo.common.IRI, org.omwg.ontology.Value)
     */
    public void addNFPValue(IRI arg0, Value arg1) throws SynchronisationException, InvalidModelException {
        throw new InvalidModelException("This is a placeholder implementation of a an Instance, with limited functionality"); 
        
    }

    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#removeNFPValue(org.wsmo.common.IRI, org.wsmo.common.Identifier)
     */
    public void removeNFPValue(IRI arg0, Identifier arg1) throws SynchronisationException, InvalidModelException {
        throw new InvalidModelException("This is a placeholder implementation of a an Instance, with limited functionality");         
    }

    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#removeNFPValue(org.wsmo.common.IRI, org.omwg.ontology.Value)
     */
    public void removeNFPValue(IRI arg0, Value arg1) throws SynchronisationException, InvalidModelException {
       throw new InvalidModelException("This is a placeholder implementation of a an Instance, with limited functionality"); 
    }

    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#removeNFP(org.wsmo.common.IRI)
     */
    public void removeNFP(IRI arg0) throws SynchronisationException, InvalidModelException {
        throw new InvalidModelException("This is a placeholder implementation of a an Instance, with limited functionality");         
    }

    /* (non-Javadoc)
     * @see org.wsmo.common.Entity#getIdentifier()
     */
    public Identifier getIdentifier() {
        return this.identifier ;
    }

    /* (non-Javadoc)
     * @see org.omwg.ontology.Instance#findAttributeDefinitions(org.wsmo.common.Identifier)
     */
    public Set findAttributeDefinitions(Identifier id) throws SynchronisationException {
        Set<Attribute> result = new HashSet<Attribute>();
        Iterator<Concept> ic = concepts.iterator();
        while (ic.hasNext()){
            result.addAll(ic.next().findAttributes(id));
        }
        if (result.isEmpty())
            return null;
        return result;
    }

    
}

