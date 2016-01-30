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

import ie.deri.wsmx.core.management.sshadapter.TerminalIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * An editable, buffered, input line. Keeps
 * a history of previously executed commands,
 * ready for modification and reuse. 
 *
 * <pre>
 * Created on Aug 4, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/shell/Editline.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.4 $ $Date: 2005-09-18 15:04:21 $
 */ 
class Editline {

	static Logger logger = Logger.getLogger(Editline.class);

	private static List<Character> validChars = new ArrayList<Character>();

	static {
		Character[] chars = new Character[]{
				'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
				'!','@','#','$','&','*','(',')','[',']','{','}','=','%','-','+',',','.','?',';',':','/','\\','|','^','_',
				'<','>','~',' ','0','1','2','3','4','5','6','7','8','9','0'};
		validChars.addAll(Arrays.asList(chars));
	}

	private static List<String> history = new ArrayList<String>();
	private static int historyIndex = 0; 
	private Buffer m_Buffer;
	private TerminalIO m_IO;
	private int m_Cursor = 0;
	private boolean m_InsertMode = true;
	private int m_LastSize = 0;
	private boolean m_HardWrapped = false;
	private char m_LastRead;
	private int m_LastCursPos = 0;

	public Editline(TerminalIO io) {
		m_IO = io;
		m_Buffer = new Buffer(m_IO.getColumns() - 1);
		m_Cursor = 0;
		m_InsertMode = true;
	}

	public int size() {
		return m_Buffer.size();
	}

	public String getValue() {
		return m_Buffer.toString();
	}

	public void setValue(String str) throws BufferOverflowException, IOException {
		storeSize();
		m_Buffer.clear();
		m_Cursor = 0;
		m_IO.moveLeft(m_LastSize);
		m_IO.eraseToEndOfLine();
		append(str);
	}

	public void clear() throws IOException {
		storeSize();
		m_Buffer.clear();
		m_Cursor = 0;
		draw();
	}

	public String getSoftwrap() throws IndexOutOfBoundsException, IOException {
		String content = m_Buffer.toString();
		int idx = content.lastIndexOf(" ");
		if (idx == -1) {
			content = "";
		} else {
			content = content.substring(idx + 1, content.length());
			m_Cursor = size();
			m_Cursor = m_Cursor - content.length();
			for (int i = 0; i < content.length(); i++) {
				m_Buffer.removeCharAt(m_Cursor);
			}
			m_IO.moveLeft(content.length());
			m_IO.eraseToEndOfLine();
		}
		return content + getLastRead();
	}

	public String getHardwrap() throws IndexOutOfBoundsException, IOException {
		String content = m_Buffer.toString();
		content = content.substring(m_Cursor, content.length());
		int lastsize = m_Buffer.size();
		for (int i = m_Cursor; i < lastsize; i++) {
			m_Buffer.removeCharAt(m_Cursor);
		}
		m_IO.eraseToEndOfLine();
		return content;
	}

	private void setCharAt(int pos, char ch) throws IndexOutOfBoundsException,
			IOException {
		m_Buffer.setCharAt(pos, ch);
		draw();
	}

	private void insertCharAt(int pos, char ch) 
			throws BufferOverflowException, IndexOutOfBoundsException, IOException {
		storeSize();
		m_Buffer.ensureSpace(1);
		m_Buffer.insertCharAt(pos, ch);
		if (m_Cursor >= pos) {
			m_Cursor++;
		}
		draw();
	}

	private void removeCharAt(int pos) throws IndexOutOfBoundsException, IOException {
		storeSize();
		m_Buffer.removeCharAt(pos);
		if (m_Cursor > pos) {
			m_Cursor--;
		}
		draw();
	}

	public void insertStringAt(int pos, String str)
			throws BufferOverflowException, IndexOutOfBoundsException, IOException {
		storeSize();
		m_Buffer.ensureSpace(str.length());
		for (int i = 0; i < str.length(); i++) {
			m_Buffer.insertCharAt(pos, str.charAt(i));
			m_Cursor++;
		}
		draw();
	}

	public void append(char ch) throws BufferOverflowException, IOException {
		storeSize();
		m_Buffer.ensureSpace(1);
		m_Buffer.append(ch);
		m_Cursor++;
		m_IO.write(ch);
	}

	public void append(String str) throws BufferOverflowException, IOException {
		storeSize();
		m_Buffer.ensureSpace(str.length());
		for (int i = 0; i < str.length(); i++) {
			m_Buffer.append(str.charAt(i));
			m_Cursor++;
		}
		m_IO.write(str);
	}

