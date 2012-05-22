package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Clears the fluo top image (not side), based on the vis mask.
 * 
 * @author Christian Klukas
 */
public class BlUseVisMaskToClearFluo_fluo extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().vis() == null || input().masks().fluo() == null)
			return input().masks().fluo();
		if (options.getCameraPosition() == CameraPosition.TOP) {
			// apply enlarged VIS mask to fluo
			ImageOperation fluo = input().masks().fluo().copy().io().print("FLUOOO", debug);
			int b = (int) (input().masks().vis().getWidth() * 0.3);
			FlexibleImage mask = input().masks().vis().copy().io().
					addBorder(b, b / 2, (b / 2), options.getBackground()).
					crop(0.23, 0.03, 0.285, 0.09).
					blur(options.isMaize() ? 25 : 5).
					binary(Color.BLACK.getRGB(), options.getBackground()).print("blurred vis mask", debug).getImage();
			if (debug)
				fluo.copy().or(mask.copy()).print("ORR");
			return fluo.applyMask_ResizeMaskIfNeeded(
					mask,
					options.getBackground()).print("FILTERED VIS", debug).getImage();
		} else
			return input().masks().fluo();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().images().nir() == null || input().masks().fluo() == null)
			return input().masks().nir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = input().masks().nir();
			
			return clearImageSide(input, input().masks().fluo(), 0.01);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = input().masks().nir();
			
			return clearImageTop(input, input().masks().fluo());
		}
		return input().masks().nir();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (input().images().nir() == null || input().masks().fluo() == null)
			return input().images().nir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = input().images().nir();
			
			return clearImageSide(input, input().masks().fluo(), 0.01);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = input().images().nir();
			
			return clearImageTop(input, input().masks().fluo());
		}
		return input().masks().nir();
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
		if (processedMasks.nir() == null || processedMasks.fluo() == null ||
				processedMasks.vis() == null) {
			processedMasks.setNir(input().masks().nir());
			return;
		}
		// if (options.getCameraPosition() == CameraPosition.TOP) {
		if (processedMasks.fluo() != null) {
			boolean printOR = false;
			
			if (printOR) {
				int w = input().masks().vis().getWidth();
				int h = input().masks().vis().getHeight();
				
				processedMasks.fluo().copy().resize(w, h).io().or(
						input().masks().vis()
						).print("OR operation", true);
			}
			// apply enlarged VIS mask to nir
			ImageOperation nir = processedMasks.nir().copy().io().print("NIRRRR", debug);
			FlexibleImage mask = processedMasks.vis().copy().io().or(
					input().masks().fluo()
					).print("OR operation", debug).blur(20).
					binary(Color.BLACK.getRGB(), options.getBackground()).print("blurred vis mask", debug).getImage();
			int gray = new Color(180, 180, 180).getRGB();
			int back = options.getBackground();
			processedMasks.setNir(nir.applyMask_ResizeMaskIfNeeded(
					mask,
					back).print("FILTERED NIR MASK", debug).getImage());
			processedImages.setNir(processedImages.nir().io().applyMask_ResizeMaskIfNeeded(
					mask,
					options.getBackground()).print("FILTERED NIR IMAGE", debug).
					replaceColor(back, gray).getImage());
			return;
		}
		// }
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = processedMasks.nir();
			
			processedMasks.setNir(clearImageSide(input, processedMasks.fluo(), 0.01));
			return;
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = processedMasks.nir();
			
			processedMasks.setNir(clearImageTop(input, processedMasks.fluo()));
			return;
		}
	}
	
}
