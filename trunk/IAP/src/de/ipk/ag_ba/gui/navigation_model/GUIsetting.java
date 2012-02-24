/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Oct 23, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_model;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JPanel;

import de.ipk.ag_ba.gui.MyNavigationPanel;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class GUIsetting {
	
	private final MyNavigationPanel navigationPanel;
	private final MyNavigationPanel actionPanel;
	private final JPanel graphPanel;
	private final ArrayList<ExperimentReference> clipboardExperiments = new ArrayList<ExperimentReference>();
	private final HashSet<String> clipboardExperimentDatabaseIds = new HashSet<String>();
	
	public GUIsetting(MyNavigationPanel navigationPanel, MyNavigationPanel actionPanel, JPanel graphPanel) {
		this.navigationPanel = navigationPanel;
		this.actionPanel = actionPanel;
		this.graphPanel = graphPanel;
	}
	
	public MyNavigationPanel getNavigationPanel() {
		return navigationPanel;
	}
	
	public MyNavigationPanel getActionPanel() {
		return actionPanel;
	}
	
	public JComponent getGraphPanel() {
		return graphPanel;
	}
	
	public boolean isInClipboard(String databaseId) {
		return clipboardExperimentDatabaseIds.contains(databaseId);
	}
	
	public void addClipboardItem(ExperimentReference experimentReference, MongoDB m) {
		// add only the header to the clipboard, as storing the whole experiment may
		// require a lot of memory
		if (experimentReference.getHeader().getDatabaseId() != null && !experimentReference.getHeader().getDatabaseId().isEmpty()) {
			ExperimentReference er = new ExperimentReference(experimentReference.getHeader());
			clipboardExperiments.add(er);
			clipboardExperimentDatabaseIds.add(er.getHeader().getDatabaseId());
			er.m = m;
		} else {
			clipboardExperiments.add(experimentReference);
			experimentReference.m = m;
		}
		
	}
	
	public void removeClipboardItem(ExperimentReference experimentReference) {
		String del = experimentReference.getHeader().getDatabaseId();
		clipboardExperimentDatabaseIds.remove(experimentReference.getHeader().getDatabaseId());
		ArrayList<ExperimentReference> toBeRemoved = new ArrayList<ExperimentReference>();
		for (ExperimentReference er : clipboardExperiments) {
			if (er.getHeader().getDatabaseId().equals(del) || er == experimentReference)
				toBeRemoved.add(er);
		}
		for (ExperimentReference d : toBeRemoved)
			clipboardExperiments.remove(d);
	}
	
	public ArrayList<ExperimentReference> getClipboardItems() {
		return clipboardExperiments;
	}
	
	public void clearClipboard() {
		clipboardExperiments.clear();
		clipboardExperimentDatabaseIds.clear();
	}
}
