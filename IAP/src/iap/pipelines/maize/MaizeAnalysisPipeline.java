package iap.pipelines.maize;

import iap.blocks.BlAdaptiveThresholdNir;
import iap.blocks.BlClosing;
import iap.blocks.BlColorBalanceFluo;
import iap.blocks.BlColorBalanceVis;
import iap.blocks.BlHighlightNullResults;
import iap.blocks.BlLabFilter;
import iap.blocks.BlLoadImages;
import iap.blocks.BlMedianFilterFluo;
import iap.blocks.BlMoveMasksToImageSet;
import iap.blocks.BlRemoveSmallObjectsVisFluo;
import iap.blocks.BlSkeletonizeNir;
import iap.blocks.BlSkeletonizeVisFluo;
import iap.blocks.BlUseFluoMaskToClearOther;
import iap.blocks.BlockCutFromSide;
import iap.blocks.arabidopsis.BlAlign;
import iap.blocks.arabidopsis.BlRotate;
import iap.blocks.hull.BlCalcConvexHull;
import iap.blocks.maize.BlCalcColorHistograms;
import iap.blocks.maize.BlCalcMainAxis;
import iap.blocks.maize.BlCalcWidthAndHeight;
import iap.blocks.maize.BlColorBalanceNir;
import iap.blocks.maize.BlDetectBlueMarkers;
import iap.blocks.maize.BlIntensityCalculationFluo;
import iap.blocks.maize.BlRemoveBackground;
import iap.blocks.maize.BlockDrawSkeleton;
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
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	public MaizeAnalysisPipeline(SystemOptions so) {
		this.so = so;
	}
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		String[] defaultBlockList = new String[] {
				// data aquisition
				BlLoadImages.class.getCanonicalName(),
				
				// preprocessing
				BlRotate.class.getCanonicalName(),
				BlAlign.class.getCanonicalName(),
				BlColorBalanceVis.class.getCanonicalName(),
				BlDetectBlueMarkers.class.getCanonicalName(),
				BlColorBalanceFluo.class.getCanonicalName(),
				BlColorBalanceNir.class.getCanonicalName(),
				BlColorBalanceFluo.class.getCanonicalName(),
				BlockCutFromSide.class.getCanonicalName(),
				
				// segmentation
				BlRemoveBackground.class.getCanonicalName(),
				BlIntensityCalculationFluo.class.getCanonicalName(),
				BlLabFilter.class.getCanonicalName(),
				BlAdaptiveThresholdNir.class.getCanonicalName(),
				BlClosing.class.getCanonicalName(),
				BlMedianFilterFluo.class.getCanonicalName(),
				BlRemoveSmallObjectsVisFluo.class.getCanonicalName(),
				BlUseFluoMaskToClearOther.class.getCanonicalName(),
				
				// feature extraction
				BlSkeletonizeVisFluo.class.getCanonicalName(),
				BlSkeletonizeNir.class.getCanonicalName(),
				BlCalcWidthAndHeight.class.getCanonicalName(),
				BlCalcMainAxis.class.getCanonicalName(),
				BlCalcColorHistograms.class.getCanonicalName(),
				BlCalcConvexHull.class.getCanonicalName(),
				
				// postprocessing
				BlockRunPostProcessors.class.getCanonicalName(),
				BlockDrawSkeleton.class.getCanonicalName(),
				BlMoveMasksToImageSet.class.getCanonicalName(),
				BlHighlightNullResults.class.getCanonicalName()
		};
		
		options.setSystemOptionStorage(so);
		
		return getPipelineFromBlockList(so, defaultBlockList);
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
