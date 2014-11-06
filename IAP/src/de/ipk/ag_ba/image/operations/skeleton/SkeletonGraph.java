package de.ipk.ag_ba.image.operations.skeleton;

import iap.blocks.data_structures.RunnableOnImage;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.AttributeHelper;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.Vector2i;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graffiti.attributes.ObjectAttribute;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugins.ios.exporters.gml.GMLWriter;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths.EdgeFollowingVetoEvaluation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths.WeightedShortestPathSelectionAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.MergeNodes;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectEdgesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

public class SkeletonGraph {
	private final boolean DEBUG = false;
	private final int w;
	private final int h;
	private final int[][] skelImg;
	Image debugImg;
	private AdjListGraph graph;
	private int background = -16777216;
	private final int visitedDuringSearch = Color.GRAY.getRGB();
	private boolean c;
	private boolean preventIntermediateNodeRemoval;
	
	public SkeletonGraph(int w, int h, int[][] skelImg) {
		this.w = w;
		this.h = h;
		this.skelImg = skelImg;
	}
	
	public SkeletonGraph(int w, int h, int[] skelImg1a) {
		this.w = w;
		this.h = h;
		this.skelImg = new Image(w, h, skelImg1a).getAs2A();
	}
	
	public void createGraph(int[] optClusterIDsPixels, final int[][] optDistanceMap, int minimumNodeDistance,
			ArrayList<RunnableOnImage> postProcessing, int delteShortEndLimbsMinimumLength) {
		this.graph = new AdjListGraph();
		HashSet<Integer> knownColors = new HashSet<Integer>();
		HashMap<String, Node> position2node = new HashMap<String, Node>();
		for (int x = 1; x < w - 1; x++) {
			for (int y = 1; y < h - 1; y++) {
				int p = skelImg[x][y];
				if (!knownColors.contains(p)) {
					// System.out.println("Pixel Color: " + SkeletonProcessor2d.getColorDesc(p) + " X=" + x + " / Y=" + y);
					knownColors.add(p);
				}
				if (p == SkeletonProcessor2d.colorEndpoints || p == SkeletonProcessor2d.colorBranches
						|| p == SkeletonProcessor2d.colorBloom) {
					createNodeForEndPointOrBranchPointInSkeletonImage(optClusterIDsPixels, postProcessing, position2node, x, y, p);
				}
			}
		}
		
		if (DEBUG) {
			Image fi = new Image(skelImg).show("TO BE ANALYZED...");
			debugImg = fi.copy();
		}
		if (DEBUG)
			debugImg.show("MARKED POINTS", false);
		for (int x = 1; x < w - 1; x++) {
			for (int y = 1; y < h - 1; y++) {
				int p = skelImg[x][y];
				if (p == SkeletonProcessor2d.colorEndpoints || p == SkeletonProcessor2d.colorBranches) {
					// System.out.println("FOUND BRANCH OR ENDPOINT POINT " + x + "/" + y);
					Vector2i startPoint = new Vector2i(x, y);
					if (DEBUG)
						System.out.println("Start: " + startPoint);
					boolean foundLine;
					Node lastStartNode = null;
					Node lastEndNode = null;
					do {
						ArrayList<Vector2i> edgePoints = traverseAndClearLineStartingFromStartPoint(startPoint);
						foundLine = edgePoints.size() > 1;
						if (DEBUG)
							System.out.println(" " + startPoint + " // Path-Len: " + edgePoints.size());
						Vector2i s = startPoint;// edgePoints.get(0);
						Vector2i e = edgePoints.get(edgePoints.size() - 1);
						Node startNode = position2node.get(s.x + ";" + s.y);
						Node endNode = position2node.get(e.x + ";" + e.y);
						if (startNode == null) {
							// System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: START POINT NOT FOUND: " + s.x + " / " + s.y);
							break;
						}
						if (endNode == null) {
							for (int offX = -2; offX <= 2; offX++)
								for (int offY = -2; offY <= 2; offY++) {
									if (endNode == null)
										endNode = position2node.get((e.x + offX) + ";" + (e.y + offY));
								}
							// if (endNode != null)
							// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: END POINT FOUND BY SURROUND SEARCH: " + e.x + " / " + e.y);
						}
						if (endNode == null) {
							// System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: END POINT NOT FOUND: " + e.x + " / " + e.y);
							break;
						}
						if (startNode == endNode)
							break;
						boolean del = false;
						String cS = new NodeHelper(startNode).getClusterID("");
						String cE = new NodeHelper(endNode).getClusterID("");
						if (!cS.equals(cE)) {
							// System.out.println("Start cluster is different to end cluster:" + cS + " <=> " + cE + " // " + s + " -- " + e);
							int i = 0;
							del = true;
							if (postProcessing != null)
								for (Vector2i ep : edgePoints)
									if ((i++) % 20 == 0)
										ImageCanvas.markPoint(ep.x / 2, ep.y / 2, postProcessing, Color.LIGHT_GRAY);
						}
						if (DEBUG)
							System.out.println("S: " + s + " ==> E: " + e + " //// " + startNode + " // " + endNode + " // "
									+ (startNode == null || endNode == null ? "NULL" : ""));
						if (startNode != null && endNode != null) {
							if (startNode.getNeighbors().contains(endNode))
								del = true;
							Edge edge = graph.addEdge(startNode, endNode, true);
							edge.addAttribute(new ObjectAttribute("info", new LimbInfo(edgePoints)), "");
							if (lastStartNode == startNode && lastEndNode == endNode) {
								break;
							}
							lastStartNode = startNode;
							lastEndNode = endNode;
							if (del)
								graph.deleteEdge(edge);
						}
					} while (foundLine);
				}
			}
		}
		for (Node a : graph.getNodes()) {
			for (Node b : graph.getNodes()) {
				if (a == b)
					continue;
				Vector2i pa = new NodeHelper(a).getPosition2i();
				Vector2i pb = new NodeHelper(b).getPosition2i();
				if (pa.distance(pb) < 2) {
					// System.out.println("CONNECT " + pa.distance(pb));
					graph.addEdge(a, b, false);
				}
			}
		}
		if (!preventIntermediateNodeRemoval) {
			for (int i = 0; i < 5; i++) {
				ArrayList<Node> delN = new ArrayList<Node>();
				for (Node leaf : GraphHelper.getLeafNodesUndir(graph)) {
					int n = 0;
					double lenS = 0;
					for (Edge e : leaf.getEdges()) {
						ArrayList<Vector2i> edgePoints = ((LimbInfo) ((ObjectAttribute) e.getAttribute("info")).getValue()).getEdgePoints();
						lenS += edgePoints.size();
						n++;
					}
					double len = lenS / n;
					if (len < delteShortEndLimbsMinimumLength)
						delN.add(leaf);
				}
				for (Node n : delN)
					graph.deleteNode(n);
				
				deleteSelfLoops();
				if (!preventIntermediateNodeRemoval)
					removeParallelEdges();
				graph.numberGraphElements();
				
				if (delN.size() > 0)
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Removed short end-limbs (graph based), " +
							"shorter than " + delteShortEndLimbsMinimumLength + ". Removed: " + delN.size());
				if (!preventIntermediateNodeRemoval)
					for (Node n : new ArrayList<Node>(graph.getNodes())) {
						// remove nodes, with degree 2 (and merge pixel list)
						if (n.getDegree() == 2) {
							// add new edge, with edge pixel list from both edges, then delete that node
							int nc = 0;
							ArrayList<Vector2i> ep = new ArrayList<Vector2i>();
							for (Edge e : n.getEdges()) {
								nc++;
								try {
									ArrayList<Vector2i> edgePoints = ((LimbInfo) ((ObjectAttribute) e.getAttribute("info")).getValue()).getEdgePoints();
									ep.addAll(edgePoints);
								} catch (Exception err) {
									// some edge has no edge point list
								}
							}
							if (nc != 2)
								ErrorMsg.addErrorMessage("Internal Error: Graph node with degree two returns different count of edges (" + nc + ")");
							else {
								Iterator<Node> it = n.getNeighborsIterator();
								Node a = it.next();
								if (!it.hasNext()) {
									// ErrorMsg.addErrorMessage("Internal Error: Graph node with degree two has only one neighbour (self edges are deleted?!)");
								} else {
									Node b = it.next();
									if (it.hasNext())
										ErrorMsg.addErrorMessage("Internal Error: Graph node with degree two has more than two neighbours");
									else {
										Edge e01 = graph.addEdge(a, b, false);
										e01.addAttribute(new ObjectAttribute("info", new LimbInfo(ep)), "");
										graph.deleteNode(n);
									}
								}
							}
						}
					}
			}
			
			for (Edge edge : graph.getEdges()) {
				if (!new GraphElementHelper(edge).hasAttribute("info"))
					continue;
				ArrayList<Vector2i> edgePoints = ((LimbInfo) ((ObjectAttribute) edge.getAttribute("info")).getValue()).getEdgePoints();
				
				edge.setDouble("len", edgePoints.size());
				if (optDistanceMap != null) {
					DescriptiveStatistics statWidth = new DescriptiveStatistics();
					DescriptiveStatistics statReveresed = new DescriptiveStatistics();
					
					ArrayList<Vector2i> reversedList = new ArrayList<Vector2i>(edgePoints);
					Collections.reverse(reversedList);
					
					for (Vector2i ep : edgePoints) {
						double width = optDistanceMap[ep.x][ep.y] / 10d;
						statWidth.addValue(width);
						if (statWidth.getN() >= 20)
							break;
					}
					for (Vector2i ep : reversedList) {
						double width = optDistanceMap[ep.x][ep.y] / 10d;
						statReveresed.addValue(width);
						if (statReveresed.getN() >= 20)
							break;
					}
					if (statReveresed.getMean() < statWidth.getMean()) {
						statWidth = statReveresed;
						edgePoints = reversedList;
					}
					int idx = 0;
					for (Vector2i ep : edgePoints) {
						if (idx == 40 || idx == edgePoints.size() - 1) {
							// double width = statWidth.getPercentile(50);
							ImageCanvas.markPoint(ep.x / 2, ep.y / 2, postProcessing, Color.YELLOW);
							// ImageCanvas.text(ep.x / 2, ep.y / 2 - 20, "W=" +
							// StringManipulationTools.formatNumber(width, 1)
							// + ", L=" + edgePoints.size() / 2, Color.MAGENTA, postProcessing);
						}
						idx++;
					}
					
					edge.setDouble("w_average", statWidth.getMean());
					edge.setDouble("w_median", statWidth.getPercentile(50));
					edge.setDouble("w_stddev", statWidth.getStandardDeviation());
					edge.setDouble("w_kurtosis", statWidth.getKurtosis());
					edge.setDouble("w_skewness", statWidth.getSkewness());
					edge.setDouble("w_min", statWidth.getMin());
					edge.setDouble("w_max", statWidth.getMax());
					AttributeHelper.setLabel((GraphElement) edge, "W:" + StringManipulationTools.formatNumber(statWidth.getMean(), 1));
				}
			}
			ArrayList<Node> delll = new ArrayList<Node>();
			for (Node n : graph.getNodes()) {
				if (n.getDegree() == 1) {
					double limb_len = n.getEdges().iterator().next().getDouble("len") / 2;
					if (limb_len < delteShortEndLimbsMinimumLength)
						delll.add(n);
				}
			}
			for (Node nooo : delll)
				graph.deleteNode(nooo);
			
			boolean checkDist = minimumNodeDistance > 0;
			if (checkDist) {
				boolean mergeOccured;
				do {
					mergeOccured = false;
					loop: for (Node n1 : graph.getNodes()) {
						for (Node n2 : graph.getNodes()) {
							if (n1 == n2)
								continue;
							if (!new NodeHelper(n1).getClusterID("-1").equals(new NodeHelper(n2).getClusterID("-1"))) {
								continue;
							}
							int x1 = n1.getInteger("x");
							int y1 = n1.getInteger("y");
							int x2 = n2.getInteger("x");
							int y2 = n2.getInteger("y");
							Vector2i a = new Vector2i(x1, y1);
							Vector2i b = new Vector2i(x2, y2);
							if (a.distance(b) < minimumNodeDistance) {
								// System.out.println(SystemAnalysis.getCurrentTime() + ">Merge nearby skeleton graph nodes: " + x1 + "/" + y1 + " near to " + x2 + "/"
								// +
								// y2);
								Collection<Node> workNodes = new ArrayList<Node>();
								workNodes.add(n1);
								workNodes.add(n2);
								MergeNodes.mergeNodesIntoSingleNode(graph, workNodes);
								mergeOccured = true;
								break loop;
							}
						}
					}
				} while (mergeOccured);
			}
		}
		deleteSelfLoops();
		if (!preventIntermediateNodeRemoval)
			removeParallelEdges();
		graph.numberGraphElements();
		
