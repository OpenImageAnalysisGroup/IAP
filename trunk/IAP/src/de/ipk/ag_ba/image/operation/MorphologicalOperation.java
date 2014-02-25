package de.ipk.ag_ba.image.operation;

import java.awt.Color;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Morphological operations supporting round mask (returns a mask image, it is recommend to apply it on the original image).
 * 
 * @author pape
 */
public class MorphologicalOperation {
	
	private final Image image;
	
	public MorphologicalOperation(Image image) {
		this.image = image;
	}
	
	public MorphologicalOperation(ImageOperation io) {
		this.image = io.getImage();
	}
	
	public Image getImage() {
		return image;
	}
	
	public MorphologicalOperation closing(int maskSize) {
		return this.erode_or_dilate(maskSize, true).erode_or_dilate(maskSize, false);
	}
	
	public MorphologicalOperation opening(int maskSize) {
		return this.erode_or_dilate(maskSize, false).erode_or_dilate(maskSize, true);
	}
	
	public MorphologicalOperation erode_or_dilate(int maskSize, boolean erode) {
		int background = ImageOperation.BACKGROUND_COLORint;
		return erode_or_dilate(maskSize, erode, background);
	}
	
	public MorphologicalOperation erode_or_dilate(int maskSize, boolean erode, int background) {
		int[][] roundmask = getRoundMask(maskSize);
		
		int[][] img2d = image.getAs2A();
		int[][] res = new int[image.getWidth()][image.getHeight()];
		
		int w = image.getWidth();
		int h = image.getHeight();
		int halfmask = roundmask.length / 2;
		
		boolean isfilled = erode;
		
		int foreground = Color.RED.getRGB();
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				
				// int foreground = img2d[x][y];
				
				maskloop: for (int xMask = -halfmask; xMask < halfmask; xMask++) {
					for (int yMask = -halfmask; yMask < halfmask; yMask++) {
						
						if (x + xMask >= 0 && x + xMask < w && y + yMask >= 0 && y + yMask < h) {
							if (roundmask[xMask + halfmask][yMask + halfmask] != background) {
								if (erode) {
									if (img2d[x + xMask][y + yMask] == background) {
										isfilled = false;
										break maskloop;
									}
								} else {
									if (img2d[x + xMask][y + yMask] != background) {
										isfilled = true;
										break maskloop;
									}
								}
								
							}
						}
					}
				}
				if (isfilled)
					res[x][y] = foreground;
				else
					res[x][y] = ImageOperation.BACKGROUND_COLORint;
				
				isfilled = erode;
			}
		}
		return new MorphologicalOperation(new Image(res));
	}
	
	private int[][] getRoundMask(int size) {
		int[][] kernel = new int[size][size];
		if (size == 0)
			return kernel;
		double m = size / 2d;
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++) {
				double cX = x - m + 0.5;
				double cY = y - m + 0.5;
				double d = Math.sqrt(cX * cX + cY * cY);
				boolean inside = d <= m - 0.25;
				int insideCircle = inside ? 1 : 0xffffffff;
				kernel[x][y] = insideCircle;
			}
		return kernel;
	}
}
