package de.ipk.ag_ba.commands.settings;

import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SettingsHelperDefaultIsTrue;
import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ActionToggleSettingDefaultIsTrue extends AbstractNavigationAction {
	
	private final String setting;
	private final String settingDefaultIsTrue;
	
	public ActionToggleSettingDefaultIsTrue(String tooltip, String setting, String settingDefaultIsTrue) {
		super("<html>" + StringManipulationTools.stringReplace(tooltip, ";", ";<br>"));
		this.setting = setting;
		this.settingDefaultIsTrue = settingDefaultIsTrue;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultTitle() {
		// if (SystemAnalysis.isHeadless())
		// return (option.getBval(0, true) ? "Include " : "Exclude ") + pretty(setting);
		// else
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
		boolean enabled = new SettingsHelperDefaultIsTrue().isEnabled(settingDefaultIsTrue);
		if (enabled)
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
		boolean enabled = new SettingsHelperDefaultIsTrue().isEnabled(settingDefaultIsTrue);
		if (enabled)
			return "img/ext/gpl2/gtce.png";
		else
			return "img/ext/gpl2/gtcd.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		synchronized (this.getClass()) {
			boolean enabled = new SettingsHelperDefaultIsTrue().isEnabled(settingDefaultIsTrue);
			new SettingsHelperDefaultIsTrue().setEnabled(settingDefaultIsTrue, !enabled);
		}
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
