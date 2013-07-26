/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Jul 29, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.datasource;

import java.util.ArrayList;

import org.AttributeHelper;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class LiteratureReference extends AbstractNavigationAction {
	
	private final String url;
	private final String title;
	private final String image;
	
	public LiteratureReference(String url, String title, String image) {
		super("Show " + url + " in browser");
		this.url = url;
		this.title = title;
		this.image = image;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return null;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		AttributeHelper.showInBrowser(url);
	}
	
	@Override
	public String getDefaultImage() {
		return image;
	}
	
	@Override
	public String getDefaultTitle() {
		return title;
	}
}
