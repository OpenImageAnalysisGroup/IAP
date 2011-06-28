/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

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
public class BlockRemoveBambooStick extends AbstractSnapshotAnalysisBlockFIS {
	
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
		
		FlexibleImage yellow = new ImageOperation(labFilter(mask, mask, 150, 255, 108, 165, 127, 255, options.getCameraPosition())).opening(1, 1).getImage();
		
		int[][] yellowarr = yellow.getAs2A();
		int[][] origarr = mask.getAs2A();
		
		int clusterSize = 9;
		
		mainLoop: for (int y = 0; y < height; y++) {
			for (int x = widthQuarter; x < widthQuarter * 3; x++) {
				if (yellowarr[x][y] != background) {
					pixelsInCluster++;
				}
				if (yellowarr[x][y] == background && pixelsInCluster > clusterSize) {
					pixelsInCluster = 0;
					numberOfClusterPerLine++;
				}
			}
			if (numberOfClusterPerLine <= 1) {
				numberOfClusterPerLine = 0;
				clearLine(width, origarr, yellowarr, y, background, clusterSize);
			} else {
				break mainLoop;
			}
		}
		return new FlexibleImage(origarr);
	}
	
	private void clearLine(int w, int[][] orig, int[][] yellow, int y, int background, int clusterSize) {
		int count = 0;
		for (int x = w / 4; x < w * 3d / 4d; x++) {
			if (yellow[x][y] != background)
				count++;
			else
				if (count > clusterSize) {
					clearPixel(count, x, y, orig, background);
					count = 0;
				} else {
					count = 0;
				}
		}
	}
	
	public void clearPixel(int count, int startX, int y, int[][] orig, int background) {
		for (int x = startX - count; x < startX; x++) {
			orig[x][y] = background;
		}
		
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB, CameraPosition typ) {
		
		int[][] image = workMask.getAs2A();
		int[][] result = new int[workMask.getWidth()][workMask.getHeight()];
		int width = workMask.getWidth();
		int height = workMask.getHeight();
		
		int back = options.getBackground();
		
		ImageOperation.doThresholdLAB(width, height, image, result,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back, typ);
		
		FlexibleImage mask = new FlexibleImage(result);
		
		return new ImageOperation(originalImage).applyMask_ResizeSourceIfNeeded(mask, options.getBackground()).getImage();
	}
}
