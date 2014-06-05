package de.ipk.ag_ba.image.operation;

import java.awt.Color;
import java.util.Stack;

import org.Vector2i;

import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.Image;

public class BorderImageOperation {
	
	private final Image image;
	private ResultsTableWithUnits rt;
	
	public BorderImageOperation(ImageOperation imageOperation) {
		image = imageOperation.getImage();
		rt = imageOperation.getResultsTable();
	}
	
	/**
	 * Locates pixels at the border from background color to any other color. The border is detected by looking
	 * at the 4-neighborhood. The border is recolored according to the given parameter. Any other pixels are
	 * recolored to background.
	 * In case the removeInnerBorders parameter is specified (set to true), a kind of flood fill starting from the borders
	 * of the image is performed. This flood fill checks for any detected borders and retains the detected borders.
	 * Borders not reachable from within the outside will be removed.
	 * The number of border pixels is added to the ResultTable of the result (column 'border').
	 * In case a ImageOperation result table is available in this object, it is extended and transfered to the result object.
	 * 
	 * @param backgroundColor
	 *           The color which is used to identify background pixels.
	 * @param borderColor
	 *           The new color for border colors (other pixels will be set to background).
	 *           If the new color is set to Integer.MAX_VALUE, the border color
	 *           will be based on the input image.
	 * @param removeInnerBorders
	 *           If true, using a special algorithm (see above) only outside borders will be returned,
	 *           borders not reachable from the image borders will be removed from the result.
	 * @return A new image(operation object) with highlighted borders.
	 * @author klukas
	 */
	public ImageOperation borderDetection(int backgroundColor, int borderColor, boolean removeInnerBorders) {
		int[] in = new ImageOperation(image, rt).getAs1D();
		int w = image.getWidth();
		int h = image.getHeight();
		int[] out = new int[w * h];
		boolean eightNeighborhood = false;
		
		int border = borderDetection(backgroundColor, borderColor, removeInnerBorders, eightNeighborhood, in, w, h, out);
		
		ImageOperation res = new ImageOperation(new Image(w, h, out));
		
		if (rt == null)
			rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		rt.addValue("border", border);
		res.setResultsTable(rt);
		
		return res;
	}
	
	/**
	 * Locates pixels at the border from background color to any other color. The border is detected by looking
	 * at the 4-neighbourhood. The border is recolored according to the given parameter. Any other pixels are
	 * recolored to background.
	 * In case the removeInnerBorders parameter is specified (set to true), a kind of flood fill starting from the borders
	 * of the image is performed. This flood fill checks for any detected borders and retains the detected borders.
	 * Borders not reachable from within the outside will be removed.
	 * The number of border pixels is added to the ResultTable of the result (column 'border').
	 * In case a ImageOperation resulttable is available in this object, it is extended and transfered to the result object.
	 * 
	 * @param backgroundColor
	 *           The color which is used to identify background pixels.
	 * @param borderColor
	 *           The new color for border colors (other pixels will be set to background).
	 * @param removeInnerBorders
	 *           If true, using a special algorithm (see above) only outside borders will be returned,
	 *           borders not reachable from the image borders will be removed from the result.
	 * @param in
	 *           Input image
	 * @param w
	 *           Image width
	 * @param h
	 *           Image height
	 * @param out
	 *           Result image (needs to be initialized for call)
	 * @return Number of border pixels.
	 * @author klukas
	 */
	public int borderDetection(int backgroundColor, int borderColor, boolean removeInnerBorders, boolean eightNeighborhood,
			int[] in, int w, int h, int[] out) {
		int bc = removeInnerBorders ? bc = Color.CYAN.getRGB() : borderColor;
		int[] tempOut = removeInnerBorders ? new int[w * h] : out;
		int res = 0;
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				int w_y = w * y;
				tempOut[x + w_y] = backgroundColor;
				if (in[x + w_y] != backgroundColor) {
					// pixels at the border of the image are ignored
					if (x < w - 1 && y < h - 1 && x > 0 && y > 0) {
						int above = in[x + w * (y - 1)];
						int left = in[x - 1 + w_y];
						int right = in[x + 1 + w_y];
						int below = in[x + w * (y + 1)];
						
						if (eightNeighborhood) {
							int aboveLeft = in[x - 1 + w * (y - 1)];
							int aboveRight = in[x + 1 + w * (y - 1)];
							int belowLeft = in[x - 1 + w * (y + 1)];
							int belowRight = in[x + 1 + w * (y + 1)];
							
							if (above == backgroundColor || left == backgroundColor || right == backgroundColor || below == backgroundColor
									|| aboveLeft == backgroundColor || aboveRight == backgroundColor || belowLeft == backgroundColor || belowRight == backgroundColor) {
								tempOut[x + w_y] = borderColor != Integer.MAX_VALUE ? bc : in[x + w_y];
								res++;
							}
						} else
							if (above == backgroundColor || left == backgroundColor || right == backgroundColor || below == backgroundColor) {
								tempOut[x + w_y] = borderColor != Integer.MAX_VALUE ? bc : in[x + w_y];
								res++;
							}
					}
				}
			}
		
