package de.ipk.ag_ba.commands.settings;

import java.util.ArrayList;

import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class ActionResetActions extends AbstractNavigationAction {
	
	private String section;
	private String group;
	private SystemOptions systemOptions;
	
	public ActionResetActions(String tooltip) {
		super(tooltip);
	}
	
	public ActionResetActions(SystemOptions systemOptions, String section, String group) {
		this("<html>Reset settings of this group <br>(remove settings and values, defaults will be recreated as needed)");
		this.systemOptions = systemOptions;
		this.section = section;
		this.group = group;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		systemOptions.removeValuesOfSectionAndGroup(section, group);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(
				"Settings for this group have been deleted and will be automatically <br>" +
						"re-created with its default-value as soon as the value is used for <br>" +
						"the connected program function.<br><br>" +
						"It is recommended to navigate now fully back within the<br>" +
						"command history (click the first button in the command history bar).");
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>Defaults<br>(delayed)</center>";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Preferences-Desktop-64.png";
	}
	
}
