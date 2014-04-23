package de.ipk.ag_ba.image.operation;

import org.Vector2d;

/**
 * @author Christian Klukas
 */
public class ImageCalculation {
	
	private final ImageOperation imageOperation;
	
	public ImageCalculation(ImageOperation imageOperation) {
		this.imageOperation = imageOperation;
	}
	
	/**
	 * @return center of gravity, or NULL, if no foreground pixels can be found.
	 */
	public Vector2d getCOG() {
		double sumX = 0;
		double sumY = 0;
		int n = 0;
		int x = 0;
		int y = 0;
		int w = imageOperation.getWidth();
		for (int p : imageOperation.getImageAs1dArray()) {
			x++;
			if (x == w) {
				y++;
				x = 0;
			}
			if (p != ImageOperation.BACKGROUND_COLORint) {
				sumX += x;
				sumY += y;
				n++;
			}
		}
		if (n > 0)
			return new Vector2d(sumX / n, sumY / n);
		else
			return null;
	}
	
}
