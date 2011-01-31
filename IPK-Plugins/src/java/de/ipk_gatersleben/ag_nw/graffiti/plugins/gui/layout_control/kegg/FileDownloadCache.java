/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 10.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.ErrorMsg;
import org.HomeFolder;

import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;

public class FileDownloadCache {
	
	/**
	 * If the parameter specifies a remote file, it is downloaded and saved at
	 * the hard disc drive. A URL to the downloaded file will be returned. In
	 * case of error (saving of file or download not possible), a error message
	 * is added to the error log and the given parameter is returned unchanged.
	 * If the caching file is existant, the file is not downloaded again.
	 * 
	 * @param url
	 *           The (remote) URL to the file.
	 * @return If possible, a local URL to a downloaded caching file.
	 */
	@SuppressWarnings("deprecation")
	public static URL getCacheURL(URL url, String fileID) {
		boolean useCache = true;
		if (!useCache)
			return url;
		else {
			String fileName = HomeFolder.getTemporaryFolderWithFinalSep() + "downloaded/" + url.toString().substring(url.toString().lastIndexOf("/") + 1);
			
			if (new File(fileName).canRead()) {
				try {
					return new File(fileName).toURL();
				} catch (MalformedURLException e) {
					ErrorMsg.addErrorMessage(e);
					return url;
				}
			} else {
				File cacheFile = new File(fileName);
				try {
					if (!downloadFile(url, cacheFile))
						return null;
					if (new File(fileName).canRead()) {
						try {
							return new File(fileName).toURL();
						} catch (MalformedURLException e) {
							ErrorMsg.addErrorMessage(e);
							return url;
						}
					} else
						return url;
				} catch (FileNotFoundException fnf) {
					// ignore for now
					return url;
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
					return url;
				}
			}
		}
	}
	
	private static boolean downloadFile(URL url, File targetFile)
						throws IOException {
		String path = HomeFolder.getTemporaryFolderWithFinalSep()
							+ "downloaded/";
		new File(path).mkdirs();
		new File(path).deleteOnExit();
		targetFile.deleteOnExit();
		if (url.getProtocol().equals("ftp"))
			return GUIhelper.performDownload(url.toString(), path, null);
		else {
			InputStream in = url.openStream();
			OutputStream out = new FileOutputStream(targetFile);
			
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			return true;
		}
	}
	
	public static boolean isCacheURL(URL url) {
		return (!url.toExternalForm().contains("http:/") && !url
							.toExternalForm().contains("ftp:/"));
	}
	
}
