package de.ipk.ag_ba.image.operation;

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
	 * Fills 1 px lines.
	 */
	public ImageConvolution fillLines() {
		int w = image.getWidth();
		int h = image.getHeight();
		int[][] img2d = image.getAs2A();
		
		int[][] mask1 = { { 0, 0, 0 }, { 1, 1, 1 }, { 0, 0, 0 } };
		int[][] mask2 = { { 0, 1, 0 }, { 0, 1, 0 }, { 0, 1, 0 } };
		int[][] mask3 = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
		int[][] mask4 = { { 0, 0, 1 }, { 0, 1, 0 }, { 1, 0, 0 } };
		
		boolean match1 = false, match2 = false, match3 = false, match4 = false;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				
				if (x - 1 >= 0 && x + 1 < w && y - 1 >= 0 && y + 1 < h && img2d[x][y] != BACKGROUND_COLORint) {
					int[][] area = new int[][] { { img2d[x - 1][y - 1], img2d[x][y - 1], img2d[x + 1][y - 1] },
							{ img2d[x - 1][y], img2d[x][y], img2d[x + 1][y] },
							{ img2d[x - 1][y + 1], img2d[x][y + 1], img2d[x + 1][y + 1] } };
					
					match1 = match(mask1, area);
					match2 = match(mask2, area);
					match3 = match(mask3, area);
					match4 = match(mask4, area);
					
					// fill mask in img
					if (match1 || match2 || match3 || match4)
						img2d = fill8N(img2d, x, y);
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
	
	private int[][] fill8N(int[][] img2d, int x, int y) {
		
		int color = img2d[x][y];
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
}
