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

package org.deri.wsmx.mediation.ooMediator.mapper;



import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.deri.wsmx.eclipse.datamediation.ooMediator.items.CompoundItem;
import org.deri.wsmx.eclipse.datamediation.ooMediator.items.Item;
import org.deri.wsmx.mediation.ooMediator.beans.AnonymousInstance;
import org.deri.wsmx.mediation.ooMediator.beans.AttributeValueNode;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.AttributeId;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.ClassId;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.DataValueIdPlaceholder;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.IndividualId;
import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.mediation.language.objectmodel.api.AttributeExpr;
import org.omwg.mediation.language.objectmodel.api.ClassExpr;
import org.omwg.mediation.language.objectmodel.api.ComplexExpression;
import org.omwg.mediation.language.objectmodel.api.ExpressionDefinition;
import org.omwg.mediation.language.objectmodel.api.IRI;
import org.omwg.mediation.language.objectmodel.api.MappingRule;
import org.omwg.mediation.language.objectmodel.api.ComplexExpression.Operator;
import org.omwg.mediation.language.objectmodel.api.MappingRule.Direction;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.Restriction;
import org.omwg.mediation.language.objectmodel.api.conditions.TypeCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.ValueCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.Restriction.Comparator;
import org.omwg.mediation.language.objectmodel.api.rules.Attribute2Attribute;
import org.omwg.mediation.language.objectmodel.api.rules.Attribute2Class;
import org.omwg.mediation.language.objectmodel.api.rules.Class2Attribute;
import org.omwg.mediation.language.objectmodel.api.rules.Class2Class;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.Concept;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Type;
import org.omwg.ontology.WsmlDataType;



/**
 * This class represents the heart of the mediation component. 
 * It contains references to the ontologies to be mapped and manages the mappings between them.
 **/
public class Mapper {
	//private List <MapperListener> listeners = new ArrayList <MapperListener> ();
    
	private Ontology srcOntology = null;
	private Ontology tgtOntology = null;
	
	private Hashtable<Type, Vector<Type>> ccMapper = null;
	private Hashtable<Attribute, Vector<Attribute>> aaMapper = null;
	private HashMap<Type, ArrayList<Attribute>> caMapper = null;
	private HashMap<Attribute, ArrayList<Type>> acMapper = null;
	private HashMap<AttributeValueNode, ArrayList<AttributeValueNode>> avavMapper = null;
	private HashMap<AttributeValueNode, ArrayList<Type>> avcMapper = null;
	private HashMap<Type, ArrayList<AttributeValueNode>> cavMapper = null;

	
	public final static int MAPPED = 1;
	public final static int NOT_MAPPED = 0;
	
	public final static int PRIMITIVE = 0;
	public final static int DECOMPOSITION_REQUIRED = 1;
	public final static int LEFT_BALANCED = 2;
	public final static int RIGHT_BALANCED = 3;
	
	private Mappings mappings = null;
	
	/**
	 * Creates a new mapper object with null managers for the source and target ontologies and 
	 * empty mapping lists.
	 */
	public Mapper(){
	    cleanMapings();
	}
	
	/**
	 * Creates a new mapper object with the given managers for the source and target concepts and 
	 * empty mapping lists
	 * @param srcConcepts the source concepts manager
	 * @param tgtConcepts the target concepts manager
	 */
	public Mapper(Ontology srcConcepts, Ontology tgtConcepts){
		this.srcOntology = srcConcepts;
		this.tgtOntology = tgtConcepts;
		cleanMapings();
		mappings = new Mappings(srcConcepts, tgtConcepts);
	}

/*    private void fireMapperChangedEvent() {
        for (MapperListener ml : listeners){
            ml.mapperChanged();
        }
    }
*/    
/*    public void addMapperListener(MapperListener theMapperListener){
        listeners.add(theMapperListener);
    }
    
    public void removeMapperListener(MapperListener theMapperListener){
        listeners.remove(theMapperListener);
    }
*/    
	/**
	 * Determines for a given concept from the source ontology the mapped concepts from the target ontology 
	 * @param concept the source concept - for this version a String denoting its name
	 * @return a vector containing all the concepts from the target ontology which <i>concept</i> is mapped to.
	 * For this version the vector contains String denoting the names of the mapped concepts. 
	 */
	public Vector getTargetConcepts(Type concept){
		if (concept == null){
			return null;
        }
		if (ccMapper == null){
			return null;
        }
		Object object = ccMapper.get(concept);
		if (object==null){
			return null; 
        }
		return (Vector)object;
	}
	/**
	 * Determines for a given concept from the source ontology the mapped concepts from the target ontology
	 * @param concept the source concept - for this version a String denoting its name
	 * @return a String containing names of all mapped target concepts separated by comma (", ")
	 */
	public String getTargetConceptsAsString(Concept concept){
		Iterator it = ((Vector)(ccMapper.get(concept))).iterator();
		String bag="";
		while (it.hasNext()){
			if (!bag.equals("")){
				bag = bag + ", ";
            }
			Concept tgtConcept = (Concept)it.next();	
			bag = bag + tgtConcept;
		}
		return bag;
	}
	/**
	 * Determines for a given attribute from the source ontology the mapped attributes from the target ontology
	 * @param attribute the source attribute
	 * @return a String containing names of all mapped target attributes separated by comma (", ")
	 */
	public String getTargetAttributesAsString(Attribute attribute){
		String bag="";
		if (attribute == null){
			return bag;
        }
		Iterator it = ((Vector)(aaMapper.get(attribute))).iterator();		
		while (it.hasNext()){
			if (!bag.equals("")){
				bag = bag + ", ";
            }
			Attribute tgtAttribute = (Attribute)it.next();	
			bag = bag + tgtAttribute.toString() + " (" + tgtAttribute.getConcept() + ")";	
		}
		return bag;		
	}
	
    public Vector getSourceItems(){
        Vector result = new Vector();
        result.addAll(aaMapper.keySet());
        result.addAll(ccMapper.keySet());
        result.addAll(acMapper.keySet());
        result.addAll(caMapper.keySet());
        result.addAll(avavMapper.keySet());
        result.addAll(avcMapper.keySet());
        result.addAll(cavMapper.keySet());
        return result;
    }
    
