package iap.pipelines.barley;

import iap.blocks.acquisition.BlCreateDummyReferenceIfNeeded;
import iap.blocks.acquisition.BlLoadImages;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcMainAxis;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.extraction.BlSkeletonizeNir;
import iap.blocks.extraction.BlSkeletonizeVisFluo;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlDrawSkeleton;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.preprocessing.BlAlign;
import iap.blocks.preprocessing.BlColorBalanceVerticalFluo;
import iap.blocks.preprocessing.BlColorBalanceVerticalNir;
import iap.blocks.preprocessing.BlColorBalanceVerticalVis;
import iap.blocks.preprocessing.BlDetectBlueMarkers;
import iap.blocks.preprocessing.BlockCutFromSide;
import iap.blocks.segmentation.BlAdaptiveThresholdNir;
import iap.blocks.segmentation.BlClosing;
import iap.blocks.segmentation.BlIntensityCalculationFluo;
import iap.blocks.segmentation.BlRemoveBackground;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.blocks.unused.BlColorBalanceCircularVisNir;
import iap.blocks.unused.BlCopyImagesApplyMask;
import iap.blocks.unused.BlCrop;
import iap.blocks.unused.BlIRdiff;
import iap.blocks.unused.BlLabFilterExt;
import iap.blocks.unused.BlLeafCurlingAnalysis;
import iap.blocks.unused.BlMedianFilter;
import iap.blocks.unused.BlUseFluoMaskToClearOtherImages;
import iap.blocks.unused.BlockClearNirPotFromNir;
import iap.pipelines.AbstractImageProcessor;
import iap.pipelines.ImageProcessorOptions;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;

/**
 * Comprehensive image analysis pipeline, processing VIS, FLUO and NIR
 * images. Depends on reference images for initial comparison and foreground /
 * background separation.
 * 
 * @author klukas, pape, entzian
 */
public class BarleyAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	private final SystemOptions so;
	
	public BarleyAnalysisPipeline(SystemOptions so) throws Exception {
		this.so = so;
	}
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		String[] defaultBlockList = new String[] {
				BlLoadImages.class.getCanonicalName(),
				BlAlign.class.getCanonicalName(),
				BlColorBalanceVerticalFluo.class.getCanonicalName(),
				BlCreateDummyReferenceIfNeeded.class.getCanonicalName(),
				BlColorBalanceVerticalVis.class.getCanonicalName(),
				BlColorBalanceVerticalNir.class.getCanonicalName(),
				BlColorBalanceCircularVisNir.class.getCanonicalName(),
				BlColorBalanceVerticalNir.class.getCanonicalName(),
				BlRemoveBackground.class.getCanonicalName(),
				BlDetectBlueMarkers.class.getCanonicalName(),
				// BlBalanceFluo.class.getCanonicalName(),
				BlMedianFilter.class.getCanonicalName(),
				BlMedianFilter.class.getCanonicalName(),
				BlMedianFilter.class.getCanonicalName(),
				BlMedianFilter.class.getCanonicalName(),
				BlLabFilterExt.class.getCanonicalName(),
				BlClosing.class.getCanonicalName(),
				BlIRdiff.class.getCanonicalName(),
				BlIntensityCalculationFluo.class.getCanonicalName(),
				BlockClearNirPotFromNir.class.getCanonicalName(),
				BlMedianFilter.class.getCanonicalName(),
				BlRemoveSmallObjectsVisFluo.class.getCanonicalName(),
				BlUseFluoMaskToClearOtherImages.class.getCanonicalName(),
				BlockCutFromSide.class.getCanonicalName(),
				BlAdaptiveThresholdNir.class.getCanonicalName(),
				BlSkeletonizeNir.class.getCanonicalName(),
				BlCopyImagesApplyMask.class.getCanonicalName(),

				BlSkeletonizeVisFluo.class.getCanonicalName(),

				// calculation of numeric values
				BlLeafCurlingAnalysis.class.getCanonicalName(),
				BlCalcMainAxis.class.getCanonicalName(),
				BlCalcWidthAndHeight.class.getCanonicalName(),
				BlCalcColorHistograms.class.getCanonicalName(),
				BlCalcConvexHull.class.getCanonicalName(),
				// postprocessing
				BlRunPostProcessors.class.getCanonicalName(),

				BlDrawSkeleton.class.getCanonicalName(),

				BlMoveMasksToImageSet.class.getCanonicalName(),
				BlCrop.class.getCanonicalName(),
				BlHighlightNullResults.class.getCanonicalName()
		};
		modifySettings(options);
		return getPipelineFromBlockList(so, defaultBlockList);
	}
	
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		
		options.setSystemOptionStorage(so);
	}
	
	@Override
	public void setStatus(
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		this.status = status;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatus() {
		return status;
	}
	
}
