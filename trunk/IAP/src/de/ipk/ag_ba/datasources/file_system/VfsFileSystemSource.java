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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ExperimentHeaderHelper;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.experiment.hsm.ActionDataExportToHsmFolder;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.io_handler.hsm.HsmResourceIoHandler;
import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class VfsFileSystemSource extends HsmFileSystemSource {
	
	protected final VirtualFileSystem url;
	private final String[] validExtensions2;
	
	public VfsFileSystemSource(Library lib, String dataSourceName, VirtualFileSystem folder,
			String[] validExtensions,
			NavigationImage mainDataSourceIcon, NavigationImage mainDataSourceIconActive,
			NavigationImage folderIcon) {
		super(lib, dataSourceName,
				(folder instanceof VirtualFileSystemFolderStorage ?
						((VirtualFileSystemFolderStorage) folder).getTargetPathName() : null),
				mainDataSourceIcon, mainDataSourceIcon, folderIcon);
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
				
				ExperimentHeader eh = getExperimentHeaderFromFileName(url, fileName);
				
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
	
	public ExperimentHeader getExperimentHeaderFromFileName(final VirtualFileSystem vfs, final String fileName) throws Exception {
		final ExperimentHeader eh = new ExperimentHeader();
		
		eh.setDatabaseId(vfs.getPrefix() + ":index" + File.separator + fileName);
		
		final String prefix = vfs.getPrefix();
		
		final VfsFileObject indexFile = vfs.getFileObjectFor(HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME + File.separator + fileName);
		ExperimentHeaderHelper ehh = new ExperimentHeaderHelper() {
			
			@Override
			public void readSourceForUpdate() throws Exception {
				System.out.println(SystemAnalysis.getCurrentTime() + ">Get current header from file (" + fileName + ")");
				HashMap<String, String> properties = new HashMap<String, String>();
				InputStream is = indexFile.getInputStream();
				TextFile tf = new TextFile(is, -1);
				properties.put("_id", prefix + ":" + HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME + File.separator + fileName);
				String lastItemId = null;
				for (String p : tf) {
					if (StringManipulationTools.count(p, ",") >= 2) {
						String[] entry = p.split(",", 3);
						properties.put(entry[1], entry[2]);
						lastItemId = entry[1];
					} else
						if (lastItemId != null) {
							properties.put(lastItemId,
									properties.get(lastItemId)
											+ System.getProperty("line.separator")
											+ p);
						}
				}
				eh.setAttributesFromMap(properties);
			}
			
			@Override
			public Long getLastModified() throws Exception {
				return indexFile.getLastModified();
			}
			
			@Override
			public Long saveUpdatedProperties() throws Exception {
				System.out.println(SystemAnalysis.getCurrentTime() + ">Save updated header information in " + indexFile.getName());
				ActionDataExportToHsmFolder.writeExperimentHeaderToIndexFile(eh, indexFile.getOutputStream(), -1);
				return indexFile.getLastModified();
			}
		};
		
		ehh.readSourceForUpdate();
		
		eh.setExperimentHeaderHelper(ehh);
		
		return eh;
	}
	
	@Override
	public ExperimentInterface getExperiment(ExperimentHeaderInterface header, boolean interactiveGetExperimentSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		String hsmFolder = header.getDatabaseId();
		String fileName = hsmFolder.substring(hsmFolder.lastIndexOf(File.separator) + File.separator.length());
		
		hsmFolder = hsmFolder.substring((url.getPrefix() + ":").length());
		hsmFolder = hsmFolder.substring(HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME.length());
		
		HSMfolderTargetDataManager hsm = new HSMfolderTargetDataManager(
				HsmResourceIoHandler.getPrefix(hsmFolder), hsmFolder);
		String experimentDirectory =
				HSMfolderTargetDataManager.DATA_FOLDER_NAME + File.separator +
						hsm.getTargetDirectory(header, null);
		String fileNameOfExperimentFile = fileName.substring(0, fileName.length() - ".iap.index.csv".length()) + ".iap.vanted.bin";
		
		IOurl u = url.getIOurlFor(experimentDirectory + File.separator + fileNameOfExperimentFile);
		String prefix = u.getPrefix();
		ExperimentInterface md = Experiment.loadFromIOurl(u);
		for (NumericMeasurementInterface nmi : Substance3D.getAllFiles(md)) {
			if (nmi != null && nmi instanceof BinaryMeasurement) {
				BinaryMeasurement bm = (BinaryMeasurement) nmi;
				if (bm.getURL() != null)
					bm.getURL().setPrefix(prefix);
				if (bm.getLabelURL() != null)
					bm.getLabelURL().setPrefix(prefix);
			}
		}
		md.setHeader(header);
		return md;
	}
	
	@Override
	public boolean canHandle(String databaseId) {
		return databaseId.startsWith(url.getPrefix() + ":");
	}
	
	public ArrayList<ExperimentReference> getAllExperiments() throws Exception {
		if (!read)
			readDataSource();
		HashSet<ExperimentReference> checkedRefs = new HashSet<ExperimentReference>();
		HashSet<DataSourceLevel> checkedLevels = new HashSet<DataSourceLevel>();
		
		ArrayList<ExperimentReference> res = new ArrayList<ExperimentReference>();
		Queue<DataSourceLevel> check = new LinkedList<DataSourceLevel>();
		check.add(thisLevel);
		while (!check.isEmpty()) {
			DataSourceLevel cl = check.poll();
			if (cl == null)
				continue;
			if (checkedLevels.contains(cl))
				continue;
			Collection<ExperimentReference> el = cl.getExperiments();
			if (el != null)
				for (ExperimentReference er : el) {
					if (checkedRefs.contains(er))
						continue;
					res.add(er);
					checkedRefs.add(er);
				}
			Collection<DataSourceLevel> sl = cl.getSubLevels();
			if (sl != null)
				check.addAll(sl);
			checkedLevels.add(cl);
		}
		return res;
	}
}
