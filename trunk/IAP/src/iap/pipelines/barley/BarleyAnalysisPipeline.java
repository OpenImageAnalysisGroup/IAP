package iap.pipelines.barley;

import iap.blocks.BlBalanceFluo;
import iap.blocks.BlCalcNirSkeleton;
import iap.blocks.BlColorBalancing;
import iap.blocks.BlColorBalancingRoundCamera;
import iap.blocks.BlCopyImagesApplyMask;
import iap.blocks.BlCreateDummyReferenceIfNeeded;
import iap.blocks.BlCrop;
import iap.blocks.BlLabFilterExt;
import iap.blocks.BlLoadImagesIfNeeded_images_masks;
import iap.blocks.BlMedianFilterFluo;
import iap.blocks.BlMoveMasksToImageSet;
import iap.blocks.BlNirFilterSide_nir;
import iap.blocks.BlRemoveSmallClustersFromVisFluo;
import iap.blocks.BlReplaceEmptyOriginalImages;
import iap.blocks.BlUseFluoMaskToClearOtherImages;
import iap.blocks.BlockClearNirPotFromNir;
import iap.blocks.BlockClosingVis;
import iap.blocks.BlockCutFromSide;
import iap.blocks.BlockSkeletonizeVisOrFluo;
import iap.blocks.arabidopsis.BlCutZoomedImages;
import iap.blocks.arabidopsis.BlIRdiff;
import iap.blocks.curling.BlLeafCurlingAnalysis;
import iap.blocks.hull.BlConvexHull;
import iap.blocks.maize.BlCalcIntensity;
import iap.blocks.maize.BlCalcMainAxis;
import iap.blocks.maize.BlCalcWidthAndHeight;
import iap.blocks.maize.BlClearBackgroundByRefComparison_vis_fluo_nir;
import iap.blocks.maize.BlFindBlueMarkers;
import iap.blocks.maize.BlIntensityConversion;
import iap.blocks.maize.BlockColorBalancingVertical;
import iap.blocks.maize.BlockDrawSkeleton;
import iap.blocks.post_process.BlockRunPostProcessors;
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
				BlLoadImagesIfNeeded_images_masks.class.getCanonicalName(),
				BlCutZoomedImages.class.getCanonicalName(),
				BlBalanceFluo.class.getCanonicalName(),
				BlCreateDummyReferenceIfNeeded.class.getCanonicalName(),
				BlColorBalancing.class.getCanonicalName(),
				BlockColorBalancingVertical.class.getCanonicalName(),
				BlColorBalancingRoundCamera.class.getCanonicalName(),
				BlockColorBalancingVertical.class.getCanonicalName(),
				BlClearBackgroundByRefComparison_vis_fluo_nir.class.getCanonicalName(),
				BlFindBlueMarkers.class.getCanonicalName(),
				// BlBalanceFluo.class.getCanonicalName(),
				BlMedianFilterFluo.class.getCanonicalName(),
				BlMedianFilterFluo.class.getCanonicalName(),
				BlMedianFilterFluo.class.getCanonicalName(),
				BlMedianFilterFluo.class.getCanonicalName(),
				BlLabFilterExt.class.getCanonicalName(),
				BlockClosingVis.class.getCanonicalName(),
				BlIRdiff.class.getCanonicalName(),
				BlIntensityConversion.class.getCanonicalName(),
				BlockClearNirPotFromNir.class.getCanonicalName(),
				BlMedianFilterFluo.class.getCanonicalName(),
				BlRemoveSmallClustersFromVisFluo.class.getCanonicalName(),
				BlUseFluoMaskToClearOtherImages.class.getCanonicalName(),
				BlockCutFromSide.class.getCanonicalName(),
				BlNirFilterSide_nir.class.getCanonicalName(),
				BlCalcNirSkeleton.class.getCanonicalName(),
				BlCopyImagesApplyMask.class.getCanonicalName(),

				BlockSkeletonizeVisOrFluo.class.getCanonicalName(),

				// calculation of numeric values
				BlLeafCurlingAnalysis.class.getCanonicalName(),
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
