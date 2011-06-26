package de.ipk.ag_ba.image.analysis.maize;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearNirTop;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosingForYellowVisMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockColorBalancing;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCopyImagesApplyMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCropImages;
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
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveLevitatingObjects;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockRemoveSmallStructuresFromTopVisUsingOpening;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockUseFluoMaskToClearVisAndNirMask;

public class ImageProcessorMaizeAnalysis extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	protected BlockPipeline getPipeline(ImageProcessorOptions options) {
		if (options.getCameraPosition() == CameraPosition.TOP) {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0); // green
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 120);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 125); // 130
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255); // all yellow
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 55);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 90); // 98 // 130 gerste wegen topf
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 125);// 125
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		} else {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 125);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255);
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 100);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 98);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 130);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		}
		
		BlockPipeline p = new BlockPipeline();
		
		// p.add(BlockDecreaseImageAndMaskSize.class);
		p.add(BlockColorBalancing.class);
		p.add(BlockImageInfo.class);
		p.add(BlockClearNirTop.class);
		// p.add(BlockDecreaseMaskSize.class);
		p.add(BlockFindBlueMarkers.class);
		p.add(BlockClearBackgroundByComparingNullImageAndImage.class);
		p.add(BlockLabFilter.class);
		p.add(BlockClearMasksBasedOnMarkers.class);
		// p.add(BlockRemoveVerticalAndHorizontalStructures);
		p.add(BlockRemoveSmallStructuresFromTopVisUsingOpening.class);
		p.add(BlockMedianFilter.class);
		p.add(BlockClosingForYellowVisMask.class);
		// p.add(BlockClosing.class);
		
		p.add(BlockLabFilter.class);
		p.add(BlockRemoveSmallClusters.class); // requires lab filter before
		p.add(BlockRemoveBambooStick.class); // requires remove small clusters before
		p.add(BlockLabFilter.class);
		p.add(BlockRemoveLevitatingObjects.class);
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
