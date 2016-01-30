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

package org.deri.wsmx.mediation.ooMediator.wsml;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.wsmx.mediation.ooMediator.mapper.MappingDocument2Mappings;
import org.deri.wsmx.mediation.ooMediator.mapper.Mappings;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.AttributeId;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.ClassId;
import org.deri.wsmx.mediation.ooMediator.mapper.mappings.DataValueIdPlaceholder;
import org.deri.wsmx.mediation.ooMediator.util.IndexGenerator;
import org.deri.wsmx.mediation.ooMediator.util.WSMOUtil;
import org.omwg.logicalexpression.Atom;
import org.omwg.logicalexpression.AttributeConstraintMolecule;
import org.omwg.logicalexpression.AttributeInferenceMolecule;
import org.omwg.logicalexpression.AttributeValueMolecule;
import org.omwg.logicalexpression.CompoundMolecule;
import org.omwg.logicalexpression.Conjunction;
import org.omwg.logicalexpression.LogicProgrammingRule;
import org.omwg.logicalexpression.LogicalExpression;
import org.omwg.logicalexpression.MembershipMolecule;
import org.omwg.logicalexpression.Molecule;
import org.omwg.logicalexpression.SubConceptMolecule;
import org.omwg.logicalexpression.terms.ConstructedTerm;
import org.omwg.logicalexpression.terms.Term;
import org.omwg.mediation.language.objectmodel.api.AttributeExpr;
import org.omwg.mediation.language.objectmodel.api.ClassExpr;
import org.omwg.mediation.language.objectmodel.api.ComplexExpression;
import org.omwg.mediation.language.objectmodel.api.ExpressionDefinition;
import org.omwg.mediation.language.objectmodel.api.Id;
import org.omwg.mediation.language.objectmodel.api.MappingRule;
import org.omwg.mediation.language.objectmodel.api.Path;
import org.omwg.mediation.language.objectmodel.api.SimpleValue;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeOccurenceCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeTypeCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeValueCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.ClassCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.Condition;
import org.omwg.mediation.language.objectmodel.api.conditions.Restriction;
import org.omwg.mediation.language.objectmodel.api.conditions.TypeCondition;
import org.omwg.mediation.language.objectmodel.api.conditions.ValueCondition;
import org.omwg.mediation.language.objectmodel.api.rules.Attribute2Attribute;
import org.omwg.mediation.language.objectmodel.api.rules.Attribute2Class;
import org.omwg.mediation.language.objectmodel.api.rules.Class2Attribute;
import org.omwg.mediation.language.objectmodel.api.rules.Class2Class;
import org.omwg.ontology.Attribute;
import org.omwg.ontology.Axiom;
import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Type;
import org.omwg.ontology.Value;
import org.omwg.ontology.Variable;
import org.wsmo.common.IRI;
import org.wsmo.common.Identifier;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 02-May-2006
 * Committed by $Author: adrian.mocan $
 * 
 * $Source: /var/repository/wsmx-datamediator/src/main/java/org/deri/wsmx/mediation/ooMediator/wsml/WSMLRuleGenerator.java,v $, 
 * @version $Revision: 1.5 $ $Date: 2008/02/26 03:20:27 $
 */

public class WSMLRuleGenerator {
    
    public static IRI function = WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#mediated");
    
    private static IRI mappedFlag = WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#mappedConcepts");
    
    private Set<Concept> requiredConceptsDefinitions = new HashSet<Concept>();
    private Set<Instance> requiredInstances = new HashSet<Instance>();
    private Set<Axiom> assignements = null; 
    
    private Mappings mappings = null;
    
    public static final String serviceCallConceptName = "ServiceCall";
    
    private Concept serviceCall = null;

    private Set <Identifier> relevantSourceConcepts = new HashSet <Identifier> ();
    private Map <Identifier, Set <Identifier>> relevantSourceAttributes = new HashMap <Identifier, Set <Identifier>> ();

    public WSMLRuleGenerator(){
        function = WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#mediated" + IndexGenerator.getStringMediatedIndex());
        
        //Create the transformation services related concepts 
        serviceCall = WSMOUtil.createConcept(MediationWSML2Reasoner.transformationServicesSpace + "#" + serviceCallConceptName);
		requiredConceptsDefinitions.add(serviceCall);
    }
    
    public Set<Concept> getRequiredConceptsDefinitions(){
        return requiredConceptsDefinitions;
    }
    
    public Set<Instance> getRequiredInstances(){
    	return requiredInstances;
    }
    
    public Set<Axiom> generateRules(Mappings mappings, Set <Instance> theSourceInstances, boolean filterMappings){
    	this.mappings = mappings;
        
        computeRelevantConceptAndAttributes(theSourceInstances);
        
    	assignements = new HashSet<Axiom>(); 
        Set<Axiom> result = new HashSet<Axiom>(); 
        result.addAll(generateMappingRulesForC2CMappings(mappings.getConceptMappings(), filterMappings));
        result.addAll(generateMappingRulesForA2AMappings(mappings.getAttributeMappings(), filterMappings));
        result.addAll(generateMappingRulesForC2AMappings(mappings.getConceptAttributeMappings(), filterMappings));
        result.addAll(generateMappingRulesForA2CMappings(mappings.getAttributeConceptMappings(), filterMappings));
        result.addAll(assignements);
        
        result.addAll(getAuxilliarryAxioms());
        
        return result;
    }

    private ConstructedTerm createFunctionSymbol(IRI function, Term firstPrameter, Term secondParameter){
        List<Term> terms = new ArrayList<Term>();
        terms.add(firstPrameter);
        terms.add(secondParameter);
        return WSMOUtil.leFactory.createConstructedTerm(function, terms);
    }
    
    private Atom createAtom(IRI function, Term firstPrameter){
        List<Term> terms = new ArrayList<Term>();
        terms.add(firstPrameter);        
        return WSMOUtil.leFactory.createAtom(function, terms);
    }
    

	private Attribute getAttribute(Id id) {
		
		if (mappings==null){
			return null;
		}
		
		return new MappingDocument2Mappings().getAttribute(id, mappings.getSrcOntology(), mappings.getTgtOntology());
		
	}
    
    private Identifier extractIdentifierFromId(Id id){
    	
        if (id instanceof DataValueIdPlaceholder || id instanceof SimpleValue)
        	return null;        
        if (id.plainText().contains("@")){
        	return WSMOUtil.createIRI(id.plainText().substring(0, id.plainText().indexOf("@")));
        }
        return WSMOUtil.createIRI(id.plainText());
    } 
    
    private String extractValue(Restriction r){
    	if (r.getValue() instanceof SimpleValue)
    		return ((SimpleValue)r.getValue()).plainText();
    	if (r.getValue() instanceof DataValueIdPlaceholder)
    		return ((DataValueIdPlaceholder)r.getValue()).getDataValue().getValue().toString();    		
    	return "MALFORMED_VALUE";
    }
    
    private Identifier extractIdentifier(ExpressionDefinition ed){
    	if (ed.isComplexExpression()){
    		ComplexExpression ce = (ComplexExpression)ed;
    		//TODO: If the target is a complex only the first entity is currently considered. To be extended...
    		return extractIdentifier(ce.getSubExpressions().iterator().next());
    	}
    	
    	return extractIdentifierFromId((Id)ed);
    } 

    private Identifier extractIdentifier(Path p){
    	//TODO: Only the first element of the path is curently extracted. To be extended... 
    	return extractIdentifier(p.getUri());
    }

    private Identifier extractIdentifier(Restriction r){
    	return extractIdentifier(r.getValue());
    }

    
    private LogicalExpression getAttributeOccurenceConditionExpression(AttributeOccurenceCondition aoc){
        //classId[attibuteId ofType _#]
        AttributeConstraintMolecule le1 = WSMOUtil.leFactory.createAttributeConstraint(extractIdentifier(aoc.getTarget()), extractIdentifier(aoc.getAttributeId()), WSMOUtil.wsmoFactory.createAnonymousID());                        
        //classId[attributeId impliestype _#]
        AttributeInferenceMolecule le2 = WSMOUtil.leFactory.createAttributeInference(extractIdentifier(aoc.getTarget()), extractIdentifier(aoc.getAttributeId()), WSMOUtil.wsmoFactory.createAnonymousID());                        
        //the concept definition will be used in validating the rules conditions
        addConceptToAuxiliary(WSMOUtil.wsmoFactory.getConcept(extractIdentifier(aoc.getTarget())));        
        return WSMOUtil.leFactory.createDisjunction(le1, le2);
    }
    
    private LogicalExpression getAttributeTypeConditionExpression(AttributeTypeCondition atc){
        //classId[attibuteId ofType classExpresionId]
        AttributeConstraintMolecule le1 = WSMOUtil.leFactory.createAttributeConstraint(extractIdentifier(atc.getTarget()), 
        																									extractIdentifier(atc.getAttributeId()), 
        																										extractIdentifier(atc.getRestriction()));                        
        //classId[attributeId impliestype classExpresionId]
        AttributeInferenceMolecule le2 = WSMOUtil.leFactory.createAttributeInference(extractIdentifier(atc.getTarget()), 
        																				extractIdentifier(atc.getAttributeId()), 
        																					extractIdentifier(atc.getRestriction()));                                
        //the concept definition will be used in validating the rules conditions
        addConceptToAuxiliary(WSMOUtil.wsmoFactory.getConcept(extractIdentifier(atc.getTarget()))); 
        return WSMOUtil.leFactory.createDisjunction(le1, le2);
    }
    
    private LogicalExpression getTypeConditionExepression(TypeCondition cevc, Term subject){
    	
    	return WSMOUtil.leFactory.createMemberShipMolecule(subject, extractIdentifier(cevc.getRestriction()));
    	
    }
    
	private LogicalExpression getTypeConditionExepression(TypeCondition condition, List<Attribute> sourceAttributes, List<Term> sourceRangeVariables) {

		LogicalExpression result = null;
		
		for (Term t : getVariablesForTheAttributesInExpression(condition.getTarget(), sourceAttributes, sourceRangeVariables)){
			if (result == null){
				result = getTypeConditionExepression(condition, t);
			}
			else{
				result = WSMOUtil.leFactory.createConjunction(result, getTypeConditionExepression(condition, t));
			}
				
		}
		
		return null;
	}
	
	private Set<Term> getVariablesForTheAttributesInExpression(ExpressionDefinition ed, List<Attribute> sourceAttributes, List<Term> sourceRangeVariables){
		
		Set<Term> result = new HashSet<Term>();
		
		if (ed.isComplexExpression()){
			for (ExpressionDefinition sed : ((ComplexExpression)ed).getSubExpressions()){
				result.addAll(getVariablesForTheAttributesInExpression(sed, sourceAttributes, sourceRangeVariables));
			}
		}
		else{
			if (ed instanceof AttributeId){
				int index = sourceAttributes.indexOf(((AttributeId)ed).getAttribute());
				if (index!=-1){
					result.add(sourceRangeVariables.get(index));
				}
			}
		}
		return result;
	}
    
