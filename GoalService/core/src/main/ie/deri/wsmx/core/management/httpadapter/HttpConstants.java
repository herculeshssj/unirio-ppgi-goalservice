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

/**
 * Define constants for the HTTP request processing
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: mzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/httpadapter/HttpConstants.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2005-07-01 16:47:37 $
 */ 
public class HttpConstants {
   /**
    * Server info header
    */
   public final static String SERVER_INFO = "WSMX-HTTPD/1.0";

   /**
    * HTTP implemented version
    */
   public final static String HTTP_VERSION = "HTTP/1.0 ";

   /**
    * Get method header
    */
   public final static String METHOD_GET = "GET";

   /**
    * Post method header
    */
   public final static String METHOD_POST = "POST";

   /**
    * Status code OK
    */
   public final static int STATUS_OKAY = 200;

   /**
    * Status code NO CONTENT
    */
   public final static int STATUS_NO_CONTENT = 204;

   /**
    * Status code MOVED PERMANENTLY
    */
   public final static int STATUS_MOVED_PERMANENTLY = 301;

   /**
    * Status code MOVED TEMPORARILY
    */
   public final static int STATUS_MOVED_TEMPORARILY = 302;

   /**
    * Status code BAD REQUEST
    */
   public final static int STATUS_BAD_REQUEST = 400;

   /**
    * Status code AUTHENTICATE
    */
   public final static int STATUS_AUTHENTICATE = 401;

   /**
    * Status code FORBIDDEN
    */
   public final static int STATUS_FORBIDDEN = 403;

   /**
    * Status code NOT FOUND
    */
   public final static int STATUS_NOT_FOUND = 404;

   /**
    * Status code NOT ALLOWED
    */
   public final static int STATUS_NOT_ALLOWED = 405;

   /**
    * Status code INTERNAL ERROR
    */
   public final static int STATUS_INTERNAL_ERROR = 500;

   /**
    * Status code NOT IMPLEMENTED
    */
   public final static int STATUS_NOT_IMPLEMENTED = 501;
}
