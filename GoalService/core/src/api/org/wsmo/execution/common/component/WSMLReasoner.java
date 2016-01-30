/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */
package org.wsmo.execution.common.component;

import java.util.Map;
import java.util.Set;

import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Variable;
import org.wsmo.common.IRI;
import org.wsmo.execution.common.exception.ComponentException;

/**
   * The interface for the WSML reasoner
   *
   * <pre>
   * Created on 16-Jun-2005
   * Committed by $Author: maitiu_moran $
   * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/WSMLReasoner.java,v $,
   * </pre>
   *
   * @author Michal Zaremba
   * @author Thomas Haselwanter
   *
   * @version $Revision: 1.4 $ $Date: 2005-12-20 14:50:09 $
   */

public interface WSMLReasoner {
    
    /**
     * Retrieves all the instances of a concept
     * @param concept the concept for wich we want to retrieve the instances
     * @param ontology the ontology that contains the concept
     * @return the set of instances
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<Instance> getAllInstances(Concept concept, IRI ontology)
			throws ComponentException, UnsupportedOperationException;

	/**
     * Retrieves all the sub-concepts of a concept
	 * @param concept the concept for wich we want to retrieve the sub-concepts
	 * @param ontology the ontology that contains the concept
	 * @return the set of sub-concepts
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public Set<Concept> getAllSubconcepts(Concept concept, IRI ontology)
			throws ComponentException, UnsupportedOperationException;

    /**
     * Retrieves all the super-concepts of a concept
     * @param concept the concept for wich we want to retrieve the super-concepts
     * @param ontology the ontology that contains the concept
     * @return the set of super-concepts
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
	public Set<Concept> getAllSuperconcepts(Concept concept, IRI ontology)
			throws ComponentException, UnsupportedOperationException;

	/**
     * Check if an instance is the instance of a certain concept
	 * @param instance
	 * @param concept
	 * @param ontology
	 * @return 
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public boolean isInstanceOf(Instance instance, Concept concept, IRI ontology)
			throws ComponentException, UnsupportedOperationException;

	/**
     * Registers an ontology to the reasoner
	 * @param ontology
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public void register(Ontology ontology) throws ComponentException,
			UnsupportedOperationException;

    /**
     * Registers a set of ontologies to the reasoner
     * @param ontologies
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public void register(Set<Ontology> ontologies) throws ComponentException,
			UnsupportedOperationException;

	/**
     * Removes an ontology from the knowledge base
	 * @param ontology
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public void deRegister(IRI ontology) throws ComponentException,
			UnsupportedOperationException;

	/**
     * Removes a set of ontologies from the knowledge base
	 * @param ontologies
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public void deRegister(Set<IRI> ontologies) throws ComponentException,
			UnsupportedOperationException;

	/**
     * Checks if a certain concept is a super-concept of another concept in a given ontology
	 * @param superConcept 
	 * @param subConcept
	 * @param ontology
	 * @return
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public boolean subsumes(Concept superConcept, Concept subConcept,
			IRI ontology) throws ComponentException,
			UnsupportedOperationException;

	/**
     * Executes a logical expression query in  given ontology
	 * @param query
	 * @param ontology
	 * @return
	 */
	public Set<Map<Variable, Term>> executeQuery(LogicalExpression query,
			IRI ontology);

	/**
     * Executes a true/false ground expression query in a given ontology
	 * @param query
	 * @param ontology
	 * @return
	 */
	public boolean executeGroundQuery(LogicalExpression query, IRI ontology);

	/**
     * Checks if a set of given logical expression are logically entailed by the ontology identified by the IRI
	 * @param ontologyID
	 * @param expressions
	 * @return
	 */
	public boolean entails(IRI ontologyID, Set<LogicalExpression> expressions);

	/**
     * Checks if a given logical expression is logically entailed by the ontology identifieed by IRI
	 * @param ontologyID
	 * @param expression
	 * @return
	 */
	public boolean entails(IRI ontologyID, LogicalExpression expression);

	/**
     * Checks if an ontology is satisfiable
	 * @param ontologyID
	 * @return
	 */
	public boolean isSatisfiable(IRI ontologyID);
}
