package de.ipk.ag_ba.image.analysis.barley;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.image.analysis.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.analysis.maize.AbstractImageProcessor;
import de.ipk.ag_ba.image.analysis.maize.BlockColorBalancing_vertical_nir;
import de.ipk.ag_ba.image.analysis.maize.BlockDrawSkeleton_vis_fluo;
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
import de.ipk.ag_ba.image.operations.blocks.cmds.BlRemoveBlackBelt_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlRemoveSmallClusters_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlReplaceEmptyOriginalImages_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearNirPot_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosing_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockSkeletonize_vis_or_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.Barley.BlCutZoomedImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.Barley.BlTranslateMatch_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.Bl_Arabidopsis_IRdiff_ir;
import de.ipk.ag_ba.image.operations.blocks.cmds.curling.BlLeafCurlingAnalysis_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.hull.BlConvexHull_vis_fluo;
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
 * Comprehensive image analysis pipeline, processing VIS, FLUO and NIR
 * images. Depends on reference images for initial comparison and foreground /
 * background separation.
 * 
 * @author klukas, pape, entzian
 */
public class BarleyAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	private final String pipelineName;
	private final SystemOptions so;
	
	public BarleyAnalysisPipeline(String pipelineFileName) throws Exception {
		this.pipelineName = pipelineFileName;
		so = SystemOptions.getInstance(
				StringManipulationTools.getFileSystemName(pipelineFileName) + ".pipeline.ini",
				null);
	}
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		String[] defaultBlockList = new String[] {
				BlLoadImagesIfNeeded_images_masks.class.getCanonicalName(),
				BlCutZoomedImages.class.getCanonicalName(),
				BlBalancing_fluo.class.getCanonicalName(),
				BlCreateDummyReferenceIfNeeded_vis.class.getCanonicalName(),
				BlColorBalancing_vis.class.getCanonicalName(),
				BlockColorBalancing_vertical_nir.class.getCanonicalName(),
				BlColorBalancingRoundCamera_vis_nir.class.getCanonicalName(),
				BlockColorBalancing_vertical_nir.class.getCanonicalName(),
				BlFindBlueMarkers_vis.class.getCanonicalName(),
				BlBalancing_fluo.class.getCanonicalName(),
				BlClearBackgroundByRefComparison_vis_fluo_nir.class.getCanonicalName(),
				BlMedianFilter_fluo.class.getCanonicalName(),
				BlMedianFilter_fluo.class.getCanonicalName(),
				BlMedianFilter_fluo.class.getCanonicalName(),
				BlMedianFilter_fluo.class.getCanonicalName(),
				BlRemoveBlackBelt_vis_fluo.class.getCanonicalName(),
				BlLabFilter_vis.class.getCanonicalName(),
				BlockClosing_vis.class.getCanonicalName(),
				BlockClearMasksBasedOnMarkers_vis_fluo_nir.class.getCanonicalName(),
				// BlMedianFilter_vis.class.getCanonicalName(),
				Bl_Arabidopsis_IRdiff_ir.class.getCanonicalName(),
				BlLeafCurlingAnalysis_vis.class.getCanonicalName(),
				BlIntensityConversion_fluo.class.getCanonicalName(),
				BlTranslateMatch_vis_fluo_nir.class.getCanonicalName(),
				BlockClearNirPot_nir.class.getCanonicalName(),
				BlMedianFilter_fluo.class.getCanonicalName(),
				BlRemoveSmallClusters_vis_fluo.class.getCanonicalName(),
				BlUseFluoMaskToClear_vis_nir.class.getCanonicalName(),
				BlNirFilterSide_nir.class.getCanonicalName(),
				BlCopyImagesApplyMask_vis_fluo.class.getCanonicalName(),
				
				BlockSkeletonize_vis_or_fluo.class.getCanonicalName(),
				
				// calculation of numeric values
				BlCalcMainAxis_vis.class.getCanonicalName(),
				BlCalcWidthAndHeight_vis.class.getCanonicalName(),
				BlCalcIntensity_vis_fluo_nir_ir.class.getCanonicalName(),
				BlConvexHull_vis_fluo.class.getCanonicalName(),
				// postprocessing
				BlockRunPostProcessors.class.getCanonicalName(),
				
				BlockDrawSkeleton_vis_fluo.class.getCanonicalName(),
				
				BlMoveMasksToImageSet_vis_fluo_nir.class.getCanonicalName(),
				BlCrop_images_vis_fluo_nir_ir.class.getCanonicalName(),
				BlReplaceEmptyOriginalImages_vis_fluo_nir.class.getCanonicalName()
		};
		
		modifySettings(options);
		
		return getPipelineFromBlockList(pipelineName, null, defaultBlockList);
	}
	
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		
		// options.addBooleanSetting(Setting.DEBUG_TAKE_TIMES, true);
		
		options.setIsArabidopsis(false);
		options.setIsBarley(true);
		options.setIsMaize(false);
		
		String g = "IMAGE-ANALYSIS-PIPELINE-SETTINGS-" + getClass().getCanonicalName();
		
		options.setSystemOptionStorage(so, g);
		
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_SIDE, 7); // 20
		options.clearAndAddIntSetting(Setting.abDiff_VIS_SIDE, 7); // 20
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_TOP, 40); // 20
		options.clearAndAddIntSetting(Setting.abDiff_VIS_TOP, 20); // 20
		options.clearAndAddIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS, 0);
		options.clearAndAddIntSetting(Setting.REAL_MARKER_DISTANCE, 1150); // for
		// Barley
		
		options.clearAndAddIntSetting(Setting.L_Diff_FLUO, 10); // 20
		options.clearAndAddIntSetting(Setting.abDiff_FLUO, 10); // 20
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
		// +" "+so.getString("","Version", "1")
	}
}
