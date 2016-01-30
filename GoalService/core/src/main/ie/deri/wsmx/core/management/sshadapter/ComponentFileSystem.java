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

package ie.deri.wsmx.core.management.sshadapter;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sshtools.daemon.platform.InvalidHandleException;
import com.sshtools.daemon.platform.NativeFileSystemProvider;
import com.sshtools.daemon.platform.PermissionDeniedException;
import com.sshtools.daemon.platform.UnsupportedFileOperationException;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.io.UnsignedInteger64;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;

/**
 * TODO Comment.
 *
 * <pre>
 * Created on 02.05.2005
 * Committed by $$Author: mzaremba $$
 * $$Source: /cygdrive/e/progs/cygwin/usr/maczar/cvsbackup/core/src/main/ie/deri/wsmx/core/management/sshadapter/ComponentFileSystem.java,v $$
 * </pre>
 * 
 * @author Thomas Haselwanter
 * @author Michal Zaremba
 *
 * @version $Revision: 1.3 $ $Date: 2005-07-01 16:45:20 $
 */ 
public class ComponentFileSystem extends NativeFileSystemProvider {

    public ComponentFileSystem() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean fileExists(String path) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getCanonicalPath(String path) throws IOException,
            FileNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRealPath(String path) throws FileNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean makeDirectory(String path) throws PermissionDeniedException,
            FileNotFoundException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public FileAttributes getFileAttributes(String path) throws IOException,
            FileNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileAttributes getFileAttributes(byte[] handle) throws IOException,
            InvalidHandleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] openDirectory(String path) throws PermissionDeniedException,
            FileNotFoundException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SftpFile[] readDirectory(byte[] handle)
            throws InvalidHandleException, EOFException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] openFile(String path, UnsignedInteger32 flags,
            FileAttributes attrs) throws PermissionDeniedException,
            FileNotFoundException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] readFile(byte[] handle, UnsignedInteger64 offset,
            UnsignedInteger32 len) throws InvalidHandleException, EOFException,
            IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeFile(byte[] handle, UnsignedInteger64 offset, byte[] data,
            int off, int len) throws InvalidHandleException, IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void closeFile(byte[] handle) throws InvalidHandleException,
            IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeFile(String path) throws PermissionDeniedException,
            IOException, FileNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void renameFile(String oldpath, String newpath)
            throws PermissionDeniedException, FileNotFoundException,
            IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeDirectory(String path) throws PermissionDeniedException,
            FileNotFoundException, IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFileAttributes(String path, FileAttributes attrs)
            throws PermissionDeniedException, IOException,
            FileNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFileAttributes(byte[] handle, FileAttributes attrs)
            throws PermissionDeniedException, IOException,
            InvalidHandleException {
        // TODO Auto-generated method stub

    }

    @Override
    public SftpFile readSymbolicLink(String path)
            throws UnsupportedFileOperationException, FileNotFoundException,
            IOException, PermissionDeniedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createSymbolicLink(String link, String target)
            throws UnsupportedFileOperationException, FileNotFoundException,
            IOException, PermissionDeniedException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDefaultPath(String username) throws FileNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void verifyPermissions(String username, String path,
            String permissions) throws PermissionDeniedException,
            FileNotFoundException, IOException {
        // TODO Auto-generated method stub

    }

}
