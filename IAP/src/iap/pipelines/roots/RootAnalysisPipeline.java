package iap.pipelines.roots;

import iap.blocks.postprocessing.BlMoveMasksToImageSet;
import iap.blocks.unused.BlLoadImagesIfNeeded;
import iap.blocks.unused.BlMoveImagesToMasks;
import iap.blocks.unused.BlRootsAddBorderAroundImage;
import iap.blocks.unused.BlRootsRemoveBoxAndNoise;
import iap.blocks.unused.BlRootsSharpenImage;
import iap.blocks.unused.BlRootsSkeletonize;
import iap.pipelines.AbstractImageProcessor;
import iap.pipelines.ImageProcessorOptions;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;

/**
 * Root analysis (scanned washed roots)
 * 
 * @author klukas
 */
public class RootAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	private final SystemOptions so;
	
	public RootAnalysisPipeline(SystemOptions so) {
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
		options.setSystemOptionStorage(so);
	}
	
}
