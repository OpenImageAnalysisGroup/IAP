package de.ipk.ag_ba.commands.lemnatec;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class ActionGridFSscreenshotMonitoring extends AbstractNavigationAction {
	
	private final MongoDB dc;
	private final String id;
	
	public ActionGridFSscreenshotMonitoring(MongoDB dc, String id) {
		super("Show desktop (" + id + ")");
		this.dc = dc;
		this.id = id;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		// empty
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
		return id;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (IAPmain.getRunMode().isSwing())
			return new MainPanelComponent(
					new PictureViewerFromDB(dc, id, getStatusProvider()));
		else
			return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getNetworkPConline();
	}
}