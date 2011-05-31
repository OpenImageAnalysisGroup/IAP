package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCropImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockLabFilterMaskToMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockLabFilterMaskToMaskBlack;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMoveMasksToImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters;
import de.ipk.ag_ba.image.operations.blocks.cmds.debug.BlockImageInfo;
import de.ipk.ag_ba.image.operations.blocks.cmds.hull.BlockConvexHullOnFLuo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalculateMainAxis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalculateWidthAndHeight;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearBackgroundByComparingNullImageAndImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearBelowMarker;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockFindBlueMarkers;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockFluoToIntensity;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockIntensityAnalysis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveSmallStructuresFromVisAndFluoUsingOpening;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockUseFluoMaskToClearVisMask;

public class MaizeAnalysisPipelineProcessor extends AbstractImageProcessor {
	
	public MaizeAnalysisPipelineProcessor(ImageProcessorOptions options) {
		super(options);
	}
	
	@Override
	protected BlockPipeline getPipeline() {
		if (options.getCameraTyp() == CameraTyp.TOP) {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0); // green
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 111); // vorher: 128
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 128); // //146);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255); // all yellow
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 104);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 98);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 194);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 132);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		} else {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 125); // VORHER: 132);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255);
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 100); // 104
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 98);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 255);// 194
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 130);// 132
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		}
		
		BlockPipeline p = new BlockPipeline(options);
		
		p.add(BlockImageInfo.class);
		p.add(BlockFindBlueMarkers.class);
		p.add(BlockClearBackgroundByComparingNullImageAndImage.class);
		p.add(BlockClearBelowMarker.class);
		p.add(BlockLabFilterMaskToMask.class);
		p.add(BlockLabFilterMaskToMaskBlack.class);
		p.add(BlockRemoveSmallStructuresFromVisAndFluoUsingOpening.class);
		p.add(BlockRemoveSmallClusters.class);
		p.add(BlockUseFluoMaskToClearVisMask.class);
		p.add(BlockCalculateMainAxis.class);
		p.add(BlockCalculateWidthAndHeight.class);
		p.add(BlockFluoToIntensity.class);
		p.add(BlockIntensityAnalysis.class);
		p.add(BlockConvexHullOnFLuo.class);
		p.add(BlockMoveMasksToImages.class);
		p.add(BlockCropImages.class);
		
		return p;
	}
	
}
