package de.ipk.ag_ba.commands.experiment.process;

import java.util.ArrayList;

import org.SystemOptions;
import org.apache.commons.lang3.StringEscapeUtils;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionAssignSettings extends AbstractNavigationAction implements NavigationAction {
	
	private String iniFileName;
	private String title;
	private ExperimentReference exp;
	
	public ActionAssignSettings(String tooltip) {
		super(tooltip);
	}
	
	public ActionAssignSettings(MongoDB m, ExperimentReference exp,
			String iniFileName, String tooltip, String title) {
		this(tooltip);
		this.exp = exp;
		this.iniFileName = iniFileName;
		this.title = title;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		String ini = SystemOptions.getInstance(iniFileName, null).getIniValue();
		ini = StringEscapeUtils.escapeXml(ini);
		exp.getHeader().setSettings(ini);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;// new ArrayList<NavigationButton>();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Data from '" + iniFileName + "' has been assigned to experiment '"
				+ exp.getExperimentName() + "'.");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public String getDefaultTitle() {
		return title;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Insert-Object-64.png";
	}
	
}