	public Vector<Object> getTargetEntity(Object wsmoEntity){
		
		Vector<Object> result = new Vector<Object>();		
		if (wsmoEntity == null){
			return result;
        }
	 
		if (wsmoEntity instanceof Attribute){
			if (aaMapper.get(wsmoEntity)!=null)
				result.addAll(aaMapper.get(wsmoEntity));
			if (acMapper.get(wsmoEntity)!=null)
				result.addAll(acMapper.get(wsmoEntity));						
        }
		if (wsmoEntity instanceof Type){
			if (ccMapper.get(wsmoEntity)!=null)
				result.addAll(ccMapper.get(wsmoEntity));
			if (caMapper.get(wsmoEntity)!=null)
				result.addAll(caMapper.get(wsmoEntity));
			if (cavMapper.get(wsmoEntity)!=null)
				result.addAll(cavMapper.get(wsmoEntity));			
        }
		if (wsmoEntity instanceof AttributeValueNode){
			if (avavMapper.get(wsmoEntity)!=null)
				result.addAll(avavMapper.get(wsmoEntity));
			if (avcMapper.get(wsmoEntity)!=null)
				result.addAll(avcMapper.get(wsmoEntity));						
        }
		
		
		return result;
	}
	
	
	
	
	/**
	 * Determines for a given attribute from the source ontology the mapped attributes from the target ontology
	 * @param attribute the source attribute
	 * @return a vector containing all the attributes from the target ontology which <i>attribute</i> is mapped to.
	 */
	public Vector getTargetAttributes(Attribute attribute){
		if (attribute == null){
			return null;
        }
		return aaMapper.get(attribute);
	}
    
	/**
	 * Sets the mappings between a concept from the source and a set of concepts from the target ontology 
	 * @param concept the source concept 
	 * @param targetConcepts a vector of target concepts 
	 */
	private void setTargetConcepts(Type concept, Vector<Type> targetConcepts){
		Vector<Type> targets = ccMapper.get(concept);
		if (targets==null){
			ccMapper.put(concept, targetConcepts);
        }
		else{
			addIfNotContained(targetConcepts, targets);
		}
	}
	
	private void setTargetConceptsForAttribute(Attribute attribute, Collection<Type> targetConcepts){
		if (targetConcepts==null){
			return;
        }
		Iterator<Type> it = targetConcepts.iterator(); 
		while (it.hasNext()){
			setTargetConceptForAttribute(attribute, it.next());
		}		
	}

	private void setTargetConceptsForAttributeValueNode(AttributeValueNode attribute, Collection<Type> targetConcepts){
		if (targetConcepts==null){
			return;
        }
		Iterator<Type> it = targetConcepts.iterator(); 
		while (it.hasNext()){			
			setTargetConceptForAttributeValueNode(attribute, it.next());			
		}		
	}

	
    private void setType2TypeMappings(Type item, Vector targetItems){
        String id = "";
        if (item instanceof Concept){
            id = ((Concept)item).getIdentifier().toString();
        }
        else{
            id = ((WsmlDataType)item).getIRI().toString();
        }
        Class2Class aMapping = new Class2Class(new IRI(id+targetItems.toString()), Direction.MAPPING);
        aMapping.setSource(new ClassExpr(new ClassId(item), null));
        if (targetItems.size()>1){      
            id = id + ComplexExpression.Operator.AND.toString();
            List<ExpressionDefinition> subExpressions = new ArrayList<ExpressionDefinition>();
            Iterator targetIt = targetItems.iterator();
            while (targetIt.hasNext()){
                Type crtTgtConcept = (Type)targetIt.next();
                id = id + WSMOUtil.getIdentifier(crtTgtConcept).toString();
                subExpressions.add(new ClassId(crtTgtConcept));
            }
            aMapping.setTarget(new ClassExpr(subExpressions, ComplexExpression.Operator.AND, null));
        }
        else{
            Type crtTgtConcept = (Type)targetItems.get(0);
            id = id + WSMOUtil.getIdentifier(crtTgtConcept).toString();
            aMapping.setTarget(new ClassExpr(new ClassId(crtTgtConcept), null));
        }
        aMapping.setId(new IRI(id));
        //for cashing
        setTargetConcepts(item, targetItems);
        mappings.addConceptMapping(aMapping);       
        //fireMapperChangedEvent();
    }

    private void setAttribute2AttributeMappings(Attribute item, Vector targetItems){
        String id = item.getIdentifier().toString();
        Attribute2Attribute aMapping = new Attribute2Attribute(new IRI(id+targetItems.toString()), Direction.MAPPING);
        aMapping.setSource(new AttributeExpr(new AttributeId(item), null));
        if (targetItems.size()>1){      
            id = id + Operator.AND;
            List<ExpressionDefinition> subExpressions = new ArrayList<ExpressionDefinition>();
            Iterator targetIt = targetItems.iterator();
            while (targetIt.hasNext()){
                Attribute crtTgtAttr = (Attribute)targetIt.next();
                id = id + crtTgtAttr.getIdentifier().toString();
                subExpressions.add(new AttributeId(crtTgtAttr));
            }
            aMapping.setTarget(new AttributeExpr(subExpressions, Operator.AND, null));
        }
        else{
            Attribute crtTgtAttr = (Attribute)targetItems.get(0);
            id = id + crtTgtAttr.getIdentifier().toString();
            aMapping.setTarget(new AttributeExpr(new AttributeId(crtTgtAttr), null));
        }
        aMapping.setId(new IRI(id));
        //for cashing
        setTargetAttributes(item, targetItems);
        mappings.addAttributeMapping(aMapping);   
        //fireMapperChangedEvent();
    }
    
    private void setType2AttributeMappings(Type item, Vector targetItems){
        String id = WSMOUtil.getIdentifier(item).toString();
        Class2Attribute caMapping = new Class2Attribute(new IRI(id+targetItems.toString()), Direction.MAPPING);
        caMapping.setSource(new ClassExpr(new ClassId(item), null));
        if (targetItems.size()>1){      
            id = id + Operator.AND;
            List<ExpressionDefinition> subExpressions = new ArrayList<ExpressionDefinition>();
            Iterator targetIt = targetItems.iterator();
            while (targetIt.hasNext()){
                Attribute crtTgtAttr = (Attribute)targetIt.next();
                id = id + crtTgtAttr.getIdentifier().toString();
                subExpressions.add(new AttributeId(crtTgtAttr));
            }
            caMapping.setTarget(new AttributeExpr(subExpressions, Operator.AND, null));
        }
        else{
            Attribute crtTgtAttr = (Attribute)targetItems.get(0);
            id = id + crtTgtAttr.getIdentifier().toString();
            caMapping.setTarget(new AttributeExpr(new AttributeId(crtTgtAttr), null));
        }
        caMapping.setId(new IRI(id));
        //for cashing
        setTargetAttributesForConcept((Type)item, targetItems);
        mappings.addConceptAttributeMapping(caMapping);   
        //fireMapperChangedEvent();
    }

    private void setAttribute2TypeMappings(Attribute item, Vector targetItems){
        String id = item.getIdentifier().toString();
        Attribute2Class acMapping = new Attribute2Class(new IRI(id + targetItems.toString()), Direction.MAPPING);
        acMapping.setSource(new AttributeExpr(new AttributeId(item), null));
        if (targetItems.size()>1){      
            id = id + Operator.AND;
            List<ExpressionDefinition> subExpressions = new ArrayList<ExpressionDefinition>();
            Iterator targetIt = targetItems.iterator();
            while (targetIt.hasNext()){
                Type crtTgtConcept = (Type)targetIt.next();
                id = id + WSMOUtil.getIdentifier(crtTgtConcept).toString();
                subExpressions.add(new ClassId(crtTgtConcept));
            }
            acMapping.setTarget(new ClassExpr(subExpressions, Operator.AND, null));
        }
        else{
            Type crtTgtConcept = (Type)targetItems.get(0);
            id = id + WSMOUtil.getIdentifier(crtTgtConcept).toString();
            acMapping.setTarget(new ClassExpr(new ClassId(crtTgtConcept), null));
        }
        acMapping.setId(new IRI(id));
        //for cashing
        setTargetConceptsForAttribute(item, targetItems);
        mappings.addAttributeConceptMapping(acMapping);
        //fireMapperChangedEvent();
    }   
    
