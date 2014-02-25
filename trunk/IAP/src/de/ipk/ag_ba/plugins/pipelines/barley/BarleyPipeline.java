package de.ipk.ag_ba.plugins.pipelines.barley;

import iap.blocks.acquisition.BlCreateDummyReferenceIfNeeded;
import iap.blocks.acquisition.BlLoadImages;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlCalcCOG;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcMainAxis;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.extraction.BlLeafCurlingAnalysis;
import iap.blocks.extraction.BlSkeletonizeNir;
import iap.blocks.extraction.BlSkeletonizeVisFluo;
import iap.blocks.postprocessing.BlCrop;
import iap.blocks.postprocessing.BlDrawSkeleton;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.preprocessing.BlAlign;
import iap.blocks.preprocessing.BlColorBalanceCircularVisNir;
import iap.blocks.preprocessing.BlColorBalanceVerticalFluo;
import iap.blocks.preprocessing.BlColorBalanceVerticalNir;
import iap.blocks.preprocessing.BlColorBalanceVerticalVis;
import iap.blocks.preprocessing.BlCutFromSide;
import iap.blocks.preprocessing.BlDetectBlueMarkers;
import iap.blocks.segmentation.BlAdaptiveThresholdNir;
import iap.blocks.segmentation.BlClosing;
import iap.blocks.segmentation.BlCopyImagesApplyMask;
import iap.blocks.segmentation.BlIRdiff;
import iap.blocks.segmentation.BlIntensityCalculationFluo;
import iap.blocks.segmentation.BlLabFilter;
import iap.blocks.segmentation.BlMedianFilter;
import iap.blocks.segmentation.BlRemoveBackground;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.blocks.segmentation.BlUseFluoMaskToClearOther;
import iap.blocks.segmentation.BlockClearNirPotFromNir;
import iap.pipelines.ImageProcessorOptionsAndResults;
import de.ipk.ag_ba.plugins.pipelines.AbstractPipelineTemplate;

/**
 * A workflow for analyzing barley images.
 * (V0.9: optimization not completed)
 * 
 * @author Christian Klukas
 */
public class BarleyPipeline extends AbstractPipelineTemplate {
	
	@Override
	public String getTitle() {
		return "Barley Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Analyze Barley Phenotype";
	}
	
	@Override
	public ImageAnalysisBlock[] getBlockList(ImageProcessorOptionsAndResults options) {
		return new ImageAnalysisBlock[] {
				// acquisition
				new BlLoadImages(),
				// preprocessing
				new BlAlign(),
				new BlColorBalanceVerticalFluo(),
				new BlCreateDummyReferenceIfNeeded(),
				new BlColorBalanceVerticalVis(),
				new BlColorBalanceVerticalNir(),
				new BlColorBalanceCircularVisNir(),
				new BlColorBalanceVerticalNir(),
				// segmentation
				new BlRemoveBackground(),
				new BlDetectBlueMarkers(),
				new BlMedianFilter(),
				new BlLabFilter(),
				new BlClosing(),
				new BlIRdiff(),
				new BlIntensityCalculationFluo(),
				new BlockClearNirPotFromNir(),
				new BlMedianFilter(),
				new BlRemoveSmallObjectsVisFluo(),
				new BlUseFluoMaskToClearOther(),
				new BlCutFromSide(),
				new BlAdaptiveThresholdNir(),
				new BlSkeletonizeNir(),
				new BlCopyImagesApplyMask(),
				new BlSkeletonizeVisFluo(),
				
				// feature extraction
				new BlLeafCurlingAnalysis(),
				new BlCalcMainAxis(),
				new BlCalcCOG(),
				new BlCalcWidthAndHeight(),
				new BlCalcColorHistograms(),
				new BlCalcConvexHull(),
				
				// postprocessing
				new BlDrawSkeleton(),
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
