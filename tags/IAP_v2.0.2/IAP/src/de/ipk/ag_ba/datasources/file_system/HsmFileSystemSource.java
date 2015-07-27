/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.file_system;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.Other;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.mongodb.ActionDomainLogout;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.io_handler.hsm.HsmResourceIoHandler;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class HsmFileSystemSource extends FileSystemSource {
	
	private static HashSet<String> registeredFolders = new HashSet<String>();
	private String login;
	
	public HsmFileSystemSource(Library lib, String dataSourceName, String folder,
			NavigationImage mainDataSourceIcon,
			NavigationImage mainDataSourceIconActive,
			NavigationImage folderIcon,
			NavigationImage folderIconOpened) {
		super(lib, dataSourceName, folder, new String[] {},
				mainDataSourceIcon,
				mainDataSourceIconActive,
				folderIcon,
				folderIconOpened);
		
		if (folder != null && !registeredFolders.contains(folder)) {
			HsmResourceIoHandler rio = new HsmResourceIoHandler(folder);
			ResourceIOManager.registerIOHandler(rio);
			registeredFolders.add(folder);
		}
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) throws Exception {
		Collection<NavigationButton> res = new ArrayList<NavigationButton>();
		if (IAPmain.getRunMode() == IAPrunMode.WEB)
			res.add(new NavigationButton(new ActionDomainLogout(), src.getGUIsetting()));
		try {
			if (!read)
				readDataSource();
			res.add(Other.getCalendarEntity(
					this.getAllExperimentsNewestByGroup(),
					null, src.getGUIsetting(), false));
		} catch (Exception e) {
			if (e.getCause() != null && e.getCause().getCause() != null)
				throw new RuntimeException(e.getCause().getCause() + "");
			else
				throw new RuntimeException(e);
		}
		
		return res;
	}
	
	@Override
	public void readDataSource() throws Exception {
		this.read = true;
		this.mainList = new ArrayList<PathwayWebLinkItem>();
		// read HSM index
		String folder = url + File.separator + HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME;
		File dir = new File(folder);
		String[] entries = dir.list(new FilenameFilter() {
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
	
	protected ExperimentHeader getHSMexperimentHeaderFromFileName(String optUrl, String fileName) throws IOException {
		String url = optUrl != null ? optUrl + File.separator : "";
		String folder = url + HSMfolderTargetDataManager.DIRECTORY_FOLDER_NAME;
		HashMap<String, String> properties = new HashMap<String, String>();
		TextFile tf = new TextFile(folder + File.separator + fileName);
		properties.put("_id", "hsm:" + folder + File.separator + fileName);
		for (String p : tf) {
			String[] entry = p.split(",", 3);
			properties.put(entry[1], entry[2]);
		}
		ExperimentHeader eh = new ExperimentHeader(properties);
		return eh;
	}
	
	public static ExperimentHeader getHSMexperimentHeaderFromFullyQualifiedFileName(String fileName) throws IOException {
		HashMap<String, String> properties = new HashMap<String, String>();
		TextFile tf = new TextFile(fileName);
		properties.put("_id", "hsm:" + fileName);
		for (String p : tf) {
			String[] entry = p.split(",", 3);
			properties.put(entry[1], entry[2]);
		}
		ExperimentHeader eh = new ExperimentHeader(properties);
		return eh;
	}
	
	@Override
	public String getName() {
		return dataSourceName;
		/*
		 * if (thisLevel == null)
		 * return super.getName();
		 * else
		 * return thisLevel.getName();
		 */
	}
	
	@Override
	public void setLogin(String login, String password) {
		super.setLogin(login, password);
		this.login = login;
	}
	
	protected boolean accessOK(ExperimentHeader eh) {
		if (login == null)
			return true;
		else {
			if (LTdataExchange.getAdministrators().contains(login))
				return true;
			if (eh.getImportusername().equals(login))
				return true;
			else {
				if ((eh.getImportusergroup() + ",").contains(login + ","))
					return true;
				else
					return false;
			}
		}
	}
	
	public Collection<ExperimentHeaderInterface> getAllExperimentsNewest() {
		if (thisLevel != null && ((HsmMainDataSourceLevel) thisLevel).experimentName2saveTime2data != null) {
			Collection<TreeMap<Long, ExperimentHeaderInterface>> a = ((HsmMainDataSourceLevel) thisLevel).experimentName2saveTime2data.values();
			Collection<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
			for (TreeMap<Long, ExperimentHeaderInterface> map : a) {
				result.add(map.lastEntry().getValue());
			}
			return result;
		} else
			return null;
	}
	
	/**
	 * @return currently the group info is not processed, the returned
	 *         set contains the same content as getAllExperimentsNewest with
	 *         some dummy group info (good enough for the calendar construction).
	 */
	public TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> getAllExperimentsNewestByGroup() {
		TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> res = new TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>();
		res.put("a", new TreeMap<String, ArrayList<ExperimentHeaderInterface>>());
		res.get("a").put("b", (ArrayList<ExperimentHeaderInterface>) getAllExperimentsNewest());
		return res;
	}
}
