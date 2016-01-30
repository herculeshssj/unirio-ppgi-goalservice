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

import ie.deri.wsmx.scheduler.RoutingException;

import java.io.Serializable;

import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.Context;

/**
 * TODO Comment this type.
 *
 * <pre>
 * Created on Sep 10, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/executionsemantic/State.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2005-11-21 03:12:10 $
 */ 
public abstract class State implements Serializable {
    public Context contextId = null;

    public abstract State handleState(Object component)
            throws UnsupportedOperationException, ComponentException, RoutingException, DataFlowException;

    public abstract WSMXExecutionSemantic.Event getAssociatedEvent();
    
    @Override
    public String toString() {
    	return this.getAssociatedEvent().toString();
    }

	public Context getContext() {
		return contextId;
	}
	
	public Serializable getData(){
		return null;
	}
	
	public abstract boolean isStatefulResponse();
	
}