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

package ch.epfl.qosdisc.codims;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.types.Type;


/**
 * This is a generic CoDIMS datatype. This is a hack. It is no longer used.
 * 
 * @author Sebastian Gerlach
 *
 * @param <Content> The content of the datatype.
 */
public class PowerType<Content> implements Type, Serializable {
	
	/**
	 * Just to keep the compiler happy.
	 */
	private static final long serialVersionUID = -1077319007154687298L;
	
	
	private Content content;
	
	public PowerType(Content content) {
		
		this.content = content;
	}
	
	public Content getContent() {
		return content;
	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.types.Type#display(java.io.Writer)
	 */
	public void display(Writer out) throws IOException {

	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.types.Type#displayWidth()
	 */
	public int displayWidth() {

		return 10;
	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.types.Type#newInstance()
	 */
	public Type newInstance() {

		return new PowerType<Content>(content);
	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.types.Type#read(java.io.DataInputStream)
	 */
	public Type read(DataInputStream in) throws IOException {

		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.types.Type#recognitionPattern()
	 */
	public String recognitionPattern() {

		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.types.Type#setMetadata(ch.epfl.codimsd.qeef.Data)
	 */
	public void setMetadata(Data data) {

		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.types.Type#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {

		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.types.Type#setValue(java.lang.String)
	 */
	public void setValue(String value) {

		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.epfl.codimsd.qeef.types.Type#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream out) throws IOException {

		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object arg0) {

		// TODO Auto-generated method stub
		return 0;
	}
	
	public Object clone() {
	
		return new PowerType<Content>(content);
	}

    public void finalize() throws Throwable {
    	
    	super.finalize();
    	content = null;
    }

}