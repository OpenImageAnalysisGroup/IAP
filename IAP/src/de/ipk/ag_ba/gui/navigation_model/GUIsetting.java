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
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;

/**
 * @author klukas
 */
public class GUIsetting {
	
	private final IAPnavigationPanel navigationPanel;
	private final IAPnavigationPanel actionPanel;
	private final JPanel graphPanel;
	private final LinkedHashSet<ExperimentReferenceInterface> clipboardExperiments = new LinkedHashSet<ExperimentReferenceInterface>();
	private final HashSet<String> clipboardExperimentDatabaseIds = new HashSet<String>();
	private static int instanceId = 0;
	private int instance;
	private Runnable closeRunnable;
	
	public GUIsetting(IAPnavigationPanel navigationPanel, IAPnavigationPanel actionPanel, JPanel graphPanel) {
		this.navigationPanel = navigationPanel;
		this.actionPanel = actionPanel;
		this.graphPanel = graphPanel;
		this.setInstance(++instanceId);
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
	
	public boolean isInClipboard(ExperimentReferenceInterface experimentReference) {
		boolean found = clipboardExperiments.contains(experimentReference);
		if (!found && experimentReference.getExperimentPeek() != null) {
			for (ExperimentReferenceInterface er : clipboardExperiments) {
				if (er.getExperimentPeek() == experimentReference.getExperimentPeek())
					return true;
			}
		}
		return found;
	}
	
	public void addClipboardItem(ExperimentReferenceInterface experimentReference) {
		// add only the header to the clipboard, as storing the whole experiment may
		// require a lot of memory
		if (experimentReference.getHeader().getDatabaseId() != null && !experimentReference.getHeader().getDatabaseId().isEmpty()) {
			ExperimentReferenceInterface er = new ExperimentReference(experimentReference.getHeader());
			clipboardExperiments.add(er);
			clipboardExperimentDatabaseIds.add(er.getHeader().getDatabaseId());
			er.setM(experimentReference.getM());
		} else {
			clipboardExperiments.add(experimentReference);
		}
		
	}
	
	public void removeClipboardItem(ExperimentReferenceInterface experimentReference) {
		String del = experimentReference.getHeader().getDatabaseId();
		clipboardExperimentDatabaseIds.remove(experimentReference.getHeader().getDatabaseId());
		ArrayList<ExperimentReferenceInterface> toBeRemoved = new ArrayList<ExperimentReferenceInterface>();
		for (ExperimentReferenceInterface er : clipboardExperiments) {
			if (er.getHeader().getDatabaseId().equals(del) || er == experimentReference ||
					er.getExperiment() == experimentReference.getExperiment())
				toBeRemoved.add(er);
		}
		for (ExperimentReferenceInterface d : toBeRemoved)
			clipboardExperiments.remove(d);
	}
	
	public Collection<ExperimentReferenceInterface> getClipboardItems() {
		return clipboardExperiments;
	}
	
	public void clearClipboard() {
		clipboardExperiments.clear();
		clipboardExperimentDatabaseIds.clear();
	}
	
	public int getInstance() {
		return instance;
	}
	
	private void setInstance(int instance) {
		this.instance = instance;
	}
	
	public void closeWindow() {
		getCloseRunnable().run();
	}
	
	private Runnable getCloseRunnable() {
		return closeRunnable;
	}
	
	public void setCloseRunnable(Runnable closeRunnable) {
		this.closeRunnable = closeRunnable;
	}
}