    private void setAttribute2AttributeMappingsWithConditions(AttributeValueNode item, Vector targetItems){
        String id = item.getIdentifier().toString();
        AttributeValueNode srcAVN = item;
        Attribute2Attribute aMapping = new Attribute2Attribute(new IRI(id+targetItems.toString()), Direction.MAPPING); 
        Set<AttributeCondition> srcConditions = new HashSet<AttributeCondition>();
        Set<AttributeCondition> tgtConditions = new HashSet<AttributeCondition>();
        if (targetItems.size()>1){      
            id = id + Operator.AND;
            List<ExpressionDefinition> subExpressions = new ArrayList<ExpressionDefinition>();            
            Iterator targetIt = targetItems.iterator();
            while (targetIt.hasNext()){
                AttributeValueNode tgtAVN = (AttributeValueNode)targetIt.next();
                Attribute crtTgtAttr = tgtAVN.getAttribute();
                id = id + crtTgtAttr.getIdentifier().toString();
                subExpressions.add(new AttributeId(crtTgtAttr));
                //tgt conditions                                 
                tgtConditions.add(getConditionForAttributeValueNode(tgtAVN));                
                //aMapping.getTarget().addCondition(getConditionForAttributeValueNode(tgtAVN));
            }
            aMapping.setTarget(new AttributeExpr(subExpressions, Operator.AND, tgtConditions));
        }
        else{
            AttributeValueNode tgtAVN = (AttributeValueNode)targetItems.get(0);         
            Attribute crtTgtAttr = tgtAVN.getAttribute();
            id = id + crtTgtAttr.getIdentifier().toString();            
            //tgtConditions
            tgtConditions.add(getConditionForAttributeValueNode(tgtAVN));  
            //aMapping.addCondition(getConditionForAttributeValueNode(tgtAVN));
            aMapping.setTarget(new AttributeExpr(new AttributeId(crtTgtAttr), tgtConditions));
        }
        aMapping.setId(new IRI(id));
        //for cashing
        setTargetAttributeValueNodes(srcAVN, targetItems);
        mappings.addAttributeMapping(aMapping); 
        //fireMapperChangedEvent();
        //srcConditions
        srcConditions.add(getConditionForAttributeValueNode(srcAVN)); 
        aMapping.setSource(new AttributeExpr(new AttributeId(srcAVN.getAttribute()), srcConditions));
    }
    
    private void setAttribute2TypeMappingsWithConditions(AttributeValueNode item, Vector targetItems){
        String id = item.getIdentifier().toString();
        AttributeValueNode srcAVN = item;
        Attribute2Class acMapping = new Attribute2Class(new IRI(id + targetItems.toString()), Direction.MAPPING);        
        if (targetItems.size()>1){      
            id = id + Operator.AND;
            List<ExpressionDefinition> subExpressions = new ArrayList<ExpressionDefinition>();
            Iterator targetIt = targetItems.iterator();
            while (targetIt.hasNext()){
                Type crtTgtConcept = (Type)targetIt.next();
                id = id + WSMOUtil.getIdentifier(crtTgtConcept).toString();
                subExpressions.add(new ClassId(crtTgtConcept));
            }
            acMapping.setTarget(new ClassExpr(subExpressions, Operator.AND, null));
        }
        else{
            Type crtTgtConcept = (Type)targetItems.get(0);
            id = id + WSMOUtil.getIdentifier(crtTgtConcept).toString();
            acMapping.setTarget(new ClassExpr(new ClassId(crtTgtConcept), null));
        }
        acMapping.setId(new IRI(id));
        //for cashing
        setTargetConceptsForAttributeValueNode(srcAVN, targetItems);        
        mappings.addAttributeConceptMapping(acMapping);
        //fireMapperChangedEvent();
        //srcCondition
        Set<AttributeCondition> srcConditions = new HashSet<AttributeCondition>();
        srcConditions.add(getConditionForAttributeValueNode(srcAVN));
        acMapping.setSource(new AttributeExpr(new AttributeId(srcAVN.getAttribute()), srcConditions));
    }
    
    private void setType2AttributeMappingsWithConditions(Type item, Vector targetItems){
        String id = WSMOUtil.getIdentifier(item).toString();
        Class2Attribute caMapping = new Class2Attribute(new IRI(id+targetItems.toString()), Direction.MAPPING);
        caMapping.setSource(new ClassExpr(new ClassId(item), null));
        Set<AttributeCondition> tgtConditions = new HashSet<AttributeCondition>();
        if (targetItems.size()>1){      
            id = id + Operator.AND;
            List<ExpressionDefinition> subExpressions = new ArrayList<ExpressionDefinition>();
            Iterator targetIt = targetItems.iterator();
            while (targetIt.hasNext()){
                AttributeValueNode tgtAVN = (AttributeValueNode)targetIt.next();
                Attribute crtTgtAttr = tgtAVN.getAttribute();
                id = id + crtTgtAttr.getIdentifier().toString();
                subExpressions.add(new AttributeId(crtTgtAttr));
                //tgt conditions
                tgtConditions.add(getConditionForAttributeValueNode(tgtAVN));  
                //caMapping.addCondition(getConditionForAttributeValueNode(tgtAVN));
            }
            caMapping.setTarget(new AttributeExpr(subExpressions, Operator.AND, tgtConditions));
        }
        else{
            AttributeValueNode tgtAVN = (AttributeValueNode)targetItems.get(0);         
            Attribute crtTgtAttr = tgtAVN.getAttribute();
            id = id + crtTgtAttr.getIdentifier().toString();                      
            //tgtConditions
            tgtConditions.add(getConditionForAttributeValueNode(tgtAVN));  
            caMapping.setTarget(new AttributeExpr(new AttributeId(crtTgtAttr), tgtConditions));
        }
        caMapping.setId(new IRI(id));
        //for cashing   
        setTargetAttributeValueNodesForConcept(item, targetItems);        
        mappings.addConceptAttributeMapping(caMapping);  
        //fireMapperChangedEvent();
    }
	/**
	 * Sets the mappings between an item from the source and a set of items from the target ontology 
	 * @param item the source item 
	 * @param targetItems a vector of target items
	 */	
	public void setTargetItems(Item item, Vector targetItems){
		if ((targetItems==null)||targetItems.isEmpty())
				return;
        Vector wsmoTargetItems = extractWSMOElements(targetItems);
        Object wsmoItem = item.getWSMOElement();
		if ((wsmoItem instanceof Type)&&(wsmoTargetItems.get(0) instanceof Type)){
		    setType2TypeMappings((Type)wsmoItem, wsmoTargetItems);
			return;
		}
		if ((wsmoItem instanceof Attribute)&&(wsmoTargetItems.get(0) instanceof Attribute)){
		    setAttribute2AttributeMappings((Attribute)wsmoItem, wsmoTargetItems);
			return;
		}

		if ((wsmoItem instanceof Type)&&(wsmoTargetItems.get(0) instanceof Attribute)){
		    setType2AttributeMappings((Type)wsmoItem, wsmoTargetItems);
			return;
		}
		
		if ((wsmoItem instanceof Attribute)&&(wsmoTargetItems.get(0) instanceof Concept)){
		    setAttribute2TypeMappings((Attribute)wsmoItem, wsmoTargetItems);
			return;
		}

		if ((wsmoItem instanceof AttributeValueNode)&&(wsmoTargetItems.get(0) instanceof AttributeValueNode)){
		    setAttribute2AttributeMappingsWithConditions((AttributeValueNode)wsmoItem, wsmoTargetItems);
			return;
		}

		if ((wsmoItem instanceof AttributeValueNode)&&(wsmoTargetItems.get(0) instanceof Concept)){
		    setAttribute2TypeMappingsWithConditions((AttributeValueNode)wsmoItem, wsmoTargetItems);
			return;
		}

		if ((wsmoItem instanceof Concept)&&(wsmoTargetItems.get(0) instanceof AttributeValueNode)){
			setType2AttributeMappingsWithConditions((Type)wsmoItem, wsmoTargetItems);
			return;
		}
	}
    
