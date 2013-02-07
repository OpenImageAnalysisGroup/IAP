/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.database_tools;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class ActionMergeAnalysisResults extends AbstractNavigationAction {
	
	private final MongoDB m;
	private ArrayList<NavigationButton> mergedDocuments;
	private String splitCount;
	private NavigationButton src;
	
	public ActionMergeAnalysisResults(MongoDB m) {
		super("Merges available split analysis results, creates new analysis jobs for missing data");
		this.m = m;
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getThreeDocuments();
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>" +
				"Merge Split Analysis Results<br>" +
				"(TODO: implement function)";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return mergedDocuments;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(splitCount + " temporary split datasets have been merged and removed. " +
				mergedDocuments.size() + " result datasets have been saved.");
	}
	
	@Override
	public boolean isProvidingActions() {
		return true;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		mergedDocuments.clear();
	}
}
