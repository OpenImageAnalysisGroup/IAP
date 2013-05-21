/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Oct 23, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.swing.JComponent;
import javax.swing.JPanel;

import de.ipk.ag_ba.gui.IAPnavigationPanel;
import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
 * @author klukas
 */
public class GUIsetting {
	
	private final IAPnavigationPanel navigationPanel;
	private final IAPnavigationPanel actionPanel;
	private final JPanel graphPanel;
	private final LinkedHashSet<ExperimentReference> clipboardExperiments = new LinkedHashSet<ExperimentReference>();
	private final HashSet<String> clipboardExperimentDatabaseIds = new HashSet<String>();
	
	public GUIsetting(IAPnavigationPanel navigationPanel, IAPnavigationPanel actionPanel, JPanel graphPanel) {
		this.navigationPanel = navigationPanel;
		this.actionPanel = actionPanel;
		this.graphPanel = graphPanel;
	}
	
	public IAPnavigationPanel getNavigationPanel() {
		return navigationPanel;
	}
	
	public IAPnavigationPanel getActionPanel() {
		return actionPanel;
	}
	
	public JComponent getGraphPanel() {
		return graphPanel;
	}
	
	public boolean isInClipboard(String databaseId) {
		return clipboardExperimentDatabaseIds.contains(databaseId);
	}
	
	public boolean isInClipboard(ExperimentReference experimentReference) {
		boolean found = clipboardExperiments.contains(experimentReference);
		if (!found && experimentReference.getExperimentPeek() != null) {
			for (ExperimentReference er : clipboardExperiments) {
				if (er.getExperimentPeek() == experimentReference.getExperimentPeek())
					return true;
			}
		}
		return found;
	}
	
	public void addClipboardItem(ExperimentReference experimentReference) {
		// add only the header to the clipboard, as storing the whole experiment may
		// require a lot of memory
		if (experimentReference.getHeader().getDatabaseId() != null && !experimentReference.getHeader().getDatabaseId().isEmpty()) {
			ExperimentReference er = new ExperimentReference(experimentReference.getHeader());
			clipboardExperiments.add(er);
			clipboardExperimentDatabaseIds.add(er.getHeader().getDatabaseId());
			er.m = experimentReference.m;
		} else {
			clipboardExperiments.add(experimentReference);
		}
		
	}
	
	public void removeClipboardItem(ExperimentReference experimentReference) {
		String del = experimentReference.getHeader().getDatabaseId();
		clipboardExperimentDatabaseIds.remove(experimentReference.getHeader().getDatabaseId());
		ArrayList<ExperimentReference> toBeRemoved = new ArrayList<ExperimentReference>();
		for (ExperimentReference er : clipboardExperiments) {
			if (er.getHeader().getDatabaseId().equals(del) || er == experimentReference ||
					er.getExperiment() == experimentReference.getExperiment())
				toBeRemoved.add(er);
		}
		for (ExperimentReference d : toBeRemoved)
			clipboardExperiments.remove(d);
	}
	
	public Collection<ExperimentReference> getClipboardItems() {
		return clipboardExperiments;
	}
	
	public void clearClipboard() {
		clipboardExperiments.clear();
		clipboardExperimentDatabaseIds.clear();
	}
}
