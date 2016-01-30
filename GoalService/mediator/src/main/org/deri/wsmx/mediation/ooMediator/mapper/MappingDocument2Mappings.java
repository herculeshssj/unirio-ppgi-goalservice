/*
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.deri.wsmx.mediation.ooMediator.mapper;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.wsmx.mediation.ooMediator.mapper.mappings.AttributeId;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.ClassId;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.DataValueIdPlaceholder;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.IndividualId;
import org.deri.wsmx.mediation.ooMediator.util.MappingUtil;
import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.mediation.language.objectmodel.api.AttributeExpr;
import org.omwg.mediation.language.objectmodel.api.ClassExpr;
import org.omwg.mediation.language.objectmodel.api.ComplexExpression;
import org.omwg.mediation.language.objectmodel.api.ExpressionDefinition;
import org.omwg.mediation.language.objectmodel.api.IRI;
import org.omwg.mediation.language.objectmodel.api.Id;
import org.omwg.mediation.language.objectmodel.api.MappingDocument;
import org.omwg.mediation.language.objectmodel.api.MappingRule;
import org.omwg.mediation.language.objectmodel.api.Path;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeOccurenceCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeTypeCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeValueCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.ClassCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.DomainAttributeCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.Restriction;
import org.omwg.mediation.language.objectmodel.api.conditions.TypeCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.ValueCondition;
import org.omwg.mediation.language.objectmodel.api.rules.Attribute2Attribute;
import org.omwg.mediation.language.objectmodel.api.rules.Attribute2Class;
import org.omwg.mediation.language.objectmodel.api.rules.Class2Attribute;
import org.omwg.mediation.language.objectmodel.api.rules.Class2Class;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Ontology;
import org.omwg.ontology.Type;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 16-Jan-2007
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/mapper/MappingDocument2Mappings.java,v $, 
 * @version $Revision: 1.2 $ $Date: 2007-12-12 12:52:27 $
 */
public class MappingDocument2Mappings {

	public Mappings mappingDocument2Mappings(Ontology src, Ontology tgt, MappingDocument md){
		
    	Mappings mappings = new Mappings(src, tgt);
    	
    	for (MappingRule r : md.getRules()){			
    		if (r instanceof Class2Class){
    			mappings.addConceptMapping(convertClass2Class(src, tgt, (Class2Class)r));    			
    		}else
    		if (r instanceof Attribute2Attribute){
    			mappings.addAttributeMapping(convertAttribute2Attribute(src, tgt, (Attribute2Attribute)r));
    		}else
    		if (r instanceof Attribute2Class){
    			mappings.addAttributeConceptMapping(convertAttribute2Class(src, tgt, (Attribute2Class)r));
    		}else
    		if (r instanceof Class2Attribute){
    			mappings.addConceptAttributeMapping(convertClass2Attribute(src, tgt, (Class2Attribute)r));
    		}
    	}
    	
    	return mappings;
		
	}
	 
	private Attribute2Attribute convertAttribute2Attribute(Ontology src, Ontology tgt, Attribute2Attribute mapping) {
		IRI ruleId = mapping.getId();
		if (ruleId == null){
			//ruleId = new IRI(mapping.getSource().getId().plainText()+mapping.getTarget().getId().plainText());
			ruleId = new IRI(MappingUtil.generatetRuleId(mapping.getSource(), mapping.getTarget()));
		}
		Attribute2Attribute convertedMapping = new Attribute2Attribute(ruleId, mapping.getOneTwoWays(), 
				convertAttrExpr(src, tgt, mapping.getSource()), convertAttrExpr(src, tgt, mapping.getTarget()), mapping.getMeasure());
		return convertedMapping;
	}

	private Class2Attribute convertClass2Attribute(Ontology src, Ontology tgt, Class2Attribute mapping) {
		IRI ruleId = mapping.getId();
		if (ruleId == null){
			//ruleId = new IRI(mapping.getSource().getId().plainText()+mapping.getTarget().getId().plainText());
			ruleId = new IRI(MappingUtil.generatetRuleId(mapping.getSource(), mapping.getTarget()));
		}
		Class2Attribute convertedMapping = new Class2Attribute(ruleId, mapping.getOneTwoWays(), 
				convertClassExpr(src, tgt, mapping.getSource()), convertAttrExpr(src, tgt, mapping.getTarget()), mapping.getMeasure());
		return convertedMapping;
	}

