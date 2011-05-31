package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockUseFluoMaskToClearVisMask extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getVis();
			
			return clearImage(input);
		}
		return getInput().getMasks().getVis();
	}
	
	private FlexibleImage clearImage(FlexibleImage input) {
		ImageOperation ioVis = new ImageOperation(input);
		int background = options.getBackground();
		FlexibleImage fluo = getInput().getMasks().getFluo();
		ImageOperation ioFluo = new ImageOperation(fluo);
		int[] positions = ioFluo.getExtremePoints(background);
		
		if (positions == null)
			return input;
		
		double scaleFactor = getInput().getMasks().getVis().getWidth() / (double) getInput().getMasks().getFluo().getWidth();
		
		int bl = background; // Color.RED.getRGB();
		int br = background; // Color.YELLOW.getRGB();
		int ba = background; // Color.BLUE.getRGB();
		
		double s = scaleFactor;
		
		double pa = 0.1;
		double pl = 0.02;
		double pr = 0.01;
		
		positions[0] = (int) (positions[0] * s - pa * getInput().getMasks().getVis().getWidth());
		positions[1] = (int) (positions[1] * s);
		positions[2] = (int) (positions[2] * s - pl * getInput().getMasks().getVis().getWidth());
		positions[3] = (int) (positions[3] * s + pr * getInput().getMasks().getVis().getWidth());
		
		return ioVis.clearImageLeft(positions[2], bl).clearImageRight(positions[3], br).clearImageAbove(positions[0], ba).getImage();
	}
}
