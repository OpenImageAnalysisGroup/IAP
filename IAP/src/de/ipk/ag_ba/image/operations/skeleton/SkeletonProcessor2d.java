package de.ipk.ag_ba.image.operations.skeleton;

import iap.blocks.data_structures.RunnableOnImage;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

public class SkeletonProcessor2d {
	
	static final int colorBloom = Color.GREEN.getRGB();
	public int[][] skelImg;
	public ArrayList<Point> endpoints = new ArrayList<Point>();
	public ArrayList<Point> branches = new ArrayList<Point>();
	public ArrayList<Limb> endlimbs = new ArrayList<Limb>();
	
	ArrayList<Limb> forRemove = new ArrayList<Limb>();
	
	public static final int colorEndpoints = Color.PINK.getRGB();
	public static final int colorBranches = Color.RED.getRGB();
	public static final int colorMarkedEndLimbs = Color.BLUE.getRGB();
	public static final int foreground = Color.MAGENTA.getRGB();
	public int background = getDefaultBackground();
	public static final int colorDebug = Color.GREEN.getRGB();
	
	public boolean debug = false;
	public static int colorBloomEndpoint = Color.CYAN.getRGB();
	
	public static String getColorDesc(int color) {
		if (color == colorEndpoints)
			return "endpoint";
		if (color == colorBranches)
			return "branch";
		if (color == colorMarkedEndLimbs)
			return "endLimb";
		if (color == foreground)
			return "foreground";
		if (color == colorDebug)
			return "debug";
		if (color == colorBloomEndpoint)
			return "bloomEndPoint";
		if (color == getDefaultBackground())
			return "background";
		return "invalid color";
	}
	
	public SkeletonProcessor2d(Image inp) {
		this.skelImg = inp.getAs2A();
	}
	
