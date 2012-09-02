package de.ipk.ag_ba.image.analysis.roots;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.image.analysis.maize.AbstractImageProcessor;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMoveImagesToMasks_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlLoadImagesIfNeeded_images;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootScannDetectZoom;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootsRemoveDarkBoxBorder;

/**
 * Roots / Waterscan Pipeline
 * 
 * @author klukas
 */
public class RootsAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	protected BlockPipeline getPipeline(ImageProcessorOptions options) {
		BlockPipeline p = new BlockPipeline();
		p.add(BlLoadImagesIfNeeded_images.class);
		p.add(BlMoveImagesToMasks_vis_fluo_nir.class);
		p.add(BlRootScannDetectZoom.class);
		p.add(BlRootsRemoveDarkBoxBorder.class);
		return p;
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
}
