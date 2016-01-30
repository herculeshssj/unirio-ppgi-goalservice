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

package ie.deri.wsmx.scheduler.transport;

import java.io.IOException;
import java.net.MalformedURLException;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

/**
 * Locates a space in a Jini environment.
 *
 * <pre>
 * Created on Feb 21, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/scheduler/transport/SpaceLocator.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-09-04 02:07:46 $
 */
public class SpaceLocator {

    /**
     * Attempts to locate a service via unicast at a given host.
     * 
     * @param targetHost
     * @param serviceClass
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object getService(String targetHost, Class serviceClass)
            throws MalformedURLException, IOException, ClassNotFoundException {

        //receiving code with a null SecurityManager is not possible
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        LookupLocator lookupLocator = new LookupLocator("jini://" + targetHost);
        ServiceRegistrar reggie = lookupLocator.getRegistrar();
        ServiceTemplate template = new ServiceTemplate(null,
                                                       new Class[] {serviceClass},
                                                       null);
        return reggie.lookup(template);
    }



 
}
