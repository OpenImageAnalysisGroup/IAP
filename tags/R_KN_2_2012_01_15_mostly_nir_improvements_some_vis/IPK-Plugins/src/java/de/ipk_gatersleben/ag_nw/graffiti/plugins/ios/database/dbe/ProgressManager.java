/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.database.dbe;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.StringManipulationTools;

public class ProgressManager {
	File progressFile = null;
	String cacheFileName = null;
	String directory = null;
	String cacheFileNameWithoutPath = null;
	
	public synchronized void init(String cacheFileName, String directory, String cacheFileNameWithoutPath) {
		this.cacheFileName = cacheFileName;
		this.directory = directory;
		this.cacheFileNameWithoutPath = cacheFileNameWithoutPath;
		progressFile = new File(cacheFileName + ".progress.INIT");
	}
	
	public synchronized String getCurrentStatus() {
		if (progressFile == null)
			return "ERROR";
		if (new File(cacheFileName).exists())
			return "READY";
		File dir = new File(directory);
		String[] progressFiles = dir.list(new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				return fileName.startsWith(cacheFileNameWithoutPath);
			}
		});
		String result = "DOREQUEST";
		for (String fn : progressFiles) {
			result = fn.substring(fn.indexOf(cacheFileNameWithoutPath) + ".progress.".length() + cacheFileNameWithoutPath.length());
		}
		result = StringManipulationTools.htmlToUnicode(result);
		return result;
	}
	
	public synchronized void setStatus(int progress, String activity) {
		if (!progressFile.exists()) {
			try {
				progressFile.createNewFile();
				progressFile.deleteOnExit();
			} catch (IOException e) {
				// ErrorMsg.addErrorMessage(e);
			}
			progressFile.deleteOnExit();
		}
		if (progressFile.exists()) {
			activity = StringManipulationTools.UnicodeToHtml(activity);
			File nf = new File(cacheFileName + ".progress." + progress + "." + activity);
			nf.deleteOnExit();
			progressFile.renameTo(nf);
			progressFile = nf;
		}
	}
	
}
