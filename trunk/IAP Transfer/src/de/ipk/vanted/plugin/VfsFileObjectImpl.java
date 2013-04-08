package de.ipk.vanted.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.provider.AbstractFileObject;

public class VfsFileObjectImpl extends AbsractVfsFileObject {
	
	public VfsFileObjectImpl(FileObject file) {
		this.file = (AbstractFileObject) file;
	}
	
	private AbstractFileObject file = null;
	
	@Override
	public FileObject getFile() {
		return file;
	}
	
	public void setFile(FileObject file) {
		this.file = (AbstractFileObject) file;
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
		// for (int i = 0; i < 10; i++) {
		// try {
		// return file.getContent().getInputStream();
		// } catch (Exception e) {
		// e.printStackTrace();
		// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: COULD NOT GET INPUT STREAM ("
		// + e.getMessage() + "). TRYING AGAIN...");
		// try {
		// Thread.sleep(10000);
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// }
		// }
		// }
		return file.getContent().getInputStream();
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		// for (int i = 0; i < 10; i++) {
		// try {
		// return file.getContent().getOutputStream();
		// } catch (Exception e) {
		// e.printStackTrace();
		// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: COULD NOT GET OUTPUT STREAM ("
		// + e.getMessage() + "). TRYING AGAIN...");
		// try {
		// Thread.sleep(10000);
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// }
		// }
		// }
		return file.getContent().getOutputStream();
	}
	
	@Override
	public boolean isDirectory() throws FileSystemException {
		return file.getType().equals(FileType.FOLDER);
	}
	
	@Override
	public boolean isFile() throws FileSystemException {
		return file.getType().equals(FileType.FILE);
	}
	
	@Override
	public boolean isReadable() throws FileSystemException {
		return file.isReadable();
	}
	
	@Override
	public boolean isWriteable() throws FileSystemException {
		return file.isWriteable();
	}
	
	@Override
	public long length() throws FileSystemException {
		return file.getContent().getSize();
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
		throw new UnsupportedOperationException("ToDo");
	}
	
	@Override
	public void setWritable(boolean writeable) {
		throw new UnsupportedOperationException("ToDo");
	}
	
	@Override
	public void setLastModified(long time) throws Exception {
		Method m = file.getClass().getDeclaredMethod("doSetLastModifiedTime", long.class);
		m.setAccessible(true);
		m.invoke(file, time);
	}
	
	@Override
	public long getLastModified() throws Exception {
		Method m = file.getClass().getDeclaredMethod("doGetLastModifiedTime", (Class[]) null);
		m.setAccessible(true);
		Object o = m.invoke(file, (Object[]) null);
		return (Long) o;
	}
}
