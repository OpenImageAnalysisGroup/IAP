package de.ipk.ag_ba.commands.experiment.scripts.Rinfo;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;

/**
 * @author klukas
 */
public class ScriptRinfo implements ActionScriptBasedDataProcessing {
	
	@Override
	public String getFileName() {
		return getTitle();
	}
	
	@Override
	public String getTitle() {
		return "Check R Installation";
	}
	
	@Override
	public String getCommand() {
		return "R";
	}
	
	@Override
	public String[] getParams() {
		return new String[] { "--version" };
	}
	
	@Override
	public String getImage() {
		return "img/ext/gpl2/Gnome-Dialog-Information-64.png";
	}
	
	@Override
	public String getTooltip() {
		return "Check R stat installation";
	}
	
	@Override
	public String[] getWebURLs() {
		return new String[] { "http://www.r-project.org" };
	}
	
	@Override
	public String[] getWebUrlTitles() {
		return new String[] { "R Project Website" };
	}
	
	@Override
	public String getExportDataFileName() {
		return null;
	}
	
	@Override
	public boolean allowGroupingColumnSelection() {
		return false;
	}
	
	@Override
	public boolean allowGroupingFiltering() {
		return false;
	}
	
	@Override
	public boolean allowSelectionOfDataColumns() {
		return false;
	}
	
	@Override
	public String[] getDesiredDataColumns() {
		return null;
	}
	
	@Override
	public String[] getScriptFileNames() {
		return null;
	}
	
	@Override
	public int getTimeoutInMin() {
		return 1;
	}
}
