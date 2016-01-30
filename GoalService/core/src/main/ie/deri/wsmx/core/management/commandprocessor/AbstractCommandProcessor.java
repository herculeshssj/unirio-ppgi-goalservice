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
import javax.management.MBeanServer;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;

public abstract class AbstractCommandProcessor implements CommandProcessor {
    
    protected MBeanServer mBeanServer;
    protected DocumentBuilder builder;
    
    public AbstractCommandProcessor() {
        super();
    }

    protected abstract Document execute() throws IOException, JMException;

    public void setMBeanServer(MBeanServer server) {
        this.mBeanServer = server;
    }

    public void setDocumentBuilder(DocumentBuilder builder) {
        this.builder = builder;
    }

}
