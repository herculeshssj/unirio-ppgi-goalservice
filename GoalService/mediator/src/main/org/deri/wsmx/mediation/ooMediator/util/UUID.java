/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian
 *
 * UUID.java, February 8, 2005, 2:13:59 PM
 *
 **/
package org.deri.wsmx.mediation.ooMediator.util;


public final class UUID {

	private String uuid = "";
	
	protected UUID(String uuid){
		this.uuid = uuid;
	}

	public String toString(){
		return uuid;
	}
}

