package org.graffiti.plugin.io.resources;

import java.io.InputStream;
import java.io.OutputStream;

public interface ResourceIOHandler {
	
	public String getPrefix();
	
	public InputStream getInputStream(IOurl url) throws Exception;
	
	public InputStream getPreviewInputStream(IOurl url) throws Exception;
	
	public InputStream getPreviewInputStream(IOurl url, int size) throws Exception;
	
	/**
	 * @return new url, or null, if not copied
	 * @throws Exception
	 *            if something went wrong during copying
	 */
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config) throws Exception;
	
	public OutputStream getOutputStream(IOurl targetFilename) throws Exception;
	
	/**
	 * @param url
	 *           IOurl
	 * @return The size of the resource, or Null if size can't be determined.
	 */
	public Long getStreamLength(IOurl url) throws Exception;
}