    public static <E> Vector<E> extractWSMOElements(Collection<Item<E>> items){
        Vector<E> result = new Vector<E>();
        if (items==null || items.isEmpty())
            return result;
        Iterator<Item<E>> it = items.iterator();
        while(it.hasNext()){
            result.add(it.next().getWSMOElement());
        }
        return result;
    }
    
    private void createMapping(Type wsmoSrcItem, Type wsmoTgtItem){
        String id = WSMOUtil.getIdentifier(wsmoSrcItem) + WSMOUtil.getIdentifier(wsmoTgtItem).toString();
		Class2Class aMapping = new Class2Class(new IRI(id), Direction.MAPPING);
		aMapping.setSource(new ClassExpr(new ClassId(wsmoSrcItem), null));
		aMapping.setTarget(new ClassExpr(new ClassId(wsmoTgtItem), null));
		//for cashing
		setTargetConcept(wsmoSrcItem, wsmoTgtItem);
		mappings.addConceptMapping(aMapping);    	
    }
    

    private void createMapping(Attribute wsmoSrcItem, Attribute wsmoTgtItem){
        String id = (wsmoSrcItem).getIdentifier().toString() + (wsmoTgtItem).getIdentifier().toString();
		Attribute2Attribute aMapping = new Attribute2Attribute(new IRI(id), Direction.MAPPING);
		aMapping.setSource(new AttributeExpr(new AttributeId(wsmoSrcItem), null));
		aMapping.setTarget(new AttributeExpr(new AttributeId(wsmoTgtItem), null));
		//for cashing
		setTargetAttribute(wsmoSrcItem, wsmoTgtItem);
		mappings.addAttributeMapping(aMapping);	
    }
    

    private void createMapping(Type wsmoSrcItem, Attribute wsmoTgtItem){
        String id = WSMOUtil.getIdentifier(wsmoSrcItem) + (wsmoTgtItem).getIdentifier().toString();
		Class2Attribute caMapping = new Class2Attribute(new IRI(id), Direction.MAPPING);
		caMapping.setSource(new ClassExpr(new ClassId(wsmoSrcItem), null));
		caMapping.setTarget(new AttributeExpr(new AttributeId(wsmoTgtItem), null));
		//for cashing
		setTargetAttributeForConcept(wsmoSrcItem, wsmoTgtItem);
		mappings.addConceptAttributeMapping(caMapping);
    }

    private void createMapping(Attribute wsmoSrcItem, Type wsmoTgtItem){
        String id = (wsmoSrcItem).getIdentifier().toString() + WSMOUtil.getIdentifier(wsmoTgtItem).toString();
		Attribute2Class acMapping = new Attribute2Class(new IRI(id), Direction.MAPPING);
		acMapping.setSource(new AttributeExpr(new AttributeId(wsmoSrcItem), null));
		acMapping.setTarget(new ClassExpr(new ClassId(wsmoTgtItem), null));
		//for cashing
		setTargetConceptForAttribute(wsmoSrcItem, wsmoTgtItem);
		mappings.addAttributeConceptMapping(acMapping);
    }    
    
    
    
