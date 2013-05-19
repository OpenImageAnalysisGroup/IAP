package de.ipk.ag_ba.plugins;

import iap.blocks.data_structures.ImageAnalysisBlockFIS;

import org.graffiti.plugin.GenericPlugin;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
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
	public DataSource[] getHomeDataSources();
	
	/**
	 * Override this method to provide the system information about a list
	 * of known ImageAnalysisBlocks. While arbitary blocks may be specified by the user,
	 * the blocks should also be made available to the IAP tool using this methods.
	 * The information will is used to provide the user with a list of known analysis blocks.
	 * 
	 * @return A list of image analysis blocks.
	 */
	public ImageAnalysisBlockFIS[] getImageAnalysisBlocks();
	
	/**
	 * @param experimentReference
	 *           Experiment data set (reference), the particular command will
	 *           work on the given data.
	 * @return A list of NavigationAction commands, which may process the particular experiment
	 *         dataset. The commands are shown once a experiment is loaded.
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
}
