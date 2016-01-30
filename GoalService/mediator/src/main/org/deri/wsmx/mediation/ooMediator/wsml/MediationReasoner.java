/*
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
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

import java.util.List;
import java.util.Set;

import org.deri.wsmx.mediation.ooMediator.mapper.Mappings;
import org.omwg.ontology.Axiom;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Namespace;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.wsml.ParserException;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 29-Apr-2006
 * Committed by $Author: adrian.mocan $
 * 
 * $Source: /var/repository/wsmx-datamediator/src/main/java/org/deri/wsmx/mediation/ooMediator/wsml/MediationReasoner.java,v $, 
 * @version $Revision: 1.3 $ $Date: 2008/01/10 12:03:23 $
 */

public interface MediationReasoner {

    public void addAxiom(Axiom axiom) throws SynchronisationException, InvalidModelException;
    
    public void addAxioms(Set<Axiom> axioms) throws SynchronisationException, InvalidModelException;
        
    public void addSourceInstances(Set<Instance> sourceInstances, Mappings theMappings) throws SynchronisationException, InvalidModelException;
    
    public Set <Instance> getSourceInstances();
    
    public Set<Instance> getInstance(Concept c) throws ParserException;
    
    public List<Set<Instance>> getInstances(Set<Concept> targetConceptsSet) throws ParserException;
    
    public List<Set<Instance>> getInstances(Set<Concept> targetConceptsSet, Set<Concept> sourceConceptsSet) throws ParserException;
    
    public Set<Instance> getInstance(Concept targetConcept, Set<Concept> sourceConceptsSet) throws ParserException;
    
    public void refreshKB();
    
    public void addNamespace(Namespace ns);
    
    public void addNamespaces(Set<Namespace> namespaces);
    
    public void addOntologies(Set<Ontology> ontologies);
    
    public void addOntology(Ontology ontology);
    
    public Set<Namespace> getNamespaces();
}

