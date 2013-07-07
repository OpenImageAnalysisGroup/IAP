package de.ipk.vanted.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

public interface VfsFileObject {
	public boolean delete() throws IOException;
	
	public boolean exists() throws IOException;
	
	public boolean isFile() throws FileSystemException;
	
	public boolean isDirectory() throws FileSystemException;
	
	public boolean isReadable() throws FileSystemException;
	
	public boolean isWriteable() throws FileSystemException;
	
	public boolean mkdir() throws IOException;
	
	public void renameTo(VfsFileObject target, boolean overWrite)
			throws IOException;
	
	public void download(VfsFileObject localFile) throws IOException;
	
	public void upload(VfsFileObject remoteFile) throws IOException;
	
	/**
	 * List files and folders.
	 * 
	 * @return List of files and/or folders.
	 * @throws IOException
	 */
	public String[] list() throws IOException;
	
	/**
	 * @return List of folders.
	 * @throws IOException
	 */
	public String[] listFolders() throws IOException;
	
	public String getName();
	
	public URL getURL() throws IOException;
	
	public long length() throws IOException;
	
	public OutputStream getOutputStream() throws IOException;
	
	public InputStream getInputStream() throws Exception;
	
	public FileObject getFile();
	
	public void setExecutable(boolean executable);
	
	public void setWritable(boolean writeable);
	
	public void setLastModified(long time) throws Exception;
	
	public long getLastModified() throws Exception;
}
