package iap.example.pipelines;

import iap.blocks.acquisition.BlLoadImagesIfNeeded;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlCalcAreas;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.preprocessing.BlMoveImagesToMasks;
import iap.blocks.preprocessing.BlObjectSeparator;
import iap.blocks.segmentation.BlKMeansVis;
import iap.blocks.segmentation.BlMorphologicalOperations;
import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;

import de.ipk.ag_ba.plugins.pipelines.AbstractPipelineTemplate;

/**
 * A template for analyzing detached barley leafs.
 * A white background is initially assumed to be used.
 * 
 * @author Christian Klukas
 */
public class DetachedLeafsPipeline extends AbstractPipelineTemplate {
	
	@Override
	public String getTitle() {
		return "Detached Leaves Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Analyse Detached Leaves Images";
	}
	
	@Override
	public ImageAnalysisBlock[] getBlockList(ImageProcessorOptionsAndResults options) {
		{ // init settings
			BlKMeansVis km = new BlKMeansVis();
			BlMorphologicalOperations mo = new BlMorphologicalOperations();
			for (CameraPosition cp : CameraPosition.values()) {
				if (cp != CameraPosition.TOP)
					continue;
				options.setCameraInfos(cp, null, null, null);
				
				options.getIntSetting(km, km.getSettingsNameForSeedColorCount(), 4);
				
				options.getBooleanSetting(km, km.getSettingsNameForForeground(0), false);
				options.getColorSetting(km, km.getSettingsNameForSeedColor(0), Color.WHITE);
				
				options.getBooleanSetting(km, km.getSettingsNameForForeground(1), true);
				options.getColorSetting(km, km.getSettingsNameForSeedColor(1), new Color(200, 229, 130)); // khaki
				
				options.getBooleanSetting(km, km.getSettingsNameForForeground(2), true);
				options.getColorSetting(km, km.getSettingsNameForSeedColor(2), new Color(204, 96, 64)); // tomato
				
				options.getBooleanSetting(km, km.getSettingsNameForForeground(3), true);
				options.getColorSetting(km, km.getSettingsNameForSeedColor(3), new Color(102, 153, 0)); // olivedrab
				
				options.getIntSetting(mo, cp + " Step 1 Erode Count", 0);
				options.getIntSetting(mo, cp + " Step 2 Dilate Count", 3);
				options.getIntSetting(mo, cp + " Step 3 Erode Count", 3);
			}
		}
		return new ImageAnalysisBlock[] {
				new BlLoadImagesIfNeeded(),
				new BlMoveImagesToMasks(),
				new BlKMeansVis(),
				new BlMorphologicalOperations(),
				new BlObjectSeparator(),
				new BlCalcColorHistograms(),
				new BlCalcAreas(),
				new BlRunPostProcessors(),
				new BlMoveMasksToImageSet()
		};
	}
	
	@Override
	public boolean analyzeTopImages() {
		return true;
	}
	
	@Override
	public boolean analyzeSideImages() {
		return true;
	}
}
