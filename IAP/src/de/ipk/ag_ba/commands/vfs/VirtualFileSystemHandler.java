package de.ipk.ag_ba.commands.vfs;

import java.io.InputStream;

import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;

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
