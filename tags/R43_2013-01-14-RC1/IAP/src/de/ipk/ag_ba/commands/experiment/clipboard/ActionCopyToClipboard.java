package de.ipk.ag_ba.commands.experiment.clipboard;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionCopyToClipboard extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	
	public ActionCopyToClipboard(String tooltip) {
		super(tooltip);
		m = null;
		experimentReference = null;
	}
	
	public ActionCopyToClipboard(MongoDB m, ExperimentReference experimentReference) {
		this("Copy experiment to clipbard");
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (guiSetting.isInClipboard(experimentReference.getHeader().getDatabaseId()) ||
				guiSetting.isInClipboard(experimentReference))
			src.getGUIsetting().removeClipboardItem(experimentReference);
		else
			src.getGUIsetting().addClipboardItem(experimentReference, m);
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
		if (guiSetting != null && (guiSetting.isInClipboard(experimentReference.getHeader().getDatabaseId()) ||
				guiSetting.isInClipboard(experimentReference)))
			return "Remove from Clipboard";
		else
			return "Add to Clipboard";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getCopyToClipboard();
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
}
