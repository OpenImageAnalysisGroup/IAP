package de.ipk.ag_ba.image.operations.skeleton;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class SkeletonProcessor2d {
	
	public int[][] skelImg;
	public ArrayList<Point> endpoints = new ArrayList<Point>();
	public ArrayList<Point> branches = new ArrayList<Point>();
	public ArrayList<Limb> endlimbs = new ArrayList<Limb>();
	
	ArrayList<Limb> forRemove = new ArrayList<Limb>();
	
	public static final int colorEndpoints = Color.PINK.getRGB();
	public static final int colorBranches = Color.RED.getRGB();
	public static final int colorMarkedEndLimbs = Color.BLUE.getRGB();
	public static final int foreground = Color.orange.getRGB();
	public static final int background = Color.BLACK.getRGB(); // TODO Color.BLACK.getRGB() or -16777216 must be replaced by the real backgroundcolor
	public static final int colorDebug = Color.GREEN.getRGB();
	
	public boolean debug = false;
	
	public SkeletonProcessor2d(FlexibleImage inp) {
		this.skelImg = inp.getAs2A().clone();
		// TODO findEndpointsAndBranches(); all in one, do it later
	}
	
	public SkeletonProcessor2d(int[][] image, ArrayList<Point> endpoints, ArrayList<Point> branches) {
		this.skelImg = image.clone();
		this.endpoints = endpoints;
		this.branches = branches;
	}
	
	public SkeletonProcessor2d(int[][] image) {
		this.skelImg = image;
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
	 * Recursive Method to calculate Endlimbs
	 */
	public void calculateEndlimbsRecursive() {
		endlimbs.clear();
		for (int i = 0; i < endpoints.size(); i++) {
			Limb res = new Limb(endpoints.get(i));
			Point actual = endpoints.get(i);
			calcrecEL2(actual, res);
		}
		basicRefreshSkeleton();
	}
	
	private void calcrecEL2(Point actual, Limb res) {
		ArrayList<Point> neighbours = getNeighbours(actual);
		boolean ok = false;
		mark(actual, colorMarkedEndLimbs);
		Point branch = hasBranch(neighbours);
		Point endpoint = hasEndpoint(neighbours);
		Point next = hasNextpoint(neighbours);
		
		if (neighbours.size() == 0) {
			res.setInitialpoint(actual);
			ok = true;
		}
		if (branch != null) {
			res.setInitialpoint(branch);
			endlimbs.add(res);
			ok = true;
		}
		if (endpoint != null) {
			res.setInitialpoint(endpoint);
			endpoints.remove(endpoint);
			res.isCut = true;
			endlimbs.add(res);
			ok = true;
		}
		if (next != null && ok == false) {
			res.addPoint(next);
			mark(next, colorMarkedEndLimbs);
			calcrecEL2(next, res);
		}
		if (next == null) {
			// System.out.println("No neighbour found!!");
		}
	}
	
	private Point hasBranch(ArrayList<Point> neighbours) {
		for (Point p : neighbours) {
			if (skelImg[p.x][p.y] == colorBranches)
				return p;
		}
		return null;
	}
	
	private Point hasEndpoint(ArrayList<Point> neighbours) {
		for (Point p : neighbours) {
			if (skelImg[p.x][p.y] == colorEndpoints)
				return p;
		}
		return null;
	}
	
	private Point hasNextpoint(ArrayList<Point> neighbours) {
		for (Point p : neighbours) {
			if (skelImg[p.x][p.y] == foreground)
				return p;
		}
		return null;
	}
	
	/**
	 * Method to detect the clade: take the lowest end-point(min y), calculate way to any endpoint
	 * -> based on the assumption - the way which contains max number of branches shall be the clade
	 */
	public void findTrailWithMaxBranches() {
		ArrayList<Limb> trails = new ArrayList<Limb>();
		Point p = getLowest(endpoints);
		Limb res = new Limb(p);
		Point actual = p;
		calcrecAllBranches(actual, res);
	}
	
	private Point getLowest(ArrayList<Point> endpoints2) {
		double y = Integer.MAX_VALUE;
		Point res = null;
		for (Point p : endpoints) {
			if (p.y < y) {
				res = p;
				y = p.y;
			}
		}
		return res;
	}
	
	private void calcrecAllBranches(Point actual, Limb res) {
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
				calcrecAllBranches(p, res);
			}
		}
		if (isEndpoint(neighbour)) {
			res.setInitialpoint(neighbour);
			endlimbs.add(res);
			ok = true;
		}
		if (neighbour != null && !(isBranch(neighbour)) && !ok) {
			res.addPoint(neighbour);
			mark(neighbour, colorMarkedEndLimbs);
			calcrecAllBranches(neighbour, res);
		}
	}
	
	public boolean connectSkeleton() {
		boolean added = false;
		for (Limb l : endlimbs) {
			if (l.isCut) {
				// System.out.println("connecting limbs: " + l.endpoint.toString() + " , " + l.initialpoint.toString());
				Point[] toConnect = getNearestLimb(l);
				connect(toConnect[0], toConnect[1], colorMarkedEndLimbs, l);
				added = true;
			}
		}
		return added;
	}
	
	private Point[] getNearestLimb(Limb l) {
		// TODO improvement: first sort y, pre/succ calc dist -> O(nlogn)
		int dist = Integer.MAX_VALUE;
		Point ini = l.initialpoint;
		Point res = null;
		for (Point p : endpoints) {
			if (ini.distance(p) < dist && p != ini) {
				res = p;
				dist = (int) ini.distance(p);
			}
		}
		for (Point p : branches) {
			if (ini.distance(p) < dist && p != ini) {
				res = p;
				dist = (int) ini.distance(p);
			}
		}
		
		boolean endok = false;
		Point end = l.endpoint;
		for (Point p : endpoints) {
			if (end.distance(p) < dist && p != end) {
				res = p;
				dist = (int) end.distance(p);
				endok = true;
			}
		}
		for (Point p : branches) {
			if (end.distance(p) < dist && p != end) {
				res = p;
				dist = (int) end.distance(p);
				endok = true;
			}
		}
		if (endok) {
			endpoints.remove(end);
			l.endpoint = res;;
			return new Point[] { res, end };
		} else {
			endpoints.remove(ini);
			l.initialpoint = res;
			return new Point[] { res, ini };
		}
	}
	
	/**
	 * Draw line using Bresenham algorithm.
	 * 
	 * @param initialpoint
	 * @param endpoint
	 * @param l
	 */
	private void connect(Point initialpoint, Point endpoint, int color, Limb l) {
		int x0 = initialpoint.x;
		int x1 = endpoint.x;
		int y0 = initialpoint.y;
		int y1 = endpoint.y;
		int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
		int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
		int err = dx + dy, e2; /* error value e_xy */
		
		while (true) { /* loop */
			skelImg[x0][y0] = color;
			l.points.add(new Point(x0, y0));
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
	
	private Neighbourhood getNeighbourhood(Point inp) {
		return new Neighbourhood(getNeighbours(inp));
	}
	
	private ArrayList<Point> getNeighbours(Point inp) {
		ArrayList<Point> res = new ArrayList<Point>();
		
		if (inp.x > 0 && inp.x < this.skelImg.length && inp.y > 0 && inp.y < this.skelImg[0].length) {
			
			if (this.skelImg[inp.x - 1][inp.y - 1] != background && this.skelImg[inp.x - 1][inp.y - 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y - 1));
			}
			
			if (this.skelImg[inp.x + 1][inp.y + 1] != background && this.skelImg[inp.x + 1][inp.y + 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x + 1, inp.y + 1));
			}
			
			if (this.skelImg[inp.x + 1][inp.y - 1] != background && this.skelImg[inp.x + 1][inp.y - 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x + 1, inp.y - 1));
			}
			
			if (this.skelImg[inp.x - 1][inp.y + 1] != background && this.skelImg[inp.x - 1][inp.y + 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y + 1));
			}
			
			if (this.skelImg[inp.x][inp.y - 1] != background && this.skelImg[inp.x][inp.y - 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x, inp.y - 1));
			}
			
			if (this.skelImg[inp.x - 1][inp.y] != background && this.skelImg[inp.x - 1][inp.y] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x - 1, inp.y));
			}
			
			if (this.skelImg[inp.x + 1][inp.y] != background && this.skelImg[inp.x + 1][inp.y] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x + 1, inp.y));
			}
			
			if (this.skelImg[inp.x][inp.y + 1] != background && this.skelImg[inp.x][inp.y + 1] != colorMarkedEndLimbs) {
				res.add(new Point(inp.x, inp.y + 1));
			}
		}
		return res;
	}
	
	/**
	 * Do this at first!
	 * Search and mark Endpoints and Branches, therefore the image will be converted to an binary image and checked with masks.
	 * Also remove Dots.
	 */
	public void findEndpointsAndBranches() {
		int[][][] listEndpointMasks = new int[13][3][3];
		int[][][] listBranchMasks = new int[18][3][3];
		
		// 0 - background, 1 - foreground, 2 - don´t care
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
		
		int[][] dot = new int[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } };
		
		int width = skelImg.length;
		int height = skelImg[0].length;
		int[][] imgbin;
		
		imgbin = rgbToBinaryArray(skelImg);
		skelImg = binaryArrayToRgb(imgbin);
		
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
					
					// delete Dots
					if (matchMask3x3(dot, area))
						skelImg[x][y] = background;
					
					// endpoints
					for (int index = 0; index < listEndpointMasks.length; index++) {
						if (matchMask3x3(listEndpointMasks[index], area)) {
							skelImg[x][y] = colorEndpoints;
							endpoints.add(new Point(x, y));
						}
					}
					
					// branches
					for (int index = 0; index < listBranchMasks.length; index++) {
						if (matchMask3x3(listBranchMasks[index], area)) {
							skelImg[x][y] = colorBranches;
							branches.add(new Point(x, y));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Not Mask based
	 */
	public void findEndpointsAndBranches2() {
		int width = skelImg.length;
		int height = skelImg[0].length;
		
		int[][] imgbin = rgbToBinaryArray(skelImg);
		skelImg = binaryArrayToRgb(imgbin);
		
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				if (imgbin[x][y] != 0) {
					int[][] area = new int[][] { { imgbin[x - 1][y - 1], imgbin[x][y - 1], imgbin[x + 1][y - 1] },
							{ imgbin[x - 1][y], imgbin[x][y], imgbin[x + 1][y] },
							{ imgbin[x - 1][y + 1], imgbin[x][y + 1], imgbin[x + 1][y + 1] } };
					int numOfNeigbours = getNumberOfNeighbours(area);
					if (numOfNeigbours < 1)
						skelImg[x][y] = background;
					if (numOfNeigbours == 1)
						skelImg[x][y] = colorEndpoints;
					if (numOfNeigbours > 2)
						skelImg[x][y] = colorBranches;
				}
			}
		}
	}
	
	private int getNumberOfNeighbours(int[][] inp) {
		int res = 0;
		for (int x = 0; x < inp.length; x++) {
			for (int y = 0; y < inp[0].length; y++) {
				if (inp[x][y] != 0)
					res++;
			}
		}
		
		return res - 1;
	}
	
	public void removeBurls() {
		
		int[][] burl1 = new int[][] { { colorBranches, foreground, background }, { foreground, background, foreground },
				{ background, foreground, colorBranches } };
		int[][] repburl1 = new int[][] { { foreground, background, background }, { background, foreground, background }, { background, background, foreground } };
		int width = skelImg.length;
		int height = skelImg[0].length;
		
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				if (skelImg[x][y] != background) {
					int[][] area = new int[][] { { skelImg[x - 1][y - 1], skelImg[x][y - 1], skelImg[x + 1][y - 1] },
							{ skelImg[x - 1][y], skelImg[x][y], skelImg[x + 1][y] },
							{ skelImg[x - 1][y + 1], skelImg[x][y + 1], skelImg[x + 1][y + 1] } };
					
					// delete and replace burl1
					if (matchMask3x3(burl1, area))
						replace(x, y, repburl1);
				}
			}
		}
	}
	
	private void replace(int x, int y, int[][] mask) {
		skelImg[x - 1][y - 1] = mask[0][0];
		skelImg[x][y - 1] = mask[1][0];
		skelImg[x + 1][y - 1] = mask[2][0];
		skelImg[x - 1][y] = mask[0][1];
		skelImg[x][y] = mask[1][1];
		skelImg[x + 1][y] = mask[2][1];
		skelImg[x - 1][y + 1] = mask[0][2];
		skelImg[x][y + 1] = mask[1][2];
		skelImg[x + 1][y + 1] = mask[2][2];
	}
	
	/**
	 * delete all endlimbs which are shorter than thresh
	 * 
	 * @param threshold
	 *           - average of n percent smallest endlimbs
	 * @param repeat
	 */
	@Deprecated
	public void deleteShortEndLimbs(int threshold, int repeat) {
		for (int i = 0; i < repeat; i++) {
			calculateEndlimbsRecursive();
			int autothreshold = getAutoThresh(threshold / (double) 100);
			// System.out.println("thresh: " + autothreshold);
			deleteShortEndLimbs(autothreshold);
		}
	}
	
	/**
	 * First try to connect, than delete
	 * 
	 * @param threshold
	 */
	public void deleteShortEndLimbs(int threshold) {
		int n = 0;
		do {
			// do {
			calculateEndlimbsRecursive();
			// } while (connectSkeleton() && n < 1000);
			int autothreshold = getAutoThresh(threshold / (double) 100);
			boolean goRecursive = false;
			do {
				goRecursive = false;
				// System.out.println("numofendlimbs: " + endlimbs.size());
				
				for (Limb l : endlimbs) {
					if (l.length() < autothreshold) {
						forRemove.add(l);
						goRecursive = true;
						// System.out.println("del");
					}
				}
				totalRefreshSkeleton();
			} while (goRecursive);
			calculateEndlimbsRecursive();
			n++;
		} while (connectSkeleton() && n < 1000);
	}
	
	/**
	 * @return Average of the smallest n percent
	 */
	private int getAutoThresh(double n) {
		double res = 0;
		int size = endlimbs.size();
		int[] lengths = new int[size];
		int idx = 0;
		for (Limb l : endlimbs) {
			lengths[idx] = l.points.size();
			idx++;
		}
		
		idx = 0;
		java.util.Arrays.sort(lengths);
		if (lengths.length > 0)
			for (int i = lengths.length - 1; i < lengths.length; i++) {
				idx++;
				res += lengths[i];
			}
		// return (int) (res / (double) idx);
		return (int) (res * n);
	}
	
	/**
	 * Removed all limbs saved in forRemove, reset the skeleton and recalculate all end- and branchpoints.
	 */
	private void totalRefreshSkeleton() {
		if (forRemove.size() > 0) {
			for (int index = 0; index < forRemove.size(); index++) {
				for (int index2 = 0; index2 < forRemove.get(index).points.size(); index2++) {
					Point point = forRemove.get(index).points.get(index2);
					int x = point.x;
					int y = point.y;
					if (debug)
						skelImg[x][y] = colorDebug;
					else
						skelImg[x][y] = background;
				}
				if (debug)
					skelImg[forRemove.get(index).endpoint.x][forRemove.get(index).endpoint.y] = colorDebug;
				else
					skelImg[forRemove.get(index).endpoint.x][forRemove.get(index).endpoint.y] = background;
				endlimbs.remove(forRemove.get(index));
			}
		}
		forRemove.clear();
		endpoints.clear();
		branches.clear();
		findEndpointsAndBranches();
	}
	
	/**
	 * remark all end- and branchpoints
	 */
	private void basicRefreshSkeleton() {
		// System.out.println("endpoints ref: " + endpoints.size());
		for (Point p : endpoints) {
			skelImg[p.x][p.y] = colorEndpoints;
		}
		for (Point p : branches) {
			skelImg[p.x][p.y] = colorBranches;
		}
	}
	
	private boolean matchMask3x3(int[][] mask, int[][] img) {
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (img[x][y] != -16777216)
					// System.out.println(img[x][y]);
					if (mask[x][y] != 2 && mask[x][y] != img[x][y])
						return false;
			}
		}
		return true;
	}
	
	public void print(String title, boolean show) {
		new FlexibleImage(this.skelImg).print(title, show);
	}
	
	public FlexibleImage getAsFlexibleImage() {
		return new FlexibleImage(this.skelImg);
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
		skelImg[inp.x][inp.y] = color;
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
	
	/**
	 * Iterative Version
	 * Only search Limbs which connected to an endpoint, stops on next branch or endpoint
	 * 
	 * @return
	 */
	@Deprecated
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
	
	@Deprecated
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
	
	/**
	 * Return Pixel in the 8 - neighbourhood, if pixel is no endpoint, background or pixel who is already visited.
	 * Branchpoints in 8 - neighbourhood will be returned.
	 * 
	 * @return
	 */
	@Deprecated
	private Point getNeighbour(Point inp) {
		Point res = null;
		Point branchInNeighbourhood = null;
		if (inp.x > 0 && inp.x < this.skelImg.length && inp.y > 0 && inp.y < this.skelImg[0].length) {
			
			if (this.skelImg[inp.x - 1][inp.y - 1] != background && this.skelImg[inp.x - 1][inp.y - 1] != colorMarkedEndLimbs
					&& this.skelImg[inp.x - 1][inp.y - 1] != colorEndpoints) {
				if (this.skelImg[inp.x - 1][inp.y - 1] != colorBranches) {
					res = new Point(inp.x - 1, inp.y - 1);
				} else
					branchInNeighbourhood = new Point(inp.x - 1, inp.y - 1);
			}
			if (this.skelImg[inp.x + 1][inp.y + 1] != background && this.skelImg[inp.x + 1][inp.y + 1] != colorMarkedEndLimbs
					&& this.skelImg[inp.x + 1][inp.y + 1] != colorEndpoints) {
				if (this.skelImg[inp.x + 1][inp.y + 1] != colorBranches) {
					res = new Point(inp.x + 1, inp.y + 1);
				} else
					branchInNeighbourhood = new Point(inp.x + 1, inp.y + 1);
			}
			if (this.skelImg[inp.x + 1][inp.y - 1] != background && this.skelImg[inp.x + 1][inp.y - 1] != colorMarkedEndLimbs
					&& this.skelImg[inp.x + 1][inp.y - 1] != colorEndpoints) {
				if (this.skelImg[inp.x + 1][inp.y - 1] != colorBranches) {
					res = new Point(inp.x + 1, inp.y - 1);
				} else
					branchInNeighbourhood = new Point(inp.x + 1, inp.y - 1);
			}
			if (this.skelImg[inp.x - 1][inp.y + 1] != background && this.skelImg[inp.x - 1][inp.y + 1] != colorMarkedEndLimbs
					&& this.skelImg[inp.x - 1][inp.y + 1] != colorEndpoints) {
				if (this.skelImg[inp.x - 1][inp.y + 1] != colorBranches) {
					res = new Point(inp.x - 1, inp.y + 1);
				} else
					branchInNeighbourhood = new Point(inp.x - 1, inp.y + 1);
			}
			if (this.skelImg[inp.x][inp.y - 1] != background && this.skelImg[inp.x][inp.y - 1] != colorMarkedEndLimbs
					&& this.skelImg[inp.x][inp.y - 1] != colorEndpoints) {
				if (this.skelImg[inp.x][inp.y - 1] != colorBranches) {
					res = new Point(inp.x, inp.y - 1);
				} else
					branchInNeighbourhood = new Point(inp.x, inp.y - 1);
			}
			if (this.skelImg[inp.x - 1][inp.y] != background && this.skelImg[inp.x - 1][inp.y] != colorMarkedEndLimbs
					&& this.skelImg[inp.x - 1][inp.y] != colorEndpoints) {
				if (this.skelImg[inp.x - 1][inp.y] != colorBranches) {
					res = new Point(inp.x - 1, inp.y);
				} else
					branchInNeighbourhood = new Point(inp.x - 1, inp.y);
			}
			if (this.skelImg[inp.x + 1][inp.y] != background && this.skelImg[inp.x + 1][inp.y] != colorMarkedEndLimbs
					&& this.skelImg[inp.x + 1][inp.y] != colorEndpoints) {
				if (this.skelImg[inp.x + 1][inp.y] != colorBranches) {
					res = new Point(inp.x + 1, inp.y);
				} else
					branchInNeighbourhood = new Point(inp.x + 1, inp.y);
			}
			if (this.skelImg[inp.x][inp.y + 1] != background && this.skelImg[inp.x][inp.y + 1] != colorMarkedEndLimbs
					&& this.skelImg[inp.x][inp.y + 1] != colorEndpoints) {
				if (this.skelImg[inp.x][inp.y + 1] != colorBranches) {
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
}