	public SkeletonProcessor2d(int[][] image) {
		this.skelImg = image;
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
			res.setCut(true);
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
	
	// /**
	// * Method to detect the clade: take the lowest end-point(min y), calculate way to any endpoint
	// * -> based on the assumption - the way which contains max number of branches shall be the clade
	// */
	// public void findTrailWithMaxBranches() {
	// ArrayList<Limb> trails = new ArrayList<Limb>();
	// Point p = getLowest(endpoints);
	// Limb res = new Limb(p);
	// Point actual = p;
	// calcrecAllBranches(actual, res);
	// }
	
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
	
	// private void calcrecAllBranches(Point actual, Limb res) {
	// Point neighbour = getNeighbour(actual);
	// boolean ok = false;
	// if (neighbour == null) {
	// res.setInitialpoint(actual);
	// ok = true;
	// }
	// if (isBranch(neighbour)) {
	// res.addPoint(neighbour);
	// ArrayList<Point> temp = getNeighbours(neighbour);
	// mark(neighbour, colorMarkedEndLimbs);
	// for (Point p : temp) {
	// calcrecAllBranches(p, res);
	// }
	// }
	// if (isEndpoint(neighbour)) {
	// res.setInitialpoint(neighbour);
	// endlimbs.add(res);
	// ok = true;
	// }
	// if (neighbour != null && !(isBranch(neighbour)) && !ok) {
	// res.addPoint(neighbour);
	// mark(neighbour, colorMarkedEndLimbs);
	// calcrecAllBranches(neighbour, res);
	// }
	// }
	
	public boolean connectSkeleton() {
		boolean added = false;
		for (Limb l : endlimbs) {
			if (l.isCut()) {
				// System.out.println("connecting limbs: " + l.endpoint.toString() + " , " + l.initialpoint.toString());
				Point[] toConnect = getNearestLimb(l);
				if (toConnect == null || toConnect.length == 0 || toConnect[0] == null || toConnect[1] == null)
					continue;
				connect(toConnect[0], toConnect[1], colorMarkedEndLimbs, l);
				added = true;
			}
		}
		return added;
	}
	
	private Point[] getNearestLimb(Limb l) {
		// missing improvement: first sort y, pre/succ calc dist -> O(nlogn)
		int dist = Integer.MAX_VALUE;
		Point ini = l.initialpoint;
		Point res = null;
		for (Point p : endpoints) {
			if (ini.distance(p) < dist && !l.initialOrEndpoint(p)) {
				res = p;
				dist = (int) ini.distance(p);
			}
		}
		for (Point p : branches) {
			if (ini.distance(p) < dist && !l.initialOrEndpoint(p)) {
				res = p;
				dist = (int) ini.distance(p);
			}
		}
		
		boolean endok = false;
		Point end = l.endpoint;
		for (Point p : endpoints) {
			if (end.distance(p) < dist && !l.initialOrEndpoint(p)) {
				res = p;
				dist = (int) end.distance(p);
				endok = true;
			}
		}
		for (Point p : branches) {
			if (end.distance(p) < dist && !l.initialOrEndpoint(p)) {
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
		if (initialpoint == null)
			return;
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
	
	public void createEndpointsAndBranchesLists() {
		createEndpointsAndBranchesLists(null);
	}
	
	/**
	 * Do this at first!
	 * Search and mark Endpoints and Branches, therefore the image will be converted to an binary image and checked with masks.
	 * Also remove Dots.
	 * 
	 * @param postProcessing
	 */
	public void createEndpointsAndBranchesLists(ArrayList<RunnableOnImage> postProcessing) {
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
		
		int[][] imgbin = rgbToBinaryArray(skelImg);
		// skelImg = binaryArrayToRgb(imgbin);
		
		if (endpoints != null)
			endpoints.clear();
		
		if (branches != null)
			branches.clear();
		
		int[][] area = new int[3][3];
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				if (imgbin[x][y] != 0) {
					area[0][0] = imgbin[x - 1][y - 1];
					area[0][1] = imgbin[x][y - 1];
					area[0][2] = imgbin[x + 1][y - 1];
					area[1][0] = imgbin[x - 1][y];
					area[1][1] = imgbin[x][y];
					area[1][2] = imgbin[x + 1][y];
					area[2][0] = imgbin[x - 1][y + 1];
					area[2][1] = imgbin[x][y + 1];
					area[2][2] = imgbin[x + 1][y + 1];
					
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
		if (postProcessing != null) {
			final int[][] unchangedSkeleton = skelImg.clone();
			postProcessing.add(new RunnableOnImage() {
				@Override
				public Image postProcess(Image in) {
					return new ImageOperation(unchangedSkeleton).replaceColor(Color.black.getRGB(),
							ImageOperation.BACKGROUND_COLORint).or(in).getImage().show("result skeleton", debug);
				}
				
			});
		}
	}
	
	/**
	 * Not Mask based
	 */
	public void markEndpointsAndBranches() {
		int width = skelImg.length;
		int height = skelImg[0].length;
		
		int[][] imgbin = new Image(skelImg).copy().getAs2A();
		// new Image(imgbin).show("bin");
		// skelImg = binaryArrayToRgb(imgbin);
		
		int back = ImageOperation.BACKGROUND_COLORint;
		
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				if (imgbin[x][y] != back) {
					int numOfNeigbours = 0;
					if (imgbin[x - 1][y - 1] != back)
						numOfNeigbours++;
					if (imgbin[x][y - 1] != back)
						numOfNeigbours++;
					if (imgbin[x + 1][y - 1] != back)
						numOfNeigbours++;
					if (imgbin[x - 1][y] != back)
						numOfNeigbours++;
					if (imgbin[x + 1][y] != back)
						numOfNeigbours++;
					if (imgbin[x - 1][y + 1] != back)
						numOfNeigbours++;
					if (imgbin[x][y + 1] != back)
						numOfNeigbours++;
					if (imgbin[x + 1][y + 1] != back)
						numOfNeigbours++;
					if (numOfNeigbours < 1)
						skelImg[x][y] = back;
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
	
	/**
	 * Make sure that the input skeleton is colorized as foreground (option skeleton-processor)
	 * First try to connect, than delete
	 * 
	 * @param threshold
	 */
	public void deleteShortEndLimbs(int threshold, boolean simulate, HashSet<Point> knownBlooms) {
		int n = 0;
		boolean repeat = threshold > 0;
		do {
			removeShortEndLimbsUpdateCrossingsAndEndpoints();
			calculateEndlimbsRecursive();
			int autothreshold = getAutoThresh(threshold / (double) 100);
			boolean goRecursive = false;
			do {
				goRecursive = false;
				// System.out.println("numofendlimbs1: " + endlimbs.size() + " auto: " + autothreshold);
				
				for (Limb l : endlimbs) {
					if (((threshold < 0 && l.length() < -threshold) ||
							(l.length() < autothreshold)
							) && !knownBlooms.contains(l.endpoint)) {
						if (!simulate && !forRemove.contains(l)) {
							forRemove.add(l);
							goRecursive = true;
							// System.out.println("del");
						}
					}
				}
				removeShortEndLimbsUpdateCrossingsAndEndpoints();
			} while (goRecursive);
			calculateEndlimbsRecursive();
			// System.out.println("numofendlimbs2: " + endlimbs.size());
			n++;
		} while (connectSkeleton() && n < 100 && repeat);
	}
	
	/**
	 * @return Average of the smallest n percent
	 */
	private int getAutoThresh(double n) {
		if (n < 0)
			return (int) -n;
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
	private void removeShortEndLimbsUpdateCrossingsAndEndpoints() {
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
		createEndpointsAndBranchesLists();
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
				// if (img[x][y] != 0)
				// System.out.println(img[x][y]);
				if (mask[x][y] != 2 && mask[x][y] != img[x][y])
					return false;
			}
		}
		return true;
	}
	
	public void show(String title, boolean show) {
		new Image(this.skelImg).copy().show(title, show);
	}
	
	public Image getAsImage() {
		return new Image(this.skelImg);
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
				
				if (input[x][y] == background)// || input[x][y] == -1)
					res[x][y] = 0;
				else {
					res[x][y] = 1;
				}
			}
		}
		return res;
	}
	
	public Image copyONOriginalImage(Image vis) {
		int[][] plantImg = vis.getAs2A();
		int w = Math.min(skelImg.length, plantImg.length);
		int h = Math.min(skelImg[0].length, plantImg[0].length);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int v = skelImg[x][y];
				if (v != background) {
					int r = 2;
					if (v == colorEndpoints)
						r = 18;
					if (v == colorBranches)
						r = 2;
					if (v == colorBloomEndpoint)
						r = 20;
					for (int diffX = -r; diffX < r; diffX++)
						for (int diffY = -r; diffY < r; diffY++) {
							if ((v == colorEndpoints || v == colorBloomEndpoint) &&
									((diffX * diffX + diffY * diffY) <= 12 * 12)) // ||
								// (diffX * diffX + diffY * diffY) >= 20 * 20)
								continue;
							if (x - diffX > 0 && y - diffY > 0 && x - diffX < w && y - diffY < h)
								plantImg[x - diffX][y - diffY] = v;// avg(v, plantImg[index - diffX + w * diffY]);
						}
				}
			}
		}
		return new Image(plantImg);
	}
	
	public HashSet<Point> detectBloom(Image vis, Image fluo, double xf, double yf) {
		ArrayList<Limb> topLimbs = getTopEndlimbs(1);
		ArrayList<Limb> bloomLimbs = new ArrayList<Limb>();
		
		double maxLen = getMaxLimbLength();
		Limb minY = getMinLimbY();
		if (topLimbs != null)
			for (Limb l : topLimbs) {
				if (l == null || l.endpoint == null || l.points.size() < 5)
					continue;
				if (checkBamboEnd(l, vis, xf, yf)) {
					markLimb(l, background);
					endlimbs.remove(l);
				} else {
					if (l.endpoint.y < minY.endpoint.y + 0.3 * vis.getHeight())
						if (l.points.size() < maxLen * 0.4d)
							if (checkBloomColorOnFluo(l, fluo, xf, yf) && checkBloomColorOnVis(l, vis, xf, yf))
								bloomLimbs.add(l);
				}
			}
		// double avgDist = avgDistBetweenLimbs(bloomLimbs);
		HashSet<Point> res = new HashSet<Point>();
		for (Limb l : bloomLimbs) {
			markLimb(l, colorBloom);
			skelImg[l.endpoint.x][l.endpoint.y] = colorBloomEndpoint;
			res.add(l.endpoint);
		}
		return res;
	}
	
	private boolean checkBamboEnd(Limb l, Image vis, double xf, double yf) {
		if (vis == null || l == null || l.points == null || vis.getHeight() <= 0)
			return false;
		
		int[][] visImg = vis.getAs2A();
		int r = 0, g, b;
		
		int c = visImg[l.endpoint.x][l.endpoint.y];
		
		r = ((c & 0xff0000) >> 16); // R 0..1
		g = ((c & 0x00ff00) >> 8); // G 0..1
		b = (c & 0x0000ff); // B 0..1
		
		if (r == 0 && g == 0 && b == 254)
			return true;
		else
			return false;
	}
	
	private boolean checkBloomColorOnVis(Limb l, Image vis, double xf, double yf) {
		if (vis == null || l == null || l.points == null)
			return false;
		
		int[][] visImg = vis.getAs2A();
		double lSum = 0, aSum = 0, bSum = 0;
		int r = 0, g, b, n = 0;
		float li, ai, bi;
		float[][][] lab = ImageOperation.getLabCubeInstance();
		for (Point p : l.points) {
			int c = visImg[p.x][p.y];
			
			if (c == ImageOperation.BACKGROUND_COLORint)
				continue;
			r = ((c & 0xff0000) >> 16); // R 0..1
			g = ((c & 0x00ff00) >> 8); // G 0..1
			b = (c & 0x0000ff); // B 0..1
			
			li = lab[r][g][b];
			ai = lab[r][g][b + 256];
			bi = lab[r][g][b + 512];
			
			lSum += li;
			aSum += ai;
			bSum += bi;
			n++;
		}
		lSum = lSum / n;
		aSum = aSum / n;
		bSum = bSum / n;
		// System.out.println("lsum: " + lSum + " aSum: " + aSum + " bSum: " + bSum);
		if (aSum < 135 && aSum > 100 && bSum > 127 && lSum > 100)
			return true;
		else
			return false;
	}
	
	private boolean checkBloomColorOnFluo(Limb l, Image fluo, double xf, double yf) {
		if (fluo == null || l == null || l.points == null)
			return false;
		int[][] visImg = fluo.getAs2A();
		if (visImg == null)
			return false;
		int b;
		double green = 0;
		int greenMax = 0;
		for (Point p : l.points) {
			int c = visImg[(int) (p.x * xf)][(int) (p.y * yf)];
			
			b = (c & 0x0000ff); // B 0..1
			
			green += b;
			if (b > greenMax)
				greenMax = b;
		}
		green /= l.points.size();
		// Point p = l.endpoint;
		// int c = visImg[(int) (p.x * xf)][(int) (p.y * yf)];
		// b = (c & 0x0000ff); // B 0..1
		// green += b;
		// if (greenMax > 0)
		// System.out.println("green: " + green + ", max: " + greenMax + " l.length: " + l.points.size() + " end: " + l.endpoint.toString());
		if (green > 10 && greenMax > 20)
			return true;
		else
			return false;
	}
	
	private void markLimb(Limb l, int color) {
		for (Point p : l.points) {
			skelImg[p.x][p.y] = color;
		}
		
	}
	
	private ArrayList<Limb> getTopEndlimbs(double n) {
		int minY = Integer.MAX_VALUE;
		Limb res = getMinLimbY();
		ArrayList<Limb> maxLimbs = new ArrayList<Limb>();
		maxLimbs.add(res);
		// System.out.println("max endpoint: " + res.endpoint.toString());
		
		int border = (int) (minY + skelImg[0].length * n);
		
		for (Limb l : endlimbs) {
			if (l != res && l.endpoint.y < border)
				maxLimbs.add(l);
		}
		return maxLimbs;
	}
	
	public Image calcProbablyBloomImage(Image image, double hueBloom) {
		int[][] visImg = image.getAs2A();
		int h = image.getHeight();
		int w = image.getWidth();
		int r, g, b, c = 0;
		float distToYellow = 0f;
		float[] hsbvals = new float[3];
		int[][] res = new int[w][h];
		
		float avgHue = (float) hueBloom;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (y < 0 || y >= h)
					continue;
				c = visImg[x][y];
				if (c != ImageOperation.BACKGROUND_COLORint) {
					r = ((c & 0xff0000) >> 16); // R 0..1
					g = ((c & 0x00ff00) >> 8); // G 0..1
					b = (c & 0x0000ff); // B 0..1
					
					Color.RGBtoHSB(r, g, b, hsbvals);
					distToYellow = 255 - Math.abs((avgHue - hsbvals[0]) * 255f * 10f); // 0.167 equivalent to Yellow
				} else {
					distToYellow = 0;
				}
				if (distToYellow < 0)
					distToYellow = 0;
				if (distToYellow > 255)
					distToYellow = 255;
				
				res[x][y] = (0xFF << 24 | ((int) distToYellow & 0xFF) << 16) | (((int) distToYellow & 0xFF) << 8) | (((int) distToYellow & 0xFF) << 0);
			}
		}
		return new Image(res);
	}
	
	public Limb getMinLimbY() {
		int maxHeight = Integer.MAX_VALUE;
		Limb res = null;
		for (Limb l : endlimbs) {
			if (l.endpoint.y < maxHeight) {
				maxHeight = l.endpoint.y;
				res = l;
			}
		}
		return res;
	}
	
	private double getMaxLimbLength() {
		int max = 0;
		for (Limb l : endlimbs) {
			if (l.points.size() > max)
				max = l.points.size();
		}
		return max;
	}
	
	public ArrayList<Point> getBranches() {
		return branches;
	}
	
	public ArrayList<Point> getEndpoints() {
		return endpoints;
	}
	
	public static int getDefaultBackground() {
		return ImageOperation.BACKGROUND_COLORint;// Color.BLACK.getRGB();
	}
	
	public ImageOperation getImageOperation() {
		return new ImageOperation(skelImg);
	}
	
	public static int countEndPoints(Image skeletonImage) {
		SkeletonProcessor2d s = new SkeletonProcessor2d(skeletonImage);
		s.createEndpointsAndBranchesLists();
		return s.endpoints.size();
	}
}
