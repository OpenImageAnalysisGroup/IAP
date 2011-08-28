package de.ipk.ag_ba.image.analysis.barley;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.analysis.maize.AbstractImageProcessor;
import de.ipk.ag_ba.image.analysis.maize.BlockRemoveVerticalAndHorizontalStructures_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosing_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockColorBalancing_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockColorBalancing_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCopyImagesApplyMask_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCrop_images_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockLabFilter_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMedianFilter_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMoveMasksToImageSet_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockNirFilterSide_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallVerySmallClusters_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.debug.BlockLoadImagesIfNeeded_images_masks;
import de.ipk.ag_ba.image.operations.blocks.cmds.hull.BlockConvexHull_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalcMainAxis_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalcWidthAndHeight_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearBackgroundByRefComparison_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearMasksBasedOnMarkers_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockFindBlueMarkers_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockIntensityConversion_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalcIntensity_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveLevitatingObjects_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveSmallStructuresUsingOpening_top_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockUseFluoMaskToClear_vis_nir;

/**
 * Comprehensive barley image analysis pipeline, processing VIS, FLUO and NIR images. Depends on reference images for initial comparison
 * and foreground / background separation.
 * 
 * @author klukas, pape, entzian
 */
public class BarleyAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	protected BlockPipeline getPipeline(ImageProcessorOptions options) {
		modifySettings(options);
		
		BlockPipeline p = new BlockPipeline();
		p.add(BlockLoadImagesIfNeeded_images_masks.class);
		p.add(BlockColorBalancing_vis.class);
		p.add(BlockFindBlueMarkers_vis.class);
		p.add(BlockColorBalancing_fluo_nir.class);
		p.add(BlockClearBackgroundByRefComparison_vis_fluo_nir.class);
		p.add(BlockMedianFilter_fluo.class);
		p.add(BlockMedianFilter_fluo.class);
		p.add(BlockLabFilter_vis_fluo.class);
		// p.add(BlockClosingOnFluo.class);
		p.add(BlockClosing_fluo.class);
		p.add(BlockRemoveSmallClusters_vis_fluo.class);
		p.add(BlockClearMasksBasedOnMarkers_vis_fluo_nir.class);
		p.add(BlockRemoveSmallStructuresUsingOpening_top_vis.class);
		p.add(BlockRemoveSmallClusters_vis_fluo.class);
		p.add(BlockMedianFilter_fluo.class);
		p.add(BlockRemoveLevitatingObjects_vis_fluo.class);
		p.add(BlockRemoveVerticalAndHorizontalStructures_vis_fluo.class);
		p.add(BlockUseFluoMaskToClear_vis_nir.class);
		p.add(BlockNirFilterSide_nir.class);
		p.add(BlockCopyImagesApplyMask_vis_fluo.class);
		
		// calculation of numeric values
		p.add(BlockCalcMainAxis_vis.class);
		p.add(BlockCalcWidthAndHeight_vis.class);
		p.add(BlockRemoveSmallClusters_vis_fluo.class);
		p.add(BlockIntensityConversion_fluo.class);
		p.add(BlockMedianFilter_fluo.class);
		p.add(BlockRemoveSmallVerySmallClusters_fluo.class);
		p.add(BlockCalcIntensity_vis_fluo_nir.class);
		p.add(BlockConvexHull_vis_fluo.class);
		
		// postprocessing
		p.add(BlockMoveMasksToImageSet_vis_fluo_nir.class);
		p.add(BlockCrop_images_vis_fluo_nir.class);
		
		return p;
	}
	
	/**
	 * Modify default LAB filter options according to the Maize analysis requirements.
	 */
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		options.setIsMaize(false);
		
		// Test Barley
		if (options.getCameraPosition() == CameraPosition.TOP) {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 100);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0); // green
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 135);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 123); // 130
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255); // all yellow
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 100);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 80); // 98 // 130 gerste wegen topf
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
		options.clearAndAddIntSetting(Setting.REAL_MARKER_DISTANCE, 1150); // for Barley
		
		options.clearAndAddIntSetting(Setting.L_Diff_FLUO, 120); // 20
		options.clearAndAddIntSetting(Setting.abDiff_FLUO, 120); // 20
		
		double cut = (0.001d) / 1;
		options.clearAndAddDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO, cut);
		options.clearAndAddDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS, cut * 0.2);
		
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
