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

package ie.deri.wsmx.core.management.shell;

import ie.deri.wsmx.core.management.shell.commands.ClearScreen;
import ie.deri.wsmx.core.management.shell.commands.ComponentInformation;
import ie.deri.wsmx.core.management.shell.commands.Invoke;
import ie.deri.wsmx.core.management.shell.commands.ListDomains;
import ie.deri.wsmx.core.management.shell.commands.LogListen;
import ie.deri.wsmx.core.management.shell.commands.Shutdown;
import ie.deri.wsmx.core.management.shell.commands.Uptime;
import ie.deri.wsmx.core.management.sshadapter.TerminalIO;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 09.05.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/shell/AdministrativeShell.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.13 $ $Date: 2006-01-08 23:48:25 $
 */ 
public class AdministrativeShell implements Runnable {
    
    static Logger logger = Logger.getLogger(AdministrativeShell.class);

    private String welcomeMessage = "WSMX Management Console\n" +
                                    "Copyright (c) 2005 National University of Ireland, Galway\n";
    String prompt;    
    private TerminalIO terminal;
    private boolean alive = true;
    private int exitCode = 0;
    private DocumentBuilder builder = null;
    
    private static Map<String, Class<? extends SSHCommandProcessor>> commandClasses = 
    	new HashMap<String, Class<? extends SSHCommandProcessor>>();
    private Map<String, SSHCommandProcessor> commands = 
    	new HashMap<String, SSHCommandProcessor>();
        
    static {
        commandClasses.put("sd", Shutdown.class);
        commandClasses.put("ll", LogListen.class);
        commandClasses.put("ut", Uptime.class);
        commandClasses.put("ld", ListDomains.class);
        commandClasses.put("iv", Invoke.class);
        commandClasses.put("ci", ComponentInformation.class);
        commandClasses.put("cs", ClearScreen.class);
    }
    
    public AdministrativeShell(MBeanServer mBeanServer, TerminalIO terminal) {
        super();
        this.terminal = terminal;
        
        DocumentBuilderFactory factory = DocumentBuilderFactory
        .newInstance();
        try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.warn("Failed to instantiate a DocumentBuilder for the SSHAdapter.");
		}

        for (Entry<String, Class<? extends SSHCommandProcessor>> entry : commandClasses.entrySet()) {
        	try {
        		SSHCommandProcessor scp = entry.getValue().newInstance();
        		scp.setMBeanServer(mBeanServer);
        		scp.setDocumentBuilder(builder);
        		commands.put(entry.getKey(), scp);
        	} catch (Throwable t) {
        		logger.fatal("Failed to instantiate command " + entry.getKey() + ", which will not be available.");
        	}
        }
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop() {
        alive = false;
    }

    public void run() {
		try {
			terminal.eraseScreen();
			terminal.homeCursor();
			terminal.write(welcomeMessage);
			terminal.write("Type ");
			terminal.setForegroundColor(TerminalIO.GREEN);
			terminal.write("?");
			terminal.setForegroundColor(TerminalIO.white);
			terminal.write(" for help");
			terminal.flush();
			terminal.println();
			terminal.println();
			terminal.write(getPrompt());
			terminal.flush();
		} catch (IOException ioe) {
			logger.warn("I/O failure of shell during startup.", ioe);
		}

		String cmd = "";

		Editline line = new Editline(terminal);
		while (alive) {
			try {
				int result = line.run();

				switch (result) {
				case TerminalIO.ENTER: {
					try {
						cmd = line.getValue();

						terminal.println();
						if (cmd.equals("exit") || cmd.equals("quit")
								|| cmd.equals("q")) {
							alive = false;
							break;
						}
						if (cmd.equals("?")) {
							printHelp(terminal);
							terminal.flush();
						} else if (cmd.trim().length() > 0) {
							processCommand(cmd);
						}
					} catch (Throwable t) {
						terminal
								.println("Failure during execution of command: "
										+ t.getMessage());
						logger.warn("Failure while executing command.", t);
					}
					cmd = "";
					terminal.write(getPrompt());
					terminal.flush();
					line = new Editline(terminal);
					break;
				}
				}

			} catch (IOException ioe) {
				//just continue since the shh daemon might be terminated
				//while we are in line edit mode line.run()
				//if this is the case alive will be false and the shell exits
			}

		}
	}

    private void processCommand(String cmd) {
        SSHCommandProcessor command = null;
		List<String> arguments = null;
		arguments = new ArrayList<String>(Arrays.asList(cmd.split(" ")));
		String key = arguments.remove(0);
		Class<? extends SSHCommandProcessor> commando = commandClasses.get(key);
		if (commando == null) {
			try {
				logger.debug("Command not supported.");
				terminal.println("Command not supported.");
				return;
			} catch (IOException ioe) {
				logger.debug("Command not supported. Terminal failed.");
				return;
			}
		}
		command = commands.get(key);

		if (command == null) {
			try {
				logger.debug("Command not supported.");
				terminal.println("Command not supported.");
				return;
			} catch (IOException ioe) {
				logger.debug("Command not supported. Terminal failed.");
				return;
			}
		}
		
        try {
        	command.execute(this, arguments);
        } catch(JMException jme) {
            try {
                logger.debug("Management extension failure during execution of command.", jme);
                terminal.println();
                terminal.println("Management extension failure during execution of command: " + jme.getMessage());
                return;
            } catch (IOException ioe) {
                logger.debug("I/O failure during exception handling of an" +
                		"exception that occured during execution of a command.", ioe);
            }
        } catch(IOException ioe) {
            try {
                logger.debug("I/O failure during execution of command.", ioe);
                terminal.println();
                terminal.println("I/O failure during execution of command: " + ioe.getMessage());
                return;
            } catch (IOException iioe) {
                logger.debug("I/O failure during exception handling of an" +
                		"exception that occured during execution of a command.", iioe);
            }
        	
        }
    }

    private void printHelp(TerminalIO terminal) throws IOException {
    	terminal.setForegroundColor(TerminalIO.RED);
        terminal.write("  ?");
    	terminal.setForegroundColor(TerminalIO.white);
    	terminal.write("  :: Help\t\tDisplay this help.\n");

    	terminal.setForegroundColor(TerminalIO.RED);
        terminal.write("  q");
    	terminal.setForegroundColor(TerminalIO.white);
    	terminal.write("  :: ");
    	terminal.setForegroundColor(TerminalIO.GREEN);
    	terminal.write("Q");
    	terminal.setForegroundColor(TerminalIO.white);
    	terminal.write("uit\t\tDisconnect.\n"); 
    	
        for (Map.Entry<String, SSHCommandProcessor> entry: commands.entrySet()) {
        	terminal.setForegroundColor(TerminalIO.RED);
            terminal.write("  " + entry.getKey());
        	terminal.setForegroundColor(TerminalIO.white);
        	terminal.write(" :: ");
            entry.getValue().printHelpText(terminal, TerminalIO.white, TerminalIO.GREEN);
            terminal.println();
        }        
    }

    public boolean isAlive() {
        return alive;
    }

    public int getExitCode() {
        return exitCode;
    }
    

    private String getPrompt() {
    	if (prompt == null) {
	    	try {
	            prompt = "root@"
	            + InetAddress.getLocalHost().getHostName() + "$ ";
	        } catch (UnknownHostException e) {
	            prompt = "$ ";
	        }
    	}
    	return prompt;
    }

    public TerminalIO getTerminal() {
        return terminal;
    }

}
