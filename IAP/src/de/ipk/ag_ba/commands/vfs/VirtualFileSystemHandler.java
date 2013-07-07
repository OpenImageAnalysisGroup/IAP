package de.ipk.ag_ba.commands.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;

import de.ipk.vanted.plugin.VfsFileObject;

public class VirtualFileSystemHandler extends AbstractResourceIOHandler {
	
	private final VirtualFileSystem vfs;
	
	public VirtualFileSystemHandler(VirtualFileSystem vfs) {
		this.vfs = vfs;
	}
	
	@Override
	public String getPrefix() {
		return vfs.getPrefix();
	}
	
	@Override
	public OutputStream getOutputStream(IOurl targetFilename) throws Exception {
		VfsFileObject fo = vfs.getFileObjectFor(targetFilename.getFileName());
		if (!fo.isWriteable())
			throw new IOException("Target file is not writable");
		return fo.getOutputStream();
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		return vfs.getInputStream(url);
	}
	
	@Override
	public InputStream getPreviewInputStream(IOurl url) throws Exception {
		return vfs.getPreviewInputStream(url);
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config) throws Exception {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@Override
	public Long getStreamLength(IOurl url) throws Exception {
		return vfs.getFileLength(url);
	}
	
	public VirtualFileSystem getVFS() {
		return vfs;
	}
}
