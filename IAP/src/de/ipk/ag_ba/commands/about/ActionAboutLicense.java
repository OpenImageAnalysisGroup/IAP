package de.ipk.ag_ba.commands.about;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.MenuItemInfoDialog;

/**
 * @author Christian Klukas
 */
public class ActionAboutLicense extends AbstractNavigationAction {
	
	public ActionAboutLicense(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> ll = new ArrayList<String>();
		ll.add("<html>" + "<h2><font face=\"Sans,Tohama,Arial\">IAP is created with the help of the " +
				"following external libraries:</font></h2><font face=\"Sans,Tohama,Arial\">" +
				"<ul>" +
				MenuItemInfoDialog.getLibsText()
				+ "</table>");
		
		MainPanelComponent mp = new MainPanelComponent(ll);// , new Color(0, 0, 20));
		return mp;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Library Licenses";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-X-Office-Address-Book-64_metaData.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> rr = new ArrayList<NavigationButton>();
		rr.add(new NavigationButton(new ActionSaveLicense("<html>" +
				"Save license text to disk<br>" +
				"(if possibility is required by license)"), guiSetting));
		return rr;
	}
}
