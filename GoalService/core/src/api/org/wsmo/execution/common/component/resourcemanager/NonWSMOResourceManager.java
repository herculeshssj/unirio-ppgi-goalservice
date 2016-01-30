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

package org.wsmo.execution.common.component.resourcemanager;

import java.util.Map;
import java.util.Set;

import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.MessageId;
import org.wsmo.execution.common.nonwsmo.WSDLDocument;
import org.wsmo.service.Goal;

/**
 * NonWSMOResourceManager interface is responsible for providing a generic access to the non WSMO elements repository. 
 * Data tackled by this interface concerns mostly execution phase in Semantic Execution Environment, namely: 
 * exchanged messages, contexts of ongoing conversations or Goal grounding to the WSDL.     
 * 
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/component/resourcemanager/NonWSMOResourceManager.java,v $,
 * </pre>
 *
 * @author Michal Zaremba
 * @author Liliana Cabral 
 * @author John Domingue
 * @author Emilia Cimpian
 * @author Thomas Haselwanter
 * @author Mick Kerrigan
 * @author Adrian Mocan
 * @author Matthew Moran
 * @author Brahmananda Sapkota
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-11-22 20:03:30 $
 */

public interface NonWSMOResourceManager  {
    
	/**
	 * Saves message of the given context
	 * 
	 * @param Context context of the message to be saved
	 * @param messageId message identifier of the message to be saved
	 * @param message message to be saved
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public void saveMessage(Context Context, MessageId messageId, String message)
        throws ComponentException, UnsupportedOperationException;
    
	/**
	 * Retrieves all contexts of ongoing conversations
	 * 
	 * @return Set of contexts
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public Set<Context> getContexts()
        throws ComponentException, UnsupportedOperationException;

	/**
	 * Retrieves all messages for the given conversation context
	 * 
	 * @param context context of the messages to retrieve
	 * @return Set of identifiers to retrieved messages.
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public Set<MessageId> getMessageIds(Context context)
        throws ComponentException, UnsupportedOperationException;
    
	/**
	 * Retrieves a set of the contexts associated with exchanged messages fulliling given criteria. Retrived data 
     * contains either contexts with messages fulfilling all specified criteria or at least one criteria. It is 
     * determinated by the conjunctive flag.
	 * 
	 * @param searchTerms Restrition on the data to be retrieved
	 * @param conjunctive flag that indicates whether the conjuction on the set of searchTerms should be applied
	 * @return Map of pairs: context with set of identifiers to the messages
	 * @throws ComponentException
	 * @throws UnsupportedOperationException
	 */
	public Map <Context, Set <MessageId>> getMessageIds(Set <Object> searchTerms, boolean conjunctive)
        throws ComponentException, UnsupportedOperationException;
    	
    /**
     * Retrieves a set of the exchanged messages fulliling given criteria for the specified conversation context. 
     * Retrived data contains either messages fulfilling all specified criteria or at least one criteria. It is 
     * determinated by the conjunctive flag.
     * 
     * @param context context of the messages to be retrieved
	 * @param searchTerms Restrition on the data to be retrieved
	 * @param conjunctive flag that indicates whether the conjuction on the set of searchTerms should be applied
	 * @return Map of pairs: context with set of identifiers to the messages
	 * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Set<MessageId> getMessageIds(Context context, Set <Object> searchTerms, boolean conjunctive)
        throws ComponentException, UnsupportedOperationException;
        
    /**
     * Loads all messages exchanged in the given context
     *     
     * @param context context of the messages to loaded 
     * @return Map of loaded messages in format Map <MessageId, String>
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Map <MessageId, String> load(Context context)
        throws ComponentException, UnsupportedOperationException;
    
    /**
     * Loads the contents of the specified message
     *  
     * @param context Context for the message to be loaded
     * @param messageId identfier of the message to be loaded
     * @return String of the message
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public String load(Context Context, MessageId messageId)
        throws ComponentException, UnsupportedOperationException;
    
    /**
     * Loads all messages from the repository
     * 
     * @return a Map of pairs <Context, <MessageId, String> >
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public Map <Context, Map <MessageId, String>> loadAll ()
        throws ComponentException, UnsupportedOperationException;
    
    /**
     * Registers a grounding to the WSDL for the given Goal in specified context 
     * @param context Context for the WSDL document
     * @param goal The Goal for the WSDL document
     * @param wsdlDocument WSDL to be registered
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public void registerWSDL(Context context, Goal goal, WSDLDocument wsdlDocument)
        throws ComponentException, UnsupportedOperationException; 

    /**
     * Retrieves WSDL associated with the given Goal in specified context
     * 
     * @param context Context for the WSDL document
     * @param goal The Goal for the WSDL document
     * @return WSDLDocument
     * @throws ComponentException
     * @throws UnsupportedOperationException
     */
    public WSDLDocument getWSDL (Context context, Goal goal)
        throws ComponentException, UnsupportedOperationException; 
}
