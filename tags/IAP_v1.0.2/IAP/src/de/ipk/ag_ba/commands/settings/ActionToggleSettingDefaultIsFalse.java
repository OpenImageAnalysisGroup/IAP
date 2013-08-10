package de.ipk.ag_ba.commands.settings;

import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SettingsHelperDefaultIsFalse;
import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ActionToggleSettingDefaultIsFalse extends AbstractNavigationAction {
	
	private final String setting;
	private final String settingDefaultIsFalse;
	private final BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider;
	private final Runnable startAction;
	
	public ActionToggleSettingDefaultIsFalse(BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider, Runnable startAction, String tooltip,
			String setting,
			String settingDefaultIsFalse) {
		super("<html>" + StringManipulationTools.stringReplace(tooltip, ";", ";<br>"));
		this.optStatusProvider = optStatusProvider;
		this.startAction = startAction;
		this.setting = setting;
		this.settingDefaultIsFalse = settingDefaultIsFalse;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public String getDefaultTitle() {
		boolean enabled = new SettingsHelperDefaultIsFalse().isEnabled(settingDefaultIsFalse);
		if (optStatusProvider != null)
			return "<html><center>" + pretty(setting) + (enabled ? " (on)" : " (off)") + "<br>"
					+ optStatusProvider.getCurrentStatusMessage1() + "<br>"
					+ optStatusProvider.getCurrentStatusMessage2() +
					"</center>";
		else
			return pretty(setting) + (enabled ? " (on)" : " (off)");
	}
	
	private String pretty(String s) {
		if (s == null || s.length() < 50)
			return s;
		else
			return s.substring(0, 47) + "...";
	}
	
	@Override
	public NavigationImage getImageIconInactive() {
		boolean enabled = new SettingsHelperDefaultIsFalse().isEnabled(settingDefaultIsFalse);
		if (enabled)
			return IAPmain.loadIcon("img/ext/gpl2/gtcf.png");
		else
			return IAPmain.loadIcon("img/ext/gpl2/gtcd.png");
	}
	
	@Override
	public NavigationImage getImageIconActive() {
		return getImageIconInactive();
	}
	
	@Override
	public String getDefaultImage() {
		boolean enabled = new SettingsHelperDefaultIsFalse().isEnabled(settingDefaultIsFalse);
		if (enabled)
			return "img/ext/gpl2/gtcf.png";
		else
			return "img/ext/gpl2/gtcd.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		synchronized (this.getClass()) {
			boolean enabled = new SettingsHelperDefaultIsFalse().isEnabled(settingDefaultIsFalse);
			enabled = !enabled;
			new SettingsHelperDefaultIsFalse().setEnabled(settingDefaultIsFalse, enabled);
			if (enabled && startAction != null)
				startAction.run();
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