    private LogicalExpression getAttributeValueConditionExpression(AttributeValueCondition avc, 
    																						MappingRule crtMapping, Term sourceVariable, Term targetVariable){
        Term variable = WSMOUtil.wsmoFactory.createAnonymousID();
        
        if (crtMapping.getSource().getConditions().contains(avc))       
            variable = sourceVariable;
        else
        	if (crtMapping.getTarget().getConditions().contains(avc))
                    variable = targetVariable;

        //variable[attrId hasValue instanceId]
        if (extractIdentifier(avc.getRestriction())!=null)
        	return WSMOUtil.leFactory.createAttributeValue(variable, extractIdentifier(avc.getAttributeId()), extractIdentifier(avc.getRestriction()));
        else //variable[attrId hasValue literal]
            return WSMOUtil.leFactory.createAttributeValue(variable, extractIdentifier(avc.getAttributeId()), WSMOUtil.createDataValue(extractValue(avc.getRestriction())));	
    }
    
    private LogicalExpression getValueConditionExpression(ValueCondition ivc, MappingRule crtMapping, Term sourceVariable, Term targetRangeVariable){
        Term variable=null;
        if (sourceVariable!=null && (crtMapping.getSource().getConditions().contains(ivc))){
            variable = sourceVariable;
            if (extractIdentifier(ivc.getRestriction())!=null)
            	//variable[attrId hasValue instanceId]
            	return WSMOUtil.leFactory.createAttributeValue(variable, extractIdentifier(ivc.getTarget()), extractIdentifier(ivc.getRestriction()));
            else
//            	variable[attrId hasValue literal]
                return WSMOUtil.leFactory.createAttributeValue(variable, extractIdentifier(ivc.getTarget()), WSMOUtil.createDataValue(extractValue(ivc.getRestriction())));             	
        }
        else
        if (targetRangeVariable!=null && (crtMapping.getTarget().getConditions().contains(ivc))){
            String assignementIndex = "assignement_" + IndexGenerator.getStringIndex();
			Axiom a = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#" + assignementIndex));
			IRI assignAtom = WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#" + assignementIndex);
			if (extractIdentifier(ivc.getRestriction())!=null){
				//return WSMOUtil.leFactory.createLogicalExpression(targetRangeVariable + " = " + extractIdentifier(ivc.getRestriction()));            		            		
				a.addDefinition(createAtom(assignAtom, extractIdentifier(ivc.getRestriction())));             		
				assignements.add(a);
				return createAtom(assignAtom, targetRangeVariable);
			}
			else{
				//return WSMOUtil.leFactory.createLogicalExpression(targetRangeVariable + " = " + WSMOUtil.createDataValue(extractValue(ivc.getRestriction())));
				//IRI assignFunction = WSMOUtil.createIRI(MediationReasoner.reasoningSpace + "#" + assignementIndex);            		
				a.addDefinition(createAtom(assignAtom, WSMOUtil.createDataValue(extractValue(ivc.getRestriction()))));            		
			    assignements.add(a);
			    return createAtom(assignAtom, targetRangeVariable);
			}
        }  
        return null;
    }
    
