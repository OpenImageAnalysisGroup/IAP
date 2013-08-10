package de.ipk.ag_ba.commands.experiment.clipboard;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;

public class ActionClearClipboard extends AbstractNavigationAction {
	
	private Experiment experimentResult;
	private NavigationButton srcButton;
	
	public ActionClearClipboard(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (experimentResult != null)
			return;
		this.srcButton = src;
		
		guiSetting.clearClipboard();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> newRes = new ArrayList<NavigationButton>(currentSet);
		return newRes;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Empty Clipboard";
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Clipboard has been emptied!");
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getFileCleaner();
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
}
