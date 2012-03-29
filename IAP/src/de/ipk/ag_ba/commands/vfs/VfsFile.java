package de.ipk.ag_ba.commands.vfs;

import java.io.InputStream;
import java.io.OutputStream;

import org.graffiti.plugin.io.resources.IOurl;

public class VfsFile {
	
	public VfsFile(String prepareAndGetDataFileNameAndPath) {
		// TODO Auto-generated constructor stub
	}
	
	public VfsFile(IOurl url) {
		// TODO Auto-generated constructor stub
	}
	
	public void setLastModified(long time) {
		// TODO Auto-generated method stub
	}
	
	public void setWritable(boolean b) {
		// TODO Auto-generated method stub
	}
	
	public void setExecutable(boolean b) {
		// TODO Auto-generated method stub
	}
	
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void renameTo(VfsFile te) {
		// TODO Auto-generated method stub
	}
	
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void delete() {
		// TODO Auto-generated method stub
	}
	
	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int length() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getCanonicalPath() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getAbsolutePath() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public InputStream getInputStream() {
		return null;
	}
}
