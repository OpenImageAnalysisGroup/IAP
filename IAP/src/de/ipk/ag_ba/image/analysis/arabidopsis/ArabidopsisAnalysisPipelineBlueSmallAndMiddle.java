package de.ipk.ag_ba.image.analysis.arabidopsis;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.image.analysis.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.analysis.maize.AbstractImageProcessor;
import de.ipk.ag_ba.image.analysis.maize.BlockDrawSkeleton_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlBalancing_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlColorBalancing_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlCrop_images_vis_fluo_nir_ir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlLabFilter_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMedianFilter_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMoveImagesToMasks_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMoveMasksToImageSet_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlRemoveSmallClusters_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlReplaceEmptyOriginalImages_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlClearMasks_Arabidopsis_PotAndTrayProcessing_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlCutZoomedImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlLoadImagesIfNeeded_images;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlRotate;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlUseFluoMaskToClear_Arabidopsis_ir;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlUseFluoMaskToClear_Arabidopsis_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlUseFluoMaskToClear_Arabidopsis_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.Bl_Arabidopsis_IRdiff_ir;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlockSkeletonize_Arabidopsis_vis_or_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.hull.BlConvexHull_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlCalcIntensity_vis_fluo_nir_ir;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlCalcWidthAndHeight_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlIntensityConversion_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockRunPostProcessors;

/**
 * Comprehensive barley image analysis pipeline, processing VIS, FLUO and NIR
 * images. Depends on reference images for initial comparison and foreground /
 * background separation.
 * 
 * @author klukas, pape, entzian
 */
public class ArabidopsisAnalysisPipelineBlueSmallAndMiddle extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		modifySettings(options);
		
		BlockPipeline p = new BlockPipeline();
		p.add(BlLoadImagesIfNeeded_images.class);
		p.add(BlBalancing_fluo.class);
		p.add(BlColorBalancing_vis.class);
		p.add(BlRotate.class);
		p.add(BlCutZoomedImages.class);
		p.add(BlClearMasks_Arabidopsis_PotAndTrayProcessing_vis_fluo_nir.class);
		p.add(BlMoveImagesToMasks_vis_fluo_nir.class);
		p.add(BlLabFilter_vis.class);
		p.add(BlIntensityConversion_fluo.class);
		p.add(BlMedianFilter_fluo.class);
		p.add(BlRemoveSmallClusters_vis_fluo.class);
		p.add(BlUseFluoMaskToClear_Arabidopsis_vis.class);
		p.add(BlUseFluoMaskToClear_Arabidopsis_nir.class);
		p.add(Bl_Arabidopsis_IRdiff_ir.class);
		p.add(BlUseFluoMaskToClear_Arabidopsis_ir.class);
		p.add(BlockSkeletonize_Arabidopsis_vis_or_fluo.class);
		p.add(BlCalcWidthAndHeight_vis.class);
		p.add(BlCalcIntensity_vis_fluo_nir_ir.class);
		p.add(BlConvexHull_vis_fluo.class);
		// postprocessing
		p.add(BlockRunPostProcessors.class);
		p.add(BlockDrawSkeleton_vis_fluo.class);
		p.add(BlMoveMasksToImageSet_vis_fluo_nir.class);
		p.add(BlCrop_images_vis_fluo_nir_ir.class);
		p.add(BlReplaceEmptyOriginalImages_vis_fluo_nir.class);
		return p;
	}
	
	/**
	 * Modify default LAB filter options according to the Maize analysis
	 * requirements.
	 */
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		
		// options.addBooleanSetting(Setting.DEBUG_TAKE_TIMES, true);
		
		options.setIsBarley(false);
		options.setIsMaize(false);
		options.setIsArabidopsis(true);
		
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_SIDE, 7); // 20
		options.clearAndAddIntSetting(Setting.abDiff_VIS_SIDE, 7); // 20
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_TOP, 50); // 20
		options.clearAndAddIntSetting(Setting.abDiff_VIS_TOP, 20); // 20
		options.clearAndAddIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS, 0);
		options.clearAndAddIntSetting(Setting.REAL_MARKER_DISTANCE, 1150); // for
		// Barley
		
		options.clearAndAddIntSetting(Setting.L_Diff_FLUO, 120); // 20
		options.clearAndAddIntSetting(Setting.abDiff_FLUO, 120); // 20
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
		return IAP_RELEASE.RELEASE_IAP_IMAGE_ANALYSIS_ARABIDOPSIS;
	}
	
}
