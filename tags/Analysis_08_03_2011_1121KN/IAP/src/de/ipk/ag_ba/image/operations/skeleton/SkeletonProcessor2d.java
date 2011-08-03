package de.ipk.ag_ba.image.operations.skeleton;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class SkeletonProcessor2d {
	
	public int[][] skelArray;
	public ArrayList<Point> endpoints = new ArrayList<Point>();
	public ArrayList<Point> branches = new ArrayList<Point>();
	public ArrayList<Limb> endlimbs = new ArrayList<Limb>();
	
	ArrayList<Limb> forRemove = new ArrayList<Limb>();
	
	public int colorEndpoints = Color.YELLOW.getRGB();
	public int colorBranches = Color.RED.getRGB();
	public int colorMarkedEndLimbs = Color.BLUE.getRGB();
	public int foreground = Color.WHITE.getRGB();
	private static final int background = Color.BLACK.getRGB(); // TODO Color.BLACK.getRGB(); -16777216 must be replaced by the real backgroundcolor
	public int colorDebug = Color.GREEN.getRGB();
	
	public boolean debug = false;
	
	public SkeletonProcessor2d(FlexibleImage inp) {
		this.skelArray = inp.getAs2A().clone();
		// TODO findEndpointsAndBranches(); all in one, do it later
	}
	
	public SkeletonProcessor2d(int[][] image, ArrayList<Point> endpoints, ArrayList<Point> branches) {
		this.skelArray = image.clone();
		this.endpoints = endpoints;
		this.branches = branches;
	}
	
	public SkeletonProcessor2d(int[][] image) {
		this.skelArray = image;
	}
	
	public void setBranches(ArrayList<Point> branches) {
		this.branches = branches;
	}
	
	public void setEndpoints(ArrayList<Point> endPoints) {
		this.endpoints = endPoints;
	}
	
	public ArrayList<Limb> getEndlimbs() {
		return endlimbs;
	}
	
	public void setEndlimbs(ArrayList<Limb> endlimbs) {
		this.endlimbs = endlimbs;
	}
	
	/**
	 * Iterative Version
	 * Only search Limbs which connected to an endpoint, stops on next branch or endpoint
	 * 
	 * @return
	 */
	public void calculateEndLimbsIterative() {
		for (int index = 0; index < endpoints.size(); index++) {
			Limb res = new Limb(endpoints.get(index));
			Point actual = endpoints.get(index);
			boolean hasNeighbour = false;
			
			if (getNeighbour(actual) != null)
				hasNeighbour = true;
			
			while (hasNeighbour) {
				Point neighbour = getNeighbour(actual);
				if (neighbour == null) {
					hasNeighbour = false;
					res.setInitialpoint(actual);
				}
				if (neighbour != null && isBranch(neighbour) == true) {
					hasNeighbour = false;
					res.setInitialpoint(neighbour);
				}
				if (neighbour != null && isEndpoint(neighbour) == true) {
					hasNeighbour = false;
					res.setInitialpoint(neighbour);
					res.isCut = true;
					endpoints.remove(neighbour);
				}
				if (neighbour != null && !isEndpoint(neighbour) && !isBranch(neighbour)) {
					actual = neighbour;
					res.addPoint(neighbour);
					mark(neighbour, colorMarkedEndLimbs);
				}
			}
			this.endlimbs.add(res);
		}
	}
	
	/**
	 * Recursive Method to calculate Endlimbs
	 */
	public void calculateEndlimbsRecursive() {
		for (int i = 0; i < endpoints.size(); i++) {
			Limb res = new Limb(endpoints.get(i));
			Point actual = endpoints.get(i);
			calcrecEL(actual, res);
		}
	}
	
	private void calcrecEL(Point actual, Limb res) {
		Point neighbour = getNeighbour(actual);
		boolean ok = false;
		if (neighbour == null) {
			res.setInitialpoint(actual);
			ok = true;
		}
		if (isBranch(neighbour)) {
			res.setInitialpoint(neighbour);
			endlimbs.add(res);
			ok = true;
		}
		if (isEndpoint(neighbour)) {
			res.setInitialpoint(neighbour);
			endpoints.remove(neighbour);
			res.isCut = true;
			endlimbs.add(res);
			ok = true;
		}
		if (neighbour != null && ok == false) {
			res.addPoint(neighbour);
			mark(neighbour, colorMarkedEndLimbs);
			calcrecEL(neighbour, res);
		}
	}
	
	public void findTrailWithMaxBranches() {
		ArrayList<Limb> trails = new ArrayList<Limb>();
		Point p = endpoints.get(0);
		Limb res = new Limb(p);
		Point actual = p;
		calcrecTWMB(actual, res);
	}
	
	private void calcrecTWMB(Point actual, Limb res) {
		Point neighbour = getNeighbour(actual);
		boolean ok = false;
		if (neighbour == null) {
			res.setInitialpoint(actual);
			ok = true;
		}
		if (isBranch(neighbour)) {
			res.addPoint(neighbour);
			ArrayList<Point> temp = getNeighbours(neighbour);
			mark(neighbour, colorMarkedEndLimbs);
			for (Point p : temp) {
				calcrecTWMB(p, res);
			}
		}
		if (isEndpoint(neighbour)) {
			res.setInitialpoint(neighbour);
			endlimbs.add(res);
			ok = true;
		}
		if (neighbour != null && ok == false) {
			res.addPoint(neighbour);
			mark(neighbour, colorMarkedEndLimbs);
			calcrecTWMB(neighbour, res);
		}
	}
	
	public void connectSkeleton() {
		for (Limb l : endlimbs) {
			if (l.isCut) {
				Point next = getNextLimb(l);
				connect(l.initialpoint, next);
			}
		}
	}
	
	private Point getNextLimb(Limb l) {
		int dist = Integer.MAX_VALUE;
		Point ini = l.getInitialpoint();
		Point res = null;
		for (Point p : endpoints) {
			if (ini.distance(p) < dist)
				res = p;
		}
		for (Point p : branches) {
			if (ini.distance(p) < dist)
				res = p;
		}
		return res;
	}
	
	/**
	 * Draw line using Bresenham algorithm.
	 * 
	 * @param initialpoint
	 * @param next
	 */
	private void connect(Point initialpoint, Point next) {
		int x0 = initialpoint.x;
		int x1 = next.x;
		int y0 = initialpoint.y;
		int y1 = next.y;
		int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
		int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
		int err = dx + dy, e2; /* error value e_xy */
		
		while (true) { /* loop */
			skelArray[x0][y0] = foreground;
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
	}
	
	private ArrayList<Point> getNeighbours(Point inp) {
		ArrayList<Point> res = new ArrayList<Point>();
		
		if (inp.x > 0 && inp.x < this.skelArray.length && inp.y > 0 && inp.y < this.skelArray[0].length) {
			
			if (this.skelArray[inp.x - 1][inp.y - 1] != background && this.skelArray[inp.x - 1][inp.y - 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y - 1));
			}
			
			if (this.skelArray[inp.x + 1][inp.y + 1] != background && this.skelArray[inp.x + 1][inp.y + 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y - 1));
			}
			
			if (this.skelArray[inp.x + 1][inp.y - 1] != background && this.skelArray[inp.x + 1][inp.y - 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y - 1));
			}
			
			if (this.skelArray[inp.x - 1][inp.y + 1] != background && this.skelArray[inp.x - 1][inp.y + 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y - 1));
			}
			
			if (this.skelArray[inp.x][inp.y - 1] != background && this.skelArray[inp.x][inp.y - 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y - 1));
			}
			
			if (this.skelArray[inp.x - 1][inp.y] != background && this.skelArray[inp.x - 1][inp.y] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y - 1));
			}
			
			if (this.skelArray[inp.x + 1][inp.y] != background && this.skelArray[inp.x + 1][inp.y] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y - 1));
			}
			
			if (this.skelArray[inp.x][inp.y + 1] != background && this.skelArray[inp.x][inp.y + 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y - 1));
			}
		}
		return res;
	}
	
	/**
	 * Return Pixel in the 8 - neighbourhood, if pixel is no endpoint, background or pixel who is already visited.
	 * Branchpoints in 8 - neighbourhood will be returned.
	 * 
	 * @return
	 */
	private Point getNeighbour(Point inp) {
		Point res = null;
		Point branchInNeighbourhood = null;
		if (inp.x > 0 && inp.x < this.skelArray.length && inp.y > 0 && inp.y < this.skelArray[0].length) {
			
			if (this.skelArray[inp.x - 1][inp.y - 1] != background && this.skelArray[inp.x - 1][inp.y - 1] != colorMarkedEndLimbs
					&& this.skelArray[inp.x - 1][inp.y - 1] != colorEndpoints) {
				if (this.skelArray[inp.x - 1][inp.y - 1] != colorBranches) {
					res = new Point(inp.x - 1, inp.y - 1);
				} else
					branchInNeighbourhood = new Point(inp.x - 1, inp.y - 1);
			}
			if (this.skelArray[inp.x + 1][inp.y + 1] != background && this.skelArray[inp.x + 1][inp.y + 1] != colorMarkedEndLimbs
					&& this.skelArray[inp.x + 1][inp.y + 1] != colorEndpoints) {
				if (this.skelArray[inp.x + 1][inp.y + 1] != colorBranches) {
					res = new Point(inp.x + 1, inp.y + 1);
				} else
					branchInNeighbourhood = new Point(inp.x + 1, inp.y + 1);
			}
			if (this.skelArray[inp.x + 1][inp.y - 1] != background && this.skelArray[inp.x + 1][inp.y - 1] != colorMarkedEndLimbs
					&& this.skelArray[inp.x + 1][inp.y - 1] != colorEndpoints) {
				if (this.skelArray[inp.x + 1][inp.y - 1] != colorBranches) {
					res = new Point(inp.x + 1, inp.y - 1);
				} else
					branchInNeighbourhood = new Point(inp.x + 1, inp.y - 1);
			}
			if (this.skelArray[inp.x - 1][inp.y + 1] != background && this.skelArray[inp.x - 1][inp.y + 1] != colorMarkedEndLimbs
					&& this.skelArray[inp.x - 1][inp.y + 1] != colorEndpoints) {
				if (this.skelArray[inp.x - 1][inp.y + 1] != colorBranches) {
					res = new Point(inp.x - 1, inp.y + 1);
				} else
					branchInNeighbourhood = new Point(inp.x - 1, inp.y + 1);
			}
			if (this.skelArray[inp.x][inp.y - 1] != background && this.skelArray[inp.x][inp.y - 1] != colorMarkedEndLimbs
					&& this.skelArray[inp.x][inp.y - 1] != colorEndpoints) {
				if (this.skelArray[inp.x][inp.y - 1] != colorBranches) {
					res = new Point(inp.x, inp.y - 1);
				} else
					branchInNeighbourhood = new Point(inp.x, inp.y - 1);
			}
			if (this.skelArray[inp.x - 1][inp.y] != background && this.skelArray[inp.x - 1][inp.y] != colorMarkedEndLimbs
					&& this.skelArray[inp.x - 1][inp.y] != colorEndpoints) {
				if (this.skelArray[inp.x - 1][inp.y] != colorBranches) {
					res = new Point(inp.x - 1, inp.y);
				} else
					branchInNeighbourhood = new Point(inp.x - 1, inp.y);
			}
			if (this.skelArray[inp.x + 1][inp.y] != background && this.skelArray[inp.x + 1][inp.y] != colorMarkedEndLimbs
					&& this.skelArray[inp.x + 1][inp.y] != colorEndpoints) {
				if (this.skelArray[inp.x + 1][inp.y] != colorBranches) {
					res = new Point(inp.x + 1, inp.y);
				} else
					branchInNeighbourhood = new Point(inp.x + 1, inp.y);
			}
			if (this.skelArray[inp.x][inp.y + 1] != background && this.skelArray[inp.x][inp.y + 1] != colorMarkedEndLimbs
					&& this.skelArray[inp.x][inp.y + 1] != colorEndpoints) {
				if (this.skelArray[inp.x][inp.y + 1] != colorBranches) {
					res = new Point(inp.x, inp.y + 1);
				} else
					branchInNeighbourhood = new Point(inp.x, inp.y + 1);
			}
		}
		if (branchInNeighbourhood == null) {
			if (res == null)
				System.out.println("res == null" + " inpx: " + inp.x + " inpy: " + inp.y);
			return res;
		} else
			return branchInNeighbourhood;
	}
	
	/**
	 * Search and mark Endpoints and Branches, thererfore the image will be converted to an binary image and checked with masks
	 */
	public void findEndpointsAndBranches() {
		int[][][] listEndpointMasks = new int[13][3][3];
		int[][][] listBranchMasks = new int[18][3][3];
		
		// 0 - no pixel, 1 - one pixel, 2 -don´t care
		listBranchMasks[0] = new int[][] { { 1, 0, 1 }, { 0, 1, 0 }, { 1, 0, 1 } };
		listBranchMasks[1] = new int[][] { { 1, 0, 1 }, { 2, 1, 2 }, { 0, 1, 0 } };
		listBranchMasks[2] = new int[][] { { 1, 2, 0 }, { 0, 1, 1 }, { 1, 2, 0 } };
		listBranchMasks[3] = new int[][] { { 0, 1, 0 }, { 2, 1, 2 }, { 1, 0, 1 } };
		listBranchMasks[4] = new int[][] { { 0, 2, 1 }, { 1, 1, 0 }, { 0, 2, 1 } };
		listBranchMasks[5] = new int[][] { { 0, 1, 0 }, { 1, 1, 1 }, { 2, 2, 2 } };
		listBranchMasks[6] = new int[][] { { 0, 1, 2 }, { 1, 1, 2 }, { 0, 1, 2 } };
		listBranchMasks[7] = new int[][] { { 2, 2, 2 }, { 1, 1, 1 }, { 0, 1, 0 } };
		listBranchMasks[8] = new int[][] { { 2, 1, 0 }, { 2, 1, 1 }, { 2, 1, 0 } };
		listBranchMasks[9] = new int[][] { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } };
		listBranchMasks[10] = new int[][] { { 2, 1, 2 }, { 1, 1, 0 }, { 2, 0, 1 } };
		listBranchMasks[11] = new int[][] { { 2, 0, 1 }, { 1, 1, 0 }, { 2, 1, 2 } };
		listBranchMasks[12] = new int[][] { { 1, 0, 2 }, { 0, 1, 1 }, { 2, 1, 2 } };
		listBranchMasks[13] = new int[][] { { 2, 1, 2 }, { 0, 1, 1 }, { 1, 0, 2 } };
		listBranchMasks[14] = new int[][] { { 1, 0, 1 }, { 0, 1, 0 }, { 1, 0, 0 } };
		listBranchMasks[15] = new int[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 1, 0, 1 } };
		listBranchMasks[16] = new int[][] { { 0, 0, 1 }, { 0, 1, 0 }, { 1, 0, 1 } };
		listBranchMasks[17] = new int[][] { { 1, 0, 1 }, { 0, 1, 0 }, { 0, 0, 1 } };
		
		listEndpointMasks[0] = new int[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 1, 0 } };
		listEndpointMasks[1] = new int[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
		listEndpointMasks[2] = new int[][] { { 0, 0, 0 }, { 0, 1, 1 }, { 0, 0, 0 } };
		listEndpointMasks[3] = new int[][] { { 0, 0, 1 }, { 0, 1, 0 }, { 0, 0, 0 } };
		listEndpointMasks[4] = new int[][] { { 0, 1, 0 }, { 0, 1, 0 }, { 0, 0, 0 } };
		listEndpointMasks[5] = new int[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } };
		listEndpointMasks[6] = new int[][] { { 0, 0, 0 }, { 1, 1, 0 }, { 0, 0, 0 } };
		listEndpointMasks[7] = new int[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 1, 0, 0 } };
		listEndpointMasks[8] = new int[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 1, 0 } };
		listEndpointMasks[9] = new int[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 1, 1 } };
		listEndpointMasks[10] = new int[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 1, 1, 0 } };
		listEndpointMasks[11] = new int[][] { { 0, 1, 1 }, { 0, 1, 0 }, { 0, 0, 0 } };
		listEndpointMasks[12] = new int[][] { { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 0 } };
		
		int width = skelArray.length;
		int height = skelArray[0].length;
		int[][] imgbin;
		
		imgbin = rgbToBinaryArray(skelArray);
		skelArray = binaryArrayToRgb(imgbin);
		
		if (endpoints != null)
			endpoints.clear();
		
		if (branches != null)
			branches.clear();
		
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				if (imgbin[x][y] != 0) {
					int[][] area = new int[][] { { imgbin[x - 1][y - 1], imgbin[x][y - 1], imgbin[x + 1][y - 1] },
							{ imgbin[x - 1][y], imgbin[x][y], imgbin[x + 1][y] },
							{ imgbin[x - 1][y + 1], imgbin[x][y + 1], imgbin[x + 1][y + 1] } };
					
					// Endpoints
					for (int index = 0; index < listEndpointMasks.length; index++) {
						if (matchMask3x3(listEndpointMasks[index], area)) {
							skelArray[x][y] = colorEndpoints;
							endpoints.add(new Point(x, y));
						}
					}
					
					// Branches
					for (int index = 0; index < listBranchMasks.length; index++) {
						if (matchMask3x3(listBranchMasks[index], area)) {
							skelArray[x][y] = colorBranches;
							branches.add(new Point(x, y));
						}
					}
				}
			}
		}
	}
	
	public void deleteShortEndLimbs(int threshold) {
		for (Limb l : endlimbs) {
			if (l.length() < threshold) {
				forRemove.add(l);
			}
		}
		refreshSkeleton();
	}
	
	/**
	 * delete all endlimbs which are shorter than thresh
	 * 
	 * @param threshold
	 *           - replaced by autothresh, calculate the avarage length of all limbs reduced by 10 percent
	 * @param repeat
	 */
	public void deleteShortEndLimbs(int threshold, int repeat) {
		for (int i = 0; i < repeat; i++) {
			calculateEndlimbsRecursive();
			threshold = getAutoThresh();
			System.out.println("thresh: " + threshold);
			deleteShortEndLimbs(20);
		}
	}
	
	private int getAutoThresh() {
		double res = 0;
		for (Limb l : endlimbs) {
			res += l.points.size();
		}
		return (int) ((res / (double) endlimbs.size()) - (res / (double) endlimbs.size()) * 0.1);
	}
	
	/**
	 * Removed all limbs saved in forRemove and recalculate all end- and branchpoints.
	 */
	private void refreshSkeleton() {
		if (forRemove.size() > 0) {
			for (int index = 0; index < forRemove.size(); index++) {
				for (int index2 = 0; index2 < forRemove.get(index).points.size(); index2++) {
					Point point = forRemove.get(index).points.get(index2);
					int x = point.x;
					int y = point.y;
					if (debug)
						skelArray[x][y] = colorDebug;
					else
						skelArray[x][y] = background;
				}
				if (debug)
					skelArray[forRemove.get(index).endpoint.x][forRemove.get(index).endpoint.y] = colorDebug;
				else
					skelArray[forRemove.get(index).endpoint.x][forRemove.get(index).endpoint.y] = background;
				endlimbs.remove(forRemove.get(index));
			}
			forRemove.clear();
			findEndpointsAndBranches();
		}
	}
	
	private boolean matchMask3x3(int[][] mask, int[][] img) {
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (mask[x][y] != 2 && mask[x][y] != img[x][y])
					return false;
			}
		}
		return true;
	}
	
	public void print(String title, boolean show) {
		new FlexibleImage(this.skelArray).print(title, show);
	}
	
	public FlexibleImage getAsFlexibleImage() {
		return new FlexibleImage(this.skelArray);
	}
	
	private boolean isBranch(Point inp) {
		if (branches.contains(inp))
			return true;
		else
			return false;
	}
	
	private boolean isEndpoint(Point inp) {
		if (endpoints.contains(inp))
			return true;
		else
			return false;
	}
	
	private void mark(Point inp, int color) {
		skelArray[inp.x][inp.y] = color;
	}
	
	private int[][] binaryArrayToRgb(int[][] input) {
		int[][] res = new int[input.length][input[0].length];
		for (int x = 0; x < input.length; x++) {
			for (int y = 0; y < input[0].length; y++) {
				
				if (input[x][y] == 0)
					res[x][y] = background;
				else
					res[x][y] = foreground;
			}
		}
		return res;
	}
	
	private int[][] rgbToBinaryArray(int[][] input) {
		int width = input.length;
		int height = input[0].length;
		int[][] res = new int[width][height];
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				if (input[x][y] == background)
					res[x][y] = 0;
				else {
					res[x][y] = 1;
				}
			}
		}
		return res;
	}
}

