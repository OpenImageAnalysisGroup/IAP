/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * Clear bamboo stick in visible image. Use lab filter to select the stick pixels (starting from top).
 * If there is more than one structure next to each other in the picture, the processing is stopped.
 * Only single sticks at the very top are cleared until a certain y position.
 * 
 * @author pape
 */
public class BlockRemoveBambooStickVis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		if (options.getCameraPosition() == CameraPosition.SIDE)
			if (processedMasks.getVis() != null && processedMasks.getFluo() != null) {
				int background = options.getBackground();
				boolean show = false;
				// visible search most high Y
				TopBottomLeftRight extremePoints = new ImageOperation(processedMasks.getVis().print("Mask Search For Maxima", show)).getExtremePoints(background);
				// cut fluo from top
				if (extremePoints != null) {
					int h = processedMasks.getFluo().getHeight();
					int temp = (int) ((extremePoints.getTopY() / (double) processedMasks.getVis().getHeight()) * processedMasks.getFluo().getHeight());
					FlexibleImage fi = new ImageOperation(processedMasks.getFluo()).clearImageAbove(temp - 0.03 * h, background).getImage();
					processedMasks.setFluo(fi.print("Fluo Result", show));
				}
			}
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() != null) {
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				return clearBamboo(getInput().getMasks().getVis());
			}
			return getInput().getMasks().getVis();
		}
		return null;
	}
	
	private FlexibleImage clearBamboo(FlexibleImage mask) {
		int widthQuarter = mask.getWidth() / 4;
		int width = mask.getWidth();
		int height = mask.getHeight();
		int background = options.getBackground();
		
		int pixelsInCluster = 0;
		int numberOfClusterPerLine = 0;
		boolean maize = true;
		FlexibleImage yellow = new ImageOperation(labFilter(mask, mask,
				150, 255, 108, 165, 127, 255, options.getCameraPosition(), maize)).opening(1, 1).getImage();
		
		int[] yellowarr = yellow.getAs1A();
		int[] origarr = mask.getAs1A();
		
		int clusterSize = 9;
		int y, lastX = -1, n = 0;
		mainLoop: for (y = 0; y < height; y++) {
			for (int x = widthQuarter; x < widthQuarter * 3; x++) {
				int ya = yellowarr[y * width + x];
				if (ya != background) {
					pixelsInCluster++;
				} else
					if (pixelsInCluster > clusterSize) {
						pixelsInCluster = 0;
						numberOfClusterPerLine++;
					}
			}
			if (numberOfClusterPerLine <= 1) {
				numberOfClusterPerLine = 0;
				int lx = clearLine(width, origarr, yellowarr, y, background, clusterSize);
				if (lx > 0) {
					lastX = lx;
					n++;
				}
			} else {
				break mainLoop;
			}
		}
		if (lastX > 0 && n > 10)
			return new FlexibleImage(width, height, origarr).getIO().getCanvas().fillRect(lastX - 8, y - 16, 16, 32, new Color(0, 0, 254).getRGB(), 0).getImage();
		else
			return new FlexibleImage(width, height, origarr);
	}
	
	private int clearLine(int w, int[] orig, int[] yellow, int y, int background, int clusterSize) {
		int count = 0;
		int yw = y * w;
		int sumX = 0, n = 0;
		for (int x = w / 4; x < w * 3d / 4d; x++) {
			if (yellow[x + yw] != background)
				count++;
			else
				if (count > clusterSize) {
					clearPixel(count, x, yw, orig, background);
					sumX += x;
					n++;
					count = 0;
				} else {
					count = 0;
				}
		}
		if (n > 0)
			return sumX / n;
		else
			return -1;
	}
	
	public void clearPixel(int count, int startX, int yw, int[] orig, int background) {
		for (int x = startX - count; x < startX; x++) {
			orig[x + yw] = background;
		}
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB, CameraPosition typ,
			boolean maize) {
		
		int[] image = workMask.getAs1A();
		int[] result = new int[image.length];
		int width = workMask.getWidth();
		int height = workMask.getHeight();
		
		int back = options.getBackground();
		
		ImageOperation.thresholdLAB(width, height, image, result,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back, typ, maize);
		
		FlexibleImage mask = new FlexibleImage(width, height, result);
		
		return new ImageOperation(originalImage).applyMask_ResizeSourceIfNeeded(mask, options.getBackground()).getImage();
	}
}
