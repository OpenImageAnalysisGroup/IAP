package de.ipk.ag_ba.image.analysis.roots;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.IniIoProvider;
import org.SystemOptions;

import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.image.analysis.maize.AbstractImageProcessor;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMoveImagesToMasks_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMoveMasksToImageSet_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlLoadImagesIfNeeded_images;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootsAddBorderAroundImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootsRemoveBoxAndNoise;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootsSharpenImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootsSkeletonize;

/**
 * Roots / Waterscan Pipeline
 * 
 * @author klukas, entzian
 */
public class RootsAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	private final String pipelineName;
	private final IniIoProvider iniIO;
	
	public RootsAnalysisPipeline(String pipelineName, IniIoProvider iniIO) {
		this.pipelineName = pipelineName;
		this.iniIO = iniIO;
	}
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		
		String[] defaultBlockList = new String[] {
				BlLoadImagesIfNeeded_images.class.getCanonicalName(),
				BlRootsAddBorderAroundImage.class.getCanonicalName(),
				BlMoveImagesToMasks_vis_fluo_nir.class.getCanonicalName(),
				BlRootsSharpenImage.class.getCanonicalName(),
				BlRootsRemoveBoxAndNoise.class.getCanonicalName(),
				BlRootsSkeletonize.class.getCanonicalName(),
				BlMoveMasksToImageSet_vis_fluo_nir.class.getCanonicalName()
		};
		modifySettings(options);
		
		return getPipelineFromBlockList(pipelineName, iniIO, defaultBlockList);
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
	
	@Override
	public IAP_RELEASE getVersionTag() {
		return IAP_RELEASE.RELEASE_IAP_IMAGE_ANALYSIS_ROOTS;
	}
	
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		
		SystemOptions so = SystemOptions.getInstance(pipelineName + ".pipeline.ini", iniIO);
		String g = "IMAGE-ANALYSIS-PIPELINE-SETTINGS-" + getClass().getCanonicalName();
		
		options.setSystemOptionStorage(so, g);
	}
	
}
