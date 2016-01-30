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

package ie.deri.wsmx.core.management.shell.commands;

import ie.deri.wsmx.core.management.commandprocessor.InvokeOperationCommandProcessor;
import ie.deri.wsmx.core.management.shell.AdministrativeShell;
import ie.deri.wsmx.core.management.shell.SSHCommandProcessor;
import ie.deri.wsmx.core.management.sshadapter.TerminalIO;

import java.io.IOException;
import java.util.List;

import javax.management.JMException;

import org.w3c.dom.Document;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 09.05.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/shell/commands/Uptime.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2006-01-08 23:47:21 $
 */ 
public class Uptime extends InvokeOperationCommandProcessor 
				    implements SSHCommandProcessor {

    public Uptime() {
        super();
    }

	public void printHelpText(TerminalIO terminal, int baseColor, int highlightColor) throws IOException {
		terminal.setForegroundColor(highlightColor);
		terminal.write("U");
		terminal.setForegroundColor(baseColor);
		terminal.write("p");
		terminal.setForegroundColor(highlightColor);
		terminal.write("t");
		terminal.setForegroundColor(baseColor);
		terminal.write("ime\t\tDisplays the amount of time this WSMX instance has been up.");
		terminal.flush();
	}

	public void printUsageText(TerminalIO terminal, int baseColor, int highlightColor) throws IOException {
		terminal.setForegroundColor(baseColor);
		terminal.write("Usage: ");
		terminal.setForegroundColor(highlightColor);
		terminal.write("ut ");
		terminal.setForegroundColor(baseColor);
		terminal.println();
	}
	
    public String getHelpText() {
        return "UpTime - Displays the amount of time this WSMX instance has been up.";
    }

    public String getUsageText() {
        return "Usage: ut";
    }

    public void execute(AdministrativeShell shell, List<String> arguments)
    		throws IOException, JMException {
        objectname = "core:name=WSMXKernel";
        operationname = "getFormattedUptime";
    	TerminalIO terminal = shell.getTerminal();
    	Document document = execute();
    	String result = document.getElementsByTagName("Operation").item(0).getAttributes().getNamedItem("result").getTextContent();
        if (result.equals("error")) {
        	String errorMsg = document.getElementsByTagName("Operation").item(0).getAttributes().getNamedItem("errorMsg").getTextContent();
        	terminal.println(errorMsg);
        } else if (result.equals("success")) {
        	String returnValue = document.getElementsByTagName("Operation").item(0).getAttributes().getNamedItem("return").getTextContent();
        	terminal.println(returnValue);	
        } else {
        	//TODO log
        	terminal.println("Unexpected condition. Operation was neither a success nor a failure.");
        }
    }

}
