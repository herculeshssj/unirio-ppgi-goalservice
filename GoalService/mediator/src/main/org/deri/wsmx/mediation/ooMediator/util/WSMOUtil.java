/*
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 *   2005 Digital Enterprise Research Insitute (DERI) Galway
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 **/


package org.deri.wsmx.mediation.ooMediator.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.deri.wsmx.mediation.ooMediator.wsml.MediationReasoner;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.Concept;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Type;
import org.omwg.ontology.Value;
import org.omwg.ontology.WsmlDataType;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.DataFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.locator.LocatorManager;
import org.wsmo.wsml.Parser;

/** 
 * Interface or class description
 * 
 * @author FirstName LastName, FirstName LastName
 *
 * Created on 04-Oct-2005
 * Committed by $Author: adrian.mocan $
 * 
 * $Source: /var/repository/wsmx-datamediator/src/main/java/org/deri/wsmx/mediation/ooMediator/util/WSMOUtil.java,v $, 
 * @version $Revision: 1.4 $ $Date: 2008/02/26 03:20:27 $
 */
public class WSMOUtil {
    

    public static WsmoFactory wsmoFactory = null;
    public static DataFactory dataFactory = null;
    public static LogicalExpressionFactory leFactory = null;
    public static Parser parser = null;
    public static LocatorManager locatorManager = null;
    
    private static long uniqueIDCounter = 0; 
    
    static{
        ClassLoader defaultCL = Thread.currentThread().getContextClassLoader();  
        Thread.currentThread().setContextClassLoader(Factory.class.getClassLoader());
        wsmoFactory = Factory.createWsmoFactory(null);
        dataFactory = Factory.createDataFactory(null);
        leFactory = Factory.createLogicalExpressionFactory(null);
        locatorManager = Factory.getLocatorManager();
        
        HashMap <String, Object> props = new HashMap <String, Object> ();
        //props.put(Parser.PARSER_WSMO_FACTORY, wsmoFactory);
        //props.put(Parser.PARSER_LE_FACTORY, leFactory);
        //props.put(Factory.PROVIDER_CLASS, parser);
        parser = Factory.createParser(props);
               
        Thread.currentThread().setContextClassLoader(defaultCL);
         
         
    }
    
    public static MediationReasoner mediationReasoner = null;
   
    //public static Serializer serializer = Factory.createSerializer(new HashMap());
    public static org.deri.wsmx.mediation.ooMediator.util.Serializer serializer = new org.deri.wsmx.mediation.ooMediator.util.Serializer();
    public static List<String> dataTypeNames = Arrays.asList(new String[]{
            WsmlDataType.WSML_BASE64BINARY, WsmlDataType.WSML_BOOLEAN, WsmlDataType.WSML_DATE, 
            WsmlDataType.WSML_DATETIME, WsmlDataType.WSML_DECIMAL, WsmlDataType.WSML_DOUBLE, 
            WsmlDataType.WSML_DURATION, WsmlDataType.WSML_FLOAT, WsmlDataType.WSML_GDAY, 
            WsmlDataType.WSML_GMONTH, WsmlDataType.WSML_GMONTHDAY, WsmlDataType.WSML_GYEAR, 
            WsmlDataType.WSML_GYEARMONTH, WsmlDataType.WSML_HEXBINARY, WsmlDataType.WSML_INTEGER, 
            WsmlDataType.WSML_IRI, WsmlDataType.WSML_SQNAME, WsmlDataType.WSML_STRING, 
            WsmlDataType.WSML_TIME});
    
    
    public static String getOntologyName(Ontology theOntology){
        if (theOntology.getIdentifier() instanceof IRI){
            return ((IRI) theOntology.getIdentifier()).getLocalName();
        }
        return theOntology.getIdentifier().toString();
    }
    
    public static String getConceptName(Concept theConcept){
        if (theConcept.getIdentifier() instanceof IRI){
            return ((IRI) theConcept.getIdentifier()).getLocalName();
        }
        return theConcept.getIdentifier().toString();
    }
    
    public static String getAttributeName(Attribute theAttribute){
        if (theAttribute.getIdentifier() instanceof IRI){
            return ((IRI) theAttribute.getIdentifier()).getLocalName();
        }
        return theAttribute.getIdentifier().toString();
    }
    
    public static String getInstanceName(Instance theInstance){
        if (theInstance.getIdentifier() instanceof IRI){
            return ((IRI) theInstance.getIdentifier()).getLocalName();
        }
        return theInstance.getIdentifier().toString();
    }
    
