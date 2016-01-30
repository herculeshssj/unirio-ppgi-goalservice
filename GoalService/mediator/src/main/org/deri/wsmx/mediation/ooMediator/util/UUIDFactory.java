/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian
 *
 * UUIDFactory.java, February 8, 2005, 2:12:40 PM
 *
 **/

package org.deri.wsmx.mediation.ooMediator.util;

import java.util.GregorianCalendar;



public class UUIDFactory {

   
    /*# private UUIDFactory _uuidFactory; */
    private static UUIDFactory instance = null;

    protected UUIDFactory() {
    }
    
    public UUID newUUID(){
    	
    	GregorianCalendar cd = new GregorianCalendar();
    	String uuid = "" + cd.get(GregorianCalendar.YEAR) + cd.get(GregorianCalendar.MONTH) + cd.get(GregorianCalendar.WEEK_OF_YEAR) + 
    			cd.get(GregorianCalendar.WEEK_OF_MONTH) + cd.get(GregorianCalendar.DAY_OF_YEAR) + cd.get(GregorianCalendar.DAY_OF_MONTH) +  
    			cd.get(GregorianCalendar.DAY_OF_WEEK) + cd.get(GregorianCalendar.DAY_OF_WEEK_IN_MONTH) + cd.get(GregorianCalendar.WEEK_OF_YEAR) + 
    			cd.get(GregorianCalendar.HOUR_OF_DAY) + cd.get(GregorianCalendar.MINUTE) + cd.get(GregorianCalendar.SECOND) + cd.get(GregorianCalendar.MILLISECOND);
    	
    	return new UUID(uuid);
    }
    public static UUIDFactory getInstance(){
            if (instance == null) {
                synchronized(UUIDFactory.class) {
                    if (instance == null) {
                        instance = new UUIDFactory();
                    }
                }
            }
            return instance;
        }
}

