package de.ipk.ag_ba.image.analysis.maize;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosingForYellowVisMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockColorBalancingFluoAndNir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockColorBalancingVis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCopyImagesApplyMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCropImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockLabFilterVisFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMedianFilterFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMoveMasksToImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockNirProcessing;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveBambooStickVis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClustersVis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClustersVisFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockReplaceEmptyOriginalImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockSkeletonize;
import de.ipk.ag_ba.image.operations.blocks.cmds.debug.BlockImageInfo;
import de.ipk.ag_ba.image.operations.blocks.cmds.hull.BlockConvexHullOnFLuoOrVis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalculateMainAxis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalculateWidthAndHeight;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearBackgroundByComparingNullImageAndImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearMasksBasedOnMarkersVisFluoNir;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockFindBlueMarkers;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockFluoToIntensity;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockIntensityAnalysis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveLevitatingObjectsVis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveLevitatingObjectsVisFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveSmallStructuresUsingOpeningTopVis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockUseFluoMaskToClearVisAndNirMask;

/**
 * Comprehensive corn image analysis pipeline, processing VIS, FLUO and NIR images. Depends on reference images for initial comparison
 * and foreground / background separation.
 * 
 * @author klukas, pape, entzian
 */
public class MaizeAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	protected BlockPipeline getPipeline(ImageProcessorOptions options) {
		modifySettings(options);
		
		BlockPipeline p = new BlockPipeline();
		p.add(BlockImageInfo.class);
		p.add(BlockColorBalancingVis.class);
		p.add(BlockFindBlueMarkers.class);
		p.add(BlockColorBalancingFluoAndNir.class);
		p.add(BlockClearBackgroundByComparingNullImageAndImage.class);
		p.add(BlockClearMasksBasedOnMarkersVisFluoNir.class);
		p.add(BlockLabFilterVisFluo.class);
		// "beforeBloomEnhancement" image is saved in the following block
		p.add(BlockClosingForYellowVisMask.class);
		p.add(BlockRemoveSmallClustersVisFluo.class);
		p.add(BlockRemoveSmallStructuresUsingOpeningTopVis.class);
		p.add(BlockMedianFilterFluo.class);
		// p.add(BlockClosingForYellowVisMask.class);
		p.add(BlockRemoveSmallClustersVisFluo.class); // requires lab filter before
		p.add(BlockRemoveBambooStickVis.class); // requires remove small clusters before (the processing would vertically stop at any noise)
		p.add(BlockRemoveLevitatingObjectsVisFluo.class);
		p.add(BlockRemoveVerticalAndHorizontalStructuresVisFluo.class);
		p.add(BlockRemoveSmallClustersVisFluo.class); // 2nd run
		p.add(BlockUseFluoMaskToClearVisAndNirMask.class);
		// "skelton" image is saved in the following block
		// "beforeBloomEnhancement" is restored by the following block
		p.add(BlockSkeletonize.class);
		
		p.add(BlockRemoveSmallClustersVis.class);
		p.add(BlockRemoveBambooStickVis.class); // requires remove small clusters before (the processing would vertically stop at any noise)
		p.add(BlockRemoveLevitatingObjectsVis.class);
		p.add(BlockRemoveVerticalAndHorizontalStructuresVis.class);
		p.add(BlockRemoveSmallClustersVis.class); // 2nd run
		p.add(BlockUseFluoMaskToClearVisAndNirMask.class);
		
		p.add(BlockNirProcessing.class);
		
		// p.add(BlockLabFilterVis.class);
		p.add(BlockCopyImagesApplyMask.class); // without nir
		
		// calculation of numeric values
		p.add(BlockCalculateMainAxis.class);
		p.add(BlockCalculateWidthAndHeight.class);
		p.add(BlockFluoToIntensity.class);
		p.add(BlockIntensityAnalysis.class);
		p.add(BlockConvexHullOnFLuoOrVis.class);
		
		p.add(drawSkeletonOnImage.class);
		// postprocessing
		p.add(BlockMoveMasksToImages.class);
		p.add(BlockCropImages.class);
		p.add(BlockReplaceEmptyOriginalImage.class);
		
		return p;
	}
	
	/**
	 * Modify default LAB filter options according to the Maize analysis requirements.
	 */
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		options.setIsMaize(true);
		if (options.getCameraPosition() == CameraPosition.TOP) {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0); // green
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 120);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 125); // 130
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255); // all yellow
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 0);
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
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 122);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255);
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 100);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 98);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 130);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		}
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_TOP, 110);
		options.clearAndAddIntSetting(Setting.abDiff_VIS_TOP, 50);
		options.clearAndAddIntSetting(Setting.L_Diff_VIS_SIDE, 60);
		options.clearAndAddIntSetting(Setting.abDiff_VIS_SIDE, 40);
		options.clearAndAddIntSetting(Setting.L_Diff_FLUO, 90);
		options.clearAndAddIntSetting(Setting.abDiff_FLUO, 90);
		options.clearAndAddIntSetting(Setting.B_Diff_NIR, 20); // 30
		options.clearAndAddIntSetting(Setting.W_Diff_NIR, 23); // 33
		
		options.clearAndAddIntSetting(Setting.REAL_MARKER_DISTANCE, 1104);
		options.clearAndAddIntSetting(Setting.CLOSING_REPEAT, 2);
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
