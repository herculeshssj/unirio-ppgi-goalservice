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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deri.wsmx.mediation.ooMediator.mapper.mappings.AttributeId;
import org.omwg.mediation.language.objectmodel.api.AttributeExpr;
import org.omwg.mediation.language.objectmodel.api.ComplexExpression;
import org.omwg.mediation.language.objectmodel.api.ExpressionDefinition;
import org.omwg.mediation.language.objectmodel.api.Id;
import org.omwg.mediation.language.objectmodel.api.rules.Attribute2Attribute;
import org.omwg.mediation.language.objectmodel.api.rules.Attribute2Class;
import org.omwg.mediation.language.objectmodel.api.rules.Class2Attribute;
import org.omwg.mediation.language.objectmodel.api.rules.Class2Class;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Ontology;
import org.wsmo.common.Identifier;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 26-Mar-2005
 * Committed by $Author: adrian.mocan $
 * 
 * $Source: /var/repository/wsmx-datamediator/src/main/java/org/deri/wsmx/mediation/ooMediator/mapper/Mappings.java,v $, 
 * @version $Revision: 1.3 $ $Date: 2008/01/10 12:03:23 $
 */

public class Mappings {

	private ArrayList<Class2Class> conceptMappings = new ArrayList<Class2Class>();
	private ArrayList<Attribute2Attribute> attributeMappings = new ArrayList<Attribute2Attribute>();
	private ArrayList<Class2Attribute> conceptAttributeMappings = new ArrayList<Class2Attribute>();
	private ArrayList<Attribute2Class> attributeConceptMappings = new ArrayList<Attribute2Class>();

    private Ontology srcOntology = null;
    private Ontology tgtOntology = null; 

	/**
	 * @param srcOntology
	 * @param tgtOntology
	 */
	public Mappings(Ontology srcOntology,
			Ontology tgtOntology) {
		this.srcOntology = srcOntology;
		this.tgtOntology = tgtOntology;
	}
	
	public Mappings(){		
	}
	/**
	 * @return Returns the attributeMappings.
	 */
	public ArrayList<Attribute2Attribute> getAttributeMappings() {
		return attributeMappings;
	}
	/**
	 * @param attributeMappings The attributeMappings to set.
	 */
	public void setAttributeMappings(
			ArrayList<Attribute2Attribute> attributeMappings) {
		this.attributeMappings = attributeMappings;
	}
	
	/**
	 * @param attributeMappings The attributeMappings to add.
	 */
	public void addAttributeMappings(
			ArrayList<Attribute2Attribute> attributeMappings) {
		addIfNotContained(attributeMappings, this.attributeMappings);
	}
	
	/**
	 * @return Returns the conceptMappings.
	 */
	public ArrayList<Class2Class> getConceptMappings() {
		return conceptMappings;
	}
	/**
	 * @param conceptMappings The conceptMappings to set.
	 */
	public void setConceptMappings(ArrayList<Class2Class> conceptMappings) {
		this.conceptMappings = conceptMappings;
	}
	
	/**
	 * @param conceptMappings The conceptMappings to add.
	 */
	public void addConceptMappings(ArrayList<Class2Class> conceptMappings) {		
		addIfNotContained(conceptMappings, this.conceptMappings);		
	}

	
	/**
	 * @return Returns the srcOntology.
	 */
	public Ontology getSrcOntology() {
		return srcOntology;
	}
	/**
	 * @param srcOntology The srcOntology to set.
	 */
	public void setSrcOntology(Ontology srcOntology) {
		this.srcOntology = srcOntology;
	}
	/**
	 * @return Returns the tgtOntology.
	 */
	public Ontology getTgtOntology() {
		return tgtOntology;
	}
	/**
	 * @param tgtOntology The tgtOntology to set.
	 */
	public void setTgtOntology(Ontology tgtOntology) {
		this.tgtOntology = tgtOntology;
	}
	/**
	 * @return Returns the attributeConceptMappings.
	 */
	public ArrayList<Attribute2Class> getAttributeConceptMappings() {
		return attributeConceptMappings;
	}
	/**
	 * @param attributeConceptMappings The attributeConceptMappings to set.
	 */
	public void setAttributeConceptMappings(
			ArrayList<Attribute2Class> attributeConceptMappings) {
		this.attributeConceptMappings = attributeConceptMappings;
	}
	
	/**
	 * @param attributeConceptMappings The attributeConceptMappings to add.
	 */
	public void addAttributeConceptMappings(
			ArrayList<Attribute2Class> attributeConceptMappings) {
		addIfNotContained(attributeConceptMappings, this.attributeConceptMappings);		
	}
	
