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
	
	private final String url;
	private final String title;
	
	public ActionLemnaCamBarleyGH(String tooltip, String url, String title) {
		super(tooltip);
		this.url = url;
		this.title = title;
	}
	
	/**
	 * @param guiSetting
	 * @return
	 */
	public static NavigationButton getLemnaCamButton(GUIsetting guiSetting) {
		String tooltip = "Show Barley Greenhouse";
		String title = "CCTV (Barley)";
		String url = "http://lemnacam.ipk-gatersleben.de/mjpg/video.mjpg";
		NavigationAction navigationAction = new ActionLemnaCamBarleyGH(
				tooltip, url, title);
		NavigationButton res = new NavigationButton(navigationAction, title,
				IAPimages.getWebCam(),
				guiSetting);
		return res;
	}
	
	@Override
	public String getURL() {
		return url;
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
			return "<html><center>" + title + "<br>" + status.getCurrentStatusMessage1();
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