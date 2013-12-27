package de.ipk.ag_ba.commands.experiment.scripts;

import java.io.File;
import java.io.IOException;

import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;

/**
 * @author klukas
 */
public class ScriptHelper {
	
	private final SystemOptions so;
	private final ActionScriptBasedDataProcessing adp;
	
	public ScriptHelper(String fn, ActionScriptBasedDataProcessing adp) throws IOException {
		this.adp = adp;
		boolean addFields = false;
		if (!new File(ReleaseInfo.getAppFolderWithFinalSep() + fn).exists()) {
			addFields = true;
		}
		this.so = SystemOptions.getInstance(fn, null);
		so.reload();
		if (addFields && adp != null) {
			so.setString("Icon Display", "title", adp.getTitle());
			so.setString("Icon Display", "tooltip", adp.getTooltip());
			so.setString("Icon Display", "icon", adp.getImage());
			so.setString("Script", "exec", adp.getCommand());
			so.setStringArray("Script", "params", StringManipulationTools.getStringListFromArray(adp.getParams()));
		}
	}
	
	public String getTitle() {
		return so.getString("Icon Display", "title", "Untitled");
	}
	
	public String getIcon() {
		return so.getString("Icon Display", "icon", "img/ext/gpl2/Gnome-Dialog-Information-64.png");
	}
	
	public String getCommand() {
		return so.getString("Script", "exec", "R");
	}
	
	public String[] getParams() {
		return so.getStringAll("Script", "params", new String[] { "--version" });
	}
}
