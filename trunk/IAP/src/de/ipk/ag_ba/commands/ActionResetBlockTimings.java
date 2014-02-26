package de.ipk.ag_ba.commands;

import iap.blocks.data_structures.AbstractImageAnalysisBlockFIS;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author Christian Klukas
 */
public class ActionResetBlockTimings extends AbstractNavigationAction {
	
	private final boolean fullReset;
	
	public ActionResetBlockTimings(String tooltip, boolean fullReset) {
		super(tooltip);
		this.fullReset = fullReset;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		AbstractImageAnalysisBlockFIS.resetBlockStatistics(fullReset);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public String getDefaultTitle() {
		if (fullReset)
			return "Reset table list";
		else
			return "Set times to zero";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Edit-Clear-64.png";
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
}
