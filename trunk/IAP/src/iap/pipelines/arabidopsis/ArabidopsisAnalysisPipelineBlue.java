package iap.pipelines.arabidopsis;

import iap.blocks.BlBalanceFluo;
import iap.blocks.BlColorBalancing;
import iap.blocks.BlCrop;
import iap.blocks.BlLabFilter;
import iap.blocks.BlMedianFilterFluo;
import iap.blocks.BlMoveImagesToMasks;
import iap.blocks.BlMoveMasksToImageSet;
import iap.blocks.BlRemoveSmallClustersFromVisFluo;
import iap.blocks.BlReplaceEmptyOriginalImages;
import iap.blocks.arabidopsis.BlClearMasks_Arabidopsis_PotAndTrayProcessing;
import iap.blocks.arabidopsis.BlCutZoomedImages;
import iap.blocks.arabidopsis.BlLoadImagesIfNeeded;
import iap.blocks.arabidopsis.BlRotate;
import iap.blocks.arabidopsis.BlUseFluoMaskToClearIr;
import iap.blocks.arabidopsis.BlUseFluoMaskToClearNir_Arabidopsis;
import iap.blocks.arabidopsis.BlUseFluoMaskToClear_Arabidopsis_vis;
import iap.blocks.arabidopsis.Bl_Arabidopsis_IRdiff;
import iap.blocks.arabidopsis.BlockSkeletonize_Arabidopsis;
import iap.blocks.hull.BlConvexHull;
import iap.blocks.maize.BlCalcIntensity;
import iap.blocks.maize.BlCalcWidthAndHeight;
import iap.blocks.maize.BlIntensityConversion;
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
public class ArabidopsisAnalysisPipelineBlue extends AbstractImageProcessor {
	
	private final SystemOptions so;
	
	public ArabidopsisAnalysisPipelineBlue(SystemOptions so) {
		this.so = so;
	}
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		modifySettings(options);
		String[] defaultBlockList = new String[] {
				BlLoadImagesIfNeeded.class.getCanonicalName(),
				BlBalanceFluo.class.getCanonicalName(),
				BlColorBalancing.class.getCanonicalName(),
				BlRotate.class.getCanonicalName(),
				BlCutZoomedImages.class.getCanonicalName(),
				BlClearMasks_Arabidopsis_PotAndTrayProcessing.class.getCanonicalName(),
				BlMoveImagesToMasks.class.getCanonicalName(),
				BlLabFilter.class.getCanonicalName(),
				BlIntensityConversion.class.getCanonicalName(),
				BlMedianFilterFluo.class.getCanonicalName(),
				BlRemoveSmallClustersFromVisFluo.class.getCanonicalName(),
				BlUseFluoMaskToClear_Arabidopsis_vis.class.getCanonicalName(),
				BlUseFluoMaskToClearNir_Arabidopsis.class.getCanonicalName(),
				Bl_Arabidopsis_IRdiff.class.getCanonicalName(),
				BlUseFluoMaskToClearIr.class.getCanonicalName(),
				BlockSkeletonize_Arabidopsis.class.getCanonicalName(),
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
		
		options.setIsBarley(false);
		options.setIsMaize(false);
		options.setIsArabidopsis(true);
		
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
