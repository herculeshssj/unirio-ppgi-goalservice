package ie.deri.wsmx.core.management.sshadapter;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TerminalIO extends com.sshtools.daemon.terminal.TerminalIO {
	

    public static final int DELETE = 126;
    public static final int BACKSPACE = 1302;

    
	public TerminalIO(InputStream in, OutputStream out, String term, int cols, int rows) throws IOException {
		super(in, out, term, cols, rows);
	}

	@Override
	public void setTerminal(String terminalName) throws IOException {
		terminal = TerminalFactory.newInstance(terminalName);        
        initTerminal();
	}
	
    private void initTerminal() throws IOException {
        write(terminal.getInitSequence());
        flush();
    }
    


	
	
}