	/**
	 * Sets the mapping between an item from the source and an item from the target ontology 
	 * @param srcItem the source item 
	 * @param tgtItem the target items
	 */	
	public void setTargetItem(Item srcItem, Item tgtItem){		
        Object wsmoSrcItem = srcItem.getWSMOElement();
        Object wsmoTgtItem = tgtItem.getWSMOElement();
		if ((wsmoSrcItem instanceof Type)&&(wsmoTgtItem instanceof Type)){
			createMapping((Type)wsmoSrcItem, (Type)wsmoTgtItem);
            //fireMapperChangedEvent();
			return;
		}
		if ((wsmoSrcItem instanceof Attribute)&&(wsmoTgtItem instanceof Attribute)){
			createMapping((Attribute)wsmoSrcItem, (Attribute)wsmoTgtItem);
            //fireMapperChangedEvent();
			return;
		}
		if ((wsmoSrcItem instanceof Type)&&(wsmoTgtItem instanceof Attribute)){
			createMapping((Type)wsmoSrcItem, (Attribute)wsmoTgtItem);
            //fireMapperChangedEvent();
			return;
		}
		if ((wsmoSrcItem instanceof Attribute)&&(wsmoTgtItem instanceof Type)){
			createMapping((Attribute)wsmoSrcItem, (Type)wsmoTgtItem);
			//fireMapperChangedEvent();
			return;
		}

		if ((wsmoSrcItem instanceof AttributeValueNode)&&(wsmoTgtItem instanceof AttributeValueNode)){
            
	        Set<AttributeCondition> srcConditions = new HashSet<AttributeCondition>();
	        Set<AttributeCondition> tgtConditions = new HashSet<AttributeCondition>();
			
			AttributeValueNode srcAVN = (AttributeValueNode)wsmoSrcItem;
			AttributeValueNode tgtAVN = (AttributeValueNode)wsmoTgtItem;
			 String id = srcAVN.getIdentifier().toString() + tgtAVN.getIdentifier().toString();
			Attribute2Attribute aaMapping = new Attribute2Attribute(new IRI(id), Direction.MAPPING);
			
			//src conditions
			srcConditions.add(getConditionForAttributeValueNode(srcAVN));
			//tgt conditions
			tgtConditions.add(getConditionForAttributeValueNode(tgtAVN));	
						
			aaMapping.setSource(new AttributeExpr(new AttributeId(srcAVN.getAttribute()), srcConditions));
			aaMapping.setTarget(new AttributeExpr(new AttributeId(tgtAVN.getAttribute()), tgtConditions));
			//for cashing
			setTargetAttributeValueNode(srcAVN, tgtAVN);
			aaMapping = mappings.addAttributeMapping(aaMapping);
            //fireMapperChangedEvent();
			return;
		}
        
        if ((wsmoSrcItem instanceof AttributeValueNode)&&(wsmoTgtItem instanceof Attribute)){

	        Set<AttributeCondition> srcConditions = new HashSet<AttributeCondition>();
        	        	
            AttributeValueNode srcAVN = (AttributeValueNode)wsmoSrcItem;
            Attribute tgtAVN = (Attribute)wsmoTgtItem;
             String id = srcAVN.getIdentifier().toString() + tgtAVN.getIdentifier().toString();
            Attribute2Attribute aaMapping = new Attribute2Attribute(new IRI(id), Direction.MAPPING);
            
            //src conditions
            srcConditions.add(getConditionForAttributeValueNode(srcAVN));
            
            aaMapping.setSource(new AttributeExpr(new AttributeId(srcAVN.getAttribute()), srcConditions));
            aaMapping.setTarget(new AttributeExpr(new AttributeId(tgtAVN), null));
            //for cashing
            setTargetAttribute(srcAVN.getAttribute(), tgtAVN);
            aaMapping = mappings.addAttributeMapping(aaMapping);
            //fireMapperChangedEvent();
 
            return;
        }       
                
        if ((wsmoSrcItem instanceof Attribute)&&(wsmoTgtItem instanceof AttributeValueNode)){
            
        	Set<AttributeCondition> tgtConditions = new HashSet<AttributeCondition>();
        	
            Attribute srcAVN = (Attribute)wsmoSrcItem;
            AttributeValueNode tgtAVN = (AttributeValueNode)wsmoTgtItem;
             String id = srcAVN.getIdentifier().toString() + tgtAVN.getIdentifier().toString();
            Attribute2Attribute aaMapping = new Attribute2Attribute(new IRI(id), Direction.MAPPING);
            
            //tgt conditions
            tgtConditions.add(getConditionForAttributeValueNode(tgtAVN));  
            
            aaMapping.setSource(new AttributeExpr(new AttributeId(srcAVN), null));
            aaMapping.setTarget(new AttributeExpr(new AttributeId(tgtAVN.getAttribute()), tgtConditions));
            //for cashing
            setTargetAttribute(srcAVN, tgtAVN.getAttribute());
            aaMapping = mappings.addAttributeMapping(aaMapping);
            //fireMapperChangedEvent();
            return;
        }       
        
        
        if ((wsmoSrcItem instanceof AttributeValueNode)&&(wsmoTgtItem instanceof Type)){
        	
        	 Set<AttributeCondition> srcConditions = new HashSet<AttributeCondition>();
        	
            AttributeValueNode srcAVN = (AttributeValueNode)wsmoSrcItem;
             String id = srcAVN.getIdentifier().toString() + WSMOUtil.getIdentifier((Type)wsmoTgtItem).toString();
            Attribute2Class acMapping = new Attribute2Class(new IRI(id), Direction.MAPPING);
            
            //src conditions
            srcConditions.add(getConditionForAttributeValueNode(srcAVN));
            
            acMapping.setSource(new AttributeExpr(new AttributeId(srcAVN.getAttribute()), srcConditions));
            acMapping.setTarget(new ClassExpr(new ClassId((Type)wsmoTgtItem), null));
            //for cashing
            setTargetConceptForAttributeValueNode(srcAVN, (Type)wsmoTgtItem);
            mappings.addAttributeConceptMapping(acMapping);
            
            //check if the owner of the AttributeValue and the target type are mapped
           	if (getMapingState(srcAVN.getAttribute().getConcept(), (Type)wsmoTgtItem)==NOT_MAPPED){
            		createMapping(srcAVN.getAttribute().getConcept(), (Type)wsmoTgtItem);
           	}            			                               
            //fireMapperChangedEvent();

            return;
        }
                                
		if ((wsmoSrcItem instanceof Type)&&(wsmoTgtItem instanceof AttributeValueNode)){
			
			Set<AttributeCondition> tgtConditions = new HashSet<AttributeCondition>();
			
			AttributeValueNode tgtAVN = (AttributeValueNode)wsmoTgtItem;
			String id = WSMOUtil.getIdentifier((Type)wsmoSrcItem) + tgtAVN.getIdentifier().toString();
			Class2Attribute caMapping = new Class2Attribute(new IRI(id), Direction.MAPPING);
			
			//tgt conditions
			tgtConditions.add(getConditionForAttributeValueNode(tgtAVN));
			
			caMapping.setSource(new ClassExpr(new ClassId((Type)wsmoSrcItem), null));
			caMapping.setTarget(new AttributeExpr(new AttributeId(tgtAVN.getAttribute()), tgtConditions));
			//for cashing
			setTargetAttributeValueNodeForConcept((Type)wsmoSrcItem, tgtAVN);
			mappings.addConceptAttributeMapping(caMapping);
			
            //check if the owner of the AttributeValue and the target type are mapped
           	if (getMapingState((Type)wsmoSrcItem, tgtAVN.getAttribute().getConcept())==NOT_MAPPED){
            		createMapping((Type)wsmoSrcItem, tgtAVN.getAttribute().getConcept());
           	}
            //fireMapperChangedEvent();

			return;
		}		
		
	}	
	
	private void addIfNotContained(Collection items, Collection bag){
		if (items==null)
			return;
		if (bag==null||bag.isEmpty()){
			bag = items;
		}
		Iterator it = items.iterator();
		while (it.hasNext()){
			Object item  = it.next();
			if (!bag.contains(item))
				bag.add(item);	
		}
	}
	
	/** Sets the mapping between a concept from the source and a concept from the target ontology 
	* @param concept the source concept - contains in this version a String denoting the name of source concept 
	* @param targetConcept the target concept - contains in this version a String denoting the name of target concept
	*/
	private void setTargetConcept(Type concept, Type targetConcept){
		Vector targets = ccMapper.get(concept);
		if (targets==null){
			Vector oneElementVector = new Vector();
			oneElementVector.add(targetConcept);
			ccMapper.put(concept, oneElementVector);
		}
		else{
			if (!targets.contains(targetConcept))
				targets.add(targetConcept);
		}		
	}
	/** Sets the mapping between an attribute from the source and an attribute from the target ontology
	 * @param attribute the source attribute
	 * @param targetAttribute the target attribute 
	 */
	private void setTargetAttribute(Attribute attribute, Attribute targetAttribute){
		
		if (attribute == null)
			return;			
		Vector targets = aaMapper.get(attribute);
		if (targets==null){
			Vector oneElementVector = new Vector();
			oneElementVector.add(targetAttribute);
			aaMapper.put(attribute, oneElementVector);
		}
		else
			if (!targets.contains(targetAttribute))
				targets.add(targetAttribute);
	}

