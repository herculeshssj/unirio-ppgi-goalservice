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

import ie.deri.wsmx.core.management.commandprocessor.AbstractCommandProcessor;
import ie.deri.wsmx.core.management.shell.AdministrativeShell;
import ie.deri.wsmx.core.management.shell.SSHCommandProcessor;
import ie.deri.wsmx.core.management.sshadapter.TerminalIO;

import java.io.IOException;
import java.util.List;

import javax.management.JMException;

import org.w3c.dom.Document;

/**
 * Erases the viewable part of the screen.
 *
 * <pre>
 * Created on Aug 4, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/shell/commands/ClearScreen.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2006-01-08 23:47:21 $
 */ 
public class ClearScreen extends AbstractCommandProcessor implements SSHCommandProcessor {

	public void execute(AdministrativeShell shell, List<String> arguments)
			throws IOException, JMException {
		shell.getTerminal().eraseScreen();
		shell.getTerminal().homeCursor();
	}

	public void printHelpText(TerminalIO terminal, int baseColor,
			int highlightColor) throws IOException {
		terminal.setForegroundColor(highlightColor);
		terminal.write("C");
		terminal.setForegroundColor(baseColor);
		terminal.write("lear");
		terminal.setForegroundColor(highlightColor);
		terminal.write("S");
		terminal.setForegroundColor(baseColor);
		terminal.write("creen\tErases the viewable part of the screen.");
		terminal.flush();
	}

	public void printUsageText(TerminalIO terminal, int baseColor,
			int highlightColor) throws IOException {
		terminal.setForegroundColor(baseColor);
		terminal.write("Usage: ");
		terminal.setForegroundColor(highlightColor);
		terminal.write("cs ");
		terminal.setForegroundColor(baseColor);
		terminal.println();
	}

	@Override
	protected Document execute() throws IOException, JMException {
		return null;
	}

}
