/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian
 *
 * IndexGenerator.java, Apr 4, 2004, 8:28:17 PM
 *
 **/
package org.deri.wsmx.mediation.ooMediator.util;

/**
 * Generates distinct sequence numbers.
 *
 */
public class IndexGenerator {
	
	private static int index = 0;
    private static int mediatedIndex = 0;
    private static int ontologyIndex = 0;
	
	/**
	 * The constructor is declared private for preventing the class instantiation. 
	 * This class is used by the mean of its static methods.
	 */
	private IndexGenerator(){
	}
	
	/**
	 * @return new sequence number as an int 
	 */
	public static int getIndex(){
		return ++index;
	}
    
    public static int getMediatedIndex(){
        return ++mediatedIndex;
    }
    
    public static int getOntologyIndex() {
        return ++ontologyIndex;
    }
	
	/**
	 * @return new sequence number as a string
	 */
	public static String getStringIndex(){
		return String.valueOf(++index);
	}
    
    public static String getStringMediatedIndex(){
        return String.valueOf(++mediatedIndex);
    }

    public static String getStringOntologyIndex() {
        return String.valueOf(++ontologyIndex);
    }
}
