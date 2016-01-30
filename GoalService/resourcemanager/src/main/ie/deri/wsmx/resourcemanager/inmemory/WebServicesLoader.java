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
package ie.deri.wsmx.resourcemanager.inmemory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.omwg.ontology.Ontology;
import org.wsmo.common.Entity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.execution.common.exception.ComponentException;
import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.factory.Factory;
import org.wsmo.factory.LogicalExpressionFactory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.service.Goal;
import org.wsmo.service.WebService;
import org.wsmo.wsml.ParserException;

public class WebServicesLoader {

    private static String[] files = new String[]{"/ie/deri/wsmx/resourcemanager/inmemory/WebService1.wsml",
                                                 "/ie/deri/wsmx/resourcemanager/inmemory/WebService2.wsml"};
    
    private static WebService webService;
    
    private InputStream input;
    private InMemoryRM rm;
    
    public ArrayList getWebServices() throws Exception {
        ArrayList<WebService> services = new ArrayList<WebService>();
        try {
            for (int i=0; i < files.length; i++) {
                webService = (WebService)load(files[i]);
                services.add(webService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return services;
    }
    
    protected Entity load(String file) throws Exception {
        try {
            input = getClass().getClassLoader().getResourceAsStream(file);
          
            if (input == null) {
                throw new Exception("Property file " + file + " not found in classpath");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(file)));
        
        String doc = "";
        String subString = "";
        while((subString = reader.readLine())!=null)
            doc+=subString +"\n";
        
        Set<Entity> entities = parse(new WSMLDocument(doc));
        Iterator iterator = entities.iterator();
        Entity entity = null;
        
        while (iterator.hasNext()) {
            entity = (Entity)(iterator.next());
        }
        return entity;
    }
    
    private Set<Entity> parse(WSMLDocument wsmlDocument) throws ComponentException, UnsupportedOperationException {
        
        org.wsmo.wsml.Parser parser = Factory.createParser(new HashMap <String, Object> ());
        
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
