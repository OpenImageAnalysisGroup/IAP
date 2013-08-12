package de.ipk.ag_ba.image.operation;

/**
 * DoublePixel: distance to "something" (center of image, or nearest 4 pot center), x,y of pixel, color of pixel
 * 
 * @author klukas
 */
public class DoublePixel {
	
	public final Double distanceToCenter;
	public final int x;
	public final int y;
	public final int c;
	
	public DoublePixel(Double distanceToCenter, int x, int y, int c) {
		this.distanceToCenter = distanceToCenter;
		this.x = x;
		this.y = y;
		this.c = c;
	}
	
}
