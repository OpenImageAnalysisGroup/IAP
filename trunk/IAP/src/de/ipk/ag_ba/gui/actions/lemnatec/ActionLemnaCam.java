package de.ipk.ag_ba.gui.actions.lemnatec;

import java.util.ArrayList;

import org.AttributeHelper;

import de.ipk.ag_ba.gui.actions.AbstractUrlNavigationAction;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public final class ActionLemnaCam extends AbstractUrlNavigationAction {
	
	public ActionLemnaCam() {
		super("Show Barley Greenhouse");
	}
	
	/**
	 * @param guiSetting
	 * @return
	 */
	public static NavigationButton getLemnaCamButton(GUIsetting guiSetting) {
		NavigationAction navigationAction = new ActionLemnaCam();
		NavigationButton res = new NavigationButton(navigationAction, "CCTV (Barley)",
				IAPimages.getWebCam(),
				guiSetting);
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