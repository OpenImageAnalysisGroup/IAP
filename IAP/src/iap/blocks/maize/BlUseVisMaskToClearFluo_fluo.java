package iap.blocks.maize;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Clears the fluo image, based on the vis mask.
 * 
 * @author Christian Klukas
 */
public class BlUseVisMaskToClearFluo_fluo extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().vis() == null || input().masks().fluo() == null)
			return input().masks().fluo();
		if (options.getCameraPosition() == CameraPosition.TOP) {
			// apply enlarged VIS mask to fluo
			ImageOperation fluo = input().masks().fluo().copy().io().show("FLUO", debug);
			int b = (int) (input().masks().vis().getWidth() * 0.3);
			FlexibleImage mask = input().masks().vis().copy().io().
					addBorder(b, b / 2, (b / 2), options.getBackground()).
					crop(0.23, 0.03, 0.285, 0.09).
					blur(options.isMaize() ? 25 : 5).
					binary(Color.BLACK.getRGB(), options.getBackground()).show("blurred vis mask", debug).getImage();
			if (debug)
				fluo.copy().or(mask.copy()).show("ORR");
			return fluo.applyMask_ResizeMaskIfNeeded(
					mask,
					options.getBackground()).show("FILTERED VIS", debug).getImage();
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
						).show("OR operation", true);
			}
			// apply enlarged VIS mask to nir
			ImageOperation nir = processedMasks.nir().copy().io().show("NIRRRR", debug);
			FlexibleImage mask = processedMasks.vis().copy().io().or(
					input().masks().fluo()
					).show("OR operation", debug).blur(20).
					binary(Color.BLACK.getRGB(), options.getBackground()).show("blurred vis mask", debug).getImage();
			int gray = new Color(180, 180, 180).getRGB();
			int back = options.getBackground();
			processedMasks.setNir(nir.applyMask_ResizeMaskIfNeeded(
					mask,
					back).show("FILTERED NIR MASK", debug).getImage());
			processedImages.setNir(processedImages.nir().io().applyMask_ResizeMaskIfNeeded(
					mask,
					options.getBackground()).show("FILTERED NIR IMAGE", debug).
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
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
}
