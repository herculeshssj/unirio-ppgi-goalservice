/* 
 * QoS Discovery Component
 * Copyright (C) 2006 Sebastian Gerlach
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package ch.epfl.qosdisc.ontologies;

/**
 * Creates a set of fake interrelated ontologies describing web services.
 * 
 * @author sgerlach
 *
 */
public class OntologyCreator {

    /**
     * Output directory for the generated ontologies
     */
    static public final String ROOT_DIRECTORY = "c:/data/LSIR/ontologies/";
    
    /**
     * Root URL for recovering the ontologies in a web context
     */
    static public final String ROOT_URL = "http://localhost/ontologies/";
    
    /**
     * Generate a random integer from 0 to range-1
     * 
     * @param range Range for the generated integers
     * @return A random integer from 0 to range-1
     */
    static int randInt(int range) {
        return ((int)(Math.random()*65536))%range;
    }
    
    /**
     * Create a batch of fake interrelated ontologies
     * 
     * @param args
     */
    public static void main(String[] args) {
        int co = 1;
        Ontology o = new Ontology(co++);
        
        // Create 1000 concepts
        Concept[] cc = new Concept[1000];
        for(int i = 0; i<1000; ++i) {
            if(i%100 == 99) {
                o.writeFile();
                o = new Ontology(co++);
            }
            if(i<15 || Math.random()>0.96)
                cc[i] = new Concept(i+1,null,o);
            else
                cc[i] = new Concept(i+1,cc[randInt(i-1)],o);
        }
        
        // Create 100 web services
        WebService ws;
        for(int i = 1; i<100; ++i) {
            ws = new WebService(i);
            do {
                ws.newInterface();
                for(int j = 0; j< 5; ++j)
                    ws.addConcept(cc[randInt(100)]);
            }
            while(Math.random()>0.9);
            ws.writeFile();
        }
        o.writeFile();
    }
}