	/**
	 * @return Returns the conceptAttributeMappings.
	 */
	public ArrayList<Class2Attribute> getConceptAttributeMappings() {
		return conceptAttributeMappings;
	}
	/**
	 * @param conceptAttributeMappings The conceptAttributeMappings to set.
	 */
	public void setConceptAttributeMappings(
			ArrayList<Class2Attribute> conceptAttributeMappings) {
		this.conceptAttributeMappings = conceptAttributeMappings;
	}
	
	/**
	 * @param conceptAttributeMappings The conceptAttributeMappings to add.
	 */
	public void addConceptAttributeMappings(
			ArrayList<Class2Attribute> conceptAttributeMappings) {
		addIfNotContained(conceptAttributeMappings, this.conceptAttributeMappings);
	}
	
	public void addConceptMapping(Class2Class class2ClassMapping){
		if (!conceptMappings.contains(class2ClassMapping))
			conceptMappings.add(class2ClassMapping);
	}
	public Attribute2Attribute addAttributeMapping(Attribute2Attribute attribute2AttributeMapping){
		if (!attributeMappings.contains(attribute2AttributeMapping)){
			attributeMappings.add(attribute2AttributeMapping);
            return attribute2AttributeMapping;
        }
        return attributeMappings.get(attributeMappings.indexOf(attribute2AttributeMapping));
        
	}
	public void addConceptAttributeMapping(Class2Attribute class2AttributeMapping){
		if (!conceptAttributeMappings.contains(class2AttributeMapping))
			conceptAttributeMappings.add(class2AttributeMapping);
	}
	public void addAttributeConceptMapping(Attribute2Class attribute2ClassMapping){
		if (!attributeConceptMappings.contains(attribute2ClassMapping))
			attributeConceptMappings.add(attribute2ClassMapping);
	}
    /**
     * @param attribute
     * @param attribute2
     * @return
     */
    public Attribute2Attribute getAttributeMapping(Attribute attribute1, Attribute attribute2) {
        Iterator<Attribute2Attribute> it = getAttributeMappings().iterator();
        while (it.hasNext()){
            Attribute2Attribute crtAM = it.next();
            Collection<Attribute> srcAttributes = new ArrayList<Attribute>();
            Collection<Attribute> tgtAttributes = new ArrayList<Attribute>();
            if (!crtAM.getSource().isComplexExpression()){
                srcAttributes.add(((AttributeId)crtAM.getSource().getId()).getAttribute());
            }
            if (!crtAM.getTarget().isComplexExpression()){
                tgtAttributes.add(((AttributeId)crtAM.getTarget().getId()).getAttribute());
            }            
            if (srcAttributes.contains(attribute1) && tgtAttributes.contains(attribute2))
                return crtAM;
        }
        return null;
    }
	
	private void addIfNotContained(Collection source, Collection target){
		for (Object src: source){
			if (!target.contains(src)){
				target.add(src);
			}
		}
	}

    public boolean containsMappingForAttribute(Identifier theAttributeIdentifier, Set<Concept> theConcepts) {
        for (Attribute2Attribute mapping : attributeMappings){
            AttributeExpr expr = mapping.getSource();
            if (expr.isComplexExpression()){
                boolean contains = complexExpressionContainsAttribute(expr.getExpresionDefinition(), theAttributeIdentifier, theConcepts);
                if (contains){
                    return true;
                }
            }
            else{
                Attribute attribute = ((AttributeId) expr.getId()).getAttribute();
                if (attribute.getIdentifier().toString().equals(theAttributeIdentifier.toString()) && theConcepts.contains(attribute.getConcept())){
                    return true;
                }
            }
        }
        for (Attribute2Class mapping : attributeConceptMappings){
            AttributeExpr expr = mapping.getSource();
            if (expr.isComplexExpression()){
                boolean contains = complexExpressionContainsAttribute(expr.getExpresionDefinition(), theAttributeIdentifier, theConcepts);
                if (contains){
                    return true;
                }
            }
            else{
                Attribute attribute = ((AttributeId) expr.getId()).getAttribute();
                if (attribute.getIdentifier().toString().equals(theAttributeIdentifier.toString()) && theConcepts.contains(attribute.getConcept())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean complexExpressionContainsAttribute(ExpressionDefinition expressionDefinition, Identifier theAttributeIdentifier, Set<Concept> theConcepts) {
        if (expressionDefinition instanceof ComplexExpression){
            for (ExpressionDefinition definition : ((ComplexExpression) expressionDefinition).getSubExpressions()){
                boolean contains = complexExpressionContainsAttribute(definition, theAttributeIdentifier, theConcepts);
                if (contains){
                    return true;
                }
            }
        }
        else if (expressionDefinition instanceof AttributeId){
            Attribute attribute = ((AttributeId) expressionDefinition).getAttribute();
            if (attribute.getIdentifier().toString().equals(theAttributeIdentifier.toString()) && theConcepts.contains(attribute.getConcept())){
                return true;
            }
        }
        return false;
    }
}

