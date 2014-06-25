package de.ipk.ag_ba.commands.about;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.MenuItemInfoDialog;

/**
 * @author Christian Klukas
 */
public class ActionSaveLicense extends AbstractNavigationAction {
	
	private NavigationButton src;
	
	public ActionSaveLicense(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		new MenuItemInfoDialog().saveFiles();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> ll = new ArrayList<String>();
		ll.add("<html><h2><font face=\"Sans,Tohama,Arial\">External Licenses</font></h2>" +
				"<font face=\"Sans,Tohama,Arial\">Some library licenses require the user to be able to save<br>" +
				"their license text to be saved on a users PC.<br>" +
				"</font>");
		
		MainPanelComponent mp = new MainPanelComponent(ll);// , new Color(0, 0, 20));
		return mp;
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>Store on disk<br>" +
				"<font color='gray'><small>(if possibility is required by license)</small></font>";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Document-Save-64.png";
	}

	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		// res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;// new ArrayList<>();
	}
}
