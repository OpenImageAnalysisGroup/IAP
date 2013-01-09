package iap.pipelines.maize;

import iap.blocks.BlBalanceFluo;
import iap.blocks.BlColorBalancing;
import iap.blocks.BlCopyImagesApplyMask;
import iap.blocks.BlCreateDummyReferenceIfNeeded;
import iap.blocks.BlCrop;
import iap.blocks.BlLabFilter;
import iap.blocks.BlLoadImagesIfNeeded_images_masks;
import iap.blocks.BlMedianFilter_fluo;
import iap.blocks.BlMoveMasksToImageSet;
import iap.blocks.BlNirFilterSide_nir;
import iap.blocks.BlRemoveSmallClustersFromVisFluo;
import iap.blocks.BlReplaceEmptyOriginalImages;
import iap.blocks.BlockClearNirPotFromNir;
import iap.blocks.BlockClosingVis;
import iap.blocks.BlockRemoveMaizeBambooStick;
import iap.blocks.BlockSkeletonizeVisOrFluo;
import iap.blocks.curling.BlLeafCurlingAnalysis;
import iap.blocks.hull.BlConvexHull;
import iap.blocks.maize.BlCalcIntensity;
import iap.blocks.maize.BlCalcMainAxis;
import iap.blocks.maize.BlCalcWidthAndHeight;
import iap.blocks.maize.BlClearBackgroundByRefComparison_vis_fluo_nir;
import iap.blocks.maize.BlFindBlueMarkers;
import iap.blocks.maize.BlIntensityConversion;
import iap.blocks.maize.BlUseFluoMaskToClearVisNir;
import iap.blocks.maize.BlockClearMasksBasedOnMarkers;
import iap.blocks.maize.BlockClearSmallBorderAroundImagesAndMasks;
import iap.blocks.maize.BlockColorBalancingVertical;
import iap.blocks.maize.BlockDrawSkeleton_vis_fluo;
import iap.blocks.maize.BlockRemoveLevitatingObjectsFromVisFluo;
import iap.blocks.maize.BlockRemoveSmallStructuresUsingOpeningFromTopVis;
import iap.blocks.maize.BlockRemoveVerticalAndHorizontalStructuresFromVisFluo;
import iap.blocks.post_process.BlockRunPostProcessors;
import iap.pipelines.AbstractImageProcessor;
import iap.pipelines.ImageProcessorOptions;
import iap.pipelines.ImageProcessorOptions.Setting;

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
	
	public MaizeAnalysisPipeline(SystemOptions so) {
		this.so = so;
	}
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		String[] defaultBlockList = new String[] {
				BlLoadImagesIfNeeded_images_masks.class.getCanonicalName(),
				BlCreateDummyReferenceIfNeeded.class.getCanonicalName(),
				BlColorBalancing.class.getCanonicalName(),
				BlFindBlueMarkers.class.getCanonicalName(),
				BlockClearSmallBorderAroundImagesAndMasks.class.getCanonicalName(),
				BlBalanceFluo.class.getCanonicalName(),
				BlockColorBalancingVertical.class.getCanonicalName(),
				BlBalanceFluo.class.getCanonicalName(),
				BlClearBackgroundByRefComparison_vis_fluo_nir.class.getCanonicalName(),
				BlIntensityConversion.class.getCanonicalName(),
				BlockClearNirPotFromNir.class.getCanonicalName(),
				BlockClearMasksBasedOnMarkers.class.getCanonicalName(),
				BlLabFilter.class.getCanonicalName(),
				BlockClosingVis.class.getCanonicalName(),
				// "beforeBloomEnhancement" image is saved in the following block
				// BlockClosingForMaizeBloom_vis_stores_image.class.getCanonicalName(),
				BlRemoveSmallClustersFromVisFluo.class.getCanonicalName(),
				BlockRemoveSmallStructuresUsingOpeningFromTopVis.class.getCanonicalName(),
				BlMedianFilter_fluo.class.getCanonicalName(),
				// BlockClosingForYellowVisMask.class.getCanonicalName(),
				BlRemoveSmallClustersFromVisFluo.class.getCanonicalName(), // requires lab filter before
				BlockRemoveMaizeBambooStick.class.getCanonicalName(), // requires remove small clusters before (the processing would vertically stop at any
																								// noise)
				BlockRemoveLevitatingObjectsFromVisFluo.class.getCanonicalName(),
				// BlTranslateMatch_vis_fluo_nir.class.getCanonicalName(),
				BlUseFluoMaskToClearVisNir.class.getCanonicalName(),
				
				BlockRemoveVerticalAndHorizontalStructuresFromVisFluo.class.getCanonicalName(),
				BlRemoveSmallClustersFromVisFluo.class.getCanonicalName(), // 2nd run
				
				BlockSkeletonizeVisOrFluo.class.getCanonicalName(),
				
				// BlockRemoveSmallClusters_vis.class.getCanonicalName(),
				// BlockRemoveMaizeBambooStick_vis.class.getCanonicalName(), // requires remove small clusters before (the processing would vertically stop at any
				// noise)
				// BlockRemoveLevitatingObjects_vis.class.getCanonicalName(),
				// BlockRemoveVerticalAndHorizontalStructures_vis.class.getCanonicalName(),
				// BlockRemoveSmallClusters_vis.class.getCanonicalName(), // 2nd run
				// BlUseFluoMaskToClear_vis_nir.class.getCanonicalName(),
				
				BlNirFilterSide_nir.class.getCanonicalName(),
				
				// BlockLabFilterVis.class.getCanonicalName(),
				BlCopyImagesApplyMask.class.getCanonicalName(), // without nir
				
				// calculation of numeric values
				BlCalcWidthAndHeight.class.getCanonicalName(),
				BlCalcMainAxis.class.getCanonicalName(),
				BlLeafCurlingAnalysis.class.getCanonicalName(),
				BlCalcIntensity.class.getCanonicalName(),
				BlConvexHull.class.getCanonicalName(),
				
				// postprocessing
				BlockRunPostProcessors.class.getCanonicalName(),
				BlockDrawSkeleton_vis_fluo.class.getCanonicalName(),
				BlMoveMasksToImageSet.class.getCanonicalName(),
				BlCrop.class.getCanonicalName(),
				BlReplaceEmptyOriginalImages.class.getCanonicalName()
		};
		
		modifySettings(options);
		
		return getPipelineFromBlockList(so, defaultBlockList);
	}
	
	/**
	 * Modify default LAB filter options according to the Maize analysis requirements.
	 */
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		
		// options.addBooleanSetting(Setting.DEBUG_TAKE_TIMES, true);
		
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
	
}
