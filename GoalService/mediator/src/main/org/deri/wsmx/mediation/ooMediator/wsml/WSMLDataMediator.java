/*
 * Copyright (c) 2005 National University of Ireland, Galway
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

package org.deri.wsmx.mediation.ooMediator.wsml; 

import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deri.wsmx.mediation.ooMediator.Mediator;
import org.deri.wsmx.mediation.ooMediator.gui.loader.wsml.WSMLLoader;
import org.deri.wsmx.mediation.ooMediator.mapper.Mapper;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.AttributeId;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.ClassId;
import org.deri.wsmx.mediation.ooMediator.storage.Loader;
import org.deri.wsmx.mediation.ooMediator.util.Timer;
import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.mediation.language.objectmodel.api.rules.Class2Attribute;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Type;
import org.omwg.ontology.Value;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;
import org.wsmo.common.Namespace;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.execution.common.component.DataMediator;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.locator.LocatorManager;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;

/** 
 * Interface or class description
 * 
 * @author FirstName LastName, FirstName LastName
 *
 * Created on 15-May-2005
 * Committed by $Author: adrian.mocan $
 * 
 * $Source: /var/repository/wsmx-datamediator/src/main/java/org/deri/wsmx/mediation/ooMediator/wsml/WSMLDataMediator.java,v $, 
 * @version $Revision: 1.4 $ $Date: 2008/02/26 03:20:26 $
 */
@WSMXComponent(name   = "DataMediator",
 	   		   events = "DATAMEDIATOR")
public class WSMLDataMediator implements Mediator , DataMediator {
    
    private Pattern instanceIdPattern = Pattern.compile("^.*?\\('(.*?), (.*?)'\\)");
    
    private Loader storageLoader = null;;
    private Mapper mapper = null;
    private Set<Namespace> namespaces = null;

    private LocatorManager locator;
    private WsmoFactory wsmoFactory;

    private Timer lastRunTimer = null;

    private Map<String, Boolean> flags;
    
    /**
     * No-argument constructor for instantiation as WSMX component.
     * @throws RuntimeException when used outside of WSMX
     */
    @SuppressWarnings("unchecked")
	public WSMLDataMediator() {
    	WSMXFacade wsmxFacade = null;
    	try {
	    	Class wsmxFacadeClass = getClass().getClassLoader().
	    		loadClass("org.deri.wsmx.mediation.ooMediator.wsmx.WSMXFacadeImpl");	    	
			wsmxFacade = (WSMXFacade) wsmxFacadeClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("WSMX facade impl not available "
					+ "(use a different constructor for instantiation outside of WSMX)", e);
		}		
		this.flags = wsmxFacade.getMediationFlagsFromConfig();
		this.locator = Factory.getLocatorManager();
        this.wsmoFactory = Factory.createWsmoFactory(new HashMap <String, Object> ());
		this.storageLoader = wsmxFacade.getRMLoader(this);
	}
	
	public WSMLDataMediator(Loader storageLoader, Map <String, Boolean> theFlags){
		this.storageLoader = storageLoader;
        this.locator = Factory.getLocatorManager();
        this.wsmoFactory = Factory.createWsmoFactory(new HashMap <String, Object> ());
        this.flags = theFlags;
	}
    
    public Timer getLastRunTimer(){
        return lastRunTimer;
    }
    
