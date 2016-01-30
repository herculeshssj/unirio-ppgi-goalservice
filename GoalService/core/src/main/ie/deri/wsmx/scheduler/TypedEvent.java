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

package ie.deri.wsmx.scheduler;

import ie.deri.wsmx.executionsemantic.AbstractExecutionSemantic;
import net.jini.entry.AbstractEntry;

/**
 * TODO comment
 *
 * <pre>
 * Created on 16.03.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/TypedEvent.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-11-17 09:26:05 $
 */
public class TypedEvent extends AbstractEntry implements Event {

    private static final long serialVersionUID = 4051048557912338740L;
    
    public String type = null;
    public String context = null;
    public AbstractExecutionSemantic executionSemantic = null;
    
    public TypedEvent() {
        this(null, null, null);
    }
    
    public TypedEvent(String type) {
        this(type, null, null);
    }

    public TypedEvent(String type, AbstractExecutionSemantic executionSemantic) {
        this(type, null, executionSemantic);
    }
    
    public TypedEvent(String type, String context, AbstractExecutionSemantic executionSemantic) {
        super();
        this.type = type;
        this.context = context;
        this.executionSemantic = executionSemantic;
    }
    
    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * @return Returns the uniqueId.
     */
    public String getContext() {
        return context;
    }
    /**
     * @param uniqueId The uniqueId to set.
     */
    public void setContext(String uniqueId) {
        this.context = uniqueId;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object event) {
        if (event instanceof TypedEvent) {
        	TypedEvent e = (TypedEvent)event;
            if (this.getType().equals(e.getType()) && this.getContext().equals(e.getContext()))
                return true;
        }
        return false;
    }
    
    /**
     * @return Returns the executionSemantic.
     */
    public AbstractExecutionSemantic getExecutionSemantic() {
        return executionSemantic;        
    }
    /**
     * @param executionSemantic The executionSemantic to set.
     */
    public void setExecutionSemantic(AbstractExecutionSemantic executionSemantic) {
        this.executionSemantic = executionSemantic;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return "Event of type " + type + " in context " + context; 
    }
}
