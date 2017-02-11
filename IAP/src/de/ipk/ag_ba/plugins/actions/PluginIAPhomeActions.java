package de.ipk.ag_ba.plugins.actions;

import java.io.File;
import java.io.FilenameFilter;
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
		
		String hwiFolder = "C:\\LemnaTec\\Hardware";
		if (!new File(hwiFolder).exists()) {
			hwiFolder = System.getProperty("user.home") + File.separator + "LemnaTec" + File.separator + "Hardware";
		}
		
		boolean added = false;
		boolean scanHWI = new File(hwiFolder).exists();
		if (scanHWI) {
			boolean checkPing = SystemOptions.getInstance().getBoolean("LT-DB", "check server availability (ping)", false);
			int pingTimeout = SystemOptions.getInstance().getInteger("LT-DB", "ping timeout", 200);
			for (String fn : new File(hwiFolder).list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toUpperCase().endsWith(".XML");
				}
			})) {
				DBinfo dbInfo;
				try {
					dbInfo = new DBinfo(hwiFolder + File.separator + fn);
					if (dbInfo.getServer() == null || dbInfo.getServer().isEmpty())
						continue;
					if (dbInfo.getUser() == null || dbInfo.getUser().isEmpty())
						continue;
					if (dbInfo.getPassword() == null || dbInfo.getPassword().isEmpty())
						continue;
					if (!checkPing || dbInfo.isValid(pingTimeout)) {
						result.add(new ActionLTnavigation(dbInfo.getUser(), dbInfo.getPassword(), dbInfo.getServer(), dbInfo.getPort(), fn));
						added = true;
					}
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">DB Info not processed: " + e.getMessage());
				}
			}
		}
		
		if (!added) {
			boolean lt = SystemOptions.getInstance().getBoolean("LT-DB", "show_icon", true);
			if (lt) {
				result.add(new ActionLTnavigation());
			}
		}
		
		for (MongoDB m : MongoDB.getMongos()) {
			result.add(new ActionMongoExperimentsNavigation(m, false, false));
		}
		
		return result.toArray(new NavigationAction[] {});
	}
}