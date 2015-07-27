package iap.example.addon;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.preprocessing.BlObjectSeparator;
import iap.example.blocks.extraction.BlRootsSkeletonize;
import iap.example.blocks.preprocessing.BlRootsAddBorderAroundImage;
import iap.example.blocks.segmentation.BlRootsRemoveBoxAndNoise;
import iap.example.blocks.segmentation.BlRootsSharpenImage;
import iap.example.pipelines.DetachedLeafsPipeline;
import iap.example.pipelines.RootPipeline;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.SystemAnalysis;
import org.graffiti.editor.GravistoService;

import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;

/**
 * @author Christian Klukas
 */
public class PluginIAPexampleAddOn extends AbstractIAPplugin {
	
	public PluginIAPexampleAddOn() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP example add-on is beeing loaded");
	}
	
	@Override
	public AnalysisPipelineTemplate[] getAnalysisTemplates() {
		return new AnalysisPipelineTemplate[] {
				new RootPipeline(),
				new DetachedLeafsPipeline()
		};
	}
	
	@Override
	public ImageAnalysisBlock[] getImageAnalysisBlocks() {
		ImageAnalysisBlock[] fromPipelines = super.getImageAnalysisBlocks();
		
		ImageAnalysisBlock[] additionalBlocks = new ImageAnalysisBlock[] {
				new BlRootsAddBorderAroundImage(),
				new BlRootsRemoveBoxAndNoise(),
				new BlRootsSharpenImage(),
				new BlRootsSkeletonize(),
				new BlObjectSeparator(),
				new BlRootsAddBorderAroundImage()
		};
		
		ImageAnalysisBlock[] res = new ImageAnalysisBlock[fromPipelines.length + additionalBlocks.length];
		int idx = 0;
		for (ImageAnalysisBlock b : fromPipelines)
			res[idx++] = b;
		for (ImageAnalysisBlock b : additionalBlocks)
			res[idx++] = b;
		
		return res;
	}
	
	@Override
	public ImageIcon getIcon() {
		try {
			return new ImageIcon(GravistoService.getResource(this.getClass(), "icon", "png"));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return super.getIcon();
		}
	}
}