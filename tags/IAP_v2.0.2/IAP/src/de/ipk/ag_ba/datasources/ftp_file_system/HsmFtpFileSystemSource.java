/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.ftp_file_system;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.mongodb.ActionDomainLogout;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.io_handler.hsm.HsmResourceIoHandler;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class HsmFtpFileSystemSource extends FtpFileSystemSource {
	
	private static HashSet<String> registeredFolders = new HashSet<String>();
	private String login;
	
	public HsmFtpFileSystemSource(Library lib, String dataSourceName, String folder, NavigationImage mainDataSourceIcon,
			NavigationImage folderIcon, NavigationImage folderIconOpened) {
		super(lib, dataSourceName, folder, new String[] {},
				mainDataSourceIcon, folderIcon, folderIconOpened);
		
		if (!registeredFolders.contains(folder)) {
			ResourceIOManager.registerIOHandler(new HsmResourceIoHandler(folder));
			registeredFolders.add(folder);
		}
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) {
		Collection<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(new NavigationButton(new ActionDomainLogout(), src.getGUIsetting()));
		
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
		
		HashMap<String, TreeMap<Long, ExperimentHeader>> experimentName2saveTime2data =
				new HashMap<String, TreeMap<Long, ExperimentHeader>>();
		
		if (entries != null)
			for (String e : entries) {
				long saveTime = Long.parseLong(e.substring(0, e.indexOf("_")));
				
				HashMap<String, String> properties = new HashMap<String, String>();
				TextFile tf = new TextFile(folder + File.separator + e);
				properties.put("_id", "hsm:" + folder + File.separator + e);
				for (String p : tf) {
					String[] entry = p.split(",", 3);
					properties.put(entry[1], entry[2]);
				}
				ExperimentHeader eh = new ExperimentHeader(properties);
				
				if (accessOK(eh)) {
					String experimentName = eh.getExperimentName();
					if (!experimentName2saveTime2data.containsKey(experimentName))
						experimentName2saveTime2data.put(experimentName, new TreeMap<Long, ExperimentHeader>());
					experimentName2saveTime2data.get(experimentName).put(saveTime, eh);
				}
			}
		
		this.thisLevel = new HsmFtpMainDataSourceLevel(experimentName2saveTime2data);
	}
	
	@Override
	public String getName() {
		if (thisLevel == null)
			return super.getName();
		else
			return thisLevel.getName();
	}
	
	@Override
	public void setLogin(String login, String password) {
		super.setLogin(login, password);
		this.login = login;
	}
	
	private boolean accessOK(ExperimentHeader eh) {
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
}
