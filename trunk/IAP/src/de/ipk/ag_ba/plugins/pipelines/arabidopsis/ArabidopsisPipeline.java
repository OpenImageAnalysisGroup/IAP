package de.ipk.ag_ba.plugins.pipelines.arabidopsis;

import iap.blocks.acquisition.BlLoadImages;
import iap.blocks.auto.BlAdaptiveSegmentationFluo;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlCalcAreas;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.extraction.BlSkeletonizeVisFluo;
import iap.blocks.postprocessing.BlCrop;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.preprocessing.BlAlign;
import iap.blocks.preprocessing.BlClearMasks_WellProcessing;
import iap.blocks.preprocessing.BlColorBalanceVerticalFluo;
import iap.blocks.preprocessing.BlColorBalanceVerticalVis;
import iap.blocks.preprocessing.BlColorCorrectionNir;
import iap.blocks.preprocessing.BlMoveImagesToMasks;
import iap.blocks.preprocessing.BlRotate;
import iap.blocks.segmentation.BlIRdiff;
import iap.blocks.segmentation.BlKMeansVis;
import iap.blocks.segmentation.BlMedianFilter;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.blocks.segmentation.BlUseFluoMaskToClearIr;
import iap.blocks.segmentation.BlUseFluoMaskToClearNir;
import iap.blocks.segmentation.BlUseFluoMaskToClearVis;
import iap.pipelines.ImageProcessorOptionsAndResults;
import de.ipk.ag_ba.plugins.pipelines.AbstractPipelineTemplate;

/**
 * A workflow for analyzing Arabidopsis plants. Top-Images may be split into several peaces,
 * if a grid of wells is used (change settings for BlClearMasks_Arabidopsis_PotAndTrayProcessing).
 * (V0.9: optimization not completed)
 * 
 * @author Christian Klukas
 */
public class ArabidopsisPipeline extends AbstractPipelineTemplate {
	
	@Override
	public String getTitle() {
		return "Arabidopsis Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Analyze Arabidopsis Phenotype";
	}
	
	@Override
	public ImageAnalysisBlock[] getBlockList(ImageProcessorOptionsAndResults options) {
		return new ImageAnalysisBlock[] {
				new BlLoadImages(),
				new BlColorBalanceVerticalVis(),
				new BlColorBalanceVerticalFluo(),
				new BlColorCorrectionNir(),
				new BlRotate(),
				new BlAlign(),
				new BlClearMasks_WellProcessing(),
				new BlMoveImagesToMasks(),
				
				new BlKMeansVis(),
				new BlAdaptiveSegmentationFluo(),
				new BlMedianFilter(),
				new BlUseFluoMaskToClearVis(),
				new BlRemoveSmallObjectsVisFluo(),
				new BlUseFluoMaskToClearVis(),
				new BlUseFluoMaskToClearNir(),
				new BlIRdiff(),
				new BlUseFluoMaskToClearIr(),
				
				new BlSkeletonizeVisFluo(),
				new BlCalcWidthAndHeight(),
				new BlCalcAreas(),
				new BlCalcColorHistograms(),
				new BlCalcConvexHull(),
				
				new BlRunPostProcessors(),
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
