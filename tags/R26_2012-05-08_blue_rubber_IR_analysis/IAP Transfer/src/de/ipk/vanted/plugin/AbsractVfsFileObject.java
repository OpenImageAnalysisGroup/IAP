package de.ipk.vanted.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;

public abstract class AbsractVfsFileObject implements VfsFileObject {

	@Override
	public boolean delete() throws IOException {
		return false;
	}

	@Override
	public void download(VfsFileObject localFile) throws IOException {

	}

	@Override
	public boolean exists() throws IOException {
		return false;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public boolean isWriteable() {
		return false;
	}

	@Override
	public long length() {
		return -1;
	}

	@Override
	public String[] list() throws IOException {
		return null;
	}

	@Override
	public boolean mkdir() throws IOException {
		return false;
	}

	@Override
	public void renameTo(VfsFileObject target, boolean overWrite)
			throws IOException {

	}

	@Override
	public void upload(VfsFileObject remoteFile) throws IOException {

	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public URL getURL() throws IOException {
		return null;
	}

	public FileObject getFile() {
		return null;
	}
}
