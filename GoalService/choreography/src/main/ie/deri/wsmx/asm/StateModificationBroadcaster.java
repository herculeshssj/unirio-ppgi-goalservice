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

package ie.deri.wsmx.asm;

import java.util.List;

/**
 * Entities that want to inform interested
 * listeners of statemodification should
 * implement this interface.
 *
 * <pre>
 * Created on Dec 24, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/choreography/src/main/ie/deri/wsmx/asm/StateModificationBroadcaster.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 *
 * @version $Revision: 1.4 $ $Date: 2006-09-01 02:13:24 $
 */ 
public interface StateModificationBroadcaster {

	public abstract void registerListener(StateModificationListener listener);

	public abstract void registerListeners(
			List<StateModificationListener> listeners);

}