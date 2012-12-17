package de.ipk.ag_ba.commands.experiment.process;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public class ActionAssignSettings extends AbstractNavigationAction implements NavigationAction {
	
	private String iniFileName;
	private String title;
	
	public ActionAssignSettings(String tooltip) {
		super(tooltip);
	}
	
	public ActionAssignSettings(String iniFileName, String tooltip, String title) {
		this(tooltip);
		this.iniFileName = iniFileName;
		this.title = title;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		// TODO Auto-generated method stub
		return null;
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