	public int getCursorPosition() {
		return m_Cursor;
	}

	public void setCursorPosition(int pos) {
		if (m_Buffer.size() < pos) {
			m_Cursor = m_Buffer.size();
		} else {
			m_Cursor = pos;
		}
	}

	private char getLastRead() {
		return m_LastRead;
	}

	private void setLastRead(char ch) {
		m_LastRead = ch;
	}

	public boolean isInInsertMode() {
		return m_InsertMode;
	}

	public void setInsertMode(boolean b) {
		m_InsertMode = b;
	}

	public boolean isHardwrapped() {
		return m_HardWrapped;
	}

	public void setHardwrapped(boolean b) {
		m_HardWrapped = b;
	}

	public int run() throws IOException {
		int in = 0;
		do {
			in = m_IO.read();
			m_LastCursPos = m_Cursor;
			
			switch (in) {
			
			case TerminalIO.LEFT:
				if (!moveLeft()) {
					return in;
				}
				break;
			case TerminalIO.RIGHT:
				if (!moveRight()) {
					return in;
				}
				break;
			case TerminalIO.BACKSPACE:
				try {
					if (m_Cursor == 0) {
						return in;
					}
					removeCharAt(m_Cursor - 1);
				} catch (IndexOutOfBoundsException ioobex) {
					m_IO.bell();
				}
				break;
			case TerminalIO.DELETE:
				try {
					removeCharAt(m_Cursor);
				} catch (IndexOutOfBoundsException ioobex) {
					m_IO.bell();
				}
				break;
			case TerminalIO.ENTER:
				history.add(getValue());
				historyIndex = history.size();
				return in;
			case TerminalIO.UP:
				try {
						if (historyIndex > 0 && historyIndex <= history.size())
							setValue(history.get(decreaseHistoryIndex()));
					} catch (BufferOverflowException e) {
						logger.warn("Bufferoverflow during history traversal.", e);
					} catch (IndexOutOfBoundsException ioobe) {
						logger.warn("Index out of bounds during history traversal.", ioobe);						
					}
					break;
			case TerminalIO.DOWN:
				try {
					if (historyIndex >= 0 && historyIndex < history.size())
							setValue(history.get(increaseHistoryIndex()));
					} catch (BufferOverflowException e) {
						logger.warn("Bufferoverflow during history traversal.", e);
					} catch (IndexOutOfBoundsException ioobe) {
						logger.warn("Index out of bounds during history traversal.", ioobe);						
					}
					break;
			case TerminalIO.TABULATOR:
				return in;
			default:
				try {
					if (validChars.contains(new Character((char)in)))
						handleCharInput(in);
				} catch (BufferOverflowException boex) {
					setLastRead((char) in);
					
					return in;
				}
			}
			m_IO.flush();
		} while (true);
	}
	
	private int increaseHistoryIndex() {
		if (historyIndex + 1 < history.size())
			historyIndex += 1;
		return historyIndex;
	}

	private int decreaseHistoryIndex() {
		if (historyIndex > 0)
			historyIndex -= 1;
		return historyIndex;		
	}

	
	public void draw() throws IOException {
		m_IO.moveLeft(m_LastCursPos);
		m_IO.eraseToEndOfLine();
		m_IO.write(m_Buffer.toString());
		if (m_Cursor < m_Buffer.size()) {
			m_IO.moveLeft(m_Buffer.size() - m_Cursor);
		}
	}

	private boolean moveRight() throws IOException {
		if (m_Cursor < m_Buffer.size()) {
			m_Cursor++;
			m_IO.moveRight(1);
			return true;
		}
		return false;
	}

	private boolean moveLeft() throws IOException {
		if (m_Cursor > 0) {
			m_Cursor--;
			m_IO.moveLeft(1);
			return true;
		}
		return false;
	}

	private boolean isCursorAtEnd() {
		return (m_Cursor == m_Buffer.size());
	}

	private void handleCharInput(int ch) throws BufferOverflowException, IOException {
		if (isCursorAtEnd()) {
			append((char) ch);
		} else {
			if (isInInsertMode()) {
				try {
					insertCharAt(m_Cursor, (char) ch);
				} catch (BufferOverflowException ex) {
					//ignore buffer overflow on insert
					m_IO.bell();
				}
			} else {
				setCharAt(m_Cursor, (char) ch);
			}
		}
	}

	private void storeSize() {
		m_LastSize = m_Buffer.size();
	}

	class Buffer extends CharBuffer {
		public Buffer(int size) {
			super(size);
		}
	}
}