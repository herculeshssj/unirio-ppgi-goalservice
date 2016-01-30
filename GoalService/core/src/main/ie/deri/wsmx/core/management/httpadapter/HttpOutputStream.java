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


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * HttpAdapter sets the basic adaptor listening for HTTP requests
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: mzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/httpadapter/HttpOutputStream.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2005-07-01 16:47:37 $
 */ 
public class HttpOutputStream extends BufferedOutputStream {
   /**
    * Answer code
    */
   protected int code;

   /**
    * whether to send the headers
    */
   protected boolean sendHeaders;

   /**
    * Headers to be sent
    */
   protected Map<String,String> headers = new HashMap<String,String>(7);

   /**
    * Creates a new HttpOutputStream with a given OutputStream and an InputStream
    *
    * @param out The OutputStream normally associated with the output socket
    *            <p/>
    *            stream of the incoming connection
    * @param in  HttpInputStream containing the incoming request
    */
   public HttpOutputStream(OutputStream out, HttpInputStream in) {
      super(out);
      code = HttpConstants.STATUS_OKAY;
      setHeader("Server", HttpConstants.SERVER_INFO);
      sendHeaders = (in.getVersion() >= 1.0);
   }


   /**
    * Sets the answer code
    *
    * @param code The new code value
    */
   public void setCode(int code) {
      this.code = code;
   }


   /**
    * Sets a given header code
    *
    * @param attr  The new header name
    * @param value The new header value
    */
   public void setHeader(String attr, String value) {
      headers.put(attr, value);
   }


   /**
    * Sends the headers
    *
    * @return Description of the Returned Value
    * @throws IOException Description of Exception
    */
   public boolean sendHeaders() throws IOException {
      if (sendHeaders) {
         StringBuffer buffer = new StringBuffer(512);
         buffer.append(HttpConstants.HTTP_VERSION);
         buffer.append(code);
         buffer.append(" ");
         buffer.append(HttpUtil.getCodeMessage(code));
         buffer.append("\r\n");
         Iterator attrs = headers.keySet().iterator();
         int size = headers.size();
         for (int i = 0; i < size; i++) {
            String attr = (String)attrs.next();
            buffer.append(attr);
            buffer.append(": ");
            buffer.append(headers.get(attr));
            buffer.append("\r\n");
         }
         buffer.append("\r\n");
         write(buffer.toString());
      }
      return sendHeaders;
   }


   /**
    * Writes a given message line
    *
    * @param msg The message to be written
    * @throws IOException
    */
   public void write(String msg) throws IOException {
      write(msg.getBytes("latin1"));
   }


   /**
    * Writes the content of the input stream to the output stream
    *
    * @param in The input stream
    * @throws IOException
    */
   public void write(InputStream in) throws IOException {
      int n;
      int length = buf.length;
      while ((n = in.read(buf, count, length - count)) >= 0) {
         if ((count += n) >= length) {
            out.write(buf, count = 0, length);
         }
      }
   }
}

