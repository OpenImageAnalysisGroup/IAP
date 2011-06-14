package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resize masks images to largest width and height.
 * Cuts of 5% from border of vis and fluo.
 * 
 * @author klukas
 */
public class BlockDecreaseImageAndMaskSize extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage vis = getInput().getImages().getVis();
		double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		return pre(vis.resize((int) (scaleFactor * vis.getWidth()), (int) (scaleFactor * vis.getHeight())));
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		FlexibleImage vis = getInput().getImages().getFluo();
		double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		return pre(vis.resize((int) (scaleFactor * vis.getWidth()), (int) (scaleFactor * vis.getHeight())));
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = getInput().getMasks().getVis();
		double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		return pre(vis.resize((int) (scaleFactor * vis.getWidth()), (int) (scaleFactor * vis.getHeight())));
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluo = getInput().getMasks().getFluo();
		double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		return pre(fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight())));
	}
	
	private FlexibleImage pre(FlexibleImage image) {
		if (image != null && image.getWidth() > 20 && image.getHeight() > 20)
			return image.crop(0.025, 0.025, 0.025, 0.025);
		else
			return null;
	}
}
