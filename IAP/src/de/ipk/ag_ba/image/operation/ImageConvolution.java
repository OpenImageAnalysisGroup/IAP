package de.ipk.ag_ba.image.operation;

import java.util.LinkedList;

import de.ipk.ag_ba.image.structures.Image;

/**
 * This class will be include all image-convolution like operations.
 * 
 * @author pape
 */
public class ImageConvolution {
	
	public static final int BACKGROUND_COLORint = ImageOperation.BACKGROUND_COLOR.getRGB();
	
	private final Image image;
	
	public ImageConvolution(Image image) {
		this.image = image;
	}
	
	public ImageConvolution(ImageOperation io) {
		this.image = io.getImage();
	}
	
	public Image getImage() {
		return image;
	}
	
	/**
	 * Enlarge 1 px lines of foreground objects.
	 */
	public ImageConvolution enlargeLines() {
		int w = image.getWidth();
		int h = image.getHeight();
		int[][] img2d = image.getAs2A();
		
		int[][] mask1 = { { 0, 0, 0 }, { 1, 1, 1 }, { 0, 0, 0 } };
		int[][] mask2 = { { 0, 1, 0 }, { 0, 1, 0 }, { 0, 1, 0 } };
		int[][] mask3 = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
		int[][] mask4 = { { 0, 0, 1 }, { 0, 1, 0 }, { 1, 0, 0 } };
		int[][] mask5 = { { 1, 0, 0 }, { 1, 1, 1 }, { 0, 0, 1 } };
		int[][] mask6 = { { 0, 0, 1 }, { 1, 1, 1 }, { 1, 0, 0 } };
		int[][] mask7 = { { 1, 1, 0 }, { 0, 1, 0 }, { 0, 1, 1 } };
		int[][] mask8 = { { 0, 1, 1 }, { 0, 1, 0 }, { 1, 1, 0 } };
		
		LinkedList<int[][]> masks = new LinkedList<>();
		masks.add(mask1);
		masks.add(mask2);
		masks.add(mask3);
		masks.add(mask4);
		masks.add(mask5);
		masks.add(mask6);
		masks.add(mask7);
		masks.add(mask8);
		
		return convolve(w, h, img2d, masks, FillType.FILLMASK8);
	}
	
	public ImageConvolution clearBorder() {
		int w = image.getWidth();
		int h = image.getHeight();
		int[][] img2d = image.getAs2A();
		
		int[][] mask1 = { { 0, 0, 1 }, { 0, 1, 1 }, { 0, 0, 0 } };
		int[][] mask2 = { { 0, 0, 0 }, { 0, 1, 1 }, { 0, 0, 1 } };
		int[][] mask3 = { { 1, 0, 0 }, { 1, 1, 0 }, { 0, 0, 0 } };
		int[][] mask4 = { { 0, 0, 0 }, { 1, 1, 0 }, { 1, 0, 0 } };
		int[][] mask5 = { { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 0 } };
		int[][] mask6 = { { 0, 1, 1 }, { 0, 1, 0 }, { 0, 0, 0 } };
		int[][] mask7 = { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 1, 1 } };
		int[][] mask8 = { { 0, 0, 0 }, { 0, 1, 0 }, { 1, 1, 0 } };
		
		LinkedList<int[][]> masks = new LinkedList<>();
		masks.add(mask1);
		masks.add(mask2);
		masks.add(mask3);
		masks.add(mask4);
		masks.add(mask5);
		masks.add(mask6);
		masks.add(mask7);
		masks.add(mask8);
		
		return convolve(w, h, img2d, masks, FillType.REMOVEPIX);
	}
	
	private ImageConvolution convolve(int w, int h, int[][] img2d, LinkedList<int[][]> masks, FillType ft) {
		boolean match[] = new boolean[masks.size()];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				
				if (x - 1 >= 0 && x + 1 < w && y - 1 >= 0 && y + 1 < h && img2d[x][y] != BACKGROUND_COLORint) {
					int[][] area = new int[][] { { img2d[x - 1][y - 1], img2d[x][y - 1], img2d[x + 1][y - 1] },
							{ img2d[x - 1][y], img2d[x][y], img2d[x + 1][y] },
							{ img2d[x - 1][y + 1], img2d[x][y + 1], img2d[x + 1][y + 1] } };
					
					int i = 0;
					for (int[][] m : masks) {
						match[i] = match(m, area);
						i++;
					}
					
					// fill mask in img
					boolean isMatch = false;
					for (boolean b : match) {
						if (b)
							isMatch = true;
					}
					if (isMatch) {
						if (ft == FillType.FILLMASK4)
							img2d = fill4N(img2d, x, y, false);
						if (ft == FillType.FILLMASK8)
							img2d = fill8N(img2d, x, y, false);
						if (ft == FillType.REMOVEMASK4)
							img2d = fill4N(img2d, x, y, true);
						if (ft == FillType.REMOVEMASK8)
							img2d = fill8N(img2d, x, y, true);
						if (ft == FillType.REMOVEPIX)
							img2d[x][y] = BACKGROUND_COLORint;
					}
				}
			}
		}
		return new ImageConvolution(new Image(img2d));
	}
	
	private boolean match(int[][] mask, int[][] area) {
		boolean match = true;
		
		for (int x = 0; x <= 2; x++) {
			for (int y = 0; y <= 2; y++) {
				int temp = area[x][y] != BACKGROUND_COLORint ? 1 : 0;
				if (mask[x][y] != temp)
					return false;
			}
		}
		return match;
	}
	
	private int[][] fill8N(int[][] img2d, int x, int y, boolean background) {
		int color;
		if (!background)
			color = img2d[x][y];
		else
			color = BACKGROUND_COLORint;
		
		img2d[x - 1][y - 1] = color;
		img2d[x][y - 1] = color;
		img2d[x + 1][y - 1] = color;
		img2d[x - 1][y] = color;
		img2d[x + 1][y] = color;
		img2d[x - 1][y + 1] = color;
		img2d[x][y + 1] = color;
		img2d[x + 1][y + 1] = color;
		
		return img2d;
	}
	
	private int[][] fill4N(int[][] img2d, int x, int y, boolean background) {
		int color;
		if (!background)
			color = img2d[x][y];
		else
			color = BACKGROUND_COLORint;
		
		color = img2d[x][y];
		img2d[x][y - 1] = color;
		img2d[x - 1][y] = color;
		img2d[x + 1][y] = color;
		img2d[x][y + 1] = color;
		
		return img2d;
	}
	
	public enum FillType {
		FILLMASK4, FILLMASK8, REMOVEMASK4, REMOVEMASK8, REMOVEPIX
	}
}
