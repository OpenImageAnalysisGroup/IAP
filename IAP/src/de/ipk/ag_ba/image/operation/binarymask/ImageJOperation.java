package de.ipk.ag_ba.image.operation.binarymask;

import iap.blocks.segmentation.BlMorphologicalOperations;
import ij.ImagePlus;
import ij.process.BinaryProcessor;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class ImageJOperation {
	
	protected final ImagePlus image;
	
	private final int w;
	private final int h;
	
	public ImageJOperation(int[] image, int w, int h) {
		int white = new Color(255, 255, 255).getRGB();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int off = x + y * w;
				int color = image[off];
				if (color != ImageOperation.BACKGROUND_COLORint) {
					image[x + w * y] = 0;
				} else {
					image[x + w * y] = white;
				}
			}
		}
		
		this.image = new ImagePlus("from 1d array", new ColorProcessor(w, h, image));
		this.w = w;
		this.h = h;
	}
	
	public ImageOperation io() {
		return new ImageOperation(image).replaceColor(-1, ImageOperation.BACKGROUND_COLORint);
	}
	
	public Image getImage() {
		return io().getImage();
	}
	
	/**
	 * Hint: Works only on binary images (use getBinaryMask and apply this to the color image after processing).
	 * Reduce area of mask.
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 */
	public ImageJOperation erode() {
		image.getProcessor().erode();
		return this;
	}
	
	public ImageJOperation erode(int size) {
		return erode(BlMorphologicalOperations.getRoundMask(size));
	}
	
	public ImageJOperation opening(int[][] mask1, int[][] mask2) {
		erode(mask1);
		dilate(mask2);
		return this;
	}
	
	public ImageJOperation opening(int[][] mask, int n) {
		for (int i = 0; i < n; i++)
			erode(mask);
		for (int i = 0; i < n; i++)
			dilate(mask);
		return this;
	}
	
	public ImageJOperation opening(int[][] mask, int n1, int n2) {
		for (int i = 0; i < n1; i++)
			erode(mask);
		for (int i = 0; i < n2; i++)
			dilate(mask);
		return this;
	}
	
	/**
	 * Erosion, then dilation. Removes small objects in the mask. es wird der
	 * 3x3 Minimum-Filter genutzt
	 * <p>
	 * The erosion of the dark-blue square by a disk, resulting in the light-blue square:<br>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/c/c1/Opening.png/220px-Opening.png" >
	 */
	public void opening() {
		image.getProcessor().erode();
		image.getProcessor().dilate();
	}
	
	/**
	 * Erosion, then dilation. Removes small objects in the mask. es wird der
	 * 3x3 Minimum-Filter genutzt
	 * <p>
	 * The erosion of the dark-blue square by a disk, resulting in the light-blue square:<br>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/c/c1/Opening.png/220px-Opening.png" >
	 */
	public ImageJOperation opening(int size) {
		return erode(size).dilate(size);
	}
	
	public ImageJOperation opening(int n1, int n2) {
		return opening(BlMorphologicalOperations.getRoundMask(n1), BlMorphologicalOperations.getRoundMask(n2));
	}
	
	/**
	 * Reduce area of mask.
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 * 
	 * @return
	 */
	public ImageJOperation erode(int[][] mask) {
		if (mask.length == 0 || mask[0].length == 0)
			return this;
		int jM = (mask.length - 1) / 2;
		int iM = (mask[0].length - 1) / 2;
		
		ImageProcessor tempImage = image.getProcessor().createProcessor(
				image.getProcessor().getWidth(),
				image.getProcessor().getHeight());
		ImageProcessor p = image.getProcessor();
		for (int j = 0; j < mask.length; j++)
			for (int i = 0; i < mask[j].length; i++)
				if (mask[j][i] != -1)
					tempImage.copyBits(p, i - iM, j - jM, Blitter.MAX);
		
		image.getProcessor().copyBits(tempImage, 0, 0, Blitter.COPY);
		return this;
	}
	
	/**
	 * Enlarge area of mask.
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/8/8d/Dilation.png/220px-Dilation.png" >
	 */
	public ImageJOperation dilate() {
		image.getProcessor().dilate();
		return this;
	}
	
	public ImageJOperation dilate(int size) {
		return dilate(BlMorphologicalOperations.getRoundMask(size));
	}
	
	/**
	 * Hint: Works only on binary images (use getBinaryMask and apply this to the color image after processing).
	 * Enlarge area of mask.
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/8/8d/Dilation.png/220px-Dilation.png" >
	 */
	public ImageJOperation dilate(int[][] mask) {
		image.getProcessor().invert();
		erode(mask);
		image.getProcessor().invert();
		return this;
	}
	
	/**
	 * Dilation, then erosion. Removes small holes in the image.
	 * <p>
	 * The closing of the dark-blue shape (union of two squares) by a disk, resulting in the union of the dark-blue shape and the light-blue areas.:<br>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/2/2e/Closing.png/220px-Closing.png" >
	 */
	public ImageJOperation closing(int[][] mask) {
		return dilate(mask).erode(mask);
	}
	
	public ImageJOperation opening(int[][] mask) {
		return erode(mask).dilate(mask);
	}
	
	public int countFilledPixels() {
		int res = 0;
		int background = -1;
		int[] img1d = getAs1A();
		
		for (int c : img1d) {
			if (c != background)
				res++;
		}
		return res;
	}
	
	public int[] getAs1A() {
		return (int[]) image.getProcessor().getPixels();
	}
	
	public ImageJOperation skeletonize() {
		ImageProcessor processor2 = image.getProcessor().convertToByte(false);
		ByteProcessor byteProcessor = new BinaryProcessor(
				(ByteProcessor) processor2);
		byteProcessor.skeletonize();
		image.setProcessor(byteProcessor.convertToRGB());
		return this;
	}
	
	public ImageJOperation medianFilter() {
		image.getProcessor().medianFilter();
		return this;
	}
}