	private void setTargetAttributeValueNode(AttributeValueNode attribute, AttributeValueNode targetAttribute){
		
		if (attribute == null)
			return;			
		ArrayList targets = avavMapper.get(attribute);
		if (targets==null){
			ArrayList oneElementArrayList = new ArrayList();
			oneElementArrayList.add(targetAttribute);
			avavMapper.put(attribute, oneElementArrayList);
		}
		else
			if (!targets.contains(targetAttribute))
				targets.add(targetAttribute);
	}

	
	
	private void setTargetAttributeForConcept(Type concept, Attribute targetAttribute){
		
		if (concept == null)
			return;			
		ArrayList<Attribute> targets = caMapper.get(concept);
		if (targets==null){
			ArrayList<Attribute> oneElementVector = new ArrayList<Attribute>();
			oneElementVector.add(targetAttribute);
			caMapper.put(concept, oneElementVector);
		}
		else
			if (!targets.contains(targetAttribute))
				targets.add(targetAttribute);
	}	

	private void setTargetAttributeValueNodeForConcept(Type concept, AttributeValueNode targetAttribute){
		
		if (concept == null)
			return;			
		ArrayList<AttributeValueNode> targets = cavMapper.get(concept);
		if (targets==null){
			ArrayList<AttributeValueNode> oneElementVector = new ArrayList<AttributeValueNode>();
			oneElementVector.add(targetAttribute);
			cavMapper.put(concept, oneElementVector);
		}
		else
			if (!targets.contains(targetAttribute))
				targets.add(targetAttribute);
	}	
	
	private void setTargetConceptForAttribute(Attribute attribute, Type targetConcept){
		
		if (attribute == null)
			return;			
		ArrayList<Type> targets = acMapper.get(attribute);
		if (targets==null){
			ArrayList<Type> oneElementVector = new ArrayList<Type>();
			oneElementVector.add(targetConcept);
			acMapper.put(attribute, oneElementVector);
		}
		else
			if (!targets.contains(targetConcept))
				targets.add(targetConcept);
	}	
	
	private void setTargetConceptForAttributeValueNode(AttributeValueNode attribute, Type targetConcept){
		
		if (attribute == null)
			return;			
		ArrayList<Type> targets = avcMapper.get(attribute);
		if (targets==null){
			ArrayList<Type> oneElementVector = new ArrayList<Type>();
			oneElementVector.add(targetConcept);
			avcMapper.put(attribute, oneElementVector);
		}
		else
			if (!targets.contains(targetConcept))
				targets.add(targetConcept);
	}	

	
	private void setTargetAttributes(Attribute attribute, Collection targets){
		Iterator it = targets.iterator();
		while (it.hasNext()){
			Attribute targetAttr= (Attribute)it.next();
			setTargetAttribute(attribute, targetAttr);
		}
	}
	
	private void setTargetAttributeValueNodes(AttributeValueNode attribute, Collection targets){
		Iterator it = targets.iterator();
		while (it.hasNext()){
			AttributeValueNode targetAttr= (AttributeValueNode)it.next();
			setTargetAttributeValueNode(attribute, targetAttr);
		}
	}
	
	private void setTargetAttributesForConcept(Type concept, Collection targets){
		Iterator it = targets.iterator();
		while (it.hasNext()){
			Attribute targetAttr= (Attribute)it.next();
			setTargetAttributeForConcept(concept, targetAttr);
		}
	}

	private void setTargetAttributeValueNodesForConcept(Type concept, Collection targets){
		Iterator it = targets.iterator();
		while (it.hasNext()){
			AttributeValueNode targetAttr= (AttributeValueNode)it.next();
			setTargetAttributeValueNodeForConcept(concept, targetAttr);
		}
	}
	/**
	 * Sets the manager for the source concepts
	 * @param srcConcepts the new source concept manager
	 */
	public void setSrcOntology(Ontology srcConcepts){
		this.srcOntology=srcConcepts;
		cleanMapings();
	}
	/**
	 * Retrieves the source concepts manager
	 * @return the source concept manager
	 */
	public Ontology getSrcOntology(){
		return srcOntology;
	}
	/**
	 * Retrieves the target concepts manager
	 * @return the target concept manager
	 */
	public Ontology getTgtOntology(){
		return tgtOntology;
	}
	/**
	 * Sets the manager for the target concepts
	 * @param tgtConcepts the new source concept manager
	 */
	public void setTgtOntology(Ontology tgtConcepts){
		this.tgtOntology=tgtConcepts;
		cleanMapings();
	}	
		   
    private void cleanMapings(){
        ccMapper = new Hashtable<Type, Vector<Type>>();
        aaMapper = new Hashtable<Attribute, Vector<Attribute>>();
        caMapper = new HashMap<Type, ArrayList<Attribute>>();
        acMapper = new HashMap<Attribute, ArrayList<Type>>(); 
        mappings = new Mappings();
        avavMapper = new HashMap<AttributeValueNode, ArrayList<AttributeValueNode>>();
        cavMapper = new HashMap<Type, ArrayList<AttributeValueNode>>();
        avcMapper = new HashMap<AttributeValueNode, ArrayList<Type>>(); 
        
        //fireMapperChangedEvent();
    }
    
	/**
	 * Checks if two given concepts (one from the source and the other form the target ontology) are mapped or not 
	 * @param srcConcept the source concept
	 * @param tgtConcept the target concept
	 * @return if the two given concepts are mapped it returns MAPPED, otherwise it returns NOT_MAPPED
	 */
	public int getMapingState(Type srcConcept, Type tgtConcept){
		Vector targetConcepts = ccMapper.get(srcConcept); 
		
		if (targetConcepts==null)
			return NOT_MAPPED;
		if (targetConcepts.contains(tgtConcept))
			return MAPPED;
		return NOT_MAPPED;
	}
	
	/**
	 * Checks if two given items (one from the source and the other form the target ontology) are mapped or not 
	 * @param srcConcept the source item
	 * @param tgtConcept the target item
	 * @return if the two given concepts are mapped it returns MAPPED, otherwise it returns NOT_MAPPED
	 */
	public int getItemsMapingState(Item srcConcept, Item tgtConcept){
		Collection<Object> targets = getTargetEntity(srcConcept.getWSMOElement());
		if (targets.contains(tgtConcept.getWSMOElement()))
			return MAPPED;

		return NOT_MAPPED;
	}	
	


    /**
     * @param attribute
     * @param attribute2
     * @return
     */
    private int getMapingState(Attribute attribute, Attribute attribute2) {
        Vector targetConcepts = aaMapper.get(attribute);
        if (targetConcepts==null)
            return NOT_MAPPED;
        if (targetConcepts.contains(attribute2))
            return MAPPED;
        return NOT_MAPPED;
    }
    
