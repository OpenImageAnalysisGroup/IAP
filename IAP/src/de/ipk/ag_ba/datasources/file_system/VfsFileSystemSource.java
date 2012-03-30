/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.file_system;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.Library;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class VfsFileSystemSource extends HsmFileSystemSource {
	
	protected final VirtualFileSystem url;
	private final String[] validExtensions2;
	
	public VfsFileSystemSource(Library lib, String dataSourceName, VirtualFileSystem folder,
			String[] validExtensions,
			NavigationImage mainDataSourceIcon, NavigationImage folderIcon) {
		super(lib, dataSourceName,
				((VirtualFileSystemFolderStorage) folder).getTargetPathName(),
				mainDataSourceIcon, folderIcon);
		this.url = folder;
		validExtensions2 = validExtensions;
	}
	
	@Override
	public void readDataSource() throws Exception {
		this.read = true;
		this.mainList = new ArrayList<PathwayWebLinkItem>();
		// read index folder
		String[] entries = url.listFiles(HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".iap.index.csv");
			}
		});
		
		HashMap<String, TreeMap<Long, ExperimentHeaderInterface>> experimentName2saveTime2data =
				new HashMap<String, TreeMap<Long, ExperimentHeaderInterface>>();
		
		if (entries != null)
			for (String fileName : entries) {
				long saveTime = Long.parseLong(fileName.substring(0, fileName.indexOf("_")));
				
				ExperimentHeader eh = getHSMexperimentHeaderFromFileName(url, fileName);
				
				if (accessOK(eh)) {
					String experimentName = eh.getExperimentName();
					if (!experimentName2saveTime2data.containsKey(experimentName))
						experimentName2saveTime2data.put(experimentName, new TreeMap<Long, ExperimentHeaderInterface>());
					experimentName2saveTime2data.get(experimentName).put(saveTime, eh);
					eh.addHistoryItems(experimentName2saveTime2data.get(experimentName));
				}
			}
		
		this.thisLevel = new HsmMainDataSourceLevel(experimentName2saveTime2data);
		((HsmMainDataSourceLevel) thisLevel).setHsmFileSystemSource(this);
	}
	
	protected ExperimentHeader getHSMexperimentHeaderFromFileName(VirtualFileSystem url, String fileName) throws Exception {
		HashMap<String, String> properties = new HashMap<String, String>();
		IOurl ioUrl = url.getIOurlFor(HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME + File.separator + fileName);
		InputStream is = ioUrl.getInputStream();
		TextFile tf = new TextFile(is, -1);
		properties.put("_id", url.getPrefix() + ":" + HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME + File.separator + fileName);
		for (String p : tf) {
			String[] entry = p.split(",", 3);
			properties.put(entry[1], entry[2]);
		}
		ExperimentHeader eh = new ExperimentHeader(properties);
		return eh;
	}
	
	@Override
	public ExperimentInterface getExperiment(ExperimentHeaderInterface header, boolean interactiveGetExperimentSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		String hsmFolder = header.getDatabaseId();
		String fileName = hsmFolder.substring(hsmFolder.lastIndexOf(File.separator) + File.separator.length());
		
		hsmFolder = hsmFolder.substring((url.getPrefix() + ":").length());
		hsmFolder = hsmFolder.substring(HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME.length());
		
		HSMfolderTargetDataManager hsm = new HSMfolderTargetDataManager(hsmFolder);
		String experimentDirectory =
				HSMfolderTargetDataManager.DATA_FOLDER_NAME + File.separator +
						hsm.getTargetDirectory(header, null);
		String fileNameOfExperimentFile = fileName.substring(0, fileName.length() - ".iap.index.csv".length()) + ".iap.vanted.bin";
		
		IOurl u = url.getIOurlFor(experimentDirectory + File.separator + fileNameOfExperimentFile);
		ExperimentInterface md = Experiment.loadFromIOurl(u);
		
		return md;
	}
	
	@Override
	public boolean canHandle(String databaseId) {
		return databaseId.startsWith(url.getPrefix() + ":");
	}
}
