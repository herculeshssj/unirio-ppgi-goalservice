/*
 * Copyright (c) 2007 National University of Ireland, Galway
 *                    Open University, Milton Keynes
 *
 * Licensed under MIT License
 * 
 * Permission is hereby granted, free of charge, to any person 
 * obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons 
 * to whom the Software is furnished to do so, subject to the 
 * following conditions:
 *
 * The above copyright notice and this permission notice shall 
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS 
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES 
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT 
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.wsmo.execution.common;

import org.wsmo.execution.common.exception.SystemException;
import org.wsmo.execution.common.nonwsmo.Context;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;

/**
 * This interface is intended to provide methods for the receipt of WSML messages that 
 * are related to an existing conversation context.
 *
 * <pre>
 * Created on 10-May-2005
 * Committed by $Author: maciejzaremba $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/api/org/wsmo/execution/common/Receiver.java,v $,
 * </pre>
 *
 * @author Michal Zaremba
 * @author Liliana Cabral 
 * @author John Domingue
 * @author David Aiken
 * @author Emilia Cimpian
 * @author Thomas Haselwanter
 * @author Mick Kerrigan
 * @author Adrian Mocan
 * @author Matthew Moran
 * @author Brahmananda Sapkota
 * @author Maciej Zaremba
 *
 * @version $Revision: 1.4 $ $Date: 2007-06-14 14:26:49 $
 */

public interface Receiver {

	/**
	 * Receive a WSML message corresponding to a particular conversation context. 
     * @param WSMLDocument containing WSML data that is required for an interaction between the 
     * service requester and WSMX.
     * @return Context that can be used for further interaction with WSMX
     * @throws SystemException throws an exception if there is a problem reported by WSMX for this task	 */
    public Context receive(WSMLDocument wsmlMessage, Context context) throws SystemException;
}
