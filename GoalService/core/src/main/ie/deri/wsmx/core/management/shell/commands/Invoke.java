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

public class Invoke extends InvokeOperationCommandProcessor
								 implements SSHCommandProcessor {

	public Invoke() {
		super();
	}

	public void printHelpText(TerminalIO terminal, int baseColor, int highlightColor) throws IOException {
		terminal.setForegroundColor(highlightColor);
		terminal.write("I");
		terminal.setForegroundColor(baseColor);
		terminal.write("n");
		terminal.setForegroundColor(highlightColor);
		terminal.write("v");
		terminal.setForegroundColor(baseColor);
		terminal.write("oke\t\tUsed to invoke arbitrary operations on components.");
		terminal.flush();		
	}

	public void printUsageText(TerminalIO terminal, int baseColor, int highlightColor) throws IOException {
		terminal.setForegroundColor(baseColor);
		terminal.write("Usage: ");
		terminal.setForegroundColor(highlightColor);
		terminal.write("iv ");
		terminal.setForegroundColor(baseColor);
		terminal.write("object_name operation_name [[argument_class][argument_value]]*");
		terminal.println();
	}
	
	public void execute(AdministrativeShell shell, List<String> arguments) 
			throws IOException, JMException {
		if (arguments.size() < 2) {
        	printUsageText(shell.getTerminal(), TerminalIO.white, TerminalIO.RED);
        	return;
		}
		objectname = arguments.get(0);
        operationname = arguments.get(1);
        
        for (int i = 0; i < arguments.size(); i++) {
        	if (i > 1) {
        		if (i % 2 == 0)
        			parameterTypes.put("type" + i, arguments.get(i));
        		else
        			parameterValues.put("value" + (i-1), arguments.get(i));
        		
        	}
        }        
        Document document = execute();
        String result = document.getElementsByTagName("Operation").item(0).getAttributes().getNamedItem("result").getTextContent();
        if (result.equals("error")) {
        	String errorMsg = document.getElementsByTagName("Operation").item(0).getAttributes().getNamedItem("errorMsg").getTextContent();
        	shell.getTerminal().println(errorMsg);
        } else if (result.equals("success")) {
        	String returnValue = document.getElementsByTagName("Operation").item(0).getAttributes().getNamedItem("return").getTextContent();
        	shell.getTerminal().println("Successful invocation. Return value: " + returnValue);	
        } else {
        	//TODO log
        	shell.getTerminal().println("Unexpected condition. Operation was neither a success nor a failure.");
        }
        return;
	}

	

}
