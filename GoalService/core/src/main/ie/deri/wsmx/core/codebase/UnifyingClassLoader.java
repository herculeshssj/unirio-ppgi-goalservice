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

import javax.management.loading.ClassLoaderRepository;

public class UnifyingClassLoader extends ClassLoader {

	private ClassLoader l;
	private ClassLoaderRepository repository = null;
	private ComponentClassLoaderRepository ccr = new ComponentClassLoaderRepository();

	public UnifyingClassLoader(ClassLoader parent) {
		super(parent);
		this.repository = null;
		l = parent;
	}

	public UnifyingClassLoader(ClassLoaderRepository repository) {
		super();
		this.repository = repository;
	}

	public UnifyingClassLoader(ClassLoader parent, ClassLoaderRepository repository) {
		super(parent);
		this.repository = repository;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return ccr.findClass(name);
		} catch (Throwable t) {
			return l.loadClass(name);
		}
	}
	
/*
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return repository.loadClass(name);
		} catch (ClassNotFoundException cnfe) {
			return l.loadClass(name, resolve);
		} catch (NullPointerException npe) {
			return l.loadClass(name, resolve);			
		}
	}
*/
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			return ccr.findClass(name);
		} catch (Throwable t) {
			return l.loadClass(name);

		}
	}

	public ClassLoaderRepository getRepository() {
		return repository;
	}

	public void setRepository(ClassLoaderRepository repository) {
		this.repository = repository;
	}

	public ComponentClassLoaderRepository getComponentClassLoaderRepository() {
		return ccr;
	}

}
