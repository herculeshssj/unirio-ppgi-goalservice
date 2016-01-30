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

package ie.deri.wsmx.commons;


public class Pair<T1, T2> {

    private T1 first;
    private T2 second;

    public Pair(T1 theFirst, T2 theSecond) {
        this.first = theFirst;
        this.second = theSecond;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }
    
    @SuppressWarnings("unchecked")
	public boolean equals(Object theObject){
        if (!(theObject instanceof Pair)){
            return false;
        }
        Pair otherPair = (Pair) theObject;
        boolean firstEqual = (otherPair.first == null) ? first == null : otherPair.first.equals(first);
        boolean secondEqual = (otherPair.second == null) ? second == null : otherPair.second.equals(second);
        return firstEqual && secondEqual;
    }

    public int hashCode() {
        return first.hashCode() ^ second.hashCode();
    }
    
   
}
