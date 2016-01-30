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

import junit.framework.TestCase;

/**
 * Interface or class description
 *
 * <pre>
 * Created on 07-Feb-2006
 * Committed by $Author: maitiu_moran $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/nonwsmo/grounding/TestGrounding.java,v $,
 * </pre>
 *
 * @author Matthew Moran
 *
 * @version $Revision: 1.1 $ $Date: 2006-02-07 12:44:57 $
 */

public class TestGrounding extends TestCase {

	public void testWSDL11() {
		String theGrounding = "https://kahhdkahj#wsdl.interfaceMessageReference/interface/operation/message";
		try {
			WSDL1_1EndpointGrounding wsdlGrounding = new WSDL1_1EndpointGrounding(theGrounding);
			System.out.println("portType = " + wsdlGrounding.getPortType());
			System.out.println("operation = " + wsdlGrounding.getOperation());
			System.out.println("message = " + wsdlGrounding.getMessage());
		} catch (WSDL1_1GroundingException e) {
			e.printStackTrace();
		}
	}

}

