package iap.pipelines.arabidopsis;

import iap.blocks.BlBalanceFluo;
import iap.blocks.BlCalcNirSkeleton;
import iap.blocks.BlColorBalancing;
import iap.blocks.BlColorBalancingRoundCamera;
import iap.blocks.BlCopyImagesApplyMask;
import iap.blocks.BlCrop;
import iap.blocks.BlFilterByLAB;
import iap.blocks.BlLoadImagesIfNeeded_images_masks;
import iap.blocks.BlMedianFilter;
import iap.blocks.BlMedianFilterFluo;
import iap.blocks.BlMoveMasksToImageSet;
import iap.blocks.BlNirFilterSide_nir;
import iap.blocks.BlRemoveSmallClustersFromVisFluo;
import iap.blocks.BlReplaceEmptyOriginalImages;
import iap.blocks.BlSmoothShape;
import iap.blocks.BlUseFluoMaskToClearOtherImages;
import iap.blocks.BlockClearNirPotFromNir;
import iap.blocks.arabidopsis.BlClearMasks_Arabidops;
import iap.blocks.arabidopsis.BlLoadImagesIfNeeded;
import iap.blocks.arabidopsis.BlockSkeletonize_Arabidopsis;
import iap.blocks.hull.BlConvexHull;
import iap.blocks.maize.BlCalcIntensity;
import iap.blocks.maize.BlCalcMainAxis;
import iap.blocks.maize.BlCalcWidthAndHeight;
import iap.blocks.maize.BlClearBackgroundByRefComparison_vis_fluo_nir;
import iap.blocks.maize.BlIntensityConversion;
import iap.blocks.maize.BlUseVisMaskToClearFluo_fluo;
import iap.blocks.maize.BlockColorBalancingVertical;
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
 * @author klukas, pape, entzian
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
				BlLoadImagesIfNeeded_images_masks.class.getCanonicalName(),
				BlLoadImagesIfNeeded.class.getCanonicalName(),
				BlBalanceFluo.class.getCanonicalName(),
				BlClearMasks_Arabidops.class.getCanonicalName(),
				BlColorBalancing.class.getCanonicalName(),
				BlockColorBalancingVertical.class.getCanonicalName(),
				BlColorBalancingRoundCamera.class.getCanonicalName(),
				BlockColorBalancingVertical.class.getCanonicalName(),
				BlBalanceFluo.class.getCanonicalName(),
				BlClearBackgroundByRefComparison_vis_fluo_nir.class.getCanonicalName(),
				BlFilterByLAB.class.getCanonicalName(),
				BlMedianFilter.class.getCanonicalName(),
				BlIntensityConversion.class.getCanonicalName(),
				BlockClearNirPotFromNir.class.getCanonicalName(),
				BlMedianFilterFluo.class.getCanonicalName(),
				BlRemoveSmallClustersFromVisFluo.class.getCanonicalName(),
				BlUseVisMaskToClearFluo_fluo.class.getCanonicalName(),
				BlUseFluoMaskToClearOtherImages.class.getCanonicalName(),

				BlSmoothShape.class.getCanonicalName(),

				BlMedianFilter.class.getCanonicalName(),
				BlMedianFilter.class.getCanonicalName(),
				BlNirFilterSide_nir.class.getCanonicalName(),
				BlCalcNirSkeleton.class.getCanonicalName(),
				BlCopyImagesApplyMask.class.getCanonicalName(),

				BlockSkeletonize_Arabidopsis.class.getCanonicalName(),

				// calculation of numeric values
				BlCalcMainAxis.class.getCanonicalName(),
				BlCalcWidthAndHeight.class.getCanonicalName(),
				BlCalcIntensity.class.getCanonicalName(),
				BlConvexHull.class.getCanonicalName(),
				// postprocessing
				BlockRunPostProcessors.class.getCanonicalName(),

				BlockDrawSkeleton.class.getCanonicalName(),

				BlMoveMasksToImageSet.class.getCanonicalName(),
				BlCrop.class.getCanonicalName(),
				BlReplaceEmptyOriginalImages.class.getCanonicalName()
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
