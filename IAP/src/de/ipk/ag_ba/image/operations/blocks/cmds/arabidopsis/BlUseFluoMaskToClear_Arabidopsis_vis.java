package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlUseFluoMaskToClear_Arabidopsis_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().images().vis() == null || input().masks().fluo() == null)
			return input().masks().vis();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = input().masks().vis();
			
			return clearImageSide(input, input().masks().fluo(), 0.01);
		}
		
		// if (options.getCameraPosition() == CameraPosition.TOP) {
		// FlexibleImage input = input().masks().vis();
		//
		// return clearImageTop(input, input().masks().fluo());
		// }
		return input().masks().vis();
	}
	
	@Override
	protected FlexibleImage processVISimage() {
		if (input().images().vis() == null || input().masks().fluo() == null)
			return input().images().vis();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = input().images().vis();
			
			return clearImageSide(input, input().masks().fluo(), 0.01);
		}
		
		// if (options.getCameraPosition() == CameraPosition.TOP) {
		// FlexibleImage input = input().images().vis();
		//
		// return clearImageTop(input, input().masks().fluo());
		// }
		return input().images().vis();
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
		if (input == null || fluo == null)
			return input;
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
		if (processedMasks.fluo() == null) {
			return;
		}
		// if (options.getCameraPosition() == CameraPosition.SIDE) {
		int back = options.getBackground();
		if (processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			ImageOperation vis = processedMasks.vis() != null ? processedMasks.vis().copy().io().print("NIRRRR", debug) : null;
			FlexibleImage mask = processedMasks.fluo().copy().io().
					blur(options.getCameraPosition() == CameraPosition.SIDE ? 3 : 30).
					binary(Color.BLACK.getRGB(), options.getBackground()).print("blurred vis mask", debug).getImage();
			if (vis != null)
				processedMasks.setVis(vis.applyMask_ResizeMaskIfNeeded(
						mask,
						back).print("FILTERED VIS MASK", debug).getImage());
			if (processedImages.vis() != null)
				processedImages.setVis(processedImages.vis().io().applyMask_ResizeMaskIfNeeded(
						mask, back).print("FILTERED VIS IMAGE", debug).getImage());
			return;
		}
		// }
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = processedMasks.vis();
			if (input != null)
				processedMasks.setVis(clearImageSide(input, processedMasks.fluo(), 0.01).io().print("RRRR").getImage());
			return;
		}
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (processedMasks.vis() != null)
				processedMasks.setVis(clearImageTop(processedMasks.vis(), processedMasks.fluo()));
			return;
		}
	}
}
