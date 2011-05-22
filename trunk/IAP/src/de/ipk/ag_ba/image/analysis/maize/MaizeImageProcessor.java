package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockLabFilter;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters;
import de.ipk.ag_ba.image.operations.blocks.cmds.debug.BlockImageInfo;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalculateMainAxis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalculateWidthAndHeight;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearBackgroundByComparingNullImageAndImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearBelowMarker;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockFindBlueMarkers;

public class MaizeImageProcessor extends AbstractImageProcessor {
	
	public MaizeImageProcessor(ImageProcessorOptions options) {
		super(options);
	}
	
	@Override
	protected BlockPipeline getPipeline() {
		if (options.getCameraTyp() == CameraTyp.TOP) {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 128);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 146);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255);
			
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
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 132);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255);
			
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 104);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 98);
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 194);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 132);
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		}
		
		BlockPipeline p = new BlockPipeline(options);
		
		p.add(BlockImageInfo.class);
		p.add(BlockFindBlueMarkers.class);
		p.add(BlockClearBackgroundByComparingNullImageAndImage.class);
		p.add(BlockClearBelowMarker.class);
		p.add(BlockCalculateMainAxis.class);
		p.add(BlockLabFilter.class);
		p.add(BlockRemoveSmallClusters.class);
		p.add(BlockCalculateWidthAndHeight.class);
		
		return p;
	}
	
}
