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

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.sshtools.j2ssh.transport.publickey.SECSHPublicKeyFormat;
import com.sshtools.j2ssh.transport.publickey.SshKeyPair;
import com.sshtools.j2ssh.transport.publickey.SshKeyPairFactory;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import com.sshtools.j2ssh.transport.publickey.SshPublicKeyFile;
import com.sshtools.j2ssh.transport.publickey.SshtoolsPrivateKeyFormat;
import com.sshtools.j2ssh.util.Base64;

/**
 * Generates 1024 bit SSH keys.
 *
 * <pre>
 * Created on Aug 13, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/sshadapter/KeyGenerator.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2005-08-14 05:28:30 $
 */ 
public class KeyGenerator {

	private static final int BITS = 1024;
	private static final String PASSPHRASE = "";
	
	static Logger logger = Logger.getLogger(KeyGenerator.class);

	public static void generateKeyPair(String type, String filename, String username)
			throws IOException {

		String keyType = type;

		if (keyType.equalsIgnoreCase("DSA")) {
			keyType = "ssh-dss";
		}

		if (keyType.equalsIgnoreCase("RSA")) {
			keyType = "ssh-rsa";
		}

		final SshKeyPair pair = SshKeyPairFactory.newInstance(keyType);
		logger.info("Generating " + String.valueOf(BITS) + " bit " + keyType + " key pair.");
		pair.generate(KeyGenerator.BITS);		

		SECSHPublicKeyFormat publicKeyFormat = new SECSHPublicKeyFormat(username,
				                                                        String.valueOf(BITS) + "-bit " + type);
		SshPublicKeyFile pub = SshPublicKeyFile.create(pair.getPublicKey(), publicKeyFormat);

		SshtoolsPrivateKeyFormat privateKeyFormat = new SshtoolsPrivateKeyFormat(username, String
				.valueOf(BITS)
				+ "-bit " + type);
				
		SshPrivateKeyFile prv = SshPrivateKeyFile.create(pair.getPrivateKey(), PASSPHRASE, privateKeyFormat);

		logger.info("Created key pair displayed in Base64 encoding:\n" +
				"------------------------- 1024-bit DSA public key --------------------------\n" +
				Base64.encodeBytes(pub.getKeyBlob(), false) + "\n" +
				"----------------------------------------------------------------------------\n" +
				"\n" +
				"------------------------- 1024-bit DSA private key -------------------------\n" +
				Base64.encodeBytes(prv.getKeyBlob(PASSPHRASE), false) + "\n" +
				"----------------------------------------------------------------------------\n");
		
		logger.info("Writing private key to file " + filename);
		FileOutputStream out = new FileOutputStream(filename);
		out.write(prv.getBytes());
		out.close();
	}
	
}
