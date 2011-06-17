package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Recolor pictures according to white point (or black point for fluo).
 * 
 * @author pape
 */
public class BlockColorBalancing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage vis = getInput().getImages().getVis();
		if (vis == null)
			return null;
		ImageOperation io = new ImageOperation(vis);
		double[] pix = io.getProbablyWhitePixels(0.08);
		return io.ImageBalancing(255, pix).getImage();
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = getInput().getMasks().getVis();
		if (vis == null)
			return null;
		ImageOperation io = new ImageOperation(vis);
		double[] pix = io.getProbablyWhitePixels(0.08);
		return io.ImageBalancing(255, pix).getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		FlexibleImage fluo = getInput().getImages().getFluo();
		if (fluo == null)
			return null;
		ImageOperation io = new ImageOperation(fluo);
		double[] pix = io.invert().getProbablyWhitePixels(0.08);
		return io.ImageBalancing(255, pix).invert().getImage();
		
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluo = getInput().getMasks().getFluo();
		if (fluo == null)
			return null;
		ImageOperation io = new ImageOperation(fluo);
		double[] pix = io.invert().getProbablyWhitePixels(0.08);
		return io.ImageBalancing(255, pix).invert().getImage();
	}
}
