package de.ipk.ag_ba.image.analysis.maize;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearNirTop;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosing;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosingForYellowVisMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockColorBalancing;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCopyImagesApplyMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCropImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockDecreaseImageAndMaskSize;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockDecreaseMaskSize;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockLabFilter;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMedianFilter;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMoveMasksToImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockNirProcessing;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveBambooStick;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters;
import de.ipk.ag_ba.image.operations.blocks.cmds.debug.BlockImageInfo;
import de.ipk.ag_ba.image.operations.blocks.cmds.hull.BlockConvexHullOnFLuo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalculateMainAxis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalculateWidthAndHeight;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearBackgroundByComparingNullImageAndImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearMasksBasedOnMarkers;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockFindBlueMarkers;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockFluoToIntensity;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockIntensityAnalysis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveSmallStructuresFromTopVisUsingOpening;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockUseFluoMaskToClearVisAndNirMask;

public class MaizeAnalysisPipelineProcessor extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	public MaizeAnalysisPipelineProcessor(ImageProcessorOptions options) {
		super(options);
	}
	
	@Override
	protected BlockPipeline getPipeline() {
		if (options.getCameraTyp() == CameraTyp.TOP) {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0); // green
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 120); // vorher: 128
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 130); // //146);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255); // all yellow
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 95);
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
		
		// preprocessing
		p.add(BlockDecreaseImageAndMaskSize.class); // divide input (but not NIR) by 2
		p.add(BlockColorBalancing.class);
		p.add(BlockImageInfo.class);
		p.add(BlockClearNirTop.class);
		p.add(BlockDecreaseMaskSize.class);
		p.add(BlockFindBlueMarkers.class);
		p.add(BlockClearBackgroundByComparingNullImageAndImage.class);
		p.add(BlockLabFilter.class);
		p.add(BlockClearMasksBasedOnMarkers.class);
		p.add(BlockRemoveSmallStructuresFromTopVisUsingOpening.class);
		p.add(BlockMedianFilter.class);
		p.add(BlockClosingForYellowVisMask.class);
		p.add(BlockClosing.class);
		p.add(BlockRemoveBambooStick.class);
		p.add(BlockRemoveSmallClusters.class);
		p.add(BlockLabFilter.class);
		// p.add(BlockRemoveSplitObjectsAbove.class);
		p.add(BlockUseFluoMaskToClearVisAndNirMask.class);
		p.add(BlockNirProcessing.class);
		p.add(BlockCopyImagesApplyMask.class); // without nir
		
		// calculation of numeric values
		p.add(BlockCalculateMainAxis.class);
		p.add(BlockCalculateWidthAndHeight.class);
		p.add(BlockFluoToIntensity.class);
		p.add(BlockIntensityAnalysis.class);
		p.add(BlockConvexHullOnFLuo.class);
		
		// postprocessing
		p.add(BlockMoveMasksToImages.class);
		p.add(BlockCropImages.class);
		
		return p;
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
