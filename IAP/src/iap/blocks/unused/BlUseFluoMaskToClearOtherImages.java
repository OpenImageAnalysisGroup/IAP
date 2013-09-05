package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;

public class BlUseFluoMaskToClearOtherImages extends AbstractSnapshotAnalysisBlock {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		if (input().masks().fluo() != null && input().masks().vis() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				Image fluoMask = clearImageSide(input().masks().fluo(), input().masks().vis(), 0.05d);
				input().masks().setFluo(fluoMask);
			} else {
				Image fluoMask = clearImageSide(input().masks().fluo(), input().masks().vis(), 0.01d);
				input().masks().setFluo(fluoMask);
			}
		}
	}
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null || input().masks().fluo() == null)
			return input().masks().vis();
		
		Image input = input().masks().vis();
		
		Image visMask;
		
		if (options.getCameraPosition() == CameraPosition.TOP)
			visMask = input.copy();
		else
			visMask = clearImageSide(input, input().masks().fluo(), 0.1).show("cleared", debug);
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (input().masks().fluo() != null) {
				// apply enlarged fluo mask to vis
				Image mask = input().masks().fluo().copy().io().
						crop(0.06, 0.08, 0.04, 0.02).show("Cropped Fluo Mask", false).
						blur(getDouble("blur VIS mask", 20)).
						binary(Color.BLACK.getRGB(), options.getBackground()).show("blurred fluo mask", debug).getImage();
				if (debug)
					visMask.copy().io().or(mask.copy()).show("ORR");
				visMask = visMask.io().applyMask_ResizeMaskIfNeeded(
						mask,
						options.getBackground()).show("FILTERED VIS", debug).getImage();
			}
		}
		
		return visMask;
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().images().nir() == null || input().masks().fluo() == null)
			return input().masks().nir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			Image input = input().masks().nir();
			
			return clearImageSide(input, input().masks().fluo().io().or(input().masks().vis()).getImage(), 0.01);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			Image input = input().masks().nir();
			
			return clearImageTop(input, input().masks().fluo());
		}
		return input().masks().nir();
	}
	
	@Override
	protected Image processNIRimage() {
		if (input().images().nir() == null || input().masks().fluo() == null)
			return input().images().nir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			Image input = input().images().nir();
			
			return clearImageSide(input, input().masks().fluo(), 0.01);
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			Image input = input().images().nir();
			
			return clearImageTop(input, input().masks().fluo());
		}
		return input().masks().nir();
	}
	
	private Image clearImageSide(Image inputToCut, Image imageSource, double cutTop) {
		if (inputToCut == null || imageSource == null)
			return null;
		ImageOperation ioInputForCut = new ImageOperation(inputToCut);
		int background = options.getBackground();
		ImageOperation ioSource = new ImageOperation(imageSource);
		TopBottomLeftRight positions = ioSource.getExtremePoints(background);
		if (positions == null)
			return inputToCut;
		
		double scaleFactor = inputToCut.getWidth() / (double) imageSource.getWidth();
		
		if (inputToCut.getCameraType() == CameraType.NIR)
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
		double offsetFactorL = getDouble("Offset Factor Left", 0.05);
		double offsetFactorR = getDouble("Offset Factor Right", 0.05);
		double offsetFactorT = getDouble("Offset Factor Top", 0.05);
		double offsetFactorB = getDouble("Offset Factor Bottom", 0.15);
		return ioInputForCut.clearImageLeft(positions.getLeftX() * (1 - offsetFactorL), bl).
				clearImageRight(positions.getRightX() * (1 + offsetFactorR), br)
				.clearImageAbove(positions.getTopY() * (1 - offsetFactorT), ba)
				.clearImageBottom((int) (positions.getBottomY() * (1 + offsetFactorB)), ba)
				.getImage();
	}
	
	private Image clearImageTop(Image input, Image fluo) {
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
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		if (processedMasks.nir() == null || processedMasks.fluo() == null) {
			processedMasks.setNir(input().masks().nir());
			return;
		}
		// if (options.getCameraPosition() == CameraPosition.TOP) {
		int gray = new Color(180, 180, 180).getRGB();
		int back = options.getBackground();
		if (processedMasks.fluo() != null) {
			// apply enlarged VIS mask to nir
			ImageOperation nir = processedMasks.nir().copy().io().show("NIRRRR", debug);
			Image mask = processedMasks.fluo().copy().io().blur(3).
					binary(Color.BLACK.getRGB(), options.getBackground()).show("blurred vis mask", debug).getImage();
			processedMasks.setNir(nir.applyMask_ResizeMaskIfNeeded(
					mask,
					back).show("FILTERED NIR MASK", debug).getImage());
			processedImages.setNir(processedImages.nir().io().applyMask_ResizeMaskIfNeeded(
					mask, back).show("FILTERED NIR IMAGE", debug).getImage());
			
			// if (processedMasks.getIr() != null) {
			// processedMasks.setIr(processedImages.getIr().getIO().applyMask_ResizeMaskIfNeeded(
			// mask, back).print("FILTERED IR IMAGE", debug).getImage());
			// }
			
			return;
		}
		// }
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			Image input = processedMasks.nir();
			
			processedMasks.setNir(clearImageSide(input, processedMasks.fluo(), 0.01).io().
					replaceColorsScanLine(back, gray).show("RRRR").getImage());
			return;
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			Image input = processedMasks.nir();
			
			processedMasks.setNir(clearImageTop(input, processedMasks.fluo()));
			return;
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
}
