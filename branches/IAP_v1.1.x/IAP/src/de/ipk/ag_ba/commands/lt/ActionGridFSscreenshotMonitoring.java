package de.ipk.ag_ba.commands.lt;

import java.util.ArrayList;

import org.StringManipulationTools;

import com.mongodb.gridfs.GridFS;

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
	private final String name;
	private final GridFS gridfs_screenshots;
	
	public ActionGridFSscreenshotMonitoring(MongoDB dc, String id, String name,
			GridFS gridfs_screenshots) {
		super("Show desktop (" + id + ")");
		this.dc = dc;
		this.id = id;
		this.gridfs_screenshots = gridfs_screenshots;
		this.name = name;
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
		return StringManipulationTools.stringReplace(name, ".png", "");
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (IAPmain.getRunMode().isSwing())
			return new MainPanelComponent(
					new PictureViewerFromDB(dc, name, getStatusProvider(), gridfs_screenshots));
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