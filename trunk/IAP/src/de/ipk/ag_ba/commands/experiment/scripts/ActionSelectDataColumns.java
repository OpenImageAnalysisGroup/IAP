package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class ActionSelectDataColumns extends AbstractNavigationAction {
	
	public ActionSelectDataColumns(String tooltip) {
		super(tooltip);
		// TODO Auto-generated constructor stub
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
		return "Data Columns<br><font color='gray'><small>5 selected</small></font>";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Applications-Engineering-64.png";
	}
	
}
