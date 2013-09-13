package de.ipk.ag_ba.plugins.pipelines.maize;

import iap.blocks.acquisition.BlCreateDummyReferenceIfNeeded;
import iap.blocks.acquisition.BlLoadImages;
import iap.blocks.auto.BlAutoSegmentationFluo;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlCalcAreas;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcMainAxis;
import iap.blocks.extraction.BlCalcVolumes;
import iap.blocks.extraction.BlCalcWidthAndHeight;
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
import iap.blocks.preprocessing.BlRotate;
import iap.blocks.segmentation.BlAdaptiveThresholdNir;
import iap.blocks.segmentation.BlClosing;
import iap.blocks.segmentation.BlLabFilter;
import iap.blocks.segmentation.BlMedianFilterFluo;
import iap.blocks.segmentation.BlRemoveBackground;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.blocks.segmentation.BlUseFluoMaskToClearOther;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;

/**
 * Analysis workflow for analyzing maize plant images.
 * (V1: tested)
 * 
 * @author Christian Klukas
 */
public class MaizePipeline implements AnalysisPipelineTemplate {
	
	@Override
	public String getTitle() {
		return "Dynamic Maize Pipeline";
	}
	
	@Override
	public String getDescription() {
		return "Analyze Maize Phenotype";
	}
	
	@Override
	public ImageAnalysisBlock[] getBlockList() {
		return new ImageAnalysisBlock[] {
				// acquisition
				new BlLoadImages(),
				new BlCreateDummyReferenceIfNeeded(),
				
				// preprocessing
				new BlRotate(),
				new BlAlign(),
				new BlDetectBlueMarkers(),
				new BlCutFromSide(),
				
				new BlColorBalanceVerticalVis(),
				new BlColorBalanceVerticalFluo(),
				new BlColorBalanceVerticalNir(),
				new BlColorBalanceCircularVisNir(),
				
				// segmentation
				new BlRemoveBackground(),
				new BlAutoSegmentationFluo(),
				new BlLabFilter(),
				new BlAdaptiveThresholdNir(),
				new BlClosing(),
				new BlMedianFilterFluo(),
				new BlRemoveSmallObjectsVisFluo(),
				new BlUseFluoMaskToClearOther(),
				
				// feature extraction
				new BlSkeletonizeVisFluo(),
				new BlSkeletonizeNir(),
				new BlCalcWidthAndHeight(),
				new BlCalcMainAxis(),
				new BlCalcColorHistograms(),
				new BlCalcConvexHull(),
				new BlCalcAreas(),
				new BlCalcVolumes(),
				
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
