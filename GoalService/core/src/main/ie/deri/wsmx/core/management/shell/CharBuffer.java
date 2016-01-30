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

import java.util.Vector;


/**
 * A simple buffer for characters.
 *
 * <pre>
 * Created on Aug 4, 2005
 * Committed by $Author: haselwanter $
 * $Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/shell/CharBuffer.java,v $
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.2 $ $Date: 2005-08-12 23:10:14 $
 */ 
class CharBuffer {

  private Vector<Character> m_Buffer;
  private int m_Size;

  public CharBuffer(int size) {
    m_Buffer = new Vector<Character>(size);
    m_Size = size;
  }

  public char getCharAt(int pos)
      throws IndexOutOfBoundsException {

    return m_Buffer.elementAt(pos).charValue();
  }

  public void setCharAt(int pos, char ch)
      throws IndexOutOfBoundsException {

    m_Buffer.setElementAt(new Character(ch), pos);
  }

  public void insertCharAt(int pos, char ch)
      throws IndexOutOfBoundsException {
    m_Buffer.insertElementAt(new Character(ch), pos);
  }

  public void append(char aChar) {
    m_Buffer.addElement(new Character(aChar));
  }

  public void append(String str) {
    for (int i = 0; i < str.length(); i++) {
      append(str.charAt(i));
    }
  }

  public void removeCharAt(int pos)
      throws IndexOutOfBoundsException {

    m_Buffer.removeElementAt(pos);
  }

  public void clear() {
    m_Buffer.removeAllElements();
  }

  public int size() {
    return m_Buffer.size();
  }

  @Override
public String toString() {
    StringBuffer sbuf = new StringBuffer();
    for (int i = 0; i < m_Buffer.size(); i++) {
      sbuf.append(m_Buffer.elementAt(i).charValue());
    }
    return sbuf.toString();
  }

  public void ensureSpace(int chars)
      throws BufferOverflowException {

    if (chars > (m_Size - m_Buffer.size())) {
      throw new BufferOverflowException();
    }
  }
}