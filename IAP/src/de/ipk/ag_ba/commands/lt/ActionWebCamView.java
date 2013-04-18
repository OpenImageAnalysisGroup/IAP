package de.ipk.ag_ba.commands.lt;

import java.util.ArrayList;

import org.AttributeHelper;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.datasource.AbstractUrlNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public final class ActionWebCamView extends AbstractUrlNavigationAction {
	
	private final IOurl url;
	private final String title;
	
	public ActionWebCamView(String tooltip, IOurl url, String title) {
		super(tooltip);
		this.url = url;
		this.title = title;
	}
	
	/**
	 * @param guiSetting
	 * @return
	 */
	public static NavigationButton getLemnaCamButton(
			GUIsetting guiSetting,
			String tooltip, String title, IOurl url) {
		NavigationAction navigationAction = new ActionWebCamView(
				tooltip, url, title);
		NavigationButton res = new NavigationButton(navigationAction, title,
				IAPimages.getWebCam(),
				guiSetting);
		return res;
	}
	
	@Override
	public IOurl getURL() {
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
					new PictureViewer(getURL(), getStatusProvider()));
		else
			return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}