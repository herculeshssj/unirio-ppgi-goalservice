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

package ie.deri.wsmx.core.management.sshadapter;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

import com.sshtools.daemon.configuration.PlatformConfiguration;
import com.sshtools.daemon.configuration.ServerConfiguration;
import com.sshtools.j2ssh.configuration.ConfigurationContext;
import com.sshtools.j2ssh.configuration.ConfigurationException;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 02.05.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/sshadapter/SSHDaemonConfigurationContext.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.8 $ $Date: 2005-08-14 05:29:46 $
 */ 
public class SSHDaemonConfigurationContext implements ConfigurationContext {
    static Logger logger = Logger.getLogger(SSHDaemonConfigurationContext.class);
    private HashMap<Class<? extends DefaultHandler>, Object> configurations = 
    	new HashMap<Class<? extends DefaultHandler>, Object>();
    private int port = 22;
    private File hostkey;
    
    InputStream serverStream = null, platformStream = null;

    boolean failOnError = true;

    public SSHDaemonConfigurationContext(File hostkey) {
    	this.hostkey = hostkey;
    }

    public void setServerConfigurationStream(InputStream serverStream) {
        this.serverStream = serverStream;
    }

    public void setPlatformConfigurationStream(InputStream platformStream) {
        this.platformStream = platformStream;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public void initialize() throws ConfigurationException {
        if (serverStream != null) {
            try {
                SSHDaemonConfiguration serverConfig = new SSHDaemonConfiguration(serverStream);
                serverConfig.setServerHostKeys(hostkey);
                serverConfig.setPort(port);
                configurations.put(ServerConfiguration.class, serverConfig);
            } catch (Exception ex) {
                logger.warn("Failure during initalization of SSH Server configuration.", ex);
                if (failOnError) {
                    throw new ConfigurationException(ex.getMessage());
                }
            }
        }
        if (platformStream != null) {
            try {
                PlatformConfiguration platformConfig = new PlatformConfiguration(
                        platformStream);
                configurations.put(PlatformConfiguration.class, platformConfig);
            } catch (Exception ex) {
                if (failOnError) {
                    throw new ConfigurationException(ex.getMessage());
                }
            }
        }
    }

    public boolean isConfigurationAvailable(Class cls) {
        return configurations.containsKey(cls);
    }

    public Object getConfiguration(Class cls) throws ConfigurationException {
        if (configurations.containsKey(cls)) {
            return configurations.get(cls);
        }
		throw new ConfigurationException(cls.getName()
		        + " configuration not available");
    }

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
