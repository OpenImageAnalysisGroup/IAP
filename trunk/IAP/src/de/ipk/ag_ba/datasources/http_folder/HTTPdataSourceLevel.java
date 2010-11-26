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
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

/**
 * @author klukas
 * 
 */
public class HTTPdataSourceLevel implements DataSourceLevel {

	private final Collection<PathwayWebLinkItem> mainList;
	private final BufferedImage icon;
	private final String name;

	private class DataSourceLevelsAndPathways {
		Collection<DataSourceLevel> levels;
		Collection<PathwayWebLinkItem> pathways;

		public DataSourceLevelsAndPathways(Collection<DataSourceLevel> levels, Collection<PathwayWebLinkItem> pathways) {
			this.levels = levels;
			this.pathways = pathways;
		}
	}

	public HTTPdataSourceLevel(String name, Collection<PathwayWebLinkItem> mainList, BufferedImage icon) {
		this.mainList = mainList;
		this.icon = icon;
		this.name = name;
	}

	@Override
	public Collection<DataSourceLevel> getSubDataSourceLevels() {
		DataSourceLevelsAndPathways res = getSubLevelsAndPathways();
		return res.levels;
	}

	private DataSourceLevelsAndPathways getSubLevelsAndPathways() {
		TreeSet<String> groups = new TreeSet<String>();
		for (PathwayWebLinkItem item : mainList)
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
				levels.add(new HTTPdataSourceLevel(group, subLevel, icon));
			}
		} else {
			groups.clear();
			for (PathwayWebLinkItem item : mainList)
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
					levels.add(new HTTPdataSourceLevel(group, subLevel, icon));
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
	public Collection<ExperimentReference> getExperimentsAtThisLevel() {
		return new ArrayList<ExperimentReference>();
	}

	@Override
	public Collection<PathwayWebLinkItem> getPathwaysAtThisLevel() {
		return getSubLevelsAndPathways().pathways;
	}

	@Override
	public BufferedImage getIconForThisLevel() {
		return icon;
	}

	@Override
	public BufferedImage getOpenedIconForThisLevel() {
		return icon;
	}

	@Override
	public String getName() {
		return name;
	}
}
