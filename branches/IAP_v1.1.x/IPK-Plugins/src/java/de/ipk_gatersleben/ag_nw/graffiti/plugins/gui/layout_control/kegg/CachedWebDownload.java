/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 15.03.2007 by Christian Klukas
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
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;

public class CachedWebDownload {
	
	/**
	 * If the parameter specifies a remote file, it is downloaded and saved at the hard disc drive.
	 * A URL to the downloaded file will be returned. In case of error (saving of file or download not
	 * possible), a error message is added to the error log and the given parameter is returned unchanged.
	 * If the caching file is existant, the file is not downloaded again.
	 * 
	 * @param url
	 *           The (remote) URL to the file.
	 * @return If possible, a local URL to a downloaded caching file.
	 */
	@SuppressWarnings("deprecation")
	public static URL getCacheURL(URL url, String fileID, String filetype) {
		
		if (url != null && url.getProtocol().equalsIgnoreCase("file"))
			return url;
		
		String fileName = ReleaseInfo.getAppSubdirFolderWithFinalSep("cached_images") + "cached_" + filetype + "_" + fileID;
		
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
				downloadFile(url, cacheFile);
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
	
	private static void downloadFile(URL url, File targetFile) throws IOException {
		MainFrame.showMessage("Download " + url.toExternalForm() + "...", MessageType.INFO);
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
		MainFrame.showMessage("", MessageType.INFO);
	}
	
	public static boolean isCacheURL(URL url) {
		return (!url.toExternalForm().contains("http:/"));
	}
	
	public static String getFileIdFromUrl(String url) {
		String s = StringManipulationTools.stringReplace(url, ":", "_");
		s = StringManipulationTools.stringReplace(s, "/", "_");
		s = StringManipulationTools.stringReplace(s, "\\", "_");
		s = StringManipulationTools.stringReplace(s, "%", "_");
		s = StringManipulationTools.stringReplace(s, "?", "_");
		s = StringManipulationTools.stringReplace(s, "'", "_");
		s = StringManipulationTools.stringReplace(s, "\"", "_");
		return s;
	}
	
}
