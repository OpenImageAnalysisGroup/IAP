// ==============================================================================
//
// MegaCreateTool.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MegaCreateTool.java,v 1.3 2012-11-07 14:42:19 klukas Exp $

package org.graffiti.plugins.modes.defaults;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.AttributeHelper;
import org.ErrorMsg;
import org.ObjectRef;
import org.Release;
import org.ReleaseInfo;
import org.Vector2d;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.attributes.SortedCollectionAttribute;
import org.graffiti.editor.GraffitiInternalFrame;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.actions.PasteAction;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.GraphElementNotFoundException;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DockingAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.gui.ToolButton;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.GraphElementComponentInterface;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.EdgeComponent;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.plugins.views.defaults.NodeComponent;
import org.graffiti.undo.AddEdgeEdit;
import org.graffiti.undo.AddNodeEdit;
import org.graffiti.undo.GraphElementsDeletionEdit;

/**
 * A tool for creating and editing a graph.
 * 
 * @author Holleis
 */
public class MegaCreateTool
		extends MegaTools {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	protected CoordinateAttribute sourceCA = new CoordinateAttribute(GraphicAttributeConstants.COORDINATE);
	
	/** DOCUMENT ME! */
	protected CoordinateAttribute targetCA = new CoordinateAttribute(GraphicAttributeConstants.COORDINATE);
	
	/** DOCUMENT ME! */
	protected Edge dummyEdge = null;
	
	/** DOCUMENT ME! */
	protected EdgeGraphicAttribute dummyEdgeGraphAttr;
	
	/**
	 * Component used to associate the key binding for deleting graph elements
	 * with.
	 */
	protected JComponent keyComponent;
	
	/** DOCUMENT ME! */
	protected Node dummyNode = null;
	
	/** Contains the first selected node when adding an edge. */
	protected Node first;
	
	/** DOCUMENT ME! */
	protected SortedCollectionAttribute bends;
	
	/** DOCUMENT ME! */
	protected SortedCollectionAttribute dummyBends;
	
	/** DOCUMENT ME! */
	protected boolean creatingEdge = false;
	
	/** DOCUMENT ME! */
	protected boolean dragged = false;
	
	/** DOCUMENT ME! */
	protected int numOfBends = 0;
	
	/** Removes last bend while creating edges. */
	private final Action backAction;
	
	/** Deletes all selected items (incl. undo support). */
	private final Action deleteAction;
	
	/** Aborts creation of edges. */
	private final Action escapeAction;
	
	// private boolean ctrlDown = false;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for this tool. Registers a key used to delete graph
	 * elements.
	 */
	public MegaCreateTool() {
		super();
		
		if (!getKnownTools().contains(this))
			getKnownTools().add(this);
		
		normCursor = new Cursor(Cursor.HAND_CURSOR);
		// protected Cursor edgeCursor = new Cursor(Cursor.HAND_CURSOR);
		nodeCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
		
		escapeAction = new AbstractAction()
		{
			private static final long serialVersionUID = 3905528206759900217L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reset();
			}
		};
		
		backAction = new AbstractAction()
		{
			private static final long serialVersionUID = 3257567308653081396L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (numOfBends == 0)
				{
					// no bends, just do the same as does escapeAction
					reset();
				}
				else
				{
					dummyEdgeGraphAttr.getBends().remove("bend" + numOfBends);
					bends.remove("bend" + numOfBends);
					numOfBends--;
					
					for (View view : session.getViews())
					{
						GraphElementComponent gec = view.getComponentForElement(dummyEdge);
						try
						{
							gec.graphicAttributeChanged(dummyEdgeGraphAttr.getBends());
						}
						catch (ShapeNotFoundException snfe)
						{
						}
					}
				}
			}
		};
		
		bends = new LinkedHashMapAttribute(GraphicAttributeConstants.BENDS);
		
		deleteAction = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// useful to check if the selection isn't empty
				// before an edit is build.
				if (!selection.isEmpty())
				{
					if (AdvancedLabelTool.getEditDeleteAction() != null)
						AdvancedLabelTool.getEditDeleteAction().delete(selection.getElements());
					else {
						Graph graph = MainFrame.getInstance().getActiveSession().getGraph();
						GraphElementsDeletionEdit edit = new GraphElementsDeletionEdit(selection.getElements(),
								graph, geMap);
						unmarkAll();
						fireSelectionChanged();
						edit.execute();
						undoSupport.postEdit(edit);
					}
				}
			}
		};
	}
	
	// ~ Methods ================================================================
	
	@Override
	public Cursor getNormCursor() {
		if (creatingEdge)
			return nodeCursor;
		else
			return normCursor;
	}
	
	@Override
	public Cursor getEdgeCursor() {
		if (creatingEdge)
			return nodeCursor;
		else
			return edgeCursor;
	}
	
	@Override
	public Cursor getNodeCursor() {
		return nodeCursor;
	}
	
	/**
	 * The method additionally registers a key used to delete graph elements.
	 * 
	 * @see org.graffiti.plugin.tool.AbstractTool#activate()
	 */
	@Override
	public void activate() {
		if (session == null || session.getActiveView() == null || session.getActiveView().getViewComponent() == null) {
			return;
		}
		super.activate();
		
		try {
			JComponent view = session.getActiveView().getViewComponent();
			
			while (!((view instanceof GraffitiInternalFrame))) {
				if (view.getParent() == null || !(view.getParent() instanceof JComponent)) {
					break;
				} else {
					view = (JComponent) view.getParent();
				}
			}
			
			keyComponent = view;
			
			String deleteName = "delete";
			view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
					java.awt.event.KeyEvent.VK_DELETE, 0), deleteName);
			view.getActionMap().put(deleteName, deleteAction);
			
			String escName = "escape";
			view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
					java.awt.event.KeyEvent.VK_ESCAPE, 0), escName);
			view.getActionMap().put(escName, escapeAction);
			
			String backName = "back";
			view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
					java.awt.event.KeyEvent.VK_BACK_SPACE, 0), backName);
			view.getActionMap().put(backName, backAction);
			MainFrame
					.showMessage(
							"<html>"
									+
									"Use the mouse to create nodes and/or edges and "
									+
									"use the DEL key to delete the last created element, "
									+
									"the BACK key to remove edge bends during edge creation or cancel active edge creation. "
									+
									"Press Ctrl while clicking on a larger node to create a new node instead of starting edge creation. The style (size, color, label, ...) is taken from the current node "
									+
									"selection.", // +
							// "Double-click a node to edit the node-label.",
							MessageType.INFO);
		} catch (ClassCastException cce) {
			ErrorMsg.addErrorMessage(cce);
			System.err.println("Failed to register a key for some action in " +
					getClass().getName() + ", activate()");
		}
	}
	
	/**
	 * This method additionaly unregisters the key used for deleting graph
	 * elements.
	 * 
	 * @see org.graffiti.plugin.tool.AbstractTool#deactivate()
	 */
	@Override
	public void deactivate() {
		super.deactivate();
		
		// System.out.println("Deactivating MegaCreateTool");
		
		reset();
		
		if (keyComponent != null) {
			keyComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(
					java.awt.event.KeyEvent.VK_DELETE, 0));
			keyComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(
					java.awt.event.KeyEvent.VK_ESCAPE, 0));
			keyComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(
					java.awt.event.KeyEvent.VK_BACK_SPACE, 0));
		}
	}
	
	/**
	 * Invoked when the mouse button has been pressed inside the editor panel
	 * and handles what has to happen. Is actually empty since all
	 * functionality is put into mousePressed etc.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		if (lastMouseEventTime >= e.getWhen())
			return;
		lastMouseEventTime = e.getWhen();
		
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}
		
		Component src = findComponentAt(e, e.getX(), e.getY());
		
		boolean doubleClickOK = false; // disable double click editing
		
		if (e.getClickCount() > 1 && (MegaTools.getLastMouseSrc() instanceof GraphElementComponent)
				&& doubleClickOK) {
			AdvancedLabelTool.processLabelEdit((GraphElementComponentInterface) MegaTools.getLastMouseSrc());
			reset();
			return;
		}
		mouseMoved(e);
		
		dragged = false;
		
		if (creatingEdge) {
			// assure that no dummyXXX is hit
			Component tempEdgeComp = null;
			
			JComponent compSrc = (JComponent) e.getSource();
			
			if ((src instanceof EdgeComponent && ((EdgeComponent) src).getGraphElement().equals(dummyEdge))) {
				tempEdgeComp = src;
				tempEdgeComp.setVisible(false);
				src = findComponentAt(e, e.getX(), e.getY());
				tempEdgeComp.setVisible(true);
			}
			
			if (src instanceof NodeComponent &&
					((NodeComponent) src).getGraphElement().equals(dummyNode)) {
				src.setVisible(false);
				src = findComponentAt(e, e.getX(), e.getY());
				// src = getCorrectComp(src, (View) e.getComponent(), e);
			}
			
			if (src instanceof EdgeComponent &&
					((EdgeComponent) src).getGraphElement().equals(dummyEdge)) {
				tempEdgeComp = src;
				tempEdgeComp.setVisible(false);
				
				src = compSrc.findComponentAt((e.getPoint()));
				src = getCorrectComp(src, (View) e.getComponent(), e);
				tempEdgeComp.setVisible(true);
			}
		}
		
		if (src instanceof NodeComponent && !getIsCtrlDown(e) && !getIsShiftDown(e)) {
			Node clickedNode = (Node) ((NodeComponent) src).getGraphElement();
			
			unmarkAll();
			
			if (!creatingEdge) {
				startAddEdge(e, clickedNode);
			} else
				if ((this.first != clickedNode) || ((bends != null) && !bends.isEmpty()))
					processEdgeCreation(e, clickedNode);
		} else {
			// clicked on background (or edge) => bend or new node
			if (creatingEdge) {
				if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR &&
						src instanceof EdgeComponent) {
					Edge edge = (Edge) ((EdgeComponent) src).getGraphElement();
					Node newNode = breakEdge(e, edge);
					processEdgeCreation(e, newNode);
				} else
					addBend(e);
			} else {
				if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR &&
						src instanceof EdgeComponent) {
					Edge edge = (Edge) ((EdgeComponent) src).getGraphElement();
					
					Node newNode = breakEdge(e, edge);
					if (newNode != null)
						startAddEdge(e, newNode);
				} else
					addNode(e, true);
			}
			for (View view : session.getViews()) {
				view.autoscroll(e.getPoint());
			}
		}
	}
	
	private boolean getIsCtrlDown(MouseEvent e) {
		return (e.getModifiers() & InputEvent.CTRL_MASK) != 0;
	}
	
	private boolean getIsShiftDown(MouseEvent e) {
		return (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;
	}
	
	/**
	 * Invoked when the mouse button has been pressed and dragged inside the
	 * editor panel and handles what has to happen.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}
		
		dragged = true;
		mouseMoved(e);
	}
	
	/**
	 * Temporarily marks the component under cursor.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// MainFrame.showMesssage("Create Edge? "+creatingEdge, MessageType.PERMANENT_INFO);
		super.mouseMoved(e);
		Component src = findComponentAt(e, e.getX(), e.getY());
		
		// ctrlDown = getIsCtrlDown(e);
		
		if (creatingEdge) {
			// assure that no dummyXXX is hit
			Component tempEdgeComp = null;
			
			JComponent compSrc = (JComponent) e.getSource();
			
			if (src instanceof EdgeComponent &&
					((EdgeComponent) src).getGraphElement().equals(dummyEdge)) {
				tempEdgeComp = src;
				tempEdgeComp.setVisible(false);
				
				src = compSrc.findComponentAt((e.getPoint()));
				
				src = getCorrectComp(src, (View) e.getComponent(), e);
				tempEdgeComp.setVisible(true);
			}
			
			if (src instanceof NodeComponent &&
					((NodeComponent) src).getGraphElement().equals(dummyNode)) {
				src.setVisible(false);
				
				src = compSrc.findComponentAt((e.getPoint()));
				src = getCorrectComp(src, (View) e.getComponent(), e);
			}
			
			if (src instanceof EdgeComponent &&
					((EdgeComponent) src).getGraphElement().equals(dummyEdge)) {
				tempEdgeComp = src;
				tempEdgeComp.setVisible(false);
				
				src = compSrc.findComponentAt((e.getPoint()));
				
				src = getCorrectComp(src, (View) e.getComponent(), e);
				tempEdgeComp.setVisible(true);
			}
		}
		
		if (lastSelectedComp != src) {
			if (lastSelectedComp != null && !selectedContain(lastSelectedComp)) {
				unDisplayAsMarked((GraphElementComponent) lastSelectedComp);
				src.getParent().repaint();
			}
			
			if (src instanceof NodeComponent &&
					(((NodeComponent) src).getGraphElement() != dummyNode)) {
				if (!selectedContain(src)) {
					highlight(src, e);
					src.getParent().repaint();
				}
				
				lastSelectedComp = src;
			}
		}
		
		if (src instanceof View) {
			lastSelectedComp = null;
		}
		
		if (dummyNode != null) {
			((NodeGraphicAttribute) dummyNode.getAttribute(GraphicAttributeConstants.GRAPHICS)).getCoordinate()
					.setCoordinate(e.getPoint());
		}
		
		if (creatingEdge) {
			// update view scrolling
			for (View view : session.getViews()) {
				view.getViewComponent().repaint();
				view.autoscroll(e.getPoint());
			}
		}
	}
	
	private long lastMouseEventTime = Long.MIN_VALUE;
	
	/**
	 * Invoked when the mouse button has been clicked (pressed and released)
	 * inside the editor panel and handles what has to happen.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// ctrlDown = getIsCtrlDown(e);
		super.mousePressed(e);
	}
	
	private static String dummyNodeID = "temporary";
	
	public static boolean isTemporaryNode(Node n) {
		try {
			if (n.getBoolean(dummyNodeID)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception err) {
			return false;
		}
	}
	
	public static boolean isPastedNode(Node node) {
		try {
			return node.getBoolean(PasteAction.pastedNodeID);
		} catch (Exception err) {
			return false;
		}
	}
	
	public boolean isCreatingEdge() {
		return creatingEdge;
	}
	
	private void startAddEdge(MouseEvent e, Node clickedNode) {
		NodeGraphicAttribute dummyGraphics;
		// start adding edge
		creatingEdge = true;
		CollectionAttribute dummyCol = new HashMapAttribute("");
		dummyGraphics = new NodeGraphicAttribute();
		dummyGraphics.getDimension().setHeight(5d);
		dummyGraphics.getDimension().setWidth(5d);
		dummyGraphics.setFrameThickness(1d);
		// ZOOMED
		dummyGraphics.getCoordinate().setCoordinate(e.getPoint());
		dummyCol.add(dummyGraphics, false);
		dummyCol.add(new BooleanAttribute(dummyNodeID, true), false);
		
		Graph graph = clickedNode.getGraph();
		dummyNode = graph.addNode(dummyCol);
		
		CollectionAttribute dummyEdgeCol = new HashMapAttribute("");
		dummyEdgeGraphAttr = new EdgeGraphicAttribute();
		dummyEdgeGraphAttr.setShape(
				"org.graffiti.plugins.views.defaults.SmoothLineEdgeShape");
		dummyEdgeCol.add(dummyEdgeGraphAttr, true);
		dummyBends = null;
		if (dummyEdge != null)
			graph.deleteEdge(dummyEdge);
		dummyEdge = graph.addEdge(clickedNode, dummyNode, true,
				dummyEdgeCol);
		this.first = clickedNode;
		sourceCA.setCoordinate(e.getPoint());
	}
	
	private Node addNode(MouseEvent e, boolean copyStyle) {
		return addNode(e, copyStyle, true);
	}
	
	private Node addNode(MouseEvent e, boolean copyStyle, boolean processGrid) {
		return addNode(e, copyStyle, processGrid, true);
	}
	
	private Node addNode(MouseEvent e, boolean copyStyle, boolean processGrid, boolean makeundo) {
		// add new node
		Object o = e.getSource();
		while (o != null && !(o instanceof GraffitiView)) {
			if (o instanceof JComponent)
				o = ((JComponent) o).getParent();
			else
				o = null;
		}
		if (o == null) {
			MainFrame.showMessage("Operation not supported for this view type", MessageType.INFO);
			return null;
		}
		
		Node node;
		
		int x = e.getX();
		int y = e.getY();
		if (!e.isShiftDown() && processGrid) {
			if (x % 10 >= 5)
				x += 10;
			if (y % 10 >= 5)
				y += 10;
			x = x - (x % 10);
			y = y - (y % 10);
		}
		
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			node = getGraph().addNode(AttributeHelper.getDefaultGraphicsAttributeForKeggNode(x, y));
			AttributeHelper.setLabel(node, "?");
			AttributeHelper.getLabel(-1, node).setFontSize(20);
		} else {
			try {
				Node nn = selection.getNodes().iterator().next();
				
				boolean visible = false;
				if (session.getActiveView() instanceof GraffitiView) {
					GraffitiView view = (GraffitiView) session.getActiveView();
					visible = view.isHidden(nn);
				}
				if (copyStyle && !visible)
					node = getGraph().addNodeCopy(nn);
				else
					node = getGraph().addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(x, y));
				AttributeHelper.setPosition(node, x, y);
			} catch (Exception err) {
				node = getGraph().addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(x, y));
			}
		}
		GraphElementComponent tempGeComp = null;
		if (node != null)
			tempGeComp = ((View) e.getComponent()).getComponentForElement(node);
		if (tempGeComp == null) {
			ErrorMsg.addErrorMessage("Internal Error: No graph element was created or found for a new node! " +
					"Eventually an error occured before, leaving a open graph-transaction. Use" +
					" Window/ReCreate-View to close this transaction.");
		}
		// tempGeComp.paintComponent(tempGeComp.getGraphics());
		// getSelectedComps().add(tempGeComp);
		mark(tempGeComp, false, false, this, false);
		fireSelectionChanged();
		
		if (tempGeComp != null && tempGeComp.getParent() != null) {
			tempGeComp.getParent().repaint();
		}
		// selectionModel.add(selection);
		// selectionModel.setActiveSelection(ACTIVE);
		if (makeundo) {
			AddNodeEdit edit = new AddNodeEdit(node, getGraph(), geMap);
			undoSupport.postEdit(edit);
		}
		return node;
	}
	
	private void addBend(MouseEvent e) {
		// add a bend ... to edge that is being created
		if (bends == null) {
			bends = new LinkedHashMapAttribute(GraphicAttributeConstants.BENDS);
		}
		
		int x = e.getX();
		int y = e.getY();
		if (!e.isShiftDown()) {
			if (x % 10 >= 5)
				x += 10;
			if (y % 10 >= 5)
				y += 10;
			x = x - (x % 10);
			y = y - (y % 10);
		}
		
		numOfBends++;
		bends.add(new CoordinateAttribute("bend" + numOfBends, x, y));
		// add a bend ... to dummy edge
		if (dummyBends == null) {
			dummyBends = new LinkedHashMapAttribute(GraphicAttributeConstants.BENDS);
			dummyEdgeGraphAttr.setBends(dummyBends);
			
			// dummyBends = dummyEdgeGraphAttr.getBends();
		}
		dummyEdgeGraphAttr.getBends().add(new CoordinateAttribute("bend" + numOfBends, e.getPoint()), true);
		// dummyEdgeGraphAttr.getBends().setValue(dummyBends.getCollection());
		// HACK to make the view update the edge ...
		Point pt = e.getPoint();
		pt.setLocation((int) pt.getX() + 1, (int) pt.getY() + 1);
		((NodeGraphicAttribute) dummyNode.getAttribute(GraphicAttributeConstants.GRAPHICS)).getCoordinate()
				.setCoordinate(pt);
	}
	
	private void processEdgeCreation(MouseEvent e, Node clickedNode) {
		// end edge here and create the edge
		// remove all temporary things
		if (dummyEdge != null)
			getGraph().deleteEdge(dummyEdge);
		if (dummyNode != null)
			getGraph().deleteNode(dummyNode);
		dummyEdge = null;
		dummyNode = null;
		targetCA.setCoordinate(e.getPoint());
		CollectionAttribute col = new HashMapAttribute("");
		EdgeGraphicAttribute graphics = new EdgeGraphicAttribute();
		col.add(graphics, false);
		DockingAttribute dock = new DockingAttribute(GraphicAttributeConstants.DOCKING, "", "");
		graphics.setDocking(dock);
		// setting the graphic attributes to the default values stored
		// in the preferences
		graphics.setThickness(prefs.getDouble("thickness", 1));
		graphics.setFrameThickness(prefs.getDouble("frameThickness", 1));
		// setting the framecolor
		GravistoPreferences fc = prefs.node("framecolor");
		int red = fc.getInt("red", 0);
		int green = fc.getInt("green", 0);
		int blue = fc.getInt("blue", 0);
		int alpha = fc.getInt("alpha", 255);
		graphics.getFramecolor().setColor(new Color(red, green, blue,
				alpha));
		// setting the fillcolor
		fc = prefs.node("fillcolor");
		red = fc.getInt("red", 0);
		green = fc.getInt("green", 0);
		blue = fc.getInt("blue", 0);
		alpha = fc.getInt("alpha", 255);
		graphics.getFillcolor().setColor(new Color(red, green, blue, alpha));
		if (numOfBends > 0) {
			graphics.setShape(prefs.get("shape", "org.graffiti.plugins.views.defaults.SmoothLineEdgeShape"));
		} else {
			graphics.setShape(prefs.get("shape", "org.graffiti.plugins.views.defaults.StraightLineEdgeShape"));
		}
		if (clickedNode.getGraph().isDirected())
			graphics.setArrowhead(prefs.get("arrowhead", "org.graffiti.plugins.views.defaults.StandardArrowShape"));
		// setting the lineMode
		GravistoPreferences da = prefs.node("dashArray");
		String[] daEntries;
		daEntries = da.keys();
		// no dashArray exists
		if (daEntries.length == 0) {
			graphics.getLineMode().setDashArray(null);
		} else {
			float[] newDA = new float[daEntries.length];
			
			for (int i = daEntries.length - 1; i >= 0; i--) {
				newDA[i] = da.getFloat(daEntries[i], 10);
			}
			
			graphics.getLineMode().setDashArray(newDA);
		}
		graphics.getLineMode().setDashPhase(prefs.getFloat(
				"dashPhase", 0.0f));
		if (numOfBends > 0) {
			graphics.setBends(bends);
			numOfBends = 0;
			bends = null;
		}
		final Edge edge = clickedNode.getGraph().addEdge(
				this.first, clickedNode,
				clickedNode.getGraph().isDirected(),
				col);
		
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			AttributeHelper.setLabel(edge, "?");
		}
		
		this.first = null;
		View v = ((View) e.getComponent());
		GraphElementComponent tempGeComp = v.getComponentForElement(edge);
		// if edge visualisation is suppressed by a global view setting or isHidden of the view, suppress the error,
		// because the edge will be created but not shown
		boolean invisibleEdge = tempGeComp == null && v instanceof GraffitiView && (((GraffitiView) v).getBlockEdges() || ((GraffitiView) v).isHidden(edge));
		if (tempGeComp == null && !invisibleEdge)
			ErrorMsg.addErrorMessage("No GraphElement-Component is created. Internal Error!");
		if (!invisibleEdge)
			mark(tempGeComp, false, false, this, false);
		fireSelectionChanged();
		if (!invisibleEdge)
			tempGeComp.getParent().repaint();
		
		AddEdgeEdit edit = new AddEdgeEdit(edge, clickedNode.getGraph(), geMap);
		undoSupport.postEdit(edit);
		creatingEdge = false;
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					AdvancedLabelTool.processLabelEdit(edge);
				}
			});
		}
	}
	
	/**
	 * Invoked when the mouse button has been released inside the editor panel
	 * and handles what has to happen.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}
		
		// ctrlDown = getIsCtrlDown(e);
		
		super.mouseReleased(e);
		
		if (creatingEdge && dragged && ((bends == null) || bends.isEmpty())) {
			mousePressed(e);
		}
		
		ToolButton.requestToolButtonFocus();
		
	}
	
	/**
	 * Resets the tool to initial values.
	 */
	
	public void reset() {
		if (dummyNode != null) {
			try {
				dummyNode.getGraph().deleteNode(dummyNode);
			} catch (GraphElementNotFoundException genfe) {
			}
		}
		
		// dummyEdge should have been removed automatically after
		// removal of dummyNode
		dummyNode = null;
		dummyEdge = null;
		
		// lastSelectedComp = null;
		creatingEdge = false;
		numOfBends = 0;
		bends = new LinkedHashMapAttribute(GraphicAttributeConstants.BENDS);
	}
	
	/**
	 * Returns the component on which the user clicked (using the information
	 * contained in the <code>MouseEvent</code>. This is used to get the <code>GraphElementComponent ge</code> even if clicked on the <code>LabelComponent</code>
	 * (associated with <code>ge</code>), for
	 * instance.
	 * 
	 * @param src
	 *           DOCUMENT ME!
	 * @param view
	 *           DOCUMENT ME!
	 * @param me
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected Component getCorrectComp(Component src, View view, MouseEvent me) {
		if (src instanceof View || src instanceof GraphElementComponent) {
			return src;
		} else
			if (src instanceof AttributeComponent) {
				Component comp = view.getComponentForElement((GraphElement) ((AttributeComponent) src).getAttribute()
						.getAttributable());
				
				if (comp == null) {
					return (Component) me.getSource();
				} else {
					return comp;
				}
			} else {
				if (src.equals(me.getComponent())) {
					return src;
				} else {
					return getCorrectComp(src.getParent(), view, me);
				}
			}
	}
	
	@Override
	public String getToolName() {
		return "MegaCreateTool";
	}
	
	private Node breakEdge(final MouseEvent e, final Edge edgeToBeBroken) {
		
		final Graph graph = edgeToBeBroken.getGraph();
		
		final ObjectRef result = new ObjectRef();
		
		AbstractUndoableEdit updateCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			Node newNode = null;
			private Edge newEdge1 = null, newEdge2 = null;
			private Node oldSource = null, oldTarget = null;
			
			private Edge oldEdge;
			
			@Override
			public String getPresentationName() {
				return "Edge Breaking";
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo Edge Breaking";
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo Edge Breaking";
			}
			
			@Override
			public void die() {
				super.die();
				
			}
			
			@Override
			public void redo() throws CannotRedoException {
				if (!graph.containsEdge(edgeToBeBroken)) {
					Object src = findComponentAt(e, e.getX(), e.getY());
					Edge edge = (Edge) ((EdgeComponent) src).getGraphElement();
					oldEdge = edge;
				}
				newNode = addNode(e, false, false, false);
				result.setObject(newNode);
				graph.getListenerManager().transactionStarted(this);
				try {
					if (oldEdge == null)
						oldEdge = edgeToBeBroken;
					newEdge1 = graph.addEdgeCopy(oldEdge, oldEdge.getSource(), oldEdge.getTarget());
					graph.deleteEdge(oldEdge);
					String docking;
					
					if (creatingEdge) {
						// change new node attributes
						AttributeHelper.setSize(newNode, 1, 1);
						AttributeHelper.setOutlineColor(newNode, AttributeHelper.getOutlineColor(newEdge1));
						AttributeHelper.setFillColor(newNode, AttributeHelper.getOutlineColor(newEdge1));
						AttributeHelper.setShapeRectangle(newNode);
						AttributeHelper.setRoundedEdges(newNode, 0);
						AttributeHelper.setBorderWidth(newNode, 1);
						// remove labels
						if (AttributeHelper.hasAttribute(newNode, GraphicAttributeConstants.LABELGRAPHICS))
							newNode.removeAttribute(GraphicAttributeConstants.LABELGRAPHICS);
						for (int k = 0; k < 100; k++)
							if (AttributeHelper.hasAttribute(newNode, GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k)))
								newNode.removeAttribute(GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k));
					}
					
					// set new node position to a position on the old not broken edge
					Vector2d pos1 = AttributeHelper.getPositionVec2d(newEdge1.getSource());
					// has old edge a docking at source node?
					// if yes, pos1 has to be changed to docking position
					docking = (String) AttributeHelper.getAttributeValue(newEdge1, GraphicAttributeConstants.DOCKING_PATH,
							GraphicAttributeConstants.SOURCE, "", new String(""), true);
					if (docking.length() > 0) {
						Vector2d size = AttributeHelper.getSize(newEdge1.getSource());
						Vector2d dockingpos = new Vector2d(Double.parseDouble(docking.substring(0, docking.indexOf(";"))),
								Double.parseDouble(docking.substring(docking.indexOf(";") + 1)));
						if (Math.abs(dockingpos.x) < 1.001)
							pos1.x = pos1.x + size.x * 0.5 * dockingpos.x;
						else
							pos1.x = pos1.x + Math.signum(dockingpos.x) * size.x * 0.5 + dockingpos.x;
						if (Math.abs(dockingpos.y) < 1.001)
							pos1.y = pos1.y + size.y * 0.5 * dockingpos.y;
						else
							pos1.y = pos1.y + Math.signum(dockingpos.y) * size.y * 0.5 + dockingpos.y;
					}
					Vector2d pos2 = AttributeHelper.getPositionVec2d(newEdge1.getTarget());
					// has old edge a docking at target node?
					// if yes, pos2 has to be changed to docking position
					docking = (String) AttributeHelper.getAttributeValue(newEdge1, GraphicAttributeConstants.DOCKING_PATH,
							GraphicAttributeConstants.TARGET, "", new String(""), true);
					if (docking.length() > 0) {
						Vector2d size = AttributeHelper.getSize(newEdge1.getTarget());
						Vector2d dockingpos = new Vector2d(Double.parseDouble(docking.substring(0, docking.indexOf(";"))),
								Double.parseDouble(docking.substring(docking.indexOf(";") + 1)));
						if (Math.abs(dockingpos.x) < 1.001)
							pos2.x = pos2.x + size.x * 0.5 * dockingpos.x;
						else
							pos2.x = pos2.x + Math.signum(dockingpos.x) * size.x * 0.5 + dockingpos.x;
						if (Math.abs(dockingpos.y) < 1.001)
							pos2.y = pos2.y + size.y * 0.5 * dockingpos.y;
						else
							pos2.y = pos2.y + Math.signum(dockingpos.y) * size.y * 0.5 + dockingpos.y;
					}
					Vector2d pos3 = AttributeHelper.getPositionVec2d(newNode);
					// has old edge bends
					// if yes, find section of the old edge which new node is next to
					// split bends between old and new edge
					ArrayList<Vector2d> edgebends = AttributeHelper.getEdgeBends(newEdge1);
					double len = 0, lensrc = 0, lentgt = 0;
					int bendsidx1 = -1, bendsidx2 = -1;
					if (!edgebends.isEmpty()) {
						double m, d, lensection, tmp_d, tmp;
						Vector2d newPos1, newPos2;
						tmp = Math.sqrt(Math.pow(edgebends.get(0).x - pos1.x, 2) + Math.pow(edgebends.get(0).y - pos1.y, 2));
						lensrc = len;
						lensection = tmp;
						if (edgebends.get(0).x == pos1.x)
							tmp_d = Math.abs(pos3.x - pos1.x);
						else {
							m = (edgebends.get(0).y - pos1.y) / (edgebends.get(0).x - pos1.x);
							tmp_d = Math.abs((m * (pos3.x - pos1.x) - pos3.y + pos1.y) / Math.sqrt(m * m + 1));
						}
						d = tmp_d;
						newPos1 = pos1;
						newPos2 = edgebends.get(0);
						bendsidx1 = -1;
						bendsidx2 = 0;
						len = len + tmp;
						for (int k = 0; k < edgebends.size() - 1; k++) {
							tmp = Math.sqrt(Math.pow(edgebends.get(k + 1).x - edgebends.get(k).x, 2) + Math.pow(edgebends.get(k + 1).y - edgebends.get(k + 1).y, 2));
							if (edgebends.get(k + 1).x == edgebends.get(k).x)
								tmp_d = Math.abs(pos3.x - edgebends.get(k).x);
							else {
								m = (edgebends.get(k + 1).y - edgebends.get(k).y) / (edgebends.get(k + 1).x - edgebends.get(k).x);
								tmp_d = Math.abs((m * (pos3.x - edgebends.get(k).x) - pos3.y + edgebends.get(k).y) / Math.sqrt(m * m + 1));
							}
							if (tmp_d < d) {
								lensrc = len;
								lensection = tmp;
								d = tmp_d;
								newPos1 = edgebends.get(k);
								newPos2 = edgebends.get(k + 1);
								bendsidx1 = k;
								bendsidx2 = k + 1;
							}
							len = len + tmp;
						}
						tmp = Math.sqrt(Math.pow(pos2.x - edgebends.get(edgebends.size() - 1).x, 2) + Math.pow(pos2.y - edgebends.get(edgebends.size() - 1).y, 2));
						if (pos2.x == edgebends.get(edgebends.size() - 1).x)
							tmp_d = Math.abs(pos3.x - edgebends.get(edgebends.size() - 1).x);
						else {
							m = (pos2.y - edgebends.get(edgebends.size() - 1).y) / (pos2.x - edgebends.get(edgebends.size() - 1).x);
							tmp_d = Math.abs((m * (pos3.x - edgebends.get(edgebends.size() - 1).x) - pos3.y + edgebends.get(edgebends.size() - 1).y)
									/ Math.sqrt(m * m + 1));
						}
						if (tmp_d < d) {
							lensrc = len;
							lensection = tmp;
							d = tmp_d;
							newPos1 = edgebends.get(edgebends.size() - 1);
							newPos2 = pos2;
							bendsidx1 = edgebends.size() - 1;
							bendsidx2 = -1;
						}
						len = len + tmp;
						// special case straight line
						if (AttributeHelper.getEdgeBendStyle(newEdge1).indexOf("Straight") == -1) {
							pos1 = newPos1;
							pos2 = newPos2;
							len = len - lensection;
							lentgt = len - lensrc;
						}
					}
					// calculate new position for new node (position on the old edge)
					double xs, ys;
					// special case vertical edge
					if ((int) pos1.x == (int) pos2.x) {
						xs = pos1.x;
						ys = pos3.y;
					}
					else {
						double m = (pos2.y - pos1.y) / (pos2.x - pos1.x);
						ys = (m * m * pos3.y + m * pos3.x - m * pos1.x + pos1.y) / (1 + m * m);
						xs = m * (pos3.y - ys) + pos3.x;
					}
					AttributeHelper.setPosition(newNode, xs, ys);
					
					// add edge
					oldTarget = newEdge1.getTarget();
					oldSource = newEdge1.getSource();
					newEdge1.setTarget(newNode);
					newEdge2 = getGraph().addEdgeCopy(newEdge1, newNode, oldTarget);
					
					// change edge attributes
					AttributeHelper.setArrowhead(newEdge1, false);
					AttributeHelper.setArrowtail(newEdge2, false);
					// labels
					len = Math.sqrt(Math.pow(pos2.x - pos1.x, 2) + Math.pow(pos2.y - pos1.y, 2)) + len;
					lensrc = Math.sqrt(Math.pow(pos3.x - pos1.x, 2) + Math.pow(pos3.y - pos1.y, 2)) + lensrc;
					lentgt = Math.sqrt(Math.pow(pos2.x - pos3.x, 2) + Math.pow(pos2.y - pos3.y, 2)) + lentgt;
					if (lensrc < len * 0.5 && AttributeHelper.hasAttribute(newEdge1, GraphicAttributeConstants.LABELGRAPHICS))
						newEdge1.removeAttribute(GraphicAttributeConstants.LABELGRAPHICS);
					if (lensrc < len * 0.333 && AttributeHelper.hasAttribute(newEdge1, "srcLabel"))
						newEdge1.removeAttribute("srcLabel");
					if (lensrc < len * 0.666 && AttributeHelper.hasAttribute(newEdge1, "tgtLabel"))
						newEdge1.removeAttribute("tgtLabel");
					if (lentgt < len * 0.5 && AttributeHelper.hasAttribute(newEdge2, GraphicAttributeConstants.LABELGRAPHICS))
						newEdge2.removeAttribute(GraphicAttributeConstants.LABELGRAPHICS);
					if (lentgt < len * 0.666 && AttributeHelper.hasAttribute(newEdge2, "srcLabel"))
						newEdge2.removeAttribute("srcLabel");
					if (lentgt < len * 0.333 && AttributeHelper.hasAttribute(newEdge2, "tgtLabel"))
						newEdge2.removeAttribute("tgtLabel");
					// docking
					docking = (String) AttributeHelper.getAttributeValue(newEdge1, GraphicAttributeConstants.DOCKING_PATH,
							GraphicAttributeConstants.TARGET, "", new String(""), true);
					if (docking.length() > 0)
						AttributeHelper.setAttribute(newEdge1, GraphicAttributeConstants.DOCKING_PATH,
								GraphicAttributeConstants.TARGET, "");
					docking = (String) AttributeHelper.getAttributeValue(newEdge2, GraphicAttributeConstants.DOCKING_PATH,
							GraphicAttributeConstants.SOURCE, "", new String(""), true);
					if (docking.length() > 0)
						AttributeHelper.setAttribute(newEdge2, GraphicAttributeConstants.DOCKING_PATH,
								GraphicAttributeConstants.SOURCE, "");
					// bends
					if (!edgebends.isEmpty()) {
						AttributeHelper.removeEdgeBends(newEdge1);
						if (bendsidx1 == -1)
							AttributeHelper.setEdgeBendStyle(newEdge1, "Straight");
						else
							AttributeHelper.addEdgeBends(newEdge1, edgebends.subList(0, bendsidx1 + 1));
						AttributeHelper.removeEdgeBends(newEdge2);
						if (bendsidx2 == -1)
							AttributeHelper.setEdgeBendStyle(newEdge2, "Straight");
						else
							AttributeHelper.addEdgeBends(newEdge2, edgebends.subList(bendsidx2, edgebends.size()));
					}
				} finally {
					graph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				graph.getListenerManager().transactionStarted(this);
				try {
					graph.deleteEdge(newEdge1);
					graph.deleteEdge(newEdge2);
					oldEdge = graph.addEdgeCopy(oldEdge, oldSource, oldTarget);
					graph.deleteNode(newNode);
					
					// newNode = null;
					// newEdge1 = null;
					// newEdge2 = null;
					// oldEdge = null;
					// oldSource = null;
					// oldTarget = null;
					
				} finally {
					graph.getListenerManager().transactionFinished(this);
				}
			}
			
		};
		
		updateCmd.redo();
		
		if (graph == MainFrame.getInstance().getActiveSession().getGraph()) {
			undoSupport.beginUpdate();
			undoSupport.postEdit(updateCmd);
			undoSupport.endUpdate();
		}
		if (this.creatingEdge)
			return (Node) result.getObject();
		else
			return null;
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
