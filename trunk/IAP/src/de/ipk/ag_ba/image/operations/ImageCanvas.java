package de.ipk.ag_ba.image.operations;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class ImageCanvas {
	
	private FlexibleImage image;
	
	public ImageCanvas(FlexibleImage image) {
		this.image = image;
	}
	
	public ImageCanvas fillRect(int x, int y, int w, int h, int color) {
		int wi = image.getWidth();
		int hi = image.getHeight();
		int[] img = image.getAs1A();
		for (int xi = x; xi <= x + w; xi++)
			for (int yi = y; yi <= y + h; yi++) {
				int i = xi + yi * wi;
				if (i >= 0 && i < img.length)
					img[i] = color;
			}
		image = new FlexibleImage(wi, hi, img);
		return this;
	}
	
	public FlexibleImage getImage() {
		return image;
	}
	
}
