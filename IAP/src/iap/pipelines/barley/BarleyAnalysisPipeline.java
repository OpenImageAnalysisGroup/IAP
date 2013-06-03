package iap.pipelines.barley;

import iap.blocks.BlColorBalanceFluo;
import iap.blocks.BlSkeletonizeNir;
import iap.blocks.BlColorBalanceVis;
import iap.blocks.BlColorBalancingRoundCamera;
import iap.blocks.BlCopyImagesApplyMask;
import iap.blocks.BlCreateDummyReferenceIfNeeded;
import iap.blocks.BlCrop;
import iap.blocks.BlLabFilterExt;
import iap.blocks.BlLoadImages;
import iap.blocks.BlMedianFilter;
import iap.blocks.BlMoveMasksToImageSet;
import iap.blocks.BlAdaptiveThresholdNir;
import iap.blocks.BlRemoveSmallObjectsVisFluo;
import iap.blocks.BlHighlightNullResults;
import iap.blocks.BlUseFluoMaskToClearOtherImages;
import iap.blocks.BlockClearNirPotFromNir;
import iap.blocks.BlClosing;
import iap.blocks.BlockCutFromSide;
import iap.blocks.BlSkeletonizeVisFluo;
import iap.blocks.arabidopsis.BlAlign;
import iap.blocks.arabidopsis.BlIRdiff;
import iap.blocks.curling.BlLeafCurlingAnalysis;
import iap.blocks.hull.BlCalcConvexHull;
import iap.blocks.maize.BlCalcColorHistograms;
import iap.blocks.maize.BlCalcMainAxis;
import iap.blocks.maize.BlCalcWidthAndHeight;
import iap.blocks.maize.BlRemoveBackground;
import iap.blocks.maize.BlDetectBlueMarkers;
import iap.blocks.maize.BlIntensityCalculationFluo;
import iap.blocks.maize.BlColorBalanceNir;
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
				BlLoadImages.class.getCanonicalName(),
				BlAlign.class.getCanonicalName(),
				BlColorBalanceFluo.class.getCanonicalName(),
				BlCreateDummyReferenceIfNeeded.class.getCanonicalName(),
				BlColorBalanceVis.class.getCanonicalName(),
				BlColorBalanceNir.class.getCanonicalName(),
				BlColorBalancingRoundCamera.class.getCanonicalName(),
				BlColorBalanceNir.class.getCanonicalName(),
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
				BlockRunPostProcessors.class.getCanonicalName(),

				BlockDrawSkeleton.class.getCanonicalName(),

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
