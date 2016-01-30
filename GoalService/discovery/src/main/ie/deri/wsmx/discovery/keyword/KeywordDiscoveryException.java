package ie.deri.wsmx.discovery.keyword; 

/**
 * Copyright (c) 2004 DERI www.deri.org
 * Created on Nov 29, 2004 
 * @author Ioan Toma
 *
 **/

import ie.deri.wsmx.discovery.*;

public class KeywordDiscoveryException extends DiscoveryException{
	
	public KeywordDiscoveryException() {
		super();
	}
		
	public KeywordDiscoveryException(Exception e) {
		super(e);		
	}
	
	public KeywordDiscoveryException(String s) {
		super(s);
	}
 
}