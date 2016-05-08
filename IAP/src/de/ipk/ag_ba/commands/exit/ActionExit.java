package de.ipk.ag_ba.commands.exit;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author Christian Klukas
 */
public class ActionExit extends AbstractNavigationAction {
	
	private boolean systemExit;
	
	public ActionExit(String tooltip, boolean systemExit) {
		super(tooltip);
		this.systemExit = systemExit;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (systemExit)
			System.exit(0);
		else
			guiSetting.closeWindow();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return null;
	}
	
	@Override
	public String getDefaultTitle() {
		if (systemExit)
			return "Quit";
		else
			return "Close";
	}
	
	@Override
	public String getDefaultImage() {
		if (systemExit)
			return "img/ext/gpl2/Gnome-Application-Exit-64.png";
		else
			return "img/ext/gpl2/Gnome-Window-Close-64.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<>();
	}
}
