package de.ipk.ag_ba.plugins;

import iap.blocks.data_structures.ImageAnalysisBlockFIS;

import org.graffiti.plugin.GenericPlugin;

import de.ipk.ag_ba.commands.analysis.ExperimentDataNavigationAction;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;

/**
 * @author Christian Klukas
 */
public interface IAPplugin extends GenericPlugin {
	public NavigationAction[] getHomeNavigationActions();

	public ExperimentDataNavigationAction[] getExperimentDataNavigationActions();

	public ImageAnalysisBlockFIS[] getImageAnalysisBlocks();
	
}