	/** 
	 * @deprecated use #mediate(Ontology, Ontology, Identifiable) or  <br/> 
	 * #mediate(Ontology, Ontology, Set<Identifiable>) instead
	 * @see #createDataMediator()	 
	 * */
	public StringBuffer mediate(Ontology sourceOntologyID, Ontology targetOntologyID, StringBuffer payload) {
        
		StringBuffer result = new StringBuffer();

        Parser parser = Factory.createParser(null);
        
        Entity[] holders = null;
		try {
			holders = parser.parse(payload);
		} catch (ParserException e) {
			e.printStackTrace();
			result.append("instance 'Mediation failure: wrong input instances' memberOf Error");
			return result;
		} catch (InvalidModelException e) {
			e.printStackTrace();
			result.append("instance 'Mediation failure: wrong input instances' memberOf Error");
			return result;
		}
		
        //----------------------------------------------------------
        
		
		Set<Instance> sourceInstances = new HashSet<Instance>();
        Set<Namespace> sourceNameSpaces = new HashSet<Namespace>();
		int size = holders.length;
		for (int i=0; i<size; i++){
			if (holders[i] instanceof Instance)
				sourceInstances.add((Instance)holders[i]);
			else
				if (holders[i] instanceof Ontology){
					sourceInstances.addAll(((Ontology)holders[i]).listInstances());
                    sourceNameSpaces.addAll(((Ontology)holders[i]).listNamespaces());
                }
		}
        
        List<Entity> targetInstances = null;

        
        try {
			targetInstances = getMediatedInstances(sourceOntologyID, targetOntologyID, sourceInstances, sourceNameSpaces);
		} catch (UnsupportedOperationException e) {
			result.append("instance 'Mediation failure: UnsupportedOperationException: Only instances and ontologis can be currently processed' memberOf Error");
			//e.printStackTrace();
		} catch (ComponentException e) {		
			//e.printStackTrace();
			result.append("instance 'Mediation failure: ComponentException' memberOf Error");
		}
		
		if (targetInstances == null)
			return result;
		
		serialize(targetInstances, result);
		

		//----------------------------------------------------------

		
		//----------------------------------------------------------------------------------------------
		/*
		int size = holders.length;
		Set<Entity> anotherInput = new HashSet<Entity>(); 
		for (int i=0; i<size; i++){
			anotherInput.add(holders[i]);
		}
		
		Map<Entity, List<Entity>> anotherResult = null;
		
		try {
			anotherResult = mediate(sourceOntology, targetOntology, anotherInput);
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (anotherResult==null)
			return result;
		
		for(Entity key: anotherResult.keySet()){
			
			result.append("\n********************\n");
			serialize(Collections.singleton(key), result);
			result.append("\n>>>>\n");
			serialize(anotherResult.get(key), result);
		}
		*/
		
		//----------------------------------------------------------------------------------------------
		
		return result;
	}
 
