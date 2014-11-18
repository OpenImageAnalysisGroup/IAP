package de.ipk.ag_ba.commands.experiment.scripts.outlier;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;

/**
 * @author klukas
 */
public class ScriptOutlierRemoval implements ActionScriptBasedDataProcessing {
	
	@Override
	public String getFileName() {
		return getTitle();
	}
	
	@Override
	public String getTitle() {
		return "Grubbs' Test for Outliers";
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
				"grubbs.test.R",
				"[float|alpha|0.01|"
						+
						"Please specify the desired alpha value. Default is 0.01, "
						+ "by accepting a false discovery rate of 0.05 more data points will be regarded as outliers.]"
		};
	}
	
	@Override
	public String getImage() {
		return "img/ext/gpl2/Gnome-Document-Properties-64.png";
	}
	
	@Override
	public String getTooltip() {
		return "Auto-remove outliers, based on Grubbs outlier test";
	}
	
	@Override
	public String[] getWebURLs() {
		return new String[] {
				"http://cran.fhcrc.org/web/packages/outliers/outliers.pdf"
		};
	}
	
	@Override
	public String[] getWebUrlTitles() {
		return new String[] {
				"R 'outlier' package description"
		};
	}
	
	@Override
	public String getExportDataFileName() {
		return "report.csv";
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
		return new String[] {};
	}
	
	@Override
	public String[] getScriptFileNames() {
		return new String[] {
				"grubbs.test.R"
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
