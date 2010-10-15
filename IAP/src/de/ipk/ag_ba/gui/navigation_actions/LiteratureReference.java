/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Jul 29, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.AttributeHelper;

import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;

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
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		return null;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		return null;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) {
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
