package de.ipk.ag_ba.plugins;

import iap.blocks.data_structures.ImageAnalysisBlock;

import java.util.Collection;

import org.graffiti.plugin.GenericPlugin;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;

/**
 * IAP extension API. Use AbstractIAPplugin to more easily provide a subset of all possible new
 * feature types.
 * 
 * @author Christian Klukas
 */
public interface IAPplugin extends GenericPlugin {
	
	/**
	 * @return A list of NavigationAction commands, which is shown to the user
	 *         in the beginning and after clicking 'Start'.
	 */
	public NavigationAction[] getHomeNavigationActions();
	
	/**
	 * @return A list of remote data source providers for pathways, web-links or other
	 *         types of datasets.
	 */
	public DataSource[] getDataSources();
	
	/**
	 * Override this method to provide the system information about a list
	 * of known ImageAnalysisBlocks. While arbitrary blocks may be specified by the user,
	 * the blocks should also be made available to the IAP tool using this methods.
	 * The information will is used to provide the user with a list of known analysis blocks.
	 * 
	 * @return A list of image analysis blocks.
	 */
	public ImageAnalysisBlock[] getImageAnalysisBlocks();
	
	/**
	 * @param experimentReference
	 *           Experiment data set (reference), the particular command will
	 *           work on the given data.
	 * @return A list of NavigationAction commands, which may process the particular experiment
	 *         data set. The commands are shown once a experiment is loaded.
	 */
	public ActionDataProcessing[] getDataProcessingActions(ExperimentReference experimentReference);
	
	/**
	 * @param experimentReference
	 *           Experiment data set (reference), the particular command will
	 *           work on the given data.
	 * @return A list of NavigationAction commands, which may process the particular experiment
	 *         dataset. The commands are shown after the user clicks the Tool command when a experiment is loaded.
	 */
	public ActionDataProcessing[] getDataProcessingTools(ExperimentReference experimentReference);
	
	/**
	 * Override this method to provide help text(s), displayed by the settings editor for a specific
	 * settings group.
	 * 
	 * @param iniFileName
	 *           The settings iniFile or null, if this variable should not be decisive for the filtering.
	 * @param section
	 *           The settings section or null, if this variable should not be decisive for the filtering.
	 * @param setting
	 *           The setting for which help is returned.
	 * @return Null (no custom help), or one or more setting text paragraphs. You may use HTML tags, to format the help text.
	 */
	public Collection<String> getHelpForSettings(String iniFileName, String section, String setting);
	
	/**
	 * Override this method to provide additional analysis templates to the user. These templates
	 * are automatically saved and may be modified by the user. A stored template (.pipeline.ini) file
	 * will not be updated or changed, if the add-on for a plugin provides a new version of a template.
	 * 
	 * @return A list of templates (containing a block list, a pipeline name and description, which will be
	 *         available to the user.
	 */
	public AnalysisPipelineTemplate[] getAnalysisTemplates();
}
