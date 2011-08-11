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
	private HashMap<Integer, HashMap<Integer, Node>> positionX2Y2node = new HashMap<Integer, HashMap<Integer, Node>>();
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
		for (int x = 1; x < w - 1; x++) {
			for (int y = 1; y < h - 1; y++) {
				int p = skelImg[x][y];
				if (p == SkeletonProcessor2d.colorEndpoints || p == SkeletonProcessor2d.colorBranches) {
					nPoints++;
					Node n = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(x, y));
					System.out.println("MEM: " + x + " // " + y);
					// if (DEBUG)
					// debugImg = debugImg.getIO().getCanvas().fillRect(x - 4, y - 4, 8, 8, Color.GREEN.getRGB()).getImage();
					for (int xd = -4; xd <= 4; xd++)
						for (int yd = -4; yd <= 4; yd++) {
							if (xd >= -1 && xd <= 1 && yd >= -1 && yd <= 1)
								skelImg[x + xd][y + yd] = background;
							if (!positionX2Y2node.containsKey(x + xd))
								positionX2Y2node.put(x + xd, new HashMap<Integer, Node>());
							positionX2Y2node.get(x + xd).put(y + yd, n);
						}
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
				if (p != background && p != 1) {
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
					Node startNode = positionX2Y2node.get(s.x).get(s.y);
					Node endNode = positionX2Y2node.get(e.x).get(e.y);
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
		boolean found;
		do {
			found = false;
			for (int xd = -1; xd <= 1; xd++)
				for (int yd = -1; yd <= 1; yd++) {
					if (xd == 0 && yd == 0)
						continue;
					if (x + xd < 0 || y + yd < 0 || x + xd >= w || y + yd >= h)
						continue;
					if (skelImg[x + xd][y + yd] != background && skelImg[x + xd][y + yd] != 1) {
						skelImg[x + xd][y + yd] = 1;
						x = x + xd;
						y = y + yd;
						found = true;
						break;
					}
				}
		} while (found);
		return new Vector2i(x, y);
	}
	
	private ArrayList<Vector2i> traverseAndClearLineStartingFrom(Vector2i startPoint) {
		ArrayList<Vector2i> result = new ArrayList<Vector2i>();
		result.add(startPoint);
		int x = startPoint.x;
		int y = startPoint.y;
		skelImg[x][x] = background;
		boolean found;
		do {
			found = false;
			skelImg[x][y] = background;
			search: for (int xd = -1; xd <= 1; xd++)
				for (int yd = -1; yd <= 1; yd++) {
					if (xd == 0 && yd == 0)
						continue;
					if (x + xd < 0 || y + yd < 0 || x + xd >= w || y + yd >= h)
						continue;
					if (skelImg[x + xd][y + yd] != background) {
						x = x + xd;
						y = y + yd;
						result.add(new Vector2i(x + xd, y + yd));
						found = true;
						break search;
					}
				}
		} while (found);
		return result;
	}
}