    private int getMapingState(AttributeValueNode attribute, AttributeValueNode attribute2) {
        Collection targetConcepts = avavMapper.get(attribute);
        if (targetConcepts==null)
            return NOT_MAPPED;
        if (targetConcepts.contains(attribute2))
            return MAPPED;
        return NOT_MAPPED;
    }

    /**
	 * Determines the structural relations between the source concept and target concepts. This relation is analyzed from the
	 * decomposition process point of view: no further decomposition is possible, only left (right) decomposition is possible, or 
	 * the decomposition is required on both sides. 
	 * @param srcConcept the source concept - for this version it is a String denoting the concept name
	 * @param tgtConcepts a vector of target concepts - contains String objects denoting the names of the target concepts
	 * @return the structural relation identifier: PRIMITIVE (no decomposition is possible);LEFT_BALANCED
	 * (left side requires decomposition); RIGHT_BALANCED (right side requires decomposition); DECOMPOSITION_REQUIRED
	 * (both left and right side require decomposition)
	 */
	public int internalStructuresRelations(Type srcConcept, Vector tgtConcepts){
	
        Collection src =  new ArrayList();
		if (srcConcept instanceof Concept)
           //src = ((Concept)srcConcept).listAttributes();
            src = WSMOUtil.listAllAttributes((Concept)srcConcept);
        
		Collection tgt = new ArrayList();
		
		Iterator it = tgtConcepts.iterator();		
		while (it.hasNext()){
            Item item = (Item)it.next();
            Type wsmoElement = (Type)item.getWSMOElement();
            if (wsmoElement instanceof Concept)
                tgt.addAll(WSMOUtil.listAllAttributes((Concept)wsmoElement));
		}
		
		if (src.isEmpty()&&tgt.isEmpty())
			return PRIMITIVE;		
		else
			if (src.isEmpty())
				return RIGHT_BALANCED;
			else
				if (tgt.isEmpty())
					return LEFT_BALANCED;
			else								
				return DECOMPOSITION_REQUIRED;
	}
	
	public int anonymousInternalStructureRelation(Item srcItem, Vector targetItems){
		
		if (targetItems.isEmpty())
			return PRIMITIVE;
		
		Iterator itTargetItems = targetItems.iterator();
		while (itTargetItems.hasNext()){
			if (itTargetItems.next() instanceof CompoundItem)
				if (srcItem instanceof CompoundItem)
					return DECOMPOSITION_REQUIRED;
				else
					return RIGHT_BALANCED;
		}
		if (srcItem instanceof CompoundItem)
			return LEFT_BALANCED;
		
		//if ((srcItem.getWSMOElement() instanceof Type)&&(((Item)targetItems.get(0)).getWSMOElement() instanceof Type))
			//return internalStructuresRelations((Type)srcItem.getWSMOElement(), targetItems);
	
		return PRIMITIVE;
	}

	/**
	 * Checks if the given pair of cencepts are mapped
	 * @param sourceConcept the source concept name
	 * @param targetConcept the target concept name
	 * @return true if the given concepts are mapped and false otherwise
	 */
	public boolean areMapped(Type sourceConcept, Type targetConcept){
		Collection targets = getTargetConcepts(sourceConcept);
		if (targets!=null)
			if (targets.contains(targetConcept))
				return true;
		 return false;
	}
	/**
	 * Checks if the given pair of cencept attribute (attribute plus owner) are mapped
	 * @param sourceAttribute the source concept attribute 
	 * @param targetAttribute the target concept attribute
	 * @return true if the given concept attributes are mapped and false otherwise
	 */
	public boolean areMapped(Attribute sourceAttribute, Attribute targetAttribute){
		Collection targets = getTargetAttributes(sourceAttribute);
		if (targets!=null)
			if (targets.contains(targetAttribute))
				return true;
		 return false;
	}
	/**
	 * Checks if two given attribute are elibible for being mapped. Two attributes are consider to be eligible for mapping 
	 * if they have already their ranges mapped one with each-other or if their ranges are eligible for map
	 * @param sourceAttribute the attribute form the source
	 * @param targetAttribute the atribute form the target
	 * @return true if the ranges of the two given attributes are already mapped and false otherwise. Returns false if 
	 * also  one (or both) of the attributes is null. 
	 */
	public boolean areEligible(Attribute sourceAttribute, Attribute targetAttribute){
		if ((sourceAttribute == null)||(targetAttribute == null))
			return false;
		if (areMapped((Concept)sourceAttribute.listTypes().iterator().next(),(Concept)targetAttribute.listTypes().iterator().next())) //TODO HACK HACK HACK
			return true;		
		if (areEligible((Concept)sourceAttribute.listTypes().iterator().next(), (Concept)targetAttribute.listTypes().iterator().next())) //TODO HACK HACK HACK
			return true;
		return false;
	}
	/**
	 * Checks if two given concepts are elibible for being mapped. Two concepts are consider to be eligible for mapping 
	 * if at least one pair of their attributes are formed by eligible attributes 
	 * @param sourceConcept the attribute form the source
	 * @param targetConcept the atribute form the target
	 * @return true if the concepts are already mapped or if at least one pair in the compound attributes is eligible and false otherwise.  
	 * Returns false if also one (or both) of the concepts is null. 
	 */	
	public boolean areEligible(Concept sourceConcept, Concept targetConcept){
		if ((sourceConcept == null)||(targetConcept == null))
			return false;
		if (areMapped(sourceConcept, targetConcept))
			return true;
		Collection sourceAttributes = sourceConcept.listAttributes();
		Collection targetAttributes = targetConcept.listAttributes();
		if ((sourceAttributes == null)||(targetAttributes == null))
			return false;	
				
		Iterator sourceIterator = sourceAttributes.iterator();
		while (sourceIterator.hasNext()){
			
			Attribute aSourceCAttr = (Attribute)sourceIterator.next();
			Iterator targetIterator = targetAttributes.iterator();
			while (targetIterator.hasNext()){
				Attribute aTargetCAttribute = (Attribute)targetIterator.next();
				if (areMapped(aSourceCAttr, aTargetCAttribute))
					return true;				
				if (areEligible(aSourceCAttr, aTargetCAttribute))
					return true;
				if (areEligible(sourceConcept, (Concept)aTargetCAttribute.listTypes().iterator().next())) //TODO HACK HACK HACK
					return true;						
			}
			if (areEligible((Concept)aSourceCAttr.listTypes().iterator().next(), targetConcept)){ //TODO HACK HACK HACK
				return true;
			}
		}
		
		Iterator targetIterator = targetAttributes.iterator();
		while (targetIterator.hasNext()){		
			Attribute aTargetCAttr = (Attribute)targetIterator.next();
			Iterator srcIterator = sourceAttributes.iterator();
			while (srcIterator.hasNext()){
				Attribute aSourceCAttribute = (Attribute)srcIterator.next();
				if (areMapped(aSourceCAttribute, aTargetCAttr))
					return true;				
				if (areEligible(aSourceCAttribute, aTargetCAttr))
					return true;
				if (areEligible((Concept)aSourceCAttribute.listTypes().iterator().next(), targetConcept)) // TODO HACK HACK HACK
					return true;						
			}
			if (areEligible(sourceConcept, (Concept)aTargetCAttr.listTypes().iterator().next())){ // TODO HACK HACK HACK
				return true;
			}
		}
		return false;
    }

