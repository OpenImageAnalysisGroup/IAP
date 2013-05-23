package iap.pipelines.maize;

import iap.blocks.BlBalanceFluo;
import iap.blocks.BlCalcNirSkeleton;
import iap.blocks.BlColorBalancing;
import iap.blocks.BlCopyImagesApplyMask;
import iap.blocks.BlCreateDummyReferenceIfNeeded;
import iap.blocks.BlCrop;
import iap.blocks.BlFilterByLAB;
import iap.blocks.BlLoadImagesIfNeeded_images_masks;
import iap.blocks.BlMedianFilterFluo;
import iap.blocks.BlMoveMasksToImageSet;
import iap.blocks.BlNirFilterSide_nir;
import iap.blocks.BlRemoveSmallClustersFromVisFluo;
import iap.blocks.BlReplaceEmptyOriginalImages;
import iap.blocks.BlUseFluoMaskToClearOther;
import iap.blocks.BlockClearNirPotFromNir;
import iap.blocks.BlockClosingVis;
import iap.blocks.BlockCutFromSide;
import iap.blocks.BlockRemoveMaizeBambooStick;
import iap.blocks.BlockSkeletonizeVisOrFluo;
import iap.blocks.arabidopsis.BlCutZoomedImages;
import iap.blocks.arabidopsis.BlRotate;
import iap.blocks.curling.BlLeafCurlingAnalysis;
import iap.blocks.hull.BlConvexHull;
import iap.blocks.maize.BlCalcIntensity;
import iap.blocks.maize.BlCalcMainAxis;
import iap.blocks.maize.BlCalcWidthAndHeight;
import iap.blocks.maize.BlClearBackgroundByRefComparison_vis_fluo_nir;
import iap.blocks.maize.BlFindBlueMarkers;
import iap.blocks.maize.BlIntensityConversion;
import iap.blocks.maize.BlockClearSmallBorderAroundImagesAndMasks;
import iap.blocks.maize.BlockColorBalancingVertical;
import iap.blocks.maize.BlockDrawSkeleton;
import iap.blocks.maize.BlockRemoveLevitatingObjectsFromVisFluo;
import iap.blocks.maize.BlockRemoveSmallStructuresUsingOpeningFromTopVis;
import iap.blocks.maize.BlockRemoveVerticalAndHorizontalStructuresFromVisFluo;
import iap.blocks.post_process.BlockRunPostProcessors;
import iap.pipelines.AbstractImageProcessor;
import iap.pipelines.ImageProcessorOptions;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;

/**
 * Comprehensive corn image analysis pipeline, processing VIS, FLUO and NIR images. Depends on reference images for initial comparison
 * and foreground / background separation.
 * 
 * @author klukas, pape, entzian
 */
public class MaizeAnalysisPipeline extends AbstractImageProcessor {
	
	private final SystemOptions so;
	
	public MaizeAnalysisPipeline(SystemOptions so) {
		this.so = so;
	}
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		String[] defaultBlockList = new String[] {
				BlLoadImagesIfNeeded_images_masks.class.getCanonicalName(),
				BlRotate.class.getCanonicalName(),
				BlCutZoomedImages.class.getCanonicalName(),
				BlCreateDummyReferenceIfNeeded.class.getCanonicalName(),
				BlColorBalancing.class.getCanonicalName(),
				BlFindBlueMarkers.class.getCanonicalName(),
				BlockClearSmallBorderAroundImagesAndMasks.class.getCanonicalName(),
				BlBalanceFluo.class.getCanonicalName(),
				BlockColorBalancingVertical.class.getCanonicalName(),
				BlBalanceFluo.class.getCanonicalName(),
				BlClearBackgroundByRefComparison_vis_fluo_nir.class.getCanonicalName(),
				BlIntensityConversion.class.getCanonicalName(),
				BlockClearNirPotFromNir.class.getCanonicalName(),
				BlFilterByLAB.class.getCanonicalName(),
				BlockClosingVis.class.getCanonicalName(),
				BlRemoveSmallClustersFromVisFluo.class.getCanonicalName(),
				BlockRemoveSmallStructuresUsingOpeningFromTopVis.class.getCanonicalName(),
				BlMedianFilterFluo.class.getCanonicalName(),
				BlRemoveSmallClustersFromVisFluo.class.getCanonicalName(),
				BlockRemoveMaizeBambooStick.class.getCanonicalName(),
				BlockRemoveLevitatingObjectsFromVisFluo.class.getCanonicalName(),
				BlockCutFromSide.class.getCanonicalName(),
				BlockRemoveVerticalAndHorizontalStructuresFromVisFluo.class.getCanonicalName(),
				BlRemoveSmallClustersFromVisFluo.class.getCanonicalName(),
				BlockSkeletonizeVisOrFluo.class.getCanonicalName(),
				BlNirFilterSide_nir.class.getCanonicalName(),
				BlUseFluoMaskToClearOther.class.getCanonicalName(),
				BlCalcNirSkeleton.class.getCanonicalName(),
				BlCopyImagesApplyMask.class.getCanonicalName(),
				
				// calculation of numeric values
				BlCalcWidthAndHeight.class.getCanonicalName(),
				BlCalcMainAxis.class.getCanonicalName(),
				BlLeafCurlingAnalysis.class.getCanonicalName(),
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
	 * Modify default LAB filter options according to the Maize analysis requirements.
	 */
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		
		options.setSystemOptionStorage(so);
	}
	
	@Override
	public void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status) {
		this.status = status;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatus() {
		return status;
	}
	
}