	private Attribute2Class convertAttribute2Class(Ontology src, Ontology tgt, Attribute2Class mapping) {
		IRI ruleId = mapping.getId();
		if (ruleId == null){
			//ruleId = new IRI(mapping.getSource().getId().plainText()+mapping.getTarget().getId().plainText());
			ruleId = new IRI(MappingUtil.generatetRuleId(mapping.getSource(), mapping.getTarget()));
		}		
		Attribute2Class convertedMapping = new Attribute2Class(ruleId, mapping.getOneTwoWays(), 
				convertAttrExpr(src, tgt, mapping.getSource()), convertClassExpr(src, tgt, mapping.getTarget()), mapping.getMeasure());
		return convertedMapping;
	}

	private Class2Class convertClass2Class(Ontology src, Ontology tgt, Class2Class mapping){
		IRI ruleId = mapping.getId();
		if (ruleId == null){
			//ruleId = new IRI(mapping.getSource().getId().plainText()+mapping.getTarget().getId().plainText());
			ruleId = new IRI(MappingUtil.generatetRuleId(mapping.getSource(), mapping.getTarget()));
		}
		Class2Class convertedMapping = new Class2Class(ruleId, mapping.getOneTwoWays(), 
				convertClassExpr(src, tgt, mapping.getSource()), convertClassExpr(src, tgt, mapping.getTarget()), mapping.getMeasure());
		return convertedMapping;
	}
	
	private ClassExpr convertClassExpr(Ontology src, Ontology tgt, ClassExpr expr) {
		
		ClassExpr result = new ClassExpr(convertExpresionDefinition(src, tgt, expr.getExpresionDefinition()), convertClassConditions(src, tgt, expr.getConditions()));
		
		return result;
	}

	private AttributeExpr convertAttrExpr(Ontology src, Ontology tgt, AttributeExpr expr) {
		
		AttributeExpr result;
		
		if (expr.getTransf()==null)
			result = new AttributeExpr(convertExpresionDefinition(src, tgt, expr.getExpresionDefinition()), convertAttributeConditions(src, tgt, expr.getConditions()));
		else
			result = new AttributeExpr(convertExpresionDefinition(src, tgt, expr.getExpresionDefinition()), convertAttributeConditions(src, tgt, expr.getConditions()), expr.getTransf());
		return result;
	}
	
	
	private Collection<AttributeCondition> convertAttributeConditions(Ontology src, Ontology tgt, Set<AttributeCondition> conditions) {
		Collection<AttributeCondition> result = new HashSet<AttributeCondition>();
		if (conditions == null)
			return result;
		for (AttributeCondition cc : conditions){
					result.add(convertAttributeCondition(src, tgt, cc));
		}
		return result;	}

	private Collection<ClassCondition> convertClassConditions(Ontology src, Ontology tgt, Set<ClassCondition> conditions) {
		
		Collection<ClassCondition> result = new HashSet<ClassCondition>();
		if (conditions == null)
			return result;
		for (ClassCondition cc : conditions){
					result.add(convertClassCondition(src, tgt, cc));
		}
		return result;
	}

	private AttributeCondition convertAttributeCondition(Ontology src, Ontology tgt, AttributeCondition ac) {
		if (ac instanceof TypeCondition){
			TypeCondition tc = (TypeCondition)ac;
			TypeCondition new_tc = new TypeCondition(convertRestriction(src, tgt, tc.getRestriction()),	convertExpresionDefinition(src, tgt, tc.getTarget()));			
			return new_tc;
		}
		if (ac instanceof ValueCondition){
			ValueCondition vc = (ValueCondition)ac;
			ValueCondition new_vc = new ValueCondition(convertRestriction(src, tgt, vc.getRestriction()),	convertExpresionDefinition(src, tgt, vc.getTarget()));			
			return new_vc;
		}
		
		if (ac instanceof DomainAttributeCondition){
			DomainAttributeCondition dac = (DomainAttributeCondition)ac;
			DomainAttributeCondition new_dac = new DomainAttributeCondition(convertRestriction(src, tgt, dac.getRestriction()),	convertExpresionDefinition(src, tgt, dac.getTarget()));			
			return new_dac;
		}
		
		
		return ac;
	}
		
