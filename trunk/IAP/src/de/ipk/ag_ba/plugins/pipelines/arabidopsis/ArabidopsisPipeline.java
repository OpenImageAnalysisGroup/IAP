package de.ipk.ag_ba.plugins.pipelines.arabidopsis;

import iap.blocks.acquisition.BlLoadImagesIfNeeded;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlCalcAreas;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.extraction.BlSkeletonize_Arabidopsis;
import iap.blocks.postprocessing.BlCrop;
import iap.blocks.postprocessing.BlDrawSkeleton;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.preprocessing.BlAlign;
import iap.blocks.preprocessing.BlClearMasks_WellProcessing;
import iap.blocks.preprocessing.BlColorBalanceVerticalFluo;
import iap.blocks.preprocessing.BlColorBalanceVerticalVis;
import iap.blocks.preprocessing.BlRotate;
import iap.blocks.segmentation.BlIRdiff;
import iap.blocks.segmentation.BlIntensityCalculationFluo;
import iap.blocks.segmentation.BlLabFilter;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.blocks.segmentation.BlUseFluoMaskToClearIr;
import iap.blocks.segmentation.BlUseFluoMaskToClearNir;
import iap.blocks.segmentation.BlUseFluoMaskToClearVis;
import iap.blocks.unused.BlMedianFilter;
import iap.blocks.unused.BlMoveImagesToMasks;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;

/**
 * A workflow for analyzing Arabidopsis plants. Top-Images may be split into several peaces,
 * if a grid of wells is used (change settings for BlClearMasks_Arabidopsis_PotAndTrayProcessing).
 * (V0.9: optimization not completed)
 * 
 * @author Christian Klukas
 */
public class ArabidopsisPipeline implements AnalysisPipelineTemplate {
	
	@Override
	public String getTitle() {
		return "Arabidopsis Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Analyze Arabidopsis Phenotype";
	}
	
	@Override
	public ImageAnalysisBlock[] getBlockList() {
		return new ImageAnalysisBlock[] {
				new BlLoadImagesIfNeeded(),
				new BlColorBalanceVerticalFluo(),
				new BlColorBalanceVerticalVis(),
				new BlRotate(),
				new BlAlign(),
				new BlClearMasks_WellProcessing(),
				new BlMoveImagesToMasks(),
				new BlLabFilter(),
				new BlIntensityCalculationFluo(),
				new BlMedianFilter(),
				new BlRemoveSmallObjectsVisFluo(),
				new BlUseFluoMaskToClearVis(),
				new BlUseFluoMaskToClearNir(),
				new BlIRdiff(),
				new BlUseFluoMaskToClearIr(),
				new BlSkeletonize_Arabidopsis(),
				new BlCalcWidthAndHeight(),
				new BlCalcAreas(),
				new BlCalcColorHistograms(),
				new BlCalcConvexHull(),
				// postprocessing
				new BlRunPostProcessors(),
				new BlDrawSkeleton(),
				new BlMoveMasksToImageSet(),
				new BlCrop(),
				new BlHighlightNullResults()
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
