package de.ipk.ag_ba.plugins.pipelines;

import iap.blocks.acquisition.BlCreateDummyReferenceIfNeeded;
import iap.blocks.acquisition.BlFilterImagesByAngle;
import iap.blocks.acquisition.BlLoadImages;
import iap.blocks.acquisition.BlLoadImagesIfNeeded;
import iap.blocks.auto.BlAdaptiveRemoveSmallObjectsVisFluo;
import iap.blocks.auto.BlAdaptiveSegmentationFluo;
import iap.blocks.auto.BlAdaptiveUseFluoMaskToClearOther;
import iap.blocks.auto.BlAutoAdaptiveThresholdNir;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.blocks.extraction.BlCalcAreas;
import iap.blocks.extraction.BlCalcCOG;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcLeafTips;
import iap.blocks.extraction.BlCalcMainAxis;
import iap.blocks.extraction.BlCalcVolumes;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.extraction.BlCalcWidthAndHeightLR;
import iap.blocks.extraction.BlLeafCurlingAnalysis;
import iap.blocks.extraction.BlSkeletonizeNir;
import iap.blocks.extraction.BlSkeletonizeVisFluo;
import iap.blocks.postprocessing.BlCrop;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlOverlayMasksOnImages;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.preprocessing.BlAlign;
import iap.blocks.preprocessing.BlClearMasks_WellProcessing;
import iap.blocks.preprocessing.BlClearRectangle;
import iap.blocks.preprocessing.BlColorBalanceCircularVisNir;
import iap.blocks.preprocessing.BlColorBalanceVerticalFluo;
import iap.blocks.preprocessing.BlColorBalanceVerticalNir;
import iap.blocks.preprocessing.BlColorBalanceVerticalVis;
import iap.blocks.preprocessing.BlColorCorrectionNir;
import iap.blocks.preprocessing.BlCutFromSide;
import iap.blocks.preprocessing.BlDetectBlueMarkers;
import iap.blocks.preprocessing.BlMoveImagesToMasks;
import iap.blocks.preprocessing.BlObjectSeparator;
import iap.blocks.segmentation.BlAdaptiveThresholdNir;
import iap.blocks.segmentation.BlClosing;
import iap.blocks.segmentation.BlCopyImagesApplyMask;
import iap.blocks.segmentation.BlFilterByHSV;
import iap.blocks.segmentation.BlIRdiff;
import iap.blocks.segmentation.BlIntensityCalculationFluo;
import iap.blocks.segmentation.BlKMeansVis;
import iap.blocks.segmentation.BlLabFilter;
import iap.blocks.segmentation.BlMedianFilter;
import iap.blocks.segmentation.BlRemoveLevitatingObjects;
import iap.blocks.segmentation.BlRemoveMaizeBambooStick;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.blocks.segmentation.BlUseFluoMaskToClearIr;
import iap.blocks.segmentation.BlUseFluoMaskToClearNir;
import iap.blocks.segmentation.BlUseFluoMaskToClearOther;
import iap.blocks.threeD.BlThreeDreconstruction;
import iap.blocks.unused.BlFluoMaskIsRequired;
import iap.blocks.unused.BlSmoothShape;
import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk.ag_ba.plugins.pipelines.arabidopsis.ArabidopsisPipeline;
import de.ipk.ag_ba.plugins.pipelines.barley.BarleyPipeline;
import de.ipk.ag_ba.plugins.pipelines.maize.MaizePipeline;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PluginIAPanalyisTemplates extends AbstractIAPplugin {
	
	public PluginIAPanalyisTemplates() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP analysis templates plugin is beeing loaded");
	}
	
	@Override
	public AnalysisPipelineTemplate[] getAnalysisTemplates() {
		return new AnalysisPipelineTemplate[] {
				new MaizePipeline(),
				new BarleyPipeline(),
				new ArabidopsisPipeline()
		};
	}
	
	@Override
	public ImageAnalysisBlock[] getImageAnalysisBlocks() {
		ImageAnalysisBlock[] fromPipelines = super.getImageAnalysisBlocks();
		
		ImageAnalysisBlock[] additionalBlocks = new ImageAnalysisBlock[] {
				new BlMoveImagesToMasks(),
				new BlMoveMasksToImageSet(),
				new BlOverlayMasksOnImages(),
				new BlAdaptiveRemoveSmallObjectsVisFluo(),
				new BlAdaptiveSegmentationFluo(),
				new BlAdaptiveThresholdNir(),
				new BlAdaptiveUseFluoMaskToClearOther(),
				new BlAutoAdaptiveThresholdNir(),
				new BlCalcAreas(),
				new BlCalcColorHistograms(),
				new BlCalcMainAxis(),
				new BlCalcVolumes(),
				new BlCalcWidthAndHeight(),
				new BlCalcWidthAndHeightLR(),
				new BlClearMasks_WellProcessing(),
				new BlClosing(),
				new BlColorBalanceCircularVisNir(),
				new BlColorBalanceVerticalFluo(),
				new BlColorBalanceVerticalNir(),
				new BlColorBalanceVerticalVis(),
				new BlColorCorrectionNir(),
				new BlCopyImagesApplyMask(),
				new BlCreateDummyReferenceIfNeeded(),
				new BlCrop(),
				new BlDetectBlueMarkers(),
				new BlFilterByHSV(),
				new BlIntensityCalculationFluo(),
				new BlIRdiff(),
				// new BlKMeans2(),
				new BlKMeansVis(),
				new BlLabFilter(),
				new BlLeafCurlingAnalysis(),
				new BlLoadImages(),
				new BlLoadImagesIfNeeded(),
				new BlRemoveMaizeBambooStick(),
				new BlRemoveSmallObjectsVisFluo(),
				new BlRunPostProcessors(),
				new BlSkeletonizeVisFluo(),
				new BlSkeletonizeNir(),
				new BlUseFluoMaskToClearIr(),
				new BlUseFluoMaskToClearNir(),
				new BlUseFluoMaskToClearOther(),
				new BlAlign(),
				new BlCalcCOG(),
				new BlCalcConvexHull(),
				new BlClearRectangle(),
				new BlCutFromSide(),
				new BlFluoMaskIsRequired(),
				new BlHighlightNullResults(),
				new BlMedianFilter(),
				new BlObjectSeparator(),
				new BlSmoothShape(),
				new BlThreeDreconstruction(),
				new BlCalcLeafTips(),
				new BlRemoveLevitatingObjects(),
				new BlFilterImagesByAngle()
		};
		
		ImageAnalysisBlock[] res = new ImageAnalysisBlock[fromPipelines.length + additionalBlocks.length];
		int idx = 0;
		for (ImageAnalysisBlock b : fromPipelines)
			res[idx++] = b;
		for (ImageAnalysisBlock b : additionalBlocks)
			res[idx++] = b;
		
		return res;
	}
	
}