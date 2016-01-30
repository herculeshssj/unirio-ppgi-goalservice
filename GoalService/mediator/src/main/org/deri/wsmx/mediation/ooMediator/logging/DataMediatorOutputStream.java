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

package org.deri.wsmx.mediation.ooMediator.logging;

import java.io.IOException;
import java.io.OutputStream;


public class DataMediatorOutputStream {

    private OutputStream outputStream;

    public DataMediatorOutputStream(OutputStream theOutputStream) {
        this.outputStream = theOutputStream;
    }

    public void println(String theString){
        print(theString);
        println();
    }

    public void print(Object o) {
        try {
            outputStream.write(o.toString().getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void println() {
        print("\n");
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void println(Object o) {
        println(o.toString());
    }
    
    public void flush() {
    	try {
    		outputStream.flush();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}
