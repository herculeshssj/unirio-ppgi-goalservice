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
 * Committed by $Author: jacek_kopecky $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/nonwsmo/grounding/WSDL1_1EndpointGrounding.java,v $,
 * </pre>
 *
 * @author Matthew Moran
 *
 * @version $Revision: 1.8 $ $Date: 2007-12-10 17:28:24 $
 */

public class WSDL1_1EndpointGrounding implements EndpointGrounding {

    static Logger logger = Logger.getLogger(WSDL1_1EndpointGrounding.class);

    private String portType;
	private String operation;
	private String message;
	private String wsdlURI;
	
    public static void main(String[] argv) {
    	try {
	    	WSDL1_1EndpointGrounding grounding = 
	    		new WSDL1_1EndpointGrounding("https://kahhdkahj#wsdl.interfaceMessageReference/interface/operation/message");
	    		System.out.println("portType = " + grounding.getPortType());
	    		System.out.println("operation = " + grounding.getOperation());
	    		System.out.println("message = " + grounding.getMessage());
    	} catch (WSDL1_1GroundingException gException) {
    		gException.printStackTrace();
    	}
    }	
	
    /**
	 * 
	 */
	public WSDL1_1EndpointGrounding() {
		operation = "";
		portType = "";
		message = "";
		wsdlURI = "";
	}

	/**
	 * @param groundingURI The URI used in the choreography descripion to ground a particular
	 *                     transition rule
	 */
	public WSDL1_1EndpointGrounding(String theGroundingURI) throws WSDL1_1GroundingException {
		operation = "";
		portType = "";
		message = "";
		wsdlURI = "";
		
		// Use a regular expression to extract the parts from the String making up the Grounding URI
		try {
			extractPartsFromGroundingURI(theGroundingURI);
		} catch (IllegalStateException e) {
			logger.debug("WSDL1_1Grounding exception - stack trace follows:\n" + e.getStackTrace().toString());
			throw new WSDL1_1GroundingException(e.getMessage(), e.getCause());
		}
		
		
		// TODO workaround
		if("".equals(wsdlURI)) {
			wsdlURI = theGroundingURI;
		}
	}


	public GroundingType getType() {
		return GroundingType.WSDL1_1;
	}

	/**
	 * @return Returns the operation.
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * @param operation The operation to set.
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}

	/**
	 * @return Returns the portType.
	 */
	public String getPortType() {
		return portType;
	}

	/**
	 * @param portType The portType to set.
	 */
	public void setPortType(String portType) {
		this.portType = portType;
	}

	/**
	 * @return Returns the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return Returns the message
	 */
	public String getWsdlURI() {
		return wsdlURI;
	}

	// The specification for the grounding URI pattern can be found at http://wsmo.org/TR/d24/d24.2/v0.1/
	// An example is: 
	// http://aia.ebankinter.com/wsBrokerService/Service1.asmx#wsdl.interfaceMessageReference(interface/operation/method)	
	// There are four parts of interest 
	// 1. The part up to but not including the '#' gives the URI of the WSDL for the service
	// 2. The interface gives the relevant WSDL2.0 interface (portType for WSDL1.1)
	// 3. operation gives the relevant portType
	// 4. method gives the relevant method
	//
	// Quick review of reg expression patterns used here:
	// ( )     --> delimit pattern groups 
	// ?       --> 0 or 1 occurrences of the previous characters
	// \.      --> escape for '.'
	// \\.     --> as above but need double slash because of enclosing quotes (" ")
	// .       --> any character is valid
	// [.[^#]] --> any character with the exception of '#' is valid, can be simplified to [^#]
	// +       --> 1 or many character occurrences of the previous character
	// {1,1}   --> min 1 and max 1 occurrence of the previous character
	// \w      --> any valid word character (look this up at http://java.sun.com/docs/books/tutorial/extra/regex/bounds.html)
	private void extractPartsFromGroundingURI(String theGroundingURI) throws IllegalStateException {
		
		String regExpression = "^([^#]+)#wsdl\\.interfaceMessageReference\\((\\w+)/(\\w+)/(\\w+)\\)$";

		logger.debug("Reg exp = " + regExpression);
		logger.debug("input = " + theGroundingURI);
		
		Pattern pattern = Pattern.compile(regExpression);
        Matcher matcher = pattern.matcher(theGroundingURI);

        try {
        while(matcher.find()) {
	        wsdlURI = matcher.group(1);
	        logger.debug("Grounding wsdlURI = " + wsdlURI);
	        portType = matcher.group(2);
	        logger.debug("Grounding portType = " + portType);
	        operation = matcher.group(3);
	        logger.debug("Grounding operation = " + operation);
	        message = matcher.group(4);
	        logger.debug("Grounding message = " + message);
        }
        } catch (IllegalStateException e) {
        	throw e;
        }
	}

	public String toString() {
		return this.getWsdlURI()+"#wsdl.interfaceMessageReference("+this.getPortType()+"/"+this.getOperation()+"/"+this.getMessage()+")";
	}
	
}

