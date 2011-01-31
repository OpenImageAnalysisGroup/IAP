package org.graffiti.plugin.io.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.HomeFolder;

public class FileSystemHandler extends AbstractResourceIOHandler {
	
	public static final String PREFIX = "file";
	
	public String getPrefix() {
		return PREFIX;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		if (url.isEqualPrefix(getPrefix()))
			return new BufferedInputStream(new FileInputStream(url.getDetail() + IOurl.SEPERATOR + url.getFileName()));
		else
			return null;
	}
	
	public static File getFile(IOurl url) {
		return new File(url.getDetail() + IOurl.SEPERATOR + url.getFileName());
	}
	
	public static boolean isFileUrl(IOurl url) {
		return PREFIX.equals(url.getPrefix() + "");
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config)
						throws Exception {
		IOurl newurl = new IOurl(getPrefix(), ((FileSystemIOConfig) config).getFileDir(), targetFilename);
		HomeFolder.copyFile(is, new File(targetFilename));
		return newurl;
	}
	
	public static IOurl getURL(File file) {
		return new IOurl(PREFIX, file.getParent(), file.getName());
	}
	
	@Override
	public IOurl saveAs(IOurl source, String targetFilename) throws Exception {
		if (source.getPrefix().equals(PREFIX)) {
			ResourceIOConfigObject config = new FileSystemIOConfig(source.getDetail());
			return copyDataAndReplaceURLPrefix(source.getInputStream(), targetFilename, config);
		} else
			throw new UnsupportedOperationException("Details are missing!");
	}
	
}
