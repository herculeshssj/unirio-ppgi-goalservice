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
package org.deri.wsmx.mediation.ooMediator.util;

import org.omwg.mediation.language.objectmodel.api.conditions.AttributeCondition;


/**
 * @author root
 *
 */
public final class MediationUtil {
    
	
    public static boolean compare(AttributeCondition src, AttributeCondition tgt){
        if (src==null || tgt==null)
            return false;
        if (src.equals(tgt))
            return true;
        /*if (src instanceof ClassExpressionValueCondition && tgt instanceof ClassExpressionValueCondition){
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
        }
        */
        return false;
    }
}
