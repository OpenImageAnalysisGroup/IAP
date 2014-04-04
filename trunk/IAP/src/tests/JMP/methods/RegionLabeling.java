package tests.JMP.methods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import org.Vector2i;

import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operations.segmentation.Segmentation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape
 */
public class RegionLabeling implements Segmentation {
	
	Image image;
	boolean isBorderImage;
	int borderColor;
	Image labeledImage;
	int[][] visitedImage;
	LinkedList<ArrayList<PositionAndColor>> regionList;
	private int numberOfClusters;
	
	public RegionLabeling(Image img, boolean isBorderImage, int borderColor) {
		this.image = img;
		this.isBorderImage = isBorderImage;
		this.borderColor = borderColor;
		this.visitedImage = img.getAs2A();
	}
	
	public Image getLabeledImage() {
		int[][] labeled = image.copy().getAs2A();
		int fac = 0;
		for (ArrayList<PositionAndColor> clu : regionList) {
			fac += 255 / numberOfClusters;
			int cluColor = new Color(fac, fac, fac).getRGB();
			for (PositionAndColor pix : clu) {
				labeled[pix.x][pix.y] = cluColor;
			}
		}
		
		return new Image(labeled);
	}
	
	public ArrayList<PositionAndColor> regionGrowing(int x, int y, int background, double radius, int geometricThresh, boolean debug)
			throws InterruptedException {
		radius = radius * radius;
		int w = image.getWidth();
		int h = image.getHeight();
		Stack<PositionAndColor> visited = new Stack<PositionAndColor>();
		ArrayList<PositionAndColor> resultRegion = new ArrayList<PositionAndColor>();
		int rx = x;
		int ry = y;
		PositionAndColor start = new PositionAndColor(x, y, visitedImage[x][y]);
		visited.push(start);
		resultRegion.add(start);
		boolean find;
		boolean inside = false;
		double dist = 0.0;
		Image show = null;
		boolean goBack = false;
		int numOfBackgroundPixels;
		if (debug) {
			show = new Image(visitedImage);
			show.show("debug");
		}
		
		// test if new pixel is in image space
		if (rx >= 0 && ry >= 0 && rx < w && ry < h)
			inside = true;
		
		visitedImage[rx][ry] = background;
		
		while (!visited.empty()) {
			// update process window for debug
			if (visitedImage[rx][ry] != background && debug) {
				show.update(new Image(visitedImage));
				Thread.sleep(100);
			}
			visitedImage[rx][ry] = background;
			
			find = false;
			numOfBackgroundPixels = 0;
			
			if (dist < radius) {
				inside = rx - 1 >= 0 && ry - 1 >= 0;
				if (inside)
					if (visitedImage[rx - 1][ry - 1] != background) {
						find = true;
						rx = rx - 1;
						ry = ry - 1;
					} else {
						numOfBackgroundPixels++;
					}
				
				inside = ry - 1 >= 0;
				if (!find && inside)
					if (visitedImage[rx][ry - 1] != background) {
						find = true;
						ry = ry - 1;
					} else {
						numOfBackgroundPixels++;
					}
				
				inside = ry - 1 >= 0 && rx + 1 < w;
				if (!find && inside)
					if (visitedImage[rx + 1][ry - 1] != background) {
						find = true;
						rx = rx + 1;
						ry = ry - 1;
					} else {
						numOfBackgroundPixels++;
					}
				
				inside = rx - 1 >= 0;
				if (!find && inside)
					if (visitedImage[rx - 1][ry] != background) {
						find = true;
						rx = rx - 1;
					} else {
						numOfBackgroundPixels++;
					}
				
				inside = rx + 1 < w;
				if (!find && inside)
					if (visitedImage[rx + 1][ry] != background) {
						find = true;
						rx = rx + 1;
					} else {
						numOfBackgroundPixels++;
					}
				
				inside = rx - 1 >= 0 && ry + 1 < h;
				if (!find && inside)
					if (visitedImage[rx - 1][ry + 1] != background) {
						find = true;
						rx = rx - 1;
						ry = ry + 1;
					} else {
						numOfBackgroundPixels++;
					}
				
				inside = ry + 1 < h;
				if (!find && inside)
					if (visitedImage[rx][ry + 1] != background) {
						find = true;
						ry = ry + 1;
					} else {
						numOfBackgroundPixels++;
					}
				
				inside = rx + 1 < w && ry + 1 < h;
				if (!find && inside)
					if (visitedImage[rx + 1][ry + 1] != background) {
						find = true;
						rx = rx + 1;
						ry = ry + 1;
					} else {
						numOfBackgroundPixels++;
					}
				
				// Found new pixel?
				if (find) {
					PositionAndColor temp = new PositionAndColor(rx, ry, visitedImage[rx][ry]);
					// count++;
					resultRegion.add(temp);
					// current region bigger than geometricThresh
					if (resultRegion.size() > geometricThresh - 1)
						return resultRegion;
					if (numOfBackgroundPixels <= 1) {
						visited.push(temp);
						dist = (x - rx) * (x - rx) + (y - ry) * (y - ry);
					} else
						goBack = true;
					// no pixel found -> go back
				} else
					goBack = true;
				
				if (goBack) {
					if (!visited.empty())
						visited.pop();
					if (!visited.empty()) {
						rx = visited.peek().x;
						ry = visited.peek().y;
						dist = (x - rx) * (x - rx) + (y - ry) * (y - ry);
					}
				}
				// new pixel is not in radius -> go back
			} else {
				if (!visited.empty())
					visited.pop();
				if (!visited.empty()) {
					rx = visited.peek().x;
					ry = visited.peek().y;
					dist = (x - rx) * (x - rx) + (y - ry) * (y - ry);
				}
				inside = true;
			}
		}
		return resultRegion;
	}
	
	/**
	 * Starts Labeling
	 */
	@Override
	public void detectClusters() {
		int w = image.getWidth();
		int h = image.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (visitedImage[x][y] != Color.WHITE.getRGB())
					try {
						ArrayList<PositionAndColor> region = regionGrowing(x, y, Color.WHITE.getRGB(), Double.MAX_VALUE, Integer.MAX_VALUE, true);
						if (region.size() > 0) {
							regionList.add(region);
							numberOfClusters++;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}
	
	@Override
	public Vector2i[] getClusterCenterPoints() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Vector2i[] getClusterDimension() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int[] getClusterSize() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getClusterCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getForegroundPixelCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int[] getClusterDimensionMinWH() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int[] getImageClusterIdMask() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int[] getImage1A() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void printOriginalImage() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void printClusterIds() {
		// TODO Auto-generated method stub
		
	}
}
