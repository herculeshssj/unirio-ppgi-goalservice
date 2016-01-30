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

package org.deri.wsmx.mediation.ooMediator.mapper.mappings;

import org.omwg.mediation.language.objectmodel.api.Id;
import org.wsmo.common.Identifier;

/** 
 * Interface or class description
 * 
 * @author Adrian Mocan
 *
 * Created on 08-Nov-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/mapper/mappings/MappedEntityId.java,v $, 
 * @version $Revision: 1.1 $ $Date: 2007-09-27 06:48:56 $
 */

public abstract class MappedEntityId extends Id{
    
    public abstract Identifier getIdentifier(); 
}

