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

package ie.deri.wsmx.executionsemantic;

import ie.deri.wsmx.scheduler.Proxy;
import ie.deri.wsmx.scheduler.transport.Transport;

import java.io.Serializable;
import java.util.List;

import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.exception.SystemException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.MessageId;

/**
 * The interface of an execution semantic.
 *
 * @author Thomas Haselwanter
 * @author Maciej Zaremba
 *
 * Created on 15.03.2005
 * Committed by $Author: maciejzaremba $
 *
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/executionsemantic/ExecutionSemantic.java,v $,
 * @version $Revision: 1.7 $ $Date: 2006-10-03 14:45:18 $
 */
public interface ExecutionSemantic extends Serializable {

    public void initialize(Context contextId, MessageId messageId, List<String> messages);
    
    public String runState(Object component, Serializable ... params)
    	throws ComponentException, SystemException;  

    public void setTransport(Transport transport);
    
    public <E> Proxy<E> getProxy(Class<E> clazz);
    
}
