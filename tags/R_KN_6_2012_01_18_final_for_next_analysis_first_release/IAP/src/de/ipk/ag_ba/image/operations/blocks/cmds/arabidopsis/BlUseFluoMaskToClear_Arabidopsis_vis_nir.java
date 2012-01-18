package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlUseFluoMaskToClear_Arabidopsis_vis_nir extends AbstractSnapshotAnalysisBlockFIS {
	
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
		
		FlexibleImage input = getInput().getMasks().getVis();
		
		return clearImageSide(input, getInput().getMasks().getFluo(), 0.1).print("cleared", debug);
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getImages().getNir() == null || getInput().getMasks().getFluo() == null)
			return getInput().getMasks().getNir();
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
		
		if (options.isArabidopsis())
			return ioInputForCut.clearImageLeft(positions.getLeftX() * 0.95, bl).
					clearImageRight(positions.getRightX() * 1.05, br)
					.clearImageAbove(positions.getTopY() * 0.95, ba)
					.clearImageBottom((int) (positions.getBottomY() * 1.15d), ba)
					.getImage();
		else
			return ioInputForCut.clearImageLeft(options.isBarleyInBarleySystem() ? positions.getLeftX() * 0.7 : positions.getLeftX(), bl).
					clearImageRight(options.isBarleyInBarleySystem() ? positions.getRightX() * 1.3 : positions.getRightX(), br)
					.clearImageAbove(positions.getTopY() * 0.95, ba)
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
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		if (processedMasks.getNir() == null || processedMasks.getFluo() == null) {
			processedMasks.setNir(getInput().getMasks().getNir());
			return;
		}
		// if (options.getCameraPosition() == CameraPosition.TOP) {
		int gray = new Color(180, 180, 180).getRGB();
		int back = options.getBackground();
		if (processedMasks.getFluo() != null) {
			// apply enlarged VIS mask to nir
			ImageOperation nir = processedMasks.getNir().copy().getIO().print("NIRRRR", debug);
			FlexibleImage mask = processedMasks.getFluo().copy().getIO().blur(3).
					binary(Color.BLACK.getRGB(), options.getBackground()).print("blurred vis mask", debug).getImage();
			processedMasks.setNir(nir.applyMask_ResizeMaskIfNeeded(
					mask,
					back).print("FILTERED NIR MASK", debug).getImage());
			processedImages.setNir(processedImages.getNir().getIO().applyMask_ResizeMaskIfNeeded(
					mask, back).print("FILTERED NIR IMAGE", debug).getImage());
			return;
		}
		// }
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = processedMasks.getNir();
			
			processedMasks.setNir(clearImageSide(input, processedMasks.getFluo(), 0.01).getIO().
					replaceColorsScanLine(back, gray).print("RRRR").getImage());
			return;
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = processedMasks.getNir();
			
			processedMasks.setNir(clearImageTop(input, processedMasks.getFluo()));
			return;
		}
	}
	
}