/*
 * private Point getNeighbour(Point inp) {
 * Point res = null;
 * Point branchInNeighbourhood = null;
 * Point endpointINeighbourhood = null;
 * if (inp.x > 0 && inp.x < this.skelArray.length && inp.y > 0 && inp.y < this.skelArray[0].length) {
 * if (this.skelArray[inp.x - 1][inp.y - 1] != background && this.skelArray[inp.x - 1][inp.y - 1] != colorMarkedEndLimbs) {
 * if (this.skelArray[inp.x - 1][inp.y - 1] != colorBranches && this.skelArray[inp.x - 1][inp.y - 1] != colorEndpoints) {
 * res = new Point(inp.x - 1, inp.y - 1);
 * if (this.skelArray[inp.x - 1][inp.y - 1] == colorBranches)
 * branchInNeighbourhood = new Point(inp.x - 1, inp.y - 1);
 * if (this.skelArray[inp.x - 1][inp.y - 1] == colorEndpoints)
 * endpointINeighbourhood = new Point(inp.x - 1, inp.y - 1);
 * }
 * }
 * if (this.skelArray[inp.x + 1][inp.y + 1] != background && this.skelArray[inp.x + 1][inp.y + 1] != colorMarkedEndLimbs) {
 * if (this.skelArray[inp.x + 1][inp.y + 1] != colorBranches && this.skelArray[inp.x + 1][inp.y + 1] != colorEndpoints) {
 * res = new Point(inp.x + 1, inp.y + 1);
 * if (this.skelArray[inp.x + 1][inp.y + 1] == colorBranches)
 * branchInNeighbourhood = new Point(inp.x + 1, inp.y + 1);
 * if (this.skelArray[inp.x + 1][inp.y + 1] == colorEndpoints)
 * endpointINeighbourhood = new Point(inp.x + 1, inp.y + 1);
 * }
 * }
 * if (this.skelArray[inp.x + 1][inp.y - 1] != background && this.skelArray[inp.x + 1][inp.y - 1] != colorMarkedEndLimbs) {
 * if (this.skelArray[inp.x + 1][inp.y - 1] != colorBranches && this.skelArray[inp.x + 1][inp.y - 1] != colorEndpoints) {
 * res = new Point(inp.x + 1, inp.y - 1);
 * if (this.skelArray[inp.x + 1][inp.y - 1] == colorBranches)
 * branchInNeighbourhood = new Point(inp.x + 1, inp.y - 1);
 * if (this.skelArray[inp.x + 1][inp.y - 1] == colorEndpoints)
 * endpointINeighbourhood = new Point(inp.x + 1, inp.y - 1);
 * }
 * }
 * if (this.skelArray[inp.x - 1][inp.y + 1] != background && this.skelArray[inp.x - 1][inp.y + 1] != colorMarkedEndLimbs) {
 * if (this.skelArray[inp.x - 1][inp.y + 1] != colorBranches && this.skelArray[inp.x - 1][inp.y + 1] != colorEndpoints) {
 * res = new Point(inp.x - 1, inp.y + 1);
 * if (this.skelArray[inp.x - 1][inp.y + 1] == colorBranches)
 * branchInNeighbourhood = new Point(inp.x - 1, inp.y + 1);
 * if (this.skelArray[inp.x - 1][inp.y + 1] == colorEndpoints)
 * endpointINeighbourhood = new Point(inp.x - 1, inp.y + 1);
 * }
 * }
 * if (this.skelArray[inp.x][inp.y - 1] != background && this.skelArray[inp.x][inp.y - 1] != colorMarkedEndLimbs) {
 * if (this.skelArray[inp.x][inp.y - 1] != colorBranches && this.skelArray[inp.x][inp.y - 1] != colorEndpoints) {
 * res = new Point(inp.x, inp.y - 1);
 * if (this.skelArray[inp.x][inp.y - 1] == colorBranches)
 * branchInNeighbourhood = new Point(inp.x, inp.y - 1);
 * if (this.skelArray[inp.x][inp.y - 1] == colorEndpoints)
 * endpointINeighbourhood = new Point(inp.x, inp.y - 1);
 * }
 * }
 * if (this.skelArray[inp.x - 1][inp.y] != background && this.skelArray[inp.x - 1][inp.y] != colorMarkedEndLimbs) {
 * if (this.skelArray[inp.x - 1][inp.y] != colorBranches && this.skelArray[inp.x - 1][inp.y] != colorEndpoints) {
 * res = new Point(inp.x - 1, inp.y);
 * if (this.skelArray[inp.x - 1][inp.y] == colorBranches)
 * branchInNeighbourhood = new Point(inp.x - 1, inp.y);
 * if (this.skelArray[inp.x - 1][inp.y] == colorEndpoints)
 * endpointINeighbourhood = new Point(inp.x - 1, inp.y);
 * }
 * }
 * if (this.skelArray[inp.x + 1][inp.y] != background && this.skelArray[inp.x + 1][inp.y] != colorMarkedEndLimbs) {
 * if (this.skelArray[inp.x + 1][inp.y] != colorBranches && this.skelArray[inp.x + 1][inp.y] != colorEndpoints) {
 * res = new Point(inp.x + 1, inp.y);
 * if (this.skelArray[inp.x + 1][inp.y] == colorBranches)
 * branchInNeighbourhood = new Point(inp.x + 1, inp.y);
 * if (this.skelArray[inp.x + 1][inp.y] == colorEndpoints)
 * endpointINeighbourhood = new Point(inp.x + 1, inp.y);
 * }
 * }
 * if (this.skelArray[inp.x][inp.y + 1] != background && this.skelArray[inp.x][inp.y + 1] != colorMarkedEndLimbs) {
 * if (this.skelArray[inp.x][inp.y + 1] != colorBranches && this.skelArray[inp.x][inp.y + 1] != colorEndpoints) {
 * res = new Point(inp.x, inp.y + 1);
 * if (this.skelArray[inp.x][inp.y + 1] == colorBranches)
 * branchInNeighbourhood = new Point(inp.x, inp.y + 1);
 * if (this.skelArray[inp.x][inp.y + 1] == colorEndpoints)
 * endpointINeighbourhood = new Point(inp.x, inp.y + 1);
 * }
 * }
 * }
 * if (branchInNeighbourhood != null && endpointINeighbourhood == null)
 * return branchInNeighbourhood;
 * if (branchInNeighbourhood == null && endpointINeighbourhood != null)
 * return endpointINeighbourhood;
 * if (branchInNeighbourhood != null && endpointINeighbourhood != null)
 * return endpointINeighbourhood;
 * return res;
 * }
 */
