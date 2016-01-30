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
package org.deri.infrawebs.sfs.adapter;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * XML to WSML translation class for the CarRental Service 
 * 
 * @author James Scicluna
 * 
 * Created on 18 Dec 2006 
 * Committed by $Author: maciejzaremba $ 
 *
 * $Source: /cvsroot/wsmx/components/communicationmanager/src/main/org/deri/infrawebs/sfs/adapter/XMLTranslator.java,v $, 
 * @version $Revision: 1.11 $ $Date: 2007/12/13 16:48:52 $
 * 
 **/

public class XMLTranslator {
	
	public String doTransformation(String initialXML, InputStream xsltFile){
		
		String wsmlFile = new String();
		StringWriter writer = new StringWriter();
		
		try
		{
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t1 = tf.newTransformer(new StreamSource(xsltFile));
			t1.transform(new StreamSource(new StringReader(initialXML)), new StreamResult(writer));
			//write the results of the transformation to the wsml document
			wsmlFile = writer.toString();
			
		}
		catch(Exception e)
		{System.out.println(e.getMessage());
		}
				
		return wsmlFile;
	}

}
