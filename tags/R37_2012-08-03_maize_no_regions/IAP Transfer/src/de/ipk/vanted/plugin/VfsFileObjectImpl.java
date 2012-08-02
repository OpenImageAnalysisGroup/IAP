package de.ipk.vanted.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;

public class VfsFileObjectImpl extends AbsractVfsFileObject {
	
	public VfsFileObjectImpl(FileObject file) {
		this.file = file;
	}
	
	private FileObject file = null;
	
	public FileObject getFile() {
		return file;
	}
	
	public void setFile(FileObject file) {
		this.file = file;
	}
	
	@Override
	public boolean delete() throws IOException {
		// the number of deleted objects
		int num = file.delete(Selectors.SELECT_SELF_AND_CHILDREN);
		if (num > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void download(VfsFileObject localFile) throws IOException {
		FileObject local = localFile.getFile();
		local.copyFrom(file, Selectors.SELECT_SELF);
	}
	
	@Override
	public boolean exists() throws IOException {
		return file.exists();
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return file.getContent().getInputStream();
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		return file.getContent().getOutputStream();
	}
	
	@Override
	public boolean isDirectory() {
		try {
			return file.getType().equals(FileType.FOLDER);
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean isFile() {
		try {
			return file.getType().equals(FileType.FILE);
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean isReadable() {
		try {
			return file.isReadable();
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean isWriteable() {
		try {
			return file.isWriteable();
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public long length() {
		try {
			return file.getContent().getSize();
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	@Override
	public String[] list() throws IOException {
		FileObject[] fs = file.getChildren();
		String[] files = new String[fs.length];
		int index = 0;
		for (FileObject obj : fs) {
			files[index++] = obj.getName().getBaseName();
		}
		return files;
	}
	
	@Override
	public boolean mkdir() throws IOException {
		if (!file.exists()) {
			file.createFolder();
			return true;
		}
		return false;
	}
	
	@Override
	public void renameTo(VfsFileObject target, boolean overWrite)
			throws IOException {
		FileObject to = target.getFile();
		if (to.exists()) {
			if (to.getType() == FileType.FILE) {
				if (overWrite && !to.delete()) {
					throw new IOException("Permission denied! Target file "
							+ to.getName().getBaseName() + " exists!");
				} else
					if (!overWrite) {
						throw new IOException("Target file "
								+ to.getName().getBaseName() + " exists!");
					}
			}
		}
		file.moveTo(to);
	}
	
	@Override
	public void upload(VfsFileObject remoteFile) throws IOException {
		FileObject remote = remoteFile.getFile();
		remote.copyFrom(file, Selectors.SELECT_SELF);
	}
	
	@Override
	public String getName() {
		return file.getName().getBaseName();
	}
	
	@Override
	public URL getURL() throws IOException {
		return file.getURL();
	}
	
	@Override
	public void setExecutable(boolean executable) {
		// todo
	}
	
	@Override
	public void setWritable(boolean writeable) {
		// file.getFileSystem().setAttribute(arg0, arg1);
	}
	
	@Override
	public void setLastModified(long time) {
		// todo
	}
}
