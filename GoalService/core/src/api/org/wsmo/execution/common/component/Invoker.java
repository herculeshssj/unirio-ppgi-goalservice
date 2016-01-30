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

import org.omwg.ontology.Ontology;
import org.wsmo.common.Entity;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;
import org.wsmo.execution.common.nonwsmo.grounding.WSDL1_1GroundingException;
import org.wsmo.service.WebService;


/**
 * Interface describing the behaviour available to WSMX to make invocations on external services.
 * Such services may be implemented using a variety of technologies e.g. Web services, J2EE.
 * The services may also have their endpoints described using different specifications e.g. WSDL, WSDL-S.
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/Invoker.java,v $,
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
 * @version $Revision: 1.6 $ $Date: 2007-06-14 14:29:34 $
 */

public interface Invoker {
    
	/**
	 * Invoke the Web service using the list of entities as the data to be passed to the
	 * service, and the grounding as an object describing the endpoint on which the invocation
	 * should be made.
     * @param service the source to be invoked
     * @param data the payload data to send to the service being invoked
     * @param endpointGrounding provides information necessary to bridge between 
     * (i) the semantic representation of the data to be and 
     * (ii) the data representation used by the service to receive and send messages
     * @return void
     * @throws ComponentException throws an exception if there is a problem reported during the invocation process
     * @throws UnsupportedOperationException throws an exception if an unexpected exception occurs 
	 * @throws WSDL1_1GroundingException 
	 */
    public List<Entity> invoke(WebService service, List<Entity> data, String grounding)
        throws ComponentException, UnsupportedOperationException, WSDL1_1GroundingException;
    
    public List<Entity> syncInvoke(WebService service, List<Entity> data, EndpointGrounding grounding, Ontology ontology) 
    	throws ComponentException, UnsupportedOperationException;

	/**
	 * This will be deprecated once the invoke method above is tested fully.
	 * Until tests complete, keep this method as it is used by the Mockup implementation
	 */
//    public void invoke(WebService service, List<Entity> data, String operation)
//    	throws ComponentException, UnsupportedOperationException;

}


