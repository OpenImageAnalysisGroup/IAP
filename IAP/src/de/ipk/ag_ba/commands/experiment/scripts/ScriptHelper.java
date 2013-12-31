package de.ipk.ag_ba.commands.experiment.scripts;

import java.io.File;

import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.commands.experiment.scripts.helperClasses.FileSaver;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;

/**
 * @author klukas
 */
public class ScriptHelper {
	
	private final SystemOptions so;
	
	public ScriptHelper(String fn, ActionScriptBasedDataProcessing adp) throws Exception {
		boolean addFields = false;
		if (!new File(new File(ReleaseInfo.getAppFolderWithFinalSep() + fn).getParent()).exists()) {
			new File(new File(ReleaseInfo.getAppFolderWithFinalSep() + fn).getParent()).mkdir();
		}
		if (!new File(ReleaseInfo.getAppFolderWithFinalSep() + fn).exists()) {
			addFields = true;
		}
		this.so = SystemOptions.getInstance(fn, null);
		so.reload();
		if (adp != null && adp.getScriptFileNames() != null && adp.getScriptFileNames().length > 0) {
			File fileFolder = new File(ReleaseInfo.getAppFolderWithFinalSep() + fn).getParentFile();
			FileSaver.saveScripts(adp, StringManipulationTools.getStringListFromArray(adp.getScriptFileNames()), fileFolder);
		}
		if (addFields && adp != null) {
			so.setString("Icon Display", "title", adp.getTitle());
			so.setString("Icon Display", "tooltip", adp.getTooltip());
			so.setString("Icon Display", "icon", adp.getImage());
			so.setString("Script", "exec", adp.getCommand());
			so.setInteger("Script", "timeout-min", adp.getTimeoutInMin());
			so.setStringArray("Script", "params", StringManipulationTools.getStringListFromArray(adp.getParams()));
			so.setStringArray("Script", "files", StringManipulationTools.getStringListFromArray(adp.getScriptFileNames()));
			so.setStringArray("Reference Infos", "urls", StringManipulationTools.getStringListFromArray(adp.getWebURLs()));
			so.setStringArray("Reference Infos", "url titles", StringManipulationTools.getStringListFromArray(adp.getWebUrlTitles()));
			so.setString("Parameter", "input file name", adp.getExportDataFileName());
			so.setBoolean("Parameter", "allow group column selection", adp.allowGroupingColumnSelection());
			so.setBoolean("Parameter", "allow group filtering", adp.allowGroupingFiltering());
			so.setBoolean("Parameter", "allow data column modification", adp.allowSelectionOfDataColumns());
			so.setStringArray("Data Columns", "columns", StringManipulationTools.getStringListFromArray(adp.getDesiredDataColumns()));
		} else
			if (adp != null) {
				so.getString("Icon Display", "title", adp.getTitle());
				so.getString("Icon Display", "tooltip", adp.getTooltip());
				so.getString("Icon Display", "icon", adp.getImage());
				so.getString("Script", "exec", adp.getCommand());
				so.getInteger("Script", "timeout-min", adp.getTimeoutInMin());
				so.getStringAll("Script", "files", adp.getScriptFileNames());
				so.getStringAll("Script", "params", adp.getParams());
				so.getStringAll("Reference Infos", "urls", adp.getWebURLs());
				so.getStringAll("Reference Infos", "url titles", adp.getWebUrlTitles());
				so.getString("Parameter", "input file name", adp.getExportDataFileName());
				so.getBoolean("Parameter", "allow group column selection", adp.allowGroupingColumnSelection());
				so.getBoolean("Parameter", "allow group filtering", adp.allowGroupingFiltering());
				so.getBoolean("Parameter", "allow data column modification", adp.allowSelectionOfDataColumns());
				so.getStringAll("Data Columns", "columns", adp.getDesiredDataColumns());
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
	
	public String[] getDesiredDataColumns() {
		return so.getStringAll("Data Columns", "columns", new String[] {});
	}
	
	public String[] getExportScriptFileNames() {
		return so.getStringAll("Script", "files", new String[] {});
	}
	
	public int getTimeoutInMinutes() {
		return so.getInteger("Script", "timeout-min", -1);
	}
}
