package de.ipk.vanted.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

public abstract class AbsractVfsFileObject implements VfsFileObject {
	
	@Override
	public boolean delete() throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public void download(VfsFileObject localFile) throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public boolean exists() throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public InputStream getInputStream() throws Exception {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public boolean isDirectory() throws FileSystemException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public boolean isFile() throws FileSystemException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public boolean isReadable() throws FileSystemException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public boolean isWriteable() throws FileSystemException {
		throw new UnsupportedOperationException("IsWriteable can't be checked for this protocoll");
	}
	
	@Override
	public long length() throws FileSystemException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public String[] list() throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public String[] listFolders() throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public boolean mkdir() throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public void renameTo(VfsFileObject target, boolean overWrite)
			throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public void upload(VfsFileObject remoteFile) throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public String getName() {
		return null;
	}
	
	@Override
	public URL getURL() throws IOException {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public FileObject getFile() {
		throw new UnsupportedOperationException("Not implemented!");
	}
}
