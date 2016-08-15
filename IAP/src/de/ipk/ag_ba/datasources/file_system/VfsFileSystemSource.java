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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ExperimentHeaderHelper;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.experiment.hsm.ActionHsmDataSourceNavigation;
import de.ipk.ag_ba.commands.experiment.hsm.DataExportHelper;
import de.ipk.ag_ba.commands.mongodb.ActionMongoExperimentsNavigation;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class VfsFileSystemSource extends HsmFileSystemSource {
	
	protected final VirtualFileSystem url;
	private final String[] validExtensions2;
	private final String subfolder;
	ArrayList<NavigationAction> folderActions = new ArrayList<NavigationAction>();
	LinkedHashSet<ExperimentHeaderInterface> trashed = new LinkedHashSet<ExperimentHeaderInterface>();
	private final NavigationImage folderIconOpened;
	private boolean forceReadOnly;
	
	public VfsFileSystemSource(Library lib, String dataSourceName, VirtualFileSystem folder,
			String[] validExtensions,
			NavigationImage mainDataSourceIcon, NavigationImage mainDataSourceIconActive,
			NavigationImage folderIcon, NavigationImage folderIconOpened) {
		this(lib, dataSourceName, folder, validExtensions, mainDataSourceIcon, mainDataSourceIconActive, folderIcon,
				folderIconOpened, null);
	}
	
	private VfsFileSystemSource(Library lib, String dataSourceName, VirtualFileSystem folder,
			String[] validExtensions,
			NavigationImage mainDataSourceIcon, NavigationImage mainDataSourceIconActive,
			NavigationImage folderIcon,
			NavigationImage folderIconOpened, String subfolder) {
		super(lib, dataSourceName,
				(folder instanceof VirtualFileSystemFolderStorage ?
						((VirtualFileSystemFolderStorage) folder).getTargetPathName() : null),
				mainDataSourceIcon, mainDataSourceIcon, folderIcon, folderIconOpened);
		this.url = folder;
		validExtensions2 = validExtensions;
		this.subfolder = subfolder;
		this.folderIconOpened = folderIconOpened;
	}
	
	@Override
	public void readDataSource() throws Exception {
		trashed.clear();
		this.read = true;
		this.mainList = new ArrayList<PathwayWebLinkItem>();
		folderActions.clear();
		HashMap<String, TreeMap<Long, ExperimentHeaderInterface>> experimentName2saveTime2data =
				new HashMap<String, TreeMap<Long, ExperimentHeaderInterface>>();
		this.thisLevel = new HsmMainDataSourceLevel(experimentName2saveTime2data);
		((HsmMainDataSourceLevel) thisLevel).setHsmFileSystemSource(this);
		
		// read folder content (if valid file extensions are not empty)
		if (validExtensions2 != null && validExtensions2.length > 0) {
			// read folders...
			for (final String fn : url.listFolders(subfolder)) {
				if (fn.equals(VirtualFileSystemVFS2.DIRECTORY_FOLDER_NAME) || fn.equals(VirtualFileSystemVFS2.DATA_FOLDER_NAME)
						|| fn.equals(VirtualFileSystemVFS2.CONDITION_FOLDER_NAME) || fn.equals(VirtualFileSystemVFS2.ICON_FOLDER_NAME))
					continue;
				VfsFileSystemSource dataSourceHsm = new VfsFileSystemSource(new Library(),
						fn, url,
						validExtensions2, mainDataSourceIconInactive, mainDataSourceIconInactive, folderIcon, folderIconOpened,
						subfolder != null ? subfolder + File.separator + fn : fn);
				ActionHsmDataSourceNavigation action = new ActionHsmDataSourceNavigation(dataSourceHsm) {
					
					@Override
					public String getDefaultTitle() {
						return fn;
					}
					
				};
				folderActions.add(action);
			}
			// WebDirectoryFileListAccess.getWebDirectoryFileListItems(url, validExtensions2, false);
			// read files...
			for (String fn : url.listFiles(subfolder, new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					boolean accept = false;
					for (String validExt : validExtensions2) {
						if (name.endsWith(validExt)) {
							accept = true;
							break;
						}
					}
					return accept;
				}
			})) {
				String folder = subfolder == null ? "" : subfolder + "/";
				IOurl u = url.getIOurlFor(folder + fn);
				if (fn.endsWith(".gml") || fn.endsWith(".graphml"))
					folderActions.add(IAPservice.getPathwayViewAction(new PathwayWebLinkItem(fn, u, false)));
				else {
					String icon = null;
					if (fn != null && fn.contains("."))
						icon = IAPimages.getImageFromFileExtension(fn.substring(fn.lastIndexOf(".")));
					if (icon != null)
						lib.add(new Book("", fn.substring(0, fn.lastIndexOf(".")), u, icon));
					else
						lib.add(new Book("", fn.substring(0, fn.lastIndexOf(".")), u));
				}
			}
		}
		if (subfolder == null) {
			// read index folder
			String[] entries = url.listFiles(HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME, new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".iap.index.csv");
				}
			});
			
			if (entries != null) {
				
				for (String fileName : entries) {
					long saveTime = Long.parseLong(fileName.substring(0, fileName.indexOf("_")));
					
					ExperimentHeader eh = getExperimentHeaderFromFileName(url, fileName);
					
					if (accessOK(eh)) {
						if (eh.inTrash())
							trashed.add(eh);
						else {
							String experimentName = eh.getExperimentName();
							if (!experimentName2saveTime2data.containsKey(experimentName))
								experimentName2saveTime2data.put(experimentName, new TreeMap<Long, ExperimentHeaderInterface>());
							experimentName2saveTime2data.get(experimentName).put(saveTime, eh);
							eh.addHistoryItems(experimentName2saveTime2data.get(experimentName));
						}
					}
				}
			}
		}
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) throws Exception {
		if (!read)
			readDataSource();
		Collection<NavigationButton> folderButtons = new ArrayList<NavigationButton>();
		for (NavigationAction na : folderActions)
			folderButtons.add(new NavigationButton(na, src.getGUIsetting()));
		if (subfolder == null)
			for (NavigationButton nb : super.getAdditionalEntities(src))
				folderButtons.add(nb);
		
		return folderButtons;
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntitiesShownAtEndOfList(NavigationButton src) {
		Collection<NavigationButton> folderButtons = super.getAdditionalEntitiesShownAtEndOfList(src);
		if (trashed.size() > 0) {
			folderButtons.add(new NavigationButton(ActionMongoExperimentsNavigation.getTrashedExperimentsAction(trashed, null), src.getGUIsetting()));
		}
		return folderButtons;
	}
	
	public synchronized ExperimentHeader getExperimentHeaderFromFileName(final VirtualFileSystem vfs, final String fileName) throws Exception {
		final ExperimentHeader eh = new ExperimentHeader();
		
		eh.setDatabaseId(vfs.getPrefix() + ":index" + File.separator + fileName);
		
		ExperimentHeaderHelper ehh = getExperimentHeaderHelper(vfs, fileName, eh);
		
		ehh.readSourceForUpdate();
		
		eh.setExperimentHeaderHelper(ehh);
		
		return eh;
	}
	
	public static ExperimentHeaderHelper getExperimentHeaderHelper(final VirtualFileSystem vfs, final String fileName,
			final ExperimentHeaderInterface eh)
			throws Exception {
		final VfsFileObject indexFile = vfs.getFileObjectFor(HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME + File.separator + fileName);
		ExperimentHeaderHelper ehh = new ExperimentHeaderHelper() {
			
			@Override
			public void readSourceForUpdate() throws Exception {
				synchronized (VfsFileSystemSource.class) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">Get current header from file (" + fileName + ")");
					HashMap<String, String> properties = new HashMap<String, String>();
					InputStream is = indexFile.getInputStream();
					TextFile tf = new TextFile(is, -1);
					properties.put("_id", vfs.getPrefix() + ":" + HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME + File.separator + fileName);
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
					System.out.println(eh.getDatabaseId());
				}
			}
			
			@Override
			public Long getLastModified() throws Exception {
				return indexFile.getLastModified();
			}
			
			@Override
			public Long saveUpdatedProperties(BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
				if (!isAbleToSaveData())
					return System.currentTimeMillis();
				synchronized (VfsFileSystemSource.class) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">Save updated header information in " + indexFile.getName());
					DataExportHelper.writeExperimentHeaderToIndexFile(eh, indexFile.getOutputStream(), -1);
					return indexFile.getLastModified();
				}
			}
			
			@Override
			public boolean isAbleToSaveData() {
				return vfs.isAbleToSaveData();
			}
			
			@Override
			public String getFileName() {
				return fileName;
			}
		};
		return ehh;
	}
	
	@Override
	public ExperimentInterface getExperiment(ExperimentHeaderInterface header, boolean interactiveGetExperimentSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		HSMfolderTargetDataManager.clearPathCache();
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
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Load Experiment");
		IOurl u = url.getIOurlFor(experimentDirectory + "/" + fileNameOfExperimentFile);
		String prefix = u.getPrefix();
		ExperimentInterface md = Experiment.loadFromIOurl(u, optStatus);
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Post processing");
		for (NumericMeasurementInterface nmi : Substance3D.getAllFiles(md)) {
			if (nmi != null && nmi instanceof BinaryMeasurement) {
				BinaryMeasurement bm = (BinaryMeasurement) nmi;
				if (bm.getURL() != null)
					bm.getURL().setPrefix(prefix);
				if (bm.getLabelURL() != null)
					bm.getLabelURL().setPrefix(prefix);
			}
			if (nmi instanceof ImageData) {
				final ImageData id = (ImageData) nmi;
				String oldRef = id.getAnnotationField("oldreference");
				if (oldRef != null && !oldRef.isEmpty()) {
					IOurl oldRefUrl = new IOurl(oldRef);
					oldRefUrl.setPrefix(prefix);
					id.setAnnotationField("oldreference", oldRefUrl.toString());
				}
			}
		}
		md.setHeader(header);
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Processing finished");
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
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.datasources.DataSourceLevel#getBookReferencesAtThisLevel()
	 */
	@Override
	public ArrayList<Book> getReferenceInfos() {
		return lib.getBooksInFolder("");
	}
	
	public void setReadOnly(boolean forceReadOnly) {
		this.forceReadOnly = forceReadOnly;
	}
}
