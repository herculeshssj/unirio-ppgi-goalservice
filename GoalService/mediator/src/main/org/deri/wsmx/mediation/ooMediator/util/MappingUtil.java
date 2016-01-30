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


import org.omwg.mediation.language.objectmodel.api.ComplexExpression;
import org.omwg.mediation.language.objectmodel.api.Expression;
import org.omwg.mediation.language.objectmodel.api.ExpressionDefinition;
import org.omwg.mediation.language.objectmodel.api.Id;
import org.omwg.mediation.language.objectmodel.api.conditions.AttributeCondition;
import org.wsmo.common.Entity;
import org.wsmo.common.Identifier;


public final class MappingUtil {
    
    public static boolean compare(AttributeCondition src, AttributeCondition tgt){
        if (src==null || tgt==null)
            return false;
        if (src.equals(tgt))
            return true;
        
        /*
        if (src instanceof ClassExpressionValueCondition && tgt instanceof ClassExpressionValueCondition){
            ClassExpressionValueCondition cevcSrc = (ClassExpressionValueCondition)src;
            ClassExpressionValueCondition cevcTgt = (ClassExpressionValueCondition)tgt;
            return (cevcSrc.getAttributeID().equals(cevcTgt.getAttributeID()))&&
                (cevcSrc.getClassID().equals(cevcTgt.getClassID()))&&
                    (cevcSrc.getClassExpression().equals(cevcTgt.getClassExpression()));
        }
        if (src instanceof DataLiteralValueCondition && tgt instanceof DataLiteralValueCondition){
            DataLiteralValueCondition dlvcSrc = (DataLiteralValueCondition)src;
            DataLiteralValueCondition dlvcTgt = (DataLiteralValueCondition)tgt;
            return (dlvcSrc.getAttributeID().equals(dlvcTgt.getAttributeID()))&&
                (dlvcSrc.getClassID().equals(dlvcTgt.getClassID()))&&
                    (dlvcSrc.getDataLiteral().equals(dlvcTgt.getDataLiteral()));
        }
        if (src instanceof IndividualIDValueCondition && tgt instanceof IndividualIDValueCondition){
            IndividualIDValueCondition iivcSrc = (IndividualIDValueCondition)src;
            IndividualIDValueCondition iivcTgt = (IndividualIDValueCondition)tgt;
            return (iivcSrc.getAttributeID().equals(iivcTgt.getAttributeID()))&&
                (iivcSrc.getClassID().equals(iivcTgt.getClassID()))&&
                    (iivcSrc.getIndividualID().equals(iivcTgt.getIndividualID()));
        }*/
        
        return false;
    }
    
	
	public static String generatetRuleId(Expression source, Expression target){
		
        return  generateId(source) + generateId(target);
	}
    
    public static String generateId(Expression expr){
		
		 String prefix = "";
		 
		if (!expr.isComplexExpression()){
			prefix = generateId((Id)expr.getId(), prefix);
		}
		else
			prefix = generateId((ComplexExpression)expr.getExpresionDefinition(), prefix);
		return prefix;
	}
	
	private static String generateId(Id ed, String prefix){
		if (ed instanceof Entity){
			Identifier eId = ((Entity)ed).getIdentifier();
			if (eId instanceof org.wsmo.common.IRI){ 
				String nameSpace = ((org.wsmo.common.IRI)eId).getNamespace();
				String localName = ((org.wsmo.common.IRI)eId).getLocalName();
				if (prefix.indexOf(nameSpace)>-1){
					return localName;
				}
				else{
					return eId.toString();
				}					
			}				
		}
		return ed.plainText();
		
  }

	private static String generateId(ComplexExpression ce, String prefix){
		String result = ce.getOperator().toString();
		for (ExpressionDefinition ed : ce.getSubExpressions()){
			if (ed instanceof Id){
				result = result + generateId((Id)ed, prefix+result);				
			}
			else{
				result = result + generateId((ComplexExpression)ed, prefix+result);
			}
		}
		return result;
  }

}
