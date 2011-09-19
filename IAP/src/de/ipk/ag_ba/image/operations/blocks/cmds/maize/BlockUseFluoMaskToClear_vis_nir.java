package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlockUseFluoMaskToClear_vis_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		if (getInput().getMasks().getFluo() != null && getInput().getMasks().getVis() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				FlexibleImage fluoMask = clearImageSide(getInput().getMasks().getFluo(), getInput().getMasks().getVis(), 0.05d);
				getInput().getMasks().setFluo(fluoMask);
			} else {
				FlexibleImage fluoMask = clearImageSide(getInput().getMasks().getFluo(), getInput().getMasks().getVis(), 0.01d);
				getInput().getMasks().setFluo(fluoMask);
			}
		}
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null || getInput().getMasks().getFluo() == null)
			return getInput().getMasks().getVis();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getMasks().getVis();
			
			return clearImageSide(input, getInput().getMasks().getFluo(), 0.1).print("cleared", debug);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = getInput().getMasks().getVis();
			
			return clearImageTop(input, getInput().getMasks().getFluo());
		}
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getMasks().getNir() == null || getInput().getMasks().getFluo() == null)
			return getInput().getMasks().getNir();
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (getInput().getMasks().getVis() != null) {
				// apply enlarged vis mask to nir
				ImageOperation nir = getInput().getMasks().getNir().copy().getIO().print("NIRRRR", debug);
				FlexibleImage mask = getInput().getMasks().getVis().copy().getIO().blur(13).
						binary(Color.BLACK.getRGB(), options.getBackground()).print("blurred vis mask", debug).getImage();
				return nir.applyMask_ResizeMaskIfNeeded(
						mask,
						options.getBackground()).print("FILTERED NIR", debug).getImage();
			}
		}
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getMasks().getNir();
			
			return clearImageSide(input, getInput().getMasks().getFluo(), 0.01);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = getInput().getMasks().getNir();
			
			return clearImageTop(input, getInput().getMasks().getFluo());
		}
		return getInput().getMasks().getNir();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (getInput().getImages().getNir() == null || getInput().getMasks().getFluo() == null)
			return getInput().getImages().getNir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getImages().getNir();
			
			return clearImageSide(input, getInput().getMasks().getFluo(), 0.01);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = getInput().getImages().getNir();
			
			return clearImageTop(input, getInput().getMasks().getFluo());
		}
		return getInput().getMasks().getNir();
	}
	
	private FlexibleImage clearImageSide(FlexibleImage inputToCut, FlexibleImage imageSource, double cutTop) {
		if (inputToCut == null || imageSource == null)
			return null;
		ImageOperation ioInputForCut = new ImageOperation(inputToCut);
		int background = options.getBackground();
		ImageOperation ioSource = new ImageOperation(imageSource);
		TopBottomLeftRight positions = ioSource.getExtremePoints(background);
		if (positions == null)
			return inputToCut;
		
		double scaleFactor = inputToCut.getWidth() / (double) imageSource.getWidth();
		
		if (inputToCut.getType() == FlexibleImageType.NIR)
			background = options.getNirBackground();
		
		int bl = background; // Color.RED.getRGB();
		int br = background; // Color.YELLOW.getRGB();
		int ba = background; // Color.ORANGE.getRGB();
		
		double s = scaleFactor;
		
		double pa = cutTop;
		double pl = 0.02;
		double pr = 0.02;
		
		double sv = inputToCut.getHeight() / (double) imageSource.getHeight();
		
		positions.setTop((int) (positions.getTopY() * sv - pa * inputToCut.getHeight()));
		positions.setBottom((int) (positions.getBottomY() * s));
		positions.setLeft((int) (positions.getLeftX() * s - pl * inputToCut.getWidth()));
		positions.setRight((int) (positions.getRightX() * s + pr * inputToCut.getWidth()));
		
		return ioInputForCut.clearImageLeft(positions.getLeftX(), bl).clearImageRight(positions.getRightX(), br).clearImageAbove(positions.getTopY(), ba)
				.getImage();
		// return ioInputForCut.clearImageAbove(positions.getTopY(), ba).getImage();
	}
	
	private FlexibleImage clearImageTop(FlexibleImage input, FlexibleImage fluo) {
		ImageOperation ioInput = new ImageOperation(input);
		int background = options.getBackground();
		ImageOperation ioFluo = new ImageOperation(fluo);
		TopBottomLeftRight positions = ioFluo.getExtremePoints(background);
		if (positions == null)
			return input;
		
		double scaleFactor = input.getWidth() / (double) fluo.getWidth();
		
		double s = scaleFactor;
		
		double pa = 0.08;
		
		positions.setTop((int) (positions.getTopY() * s - pa * input.getWidth()));
		positions.setBottom((int) (positions.getBottomY() * s + pa * input.getWidth()));
		positions.setLeft((int) (positions.getLeftX() * s - pa * input.getWidth()));
		positions.setRight((int) (positions.getRightX() * s + pa * input.getWidth()));
		
		return ioInput.clearImageLeft(positions.getLeftX(), background).clearImageRight(positions.getRightX(), background)
				.clearImageAbove(positions.getTopY(), background)
				.clearImageBottom(positions.getBottomY(), background).getImage();
	}
}