		if (DEBUG)
			System.out.println("Skeletongraph: " + graph + " Nodes: " + graph.getNumberOfNodes() + " Edges: " + graph.getNumberOfEdges());
	}
	
	private void createNodeForEndPointOrBranchPointInSkeletonImage(int[] optClusterIDsPixels, ArrayList<RunnableOnImage> postProcessing,
			HashMap<String, Node> position2node, int x, int y, int p) {
		final Node n = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(x, y));
		n.setInteger("x", x);
		n.setInteger("y", y);
		String cid = "-";
		if (optClusterIDsPixels != null)
			cid = optClusterIDsPixels[y * w + x] + "";
		
		new NodeHelper(n).setLabel("C" + cid);
		
		RunnableWithBooleanResult delCheck = new RunnableWithBooleanResult() {
			@Override
			public boolean enabled() {
				return n.getGraph() != null; // only if it has not been deleted by the graph post processing, e.g. short limb etc.
			}
		};
		
		String dd = SkeletonProcessor2d.getColorDesc(p);
		if (dd.equals("branch"))
			dd = "____b";
		if (dd.equals("endpoint") && postProcessing != null) {
			ImageCanvas.markPoint2(x / 2, y / 2, postProcessing, delCheck);
			dd = "_e";
		}
		if (postProcessing != null)
			ImageCanvas.text(x / 2 + 16, y / 2, dd, Color.BLACK, postProcessing, delCheck);
		if (optClusterIDsPixels != null) {
			// System.out.println("NEW NODE: " + n + ": " + cid);
			new NodeHelper(n).setClusterID(cid);
		}
		position2node.put(x + ";" + y, n);
		if (DEBUG)
			System.out.println("MEM: " + x + " // " + y);
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
				if (p == SkeletonProcessor2d.getDefaultBackground())
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
		do {
			found = false;
			stop = false;
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
	
	public void deleteSelfLoops() {
		for (Edge e : new ArrayList<Edge>(graph.getEdges()))
			if (e.getSource() == e.getTarget())
				graph.deleteEdge(e);
	}
	
	/**
	 * @param postProcessing
	 * @param postProcessors
	 * @return map from cluster ID 2 size, -1 to largest size
	 */
	public HashMap<Integer, Double> calculateDiameter(
			boolean saveGraphFiles,
			boolean findAndProcessMostLefAndRightEndPointsOnly,
			ArrayList<RunnableOnImage> postProcessing, boolean thinned) {
		HashMap<Integer, Double> id2size = new HashMap<Integer, Double>();
		Collection<Graph> gl = GraphHelper.getConnectedComponents(graph);
		if (gl.size() != 1)
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Skeleton graph with more than one component created. Number of components: "
					+ gl.size());
		Graph lcgg = null;
		double largestDiameter = 0;
		String optGMLoutputFileName = !saveGraphFiles ? null : ReleaseInfo.getAppSubdirFolderWithFinalSep("graph_files") + "skeleton_"
				+ System.currentTimeMillis() + ".gml";
		final HashMap<Graph, List<GraphElement>> graphComponent2shortestPathElements = new HashMap<Graph, List<GraphElement>>();
		for (final Graph gg : gl) {
			if (gg.getNumberOfNodes() < 2)
				continue;
			Integer id = Integer.parseInt(new NodeHelper(gg.getNodes().iterator().next()).getClusterID(null));
			if (optGMLoutputFileName != null)
				System.out.print(SystemAnalysis.getCurrentTime() + ">INFO: Determine graph diameter: ");
			ThreadSafeOptions optLengthReturn = new ThreadSafeOptions();
			List<GraphElement> elem = WeightedShortestPathSelectionAlgorithm.findLongestShortestPathElements(
					findAndProcessMostLefAndRightEndPointsOnly ? filterMostLeftAndRightEndpoints(gg.getGraphElements()) : gg.getGraphElements(),
					new AttributePathNameSearchType("", "len", SearchType.searchDouble, "len"),
					optLengthReturn, false);
			graphComponent2shortestPathElements.put(gg, elem);
			if (optGMLoutputFileName != null && !thinned)
				for (GraphElement ge : elem) {
					if (ge instanceof Node) {
						final NodeHelper nh = new NodeHelper((Node) ge);
						nh.setFillColor(Color.YELLOW)
								.setAttributeValue("shortest_path", "maxlen", optLengthReturn.getDouble());
					}
				}
			Double dia = optLengthReturn.getDouble();
			id2size.put(id, dia);
			if (optGMLoutputFileName != null)
				System.out.println(dia.intValue());
			if (dia > largestDiameter) {
				lcgg = gg;
				largestDiameter = dia;
				id2size.put(-1, dia);
			}
		}
		if (lcgg != null) {
			lcgg.numberGraphElements();
			final Graph lcggF = lcgg;
			postProcessing.add(new RunnableOnImage() {
				@Override
				public Image postProcess(Image in) {
					ImageCanvas canv = in.io().canvas();
					List<GraphElement> elms = graphComponent2shortestPathElements.get(lcggF);
					for (GraphElement ge : elms) {
						if (ge instanceof Node) {
							final NodeHelper nh = new NodeHelper((Node) ge);
							
							Point2D p = nh.getPosition();
							canv.drawCircle(
									(int) p.getX() / 2,
									(int) p.getY() / 2, 22, Color.GREEN.getRGB(), 0, 2).getImage();
						}
					}
					return canv.getImage();
				}
			});
			if (optGMLoutputFileName != null) {
				GMLWriter gw = new GMLWriter();
				// ReleaseInfo.getDesktopFolder() + "/skel.gml")
				try {
					gw.write(new FileOutputStream(optGMLoutputFileName), graph);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Largest Component Diameter: " + (int) largestDiameter + ", saved graph in "
						+ optGMLoutputFileName);
			}
		}
		return id2size;
	}
	
	private Collection<GraphElement> filterMostLeftAndRightEndpoints(Collection<GraphElement> graphElements) {
		ArrayList<GraphElement> res = new ArrayList<>();
		int mostLeft = Integer.MAX_VALUE;
		int mostRight = -1;
		Node ml = null;
		Node mr = null;
		for (GraphElement g : graphElements) {
			if (g instanceof Node) {
				Vector2i pos = new NodeHelper((Node) g).getPosition2i();
				if (pos.x < mostLeft) {
					mostLeft = pos.x;
					ml = (Node) g;
				}
				if (pos.x > mostRight) {
					mostRight = pos.x;
					mr = (Node) g;
				}
			}
		}
		if (ml != null)
			res.add(ml);
		if (mr != null)
			res.add(mr);
		return res;
	}
	
	/**
	 * @param postProcessing
	 * @param rt
	 * @param postProcessors
	 * @return map from cluster ID 2 size, -1 to largest size
	 */
	public HashMap<Integer, Double> calculateDiameterThickToThin(boolean saveGraphFiles,
			boolean isThinned, ArrayList<RunnableOnImage> postProcessing, ResultsTableWithUnits rt, final boolean VETO_SUPPORT) {
		HashMap<Integer, Double> id2size = new HashMap<Integer, Double>();
		Collection<Graph> gl = GraphHelper.getConnectedComponents(graph);
		
		// System.out.println("Skeleton graph created. Number of components: " + gl.size());
		Graph lcgg = null;
		double largestDiameter = 0;
		
		EdgeFollowingVetoEvaluation edgeVeto = new EdgeFollowingVetoEvaluation() {
			@Override
			public boolean followEdge(Edge e) {
				if (!VETO_SUPPORT)
					return true;
				// don't follow edges, which are connected at each end to nodes, which have higher width edges connected with them
				Node nodeA = e.getSource();
				Node nodeB = e.getTarget();
				if (nodeA == nodeB)
					return false; // don't follow self-edges (shouldn't be in the dataset, anyway)
				double thicknessOfEdgesNodeA = getMaxThicknessOfEdgesNotConnectedTo(nodeA, nodeB);
				double thicknessOfEdgesNodeB = getMaxThicknessOfEdgesNotConnectedTo(nodeB, nodeA);
				double tt = e.getDouble("w_median");
				boolean aHigher = thicknessOfEdgesNodeA > 1.05 * tt;
				boolean bHigher = thicknessOfEdgesNodeB > 1.05 * tt;
				boolean aMiddleHigher = thicknessOfEdgesNodeA > 1.1 * tt;
				boolean bMiddleHigher = thicknessOfEdgesNodeB > 1.1 * tt;
				boolean aMuchHigher = thicknessOfEdgesNodeA > 1.5 * tt;
				boolean bMuchHigher = thicknessOfEdgesNodeB > 1.5 * tt;
				boolean invalid = (aMiddleHigher && bMiddleHigher) || (aMuchHigher && bHigher) || (bMuchHigher && aHigher);
				if (invalid && e.getDouble("len") < 80)
					invalid = false;
				return !invalid;
			}
			
			private double getMaxThicknessOfEdgesNotConnectedTo(Node node, Node notAllowed) {
				double max = -1;
				for (Edge e : node.getEdges()) {
					if (e.getTarget() == notAllowed || e.getSource() == notAllowed)
						continue;
					double t = e.getDouble("w_median"); // (from thinner end of edge)
					if (t > max)
						max = t;
				}
				return max < 0 ? Double.MAX_VALUE : max;
			}
		};
		
		String optGMLoutputFileName = !saveGraphFiles ? null : ReleaseInfo.getAppSubdirFolderWithFinalSep("graph_files") + "skeleton_"
				+ System.currentTimeMillis() + ".gml";
		
		if (!isThinned) {
			double vol = 0, vol2 = 0;
			double lenA = 0;
			double lenB = 0;
			for (Edge e : graph.getEdges()) {
				if (!e.getAttributes().getCollection().keySet().contains("w_median"))
					continue;
				double mm = e.getDouble("w_median");
				double mv = e.getDouble("w_average");
				mm = mm / 2d; // calculate radius from diameter
				mv = mv / 2d; // calculate radius from diameter
				double lin = e.getDouble("len") / 2;
				double l1 = lin;
				l1 = l1 + mm * 2d;
				double l2 = lin + mv * 2d;
				lenA += l1;
				lenB += l2;
				double v = Math.PI * mm * mm * l1;
				double v2 = Math.PI * mv * mv * l2;
				vol += v;
				vol2 += v2;
				// System.out.println("V_median: r=" + mm + ", l=" + l1 + " ==> v=" + v + " // vol_s=" + vol);
				// System.out.println("V_mean:   r=" + mv + ", l=" + l2 + " ==> v=" + v2 + " // vol_s=" + vol2);
			}
			rt.addValue("roots.volume.graph_median_based", vol);
			rt.addValue("roots.volume.graph_mean_based", vol2);
			rt.addValue("roots.skeleton.length.graph_based", lenA);
			rt.addValue("roots.skeleton.length.graph_based_tip_corr", lenB);
		}
		if (!isThinned) {
			postProcessing.add(new RunnableOnImage() {
				@Override
				public Image postProcess(Image in) {
					ImageCanvas c = in.io().canvas();
					for (GraphElement g : graph.getGraphElements()) {
						if (g.getGraph() == null)
							System.exit(2); // fck
					}
					
					for (Node n : graph.getNodes()) {
						NodeHelper nh = new NodeHelper(n);
						Vector2i p = nh.getPosition2i().scale(0.5);
						if (n.getGraph() == null)
							c = c.drawCircle(p.x, p.y, 24, Color.BLACK.getRGB(), 0.5, 2);
						else
							if (n.getDegree() == 1) {
								// double limb_len = n.getEdges().iterator().next().getDouble("len");
								// if (limb_len < 20)
								// c = c.drawCircle(p.x, p.y, 14, Color.RED.getRGB(), 0.5, 5);
								// else
								c = c.drawCircle(p.x, p.y, 4, Color.RED.getRGB(), 0.5, 1);
							} else
								c = c.drawCircle(p.x, p.y, 3, Color.BLUE.getRGB(), 0.5, 1);
					}
					return c.getImage();
				}
			});
		}
		
		for (Node ge : graph.getNodes()) {
			if (ge.getNeighbors().size() != 1)
				continue;
			final NodeHelper nh = new NodeHelper(ge);
			postProcessing.add(new RunnableOnImage() {
				@Override
				public Image postProcess(Image in) {
					Point2D p = nh.getPosition();
					ImageCanvas canv = in.io().canvas();
					try {
						double wi = nh.getEdges().iterator().next().getDouble("w_median");
						double limb_len = nh.getEdges().iterator().next().getDouble("len") / 2;
						// limb_len += wi * 2;
						canv.text((int) (p.getX() / 2 + 18), (int) (p.getY() / 2) + 18, "w="
								+ StringManipulationTools.formatNumber(wi, 1) + ",L=" + (int) limb_len, Color.GRAY);
					} catch (Exception e) {
						canv.text((int) (p.getX() / 2 + 18), (int) (p.getY() / 2) + 18, "W=ERROR: " + e.getMessage(), Color.RED);
					}
					return canv.getImage();
				}
			});
		}
		
		HashMap<Graph, List<GraphElement>> graph2elems = new HashMap<Graph, List<GraphElement>>();
		for (Graph gg : gl) {
			if (gg.getNumberOfNodes() == 0)
				continue;
			Integer id = Integer.parseInt(new NodeHelper(gg.getNodes().iterator().next()).getClusterID("-1"));
			if (id < 0) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Graph node has no assigned cluster ID (internal error)");
				continue;
			}
			
			if (optGMLoutputFileName != null) {
				System.out.print(SystemAnalysis.getCurrentTime() + ">INFO Determine graph diameter: ");
				try {
					new GMLWriter().write(new FileOutputStream(ReleaseInfo.getAppFolderWithFinalSep() + "GG1.gml"), gg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			int delcnt = 0;
			for (Edge e : new ArrayList<Edge>(gg.getEdges())) {
				if (!edgeVeto.followEdge(e)) {
					gg.deleteEdge(e);
					delcnt++;
				}
			}
			if (optGMLoutputFileName != null) {
				System.out.println("Deleted " + delcnt + " edges!");
				try {
					new GMLWriter().write(new FileOutputStream(ReleaseInfo.getAppFolderWithFinalSep() + "GG2.gml"), gg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			
			Collection<Node> thickestStartingPoints = filterThickestStartingPoints(gg.getGraphElements(), postProcessing);
			
			List<GraphElement> elem = null;
			Double dia = null;
			double longestDia = 0;
			for (GraphElement thick : thickestStartingPoints) {
				ThreadSafeOptions optLengthReturn = new ThreadSafeOptions();
				List<GraphElement> elemTest = WeightedShortestPathSelectionAlgorithm.findLongestShortestPathStartAndEndPoints(
						thick, gg.getGraphElements(),
						new AttributePathNameSearchType("", "len", SearchType.searchDouble, "len"),
						optLengthReturn, true);
				Double diaTest = optLengthReturn.getDouble();
				// System.out.println("DIA TEST: " + diaTest);
				if (diaTest > longestDia) {
					longestDia = diaTest;
					elem = elemTest;
					dia = diaTest;
				}
			}
			if (dia == null)
				continue;
			graph2elems.put(gg, elem);
			boolean first = true;
			final int diaF = dia.intValue();
			
			if (!isThinned)
				for (GraphElement ge : elem) {
					if (ge instanceof Node) {
						final NodeHelper nh = new NodeHelper((Node) ge);
						final boolean firstF = first;
						first = false;
						nh.setFillColor(Color.YELLOW)
								.setAttributeValue("shortest_path", "maxlen", dia / 2);
						postProcessing.add(new RunnableOnImage() {
							@Override
							public Image postProcess(Image in) {
								Point2D p = nh.getPosition();
								ImageCanvas canv = in.io().canvas();
								if (firstF)
									canv.text((int) (p.getX() / 2 + 12), (int) (p.getY() / 2), "L=" + diaF, Color.DARK_GRAY);
								return canv.drawCircle(
										(int) p.getX() / 2,
										(int) p.getY() / 2, 12, Color.PINK.getRGB(), 0, 1).getImage();
							}
						});
					}
				}
			id2size.put(id, dia);
			if (optGMLoutputFileName != null)
				System.out.println(SystemAnalysis.getCurrentTime() + "INFO: Graph segment diameter (px): " + dia.intValue());
			if (dia > largestDiameter) {
				lcgg = gg;
				largestDiameter = dia;
				id2size.put(-1, dia);
			}
		}
		if (!isThinned)
			if (lcgg != null) {
				lcgg.numberGraphElements();
				final int largestDiaF = (int) largestDiameter;
				boolean first = true;
				for (GraphElement ge : graph2elems.get(lcgg)) {
					if (ge instanceof Node) {
						final boolean firstF = first;
						first = false;
						final NodeHelper nh = new NodeHelper((Node) ge);
						postProcessing.add(new RunnableOnImage() {
							@Override
							public Image postProcess(Image in) {
								Point2D p = nh.getPosition();
								ImageCanvas canv = in.io().canvas();
								if (firstF)
									canv.text((int) (p.getX() / 2 + 18), (int) (p.getY() / 2), "L=" + largestDiaF, Color.RED);
								
								return canv.drawCircle(
										(int) p.getX() / 2,
										(int) p.getY() / 2, 21, Color.YELLOW.getRGB(), 0, 1).getImage();
							}
						});
					}
				}
				
				if (optGMLoutputFileName != null) {
					GMLWriter gw = new GMLWriter();
					// ReleaseInfo.getDesktopFolder() + "/skel.gml")
					try {
						gw.write(new FileOutputStream(optGMLoutputFileName), graph);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Largest Component Diameter: " + (int) largestDiameter + ", saved graph in "
							+ optGMLoutputFileName);
				}
			}
		return id2size;
	}
	
	/**
	 * If the distance map was available during graph creation, the distance of the skeleton point to the border is available.
	 * This distance is stored on the nodes, in the integer attribute "d".
	 * This method should not return all graph elements, but only those with the maximum distance to the border, which
	 * means, that only very thick starting points should be considered.
	 */
	private Collection<Node> filterThickestStartingPoints(Collection<GraphElement> graphElements,
			ArrayList<RunnableOnImage> postProcessors) {
		double maximumWidth = -1;
		final ArrayList<Node> thickestStartingPoints = new ArrayList<Node>();
		for (GraphElement ge : graphElements) {
			if (ge.getGraph() == null) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Internal error: graphelement list contains deleted elements!");
				continue;
			}
			if (ge instanceof Node) {
				Node n = (Node) ge;
				if (n.getNeighbors().size() > 1)
					continue;
				try {
					double d = n.getEdges().iterator().next().getDouble("w_median");
					if (d * 0.9 > maximumWidth) {
						thickestStartingPoints.clear();
					}
					if (d >= maximumWidth * 0.9) {
						thickestStartingPoints.add(n);
						maximumWidth = d;
					}
				} catch (Exception e) {
					// attribute not available
					// ignore this element
				}
			}
		}
		for (Node thickStart : thickestStartingPoints) {
			thickStart.setBoolean("thickStart", true);
		}
		RunnableOnImage pp = new RunnableOnImage() {
			
			@Override
			public Image postProcess(Image in) {
				ImageCanvas c = in.io().canvas();
				for (GraphElement ge : thickestStartingPoints) {
					Node n = (Node) ge;
					NodeHelper nh = new NodeHelper(n);
					c = c.drawRectangle((int) nh.getX() / 2 - 10, (int) nh.getY() / 2 - 10, 20, 20, Color.BLUE, 2);
				}
				return c.getImage();
			}
		};
		postProcessors.add(pp);
		return thickestStartingPoints;
	}
	
	public void removeParallelEdges() {
		for (Edge e : new ArrayList<Edge>(graph.getEdges()))
			if (SelectEdgesAlgorithm.parallelEdgeExists(e) || SelectEdgesAlgorithm.antiParallelEdgeExists(e))
				graph.deleteEdge(e);
	}
	
	public void setBackground(int back) {
		this.background = back;
	}
	
	public void setPreventIntermediateNodeRemoval(boolean b) {
		this.preventIntermediateNodeRemoval = b;
	}
	
	public void connectGraphComponents() {
		int connectedComponents;
		HashSet<Graph> components = new HashSet<Graph>();
		HashMap<Node, Node> map = GraphHelper.getConnectedComponentMap(graph);
		HashMap<Node, Node> mapInverse = NodeHelper.inverseMap(map);
		for (Node n : map.values()) {
			components.add(n.getGraph());
		}
		connectedComponents = components.size();
		Node nearestA = null, nearestB = null;
		do {
			
			if (connectedComponents > 1) {
				nearestA = null;
				nearestB = null;
				double minDist = Double.MAX_VALUE;
				for (Graph gA : components)
					for (Graph gB : components) {
						if (gA == gB)
							continue;
						for (Node a : gA.getNodes()) {
							if (mapInverse.get(a) == null) {
								System.out.println("ERR_A");
								continue;
							}
							for (Node b : gB.getNodes()) {
								if (mapInverse.get(b) == null) {
									System.out.println("ERR_B");
									continue;
								}
								
								if (a == b)
									continue;
								
								double d = AttributeHelper.getPositionVec2d(a).distance(AttributeHelper.getPositionVec2d(b));
								if (d < minDist) {
									nearestA = a;
									nearestB = b;
									minDist = d;
								}
							}
						}
					}
				if (nearestA != null) {
					Node sourceA = mapInverse.get(nearestA);
					Node sourceB = mapInverse.get(nearestB);
					if (sourceA == null || sourceB == null)
						System.out.println("ERRR");
					else
						graph.addEdge(sourceA, sourceB, false);
				}
				components.clear();
				graph.numberGraphElements();
				map = GraphHelper.getConnectedComponentMap(graph);
				for (Node n : map.values()) {
					components.add(n.getGraph());
				}
				connectedComponents = components.size();
			}
		} while (connectedComponents > 1 && nearestA != null && nearestB != null);
	}
}