    public static String getValueName(Value theValue){
        if (theValue instanceof Instance){
            return getInstanceName((Instance) theValue);
        }
        else
            if (theValue instanceof DataValue)
                return ((DataValue)theValue).getValue().toString();
        return theValue.toString();
    }
    
    public static String getTypeName(Type theType){
        String result = "";
        if (theType instanceof Concept){
            Identifier id = ((Concept)theType).getIdentifier();
            if (id instanceof IRI)
                result = ((IRI)id).getLocalName();
            else
                result = id.toString();
        }
        else 
            if (theType instanceof WsmlDataType)
                result = ((WsmlDataType)theType).getIRI().getLocalName();
        return result;
    }
    
    public static Identifier getIdentifier(Type theType){
        if (theType instanceof Concept)
            return ((Concept)theType).getIdentifier();
        return ((WsmlDataType)theType).getIRI();
    }
    
   
    public static String getFullTypeName(Type theType){
        String result = theType.toString();
        if (theType instanceof Concept){
            result = ((Concept)theType).getIdentifier().toString();
        }
        else 
            if (theType instanceof WsmlDataType)
                result = ((WsmlDataType)theType).getIRI().toString();
        return result;
    }
    
    public static DataValue createDataValueFromMediationService(Object value){
        if (value instanceof Integer){
            return dataFactory.createWsmlInteger(value.toString());
        }
        else if (value instanceof String){
            return dataFactory.createWsmlString(value.toString());
        }
        else if (value instanceof Boolean){
            return dataFactory.createWsmlBoolean(value.toString());
        }
        return dataFactory.createWsmlString("I can't handle that datatype yet!!!");
    }
    
    public static DataValue createDataValue(String value){    
        try {
            return dataFactory.createWsmlInteger(value);
        } catch (IllegalArgumentException e) {
            try{
            	if (value.equals("_boolean(\"true\")") || value.equals("true"))
            		return dataFactory.createWsmlBoolean("true");
            	if (value.equals("_boolean(\"true\")") || value.equals("true"))
            		return dataFactory.createWsmlBoolean("false");
                throw new IllegalArgumentException();
            }
            catch (IllegalArgumentException e1){
                return dataFactory.createWsmlString(value);
            }
        }
    }
    
    public static Concept createConcept(String concept){
        Concept c = getConcept(concept);
    	if (c == null){
    		c = wsmoFactory.createConcept(createIRI(concept));
        }
        return c;
    }
    
    public static Concept getConcept(String concept){
        return (Concept) locatorManager.lookup(createIRI(concept), Concept.class);
    }
    
    public static Type createType(String type){
        if (dataTypeNames.contains(type))
            return dataFactory.createWsmlDataType(type);
        return createConcept(type);
    }
    
    public static WsmlDataType createDataType(String type){
        if (dataTypeNames.contains(type))
            return dataFactory.createWsmlDataType(type);
        return null;
    }
    
