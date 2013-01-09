package iap.pipelines.roots;

import iap.blocks.BlMoveImagesToMasks;
import iap.blocks.BlMoveMasksToImageSet;
import iap.blocks.arabidopsis.BlLoadImagesIfNeeded;
import iap.blocks.roots.BlRootsAddBorderAroundImage;
import iap.blocks.roots.BlRootsRemoveBoxAndNoise;
import iap.blocks.roots.BlRootsSharpenImage;
import iap.blocks.roots.BlRootsSkeletonize;
import iap.pipelines.AbstractImageProcessor;
import iap.pipelines.ImageProcessorOptions;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;

/**
 * Roots / Waterscan Pipeline
 * 
 * @author klukas, entzian
 */
public class RootsAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	private final SystemOptions so;
	
	public RootsAnalysisPipeline(SystemOptions so) {
		this.so = so;
	}
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		
		String[] defaultBlockList = new String[] {
				BlLoadImagesIfNeeded.class.getCanonicalName(),
				BlRootsAddBorderAroundImage.class.getCanonicalName(),
				BlMoveImagesToMasks.class.getCanonicalName(),
				BlRootsSharpenImage.class.getCanonicalName(),
				BlRootsRemoveBoxAndNoise.class.getCanonicalName(),
				BlRootsSkeletonize.class.getCanonicalName(),
				BlMoveMasksToImageSet.class.getCanonicalName()
		};
		modifySettings(options);
		
		return getPipelineFromBlockList(so, defaultBlockList);
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
	
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		
		String g = "IMAGE-ANALYSIS-PIPELINE-SETTINGS-" + getClass().getCanonicalName();
		
		options.setSystemOptionStorage(so, g);
	}
	
}
