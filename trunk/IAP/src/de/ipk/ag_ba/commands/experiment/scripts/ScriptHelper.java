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
	
	public ScriptHelper(String fn, ActionScriptBasedDataProcessing adp) throws IOException {
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
			so.setStringArray("Reference Infos", "urls", StringManipulationTools.getStringListFromArray(adp.getWebURLs()));
			so.setStringArray("Reference Infos", "url titles", StringManipulationTools.getStringListFromArray(adp.getWebUrlTitles()));
			so.setString("Parameter", "input file name", adp.getExportDataFileName());
			so.setBoolean("Parameter", "allow group column selection", adp.allowGroupingColumnSelection());
			so.setBoolean("Parameter", "allow group filtering", adp.allowGroupingFiltering());
			so.setBoolean("Parameter", "allow data column modification", adp.allowSelectionOfDataColumns());
		} else
			if (adp != null) {
				so.getString("Icon Display", "title", adp.getTitle());
				so.getString("Icon Display", "tooltip", adp.getTooltip());
				so.getString("Icon Display", "icon", adp.getImage());
				so.getString("Script", "exec", adp.getCommand());
				so.getStringAll("Script", "params", adp.getParams());
				so.getStringAll("Reference Infos", "urls", adp.getWebURLs());
				so.getStringAll("Reference Infos", "url titles", adp.getWebUrlTitles());
				so.getString("Parameter", "input file name", adp.getExportDataFileName());
				so.getBoolean("Parameter", "allow group column selection", adp.allowGroupingColumnSelection());
				so.getBoolean("Parameter", "allow group filtering", adp.allowGroupingFiltering());
				so.getBoolean("Parameter", "allow data column modification", adp.allowSelectionOfDataColumns());
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
	
	public String[] getWebURLs() {
		return so.getStringAll("Reference Infos", "urls", new String[] {});
	}
	
	public String[] getWebUrlTitles() {
		return so.getStringAll("Reference Infos", "url titles", new String[] {});
	}
	
	public String getExportFileName() {
		return so.getString("Parameter", "input file name", "");
	}
	
	public boolean isAllowGroupColumnSelection() {
		return so.getBoolean("Parameter", "allow group column selection", false);
	}
	
	public boolean isAllowGroupFiltering() {
		return so.getBoolean("Parameter", "allow group filtering", false);
	}
	
	public boolean isAllowDataColumnSelection() {
		return so.getBoolean("Parameter", "allow data column modification", false);
	}
	
	public String getTooltip() {
		return so.getString("Icon Display", "tooltip", "[No tooltip text defined]");
	}
}
