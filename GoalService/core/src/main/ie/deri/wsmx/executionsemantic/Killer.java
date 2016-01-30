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

import java.util.ArrayList;
import java.util.List;

import ie.deri.wsmx.scheduler.Proxy;

import org.apache.log4j.Logger;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.MessageId;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;

/**
 * Replacement execution semantic that takes over in case of a failure
 * of an execution semantic or one of the component used by it.
 *
 * <pre>
 * Created on Nov 18, 2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/executionsemantic/Killer.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.4 $ $Date: 2006-10-03 14:45:18 $
 */ 
public class Killer extends WSMXExecutionSemantic {
    private static final long serialVersionUID = 3256446889141418038L;
    static Logger logger = Logger.getLogger(Killer.class);
    Exception cause = null;
    
    public Killer() {
        super();
    }

    public Killer(Context contextId) {
        super();
        initialize(contextId, null, null);
    }

    public Killer(Context contextId, Exception cause) {
        super();
        initialize(contextId, null, null);
        this.cause = cause;
    }

    public Killer(Context contextId, MessageId messageId, String message) {
        super();
		List<String> msgs = new ArrayList<String>();
		msgs.add(message);
        initialize(contextId, messageId, msgs);
    }
    
    /* (non-Javadoc)
	 * @see ie.deri.wsmx.executionsemantic.StoreEntity#initialize(org.wsmo.execution.common.nonwsmo.Context, org.wsmo.execution.common.nonwsmo.MessageId, java.lang.String)
	 */
    public void initialize(Context contextId, MessageId messageId, List<String> messages) {
    	state = new Exodus(contextId);
    }

    /* (non-Javadoc)
	 * @see ie.deri.wsmx.executionsemantic.StoreEntity#getProxy(java.lang.Class)
	 */
    public <E> Proxy<E> getProxy(Class<E> clazz) {
        return null;
    }
	
	class Exodus extends State {
		private static final long serialVersionUID = 5417996174132567582L;
		public Context context;
		
	    public Exodus(Context context) {
	        super();
	        this.context = context;
	    }
		
		@Override
		public State handleState(Object component) throws UnsupportedOperationException, ComponentException {
			return null;
		}

		@Override
		public Event getAssociatedEvent() {
			return Event.EXODUS;
		}

		@Override
		public boolean isStatefulResponse() {
			return false;
		}		
	}

	public Exception getCause() {
		return cause;
	}

	public void setCause(Exception cause) {
		this.cause = cause;
	}
	
}
