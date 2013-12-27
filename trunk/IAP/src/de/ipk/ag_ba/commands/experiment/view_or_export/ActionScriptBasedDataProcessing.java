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
	
	String[] getParams();
	
	String getImage();
	
	String getTooltip();
	
}
