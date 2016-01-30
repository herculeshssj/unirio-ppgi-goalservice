package org.deri.wsmx.mediation.ooMediator.mapper;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * Copyright (c) 2004 National University of Ireland, Galway
 * 
 * @author Adrian
 *
 *RelationsManager.java, Jan 17, 2005, 2:46:32 PM
 */
public class RelationsManager {

	private Hashtable relationsKey = new Hashtable(); 
	private Vector relations = new Vector();
	private Hashtable arity = new Hashtable();
	public static int INHERITANCE_KEY = 0;
	public static String INHERITANCE = "INHERITANCE";
	
	
	public RelationsManager(){
		relationsKey.put(INHERITANCE, new Integer(INHERITANCE_KEY));
		relations.add(new Hashtable());
		setArity(INHERITANCE, 2);
	}
	
	public void setArity(String relationName, int relationArity){
		arity.put(relationName, new Integer(relationArity));
	}
	
	public int getArity(String relation){
		return ((Integer)arity.get(relation)).intValue();
	}
	
	private int getRelationKey(String relationName){
		return ((Integer)relationsKey.get(relationName)).intValue();
	}
	
	public void addBinaryRelationship(String binaryRelationName, Object firstConcept, Object secondConcept){
		if (getArity(binaryRelationName)!=2)
			return;
		
		Vector relatives = (Vector)((Hashtable)relations.elementAt(getRelationKey(binaryRelationName))).get(firstConcept);
		if (relatives==null){
			relatives = new Vector();
			relatives.add(secondConcept);
			((Hashtable)relations.elementAt(getRelationKey(binaryRelationName))).put(firstConcept, relatives);
		}
		else
			if (!relatives.contains(secondConcept))
				relatives.add(secondConcept);
	}
	
	public void addBinaryRelationships(String binaryRelationName, Object firstConcept, Vector relatives){
		if (relatives == null)
			return;
		Iterator it = relatives.iterator();
		while (it.hasNext()){
			addBinaryRelationship(binaryRelationName, firstConcept, it.next());
		}
	}
	
	public Vector getRelativesFor(Object concept, String relationName){
		
		Hashtable relationship = (Hashtable)relations.elementAt(getRelationKey(relationName));
		if (relationship==null)
			return null;
		return (Vector)relationship.get(concept);
	}
}