	private ClassCondition convertClassCondition(Ontology src, Ontology tgt, ClassCondition cc){
		if (cc instanceof AttributeOccurenceCondition){
			AttributeOccurenceCondition aoc = (AttributeOccurenceCondition)cc;
			AttributeOccurenceCondition new_aoc = new AttributeOccurenceCondition(convertPath(src, tgt, aoc.getAttributeId()), 
											convertRestriction(src, tgt, aoc.getRestriction()), convertExpresionDefinition(src, tgt, aoc.getTarget()));
			return new_aoc;
		}
		if (cc instanceof AttributeTypeCondition){
			AttributeTypeCondition atc = (AttributeTypeCondition)cc;
			AttributeTypeCondition new_atc = new AttributeTypeCondition(convertPath(src, tgt, atc.getAttributeId()), 
										convertRestriction(src, tgt, atc.getRestriction()), convertExpresionDefinition(src, tgt, atc.getTarget()));
			return new_atc;
		}
		if (cc instanceof AttributeValueCondition){
			AttributeValueCondition avc = (AttributeValueCondition)cc;
			AttributeValueCondition new_avc = new AttributeValueCondition(convertPath(src, tgt, avc.getAttributeId()), 
										convertRestriction(src, tgt, avc.getRestriction()), convertExpresionDefinition(src, tgt, avc.getTarget()));
			return new_avc;
		}
		
		return cc;		
	}

	private Restriction convertRestriction(Ontology src, Ontology tgt, Restriction restriction) {

		if (restriction.isPath())
			return new Restriction(convertPath(src, tgt, restriction.getPathValue()), restriction.getComparator());
		else
			return 	new Restriction(convertExpresionDefinition(src, tgt, restriction.getValue()), restriction.getComparator());
	}

	private Path convertPath(Ontology src, Ontology tgt, Path attributeId) {
		
		List<Id> idsList = new ArrayList<Id>();
		Path crtP = attributeId;
		while (crtP.hasNext()){
			idsList.add(convertId(src, tgt, crtP.getUri()));
			crtP = crtP.getNext();
		}
		idsList.add(crtP.getUri());
		Id[] ids = new Id[idsList.size()];
		return new Path(idsList.toArray(ids)); 
		
	}

	private ExpressionDefinition convertExpresionDefinition(Ontology src, Ontology tgt, ExpressionDefinition expresionDefinition) {
		
		if (expresionDefinition.isComplexExpression()){
			return convertComplexExpression(src, tgt, (ComplexExpression)expresionDefinition);
		}
		else{			
			return convertId(src, tgt, (Id)expresionDefinition);
		}
	}

	public Id convertId(Ontology src, Ontology tgt, Id id){
		if (id instanceof org.omwg.mediation.language.objectmodel.api.ClassExpr.ClassId){
			ClassId newClassId = new ClassId(getType(id));
			return newClassId;
		}
		if (id instanceof org.omwg.mediation.language.objectmodel.api.AttributeExpr.AttributeId){
			AttributeId newAttributeId = new AttributeId(getAttribute(id, src, tgt));
			return newAttributeId;
		}
		if (id instanceof org.omwg.mediation.language.objectmodel.api.InstanceExpr.InstanceId){
			IndividualId newInstanceId = new IndividualId(WSMOUtil.createInstance((id).plainText()));
			return newInstanceId;
		}
		if (id instanceof org.omwg.mediation.language.objectmodel.api.SimpleValue){
			try{			
				Instance instance = findInstance(src, id.plainText());
				if (instance==null){
                    instance = findInstance(tgt, id.plainText());
                }
				if (instance == null){
					DataValueIdPlaceholder newPlaceHolderId = new DataValueIdPlaceholder(WSMOUtil.createDataValue((id).plainText()));
					return newPlaceHolderId;
				}
				else
					return new IndividualId(instance);
			}
			catch(java.lang.IllegalArgumentException e){
				DataValueIdPlaceholder newPlaceHolderId = new DataValueIdPlaceholder(WSMOUtil.createDataValue((id).plainText()));
				return newPlaceHolderId;
			}
		}		
		return id;
	}
	
