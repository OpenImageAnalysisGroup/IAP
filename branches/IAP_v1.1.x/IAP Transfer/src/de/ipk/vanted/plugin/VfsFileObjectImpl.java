package de.ipk.vanted.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.http.HttpFileSystem;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.jsoup.Jsoup;

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
		if (!file.getFileSystem().hasCapability(Capability.LIST_CHILDREN)) {
			if (file.getFileSystem().getClass().isAssignableFrom(HttpFileSystem.class)) {
				return httpListFiles(false);
			}
		}
		FileObject[] fs = file.getChildren();
		String[] files = new String[fs.length];
		int index = 0;
		for (FileObject obj : fs) {
			files[index++] = obj.getName().getBaseName();
		}
		return files;
	}
	
	@Override
	public String[] listFolders() throws IOException {
		if (!file.getFileSystem().hasCapability(Capability.LIST_CHILDREN)) {
			if (file.getFileSystem().getClass().isAssignableFrom(HttpFileSystem.class)) {
				return httpListFiles(true);
			}
		}
		FileObject[] fs = file.getChildren();
		ArrayList<String> directories = new ArrayList<String>();
		for (FileObject obj : fs) {
			if (obj.getType() == FileType.FOLDER)
				directories.add(obj.getName().getBaseName());
		}
		return directories.toArray(new String[] {});
	}
	
	private String[] httpListFiles(boolean onlyFoldersTrue_onlyFilesFalse) throws FileSystemException, IOException {
		String uri = file.getName().getURI();
		if (!uri.endsWith("/")) {
			uri = uri + "/";
		}
		InputStream inputStream = file.getInputStream();
		org.jsoup.nodes.Document doc = null;
		try {
			String urlCharset = HttpFileSystemConfigBuilder.getInstance().getUrlCharset(file.getFileSystem().getFileSystemOptions());
			doc = Jsoup.parse(inputStream, urlCharset, uri);
		} finally {
			inputStream.close();
		}
		org.jsoup.select.Elements links = doc.select("a");
		List<String> urls = new ArrayList<String>();
		for (org.jsoup.nodes.Element link : links) {
			String url = link.attr("abs:href");
			// not a child
			if (!url.contains(uri)) {
				continue;
			}
			String relativeUrl = link.attr("href").trim();
			// remove parameters
			int indexOfParam = relativeUrl.indexOf('?');
			if (indexOfParam != -1) {
				relativeUrl = relativeUrl.substring(0, indexOfParam);
			}
			// skip references to root or empty
			if ("/".equals(relativeUrl) || relativeUrl.isEmpty()) {
				continue;
			}
			if (onlyFoldersTrue_onlyFilesFalse && !relativeUrl.endsWith("/"))
				continue;
			if (!onlyFoldersTrue_onlyFilesFalse && relativeUrl.endsWith("/"))
				continue;
			int indexOfSlash = relativeUrl.indexOf('/');
			
			if (indexOfSlash > 0) {
				// forbid adding subfolders
				relativeUrl = relativeUrl.substring(0, indexOfSlash);
			}
			String decoded;
			try {
				decoded = decode(relativeUrl.toCharArray(), "UTF-8");
			} catch (Exception e) {
				throw new IOException(e);
			}
			urls.add(decoded);
		}
		return urls.toArray(new String[urls.size()]);
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
		try {
			Method m = file.getClass().getDeclaredMethod("doSetLastModifiedTime", long.class);
			m.setAccessible(true);
			m.invoke(file, time);
		} catch (NoSuchMethodException nsme) {
			// empty
		}
	}
	
	@Override
	public long getLastModified() throws Exception {
		try {
			Method m = file.getClass().getDeclaredMethod("doGetLastModifiedTime", (Class[]) null);
			m.setAccessible(true);
			Object o = m.invoke(file, (Object[]) null);
			return (Long) o;
		} catch (NoSuchMethodException nsme) {
			return 0;
		}
	}
	
	protected static String decode(char[] component, String charset) throws Exception {
		byte[] oct = new String(component).getBytes(charset);
		
		int length = oct.length;
		int oi = 0;
		for (int ii = 0; ii < length; oi++) {
			byte aByte = (byte) oct[ii++];
			if (aByte == '%' && ii + 2 <= length) {
				byte high = (byte) Character.digit((char) oct[ii++], 16);
				byte low = (byte) Character.digit((char) oct[ii++], 16);
				aByte = (byte) ((high << 4) + low);
			}
			oct[oi] = (byte) aByte;
		}
		
		return new String(oct, 0, oi, charset);
	}
}
