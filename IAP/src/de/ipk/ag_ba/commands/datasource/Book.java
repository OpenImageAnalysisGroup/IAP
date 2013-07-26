/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.datasource;

import java.io.StringWriter;
import java.util.ArrayList;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.apache.commons.io.IOUtils;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;

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
		NavigationAction action = new AbstractUrlNavigationAction("Show in browser") {
			IOurl trueURL = null;
			String htmlTextPanel = null;
			
			@Override
			public void performActionCalculateResults(NavigationButton src) {
				IOurl referenceURL = Book.this.getUrl();
				if (trueURL == null)
					if (referenceURL.endsWith(".url") || referenceURL.endsWith(".webloc"))
						try {
							trueURL = IAPservice.getURLfromWeblocFile(referenceURL);
						} catch (Exception e) {
							MongoDB.saveSystemErrorMessage("Could not read webloc-file from " + referenceURL + ".", e);
							AttributeHelper.showInBrowser(referenceURL);
							return;
						}
					else
						if (referenceURL.endsWith(".txt") || referenceURL.endsWith(".htm") || referenceURL.endsWith(".html"))
							try {
								StringWriter writer = new StringWriter();
								IOUtils.copy(referenceURL.getInputStream(), writer);
								htmlTextPanel = writer.toString();
								if (referenceURL.endsWith(".txt"))
									htmlTextPanel = StringManipulationTools.txt2html(htmlTextPanel);
							} catch (Exception e) {
								MongoDB.saveSystemErrorMessage("Could not read content from " + referenceURL + ".", e);
								AttributeHelper.showInBrowser(referenceURL);
								return;
							}
						else
							trueURL = referenceURL;
				AttributeHelper.showInBrowser(trueURL);
			}
			
			@Override
			public MainPanelComponent getResultMainPanel() {
				if (htmlTextPanel != null)
					return new MainPanelComponent(htmlTextPanel);
				return super.getResultMainPanel();
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
			public IOurl getURL() {
				IOurl referenceURL = Book.this.getUrl();
				if (trueURL == null)
					if (referenceURL.endsWith(".url") || referenceURL.endsWith(".webloc"))
						try {
							trueURL = IAPservice.getURLfromWeblocFile(referenceURL);
						} catch (Exception e) {
							MongoDB.saveSystemErrorMessage("Could not read webloc-file from " + referenceURL + ".", e);
							return trueURL;
						}
					else
						trueURL = referenceURL;
				return trueURL;
			}
		};
		NavigationButton website = new NavigationButton(action, getTitle(), icon,
				src.getGUIsetting());
		website.setToolTipText("Open " + getUrl());
		return website;
	}
	
}
