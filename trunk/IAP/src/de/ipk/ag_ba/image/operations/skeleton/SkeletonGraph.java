package de.ipk.ag_ba.image.operations.skeleton;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.AttributeHelper;
import org.SystemAnalysis;
import org.Vector2i;
import org.graffiti.attributes.ObjectAttribute;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class SkeletonGraph {
	private final boolean DEBUG = true;
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
	
	public SkeletonGraph(int w, int h, int[] skelImg1a) {
		this.w = w;
		this.h = h;
		this.skelImg = new FlexibleImage(w, h, skelImg1a).getAs2A();
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
					n.setInteger("x", x);
					n.setInteger("y", y);
					// for (int xd = -1; xd <= 2; xd++)
					// for (int yd = -2; yd <= 2; yd++) {
					// String key = (x + xd) + ";" + (y + yd);
					// if (!position2node.containsKey(key))
					// position2node.put(key, n);
					// }
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
		// new FlexibleImage(skelImg).copy().print("BEFORE AAAAAAAA");
		for (int x = 1; x < w - 1; x++) {
			for (int y = 1; y < h - 1; y++) {
				int p = skelImg[x][y];
				if (p == SkeletonProcessor2d.colorEndpoints || p == SkeletonProcessor2d.colorBranches) {
					Vector2i startPoint = new Vector2i(x, y);
					System.out.println("Start: " + startPoint);
					boolean foundLine;
					do {
						ArrayList<Vector2i> edgePoints = traverseAndClearLineStartingFromStartPoint(new Vector2i(x, y));
						foundLine = edgePoints.size() > 1;
						if (DEBUG)
							System.out.println(" " + startPoint + " // Path-Len: " + edgePoints.size());
						Vector2i s = edgePoints.get(0);
						Vector2i e = edgePoints.get(edgePoints.size() - 1);
						Node startNode = position2node.get(s.x + ";" + s.y);
						Node endNode = position2node.get(e.x + ";" + e.y);
						if (endNode == null) {
							System.out.println("END POINT NOT FOUND: " + e.x + " / " + e.y);
						}
						if (DEBUG)
							System.out.println("S: " + s + " ==> E: " + e + " //// " + startNode + " // " + endNode + " // "
									+ (startNode == null || endNode == null ? "NULL" : ""));
						if (startNode != null && endNode != null) {
							Edge edge = graph.addEdge(startNode, endNode, true);
							ObjectAttribute oa = new ObjectAttribute("info");
							oa.setValue(new LimbInfo(edgePoints));
							edge.addAttribute(oa, "");
							nGraphEdgePoints += edgePoints.size();
							nGraphEdgePoints--;// either source or target pixel is not really part of the edge
							edge.setDouble("len", edgePoints.size());
							
						}
					} while (foundLine);
				}
			}
		}
		boolean checkDist = false;
		if (checkDist)
			for (Node n1 : graph.getNodes()) {
				for (Node n2 : graph.getNodes()) {
					if (n1 == n2)
						continue;
					int x1 = n1.getInteger("x");
					int y1 = n1.getInteger("y");
					int x2 = n2.getInteger("x");
					int y2 = n2.getInteger("y");
					Vector2i a = new Vector2i(x1, y1);
					Vector2i b = new Vector2i(x2, y2);
					if (a.distance(b) < 5)
						System.out.println("NNN: " + x1 + "/" + y1 + " near to " + x2 + "/" + y2);
				}
			}
		// new FlexibleImage(skelImg).copy().print("AFTER AAAAAAAA");
		if (DEBUG)
			System.out.println("Skeletonimage: Marked Pixels: " + nPoints + " Edge Pixels: " + nEdgePoints);
		if (DEBUG)
			System.out.println("Skeletongraph: " + graph + " Nodes: " + graph.getNumberOfNodes() + " Edges: " + graph.getNumberOfEdges() + " Edge Pixels: "
					+ nGraphEdgePoints);
		tryRemove4crossings(graph);
		if (DEBUG)
			System.out.println("Skeletongraph: " + graph + " Nodes: " + graph.getNumberOfNodes() + " Edges: " + graph.getNumberOfEdges() + " Edge Pixels: "
					+ nGraphEdgePoints);
	}
	
	private void tryRemove4crossings(AdjListGraph graph) {
		ArrayList<Node> delN = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			if (n.getDegree() > 4)
				System.out.println(SystemAnalysis.getCurrentTime() + ">INTERNAL ERROR: FOUND HIGH SKELETON GRAPH NODE DEGREE: " + n.getDegree());
			else
				if (n.getDegree() == 4) {
					LimbInfo[] lia = new LimbInfo[4];
					Node[] limbN = new Node[4];
					int idx = 0;
					for (Edge e : n.getEdges()) {
						ObjectAttribute oa = (ObjectAttribute) e.getAttribute("info");
						limbN[idx] = e.getSource() != n ? e.getSource() : e.getTarget();
						lia[idx++] = (LimbInfo) oa.getValue();
					}
					double diffVariant1 = Math.abs(lia[0].getLinearMx() - lia[1].getLinearMx()) + Math.abs(lia[2].getLinearMx() - lia[3].getLinearMx());
					double diffVariant2 = Math.abs(lia[0].getLinearMx() - lia[2].getLinearMx()) + Math.abs(lia[1].getLinearMx() - lia[3].getLinearMx());
					double diffVariant3 = Math.abs(lia[0].getLinearMx() - lia[3].getLinearMx()) + Math.abs(lia[1].getLinearMx() - lia[2].getLinearMx());
					int a1 = -1, a2 = -1, b1 = -1, b2 = -1;
					if (diffVariant1 < diffVariant2 && diffVariant1 < diffVariant3) {
						// crossing of 0<-->1 and 2<-->3
						a1 = 0;
						a2 = 1;
						b1 = 2;
						b2 = 3;
					} else
						if (diffVariant2 < diffVariant1 && diffVariant2 < diffVariant3) {
							// crossing of 0<-->2 and 1<-->3
							a1 = 0;
							a2 = 2;
							b1 = 1;
							b2 = 3;
						} else {
							// crossing of 0<-->3 and 1<-->2
							a1 = 0;
							a2 = 3;
							b1 = 1;
							b2 = 2;
						}
					Edge e01 = graph.addEdge(limbN[a1], limbN[a2], true);
					e01.addAttribute(new ObjectAttribute("info", new LimbInfo(lia[a1], lia[a2])), "");
					Edge e23 = graph.addEdge(limbN[b1], limbN[b2], true);
					e23.addAttribute(new ObjectAttribute("info", new LimbInfo(lia[b1], lia[b2])), "");
					delN.add(n);
				} else {
					// todo
				}
		}
		for (Node n : delN)
			graph.deleteNode(n);
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
	
	private ArrayList<Vector2i> traverseAndClearLineStartingFromStartPoint(Vector2i startPoint) {
		ArrayList<Vector2i> result = new ArrayList<Vector2i>();
		
		int x = startPoint.x;
		int y = startPoint.y;
		
		int cMem = skelImg[x][y];
		int xMem = x;
		int yMem = y;
		result.add(new Vector2i(x, y));
		skelImg[x][y] = visitedDuringSearch;
		boolean found, stop;
		int n = 0;
		do {
			found = false;
			stop = false;
			n++;
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
				search: for (int xd = -1; xd <= 1; xd++)
					for (int yd = -1; yd <= 1; yd++) {
						if (xd == 0 && yd == 0)
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
		if (skelImg[x][y] != SkeletonProcessor2d.colorBranches && skelImg[x][y] != SkeletonProcessor2d.colorEndpoints)
			skelImg[x][y] = background;
		skelImg[xMem][yMem] = cMem;
		return result;
	}
	
	public Graph getGraph() {
		return graph;
	}
}
