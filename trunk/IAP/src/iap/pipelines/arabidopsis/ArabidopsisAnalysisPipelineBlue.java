package iap.pipelines.arabidopsis;

import iap.blocks.BlCrop;
import iap.blocks.BlLabFilterDepr;
import iap.blocks.BlMedianFilter;
import iap.blocks.BlMoveImagesToMasks;
import iap.blocks.arabidopsis.BlClearMasks_Arabidopsis_PotAndTrayProcessing;
import iap.blocks.arabidopsis.BlIRdiff;
import iap.blocks.arabidopsis.BlLoadImagesIfNeeded;
import iap.blocks.arabidopsis.BlUseFluoMaskToClearIr;
import iap.blocks.arabidopsis.BlUseFluoMaskToClearNir_Arabidopsis;
import iap.blocks.arabidopsis.BlUseFluoMaskToClear_Arabidopsis_vis;
import iap.blocks.arabidopsis.BlockSkeletonize_Arabidopsis;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlockDrawSkeleton;
import iap.blocks.postprocessing.BlockRunPostProcessors;
import iap.blocks.preprocessing.BlAlign;
import iap.blocks.preprocessing.BlColorBalanceFluo;
import iap.blocks.preprocessing.BlColorBalanceVis;
import iap.blocks.preprocessing.BlRotate;
import iap.blocks.segmentation.BlIntensityCalculationFluo;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
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
				BlColorBalanceFluo.class.getCanonicalName(),
				BlColorBalanceVis.class.getCanonicalName(),
				BlRotate.class.getCanonicalName(),
				BlAlign.class.getCanonicalName(),
				BlClearMasks_Arabidopsis_PotAndTrayProcessing.class.getCanonicalName(),
				BlMoveImagesToMasks.class.getCanonicalName(),
				BlLabFilterDepr.class.getCanonicalName(),
				BlIntensityCalculationFluo.class.getCanonicalName(),
				BlMedianFilter.class.getCanonicalName(),
				BlRemoveSmallObjectsVisFluo.class.getCanonicalName(),
				BlUseFluoMaskToClear_Arabidopsis_vis.class.getCanonicalName(),
				BlUseFluoMaskToClearNir_Arabidopsis.class.getCanonicalName(),
				BlIRdiff.class.getCanonicalName(),
				BlUseFluoMaskToClearIr.class.getCanonicalName(),
				BlockSkeletonize_Arabidopsis.class.getCanonicalName(),
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
