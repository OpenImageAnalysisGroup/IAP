/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.gui.navigation_actions.Book;
import de.ipk.ag_ba.gui.navigation_actions.Library;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

/**
 * @author klukas
 */
public class HTTPdataSourceLevel implements DataSourceLevel {

	private final Collection<PathwayWebLinkItem> mainList;
	private final String name;
	private final NavigationImage icon;
	private final NavigationImage folderIcon;
	private final Library lib;

	private class DataSourceLevelsAndPathways {
		Collection<DataSourceLevel> levels;
		Collection<PathwayWebLinkItem> pathways;

		public DataSourceLevelsAndPathways(Collection<DataSourceLevel> levels, Collection<PathwayWebLinkItem> pathways) {
			this.levels = levels;
			this.pathways = pathways;
		}
	}

	public HTTPdataSourceLevel(Library lib, String name, Collection<PathwayWebLinkItem> mainList,
						NavigationImage icon, NavigationImage folderIcon) {
		this.lib = lib;
		this.mainList = mainList;
		this.icon = icon;
		this.folderIcon = folderIcon;
		this.name = name;
	}

	@Override
	public Collection<DataSourceLevel> getSubLevels() {
		DataSourceLevelsAndPathways res = getSubLevelsAndPathways();
		return res.levels;
	}

	private DataSourceLevelsAndPathways getSubLevelsAndPathways() {
		TreeSet<String> groups = new TreeSet<String>();
		for (PathwayWebLinkItem item : mainList)
			if (item.getGroup1() != null)
				groups.add(item.getGroup1());

		Collection<DataSourceLevel> levels = new ArrayList<DataSourceLevel>();
		Collection<PathwayWebLinkItem> pathways = new ArrayList<PathwayWebLinkItem>();

		if (groups.size() >= 2) {
			for (String group : groups) {
				ArrayList<PathwayWebLinkItem> subLevel = new ArrayList<PathwayWebLinkItem>();
				for (PathwayWebLinkItem item : mainList)
					if (group.equals(item.getGroup1())) {
						if (group.isEmpty())
							pathways.add(item);
						else
							subLevel.add(item);
					}
				levels.add(new HTTPdataSourceLevel(lib, group, subLevel, folderIcon, folderIcon));
			}
		} else {
			groups.clear();
			for (PathwayWebLinkItem item : mainList)
				if (item != null && item.getGroup2() != null)
					groups.add(item.getGroup2());

			if (groups.size() >= 2) {
				for (String group : groups) {
					ArrayList<PathwayWebLinkItem> subLevel = new ArrayList<PathwayWebLinkItem>();
					for (PathwayWebLinkItem item : mainList)
						if (group.equals(item.getGroup2())) {
							if (group.isEmpty())
								pathways.add(item);
							else
								subLevel.add(item);
						}
					levels.add(new HTTPdataSourceLevel(lib, group, subLevel, folderIcon, folderIcon));
				}
			} else {
				// return all items as pathways at primary level (no grouping
				// information)
				pathways.addAll(mainList);
			}
		}
		return new DataSourceLevelsAndPathways(levels, pathways);
	}

	@Override
	public Collection<ExperimentReference> getExperiments() {
		return new ArrayList<ExperimentReference>();
	}

	@Override
	public Collection<PathwayWebLinkItem> getPathways() {
		return getSubLevelsAndPathways().pathways;
	}

	@Override
	public NavigationImage getIcon() {
		return icon;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ArrayList<Book> getReferenceInfos() {
		return lib.getBooksInFolder(name);
	}

	@Override
	public void setDescription(String description) {
		// empty
	}

	@Override
	public String getDescription() {
		return null;
	}
}
