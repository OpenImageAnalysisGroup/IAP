package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockUseFluoMaskToClearVisAndNirMask extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected void prepare() {
		super.prepare();
		if (getInput().getMasks().getFluo() != null && getInput().getMasks().getVis() != null) {
			FlexibleImage fluoMask = clearImageSide(getInput().getMasks().getFluo(), getInput().getMasks().getVis(), -0.02d);
			getInput().getMasks().setFluo(fluoMask);
		}
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null || getInput().getMasks().getFluo() == null)
			return null;
		if (options.getCameraTyp() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getMasks().getVis();
			
			return clearImageSide(input, getInput().getMasks().getFluo(), 0.1);
		}
		
		if (options.getCameraTyp() == CameraPosition.TOP) {
			FlexibleImage input = getInput().getMasks().getVis();
			
			return clearImageTop(input, getInput().getMasks().getFluo());
		}
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getMasks().getNir() == null || getInput().getMasks().getFluo() == null)
			return null;
		if (options.getCameraTyp() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getMasks().getNir();
			
			return clearImageSide(input, getInput().getMasks().getFluo(), 0.03);
		}
		
		if (options.getCameraTyp() == CameraPosition.TOP) {
			FlexibleImage input = getInput().getMasks().getNir();
			
			return clearImageTop(input, getInput().getMasks().getFluo());
		}
		return getInput().getMasks().getNir();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (getInput().getImages().getNir() == null || getInput().getMasks().getFluo() == null)
			return null;
		if (options.getCameraTyp() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getImages().getNir();
			
			return clearImageSide(input, getInput().getMasks().getFluo(), 0.03);
		}
		
		if (options.getCameraTyp() == CameraPosition.TOP) {
			FlexibleImage input = getInput().getImages().getNir();
			
			return clearImageTop(input, getInput().getMasks().getFluo());
		}
		return getInput().getMasks().getNir();
	}
	
	private FlexibleImage clearImageSide(FlexibleImage input, FlexibleImage fluo, double cutTop) {
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
		
		double pa = cutTop;
		double pl = 0.02;
		double pr = 0.02;
		
		positions[0] = (int) (positions[0] * s - pa * input.getWidth());
		positions[1] = (int) (positions[1] * s);
		positions[2] = (int) (positions[2] * s - pl * input.getWidth());
		positions[3] = (int) (positions[3] * s + pr * input.getWidth());
		
		return ioInput.clearImageLeft(positions[2], bl).clearImageRight(positions[3], br).clearImageAbove(positions[0], ba).getImage();
	}
	
	private FlexibleImage clearImageTop(FlexibleImage input, FlexibleImage fluo) {
		ImageOperation ioInput = new ImageOperation(input);
		int background = options.getBackground();
		ImageOperation ioFluo = new ImageOperation(fluo);
		int[] positions = ioFluo.getExtremePoints(background);
		if (positions == null)
			return input;
		
		double scaleFactor = input.getWidth() / (double) fluo.getWidth();
		
		double s = scaleFactor;
		
		double pa = 0.07;
		
		positions[0] = (int) (positions[0] * s - pa * input.getWidth());
		positions[1] = (int) (positions[1] * s + pa * input.getWidth());
		positions[2] = (int) (positions[2] * s - pa * input.getWidth());
		positions[3] = (int) (positions[3] * s + pa * input.getWidth());
		
		return ioInput.clearImageLeft(positions[2], background).clearImageRight(positions[3], background).clearImageAbove(positions[0], background)
				.clearImageBottom(positions[1], background).getImage();
	}
}
