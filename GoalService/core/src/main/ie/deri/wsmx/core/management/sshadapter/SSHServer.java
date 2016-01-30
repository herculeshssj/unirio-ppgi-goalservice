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

import java.io.IOException;

import org.apache.log4j.Logger;

import com.sshtools.daemon.SshServer;
import com.sshtools.daemon.configuration.ServerConfiguration;
import com.sshtools.daemon.forwarding.ForwardingServer;
import com.sshtools.daemon.session.SessionChannelFactory;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.connection.ConnectionProtocol;

/**
 * TODO Comment this type.
 *
 * <pre>
 * Created on Aug 5, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/sshadapter/SSHServer.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.2 $ $Date: 2005-08-06 02:54:41 $
 */ 
public class SSHServer extends SshServer {

	static Logger logger = Logger.getLogger(SSHServer.class);
	
	public SSHServer() throws IOException {
		super();
	}

	@Override
	public void configureServices(ConnectionProtocol connection)
			throws IOException {
		connection.addChannelFactory(SessionChannelFactory.SESSION_CHANNEL,
				new SessionChannelFactory());

		if (ConfigurationLoader
				.isConfigurationAvailable(ServerConfiguration.class)) {
			if (((ServerConfiguration) ConfigurationLoader
					.getConfiguration(ServerConfiguration.class))
					.getAllowTcpForwarding()) {
				new ForwardingServer(connection);
			}
		}

	}

	@Override
	public void shutdown(String msg) {
		//all the work is already done in the stopServer method that calls this one here
		logger.info(msg);
	}
	
	@Override
    public void startServer() throws IOException {
        startServerSocket();
    }

}
