/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.AttributeHelper;

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
		this.title = title;
		this.url = url;
		this.icon = icon;
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
		NavigationAction action = new AbstractNavigationAction("Show in browser") {
			@Override
			public void performActionCalculateResults(NavigationButton src) {
				AttributeHelper.showInBrowser(getUrl());
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				return null;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
		};
		NavigationButton website = new NavigationButton(action, getTitle(), getIcon(),
							src.getGUIsetting());
		website.setToolTipText("Open " + getUrl());
		return website;
	}
}
