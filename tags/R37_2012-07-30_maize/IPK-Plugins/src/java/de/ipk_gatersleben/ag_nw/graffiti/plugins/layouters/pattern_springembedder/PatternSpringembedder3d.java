/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 04.06.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder;

import info.clearthought.layout.SingleFiledLayout;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.JLabelJavaHelpLink;
import org.JMButton;
import org.Release;
import org.ReleaseInfo;
import org.Vector3d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeAlgorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.copy_pattern_layout.CopyPatternLayoutAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.no_overlapp_as_tim.NoOverlappLayoutAlgorithmAS;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

// bValues: 0 = RotatePattern?
// * stiffness of spring between two connected nodes, see Graph Drawing p.
// * 308
// public double k1 = 10;
// /**
// * strength of the electrical repulsion between all nodes, see Graph
// * Drawing p. 308
// */
// public double k2hor = 1000;
//
// /**
// * DOCUMENT ME!
// */
// public double k2vert = 1000;
// /**
// * natural (zero energy) length of spring between two connected nodes, see
// * Graph Drawing p. 308
// */
// public double nat_l = 200;

/**
 * Spring Embedder Algorithm. Example #3.
 * 
 * @author Christian Klukas
 */
public class PatternSpringembedder3d
					extends ThreadSafeAlgorithm