    public Set<Axiom> generateMappingRulesForC2CMappings(List<Class2Class> mappings, boolean filterMappings) {
        
        Set<Axiom> result = new HashSet<Axiom>();
        
        Iterator<Class2Class> cmIt = mappings.iterator();
        while (cmIt.hasNext()){
            Class2Class crtMapping = cmIt.next();
            
            if (!filterMappings || mappingSourceContainsARelevantSourceConcept(crtMapping.getSource())){
            	//?x
                Variable sourceVariable = WSMOUtil.leFactory.createVariable("X" + IndexGenerator.getStringIndex());            
                //mediated(?x, targetOwner)
                Identifier targetOwner = extractIdentifier(crtMapping.getTarget().getId());
                ConstructedTerm targetVariable = createFunctionSymbol(function, sourceVariable, targetOwner);            
                //mediated(?x, targetOwner) memberOf targetOwner
                LogicalExpression  leftSide = WSMOUtil.leFactory.createMemberShipMolecule(targetVariable, targetOwner);            
                //?x memeberOf sourceOwner
                MembershipMolecule rightSideMembership = WSMOUtil.leFactory.createMemberShipMolecule(sourceVariable, extractIdentifier(crtMapping.getSource().getId()));     
                
                
                //AttributeValueMolecule rightSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(targetVariable, WSMOUtil.leFactory.createVariable("A" + IndexGenerator.getStringIndex()), WSMOUtil.leFactory.createVariable("V" + IndexGenerator.getStringIndex()));
                
                //LogicalExpression rightSideExpression = WSMOUtil.leFactory.createConjunction(rightSideMembership, rightSideAVMolecule);
                
                LogicalExpression rightSideExpression = rightSideMembership;
                
                List<Term> parameters = new ArrayList<Term>();
                parameters.add(extractIdentifier(crtMapping.getSource().getId()));
                parameters.add(extractIdentifier(crtMapping.getTarget().getId()));
            	parameters.add(sourceVariable);
                 
                //NOTE: the mappedFlagg should not be used when AtributeValueClassConditions are present
            	if (!crtMapping.getSource().equals(crtMapping.getTarget())){
            		leftSide = WSMOUtil.leFactory.createConjunction(WSMOUtil.leFactory.createAtom(mappedFlag, parameters), leftSide);
            	}else
            	{
            		//if the source and target are the same there should be no mediated(?x,...) memberOf TargetConcept since this would produc ean unfinite loop
            		//the only expression remaining on the left side is the mappedFlag.
            		leftSide = WSMOUtil.leFactory.createAtom(mappedFlag, parameters);
            	}
                //classId[attibuteId ofType _#] or classId[attributeId impliestypeclassExpresionId]
                LogicalExpression attrOccurenceConditionLE = null;
                //classId[attibuteId ofType _#] or classId[attributeId impliestype classExpresionId]
                LogicalExpression classExprAttrValueCondLE = null;
                //variable[attrId hasValue literal]
                LogicalExpression dataLiteralAttrValueCondLE = null;
                //variable[attrId hasValue instanceId]
                LogicalExpression IndividualIdAttrValueCondLE = null;
                
                
                
                
                Collection<ClassCondition> classConditions = new HashSet<ClassCondition>();
                if (crtMapping.getSource().getConditions()!=null)
                	classConditions.addAll(crtMapping.getSource().getConditions());
                if (crtMapping.getTarget().getConditions()!=null)
                	classConditions.addAll(crtMapping.getSource().getConditions());
                
                if (classConditions!=null && !classConditions.isEmpty()){
                    Iterator<ClassCondition> cIt = classConditions.iterator();
                    while (cIt.hasNext()){
                        ClassCondition crtCondition = cIt.next();                    
                        if (crtCondition instanceof AttributeOccurenceCondition){                      
                            attrOccurenceConditionLE = getAttributeOccurenceConditionExpression((AttributeOccurenceCondition)crtCondition);
                        }else
                        if (crtCondition instanceof AttributeTypeCondition){                        
                            attrOccurenceConditionLE = getAttributeTypeConditionExpression((AttributeTypeCondition)crtCondition);
                        }else
                        if (crtCondition instanceof AttributeValueCondition){                        
                            dataLiteralAttrValueCondLE = getAttributeValueConditionExpression((AttributeValueCondition)crtCondition, crtMapping, sourceVariable, targetVariable);
                        }   
                    }
                }
                Conjunction conditions = null;
                if (attrOccurenceConditionLE != null)
                    conditions = WSMOUtil.leFactory.createConjunction(rightSideExpression, attrOccurenceConditionLE);
                
                if (classExprAttrValueCondLE!=null && conditions!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(conditions, classExprAttrValueCondLE);
                else
                	if (classExprAttrValueCondLE!=null)
                		conditions = WSMOUtil.leFactory.createConjunction(rightSideExpression, classExprAttrValueCondLE);
                
                if (dataLiteralAttrValueCondLE!=null && conditions!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(conditions, dataLiteralAttrValueCondLE);
                else
                	if (dataLiteralAttrValueCondLE!=null)
                		conditions = WSMOUtil.leFactory.createConjunction(rightSideExpression, dataLiteralAttrValueCondLE);
    
                
                if (IndividualIdAttrValueCondLE!=null && conditions!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(conditions, IndividualIdAttrValueCondLE);
                else
                    if (IndividualIdAttrValueCondLE!=null)
                        conditions = WSMOUtil.leFactory.createConjunction(rightSideExpression, IndividualIdAttrValueCondLE);
                
                LogicProgrammingRule rule = null; 
                if (conditions!=null)
                    rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSide, conditions);
                else
                    rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSide, rightSideExpression);            
                
	            Axiom anAxiom = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#ccMappingRule" + IndexGenerator.getStringIndex()));
                anAxiom.addDefinition(rule);
                result.add(anAxiom);            
            }
        }
        return result;
    }

/*
    public Set<Axiom> generateMappingRulesForC2CMappings(List<Class2Class> mappings) {
        
        Set<Axiom> result = new HashSet<Axiom>();
        
        Iterator<Class2Class> cmIt = mappings.iterator();
        while (cmIt.hasNext()){
            Class2Class crtMapping = cmIt.next();
            //?x
            Variable sourceVariable = WSMOUtil.leFactory.createVariable("X" + IndexGenerator.getStringIndex());            
            //mediated(?x, targetOwner)
            Identifier targetOwner = extractIdentifier(crtMapping.getTarget().getId());
            ConstructedTerm targetVariable = createFunctionSymbol(function, sourceVariable, targetOwner);            
            //mediated(?x, targetOwner) memberOf targetOwner
            MembershipMolecule leftSide = WSMOUtil.leFactory.createMemberShipMolecule(targetVariable, targetOwner);            
            //?x memeberOf sourceOwner
            MembershipMolecule rightSideMembership = WSMOUtil.leFactory.createMemberShipMolecule(sourceVariable, extractIdentifier(crtMapping.getSource().getId()));     
            
            
            AttributeValueMolecule rightSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(targetVariable, WSMOUtil.leFactory.createVariable("A" + IndexGenerator.getStringIndex()), WSMOUtil.leFactory.createVariable("V" + IndexGenerator.getStringIndex()));
            
            LogicalExpression rightSideExpression = WSMOUtil.leFactory.createConjunction(rightSideMembership, rightSideAVMolecule);
            
            
            //classId[attibuteId ofType _#] or classId[attributeId impliestypeclassExpresionId]
            LogicalExpression attrOccurenceConditionLE = null;
            //classId[attibuteId ofType _#] or classId[attributeId impliestype classExpresionId]
            LogicalExpression classExprAttrValueCondLE = null;
            //variable[attrId hasValue literal]
            LogicalExpression dataLiteralAttrValueCondLE = null;
            //variable[attrId hasValue instanceId]
            LogicalExpression IndividualIdAttrValueCondLE = null;
            
            Collection<ClassCondition> classConditions = new HashSet<ClassCondition>();
            if (crtMapping.getSource().getConditions()!=null)
            	classConditions.addAll(crtMapping.getSource().getConditions());
            if (crtMapping.getTarget().getConditions()!=null)
            	classConditions.addAll(crtMapping.getSource().getConditions());
            
            if (classConditions!=null && !classConditions.isEmpty()){
                Iterator<ClassCondition> cIt = classConditions.iterator();
                while (cIt.hasNext()){
                    ClassCondition crtCondition = cIt.next();                    
                    if (crtCondition instanceof AttributeOccurenceCondition){                      
                        attrOccurenceConditionLE = getAttributeOccurenceConditionExpression((AttributeOccurenceCondition)crtCondition);
                    }else
                    if (crtCondition instanceof AttributeTypeCondition){                        
                        attrOccurenceConditionLE = getAttributeTypeConditionExpression((AttributeTypeCondition)crtCondition);
                    }else
                    if (crtCondition instanceof AttributeValueCondition){                        
                        dataLiteralAttrValueCondLE = getAttributeValueConditionExpression((AttributeValueCondition)crtCondition, crtMapping, sourceVariable, targetVariable);
                    }   
                }
            }
            Conjunction conditions = null;
            if (attrOccurenceConditionLE != null)
                conditions = WSMOUtil.leFactory.createConjunction(rightSideExpression, attrOccurenceConditionLE);
            
            if (classExprAttrValueCondLE!=null && conditions!=null)
                conditions = WSMOUtil.leFactory.createConjunction(conditions, classExprAttrValueCondLE);
            else
            	if (classExprAttrValueCondLE!=null)
            		conditions = WSMOUtil.leFactory.createConjunction(rightSideExpression, classExprAttrValueCondLE);
            
            if (dataLiteralAttrValueCondLE!=null && conditions!=null)
                conditions = WSMOUtil.leFactory.createConjunction(conditions, dataLiteralAttrValueCondLE);
            else
            	if (dataLiteralAttrValueCondLE!=null)
            		conditions = WSMOUtil.leFactory.createConjunction(rightSideExpression, dataLiteralAttrValueCondLE);

            
            if (IndividualIdAttrValueCondLE!=null && conditions!=null)
                conditions = WSMOUtil.leFactory.createConjunction(conditions, IndividualIdAttrValueCondLE);
            else
                if (IndividualIdAttrValueCondLE!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(rightSideExpression, IndividualIdAttrValueCondLE);
            
            LogicProgrammingRule rule = null; //WSMOUtil.leFactory.createLogicProgrammingRule()
            if (conditions!=null)
                rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSide, conditions);
            else
                rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSide, rightSideExpression);            
            Axiom anAxiom = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#ccMappingRule" + IndexGenerator.getStringIndex()));
            anAxiom.addDefinition(rule);
            result.add(anAxiom);            
        }
        return result;
    }
 */    

    public Set<Axiom> generateMappingRulesForA2AMappings(List<Attribute2Attribute> mappings, boolean filterMappings) {        
        Set<Axiom> result = new HashSet<Axiom>();        
        Iterator<Attribute2Attribute> am = mappings.iterator();
        while (am.hasNext()){
            Attribute2Attribute crtMapping = am.next();
            if (!filterMappings || mappingSourceContainsARelevantSourceAttribute(crtMapping.getSource())){
                if (crtMapping.getSource().isComplexExpression()){
                	if (crtMapping.getSource().getTransf()!=null){            		
                		result.addAll(generateMappingRulesForA2AMappingWithFunctoids(crtMapping));            		        		
                	}
                }
                else{
                    result.addAll(generateMappingRulesForA2AMapping(crtMapping));
                }
            }
        }
        return result;
     }    
    
	private Collection<Attribute> getAttribute(ComplexExpression cExpression) {
		
		List<Attribute> result = new ArrayList<Attribute>();
		
		if (cExpression == null)
			return result;
		
		for (ExpressionDefinition ed : cExpression.getSubExpressions()){
			if (ed instanceof Id){				
				result.add(getAttribute((Id)ed));				
			}
			else{
				result.addAll(getAttribute((ComplexExpression)ed));
			}				
		}
		
	    return result;
	}
    
	private List<Attribute> getAttribute(AttributeExpr attrExpr) {
		
		if (attrExpr == null)
			return null;
		
		List<Attribute> result = new ArrayList<Attribute>();
		
		if (!attrExpr.isComplexExpression()){
			result.add(getAttribute(attrExpr.getId()));
			return result;
		}
		
		result.addAll(getAttribute((ComplexExpression)attrExpr.getExpresionDefinition()));
		
		return result; 
		
	}

	private Collection<? extends Axiom> generateMappingRulesForA2AMappingWithFunctoids(Attribute2Attribute crtMapping) {
		
		Set<Axiom> result = new HashSet<Axiom>();
		List<Attribute> sourceAttributes = getAttribute(crtMapping.getSource());
//      ?x
        Variable sourceVariable = WSMOUtil.leFactory.createVariable("X" + IndexGenerator.getStringIndex());
//      mediated(?x, targetOwner)
        //--------//Identifier targetOwner = ((AttributeId)crtMapping.getTarget().getId()).getAttribute().getConcept().getIdentifier();
        Attribute targetAttriibute = getAttribute(crtMapping.getTarget().getId());
        Identifier targetOwner = targetAttriibute.getConcept().getIdentifier();;
        ConstructedTerm targetVariable = createFunctionSymbol(function, sourceVariable, targetOwner); 

    	Collection<AttributeCondition> attributeConditions = new HashSet<AttributeCondition>();
    	if (crtMapping.getSource().getConditions() != null)
    		attributeConditions.addAll(crtMapping.getSource().getConditions());
    	if (crtMapping.getTarget().getConditions() != null)
    		attributeConditions.addAll(crtMapping.getTarget().getConditions());            
        
    	//i assume that the attributes involved do not have compound items as range
    	
        List<Term> sourceRangeVariables = new ArrayList<Term>();
        for (int i=0; i<sourceAttributes.size(); i++){
        	sourceRangeVariables.add(WSMOUtil.leFactory.createVariable("YI" + IndexGenerator.getStringIndex()));
        }
        
        Term targetRangeVariable = null;
        
        if (crtMapping.getSource().getTransf()==null){
            targetRangeVariable = WSMOUtil.leFactory.createVariable("YO" + IndexGenerator.getStringIndex());
        }
        else{
        	//build the instance denoting the service call service
        	IRI serviceIRI = null;
        	
        	String serviceString = "serviceString";
        	if (crtMapping.getSource().getTransf().getId()!=null){
        		serviceString = crtMapping.getSource().getTransf().getId().toString();
        	}
        	else{
        		serviceString = crtMapping.getSource().getTransf().getRes().toString();
        	}
        		
        	try{
        		serviceIRI = WSMOUtil.createIRI(serviceString);
        	}
        	catch(IllegalArgumentException e){
        		serviceIRI = WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace+ "#" + serviceString);
        	}
        	
        	targetRangeVariable = WSMOUtil.leFactory.createConstructedTerm(serviceIRI, sourceRangeVariables);
        }
        

        //mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable]
        AttributeValueMolecule leftSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(targetVariable, 
        																				extractIdentifier(crtMapping.getTarget().getId()), targetRangeVariable);
        
        //targetRabgeVariable memberOf _string and targetRangeVariable memberOf ServiceCall
        List<Term> owners = new ArrayList<Term>();
        for (Type t : targetAttriibute.listTypes()){
        	owners.add(WSMOUtil.getIdentifier(t));
        }
        owners.add(serviceCall.getIdentifier());
        LogicalExpression rangeMembership = WSMOUtil.leFactory.createMemberShipMolecules(targetRangeVariable, owners);
        
        
        //mediated(?x, targetOwner) memberOf targetOwner
        MembershipMolecule leftSideMemebershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(targetVariable, targetOwner);
//      mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable] memberOf targetOwner
        List<Molecule> targetMolecules = new ArrayList<Molecule>();
        targetMolecules.add(leftSideAVMolecule); targetMolecules.add(leftSideMemebershipMolecule);
        LogicalExpression leftSideMolecule  = WSMOUtil.leFactory.createCompoundMolecule(targetMolecules);
        
        leftSideMolecule = WSMOUtil.leFactory.createConjunction(leftSideMolecule, rangeMembership);

        LogicalExpression rightSideMolecule = null;
        
        for (int i=0; i<sourceRangeVariables.size(); i++){
	        //?X[sourceAttrId hasValue sourceRangeVariable]
	        Identifier sourceOwner = sourceAttributes.get(i).getConcept().getIdentifier();
	        AttributeValueMolecule rightSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(sourceVariable, 
	        															sourceAttributes.get(i).getIdentifier(), sourceRangeVariables.get(i));
	//      ?X memberOf sourceOwner
	        MembershipMolecule rightSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(sourceVariable, sourceOwner);
	//      ?X[sourceAttrId hasValue sourceRangeVariable] memberOf soourceOwner
	        List<Molecule> sourceMolecules = new ArrayList<Molecule>();
	        sourceMolecules.add(rightSideAVMolecule); sourceMolecules.add(rightSideMembershipMolecule);
	        CompoundMolecule rightSideCompoundMolecule  = WSMOUtil.leFactory.createCompoundMolecule(sourceMolecules);
	        
	        if (rightSideMolecule==null){
	        	rightSideMolecule = rightSideCompoundMolecule;
	        }
	        else{
	        	rightSideMolecule = WSMOUtil.leFactory.createConjunction(rightSideMolecule, rightSideCompoundMolecule);
	        }
        }
        
        //conditions******************************************************************************

        LogicalExpression classExpressionValueConditionExpression = null;
        LogicalExpression dataLitteralValueConditionExpression = null;
        LogicalExpression individualIDValueConditionExpression = null;            
        if (attributeConditions!=null){
            Iterator<AttributeCondition> cIt = attributeConditions.iterator();
            while (cIt.hasNext()){
                AttributeCondition crtCondition = cIt.next();
                if (crtCondition instanceof TypeCondition){                    	                    	
                	LogicalExpression newClassExpressionValueConditionExpression = null;
                	if (crtMapping.getSource().getConditions().contains(crtCondition))             
                		newClassExpressionValueConditionExpression = getTypeConditionExepression((TypeCondition)crtCondition, sourceAttributes, sourceRangeVariables);                    	                    	 
                	if (crtMapping.getTarget().getConditions().contains(crtCondition))
                		newClassExpressionValueConditionExpression = getTypeConditionExepression((TypeCondition)crtCondition, targetRangeVariable);
                	if (classExpressionValueConditionExpression == null)
                		classExpressionValueConditionExpression = newClassExpressionValueConditionExpression;
                	else
                		classExpressionValueConditionExpression = WSMOUtil.leFactory.createConjunction(classExpressionValueConditionExpression, newClassExpressionValueConditionExpression);                    	                    		
                }else
                if (crtCondition instanceof ValueCondition){                    	
                    LogicalExpression newDataLitteralValueConditionExpression = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, sourceVariable, targetRangeVariable);
                    if (dataLitteralValueConditionExpression == null)
                    	dataLitteralValueConditionExpression = newDataLitteralValueConditionExpression;
                    else
                    	dataLitteralValueConditionExpression = WSMOUtil.leFactory.createConjunction(dataLitteralValueConditionExpression, newDataLitteralValueConditionExpression);
                }else
                if (crtCondition instanceof ValueCondition){
                    LogicalExpression newIndividualIDValueConditionExpression = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, sourceVariable, targetRangeVariable);
                    if (individualIDValueConditionExpression == null)
                    	individualIDValueConditionExpression = newIndividualIDValueConditionExpression;
                    else
                    	individualIDValueConditionExpression = WSMOUtil.leFactory.createConjunction(individualIDValueConditionExpression, newIndividualIDValueConditionExpression);
                }                       
            }
        }
        Conjunction conditions = null;
        if (classExpressionValueConditionExpression != null)
            conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, classExpressionValueConditionExpression);
        if (dataLitteralValueConditionExpression!=null && conditions!=null)
            conditions = WSMOUtil.leFactory.createConjunction(conditions, dataLitteralValueConditionExpression);
        else
        	if (dataLitteralValueConditionExpression!=null)
        		conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, dataLitteralValueConditionExpression);
        if (individualIDValueConditionExpression!=null && conditions!=null)
            conditions = WSMOUtil.leFactory.createConjunction(conditions, individualIDValueConditionExpression);
        else
        	if (individualIDValueConditionExpression!=null)
        		conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, individualIDValueConditionExpression);
        
        LogicProgrammingRule rule = null; //WSMOUtil.leFactory.createLogicProgrammingRule()
        if (conditions!=null)
            rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, conditions);
        else
            rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, rightSideMolecule);            
        Axiom anAxiom = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#aFaMappingRule" + IndexGenerator.getStringIndex()));
        anAxiom.addDefinition(rule);  
        result.add(anAxiom); 
        return result;
	}


