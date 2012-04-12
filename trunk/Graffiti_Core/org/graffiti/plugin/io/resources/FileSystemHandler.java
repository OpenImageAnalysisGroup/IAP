package org.graffiti.plugin.io.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;

import org.HomeFolder;
import org.StringManipulationTools;

import sun.net.spi.DefaultProxySelector;

public class FileSystemHandler extends AbstractResourceIOHandler {
	
	public static final String DEFAULT_PREFIX = "file";
	private String prefix;
	private String folder;
	
	public FileSystemHandler() {
		this(DEFAULT_PREFIX, null);
	}

	public FileSystemHandler(String prefix, String folder) {
		this.prefix = prefix;
		this.folder = folder;
	}

	public String getPrefix() {
		return prefix;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		if (url.isEqualPrefix(getPrefix())) {
			File file = new File(
					(folder!=null ? folder+IOurl.SEPERATOR : "") +
					url.getDetail()
							+ IOurl.SEPERATOR + filter(url.getFileName()));
			if (file.exists()) {
				FileInputStream fis = new FileInputStream(file);
				return new BufferedInputStream(fis);
			} else {
				String decoded = (folder!=null ? folder+IOurl.SEPERATOR : "") +URLDecoder.decode(url.getDetail() + IOurl.SEPERATOR + filter(url.getFileName()));
				file = new File(
						decoded
						);
				if (file.exists()) {
					FileInputStream fis = new FileInputStream(file);
					return fis;
				} else {
					String fn = url.getDetail() + IOurl.SEPERATOR + url.getFileName();
					fn = fn.substring(fn.indexOf("!") + 1);
					return getClass().getResourceAsStream(fn);
				}
			}
		} else
			return null;
	}
	
	private String filter(String fileName) {
		if (folder==null)
			return fileName;
		else {
			if (fileName.indexOf("#")>=0)
				fileName = fileName.substring(0, fileName.lastIndexOf("#"));
			return fileName;
		}
	}
	
	@Override
	public InputStream getPreviewInputStream(final IOurl url) throws Exception {
		if (folder==null)
			return super.getPreviewInputStream(url);
		else {
			String fn = url.getFileName();
			String path = url.getDetail().substring(url.getDetail().indexOf(File.separator) + File.separator.length());
			if (path.indexOf(File.separator + "data" + File.separator)>=0)
				path = StringManipulationTools.stringReplace(path, File.separator + "data" + File.separator, File.separator + "icons" + File.separator);
			else
				path = "icons"+File.separator+path;
			fn = folder +File.separator+ path + File.separator + fn.substring(0, fn.lastIndexOf("#"));
			if (!new File(fn).exists()) {
				final byte[] rrr = ((MyByteArrayInputStream) super.getPreviewInputStream(url)).getBuffTrimmed();
				return new MyByteArrayInputStream(rrr, rrr.length);
			} else {
				return new FileInputStream(new File(fn));
			}
		}
	}

	public static File getFile(IOurl url) {
		return new File(
				URLDecoder.decode(
						
						url.getDetail()
						) + IOurl.SEPERATOR + url.getFileName());
	}
	
	public static boolean isFileUrl(IOurl url) {
		return DEFAULT_PREFIX.equals(url.getPrefix() + "");
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config)
						throws Exception {
		IOurl newurl = new IOurl(getPrefix(), ((FileSystemIOConfig) config).getFileDir(), targetFilename);
		HomeFolder.copyFile(is, new File(targetFilename));
		return newurl;
	}
	
	public static IOurl getURL(File file) {
		return new IOurl(DEFAULT_PREFIX, file.getParent(), file.getName());
	}
	
	@Override
	public IOurl saveAs(IOurl source, String targetFilename) throws Exception {
		if (source.getPrefix().equals(DEFAULT_PREFIX)) {
			ResourceIOConfigObject config = new FileSystemIOConfig(source.getDetail());
			return copyDataAndReplaceURLPrefix(source.getInputStream(), targetFilename, config);
		} else
			throw new UnsupportedOperationException("Details are missing!");
	}
	
	@Override
	public Long getStreamLength(IOurl url) {
		File f = getFile(url);
		if (f == null || !f.exists())
			return null;
		else
			return f.length();
	}
	
}
