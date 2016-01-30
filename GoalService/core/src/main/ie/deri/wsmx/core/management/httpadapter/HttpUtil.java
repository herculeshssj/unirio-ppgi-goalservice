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
 * Utility methods for the HTTP adaptor
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: mzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/httpadapter/HttpUtil.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2005-07-01 16:47:37 $
 */ 
public class HttpUtil {
    /**
     * Gets a message apropriate for a give HTTP code
     * 
     * @param code
     *            Reference Code
     * @return The result message
     * @see HttpConstants
     */
    public static String getCodeMessage(int code) {
        switch (code) {
        case HttpConstants.STATUS_OKAY:
            return "OK";
        case HttpConstants.STATUS_NO_CONTENT:
            return "No Content";
        case HttpConstants.STATUS_MOVED_PERMANENTLY:
            return "Moved Permanently";
        case HttpConstants.STATUS_MOVED_TEMPORARILY:
            return "Moved Temporarily";
        case HttpConstants.STATUS_BAD_REQUEST:
            return "Bad Request";
        case HttpConstants.STATUS_FORBIDDEN:
            return "Forbidden";
        case HttpConstants.STATUS_NOT_FOUND:
            return "Not Found";
        case HttpConstants.STATUS_NOT_ALLOWED:
            return "Method Not Allowed";
        case HttpConstants.STATUS_INTERNAL_ERROR:
            return "Internal Server Error";
        case HttpConstants.STATUS_AUTHENTICATE:
            return "Authentication requested";
        case HttpConstants.STATUS_NOT_IMPLEMENTED:
            return "Not Implemented";
        default:
            return "Unknown Code (" + code + ")";
        }
    }
    
    /**
     * Makes a path canonical
     * 
     * @param path
     *            Target path
     * @return The canonicalized path
     */
    public static String canonicalizePath(String path) {
        char[] chars = path.toCharArray();
        int length = chars.length;
        int idx;
        int odx = 0;
        while ((idx = indexOf(chars, length, '/', odx)) < length - 1) {
            int ndx = indexOf(chars, length, '/', idx + 1);
            int kill = -1;
            if (ndx == idx + 1) {
                kill = 1;
            } else if ((ndx >= idx + 2) && (chars[idx + 1] == '.')) {
                if (ndx == idx + 2) {
                    kill = 2;
                } else if ((ndx == idx + 3) && (chars[idx + 2] == '.')) {
                    kill = 3;
                    while ((idx > 0) && (chars[--idx] != '/')) {
                        ++kill;
                    }
                }
            }
            if (kill == -1) {
                odx = ndx;
            } else if (idx + kill >= length) {
                length = odx = idx + 1;
            } else {
                length -= kill;
                System.arraycopy(chars, idx + 1 + kill, chars, idx + 1, length
                        - idx - 1);
                odx = idx;
            }
        }
        return new String(chars, 0, length);
    }
    
    protected static int indexOf(char[] chars, int length, char chr, int from) {
        while ((from < length) && (chars[from] != chr)) {
            ++from;
        }
        return from;
    }
    
    /**
     * Returns whether a boolean variable is in the variables. It tries to find
     * it. If not found the the default is used. If found is tested to check if
     * it is <code>true</code> or <code>1</code> and the answer is true.
     * Otherwise is false
     */
    public static boolean booleanVariableValue(HttpInputStream in,
            String variable, boolean defaultValue) {
        if (in.getVariables().containsKey(variable)) {
            String result = (String) in.getVariables().get(variable);
            return result.equals("true") || result.equals("1");
        }
        return defaultValue;
    }
}
