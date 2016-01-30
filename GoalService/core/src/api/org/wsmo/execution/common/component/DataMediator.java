/*
 * Copyright (c) 2005 National University of Ireland, Galway
 *                    Open University, Milton Keynes
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omwg.ontology.Ontology;
import org.wsmo.common.Entity;
import org.wsmo.execution.common.exception.ComponentException;

/**
 * This is an interface for the runtime data mediator and it provides support for instance transformation. 
 * That is instances expressed in terms of source ontology are transformed in instances expressed in terms 
 * of the target ontology. 
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/DataMediator.java,v $,
 * </pre>
 *
 * @author Michal Zaremba
 * @author Liliana Cabral 
 * @author John Domingue
 * @author David Aiken
 * @author Emilia Cimpian
 * @author Thomas Haselwanter
 * @author Mick Kerrigan
 * @author Adrian Mocan
 * @author Matthew Moran
 * @author Brahmananda Sapkota
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-11-05 22:32:47 $
 */

public interface DataMediator {
 
    /**
     * Transforms a set of source ontology instances into instances of the target ontology.
     * 
     * @param sourceOntology the source ontology 
     * @param targetOntology the target ontology 
     * @param data contains the subject of medition, a set containing instances in terms of source ontology
     * @return the mediated data, returned as a map between the initial (source) instances and the mediated data,
     * the instances in terms of target ontology
     * @throws ComponentException throws an exception if there is a problem reported during the mediation process
     * @throws UnsupportedOperationException throws an exception if data contains entities that cannot be mediated 
     */
    public Map<Entity, List<Entity>> mediate(Ontology sourceOntology, Ontology targetOntology, Set<Entity> data)
        throws ComponentException, UnsupportedOperationException;
       
    /**
     * Transforms a give source ontology instance into instances of the target ontology.
     * 
     * @param sourceOntology the source ontology 
     * @param targetOntology the target ontology 
     * @param data contains the subject of medition, an instance in terms of source ontology
     * @return the mediated data, a list of instances in terms of target ontology
     * @throws ComponentException throws an exception if there is a problem reported during the mediation process
     * @throws UnsupportedOperationException throws an exception if data contains entities that cannot be mediated
     */
    public List<Entity> mediate(Ontology sourceOntology, Ontology targetOntology, Entity data)
        throws ComponentException, UnsupportedOperationException;
}
