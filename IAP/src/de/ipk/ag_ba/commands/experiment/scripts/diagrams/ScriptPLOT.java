package de.ipk.ag_ba.commands.experiment.scripts.diagrams;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;

/**
 * @author klukas
 */
public class ScriptPLOT implements ActionScriptBasedDataProcessing {
	
	@Override
	public String getFileName() {
		return getTitle();
	}
	
	@Override
	public String getTitle() {
		return "Plot Diagrams";
	}
	
	@Override
	public String getCommand() {
		return "Rscript";
	}
	
	@Override
	public String[] getParams() {
		return new String[] {
				"--vanilla",
				"--encoding=UTF-8",
				"plotdiagrams.R"
		};
	}
	
	@Override
	public String getImage() {
		return "img/plotting.png";
	}
	
	@Override
	public String getTooltip() {
		return "line plots of data for individual plant IDs";
	}
	
	@Override
	public String[] getWebURLs() {
		return new String[] {};
	}
	
	@Override
	public String[] getWebUrlTitles() {
		return new String[] {};
	}
	
	@Override
	public String getExportDataFileName() {
		return "report.csv";
	}
	
	@Override
	public boolean allowGroupingColumnSelection() {
		return true;
	}
	
	@Override
	public boolean allowGroupingFiltering() {
		return true;
	}
	
	@Override
	public boolean allowSelectionOfDataColumns() {
		return false;
	}
	
	@Override
	public String[] getDesiredDataColumns() {
		return new String[] {};
	}
	
	@Override
	public String[] getScriptFileNames() {
		return new String[] {
				"plotdiagrams.R"
		};
	}
	
	@Override
	public int getTimeoutInMin() {
		return 10;
	}
	
	@Override
	public boolean exportClusteringDataset() {
		return false;
	}
}
