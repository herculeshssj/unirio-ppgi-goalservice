/*
 * Copyright (c) 2007 National University of Ireland, Galway
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

package org.wsmo.execution.common;

import org.wsmo.execution.common.exception.SystemException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;

/**
 * The EntryPoint interface describes the external interface to WSMX as a whole.
 * Each exposed entrypoint corresponds to a defined execution semantics which
 * WSMX instantiates to carry out specific tasks.
 * Execution semantics provide a means to describe the operational behaviour of WSMX
 * in terms of the invocation order of the functional components.
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maciejzaremba $
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
 * @version $Revision: 1.4 $ $Date: 2007-06-14 14:26:49 $
 */

public interface EntryPoint {
    
	/**
	 * The getWebService entry point addresses the scenario where the service requester 
	 * wishes to consult WSMX to find a single Web services capable of satisfying their 
	 * Goal. In this call, service requester provides Goal and expects to get back a 
	 * context in return. 
	 * This context can then be used by the requester to either review the details of the
	 * discovered Web Service or to request an invocation of the service in a subsequent 
	 * call to WSMX. 
     * @param WSMLDocument containing a goal description
     * @return Context that can be used for further interaction with WSMX
     * @throws SystemException throws an exception if there is a problem reported by WSMX for this task
	 */
    public Context getWebService (WSMLDocument wsmlMessage)
        throws SystemException;
    
	/**
	 * The achieveGoal extrypoint allows for an end-to-end goal based task execution.
	 * A service requester uses WSMO to describe the goal they wish to achieve using WSMO 
	 * and submit this to WSMX. The execution environment provided by WSMX takes care of
	 * discovering, selecting, mediating and invoking the service(s) requireds to fulfil
	 * the requester's goal.
     * @param WSMLDocument containing a goal description
     * @return Context that can be used for further interaction with WSMX
     * @throws SystemException throws an exception if there is a problem reported by WSMX for this task
	 */
    public Context achieveGoal(WSMLDocument wsmlMessage) 
        throws SystemException;

	/**
	 * This invokeWebService entrypoint is intended for the case where a service requester
	 * already has a description of a Web Service they wish to invoke. This entrypoint does
	 * not specify how this mechanism will be implemented by WSMX.
     * @param WSMLDocument containing a Web Service description as well as any data  
     * that should be sent with the service invocation.
     * @return Context that can be used for further interaction with WSMX
     * @throws SystemException throws an exception if there is a problem reported by WSMX for this task
	 */
    public Context invokeWebService(WSMLDocument wsmlMessage)
        throws SystemException;
    
	/**
	 * This invokeWebService entrypoint is intended for the case where a service requester
	 * already has a context that can be used by WSMX to determine the Web Service that 
	 * should be invoked. This API does not prescribe how this entrypoint will be implemented.
     * @param WSMLDocument containing any data that should be sent with the service invocation.
     * @return Context that can be used for further interaction with WSMX
     * @throws SystemException throws an exception if there is a problem reported by WSMX for this task
	 */
    public Context invokeWebService(WSMLDocument wsmlMessage, Context context)
        throws SystemException;
   
	/**
	 * This entrypoint is intended where a WSMO entity (Web Service, Goal, Mediator or Ontology)
	 * needs to be stored to the internal WSMO registry of WSMX. 
	 * Entities stored in this way are immediately available to components such as discovery 
	 * and mediation.
     * @param WSMLDocument containing the WSML description of the entities to be stored.
     * @return Context that can be used for further interaction with WSMX
     * @throws SystemException throws an exception if there is a problem reported by WSMX for this task
	 */
    public Context store(WSMLDocument wsmlMessage) throws SystemException;
}
