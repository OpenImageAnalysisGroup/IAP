package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.AttributeHelper;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public final class LemnaCam extends AbstractNavigationAction {

	public LemnaCam() {
		super("Access IPK Glasshouse CCTV (restricted access)");
	}

	@Override
	public void performActionCalculateResults(NavigationButton src) {
	}

	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		AttributeHelper.showInBrowser("http://lemnacam.ipk-gatersleben.de/mjpg/video.mjpg");
		return null;
	}

	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return null;
	}

	@Override
	public ArrayList<NavigationButton> getAdditionalEntities() {
		return null;
	}

	@Override
	public void addAdditionalEntity(NavigationButton ne) {

	}

	/**
	 * @param guiSetting
	 * @return
	 */
	public static NavigationButton getLemnaCamButton(GUIsetting guiSetting) {
		NavigationAction navigationAction = new LemnaCam();
		NavigationButton res = new NavigationButton(navigationAction, "LemnaCam 1", "img/ext/camera.png", guiSetting);
		return res;
	}
}