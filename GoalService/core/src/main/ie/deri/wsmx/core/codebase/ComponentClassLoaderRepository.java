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

package ie.deri.wsmx.core.codebase;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

public class ComponentClassLoaderRepository {


	private Map<ObjectName, ComponentClassLoader> loaders = new HashMap<ObjectName, ComponentClassLoader>();
	
	public void addLoader(ObjectName name, ComponentClassLoader loader) {
		loaders.put(name, loader);
	}

	public void removeLoader(ObjectName name) {
		loaders.remove(name);
	}
	
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		for (ClassLoader loader : loaders.values()) {
			try {
				return loader.loadClass(name);
			} catch (ClassNotFoundException cnfe) {
				//continue
			}
		}
		throw new ClassNotFoundException(name);
	}


	public URL getResource(String name) {
		URL resource = null;
		for (ClassLoader loader : loaders.values()) {
				resource = loader.getResource(name);
				if (resource != null)
					return resource;
		}
		return null;
	}
		
	public InputStream getResourceAsStream(String name) {
		InputStream resource = null;
		for (ClassLoader loader : loaders.values()) {
				resource = loader.getResourceAsStream(name);
				if (resource != null)
					return resource;
		}
		return null;
	}

}
