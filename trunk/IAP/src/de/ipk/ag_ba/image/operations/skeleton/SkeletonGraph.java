package de.ipk.ag_ba.image.operations.skeleton;

import java.awt.Color;
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
	private final int w;
	private final int h;
	private final int[][] skelImg;
	FlexibleImage debugImg;
	private AdjListGraph graph;
	private final int background = -16777216;
	private final int visitedDuringSearch = Color.GRAY.getRGB();
	
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
				if (p == SkeletonProcessor2d.colorEndpoints || p == SkeletonProcessor2d.colorBranches
							|| p == SkeletonProcessor2d.colorBloom) {
					nPoints++;
					Node n = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(x, y));
					for (int xd = -1; xd <= 1; xd++)
						for (int yd = -1; yd <= 1; yd++) {
							position2node.put((x + xd) + ";" + (y + yd), n);
						}
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
				if (p == SkeletonProcessor2d.colorEndpoints || p == SkeletonProcessor2d.colorBranches) {
					for (int xd = -1; xd <= 1; xd++)
						for (int yd = -1; yd <= 1; yd++) {
							p = skelImg[x + xd][y + yd];
							if (p != SkeletonProcessor2d.background && !(xd == 0 && yd == 0)) {
								Vector2i startPoint = new Vector2i(x, y);
								System.out.println("Start: " + startPoint);
								ArrayList<Vector2i> edgePoints = traverseAndClearLineStartingFrom(new Vector2i(x + xd, y + yd));
								edgePoints.add(0, startPoint);
								if (DEBUG)
									System.out.println(" " + startPoint + " // Path-Len: " + edgePoints.size());
								Vector2i s = edgePoints.get(0);
								Vector2i e = edgePoints.get(edgePoints.size() - 1);
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
			}
		}
		if (DEBUG)
			System.out.println("Skeletonimage: Marked Pixels: " + nPoints + " Edge Pixels: " + nEdgePoints);
		if (DEBUG)
			System.out.println("Skeletongraph: " + graph + " Nodes: " + graph.getNumberOfNodes() + " Edges: " + graph.getNumberOfEdges() + " Edge Pixels: "
					+ nGraphEdgePoints);
	}
	
	private void printMatrix(int[][] skelImg2, int x, int y) {
		String S = "";
		for (int yd = -2; yd <= 2; yd++) {
			for (int xd = -2; xd <= 2; xd++) {
				int p = skelImg2[x + xd][y + yd];
				String s = "";
				if (p == SkeletonProcessor2d.background)
					s = "---";
				else
					if (p == SkeletonProcessor2d.colorBranches)
						s = "BRA";
					else
						if (p == SkeletonProcessor2d.colorBloom)
							s = "BLO";
						else
							if (p == SkeletonProcessor2d.colorEndpoints)
								s = "END";
							else
								if (p == SkeletonProcessor2d.colorMarkedEndLimbs)
									s = "LEA";
								else
									if (p == SkeletonProcessor2d.foreground)
										s = "FOR";
									else
										if (p == visitedDuringSearch)
											s = "111";
										else {
											Color r = new Color(p);
											s = r.getRed() + "-" + r.getGreen() + "-" + r.getBlue() + " ";
										}
				System.out.print(s + " ");
				if (xd == 0 && yd == 0)
					S = s;
			}
			System.out.println();
		}
		System.out.println("^^^ " + S + " XY: " + x + " " + y);
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
			if (n > 3)
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
				// printMatrix(skelImg, x, y);
				if (skelImg[x][y] != SkeletonProcessor2d.colorBranches && skelImg[x][y] != SkeletonProcessor2d.colorEndpoints)
					skelImg[x][y] = background;
				search: for (Boolean allow9 : new Boolean[] { false, true })
					for (int xd = -1; xd <= 1; xd++)
						for (int yd = -1; yd <= 1; yd++) {
							if (xd == 0 && yd == 0)
								continue;
							if (!allow9 && xd * xd + yd * yd == 2)
								continue;
							if (x + xd < 0 || y + yd < 0 || x + xd >= w || y + yd >= h)
								continue;
							if (skelImg[x + xd][y + yd] != background) {
								skelImg[x + xd][y + yd] = visitedDuringSearch;
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
