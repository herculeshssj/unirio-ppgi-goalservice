/*
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.deri.wsmx.mediation.ooMediator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omwg.ontology.Concept;
import org.omwg.ontology.DataValue;
import org.omwg.ontology.Instance;
import org.omwg.ontology.Value;
import org.wsmo.common.Identifier;


public class InstanceCompare {

	private static Set <Instance> theExpanded = null;
	
    public static boolean compare(Set <Instance> theExpectedInstances, Set <Instance> theResultInstances){
    	
    	List <Instance> expectedInstances = null;
    	List <Instance> resultInstances = null;    	
    	if (theExpectedInstances.size()==theResultInstances.size()){
    		expectedInstances = new ArrayList <Instance> (theExpectedInstances);
    		resultInstances = new ArrayList <Instance> (theResultInstances);
    	}
    	else{
    		theExpanded = new HashSet <Instance> ();
    		expectedInstances = expand(theExpectedInstances);
    		theExpanded = new HashSet <Instance> ();
    		resultInstances = expand(theResultInstances);
    	}
        if (expectedInstances.size() != resultInstances.size()){
            System.out.println("**Number of expected Instances not equal to number of result instances**");
            return false;
        }
        
        List <ComparisonPair> comparisons = new ArrayList <ComparisonPair> ();
        for (Iterator <Instance> i = expectedInstances.iterator(); i.hasNext();){
            Instance expected = i.next();
            int num = resultInstances.size();
            for (Iterator <Instance> j = resultInstances.iterator(); j.hasNext();){
                Instance result = j.next();
                if (compare(expected, result, comparisons)){
                    i.remove();
                    j.remove();
                    break;
                }
            }
            if (num == resultInstances.size()){
                System.out.println("**No matching result for expected " + expected.getIdentifier() + "**");
            }
        }
        if (expectedInstances.size() == 0){
            return true;
        }
        return false;
    }

    private static List<Instance> expand(Set<Instance> theInstances) {
        List <Instance> result = new ArrayList <Instance> ();
        for (Instance instance : theInstances){
            result.addAll(expand(instance));
        }
        
        Collections.sort(result, new Comparator <Instance>() {
            public int compare(Instance o1, Instance o2) {
                return o1.getIdentifier().toString().compareTo(o2.getIdentifier().toString()) * -1;
            }
        });
        return result;
        //return new ArrayList <Instance> (theInstances);
    }
    
    private static Set<Instance> expand (Instance theInstance){
        Set <Instance> result = new HashSet <Instance> ();
        if (!theExpanded.contains(theInstance)){
        	result.add(theInstance);
        	theExpanded.add(theInstance);
        }
        for (Set <Value> values : theInstance.listAttributeValues().values()){
            for (Value value : values){
                if (value instanceof Instance && !theExpanded.contains(value)){
                    result.addAll(expand((Instance) value));
                }
            }
        }
        return result;
    }

    private static boolean compare(Instance theInstance1, Instance theInstance2, List <ComparisonPair> theComparisons) {
        ComparisonPair cp = new InstanceCompare().new ComparisonPair(theInstance1, theInstance2);
        
        int index = theComparisons.indexOf(cp);
        if (index != -1){
            return theComparisons.get(index).matches();
        }
        theComparisons.add(cp);
        
        if (!memberOfSameConcepts(theInstance1, theInstance2)){
            return false;
        }
        
        Map <Identifier, Set <Value>> i1Values = theInstance1.listAttributeValues();
        Map <Identifier, Set <Value>> i2Values = theInstance2.listAttributeValues();
        
        if (i1Values.keySet().size() != i2Values.keySet().size()){
//            System.out.println("** " + theInstance2.getIdentifier() + " and "+ theInstance2.getIdentifier() + " do not have same attributes (Unequal numbers)**");
            return false;
        }        
        
        for (Iterator <Identifier> i = i1Values.keySet().iterator(); i.hasNext();){
            Identifier a1 = i.next();
            
            int num = i2Values.keySet().size();
            for (Iterator <Identifier> j = i2Values.keySet().iterator(); j.hasNext();){
                Identifier a2 = j.next();
                if (a1.equals(a2)){
                    
                    List <Value> a1Values = new ArrayList <Value> (i1Values.get(a1));
                    List <Value> a2Values = new ArrayList <Value> (i2Values.get(a2));
                    for (Iterator <Value> k = a1Values.iterator(); k.hasNext();){
                        Value v1 = k.next();
                        
                        int num2 = a2Values.size();
                        for (Iterator <Value> l = a2Values.iterator(); l.hasNext();){
                            Value v2 = l.next();
                            boolean same = false;
                            if (v1 instanceof Instance && v2 instanceof Instance){
                                same = compare((Instance) v1, (Instance) v2, theComparisons);
                            }
                            else if (v1 instanceof DataValue && v2 instanceof DataValue){
                                same = compare((DataValue) v1, (DataValue) v2);
                            }
                            
                            if (same){
                               
                                k.remove();
                                l.remove();
                                break;
                            }
                        }
                        
                        if (num2 == a2Values.size()){
                            String value = v1.toString();
                            if (v1 instanceof Instance){
                                value = ((Instance) v1).getIdentifier().toString();
                            }
//                            System.out.println("** " + theInstance2.getIdentifier() + " does not have expected attribute value '" + value + "' for attribute " + a1 + " **");
                            return false;
                        }
                    }
                    
                    i.remove();
                    j.remove();
                    break;
                }
            }
            
            if (num == i2Values.keySet().size()){
//                System.out.println("** " + theInstance2.getIdentifier() + " does not have expected attribute " + a1 + " **");
                return false;
            }
        }
        
        if (i1Values.size() == 0 && i2Values.size() == 0){
//            System.out.println("'" + theInstance1.getIdentifier() + "' == '" + theInstance2.getIdentifier() + "'");
            cp.setMatches(true);
            return true;
        }
        return false;
    }

    private static boolean compare(DataValue value1, DataValue value2) {
        if (value1.equals(value2)){
            return true;
        }
//        System.out.println("**Datavalue '" +  value1 + "' != '" + value2 + "'**");
        return false;
    }

    private static boolean memberOfSameConcepts(Instance theInstance1, Instance theInstance2) {
        List <Concept> i1Concepts = new ArrayList <Concept> (theInstance1.listConcepts());
        List <Concept> i2Concepts = new ArrayList <Concept> (theInstance2.listConcepts());
        
        if (i1Concepts.size()!= i2Concepts.size()){
//            System.out.println("** " + theInstance2.getIdentifier() + " and "+ theInstance2.getIdentifier() + " are not member of the same concepts (Unequal numbers)**");
            return false;
        }
        
        for (Iterator <Concept> i = i1Concepts.iterator(); i.hasNext();){
            Concept c1 = i.next();
            int num = i2Concepts.size();
            for (Iterator <Concept> j = i2Concepts.iterator(); j.hasNext();){
                Concept c2 = j.next();
                if (c1.getIdentifier().equals(c2.getIdentifier())){
                    i.remove();
                    j.remove();
                    break;
                }
            }
            
            if (num == i2Concepts.size()){
//                System.out.println("** " + theInstance2.getIdentifier() + " not member of the expected concept " + c1.getIdentifier() + " **");
                return false;
            }
        }
        
        if (i1Concepts.size() == 0 && i2Concepts.size() == 0){
            return true;
        }
        return false;
    }
    
    public class ComparisonPair {
        protected Instance instance1;
        protected Instance instance2;
        protected boolean matches = false;

        public ComparisonPair (Instance theInstance1, Instance theInstance2){
            this.instance1 = theInstance1;
            this.instance2 = theInstance2;
        }
        
        public void setMatches(boolean theMatches) {
            this.matches = theMatches;
        }

        public boolean matches() {
            return matches;
        }

        public boolean equals(Object theObject){
            if (!(theObject instanceof ComparisonPair)){
                return false;
            }
            return instance1.getIdentifier().equals(((ComparisonPair) theObject).instance1.getIdentifier()) &&
                   instance2.getIdentifier().equals(((ComparisonPair) theObject).instance2.getIdentifier());
        }

    }
}
