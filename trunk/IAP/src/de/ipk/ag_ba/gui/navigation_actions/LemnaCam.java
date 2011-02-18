package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.AttributeHelper;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public final class LemnaCam extends AbstractUrlNavigationAction {
	
	public LemnaCam() {
		super("Access IPK Glasshouse CCTV (restricted access)");
	}
	
	/**
	 * @param guiSetting
	 * @return
	 */
	public static NavigationButton getLemnaCamButton(GUIsetting guiSetting) {
		NavigationAction navigationAction = new LemnaCam();
		NavigationButton res = new NavigationButton(navigationAction, "LemnaCam 1", "img/ext/cctv.png", guiSetting);
		return res;
	}
	
	@Override
	public String getURL() {
		return "http://lemnacam.ipk-gatersleben.de/mjpg/video.mjpg";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		AttributeHelper.showInBrowser(getURL());
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}