package de.ipk.ag_ba.commands.experiment.process;

import java.util.ArrayList;

import org.IniIoProvider;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public class ActionPerformAnalysisLocally extends AbstractNavigationAction {
	
	private IniIoProvider iniIO;
	private SystemOptions so;
	
	public ActionPerformAnalysisLocally(String tooltip) {
		super(tooltip);
	}
	
	public ActionPerformAnalysisLocally(IniIoProvider iniIO) {
		this("Perform Analysis (local execution)");
		this.iniIO = iniIO;
		so = SystemOptions.getInstance(null, iniIO);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public String getDefaultTooltip() {
		return so.getString("DESCRIPTION", "pipeline_description", "(no description specified)", true);
	}
	
	@Override
	public String getDefaultTitle() {
		return "Perform " + so.getString("DESCRIPTION", "pipeline_name", "(unnamed)", true);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Applications-Engineering-64.png";
	}
}
