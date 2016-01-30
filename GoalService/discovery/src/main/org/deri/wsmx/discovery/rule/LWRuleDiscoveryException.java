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

package org.deri.wsmx.discovery.rule;

import java.util.*;

import ie.deri.wsmx.discovery.*;

/**
 * Interface or class description
 *
 * <pre>
 * Created on 01.12.2006
 * Committed by $Author: holgerlausen $
 * $Source: /cvsroot/wsmx/components/discovery/src/main/org/deri/wsmx/discovery/rule/LWRuleDiscoveryException.java,v $,
 * </pre>
 *
 * @author Holger Lausen
 *
 * @version $Revision: 1.3 $ $Date: 2007/09/25 08:46:11 $
 */
public class LWRuleDiscoveryException extends DiscoveryException {
    private static final long serialVersionUID = 1L;
    private List<String> errors = new ArrayList<String>();

    public static final String NO_SERVICE = "no service given to register";
    
    public LWRuleDiscoveryException(String msg){
        super(msg);
    }

    public LWRuleDiscoveryException(String msg, List<String> errors){
        super(msg);
        this.errors=errors;
    }
    
    public List<String> getErrors(){
        return errors;
    }
}
