package de.ipk.ag_ba.plugins.pipelines.detached_leafs;

import iap.blocks.acquisition.BlLoadImagesIfNeeded;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlCalcAreas;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.preprocessing.BlMoveImagesToMasks;
import iap.blocks.preprocessing.BlObjectSeparator;
import iap.blocks.segmentation.BlClosing;
import iap.blocks.segmentation.BlKMeansVis;
import iap.pipelines.ImageProcessorOptions;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

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
		return "Detached Leafs Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Analyse Detached Leafs Images";
	}
	
	@Override
	public ImageAnalysisBlock[] getBlockList(ImageProcessorOptions options) {
		{ // init settings
			BlKMeansVis km = new BlKMeansVis();
			for (CameraPosition cp : CameraPosition.values()) {
				if (cp != CameraPosition.TOP)
					continue;
				options.setCameraInfos(cp, null, null);
				
				options.getIntSetting(km, km.getSettingsNameForSeedColorCount(), 2);
				
				options.getBooleanSetting(km, km.getSettingsNameForForeground(0), false);
				options.getColorSetting(km, km.getSettingsNameForSeedColor(0), Color.WHITE);
				
				options.getBooleanSetting(km, km.getSettingsNameForForeground(1), true);
				options.getColorSetting(km, km.getSettingsNameForSeedColor(1), new Color(226, 255, 226)); // light green
			}
		}
		return new ImageAnalysisBlock[] {
				new BlLoadImagesIfNeeded(),
				new BlMoveImagesToMasks(),
				new BlKMeansVis(),
				new BlClosing(),
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
		return false;
	}
}
