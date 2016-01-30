/*
 * Copyright (c) 2007 National University of Ireland, Galway
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

package org.ipsuper.nexcom.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import org.apache.log4j.Logger;
import org.ipsuper.nexcom.datamodel.ApprovalRequest;
import org.ipsuper.nexcom.datamodel.Document;
import org.ipsuper.nexcom.datamodel.DocumentStatus;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;

/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 10 Jun 2007
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/jaxws/org/ipsuper/nexcom/services/LegalDepartmentWebService.java,v $, * @version $Revision: 1.1 $ $Date: 2007-10-11 14:32:17 $
 */
@WebService(name = "LegalDepartmentWebService", targetNamespace = "http://ip-super.org/usecase/nexcom/")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
public class LegalDepartmentWebService {

	private static WsmoFactory wsmoFactory = Factory.createWsmoFactory(new HashMap<String, Object>());
	protected static Logger logger = Logger.getLogger(VoIPSEEEntryPoint.class);
	private static Random randomGenarator = new Random(100);
	
	@WebMethod(operationName="requestLegalApproval", action="requestLegalApproval")
	@WebResult(name="QoSLegalResponse", partName="Document", targetNamespace = "http://ip-super.org/usecase/nexcom/")
    public Document requestLegalApproval(
   		 @WebParam(name="QoSLegalRequest", partName="ApprovalRequest", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
		 ApprovalRequest req){
		
		Document resp = new Document();
		resp.setTtID(req.getTtID());
		resp.setSignedBy("Andrew Fox, Head of Nexcom Legal Department");
		
		int value = randomGenarator.nextInt(100);
		//in 15% of cases reject
		if ( value > 15){
			resp.setContents("Legal department has not accepted VoIP agreeement.");
			resp.setStatus(DocumentStatus.APPROVED);
		} else {
			resp.setContents("Legal department has accepted VoIP.");
			resp.setStatus(DocumentStatus.NOT_APPROVED);
		}

		return resp;
	}

}
 