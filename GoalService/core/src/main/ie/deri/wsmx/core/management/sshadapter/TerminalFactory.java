package ie.deri.wsmx.core.management.sshadapter;

import com.sshtools.daemon.terminal.Terminal;
import com.sshtools.daemon.terminal.ansi;
import com.sshtools.daemon.terminal.vt100;
import com.sshtools.daemon.terminal.xterm;


public class TerminalFactory {
    
	public TerminalFactory() {
    }

    public static Terminal newInstance(String term) {
        if (term.equalsIgnoreCase("ANSI")) {
            return new ansi();
        } else if (term.equalsIgnoreCase("xterm")) {
            return new xterm();
        } else if (term.equalsIgnoreCase("linux")) {
            return new ansi();
        } else {
            return new vt100();
        }
    }
}
