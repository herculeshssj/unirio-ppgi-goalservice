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

/**
 * This Interface specifies the basic behaviour of lightweight classes used
 * to pass information on what transport mechanism should be used to send
 * a message to a service.
 * For example, classes representing endpoint grounding info for WSDL1.1, WSDL2.0, J2EE etc.
 * are intended to implement this interface.
 *
 * <pre>
 * Created on 20-Sep-2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/nonwsmo/grounding/EndpointGrounding.java,v $,
 * </pre>
 *
 * @author Matthew Moran
 *
 * @version $Revision: 1.2 $ $Date: 2007-06-14 14:36:05 $
 */

public interface EndpointGrounding {

	public enum GroundingType
	{
		WSDL1_1,
		WSDL2_0,
		HTTP,
		J2EE
	}

	public GroundingType getType();

}

