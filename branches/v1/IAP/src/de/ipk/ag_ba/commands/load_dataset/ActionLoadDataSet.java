package de.ipk.ag_ba.commands.load_dataset;

import java.util.ArrayList;

import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionLoadLTexportFileHierarchy;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public class ActionLoadDataSet extends AbstractNavigationAction {
	
	private NavigationButton src;
	
	public ActionLoadDataSet(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		boolean showLoadLocalOrRemote = SystemOptions.getInstance().getBoolean("File Import", "Show Load From Exported VFS Icon", true);
		boolean showLoadLTfileExport = IAPoptions.getInstance().getBoolean("File Import", "Show LT DB-Import-Export-Tool Import Icon", true);
		boolean vfs = IAPoptions.getInstance().getBoolean("VFS", "enabled", false);
		
		if (showLoadLTfileExport) {
			NavigationButton ltl = new NavigationButton(
					new ActionLoadLTexportFileHierarchy("Load DB-Import-Export Tool dataset from folder hierarchy"),
					src != null ? src.getGUIsetting() : guiSetting);
			res.add(ltl);
		}
		
		if (showLoadLocalOrRemote)
			res.add(new NavigationButton(new ActionDataLoadingFromUserSelectedFileSystemFolder(
					"Load dataset from local file system"), src.getGUIsetting()));
		
		if (vfs)
			res.add(new NavigationButton(new ActionDataLoadingFromUserSelectedVFStarget(
					"Load dataset from remote server"), src.getGUIsetting()));
		
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Load Dataset";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Document-Open-64.png";
	}
	
}
