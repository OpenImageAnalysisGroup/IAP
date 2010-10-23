package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.AttributeHelper;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;

/**
 * @author klukas
 * 
 */
public final class LemnaCam extends AbstractNavigationAction {

	public LemnaCam() {
		super("Access IPK Glasshouse CCTV (restricted access)");
	}

	@Override
	public void setOneTimeFinishAction(Runnable runnable) {
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) {
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		AttributeHelper.showInBrowser("http://lemnacam.ipk-gatersleben.de/mjpg/video.mjpg");
		return null;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		return null;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return null;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getAdditionalEntities() {
		return null;
	}

	@Override
	public void addAdditionalEntity(NavigationGraphicalEntity ne) {

	}

	/**
	 * @param guiSetting
	 * @return
	 */
	public static NavigationGraphicalEntity getLemnaCamButton(GUIsetting guiSetting) {
		NavigationAction navigationAction = new LemnaCam();
		NavigationGraphicalEntity res = new NavigationGraphicalEntity(navigationAction, "LemnaCam 1",
				"img/ext/camera.png", guiSetting);
		return res;
	}
}