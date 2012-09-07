package de.ipk.vanted.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;

public interface VfsFileObject {
	public boolean delete() throws IOException;
	
	public boolean exists() throws IOException;
	
	public boolean isFile();
	
	public boolean isDirectory();
	
	public boolean isReadable();
	
	public boolean isWriteable();
	
	public boolean mkdir() throws IOException;
	
	public void renameTo(VfsFileObject target, boolean overWrite)
			throws IOException;
	
	public void download(VfsFileObject localFile) throws IOException;
	
	public void upload(VfsFileObject remoteFile) throws IOException;
	
	public String[] list() throws IOException;
	
	public String getName();
	
	public URL getURL() throws IOException;
	
	public long length();
	
	public OutputStream getOutputStream() throws IOException;
	
	public InputStream getInputStream() throws IOException;
	
	public FileObject getFile();
	
	public void setExecutable(boolean executable);
	
	public void setWritable(boolean writeable);
	
	public void setLastModified(long time);
}
