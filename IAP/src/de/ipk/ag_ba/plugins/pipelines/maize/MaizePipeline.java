package de.ipk.ag_ba.plugins.pipelines.maize;

import iap.blocks.acquisition.BlCreateDummyReferenceIfNeeded;
import iap.blocks.acquisition.BlLoadImages;
import iap.blocks.auto.BlAdaptiveRemoveSmallObjectsVisFluo;
import iap.blocks.auto.BlAdaptiveSegmentationFluo;
import iap.blocks.auto.BlAdaptiveUseFluoMaskToClearOther;
import iap.blocks.auto.BlAutoAdaptiveThresholdNir;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlCalcAreas;
import iap.blocks.extraction.BlCalcCOG;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcMainAxis;
import iap.blocks.extraction.BlCalcVolumes;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.extraction.BlDetectLeafTips;
import iap.blocks.extraction.BlSkeletonizeNir;
import iap.blocks.extraction.BlSkeletonizeVisFluo;
import iap.blocks.extraction.BlTrackLeafTips;
import iap.blocks.postprocessing.BlCrop;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.postprocessing.BlSaveResultImages;
import iap.blocks.preprocessing.BlAlign;
import iap.blocks.preprocessing.BlColorBalanceCircularVisNir;
import iap.blocks.preprocessing.BlColorBalanceVerticalFluo;
import iap.blocks.preprocessing.BlColorBalanceVerticalVis;
import iap.blocks.preprocessing.BlColorCorrectionNir;
import iap.blocks.preprocessing.BlCutFromSide;
import iap.blocks.preprocessing.BlDetectBlueMarkers;
import iap.blocks.preprocessing.BlRotate;
import iap.blocks.segmentation.BlKMeansVis;
import iap.blocks.segmentation.BlMedianFilterFluo;
import iap.blocks.segmentation.BlMorphologicalOperations;
import iap.blocks.segmentation.BlRemoveBackground;
import iap.pipelines.ImageProcessorOptionsAndResults;
import de.ipk.ag_ba.plugins.pipelines.AbstractPipelineTemplate;

/**
 * Analysis workflow for analyzing maize plant images.
 * (V1: tested)
 * 
 * @author Christian Klukas
 */
public class MaizePipeline extends AbstractPipelineTemplate {
	
	@Override
	public String getTitle() {
		return "Maize Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Analyze Maize Phenotype";
	}
	
	@Override
	public ImageAnalysisBlock[] getBlockList(ImageProcessorOptionsAndResults options) {
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
				new BlColorCorrectionNir(),
				new BlColorBalanceCircularVisNir(),
				
				// segmentation
				new BlRemoveBackground(),
				new BlAdaptiveSegmentationFluo(),
				new BlKMeansVis(),
				new BlAutoAdaptiveThresholdNir(),
				new BlMedianFilterFluo(),
				new BlMorphologicalOperations(),
				new BlAdaptiveRemoveSmallObjectsVisFluo(),
				new BlAdaptiveUseFluoMaskToClearOther(),
				
				// feature extraction
				new BlSkeletonizeVisFluo(),
				new BlSkeletonizeNir(),
				new BlCalcWidthAndHeight(),
				new BlCalcCOG(),
				new BlCalcMainAxis(),
				new BlCalcColorHistograms(),
				new BlCalcConvexHull(),
				new BlCalcAreas(),
				new BlCalcVolumes(),
				new BlDetectLeafTips(),
				new BlTrackLeafTips(),
				
				// postprocessing
				new BlRunPostProcessors(),
				new BlMoveMasksToImageSet(),
				new BlCrop(),
				new BlHighlightNullResults(),
				new BlSaveResultImages()
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
