package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import org.ReleaseInfo;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.mongo.MongoDBhandler;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class DownloadCache {
	
	private static HashMap<String, ArrayList<File>> plant2cacheFiles = new HashMap<String, ArrayList<File>>();
	
	private static DownloadCache instance;
	
	private DownloadCache() {
		
	}
	
	public synchronized static DownloadCache getInstance() {
		if (instance == null)
			instance = new DownloadCache();
		return instance;
	}
	
	public synchronized void downloadSnapshots(String plantID, Collection<TreeMap<Long, TreeMap<String, ImageSet>>> values) {
		System.out.print(">Create cache for plant ID " + plantID + "");
		for (TreeMap<Long, TreeMap<String, ImageSet>> vvv : values) {
			for (TreeMap<String, ImageSet> vv : vvv.values()) {
				for (ImageSet is : vv.values()) {
					for (ImageData id : new ImageData[] { is.getVIS(), is.getFLUO(), is.getNIR(), is.getIR() }) {
						if (id == null || id.getURL() == null || id.getURL().getDetail() == null)
							continue;
						try {
							MongoDBhandler h = (MongoDBhandler) ResourceIOManager.getHandlerFromPrefix(id.getURL().getPrefix());
							InputStream inp = h.getInputStream(id.getURL());
							File cf = new File(ReleaseInfo.getAppSubdirFolderWithFinalSep("cache") + id.getURL().getDetail());
							if (!cf.exists())
								ResourceIOManager.copyContent(inp, new FileOutputStream(cf));
							if (!plant2cacheFiles.containsKey(plantID))
								plant2cacheFiles.put(plantID, new ArrayList<File>());
							plant2cacheFiles.get(plantID).add(cf);
						} catch (Exception e) {
							// empty
						}
					}
				}
			}
		}
	}
	
	public synchronized void finished(String plantID) {
		if (plant2cacheFiles != null)
			System.out.print(">Remove cached files for plant ID " + plantID + "");
		if (plant2cacheFiles != null && plantID != null)
			for (File f : plant2cacheFiles.get(plantID))
				f.delete();
		if (plant2cacheFiles != null)
			plant2cacheFiles.remove(plantID);
	}
	
	public synchronized InputStream getFileInputStream(String detail) throws Exception {
		File cf = new File(ReleaseInfo.getAppSubdirFolderWithFinalSep("cache") + detail);
		if (cf.exists()) {
			return ResourceIOManager.getInputStreamMemoryCached(new FileInputStream(cf));
		} else
			return null;
	}
	
}
