package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlockDecreaseImageAndMaskSize extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage vis = getInput().getImages().getVis();
		double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		return vis.resize((int) (scaleFactor * vis.getWidth()), (int) (scaleFactor * vis.getHeight()));
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		FlexibleImage vis = getInput().getImages().getFluo();
		double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		return vis.resize((int) (scaleFactor * vis.getWidth()), (int) (scaleFactor * vis.getHeight()));
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = getInput().getMasks().getVis();
		double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		return vis.resize((int) (scaleFactor * vis.getWidth()), (int) (scaleFactor * vis.getHeight()));
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluo = getInput().getMasks().getFluo();
		double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		return fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
	}
}
