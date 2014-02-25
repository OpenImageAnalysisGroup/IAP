package de.ipk.ag_ba.commands;

import iap.blocks.data_structures.AbstractImageAnalysisBlockFIS;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author Christian Klukas
 */
public class ActionResetBlockTimings extends AbstractNavigationAction {
	
	public ActionResetBlockTimings(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		AbstractImageAnalysisBlockFIS.resetBlockStatistics();
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
		return "Reset timing information";
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
