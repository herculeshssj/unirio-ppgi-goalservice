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

package ie.deri.wsmx.core.management.commandprocessor;


import java.io.IOException;

import javax.management.JMException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * EmptyCommandProcessor, returns an empty xml tree
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/commandprocessor/EmptyCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.2 $ $Date: 2005-09-18 16:11:13 $
 */ 
public class EmptyCommandProcessor extends AbstractCommandProcessor {

    /**
     * Constructs a new EmptyCommandProcessor
     */
    public EmptyCommandProcessor() {
    	//nothing to do
    }

    @Override
	public Document execute() throws IOException,
            JMException {
        Document document = builder.newDocument();

        Element root = document.createElement("empty");
        document.appendChild(root);

        return document;
    }

}
