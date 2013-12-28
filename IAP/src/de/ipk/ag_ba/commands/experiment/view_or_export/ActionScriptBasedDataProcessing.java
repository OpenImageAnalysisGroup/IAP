package de.ipk.ag_ba.commands.experiment.view_or_export;

/**
 * @author Christian Klukas
 */
public interface ActionScriptBasedDataProcessing {
	
	/**
	 * @return INI-Script-Filename (e.g. 'myscript.ini').
	 */
	String getFileName();
	
	String getTitle();
	
	String getCommand();
	
	/**
	 * @return Examples: "--version", "[int|bootstrap sample size|1000]"
	 */
	String[] getParams();
	
	String getImage();
	
	String getTooltip();
	
	String[] getWebURLs();
	
	String[] getWebUrlTitles();
	
	/**
	 * @return NULL, if no data export is needed, otherwise, the filename of the
	 *         target file (e.g. data.csv or data.xlsx).
	 */
	String getExportDataFileName();
	
	boolean allowGroupingColumnSelection();
	
	boolean allowGroupingFiltering();
	
	boolean allowSelectionOfDataColumns();
	
	String[] getDesiredDataColumns();
}
