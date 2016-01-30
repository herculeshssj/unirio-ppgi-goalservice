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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.sshtools.daemon.configuration.ServerConfiguration;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

public class SSHDaemonConfiguration extends ServerConfiguration {
    
    static Logger logger = Logger.getLogger(SSHDaemonConfiguration.class);
    private Map<String, SshPrivateKey> serverHostKeys = new HashMap<String, SshPrivateKey>();
    private int port = 22;
    
    public SSHDaemonConfiguration(InputStream in) throws SAXException,
            ParserConfigurationException, IOException {
        super(in);
    }

    @Override
	public Map getServerHostKeys() {
        return serverHostKeys;
    }

    public void setServerHostKeys(File keyfile) {
        logger.debug("Keyfile location: " + keyfile);
        try {
            if (keyfile.exists()) {
                SshPrivateKeyFile pkf = SshPrivateKeyFile.parse(keyfile);
                SshPrivateKey key = pkf.toPrivateKey(null);
                logger.info("Algorithm " + key.getAlgorithmName() +
                             " with fingerprint " + key.getPublicKey().getFingerprint());

                serverHostKeys.put(key.getAlgorithmName(), key);
            } else {
                logger.warn("Private key file doesn't exist: " + keyfile.getAbsolutePath());
            }
        } catch (InvalidSshKeyException iske) {
            logger.warn("Failed to load private key file: " + iske.getMessage());
        } catch (IOException ioe) {
            logger.warn("Failed to load private key file" + keyfile.getAbsolutePath(), ioe);
        }
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
	public int getPort() {
        return port;
    }
    
}
