package ie.deri.wsmx.core.management.shell.commands;

import ie.deri.wsmx.core.logging.CleanPatternLayout;
import ie.deri.wsmx.core.management.shell.AdministrativeShell;
import ie.deri.wsmx.core.management.shell.SSHCommandProcessor;
import ie.deri.wsmx.core.management.sshadapter.TerminalIO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

public class LogListen extends WriterAppender implements SSHCommandProcessor  {

	protected Logger logger = Logger.getLogger(LogListen.class);
	TerminalIO terminal;
	
	public LogListen() {
		super();
	}

    public void activateOptions() {
    	setWriter(createWriter(new SshOutStream(terminal)));
    	PatternLayout layout = new CleanPatternLayout("%-5p %-25c{1}: %m%n");
    	setLayout(layout);
    	super.activateOptions();
    }
	
	public void execute(AdministrativeShell shell, List<String> arguments) throws IOException, JMException {
	    Logger root = Logger.getRootLogger();
		this.terminal = shell.getTerminal();
  	    activateOptions();
	    root.addAppender(this);
		terminal.write("Entering listen mode. Press ENTER to return to the command prompt.\n");
		terminal.flush();
		int in = 0;
		while(in != TerminalIO.ENTER)
			in = terminal.read();
		//clean up
		root.removeAppender(this);
	}

	public void printHelpText(TerminalIO terminal, int baseColor, int highlightColor) throws IOException {
		terminal.setForegroundColor(highlightColor);
		terminal.write("L");
		terminal.setForegroundColor(baseColor);
		terminal.write("og");
		terminal.setForegroundColor(highlightColor);
		terminal.write("l");
		terminal.setForegroundColor(baseColor);
		terminal.write("istener\tListen to the log statement and print them on the console.");
		terminal.flush();		
	}

	public void printUsageText(TerminalIO terminal, int baseColor, int highlightColor) throws IOException {
		terminal.setForegroundColor(baseColor);
		terminal.write("Usage: ");
		terminal.setForegroundColor(highlightColor);
		terminal.write("ll ");
		terminal.setForegroundColor(baseColor);
		terminal.println();
	}

	public void setMBeanServer(MBeanServer server) {
	}

	public void setDocumentBuilder(DocumentBuilder builder) {
	}

    /**
     * An implementation of OutputStream that redirects to the
     * ssh console writer.
     */
    private static class SshOutStream extends OutputStream {

    	protected Logger logger = Logger.getLogger(SshOutStream.class);
    	private TerminalIO terminal;
    	
    	public SshOutStream(TerminalIO terminal) {
    		this.terminal = terminal;
        }

        public void close() {
        }

        public void flush() {
        	try {
				terminal.flush();
			} catch (IOException e) {
				logger.warn("Failed to flush.", e);
			}
        }

        public void write(final byte[] b) throws IOException {
        	terminal.write(b);
        }

        public void write(final byte[] b, final int off, final int len)
            throws IOException {
        	if (off > 0)
        		terminal.moveRight(off);
        	byte[] shortened = new byte[len];
        	for (int i = 0; i < shortened.length; i++) {
        		shortened[i] = b[i];
			}        	
        	terminal.write(shortened);
        }

        public void write(final int b) throws IOException {
        	terminal.write(b);
        }
    }	
	
}
