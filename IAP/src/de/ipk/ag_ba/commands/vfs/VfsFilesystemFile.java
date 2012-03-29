package de.ipk.ag_ba.commands.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;

public class VfsFilesystemFile extends AbstractVfsFile {
	
	File file = null;
	
	public VfsFilesystemFile(String fileName) {
		file = new File(fileName);
	}
	
	public VfsFilesystemFile(IOurl url) {
		file = FileSystemHandler.getFile(url);
	}
	
	@Override
	public void setLastModified(long time) {
		file.setLastModified(time);
	}
	
	@Override
	public void setWritable(boolean writable) {
		file.setWritable(writable);
	}
	
	@Override
	public void setExecutable(boolean executable) {
		file.setExecutable(executable);
	}
	
	@Override
	public String getName() {
		return file.getName();
	}
	
	@Override
	public void renameTo(VfsFile te) {
		File nf = new File(file.getParentFile().getAbsolutePath() + File.separator + te.getName());
		file.renameTo(nf);
		file = nf;
	}
	
	@Override
	public boolean exists() {
		return file.exists();
	}
	
	@Override
	public void delete() {
		file.delete();
	}
	
	@Override
	public OutputStream getOutputStream() throws FileNotFoundException {
		return new FileOutputStream(file);
	}
	
	@Override
	public long length() {
		return file.length();
	}
	
	@Override
	public String getCanonicalPath() throws IOException {
		return file.getCanonicalPath();
	}
	
	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}
	
	@Override
	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(file);
	}
}