	private void serialize(Collection<Entity> target, StringBuffer result){
        Entity[] array = new Entity[target.size()];
        Iterator<Entity> it = target.iterator();
        int index=0;
        while (it.hasNext()){
            array[index] = it.next();
            index++;
        }
        if (namespaces!=null)
        	WSMOUtil.serializer.serialize(array, result, namespaces);
        else
        	WSMOUtil.serializer.serialize(array, result);
        	
	}


	
	private String getNamespace(String input){
        if (input.endsWith("#"))
        	;
        else
        	if (input.indexOf("#")!=-1)
        		input = input.substring(0, input.indexOf("#") + 1);
        	else
        		input = input + 1;
		return input;
	}
	
	
	private List<Entity> getMediatedInstances(Ontology sourceOntology, Ontology targetOntology, Set<Instance> sourceInstances, Set<Namespace> sourceNameSpaces) throws ComponentException, UnsupportedOperationException{
        boolean printToConsole = shouldPrintToConsole();
        
	    Timer timer = new Timer("Total Mediation Process");
	    timer.start();
               
        List<Entity> result = new ArrayList<Entity>();

		if (sourceInstances.isEmpty())
			throw new UnsupportedOperationException("No Input Data Specified");
		
        Set<Concept> targetConcepts = null;
        Set<Concept> sourceConcepts = null;
        MediationReasoner mediationReasoner = new MediationWSML2Reasoner(flags, storageLoader.getOutputStream());
        WSMLRuleGenerator wrg = new WSMLRuleGenerator();
        try {
            Timer preparation = new Timer("Grounding mappings and cloning instances");
            preparation.start();
            mapper = getMapper(sourceOntology, targetOntology);
            
            String sourceNSString = ((IRI) mapper.getSrcOntology().getIdentifier()).toString();
            
            mediationReasoner.addNamespace(WSMOUtil.wsmoFactory.createNamespace("o1", WSMOUtil.wsmoFactory.createIRI(getNamespace(sourceNSString))));
            mediationReasoner.addNamespaces(sourceNameSpaces);
            
            String targetNSString = ((IRI) mapper.getTgtOntology().getIdentifier()).toString();
            
            mediationReasoner.addNamespace(WSMOUtil.wsmoFactory.createNamespace("o2", WSMOUtil.wsmoFactory.createIRI(getNamespace(targetNSString))));
                        
            mediationReasoner.addOntology(sourceOntology);
            mediationReasoner.addOntology(targetOntology);
            
            mediationReasoner.addSourceInstances(sourceInstances, mapper.getMappings());
            mediationReasoner.addAxioms(wrg.generateRules(mapper.getMappings(), mediationReasoner.getSourceInstances(), shouldFilterMappings()));
            
            if (sourceInstances == null || sourceInstances.isEmpty()){
                throw new UnsupportedOperationException("No Input Data Specified");
            }
            
            sourceConcepts = getSourceConcepts(sourceInstances);
            targetConcepts = getTargetConcepts(sourceConcepts);
            preparation.stop();
            if (printToConsole){
                storageLoader.getOutputStream().println(preparation);
            }
            
            Timer knowledgebase = new Timer("Creating Knowledgebase");
            knowledgebase.start();
            mediationReasoner.refreshKB();
            knowledgebase.stop();
            if (printToConsole){
                storageLoader.getOutputStream().println(knowledgebase);          
                storageLoader.getOutputStream().flush();
            }
            
        } catch (SynchronisationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InvalidModelException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
                
        
        List<Set<Instance>> instances=null;
        try {
            Timer querying = new Timer("Querying Knowledgebase and executing functoids");
            querying.start();
            instances = mediationReasoner.getInstances(targetConcepts, sourceConcepts);
            querying.stop();
            if (printToConsole){
                storageLoader.getOutputStream().println(querying);
            }
            
            Timer cleaning = new Timer("Cleaning Result Set");
            cleaning.start();
            instances = cleanUp(instances);
            cleaning.stop();
            if (printToConsole){
                storageLoader.getOutputStream().println(cleaning);
            }
        } catch (ParserException e2) {
            e2.printStackTrace();
        }
        Iterator<Set<Instance>> itSI = instances.iterator();
        while (itSI.hasNext()){
            result.addAll(itSI.next());
        }
        
        namespaces = mediationReasoner.getNamespaces();
        timer.stop();
        if (printToConsole){
            storageLoader.getOutputStream().println(timer);
            storageLoader.getOutputStream().flush();
        }
        this.lastRunTimer  = timer; 
        return result;				
	}

    private boolean shouldFilterMappings() {
        if (flags.containsKey(MediationFlags.FILTER_MAPPINGS_BASED_ON_INPUT)){
            return flags.get(MediationFlags.FILTER_MAPPINGS_BASED_ON_INPUT);
        }
        return false;
    }
    
    private boolean shouldPrintToConsole() {
        if (flags.containsKey(MediationFlags.PRINT_TIMING_INFORMATION_TO_CONSOLE)){
            return flags.get(MediationFlags.PRINT_TIMING_INFORMATION_TO_CONSOLE);
        }
        return false;
    }

    public List<Entity> mediate(Ontology sourceOntology, Ontology targetOntology, Entity data)
			throws ComponentException, UnsupportedOperationException {
		
		if (data==null || !storageLoader.containsMappings(sourceOntology, targetOntology)) 
			return new ArrayList<Entity>();
		
		Set<Instance> sourceInstances = new HashSet<Instance>();
        Set<Namespace> sourceNameSpaces = new HashSet<Namespace>();
		
		if (data instanceof Instance)
			sourceInstances.add((Instance)data);
		else
			if (data instanceof Ontology){
				sourceInstances.addAll(((Ontology)data).listInstances());
                sourceNameSpaces.addAll(((Ontology)data).listNamespaces());
			}
			else
				throw new UnsupportedOperationException("Wrong input data for data mediation - only instances and ontologis can be currently processed.");
        
		return getMediatedInstances(sourceOntology, targetOntology, sourceInstances, sourceNameSpaces);		
	}
	
	
	/** 
	 * @return 
	 * 
	 * @see org.wsmo.execution.common.component.DataMediator#mediate(org.omwg.ontology.Ontology, org.omwg.ontology.Ontology, java.util.Set)
	 */
	public Map<Entity, List<Entity>> mediate(Ontology sourceOntology, Ontology targetOntology, Set<Entity> data) throws ComponentException, UnsupportedOperationException {
		
		Map<Entity, List<Entity>> result = new HashMap<Entity, List<Entity>>();
			
		if (data==null || data.isEmpty() || (!storageLoader.containsMappings(sourceOntology, targetOntology))) 
			return result;
		
		Set<Instance> sourceInstances = new HashSet<Instance>();
        Set<Namespace> sourceNameSpaces = new HashSet<Namespace>();
		
        for (Entity dataElement : data){
			if (dataElement instanceof Instance)
				sourceInstances.add((Instance)dataElement);
			else
				if (dataElement instanceof Ontology){
					sourceInstances.addAll(((Ontology)dataElement).listInstances());
	                sourceNameSpaces.addAll(((Ontology)dataElement).listNamespaces());
				}								
        }
        if (sourceInstances.isEmpty()){
        	throw new UnsupportedOperationException("Wrong input data for data mediation - only instances and ontologies can be currently processed.");
        }
        
		List<Entity> targetInstances = getMediatedInstances(sourceOntology, targetOntology, sourceInstances, sourceNameSpaces);
		List<Entity> notAddedTargetInstances = new ArrayList<Entity>(targetInstances);		
		
		for (Instance aSourceInstance : sourceInstances){			
			List<Entity> targetInstancesList = new ArrayList<Entity>();						
			for (Entity aTarget : targetInstances){			
				Instance aTargetInstance = (Instance)aTarget;
				if (aTargetInstance.getIdentifier().toString().contains(aSourceInstance.getIdentifier().toString().replace('#', '/'))){
                    if (aTargetInstance.getIdentifier().toString().startsWith(WSMLRuleGenerator.function.toString() + "('" + aSourceInstance.getIdentifier().toString().replace('#', '/') + ",")){
                        targetInstancesList.add(aTargetInstance);
                        notAddedTargetInstances.remove(aTargetInstance);
                    }
                }
			}
			if (!targetInstancesList.isEmpty())
				result.put(aSourceInstance, targetInstancesList);
		}
		if (! notAddedTargetInstances.isEmpty())  {
			result.put((Entity)null, notAddedTargetInstances);
//			logger.debug("adding another " + notAddedTargetInstances.size() + " instances from " + targetInstances.size());
		}
		Map<Entity, List<Entity>> fixedResult = new HashMap<Entity, List<Entity>>();
		fixedResult = fixMediatedDataIRIs( result );
		//WSMOUtil.cleanUpOntology( result ); // clean mediated ontology for next run
		return fixedResult; 		
	}
	

    private Mapper getMapper(Ontology sourceOntology, Ontology targetOntology){
        if (!(storageLoader.containsOntology(sourceOntology)
        		&& storageLoader.containsOntology(targetOntology)))
        	throw new IllegalStateException("Source and/or target ontology not available in storage.");
        Mapper m = new Mapper();
        if (sourceOntology.listConcepts().isEmpty())
            m.setSrcOntology(storageLoader.loadOntology(sourceOntology));
        else
            m.setSrcOntology(new WSMLLoader(sourceOntology).getOntology());
        if (targetOntology.listConcepts().isEmpty())
                m.setTgtOntology(storageLoader.loadOntology(targetOntology));
        else
            m.setTgtOntology(new WSMLLoader(targetOntology).getOntology());
        m.setMappings(storageLoader.loadMappings(sourceOntology, targetOntology));
        //m.setCcMapper(dbLoader.loadConceptsMappings(sourceOntology, targetOntology));
        //m.setAaMapper(dbLoader.loadAttributesMappings(sourceOntology, targetOntology));
        mapper = m;
        return m;
    }
    
    private Set<Concept> getTargetConcepts(Concept sourceConcept){
        
        Set<Concept> result = new HashSet<Concept>();
        Concept sourceC = sourceConcept;
        if (sourceC == null || mapper == null)
            return null;
        Collection cTC = mapper.getTargetConcepts(sourceC);
        if (cTC==null)
            return null;
        Iterator<Type> targetsIt = cTC.iterator();
        Set<Concept> conceptsMappedThroughAttributes = getConceptsMappedThroughAttributes(sourceConcept);
        while (targetsIt.hasNext()){
        	Type ctrC = targetsIt.next();
            if (ctrC instanceof Concept && !conceptsMappedThroughAttributes.contains(ctrC)){
                result.add((Concept) ctrC);
            }
        }
       
        return result;
    }
    
    private Set<Concept> getConceptsMappedThroughAttributes(Concept c){
        Set<Concept> result = new HashSet<Concept>();
        Iterator<Class2Attribute> caMappingsIt = mapper.getMappings().getConceptAttributeMappings().iterator();
        while (caMappingsIt.hasNext()){
            Class2Attribute crtCA = caMappingsIt.next();
            if (((ClassId)crtCA.getSource().getId()).getType().equals(c)){
                Iterator<Type> typesIt = ((AttributeId)crtCA.getTarget().getId()).getAttribute().listTypes().iterator();
                while (typesIt.hasNext()){
                    Type crtType = typesIt.next();
                    if (crtType instanceof Concept){
                        result.add((Concept)crtType);
                    }
                }
            }
        }        
        return result;
    }
    
    private Set<Concept> getTargetConcepts(Set<Concept> sourceConcepts){
        
        if (sourceConcepts==null || sourceConcepts.isEmpty())
            return null;
       Set<Concept> result = new HashSet<Concept>(); 
       Iterator<Concept> itC = sourceConcepts.iterator();
       while (itC.hasNext()){
           Set<Concept> crtTgtConcepts =  getTargetConcepts(itC.next());
           if (crtTgtConcepts!=null)
               result.addAll(crtTgtConcepts);
       }
        return result;
        
    }    
    
    private Concept getSourceConcept(Set<Instance> instances){
     
        if (instances!=null && !instances.isEmpty()){
            return (Concept)instances.iterator().next().listConcepts().iterator().next();
        }
        return null;
    }
    
    /*private List<Concept> getSourceConcepts(Set<Instance> instances){
           List<Concept> result = new ArrayList<Concept>(); 
           if (instances==null || instances.isEmpty()){
               return null;
           }
           Iterator<Instance> it = instances.iterator();
           while (it.hasNext()){
               result.addAll(it.next().listConcepts());
           }
           return result;
       }*/
    
    private Set<Concept> getSourceConcepts(Set<Instance> instances){
        Set<Concept> result = new HashSet<Concept>();
        Set<Identifier> identifiers = new HashSet<Identifier>();
        if (instances==null || instances.isEmpty()){
            return null;
        }
        Iterator<Instance> it = instances.iterator();
        while (it.hasNext()){
            Instance crtInstance = it.next();
            if (!identifiers.contains(WSMOUtil.getIdentifier(crtInstance))){
                Iterator<Set<Value>> itAV = crtInstance.listAttributeValues().values().iterator();
                while (itAV.hasNext()){
                    Iterator<Value> itV = itAV.next().iterator();
                    while (itV.hasNext()){
                        Value crtValue =  itV.next();
                        if (crtValue instanceof Instance)
                            identifiers.add(WSMOUtil.getIdentifier(crtValue));
                    }
                }     
            }
        }
        it = instances.iterator();
        while (it.hasNext()){
            Instance crtInstance = it.next();
            if (!identifiers.contains(crtInstance.getIdentifier())){
                result.addAll(crtInstance.listConcepts());
            }
        }
        
        return result;
    }

    public List<Set<Instance>> cleanUp(List<Set<Instance>> theInstances){
        Set <Instance> allInstances = new HashSet <Instance> ();
        for (Set <Instance> instanceSet : theInstances){
            for (Instance instance : instanceSet){
                if (instance != null){
                    if (!allInstances.contains(instance)){
                        allInstances.add(instance);
                        getInstancesFromInstance(instance, allInstances);
                    }
                }
            }
        }
        
        Set <Instance> extraInstances = discoverExtraInstances(allInstances);
        removeExtraInstances(theInstances, extraInstances);
        removeDeadAttributes(allInstances, extraInstances);
        removeEmptyInstances(theInstances);
        
        for (Instance instance : allInstances){
            cleanupInstanceMembership(instance);
        }
        
        storageLoader.getOutputStream().flush();
        
        return theInstances;
    }
    
    private void getInstancesFromInstance(Instance instance, Set<Instance> theProcessedInstances) {
        for (Set <Value> valueSet : instance.listAttributeValues().values()){
            for (Value v : valueSet){
                if (v instanceof Instance){
                    Instance anInstance = (Instance) v;
                    if (anInstance != null){
                        if (!theProcessedInstances.contains(anInstance)){
                            theProcessedInstances.add(anInstance);
                            getInstancesFromInstance(anInstance, theProcessedInstances);
                        }
                    }
                }
            }
        }
    }
    
    private void removeEmptyInstances(List<Set<Instance>> input){
        for (Iterator <Set <Instance>> outer = input.iterator(); outer.hasNext();) {
            Set <Instance> instanceSet = outer.next();
            for (Iterator <Instance> inner = instanceSet.iterator(); inner.hasNext();) {
                Instance instance = inner.next();
                if (instance == null || instance.listAttributeValues() == null || instance.listAttributeValues().isEmpty()){
                    inner.remove();
                    if (instance != null){
                        Identifier id = instance.getIdentifier();
                        storageLoader.getOutputStream().println("Removed empty Instance '" + id + "'");
                    }
                }
            }
            if (instanceSet.size() == 0){
                outer.remove();
            }
        }
    }
    
    private void removeExtraInstances(List<Set<Instance>> input, Set <Instance> extraInstances){
        for (Iterator <Set <Instance>> outer = input.iterator(); outer.hasNext();) {
            Set <Instance> instanceSet = outer.next();
            for (Iterator <Instance> inner = instanceSet.iterator(); inner.hasNext();) {
                Instance i = inner.next();
                if (i != null){
                    Identifier id = i.getIdentifier();
                    if (extraInstances != null && extraInstances.contains(i)){
                        inner.remove();
                        storageLoader.getOutputStream().println("Removed extra Instance '" + id + "'");
                    }
                }
                else{
                    inner.remove();
                }
            }
            if (instanceSet.size() == 0){
                outer.remove();
            }
        }
    }
    

    private void removeDeadAttributes(Set<Instance> theInstances, Set<Instance> extraInstances) {
        for (Instance instance : theInstances){
            Map <Identifier, Set <Value>> deadAttributes = new HashMap <Identifier, Set <Value>> ();
            for (Identifier attribute : instance.listAttributeValues().keySet()){
                for (Value v : instance.listAttributeValues(attribute)){
                    if (v instanceof Instance && extraInstances.contains(v)){
                        Set <Value> deadValues = deadAttributes.get(attribute);
                        if (deadValues == null){
                            deadValues = new HashSet <Value> ();
                            deadAttributes.put(attribute, deadValues);
                        }
                        deadValues.add(v);
                    }
                }
            }
            
            for (Identifier id : deadAttributes.keySet()){
                for (Value v : deadAttributes.get(id)){
                    try {
                        instance.removeAttributeValue(id, v);
                    }
                    catch (SynchronisationException e) {
                        e.printStackTrace();
                    }
                    catch (InvalidModelException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void cleanupInstanceMembership(Instance theInstance) {
        Set <Concept> toRemove = new HashSet <Concept> ();
        for (Concept concept : theInstance.listConcepts()){
            toRemove.addAll(WSMOUtil.getSuperConcepts(concept));
        }
        for (Concept concept : toRemove){
            try {
                theInstance.removeConcept(concept);
            }
            catch (SynchronisationException e) {
                e.printStackTrace();
            }
            catch (InvalidModelException e) {
                e.printStackTrace();
            }
        }
    }
    
    private Set<Instance> discoverExtraInstances(Set<Instance> theInstances) {
        Set <Instance> result =  new HashSet <Instance> ();
        
        Map <String, Set <Instance>> srcToTgt = new HashMap <String, Set <Instance>> ();
        for (Instance instance : theInstances){
            String src = getInstanceSource(instance);
            if (src != null){
                Set <Instance> instances = srcToTgt.get(src);
                if (instances == null){
                    instances = new HashSet <Instance> ();
                    srcToTgt.put(src, instances);
                }
                instances.add(instance);
            }
        }
        
        for (String source : srcToTgt.keySet()){
            Set <String> toRemove = new HashSet <String> ();
            for (Instance instance : srcToTgt.get(source)){
                String conceptID = getInstanceTargetConcept(instance);
                Concept concept = (Concept) locator.lookup(wsmoFactory.createIRI(conceptID), Concept.class);
                if (concept == null){
                    int i = conceptID.lastIndexOf('/');
                    conceptID = conceptID.substring(0, i) + "#" + conceptID.substring(i + 1);
                    concept = (Concept) locator.lookup(wsmoFactory.createIRI(conceptID), Concept.class);
                }
                if (concept != null){
                    for (Concept superConcept : WSMOUtil.getSuperConcepts(concept)){
                        toRemove.add(superConcept.getIdentifier().toString());
                    }
                }
            }
            
            for (Instance instance : srcToTgt.get(source)){
                String conceptID = getInstanceTargetConcept(instance);
                
                if (conceptID != null){
                    int i = conceptID.lastIndexOf('/');
                    String conceptID2 = conceptID.substring(0, i) + "#" + conceptID.substring(i + 1);
                    
                    if (toRemove.contains(conceptID) || toRemove.contains(conceptID2)){
                        result.add(instance);
                    }
                }
            }
        }
        
        return result;
    }
    
	private String getInstanceSource(Instance instance) {
        Matcher m = instanceIdPattern.matcher(instance.getIdentifier().toString());
        if (m.matches()){
            return m.group(1);
        }
        return null;
    }
    
    private String getInstanceTargetConcept(Instance instance) {
        Matcher m = instanceIdPattern.matcher(instance.getIdentifier().toString());
        if (m.matches()){
            return m.group(2);
        }
        return null;
    }
    
    public void setStorageLoader(Loader storageLoader) {
		this.storageLoader = storageLoader;
	}
    
    
    /* ***************************** */
    /* *** CLONING / FIXING IRIs *** */
    /* ***************************** */
    
    
	/**
	 * Clones the mediatedData and replaces IRIs with IRIs which can be processed by all reasoners.
	 * 
	 * @param mediatedData
	 * @return
	 * @throws ComponentException
	 */
	protected Map<Entity, List<Entity>> fixMediatedDataIRIs(Map<Entity, List<Entity>> mediatedData) 
	throws ComponentException {
//		logger.debug("fixing mediated data IRIs ...");		
		
		String errors = "";							
		String cloneOntoNS = "http://www.wsmx.org/datamediator/runtime/result_ontology_" + WSMOUtil.generateUniqueID();
		Ontology cloneOnto = wsmoFactory.createOntology(wsmoFactory.createIRI( cloneOntoNS ));
		
		Map<IRI, Instance> iri2Cloned = new HashMap<IRI, Instance>();		
		Map<Instance, Instance> instance2Cloned = new HashMap<Instance, Instance>();
		for (List<Entity> l : mediatedData.values()) {
			for (Entity e : l) {
				try {
					createFixedIRIClone((Instance)e, cloneOntoNS, cloneOnto, iri2Cloned, instance2Cloned);
				} catch (SynchronisationException e1) {
					errors += "SynchronizationException for instance " + e.getIdentifier() + ":  " + e1.getMessage() + "\n";
					e1.printStackTrace();					
				} catch (InvalidModelException e1) {
					errors += "InvalidModelException for instance " + e.getIdentifier() + ":  " + e1.getMessage() + "\n";
					e1.printStackTrace();					
				}
			}
		}
		
		if (! errors.isEmpty()) 
			throw new ComponentException(errors);			
		
		Map<Entity, List<Entity>> result = new HashMap<Entity, List<Entity>>();		
		Set<Instance> clonesAddedToResult = new HashSet<Instance>();
		
		for (Entity key : mediatedData.keySet()) {
			List<Entity> clonedInstances = new ArrayList<Entity>();
			for (Entity e: mediatedData.get(key))  {				
				Instance clone = instance2Cloned.get(e);
//				logger.debug("adding fixed clone \"" 
//						+ (clone == null ? "(null)" : clone.getIdentifier().toString()) 
//						+ "\" to result, replacing \"" + e.getIdentifier().toString() + "\"");
				clonedInstances.add(clone);
				clonesAddedToResult.add(clone);
			}
			result.put(key, clonedInstances);
		}
		
		List<Entity> clonesStillToAddToResult = new ArrayList<Entity>();
		for (Instance i : cloneOnto.listInstances()) {	
			if (! clonesAddedToResult.contains(i)) 
				clonesStillToAddToResult.add(i);
		}		
		result.put((Entity)null, clonesStillToAddToResult);
		
//		logger.debug("All cloned instances size: " + clonesAddedToResult.size() + " + " + clonesStillToAddToResult.size());		
//		StringBuffer sb = new StringBuffer("");
//		sb.append("\n\n*********** THE FIXED CLONED INSTANCES *****\n\n");
//		serialize(clonesAddedToResult, new HashSet <Instance> (), sb);
//		logger.debug(sb.toString());
		
		return result;
	}
	
	protected Instance createFixedIRIClone(Instance theInstance, String theNamespace, Ontology theOntology, Map <IRI, Instance> iri2Cloned, Map<Instance, Instance> original2Cloned) 
	throws SynchronisationException, InvalidModelException {
		
		// create new IRI
		IRI cloneIRI = generateValidIRI( theInstance.getIdentifier().toString(), theNamespace );		
		
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
                	clone.addAttributeValue(attributeID, createFixedIRIClone((Instance) value, theNamespace, theOntology, iri2Cloned, original2Cloned));
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
	
	/**
	 * <p>Changes an IRI as from the data mediation output instances to a standard IRI which can be processed by all reasoners.</p>
	 * 
	 * Example:<br/>
	 * 	<code>http://www.wsmx.org/datamediator/runtime/merged_ontology#mediated1('http://www.wsmx.org/ontologies/rosetta/MoonGoal/ShipToPo, http://www.example.org/ontologies/sws-challenge/Moon/Address')</code>
	 *  <br/>will be changed to: <br/>
	 *  <code>http://www.wsmx.org/datamediator/runtime/result_ontology#__Address____-999625877</code><br/>
	 *  (depending on the newNamespace).
	 * 
	 * @param mediatedIRI old IRI
	 * @param newNamespace namespace of the new IRI
	 * @return the new IRI
	 */
	protected IRI generateValidIRI(String mediatedIRI, String newNamespace) {
		String local = mediatedIRI;
		int localIdx = local.indexOf("#");
		if (localIdx != -1) { 
			local = local.substring( localIdx + 1 );
			// keep target instance concept in name (after last slash)
			int lastSlashIdx = local.lastIndexOf('/');
			if (lastSlashIdx != -1) {
				local = local.substring(lastSlashIdx);
			}
			// replace any potentially offending characters
			local = local.replaceAll("[\\(\\)', .:]", "_");
			local = local.replaceAll("/", "__");
			// make sure resulting name is as unique as the original
			local += "__" + mediatedIRI.hashCode();
			return wsmoFactory.createIRI( newNamespace + "#" + local );
		} else {
			// actually this should never happen
			return wsmoFactory.createIRI( newNamespace + "#" + mediatedIRI.hashCode() );
		}		
	}
    
    

}
