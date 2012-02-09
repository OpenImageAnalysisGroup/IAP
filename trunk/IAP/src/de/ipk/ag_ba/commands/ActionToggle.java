package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ActionToggle extends AbstractNavigationAction {
	
	private boolean enabled = true;
	private final String setting;
	
	public ActionToggle(String tooltip, String setting) {
		super(tooltip);
		this.setting = setting;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultTitle() {
		return (enabled ? "Include " : "Exclude ") + setting;
	}
	
	@Override
	public NavigationImage getImageIcon() {
		return super.getImageIcon();
	}
	
	@Override
	public String getDefaultImage() {
		if (enabled)
			return "img/ext/gpl2/gtce.png";
		else
			return "img/ext/gpl2/gtcd.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		enabled = !enabled;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
		return new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
	}
}
