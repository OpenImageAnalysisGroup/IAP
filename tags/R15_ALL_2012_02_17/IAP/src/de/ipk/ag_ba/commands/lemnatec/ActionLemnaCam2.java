package de.ipk.ag_ba.commands.lemnatec;

import java.util.ArrayList;

import org.AttributeHelper;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractUrlNavigationAction;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public final class ActionLemnaCam2 extends AbstractUrlNavigationAction {
	
	public ActionLemnaCam2() {
		super("Show Maize Greenhouse");
	}
	
	public static NavigationButton getLemnaCamButton(GUIsetting guiSetting) {
		NavigationAction navigationAction = new ActionLemnaCam2();
		NavigationButton res = new NavigationButton(navigationAction, "CCTV (Maize)",
				IAPimages.getWebCam2(),
				guiSetting);
		return res;
	}
	
	@Override
	public String getURL() {
		if (SystemAnalysis.isHeadless())
			return "file:///home/view/view/view.jpg";
		else  
			return "http://ba-10.ipk-gatersleben.de/SnapshotJPEG?Resolution=640x480&Quality=Clarity";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		AttributeHelper.showInBrowser(getURL());
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public String getDefaultTooltip() {
		return "Show Camera Snapshots";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}