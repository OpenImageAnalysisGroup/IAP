package org.graffiti.plugin.io.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;

public class ResourceIOManager {
	
	private static ResourceIOManager instance;
	
	private static ResourceIOManager getInstance() {
		if (instance == null) {
			instance = new ResourceIOManager();
			registerIOHandler(new FileSystemHandler());
			registerIOHandler(new HTTPhandler());
			registerIOHandler(new FTPhandler());
		}
		return instance;
	}
	
	LinkedHashSet<ResourceIOHandler> handlers;
	public static final String SEPERATOR = "||";
	
	private ResourceIOManager() {
		super();
		handlers = new LinkedHashSet<ResourceIOHandler>();
	}
	
	public static void registerIOHandler(ResourceIOHandler handler) {
		ResourceIOHandler mh = getHandlerFromPrefix(handler.getPrefix());
		if (mh != null) {
			System.err.println("IO Handler with Prefix " + handler.getPrefix() + " can't be registered more than once!");
		} else
			getInstance().handlers.add(handler);
	}
	
	public static void removeIOHandler(ResourceIOHandler handler) {
		getInstance().handlers.remove(handler);
	}
	
	/**
	 * Use {@link IOurl}.getInputStream instead.
	 * 
	 * @return inputstream of file or null, if no handler found.
	 */
	static InputStream getInputStream(IOurl url) throws Exception {
		if (url == null) {
			System.err.println("Could not create inputstream from NULL url!");
			return null;
		}
		ResourceIOHandler mh = getHandlerFromPrefix(url.getPrefix());
		if (mh == null) {
			System.err.println("Could not get handler from URL " + url.toString() + "!");
			return null;
		} else {
			InputStream is = mh.getInputStream(url);
			if (is != null)
				return is;
			else {
				//System.err.println("Could not create inputstream from URL " + url.toString() + "!");
				return null;
			}
		}
	}
	
	/**
	 * @return new url or null, if not copied
	 */
	public static IOurl copyDataAndReplaceURLPrefix(String targetHandlerPrefix, IOurl sourceURL,
						ResourceIOConfigObject config) throws Exception {
		if (sourceURL == null)
			return null;
		
		InputStream is = getInputStream(sourceURL);
		String filename = sourceURL.getFileName();
		
		return copyDataAndReplaceURLPrefix(targetHandlerPrefix, filename, is, config);
	}
	
	public static IOurl copyDataAndReplaceURLPrefix(String targetHandlerPrefix, String srcFileName, InputStream is,
						ResourceIOConfigObject config) throws Exception {
		if (is == null || srcFileName == null)
			return null;
		
		ResourceIOHandler mh = getHandlerFromPrefix(targetHandlerPrefix);
		if (mh == null)
			return null;
		else
			return mh.copyDataAndReplaceURLPrefix(is, srcFileName, config);
	}
	
	public static MyByteArrayInputStream getInputStreamMemoryCached(IOurl url) throws IOException, Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			is = url != null ? url.getInputStream() : null;
		} catch (Exception e) {
			System.out.println("IO-ERROR " + e.getMessage() + " FOR URL " + url);
		}
		if (is == null)
			return null;
		if (is instanceof MyByteArrayInputStream) {
			return (MyByteArrayInputStream) is;
		} else {
			ResourceIOManager.copyContent(is, bos);
			System.out.print(".");
			return new MyByteArrayInputStream(bos.toByteArray(), bos.size());
		}
	}
	
	public static MyByteArrayInputStream getInputStreamMemoryCached(InputStream is) throws IOException, Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ResourceIOManager.copyContent(is, bos);
		return new MyByteArrayInputStream(bos.toByteArray(), bos.size());
	}
	
	public static void copyContent(InputStream intemp, OutputStream out) throws IOException {
		copyContent(intemp, out, -1);
	}
	
	public static void copyContent(InputStream in, OutputStream out, long maxIO) throws IOException {
		
		if (maxIO <= 0) {
			byte[] buffer = new byte[0xFFFF];
			for (int len; (len = in.read(buffer)) != -1;) {
				out.write(buffer, 0, len);
			}
		} else {
			long read = 0;
			byte[] buffer = new byte[0xFFFF];
			for (int len; (len = in.read(buffer)) != -1;) {
				read += len;
				if (read < maxIO)
					out.write(buffer, 0, len);
				else {
					read -= len;
					out.write(buffer, 0, (int) (maxIO - read));
					break;
				}
			}
		}
		
		in.close();
		out.close();
	}
	
	public static ResourceIOHandler getHandlerFromPrefix(String prefix) {
		for (ResourceIOHandler mh : getInstance().handlers)
			if (mh.getPrefix().startsWith(prefix))
				return mh;
		
//		if (prefix.contains("_"))
//			return getHandlerFromPrefix(prefix.substring(0, prefix.indexOf("_")));
//		else
			return null;
	}
	
	public static byte[] getPreviewImageContent(IOurl ioUrl) throws Exception {
		MyByteArrayOutputStream output = new MyByteArrayOutputStream();
		ResourceIOHandler handler = getHandlerFromPrefix(ioUrl.getPrefix());
		if (handler == null) {
			System.out.println("INFO: No handler for IO-URL: " + ioUrl);
			return null;
		}
		InputStream in = handler.getPreviewInputStream(ioUrl);
		if (in == null)
			return null;
		copyContent(in, output);
		byte[] imageContent = output.getBuffTrimmed();
		return imageContent;
	}
}