/*	
	private Collection<? extends Axiom> generateMappingRulesForA2AMapping(Attribute2Attribute crtMapping) {
//      ?x
        Variable sourceVariable = WSMOUtil.leFactory.createVariable("X" + IndexGenerator.getStringIndex());
        //---------//Identifier sourceOwner = ((AttributeId)crtMapping.getSource().getId()).getAttribute().getConcept().getIdentifier();
        Identifier sourceOwner = getAttribute(crtMapping.getSource().getId()).getConcept().getIdentifier();
        
//      mediated(?x, targetOwner)
        //--------//Identifier targetOwner = ((AttributeId)crtMapping.getTarget().getId()).getAttribute().getConcept().getIdentifier();
        Identifier super_targetOwner = getAttribute(crtMapping.getTarget().getId()).getConcept().getIdentifier();;
        Variable targetOwner = WSMOUtil.leFactory.createVariable("O" + IndexGenerator.getStringIndex());        
        LogicalExpression targetOwnerSubconcepts = WSMOUtil.leFactory.createSubConceptMolecule(targetOwner, super_targetOwner);
        
        ConstructedTerm targetVariable = createFunctionSymbol(function, sourceVariable, targetOwner);  
        
    	Collection<AttributeCondition> attributeConditions = new HashSet<AttributeCondition>();
    	if (crtMapping.getSource().getConditions() != null)
    		attributeConditions.addAll(crtMapping.getSource().getConditions());
    	if (crtMapping.getTarget().getConditions() != null)
    		attributeConditions.addAll(crtMapping.getTarget().getConditions());            
        
        Term sourceRangeVariable = null;
        Term targetRangeVariable = null;     
        AttributeValueMolecule rightSideCheckAttributesExistance = null;
        if (WSMOUtil.listAtributesForRangeConcepts(getAttribute(crtMapping.getSource().getId()))==null ||
                WSMOUtil.listAtributesForRangeConcepts(getAttribute(crtMapping.getSource().getId())).isEmpty()){
            if (attributeConditions==null || attributeConditions.isEmpty())
                    sourceRangeVariable = targetRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
            else{
                sourceRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
                targetRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
            }
        }
        else{
            sourceRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
            
            Identifier attributeValueSuperType = WSMOUtil.getIdentifier(
                    WSMOUtil.getRandomRangeType(getAttribute(crtMapping.getTarget().getId())));
            Variable attributeValueType = WSMOUtil.leFactory.createVariable("VO" + IndexGenerator.getStringIndex());
            
            targetOwnerSubconcepts = WSMOUtil.leFactory.createConjunction(targetOwnerSubconcepts, WSMOUtil.leFactory.createSubConceptMolecule(attributeValueType, attributeValueSuperType));
                    
            targetRangeVariable = createFunctionSymbol(function, sourceRangeVariable, attributeValueType); 
            
            //targetRanageVariable[?x hasValue ?y]
            rightSideCheckAttributesExistance = WSMOUtil.leFactory.createAttributeValue(targetRangeVariable, 
            		WSMOUtil.leFactory.createVariable("A" + IndexGenerator.getStringIndex()), WSMOUtil.leFactory.createVariable("V" + IndexGenerator.getStringIndex()));
            
        }             
        //mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable]
        AttributeValueMolecule leftSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(targetVariable, extractIdentifier(crtMapping.getTarget().getId()), targetRangeVariable);
        //mediated(?x, targetOwner) memberOf targetOwner
        MembershipMolecule leftSideMemebershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(targetVariable, targetOwner);
//      mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable] memberOf targetOwner
        List<Molecule> targetMolecules = new ArrayList<Molecule>();
        targetMolecules.add(leftSideAVMolecule); targetMolecules.add(leftSideMemebershipMolecule);
        CompoundMolecule leftSideMolecule  = WSMOUtil.leFactory.createCompoundMolecule(targetMolecules);
  
        //?X[sourceAttrId hasValue sourceRangeVariable]
        AttributeValueMolecule rightSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(sourceVariable, extractIdentifier(crtMapping.getSource().getId()), sourceRangeVariable);
//      ?X memberOf sourceOwner
        MembershipMolecule rightSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(sourceVariable, sourceOwner);
//      ?X[sourceAttrId hasValue sourceRangeVariable] memberOf soourceOwner
        List<Molecule> sourceMolecules = new ArrayList<Molecule>();
        sourceMolecules.add(rightSideAVMolecule); sourceMolecules.add(rightSideMembershipMolecule);
        CompoundMolecule rightSideCompoundMolecule  = WSMOUtil.leFactory.createCompoundMolecule(sourceMolecules);  
        
        LogicalExpression rightSideMolecule = null;
        if (rightSideCheckAttributesExistance!=null)
        	//?X[sourceAttrId hasValue sourceRangeVariable] memberOf soourceOwner and targetRanageVariable[?x hasValue ?y]
        	rightSideMolecule = WSMOUtil.leFactory.createConjunction(rightSideCompoundMolecule, rightSideCheckAttributesExistance); // SWITCH THINGS WITH THIS
        else
        	rightSideMolecule = rightSideCompoundMolecule;
        
        //trying to fix the inheritance code by reffering to the possible subclasses that could inherit the mapped attribut
        rightSideMolecule = WSMOUtil.leFactory.createConjunction(targetOwnerSubconcepts, rightSideMolecule);
        
        //classId[attrId ofType classExpr] or classId[attrId impliesType classExpr] 
        LogicalExpression classExpressionValueConditionExpression = null;
        LogicalExpression dataLitteralValueConditionExpression = null;
        LogicalExpression individualIDValueConditionExpression = null;            
        if (attributeConditions!=null){
            Iterator<AttributeCondition> cIt = attributeConditions.iterator();
            while (cIt.hasNext()){
                AttributeCondition crtCondition = cIt.next();
                if (crtCondition instanceof TypeCondition){                    	                    	
                	LogicalExpression newClassExpressionValueConditionExpression = null;
                	if (crtMapping.getSource().getConditions().contains(crtCondition))             
                		newClassExpressionValueConditionExpression = getTypeConditionExepression((TypeCondition)crtCondition, sourceRangeVariable);                    	                    	 
                	if (crtMapping.getTarget().getConditions().contains(crtCondition))
                		newClassExpressionValueConditionExpression = getTypeConditionExepression((TypeCondition)crtCondition, targetRangeVariable);
                	if (classExpressionValueConditionExpression == null)
                		classExpressionValueConditionExpression = newClassExpressionValueConditionExpression;
                	else
                		classExpressionValueConditionExpression = WSMOUtil.leFactory.createConjunction(classExpressionValueConditionExpression, newClassExpressionValueConditionExpression);                    	                    		
                }else
                if (crtCondition instanceof ValueCondition){                    	
                    LogicalExpression newDataLitteralValueConditionExpression = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, sourceVariable, targetRangeVariable);
                    if (dataLitteralValueConditionExpression == null)
                    	dataLitteralValueConditionExpression = newDataLitteralValueConditionExpression;
                    else
                    	dataLitteralValueConditionExpression = WSMOUtil.leFactory.createConjunction(dataLitteralValueConditionExpression, newDataLitteralValueConditionExpression);
                }else
                if (crtCondition instanceof ValueCondition){
                    LogicalExpression newIndividualIDValueConditionExpression = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, sourceVariable, targetRangeVariable);
                    if (individualIDValueConditionExpression == null)
                    	individualIDValueConditionExpression = newIndividualIDValueConditionExpression;
                    else
                    	individualIDValueConditionExpression = WSMOUtil.leFactory.createConjunction(individualIDValueConditionExpression, newIndividualIDValueConditionExpression);
                }                       
            }
        }
        Conjunction conditions = null;
        if (classExpressionValueConditionExpression != null)
            conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, classExpressionValueConditionExpression);
        if (dataLitteralValueConditionExpression!=null && conditions!=null)
            conditions = WSMOUtil.leFactory.createConjunction(conditions, dataLitteralValueConditionExpression);
        else
        	if (dataLitteralValueConditionExpression!=null)
        		conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, dataLitteralValueConditionExpression);
        if (individualIDValueConditionExpression!=null && conditions!=null)
            conditions = WSMOUtil.leFactory.createConjunction(conditions, individualIDValueConditionExpression);
        else
        	if (individualIDValueConditionExpression!=null)
        		conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, individualIDValueConditionExpression);
         
        LogicProgrammingRule rule = null; //WSMOUtil.leFactory.createLogicProgrammingRule()
        if (conditions!=null)
            rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, conditions);
        else
            rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, rightSideMolecule);            
        Axiom anAxiom = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#aaMappingRule" + IndexGenerator.getStringIndex()));
        anAxiom.addDefinition(rule);
        Set<Axiom> result = new HashSet<Axiom>();  
        result.add(anAxiom); 
        return result;
	}	
*/
	
	
	private Collection<? extends Axiom> generateMappingRulesForA2AMapping(Attribute2Attribute crtMapping) {
		
		Set<Axiom> result = new HashSet<Axiom>();  
		
		Concept targetOwnerConcept = getAttribute(crtMapping.getTarget().getId()).getConcept();
		Type targetAttributeRangeType = WSMOUtil.getRandomRangeType(getAttribute(crtMapping.getTarget().getId()));
		
		Set<Concept> targetOwnerSubConcepts = new HashSet<Concept>(); 
		targetOwnerSubConcepts.addAll(WSMOUtil.getSubConcepts(targetOwnerConcept));
		targetOwnerSubConcepts.add(targetOwnerConcept);
		
		Set<Type> targetAttributeRangeSubTypes = new HashSet<Type>();
		if (targetAttributeRangeType instanceof Concept){
			targetAttributeRangeSubTypes.addAll(WSMOUtil.getSubConcepts((Concept) targetAttributeRangeType));
		}
		targetAttributeRangeSubTypes.add(targetAttributeRangeType);
		
		
		for (Concept toC: targetOwnerSubConcepts){
			for (Type tarC: targetAttributeRangeSubTypes){
				result.addAll(generateMappingRulesForA2AMapping(crtMapping, toC, tarC));
			}
		}
		return result;
		//return generateMappingRulesForA2AMapping(crtMapping, targetOwnerConcept, targetAttributeRangeType);
	}
	
	private Collection<? extends Axiom> generateMappingRulesForA2AMapping(Attribute2Attribute crtMapping, Concept targetOwnerConcept, Type targetAttributeRangeType) {
//      ?x
        Variable sourceVariable = WSMOUtil.leFactory.createVariable("X" + IndexGenerator.getStringIndex());
        Identifier sourceOwner = getAttribute(crtMapping.getSource().getId()).getConcept().getIdentifier();
        
//      mediated(?x, targetOwner)
        
        
        Identifier targetOwner = targetOwnerConcept.getIdentifier();;
        ConstructedTerm targetVariable = createFunctionSymbol(function, sourceVariable, targetOwner);  
        
    	Collection<AttributeCondition> attributeConditions = new HashSet<AttributeCondition>();
    	if (crtMapping.getSource().getConditions() != null)
    		attributeConditions.addAll(crtMapping.getSource().getConditions());
    	if (crtMapping.getTarget().getConditions() != null)
    		attributeConditions.addAll(crtMapping.getTarget().getConditions());            
        
        Term sourceRangeVariable = null;
        Term targetRangeVariable = null;     
        AttributeValueMolecule rightSideCheckAttributesExistance = null;
        if (WSMOUtil.listAtributesForRangeConcepts(getAttribute(crtMapping.getSource().getId()))==null ||
                WSMOUtil.listAtributesForRangeConcepts(getAttribute(crtMapping.getSource().getId())).isEmpty()){
            if (attributeConditions==null || attributeConditions.isEmpty())
                    sourceRangeVariable = targetRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
            else{
                sourceRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
                targetRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
            }
        }
        else{
        	if (attributeConditions==null || attributeConditions.isEmpty()){
        	
        		sourceRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
            	targetRangeVariable = createFunctionSymbol(function, sourceRangeVariable, WSMOUtil.getIdentifier(targetAttributeRangeType)); 
            
            	//targetRanageVariable[?x hasValue ?y]
            	rightSideCheckAttributesExistance = WSMOUtil.leFactory.createAttributeValue(targetRangeVariable, 
            		WSMOUtil.leFactory.createVariable("A" + IndexGenerator.getStringIndex()), WSMOUtil.leFactory.createVariable("V" + IndexGenerator.getStringIndex()));
        	}
        	else{
                sourceRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
                targetRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());        		
        	}
            
        }             
        //mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable]
        AttributeValueMolecule leftSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(targetVariable, extractIdentifier(crtMapping.getTarget().getId()), targetRangeVariable);
        //mediated(?x, targetOwner) memberOf targetOwner
        MembershipMolecule leftSideMemebershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(targetVariable, targetOwner);
