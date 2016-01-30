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
package ie.deri.wsmx.core.management.httpadapter;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * HttpException is emitted when an error parsing an HTTP request appears
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/httpadapter/HttpException.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.6 $ $Date: 2005-07-28 15:59:39 $
 */ 
public class HttpException extends IOException {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257006566296138039L;
    
    /**
     * Error code
     */
    protected int code;

    /**
     * Constructor for the HttpException object
     * 
     * @param code
     *            Error code
     * @param description
     *            Description
     */
    public HttpException(int code, String description) {
        super(description);
        this.code = code;
    }

    /**
     * Return the exception code
     */
    public int getCode() {
        return code;
    }

    public Document getResponseDoc() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement("HttpException");
            root.setAttribute("code", Integer.toString(code));
            root.setAttribute("description", getMessage());
            document.appendChild(root);
            return document;
        } catch (ParserConfigurationException e) {
            return null;
        }
    }
}
