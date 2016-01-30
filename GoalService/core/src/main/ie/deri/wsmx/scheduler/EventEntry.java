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

import net.jini.entry.AbstractEntry;

/**
 * Tuplespace representation of an event.
 *
 * <pre>
 * Created on Feb 21, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/EventEntry.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-09-04 02:10:35 $
 */
public class EventEntry extends AbstractEntry {

	private static final long serialVersionUID = 3544388102190674485L;
    public Event event;    
     
    public EventEntry() {
        super();
    }

    public EventEntry(Event event) {
        super();
        this.event = event;
    }
           
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return "EventEntry " + event;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object arg0) {
        if (arg0 instanceof EventEntry)
            return event.equals(((EventEntry)arg0).getEvent());
        return false;
    }
    
    /**
     * @return Returns the event.
     */
    public Event getEvent() {
        return event;
    }
    
    /**
     * @param event The event to set.
     */
    public void setEvent(Event event) {
        this.event = event;
    }
}
