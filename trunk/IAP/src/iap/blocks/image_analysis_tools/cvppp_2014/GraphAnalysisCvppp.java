package iap.blocks.image_analysis_tools.cvppp_2014;

import iap.blocks.image_analysis_tools.leafClustering.Feature;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.ReleaseInfo;
import org.Vector2d;
import org.Vector2i;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.ObjectAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugins.ios.exporters.gml.GMLWriter;

import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operations.skeleton.LimbInfo;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonGraph;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths.WeightedShortestPathSelectionAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

public class GraphAnalysisCvppp {
	
	int[] skeletonDistImage;
	private final ArrayList<Feature> centerPoints;
	int w;
	int h;
	int back;
	ArrayList<PositionAndColor> splitPoints = new ArrayList<PositionAndColor>();
	Image distImage;
	
	public GraphAnalysisCvppp(Image distImage, int[] mapped, ArrayList<Feature> leafCenterPoints, int w, int h, int background) {
		skeletonDistImage = mapped;
		this.centerPoints = leafCenterPoints;
		this.w = w;
		this.h = h;
		this.back = background;
		this.distImage = distImage;
		
	}
	
	public ArrayList<PositionAndColor> getSplitPoints() {
		return splitPoints;
	}
	
	public void doTracking(String graphName) {
		splitPoints.clear();
		// fix position of maxima, which is often not directly on the skeleton!
		for (Feature cp : centerPoints) {
			double minDist = Double.MAX_VALUE;
			int minPos = -1;
			Vector2D v = cp.getPosition();
			int cpx = (int) v.getX();
			int cpy = (int) v.getY();
			
			int idx = 0;
			for (int skelPix : skeletonDistImage) {
				if (skelPix == back) {
					idx++;
					continue;
				}
				int x = idx % w;
				int y = idx / w;
				double d = new Vector2d(x, y).distance(cpx, cpy);
				if (d < minDist) {
					minPos = idx;
					minDist = d;
				}
				idx++;
			}
			if (minPos >= 0)
				cp.setPosition(minPos % w, minPos / w);
			else
				System.out.println("START FU");
		}
		for (Feature cp : centerPoints) {
			Vector2D v = cp.getPosition();
			int cpx = (int) v.getX();
			int cpy = (int) v.getY();
			if (skeletonDistImage[cpx + cpy * w] == back) {
				System.out.println("UNFU");
			}
		}
		// sort leaf tips according to nearest distance to center of gravity
		final Vector2d cog = distImage.io().stat().getCOG();
		Collections.sort(centerPoints, new Comparator<Feature>() {
			
			@Override
			public int compare(Feature a, Feature b) {
				Vector2D v = a.getPosition();
				int cpx = (int) v.getX();
				int cpy = (int) v.getY();
				
				Double dA = cog.distance(cpx, cpy);
				
				v = b.getPosition();
				cpx = (int) v.getX();
				cpy = (int) v.getY();
				
				Double dBB = cog.distance(cpx, cpy);
				
				return dA.compareTo(dBB);
			}
		});
		ArrayList<Vector2i> cps = new ArrayList<Vector2i>();
		for (Feature fff : centerPoints) {
			Vector2D v = fff.getPosition();
			int cpx = (int) v.getX();
			int cpy = (int) v.getY();
			cps.add(new Vector2i(cpx, cpy));
		}
		SkeletonProcessor2d skel = new SkeletonProcessor2d(new Image(w, h, skeletonDistImage).copy().io().bm().getImage()
				.show("SCHNOW", false));
		skel.background = back;
		skel.createEndpointsAndBranchesLists(null);
		skel.calculateEndlimbsRecursive();
		skel.markAdditionalBranchpoints(cps);
		
		SkeletonGraph sg = new SkeletonGraph(w, h, skel.skelImg);
		sg.setBackground(back);
		sg.setPreventIntermediateNodeRemoval(true);
		sg.createGraph(null, null, 0, null, 0, false);
		Graph g = sg.getGraph();
		for (Edge e : g.getEdges()) {
			// search minimum on edges
			if (!new GraphElementHelper(e).hasAttribute(".info"))
				continue;
			Object oov = ((ObjectAttribute) e.getAttribute(".info")).getValue();
			if (oov instanceof String) {
				new GraphElementHelper(e).setCluster("X");
			} else {
				// System.out.println("INFO: " + oov.getClass().getName());
				LimbInfo inf = (LimbInfo) oov;
				int min = Integer.MAX_VALUE;
				int minX = -1;
				int minY = -1;
				for (Vector2i p : inf.getEdgePoints()) {
					int v = skeletonDistImage[p.x + p.y * w];
					if (v >= 0 && v < min) {
						min = v;
						minX = p.x;
						minY = p.y;
					}
				}
				e.setInteger("minimumdist", min);
				e.setInteger("minimumX", minX);
				e.setInteger("minimumY", minY);
				e.setInteger("pixelcount", inf.getEdgePoints().size());
				new GraphElementHelper(e).setLabel("M=" + min + " at " + minX + "/" + minY);
			}
		}
		
		try {
			GMLWriter gml = new GMLWriter();
			if (graphName != null)
				gml.write(new FileOutputStream(new File(ReleaseInfo.getDesktopFolder() + "/ga" + graphName + ".gml")), g);
		} catch (Exception e) {
		}
		for (Edge e : g.getEdges())
			e.setDirected(false);
		
		double max = 0;
		
		Collection<Graph> gcl = GraphHelper.getConnectedComponents(g);
		if (gcl.size() > 2) {
			for (Graph gg : gcl) {
				double length = 0;
				int n = 0;
				double distToCenter = 0;
				for (Edge e : gg.getEdges()) {
					try {
						length += e.getInteger("pixelcount");
						n++;
						distToCenter += (new NodeHelper(e.getSource()).getPosition().distance(w / 2, h / 2) + new NodeHelper(e.getTarget()).getPosition().distance(
								w / 2, h / 2)) / 2;
					} catch (Exception err) {
						
					}
				}
				if (n > 2) {
					distToCenter = distToCenter / n;
					length = (length * (1 - distToCenter / w * 2) / h);
					if (length / n > max)
						max = length / n;
				}
			}
			
			ArrayList<Feature> forRemove = new ArrayList<Feature>();
			if (max > 0 && false)
				for (Graph gg : gcl) {
					double length = 0;
					int n = 0;
					double distToCenter = 0;
					for (Edge e : gg.getEdges()) {
						try {
							length += e.getInteger("pixelcount");
							n++;
							distToCenter += (new NodeHelper(e.getSource()).getPosition().distance(w / 2, h / 2) + new NodeHelper(e.getTarget()).getPosition()
									.distance(
											w / 2, h / 2)) / 2;
						} catch (Exception err) {
							err.printStackTrace();
						}
					}
					if (n > 2) {
						distToCenter = distToCenter / n;
						length = (length * (1 - distToCenter / w * 2) / h);
					}
					if (n > 2 && length / n < max * 0.9) {
						for (Node nn : gg.getNodes()) {
							Vector2i p = new NodeHelper(nn).getPosition2i();
							for (Feature f : centerPoints) {
								Vector2D fp = f.getPosition();
								int x = (int) fp.getX();
								int y = (int) fp.getY();
								
								if (Math.abs(p.x - x) < 2 && Math.abs(p.y - y) < 2) {
									forRemove.add(f);
								}
							}
						}
					}
				}
			
			centerPoints.removeAll(forRemove);
		}
		
		ArrayList<Node> centerPointNodes = new ArrayList<Node>();
		for (Vector2i cpv : cps) {
			for (Node n : g.getNodes()) {
				Vector2i p = new NodeHelper(n).getPosition2i();
				new NodeHelper(n).setLabel(p.x + "/ " + p.y);
				if (cpv.distance(p) < 2) {
					centerPointNodes.add(n);
					new NodeHelper(n).setBorderColor(Color.RED);
					break;
				}
			}
		}
		
		for (Node na : centerPointNodes) {
			int nnbb = 0;
			// new NodeHelper(na).setLabel("CP" + (nnaa++));
			for (Node nb : centerPointNodes) {
				// new NodeHelper(nb).setLabel("CP" + (nnbb++));
				if (na == nb)
					continue;
				
				boolean foundPath = false;
				do {
					ListOrderedSet nbl = new ListOrderedSet();
					nbl.add(nb);
					Collection<GraphElement> shortestPathNodesAndEdges =
							WeightedShortestPathSelectionAlgorithm.getShortestPathElements(
									g.getGraphElements(),
									na,
									nbl, false, false,
									false, Double.MAX_VALUE, null, false, false, false, true);
					boolean foundEdges = false;
					// System.out.println("Path from " + (nnaa - 1) + " to " + (nnbb - 1) + " length is: " + shortestPathNodesAndEdges.size());
					if (shortestPathNodesAndEdges.size() > 1) {
						Edge minimumEdge = null;
						int minimumDist = Integer.MAX_VALUE;
						int minimumX = -1;
						int minimumY = -1;
						
						for (GraphElement gg : shortestPathNodesAndEdges) {
							if (gg instanceof Edge) {
								
								try {
									Integer minDist = ((Edge) gg).getInteger("minimumdist");
									Integer minX = ((Edge) gg).getInteger("minimumX");
									Integer minY = ((Edge) gg).getInteger("minimumY");
									if (minDist < minimumDist) {
										foundEdges = true;
										minimumDist = minDist;
										minimumEdge = (Edge) gg;
										minimumX = minX;
										minimumY = minY;
									}
								} catch (AttributeNotFoundException anf) {
									//
								}
							}
						}
						if (minimumEdge != null) {
							new GraphElementHelper(minimumEdge).setLabel("CUT AT M=" + minimumDist + " at " + minimumX + "/" + minimumY);
							splitPoints.add(new PositionAndColor(minimumX, minimumY, minimumDist));
							g.deleteEdge(minimumEdge);
						}
					}
					foundPath = shortestPathNodesAndEdges.size() > 1 && foundEdges;
				} while (foundPath);
			}
		}
		
		// if (graphName != null)
		// System.out.println("FFFFFFFFFFXXXXXXXXXXXXXXXXXXXXXXXXUUJUU");
		// IAPmain.showVANTED(false);
		try {
			GMLWriter gml = new GMLWriter();
			if (graphName != null)
				gml.write(new FileOutputStream(new File(ReleaseInfo.getDesktopFolder() + "/gb" + graphName + ".gml")), g);
		} catch (Exception e) {
		}
		
	}
	
	private boolean isCenterPoint(int f) {
		int x = f % w;
		int y = f / w;
		for (Feature cp : centerPoints) {
			Vector2D v = cp.getPosition();
			int cpx = (int) v.getX();
			int cpy = (int) v.getY();
			
			if (x == cpx && y == cpy)
				return true;
		}
		return false;
	}
}
