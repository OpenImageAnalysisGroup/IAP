package iap.example.pipelines;

import iap.blocks.acquisition.BlLoadImages;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.postprocessing.BlSaveResultImages;
import iap.blocks.preprocessing.BlCutFromSide;
import iap.blocks.preprocessing.BlMoveImagesToMasks;
import iap.blocks.segmentation.BlKMeansVis;
import iap.blocks.segmentation.BlMorphologicalOperations;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.example.blocks.extraction.BlRootsSkeletonize;
import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;

import de.ipk.ag_ba.plugins.pipelines.AbstractPipelineTemplate;

/**
 * A template for analyzing barley roots, captured using a scanner or a camera setup.
 * (V0.5: initial pipeline, needs further refinement for additional traits)
 * 
 * @author Christian Klukas
 */
public class RootPipeline extends AbstractPipelineTemplate {
	
	@Override
	public String getTitle() {
		return "Roots Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Analyse Roots";
	}
	
	@Override
	public ImageAnalysisBlock[] getBlockList(ImageProcessorOptionsAndResults options) {
		BlKMeansVis km = new BlKMeansVis();
		{ // init settings
		
			for (CameraPosition cp : CameraPosition.values()) {
				if (cp != CameraPosition.TOP)
					continue;
				options.setCameraInfos(cp, null, null, null);
				
				options.getIntSetting(km, km.getSettingsNameForSeedColorCount(), 2);
				
				options.getBooleanSetting(km, km.getSettingsNameForForeground(0), false);
				options.getColorSetting(km, km.getSettingsNameForSeedColor(0), new Color(48, 68, 131)); // background color (blue)
				
				options.getBooleanSetting(km, km.getSettingsNameForForeground(1), true);
				options.getColorSetting(km, km.getSettingsNameForSeedColor(1), new Color(177, 162, 114)); // root color
				options.getBooleanSetting(km, km.getSettingsNameForLoop(), false);
			}
		}
		return new ImageAnalysisBlock[] {
				new BlLoadImages(),
				new BlCutFromSide(),
				new BlMoveImagesToMasks(),
				km,
				new BlMorphologicalOperations(),
				new BlRemoveSmallObjectsVisFluo(),
				new BlRootsSkeletonize(),
				new BlRunPostProcessors(),
				new BlMoveMasksToImageSet(),
				new BlSaveResultImages(),
		
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
