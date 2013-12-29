package de.ipk.ag_ba.commands.experiment.view_or_export;

/**
 * @author Christian Klukas
 */
public interface ActionScriptBasedDataProcessing {
	
	/**
	 * @return INI-Script-Filename (e.g. 'myscript.ini').
	 */
	String getFileName();
	
	/**
	 * @return Action command title (within command group "Scripts").
	 */
	String getTitle();
	
	String getCommand();
	
	/**
	 * Currently only integer-parameters are supported (see format example).
	 * 
	 * @return Examples: "--version", "[int|bootstrap sample size|1000]"
	 */
	String[] getParams();
	
	String getImage();
	
	/**
	 * @return Action command tooltip text.
	 */
	String getTooltip();
	
	String[] getWebURLs();
	
	String[] getWebUrlTitles();
	
	/**
	 * @return NULL, if no data export is needed, otherwise, the filename of the
	 *         target file (e.g. data.csv or data.xlsx).
	 */
	String getExportDataFileName();
	
	/**
	 * @return True, if the user should be able to define the metadata grouping. E.g. comparison of genotypes or of different treatments, or both,
	 *         on a case-by-case basis.
	 */
	boolean allowGroupingColumnSelection();
	
	/**
	 * @return True, if the user should be able to reduce the dataset conditions.
	 */
	boolean allowGroupingFiltering();
	
	/**
	 * @return True, if the defined data columns may be modified (by un-selection) by the user.
	 */
	boolean allowSelectionOfDataColumns();
	
	/**
	 * @return Format: preferred column name (without unit) : Description (visible title in GUI) // optional column name : Description
	 *         The '//' and the optional column name is normally omitted but can be used to utilize one of two or more columns. If the first
	 *         defined column can't be found, the second one in this single definition is used, and so on.
	 */
	String[] getDesiredDataColumns();
}
