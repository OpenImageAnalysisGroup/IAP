package tests.plugins.pipelines.tobacco;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlDetectLeafTips;
import iap.blocks.extraction.BlTrackLeafTips;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Jean-Michel Pape
 */
public class PluginIAPTobaccoAnalyisPipelines extends AbstractIAPplugin {
	
	@Override
	public ImageAnalysisBlock[] getImageAnalysisBlocks() {
		return new ImageAnalysisBlock[] {
				new BlDetectLeafTips(),
				new BlTrackLeafTips()
		};
	}
	
	public PluginIAPTobaccoAnalyisPipelines() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP advanced leaf analysis plugin is beeing loaded");
	}
	
	@Override
	public AnalysisPipelineTemplate[] getAnalysisTemplates() {
		return new AnalysisPipelineTemplate[] {
		
		};
	}
	
	@Override
	public NavigationAction[] getHomeNavigationActions() {
		return new NavigationAction[] {
		
		};
	}
	
}