		if (removeInnerBorders) {
			if (borderColor == Integer.MAX_VALUE)
				throw new UnsupportedOperationException("This combination of removeInnerBorders and border coloring mode not supported");
			boolean tr = true;
			if (tr)
				throw new UnsupportedOperationException("ToDo");
			res = 0;
			int fillColor = Color.YELLOW.getRGB();
			for (int x = 0; x < w; x++) {
				floodFill(tempOut, w, h, backgroundColor, fillColor, x, 0);
				floodFill(tempOut, w, h, backgroundColor, fillColor, x, h - 1);
			}
			for (int y = 0; y < h; y++) {
				floodFill(tempOut, w, h, backgroundColor, fillColor, 0, y);
				floodFill(tempOut, w, h, backgroundColor, fillColor, w - 1, y);
			}
			// highlight outside borders, anything else (inner borders) will cleaned
			res = borderDetection(fillColor, borderColor, false, false, tempOut, w, h, out);
		}
		return res;
	}
	
	/**
	 * @return number of filled pixels
	 * @author klukas
	 */
	private int floodFill(int[] image, int w, int h, int background, int fill, Integer startX, Integer startY) {
		if (background == fill)
			throw new UnsupportedOperationException("Fill-color needs to differ from background-color.");
		int res = 0;
		if (image[startX + w * startY] == background) {
			Stack<Vector2i> toDo = new Stack<Vector2i>();
			toDo.add(new Vector2i(startX, startY));
			int x, y;
			while (!toDo.isEmpty()) {
				Vector2i p = toDo.pop();
				x = p.x;
				y = p.y;
				image[x + w * y] = fill;
				res++;
				
				// check right
				if (x < w - 1 && image[x + 1 + w * y] == background) {
					toDo.push(new Vector2i(x + 1, y));
				}
				// check above
				if (y > 0 && image[x + w * (y - 1)] == background) {
					toDo.add(new Vector2i(x, y - 1));
				}
				// check left
				if (x > 0 && image[x - 1 + w * y] == background) {
					toDo.push(new Vector2i(x - 1, y));
				}
				// check below
				if (y < h - 1 && image[x + w * (y + 1)] == background) {
					toDo.push(new Vector2i(x, y + 1));
				}
			}
		}
		return res;
	}
	
	/**
	 * @return number of filled pixels
	 * @author klukas
	 */
	private int floodFillToBorder(int[] image, int w, int h, int border, Integer startX, Integer startY) {
		int res = 0;
		if (image[startX + w * startY] != border) {
			Stack<Vector2i> toDo = new Stack<Vector2i>();
			toDo.add(new Vector2i(startX, startY));
			int x, y;
			while (!toDo.isEmpty()) {
				Vector2i p = toDo.pop();
				x = p.x;
				y = p.y;
				image[x + w * y] = border;
				res++;
				
				// check right
				if (x < w - 1 && image[x + 1 + w * y] != border) {
					toDo.push(new Vector2i(x + 1, y));
				}
				// check above
				if (y > 0 && image[x + w * (y - 1)] != border) {
					toDo.add(new Vector2i(x, y - 1));
				}
				// check left
				if (x > 0 && image[x - 1 + w * y] != border) {
					toDo.push(new Vector2i(x - 1, y));
				}
				// check below
				if (y < h - 1 && image[x + w * (y + 1)] != border) {
					toDo.push(new Vector2i(x, y + 1));
				}
			}
		}
		return res;
	}
	
	/**
	 * Flood fill starting from the image borders (top, left, right, bottom).
	 * The number of pixels is added to the ResultTable of the result (column 'filled').
	 * In case a ImageOperation resulttable is available in this object, it is extended and transfered to the result object.
	 * 
	 * @param background
	 *           Background-color.
	 * @param fill
	 *           Fill-color.
	 * @return New image, outside background (pixels equal to background-color) is filled with the fill-color.
	 * @author klukas
	 */
	public ImageOperation floodFillFromOutside(int background, int fill) {
		
		int[] out = new ImageOperation(image, rt).getAs1D();
		int w = image.getWidth();
		int h = image.getHeight();
		int filled = 0;
		// StopWatch sw = new StopWatch("Flood-fill");
		for (int x = 0; x < w; x++) {
			filled += floodFill(out, w, h, background, fill, x, 0);
			filled += floodFill(out, w, h, background, fill, x, h - 1);
		}
		for (int y = 0; y < h; y++) {
			filled += floodFill(out, w, h, background, fill, 0, y);
			filled += floodFill(out, w, h, background, fill, w - 1, y);
		}
		// sw.printTime(0);
		ImageOperation res = new ImageOperation(new Image(w, h, out));
		if (rt == null)
			rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		rt.addValue("filled", filled);
		res.setResultsTable(rt);
		return res;
	}
	
	/**
	 * Flood fill starting from the image borders (top, left, right, bottom).
	 * The number of pixels is added to the ResultTable of the result (column 'filled').
	 * In case a ImageOperation resulttable is available in this object, it is extended and transfered to the result object.
	 * 
	 * @param background
	 *           Background-color.
	 * @param fill
	 *           Fill-color.
	 * @return New image, outside background (pixels equal to background-color) is filled with the fill-color.
	 * @author klukas
	 */
	public ImageOperation floodFillFromOutsideToBorder(int border) {
		
		int[] out = new ImageOperation(image, rt).getAs1D();
		int w = image.getWidth();
		int h = image.getHeight();
		int filled = 0;
		// StopWatch sw = new StopWatch("Flood-fill");
		for (int x = 0; x < w; x++) {
			filled += floodFillToBorder(out, w, h, border, x, 0);
			filled += floodFillToBorder(out, w, h, border, x, h - 1);
		}
		for (int y = 0; y < h; y++) {
			filled += floodFillToBorder(out, w, h, border, 0, y);
			filled += floodFillToBorder(out, w, h, border, w - 1, y);
		}
		// sw.printTime(0);
		ImageOperation res = new ImageOperation(new Image(w, h, out));
		if (rt == null)
			rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		rt.addValue("filled", filled);
		res.setResultsTable(rt);
		return res;
	}
	
}
