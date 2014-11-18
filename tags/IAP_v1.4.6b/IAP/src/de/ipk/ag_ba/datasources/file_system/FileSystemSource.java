/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.file_system;

import java.util.ArrayList;
import java.util.Collection;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.plugin.io.resources.HTTPhandler;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.datasources.DataSourceGroup;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.HTTPdataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

/**
 * @author klukas
 */
public class FileSystemSource extends HTTPhandler implements DataSource {
	
	protected final String url;
	private final String[] validExtensions;
	protected Collection<PathwayWebLinkItem> mainList;
	protected final NavigationImage mainDataSourceIconInactive;
	protected final NavigationImage mainDataSourceIconActive;
	protected final String dataSourceName;
	protected final NavigationImage folderIcon;
	protected boolean read;
	protected DataSourceLevel thisLevel;
	protected final Library lib;
	private String description;
	
	public FileSystemSource(Library lib, String dataSourceName, String folder, String[] validExtensions,
			NavigationImage mainDataSourceIcon,
			NavigationImage mainDataSourceIconActive,
			NavigationImage folderIcon) {
		this.lib = lib;
		this.url = folder;
		this.validExtensions = validExtensions;
		this.mainDataSourceIconInactive = mainDataSourceIcon;
		this.mainDataSourceIconActive = mainDataSourceIcon;
		this.folderIcon = folderIcon;
		this.dataSourceName = dataSourceName;
		
		ExperimentReference.registerExperimentLoader(this);
	}
	
	@Override
	public void setLogin(String login, String password) {
		// empty
	}
	
	@Override
	public void readDataSource() throws Exception {
		mainList = FileSystemAccess.getWebDirectoryFileListItems(url, validExtensions, false);
		thisLevel = new HTTPdataSourceLevel(
				lib, dataSourceName, mainList,
				mainDataSourceIconInactive,
				mainDataSourceIconActive,
				folderIcon);
		read = true;
	}
	
	@Override
	public Collection<DataSourceLevel> getSubLevels() {
		try {
			if (!read)
				readDataSource();
			if (thisLevel == null)
				return new ArrayList<DataSourceLevel>();
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
			if (thisLevel != null)
				return thisLevel.getExperiments();
			else
				return new ArrayList<ExperimentReference>();
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
			if (thisLevel == null)
				return new ArrayList<PathwayWebLinkItem>();
			else
				return thisLevel.getPathways();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return new ArrayList<PathwayWebLinkItem>();
		}
	}
	
	@Override
	public NavigationImage getIconInactive() {
		return mainDataSourceIconInactive;
	}
	
	@Override
	public NavigationImage getIconActive() {
		return mainDataSourceIconActive;
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
			if (thisLevel == null)
				return new ArrayList<Book>();
			return thisLevel.getReferenceInfos();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return new ArrayList<Book>();
		}
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) throws Exception {
		return new ArrayList<NavigationButton>();
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntitiesShownAtEndOfList(NavigationButton src) {
		return new ArrayList<NavigationButton>();
	}
	
	@Override
	public ExperimentInterface getExperiment(ExperimentHeaderInterface experimentReq, boolean interactiveGetExperimentSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		throw new UnsupportedOperationException("TODO");
	}
	
	@Override
	public boolean canHandle(String databaseId) {
		return false;
	}
	
	@Override
	public DataSourceGroup getDataSourceGroup() {
		return null;
	}
}
