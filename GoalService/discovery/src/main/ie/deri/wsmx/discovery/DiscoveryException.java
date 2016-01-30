
package ie.deri.wsmx.discovery;

import org.wsmo.execution.common.exception.*;

/**
 * Copyright (c) 2004 DERI www.deri.org
 * 
 * @author Ioan Toma
 *
 **/

public class DiscoveryException extends ComponentException{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DiscoveryException() {
		super();
	}
		
	public DiscoveryException(Exception e) {
		super(e);		
	}
	
	public DiscoveryException(String s) {
		super(s);
	}
 
}