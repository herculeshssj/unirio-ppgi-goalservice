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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.wsmo.common.Entity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.ParserException;


/**
 * Interface or class description
 *
 * <pre>
 * Created on 01-Jul-2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/parser/src/main/ie/deri/wsmx/parser/AlphaParser.java,v $,
 * </pre>
 *
 * @author Michal Zaremba
 *
 * @version $Revision: 1.7 $ $Date: 2006-02-09 03:15:48 $
 * @deprecated
 */
public class AlphaParser implements org.wsmo.execution.common.component.Parser {

    public Set<Entity> parse(WSMLDocument wsmlDocument) throws ComponentException, UnsupportedOperationException {
    
        HashMap <String, Object> props = new HashMap <String, Object> ();
        WsmoFactory factory = Factory.createWsmoFactory(null);
        LogicalExpressionFactory leFactory = Factory.createLogicalExpressionFactory(null);
        org.wsmo.wsml.Parser parser = Factory.createParser(null);
        
        Entity[]  parsed = null;
        try {
            String docForParsing = wsmlDocument.getContent();
            StringReader reader = new StringReader(docForParsing);
            parsed = parser.parse(reader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidModelException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }
        
        Set<Entity> set = new HashSet<Entity>();
        
        for (int i=0; i < parsed.length; i++) {
            set.add(parsed[i]);
        }
          
        return set;
    }

}
