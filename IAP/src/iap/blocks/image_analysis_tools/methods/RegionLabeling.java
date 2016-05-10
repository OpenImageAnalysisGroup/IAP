package iap.blocks.image_analysis_tools.methods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import org.Colors;
import org.Vector2i;

import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
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
	int numberOfClusters;
	private Vector2i[] centerPoints;
	private Vector2i[] clusterDimension;
	int[] clusterSize;
	private TopBottomLeftRight[] boundingBox;
	boolean eightNeighbourhood = true;
	long foregroundpixelCount = 0;
	
	public LinkedList<ArrayList<PositionAndColor>> getRegionList() {
		return regionList;
	}
	
	public void setNeigbourhoodFour() {
		eightNeighbourhood = false;
	}
	
	public ArrayList<PositionAndColor> getCluster(int idx) {
		return regionList.get(idx);
	}
	
	/**
	 * Converts integer values to real gray values.
	 */
	public LinkedList<ArrayList<PositionAndColor>> getRegionListConvertedToGrayValues() {
		for (ArrayList<PositionAndColor> list : regionList)
			for (PositionAndColor pix : list)
				pix.setIntensityInt((pix.intensityInt & 0xff0000) >> 16);
		return regionList;
	}
	
	private final int background;
	
	public RegionLabeling(Image img, boolean isBorderImage, int background, int borderColor) {
		this.image = img;
		this.isBorderImage = isBorderImage;
		this.borderColor = borderColor;
		this.visitedImage = img.getAs2A();
		this.background = background;
		centerPoints = null;
		labeledImage = null;
		clusterSize = null;
		clusterDimension = null;
	}
	
	@Override
	public Image getClusterImage() {
		if (labeledImage == null) {
			int[][] labeled = image.copy().getAs2A();
			ArrayList<Color> colors = Colors.get(regionList.size(), 1);
			int idx = 0;
			for (ArrayList<PositionAndColor> clu : regionList) {
				int cluColor = colors.get(idx).getRGB();
				for (PositionAndColor pix : clu) {
					labeled[pix.x][pix.y] = cluColor;
				}
				idx++;
			}
			labeledImage = new Image(labeled);
		}
		return labeledImage;
	}
	
	private ArrayList<PositionAndColor> regionGrowing(int x, int y, int background, double radius, int geometricThresh, boolean debug) {
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
		if (debug) {
			show = new Image(visitedImage);
			show.show("debug");
		}
		
		// test if new pixel is in image space
		if (rx >= 0 && ry >= 0 && rx < w && ry < h)
			inside = true;
		
		while (!visited.empty()) {
			// update process window for debug
			if (visitedImage[rx][ry] != background && debug) {
				show.update(new Image(visitedImage));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			visitedImage[rx][ry] = background;
			
			find = false;
			
			if (dist < radius) {
				// 8-Neighbourhood
				if (eightNeighbourhood) {
					inside = rx - 1 >= 0 && ry - 1 >= 0;
					if (inside)
						if (visitedImage[rx - 1][ry - 1] != background) {
							find = true;
							rx = rx - 1;
							ry = ry - 1;
						}
					
					inside = ry - 1 >= 0 && rx + 1 < w;
					if (inside)
						if (visitedImage[rx + 1][ry - 1] != background) {
							if (!find) {
								find = true;
								rx = rx + 1;
								ry = ry - 1;
							}
						}
					
					inside = rx - 1 >= 0 && ry + 1 < h;
					if (inside)
						if (visitedImage[rx - 1][ry + 1] != background) {
							if (!find) {
								find = true;
								rx = rx - 1;
								ry = ry + 1;
							}
						}
					
					inside = rx + 1 < w && ry + 1 < h;
					if (inside)
						if (visitedImage[rx + 1][ry + 1] != background) {
							if (!find) {
								find = true;
								rx = rx + 1;
								ry = ry + 1;
							}
						}
				}
				// 4-Neighbourhood
				inside = ry - 1 >= 0;
				if (inside)
					if (visitedImage[rx][ry - 1] != background) {
						if (!find) {
							find = true;
							ry = ry - 1;
						}
					}
				
				inside = rx - 1 >= 0;
				if (inside)
					if (visitedImage[rx - 1][ry] != background) {
						if (!find) {
							find = true;
							rx = rx - 1;
						}
					}
				
				inside = rx + 1 < w;
				if (inside)
					if (visitedImage[rx + 1][ry] != background) {
						if (!find) {
							find = true;
							rx = rx + 1;
						}
					}
				
				inside = ry + 1 < h;
				if (inside)
					if (visitedImage[rx][ry + 1] != background) {
						if (!find) {
							find = true;
							ry = ry + 1;
						}
					}
				
				// Found new pixel?
				if (find) {
					foregroundpixelCount++;
					PositionAndColor temp = new PositionAndColor(rx, ry, visitedImage[rx][ry]);
					resultRegion.add(temp);
					// current region bigger than geometricThresh
					if (resultRegion.size() > geometricThresh - 1)
						return resultRegion;
					visited.push(temp);
					dist = (x - rx) * (x - rx) + (y - ry) * (y - ry);
					// no pixel found -> go back
				} else {
					// if (goBack) {
					visitedImage[rx][ry] = background;
					if (!visited.empty())
						visited.pop();
					if (!visited.empty()) {
						rx = visited.peek().x;
						ry = visited.peek().y;
						dist = (x - rx) * (x - rx) + (y - ry) * (y - ry);
						// }
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
		foregroundpixelCount = 0;
		regionList = new LinkedList<>();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (visitedImage[x][y] != background) {
					ArrayList<PositionAndColor> region = regionGrowing(x, y, background, Double.MAX_VALUE, Integer.MAX_VALUE, false);
					if (region.size() > 0) {
						regionList.add(region);
					}
				}
			}
		}
		numberOfClusters = regionList.size();
	}
	
	@Override
	public Vector2i[] getClusterCenterPoints() {
		if (centerPoints == null) {
			centerPoints = new Vector2i[numberOfClusters];
			int idx = 0;
			for (ArrayList<PositionAndColor> cluster : regionList) {
				int minX = Integer.MAX_VALUE;
				int maxX = Integer.MIN_VALUE;
				int minY = Integer.MAX_VALUE;
				int maxY = Integer.MIN_VALUE;
				for (PositionAndColor pix : cluster) {
					if (pix.x > maxX)
						maxX = pix.x;
					if (pix.x < minX)
						minX = pix.x;
					if (pix.y > maxY)
						maxY = pix.y;
					if (pix.y < minY)
						minY = pix.y;
				}
				centerPoints[idx] = new Vector2i((maxX - minX) / 2 + minX, (maxY - minY) / 2 + minY);
				idx++;
			}
		}
		return centerPoints;
	}
	
	@Override
	public Vector2i[] getClusterDimension() {
		if (clusterDimension == null) {
			clusterDimension = new Vector2i[numberOfClusters];
			int idx = 0;
			for (ArrayList<PositionAndColor> cluster : regionList) {
				int minX = Integer.MAX_VALUE;
				int maxX = Integer.MIN_VALUE;
				int minY = Integer.MAX_VALUE;
				int maxY = Integer.MIN_VALUE;
				for (PositionAndColor pix : cluster) {
					if (pix.x > maxX)
						maxX = pix.x;
					if (pix.x < minX)
						minX = pix.x;
					if (pix.y > maxY)
						maxY = pix.y;
					if (pix.y < minY)
						minY = pix.y;
				}
				clusterDimension[idx] = new Vector2i((maxX - minX), (maxY - minY));
				idx++;
			}
		}
		return clusterDimension;
	}
	
	@Override
	public int[] getClusterSize() {
		if (clusterSize == null) {
			clusterSize = new int[numberOfClusters];
			int idx = 0;
			for (ArrayList<PositionAndColor> cluster : regionList)
				clusterSize[idx++] = cluster.size();
		}
		return clusterSize;
	}
	
	@Override
	public int getClusterCount() {
		return regionList.size();
	}
	
	/**
	 * Use getForegroundPixelCountl() instead!
	 */
	@Override
	@Deprecated
	public int getForegroundPixelCount() {
		return (Integer) null;
	}
	
	public long getForegroundPixelCountl() {
		return foregroundpixelCount;
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
	
	public TopBottomLeftRight[] getBoundingBox() {
		if (boundingBox == null) {
			boundingBox = new TopBottomLeftRight[numberOfClusters];
			int idx = 0;
			for (ArrayList<PositionAndColor> cluster : regionList) {
				int minX = Integer.MAX_VALUE;
				int maxX = Integer.MIN_VALUE;
				int minY = Integer.MAX_VALUE;
				int maxY = Integer.MIN_VALUE;
				for (PositionAndColor pix : cluster) {
					if (pix.x > maxX)
						maxX = pix.x;
					if (pix.x < minX)
						minX = pix.x;
					if (pix.y > maxY)
						maxY = pix.y;
					if (pix.y < minY)
						minY = pix.y;
				}
				boundingBox[idx] = new TopBottomLeftRight(minY, maxY, minX, maxX);
				idx++;
			}
		}
		return boundingBox;
	}
}
