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

import ie.deri.wsmx.scheduler.transport.Transport;

import javax.management.ObjectName;

import org.apache.log4j.Logger;

/**
 * Hides asynchronity from a synchronous
 * caller.
 *
 * <pre>
 * Created on 15.02.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/AbstractProxy.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-09-12 11:55:45 $
 */
public abstract class AbstractProxy<E> implements Proxy<E>{
	
    static Logger logger = Logger.getLogger(AbstractProxy.class);
    Transport transport;
    ObjectName counterpart;
    
    public AbstractProxy(Transport transport) {
        super();
        this.transport = transport;
    }
    
    public abstract E muteToComponent();
    
}