    public static Attribute createAttribute(IRI attribute, Concept owner){
        
        try {
        	Attribute result = owner.createAttribute(attribute);
        	result.setConstraining(true);
            return result;
        } catch (InvalidModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }  
    
    public static Attribute createAttribute(String attribute, Concept owner){
      return createAttribute(createIRI(attribute), owner);
    }  
    
    public static Attribute getAttribute(IRI attributeIRI, Concept owner){
        try {
        	Attribute result = owner.createAttribute(attributeIRI);
        	result.setConstraining(true);
            return result;
            //return wsmoFactory.getAttribute(attributeIRI);
        } catch (InvalidModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public static Attribute getAttribute(String attributeID, Concept owner){
        return getAttribute(createIRI(attributeID), owner);
    }
    
    
    public static Instance createInstance(String instance){
        return wsmoFactory.createInstance(createIRI(instance));
    }
    
    public static Instance createInstance(String instance, Concept owner){
        try {
            return wsmoFactory.createInstance(createIRI(instance), owner);
        } catch (SynchronisationException e) {
            // TODO Auto-generated catch block
            return createInstance(instance);
        } catch (InvalidModelException e) {
            // TODO Auto-generated catch block
            return createInstance(instance);
        }
    }
    

    public static IRI createIRI(String fullIRI){
        return wsmoFactory.createIRI(fullIRI);
    }
    
    public static Ontology createOntology(String ontology){
        return wsmoFactory.createOntology(createIRI(ontology));
        
    }
    
    public static Namespace createNameSpace(String s){
        return wsmoFactory.createNamespace(null, createIRI(s));
    }
    
    public static String getRangesAsString(Attribute attr){
        String result = "";
        Iterator it = attr.listTypes().iterator();
        while (it.hasNext()){
            Type crtType = (Type)it.next();
            result = result + getTypeName(crtType);
        }
        return result;
    }
    
    public static Set listAtributesForRangeConcepts(Attribute attr){
        Set result = new LinkedHashSet();
        Iterator it = attr.listTypes().iterator();
        while (it.hasNext()){
            Type crtType = (Type)it.next();
            if (crtType instanceof Concept)
                result.addAll(WSMOUtil.listAllAttributes((Concept)crtType));
        }
        return result;
    }
    public static Type getRandomRangeType(Attribute attr){
        Iterator <Type> it = attr.listTypes().iterator();
        if (it.hasNext()){
            return it.next();
        }
        else{
            System.out.println("This shoudl never happen, but the attribute "+ attr + " has no types!!!");
        }
        return null;
    }
    
    public static Identifier getIdentifier(Value value){
        if (value instanceof Instance)
            return ((Instance)value).getIdentifier();
        if (value instanceof DataValue)
            return wsmoFactory.createIRI(/*((DataValue)value).getType().getIRI().toString() + */"http://" + ((DataValue)value).getValue().toString());
        return null;
    }
    
    public static List<Concept> listSuperConcepts(Concept theConcept) {
        List <Concept> sconcepts = new ArrayList <Concept>();
        Iterator it = theConcept.listSuperConcepts().iterator();
        while (it.hasNext()){
            Concept sc = (Concept)it.next();
            sconcepts.add(sc);
            sconcepts.addAll(listSuperConcepts(sc));
        }        
        return sconcepts;
    }
    
    public static Set<Attribute> listAncestorsAttributes(Concept concept){
        Set<Attribute> result = new HashSet<Attribute>();
        Iterator<Concept> it = listSuperConcepts(concept).iterator();
        while (it.hasNext()){
            result = Util.addAllIfNotExist(result, it.next().listAttributes());
        }
        return result;
    }
    
    public static Set<Attribute> listAllAttributes(Concept concept){
        Set<Attribute> result = new HashSet<Attribute>();
        Iterator<Concept> it = listSuperConcepts(concept).iterator();
        while (it.hasNext()){
            result = Util.addAllIfNotExist(result, it.next().listAttributes());
        }
        result = Util.addAllIfNotExist(result, concept.listAttributes());
        return result;
    }
    
    public static Set<Type> getSubRanges(Attribute attr){
    	Set<Type> result = new HashSet<Type>();
    	result.addAll(attr.listTypes());
    	for (Type t : attr.listTypes()){
    		if (t instanceof Concept){
    			result.addAll(((Concept)t).listSubConcepts());
    		}
    	}
    	return result;
    }
    
    public static boolean isAllowedRangeOfAttribute(Attribute theAttribute, Concept theConcept){
        Set <Type> types = theAttribute.listTypes();
        List <Concept> superConcepts = getSuperConcept(theConcept);
        for (Concept concept : superConcepts){
            if (types.contains(concept)){
                return true;
            }
        }
        return false;
    }

    private static List<Concept> getSuperConcept(Concept theConcept) {
        List <Concept> result = new ArrayList <Concept>();
        result.add(theConcept);
        for (Concept superConcept : theConcept.listSuperConcepts()){
            result.addAll(getSuperConcept(superConcept));
        }
        return result;
    }

    public static Set <Concept> getSubConcepts(Concept theConcept) {
        Set <Concept> result = new HashSet <Concept> ();
        for (Concept subConcept : theConcept.listSubConcepts()){
            result.add(subConcept);
            result.addAll(getSubConcepts(subConcept));
        }
        return result;
    }

    public static Set <Concept> getSuperConcepts(Concept theConcept) {
        Set <Concept> result = new HashSet <Concept> ();
        for (Concept superConcept : theConcept.listSuperConcepts()){
            result.add(superConcept);
            result.addAll(getSuperConcepts(superConcept));
        }
        return result;
    }
    
    public static String generateUniqueID() {
    	return (new Date()).hashCode() + "_" + getUniqueIdCounter();
    }
    
    protected static synchronized long getUniqueIdCounter() {
    	return uniqueIDCounter++;
    }
}
