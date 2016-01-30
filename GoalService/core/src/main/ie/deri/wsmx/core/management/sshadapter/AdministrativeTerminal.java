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

import ie.deri.wsmx.core.management.shell.AdministrativeShell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.management.MBeanServer;

import org.apache.log4j.Logger;

import com.sshtools.daemon.platform.NativeProcessProvider;
import com.sshtools.j2ssh.io.DynamicBuffer;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 02.05.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/sshadapter/AdministrativeTerminal.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.9 $ $Date: 2005-09-21 16:33:49 $
 */ 
public class AdministrativeTerminal extends NativeProcessProvider {
    
    static Logger logger = Logger.getLogger(AdministrativeTerminal.class);

    private DynamicBuffer stdin = new DynamicBuffer();
    private DynamicBuffer stderr = new DynamicBuffer();
    private DynamicBuffer stdout = new DynamicBuffer();

    private TerminalIO terminal;
    private AdministrativeShell shell;
    private static MBeanServer mBeanServer;
    
   
    public AdministrativeTerminal() {
        super();
        try {
            new TerminalIO(
                    stdin.getInputStream(),
                    stdout.getOutputStream(),
                    "ansi",
                    50,
                    50);
        } catch (IOException ioe) {
            logger.warn("Terminal failed during initialization.", ioe);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return stdin.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return stdout.getOutputStream();
    }

    @Override
    public InputStream getStderrInputStream() throws IOException {
        return stderr.getInputStream();
    }

    @Override
    public void kill() {
        shell.stop();
        stdin.close();
        stdout.close();
        stderr.close();
    }

    @Override
    public boolean stillActive() {
        return true;
    }

    @Override
    public int waitForExitCode() {
       shell.start();
        while (shell.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.warn("Terminal operation failed.", ie);
            }
        }
        return shell.getExitCode();            
    }

    @Override
    public String getDefaultTerminalProvider() {
        return "UnsupportedShell";
    }

    @Override
    public boolean createProcess(String command, Map environment)
            throws IOException {
        try {
            if (terminal == null) {
                stderr.getOutputStream().write("No Pseudoterminal was requested, but the shell requires one.\r\n"
                    .getBytes());
                return false;
            }
        } catch (IOException ioe) {
            logger.warn("Terminal operation failed.", ioe);
        }
        
        shell = new AdministrativeShell(mBeanServer, terminal);
        
        return true;
    }

    @Override
    public void start() throws IOException {
    }

    @Override
    public boolean supportsPseudoTerminal(String term) {
        return true;
    }

    @Override
    public boolean allocatePseudoTerminal(String term, int cols, int rows,
            int width, int height, String modes) {
        try {
            terminal = new TerminalIO(stdout.getInputStream(),
                    stdin.getOutputStream(), term, cols, rows);

            return true;
        } catch (IOException ioe) {
            logger.warn("Terminal operation failed.", ioe);
            return false;
        }
    }

	public static MBeanServer getMBeanServer() {
		return mBeanServer;
	}

	public static void setMBeanServer(MBeanServer beanServer) {
		mBeanServer = beanServer;
	}


}
