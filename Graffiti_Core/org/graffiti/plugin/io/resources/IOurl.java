package org.graffiti.plugin.io.resources;

import java.io.InputStream;
import java.io.OutputStream;

import org.ErrorMsg;

public class IOurl {
	
	public static final String SEPERATOR = "/";
	
	private String prefix;
	private String detail;
	private String filename;
	
	public IOurl(String url) {
		if (url != null && url.indexOf("://") > 0) {
			prefix = url.substring(0, url.indexOf("://"));
			url = url.substring(url.indexOf("://") + "://".length());
		} else
			if (url != null && url.indexOf(":") > 0) {
				prefix = url.substring(0, url.indexOf(":"));
				url = url.substring(url.indexOf(":") + ":".length());
			} else
				prefix = FileSystemHandler.DEFAULT_PREFIX;
		if (url != null && url.lastIndexOf("/") > 0) {
			detail = url.substring(0, url.lastIndexOf(SEPERATOR));
			url = url.substring(url.lastIndexOf(SEPERATOR) + SEPERATOR.length());
		}
		filename = url;
	}
	
	public IOurl(String prefix, String filename) {
		this(prefix, null, filename);
	}
	
	public IOurl(String prefix, String detail, String filename) {
		this.prefix = prefix;
		this.detail = detail;
		this.filename = filename;
	}
	
	public IOurl(IOurl url) {
		this.prefix = url.prefix;
		this.detail = url.detail;
		this.filename = url.filename;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getDetail() {
		return detail;
	}
	
	public String getFileName() {
		return filename;
	}
	
	public String getFileNameWithoutExtension() {
		return getFileName().substring(0, getFileName().length() - getFileNameExtension().length());
	}
	
	@Override
	public String toString() {
		String s = "";
		if (getPrefix() != null)
			s += getPrefix() + "://";
		if (getDetail() != null)
			s += getDetail() + SEPERATOR;
		return s + getFileName();
	}
	
	public boolean isEqualPrefix(String prefix) {
		if (getPrefix() == null)
			return false;
		if (getPrefix() != null && getPrefix().contains("@"))
			return getPrefix().split("@", 2)[1].equals(prefix);
		else
			return getPrefix().equals(prefix);
	}
	
	public void setFileName(String filename) {
		this.filename = filename;
	}
	
	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public InputStream getInputStream() throws Exception {
		return ResourceIOManager.getInputStream(this);
	}
	
	public OutputStream getOutputStream() throws Exception {
		return ResourceIOManager.getOutputStream(this);
	}
	
	public String getFileNameDecoded() {
		try {
			String res = java.net.URLDecoder.decode(getFileName(), "UTF-8");
			return res;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return getFileName();
		}
	}
	
	public IOurl copy() {
		return new IOurl(prefix, detail, filename);
	}
	
	public String getFileNameExtension() {
		String fn = getFileName();
		if (fn == null)
			return null;
		if (fn.contains("#"))
			fn = fn.split("#")[0];
		if (fn.contains("."))
			fn = fn.substring(fn.lastIndexOf("."));
		return fn;
	}
	
	public boolean endsWith(String postfix) {
		return filename.endsWith(postfix);
	}
}
