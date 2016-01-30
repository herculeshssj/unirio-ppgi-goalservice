/*
 * Copyright (c) 2005-2007 National University of Ireland, Galway
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
 */

package ie.deri.wsmx.commons;

import ie.deri.wsmx.scheduler.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.io.parser.wsml.ParserImpl;
import org.deri.wsmo4j.io.serializer.wsml.SerializerImpl;
import org.omwg.mediation.language.objectmodel.api.MappingDocument;
import org.omwg.mediation.language.objectmodel.api.OntologyId;
import org.omwg.ontology.ComplexDataValue;
import org.omwg.ontology.Concept;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Value;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.mediator.Mediator;
import org.wsmo.mediator.OOMediator;
import org.wsmo.service.Goal;
import org.wsmo.service.Interface;
import org.wsmo.service.ServiceDescription;
import org.wsmo.service.WebService;
import org.wsmo.service.choreography.Choreography;
import org.wsmo.service.signature.StateSignature;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;
import org.wsmo.wsml.Serializer;

import com.isoco.wsmx.monitor.Message;
import com.isoco.wsmx.monitor.WSMXvisualization;

/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 2006-02-07
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/commons/Helper.java,v $, * @version $Revision: 1.10 $ $Date: 2007-09-24 20:09:23 $
 */
public class Helper {

