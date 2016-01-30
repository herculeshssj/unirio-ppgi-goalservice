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

import ie.deri.wsmx.core.management.kerneldata.KernelData;
import ie.deri.wsmx.exceptions.KernelDataException;

import java.io.IOException;

import javax.management.JMException;

import org.w3c.dom.Document;

/**
 * The main view of the management console, displays some basic data.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/commandprocessor/MainCommandProcessor.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-08-01 10:24:23 $
 */ 
public class MainCommandProcessor extends AbstractCommandProcessor {

    public MainCommandProcessor() {
        super();
    }

    @Override
    public Document execute() throws IOException, JMException {
        try {
            return KernelData.getViewForNameStatically("wsmx.core.server");
        } catch (KernelDataException kde) {
            // FIXME replacement document
            return null;
        }
    }

}
