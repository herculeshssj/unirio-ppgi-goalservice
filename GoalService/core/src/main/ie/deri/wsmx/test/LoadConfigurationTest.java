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

package ie.deri.wsmx.test;

import ie.deri.wsmx.core.WSMXKernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;



/** * Interface or class description
 * * @author Maciej Zaremba
 *
 * Created on 11 Dec 2006
 * Committed by $Author: maciejzaremba $
 * * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/test/LoadConfigurationTest.java,v $, * @version $Revision: 1.1 $ $Date: 2007-01-08 15:38:41 $
 */
public class LoadConfigurationTest {
	
	public static final String PROPERTIES_CONFIGURATION_NAME = "config.properties";
	public static final File PROPERTIES_CONFIG_LOCATION = new File("core/etc/" + PROPERTIES_CONFIGURATION_NAME);
	
	public static void main(String[] args){
		Properties config = new Properties();
		InputStream stream;
		try {
			stream = new FileInputStream(PROPERTIES_CONFIG_LOCATION);
			config.load(stream);
			stream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String ontosStr = config.getProperty("wsmx.resourcemanager.ontologies");
		if (ontosStr!=null) {
			ArrayList<String> dirs = getDirectories(ontosStr);
			System.out.println(dirs.size());
		}

		String webservicesStr = config.getProperty("wsmx.resourcemanager.webservices");
		if (webservicesStr!=null) {
			ArrayList<String> dirs = getDirectories(webservicesStr);
			System.out.println(dirs.size());
		}

		String goalsStr = config.getProperty("wsmx.resourcemanager.goals");
		if (goalsStr!=null) {
			ArrayList<String> dirs = getDirectories(goalsStr);
			System.out.println(dirs.size());
		}
	}
	
    /* Tokenizes String according to the following convention:
     * "dir1"; "dir2"; ...  
    */
    static ArrayList getDirectories(String propertyString){
		ArrayList<String> dirs = new ArrayList<String>();
    	StringTokenizer st = new StringTokenizer(propertyString,";");
		while (st.hasMoreElements()){
			String dir = st.nextToken();
			if (dir == null || dir.equals("") || dir.indexOf("\"") == -1) 
				continue;
			dir = dir.substring(dir.indexOf("\"")+1);
			if (dir == null || dir.equals("") || dir.indexOf("\"") == -1)
				continue;
			dir = dir.substring(0, dir.indexOf("\""));
			dirs.add(dir);
		}
		return dirs; 
    }

}
 