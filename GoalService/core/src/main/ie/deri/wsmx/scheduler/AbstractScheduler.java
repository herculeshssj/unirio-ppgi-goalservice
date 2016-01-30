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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Logger;


/**
 * Abstract superclass for all component wrappers.
 *
 * <pre>
 * Created on 14.02.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/AbstractScheduler.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.6 $ $Date: 2005-11-25 14:46:28 $
 */
public abstract class AbstractScheduler implements NotificationListener, Runnable, Serializable {

    static Logger logger = Logger.getLogger(AbstractScheduler.class);
    
    protected boolean alive = true;
    protected transient Reviver reviver = null;
    protected transient Transport transport = null;
    protected transient Thread reviverThread = null;
    protected transient List<AbstractProxy>proxies = new ArrayList<AbstractProxy>();
    
    public AbstractScheduler() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    public abstract void handleNotification(Notification notification, Object object); 
    
    public abstract void start(String spaceLocation, String eventType);

    /* (non-Javadoc)
     * @see ie.deri.wsmx.component.AbstractWrapperMBean#stop()
     */
    public void stop() {
        alive = false;    
    }

    public abstract AbstractProxy getProxy(String name);    

    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {                
        logger.debug("Wrapper started in Thread " + Thread.currentThread());        
        while(alive) {  
            Thread.yield();
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ie) {
                //back to loop
            }            
        }
        logger.debug("Wrapper running in Thread " + Thread.currentThread() +
                     " is stopping.");        

        reviver.stop();
        
        logger.debug("Wrapper running in Thread " + Thread.currentThread() +
        " ceases to exist.");        
    }
    
}
