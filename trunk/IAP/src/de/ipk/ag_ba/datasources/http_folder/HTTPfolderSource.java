/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import java.awt.image.BufferedImage;
import java.util.Collection;

import org.graffiti.plugin.io.resources.HTTPhandler;

import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.WebDirectoryFileListAccess;

/**
 * @author klukas
 * 
 */
public class HTTPfolderSource extends HTTPhandler implements DataSource {

	private final String url;
	private final String[] validExtensions;
	private Collection<PathwayWebLinkItem> mainList;
	private final BufferedImage mainDataSourceIcon;
	private final String dataSourceName;

	public HTTPfolderSource(String dataSourceName, String url, String[] validExtensions, BufferedImage mainDataSourceIcon) {
		this.url = url;
		this.validExtensions = validExtensions;
		this.mainDataSourceIcon = mainDataSourceIcon;
		this.dataSourceName = dataSourceName;
	}

	@Override
	public void setLogin(String login, String password) {
		// empty
	}

	@Override
	public void readDataSource() throws Exception {
		mainList = WebDirectoryFileListAccess.getWebDirectoryFileListItems(url, validExtensions, false);
	}

	@Override
	public DataSourceLevel getMainLevelInfo() {
		return new HTTPdataSourceLevel(dataSourceName, mainList, mainDataSourceIcon);
	}
}