	private ExpressionDefinition convertComplexExpression(Ontology src, Ontology tgt, ComplexExpression expression) {
		Collection<ExpressionDefinition> subExpr = new ArrayList<ExpressionDefinition>();
		for (ExpressionDefinition ed : expression.getSubExpressions()){
			subExpr.add(convertExpresionDefinition(src, tgt, ed));
		}
		ComplexExpression ce = new ComplexExpression(subExpr, expression.getOperator());
		return ce;
	}

	public Attribute getAttribute(Id id, Ontology srcOntology, Ontology tgtOntology) {
		
		if (id instanceof AttributeId)
			return ((AttributeId)id).getAttribute();
		
		String attributeStringId = null;
		String conceptStringId = null;
		
		if (id.plainText().contains("@")){
			attributeStringId = id.plainText().substring(0, id.plainText().indexOf("@"));
			conceptStringId = id.plainText().substring(id.plainText().indexOf("@")+1);
		}
		else{
			attributeStringId = id.plainText();
		}
		
		if (srcOntology != null){			
			if (conceptStringId!=null){
				Concept c = findConcept(srcOntology, conceptStringId);
				if (c!=null){				
				  	 Set attributes = ((Concept)c).findAttributes(WSMOUtil.createIRI(attributeStringId));
					 if (attributes!=null && !attributes.isEmpty()){
						 return (Attribute)attributes.iterator().next();
					 }		
				}
			}
			else{
				for (Object c : srcOntology.listConcepts()){					 
					 Set attributes = ((Concept)c).findAttributes(WSMOUtil.createIRI(attributeStringId));
					 if (attributes!=null && !attributes.isEmpty()){
						 return (Attribute)attributes.iterator().next();
					 }					 
				}
			}
		}
		if (tgtOntology != null){
			if (conceptStringId!=null){
                Concept c = findConcept(tgtOntology, conceptStringId);
				if (c!=null){
					Set attributes = ((Concept)c).findAttributes(WSMOUtil.createIRI(attributeStringId));
					if (attributes!=null && !attributes.isEmpty()){
						 return (Attribute)attributes.iterator().next();
					 }		
				}
			}
			else{
				for (Object c : tgtOntology.listConcepts()){				
				 	Set attributes = ((Concept)c).findAttributes(WSMOUtil.createIRI(attributeStringId));
				 	if (attributes!=null && !attributes.isEmpty()){
				 		return (Attribute)attributes.iterator().next();
				 	}				
				}
			}
		}
		return null;
	}
	
	protected Type getType(Id id){
		if (id==null)
			return null;
		return WSMOUtil.createType(id.plainText());
	}
	

    protected Concept findConcept(Ontology theOntology, String theId){
        org.wsmo.common.IRI conceptIRI = WSMOUtil.createIRI(theId);
        
        Set <Ontology> ontologies = getImportedOntologies(theOntology);
        ontologies.add(theOntology);
        for (Ontology o : ontologies){
            try{
                Concept c = o.findConcept(conceptIRI);
                if (c != null){
                    return c;
                }
            }
            catch (Throwable t){
            }
        }
        
        return null;
    }
    
    protected Instance findInstance(Ontology theOntology, String theId){
        org.wsmo.common.IRI instanceIRI = WSMOUtil.createIRI(theId);
        
        Set <Ontology> ontologies = getImportedOntologies(theOntology);
        ontologies.add(theOntology);
        for (Ontology o : ontologies){
            try{
                Instance i = o.findInstance(instanceIRI);
                if (i != null){
                    return i;
                }
            }
            catch (Throwable t){
            }
        }
        
        return null;
    }
    
    public static Set <Ontology> getImportedOntologies(Ontology theOntology){
        Set <Ontology> result = new HashSet <Ontology> ();
        getImportedOntologies(theOntology, result);
        return result;
    }
    
    private static void getImportedOntologies(Ontology theOntology, Set<Ontology> theImportedOntologies){
        for (Ontology importedOntology : theOntology.listOntologies()) {
            if (!theImportedOntologies.contains(importedOntology)) {
                theImportedOntologies.add(importedOntology);
                getImportedOntologies(importedOntology, theImportedOntologies);
            }
        }
    }
}
