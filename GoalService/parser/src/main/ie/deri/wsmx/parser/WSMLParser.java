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
package ie.deri.wsmx.parser;

import ie.deri.wsmx.commons.Helper;
import ie.deri.wsmx.core.configuration.annotation.Exposed;
import ie.deri.wsmx.core.configuration.annotation.WSMXComponent;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.choreography.ChoreographyFactoryRI;
import org.deri.wsmo4j.io.parser.wsml.ParserImpl;
import org.wsmo.common.Entity;
import org.wsmo.execution.common.component.Parser;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.factory.ChoreographyFactory;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;

 /**
   * 
   * Provides parsing services for WSML.
   *
   * <pre>
   * Created on 24-Oct-2005
   * Committed by $Author$
   * $Source$,
   * </pre>
   *
   * @author Michal Zaremba
   * @author Thomas Haselwanter
   *
   * @version $Revision$ $Date$
 */
@WSMXComponent(name = "Parser",
               description = "This is a parser.",
               events = "PARSER")
public class WSMLParser implements Parser {
   
	static Logger logger = Logger.getLogger(WSMLParser.class);  

	@Exposed(description = "Parses WSML documents into an object model.")
    public Set<Entity> parse(WSMLDocument wsmlDocument) throws ComponentException, UnsupportedOperationException {
       
        //instantiate to choreography enabled parser directly because the
        //factory creates the non-choreography aware parser  	
        
        Entity[]  parsed = null;
        try {
            String docForParsing = wsmlDocument.getContent();
            StringReader reader = new StringReader(docForParsing);
            parsed = Helper.parse(reader);
        } catch (Exception e) {
        	throw new ComponentException("Parsing failed:" + e.getMessage(), e);
        }
        
        Set<Entity> set = new HashSet<Entity>();
        
        for (int i=0; i < parsed.length; i++) {
            set.add(parsed[i]);
        }  
        logger.debug("parsing finished");
        return set;
    }
}
