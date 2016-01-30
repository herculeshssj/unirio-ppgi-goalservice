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

import java.util.HashMap;
import java.util.Random;

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

/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 10 Jun 2007
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/jaxws/org/ipsuper/nexcom/services/CEOApprovalWebService.java,v $, * @version $Revision: 1.1 $ $Date: 2007-10-11 14:32:17 $
 */
@WebService(name = "CEOApproval", targetNamespace = "http://ip-super.org/usecase/nexcom/")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
public class CEOApprovalWebService {

	protected static Logger logger = Logger.getLogger(VoIPSEEEntryPoint.class);
	private static Random randomGenarator = new Random(100);
	
	@WebMethod(operationName="requestCEOApproval", action="requestCEOApproval")
	@WebResult(name="QoSCEOResponse", partName="Document", targetNamespace = "http://ip-super.org/usecase/nexcom/")
    public Document requestCEOApproval(
   		 @WebParam(name="QoSCEORequest", partName="ApprovalRequest", targetNamespace = "http://ip-super.org/usecase/nexcom/") 
		 ApprovalRequest req) {

		Document resp = new Document();
		resp.setTtID(req.getTtID());
		resp.setSignedBy("John Brown, CEO");
		
		int value = randomGenarator.nextInt(100);
		
		//in 15% of cases reject
		if (value > 15){
			resp.setContents("QoS values has been accepted.");
			resp.setStatus(DocumentStatus.APPROVED);
		} else {
			resp.setContents("QoS values has not been accepted.");
			resp.setStatus(DocumentStatus.NOT_APPROVED);
		}
		
		return resp;
	}
}
 