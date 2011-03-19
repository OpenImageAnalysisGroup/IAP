/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.AttributeHelper;
import org.StringManipulationTools;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class Book {
	
	private final String folder;
	private final String title;
	private final String url;
	private final String icon;
	
	public Book(String folder, String title, String url) {
		this(folder, title, url, "img/dataset.png");
	}
	
	public Book(String folder, String title, String url, String icon) {
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
		return title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public NavigationButton getNavigationButton(NavigationButton src) {
		return getNavigationButton(src, getIcon());
	}
	
	public NavigationButton getNavigationButton(NavigationButton src, String icon) {
		NavigationAction action = new AbstractUrlNavigationAction("Show in browser") {
			@Override
			public void performActionCalculateResults(NavigationButton src) {
				AttributeHelper.showInBrowser(Book.this.getUrl());
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				return null;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
			
			@Override
			public String getURL() {
				return Book.this.getUrl();
			}
		};
		NavigationButton website = new NavigationButton(action, getTitle(), icon,
							src.getGUIsetting());
		website.setToolTipText("Open " + getUrl());
		return website;
	}
	
}
