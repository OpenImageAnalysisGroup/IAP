package de.ipk.ag_ba.commands.experiment.scripts.Rinfo;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;

public class ScriptRinfo implements ActionScriptBasedDataProcessing {
	
	@Override
	public String getFileName() {
		return getTitle();
	}
	
	@Override
	public String getTitle() {
		return "Check R installation";
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
	
}
