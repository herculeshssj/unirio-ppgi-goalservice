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

package ie.deri.wsmx.core.management.httpadapter;

import ie.deri.wsmx.core.management.commandprocessor.CreateMBeanCommandProcessor;

import java.io.IOException;

import javax.management.JMException;

import org.w3c.dom.Document;

/**
 * CreateMBeanCommandProcessor, processes a request for creating and registering
 * an MBean
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/httpadapter/CreateMBeanHttpCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-08-01 10:23:35 $
 */ 
public class CreateMBeanHttpCommandProcessor extends CreateMBeanCommandProcessor
                                             implements HttpCommandProcessor {

    /**
     * Constructs a new CreateMBeanCommandProcessor
     */
    public CreateMBeanHttpCommandProcessor() {
        super();
    }

    public Document executeRequest(HttpInputStream in) throws IOException,
            JMException {
        objectVariable = in.getVariable("objectname");
        classVariable = in.getVariable("class");
        int i = 0;
        boolean valid = false;
        do {
            String parameterType = in.getVariable("type" + i);
            String parameterValue = in.getVariable("value" + i);
            valid = (parameterType != null && parameterValue != null);
            if (valid) {
                parameterTypes.put("type" + i, parameterType);
                parameterValues.put("value" + i, parameterValue);
            }
            i++;
        } while (valid);
        return execute();
    }

}
