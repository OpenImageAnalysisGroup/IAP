package de.ipk.ag_ba.plugins.pipelines.arabidopsis;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.postprocessing.BlDrawSkeleton;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.preprocessing.BlAlign;
import iap.blocks.preprocessing.BlColorBalanceVerticalFluo;
import iap.blocks.preprocessing.BlColorBalanceVerticalVis;
import iap.blocks.preprocessing.BlRotate;
import iap.blocks.segmentation.BlIntensityCalculationFluo;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.blocks.unused.BlClearMasks_Arabidopsis_PotAndTrayProcessing;
import iap.blocks.unused.BlCrop;
import iap.blocks.unused.BlIRdiff;
import iap.blocks.unused.BlLabFilterDepr;
import iap.blocks.unused.BlLoadImagesIfNeeded;
import iap.blocks.unused.BlMedianFilter;
import iap.blocks.unused.BlMoveImagesToMasks;
import iap.blocks.unused.BlUseFluoMaskToClearIr;
import iap.blocks.unused.BlUseFluoMaskToClearNir_Arabidopsis;
import iap.blocks.unused.BlUseFluoMaskToClear_Arabidopsis_vis;
import iap.blocks.unused.BlockSkeletonize_Arabidopsis;
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
				new BlClearMasks_Arabidopsis_PotAndTrayProcessing(),
				new BlMoveImagesToMasks(),
				new BlLabFilterDepr(),
				new BlIntensityCalculationFluo(),
				new BlMedianFilter(),
				new BlRemoveSmallObjectsVisFluo(),
				new BlUseFluoMaskToClear_Arabidopsis_vis(),
				new BlUseFluoMaskToClearNir_Arabidopsis(),
				new BlIRdiff(),
				new BlUseFluoMaskToClearIr(),
				new BlockSkeletonize_Arabidopsis(),
				new BlCalcWidthAndHeight(),
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
