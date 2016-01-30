/*
 * Copyright (c) 2006, University of Innsbruck, Austria.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ie.deri.wsmx.discovery.lightweightdl;

import java.util.*;

import ie.deri.wsmx.discovery.*;

/**
 * Interface or class description
 *
 * <pre>
 * Created on 01.12.2006
 * Committed by $Author: holgerlausen $
 * $Source: /cvsroot/wsmx/components/discovery/src/main/ie/deri/wsmx/discovery/lightweightdl/DLDiscoveryValidationException.java,v $,
 * </pre>
 *
 * @author Holger Lausen
 *
 * @version $Revision: 1.1 $ $Date: 2006/12/01 17:58:57 $
 */
public class DLDiscoveryValidationException extends DiscoveryException {
    private static final long serialVersionUID = 1L;
    private List errors = new ArrayList();

    public static final String NO_SERVICE = "no service given to register";
    
    public DLDiscoveryValidationException(String msg){
        super(msg);
    }

    public DLDiscoveryValidationException(String msg, List errors){
        super(msg);
        this.errors=errors;
    }
    
    public List getErrors(){
        return errors;
    }
}
