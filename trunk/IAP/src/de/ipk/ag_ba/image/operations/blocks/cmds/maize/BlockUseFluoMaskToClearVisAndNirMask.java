package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockUseFluoMaskToClearVisAndNirMask extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getVis();
			
			return clearImage(input, getInput().getMasks().getFluo());
		}
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getNir();
			
			return clearImage(input, getInput().getMasks().getFluo());
		}
		return getInput().getMasks().getNir();
	}
	
	private FlexibleImage clearImage(FlexibleImage input, FlexibleImage fluo) {
		ImageOperation ioInput = new ImageOperation(input);
		int background = options.getBackground();
		ImageOperation ioFluo = new ImageOperation(fluo);
		int[] positions = ioFluo.getExtremePoints(background);
		if (positions == null)
			return input;
		
		double scaleFactor = input.getWidth() / (double) fluo.getWidth();
		
		int bl = background; // Color.RED.getRGB();
		int br = background; // Color.YELLOW.getRGB();
		int ba = background; // Color.ORANGE.getRGB();
		
		double s = scaleFactor;
		
		double pa = 0.1;
		double pl = 0.02;
		double pr = 0.02;
		
		positions[0] = (int) (positions[0] * s - pa * input.getWidth());
		positions[1] = (int) (positions[1] * s);
		positions[2] = (int) (positions[2] * s - pl * input.getWidth());
		positions[3] = (int) (positions[3] * s + pr * input.getWidth());
		
		return ioInput.clearImageLeft(positions[2], bl).clearImageRight(positions[3], br).clearImageAbove(positions[0], ba).getImage();
	}
}
