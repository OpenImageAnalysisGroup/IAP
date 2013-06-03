package iap.pipelines.maize;

import iap.blocks.acquisition.BlLoadImages;
import iap.blocks.extraction.BlCalcAreas;
import iap.blocks.extraction.BlCalcColorHistograms;
import iap.blocks.extraction.BlCalcConvexHull;
import iap.blocks.extraction.BlCalcMainAxis;
import iap.blocks.extraction.BlCalcVolumes;
import iap.blocks.extraction.BlCalcWidthAndHeight;
import iap.blocks.extraction.BlSkeletonizeNir;
import iap.blocks.extraction.BlSkeletonizeVisFluo;
import iap.blocks.postprocessing.BlDrawSkeleton;
import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.postprocessing.BlRunPostProcessors;
import iap.blocks.preprocessing.BlAlign;
import iap.blocks.preprocessing.BlColorBalanceVerticalFluo;
import iap.blocks.preprocessing.BlColorBalanceVerticalNir;
import iap.blocks.preprocessing.BlColorBalanceVerticalVis;
import iap.blocks.preprocessing.BlDetectBlueMarkers;
import iap.blocks.preprocessing.BlRotate;
import iap.blocks.preprocessing.BlockCutFromSide;
import iap.blocks.segmentation.BlAdaptiveThresholdNir;
import iap.blocks.segmentation.BlClosing;
import iap.blocks.segmentation.BlIntensityCalculationFluo;
import iap.blocks.segmentation.BlLabFilter;
import iap.blocks.segmentation.BlMedianFilterFluo;
import iap.blocks.segmentation.BlRemoveBackground;
import iap.blocks.segmentation.BlRemoveSmallObjectsVisFluo;
import iap.blocks.segmentation.BlUseFluoMaskToClearOther;
import iap.blocks.unused.BlColorBalanceCircularVisNir;
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
				// acquisition
				BlLoadImages.class.getCanonicalName(),
				
				// preprocessing
				BlRotate.class.getCanonicalName(),
				BlAlign.class.getCanonicalName(),
				BlColorBalanceVerticalVis.class.getCanonicalName(),
				BlColorBalanceVerticalFluo.class.getCanonicalName(),
				BlColorBalanceVerticalNir.class.getCanonicalName(),
				BlColorBalanceCircularVisNir.class.getCanonicalName(),
				BlDetectBlueMarkers.class.getCanonicalName(),
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
				BlCalcAreas.class.getCanonicalName(),
				BlCalcVolumes.class.getCanonicalName(),
				
				// postprocessing
				BlRunPostProcessors.class.getCanonicalName(),
				BlDrawSkeleton.class.getCanonicalName(),
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
