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
package ie.deri.wsmx.adapter;


import org.wsmo.execution.common.nonwsmo.WSMLDocument;
import org.wsmo.execution.common.nonwsmo.grounding.EndpointGrounding;

/**
 * An <b>Adapter Skeleton</b>. <br>All adapters must extend this class.
 *
 * @author Brahmananda Sapkota
 *
 * Created on Feb 11, 2005
 * Committed by $Author: maciejzaremba $
 *
 * $Source: /cvsroot/wsmx/components/communicationmanager/src/main/ie/deri/wsmx/adapter/Adapter.java,v $,
 * @version $Revision: 1.12 $ $Date: 2007/12/13 16:48:52 $
 */ 

public abstract class Adapter {
	
	private String id;
	
	public Adapter () {	
		
	}
	
	public Adapter(String id) {
		this.id = id;
	}
	
	public String getAdapterID() {
		return this.id;
	}
	
	public void setAdapterID(String id) {
		this.id = id;
	}
	
	public abstract WSMLDocument getWSML(String document, EndpointGrounding endpoint);
}
