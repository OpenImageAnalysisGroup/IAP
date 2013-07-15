package de.ipk.ag_ba.plugins.pipelines.roots;

import iap.blocks.acquisition.BlLoadImagesIfNeeded;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.unused.BlMoveImagesToMasks;
import iap.blocks.unused.BlRootsAddBorderAroundImage;
import iap.blocks.unused.BlRootsRemoveBoxAndNoise;
import iap.blocks.unused.BlRootsSharpenImage;
import iap.blocks.unused.BlRootsSkeletonize;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;

/**
 * A template for analyzing washed barley roots, scanned using a scanner.
 * (V0.5: initial pipeline, needs further refinement for additional traits)
 * 
 * @author Christian Klukas
 */
public class RootScannPipeline implements AnalysisPipelineTemplate {
	
	@Override
	public String getTitle() {
		return "Scanned Roots Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Analyse Scanned Roots";
	}
	
	@Override
	public ImageAnalysisBlock[] getBlockList() {
		return new ImageAnalysisBlock[] {
				new BlLoadImagesIfNeeded(),
				new BlRootsAddBorderAroundImage(),
				new BlMoveImagesToMasks(),
				new BlRootsSharpenImage(),
				new BlRootsRemoveBoxAndNoise(),
				new BlRootsSkeletonize(),
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
