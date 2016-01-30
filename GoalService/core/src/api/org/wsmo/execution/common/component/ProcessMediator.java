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
import org.wsmo.common.Identifier;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.service.choreography.Choreography;


/**
 * This is an interface for the process mediator. 
 * Instances sent by one of the partners are transformed and forwarded to the targeted partner as needed 
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maitiu_moran $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/ProcessMediator.java,v $,
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
 * @version $Revision: 1.4 $ $Date: 2005-12-20 14:50:09 $
 */

public interface ProcessMediator {
    

    /**
     * The invocation direction is from goal to WS; that is, the requestor sends a message to the provider.
     */
    public final static int GOAL_TO_WS_INVOCATION  = 0;
    
    /**
     * The invocation direction is from WS to goal; that is, the provider sends a message to the requestor.
     */
    public final static int WS_TO_GOAL_INVOCATION = 1;
    /**
     * Generates a list of WSMO4j identifiable instances that can represent the concepts (and/or) instances to be sent to one of the two involved parties
     * @param sourceOntology the identifier of the choreography instance of the sender of a message; the 
     * choreography instance correspond to this particular phase of the conversation  
     * @param targetOntology the identifier of the choreography instance of the targeted partner; the 
     * choreography instance correspond to this particular phase of the conversation  
     * @param data the information contained in the message
     * @param the direction of the invocation (from goal to WS or the other way around)
     * @return the identifier of the choreography instance of the partner that needs to receive some messages, together 
     * with the list of identifiable objects containing the actual information; each of the two partners may receive one or more messages 
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Map<Identifier, List<Entity>> generate(Identifier sourceOntology, Identifier targetOntology, Set<Entity> data, int invocationDirection)
        throws ComponentException, UnsupportedOperationException;
   
    /**
     * Generates a list of WSMO4j identifiable instances that can represent the concepts (and/or) instances to be sent to one of the two involved parties
     * @param sourceOntology the ontology instance of the sender of a message (the choreography is seen as an ontology; 
     * this parameter is not refering to the internal ontology of a partner, but to its choreography) 
     * @param targetOntology the ontology instance of the sender of a message (the choreography is seen as an ontology; 
     * this parameter is not refering to the internal ontology of a partner, but to its choreography)  
     * @param data the information contained in the message
     * @param the direction of the invocation (from goal to WS or the other way around)
     * @return the identifier of the ontology (choreography) instance of the partner that needs to receive some messages, together 
     * with the list of identifiable objects containing the actual information; each of the two partners may receive one or more messages 
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Map<Identifier, List<Entity>> generate(Ontology sourceOntology, Ontology targetOntology, Set<Entity> data, int invocationDirection)
        throws ComponentException, UnsupportedOperationException;
 
    /**
     * Generates a list of identifiable of the concepts/instances to be sent to any of the two involved parties
     * @param sourceChoreography the choreography instance of the sender of a message 
     * @param targetChoreography the choreography instance of the sender of a message
     * @param data the information contained in the message
     * @param the direction of the invocation (from goal to WS or the other way around)
     * @return the identifier of the choreography instance of the partner that needs to receive some messages, together 
     * with the list of identifiable objects containing the actual information; each of the two partners may receive one or more messages 
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Map<Identifier, List<Entity>> generate(Choreography sourceChoreography, Choreography targetChoreography, Set<Entity> data, int invocationDirection)
        throws ComponentException, UnsupportedOperationException;

}
