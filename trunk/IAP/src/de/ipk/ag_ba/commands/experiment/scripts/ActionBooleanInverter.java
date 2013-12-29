package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class ActionBooleanInverter extends AbstractNavigationAction {
	
	private ArrayList<ThreadSafeOptions> groupSelection;
	
	public ActionBooleanInverter(String tooltip) {
		super(tooltip);
	}
	
	public ActionBooleanInverter(String tooltip, ArrayList<ThreadSafeOptions> groupSelection) {
		this(tooltip);
		this.groupSelection = groupSelection;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		for (ThreadSafeOptions tso : groupSelection)
			tso.setBval(0, !tso.getBval(0, false));
	}
	
	@Override
	public String getDefaultTitle() {
		return "Invert Selection";
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
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
	public String getDefaultImage() {
		return "img/ext/gpl2/gtcf.png";
	}
}
