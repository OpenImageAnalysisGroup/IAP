package de.ipk.ag_ba.commands.about;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author Christian Klukas
 */
public class ActionFeedback extends AbstractNavigationAction {
	
	public ActionFeedback(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> ll = new ArrayList<String>();
		ll.add("<html><h2><font face=\"Sans,Tohama,Arial\">Feedback</font></h2>" +
				"<font face=\"Sans,Tohama,Arial\">Thank you very much for using IAP!<br><br>" +
				"If you have any question, don't hesitate to send an E-mail to " +
				"<a href=\"mailto:klukas@users.sf.net\">klukas@users.sf.net</a>.<br><br>" +
				"</font>");
		// AttributeHelper.showInBrowser("mailto:klukas@ipk-gatersleben.de?subject=" + DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT + "%20feedback");
		
		MainPanelComponent mp = new MainPanelComponent(ll);// , new Color(0, 0, 20));
		return mp;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Feedback";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Emblem-Mail-64.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}
