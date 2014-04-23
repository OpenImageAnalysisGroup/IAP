package org.graffiti.plugin.io.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.zip.GZIPOutputStream;

import org.SystemAnalysis;

public class ResourceIOManager {
	
	private static ResourceIOManager instance;
	
	public synchronized static ResourceIOManager getInstance() {
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
	
	public synchronized static void registerIOHandler(ResourceIOHandler handler) {
		ResourceIOHandler mh = getHandlerFromPrefix(handler.getPrefix());
		if (mh != null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IO Handler with Prefix " + handler.getPrefix() + " is already registered.");
		} else
			getInstance().handlers.add(handler);
	}
	
	public synchronized static void removeIOHandler(ResourceIOHandler handler) {
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
		if (mh == null && url.getPrefix() != null && url.getPrefix().contains("@"))
			mh = getHandlerFromPrefix(url.getPrefix().split("@", 2)[1]);
		if (mh == null) {
			System.err.println("Could not get handler from URL " + url.toString() + "!");
			return null;
		} else {
			InputStream is = mh.getInputStream(url);
			if (is != null)
				return is;
			else {
				// System.err.println("Could not create inputstream from URL " + url.toString() + "!");
				return null;
			}
		}
	}
	
	static OutputStream getOutputStream(IOurl url) throws Exception {
		if (url == null) {
			System.err.println("Could not create outputstream from NULL url!");
			return null;
		}
		ResourceIOHandler mh = getHandlerFromPrefix(url.getPrefix());
		if (mh == null && url.getPrefix() != null && url.getPrefix().contains("@"))
			mh = getHandlerFromPrefix(url.getPrefix().split("@", 2)[1]);
		if (mh == null) {
			System.err.println("Could not get handler from URL " + url.toString() + "!");
			return null;
		} else {
			OutputStream os = mh.getOutputStream(url);
			if (os != null)
				return os;
			else {
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
			// System.out.print(".");
			return new MyByteArrayInputStream(bos.toByteArray(), bos.size());
		}
	}
	
	public static MyByteArrayInputStream getInputStreamMemoryCached(InputStream is) throws IOException, Exception {
		if (is != null && is instanceof MyByteArrayInputStream)
			return (MyByteArrayInputStream) is;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ResourceIOManager.copyContent(is, bos);
		return new MyByteArrayInputStream(bos.toByteArray(), bos.size());
	}
	
	public static long copyContent(InputStream intemp, OutputStream out) throws IOException {
		return copyContent(intemp, out, -1);
	}
	
	public static long copyContent(InputStream in, OutputStream out, long maxIO) throws IOException {
		long written = 0;
		try {
			if (maxIO <= 0) {
				byte[] buffer = new byte[0x7FFF];
				for (int len; (len = in.read(buffer)) != -1;) {
					out.write(buffer, 0, len);
					written += len;
				}
			} else {
				long read = 0;
				byte[] buffer = new byte[0x7FFF];
				for (int len; (len = in.read(buffer)) != -1;) {
					read += len;
					if (read < maxIO) {
						out.write(buffer, 0, len);
						written += len;
					} else {
						read -= len;
						out.write(buffer, 0, (int) (maxIO - read));
						written += maxIO - read;
						break;
					}
				}
			}
		} finally {
			try {
				in.close();
			} finally {
				out.close();
			}
		}
		return written;
	}
	
	public synchronized static ResourceIOHandler getHandlerFromPrefix(String prefix) {
		for (ResourceIOHandler mh : getInstance().handlers)
			if (mh.getPrefix().startsWith(prefix))
				return mh;
		return null;
	}
	
	public static byte[] getPreviewImageContent(IOurl ioUrl) throws Exception {
		MyByteArrayOutputStream output = new MyByteArrayOutputStream();
		ResourceIOHandler handler = getHandlerFromPrefix(ioUrl.getPrefix());
		if (handler == null) {
			System.out.println("INFO: No handler for IO-URL: " + ioUrl);
			output.close();
			return null;
		}
		InputStream in = handler.getPreviewInputStream(ioUrl);
		if (in == null) {
			output.close();
			return null;
		}
		copyContent(in, output);
		byte[] imageContent = output.getBuffTrimmed();
		return imageContent;
	}
	
	public static InputStream getCompressedInputStream(MyByteArrayInputStream is) throws IOException, Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream gzipOut = new GZIPOutputStream(bos);
		copyContent(is, gzipOut);
		int len1 = is.getCount();
		int len2 = bos.size();
		int saved = (int) ((len1 - len2) / 1024d / 1024d);
		if (saved > 0) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Compress stream: " + (int) (100d * len2 / len2) + "% (saved " + saved + " MB)");
			return new MyByteArrayInputStream(bos.toByteArray(), bos.size());
		} else {
			saved = ((len1 - len2));
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Compressed stream is not smaller, skipping (" + (int) (100d * len2 / len2) + "%, "
					+ saved + " bytes difference)");
			is.reset();
			return is;
		}
	}
	
	public synchronized LinkedHashSet<ResourceIOHandler> getHandlers() {
		return new LinkedHashSet<ResourceIOHandler>(handlers);
	}
}
