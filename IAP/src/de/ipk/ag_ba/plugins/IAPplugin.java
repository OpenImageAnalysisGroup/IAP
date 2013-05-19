package de.ipk.ag_ba.plugins;

import iap.blocks.data_structures.ImageAnalysisBlockFIS;

import org.graffiti.plugin.GenericPlugin;

import de.ipk.ag_ba.commands.analysis.ExperimentDataNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
 * @author Christian Klukas
 */
public interface IAPplugin extends GenericPlugin {
	public NavigationAction[] getHomeNavigationActions();
	
	public DataSource[] getHomeDataSources();
	
	public ExperimentDataNavigationAction[] getExperimentDataNavigationActions();
	
	public ImageAnalysisBlockFIS[] getImageAnalysisBlocks();
	
	public ActionDataProcessing[] getDataProcessingActions(ExperimentReference er);
	
}
