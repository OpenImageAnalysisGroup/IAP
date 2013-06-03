package iap.pipelines.arabidopsis;

import iap.blocks.acquisition.BlLoadImages;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcMainAxis;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.extraction.BlSkeletonizeNir;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlockDrawSkeleton;
import iap.blocks.postprocessing.BlockRunPostProcessors;
import iap.blocks.preprocessing.BlColorBalanceFluo;
import iap.blocks.preprocessing.BlColorBalanceNir;
import iap.blocks.preprocessing.BlColorBalanceVis;
import iap.blocks.segmentation.BlAdaptiveThresholdNir;
import iap.blocks.segmentation.BlIntensityCalculationFluo;
import iap.blocks.segmentation.BlLabFilter;
import iap.blocks.segmentation.BlRemoveBackground;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.blocks.unused.BlClearMasks_Arabidops;
import iap.blocks.unused.BlColorBalancingRoundCamera;
import iap.blocks.unused.BlCopyImagesApplyMask;
import iap.blocks.unused.BlCrop;
import iap.blocks.unused.BlLoadImagesIfNeeded;
import iap.blocks.unused.BlMedianFilter;
import iap.blocks.unused.BlMedianFilterVisDepr;
import iap.blocks.unused.BlSmoothShape;
import iap.blocks.unused.BlUseFluoMaskToClearOtherImages;
import iap.blocks.unused.BlUseVisMaskToClearFluo_fluo;
import iap.blocks.unused.BlockClearNirPotFromNir;
import iap.blocks.unused.BlockSkeletonize_Arabidopsis;
import iap.pipelines.AbstractImageProcessor;
import iap.pipelines.ImageProcessorOptions;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;

/**
 * Comprehensive barley image analysis pipeline, processing VIS, FLUO and NIR
 * images. Depends on reference images for initial comparison and foreground /
 * background separation.
 * 
 * @author klukas
 */
public class ArabidopsisAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	private final SystemOptions so;
	
	public ArabidopsisAnalysisPipeline(SystemOptions so) {
		this.so = so;
	}
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		String[] defaultBlockList = new String[] {
				BlLoadImages.class.getCanonicalName(),
				BlLoadImagesIfNeeded.class.getCanonicalName(),
				BlColorBalanceFluo.class.getCanonicalName(),
				BlClearMasks_Arabidops.class.getCanonicalName(),
				BlColorBalanceVis.class.getCanonicalName(),
				BlColorBalanceNir.class.getCanonicalName(),
				BlColorBalancingRoundCamera.class.getCanonicalName(),
				BlColorBalanceNir.class.getCanonicalName(),
				BlColorBalanceFluo.class.getCanonicalName(),
				BlRemoveBackground.class.getCanonicalName(),
				BlLabFilter.class.getCanonicalName(),
				BlMedianFilterVisDepr.class.getCanonicalName(),
				BlIntensityCalculationFluo.class.getCanonicalName(),
				BlockClearNirPotFromNir.class.getCanonicalName(),
				BlMedianFilter.class.getCanonicalName(),
				BlRemoveSmallObjectsVisFluo.class.getCanonicalName(),
				BlUseVisMaskToClearFluo_fluo.class.getCanonicalName(),
				BlUseFluoMaskToClearOtherImages.class.getCanonicalName(),
				
				BlSmoothShape.class.getCanonicalName(),
				
				BlMedianFilterVisDepr.class.getCanonicalName(),
				BlMedianFilterVisDepr.class.getCanonicalName(),
				BlAdaptiveThresholdNir.class.getCanonicalName(),
				BlSkeletonizeNir.class.getCanonicalName(),
				BlCopyImagesApplyMask.class.getCanonicalName(),
				
				BlockSkeletonize_Arabidopsis.class.getCanonicalName(),
				
				// calculation of numeric values
				BlCalcMainAxis.class.getCanonicalName(),
				BlCalcWidthAndHeight.class.getCanonicalName(),
				BlCalcColorHistograms.class.getCanonicalName(),
				BlCalcConvexHull.class.getCanonicalName(),
				// postprocessing
				BlockRunPostProcessors.class.getCanonicalName(),
				
				BlockDrawSkeleton.class.getCanonicalName(),
				
				BlMoveMasksToImageSet.class.getCanonicalName(),
				BlCrop.class.getCanonicalName(),
				BlHighlightNullResults.class.getCanonicalName()
		};
		
		modifySettings(options);
		
		return getPipelineFromBlockList(so, defaultBlockList);
	}
	
	/**
	 * Modify default LAB filter options according to the Maize analysis
	 * requirements.
	 */
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
