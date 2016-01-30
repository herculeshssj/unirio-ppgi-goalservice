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

package ie.deri.wsmx.core.management.webfrontend;

import ie.deri.wsmx.core.management.httpadapter.HttpException;
import ie.deri.wsmx.core.management.httpadapter.HttpInputStream;
import ie.deri.wsmx.core.management.httpadapter.HttpOutputStream;

import java.io.IOException;

import org.w3c.dom.Document;

/**
 * Processor ManagementBean, defines a generic description.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: mzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/webfrontend/ProcessorMBean.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2005-07-01 16:48:14 $
 */ 
public interface ProcessorMBean {
    public String getName();

    /**
     * The method will process the result string and produce an output. The
     * implementor is also responsible to set the mime type, response code and
     * send the headers before answering as follow: <code>
     * out.setCode(HttpConstants.STATUS_OKAY);
     * out.setHeader("Content-type", "text/html");
     * out.sendHeaders();
     * out.write("some text");
     * </code>
     * 
     * @param out
     *            The output stream
     * @param in
     *            The input stream
     * @param document
     *            A document containing the data
     */
    public void writeResponse(HttpOutputStream out, HttpInputStream in,
            Document document) throws IOException;

    /**
     * The method will process the result exception and produce output. The
     * implementor is also responsible to set the mime type, response code and
     * send the headers before answering as follow: <code>
     * out.setCode(HttpConstants.STATUS_OKAY);
     * out.setHeader("Content-type", "text/html");
     * out.sendHeaders();
     * out.write("some text");
     * </code>
     * 
     * @param out
     *            The output stream
     * @param in
     *            The input stream
     * @param e
     *            The exception to be reported
     */
    public void writeError(HttpOutputStream out, HttpInputStream in, Exception e)
            throws IOException;

    /**
     * Preprocess a path and return a replacement path. For instance the / path
     * could be replaced by the server path
     * 
     * @param path
     *            The original path
     * @return the replacement path. If not modification the path param should
     *         be returned
     */
    public String preProcess(String path);

    /**
     * Let the processor load internally a not found element. This can be used
     * to load images, stylesheets and so on. If return is not null, the path is
     * processed
     * 
     * @param path
     *            The request element
     * @param out
     *            The output stream
     * @param in
     *            The input stream
     */
    public String notFoundElement(String path, HttpOutputStream out,
            HttpInputStream in) throws IOException, HttpException;
}
