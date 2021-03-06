/*
 * Copyright (c) 2006 University of Innsbruck, Austria
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

package ie.deri.wsmx.orchestration;

import ie.deri.wsmx.orchestration.asm.State;
import ie.deri.wsmx.orchestration.asm.StateModificationListener;

import org.apache.log4j.Logger;
import org.omwg.logicalexpression.AttributeMolecule;
import org.omwg.logicalexpression.MembershipMolecule;

/**
 * Keeps a history of state modification of the state it is subscribed to.
 *
 * <pre>
 * Created on Dec 24, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/orchestration/src/main/ie/deri/wsmx/orchestration/ModificationHistory.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.3 $ $Date: 2006-12-12 10:52:44 $
 */ 
public class ModificationHistory implements StateModificationListener {

	String history = "";

	protected static Logger logger = Logger.getLogger(ModificationHistory.class);

	public ModificationHistory() {
		super();
	}
	
	public void addedAttribute(State state, AttributeMolecule attribute) {
		history = history + attribute.toString() + "\n";
	}

	public void addedMembership(State state, MembershipMolecule membership) {
		history = history + membership.toString() + "\n";		
	}

	public String getHistory() {
		return new String(history);
	}

	public void resetHistory() {
		this.history = "";
	}

}
