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

package org.deri.wsmx.mediation.ooMediator.gui;

/** 
 * This is replacement of the design-time component workbench. Its role is to adapt the specific 
 * calls for the design time worbench with default placeholders. 
 * 
 * @author Adrian Mocan
 *
 * Created on 08-Jun-2005
 * Committed by $Author: adrianmocan $
 * 
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/mediator/src/main/org/deri/wsmx/mediation/ooMediator/gui/WorkBench.java,v $, 
 * @version $Revision: 1.1 $ $Date: 2007-09-27 06:49:03 $
 */

public abstract class WorkBench {

	public static final int PART_OF_HIERARCHY = 0;
	public static final int INSTANCE_OF_HIERARCHY = 1;
	
	public static int sourceHierarchy = PART_OF_HIERARCHY;
	public static int targetHierarchy = PART_OF_HIERARCHY;

	public static void writeMessage(String string) {
		//System.out.println(string);		
	}

}

