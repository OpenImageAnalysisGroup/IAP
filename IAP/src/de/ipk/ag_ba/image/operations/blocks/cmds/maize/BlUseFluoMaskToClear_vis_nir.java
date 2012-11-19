package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlUseFluoMaskToClear_vis_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	boolean debugOR = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		if (input().masks().fluo() != null && input().masks().vis() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				FlexibleImage fluoMask = clearImageSide(input().masks().fluo(), input().masks().vis(), 0.05d);
				input().masks().setFluo(fluoMask);
			} else {
				FlexibleImage fluoMask = clearImageSide(input().masks().fluo(), input().masks().vis(), 0.01d);
				input().masks().setFluo(fluoMask);
			}
		}
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null || input().masks().fluo() == null)
			return input().masks().vis();
		if (options.getCameraPosition() == CameraPosition.TOP || (options.isBarley() && !options.isBarleyInBarleySystem())
				|| options.isMaize()) {
			if (input().masks().fluo() != null) {
				// apply enlarged FLUO mask to vis
				ImageOperation vis = input().masks().vis().copy().io().print("VISSSS", debug);
				FlexibleImage mask = input().masks().fluo().copy().io()
						.blur(options.isMaize() ? 15 : ((options.isBarley() && !options.isBarleyInBarleySystem()) ? 30 : 20)).
						binary(Color.BLACK.getRGB(), options.getBackground()).print("blurred fluo mask", debug).getImage();
				// if (options.isBarley() && !options.isBarleyInBarleySystem()) {
				// mask = mask.getIO().replaceColors(Color.BLACK.getRGB(), Color.BLUE.getRGB()).translate(0, 20).scale(0.96, 1).getImage();
				// }
				// input().masks().vis().copy().io().or(
				// mask).print("OR operation", debug);
				return vis.applyMask_ResizeMaskIfNeeded(
						mask,
						options.getBackground()).print("FILTERED VIS", debug).getImage();
			}
		}
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = input().masks().vis();
			
			return clearImageSide(input, input().masks().fluo(), 0.1).print("cleared", debug);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = input().masks().vis();
			
			return clearImageLeftAround(input, input().masks().fluo());
		}
		return input().masks().vis();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().images().nir() == null || input().masks().fluo() == null)
			return input().masks().nir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = input().masks().nir();
			
			return clearImageSide(input, input().masks().fluo().io().or(input().masks().vis()).getImage(), 0.01);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = input().masks().nir();
			
			return clearImageLeftAround(input, input().masks().fluo());
		}
		return input().masks().nir();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (input().images().nir() == null || input().masks().fluo() == null || options.isBarleyInBarleySystem())
			return input().images().nir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = input().images().nir();
			return clearImageSide(input, input().masks().fluo().io().or(input().masks().vis()).getImage(), 0.001);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = input().images().nir();
			
			return clearImageLeftAround(input, input().masks().fluo());
		}
		return input().masks().nir();
	}
	
	@Override
	protected FlexibleImage processIRimage() {
		if (input().images().ir() == null || input().masks().fluo() == null || options.isBarleyInBarleySystem())
			return input().images().ir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = input().images().ir();
			return clearImageSide(input, input().masks().fluo().io().or(input().masks().vis()).getImage(), 0.001);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = input().images().ir();
			
			return clearImageLeftAround(input, input().masks().fluo());
		}
		return input().masks().ir();
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
	
	private FlexibleImage clearImageLeftAround(FlexibleImage input, FlexibleImage fluo) {
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
			// apply enlarged VIS mask to nir
			ImageOperation nir = processedMasks.nir().copy().io().print("NIRRRR", debug);
			ImageOperation maskIO =
					options.isBarley() && !options.isBarleyInBarleySystem() ? input().masks().fluo().copy().io() :
							processedMasks.vis().copy().io().or(
									input().masks().fluo()
									).print("OR operation", debug);
			if (options.isBarley())
				maskIO = maskIO.blur(20);
			else
				maskIO = maskIO.blur(20);
			FlexibleImage mask = maskIO.binary(Color.BLACK.getRGB(), options.getBackground()).print("blurred vis mask", debug).getImage();
			int gray = new Color(180, 180, 180).getRGB();
			int back = options.getBackground();
			processedMasks.setNir(nir.applyMask_ResizeMaskIfNeeded(
					mask,
					back).print("FILTERED NIR MASK", debug).getImage());
			processedImages.setNir(processedImages.nir().io().applyMask_ResizeMaskIfNeeded(
					mask,
					options.getBackground()).print("FILTERED NIR IMAGE", debug).
					replaceColor(back, gray).getImage());
			
			if (options.getCameraPosition() == CameraPosition.SIDE)
				processedMasks.setNir(processedImages.nir().copy());
			
			if (options.isBarleyInBarleySystem() && options.getCameraPosition() == CameraPosition.SIDE)
				if (processedImages.nir() != null) {
					processedMasks.setNir(processedImages.nir().copy());
					return;
				}
			
			return;
		}
		// }
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = processedMasks.nir();
			
			processedMasks.setNir(clearImageSide(input, processedMasks.fluo(), 0.001));
			return;
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage input = processedMasks.nir();
			
			processedMasks.setNir(clearImageLeftAround(input, processedMasks.fluo()));
			
			return;
		}
	}
	
}
