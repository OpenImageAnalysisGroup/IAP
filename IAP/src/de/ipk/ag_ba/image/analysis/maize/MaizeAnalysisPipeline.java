package de.ipk.ag_ba.image.analysis.maize;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.IoStringProvider;
import org.SystemOptions;

import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlBalancing_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlColorBalancing_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlCopyImagesApplyMask_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlCreateDummyReferenceIfNeeded_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlCrop_images_vis_fluo_nir_ir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlLabFilter_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlLoadImagesIfNeeded_images_masks;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMedianFilter_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMoveMasksToImageSet_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlNirFilterSide_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlRemoveSmallClusters_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlReplaceEmptyOriginalImages_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearNirPot_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosing_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveMaizeBambooStick_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockSkeletonize_vis_or_fluo;
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
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearSmallBorderAroundImagesAndMasks;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveLevitatingObjects_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveSmallStructuresUsingOpening_top_vis;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockRunPostProcessors;

/**
 * Comprehensive corn image analysis pipeline, processing VIS, FLUO and NIR images. Depends on reference images for initial comparison
 * and foreground / background separation.
 * 
 * @author klukas, pape, entzian
 */
public class MaizeAnalysisPipeline extends AbstractImageProcessor {
	
	private final String pipelineFileName;
	private final IoStringProvider iniIO;
	
	public MaizeAnalysisPipeline(String pipelineFileName, IoStringProvider iniIO) {
		this.pipelineFileName = pipelineFileName;
		this.iniIO = iniIO;
	}
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		String[] defaultBlockList = new String[] {
				BlLoadImagesIfNeeded_images_masks.class.getCanonicalName(),
				BlCreateDummyReferenceIfNeeded_vis.class.getCanonicalName(),
				BlColorBalancing_vis.class.getCanonicalName(),
				BlFindBlueMarkers_vis.class.getCanonicalName(),
				BlockClearSmallBorderAroundImagesAndMasks.class.getCanonicalName(),
				BlBalancing_fluo.class.getCanonicalName(),
				BlockColorBalancing_vertical_nir.class.getCanonicalName(),
				BlBalancing_fluo.class.getCanonicalName(),
				BlClearBackgroundByRefComparison_vis_fluo_nir.class.getCanonicalName(),
				BlIntensityConversion_fluo.class.getCanonicalName(),
				BlockClearNirPot_nir.class.getCanonicalName(),
				BlockClearMasksBasedOnMarkers_vis_fluo_nir.class.getCanonicalName(),
				BlLabFilter_vis.class.getCanonicalName(),
				BlockClosing_vis.class.getCanonicalName(),
				// "beforeBloomEnhancement" image is saved in the following block
				// BlockClosingForMaizeBloom_vis_stores_image.class.getCanonicalName(),
				BlRemoveSmallClusters_vis_fluo.class.getCanonicalName(),
				BlockRemoveSmallStructuresUsingOpening_top_vis.class.getCanonicalName(),
				BlMedianFilter_fluo.class.getCanonicalName(),
				// BlockClosingForYellowVisMask.class.getCanonicalName(),
				BlRemoveSmallClusters_vis_fluo.class.getCanonicalName(), // requires lab filter before
				BlockRemoveMaizeBambooStick_vis.class.getCanonicalName(), // requires remove small clusters before (the processing would vertically stop at any
																								// noise)
				BlockRemoveLevitatingObjects_vis_fluo.class.getCanonicalName(),
				// BlTranslateMatch_vis_fluo_nir.class.getCanonicalName(),
				BlUseFluoMaskToClear_vis_nir.class.getCanonicalName(),
				
				BlockRemoveVerticalAndHorizontalStructures_vis_fluo.class.getCanonicalName(),
				BlRemoveSmallClusters_vis_fluo.class.getCanonicalName(), // 2nd run
				
				BlockSkeletonize_vis_or_fluo.class.getCanonicalName(),
				
				// BlockRemoveSmallClusters_vis.class.getCanonicalName(),
				// BlockRemoveMaizeBambooStick_vis.class.getCanonicalName(), // requires remove small clusters before (the processing would vertically stop at any
				// noise)
				// BlockRemoveLevitatingObjects_vis.class.getCanonicalName(),
				// BlockRemoveVerticalAndHorizontalStructures_vis.class.getCanonicalName(),
				// BlockRemoveSmallClusters_vis.class.getCanonicalName(), // 2nd run
				// BlUseFluoMaskToClear_vis_nir.class.getCanonicalName(),
				
				BlNirFilterSide_nir.class.getCanonicalName(),
				
				// BlockLabFilterVis.class.getCanonicalName(),
				BlCopyImagesApplyMask_vis_fluo.class.getCanonicalName(), // without nir
				
				// calculation of numeric values
				BlCalcWidthAndHeight_vis.class.getCanonicalName(),
				BlCalcMainAxis_vis.class.getCanonicalName(),
				BlLeafCurlingAnalysis_vis.class.getCanonicalName(),
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
		
		return getPipelineFromBlockList(pipelineFileName, iniIO, defaultBlockList);
	}
	
	/**
	 * Modify default LAB filter options according to the Maize analysis requirements.
	 */
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		
		// options.addBooleanSetting(Setting.DEBUG_TAKE_TIMES, true);
		
		SystemOptions so = SystemOptions.getInstance(pipelineFileName + ".pipeline.ini", iniIO);
		String g = "IMAGE-ANALYSIS-PIPELINE-SETTINGS-" + getClass().getCanonicalName();
		
		options.setSystemOptionStorage(so, g);
		
		options.setIsMaize(true);
		
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_TOP, 20);
		options.clearAndAddIntSetting(Setting.abDiff_VIS_TOP, 20);
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_SIDE, 20);
		options.clearAndAddIntSetting(Setting.abDiff_VIS_SIDE, 20);
		options.clearAndAddIntSetting(Setting.L_Diff_FLUO, 90);
		options.clearAndAddIntSetting(Setting.abDiff_FLUO, 90);
		options.clearAndAddIntSetting(Setting.B_Diff_NIR, 30); // 20
		options.clearAndAddIntSetting(Setting.W_Diff_NIR, 33); // 23
		options.clearAndAddIntSetting(Setting.B_Diff_NIR_TOP, 20); // 30); // 20
		options.clearAndAddIntSetting(Setting.W_Diff_NIR_TOP, 33);// 33); // 23
		
		options.clearAndAddIntSetting(Setting.REAL_MARKER_DISTANCE, 1448);
		// options.setSystemOptionStorage(null, null);
	}
	
	@Override
	public void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status) {
		this.status = status;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatus() {
		return status;
	}
	
	@Override
	public IAP_RELEASE getVersionTag() {
		return IAP_RELEASE.RELEASE_IAP_IMAGE_ANALYSIS_MAIZE;
	}
	
}
