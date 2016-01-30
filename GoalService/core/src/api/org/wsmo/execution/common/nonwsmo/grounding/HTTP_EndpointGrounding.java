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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  */

package org.wsmo.execution.common.nonwsmo.grounding;

import java.util.regex.*;

import org.apache.log4j.Logger;


/**
 * Class to represent the information required to ground an endpoint whose
 * description is provided using WSDL1.1
 *
 * <pre>
 * Created on 21-Sep-2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/nonwsmo/grounding/HTTP_EndpointGrounding.java,v $,
 * </pre>
 *
 * @author Matthew Moran
 *
 * @version $Revision: 1.1 $ $Date: 2007-06-14 14:36:04 $
 */

public class HTTP_EndpointGrounding implements EndpointGrounding {

    static Logger logger = Logger.getLogger(HTTP_EndpointGrounding.class);

	private String URI;
	
	public HTTP_EndpointGrounding() {
		super();
		URI = "";
	}

	public HTTP_EndpointGrounding(String theGroundingURI) throws WSDL1_1GroundingException {
		URI = theGroundingURI;
	}

	public GroundingType getType() {
		return GroundingType.HTTP;
	}

	public String getURI() {
		return URI;
	}	
}