//      mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable] memberOf targetOwner
        List<Molecule> targetMolecules = new ArrayList<Molecule>();
        targetMolecules.add(leftSideAVMolecule); targetMolecules.add(leftSideMemebershipMolecule);
        CompoundMolecule leftSideMolecule  = WSMOUtil.leFactory.createCompoundMolecule(targetMolecules);
  
        //?X[sourceAttrId hasValue sourceRangeVariable]
        AttributeValueMolecule rightSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(sourceVariable, extractIdentifier(crtMapping.getSource().getId()), sourceRangeVariable);
//      ?X memberOf sourceOwner
        MembershipMolecule rightSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(sourceVariable, sourceOwner);
//      ?X[sourceAttrId hasValue sourceRangeVariable] memberOf soourceOwner
        List<Molecule> sourceMolecules = new ArrayList<Molecule>();
        sourceMolecules.add(rightSideAVMolecule); sourceMolecules.add(rightSideMembershipMolecule);
        CompoundMolecule rightSideCompoundMolecule  = WSMOUtil.leFactory.createCompoundMolecule(sourceMolecules);  
        
        LogicalExpression rightSideMolecule = null;
        if (rightSideCheckAttributesExistance!=null)
        	//?X[sourceAttrId hasValue sourceRangeVariable] memberOf soourceOwner and targetRanageVariable[?x hasValue ?y]
        	rightSideMolecule = WSMOUtil.leFactory.createConjunction(rightSideCompoundMolecule, rightSideCheckAttributesExistance); // SWITCH THINGS WITH THIS
        else
        	rightSideMolecule = rightSideCompoundMolecule;
        
        //classId[attrId ofType classExpr] or classId[attrId impliesType classExpr] 
        LogicalExpression classExpressionValueConditionExpression = null;
        LogicalExpression dataLitteralValueConditionExpression = null;
        LogicalExpression individualIDValueConditionExpression = null;            
        if (attributeConditions!=null){
            Iterator<AttributeCondition> cIt = attributeConditions.iterator();
            while (cIt.hasNext()){
                AttributeCondition crtCondition = cIt.next();
                if (crtCondition instanceof TypeCondition){                    	                    	
                	LogicalExpression newClassExpressionValueConditionExpression = null;
                	if (crtMapping.getSource().getConditions().contains(crtCondition))             
                		newClassExpressionValueConditionExpression = getTypeConditionExepression((TypeCondition)crtCondition, sourceRangeVariable);                    	                    	 
                	if (crtMapping.getTarget().getConditions().contains(crtCondition))
                		newClassExpressionValueConditionExpression = getTypeConditionExepression((TypeCondition)crtCondition, targetRangeVariable);
                	if (classExpressionValueConditionExpression == null)
                		classExpressionValueConditionExpression = newClassExpressionValueConditionExpression;
                	else
                		classExpressionValueConditionExpression = WSMOUtil.leFactory.createConjunction(classExpressionValueConditionExpression, newClassExpressionValueConditionExpression);                    	                    		
                }else
                if (crtCondition instanceof ValueCondition){                    	
                    LogicalExpression newDataLitteralValueConditionExpression = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, sourceVariable, targetRangeVariable);
                    if (dataLitteralValueConditionExpression == null)
                    	dataLitteralValueConditionExpression = newDataLitteralValueConditionExpression;
                    else
                    	dataLitteralValueConditionExpression = WSMOUtil.leFactory.createConjunction(dataLitteralValueConditionExpression, newDataLitteralValueConditionExpression);
                }else
                if (crtCondition instanceof ValueCondition){
                    LogicalExpression newIndividualIDValueConditionExpression = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, sourceVariable, targetRangeVariable);
                    if (individualIDValueConditionExpression == null)
                    	individualIDValueConditionExpression = newIndividualIDValueConditionExpression;
                    else
                    	individualIDValueConditionExpression = WSMOUtil.leFactory.createConjunction(individualIDValueConditionExpression, newIndividualIDValueConditionExpression);
                }                       
            }
        }
        
        Variable sourceConceptOwnerVariable = WSMOUtil.leFactory.createVariable("SC" + IndexGenerator.getStringIndex());
        List<Term> parameters = new ArrayList<Term>();
        parameters.add(sourceConceptOwnerVariable);
        parameters.add(targetOwner);
        parameters.add(sourceVariable);
        rightSideMolecule = WSMOUtil.leFactory.createConjunction(rightSideMolecule, WSMOUtil.leFactory.createMemberShipMolecule(sourceVariable, sourceConceptOwnerVariable));
        rightSideMolecule = WSMOUtil.leFactory.createConjunction(rightSideMolecule, WSMOUtil.leFactory.createAtom(mappedFlag, parameters));
        
        Conjunction conditions = null;
        if (classExpressionValueConditionExpression != null)
            conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, classExpressionValueConditionExpression);
        if (dataLitteralValueConditionExpression!=null && conditions!=null)
            conditions = WSMOUtil.leFactory.createConjunction(conditions, dataLitteralValueConditionExpression);
        else
        	if (dataLitteralValueConditionExpression!=null)
        		conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, dataLitteralValueConditionExpression);
        if (individualIDValueConditionExpression!=null && conditions!=null)
            conditions = WSMOUtil.leFactory.createConjunction(conditions, individualIDValueConditionExpression);
        else
        	if (individualIDValueConditionExpression!=null)
        		conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, individualIDValueConditionExpression);
         
        LogicProgrammingRule rule = null; //WSMOUtil.leFactory.createLogicProgrammingRule()
        if (conditions!=null)
            rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, conditions);
        else
            rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, rightSideMolecule);            
        Axiom anAxiom = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#aaMappingRule" + IndexGenerator.getStringIndex()));
        anAxiom.addDefinition(rule);
        Set<Axiom> result = new HashSet<Axiom>();  
        result.add(anAxiom); 
        
        return result;
	}
	
	
