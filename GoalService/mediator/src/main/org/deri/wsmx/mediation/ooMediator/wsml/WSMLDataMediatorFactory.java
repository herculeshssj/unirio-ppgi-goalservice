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

package org.deri.wsmx.mediation.ooMediator.wsml;


import java.util.HashMap;
import java.util.Map;

import org.deri.wsmx.mediation.ooMediator.Mediator;
import org.deri.wsmx.mediation.ooMediator.storage.Loader;
import org.wsmo.execution.common.component.DataMediator;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 15-May-2005
 * Committed by $Author: adrian.mocan $
 * 
 * $Source: /var/repository/wsmx-datamediator/src/main/java/org/deri/wsmx/mediation/ooMediator/wsml/WSMLDataMediatorFactory.java,v $, 
 * @version $Revision: 1.4 $ $Date: 2008/02/26 03:20:27 $
 */

public abstract class WSMLDataMediatorFactory {

    public static DataMediator createDataMediator(Loader loader){
        Map <String, Boolean> flags = new HashMap <String, Boolean> ();
        flags.put(MediationFlags.PRINT_TIMING_INFORMATION_TO_CONSOLE, true);
        flags.put(MediationFlags.PRINT_QUERIES_TO_CONSOLE, true);
        flags.put(MediationFlags.WRITE_MERGED_ONTOLOGY_TO_FILE, true);
        return new WSMLDataMediator(loader, flags);
    }
    
	public static DataMediator createDataMediator(Loader loader, Map <String, Boolean> flags){
        return new WSMLDataMediator(loader, flags);
	}
	
	/** 
	 * @deprecated use #createDataMediator() instead
	 * @see #createDataMediator()	 
	 * */		
	public static Mediator createMediator(Loader loader){
        Map <String, Boolean> flags = new HashMap <String, Boolean> ();
        flags.put(MediationFlags.PRINT_TIMING_INFORMATION_TO_CONSOLE, true);
        flags.put(MediationFlags.PRINT_QUERIES_TO_CONSOLE, true);
        return new WSMLDataMediator(loader, flags);
	}
}