    private void cashConcept2ConceptMappings(){
        ccMapper.clear();
        Iterator<Class2Class> cmIt = mappings.getConceptMappings().iterator();
        while (cmIt.hasNext()){
            Class2Class crtMapping = cmIt.next();
            ClassExpr srcExpr = crtMapping.getSource();
            ClassExpr tgtExpr = crtMapping.getTarget();
            if ((!srcExpr.isComplexExpression())&&(!tgtExpr.isComplexExpression()))
            	if (srcExpr.getId() instanceof ClassId && tgtExpr.getId() instanceof ClassId)
            		setTargetConcept(((ClassId)srcExpr.getId()).getType(), ((ClassId)tgtExpr.getId()).getType());
            	else{
            		setTargetConcept(WSMOUtil.createType(srcExpr.getId().plainText()), 
            				WSMOUtil.createType(tgtExpr.getId().plainText()));
            	}
        }
        
    }
    
    private void cashAttribute2AttributeMappings(){
        aaMapper.clear();
        Iterator<Attribute2Attribute> amIt = mappings.getAttributeMappings().iterator();
        while (amIt.hasNext()){
            Attribute2Attribute crtMapping = amIt.next();
            AttributeExpr srcExpr = crtMapping.getSource();
            AttributeExpr tgtExpr = crtMapping.getTarget();
            if ((!srcExpr.isComplexExpression())&&(!tgtExpr.isComplexExpression()))
                setTargetAttribute(((AttributeId)srcExpr.getId()).getAttribute(), ((AttributeId)tgtExpr.getId()).getAttribute());
        }        
    }
    
    public void updateAttribute2AttributeMappingCashing(){
        cashAttribute2AttributeMappings();
    }
    
    public void updateConcept2ConceptMappingCashing(){
        cashConcept2ConceptMappings();
    }
	
	private AttributeCondition getConditionForAttributeValueNode(AttributeValueNode itemAVN){
		AttributeCondition condition = null;
		ClassId tgtOwnerId = new ClassId(itemAVN.getAttribute().getConcept());
		AttributeId tgtSubject = new AttributeId(itemAVN.getAttribute());
        if (itemAVN.getValue() instanceof AnonymousInstance){
		//if (itemAVN.getInstance().getIdentifier().toString().startsWith("?")){
			ClassExpr tgtConditioner = new ClassExpr(new ClassId((Concept)itemAVN.getAttribute().listTypes().iterator().next()), null); //TODO HACK HACK HACK
			Restriction<ExpressionDefinition> res = new Restriction<ExpressionDefinition>(new ClassId((Concept)itemAVN.getAttribute().listTypes().iterator().next()), Comparator.EQUAL);
			//condition = new TypeCondition(tgtOwnerId, tgtSubject, tgtConditioner);
			condition = new TypeCondition<ExpressionDefinition>(res, tgtSubject);
		}else{
			if (itemAVN.getValue() instanceof DataValue){
				
				DataValueIdPlaceholder tgtConditioner = new DataValueIdPlaceholder((DataValue)itemAVN.getValue());
				Restriction<DataValueIdPlaceholder> res = new Restriction<DataValueIdPlaceholder>(tgtConditioner, Comparator.EQUAL);
				condition = new ValueCondition<DataValueIdPlaceholder>(res, tgtSubject);
				//condition = new ValueCondition(tgtOwnerId, tgtSubject, itemAVN.getValue().toString());
			}
			else{
				IndividualId tgtConditioner = new IndividualId(itemAVN.getValue());
				Restriction<IndividualId> res = new Restriction<IndividualId>(tgtConditioner, Comparator.EQUAL);
				//condition = new ValueCondition(tgtOwnerId, tgtSubject, tgtConditioner);
				condition = new ValueCondition<IndividualId>(res, tgtSubject);
			}
		}
		return condition;
	}
	
	private Collection<Attribute> getAttributesFromAVN(Collection<AttributeValueNode> collection){
		Collection<Attribute> result = new Vector<Attribute>();
		Iterator<AttributeValueNode> it = collection.iterator();
		while (it.hasNext())
			result.add(it.next().getAttribute());
		return result;
	}

    public void removeAllConceptToConceptMappings() {
        mappings.getConceptMappings().clear();
        //updateConcept2ConceptMappingCashing();
        ccMapper.clear();
        //fireMapperChangedEvent();
    }
    
    public void removeAllAttributeToAttributeMappings() {
        mappings.getAttributeMappings().clear();
        //updateAttribute2AttributeMappingCashing();
        aaMapper.clear();
        avavMapper.clear();
        //fireMapperChangedEvent();
    }
    
    public void removeAllAttributeToConceptMappings() {
        mappings.getAttributeConceptMappings().clear();
        acMapper.clear();
        avcMapper.clear();
        //fireMapperChangedEvent();
    }
    
    public void removeAllConceptToAttributeMappings() {
        mappings.getConceptAttributeMappings().clear();
        caMapper.clear();
        cavMapper.clear();
        //fireMapperChangedEvent();
    }

    public void removeConceptToConceptMappings(List<MappingRule> rules) {
        for (MappingRule r : rules){
            mappings.getConceptMappings().remove(r);            
        }
        updateConcept2ConceptMappingCashing();
        //fireMapperChangedEvent();
    }
    
    public void removeAttributeToAttributeMappings(List<MappingRule> rules) {
        for (MappingRule r : rules){
            mappings.getAttributeMappings().remove(r);
            
        }
        updateAttribute2AttributeMappingCashing();
        //fireMapperChangedEvent();
    }
    
    public void removeAttributeToConceptMappings(List<MappingRule> rules) {
        for (MappingRule r : rules){
            mappings.getAttributeConceptMappings().remove(r);
        }
        //fireMapperChangedEvent();
    }

    public void removeConceptToAttributeMappings(List<MappingRule> rules) {
        for (MappingRule r : rules){
            mappings.getConceptAttributeMappings().remove(r);
        }
        //fireMapperChangedEvent();
    }
    
    public Collection<Class2Class> getConceptToConceptMappings(){
        return mappings.getConceptMappings();
    }
    
    public Collection<Attribute2Attribute> getAttributeToAttributeMappings(){
        return mappings.getAttributeMappings();
    }
    
    public Collection<Attribute2Class> getAttributeToConceptMappings(){
        return mappings.getAttributeConceptMappings();
    }
    
    public Collection<Class2Attribute> getConceptToAttributeMappings(){
        return mappings.getConceptAttributeMappings();
    }

	public Mappings getMappings() {
		return mappings;
	}

	public void setMappings(Mappings mappings) {
		this.mappings = mappings;
		updateConcept2ConceptMappingCashing();
	}
}