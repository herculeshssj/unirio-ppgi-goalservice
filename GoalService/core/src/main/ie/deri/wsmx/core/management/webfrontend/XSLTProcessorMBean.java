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

import java.util.Locale;

/**
 * Management interface for the XSLTProcessor MBean.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: mzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/webfrontend/XSLTProcessorMBean.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.5 $ $Date: 2005-07-01 16:48:14 $
 */ 
public interface XSLTProcessorMBean extends ProcessorMBean {
    /**
     * Sets the jar/zip file or the directory where to find the XSL files
     * 
     * @see #getFile
     */
    public void setFile(String file);

    /**
     * Returns the jar/zip file or the directory where XSL files are loaded
     * 
     * @see #setFile
     */
    public String getFile();

    /**
     * Returns the path of the XSL templates inside a jar file.
     * 
     * @see #setPathInJar
     */
    public String getPathInJar();

    /**
     * Specifies the path of the XSL templates inside a jar file.
     * 
     * @see #getPathInJar
     */
    public void setPathInJar(String path);

    /**
     * Returns the default start page
     * 
     * @see #setDefaultPage
     */
    public String getDefaultPage();

    /**
     * Sets the default start page, serverbydomain as a default
     * 
     * @see #getDefaultPage
     */
    public void setDefaultPage(String defaultPage);

    /**
     * Returns if the XSL files are contained in a jar/zip file.
     * 
     * @see #isUsePath
     * @see #setFile
     */
    boolean isUseJar();

    /**
     * Returns if the XSL files are contained in a path.
     * 
     * @see #isUseJar
     * @see #setFile
     */
    boolean isUsePath();

    /**
     * Maps a given extension with a specified MIME type
     */
    public void addMimeType(String extension, String type);

    /**
     * Sets the caching of the XSL Templates.
     */
    public void setUseCache(boolean useCache);

    /**
     * Returns if the XSL Templates are cached
     */
    boolean isUseCache();

    /**
     * Returns the Locale used to internationalize the output
     */
    public Locale getLocale();

    /**
     * Sets the locale used to internationalize the output
     */
    public void setLocale(Locale locale);

    /**
     * Sets the locale used to internationalize the output, as a string
     */
    public void setLocaleString(String locale);
}
