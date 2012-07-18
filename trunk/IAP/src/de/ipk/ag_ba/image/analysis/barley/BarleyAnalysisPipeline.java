package de.ipk.ag_ba.image.analysis.barley;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.image.analysis.maize.AbstractImageProcessor;
import de.ipk.ag_ba.image.analysis.maize.BlockColorBalancing_vertical_nir;
import de.ipk.ag_ba.image.analysis.maize.BlockDrawSkeleton_vis_fluo;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlBalancing_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlColorBalancingRoundCamera_vis_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlColorBalancing_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlCopyImagesApplyMask_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlCreateDummyReferenceIfNeeded_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlCrop_images_vis_fluo_nir_ir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlLabFilter_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlLoadImagesIfNeeded_images_masks;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMedianFilter_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMoveMasksToImageSet_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlNirFilterSide_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlReplaceEmptyOriginalImages_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearNirPot_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosing_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockSkeletonize_vis_or_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.Barley.BlCutZoomedImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.Barley.BlTranslateMatch_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.hull.BlConvexHull_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlCalcIntensity_vis_fluo_nir_ir;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlCalcMainAxis_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlCalcWidthAndHeight_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlClearBackgroundByRefComparison_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlFindBlueMarkers_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlIntensityConversion_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlUseFluoMaskToClear_vis_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearMasksBasedOnMarkers_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockRunPostProcessors;

/**
 * Comprehensive barley image analysis pipeline, processing VIS, FLUO and NIR
 * images. Depends on reference images for initial comparison and foreground /
 * background separation.
 * 
 * @author klukas, pape, entzian
 */
public class BarleyAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	protected BlockPipeline getPipeline(ImageProcessorOptions options) {
		modifySettings(options);
		
		boolean skelet = true;
		
		BlockPipeline p = new BlockPipeline();
		p.add(BlLoadImagesIfNeeded_images_masks.class);
		p.add(BlCutZoomedImages.class);
		p.add(BlBalancing_fluo.class);
		p.add(BlCreateDummyReferenceIfNeeded_vis.class);
		p.add(BlColorBalancing_vis.class);
		p.add(BlockColorBalancing_vertical_nir.class);
		p.add(BlColorBalancingRoundCamera_vis_nir.class);
		p.add(BlockColorBalancing_vertical_nir.class);
		p.add(BlFindBlueMarkers_vis.class);
		p.add(BlBalancing_fluo.class);
		p.add(BlClearBackgroundByRefComparison_vis_fluo_nir.class);
		p.add(BlMedianFilter_fluo.class);
		p.add(BlMedianFilter_fluo.class);
		p.add(BlMedianFilter_fluo.class);
		p.add(BlMedianFilter_fluo.class);
		p.add(BlLabFilter_vis.class);
		p.add(BlockClosing_vis.class);
		p.add(BlockClearMasksBasedOnMarkers_vis_fluo_nir.class);
		// p.add(BlMedianFilter_vis.class);
		p.add(BlIntensityConversion_fluo.class);
		p.add(BlTranslateMatch_vis_fluo_nir.class);
		p.add(BlockClearNirPot_nir.class);
		p.add(BlMedianFilter_fluo.class);
		p.add(BlockRemoveSmallClusters_vis_fluo.class);
		p.add(BlUseFluoMaskToClear_vis_nir.class);
		p.add(BlNirFilterSide_nir.class);
		p.add(BlCopyImagesApplyMask_vis_fluo.class);
		
		if (skelet)
			p.add(BlockSkeletonize_vis_or_fluo.class);
		
		// calculation of numeric values
		p.add(BlCalcMainAxis_vis.class);
		p.add(BlCalcWidthAndHeight_vis.class);
		p.add(BlCalcIntensity_vis_fluo_nir_ir.class);
		p.add(BlConvexHull_fluo.class);
		// postprocessing
		p.add(BlockRunPostProcessors.class);
		if (skelet)
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
		
		options.setIsArabidopsis(false);
		options.setIsBarley(true);
		options.setIsMaize(false);
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 100);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0); // green
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 135);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 123); // 130
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255); // all
			// yellow
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 100);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 80); // 98
			// //
			// 130
			// gerste
			// wegen
			// topf
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 125);// 125
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		} else {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 123);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255);
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 100);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 98);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 130);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		}
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_SIDE, 7); // 20
		options.clearAndAddIntSetting(Setting.abDiff_VIS_SIDE, 7); // 20
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_TOP, 50); // 20
		options.clearAndAddIntSetting(Setting.abDiff_VIS_TOP, 20); // 20
		options.clearAndAddIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS, 0);
		options.clearAndAddIntSetting(Setting.REAL_MARKER_DISTANCE, 1150); // for
		// Barley
		
		options.clearAndAddIntSetting(Setting.L_Diff_FLUO, 10); // 20
		options.clearAndAddIntSetting(Setting.abDiff_FLUO, 10); // 20
		
		double cut = (0.001d) / 100; // seems to have no effect // ck 19.11.2011
		options.clearAndAddDoubleSetting(
				Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO, cut);
		options.clearAndAddDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS,
				cut * 0.1);
		options.addBooleanSetting(Setting.DRAW_CONVEX_HULL, true);
		options.addBooleanSetting(Setting.DRAW_SKELETON, true);
		
		// barley in Maize sytem
		options.clearAndAddIntSetting(Setting.FIXED_CROP_BOTTOM_POT_POSITION_VIS, 150);
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
		return IAP_RELEASE.RELEASE_IAP_IMAGE_ANALYSIS_BARLEY;
	}
}
