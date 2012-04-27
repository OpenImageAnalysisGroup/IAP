package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ActionToggle extends AbstractNavigationAction {
	
	private final String setting;
	private final ThreadSafeOptions option;
	
	public ActionToggle(String tooltip, String setting, ThreadSafeOptions option) {
		super("<html>" + StringManipulationTools.stringReplace(tooltip, ";", ";<br>"));
		this.setting = setting;
		this.option = option;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public boolean getProvidesActions() {
		return false;
	}
	
	@Override
	public String getDefaultTitle() {
		if (SystemAnalysis.isHeadless())
			return (option.getBval(0, true) ? "Include " : "Exclude ") + pretty(setting);
		else
			return pretty(setting);
	}
	
	private String pretty(String s) {
		if (s == null || s.length() < 50)
			return s;
		else
			return s.substring(0, 47) + "...";
	}
	
	@Override
	public NavigationImage getImageIconInactive() {
		if (option.getBval(0, true))
			return IAPmain.loadIcon("img/ext/gpl2/gtce.png");
		else
			return IAPmain.loadIcon("img/ext/gpl2/gtcd.png");
	}
	
	@Override
	public NavigationImage getImageIconActive() {
		return getImageIconInactive();
	}
	
	@Override
	public String getDefaultImage() {
		if (option.getBval(0, true))
			return "img/ext/gpl2/gtce.png";
		else
			return "img/ext/gpl2/gtcd.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		option.setBval(0, !option.getBval(0, true));
		option.setLong(System.currentTimeMillis());
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