    static Logger logger = Logger.getLogger(Helper.class);
    static private Map<String, Object> attributes;
    static Parser wsmlParser = new ParserImpl(new HashMap<String, Object>());
    static WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap<String, Object>());
        
    final static Serializer wsmlSerializer = new SerializerImpl(new HashMap());
    final static Map<String, Ontology> ontologies= new HashMap<String, Ontology>();
    final static Map<String, Goal> goals= new HashMap<String, Goal>();
    final static Map<String, WebService> webServices= new HashMap<String, WebService>();
    final static Map<String, Mediator> mediators= new HashMap<String, Mediator>();
    final static Map<String, TopEntity> topEntities = new HashMap<String, TopEntity>();
    final static Map<String, TopEntity[]> namespaceTopEntities = new HashMap<String, TopEntity[]>();
    
    // (sourceOntologyIRI,targetOntologyIRI) -> mapping document IRI
    final static Map<Pair<IRI, IRI>, org.omwg.mediation.language.objectmodel.api.IRI> 
		mappingDocumentIRIs = new HashMap<Pair<IRI, IRI>, org.omwg.mediation.language.objectmodel.api.IRI>();
    // mapping document IRI -> loaded mapping document
    final static Map<org.omwg.mediation.language.objectmodel.api.IRI, MappingDocument> 
		mappingDocuments = new HashMap<org.omwg.mediation.language.objectmodel.api.IRI, MappingDocument>();
    
    static private Random randomGenarator = new Random(System.currentTimeMillis());

    static private WSMXvisualization wsmxVisualizer = null; // = WSMXvisualization.getInstance();
    static private WSMXActiveMQPublisher wsmxActiveMQPublisher = null;
    
	public final static int FILTER_INCOMING = 3;
	public final static int FILTER_OUTGOING = 1;
	public final static int FILTER_DISCOVERY = 2;
	public final static int FILTER_CHOREOGRAPHY = 0;
	private static long counter = 0;
	
	public final static String SUPPORTED_MAPPING_FORMALISM_IRI = "http://www.wsmo.org/wsml";
	public final static IRI OOMEDIATOR_NFP_ROOTCONCEPTS_IRI = wsmoFactory.createIRI("http://www.wsmo.org/datamediation/rootConcepts"); 

	public static void setAttribute(String key, Object value){
		if (attributes == null)
			attributes = new HashMap<String, Object>();
		attributes.put(key, value);	
	}

	public static Object getAttribute(String key){
		if (attributes == null)
			return null;
		return attributes.get(key);	
	}

	
	static public void cleanUp(){
		if (wsmxActiveMQPublisher != null) {
			wsmxActiveMQPublisher.stop();
			wsmxActiveMQPublisher = null;
		}
	}
	
    
	//returns possitive random long number
	static public synchronized long getRandomLong(){
    	
    	long i = System.currentTimeMillis();
    	try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	randomGenarator.setSeed(i);
    	return Math.abs(randomGenarator.nextLong()+(counter++));
    }
  
    static public synchronized void visualizerLog(int FilterID, String msg){
    	String visulizeStr = Environment.getConfiguration().getProperty("wsmx.visualizer");
    	if ( (visulizeStr!=null) && visulizeStr.toLowerCase().equals("true")) {
    		if (wsmxVisualizer == null){
        		//initialize
        		wsmxVisualizer = WSMXvisualization.getInstance();	
    		}
    		Message visMsg = new Message(FilterID,msg);
    		wsmxVisualizer.showMessage(visMsg);
    	}
    }
    
    static public synchronized void generateMonitoringEvent(Instance instance){
    	String monitoringStr = Environment.getConfiguration().getProperty("wsmx.monitoring");
    	if ( (monitoringStr!=null) && monitoringStr.toLowerCase().equals("true")) {
    		if (wsmxActiveMQPublisher == null){
        		//initialize for the first time
    			try {
    				wsmxActiveMQPublisher = new WSMXActiveMQPublisher();
    			} catch (Exception e){
    				return;
    			}
    		}
    		wsmxActiveMQPublisher.publish(instance);
    	}
    }

    static public boolean isMemberOf(Instance instance, IRI conceptID){
    	String strIRI = conceptID.toString();
    	
    	Set<Concept> concepts = instance.listConcepts();
    	for (Concept c: concepts){
    		if (c.getIdentifier().toString().equals(strIRI))
    			return true;
    	}
    	return false;
    }
    
    static public Instance getInstanceAttribute(Instance instance, IRI attributeIRI, IRI attributeOfConceptIRI){
    	if (instance == null)
    		return null;
    	
		Set <Value>values = instance.listAttributeValues(attributeIRI);
  		for (Value value: values)
  		{
  			if( (value instanceof Instance) && ( isMemberOf( (Instance)value, attributeOfConceptIRI)))  {
  				return (Instance) value;
  			}
  		}
  		return null;
    }
    
    static public String getAttribute(Instance instance, IRI attributeIRI){
		Set <Value>values = instance.listAttributeValues(attributeIRI);
  		for (Value value: values)
  		{
  				return value.toString();
  		}
  		return "";
    }
    
    static public String getAttribute(Instance instance, String attributeStr){
    	return getAttribute(instance, wsmoFactory.createIRI(attributeStr));
    }
    
    static public String getInstanceAttribute(Instance instance, IRI attributeIRI){
    	if (instance == null)
    		return "";
    	Set <Value>values = instance.listAttributeValues(attributeIRI);
  		for (Value value: values)
  		{
//  			logger.debug(value.getClass());
  			if( value instanceof Instance) {
  				//return local name of the concept
				String identifier = ((Instance)value).getIdentifier().toString(); 
	  			return identifier.substring(identifier.indexOf('#')+1);
  			} else if (value instanceof ComplexDataValue){
	  			ComplexDataValue cData = (ComplexDataValue) value;
//	  			cData.getType()	
	  			return cData.toString();
	  		} else {
  				return value.toString();
  			}  			
  		}
  		return "";
    }
    
    static public String getEntitiesAsString(List<Entity> entities) {
    	String str = "";
    	for (Entity entity : entities)
    		str += getEntityAsString(entity);
    	return str;
    }
    
    static public String getEntityAsString(Entity e) {
    	String str = "";
    	
        if (e instanceof Instance) {
			Instance i = (Instance) e;
			str += ((IRI)i.getIdentifier()).getLocalName();
			Set<Concept> concepts = i.listConcepts(); 
			if (concepts.size()==1){
				str += " memberOf ";
				Concept c = (Concept) concepts.toArray()[0];
				str += ((IRI) c.getIdentifier()).getLocalName();
			} else if (concepts.size() > 1) {
				str += " memberOf {";
				Object[] cs = (Object[]) concepts.toArray();
				for (int a = 0; a < cs.length-1; a++){
					if (cs[a] instanceof Concept) 
						str+= ((IRI)((Concept)cs[a]).getIdentifier()).getLocalName() + " ,";
				}
				if (cs[cs.length-1] instanceof Concept)
					str += ((IRI)((Concept)cs[cs.length-1]).getIdentifier()).getLocalName() + "}";
			}
			
			str+="\n";
			
			Map attributes = i.listAttributeValues();
			for (Entry entry : (Set<Entry>) attributes.entrySet() ){
				Object o = entry.getValue();
				str+= "   " + ((IRI)entry.getKey()).getLocalName() + " hasValue "; 
				
				Set values = (Set) entry.getValue();
				
				for (Object value : values){
					if (value instanceof Instance)
						str += ((IRI) ((Instance)value).getIdentifier() ).getLocalName() + " ";
					else 
						str += value.toString() + " ";
				}
				str+="\n";
			}
		}
        return str;
}

    
	//returns all instances from topEnities
    static public List<Entity> getInstances(TopEntity[] entities) {
    	logger.debug("Number of top entities: " + entities.length);

		List<Entity> theList = new Vector<Entity>();

		Ontology onto = null;
		//find Ontology TopEntity
		for (int i=0; i < entities.length; i++) {
			if (entities[i] instanceof Ontology){
				onto = (Ontology) entities[i];
				Set<Instance> instances = onto.listInstances();
				for (Instance inst : instances){
					theList.add(inst);
					logger.debug("Recognized instance URI: "+ inst.getIdentifier());
				}
			}
		}
			
		return theList;
	}

    //wsmo4j $proxy workaround
    static public TopEntity[] parse(Reader reader){
    	TopEntity[] topEnts = {};
    	
		try	{
			topEnts = wsmlParser.parse(reader);
		} catch (Exception e) {
			logger.warn("Failed to parse " + reader.toString());
			logger.debug(e.getMessage());
		}
		
		//get ontology references
		storeEntities(topEnts);
		return topEnts;
    }
    
    //wsmo4j $proxy workaround
    static public TopEntity[] parse(StringBuffer buffer){
    	TopEntity[] topEnts = {};
    	
		try	{
			topEnts = wsmlParser.parse(buffer);
		} catch (Exception e) {
			logger.warn("Failed to parse " + buffer.toString());
		}		

		storeEntities(topEnts);
		return topEnts;
    }

    //wsmo4j $proxy workaround
    static public TopEntity[] parse(File file){
    	TopEntity[] topEnts = {};
    	
		try	{
			topEnts = wsmlParser.parse(new FileReader(file));
		} catch (ParserException e) {
			logger.warn("Failed to parse " + file.getAbsolutePath() + ".", e);
			e.printStackTrace();
		} catch (InvalidModelException e) {
			logger.warn("Failed to parse " + file.getAbsolutePath() + ": Invalid model.", e);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			logger.warn("Failed to load " + file.getAbsolutePath() + ": File doesn't exist anymore.", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.warn("Failed to load " + file.getAbsolutePath() + ": I/O failure. Check permissions.", e);
			e.printStackTrace();
		} catch (Throwable t) {
			logger.warn("Failed to parse " + file.getAbsolutePath() + ". Skipping this file.");
			t.printStackTrace();
		}
		
		storeEntities(topEnts);
		return topEnts;
    }
    
    synchronized static private void storeEntities(TopEntity[] topEnts){
    	if (topEnts==null || topEnts.length==0)
    		return;
    	
    	String namespace = "", defNamespace = "";
    	
		namespace = ((IRI)topEnts[0].getIdentifier()).getNamespace();
		
		if (topEnts[0].getDefaultNamespace()!=null) {
			defNamespace = topEnts[0].getDefaultNamespace().getIRI().toString();
			if(namespace == null || (!namespace.equals(defNamespace)))
				namespace = defNamespace;
		}
		
		namespaceTopEntities.put(namespace, topEnts); 
    	
    	for (TopEntity tEntity : topEnts){
    		String iri = tEntity.getIdentifier().toString();
    		topEntities.put(iri, tEntity);
    		
    		if (tEntity instanceof Ontology){
    			ontologies.put(iri, (Ontology) tEntity);
    		} 
    		else if (tEntity instanceof Goal){
    			goals.put(iri, (Goal) tEntity);
    		}
    		else if (tEntity instanceof WebService){
    			webServices.put(iri, (WebService) tEntity);
    		} 
    		else if (tEntity instanceof Mediator) {
    			mediators.put(iri, (Mediator) tEntity);
    		}
    	}
    }

    public static TopEntity getTopEntity (String iriStr){
    	TopEntity t = topEntities.get(iriStr); 
    	return t;
    }
    
    public static Ontology getOntology (String iriStr){
    	Ontology o = ontologies.get(iriStr); 
    	return o;
    }
    
    public static Ontology getOntology (IRI iri){
    	Ontology o = ontologies.get(iri.toString()); 
    	return o;
    }
    
    
    static public List<Entity> getInstances(Ontology onto) {
		List<Entity> theList = new Vector<Entity>();

		//if no ontology has been found return empty list
		if (onto == null)
			return theList;

		Set<Instance> instances = onto.listInstances();
		for (Instance inst : instances){
			theList.add(inst);
			logger.debug("Recognized instance URI: "+ inst.getIdentifier());
		}

		return theList;
	}
    
    static public List<Entity> getInstancesOfConcept(List<Entity> instances, String fullID ) {
    	List<Entity> respEnt = new ArrayList<Entity>();
    	
    	for (Entity ent: instances){
    		Set<Concept> concepts = ((Instance)ent).listConcepts();    		
    		
    		for (Concept c : concepts){
    			if (c.getIdentifier().toString().equalsIgnoreCase(fullID))
    					respEnt.add(ent);
    		}
    	}
    	return respEnt;
    }
    
    //TODO provide this method for other top entities
    //removes given all elements (e.g. concepts, instances) of given TopEntity 
    static public void removeTopEntity (TopEntity topEntity){
    	try {
			if (topEntity instanceof Ontology) {
				Ontology onto = (Ontology) topEntity;

				List<Entity> instances = getInstances(onto);

				for (Entity i : instances) {
					Map attr = ((Instance) i).listAttributeValues();
					Set<IRI> keys = attr.keySet();

					for (IRI iri : keys) {
						Object values = attr.get(iri);
						((Instance) i).removeAttributeValues(iri);
					}
				}
				onto.removeOntology(onto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
     }

    public static HashMap<String, Object> getOntologies(TopEntity[] topEntity) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        //if no ontology has been found return empty list
        if (topEntity == null)
            return map;

        for (int i = 0; i < topEntity.length; i++) {
            if (topEntity[i] instanceof Ontology)
                map.put(topEntity[i].getIdentifier().toString(), topEntity[i]);
        };

        return map;    
    }

    public static HashMap<String, Object> getWebServices(TopEntity[] topEntity) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        //if no ontology has been found return empty list
        if (topEntity == null)
            return map;

        for (int i = 0; i < topEntity.length; i++) {
            if (topEntity[i] instanceof WebService)
                map.put(topEntity[i].getIdentifier().toString(), topEntity[i]);
        };

        return map;   
    }
    
    public static Set<WebService> getWebServices(Namespace ns){
    	Set<WebService> result = new HashSet<WebService>();
    	
    	TopEntity[] topEnts = namespaceTopEntities.get(ns.getIRI().toString());
    	for (TopEntity t : topEnts) {
    		if (t instanceof WebService)
    			result.add((WebService)t);
    	}
    	return result;
    }
    
    public static HashMap<String, Object> getGoals(TopEntity[] topEntity) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        //if no ontology has been found return empty list
        if (topEntity == null)
            return map;

        for (int i = 0; i < topEntity.length; i++) {
            if (topEntity[i] instanceof Goal)
                map.put(topEntity[i].getIdentifier().toString(), topEntity[i]);
        };

        return map;   
    }

    public static Map<String, TopEntity[]> getAllNamespaceTopEntities() {
    	Map<String, TopEntity[]> localMap = new HashMap<String, TopEntity[]>();
    	localMap.putAll(namespaceTopEntities);
        return localMap;
    }
    
    public static Goal getGoal (IRI iri){
    	Goal g = goals.get(iri.toString()); 
    	return g;
    }

    public static Set<Ontology> getOntologySet(TopEntity[] topEntity) {
        Set<Ontology> set = new HashSet<Ontology>();

        //if no ontology has been found return empty set
         for (int i = 0; i < topEntity.length; i++) {
            if (topEntity[i] instanceof Ontology)
                set.add((Ontology)topEntity[i]);
        }
        return set;   
    }
    
    public static Set<Ontology> getAllOntologies() {
        return new HashSet<Ontology>(ontologies.values());   
    }
    
    public static Set<WebService> getAllWebServices() {
        return new HashSet<WebService>(webServices.values());   
    }
    
    public static Set<Goal> getAllGoals() {
        return new HashSet<Goal>(goals.values());   
    }
    
    public static Set<Mediator> getAllMediators() {
    	return new HashSet<Mediator>(mediators.values());
    }


    public static Set<Goal> getGoalSet(TopEntity[] topEntity) {
        Set<Goal> set = new HashSet<Goal>();

        //if no ontology has been found return empty set
         for (int i = 0; i < topEntity.length; i++) {
            if (topEntity[i] instanceof Goal)
                set.add((Goal)topEntity[i]);
        }
        return set;   
    }

    public static Set<WebService> getWebServiceSet(TopEntity[] topEntity) {
        Set<WebService> set = new HashSet<WebService>();

        //if no ontology has been found return empty set
         for (int i = 0; i < topEntity.length; i++) {
            if (topEntity[i] instanceof WebService)
                set.add((WebService)topEntity[i]);
        }
        return set;   
    }
    
    public static String[] serializeTopEntities(Set topEntities) {
    	String[] response = new String[topEntities.size()];
    	int i=0;
    	for (Object topEntity : topEntities){
    		StringBuffer sb = new StringBuffer();
    		if (topEntity instanceof TopEntity) {
        		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)topEntity}, sb);
        		response[i++] = sb.toString();
    		}
		}
    	return response;
    }
    
    public static String serializeTopEntity(TopEntity topEntity) {
    	String response = "";
    	if (topEntity == null)
    		return response;
    	StringBuffer sb = new StringBuffer();
		wsmlSerializer.serialize(new TopEntity[]{(TopEntity)topEntity}, sb);
		response = sb.toString();
		return response;
	}
    
    public static String[] serializeIdentifiers(Set<Identifier> identifiers) {
    	String[] response = new String[identifiers.size()];
    	int i=0;
    	for (Object identifier : identifiers){
    		response[i++] = ((Identifier)identifier).toString();
    	}
    	return response;
    }

    
    public static String[] serializeNamespaces(Set namespaces) {
    	String[] response = new String[namespaces.size()];
    	int i=0;
    	for (Object namespace : namespaces){
    		response[i++] = ((Namespace)namespace).getIRI().toString();
    	}
    	return response;
    }
    
    /**
     * @param instances A set of entities.
     * @return a nicer string representation of the given set
     */
    public static String printSetFull(Set instances){
    	String toReturn = "";
    	if(instances != null) {
    		for (Iterator iter = instances.iterator(); iter.hasNext();) {
				Entity entity = (Entity) iter.next();
				if (entity instanceof Instance)
					toReturn += printInstance((Instance)entity) + "\n ";
				else 
					toReturn += entity.getIdentifier() + "\n";
			}
    	} else {
    		toReturn += "empty";
    	}
    	return toReturn;
    }
    
    public static String printSetShort(Set instances){
    	String toReturn = "";
    	if(instances != null) {
    		for (Iterator iter = instances.iterator(); iter.hasNext();) {
				Entity entity = (Entity) iter.next();
				if (entity instanceof Instance)
					toReturn += printInstanceShort((Instance)entity) + "\n ";
				else 
					toReturn += entity.getIdentifier() + "\n";
			}
    	} else {
    		toReturn += "empty";
    	}
    	return toReturn;
    }

	
    private static String printInstanceShort(Instance instance){
		String resp = "";
		
		if (instance == null)
			return resp;
		
		resp+=("[Name = " + printIdentifier(instance.getIdentifier())+"\n");
    	
    	for (Entry<Identifier, Set<Value>> a :  instance.listAttributeValues().entrySet()){
    		resp+=("   " + printIdentifier(a.getKey()));
    		Set <Value> values = a.getValue();
    		if (values.size() == 1){
    			resp+=(" hasValue ");
    		}
    		else{
    			resp+=(" hasValues {");
    		}
    		
    		for (Value v : values){
    			if (v instanceof Instance){
    				resp+=((String)printInstance( (Instance) v)).replaceAll("\n", "")+"\n";
    			}
    			else{
    				resp+=v.toString()+"\n";
    			}
    		}
    		
    		if (values.size() > 1){
    			resp+=("}");
    		}
    	}
    	resp+="]";
    	return resp;
    }
		
    
	public static String printInstance(Instance instance){
		String resp = "";
		
		if (instance == null)
			return resp;
		
    	resp += printIdentifier(instance.getIdentifier()) + " memberOf ";
    	if (instance.listConcepts()!= null && instance.listConcepts().size()>0) {
    		resp += printIdentifier(instance.listConcepts().iterator().next().getIdentifier());
    	}
    	resp += "\n";
    	
    	List<Instance> toPrintOut = new ArrayList<Instance>(); 
    	for (Entry<Identifier, Set<Value>> a :  instance.listAttributeValues().entrySet()){
    		resp += "  " + printIdentifier(a.getKey());
    		Set <Value> values = a.getValue();
    		if (values.size() == 1){
    			resp += " hasValue ";
    		}
    		else{
    			resp += " hasValues {";
    		}
    		
    		for (Value v : values){
    			if (v instanceof Instance){
    				resp += printIdentifier(((Instance) v).getIdentifier());
    				toPrintOut.add((Instance) v);
    			}
    			else
    				resp += v.toString();
    		}
    		if (values.size() > 1)
    			resp +="}";

    		resp += "\n";
    	}
    	for (Instance inst: toPrintOut){
    		resp += "\n" + printInstance(inst);
    	}
    	
    	return resp;
    }
	
	static private String printIdentifier(Identifier identifier) {
		if (identifier instanceof IRI){
			return ((IRI) identifier).getLocalName();
		}
		return identifier.toString();
	}

	/**
     * @param instances A set of entities.
     * @return a nicer string representation of the given set
     */
    public static String printMap(Map instances){
    	String toReturn = "[ ";
    	if(instances != null) {
    		Set set = instances.entrySet();
    		for (Iterator iter = set.iterator(); iter.hasNext();) {
				Entity entity = (Entity) iter.next();
				toReturn += entity + ", ";
			}
    	}
    	toReturn += " ]";
    	return toReturn;
    }
    
    public static Ontology createOntology(List<Entity> input){
    	Ontology resp = wsmoFactory.createOntology(wsmoFactory.createIRI("http://www.wsmo.org/ontologies/resp"+getRandomLong()));

    	//namespaces, q0, q1, ... qn
    	Set<String> namespaces = new HashSet<String>(); 
    	
    	int counter = 0; 
    	for (Entity e : input){
    		if (e instanceof Instance) {
				Instance i = (Instance) e;
				Ontology o = i.getOntology();
				if(o != null){
					Namespace ns = o.getDefaultNamespace();
					if (ns!=null && ns.getIRI()!=null && !namespaces.contains(ns.getIRI().toString())) {
						String nsPrefix = ns.getPrefix();
						if (nsPrefix==null || nsPrefix.equals("") || nsPrefix.equals("_")){
							nsPrefix = "q"+counter++;
						}
						namespaces.add(ns.getIRI().toString());
						resp.addNamespace(wsmoFactory.createNamespace(nsPrefix, ns.getIRI()));    				
					}
					
					
				}
				try {
					resp.addInstance(i);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Set<Concept> concepts = i.listConcepts();
				for (Concept c : concepts){
					Namespace ns2 = null;
					if (c.getOntology() !=null)
						ns2 = c.getOntology().getDefaultNamespace();
					if (ns2!=null && ns2.getIRI()!=null && !namespaces.contains(ns2.getIRI().toString())) {
						String nsPrefix2 = ns2.getPrefix();
						if (nsPrefix2==null || nsPrefix2.equals("") || nsPrefix2.equals("_")){
							nsPrefix2 = "q"+counter++;
						}
						namespaces.add(ns2.getIRI().toString());
						resp.addNamespace(wsmoFactory.createNamespace(nsPrefix2, ns2.getIRI()));    				
					}
				}
    		}
    	}
    	return resp;
    }
    
    public static boolean containsOOMediator(IRI sourceOntologyIRI, IRI targetOntologyIRI) throws ComponentException, UnsupportedOperationException {
		return (retrieveOOMediator(sourceOntologyIRI, targetOntologyIRI) != null);
	}
    
    public static OOMediator retrieveOOMediator(IRI sourceOntologyIRI, IRI targetOntologyIRI) throws ComponentException, UnsupportedOperationException {
		for (Mediator m : mediators.values()) {
			if (m instanceof OOMediator) {
				OOMediator oom = (OOMediator) m;
				if (oom.getTarget().equals(targetOntologyIRI) && oom.listSources().contains(sourceOntologyIRI)) {
					return oom;
				}
			}
		}
		return null;
	}
    
    
    /**
     * Checks whether the given mapping document 
     *   is not null, has an id, 
     *   and whether source and target ontology are in a supported formalism. 
     * 
     * @param mapdoc
     * @param supportedFormalismIRI
     * @return 
     */
    public static boolean isValidMapping(MappingDocument mapdoc, String supportedFormalismIRI) {
    	if (mapdoc == null ) {
    		return false;
    	} else {
	    	OntologyId sourceOID = mapdoc.getSource();
			OntologyId targetOID = mapdoc.getTarget();
	    	return (   (mapdoc.getId() != null)
	    			&& (sourceOID != null) 
	    			&& (targetOID != null)    			
	    			&& (sourceOID.getFormalismUri() != null)
	    			&& (targetOID.getFormalismUri() != null)
	    			&& (sourceOID.getFormalismUri().toString().equals(supportedFormalismIRI))
	    			&& (targetOID.getFormalismUri().toString().equals(supportedFormalismIRI))
	    	);
    	}
    }
    
    /** @see org.wsmo.execution.common.component.resourcemanager.MappingResourceManager#storeMapping(MappingDocument) */
    public static void storeMapping(MappingDocument mapdoc) throws UnsupportedOperationException {	
		// Note that in this mapping storage implementation, the assumptions are that:
		// (1) mapping document ids must be unique (this should be self-evident) and
		// (2) there may be only one mapping specified for any given source and target ontology pair 
		if (mapdoc == null)
			throw new IllegalArgumentException("Failed to store mapping document (must not be null)!");
		try {			
			if (! Helper.isValidMapping(mapdoc, SUPPORTED_MAPPING_FORMALISM_IRI) ) {
				throw new UnsupportedOperationException(
						"Mapping document must have an id; "
						+ "and source and target ontology formalism must both be \""
						+ SUPPORTED_MAPPING_FORMALISM_IRI + "\".");
			}
			
			IRI srcOntoIRI = wsmoFactory.createIRI(mapdoc.getSource().getUri().plainText());
			IRI tgtOntoIRI = wsmoFactory.createIRI(mapdoc.getTarget().getUri().plainText());
			org.omwg.mediation.language.objectmodel.api.IRI mapdocIRI = mapdoc.getId();			
			Pair<IRI,IRI> ontoIRIs = new Pair<IRI,IRI>(srcOntoIRI, tgtOntoIRI);
			
			// check whether existing entry needs to be overwritten
			if (mappingDocumentIRIs.containsKey(ontoIRIs)) {
				logger.warn("Duplicate mapping found for source \"" + ontoIRIs.getFirst().toString() 
						+ "\" and target \"" + ontoIRIs.getSecond().toString() 
						+ "\", discarding previous entry!"); 
				mappingDocuments.remove(mappingDocumentIRIs.get(ontoIRIs));				
			} else if (mappingDocuments.containsKey(mapdocIRI)) {
				logger.warn("Two mapping files with the same identifier found: \"" + mapdocIRI.plainText() 
						+ "\", discarding previous entry!");
				mappingDocumentIRIs.values().remove(mapdocIRI);
			}
			
			// store
			mappingDocumentIRIs.put(ontoIRIs, mapdocIRI);
			mappingDocuments.put(mapdocIRI, mapdoc);
			
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("Failed to store mapping document with id=\"" 
				+ mapdoc.getId().plainText()
				+ "\" (it does not contain source and/or target ontology URI and/or formalism)!");
		}
    }
    
    /** @see org.wsmo.execution.common.component.resourcemanager.MappingResourceManager#removeMapping(org.omwg.mediation.language.objectmodel.api.IRI) */
    public static void removeMapping(org.omwg.mediation.language.objectmodel.api.IRI iri) {
		mappingDocuments.remove(iri);
		mappingDocumentIRIs.values().remove(iri);
	}
    
    /** @see org.wsmo.execution.common.component.resourcemanager.MappingResourceManager#retrieveMappings() */
    public static Collection<MappingDocument> getAllMappings() {
		return Collections.unmodifiableCollection(mappingDocuments.values());
	}
    
    /** @see org.wsmo.execution.common.component.resourcemanager.MappingResourceManager#retrieveMapping(org.omwg.mediation.language.objectmodel.api.IRI) */
    public static MappingDocument getMapping(org.omwg.mediation.language.objectmodel.api.IRI iri) {
		return mappingDocuments.get(iri);
	}
    
    /** @see org.wsmo.execution.common.component.resourcemanager.MappingResourceManager#retrieveMapping(IRI, IRI) */
    public static MappingDocument getMapping(IRI sourceOntologyIRI, IRI targetOntologyIRI) {
		if (containsMapping(sourceOntologyIRI, targetOntologyIRI)) {
			org.omwg.mediation.language.objectmodel.api.IRI mapdocIRI
				 = mappingDocumentIRIs.get(new Pair<IRI,IRI>(sourceOntologyIRI, targetOntologyIRI));
			return getMapping( mapdocIRI );
		} else {
			return null;
		}
	}
    
    /** @see org.wsmo.execution.common.component.resourcemanager.MappingResourceManager#containsMapping(org.omwg.mediation.language.objectmodel.api.IRI) */
	public static boolean containsMapping(org.omwg.mediation.language.objectmodel.api.IRI iri) {
		return mappingDocuments.containsKey(iri);
	}

	/** @see org.wsmo.execution.common.component.resourcemanager.MappingResourceManager#containsMapping(IRI, IRI) */
	public static boolean containsMapping(IRI sourceOntologyIRI, IRI targetOntologyIRI) {
		if ((sourceOntologyIRI != null) && (targetOntologyIRI != null)) {
			return mappingDocumentIRIs.containsKey(
				new Pair<IRI,IRI>(sourceOntologyIRI, targetOntologyIRI)
			);
		} else {
			return false;
		}
	}
	
	/**
     * Gets the ontologies imported in the choreography's state signature of the first interface
     * 
     * @param sd service description (i.e. goal or web service)
     * @return a copy of the set of ontologies, or null if state signature is missing
     */
    public static Set<Ontology> getOntologiesFromStateSignature(ServiceDescription sd) { 
    	StateSignature signature = getStateSignature(sd);
    	return (signature == null) ? null : signature.listOntologies();
    }
    
    public static StateSignature getStateSignature(ServiceDescription sd) {
    	try {
	    	Interface i = sd.listInterfaces().iterator().next();
	    	Choreography chor = (Choreography) i.getChoreography();	    	
	    	return chor.getStateSignature();
    	} catch (NullPointerException ex) {    		
    	} catch (NoSuchElementException ex) {    		
    	}
    	return null;
    }
    
    /**
     * Gets the root instances as input for data mediation.
     * Looks for a NFP in the given ooMediator and expects a list of concept IRIs; 
     * instances of any of those concepts are returned.
     * 
     * @param entity ooMediator where the NFP is looked up
     * @param data the data to filter
     * @return subset of data
     */
    public static Set<Entity> getMediationRootInstances(final OOMediator entity, final Set<Entity> data) {
    	Set<Entity> rootConcepts = new HashSet<Entity>();
    	Set<Object> conceptIRIStrings = entity.listNFPValues( OOMEDIATOR_NFP_ROOTCONCEPTS_IRI ); 
    	for (Object iriStr : conceptIRIStrings) {
    		rootConcepts.addAll(getInstancesOfConceptAsSet(data, iriStr.toString()));
    	}
    	return rootConcepts;
    }
    
    public static Set<Entity> getInstancesOfConceptAsSet(Set<Entity> instances, String fullID ) {
    	Set<Entity> result = new HashSet<Entity>();
    	for (Entity ent: instances){
    		Set<Concept> concepts = ((Instance)ent).listConcepts();    		
    		for (Concept c : concepts){
    			if (c.getIdentifier().toString().equalsIgnoreCase(fullID))
    				result.add(ent);
    		}
    	}
    	return result;
    }
    
    /**
     * serializes a set of instances
     * @param theInstances
     * @return 
     */
    public static String serialize(Set<Instance> theInstances){
    	StringBuffer sb = new StringBuffer();
    	serialize(theInstances, new HashSet<Instance>(), sb);
    	return sb.toString();
    }
    
    /**
     * Serializes a set of instances (recursively)
     * @param theInstances
     * @param theAlreadySerialized
     * @param result string buffer which the results are appended to
     */
    public static void serialize(Set <Instance> theInstances, Set <Instance> theAlreadySerialized, StringBuffer result) {
        List<StringBuffer> moreResults = new ArrayList<StringBuffer>();
        for (Instance crtInstance : theInstances){
            if (!theAlreadySerialized.contains(crtInstance)){
                theAlreadySerialized.add(crtInstance);
                result.append("instance " + crtInstance.getIdentifier() + "");
                if (crtInstance.listConcepts() != null && !crtInstance.listConcepts().isEmpty()){
                    result.append(" memberOf ");
                    Iterator<Concept> it = crtInstance.listConcepts().iterator();
                    while(it.hasNext()){
                        result.append(it.next().getIdentifier());
                        if (it.hasNext())
                            result.append(", ");
                    }
                }
                if (crtInstance.listAttributeValues() !=null && !crtInstance.listAttributeValues().isEmpty()){
                    Iterator<Identifier> it = crtInstance.listAttributeValues().keySet().iterator();
                    while (it.hasNext()){
                        Identifier crtAttribute = it.next();
                        Iterator<Value> valueIt = crtInstance.listAttributeValues(crtAttribute).iterator();
                        if (valueIt.hasNext())
                            result.append("\n\t" + crtAttribute +" hasValue ");//TODO Check the syntax
                        while (valueIt.hasNext()){
                            Value crtValue = valueIt.next();
                            if (crtValue instanceof Instance){
                                result.append(((Instance) crtValue).getIdentifier().toString());
                                StringBuffer addOns = new StringBuffer();
                                Set <Instance> arrayAddOns = new HashSet <Instance> ();
                                arrayAddOns.add((Instance) crtValue);
                                serialize(arrayAddOns, theAlreadySerialized, addOns);
                                moreResults.add(addOns);
                            }
                            else if (crtValue instanceof DataValue){
                                DataValue dv = (DataValue) crtValue;
                                Object o = dv.getValue();
                                if (o instanceof String){
                                    result.append("\"" + o + "\"");
                                }
                                else if (o instanceof Boolean){
                                    result.append("_boolean(\"" + Boolean.toString(((Boolean) o)) + "\")");
                                }
                                else{
                                    result.append(o.toString());
                                }
                            }
                            if (valueIt.hasNext())
                                result.append(", ");
                        }
                        if (!it.hasNext())
                            result.append("\n");
                    }
                    
                }
                result.append("\n");
            }
        }
        Iterator <StringBuffer> itSB = moreResults.iterator();
        while (itSB.hasNext()){
            result.append(itSB.next());
            result.append("\n");
        }
    }
    
    /**
     * Removes all instances in the collection from their ontology,
     * and all attribute values from the instances.
     * 
     * (Does not recursively go through the attributes, so only 
     *  the actually specified instances are regarded.)
     * 
     * @param instances
     * @throws SynchronisationException
     * @throws InvalidModelException
     */
    public static void cleanUpInstances(Collection<Entity> instances) 
    throws SynchronisationException, InvalidModelException {
    	if(instances != null) {
    		for (Entity e : instances) {
    			try {
    				logger.debug("Cleaning instance " + e.getIdentifier());
    				Instance i = (Instance) e;
    				Map<Identifier, Set<Value>> attr = i.listAttributeValues();			
    				Set<Identifier> keys = attr.keySet();			
    				
    				for (Identifier id : keys) {
    					i.removeAttributeValues((IRI)id);				
    				}
    				
    				Ontology instanceOnto = i.getOntology();
    				if (instanceOnto instanceof Ontology)
    					instanceOnto.removeInstance(i);
    			} catch (ClassCastException cce) {
    				// no worries if the entity was no instance
    			}
    		}
    	}
    }
    
	static public Instance clone(Instance theInstance, String theNamespace, Ontology theOntology, Map <IRI, Instance> iri2Cloned, Map<Instance, Instance> original2Cloned) 
	throws SynchronisationException, InvalidModelException {
		
		// create new IRI
		IRI cloneIRI = wsmoFactory.createIRI(
				theNamespace + "#"
				+ ((IRI)theInstance.getIdentifier()).getLocalName() + "_"
				+ theInstance.getIdentifier().toString().hashCode());
		
//		logger.debug("Fixing instance with IRI \"" + theInstance.getIdentifier().toString() + "\" to clone with IRI \"" + cloneIRI.toString()+ "\"");
		
        if (iri2Cloned.containsKey(cloneIRI)) {
            return iri2Cloned.get(cloneIRI); 
        }
        
        // create clone instance
        Instance clone = wsmoFactory.createInstance(cloneIRI);
        iri2Cloned.put(cloneIRI, clone);
        original2Cloned.put(theInstance, clone);
        
        // add concepts
        for (Concept concept : theInstance.listConcepts()) {
            Concept nonProxyConcept = wsmoFactory.getConcept(concept.getIdentifier());
            clone.addConcept(nonProxyConcept);
        }
        
        // add attributes
        for (Identifier attributeID : theInstance.listAttributeValues().keySet()) {
            for (Value value : theInstance.listAttributeValues(attributeID)) {
                if (value instanceof Instance ) {
                	clone.addAttributeValue(attributeID, clone((Instance) value, theNamespace, theOntology, iri2Cloned, original2Cloned));
                } else {
                    clone.addAttributeValue(attributeID, value);
                }
            }
        }
        // set ontology
        clone.setOntology(theOntology);
        theOntology.addInstance(clone);    
        
        return clone;
    }	
    
    
//    static private Instance copy(Instance instance) {
//		//FIXME this is a workaround for what should be clone() in wsmo4j
//		//does not use factory to ensure that a real copy is created
//   	
//		InstanceImpl instanceImpl = new InstanceImpl(instance.getIdentifier());
//		try {
//			for (Concept c : (Collection<Concept>)instance.listConcepts())
//				instanceImpl.addConcept(c);
//			Map map = instance.listAttributeValues();
//			
//		    Iterator it = map.entrySet().iterator();
//		    while (it.hasNext()) {
//		        Map.Entry pairs = (Map.Entry)it.next();
//		        Object key = pairs.getKey();
//		        Object values = pairs.getValue();
//		        Set valueSet = (Set) values;
//		        
//		        for (Object value : valueSet){
//			        if (value instanceof Instance){
//			        	Instance val = copy((Instance)value);
//			        	instanceImpl.addAttributeValue(wsmoFactory.createIRI(key.toString()),val);
//			        } else if (value instanceof DataValue){
//				        DataValue dv = (DataValue) value; 
//			        	instanceImpl.addAttributeValue(wsmoFactory.createIRI(key.toString()), dv);
//			        }
//		        }
//		    }
//		} catch (Exception e){
//			e.printStackTrace();
//		}
//		return instanceImpl;
//    }
}
