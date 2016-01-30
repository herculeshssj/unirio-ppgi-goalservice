package org.deri.wsmx.mediation.ooMediator.wsmx;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * Outputstream that uses the given log4j logger.
 * Needs to be flushed explicitly to write.
 * 
 * @author Max
 *
 */
public class Log4jOutputStream extends OutputStream {
	

	protected Logger log;
	protected byte[] buf;
	protected int count = 0;
	protected boolean closed = false;
	private static final int DEFAULT_INITIAL_BUFFER_SIZE = 32;

	public Log4jOutputStream(Logger log) {
		this.log = log;
		buf = new byte[DEFAULT_INITIAL_BUFFER_SIZE];
	}

	@Override
	public void write(int b) throws IOException {
		if (closed) throw new IOException("Tried to write on closed output stream.");
		resize(1);
		buf[count++] = (byte) b;
	}	

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (closed) throw new IOException("Tried to write on closed output stream.");
		if (len >= 0)
			resize(len);
		System.arraycopy(b, off, buf, count, len);
		count += len;
	}

	@Override
	public void flush() throws IOException {
		if (closed) throw new IOException("Tried to flush closed output stream.");
		log.info(toString());
		count = 0;
	}

	@Override
	public void close() throws IOException {
		if (closed) throw new IOException("Output stream already closed.");
		flush();
		this.closed = true;
		buf = null;
	}

	@Override
	public String toString() {
		return new String(buf, 0, count);
	}

	private void resize(int add) {
		if (count + add > buf.length) {
			int newlen = buf.length * 2;
			if (count + add > newlen)
				newlen = count + add;
			byte[] newbuf = new byte[newlen];
			System.arraycopy(buf, 0, newbuf, 0, count);
			buf = newbuf;
		}
	}
	
	

}
