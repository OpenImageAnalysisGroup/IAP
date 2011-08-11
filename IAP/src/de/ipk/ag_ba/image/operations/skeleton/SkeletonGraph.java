package de.ipk.ag_ba.image.operations.skeleton;

import java.util.ArrayList;
import java.util.HashMap;

import org.AttributeHelper;
import org.Vector2i;
import org.graffiti.attributes.ObjectAttribute;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class SkeletonGraph {
	private static final boolean DEBUG = true;
	private int w;
	private int h;
	private int[][] skelImg;
	FlexibleImage debugImg;
	private AdjListGraph graph;
	private int background = -16777216;
	
	public SkeletonGraph(int w, int h, int[][] skelImg) {
		this.w = w;
		this.h = h;
		this.skelImg = skelImg;
	}
	
	public void createGraph() {
		this.graph = new AdjListGraph();
		int nPoints = 0;
		HashMap<String, Node> position2node = new HashMap<String, Node>();
		for (int x = 1; x < w - 1; x++) {
			for (int y = 1; y < h - 1; y++) {
				int p = skelImg[x][y];
				if (p == SkeletonProcessor2d.colorEndpoints || p == SkeletonProcessor2d.colorBranches) {
					nPoints++;
					Node n = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(x, y));
					position2node.put(x + ";" + y, n);
					System.out.println("MEM: " + x + " // " + y);
				}
			}
		}
		int nEdgePoints = 0;
		for (int x = 1; x < w - 1; x++) {
			for (int y = 1; y < h - 1; y++) {
				int p = skelImg[x][y];
				if (p != SkeletonProcessor2d.background) {
					nEdgePoints++;
				}
			}
		}
		if (DEBUG) {
			FlexibleImage fi = new FlexibleImage(skelImg).print("TO BE ANALYZED...");
			
			debugImg = fi.copy();
		}
		if (DEBUG)
			debugImg.print("MARKED POINTS");
		int nGraphEdgePoints = 0;
		for (int x = 1; x < w - 1; x++) {
			for (int y = 1; y < h - 1; y++) {
				int p = skelImg[x][y];
				if (p != background && p != 1 && p != SkeletonProcessor2d.colorEndpoints && p != SkeletonProcessor2d.colorBranches) {
					Vector2i startPoint = searchOneEndPointFromThisPointOn(x, y);
					ArrayList<Vector2i> edgePoints = traverseAndClearLineStartingFrom(startPoint);
					if (DEBUG)
						System.out.println("Start: " + startPoint + " // Path-Len: " + edgePoints.size());
					Vector2i s = edgePoints.get(0);
					Vector2i e = edgePoints.get(edgePoints.size() - 1);
					if (DEBUG) {
						// debugImg = debugImg.getIO().getCanvas().fillRect(s.x - 2, s.y - 2, 4, 4, Color.PINK.getRGB()).getImage();
						// debugImg = debugImg.getIO().getCanvas().fillRect(e.x - 2, e.y - 2, 4, 4, Color.PINK.getRGB()).getImage();
					}
					Node startNode = position2node.get(s.x + ";" + s.y);
					Node endNode = position2node.get(e.x + ";" + e.y);
					if (DEBUG)
						System.out.println("S: " + s + " ==> E: " + e + " //// " + startNode + " // " + endNode + " // "
								+ (startNode == null || endNode == null ? "NULL" : ""));
					if (startNode != null && endNode != null) {
						Edge edge = graph.addEdge(startNode, endNode, true);
						ObjectAttribute oa = new ObjectAttribute("points");
						oa.setValue(edgePoints);
						edge.addAttribute(oa, "");
						nGraphEdgePoints += edgePoints.size();
						edge.setDouble("len", edgePoints.size());
					}
				}
			}
		}
		if (DEBUG)
			System.out.println("Skeletonimage: Marked Pixels: " + nPoints + " Edge Pixels: " + nEdgePoints);
		if (DEBUG)
			System.out.println("Skeletongraph: " + graph + " Nodes: " + graph.getNumberOfNodes() + " Edges: " + graph.getNumberOfEdges() + " Edge Pixels: "
					+ nGraphEdgePoints);
	}
	
	private Vector2i searchOneEndPointFromThisPointOn(int x, int y) {
		boolean found, stop;
		System.out.println("START: " + x + ";" + y);
		if (x == 957 && y == 617) {
			System.out.println("PROBLEM START");
		}
		do {
			found = false;
			stop = false;
			if (DEBUG)
				printMatrix(skelImg, x, y);
			searchBranchOrEndPoint: for (int xd = -1; xd <= 1; xd++)
				for (int yd = -1; yd <= 1; yd++) {
					if (x + xd < 0 || y + yd < 0 || x + xd >= w || y + yd >= h)
						continue;
					if (skelImg[x + xd][y + yd] == SkeletonProcessor2d.colorBranches || skelImg[x + xd][y + yd] == SkeletonProcessor2d.colorEndpoints) {
						stop = true;
						x = x + xd;
						y = y + yd;
						found = true;
						break searchBranchOrEndPoint;
					}
				}
			if (!stop)
				for (int xd = -1; xd <= 1; xd++)
					for (int yd = -1; yd <= 1; yd++) {
						if (xd == 0 && yd == 0)
							continue;
						if (x + xd < 0 || y + yd < 0 || x + xd >= w || y + yd >= h)
							continue;
						if (skelImg[x + xd][y + yd] != background && skelImg[x + xd][y + yd] != 1) {
							x = x + xd;
							y = y + yd;
							skelImg[x][y] = 1;
							found = true;
							break;
						}
					}
		} while (found && !stop);
		if (skelImg[x][y] != SkeletonProcessor2d.colorBranches && skelImg[x][y] != SkeletonProcessor2d.colorEndpoints) {
			System.out.println("ERROR: NO BRANCH OR END AT " + x + ";" + y);
		}
		return new Vector2i(x, y);
	}
	
	private void printMatrix(int[][] skelImg2, int x, int y) {
		for (int yd = -2; yd <= 2; yd++) {
			for (int xd = -2; xd <= 2; xd++) {
				int p = skelImg2[x + xd][y + yd];
				if (p == SkeletonProcessor2d.background)
					System.out.print("- ");
				else
					if (p == SkeletonProcessor2d.colorBranches)
						System.out.print("X ");
					else
						if (p == SkeletonProcessor2d.colorEndpoints)
							System.out.print("E ");
						else
							if (p == SkeletonProcessor2d.colorBranches)
								System.out.print("* ");
							else
								if (p == SkeletonProcessor2d.colorMarkedEndLimbs)
									System.out.print("# ");
								else
									if (p == 1)
										System.out.print("1 ");
									else
										System.out.print(p + " ");
			}
			System.out.println();
		}
	}
	
	private ArrayList<Vector2i> traverseAndClearLineStartingFrom(Vector2i startPoint) {
		ArrayList<Vector2i> result = new ArrayList<Vector2i>();
		result.add(startPoint);
		int x = startPoint.x;
		int y = startPoint.y;
		boolean found, stop;
		int n = 0;
		do {
			found = false;
			stop = false;
			n++;
			if (n > 2)
				for (int xd = -1; xd <= 1; xd++)
					for (int yd = -1; yd <= 1; yd++) {
						if (x + xd < 0 || y + yd < 0 || x + xd >= w || y + yd >= h)
							continue;
						if (skelImg[x + xd][y + yd] == SkeletonProcessor2d.colorBranches || skelImg[x + xd][y + yd] == SkeletonProcessor2d.colorEndpoints) {
							stop = true;
							result.add(new Vector2i(x + xd, y + yd));
							found = true;
							break;
						}
					}
			if (!stop) {
				if (skelImg[x][y] != SkeletonProcessor2d.colorBranches)
					skelImg[x][y] = background;
				search: for (int xd = -1; xd <= 1; xd++)
					for (int yd = -1; yd <= 1; yd++) {
						if (xd == 0 && yd == 0)
							continue;
						if (x + xd < 0 || y + yd < 0 || x + xd >= w || y + yd >= h)
							continue;
						if (skelImg[x + xd][y + yd] != background) {
							skelImg[x + xd][y + yd] = 1;
							result.add(new Vector2i(x + xd, y + yd));
							x = x + xd;
							y = y + yd;
							found = true;
							break search;
						}
					}
			}
		} while (found && !stop);
		return result;
	}
}
