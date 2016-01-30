/*
 * Copyright (c) 2008 National University of Ireland, Galway
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

package org.ipsuper.prereview;

import java.util.Random;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

/** Simple JAX-WS Web service generating random URLs. 
 *  @author Maciej Zaremba
 *
 * Created on 9 Jan 2007
 * Committed by $Author: maciejzaremba $
 */

@WebService(name = "Packager", targetNamespace = "http://ip-super.org/usecase/prereview/")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
public class PackagerWebService {

	static private Random random = new Random();
	
	String[] urls = {"http://youtube.com/watch?v=DuiSNf0rQjI",
					 "http://youtube.com/watch?v=BmCyxzSAQrM",
					 "http://youtube.com/watch?v=3me8kvmEA7w"};

	@WebMethod
	public String generateURL(String userID, String contentID){
		return urls[random.nextInt(urls.length)];
	}
}
 