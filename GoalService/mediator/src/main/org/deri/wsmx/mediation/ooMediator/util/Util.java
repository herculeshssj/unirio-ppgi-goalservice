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

package org.deri.wsmx.mediation.ooMediator.util;

import java.util.Iterator;
import java.util.Set;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 03-Nov-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/util/Util.java,v $, 
 * @version $Revision: 1.2 $ $Date: 2007-12-12 12:52:26 $
 */

public class Util {

    public static <E> Set<E> addAllIfNotExist(Set<E> original,  Set<E> external){
        Iterator<E> it = external.iterator();
        while(it.hasNext()){
            E crtE = it.next();
            if (!original.contains(crtE))
                original.add(crtE);
        }
        return original;
    }
    
    public static <E> Set<E> addAllIfNotExists(Set<E> original, E element){
        if (!original.contains(element))
            original.add(element);
        return original;        
    }
    
}

