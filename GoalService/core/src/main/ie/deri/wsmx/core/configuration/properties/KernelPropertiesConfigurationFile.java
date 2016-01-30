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

package ie.deri.wsmx.core.configuration.properties;

import ie.deri.wsmx.core.WSMXKernel;
import ie.deri.wsmx.core.configuration.ConfigurationRepository;
import ie.deri.wsmx.core.configuration.KernelConfiguration;
import ie.deri.wsmx.exceptions.WSMXConfigurationException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Properties;

import net.jini.core.discovery.LookupLocator;

import org.apache.log4j.Logger;

public class KernelPropertiesConfigurationFile implements
        ConfigurationRepository {

    private static final String KEY_SPACEADDRESS = "wsmx.spaceaddress";
	private static final String KEY_HTTPPORT = "wsmx.httpport";
	private static final String KEY_AXISPORT = "wsmx.axisport";
	private static final String KEY_SSHPORT = "wsmx.sshport";
	private static final String KEY_SYSTEMCODEBASE = "wsmx.systemcodebase";
	static Logger logger = Logger.getLogger(KernelPropertiesConfigurationFile.class);
    Properties instance = null;
    
    public KernelPropertiesConfigurationFile(Properties instance) throws WSMXConfigurationException {
        super();
        if (instance == null)
            throw new WSMXConfigurationException("Properties configuration instance may not be null.");
        this.instance = instance;
    }

    public KernelConfiguration load() throws WSMXConfigurationException {
        if (instance == null)
            throw new WSMXConfigurationException("Properties configuration instance may not be null.");
        int httpport = 0, axisport = 0, sshport = 0;
        try {
            httpport = Integer.parseInt(instance.getProperty(KEY_HTTPPORT, Integer.toString(WSMXKernel.DEFAULT_HTTP_PORT)));
        } catch (NumberFormatException nfe) {
            logger.warn("HTTP port specified in properties file is not a valid integer. " +
                    "Falling back to defaultport " + WSMXKernel.DEFAULT_HTTP_PORT +".");
            httpport = WSMXKernel.DEFAULT_HTTP_PORT;
        }

        try {
            axisport = Integer.parseInt(instance.getProperty(KEY_AXISPORT, Integer.toString(WSMXKernel.DEFAULT_AXIS_PORT)));
        } catch (NumberFormatException nfe) {
            logger.warn("AXIS port specified in properties file is not a valid integer. " +
                    "Falling back to defaultport " + WSMXKernel.DEFAULT_AXIS_PORT +".");
            axisport = WSMXKernel.DEFAULT_AXIS_PORT;
        }
        
        
        try {
            sshport = Integer.parseInt(instance.getProperty(KEY_SSHPORT, Integer.toString(WSMXKernel.DEFAULT_SSH_PORT)));
        } catch (NumberFormatException nfe) {
            logger.warn("SSH port specified in properties file is not a valid integer. " +
                    "Falling back to defaultport " + WSMXKernel.DEFAULT_SSH_PORT +".");
            sshport = WSMXKernel.DEFAULT_SSH_PORT;
        }
        File codebase = null;
        String codebaseString = instance.getProperty(KEY_SYSTEMCODEBASE);
        if (codebaseString != null) {
        	codebase = new File(codebaseString);
        	if (!codebase.canRead()) {
	            logger.warn("Systemcodebase specified in properties can not be read: " + codebase +
	                    " Falling back to defaultcodebase " + WSMXKernel.KERNEL_LOCATION.getAbsolutePath() +".");
	            codebase = WSMXKernel.KERNEL_LOCATION;
	        }
        } else
            codebase = WSMXKernel.KERNEL_LOCATION;
        
        String spaceaddress = instance.getProperty(KEY_SPACEADDRESS, WSMXKernel.DEFAULT_SPACEADDRESS);
        try {
            new LookupLocator("jini://" + spaceaddress);
        } catch (MalformedURLException e) {
            logger.warn("Spaceaddress specified in properties is not valid: " + e.getMessage() + " " +
                    " Falling back to defaultaddress " + WSMXKernel.DEFAULT_SPACEADDRESS +".");
            spaceaddress = WSMXKernel.DEFAULT_SPACEADDRESS;
        }
        return new KernelConfiguration(
                codebase,
                spaceaddress,
                httpport,
                axisport,
                sshport
        );
    }

}

