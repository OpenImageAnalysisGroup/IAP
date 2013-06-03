package iap.pipelines.arabidopsis;

import iap.blocks.BlColorBalanceFluo;
import iap.blocks.BlSkeletonizeNir;
import iap.blocks.BlColorBalanceVis;
import iap.blocks.BlColorBalancingRoundCamera;
import iap.blocks.BlCopyImagesApplyMask;
import iap.blocks.BlCrop;
import iap.blocks.BlLabFilter;
import iap.blocks.BlLoadImages;
import iap.blocks.BlMedianFilterVisDepr;
import iap.blocks.BlMedianFilter;
import iap.blocks.BlMoveMasksToImageSet;
import iap.blocks.BlAdaptiveThresholdNir;
import iap.blocks.BlRemoveSmallObjectsVisFluo;
import iap.blocks.BlHighlightNullResults;
import iap.blocks.BlSmoothShape;
import iap.blocks.BlUseFluoMaskToClearOtherImages;
import iap.blocks.BlockClearNirPotFromNir;
import iap.blocks.arabidopsis.BlClearMasks_Arabidops;
import iap.blocks.arabidopsis.BlLoadImagesIfNeeded;
import iap.blocks.arabidopsis.BlockSkeletonize_Arabidopsis;
import iap.blocks.hull.BlCalcConvexHull;
import iap.blocks.maize.BlCalcColorHistograms;
import iap.blocks.maize.BlCalcMainAxis;
import iap.blocks.maize.BlCalcWidthAndHeight;
import iap.blocks.maize.BlRemoveBackground;
import iap.blocks.maize.BlIntensityCalculationFluo;
import iap.blocks.maize.BlUseVisMaskToClearFluo_fluo;
import iap.blocks.maize.BlColorBalanceNir;
import iap.blocks.maize.BlockDrawSkeleton;
import iap.blocks.post_process.BlockRunPostProcessors;
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