/*  @@@@@@@@@@@@@@@@@@@Original method@@@@@@@@@@@@@@@@@@@@@@@
	private Collection<? extends Axiom> generateMappingRulesForA2AMapping(Attribute2Attribute crtMapping) {
//      ?x
        Variable sourceVariable = WSMOUtil.leFactory.createVariable("X" + IndexGenerator.getStringIndex());
        //---------//Identifier sourceOwner = ((AttributeId)crtMapping.getSource().getId()).getAttribute().getConcept().getIdentifier();
        Identifier sourceOwner = getAttribute(crtMapping.getSource().getId()).getConcept().getIdentifier();
        
//      mediated(?x, targetOwner)
        //--------//Identifier targetOwner = ((AttributeId)crtMapping.getTarget().getId()).getAttribute().getConcept().getIdentifier();
        Identifier targetOwner = getAttribute(crtMapping.getTarget().getId()).getConcept().getIdentifier();;
        ConstructedTerm targetVariable = createFunctionSymbol(function, sourceVariable, targetOwner);  
        
    	Collection<AttributeCondition> attributeConditions = new HashSet<AttributeCondition>();
    	if (crtMapping.getSource().getConditions() != null)
    		attributeConditions.addAll(crtMapping.getSource().getConditions());
    	if (crtMapping.getTarget().getConditions() != null)
    		attributeConditions.addAll(crtMapping.getTarget().getConditions());            
        
        Term sourceRangeVariable = null;
        Term targetRangeVariable = null;     
        AttributeValueMolecule rightSideCheckAttributesExistance = null;
        if (WSMOUtil.listAtributesForRangeConcepts(getAttribute(crtMapping.getSource().getId()))==null ||
                WSMOUtil.listAtributesForRangeConcepts(getAttribute(crtMapping.getSource().getId())).isEmpty()){
            if (attributeConditions==null || attributeConditions.isEmpty())
                    sourceRangeVariable = targetRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
            else{
                sourceRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
                targetRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
            }
        }
        else{
            sourceRangeVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
            Type targetAttributeRangeType = WSMOUtil.getRandomRangeType(getAttribute(crtMapping.getTarget().getId()));
            targetRangeVariable = createFunctionSymbol(function, sourceRangeVariable, WSMOUtil.getIdentifier(targetAttributeRangeType)); 
            
            //targetRanageVariable[?x hasValue ?y]
            rightSideCheckAttributesExistance = WSMOUtil.leFactory.createAttributeValue(targetRangeVariable, 
            		WSMOUtil.leFactory.createVariable("A" + IndexGenerator.getStringIndex()), WSMOUtil.leFactory.createVariable("V" + IndexGenerator.getStringIndex()));
            
        }             
        //mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable]
        AttributeValueMolecule leftSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(targetVariable, extractIdentifier(crtMapping.getTarget().getId()), targetRangeVariable);
        //mediated(?x, targetOwner) memberOf targetOwner
        MembershipMolecule leftSideMemebershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(targetVariable, targetOwner);
//      mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable] memberOf targetOwner
        List<Molecule> targetMolecules = new ArrayList<Molecule>();
        targetMolecules.add(leftSideAVMolecule); targetMolecules.add(leftSideMemebershipMolecule);
        CompoundMolecule leftSideMolecule  = WSMOUtil.leFactory.createCompoundMolecule(targetMolecules);
  
        //?X[sourceAttrId hasValue sourceRangeVariable]
        AttributeValueMolecule rightSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(sourceVariable, extractIdentifier(crtMapping.getSource().getId()), sourceRangeVariable);
//      ?X memberOf sourceOwner
        MembershipMolecule rightSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(sourceVariable, sourceOwner);
//      ?X[sourceAttrId hasValue sourceRangeVariable] memberOf soourceOwner
        List<Molecule> sourceMolecules = new ArrayList<Molecule>();
        sourceMolecules.add(rightSideAVMolecule); sourceMolecules.add(rightSideMembershipMolecule);
        CompoundMolecule rightSideCompoundMolecule  = WSMOUtil.leFactory.createCompoundMolecule(sourceMolecules);  
        
        LogicalExpression rightSideMolecule = null;
        if (rightSideCheckAttributesExistance!=null)
        	//?X[sourceAttrId hasValue sourceRangeVariable] memberOf soourceOwner and targetRanageVariable[?x hasValue ?y]
        	rightSideMolecule = WSMOUtil.leFactory.createConjunction(rightSideCompoundMolecule, rightSideCheckAttributesExistance); // SWITCH THINGS WITH THIS
        else
        	rightSideMolecule = rightSideCompoundMolecule;
        
        //classId[attrId ofType classExpr] or classId[attrId impliesType classExpr] 
        LogicalExpression classExpressionValueConditionExpression = null;
        LogicalExpression dataLitteralValueConditionExpression = null;
        LogicalExpression individualIDValueConditionExpression = null;            
        if (attributeConditions!=null){
            Iterator<AttributeCondition> cIt = attributeConditions.iterator();
            while (cIt.hasNext()){
                AttributeCondition crtCondition = cIt.next();
                if (crtCondition instanceof TypeCondition){                    	                    	
                	LogicalExpression newClassExpressionValueConditionExpression = null;
                	if (crtMapping.getSource().getConditions().contains(crtCondition))             
                		newClassExpressionValueConditionExpression = getTypeConditionExepression((TypeCondition)crtCondition, sourceRangeVariable);                    	                    	 
                	if (crtMapping.getTarget().getConditions().contains(crtCondition))
                		newClassExpressionValueConditionExpression = getTypeConditionExepression((TypeCondition)crtCondition, targetRangeVariable);
                	if (classExpressionValueConditionExpression == null)
                		classExpressionValueConditionExpression = newClassExpressionValueConditionExpression;
                	else
                		classExpressionValueConditionExpression = WSMOUtil.leFactory.createConjunction(classExpressionValueConditionExpression, newClassExpressionValueConditionExpression);                    	                    		
                }else
                if (crtCondition instanceof ValueCondition){                    	
                    LogicalExpression newDataLitteralValueConditionExpression = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, sourceVariable, targetRangeVariable);
                    if (dataLitteralValueConditionExpression == null)
                    	dataLitteralValueConditionExpression = newDataLitteralValueConditionExpression;
                    else
                    	dataLitteralValueConditionExpression = WSMOUtil.leFactory.createConjunction(dataLitteralValueConditionExpression, newDataLitteralValueConditionExpression);
                }else
                if (crtCondition instanceof ValueCondition){
                    LogicalExpression newIndividualIDValueConditionExpression = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, sourceVariable, targetRangeVariable);
                    if (individualIDValueConditionExpression == null)
                    	individualIDValueConditionExpression = newIndividualIDValueConditionExpression;
                    else
                    	individualIDValueConditionExpression = WSMOUtil.leFactory.createConjunction(individualIDValueConditionExpression, newIndividualIDValueConditionExpression);
                }                       
            }
        }
        Conjunction conditions = null;
        if (classExpressionValueConditionExpression != null)
            conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, classExpressionValueConditionExpression);
        if (dataLitteralValueConditionExpression!=null && conditions!=null)
            conditions = WSMOUtil.leFactory.createConjunction(conditions, dataLitteralValueConditionExpression);
        else
        	if (dataLitteralValueConditionExpression!=null)
        		conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, dataLitteralValueConditionExpression);
        if (individualIDValueConditionExpression!=null && conditions!=null)
            conditions = WSMOUtil.leFactory.createConjunction(conditions, individualIDValueConditionExpression);
        else
        	if (individualIDValueConditionExpression!=null)
        		conditions = WSMOUtil.leFactory.createConjunction(rightSideMolecule, individualIDValueConditionExpression);
         
        LogicProgrammingRule rule = null; //WSMOUtil.leFactory.createLogicProgrammingRule()
        if (conditions!=null)
            rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, conditions);
        else
            rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, rightSideMolecule);            
        Axiom anAxiom = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#aaMappingRule" + IndexGenerator.getStringIndex()));
        anAxiom.addDefinition(rule);
        Set<Axiom> result = new HashSet<Axiom>();  
        result.add(anAxiom); 
        
        return result;
	}
 */	
	
	
	
	public Set<Axiom> generateMappingRulesForC2AMappings(List<Class2Attribute> mappings, boolean filterMappings) {
        Set<Axiom> result = new HashSet<Axiom>();
        Iterator<Class2Attribute> caIt = mappings.iterator();
        while (caIt.hasNext()){
            Class2Attribute crtMapping = caIt.next();
            if (!filterMappings || mappingSourceContainsARelevantSourceConcept(crtMapping.getSource())){
                Term sourceVariable = WSMOUtil.leFactory.createVariable("X" + IndexGenerator.getStringIndex());
                Term u1 = sourceVariable; 
                
                
                Term u2 = getAttribute(crtMapping.getTarget().getId()).getConcept().getIdentifier();
                Term targetVariable = createFunctionSymbol(function, u1, u2);
                
                
                Identifier targetOwner = getAttribute(crtMapping.getTarget().getId()).getConcept().getIdentifier();
                Identifier targetRange = WSMOUtil.getIdentifier(WSMOUtil.getRandomRangeType(getAttribute(crtMapping.getTarget().getId())));
                
    
                //there is need for a test here: if there is an value condition on the target then the targetRangeVariable becomes a simple new variable.
                //Otherwise the targetRangeVariable becomes a mediated(...)
                
                Term targetRangeVariable = createFunctionSymbol(function, sourceVariable, targetRange); 
                
                Collection<Condition> conditions = new HashSet<Condition>();
                if (crtMapping.getSource().getConditions()!=null)
                	conditions.addAll(crtMapping.getSource().getConditions());
                if (crtMapping.getTarget().getConditions()!=null)
                	conditions.addAll(crtMapping.getTarget().getConditions());
                
                if (conditions!=null && !conditions.isEmpty()){
                    Iterator<? extends Condition> cIt = conditions.iterator();
                    while (cIt.hasNext()){
                    	Condition crtCondition = cIt.next(); 
                    	if (crtCondition instanceof ValueCondition){
                    		if (crtMapping.getTarget().getConditions().contains(crtCondition)){
                    			targetRangeVariable = WSMOUtil.leFactory.createVariable("V" + IndexGenerator.getStringIndex());
                    		}
                    	}
                    }
                }
                    
                
                           
                
                //mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable]
                AttributeValueMolecule leftSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(targetVariable, extractIdentifier(crtMapping.getTarget().getId()), targetRangeVariable);  
    //          mediated(?x, targetOwner) memberOf targetOwner
                MembershipMolecule leftSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(targetVariable, targetOwner);
    //          mediated(?x, targetOwner)[targetAttrId hasValue targetRangeVariable] memberOf targetOwner
                List<Molecule> leftSideMolecules = new ArrayList<Molecule>();
                leftSideMolecules.add(leftSideAVMolecule); leftSideMolecules.add(leftSideMembershipMolecule);
                CompoundMolecule leftSideMolecule = WSMOUtil.leFactory.createCompoundMolecule(leftSideMolecules);
                
                //?x memberOf sourceOwner
                MembershipMolecule rightSideMembership1 = WSMOUtil.leFactory.createMemberShipMolecule(sourceVariable, extractIdentifier(crtMapping.getSource().getId())); 
                
                /*
                MembershipMolecule rightSideMembership2 = WSMOUtil.leFactory.createMemberShipMolecule(targetVariable, u2);
                */
                MembershipMolecule rightSideMembership2 = WSMOUtil.leFactory.createMemberShipMolecule(targetRangeVariable, targetRange);
                
                
                //checks if the mediated instace has any attributes in order to avoid empty instances. But I HAS to be added only if no conditions are present 
                //(i.e. in case of an assignemtn the target instance can be an empty isntace from the target which is perfectly legal)  
                AttributeValueMolecule rightSideAttributeValue =  WSMOUtil.leFactory.createAttributeValue(targetRangeVariable, 
                		WSMOUtil.leFactory.createVariable("A" + IndexGenerator.getStringIndex()), WSMOUtil.leFactory.createVariable("V" + IndexGenerator.getStringIndex()));
                
    //          !!!!!!!!!!!!!MembershipMolecule rightSideMembership2 = WSMOUtil.leFactory.createMemberShipMolecule(targetRangeVariable, targetOwner);//!!!!!!!!!!!!!!!!!!!!!!!!
                LogicalExpression rightSideMembership = WSMOUtil.leFactory.createConjunction(rightSideMembership1, rightSideMembership2);
                //-------------------
                LogicalExpression attributeOccurenceConditionExppression = null;
                LogicalExpression classExpressionAttributeValueCondition = null;
                LogicalExpression dataLitteralAttributeValueCondition = null;
                LogicalExpression individualIDAttributeValueCOndition = null;
                LogicalExpression classExpressionValueCondition = null;
                LogicalExpression dataLitteralValueCondition = null;
                LogicalExpression individualIDValueCondition = null;
                
    
                
                if (conditions!=null && !conditions.isEmpty()){
                    Iterator<? extends Condition> cIt = conditions.iterator();
                    while (cIt.hasNext()){
                        Condition crtCondition = cIt.next(); 
                        if (crtCondition instanceof AttributeOccurenceCondition){                        
                            attributeOccurenceConditionExppression = getAttributeOccurenceConditionExpression((AttributeOccurenceCondition)crtCondition);                     
                        }else
                        if (crtCondition instanceof AttributeTypeCondition){
                            classExpressionAttributeValueCondition = getAttributeTypeConditionExpression((AttributeTypeCondition)crtCondition);
                        }else
                        if (crtCondition instanceof AttributeValueCondition){
                            dataLitteralAttributeValueCondition = getAttributeValueConditionExpression((AttributeValueCondition)crtCondition, crtMapping, sourceVariable, targetVariable);
                        }else
                        if (crtCondition instanceof TypeCondition){
                            classExpressionValueCondition = getTypeConditionExepression((TypeCondition)crtCondition, targetRangeVariable);
                        }else
                        if (crtCondition instanceof ValueCondition){
                            dataLitteralValueCondition = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, null, targetRangeVariable);
                        }                       
                    }                       
                }           
                //----------------------
                Conjunction conditionsExpr = null;
                if (attributeOccurenceConditionExppression != null)
                	conditionsExpr = WSMOUtil.leFactory.createConjunction(rightSideMembership, attributeOccurenceConditionExppression);
                
                if (classExpressionAttributeValueCondition!=null && conditionsExpr!=null)
                	conditionsExpr = WSMOUtil.leFactory.createConjunction(conditionsExpr, classExpressionAttributeValueCondition);
                else
                    if (classExpressionAttributeValueCondition!=null)
                    	conditionsExpr = WSMOUtil.leFactory.createConjunction(rightSideMembership, classExpressionAttributeValueCondition);
                	
                if (dataLitteralAttributeValueCondition!=null && conditionsExpr!=null)
                	conditionsExpr = WSMOUtil.leFactory.createConjunction(conditionsExpr, dataLitteralAttributeValueCondition);
                else
                    if (dataLitteralAttributeValueCondition!=null)
                    	conditionsExpr = WSMOUtil.leFactory.createConjunction(rightSideMembership, dataLitteralAttributeValueCondition);
                	
                if (individualIDAttributeValueCOndition!=null && conditionsExpr!=null)
                	conditionsExpr = WSMOUtil.leFactory.createConjunction(conditionsExpr, individualIDAttributeValueCOndition);
                else
                    if (individualIDAttributeValueCOndition!=null)
                    	conditionsExpr = WSMOUtil.leFactory.createConjunction(rightSideMembership, individualIDAttributeValueCOndition);
                	            
                if (classExpressionValueCondition!=null  && conditionsExpr!=null)
                	conditionsExpr = WSMOUtil.leFactory.createConjunction(conditionsExpr, classExpressionValueCondition);
                else
                    if (classExpressionValueCondition!=null)
                    	conditionsExpr = WSMOUtil.leFactory.createConjunction(rightSideMembership, classExpressionValueCondition);
                	            
                if (dataLitteralValueCondition!=null && conditionsExpr!=null)
                	conditionsExpr = WSMOUtil.leFactory.createConjunction(conditionsExpr, dataLitteralValueCondition);
                else
                    if (dataLitteralValueCondition!=null)
                    	conditionsExpr = WSMOUtil.leFactory.createConjunction(rightSideMembership, dataLitteralValueCondition);
                	
                if (individualIDValueCondition!=null && conditionsExpr!=null)
                	conditionsExpr = WSMOUtil.leFactory.createConjunction(conditionsExpr, individualIDValueCondition);
                else
                    if (individualIDValueCondition!=null)
                    	conditionsExpr = WSMOUtil.leFactory.createConjunction(rightSideMembership, individualIDValueCondition);
    
                LogicProgrammingRule rule = null; //WSMOUtil.leFactory.createLogicProgrammingRule()
                if (conditionsExpr!=null){
                    rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, conditionsExpr);
                }
                else{
                	//there are no conditions so the empty-isntaces removal condition can be added
                	rightSideMembership = WSMOUtil.leFactory.createConjunction(rightSideMembership, rightSideAttributeValue);
                	rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, rightSideMembership);      
                
                }
                Axiom anAxiom = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#caMappingRule" + IndexGenerator.getStringIndex()));
                anAxiom.addDefinition(rule);
                result.add(anAxiom);
            }
        }
        return result;
    }  

    public Set<Axiom> generateMappingRulesForA2CMappings(List<Attribute2Class> mappings, boolean filterMappings) {
        Set<Axiom> result = new HashSet<Axiom>();
        Iterator<Attribute2Class> acIt = mappings.iterator();
        while (acIt.hasNext()){
            Attribute2Class crtMapping = acIt.next();
            if (!filterMappings || mappingSourceContainsARelevantSourceAttribute(crtMapping.getSource())){
                Term attrName = WSMOUtil.leFactory.createVariable("A" + IndexGenerator.getStringIndex());
                Term attrRangeName = WSMOUtil.leFactory.createVariable("AR" + IndexGenerator.getStringIndex());            
                Term sourceVariable = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex()); 
                Term targetVariable = createFunctionSymbol(function, sourceVariable, extractIdentifier(crtMapping.getTarget().getId()));
                        
                Term u3t_range = extractIdentifier(crtMapping.getTarget().getId());
                Term u4s_attribute = extractIdentifier(crtMapping.getSource().getId());
                Term u5s = WSMOUtil.leFactory.createVariable("Z" + IndexGenerator.getStringIndex());
                
                MembershipMolecule leftSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(targetVariable, extractIdentifier(crtMapping.getTarget().getId()));
                AttributeValueMolecule leftSideAttibuteValueMolecule = WSMOUtil.leFactory.createAttributeValue(targetVariable, attrName, attrRangeName);
                
                List<Molecule> molecules = new ArrayList<Molecule>(); 
                molecules.add(leftSideAttibuteValueMolecule); 
                molecules.add(leftSideMembershipMolecule);
                CompoundMolecule leftSideMolecule = WSMOUtil.leFactory.createCompoundMolecule(molecules);
                            
                MembershipMolecule rightSideMembershipMolecule1 = WSMOUtil.leFactory.createMemberShipMolecule(sourceVariable, getAttribute(crtMapping.getSource().getId()).getConcept().getIdentifier());
                AttributeValueMolecule rightSideAttributeValueMolecule1 = WSMOUtil.leFactory.createAttributeValue(sourceVariable, u4s_attribute, u5s);
                MembershipMolecule rightSideMembershipMolecule2 = WSMOUtil.leFactory.createMemberShipMolecule(createFunctionSymbol(function, u5s, u3t_range), u3t_range);
                AttributeValueMolecule rightSideAttributeValueMolecule2 = WSMOUtil.leFactory.createAttributeValue(createFunctionSymbol(function, u5s, u3t_range), attrName, attrRangeName);
                molecules = new ArrayList<Molecule>(); 
                molecules.add(rightSideMembershipMolecule2); 
                molecules.add(rightSideAttributeValueMolecule2);           
                CompoundMolecule rightSideCompoundMolecule2 = WSMOUtil.leFactory.createCompoundMolecule(molecules);
                molecules = new ArrayList<Molecule>(); 
                molecules.add(rightSideMembershipMolecule1); 
                molecules.add(rightSideAttributeValueMolecule1);           
                CompoundMolecule rightSideCompoundMolecule1 = WSMOUtil.leFactory.createCompoundMolecule(molecules);            
                Conjunction rightSide = WSMOUtil.leFactory.createConjunction(rightSideCompoundMolecule1, rightSideCompoundMolecule2);
                
                //-------------------
                LogicalExpression attributeOccurenceConditionExppression = null;
                LogicalExpression classExpressionAttributeValueCondition = null;
                LogicalExpression dataLitteralAttributeValueCondition = null;
                LogicalExpression individualIDAttributeValueCOndition = null;
                LogicalExpression classExpressionValueCondition = null;
                LogicalExpression dataLitteralValueCondition = null;
                LogicalExpression individualIDValueCondition = null;
                
                Collection<Condition> cond = new HashSet<Condition>();
                if (crtMapping.getSource().getConditions()!=null)
                	cond.addAll(crtMapping.getSource().getConditions());
                if (crtMapping.getTarget().getConditions()!=null)
                	cond.addAll(crtMapping.getTarget().getConditions());            
                if (cond!=null && !cond.isEmpty()){
                    Iterator<? extends Condition> cIt = cond.iterator();
                    while (cIt.hasNext()){
                        Condition crtCondition = cIt.next(); 
                        if (crtCondition instanceof AttributeOccurenceCondition){                        
                            attributeOccurenceConditionExppression = getAttributeOccurenceConditionExpression((AttributeOccurenceCondition)crtCondition);                     
                        }else
                        if (crtCondition instanceof AttributeTypeCondition){
                            classExpressionAttributeValueCondition = getAttributeTypeConditionExpression((AttributeTypeCondition)crtCondition);
                        }else
                        if (crtCondition instanceof AttributeValueCondition){
                            dataLitteralAttributeValueCondition = getAttributeValueConditionExpression((AttributeValueCondition)crtCondition, crtMapping, sourceVariable, targetVariable);
                        }else
                        if (crtCondition instanceof TypeCondition){
                            classExpressionValueCondition = getTypeConditionExepression((TypeCondition)crtCondition, attrRangeName);
                        }else
                        if (crtCondition instanceof ValueCondition){
                            dataLitteralValueCondition = getValueConditionExpression((ValueCondition)crtCondition, crtMapping, sourceVariable, null);
                        }                       
                    }                       
                }           
                //----------------------
                Conjunction conditions = null;
                if (attributeOccurenceConditionExppression != null)
                    conditions = WSMOUtil.leFactory.createConjunction(rightSide, attributeOccurenceConditionExppression);
                if (classExpressionAttributeValueCondition!=null && conditions!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(conditions, classExpressionAttributeValueCondition);
                else
                    if (classExpressionAttributeValueCondition!=null)
                        conditions = WSMOUtil.leFactory.createConjunction(rightSide, classExpressionAttributeValueCondition);            	
    
                if (dataLitteralAttributeValueCondition!=null && conditions!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(conditions, dataLitteralAttributeValueCondition);
                else
                    if (dataLitteralAttributeValueCondition!=null)
                        conditions = WSMOUtil.leFactory.createConjunction(rightSide, dataLitteralAttributeValueCondition);
    	
                if (individualIDAttributeValueCOndition!=null  && conditions!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(conditions, individualIDAttributeValueCOndition);
                else
                    if (individualIDAttributeValueCOndition!=null)
                        conditions = WSMOUtil.leFactory.createConjunction(rightSide, individualIDAttributeValueCOndition);
                	
                if (classExpressionValueCondition!=null && conditions!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(conditions, classExpressionValueCondition);
                else
                    if (classExpressionValueCondition!=null)
                        conditions = WSMOUtil.leFactory.createConjunction(rightSide, classExpressionValueCondition);
                
                if (dataLitteralValueCondition!=null && conditions!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(conditions, dataLitteralValueCondition);
                else
                	if (dataLitteralValueCondition!=null)
                		conditions = WSMOUtil.leFactory.createConjunction(rightSide, dataLitteralValueCondition);
                
                if (individualIDValueCondition!=null && conditions!=null)
                    conditions = WSMOUtil.leFactory.createConjunction(conditions, individualIDValueCondition);
                else
                    if (individualIDValueCondition!=null)
                        conditions = WSMOUtil.leFactory.createConjunction(rightSide, individualIDValueCondition);
                	
            	//the case when value conditions are present on the source attribute a different result should be prodiced than when such a condition is missing                       
	            LogicProgrammingRule rule = null; //WSMOUtil.leFactory.createLogicProgrammingRule()
    
    	        if (dataLitteralValueCondition==null){
        	        if (conditions!=null)
            	        rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, conditions);
                	else
	                    rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, rightSide);            
    	        }
        	    else{
            		rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMembershipMolecule, dataLitteralValueCondition);
	            }
                Axiom anAxiom = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#acMappingRule" + IndexGenerator.getStringIndex()));
                anAxiom.addDefinition(rule);
                result.add(anAxiom);             
            }
        }
        return result; 
    }
    

    /**
     * @param concept
     */
    private void addConceptToAuxiliary(Concept concept) {
        if (concept != null && !requiredConceptsDefinitions.contains(concept)){
            requiredConceptsDefinitions.add(concept);
        }
        
    }
    
    private Set<Axiom> getAuxilliarryAxioms(){
    	Set<Axiom> result = new HashSet<Axiom>();
    	
    	//mediated(?x, ?c)[?y hasValue ?z] memberOf ?subC :- ?subC subconceptOf ?c and mediated(?x, ?c)[?y hasValue ?z] memberOf ?c

    	Variable sourceInstanceVariable = WSMOUtil.leFactory.createVariable("I" + IndexGenerator.getStringIndex());
    	Variable subCVariable = WSMOUtil.leFactory.createVariable("subC" + IndexGenerator.getStringIndex());
    	Variable cVariable = WSMOUtil.leFactory.createVariable("C" + IndexGenerator.getStringIndex());
    	
    	ConstructedTerm  leftCT = createFunctionSymbol(function, sourceInstanceVariable, subCVariable);
    	
    	//mediated(?x, ?subC)[?y hasValue ?z]
    	Variable theAttribute = WSMOUtil.leFactory.createVariable("Y" + IndexGenerator.getStringIndex());
    	Variable theAttributeRange = WSMOUtil.leFactory.createVariable("Z" + IndexGenerator.getStringIndex());
    	
        AttributeValueMolecule leftSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(leftCT, theAttribute, theAttributeRange);  
       //mediated(?x, ?subC) memberOf ?subC
        MembershipMolecule leftSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(leftCT, subCVariable);
        //mediated(?x, ?subC)[?y hasValue ?z] memberOf ?subC
        List<Molecule> leftSideMolecules = new ArrayList<Molecule>();
        leftSideMolecules.add(leftSideAVMolecule); leftSideMolecules.add(leftSideMembershipMolecule);
        CompoundMolecule leftSideMolecule  = WSMOUtil.leFactory.createCompoundMolecule(leftSideMolecules);
    	
        
    	
    	
    	ConstructedTerm  rightCT = createFunctionSymbol(function, sourceInstanceVariable, cVariable);    	
    	//mediated(?x, ?c)[?y hasValue ?z]
        AttributeValueMolecule rightSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(rightCT, theAttribute, theAttributeRange);  
       //mediated(?x, ?c) memberOf ?c
        MembershipMolecule rightSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(rightCT, cVariable);
        //mediated(?x, ?c)[?y hasValue ?z] memberOf ?c
        List<Molecule> rightSideMolecules = new ArrayList<Molecule>();
        rightSideMolecules.add(rightSideAVMolecule); rightSideMolecules.add(rightSideMembershipMolecule);
        CompoundMolecule rightSideMolecule  = WSMOUtil.leFactory.createCompoundMolecule(rightSideMolecules);
        
        SubConceptMolecule sCM = WSMOUtil.leFactory.createSubConceptMolecule(subCVariable, cVariable);
        
        LogicalExpression rightSide = WSMOUtil.leFactory.createConjunction(sCM, rightSideMolecule);
        
        LogicProgrammingRule rule = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, rightSide);
        
        Axiom anAxiom = WSMOUtil.wsmoFactory.createAxiom(WSMOUtil.createIRI(MediationWSML2Reasoner.reasoningSpace + "#AuxilliarY" + IndexGenerator.getStringIndex()));
        //->> if this rule is added in conjunction with rule 2 the query answering time is longer
        //anAxiom.addDefinition(rule);

        //the second rule
        
        leftCT = createFunctionSymbol(function, sourceInstanceVariable, cVariable);
        leftSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(leftCT, subCVariable);
        leftSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(leftCT, theAttribute, theAttributeRange);  
        leftSideMolecules = new ArrayList<Molecule>();
        leftSideMolecules.add(leftSideAVMolecule); leftSideMolecules.add(leftSideMembershipMolecule);
        leftSideMolecule  = WSMOUtil.leFactory.createCompoundMolecule(leftSideMolecules);
        
        rightCT = createFunctionSymbol(function, sourceInstanceVariable, subCVariable);    	
    	//mediated(?x, ?c)[?y hasValue ?z]
        rightSideAVMolecule = WSMOUtil.leFactory.createAttributeValue(rightCT, theAttribute, theAttributeRange);  
       //mediated(?x, ?c) memberOf ?c
        rightSideMembershipMolecule = WSMOUtil.leFactory.createMemberShipMolecule(rightCT, subCVariable);
        //mediated(?x, ?c)[?y hasValue ?z] memberOf ?c
        rightSideMolecules = new ArrayList<Molecule>();
        rightSideMolecules.add(rightSideAVMolecule); rightSideMolecules.add(rightSideMembershipMolecule);
        rightSideMolecule  = WSMOUtil.leFactory.createCompoundMolecule(rightSideMolecules);
        
        sCM = WSMOUtil.leFactory.createSubConceptMolecule(subCVariable, cVariable);
        
        rightSide = WSMOUtil.leFactory.createConjunction(sCM, rightSideMolecule);
        
        
        
        LogicProgrammingRule rule2 = WSMOUtil.leFactory.createLogicProgrammingRule(leftSideMolecule, rightSide);
        anAxiom.addDefinition(rule2);
        
        
//        result.add(anAxiom);           
    	
    	return result;
    }

    private void computeRelevantConceptAndAttributes(Set<Instance> theSourceInstances) {
        Set <Instance> processed = new HashSet <Instance> ();
        for (Instance sourceInstance : theSourceInstances){
            addReferences(sourceInstance, processed);
        }
    }
    
    private Set <Identifier> addReferences(Instance theSourceInstance, Set <Instance> processed) {
        Set <Identifier> result = new HashSet <Identifier> ();
        if (!processed.contains(theSourceInstance)){
            processed.add(theSourceInstance);
            for (Concept concept : theSourceInstance.listConcepts()){
                relevantSourceConcepts.add(concept.getIdentifier());
            }
            for (Identifier identifier : theSourceInstance.listAttributeValues().keySet()){
                addRelevantSourceAttribute(identifier, theSourceInstance);
                for (Value v : theSourceInstance.listAttributeValues(identifier)){
                    if (v instanceof Instance){
                        addReferences((Instance) v, processed);
                    }
                }
            }
        }
        return result;
    }

    private Set<Identifier> getSuperConcepts(Concept theConcept) {
        Set <Identifier> result = new HashSet <Identifier> ();
        result.add(theConcept.getIdentifier());
        for (Concept concept : theConcept.listSuperConcepts()){
            result.addAll(getSuperConcepts(concept));
        }
        return result;
    }

    private void addRelevantSourceAttribute(Identifier theIdentifier, Instance theSourceInstance) {
        for (Concept concept : getConcepts(theSourceInstance)){
            Set <Identifier> ids = relevantSourceAttributes.get(concept.getIdentifier());
            if (ids == null){
                ids = new HashSet <Identifier> ();
                relevantSourceAttributes.put(concept.getIdentifier(), ids);
            }
            ids.add(theIdentifier);
        }
    }
    
    private Set <Concept> getConcepts(Instance theInstance) {
        Set <Concept> result = new HashSet <Concept> ();
        for (Concept concept : theInstance.listConcepts()){
            result.addAll(getConcepts(concept));
        }
        return result;
    }

    private Set<Concept> getConcepts(Concept theConcept) {
        Set <Concept> result = new HashSet <Concept> ();
        result.add(theConcept);
        for (Concept concept : theConcept.listSuperConcepts()){
            result.addAll(getConcepts(concept));
        }
        return result;
    }
    
    private boolean mappingSourceContainsARelevantSourceConcept(ClassExpr theClassExpr) {
        for (Identifier conceptId : getReferencedSourceConcepts(theClassExpr)){
            if (relevantSourceConcepts.contains(conceptId)){
                return true;
            }
        }
        return false;
    }

    private Set <Identifier> getReferencedSourceConcepts(ClassExpr theClassExpr) {
        Set <Identifier> result = new HashSet <Identifier> ();
        if (theClassExpr.isComplexExpression()){
            result.addAll(getReferencedSourceConcepts(theClassExpr.getExpresionDefinition()));
        }
        else{
             result.add(((ClassId) theClassExpr.getId()).getIdentifier());
        }
        return result;
    }

    private Set<Identifier> getReferencedSourceConcepts(ExpressionDefinition theExpressionDefinition) {
        Set <Identifier> result = new HashSet <Identifier> ();
        if (theExpressionDefinition instanceof ComplexExpression){
            for (ExpressionDefinition definition : ((ComplexExpression) theExpressionDefinition).getSubExpressions()){
                result.addAll(getReferencedSourceConcepts(definition));
            }
        }
        else if (theExpressionDefinition instanceof ClassId){
            result.add(((ClassId) theExpressionDefinition).getIdentifier());
        }
        return result;
    }
    
    private boolean mappingSourceContainsARelevantSourceAttribute(AttributeExpr theAttributeExpr) {
        for (Attribute attribute : getReferencedSourceAttributes(theAttributeExpr)){
            Identifier conceptId = attribute.getConcept().getIdentifier();
            if (relevantSourceAttributes.containsKey(conceptId)){
                Set <Identifier> attributes = relevantSourceAttributes.get(conceptId);
                if (attributes != null && attributes.contains(attribute.getIdentifier())){
                    return true;
                }
            }
        }
        return false;
    }

    private Set <Attribute> getReferencedSourceAttributes(AttributeExpr theAttributeExpr) {
        Set <Attribute> result = new HashSet <Attribute> ();
        if (theAttributeExpr.isComplexExpression()){
            result.addAll(getReferencedSourceAttributes(theAttributeExpr.getExpresionDefinition()));
        }
        else{
             result.add(((AttributeId) theAttributeExpr.getId()).getAttribute());
        }
        return result;
    }

    private Set <Attribute> getReferencedSourceAttributes(ExpressionDefinition theExpressionDefinition) {
        Set <Attribute> result = new HashSet <Attribute> ();
        if (theExpressionDefinition instanceof ComplexExpression){
            for (ExpressionDefinition definition : ((ComplexExpression) theExpressionDefinition).getSubExpressions()){
                result.addAll(getReferencedSourceAttributes(definition));
            }
        }
        else if (theExpressionDefinition instanceof AttributeId){
            result.add(((AttributeId) theExpressionDefinition).getAttribute());
        }
        return result;
    }
}
 
