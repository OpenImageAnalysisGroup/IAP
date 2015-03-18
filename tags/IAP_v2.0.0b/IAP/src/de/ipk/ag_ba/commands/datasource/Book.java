/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.datasource;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class Book {
	
	private final String folder;
	private final String title;
	private final IOurl url;
	private final String icon;
	
	public Book(String folder, String title, IOurl url) {
		this(folder, title, url, "img/dataset.png");
	}
	
	public Book(String folder, String title, IOurl url, String icon) {
		this.folder = folder;
		this.title = prettify(title);
		this.url = url;
		this.icon = icon;
	}
	
	private String prettify(String title) {
		String nice = StringManipulationTools.stringReplace(title, "u%cc%88", "ü");
		nice = StringManipulationTools.stringReplace(nice, "%c3%bc", "ü");
		nice = StringManipulationTools.stringReplace(nice, "%c3%9c", "Ü");
		return StringManipulationTools.stringReplace(nice, "U%cc%88", "Ü");
	}
	
	public String getFolder() {
		return folder;
	}
	
	public String getTitle() {
		return StringManipulationTools.stringReplace(
				StringManipulationTools.stringReplace(title, ".url", ""), ".webloc", "");
	}
	
	public IOurl getUrl() {
		return url;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public NavigationButton getNavigationButton(NavigationButton src) {
		return getNavigationButton(src, getIcon());
	}
	
	public NavigationButton getNavigationButton(NavigationButton src, String icon) {
		NavigationAction action = new WebUrlAction(getUrl(), "Show in browser");
		NavigationButton website = new NavigationButton(action, getTitle(), icon,
				src.getGUIsetting());
		website.setToolTipText("Open " + getUrl());
		return website;
	}
	
}