// implements EditorAlgorithm
{
	
	public static boolean enabled = true;
	
	public static final String springName = "Force Directed";
	
	private Graph non_interact_graph;
	
	private Selection non_interact_selection;
	
	public int calcCounter = 0;
	
	/**
	 * Saves the positions of the clusters. The hashMap contains Vector2d values.
	 * The keys are Integers for the cluster numbers.
	 * This hashMap might be empty, if no cluster locations are calculated or known.
	 */
	private HashMap<String, Vector3d> clusterLocations = new HashMap<String, Vector3d>();
	
	/**
	 * used for non interactive run, is used in the <code>reset</code> method to
	 * set the maximum move value back to the desired value.
	 */
	private double initLength;
	
	private double cachedClusterForce;
	
	private ThreadSafeOptions nonInteractiveTSO;
	
	/**
	 * An image with an animated progress bar
	 */
	// ImageIcon progressImg;
	
	/**
	 * An image with an progress bar, which shows the idle state
	 */
	// ImageIcon progressImgOK;
	
	/**
	 * Sets Menu Command Title
	 * 
	 * @return Menu command title
	 */
	public String getName() {
		if (!enabled)
			return null;
		else
			return springName + " 3D";
	}
	
	public String toString() {
		return getName();
	}
	
	public ActionEvent getActionEvent() {
		return null;
	}
	
	public void setActionEvent(ActionEvent a) {
		// empty
	}
	
	/**
	 * This method returns a <code>Vector</code> with all <code>NodeCacheEntry3d</code> entries that have the same pattern type and index.
	 * 
	 * @param options
	 *           DOCUMENT ME!
	 * @param nodeInfo
	 * @return <code>Vector</code> with <code>NodeCacheEntry3d</code> Objects. The node is returned in the result set, if it has no
	 *         pattern.
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<NodeCacheEntry3d> getPatternNodes(ThreadSafeOptions options,
						NodeCacheEntry3d nodeInfo) {
		ArrayList<NodeCacheEntry3d> resultVec = getPatternNodesPublic((ArrayList<NodeCacheEntry3d>) options.nodeArray, nodeInfo);
		
		if (resultVec.size() > 1) {
			// System.out.println("PC: " + resultVec.size());
		}
		
		return resultVec;
	}
	
	public static ArrayList<NodeCacheEntry3d> getPatternNodesPublic(
						ArrayList<NodeCacheEntry3d> nodeArray,
						NodeCacheEntry3d nodeInfo) {
		ArrayList<NodeCacheEntry3d> resultVec = new ArrayList<NodeCacheEntry3d>();
		
		if (nodeInfo.patternType.compareTo("") != 0) {
			for (int i = 0; i < nodeArray.size(); i++) {
				if (nodeArray.get(i).patternType
									.compareTo("") != 0) {
					if ((nodeArray.get(i).patternType
										.compareTo(nodeInfo.patternType) == 0)
										&& (nodeArray.get(i).patternIndex == nodeInfo.patternIndex)) {
						resultVec.add(nodeArray.get(i));
					}
				}
				// patternType <> null
			}
		} else {
			resultVec.add(nodeInfo);
		}
		return resultVec;
	}
	
	/**
	 * Error Checking
	 * 
	 * @throws PreconditionException
	 * @throws PreconditionException
	 *            DOCUMENT ME!
	 */
	public void check() throws PreconditionException {
		if (non_interact_graph == null)
			throw new PreconditionException("No graph available!");
	}
	
	/**
	 * euclidian distance
	 */
	private double getDistance(org.Vector3d a, org.Vector3d b) {
		return Math.sqrt(
							(a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) + (a.z - b.z) * (a.z - b.z)
							);
	}
	
	private double borderForceX(ThreadSafeOptions options, double x) {
		if (x < options.borderWidth) {
			return Math.max(-options.maxBorderForce / options.borderWidth * x
								+ options.maxBorderForce, 0);
		} else { // return 0;
			return -1;
		}
	}
	
	private double borderForceY(ThreadSafeOptions options, double y) {
		if (y < options.borderWidth) {
			return Math.max(-options.maxBorderForce / options.borderWidth * y
								+ options.maxBorderForce, 0);
		} else { // return 0;
			return -1;
		}
	}
	
	private double borderForceZ(ThreadSafeOptions options, double y) {
		if (y < options.borderWidth) {
			return Math.max(-options.maxBorderForce / options.borderWidth * y
								+ options.maxBorderForce, 0);
		} else { // return 0;
			return -1;
		}
	}
	
	private NodeCacheEntry3d getPatternNodeStructFromNode(
						ThreadSafeOptions options, Node search) {
		return (NodeCacheEntry3d) options.nodeSearch.get(search);
	}
	
	/**
	 * @return If position-update for Nodes are done, length of movement
	 *         vectors
	 */
	private double doSpringEmbedder(ThreadSafeOptions options, int runValue,
						int n) {
		double returnValue = 0;
		
		for (int i = 0; i < n; i++) {
			returnValue += doCalcAndMoveNode(options, runValue, returnValue, i);
		}
		return returnValue;
	}
	
	@SuppressWarnings("unchecked")
	double doCalcAndMoveNode(ThreadSafeOptions options, int runValue,
						double returnValue, int i) {
		NodeCacheEntry3d nodeI = (NodeCacheEntry3d) options.nodeArray.get(i);
		
		calcCounter++;
		
		boolean calcNode = true;
		
		// in case the node has been "touched" before, do not calc again
		if (nodeI.lastTouch >= runValue)
			calcNode = false;
		
		// in case the current option says, move only selected nodes and the current node
		// is not selected, then do not calc this node
		if (options.getSelection().getNodes().size() > 0 && !nodeI.selected)
			calcNode = false;
		
		if (calcNode) {
			org.Vector3d force = new org.Vector3d(0, 0, 0);
			org.Vector3d sumForce = new org.Vector3d(0, 0, 0);
			for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
				NodeCacheEntry3d patternNode = (NodeCacheEntry3d) nodeI.patternNodes
									.get(patternI);
				
				patternNode.lastTouch = runValue;
				calcSpringEmbedderForce(options, patternNode, force, sumForce);
			}
			double s0 = Math.abs(sumForce.x) + Math.abs(sumForce.y) + Math.abs(sumForce.z);
			// s0 = Double.MAX_VALUE;
			force.x /= nodeI.patternNodes.size();
			force.y /= nodeI.patternNodes.size();
			force.z /= nodeI.patternNodes.size();
			force.x /= 7;
			force.y /= 7;
			force.z /= 7;
			for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
				NodeCacheEntry3d patternNode = (NodeCacheEntry3d) nodeI.patternNodes
									.get(patternI);
				returnValue += moveNode(options, force, patternNode);
			}
			boolean calcRotation = nodeI.patternNodes.size() > 1 && options.getBval(myOp.BvalIndexRotatePatternIndex, false);
			if (calcRotation) {
				double plusMinusAngle = linearTransformation(options.temperature_max_move, 0, 300, 0, 10 * Math.PI / 180);
				Vector3d centerOfPattern = NodeTools.getCenter3d(nodeI.patternNodes);
				new ArrayList<Vector3d>();
				rotate(+plusMinusAngle, nodeI.patternNodes, centerOfPattern);
				org.Vector3d temp = new org.Vector3d(0, 0, 0);
				org.Vector3d forceS1 = new org.Vector3d(0, 0, 0);
				for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
					NodeCacheEntry3d patternNode = (NodeCacheEntry3d) nodeI.patternNodes.get(patternI);
					calcSpringEmbedderForce(options, patternNode, temp, forceS1);
				}
				rotate(-plusMinusAngle * 2, nodeI.patternNodes, centerOfPattern);
				org.Vector3d forceS2 = new org.Vector3d(0, 0, 0);
				for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
					NodeCacheEntry3d patternNode = (NodeCacheEntry3d) nodeI.patternNodes.get(patternI);
					calcSpringEmbedderForce(options, patternNode, temp, forceS2);
				}
				rotate(+plusMinusAngle, nodeI.patternNodes, centerOfPattern);
				double s1 = Math.abs(forceS1.x) + Math.abs(forceS1.y);
				double s2 = Math.abs(forceS2.x) + Math.abs(forceS2.y);
				// System.out.println("Rotation forces S0, S1, S2: "+s0+" / "+s1+" / "+s2);
				if (s1 < s0 && s1 < s2)
					rotate(+plusMinusAngle, nodeI.patternNodes, centerOfPattern);
				if (s2 < s0 && s2 < s1)
					rotate(-plusMinusAngle, nodeI.patternNodes, centerOfPattern);
			}
		}
		return returnValue;
	}
	
	private void rotate(double angle,
						ArrayList<NodeCacheEntry3d> patternNodes, Vector3d centerOfPattern) {
		AffineTransform transform = AffineTransform.getRotateInstance(angle, centerOfPattern.x, centerOfPattern.y);
		for (NodeCacheEntry3d nce : patternNodes) {
			double currentDistance = getDistance(nce.position, centerOfPattern);
			if (Math.abs(currentDistance) > 0.00001) {
				Point2D.Double ptSrc = new Point2D.Double(nce.position.x, nce.position.y);
				Point2D.Double ptDst = new Point2D.Double(nce.position.x, nce.position.y);
				transform.transform(ptSrc, ptDst);
				nce.position.x = ptDst.getX();;
				nce.position.y = ptDst.getY();
			}
		}
	}
	
	private double linearTransformation(double value, double minS, double maxS, double minT, double maxT) {
		if (value <= minS)
			return minT;
		if (value >= maxS)
			return maxT;
		return (value - minS) / (maxS - minS) * (maxT - minT) + minT;
	}
	
	/**
	 * Determines if two nodes belong to the same pattern.
	 * 
	 * @param n1
	 *           Node 1
	 * @param n2
	 *           Node 2
	 * @return True, if the two nodes belong to the same pattern. False, if not.
	 */
	private boolean samePattern(NodeCacheEntry3d n1, NodeCacheEntry3d n2) {
		boolean sameNode = n1.nodeIndex == n2.nodeIndex;
		
		if (sameNode) {
			return true;
		}
		
		boolean noPattern = ((n1.patternType.compareTo("") == 0) && (n2.patternType
							.compareTo("") == 0));
		
		if (noPattern) {
			return false;
		}
		
		boolean samePattern = (n1.patternType.compareTo(n2.patternType) == 0);
		boolean sameIndex = n1.patternIndex == n2.patternIndex;
		
		return samePattern && sameIndex;
	}
	
	/**
	 * Calculates the springembedder force for a given node. For all nodes the distance to the given node is calculated
	 * and used to calculate an average repulsive force.
	 * Optional a border force is used to move the nodes away from the top/left. This force also moves the nodes
	 * to left/top if the position is greater than the borderLengths.
	 * 
	 * @param options
	 *           The options
	 * @param nodeI
	 *           For this node the force will be calculated.
	 * @param force
	 *           The calculation result (RETURN/call by reference).
	 * @param sumForce
	 */
	private void calcSpringEmbedderForce(
						ThreadSafeOptions options, NodeCacheEntry3d nodeI, org.Vector3d force, Vector3d sumForce) {
		double distance;
		double distanceX;
		double distanceY;
		double distanceZ;
		
		double d1_1000 = options.getDval(myOp.DvalIndexSliderHorForce, 1000);
		double d2_1000 = options.getDval(myOp.DvalIndexSliderVertForce, 1000);
		
		double initFx = force.x;
		double initFy = force.y;
		double initFz = force.z;
		
		// Abstoßungskräfte zu restlichen Knoten
		int sz = options.nodeArray.size();
		for (int i2 = 0; i2 < sz; i2++) {
			NodeCacheEntry3d nodeI2 = (NodeCacheEntry3d) options.nodeArray.get(i2);
			
			if (calcForce(nodeI, nodeI2)) {
				org.Vector3d workA = nodeI.position;
				org.Vector3d workB = nodeI2.position;
				
				distance = getDistance(workA, workB);
				double d_sq = distance * distance;
				distanceX = workA.x - workB.x;
				distanceY = workA.y - workB.y;
				distanceZ = workA.z - workB.z;
				if (Math.abs(distanceZ) < 0.000001)
					force.z += Math.random() * 2 - 1;
				if (distance > 0) {
					double multiplyRepulsiveForces = 1d;
					if (nodeI.patternIndex >= 0 || nodeI2.patternIndex >= 0) {
						multiplyRepulsiveForces = options.getDval(myOp.DmultiplyRepulsiveForces2Patterns, 1d);
					}
					force.x += multiplyRepulsiveForces * d1_1000 / d_sq * (distanceX / distance);
					force.y += multiplyRepulsiveForces * d2_1000 / d_sq * (distanceY / distance);
					force.z += multiplyRepulsiveForces * d2_1000 / d_sq * (distanceZ / distance);
				} else {
					force.x += Math.random() * 2 - 1;
					force.y += Math.random() * 2 - 1;
					force.z += Math.random() * 2 - 1;
				}
			}
		}
		double dv0_10_stiffness = options.getDval(myOp.DvalIndexSliderStiffness,
							10);
		double dv3_200_zero_len = options.getDval(myOp.DvalIndexSliderZeroLength,
							200);
		// Anziehungskräfte zwischen verbundenen Knoten
		double sumAddX = 0;
		double sumAddY = 0;
		double sumAddZ = 0;
		if (!nodeI.connectedNodes.isEmpty()) {
			sz = nodeI.connectedNodes.size();
			for (int i2 = 0; i2 < sz; i2++) {
				NodeCacheEntry3d nodeI2 = (NodeCacheEntry3d) nodeI.connectedNodes
									.get(i2);
				
				if (calcForce(nodeI, nodeI2)) {
					org.Vector3d workA = nodeI.position;
					org.Vector3d workB = nodeI2.position;
					
					distance = getDistance(workA, workB);
					distanceX = workB.x - workA.x;
					distanceY = workB.y - workA.y;
					distanceZ = workB.z - workA.z;
					if (distance > 0) {
						// TODO: CK: Vorschlag von Falk:
						// Target-length of edge could be modified
						// by correlation factor!!!!
						// the higher the correlation, the longer the
						// target length should be.
						// A little problem:
						// what is with positive / negative correlations?
						// should the abs() be used, or should this be
						// threated some way?
						double currFx;
						double currFy;
						double currFz;
						currFx = dv0_10_stiffness / 10
											* (distance - dv3_200_zero_len) * distanceX / distance;
						currFy = dv0_10_stiffness / 10
											* (distance - dv3_200_zero_len) * distanceY / distance;
						currFz = dv0_10_stiffness / 10
											* (distance - dv3_200_zero_len) * distanceZ / distance;
						force.x += currFx;
						force.y += currFy;
						force.z += currFz;
						sumAddX += -currFx + dv0_10_stiffness / 10 * distanceX;
						sumAddY += -currFy + dv0_10_stiffness / 10 * distanceY;
						sumAddZ += -currFz + dv0_10_stiffness / 10 * distanceZ;
					} else {
						force.x += Math.random() * 2 - 1;
						force.y += Math.random() * 2 - 1;
						force.z += Math.random() * 2 - 1;
					}
				} // if calc force
			}
		}
		
		if (options.borderForce) {
			force.x += borderForceX(options, nodeI.position.x) / nodeI.patternNodes.size();
			force.y += borderForceY(options, nodeI.position.y) / nodeI.patternNodes.size();
			force.z += borderForceZ(options, nodeI.position.z) / nodeI.patternNodes.size();
		}
		
		if (options.getBval(myOp.BvalIndexDoClusterLayoutIndex, false)) {
			// apply cluster forces
			Vector3d clusterPosition = clusterLocations.get(nodeI.clusterIndexNumber);
			if (clusterPosition != null)
				applyMagneticClusterForce(options, force, nodeI.position,
									clusterPosition);
		}
		
		sumForce.x += Math.abs(force.x + sumAddX - initFx);
		sumForce.y += Math.abs(force.y + sumAddY - initFy);
		sumForce.z += Math.abs(force.z + sumAddZ - initFz);
	}
	
	/**
	 * Applies magnetic forces between a node (with a cluster index) and a cluster graph node that
	 * represents the cluster the node is belonging to. This force is attracting, this way all nodes
	 * that belong to one cluster will move to the position of the cluster representation node.
	 * 
	 * @param force
	 *           This force is modified
	 * @param nodePosition
	 * @param clusterPosition
	 */
	private void applyMagneticClusterForce(ThreadSafeOptions options,
						Vector3d force, Vector3d nodePosition, Vector3d clusterPosition) {
		options.getDval(
							myOp.DvalIndexSliderClusterForce, 20);
		double xdiff = nodePosition.x - clusterPosition.x;
		double ydiff = nodePosition.y - clusterPosition.y;
		double zdiff = nodePosition.z - clusterPosition.z;
		// System.out.println(clusterPosition);
		double len = Math.sqrt(xdiff * xdiff + ydiff * ydiff + zdiff * zdiff);
		force.x += -xdiff / len * cachedClusterForce;
		force.y += -ydiff / len * cachedClusterForce;
		force.z += -zdiff / len * cachedClusterForce;
	}
	
	/**
	 * Layout Algorithm
	 */
	public void execute() {
		
		// Graph gi = GravistoService.getInstance().getMainFrame().getActiveSession().getGraph();
		// Selection s = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getSelectionModel().getActiveSelection();
		
		MyNonInteractiveSpringEmb mse = new MyNonInteractiveSpringEmb(
							non_interact_graph, non_interact_selection, nonInteractiveTSO);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(mse, mse,
							"Force Directed Layout", "Force Directed Layout", true, false);
		bth.startWork(this);
		
		// JOptionPane.showMessageDialog(
		// GraffitiSingleton.getInstance().getMainFrame(),
		// "Use the Pattern Graffiti Plugins to start this plugin interactively.",
		// "This plugin currently can not be started from the Plugin-Menu",
		// JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * returns length of move vector
	 * 
	 * @param options
	 *           options var for the thread
	 * @param moveVec
	 *           movement vector (will be modified to be smaller than maxmove)
	 * @param node
	 *           node to be moved
	 * @return actual movement
	 */
	private double moveNode(ThreadSafeOptions options, org.Vector3d moveVec,
						NodeCacheEntry3d node) {
		double l = Math.sqrt(moveVec.x * moveVec.x + moveVec.y * moveVec.y);
		
		if (l > options.temperature_max_move) {
			moveVec.x = moveVec.x / l * options.temperature_max_move;
			moveVec.y = moveVec.y / l * options.temperature_max_move;
			moveVec.z = moveVec.z / l * options.temperature_max_move;
			l = options.temperature_max_move;
		}
		// if (moveVec.x!=Double.NaN && moveVec.y!=Double.NaN) {
		node.position.x += moveVec.x;
		node.position.y += moveVec.y;
		node.position.z += moveVec.z;
		return l;
		/*
		 * } else
		 * return 0;
		 */
	}
	
	/**
	 * Defines if a force between two nodes should be calculated.
	 * 
	 * @param n1
	 *           Node 1
	 * @param relN
	 *           Node 2
	 * @return True, if a force between the two nodes should be calculated,
	 *         false if not
	 */
	private boolean calcForce(NodeCacheEntry3d n1, NodeCacheEntry3d relN) {
		if (n1.nodeIndex == relN.nodeIndex) {
			return false;
		}
		
		if (n1.patternType.length() == 0) {
			return true;
		}
		
		return !samePattern(n1, relN);
	}
	
	/**
	 * Init method for cache
	 * 
	 * @param options
	 *           the thread-safe options
	 * @param nodeI
	 *           the node to be analyzed
	 * @return An <code>Vector</code> with the connected nodes. (<code>NodeCacheEntry3d</code> list)
	 */
	private ArrayList<NodeCacheEntry3d> getConnectedNodes(ThreadSafeOptions options,
						NodeCacheEntry3d nodeI) {
		ArrayList<NodeCacheEntry3d> connectedNodes = new ArrayList<NodeCacheEntry3d>();
		for (Node tempNode : nodeI.node.getNeighbors()) {
			NodeCacheEntry3d n2 = getPatternNodeStructFromNode(options, tempNode);
			if (n2 == null)
				System.err.println("ERROR: Node " + tempNode.getID() + " not found in nodeSearch map!");
			else
				connectedNodes.add(n2);
		}
		
		return connectedNodes;
	}
	
	/**
	 * Init cache entry for a node (get pattern nodes-call cache)
	 * 
	 * @param options
	 *           DOCUMENT ME!
	 */
	public void readPatternConnections(ThreadSafeOptions options) {
		for (int i = 0; i < options.nodeArray.size(); i++) {
			NodeCacheEntry3d pi = (NodeCacheEntry3d) options.nodeArray.get(i);
			
			pi.patternNodes = getPatternNodes(options, pi);
			
			pi.connectedNodes = getConnectedNodes(options, pi);
		}
	}
	
	public void setParameters(Parameter[] params) {
		initLength = ((DoubleParameter) params[0]).getDouble().doubleValue();
		nonInteractiveTSO.setDval(myOp.DvalIndexSliderZeroLength, ((DoubleParameter) params[0]).getDouble().doubleValue());
		nonInteractiveTSO.setDval(myOp.DvalIndexSliderHorForce, ((DoubleParameter) params[1]).getDouble().doubleValue());
		nonInteractiveTSO.setDval(myOp.DvalIndexSliderVertForce, ((DoubleParameter) params[2]).getDouble().doubleValue());
	}
	
	public Parameter[] getParameters() {
		if (nonInteractiveTSO == null) {
			nonInteractiveTSO = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
			nonInteractiveTSO.temp_alpha = 0.98;
		}
		double dv3_200_zero_len = nonInteractiveTSO.getDval(myOp.DvalIndexSliderZeroLength, 100);
		double d1_1000 = nonInteractiveTSO.getDval(myOp.DvalIndexSliderHorForce, 90000);
		double d2_1000 = nonInteractiveTSO.getDval(myOp.DvalIndexSliderVertForce, 90000);
		return new Parameter[] {
							new DoubleParameter(dv3_200_zero_len, "Target Edge Length", "The target length of the edges"),
							new DoubleParameter(d1_1000, "Horizontal Repulsion", "Strength of horizontal repulsion"),
							new DoubleParameter(d2_1000, "Vertical Repulsion", "Strength of vertical repulsion") };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#setControlInterface(javax.swing.JComponent)
	 */
	public boolean setControlInterface(final ThreadSafeOptions options,
						JComponent jc) {
		int border = 5;
		jc.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
		SingleFiledLayout sfl = new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 1);
		jc.setLayout(sfl);
		this.getClass().getClassLoader();
		this.getClass().getPackage().getName().replace('.', '/');
		// System.out.println("Image directory: " + path);
		// progressImg = new ImageIcon(cl.getResource(path + "/" + "waitSlow.gif"));
		// progressImgOK = new ImageIcon(cl.getResource(path + "/" + "waitOK.gif"));
		final JButton startStopButton = new JMButton("Layout Network (3D)");
		startStopButton.setToolTipText("Start or Stop Layout Algorithm (processes the graph in the active window)");
		startStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (startStopButton.getText().equalsIgnoreCase("Layout Network (3D)")) {
					try {
						startStopButton.setText("Stop Layouter");
						Thread newBackgroundThread = new Thread(new Runnable() {
							public void run() {
								options.setGraphInstance(GravistoService.getInstance()
													.getMainFrame().getActiveSession().getGraph());
								EditorSession session = GravistoService.getInstance()
													.getMainFrame().getActiveEditorSession();
								Selection selection = session.getSelectionModel()
													.getActiveSelection();
								options.setSelection(selection);
								executeThreadSafe(options);
							}
						}) {
											};
						newBackgroundThread.setName("SpringEmbedderLayout3d");
						newBackgroundThread.setPriority(Thread.MIN_PRIORITY);
						newBackgroundThread.start();
						// startStopButton.setIcon(progressImg);
						startStopButton.setVerticalTextPosition(SwingConstants.BOTTOM);
						startStopButton.setHorizontalTextPosition(SwingConstants.CENTER);
					} catch (NullPointerException err) {
						MainFrame.showMessageDialog("No active graph editor window found.", "Can not proceed");
					}
				} else {
					options.setAbortWanted(true);
					// startStopButton.setIcon(null);
				}
			}
		});
		
		jc.add(startStopButton);
		
		final JButton redrawButton = new JButton("Refresh View");
		redrawButton.setToolTipText("Make the current calculated graph layout visible");
		redrawButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				options.redraw = true;
			}
		});
		if (options.autoRedraw) {
			redrawButton.setEnabled(false);
			options.redraw = true;
		} else {
			redrawButton.setEnabled(true);
		}
		
		JCheckBox autoRefresh = new JCheckBox("Auto Redraw", options.autoRedraw);
		autoRefresh.setToolTipText(
							"<html>If selected, the graph view will be updated after each run of the spring embedder loop.<br>" +
												"This is useful to determine a good parameter setting, but slows down the execution speed to a large extend.");
		autoRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				options.autoRedraw = ((JCheckBox) arg0.getSource()).isSelected();
				if (options.autoRedraw) {
					redrawButton.setEnabled(false);
					options.redraw = true;
				} else {
					redrawButton.setEnabled(true);
				}
			}
		});
		
		JComponent helpButton = FolderPanel.getHelpButton(
							JLabelJavaHelpLink.getHelpActionListener("layout_force"), jc.getBackground());
		
		jc.add(TableLayout.getSplit(redrawButton,
							TableLayout.get3Split(
												autoRefresh,
												new JLabel(""),
												helpButton,
												TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED),
							TableLayout.PREFERRED, TableLayout.FILL));
		
		// ///////////////////////////
		JLabel labelSliderLength = new JLabel("Target Length of Edges:");
		
		JSlider sliderLength = new JSlider();
		sliderLength.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
		sliderLength.setMinimum(0);
		sliderLength.setMaximum(800);
		sliderLength.setToolTipText(
							"<html>This value determines the &quot;natural&quot; (zero energy)<br>" +
												"length of the graph edges (&quot;springs&quot;)");
		sliderLength.setMinorTickSpacing(50);
		sliderLength.setMajorTickSpacing(100);
		sliderLength.setPaintLabels(true);
		sliderLength.setPaintTicks(true);
		sliderLength.setLabelTable(sliderLength.createStandardLabels(100));
		sliderLength.setValue((int) options.getDval(
							myOp.DvalIndexSliderZeroLength, 200));
		
		jc.add(TableLayout.getDoubleRow(labelSliderLength, sliderLength, Color.WHITE));
		
		sliderLength.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.setDval(myOp.DvalIndexSliderZeroLength, ((JSlider) e
									.getSource()).getValue());
			}
		});
		
		sliderLength.setAlignmentX(10);
		sliderLength.setAlignmentY(70);
		
		JSlider sliderEnergyHor = new JSlider();
		sliderEnergyHor.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
		sliderEnergyHor.setMinimum(0);
		sliderEnergyHor.setMaximum(1000000);
		sliderEnergyHor.setMinorTickSpacing(50000);
		sliderEnergyHor.setMajorTickSpacing(200000);
		sliderEnergyHor.setPaintLabels(true);
		sliderEnergyHor.setPaintTicks(true);
		Dictionary<Integer, Component> d = new Hashtable<Integer, Component>();
		d.put(new Integer(0), new JLabel("low repulsion"));
		d.put(new Integer(1000000), new JLabel("high repulsion"));
		sliderEnergyHor.setLabelTable(d);
		sliderEnergyHor.setToolTipText(
							"<html>This value determines the horizontal<br>" +
												"repulsive energy between all nodes");
		sliderEnergyHor.setValue((int) options.getDval(
							myOp.DvalIndexSliderHorForce, 1000));
		
		sliderEnergyHor.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.setDval(myOp.DvalIndexSliderHorForce, ((JSlider) e
									.getSource()).getValue());
			}
		});
		jc.add(TableLayout.getDoubleRow(new JLabel("Horizontal Repulsive Force Between Nodes:"), sliderEnergyHor, Color.WHITE));
		
		JSlider sliderEnergyVert = new JSlider();
		
		sliderEnergyVert.setMinimum(0);
		sliderEnergyVert.setMaximum(1000000);
		sliderEnergyVert.setMinorTickSpacing(50000);
		sliderEnergyVert.setMajorTickSpacing(200000);
		sliderEnergyVert.setPaintLabels(true);
		sliderEnergyVert.setPaintTicks(true);
		Dictionary<Integer, Component> d2 = new Hashtable<Integer, Component>();
		d2.put(new Integer(0), new JLabel("low repulsion"));
		d2.put(new Integer(1000000), new JLabel("high repulsion"));
		sliderEnergyVert.setLabelTable(d2);
		sliderEnergyVert.setValue((int) options.getDval(
							myOp.DvalIndexSliderVertForce, 1000));
		
		jc.add(TableLayout.getDoubleRow(new JLabel("Vertical Repulsive Force Between Nodes:"), sliderEnergyVert, Color.WHITE));
		sliderEnergyVert.setToolTipText(
							"<html>This value determines the vertical<br>repulsive energy between all nodes");
		sliderEnergyVert.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.setDval(myOp.DvalIndexSliderVertForce, ((JSlider) e
									.getSource()).getValue());
			}
		});
		
		// if (ReleaseInfo.getIsAllowedFeature(FeatureSet.PATTERN_LAYOUT)) {
		JSlider sliderMultiplyRepulsive = new JSlider();
		// sliderMultiplyRepulsive.setBackground(Color.YELLOW);
		sliderMultiplyRepulsive.setMinimum(-1);
		sliderMultiplyRepulsive.setMaximum(10);
		sliderMultiplyRepulsive.setMajorTickSpacing(1);
		sliderMultiplyRepulsive.setMinorTickSpacing(1);
		Dictionary<Integer, Component> dMF = new Hashtable<Integer, Component>();
		dMF.put(new Integer(-1), new JLabel("-1x"));
		dMF.put(new Integer(0), new JLabel("0x"));
		dMF.put(new Integer(1), new JLabel("1x"));
		dMF.put(new Integer(3), new JLabel("3x"));
		dMF.put(new Integer(5), new JLabel("5x"));
		dMF.put(new Integer(8), new JLabel("8x"));
		dMF.put(new Integer(10), new JLabel("10x"));
		sliderMultiplyRepulsive.setLabelTable(dMF);
		sliderMultiplyRepulsive.setPaintLabels(true);
		sliderMultiplyRepulsive.setPaintTicks(true);
		sliderMultiplyRepulsive.setValue((int) options.getDval(myOp.DmultiplyRepulsiveForces2Patterns, 1d));
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH))
			jc
								.add(TableLayout.getDoubleRow(new JLabel("Multiply repulsive Forces between Patterns and remaining Network:"), sliderMultiplyRepulsive,
													Color.WHITE));
		
		sliderMultiplyRepulsive.setToolTipText(
							"<html>This value determines a multipicator for the repulsive energy between pattern nodes and the remaining nodes");
		sliderMultiplyRepulsive.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.setDval(myOp.DmultiplyRepulsiveForces2Patterns, ((JSlider) e.getSource()).getValue());
			}
		});
		// }
		JLabel stiffnessDesc = new JLabel("Stiffness of Springs:");
		
		JSlider sliderStiffnes = new JSlider();
		sliderStiffnes.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
		sliderStiffnes.setToolTipText("Modifes the forces determined by the connection to other nodes (edge target length).");
		
		sliderStiffnes.setMinimum(0);
		sliderStiffnes.setMaximum(75);
		sliderStiffnes.setMinorTickSpacing(10);
		sliderStiffnes.setMajorTickSpacing(10);
		sliderStiffnes.setPaintLabels(true);
		sliderStiffnes.setPaintTicks(true);
		sliderStiffnes.setValue((int) options.getDval(myOp.DvalIndexSliderStiffness, 10d));
		Dictionary<Integer, Component> d3 = new Hashtable<Integer, Component>();
		d3.put(new Integer(0), new JLabel("0"));
		d3.put(new Integer(10), new JLabel("1 (norm)"));
		d3.put(new Integer(50), new JLabel("5 (strong)"));
		sliderStiffnes.setLabelTable(d3);
		// sliderStiffnes.setLabelTable(sliderStiffnes.createStandardLabels(10));
		jc.add(TableLayout.getDoubleRow(stiffnessDesc, sliderStiffnes, Color.WHITE));
		sliderStiffnes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.setDval(myOp.DvalIndexSliderStiffness, ((JSlider) e
									.getSource()).getValue());
			}
		});
		
		// final JCheckBox useIndepClusterLayout = new JCheckBox();
		final JCheckBox useClusterInfo = new JCheckBox();
		useClusterInfo.setToolTipText(
							"<html>If selected, a clustered graph will be processed in a way so that a additional " +
												"&quot;Cluster-Force&quot;<br>" +
												"towards the direction of cluster-reference-nodes is applied");
		useClusterInfo.setSelected(options.getBval(
							myOp.BvalIndexDoClusterLayoutIndex, false));
		useClusterInfo.setText("Apply Cluster-Force:");
		useClusterInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean newVal = ((JCheckBox) e.getSource()).isSelected();
				options.setBval(myOp.BvalIndexDoClusterLayoutIndex, newVal);
				options.setBval(myOp.BvalIndexDoIndividualClusterLayoutIndex,
									!newVal);
			}
		});
		
		JSlider sliderClusterForce = new JSlider();
		sliderClusterForce.setMinimum(0);
		sliderClusterForce.setMaximum(1000);
		sliderClusterForce.setMinorTickSpacing(50);
		sliderClusterForce.setMajorTickSpacing(100);
		sliderClusterForce.setPaintLabels(true);
		sliderClusterForce.setPaintTicks(true);
		Dictionary<Integer, Component> d4 = new Hashtable<Integer, Component>();
		d4.put(new Integer(0), new JLabel("zero force"));
		d4.put(new Integer(500), new JLabel("average force"));
		d4.put(new Integer(1000), new JLabel("strong force"));
		sliderClusterForce.setLabelTable(d4);
		sliderClusterForce.setValue((int) options.getDval(
							myOp.DvalIndexSliderClusterForce, myOp.InitClusterForce));
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			jc.add(useClusterInfo);
			jc.add(sliderClusterForce);
			jc.add(TableLayout.getDoubleRow(useClusterInfo, sliderClusterForce, Color.WHITE));
		}
		sliderClusterForce.setToolTipText(
							"<html>This value determines the constant additional node-force" +
												"<br>towards " +
												"the position of the cluster-reference-nodes in the cluster-graph.");
		sliderClusterForce.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.setDval(myOp.DvalIndexSliderClusterForce, ((JSlider) e
									.getSource()).getValue());
				if (!useClusterInfo.isSelected()) {
					useClusterInfo.doClick();
				}
			}
		});
		
		final JSlider tempSlider = new JSlider();
		tempSlider.setMinimum(0);
		tempSlider.setMaximum(300);
		tempSlider.setMinorTickSpacing(25);
		tempSlider.setMajorTickSpacing(50);
		tempSlider.setPaintLabels(true);
		tempSlider.setPaintTicks(true);
		tempSlider.setValue((int) options.temperature_max_move);
		tempSlider.setLabelTable(tempSlider.createStandardLabels(50));
		tempSlider.setToolTipText(
							"<html>" +
												"<b>Move this slider to decrease or increase the run-time of the algorithm</b><br>" +
												"This value determines the maximum node movement during one layout-loop run." +
												"");
		tempSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.temperature_max_move = ((JSlider) e.getSource()).getValue();
			}
		});
		jc.add(TableLayout.getDoubleRow(new JLabel("Maximum Node Movement (progress):"), tempSlider, Color.WHITE));
		
		JCheckBox borderForce = new JCheckBox("Border Force", options.borderForce);
		borderForce.setToolTipText(
							"<html>If selected, a force will be added, which lets the nodes<br>" +
												"move slowly to the top left. The nodes will avoid movement towards negative coordinates.");
		borderForce.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				options.borderForce = ((JCheckBox) e.getSource()).isSelected();
			}
		});
		// jc.add(borderForce);
		
		// JCheckBox useSelection = new JCheckBox();
		// useSelection.setText("Work on Selection");
		// useSelection.setToolTipText("If seleceted, the not selected nodes will have a fixed position");
		// useSelection.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// boolean newVal = ((JCheckBox) e.getSource()).isSelected();
		// options.setBval(myOp.BvalIndexMoveSelectionIndex, newVal);
		// }
		// });
		// jc.add(useSelection);
		
		JCheckBox randomInit = new JCheckBox("Init: Random Node Positions",
							options.doRandomInit);
		randomInit.setToolTipText(
							"<html>If selected, the graph will have a random layout applied<br>" +
												"before executing the spring embedder layouter");
		randomInit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				options.doRandomInit = ((JCheckBox) e.getSource()).isSelected();
			}
		});
		jc.add(randomInit);
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH)) {
			JCheckBox copyLayout = new JCheckBox("Init: Apply Search-Subgraph Layout",
								options.doCopyPatternLayout);
			
			copyLayout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					options.doCopyPatternLayout = ((JCheckBox) e.getSource())
										.isSelected();
				}
			});
			jc.add(copyLayout);
		}
		
		JCheckBox removeOverlapping = new JCheckBox("Finish: Remove Node Overlaps",
							options.doFinishRemoveOverlapp);
		removeOverlapping.setToolTipText(
							"If selected, the final layout will be modified to remove any node overlaps"
							);
		removeOverlapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				options.doFinishRemoveOverlapp = ((JCheckBox) e.getSource())
									.isSelected();
			}
		});
		jc.add(removeOverlapping);
		
		JCheckBox finishToTop = new JCheckBox("Finish: Move Network to Upper-Left",
							options.doFinishMoveToTop);
		finishToTop.setToolTipText(
							"If selected, all network elements will be moved to the upper left of the view");
		finishToTop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				options.doFinishMoveToTop = ((JCheckBox) e.getSource())
									.isSelected();
			}
		});
		jc.add(finishToTop);
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH)) {
			JCheckBox rotatePattern = new JCheckBox(
								"Rotate Patterns", options.getBval(
													myOp.BvalIndexRotatePatternIndex, false));
			
			rotatePattern.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean newVal = ((JCheckBox) e.getSource()).isSelected();
					options.setBval(myOp.BvalIndexRotatePatternIndex, newVal);
				}
			});
			jc.add(rotatePattern);
		}
		
		// useIndepClusterLayout.setSelected(options.getBval(
		// myOp.BvalIndexDoIndividualClusterLayoutIndex, false));
		// useIndepClusterLayout.setText("Do individual Cluster Layout");
		// useIndepClusterLayout.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// boolean newVal = ((JCheckBox) e.getSource()).isSelected();
		// options.setBval(myOp.BvalIndexDoIndividualClusterLayoutIndex,
		// newVal);
		// options.setBval(myOp.BvalIndexDoClusterLayoutIndex, !newVal);
		// if (newVal)
		// useClusterInfo.setSelected(!useIndepClusterLayout.isSelected());
		// }
		// });
		// jc.add(useIndepClusterLayout, "1,21");
		
		// /////////////////////////////
		// double threadBorder = 2;
		// double[][] threadSize = {
		// { threadBorder, TableLayout.PREFERRED, TableLayoutConstants.FILL,
		// threadBorder },
		// // Columns
		// { threadBorder, TableLayout.PREFERRED, threadBorder } }; // Rows
		// JPanel componentForThreadSetting = new JPanel();
		// componentForThreadSetting.setLayout(new TableLayout(threadSize));
		
		// JLabel threadDesc = new JLabel("Thread-Count:");
		
		// JSpinner maxThreads = new JSpinner();
		// maxThreads.addChangeListener(new ChangeListener() {
		//
		// public void stateChanged(ChangeEvent e) {
		// try {
		// options.maxThreads = ((Integer) ((JSpinner) e.getSource())
		// .getValue()).intValue();
		// if (options.maxThreads < 0)
		// options.maxThreads = 0;
		// if (options.maxThreads > maxThreadCount)
		// options.maxThreads = maxThreadCount;
		// } finally {
		// ((JSpinner) e.getSource()).setValue(new Integer(
		// options.maxThreads));
		// }
		// }
		// });
		// componentForThreadSetting.add(threadDesc, "1,1");
		// componentForThreadSetting.add(maxThreads, "2,1");
		// componentForThreadSetting.validate();
		// jc.add(componentForThreadSetting, "1,21");
		
		sliderLength.setValue(100);
		// sliderStiffnes.setValue(10);
		sliderEnergyHor.setValue(90000);
		sliderEnergyVert.setValue(90000);
		
		jc.validate();
		
		// /////////////////////////////////////////
		// TIMER
		
		Timer runCheckTimer = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switch (options.runStatus) {
					case 0:
							// startStopButton.setIcon(null); // not started
							break;
						case 1:
							// startStopButton.setIcon(progressImg); // running
							startStopButton.setText("Stop Layout (Running)");
							break;
						case 2:
							// startStopButton.setIcon(progressImgOK); // idle
							startStopButton.setText("Stop Layout (Idle)");
							break;
						case 3:
							// startStopButton.setIcon(null); // finished
							startStopButton.setText("Layout Network (3D)");
							if (tempSlider.getValue() == 0) {
								options.temperature_max_move = 300;
							}
							break;
					}
					// set max movement / temperature slider value to current value
					tempSlider.setValue((int) options.temperature_max_move);
				}
		});
		runCheckTimer.start();
		
		return true;
	}
	
	/**
	 * Initialized the node cache structures.
	 * 
	 * @param options
	 *           The options to use
	 */
	@SuppressWarnings("unchecked")
	public void resetDataCache(ThreadSafeOptions options) {
		options.nodeArray = new ArrayList<NodeCacheEntry3d>();
		options.nodeSearch = new HashMap<Node, NodeCacheEntry3d>();
		MyTools.initNodeCache3d(options.nodeArray, options.nodeSearch,
							options.getGraphInstance(), options.getSelection());
		readPatternConnections(options);
	}
	
	// private Timer errorDebugTimer = new Timer(500, new DebugSelectionCheck());
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#executeThreadSafe(org.graffiti.plugin.algorithm.ThreadSafeOptions)
	 */
	public void executeThreadSafe(final ThreadSafeOptions options) {
		
		// if (!errorDebugTimer.isRunning()) {
		// DebugSelectionCheck.setCheckThis(options);
		// errorDebugTimer.setRepeats(true);
		// errorDebugTimer.start();
		// }
		if (options.doRandomInit) {
			RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
			rla.enable3d();
			rla.attach(options.getGraphInstance(), options.getSelection());
			rla.execute();
		}
		
		if (options.doCopyPatternLayout) {
			GravistoService.getInstance().runPlugin(new CopyPatternLayoutAlgorithm().getName(),
								options.getGraphInstance(), null);
		}
		
		// Remove bends
		if (options.doRemoveAllBends)
			GraphHelper.removeAllBends(options.getGraphInstance(), true);
		
		resetDataCache(options);
		
		doClusterInitialization(options);
		
		int runValue = 0;
		
		long loopTime = 0;
		
		double moveRun;
		
		HashMap<CoordinateAttribute, Vector3d> oldPositions = new HashMap<CoordinateAttribute, Vector3d>();
		HashMap<CoordinateAttribute, Vector3d> newPositions = new HashMap<CoordinateAttribute, Vector3d>();
		
		GraphHelper.enumerateNodePositions3d(options.getGraphInstance(), oldPositions);
		
		int n = options.getGraphInstance().getNumberOfNodes();
		
		Graph clusterGraph = (Graph) AttributeHelper.getAttributeValue(options
							.getGraphInstance(), "cluster", "clustergraph", null,
							new AdjListGraph());
		boolean clusterGraphAvailable = clusterGraph != null;
		boolean idleCheckResultOK = true;
		do {
			runValue++;
			
			loopTime = System.currentTimeMillis();
			calcCounter = 0;
			moveRun = doSpringEmbedder(options, runValue, n);
			
			options.temperature_max_move *= options.temp_alpha;
			if (options.redraw) {
				propagateCachedGraphPositions(options);
			}
			
			if (!options.autoRedraw) {
				options.redraw = false;
			}
			
			cachedClusterForce = options.getDval(myOp.DvalIndexSliderClusterForce,
								myOp.InitClusterForce);
			
			loopTime = System.currentTimeMillis() - loopTime;
			
			if (moveRun <= 0.1) {
				try {
					options.runStatus = 2; // idle
					MainFrame.showMessage("Spring Embedder 3d - IDLE",
										MessageType.INFO, 10000);
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// ignore (no problem)
				}
			} else {
				options.runStatus = 1; // running
				String clusterMessage = "";
				boolean useClusterLayout = options.getBval(
									myOp.BvalIndexDoClusterLayoutIndex, false);
				if (clusterGraphAvailable && useClusterLayout)
					clusterMessage = ", considering cluster-information";
				if (!clusterGraphAvailable && useClusterLayout)
					clusterMessage = ", no cluster-information available";
				
				MainFrame.showMessage(
									"Force Directed Layout 3d: RUNNING (max single node movements:"
														+ Math.round(options.temperature_max_move)
														+ ", loop time:" + loopTime
														+ " ms, max. sum of node movement: " + moveRun
														+ clusterMessage + ")", MessageType.PERMANENT_INFO);
			}
			if (options.getBval(myOp.BvalIndexStopWhenIdle, false)) {
				if (options.runStatus == 2)
					idleCheckResultOK = false;
			}
		} while (!options.isAbortWanted()
							&& options.temperature_max_move > 0.1 && idleCheckResultOK);
		
		propagateCachedGraphPositions(options);
		
		GraphHelper.enumerateNodePositions3d(options.getGraphInstance(), newPositions);
		
		GraphHelper.postUndoableChanges3d(options.getGraphInstance(), oldPositions, newPositions, getName());
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				if (options.doFinishRemoveOverlapp) {
					// int enlDir = getEnlargeDirectionFromNodesSize(options.nodeArray);
					GravistoService.getInstance().runAlgorithm(
										new NoOverlappLayoutAlgorithmAS(
															5, 5),
										options.getGraphInstance(),
										options.getSelection(), getActionEvent());
				}
				if (options.doFinishMoveToTop)
					GravistoService.getInstance().runAlgorithm(
										new CenterLayouterAlgorithm(),
										options.getGraphInstance(),
										new Selection(""), getActionEvent());
				// if (options.redraw && createGIF) {
				// if (agif!=null) {
				// try {
				// agif.encode();
				// } catch (IOException e) {
				// ErrorMsg.addErrorMessage(e);
				// }
				// }
				// }
				MainFrame.showMessage("Spring Embedder 3d - STOP", MessageType.INFO, 3000);
			}
		});
		
		options.setAbortWanted(false);
		
		options.runStatus = 3; // finished
	}
	
	/**
	 * @param options
	 */
	private void doClusterInitialization(final ThreadSafeOptions options) {
		clusterLocations.clear();
		// Collection clusters = GraphHelper.getClusters(options.getGraphInstance().getNodes());
		Graph clusterGraph = (Graph) AttributeHelper.getAttributeValue(options
							.getGraphInstance(), "cluster", "clustergraph", null,
							new AdjListGraph());
		boolean clusterGraphAvailable = clusterGraph != null;
		if (clusterGraphAvailable) {
			for (Iterator<?> it = clusterGraph.getNodesIterator(); it.hasNext();) {
				Node clusterNode = (Node) it.next();
				String clusterId = NodeTools.getClusterID(clusterNode, "");
				if (clusterId.equals("")) {
					ErrorMsg.addErrorMessage("Cluster-Graph-Node with no Cluster ID found!");
				} else {
					clusterLocations.put(clusterId, AttributeHelper.getPositionVec3d(clusterNode, false));
				}
			}
		}
	}
	
	private void propagateCachedGraphPositions(final ThreadSafeOptions options) {
		try {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						propagatePositions(options);
					}
				});
			} else {
				propagatePositions(options);
			}
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (InvocationTargetException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	// AnimGifEncoder agif;
	// boolean createGIF = false;
	int pictureCount = 0;
	
	private void propagatePositions(final ThreadSafeOptions options) {
		options.getGraphInstance().getListenerManager()
							.transactionStarted(this);
		for (int i = 0; i < options.nodeArray.size(); i++) {
			NodeCacheEntry3d curNode = (NodeCacheEntry3d) options.nodeArray
								.get(i);
			
			MyTools.setXYZ(curNode.node, curNode.position.x,
								curNode.position.y, curNode.position.z);
		}
		options.getGraphInstance().getListenerManager()
							.transactionFinished(this);
		// if (createGIF) {
		// if (agif == null) {
		// try {
		// agif = new AnimGifEncoder(new FileOutputStream("/tmp/lastanim.gif"));
		// } catch (FileNotFoundException e) {
		// ErrorMsg.addErrorMessage(e);
		// }
		// }
		// try {
		// pictureCount++;
		// if (true){
		// BufferedImage bi = PNGAlgorithm.getActiveGraphViewImage(BufferedImage.TYPE_BYTE_INDEXED, "gif", null);
		// if (bi!=null)
		// agif.add(bi, 40);
		// }
		// } catch (IOException e) {
		// ErrorMsg.addErrorMessage(e);
		// }
		// }
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#attach(org.graffiti.graph.Graph)
	 */
	public void attach(Graph g, Selection s) {
		non_interact_graph = g;
		non_interact_selection = s;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	public void reset() {
		nonInteractiveTSO.setDval(myOp.DvalIndexSliderZeroLength, initLength);
		nonInteractiveTSO.temperature_max_move = initLength;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
	 */
	public String getCategory() {
		return "Layout";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#isLayoutAlgorithm()
	 */
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	public String getDescription() {
		//
		return null;
	}
	
	// /* (non-Javadoc)
	// * @see org.graffiti.plugin.algorithm.EditorAlgorithm#getParameterDialog(org.graffiti.selection.Selection)
	// */
	// public ParameterDialog getParameterDialog(Selection sel) {
	// if (nonInteractiveTSO==null)
	// nonInteractiveTSO = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
	// return new SpringEmbedderParameterDialog(MainFrame.getInstance(), true, nonInteractiveTSO, this);
	// }
}
