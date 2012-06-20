package de.ipk.ag_ba.commands.vfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractVfsFile implements VfsFile {
	
	public void setLastModified(long time) {
		// empty
	}
	
	public void setWritable(boolean b) {
		// empty
	}
	
	public void setExecutable(boolean b) {
		// empty
	}
	
	public InputStream getInputStream() throws FileNotFoundException {
		return null;
	}
	
	public String getAbsolutePath() {
		return null;
	}
	
	public String getCanonicalPath() throws IOException {
		return null;
	}
	
	public long length() {
		return -1;
	}
	
	public OutputStream getOutputStream() throws FileNotFoundException {
		return null;
	}
	
	public boolean delete() throws IOException {
		return false;
	}
	
	public boolean exists() {
		return false;
	}
	
	public void renameTo(VfsFile te) {
		// empty
	}
	
	public String getName() {
		return null;
	}
	
}
