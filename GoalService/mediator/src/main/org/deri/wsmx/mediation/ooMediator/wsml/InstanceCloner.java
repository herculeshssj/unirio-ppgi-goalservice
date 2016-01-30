/*
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
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
 */

package org.deri.wsmx.mediation.ooMediator.wsml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deri.wsmx.mediation.ooMediator.mapper.Mappings;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;


public class InstanceCloner {

    private static final WsmoFactory factory = Factory.createWsmoFactory(new HashMap <String, Object> ());
    
    public static Instance clone(Instance theInstance, String theNamespace, Ontology theOntology, Mappings theMappings, boolean filter, Map <IRI, Instance> cloned) throws SynchronisationException, InvalidModelException{
        IRI cloneIRI = factory.createIRI(theNamespace + ((IRI) theInstance.getIdentifier()).getLocalName());
        
        if (cloned.containsKey(cloneIRI)){
            return cloned.get(cloneIRI); 
        }
        
        Instance clone = factory.createInstance(cloneIRI);
        cloned.put(cloneIRI, clone);
        
        for (Concept concept : theInstance.listConcepts()){
            Concept nonProxyConcept = factory.getConcept(concept.getIdentifier());
            clone.addConcept(nonProxyConcept);
        }
        for (Identifier attributeID : theInstance.listAttributeValues().keySet()){
            for (Value value : theInstance.listAttributeValues(attributeID)){
                if (!filter || 
                		(theMappings.containsMappingForAttribute(attributeID, getConcepts(theInstance)) ||
                		//if an attribute pints to an instance defined in the source ontology, it wil be kept even if it is not involved in any mappings (becouse it can be reffered from conditions). 		
                				(     value instanceof Instance && ((Instance) value).getOntology() != null && (!((Instance) value).getOntology().equals(theInstance.getOntology()))		)
                		)
                   ){
                    if (value instanceof Instance && ((Instance) value).getOntology() != null && ((Instance) value).getOntology().equals(theInstance.getOntology())){
                    	clone.addAttributeValue(attributeID, clone((Instance) value, theNamespace, theOntology, theMappings, filter, cloned));
                    }
                    else{
                        clone.addAttributeValue(attributeID, value);
                    }
                }
            }
        }
        clone.setOntology(theOntology);
        theOntology.addInstance(clone);
        return clone;
    }

    private static Set <Concept> getConcepts(Instance theInstance) {
        Set <Concept> result = new HashSet <Concept> ();
        for (Concept concept : theInstance.listConcepts()){
            result.addAll(getConcepts(concept));
        }
        return result;
    }

    private static Set<Concept> getConcepts(Concept theConcept) {
        Set <Concept> result = new HashSet <Concept> ();
        result.add(theConcept);
        for (Concept concept : theConcept.listSuperConcepts()){
            result.addAll(getConcepts(concept));
        }
        return result;
    }
}
