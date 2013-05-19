package de.ipk.ag_ba.plugins;

import iap.blocks.data_structures.ImageAnalysisBlockFIS;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.GenericPluginAdapter;

import de.ipk.ag_ba.commands.analysis.ExperimentDataNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author Christian Klukas
 */
public class AbstractIAPplugin extends GenericPluginAdapter implements
		IAPplugin {
	
	@Override
	public NavigationAction[] getHomeNavigationActions() {
		return new NavigationAction[] {};
	}
	
	@Override
	public DataSource[] getHomeDataSources() {
		return new DataSource[] {};
	}
	
	@Override
	public ExperimentDataNavigationAction[] getExperimentDataNavigationActions() {
		return new ExperimentDataNavigationAction[] {};
	}
	
	@Override
	public ImageAnalysisBlockFIS[] getImageAnalysisBlocks() {
		return new ImageAnalysisBlockFIS[] {};
	}
	
	public ImageIcon getIcon() {
		return getIAPicon();
	}
	
	public static ImageIcon getIAPicon() {
		try {
			return new ImageIcon(GravistoService.loadImage(IAPmain.class, "img/favicon.ico", 48, 48));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	@Override
	public ActionDataProcessing[] getDataProcessingActions(ExperimentReference er) {
		return new ActionDataProcessing[] {};
	}
}
