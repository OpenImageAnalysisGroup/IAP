/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 25.11.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.rotate;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FolderPanel;
import org.JLabelJavaHelpLink;
import org.SystemInfo;
import org.Vector2d;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.ProvidesGeneralContextMenu;
import org.graffiti.plugin.algorithm.ThreadSafeAlgorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.AttributeConstants;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author Henning, Christian Klukas
 */
public class RotateAlgorithm extends ThreadSafeAlgorithm // AbstractAlgorithm
		implements ProvidesGeneralContextMenu, ActionListener {
	
	Vector2d centerOfMass;
	
	double centerX;
	
	double centerY;
	
	Collection<Node> nodeList = null;
	Collection<Edge> edgeList = null;
	
	Graph nonInteractiveGraph;
	
	Selection nonInteractiveSelection;
	
	private double degree = 0;
	
	private ThreadSafeOptions options;
	
	private boolean useUndoSupport = true;
	
	public String getName() {
		return "Rotate";
	}
	
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public Parameter[] getParameters() {
		DoubleParameter degreeParam = new DoubleParameter(degree, "Degree",
							"Degree to rotate graph clockwise");
		
		// BooleanParameter useSelectionParam = new BooleanParameter(useSelection,
		// "Work on Selection", "Rotate only selected Nodes");
		
		return new Parameter[] { degreeParam /* , useSelectionParam */};
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
	 * @param params
	 *           DOCUMENT ME!
	 */
	public void setParameters(Parameter[] params) {
		degree = ((DoubleParameter) params[0]).getDouble().doubleValue();
		
		// useSelection = ((BooleanParameter) params[1]).getBoolean().booleanValue();
	}
	
	public void check() {
	}
	
	void updateSelectionDependentVariables(Graph graph, Selection selection) {
		
		double x = 0.0;
		double y = 0.0;
		
		if (selection != null && selection.getNodes().size() > 0) {
			nodeList = selection.getNodes();
			edgeList = selection.getEdges();
		} else {
			nodeList = graph.getNodes();
			edgeList = graph.getEdges();
		}
		
		int numberOfSelectedNodes = 0;
		for (Node currentNode : nodeList) {
			x += AttributeHelper.getPositionX(currentNode);
			y += AttributeHelper.getPositionY(currentNode);
			numberOfSelectedNodes++;
		}
		
		x = x / numberOfSelectedNodes;
		y = y / numberOfSelectedNodes;
		
		centerOfMass = new Vector2d(x, y);
		centerX = centerOfMass.x;
		centerY = centerOfMass.y;
	}
	
	@SuppressWarnings("unchecked")
	public void execute() {
		double targetDegree = degree;
		
		// current angle
		final ThreadSafeOptions absoluteDegree = new ThreadSafeOptions(); // Drehwinkel, ausgehend vom Startzustand
		
		if (options == null || options.getGraphInstance() == null) {
			GravistoService.getInstance().algorithmAttachData(this);
		}
		if (options != null) {
			nonInteractiveGraph = options.getGraphInstance();
			nonInteractiveSelection = options.getSelection();
		}
		updateSelectionDependentVariables(nonInteractiveGraph, nonInteractiveSelection);
		
		final Graph graph = nonInteractiveGraph;
		
		if (nodeList.size() < 2)
			return;
		// options == null ==> Vom Menü, sonst Schleife für Background-Thread
		final HashMap<CoordinateAttribute, Vector2d> coordinates2newPositions = new HashMap<CoordinateAttribute, Vector2d>();
		final HashMap<CoordinateAttribute, Vector2d> coordinates2oldPositions = new HashMap<CoordinateAttribute, Vector2d>();
		// memorize old positions
		for (Node n : nodeList) {
			CoordinateAttribute coA = (CoordinateAttribute) n.getAttribute(GraphicAttributeConstants.COORD_PATH);
			coordinates2oldPositions.put(coA, new Vector2d(coA.getCoordinate()));
		}
		for (Edge e : edgeList) {
			LinkedHashMapAttribute ha =
								((LinkedHashMapAttribute)
								e.getAttribute(
													AttributeConstants.BENDS));
			if (ha == null)
				continue;
			Map m = ha.getCollection();
			if (m == null)
				continue;
			for (Iterator bi = m.entrySet().iterator(); bi.hasNext();) {
				// transform bends
				Map.Entry en = (java.util.Map.Entry) bi.next();
				CoordinateAttribute co = (CoordinateAttribute) en.getValue();
				coordinates2oldPositions.put(co, new Vector2d(co.getCoordinate()));
			}
		}
		
		do {
			if (!SwingUtilities.isEventDispatchThread()) { // im Thread
				targetDegree = ((Integer) options.getParam(1, new Integer(0)))
									.intValue();
				double localDegree = targetDegree - absoluteDegree.getDouble();
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// empty
				}
				
				if (Math.abs(localDegree) < 0.5) {
					continue;
				}
				
			}
			
			final double targetDegreeF = targetDegree;
			
			// graph.getListenerManager().transactionStarted(this);
			
			absoluteDegree.addInt(1);
			final int latestRunID = absoluteDegree.getInt();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (absoluteDegree.getInt() != latestRunID) {
						return;
					}
					if (Math.abs(absoluteDegree.getDouble() - targetDegreeF) < 0.001)
						return;
					ArrayList<CoordinateAttribute> transformThese = new ArrayList<CoordinateAttribute>();
					
					for (Node n : nodeList) {
						CoordinateAttribute coA = (CoordinateAttribute) n.getAttribute(GraphicAttributeConstants.COORD_PATH);
						transformThese.add(coA);
					}
					for (Edge e : edgeList) {
						LinkedHashMapAttribute ha =
											((LinkedHashMapAttribute)
											e.getAttribute(
																AttributeConstants.BENDS));
						if (ha == null)
							continue;
						Map m = ha.getCollection();
						if (m == null)
							continue;
						for (Iterator bi = m.entrySet().iterator(); bi.hasNext();) {
							// transform bends
							Map.Entry en = (java.util.Map.Entry) bi.next();
							CoordinateAttribute co = (CoordinateAttribute) en.getValue();
							transformThese.add(co);
						}
					}
					graph.getListenerManager().transactionStarted(this);
					for (CoordinateAttribute coA : transformThese) {
						Vector2d currentPosition = coordinates2oldPositions.get(coA);
						double currentDistance = currentPosition.distance(centerOfMass);
						if (Math.abs(currentDistance) > 0.00001) {
							double x = currentPosition.x;
							double y = currentPosition.y;
							
							double currentDegree = Math.asin((x - centerX) / currentDistance)
												/ (2.0 * Math.PI) * 360.0;
							
							// 0 <= currentDegree < = 90
							// nothing needs to be changed
							if (y > centerY) {
								// 90 < currentDegree < 270
								currentDegree = 90 + (90 - currentDegree);
							} else {
								if (x < centerX) {
									// 270 <= currentDegree < 360
									currentDegree = 360 + currentDegree;
								}
							}
							
							x = centerOfMass.x
												+ currentDistance
												* Math.sin((targetDegreeF + currentDegree) / 180.0 * Math.PI);
							y = centerOfMass.y
												- currentDistance
												* Math.cos((targetDegreeF + currentDegree) / 180.0 * Math.PI);
							coA.setCoordinate(x, y);
							coordinates2newPositions.put(coA, new Vector2d(x, y));
						}
					}
					graph.getListenerManager().transactionFinished(this);
					
					// graph.getListenerManager().transactionFinished(this);
					absoluteDegree.setDouble(targetDegreeF);
				}
			});
			
		} while (options != null && !options.isAbortWanted());
		if (options != null) {
			options.setAbortWanted(false);
		}
		final Graph graphF = graph;
		if (useUndoSupport) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					GraphHelper.postUndoableChanges(graphF, coordinates2oldPositions, coordinates2newPositions, "Rotation");
				}
			});
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public JMenuItem[] getCurrentContextMenuItem() {
		JMenuItem menuItem = new JMenuItem(getName());
		
		menuItem.addActionListener(this);
		return new JMenuItem[] { menuItem };
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		// executeThreadSafe(null);
		GravistoService.getInstance().algorithmAttachData(this);
		GravistoService.getInstance().runAlgorithm(this, nonInteractiveGraph,
							nonInteractiveSelection, e);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#setControlInterface(org.graffiti.plugin.algorithm.ThreadSafeOptions, javax.swing.JComponent)
	 */
	@Override
	public boolean setControlInterface(final ThreadSafeOptions options,
						JComponent jc) {
		this.options = options;
		
		double border = 5;
		double[][] size = {
							{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED,
												TableLayoutConstants.FILL, border } }; // Rows
		
		jc.setLayout(new TableLayout(size));
		
		final FolderPanel fp = new FolderPanel(
							"Rotate Nodes",
							false, false, false,
							JLabelJavaHelpLink.getHelpActionListener("layout_rotate"));
		
		JSlider angle = new JSlider();
		angle.setBackground(null);
		angle.setMinimum(0);
		angle.setMaximum(360);
		angle.setMinorTickSpacing(15);
		angle.setMajorTickSpacing(30);
		angle.setPaintLabels(true);
		angle.setPaintTicks(true);
		if (SystemInfo.isMac())
			angle.setPaintTrack(false);
		angle.setLabelTable(angle.createStandardLabels(60));
		angle.setValue(((Integer) options.getParam(1, new Integer(0))).intValue());
		
		angle.addFocusListener(new FocusListener() {
			
			public void focusGained(FocusEvent e) {
				options.setParam(2, new Integer(((JSlider) e.getSource()).getValue()));
				options.setAbortWanted(true);
				try {
					Graph graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
					Selection s = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
					options.setGraphInstance(graph);
					options.setSelection(s);
					updateSelectionDependentVariables(options.getGraphInstance(), options.getSelection());
					Thread newBackgroundThread = null;
					newBackgroundThread =
										new Thread(new Runnable() {
											public void run() {
												executeThreadSafe(options);
											}
										}) {
										};
					
					newBackgroundThread.setPriority(Thread.MIN_PRIORITY);
					options.setAbortWanted(false);
					newBackgroundThread.start();
					options.getParam(2, new Integer(0));
					options.setParam(1, new Integer(0));
					fp.setTitle("Rotate Nodes");
				} catch (NullPointerException err) {
					fp.setTitle("Rotate Nodes (no graph active!)");
				}
			}
			
			public void focusLost(FocusEvent e) {
				options.setAbortWanted(true);
			}
		});
		
		angle.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// Ziel-Grad-Wert
				int oldOffset = (Integer) options.getParam(2, new Integer(0));
				options.setParam(1, new Integer(((JSlider) e.getSource()).getValue() - oldOffset));
			}
		});
		
		fp.addComp(angle);
		fp.layoutRows();
		
		jc.add(fp, "1,1");
		jc.validate();
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#executeThreadSafe(org.graffiti.plugin.algorithm.ThreadSafeOptions)
	 */
	@Override
	public void executeThreadSafe(ThreadSafeOptions options) {
		degree = 0; // not yet rotated
		this.options = options;
		try {
			GravistoService.getInstance().algorithmAttachData(this);
			check();
			execute();
			reset();
		} catch (NullPointerException npe) {
			ErrorMsg.addErrorMessage(npe);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.ThreadSafeAlgorithm#resetDataCache(org.graffiti.plugin.algorithm.ThreadSafeOptions)
	 */
	@Override
	public void resetDataCache(ThreadSafeOptions options) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	public void reset() {
		nonInteractiveGraph = null;
		nonInteractiveSelection = null;
		options = null;
		degree = 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#attach(org.graffiti.graph.Graph, org.graffiti.selection.Selection)
	 */
	public void attach(Graph g, Selection selection) {
		if (options != null) {
			options.setGraphInstance(g);
			options.setSelection(selection);
		} else {
			nonInteractiveGraph = g;
			nonInteractiveSelection = selection;
		}
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
	
	public void disableUndo() {
		this.useUndoSupport = false;
	}
}
