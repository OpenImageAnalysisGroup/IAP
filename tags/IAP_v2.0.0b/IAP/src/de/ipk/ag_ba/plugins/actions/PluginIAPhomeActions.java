package de.ipk.ag_ba.plugins.actions;

import java.util.ArrayList;

import org.SystemOptions;

import de.ipk.ag_ba.commands.load_dataset.ActionLoadDataSet;
import de.ipk.ag_ba.commands.lt.ActionLTnavigation;
import de.ipk.ag_ba.commands.mongodb.ActionMongoExperimentsNavigation;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PluginIAPhomeActions extends AbstractIAPplugin {
	public PluginIAPhomeActions() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP home action plugin is beeing loaded");
	}
	
	@Override
	public NavigationAction[] getHomeNavigationActions() {
		ArrayList<NavigationAction> result = new ArrayList<NavigationAction>();
		
		boolean addLoadFilesIcon = SystemOptions.getInstance().getBoolean("File Import", "Show Load Files Icon", true);
		boolean addLoadExportedIcons = IAPmain.getRunMode() == IAPrunMode.SWING_MAIN || IAPmain.getRunMode() == IAPrunMode.SWING_APPLET;
		boolean showLoadLocalOrRemote = SystemOptions.getInstance().getBoolean("File Import", "Show Load From Exported VFS Icon", true);
		boolean showLoadLTfileExport = SystemOptions.getInstance().getBoolean("File Import", "Show LT DB-Import-Export-Tool Import Icon", true);
		if (addLoadExportedIcons && (showLoadLocalOrRemote || showLoadLTfileExport || addLoadFilesIcon)) {
			result.add(new ActionLoadDataSet("Load dataset from local or remote storage"));
		}
		
		boolean lt = SystemOptions.getInstance().getBoolean("LT-DB", "show_icon", true);
		if (lt) {
			result.add(new ActionLTnavigation());
		}
		
		for (MongoDB m : MongoDB.getMongos()) {
			result.add(new ActionMongoExperimentsNavigation(m, false, false));
		}
		
		return result.toArray(new NavigationAction[] {});
	}
}