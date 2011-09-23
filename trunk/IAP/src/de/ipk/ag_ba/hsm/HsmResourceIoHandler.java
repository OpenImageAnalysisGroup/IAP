/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 12, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.hsm;

import info.StopWatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class HsmResourceIoHandler extends AbstractResourceIOHandler {
	private final String prefix;
	private final String folder;
	
	// EXPERIMENT-XML-URL:
	// hsm_media_nfs_hsm://Phenotyping\ Experiment\ \(unknown\ greenhouse\)/LemnaTec\ \(APH\)/klukas/WT_H3/1303994908266_0_klukas_WT_H3.iap.vanted.bin
	// DATA-URL:
	// hsm_media_nfs_hsm://Maize\ Greenhouse\ Experiment/LemnaTec\ \(CGH\)/klukas/1107BA_Corn/data/2011-03-07/fluo.side\ DEG_000\ REPL_2\ 1107BA1009\ day_6\
	// 2011-03-07\ 09_16_59.png#original-Filename.png
	// ICON-URL:
	// hsm_media_nfs_hsm://Maize\ Greenhouse\ Experiment/LemnaTec\ \(CGH\)/klukas/1107BA_Corn/icons/2011-03-07/fluo.side\ DEG_000\ REPL_2\ 1107BA1009\ day_6\
	// 2011-03-07\ 09_16_59.png#original-Filename.png
	
	// for file I/O the "#original-Filename.png" needs to be removed
	
	public HsmResourceIoHandler(String folder) {
		if (folder.contains("_"))
			throw new UnsupportedOperationException("Invalid HSM folder! Folder must not contain the _ character: " + folder);
		this.folder = folder;
		this.prefix = getPrefix(folder);
	}
	
	public static String getPrefix(String folder) {
		return "hsm_" + StringManipulationTools.stringReplace(folder, File.separator, "_");
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream srcIS, String targetFilename, ResourceIOConfigObject config)
						throws Exception {
		throw new UnsupportedOperationException("Direct IO storage update not yet supported");
	}
	
	@Override
	public InputStream getInputStream(final IOurl url) throws Exception {
		String fn = url.getFileName();
		String path = url.getDetail().substring(url.getDetail().indexOf(File.separator) + File.separator.length());
		fn = folder + path + File.separator + fn.substring(0, fn.lastIndexOf("#"));
/*		if (!new File(fn).exists())
			System.out.println("Error: Can't find HSM file: " + fn);
			
*/		
		Semaphore lock = BackgroundTaskHelper.lockGetSemaphore("hsm", 4);
		StopWatch fw = new StopWatch("WAIT: "+url.getFileName()+"", false);
		lock.acquire();
		fw.printTime(500);
		StopWatch fr = new StopWatch(url.getFileName()+"", false);
		MyByteArrayInputStream res = ResourceIOManager.getInputStreamMemoryCached(new FileInputStream(new File(fn)));
		fr.printTime(500);
		lock.release();
		return res;
	}
	
	@Override
	public InputStream getPreviewInputStream(final IOurl url) throws Exception {
		String fn = url.getFileName();
		String path = url.getDetail().substring(url.getDetail().indexOf(File.separator) + File.separator.length());
		path = StringManipulationTools.stringReplace(path, File.separator + "data" + File.separator, File.separator + "icons" + File.separator);
		fn = folder + path + File.separator + fn.substring(0, fn.lastIndexOf("#"));
		if (!new File(fn).exists()) {
			final byte[] rrr = ((MyByteArrayInputStream) super.getPreviewInputStream(url)).getBuffTrimmed();
			return new MyByteArrayInputStream(rrr, rrr.length);
		} else {
			return new FileInputStream(new File(fn));
		}
	}
	
	@Override
	public String getPrefix() {
		return prefix;
	}
	
	@Override
	public Long getStreamLength(final IOurl url) throws Exception {
		String fn = url.getFileName();
		String path = url.getDetail().substring(url.getDetail().indexOf(File.separator) + File.separator.length());
		fn = folder + File.separator + path + File.separator + fn.substring(0, fn.lastIndexOf("#"));
		return new File(fn).length();
	}
	
}