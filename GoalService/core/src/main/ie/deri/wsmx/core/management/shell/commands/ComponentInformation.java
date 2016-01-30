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

import ie.deri.wsmx.core.management.commandprocessor.MBeanCommandProcessor;
import ie.deri.wsmx.core.management.shell.AdministrativeShell;
import ie.deri.wsmx.core.management.shell.SSHCommandProcessor;
import ie.deri.wsmx.core.management.sshadapter.TerminalIO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;


/**
 * Displays information about one or more components.
 *
 * <pre>
 * Created on 09.05.2005
 * Committed by $$Author: haselwanter $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/shell/commands/ComponentInformation.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.1 $ $Date: 2006-01-08 23:47:21 $
 */ 
public class ComponentInformation extends MBeanCommandProcessor
					   			  implements SSHCommandProcessor {

    public ComponentInformation() {
        super();
    }

	public void printHelpText(TerminalIO terminal, int baseColor, int highlightColor)
			throws IOException {
		terminal.setForegroundColor(highlightColor);
		terminal.write("C");
		terminal.setForegroundColor(baseColor);
		terminal.write("omponent");
		terminal.setForegroundColor(highlightColor);
		terminal.write("I");
		terminal.setForegroundColor(baseColor);
		terminal.write("nfo\tDisplays information about one or more components.");
		terminal.flush();
	}

	public void printUsageText(TerminalIO terminal, int baseColor, int highlightColor)
			throws IOException {
		terminal.setForegroundColor(baseColor);
		terminal.write("Usage: ");
		terminal.setForegroundColor(highlightColor);
		terminal.write("ci ");
		terminal.setForegroundColor(baseColor);
		terminal.write("object_name [object_name]*");
		terminal.println();
	}
	

    //TODO handle multiple objectnames as parameters
	//TODO color
    public void execute(AdministrativeShell shell, List<String> arguments)
    		throws IOException {
    	TerminalIO terminal = shell.getTerminal();
    	if (arguments.size() < 1) {
    		printUsageText(terminal, TerminalIO.white, TerminalIO.RED);
            return;
    	}
    	
    	objectName = arguments.get(0);
    	
        terminal.println();
        ObjectName name = null;
        MBeanInfo info = null;
        try {
        	name = new ObjectName(objectName);
		} catch (MalformedObjectNameException e) {
            terminal.println("Malformed objectname: " + e.getMessage());
            return;
		} catch (NullPointerException e) {
            terminal.println("Null parameter: " + e.getMessage());
            return;
		}
        try {
			info = mBeanServer.getMBeanInfo(name);
		} catch (InstanceNotFoundException e) {
            terminal.println("Instance not found: " + e.getMessage());
            return;
		} catch (IntrospectionException e) {
            terminal.println("Introspection failure: " + e.getMessage());
            return;
		} catch (ReflectionException e) {
            terminal.println("Reflection failure: " + e.getMessage());
            return;
		}
        terminal.println();
		terminal.println("Objectname:\t" + objectName);
        terminal.println("Class:\t\t" + info.getClassName());
        terminal.println("Hashcode:\t" + info.hashCode());
        terminal.println("Description:\t" + info.getDescription());
        terminal.println();
        terminal.println("Attribute count:\t" + info.getAttributes().length);
        terminal.println("Operation count:\t" + info.getOperations().length);
        terminal.println("Notification count:\t" + info.getNotifications().length);
        terminal.println();
        terminal.println("Attributes:");        
        for(MBeanAttributeInfo attribute :  info.getAttributes()) {
        	terminal.println();
        	terminal.println("   Name:\t\t" + attribute.getName());
        	terminal.println("   Type:\t\t" + attribute.getType());
        	terminal.println("   Description:\t" + attribute.getDescription());
        	terminal.println("   Readable:\t" + attribute.isReadable());
        	terminal.println("   Writable:\t" + attribute.isWritable());
        }
        terminal.println();
        terminal.println("Operations:");        
        for(MBeanOperationInfo operation :  info.getOperations()) {
        	terminal.println();
        	terminal.println("   Name:\t\t" + operation.getName());
        	terminal.println("   Impact:\t\t" + operation.getImpact());
        	terminal.println("   Description:\t" + operation.getDescription());
        	terminal.println("   Return type:\t" + operation.getReturnType());
        	//TODO signature, parameters
        }
        terminal.println();
        terminal.println("Notifications:");        
        for(MBeanNotificationInfo notification :  info.getNotifications()) {
        	terminal.println();
        	terminal.println("   Name:\t\t\t" + notification.getName());
        	terminal.println("   Description:\t\t" + notification.getDescription());
        	terminal.println("   Notification types:\t" + Arrays.asList(notification.getNotifTypes()));
        }
    }

}
