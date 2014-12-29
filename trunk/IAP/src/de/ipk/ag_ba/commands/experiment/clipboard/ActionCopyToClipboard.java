package de.ipk.ag_ba.commands.experiment.clipboard;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;

public class ActionCopyToClipboard extends AbstractNavigationAction implements ActionDataProcessing {
	private ExperimentReferenceInterface experimentReference;
	
	public ActionCopyToClipboard(String tooltip) {
		super(tooltip);
		experimentReference = null;
	}
	
	public ActionCopyToClipboard() {
		this("Copy experiment to clipbard");
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (guiSetting.isInClipboard(experimentReference.getHeader().getDatabaseId()) ||
				guiSetting.isInClipboard(experimentReference))
			src.getGUIsetting().removeClipboardItem(experimentReference);
		else
			src.getGUIsetting().addClipboardItem(experimentReference);
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
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.experimentReference = experimentReference;
	}
	
}
