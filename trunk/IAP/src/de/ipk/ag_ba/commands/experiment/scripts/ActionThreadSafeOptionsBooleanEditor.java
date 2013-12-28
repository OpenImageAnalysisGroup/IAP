package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public class ActionThreadSafeOptionsBooleanEditor extends AbstractNavigationAction {
	
	private ThreadSafeOptions tso;
	
	public ActionThreadSafeOptionsBooleanEditor(String tooltip) {
		super(tooltip);
	}
	
	public ActionThreadSafeOptionsBooleanEditor(ThreadSafeOptions tso) {
		this("Change selection");
		this.tso = tso;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		tso.setBval(0, !tso.getBval(0, false));
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultTitle() {
		return (String) tso.getParam(0, "[Unknown Property]");
	}
	
	@Override
	public String getDefaultImage() {
		if (tso.getBval(0, false))
			return "img/ext/gpl2/gtcf.png";
		else
			return "img/ext/gpl2/gtcd.png";
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
}
