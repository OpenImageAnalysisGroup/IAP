/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 04.06.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder_no_cache;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeAlgorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.copy_pattern_layout.CopyPatternLayoutAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.NodeCacheEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;

/*
 * K1 | dValues: 0
 * K2hor | dValues: 1
 * K2vert | dValues: 2
 * nat_l | dValues: 3
 */

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
public class PatternSpringembedder
					extends ThreadSafeAlgorithm {
	
	/**
	 * Sets Menu Command Title
	 * 
	 * @return Menu command title
	 */
	public String getName() {
		return null; // "Pattern Springembedder (no cache)";
	}
	
	public ActionEvent getActionEvent() {
		return null;
	}
	
	public void setActionEvent(ActionEvent a) {
		// empty
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * This method returns a <code>Vector</code> with all <code>NodeCacheEntry</code> entries that have the same pattern type
	 * and index.
	 * 
	 * @param options
	 *           DOCUMENT ME!
	 * @param nodeInfo
	 * @return <code>Vector</code> with <code>NodeCacheEntry</code> Objects.
	 *         The node is returned in the result set, if it has no pattern.
	 */
	private ArrayList<NodeCacheEntry> getPatternNodes(ThreadSafeOptions options,
						NodeCacheEntry nodeInfo) {
		ArrayList<NodeCacheEntry> resultVec = new ArrayList<NodeCacheEntry>();
		
		if (!nodeInfo.patternTypeEmpty) {
			for (int i = 0; i < options.nodeArray.size(); i++) {
				if (((NodeCacheEntry) options.nodeArray.get(i)).patternType
									.compareTo("") != 0) {
					if ((((NodeCacheEntry) options.nodeArray.get(i)).patternType
										.compareTo(nodeInfo.patternType) == 0)
										&& (((NodeCacheEntry) options.nodeArray.get(i)).patternIndex == nodeInfo.patternIndex)) {
						resultVec.add((NodeCacheEntry) options.nodeArray.get(i));
					}
				}
				
				// patternType <> null
			}
		} else {
			resultVec.add(nodeInfo);
		}
		
		if (resultVec.size() > 1) {
			// System.out.println("PC: " + resultVec.size());
		}
		
		return resultVec;
	}
	
	/**
	 * Error Checking
	 * 
	 * @throws PreconditionException
	 *            DOCUMENT ME!
	 */
	public void check() {
		// empty
	}
	
	/**
	 * euclidian distance
	 * 
	 * @param a
	 *           DOCUMENT ME!
	 * @param b
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private double getDistance(Vector2d a, Vector2d b) {
		return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param options
	 *           DOCUMENT ME!
	 * @param x
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private double borderForceX(ThreadSafeOptions options, double x) {
		if (x < options.borderWidth) {
			return Math.max(-options.maxBorderForce / options.borderWidth * x
								+ options.maxBorderForce, 0);
		} else { // return 0;
			return -1;
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param options
	 *           DOCUMENT ME!
	 * @param y
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private double borderForceY(ThreadSafeOptions options, double y) {
		if (y < options.borderWidth) {
			return Math.max(-options.maxBorderForce / options.borderWidth * y
								+ options.maxBorderForce, 0);
		} else { // return 0;
			return -1;
		}
	}
	
	/**
	 * Returns the Cache Entry for a given Graffiti-Node
	 * 
	 * @param options
	 *           The cache
	 * @param search
	 *           The node to search
	 * @return The cache entry
	 */
	private NodeCacheEntry getPatternNodeStructFromNode(ThreadSafeOptions options,
						Node search) {
		return (NodeCacheEntry) options.nodeSearch.get(search);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param options
	 *           DOCUMENT ME!
	 * @param maxMove
	 *           Collection of Graffiti nodes
	 * @param runValue
	 *           Collection of<code>Vector2d</code> Node positions
	 * @return If position-update for Nodes are done, length of movement
	 *         vectors
	 */
	private double doSpringEmbedder(ThreadSafeOptions options, double maxMove,
						int runValue) {
		double returnValue = 0;
		
		int i; // Index current node
		
		int n = options.getGraphInstance().getNumberOfNodes();
		
		for (i = 0; i < n; i++) {
			NodeCacheEntry nodeI = (NodeCacheEntry) options.nodeArray.get(i);
			
			if (nodeI.lastTouch < runValue) {
				Vector2d force = new Vector2d(0, 0);
				
				for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
					NodeCacheEntry patternNode =
										(NodeCacheEntry) nodeI.patternNodes.get(patternI);
					
					patternNode.lastTouch = runValue;
					
					calcSpringEmbedderForce(options, patternNode, force);
				}
				
				force.x /= nodeI.patternNodes.size();
				force.y /= nodeI.patternNodes.size();
				
				force.x /= options.nodeArray.size();
				force.y /= options.nodeArray.size();
				
				for (int patternI = 0; patternI < nodeI.patternNodes.size(); patternI++) {
					NodeCacheEntry patternNode =
										(NodeCacheEntry) nodeI.patternNodes.get(patternI);
					
					returnValue += moveNode(options, maxMove, force,
										patternNode);
				}
			}
		}
		
		return returnValue;
	}
	
	/**
	 * Checks if two given Cache-Nodes belong to the same pattern
	 * 
	 * @param n1
	 *           Cache-Node 1
	 * @param n2
	 *           Cache-Node 2
	 * @return True, if they belong to the same pattern. True, if n1 and n2
	 *         store info about the same node. False, if they both belong to no pattern.
	 */
	private boolean samePattern(NodeCacheEntry n1, NodeCacheEntry n2) {
		boolean sameNode = n1.nodeIndex == n2.nodeIndex;
		
		if (sameNode) {
			return true;
		}
		
		boolean noPattern =
							((n1.patternTypeEmpty)
							&& (n2.patternTypeEmpty));
		
		if (noPattern) {
			return false;
		}
		
		boolean samePattern = (n1.patternType.compareTo(n2.patternType) == 0);
		boolean sameIndex = n1.patternIndex == n2.patternIndex;
		
		return samePattern && sameIndex;
	}
	
	/**
	 * Calculates the spring embedder force
	 * 
	 * @param options
	 *           Options
	 * @param nodeI
	 *           The node for which the force is calculated
	 * @param force
	 *           The result of the calculation is stored in this Vector
	 */
	private void calcSpringEmbedderForce(ThreadSafeOptions options,
						NodeCacheEntry nodeI, Vector2d force) {
		double distance;
		double distanceX;
		double distanceY;
		
		Vector2d workA = new Vector2d(-1, -1);
		Vector2d workB = new Vector2d(-1, -1);
		
		// Abstoßungskräfte zu restlichen Knoten
		if (options.nodeArray.size() > 0) {
			for (int i2 = 0; i2 < options.nodeArray.size(); i2++) {
				NodeCacheEntry nodeI2 =
									(NodeCacheEntry) options.nodeArray.get(i2);
				
				if (calcForce(nodeI, nodeI2)) {
					MyTools.getPosition(nodeI.node, workA);
					MyTools.getPosition(nodeI2.node, workB);
					
					distance = getDistance(workA, workB);
					distanceX = workA.x - workB.x;
					distanceY = workA.y - workB.y;
					if (distance > 0) {
						force.x += options.getDval(1, 1000) / (distance * distance) * (distanceX / distance);
						force.y += options.getDval(2, 1000) / (distance * distance) * (distanceY / distance);
					} else {
						// force.x += Math.random() * 400 - 200;
						// force.y += Math.random() * 400 - 200;
					}
				}
			}
		}
		
		// Anziehungskräfte zwischen verbundenen Knoten
		if (!nodeI.connectedNodes.isEmpty()) {
			for (int i2 = 0; i2 < nodeI.connectedNodes.size(); i2++) {
				NodeCacheEntry nodeI2 =
									(NodeCacheEntry) nodeI.connectedNodes.get(i2);
				
				if (calcForce(nodeI, nodeI2)) {
					MyTools.getPosition(nodeI.node, workA);
					MyTools.getPosition(nodeI2.node, workB);
					
					distance = getDistance(workA, workB);
					distanceX = workB.x - workA.x;
					distanceY = workB.y - workA.y;
					
					if (distance > 0) {
						force.x += options.getDval(0, 10) / 10 * (distance
											- options.getDval(3, 200)) * distanceX / distance;
						force.y += options.getDval(0, 10) / 10 * (distance
											- options.getDval(3, 200)) * distanceY / distance;
					} else {
						// force.x += Math.random() * 400 - 200;
						// force.y += Math.random() * 400 - 200;
					}
				}
			}
		}
		
		if (options.borderForce) {
			force.x += borderForceX(options,
								MyTools.getX(nodeI.node));
			force.y += borderForceY(options,
								MyTools.getY(nodeI.node));
		}
	}
	
	/**
	 * Layout Algorithm
	 */
	public void execute() {
		JOptionPane.showMessageDialog(GravistoService.getInstance().getMainFrame(),
							"Use the Pattern Graffiti Plugins to start this plugin interactively.",
							"This plugin currently can not be started from the Plugin-Menu",
							JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * returns length of move vector
	 * 
	 * @param temperature_max_move_local
	 *           max movement length
	 * @param temperature_max_move_local
	 *           DOCUMENT ME!
	 * @param moveVec
	 *           movement vector (will be modified to be smaller than
	 *           maxmove)
	 * @param node
	 *           node to be moved
	 * @return actual movement
	 */
	private double moveNode(ThreadSafeOptions options,
						double temperature_max_move_local,
						Vector2d moveVec, NodeCacheEntry node) {
		double l = Math.sqrt(moveVec.x * moveVec.x + moveVec.y * moveVec.y);
		
		if (l > temperature_max_move_local) {
			moveVec.x = moveVec.x / l * temperature_max_move_local;
			moveVec.y = moveVec.y / l * temperature_max_move_local;
			l = temperature_max_move_local;
		}
		
		MyTools.setXY(node.node, MyTools.getX(node.node) + moveVec.x,
							MyTools.getY(node.node) + moveVec.y);
		
		return l;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Parameter[] getParameters() {
		return null;
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
	private boolean calcForce(NodeCacheEntry n1, NodeCacheEntry relN) {
		if (n1.nodeIndex == relN.nodeIndex) {
			return false;
		}
		
		if (n1.patternTypeEmpty) {
			return true;
		}
		
		return !samePattern(n1, relN);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param options
	 *           DOCUMENT ME!
	 * @param nodeI
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private ArrayList<NodeCacheEntry> getConnectedNodes(ThreadSafeOptions options,
						NodeCacheEntry nodeI) {
		ArrayList<NodeCacheEntry> connectedNodes = new ArrayList<NodeCacheEntry>();
		Collection<Node> neighbours = nodeI.node.getNeighbors();
		Iterator<Node> neighIter = neighbours.iterator();
		
		while (neighIter.hasNext()) {
			Node tempNode = (Node) neighIter.next();
			
			NodeCacheEntry n2 =
								getPatternNodeStructFromNode(options, tempNode);
			
			connectedNodes.add(n2);
		}
		
		return connectedNodes;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param options
	 *           DOCUMENT ME!
	 */
	public void readPatternConnections(ThreadSafeOptions options) {
		for (int i = 0; i < options.nodeArray.size(); i++) {
			NodeCacheEntry pi = (NodeCacheEntry) options.nodeArray.get(i);
			
			pi.patternNodes = getPatternNodes(options, pi);
			
			pi.connectedNodes = getConnectedNodes(options, pi);
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param params
	 *           DOCUMENT ME!
	 */
	public void setParameters(Parameter[] params) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#setControlInterface(javax.swing.JComponent)
	 */
	@Override
	public boolean setControlInterface(final ThreadSafeOptions options,
						JComponent jc) {
		double border = 5;
		double[][] size =
		{
							{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, border }
		}; // Rows
		
		jc.setLayout(new TableLayout(size));
		
		JSlider sliderLength;
		JSlider sliderEnergyHor;
		JSlider sliderEnergyVert;
		JSlider sliderStiffnes;
		
		JButton startStopButton = new JButton("Start Plugin");
		
		startStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (((JButton) e
									.getSource()).getText()
									.equalsIgnoreCase("Start Plugin")) {
					((JButton) e.getSource()).setText("Stop Plugin");
					Thread newBackgroundThread =
										new Thread(new Runnable() {
											public void run()
										{
											options.setGraphInstance(GravistoService.getInstance().getMainFrame().getActiveSession().getGraph());
											executeThreadSafe(options);
										}
										}) {
										};
					newBackgroundThread.setPriority(Thread.MIN_PRIORITY);
					newBackgroundThread.start();
				} else {
					((JButton) e.getSource()).setText("Start Plugin");
					options.setAbortWanted(true);
				}
			}
		});
		
		jc.add(startStopButton, "1,1");
		
		JLabel JLabelSlider1 = new JLabel("Zero Energy Length");
		
		jc.add(JLabelSlider1, "1,2");
		
		sliderLength = new JSlider();
		sliderLength.setMinimum(0);
		sliderLength.setMaximum(800);
		sliderLength.setMinorTickSpacing(50);
		sliderLength.setMajorTickSpacing(100);
		sliderLength.setPaintLabels(true);
		sliderLength.setPaintTicks(true);
		sliderLength.setLabelTable(sliderLength.createStandardLabels(100));
		
		jc.add(sliderLength, "1,3");
		
		sliderLength.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				options.setDval(3,
									((JSlider) e.getSource()).getValue());
			}
		});
		
		sliderLength.setAlignmentX(10);
		sliderLength.setAlignmentY(70);
		
		sliderEnergyHor = new JSlider();
		sliderEnergyHor.setMinimum(0);
		sliderEnergyHor.setMaximum(1000000);
		sliderEnergyHor.setMinorTickSpacing(5000);
		sliderEnergyHor.setMajorTickSpacing(100000);
		sliderEnergyHor.setPaintLabels(false);
		sliderEnergyHor.setPaintTicks(false);
		
		jc.add(new JLabel("Horizontal Repulsive Energy (low   <==>   high)"),
							"1,4");
		jc.add(sliderEnergyHor, "1,5");
		sliderEnergyHor.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				options.setDval(1,
									((JSlider) e.getSource()).getValue());
			}
		});
		
		sliderEnergyVert = new JSlider();
		sliderEnergyVert.setMinimum(0);
		sliderEnergyVert.setMaximum(1000000);
		sliderEnergyVert.setMinorTickSpacing(5000);
		sliderEnergyVert.setMajorTickSpacing(100000);
		sliderEnergyVert.setPaintLabels(false);
		sliderEnergyVert.setPaintTicks(false);
		jc.add(new JLabel("Vertical Repulsive Energy (low   <==>   high)"),
							"1,6");
		jc.add(sliderEnergyVert, "1,7");
		sliderEnergyVert.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				options.setDval(2,
									((JSlider) e.getSource()).getValue());
			}
		});
		
		JLabel JLabelSlider3 = new JLabel("Stiffness of Springs (*1/10)");
		
		jc.add(JLabelSlider3, "1,8");
		
		sliderStiffnes = new JSlider();
		sliderStiffnes.setMinimum(0);
		sliderStiffnes.setMaximum(75);
		sliderStiffnes.setMinorTickSpacing(10);
		sliderStiffnes.setMajorTickSpacing(25);
		sliderStiffnes.setPaintLabels(true);
		sliderStiffnes.setPaintTicks(true);
		sliderStiffnes.setLabelTable(sliderStiffnes.createStandardLabels(10));
		jc.add(sliderStiffnes, "1,9");
		sliderStiffnes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				options.setDval(0,
									((JSlider) e.getSource()).getValue());
			}
		});
		
		JCheckBox borderForce =
							new JCheckBox("Border Force", options.borderForce);
		
		borderForce.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				options.borderForce =
									((JCheckBox) e.getSource()).isSelected();
			}
		});
		jc.add(borderForce, "1,10");
		
		JCheckBox randomInit =
							new JCheckBox("Init: Random Node Positions", options.doRandomInit);
		
		randomInit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				options.doRandomInit =
									((JCheckBox) e.getSource()).isSelected();
			}
		});
		jc.add(randomInit, "1,11");
		
		JCheckBox copyLayout =
							new JCheckBox("Init: Copy Pattern Layout",
												options.doCopyPatternLayout);
		
		copyLayout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				options.doCopyPatternLayout =
									((JCheckBox) e.getSource()).isSelected();
			}
		});
		jc.add(copyLayout, "1,12");
		
		JCheckBox finishToTop =
							new JCheckBox("Finish: Move Graph to Top-Left",
												options.doFinishMoveToTop);
		
		finishToTop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				options.doFinishMoveToTop =
									((JCheckBox) e.getSource()).isSelected();
			}
		});
		jc.add(finishToTop, "1,13");
		
		sliderLength.setValue(100);
		sliderStiffnes.setValue(10);
		sliderEnergyHor.setValue(90000);
		sliderEnergyVert.setValue(90000);
		
		jc.revalidate();
		
		return true;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param options
	 *           DOCUMENT ME!
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void resetDataCache(ThreadSafeOptions options) {
		options.nodeArray = new ArrayList<NodeCacheEntry>();
		options.nodeSearch = new HashMap<Node, NodeCacheEntry>();
		MyTools.initNodeCache(options.nodeArray,
							options.nodeSearch,
							options.getGraphInstance());
		readPatternConnections(options);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#executeThreadSafe(org.graffiti.plugin.algorithm.ThreadSafeOptions)
	 */
	@Override
	public void executeThreadSafe(ThreadSafeOptions options) {
		if (options.doRandomInit) {
			GravistoService.getInstance().runPlugin(
								new RandomLayouterAlgorithm().getName(), options.getGraphInstance(), null);
		}
		
		if (options.doCopyPatternLayout) {
			GravistoService.getInstance()
								.runPlugin(new CopyPatternLayoutAlgorithm().getName(), options.getGraphInstance(), null);
		}
		
		// Remove bends
		GraphHelper.removeAllBends(options.getGraphInstance(), true);
		
		resetDataCache(options);
		
		int runValue = 0;
		double loopTime = 0;
		
		double moveRun;
		
		HashMap<CoordinateAttribute, Vector2d> oldPositions = new HashMap<CoordinateAttribute, Vector2d>();
		HashMap<CoordinateAttribute, Vector2d> newPositions = new HashMap<CoordinateAttribute, Vector2d>();
		
		GraphHelper.enumerateNodePositions(options.getGraphInstance(), oldPositions);
		
		do {
			runValue++;
			
			if (Thread.interrupted()) {
				return;
			}
			
			if (!options.redraw) {
				options.temperature_max_move *= options.temp_alpha;
			}
			
			long runTime = System.currentTimeMillis();
			// options.getGraphInstance().getListenerManager().transactionStarted(this);
			moveRun =
								doSpringEmbedder(options, options.temperature_max_move,
													runValue);
			// options.getGraphInstance().getListenerManager().transactionFinished(this);
			loopTime =
								loopTime * 0.8 + 0.2 * (System.currentTimeMillis() - runTime);
			
			// options.propagateNodePositionsFromCacheToGraph(options.getGraphInstance());
			if (moveRun <= 0.1) {
				try {
					MainFrame
										.showMessage("Spring Embedder - IDLE (loop time:"
															+ Math.round(loopTime) + " ms)",
															MessageType.INFO, 10000);
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// ignore (no problem)
				}
			} else {
				MainFrame
									.showMessage("Spring Embedder - RUNNING (max single node movement:"
														+ Math.round(options.temperature_max_move)
														+ ", loop time:"
														+ Math.round(loopTime) + " ms)",
														MessageType.PERMANENT_INFO);
			}
		} while (!options.isAbortWanted());
		
		options.setAbortWanted(false);
		
		GraphHelper.enumerateNodePositions(options.getGraphInstance(), newPositions);
		
		GraphHelper.postUndoableChanges(options.getGraphInstance(), oldPositions, newPositions, getName());
		
		MainFrame
							.showMessage("Spring Embedder - STOP", MessageType.INFO, 3000);
		if (options.doFinishMoveToTop) {
			GravistoService.getInstance()
								.runPlugin(new CenterLayouterAlgorithm().getName(),
													options.getGraphInstance(), null);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#attach(org.graffiti.graph.Graph)
	 */
	public void attach(Graph g) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	public void reset() {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#attach(org.graffiti.graph.Graph, org.graffiti.selection.Selection)
	 */
	public void attach(Graph g, Selection selection) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
	 */
	public String getCategory() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#isLayoutAlgorithm()
	 */
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	public String getDescription() {
		//
		return null;
	}
}
