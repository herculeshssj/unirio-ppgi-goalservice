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
import java.io.IOException;
import java.io.InputStream;

import javax.management.Descriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.apache.log4j.Logger;

import com.sshtools.daemon.configuration.ServerConfiguration;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 02.05.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/sshadapter/SSHAdapter.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.14 $ $Date: 2005-10-13 00:33:57 $
 */ 
public class SSHAdapter {
	private static final int DEFAULT_PORT = 22;
	
	static Logger logger = Logger.getLogger(SSHAdapter.class);

	private SSHServer server;
    private MBeanServer mBeanServer;
    private boolean alive = false;
    private boolean keyAvailable = false;
    private File hostkey;
    private ServerConfiguration serverConfig ;

	private SSHDaemonConfigurationContext configurationContext;
   
    /**
     * Constructs a deamon running on the default port 22.
     * 
     * @param kernel
     * @param server
     * @param platform
     * @throws ConfigurationException if the configuration is invalid
     */
    public SSHAdapter(MBeanServer mBeanServer, File key, InputStream server, InputStream platform) throws ConfigurationException {
    	this(mBeanServer, key, server, platform, DEFAULT_PORT);
    }

    /**
     * Constructs a deamon running on the specified port.
     * 
     * @param kernel
     * @param server
     * @param platform
     * @param port
     * @throws ConfigurationException if the configuration is invalid
     */
    public SSHAdapter(MBeanServer mBeanServer, File key, InputStream server, InputStream platform, int port)
    		throws ConfigurationException {
        super();
        this.mBeanServer = mBeanServer;
        hostkey = key;
        if (!hostkey.canRead()) {
        	if (hostkey.exists()) {
				logger.warn("DSA keyfile exists but cannot be read.");
                keyAvailable = false;
        	}
            logger.warn("DSA keyfile does not exist. Attempting to recover and generate keys.");                
            try {
				generateKeys();
                keyAvailable = true;
			} catch (Throwable t) {
				logger.warn("Generating keys failed.", t);
                keyAvailable = false;
			}
        } else
        	keyAvailable = true;
        configurationContext = new SSHDaemonConfigurationContext(hostkey);
        configurationContext.setServerConfigurationStream(server);
        configurationContext.setPlatformConfigurationStream(platform);
        configurationContext.setPort(port);
        ConfigurationLoader.initialize(true, configurationContext);        
    }
    
    private void generateKeys () throws IOException {
    	KeyGenerator.generateKeyPair("DSA", hostkey.getAbsolutePath(), "WSMX Microkernel Keygenerator");
	}
    
    public void start() throws IOException, KeyNotAvailableException {
    	if (!keyAvailable)
    		throw new KeyNotAvailableException(hostkey.toString());
    	alive = true;
        try {
            AdministrativeTerminal.setMBeanServer(mBeanServer);
            
            server = new SSHServer();
            server.startServer();
        } catch (IOException e) {
            logger.warn("SSHAdapter startup failed: " + e.getMessage(), e);
            alive = false;
            return;            
        }
        serverConfig = ((ServerConfiguration) ConfigurationLoader.getConfiguration(ServerConfiguration.class));
        logger.info("SSHAdapter started on port " + serverConfig.getPort());
    }

    public void stop() throws IOException {
    	if(alive)
    		server.stopServer("SSH daemon stopped.");
    }

    public ModelMBeanInfo getModelMBeanInfo(ObjectName objectName) {
        Descriptor sshAdapterDescription =
            new DescriptorSupport(
                new String[] {
                    ("name=" + objectName),
                    "descriptorType=mbean",
                    ("displayName=" + SSHAdapter.class.getSimpleName()),
                    "type=" + SSHAdapter.class.getCanonicalName(),
                    "log=T",
                    "logFile=wrapper.log",
                    "currencyTimeLimit=10" });
        ModelMBeanInfo cmMBeanInfo =
            new ModelMBeanInfoSupport(
                SSHAdapter.class.getSimpleName(),
                "A SSH daemon that provides a secure, administrative shell",
                getModelMBeanAttributeInfo(),
                new ModelMBeanConstructorInfo[0],
                getModelMBeanOperationInfo(),
                new ModelMBeanNotificationInfo[0]);
        try {
            cmMBeanInfo.setMBeanDescriptor(sshAdapterDescription);
        } catch (Exception e) {
            logger.warn("CreateMBeanInfo failed with " + e.getMessage());
        }
        return cmMBeanInfo;
    }

	private ModelMBeanOperationInfo[] getModelMBeanOperationInfo() {
		ModelMBeanOperationInfo[] operations = new ModelMBeanOperationInfo[1];
		Descriptor getPort = new DescriptorSupport(
				new String[] {
						"name=getPort",
						"descriptorType=operation",
						"class=" + SSHAdapter.class.getCanonicalName(),
						"role=operation" });
		operations[0] = new ModelMBeanOperationInfo("getPort",
				"Returns the port this daemon is listening on.",
				new MBeanParameterInfo[0],
				"int",
				MBeanOperationInfo.INFO,
				getPort);
		return operations;
	}

	private ModelMBeanAttributeInfo[] getModelMBeanAttributeInfo() {
		ModelMBeanAttributeInfo[] attributes = new ModelMBeanAttributeInfo[1];
		Descriptor port =
			new DescriptorSupport(
				new String[] {
					"name=Port",
					"descriptorType=attribute",
					"displayName=Port",
					"getMethod=getPort",
                    "currencyTimeLimit=-1"  });
		attributes[0] =
			new ModelMBeanAttributeInfo(
				"Port",
				"int",
				"The port that the SSH daemon is running on.",
				true,
				false,
				false,
				port);
		return attributes;
	}
	
    public int getPort() {
    	if (configurationContext != null)
    		return configurationContext.getPort();  	
    	return 0;
    }
    
}
