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

package ie.deri.wsmx.core.configuration;

import ie.deri.wsmx.exceptions.WSMXConfigurationException;
import ie.deri.wsmx.exceptions.WSMXException;


/**
 * Anything a configuration can be loaded from and stored to.
 * Examples include, but are not limited to XML files, MOF files,
 * databases.
 *
 * <pre>
 * Created on Feb 11, 2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/configuration/ConfigurationRepository.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.6 $ $Date: 2005-09-21 16:30:34 $
 */ 
public interface ConfigurationRepository {

	/**
	 * Loads the Configuration and returns it.
	 * 
	 * @return KernelConfiguration in-memory representation of the configuration
	 * @throws WSMXConfigurationException if loading or parsing fails
	 * @throws WSMXException
	 */
    public KernelConfiguration load() throws WSMXConfigurationException;
	
}






