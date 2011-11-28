package de.ipk.ag_ba.image.operations;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class ImageCanvas {
	
	private FlexibleImage image;
	private Graphics graphics;
	
	public ImageCanvas(FlexibleImage image) {
		this.image = image;
	}
	
	public ImageCanvas drawLine(int x0, int y0, int x1, int y1, int color, double alpha, int size) {
		int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
		int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
		int err = dx + dy, e2; /* error value e_xy */
		
		while (true) { /* loop */
			fillRect(x0 - size, y0 - size, size + size, size + size, color, alpha);
			if (x0 == x1 && y0 == y1)
				break;
			e2 = 2 * err;
			if (e2 >= dy) {
				err += dy;
				x0 += sx;
			} /* e_xy+e_x > 0 */
			if (e2 <= dx) {
				err += dx;
				y0 += sy;
			} /* e_xy+e_y < 0 */
		}
		return this;
	}
	
	/**
	 * @param alpha
	 *           - opacity of the filled rectangle, Wikipedia: Alpha_Blending C = αAA + (1 − αA)B
	 * @return
	 */
	public ImageCanvas fillRect(int x, int y, int w, int h, int color, double alpha) {
		int wi = image.getWidth();
		int hi = image.getHeight();
		int[] img = image.getAs1A();
		int r, g, b, c, red, green, blue;
		int colorRedRect = (color & 0xff0000) >> 16;
		int colorGreenRect = (color & 0x00ff00) >> 8;
		int colorBlueRect = (color & 0x0000ff);
		
		for (int xi = x; xi <= x + w; xi++)
			for (int yi = y; yi <= y + h; yi++) {
				int i = xi + yi * wi;
				if (i >= 0 && i < img.length) {
					c = img[i];
					r = (c & 0xff0000) >> 16;
					g = (c & 0x00ff00) >> 8;
					b = (c & 0x0000ff);
					
					red = (int) (alpha * r + (1 - alpha) * colorRedRect);
					green = (int) (alpha * g + (1 - alpha) * colorGreenRect);
					blue = (int) (alpha * b + (1 - alpha) * colorBlueRect);
					
					img[i] = (0xFF << 24 | (red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 0);
				}
			}
		image = new FlexibleImage(wi, hi, img);
		return this;
	}
	
	/**
	 * Draws the border of a Circle.(based on Bresenham-Algorithm, see Wikipedia)
	 * 
	 * @param mx
	 * @param my
	 * @param radius
	 * @param color
	 * @param alpha
	 *           - ! opacity, function corrupt
	 * @param s
	 *           - Stroke width
	 * @return
	 */
	public ImageCanvas drawCircle(int mx, int my, int radius, int color, double alpha, int s) {
		
		int f = 1 - radius;
		int ddF_x = 0;
		int ddF_y = -2 * radius;
		int x = 0;
		int y = radius;
		
		fillRect(mx - s, my + radius - s, s + s, s + s, color, alpha);
		fillRect(mx - s, my - radius - s, s + s, s + s, color, alpha);
		fillRect(mx + radius - s, my - s, s + s, s + s, color, alpha);
		fillRect(mx - radius - s, my - s, s + s, s + s, color, alpha);
		while (x < y) {
			if (f >= 0) {
				y--;
				ddF_y += 2;
				f += ddF_y;
			}
			x++;
			ddF_x += 2;
			f += ddF_x + 1;
			
			fillRect(mx + x - s, my + y - s, s + s, s + s, color, alpha);
			fillRect(mx - x - s, my + y - s, s + s, s + s, color, alpha);
			fillRect(mx + x - s, my - y - s, s + s, s + s, color, alpha);
			fillRect(mx - x - s, my - y - s, s + s, s + s, color, alpha);
			fillRect(mx + y - s, my + x - s, s + s, s + s, color, alpha);
			fillRect(mx - y - s, my + x - s, s + s, s + s, color, alpha);
			fillRect(mx + y - s, my - x - s, s + s, s + s, color, alpha);
			fillRect(mx - y - s, my - x - s, s + s, s + s, color, alpha);
		}
		return this;
	}
	
	/**
	 * Draws a filled Circle.
	 * 
	 * @param mx
	 * @param my
	 * @param radius
	 * @param color
	 * @param alpha
	 * @return
	 */
	public ImageCanvas fillCircle(int mx, int my, int radius, int color, double alpha) {
		int x = 0;
		int y = radius;
		int d = radius - 1;
		while (y >= x) {
			fillRect(mx - x, my - y, 2 * x, 1, color, alpha);
			fillRect(mx - y, my - x, 2 * y, 1, color, alpha);
			fillRect(mx - x, my + y, 2 * x, 1, color, alpha);
			fillRect(mx - y, my + x, 2 * y, 1, color, alpha);
			if (y == x)
				break;
			if (d < 0) {
				d += 2 * x + 3;
			} else {
				y -= 1;
				d += 2 * x - 2 * y + 5;
			}
			x += 1;
		}
		return this;
	}
	
	public FlexibleImage getImage() {
		return image;
	}
	
	public ImageCanvas fillRectOutside(Rectangle rectangle, int backgroundColorint) {
		int w = image.getWidth();
		int h = image.getHeight();
		ImageCanvas res = this;
		
		int xL = rectangle.x - 1;
		if (xL > 0)
			res = fillRect(0, 0, xL, h, backgroundColorint, 1);
		
		int yT = rectangle.y - 1;
		if (yT > 0)
			res = fillRect(0, 0, w, yT, backgroundColorint, 1);
		
		int xR = rectangle.x + rectangle.width + 1;
		if (xR < w && xR > 0)
			res = fillRect(xR, 0, w - xR, h, backgroundColorint, 1);
		
		int yB = rectangle.y + rectangle.height + 1;
		if (yB < h && yB > 0)
			res = fillRect(0, yB, w, h - yB, backgroundColorint, 1);
		
		return res;
	}
	
	private BufferedImage buf = null;
	
	public Graphics getGraphics() {
		if (buf == null)
			buf = image.getAsBufferedImage();
		return buf.getGraphics();
	}
	
	public void updateFromGraphics() {
		image = new FlexibleImage(buf);
	}
}
