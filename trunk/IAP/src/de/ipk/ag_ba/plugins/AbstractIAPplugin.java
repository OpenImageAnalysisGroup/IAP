package de.ipk.ag_ba.plugins;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.pipelines.ImageProcessorOptionsAndResults;

import java.util.Collection;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.GenericPluginAdapter;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;

/**
 * @author Christian Klukas
 */
public class AbstractIAPplugin extends GenericPluginAdapter implements IAPplugin {
	
	@Override
	public NavigationAction[] getHomeNavigationActions() {
		return new NavigationAction[] {};
	}
	
	@Override
	public DataSource[] getDataSources() {
		return new DataSource[] {};
	}
	
	@Override
	public ImageAnalysisBlock[] getImageAnalysisBlocks() {
		if (getAnalysisTemplates() == null || getAnalysisTemplates().length == 0)
			return new ImageAnalysisBlock[] {};
		else {
			TreeSet<ImageAnalysisBlock> res = new TreeSet<ImageAnalysisBlock>();
			for (AnalysisPipelineTemplate t : getAnalysisTemplates()) {
				SystemOptions options = null;
				ImageProcessorOptionsAndResults ipo = new ImageProcessorOptionsAndResults(options, null, null);
				try {
					ImageAnalysisBlock[] bl = t.getBlockList(ipo);
					if (bl != null)
						for (ImageAnalysisBlock b : bl) {
							if (b != null)
								res.add(b);
						}
				} catch (Error e) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage() + " // template: " + t.getTitle());
					ErrorMsg.addErrorMessage(e.getMessage() + " // template: " + t.getTitle());
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage() + " // template: " + t.getTitle());
					ErrorMsg.addErrorMessage(e);
				}
			}
			return res.toArray(new ImageAnalysisBlock[] {});
		}
	}
	
	@Override
	public ImageIcon getIcon() {
		return getIAPicon();
	}
	
	public static ImageIcon getIAPicon() {
		try {
			return IAPimages.getIcon("img/public.png", 572, 572);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	@Override
	public ActionDataProcessing[] getDataProcessingActions(ExperimentReferenceInterface er) {
		return new ActionDataProcessing[] {};
	}
	
	@Override
	public ActionDataProcessing[] getDataProcessingTools(ExperimentReferenceInterface er) {
		return new ActionDataProcessing[] {};
	}
	
	@Override
	public Collection<String> getHelpForSettings(String iniFileName, String section, String setting) {
		return null;
	}
	
	@Override
	public AnalysisPipelineTemplate[] getAnalysisTemplates() {
		return new AnalysisPipelineTemplate[] {};
	}
	
	@Override
	public ActionScriptBasedDataProcessing[] getScriptBasedDataProcessingTools(ExperimentReferenceInterface experimentReference) {
		return new ActionScriptBasedDataProcessing[] {};
	}
}
