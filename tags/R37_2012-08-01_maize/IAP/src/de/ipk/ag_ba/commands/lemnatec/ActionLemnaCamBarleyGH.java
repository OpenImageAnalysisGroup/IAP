package de.ipk.ag_ba.commands.lemnatec;

import java.util.ArrayList;

import org.AttributeHelper;

import de.ipk.ag_ba.commands.AbstractUrlNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.IAPwebcam;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public final class ActionLemnaCamBarleyGH extends AbstractUrlNavigationAction {
	
	public ActionLemnaCamBarleyGH() {
		super("Show Barley Greenhouse");
	}
	
	/**
	 * @param guiSetting
	 * @return
	 */
	public static NavigationButton getLemnaCamButton(GUIsetting guiSetting) {
		NavigationAction navigationAction = new ActionLemnaCamBarleyGH();
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
		if (!IAPmain.getRunMode().isSwing())
			AttributeHelper.showInBrowser(getURL());
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return null;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return super.requestTitleUpdates();
	}
	
	@Override
	public String getDefaultTitle() {
		if (!IAPmain.getRunMode().isSwing())
			return super.getDefaultTitle();
		else
			return "<html><center>CCTV (Barley)<br>" + status.getCurrentStatusMessage1();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (IAPmain.getRunMode().isSwing())
			return new MainPanelComponent(
					new PictureViewer(IAPwebcam.BARLEY,
							getURL(), getStatusProvider()));
		else
			return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}