/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.file_system;

import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;
import org.graffiti.plugin.io.resources.HTTPhandler;

import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.HTTPdataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.actions.Book;
import de.ipk.ag_ba.gui.actions.Library;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

/**
 * @author klukas
 */
public class FileSystemSource extends HTTPhandler implements DataSource {
	
	protected final String url;
	private final String[] validExtensions;
	protected Collection<PathwayWebLinkItem> mainList;
	private final NavigationImage mainDataSourceIcon;
	private final String dataSourceName;
	private final NavigationImage folderIcon;
	protected boolean read;
	protected DataSourceLevel thisLevel;
	private final Library lib;
	private String description;
	
	public FileSystemSource(Library lib, String dataSourceName, String url, String[] validExtensions,
						NavigationImage mainDataSourceIcon, NavigationImage folderIcon) {
		this.lib = lib;
		this.url = url;
		this.validExtensions = validExtensions;
		this.mainDataSourceIcon = mainDataSourceIcon;
		this.folderIcon = folderIcon;
		this.dataSourceName = dataSourceName;
	}
	
	@Override
	public void setLogin(String login, String password) {
		// empty
	}
	
	@Override
	public void readDataSource() throws Exception {
		mainList = FileSystemAccess.getWebDirectoryFileListItems(url, validExtensions, false);
		thisLevel = new HTTPdataSourceLevel(lib, dataSourceName, mainList, mainDataSourceIcon, folderIcon);
		read = true;
	}
	
	@Override
	public Collection<DataSourceLevel> getSubLevels() {
		try {
			if (!read)
				readDataSource();
			return thisLevel.getSubLevels();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return new ArrayList<DataSourceLevel>();
		}
	}
	
	@Override
	public Collection<ExperimentReference> getExperiments() {
		try {
			if (!read)
				readDataSource();
			return thisLevel.getExperiments();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return new ArrayList<ExperimentReference>();
		}
	}
	
	@Override
	public Collection<PathwayWebLinkItem> getPathways() {
		try {
			if (!read)
				readDataSource();
			return thisLevel.getPathways();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return new ArrayList<PathwayWebLinkItem>();
		}
	}
	
	@Override
	public NavigationImage getIcon() {
		return mainDataSourceIcon;
	}
	
	@Override
	public String getName() {
		return dataSourceName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.datasources.DataSourceLevel#getBookReferencesAtThisLevel()
	 */
	@Override
	public ArrayList<Book> getReferenceInfos() {
		try {
			if (!read)
				readDataSource();
			return thisLevel.getReferenceInfos();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return new ArrayList<Book>();
		}
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) {
		return new ArrayList<NavigationButton>();
	}
}
