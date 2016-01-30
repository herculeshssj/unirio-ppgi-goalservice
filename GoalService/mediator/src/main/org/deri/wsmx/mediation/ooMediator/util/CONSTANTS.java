/**
 * Copyright (c) 2004 National University of Ireland, Galway
 *
 * @author Adrian
 *
 * CONSTANTS.java, Apr 29, 2004, 12:35:14 PM
 *
 **/
package org.deri.wsmx.mediation.ooMediator.util;


public abstract class CONSTANTS {

	//payload types
	public static final int FLORA_DATA_MEDIATOR = 0;
	public static final int FLORA_DATA_MEDIATOR_DEBUGGER = 1;
	public static final int WSML_DATA_MEDIATOR = 2;
	public static final int WSML_DATA_MEDIATOR_DEBUGGER = 3;
	
	//suggestion types
	public static final byte NO_SUGESTIONS = 0;
	public static final byte BEST_SUGESTION = 1;
	public static final byte ALL_SUGESTIONS = 100;
	
	//mediation stuff
	public static final int SYNONYMS_NO = 2;

	public static final int HYPERNYMS_NO = 1;
	public static final int HYPERNYMS_DEPTH = 2;
		
	public static final int HYPONYMS_NO=1; 
	public static final int HYPONYMS_DEPTH=2;
	
	public static final int rowHeight = 18;
    public static final String NO_CONDITIONS = "No conditions associated";
	
}
