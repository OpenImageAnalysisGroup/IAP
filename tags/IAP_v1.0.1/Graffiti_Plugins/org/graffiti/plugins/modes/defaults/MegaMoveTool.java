// ==============================================================================
//
// MegaMoveTool.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MegaMoveTool.java,v 1.3 2012-11-07 14:42:19 klukas Exp $

package org.graffiti.plugins.modes.defaults;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.SortedCollectionAttribute;
import org.graffiti.editor.GraffitiFrame;
import org.graffiti.editor.GraffitiInternalFrame;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.gui.ToolButton;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.EdgeComponentInterface;
import org.graffiti.plugin.view.EdgeShape;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.EdgeComponent;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.plugins.views.defaults.NodeComponent;
import org.graffiti.plugins.views.defaults.PolyLineEdgeShape;
import org.graffiti.session.Session;
import org.graffiti.undo.ChangeAttributesEdit;
import org.graffiti.undo.GraphElementsDeletionEdit;

/**
 * A tool for creating and editing a graph.
 * 
 * @author Holleis, Klukas
 * @version $Revision: 1.3 $
 */
public class MegaMoveTool extends MegaTools {
	// ~ Static fields/initializers =============================================
	private static String rkey = "selrect";
	
	// ~ Instance fields ========================================================
	
	/**
	 * Specifies if - when there is a selection - graphelements are highlighted
	 * only if the CTRL key is pressed.
	 */
	protected boolean onlyIfCtrl = false;
	
	private final Action deleteAction, leftAction, upAction, rightAction, downAction;
	
	private final Action leftActionFine, upActionFine, rightActionFine,
						downActionFine;
	
	/** Component that was last marked. */
	private Component lastPressed;
	
	/** Coordinates of the bend on which the user most recently pressed. */
	private CoordinateAttribute lastBendHit;
	
	/**
	 * Component used to associate the key binding for deleting graph elements
	 * with.
	 */
	protected JComponent keyComponent;
	
	protected Cursor myNormCursor = new Cursor(Cursor.HAND_CURSOR);
	
	protected Cursor myResize_TL_Cursor = new Cursor(Cursor.NW_RESIZE_CURSOR);
	protected Cursor myResize_TR_Cursor = new Cursor(Cursor.NE_RESIZE_CURSOR);
	protected Cursor myResize_BR_Cursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
	protected Cursor myResize_BL_Cursor = new Cursor(Cursor.SW_RESIZE_CURSOR);
	
	/**
	 * Saves original attributes with their values in this map before they are
	 * changed; used for undo
	 */
	private Map<Attribute, Object> originalCoordinates;
	
	/** First corner of selection rectangle. */
	private Point2D lastPressedPoint;
	
	/** Second corner of selection rectangle. */
	private Point2D lastPressedPoint2;
	
	/** Point used for moving. */
	private Point2D lastPressedMousePointRel;
	
	/** The selection rectangle. */
	private final Rectangle selRect = new Rectangle();
	
	/** Used to distinguish between simple move and drag. */
	private boolean dragged = false;
	
	private static double lastDragX = 100;
	private static double lastDragY = 100;
	
	private int prevPosX;
	private int prevPosY;
	
	private boolean resizeHit = false;
	private boolean resizeHitTl = false;
	private boolean resizeHitTr = false;
	private boolean resizeHitBl = false;
	private boolean resizeHitBr = false;
	
	private int resizeStartDimW = 0;
	private int resizeStartDimH = 0;
	
	private int moveStartX = 0;
	private int moveStartY = 0;
	
	private JComponent selRectComp;
	
	private CoordinateAttribute lastBendAdded;
	private double lastBendAddedInitX, lastBendAddedInitY;
	private long lastBendAddedTime = 0;
	
	private static MegaMoveTool thisInstance;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for this tool. Registers a key used to delete graph elements.
	 */
	public MegaMoveTool() {
		super();
		
		if (!getKnownTools().contains(this))
			getKnownTools().add(this);
		
		thisInstance = this;
		
		edgeCursor = new Cursor(Cursor.HAND_CURSOR);
		nodeCursor = new Cursor(Cursor.MOVE_CURSOR);
		
		deleteAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent e) {
				if (AdvancedLabelTool.getEditDeleteAction() != null)
					AdvancedLabelTool.getEditDeleteAction().delete(selection.getElements());
				else {
					// useful to check if the selection isn't empty
					// before an edit is build.
					if (!selection.isEmpty()) {
						GraphElementsDeletionEdit edit = new GraphElementsDeletionEdit(
											selection.getElements(), getGraph(), geMap);
						unmarkAll();
						edit.execute();
						undoSupport.postEdit(edit);
						fireSelectionChanged();
					}
				}
			}
		};
		
		leftAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println("LEFT");
				processMovement(-10, 0, arg0);
			}
		};
		
		upAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println("UP");
				processMovement(0, -10, arg0);
			}
		};
		
		rightAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println("RIGHT");
				processMovement(10, 0, arg0);
			}
			
		};
		
		downAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println("DOWN");
				processMovement(0, 10, arg0);
			}
		};
		
		leftActionFine = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println("LEFT FINE");
				processMovement(-1, 0, arg0);
			}
		};
		
		upActionFine = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println("UP FINE");
				processMovement(0, -1, arg0);
			}
		};
		
		rightActionFine = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println("RIGHT FINE");
				processMovement(1, 0, arg0);
			}
			
		};
		
		downActionFine = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println("DOWN FINE");
				processMovement(0, 1, arg0);
			}
		};
	}
	
	private void processMovement(int diffX, int diffY, ActionEvent actionEvent) {
		System.out.print(".");
		int xn = diffX;
		int yn = diffY;
		
		lastPressed = null;
		
		MouseEvent me = new MouseEvent(
							(Component) actionEvent.getSource(), -1, 0,
							InputEvent.BUTTON1_MASK + InputEvent.SHIFT_MASK, xn, yn, 1, false, MouseEvent.BUTTON1);
		
		lastBendHit = null;
		lastPressed = null;
		lastPressedPoint = null;
		lastPressedPoint2 = null;
		lastPressedMousePointRel = null;
		
		mouseDragged(me);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * States whether this class wants to be registered as a <code>SessionListener</code>. This tool returns true.
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isSessionListener() {
		return true;
	}
	
	@Override
	public void sessionChanged(Session s) {
		super.sessionChanged(s);
		lastPressed = null;
	}
	
	/**
	 * The method additionally registers a key used to delete graph elements.
	 * 
	 * @see org.graffiti.plugin.tool.AbstractTool#activate()
	 */
	@Override
	public void activate() {
		if (session == null || session.getActiveView() == null
							|| session.getActiveView().getViewComponent() == null)
			return;
		
		super.activate();
		
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
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
							KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteName);
		view.getActionMap().put(deleteName, deleteAction);
		
		// / move nodes CTRL
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
							KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK),
							leftAction.toString());
		view.getActionMap().put(leftAction.toString(), leftAction);
		
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
							.put(
												KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
																	InputEvent.CTRL_DOWN_MASK), rightAction.toString());
		view.getActionMap().put(rightAction.toString(), rightAction);
		
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
							KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK),
							upAction.toString());
		view.getActionMap().put(upAction.toString(), upAction);
		
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
							KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK),
							downAction.toString());
		view.getActionMap().put(downAction.toString(), downAction);
		
		// / move nodes SHIFT
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
							KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK),
							leftActionFine.toString());
		view.getActionMap().put(leftActionFine.toString(), leftActionFine);
		
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
							KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK),
							rightActionFine.toString());
		view.getActionMap().put(rightActionFine.toString(), rightActionFine);
		
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
							KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK),
							upActionFine.toString());
		view.getActionMap().put(upActionFine.toString(), upActionFine);
		
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
							KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK),
							downActionFine.toString());
		view.getActionMap().put(downActionFine.toString(), downActionFine);
		
		MainFrame
							.showMessage(
												"<html>Use the mouse or keyboard (Shift/Ctrl + Cursor Keys) to move "
																	+
																	// "and Shift/Ctrl + Mouse Wheel to rotate" +
																	" the selected or all items. "
																	+
																	"Double-click (or start typing*) to edit a node- or edge-label (*select elements before and press the move-tool button again). Shift+Double-Click hides/makes visible child elements of a node. "
																	+
																	"Use middle mouse click to select a node and all nodes, placed inside the node.",
												MessageType.INFO);
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
		
		if (keyComponent != null)
			keyComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
								KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0),
								null);
	}
	
	/**
	 * Empty method. Invoked at mouse clicks. Does not do anything. All is done
	 * via mousePressed.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		mouseMoved(e);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		Component src;
		if (SwingUtilities.isMiddleMouseButton(e))
			src = null;
		else
			src = findComponentAt(e, e.getX(), e.getY());
		
		edgeCursor = myNormCursor;
		
		if (src != null) {
			if (src instanceof EdgeComponentInterface) { // instanceof View
				EdgeComponentInterface eci = (EdgeComponentInterface) src;
				boolean bendHit = false;
				Point2D coord;
				CoordinateAttribute coordAttr;
				if (eci.getGraphElement() == null || eci.getGraphElement().getGraph() == null)
					return;
				ArrayList<Edge> edges = new ArrayList<Edge>(eci.getGraphElement().getGraph().getEdges());
				for (Edge edge : edges) {
					if (edge != ((EdgeComponentInterface) src).getGraphElement())
						continue;
					SortedCollectionAttribute bendsColl = (SortedCollectionAttribute) edge
										.getAttribute(GraphicAttributeConstants.BENDS_PATH);
					Collection<?> bends = bendsColl.getCollection().values();
					for (Iterator<?> it = bends.iterator(); it.hasNext();) {
						coordAttr = (CoordinateAttribute) it.next();
						coord = coordAttr.getCoordinate();
						
						if (hit(e.getPoint(), coord)) {
							bendHit = true;
							break;
						}
					}
					if (bendHit)
						edgeCursor = myMoveCursor;
				}
			}
		}
		super.mouseMoved(e);
	}
	protected void processScrolling(MouseEvent e) {

//		System.out.println("-scrolling");
		Object o = e.getSource();
		if (o != null && o instanceof JComponent) {
			JComponent jc = (JComponent) o;
			JScrollPane jsp = (JScrollPane) ErrorMsg.findParentComponent(jc, JScrollPane.class);
			JScrollBar jsb = null;
			float dx = prevPosX - e.getX();
			float dy = prevPosY - e.getY();
			
			AffineTransform zoom = null;
			if(o instanceof GraffitiView)
				zoom = ((GraffitiView)o).getZoom();

//			System.out.println("zoom: "+ zoom.toString()+" dx:"+dx+" dy:"+dy);
//			System.out.println("prevDragX:"+prevPosX+" prevDragY:"+prevPosY);
//			System.out.println(" mouseX:" + e.getX()+" mouseY:" + e.getY());
//			System.out.println(" horSBpos:" + jsp.getHorizontalScrollBar().getValue());
//			System.out.println(" verSBpos:" + jsp.getVerticalScrollBar().getValue());
//			if(dx < 0 || dy < 0)
//				System.out.println();
//			if(Math.abs(dx) > 100 || Math.abs(dy) > 100)
//			System.out.println();
			if(dx != 0){
				jsb = jsp.getHorizontalScrollBar();
				
				
				int v = jsb.getValue();

				v+=dx*zoom.getScaleX();
				if (v < jsb.getMinimum())
					v = jsb.getMinimum();
				if (v > jsb.getMaximum())
					v = jsb.getMaximum();
				jsb.setValue(v);
			}			
			if(dy != 0){
				jsb = jsp.getVerticalScrollBar();
				
				int v = jsb.getValue();

				v+=dy*zoom.getScaleY();
				if (v < jsb.getMinimum())
					v = jsb.getMinimum();
				if (v > jsb.getMaximum())
					v = jsb.getMaximum();
				jsb.setValue(v);
			}

			/*
			 * adjust prev position to the new scroll position of the view
			 * with respect to the changed dx values
			 * Just setting the previous x/y coordinates results in flicker, due 
			 * to the changed scroll position
			 */
			prevPosX = e.getX()+ (int)dx;
			prevPosY = e.getY() +(int)dy;
			
		}
//		System.out.println("--- finished scrolling");
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
		// logger.info("MOUSE DRAGGED");
		
		lastDragX = e.getPoint().getX();
		lastDragY = e.getPoint().getY();
		
		View view;
		if (e.getComponent() instanceof GraffitiInternalFrame) {
			view = ((GraffitiInternalFrame) e.getComponent()).getView();
		} else {
			if (e.getComponent() instanceof View) {
				view = (View) e.getComponent();
			} else
				view = findView(e.getComponent());
		}
		if(SwingUtilities.isRightMouseButton(e)){
			if(selectedView == null)
				if(view != null)
					selectedView = view.getViewComponent();
			if(selectedView != null){
				processScrolling(e);
				scrollpanemovement = true;
				return;
			}
		}
		super.mouseMoved(e);

		if (!SwingUtilities.isLeftMouseButton(e) && !SwingUtilities.isMiddleMouseButton(e)) {
			dragged = true;
			return;
		}
		
		if (lastBendHit == null && !dragged) {
			int x = e.getX();
			int y = e.getY();
			if (e.getWhen() - lastClick < 1000) {
				x = lastClickPoint.x;
				y = lastClickPoint.y;
			}
			Component c = findComponentAt(e, x, y);
			if (c instanceof EdgeComponent) {
				// break!
				processBendCreation(e, c);
				return;
			}
		}
		
		if (lastBendHit != null) {
			// calculated moved distance
			double newX = e.getPoint().getX() + lastPressedMousePointRel.getX();
			double newY = e.getPoint().getY() + lastPressedMousePointRel.getY();
			
			if (!dragged) {
				ChangeAttributesEdit edit = new ChangeAttributesEdit(
									getGraph(), lastBendHit, geMap);
				undoSupport.postEdit(edit);
			}
			
			if (!e.isShiftDown()) {
				int grid = getGrid(-1);
				if (grid > 0) {
					newX = newX - (newX % grid);
					newY = newY - (newY % grid);
				}
			}
			
			Component c = findComponentAt(e, e.getX(), e.getY());
			if (c != null && c instanceof EdgeComponent) {
				if (e.isControlDown() && e.isShiftDown()) {
					EdgeComponent ec = (EdgeComponent) c;
					Edge edge = (Edge) ec.getGraphElement();
					AttributeHelper.setEdgeBendStyle(edge, "org.graffiti.plugins.views.defaults.PolyLineEdgeShape");
				} else
					if (e.isControlDown() && !e.isShiftDown()) {
						EdgeComponent ec = (EdgeComponent) c;
						Edge edge = (Edge) ec.getGraphElement();
						AttributeHelper.setEdgeBendStyle(edge, "org.graffiti.plugins.views.defaults.SmoothLineEdgeShape");
					}
			}
			
			lastBendHit.setCoordinate(new Point2D.Double(newX, newY));
			e.getComponent().repaint();
			for (View v : MainFrame.getInstance().getActiveEditorSession().getViews())
				v.repaint(null);
			dragged = true;
			
			view.getViewComponent().setCursor(myMoveCursor);
			
			MainFrame.showMessage("<html>Hint: While moving edge bend press Ctrl key to activate smooth-line, press Ctrl and Shift keys " +
								"to activate poly-line edge bend style. After activation of desired style stop mouse movement then release keyboard keys. " +
								"Press Shift key while moving edge bend to disable position grid.", MessageType.INFO);
			
			return;
		}
		
		if (lastPressedPoint != null) { // equiv. to "pressed" on view before
			lastPressedPoint2 = e.getPoint();
			paintSelectionRectangle((JComponent) e.getSource(), lastPressedPoint, lastPressedPoint2);
			
			// update view scrolling
			// View view = (View) e.getComponent();
			view.autoscroll(new Point((int) lastPressedPoint2.getX(),
								(int) lastPressedPoint2.getY()));
			view.getViewComponent().repaint();
		} else
			if (lastPressed instanceof NodeComponent || lastPressed == null) {
				if (selection.isEmpty())
					return;
				if (isControlDown(e)) {
					dragged = true;
					
					return;
				}
				if (!dragged)
					mouseDraggedFirstCallStoreCoordinatesForUndoSupport();
				
				Vector2d delta = mouseDraggedCalculatedXYmovementValues(e);
				
				if ((int) delta.x == 0 && (int) delta.y == 0)
					return;
				
				mouseDraggedToMoveNodesAndEdgeBends(e, view, delta.x, delta.y);
			}
		
		dragged = true;
		
		// else do nothing with labels and other attributecomponents
	}
	
	public CoordinateAttribute processBendCreation(final MouseEvent e, final Component c) {
		
		AbstractUndoableEdit createNewBend = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			private ArrayList<CoordinateAttribute> bendsBeforeBending = new ArrayList<CoordinateAttribute>();
			private boolean newStyleSet = false;
			private Edge bendedEdge;
			
			@Override
			public String getPresentationName() {
				return "Create Edge Bend";
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo Create Edge Bend";
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo Create Edge Bend";
			}
			
			@Override
			public void redo() throws CannotRedoException {
				newStyleSet = false;
				
				EdgeComponent ec = (EdgeComponent) c;
				bendedEdge = (Edge) ec.getGraphElement();
				boolean added = false;
				lastBendAdded = null;
				if (ec.getShape() != null && ec.getShape() instanceof PolyLineEdgeShape) {
					PolyLineEdgeShape ls = (PolyLineEdgeShape) ec.getShape();
					int idx = ls.getIndexOfPathWhichContains(e.getX(), e.getY());
					bendsBeforeBending = AttributeHelper.getEdgeBendCoordinateAttributes(bendedEdge);
					if (bendsBeforeBending.size() >= idx && idx >= 0) {
						AttributeHelper.removeEdgeBends(bendedEdge);
						for (int i = 0; i < bendsBeforeBending.size(); i++) {
							if (i == idx) {
								// introduce a bend between bends i and i+1 (and not at the end)
								lastBendAdded = AttributeHelper.addEdgeBend(bendedEdge, e.getX(), e.getY(), true);
								added = true;
							}
							AttributeHelper.addEdgeBend(bendedEdge, bendsBeforeBending.get(i).getX(), bendsBeforeBending.get(i).getY(), true);
						}
					}
				}
				if (!added) // new bend created and appended at the end
					lastBendAdded = AttributeHelper.addEdgeBend(bendedEdge, e.getX(), e.getY(), true);
				
				if (lastBendAdded != null) {
					lastBendAddedInitX = lastBendAdded.getX();
					lastBendAddedInitY = lastBendAdded.getY();
					lastBendAddedTime = e.getWhen();
					// System.out.println("Bend added at "+lastBendAddedInitX+" / "+lastBendAddedInitY);
					String style = AttributeHelper.getEdgeBendStyle(bendedEdge);
					if (style.equals("org.graffiti.plugins.views.defaults.StraightLineEdgeShape")) {
						AttributeHelper.setEdgeBendStyle(bendedEdge, "org.graffiti.plugins.views.defaults.PolyLineEdgeShape");
						newStyleSet = true;
					}
				}
				
				dragged = false;
				mousePressed(e);
			}
			
			@Override
			public void undo() throws CannotUndoException {
				AttributeHelper.removeEdgeBends(bendedEdge);
				for (int i = 0; i < bendsBeforeBending.size(); i++)
					AttributeHelper.addEdgeBend(bendedEdge, bendsBeforeBending.get(i).getX(), bendsBeforeBending.get(i).getY(), true);
				if (newStyleSet)
					AttributeHelper.setEdgeBendStyle(bendedEdge, "org.graffiti.plugins.views.defaults.StraightLineEdgeShape");
			}
			
		};
		
		createNewBend.redo();
		
		undoSupport.beginUpdate();
		undoSupport.postEdit(createNewBend);
		undoSupport.endUpdate();
		
		return lastBendAdded;
	}
	
	private double distance(double x0, double y0, double x, double y) {
		return new Vector2d(x0, y0).distance(new Vector2d(x, y));
	}
	
	private View findView(Component component) {
		for (GraffitiFrame f : MainFrame.getInstance().getDetachedFrames()) {
			while (component != null) {
				if (component == f)
					return f.getView();
				component = component.getParent();
			}
		}
		return null;
	}
	
	private Vector2d mouseDraggedCalculatedXYmovementValues(MouseEvent mouse) {
		Vector2d delta = new Vector2d(0, 0);
		
		if (lastPressed != null) {
			NodeComponent nodecomp = (NodeComponent) lastPressed;
			
			// calculated moved distance
			if (resizeHit) {
				delta.x = mouse.getPoint().getX() - lastPressedMousePointRel.getX();
				delta.y = mouse.getPoint().getY() - lastPressedMousePointRel.getY();
			} else {
				double curPosX = getTransformersX(nodecomp.getGraphElement());
				double curPosY = getTransformersY(nodecomp.getGraphElement());
				if (!mouse.isShiftDown() && selection.getNumberOfNodes() <= 1) {
					int grid = getGrid(-1);
					if (grid > 0) {
						curPosX = curPosX - (curPosX % grid);
						curPosY = curPosY - (curPosY % grid);
					}
					setTransformersXY(nodecomp.getGraphElement(), curPosX, curPosY);
				}
				delta.x = mouse.getPoint().getX();
				delta.y = mouse.getPoint().getY();
				delta.x = mouse.getPoint().getX() - curPosX
									+ lastPressedMousePointRel.getX();
				delta.y = mouse.getPoint().getY() - curPosY
									+ lastPressedMousePointRel.getY();
			}
		} else {
			delta.x = mouse.getPoint().getX();
			delta.y = mouse.getPoint().getY();
		}
		
		if (!mouse.isShiftDown()) {
			int grid = getGrid(-1);
			if (grid > 0) {
				delta.x = delta.x - (delta.x % grid);
				delta.y = delta.y - (delta.y % grid);
			}
		}
		return delta;
	}
	
	private double getTransformersX(GraphElement graphElement) {
		CoordinateAttribute ca = (CoordinateAttribute) graphElement.getAttribute(
							GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR + GraphicAttributeConstants.COORDINATE);
		
		switch (getCoordinateSystem()) {
			case XY:
				return ca.getX();
			case XZ:
				return ca.getX();
			case ZY:
				return AttributeHelper.getPositionZ((Node) graphElement, true);
			default:
				return Double.NaN;
		}
	}
	
	private double getTransformersY(GraphElement graphElement) {
		CoordinateAttribute ca = (CoordinateAttribute) graphElement.getAttribute(
							GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR + GraphicAttributeConstants.COORDINATE);
		
		switch (getCoordinateSystem()) {
			case XY:
				return ca.getY();
			case XZ:
				return AttributeHelper.getPositionZ((Node) graphElement, true);
			case ZY:
				return ca.getY();
			default:
				return Double.NaN;
		}
	}
	
	private void setTransformersXY(GraphElement graphElement, double curPosX, double curPosY) {
		
		CoordinateAttribute coord = (CoordinateAttribute) graphElement.getAttribute(
							GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR + GraphicAttributeConstants.COORDINATE);
		
		switch (getCoordinateSystem()) {
			case XY:
				coord.setCoordinate(curPosX, curPosY);
				break;
			case XZ:
				coord.setX(curPosX);
				AttributeHelper.setPositionZ((Node) graphElement, curPosY);
				break;
			case ZY:
				AttributeHelper.setPositionZ((Node) graphElement, curPosX);
				coord.setY(curPosY);
				break;
		}
	}
	
	private CoordinateSystem getCoordinateSystem() {
		View v = MainFrame.getInstance().getActiveEditorSession().getActiveView();
		if (v instanceof GraffitiView)
			return ((GraffitiView) v).getCoordinateSystem();
		else
			return CoordinateSystem.XY;
	}
	
	private void mouseDraggedFirstCallStoreCoordinatesForUndoSupport() {
		// first call to mouseDragged
		// save changed (Coordinate/Bends Attributes) for undo edit
		List<GraphElement> selElements = selection.getElements();
		
		originalCoordinates = new HashMap<Attribute, Object>();
		
		ChangeAttributesEdit edit;
		
		for (GraphElement ge : selElements) {
			if (ge instanceof Node) {
				// for nodes, add their coordinates
				
				if (resizeHit) {
					Attribute dimAttr = ge.getAttribute(GraphicAttributeConstants.DIM_PATH);
					originalCoordinates.put(dimAttr, ((Attribute) dimAttr.copy()).getValue());
				} else {
					Attribute coAttr = ge.getAttribute(GraphicAttributeConstants.COORD_PATH);
					originalCoordinates.put(coAttr, ((Attribute) coAttr.copy()).getValue());
				}
			} else {
				// for edges, add the coordinates of their bends
				Collection<Node> selNodes = selection.getNodes();
				
				for (Node node : selNodes) {
					for (Edge edge : node.getEdges()) {
						if (selNodes.contains(edge.getSource())
											&& selNodes.contains(edge.getTarget())) {
							SortedCollectionAttribute bends = (SortedCollectionAttribute) edge
												.getAttribute(GraphicAttributeConstants.GRAPHICS
																	+ Attribute.SEPARATOR
																	+ GraphicAttributeConstants.BENDS);
							
							originalCoordinates.put(bends, ((Attribute) bends.copy()).getValue());
						}
					}
				}
			}
		}
		
		edit = new ChangeAttributesEdit(session.getGraph(), originalCoordinates, geMap);
		undoSupport.postEdit(edit);
	}
	
	private void mouseDraggedToMoveNodesAndEdgeBends(MouseEvent e, View view,
						double deltaX, double deltaY) {
		try {
			getGraph().getListenerManager().transactionStarted(this);
			
			// save max values (for autoscroll)
			double maxX = 0;
			double maxY = 0;
			
			if (resizeHit) {
				// update node sizes
				for (Node node : selection.getNodes()) {
					DimensionAttribute coord = (DimensionAttribute) node.getAttribute(GraphicAttributeConstants.DIM_PATH);
					
					double fx = 1;
					double fy = 1;
					if (resizeHitTl || resizeHitBl)
						fx = -2;
					if (resizeHitTr || resizeHitBr)
						fx = 2;
					if (resizeHitTl || resizeHitTr)
						fy = -2;
					if (resizeHitBl || resizeHitBr)
						fy = 2;
					double newW = resizeStartDimW + deltaX * fx;
					double newH = resizeStartDimH + deltaY * fy;
					double ratio = (double) resizeStartDimW / (double) resizeStartDimH;
					
					newW = Math.abs(newW);
					newH = Math.abs(newH);
					
					if (!e.isShiftDown()) {
						int grid = getGrid(newW);
						if (grid > 0) {
							newW = newW - (newW % grid);
							grid = getGrid(newH);
							if (grid > 0) {
								newH = newH - (newH % grid);
							}
						}
					}
					coord.setDimension(newW, newH);
					String status = "Resize: w/h: " + resizeStartDimW + " / " + resizeStartDimH + " --> " +
										AttributeHelper.formatNumber(newW, "#") + " / " +
										AttributeHelper.formatNumber(newH, "#") + " ratio: " + AttributeHelper.formatNumber(ratio, "#.###") + " --> "
										+ AttributeHelper.formatNumber(newW / newH, "#.###") +
										(e.isShiftDown() ? "" : " - press Shift to disable grid point resize");
					MainFrame.showMessage(status, MessageType.INFO);
				}
			} else {
				// update coordinates
				for (Node node : selection.getNodes()) {
					CoordinateAttribute coord = (CoordinateAttribute) node.getAttribute(
										GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR + GraphicAttributeConstants.COORDINATE);
					
					double newX = coord.getX();
					double newY = coord.getY();
					double newZ = Double.NaN;
					if (!(getCoordinateSystem() == CoordinateSystem.XY))
						newZ = AttributeHelper.getPositionZ(node, true);
					double dX = 0;
					double dY = 0;
					double dZ = 0;
					String ppp = "[internal error]";
					
					switch (getCoordinateSystem()) {
						case XY:
							dX = deltaX;
							dY = deltaY;
							dZ = 0;
							ppp = "x/y";
							break;
						case XZ:
							dX = deltaX;
							dY = 0;
							dZ = deltaY;
							ppp = "x/z";
							break;
						case ZY:
							dX = 0;
							dY = deltaY;
							dZ = deltaX;
							ppp = "z/y";
							break;
					}
					newX += dX;
					newY += dY;
					newZ += dZ;
					
					if (!e.isShiftDown() && e.isAltDown()) {
						int grid = getGrid(-1);
						if (grid > 0) {
							newX = newX - (newX % grid);
							newY = newY - (newY % grid);
							newZ = newZ - (newZ % grid);
						}
					}
					
					coord.setCoordinate(new Point2D.Double(newX, newY));
					if (!(getCoordinateSystem() == CoordinateSystem.XY))
						AttributeHelper.setPositionZ(node, newZ);
					
					if (maxX < newX)
						maxX = newX;
					
					if (maxY < newY)
						maxY = newY;
					
					String status = "Move: " + ppp + ": " + moveStartX + " / " + moveStartY + " --> " + (int) newX + " / " + (int) newY +
										(e.isShiftDown() ? "" : " - press Shift to disable grid point movement, press Alt to move selection to grid points");
					MainFrame.showMessage(status, MessageType.INFO);
				}
			}
			
			if (!resizeHit) {
				Set<CoordinateAttribute> bendsCoordsSet = new HashSet<CoordinateAttribute>();
				Collection<Node> selNodes = selection.getNodes();
				
				// update all bends of all edges where source AND target node
				// is in the selection
				for (Node node : selNodes) {
					for (Edge edge : node.getEdges()) {
						if (selNodes.contains(edge.getSource()) && selNodes.contains(edge.getTarget())) {
							addBends(bendsCoordsSet, edge);
						}
					}
				}
				
				for (CoordinateAttribute coord : bendsCoordsSet) {
					Point2D coordPt = coord.getCoordinate();
					double newX = coordPt.getX() + deltaX;
					double newY = coordPt.getY() + deltaY;
					
					if (!e.isShiftDown() && e.isAltDown()) {
						int grid = getGrid(-1);
						if (grid > 0) {
							newX = newX - (newX % grid);
							newY = newY - (newY % grid);
						}
					}
					
					coord.setCoordinate(new Point2D.Double(newX, newY));
					
					if (maxX < newX)
						maxX = newX;
					
					if (maxY < newY)
						maxY = newY;
				}
			}
			// update view scrolling
			if (e.getID() != -1) {
				view.autoscroll(e.getPoint());
				// view.autoscroll(new Point((int) maxX, (int) maxY));
				e.getComponent().repaint();
			}
		} finally {
			getGraph().getListenerManager().transactionFinished(this);
		}
	}
	
	public static int gridMovement = 10;
	public static int gridResizeSmallNodes = 2;
	public static int gridResizeNormalNodes = 5;
	public static int gridResizeLargeNodes = 10;
	public static boolean gridEnabled = true;
	
	public static int getGrid(double sz) {
		if (!gridEnabled)
			return 0;
		if (sz < 0)
			return gridMovement;
		if (sz < 10)
			return gridResizeSmallNodes;
		if (sz < 30)
			return gridResizeNormalNodes;
		return gridResizeLargeNodes;
	}
	
	protected boolean resizeHit_TL(int mx, int my, int x, int y, int w, int h) {
		return resizeHit(mx, my, x, y, w, h, true, false, false, false);
	}
	
	protected boolean resizeHit_TR(int mx, int my, int x, int y, int w, int h) {
		return resizeHit(mx, my, x, y, w, h, false, true, false, false);
	}
	
	protected boolean resizeHit_BR(int mx, int my, int x, int y, int w, int h) {
		return resizeHit(mx, my, x, y, w, h, false, false, false, true);
	}
	
	protected boolean resizeHit_BL(int mx, int my, int x, int y, int w, int h) {
		return resizeHit(mx, my, x, y, w, h, false, false, true, false);
	}
	
	protected boolean resizeHit(int mx, int my, int x, int y, int w, int h,
						boolean tl, boolean tr, boolean bl, boolean br) {
		if (my < y || my > y + h || mx < x || mx > x + w) {
			return false;
		} else {
			int b = BORDERSIZE;
			if (w <= 25 | h <= 25)
				b = 4;
			if (w <= 10 | h <= 10)
				b = 2;
			if (w <= 5 | h <= 5)
				b = 1;
			if (my < y + b) {
				if (mx < x + b) // top left
					return tl;
				if (mx > x + w - b) // top right
					return tr;
			}
			if (my > y + h - b) {
				if (mx < x + b) // bottom left
					return bl;
				if (mx > x + w - b) // bottom right
					return br;
			}
		}
		return false;
	}
	
	private long lastClick = Long.MIN_VALUE;
	
	private Point lastClickPoint;
	
	/**
	 * Invoked when the mouse button has been pressed.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getWhen() <= lastClick)
			return;
		lastClick = e.getWhen();
		
		Component src = findComponentAt(e, e.getX(), e.getY());

		/*
		 * init stuff for view scrolling 
		 */
		prevPosX = e.getX();
		prevPosY = e.getY();
//		System.out.println("selected view: "+src.toString());
		if(src instanceof View)
			selectedView = src;
		else
			selectedView = null;
		
		if (!SwingUtilities.isLeftMouseButton(e) && !SwingUtilities.isMiddleMouseButton(e))
			return;
		
		lastClickPoint = e.getPoint();

		dragged = false;
		
		GravistoService.ensureActiveViewAndSession(e);
		
		selection = session.getSelectionModel().getActiveSelection();
		

		resizeHit = false;
		resizeHitTl = false;
		resizeHitTr = false;
		resizeHitBl = false;
		resizeHitBr = false;
		resizeStartDimW = 0;
		resizeStartDimH = 0;
		moveStartX = 0;
		moveStartY = 0;
		
		if (src == null)
			return;
		
		if (src instanceof View) {
			lastBendHit = null;
			
			Point2D coord;
			CoordinateAttribute coordAttr;
			View v = (View) src;
			
			bendsearch: for (Edge edge : v.getGraph().getEdges()) {
				SortedCollectionAttribute bendsColl = (SortedCollectionAttribute) edge
									.getAttribute(GraphicAttributeConstants.BENDS_PATH);
				Collection<?> bends = bendsColl.getCollection().values();
				
				for (Iterator<?> it = bends.iterator(); it.hasNext();) {
					coordAttr = (CoordinateAttribute) it.next();
					coord = coordAttr.getCoordinate();
					
					if (hit(e.getPoint(), coord)) {
						lastBendHit = coordAttr;
						lastPressedMousePointRel = new Point2D.Double(coord.getX()
											- e.getPoint().getX(), coord.getY()
											- e.getPoint().getY());
						
						break bendsearch;
					}
				}
			}
			
			if (lastBendHit == null) {
				if (!isControlDown(e)) {
					if (getAllMarkedComps().size() > 0) {
						unmarkAll();
						fireSelectionChanged();
					} else {
						if (!selection.isEmpty()) {
							selection.clear();
							fireSelectionChanged();
						}
					}
				}
			}
			
			lastPressedPoint = e.getPoint();
		} else
			if (src instanceof NodeComponent) {
				NodeComponent nodeComp = (NodeComponent) src;
				
				if (e.getClickCount() > 1 && e.isShiftDown()) {
					LinkedHashSet<Node> nodes = new LinkedHashSet<Node>();
					Node node = (Node) nodeComp.getGraphElement();
					nodes.add(node);
					nodes.addAll(MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection().getNodes());
					AttributeHelper.switchVisibilityOfChildElements(nodes);
					postProcessVisibilityChange(node);
				} else
					if (e.getClickCount() > 1) {
						AdvancedLabelTool.processLabelEdit(nodeComp);
						return;
					}
				
				checkResizeHit(e.getX(), e.getY(), nodeComp);
				
				// if (!selection.contains(nodeComp.getGraphElement()) || isInnerNode(nodeComp.getGraphElement()))
				boolean mid = SwingUtilities.isMiddleMouseButton(e);
				
				// if (!isInnerNode(nodeComp.getGraphElement()))
				mid = !mid;
				
				mark(nodeComp,
									(!resizeHit && !mid),
									isControlDown(e) || (e.getClickCount() == 1 && e.isShiftDown()), this, true);
				
				if (resizeHit) {
					lastPressedMousePointRel = new Point2D.Double(
										e.getPoint().getX(),
										e.getPoint().getY());
					Dimension size = ((DimensionAttribute) (nodeComp
										.getGraphElement().getAttribute(GraphicAttributeConstants.DIM_PATH)))
										.getDimension();
					resizeStartDimW = size.width;
					resizeStartDimH = size.height;
				} else {
					// move hit
					lastPressedMousePointRel = new Point2D.Double(
										getTransformersX(nodeComp.getGraphElement()) - e.getPoint().getX(),
										getTransformersY(nodeComp.getGraphElement()) - e.getPoint().getY());
					moveStartX = (int) getTransformersX(nodeComp.getGraphElement());
					moveStartY = (int) getTransformersY(nodeComp.getGraphElement());
				}
				
				lastPressedPoint = null;
				lastPressed = nodeComp;
				lastBendHit = null;
				
			} else
				if (src instanceof EdgeComponent) {
					
					EdgeComponent edgeComp = (EdgeComponent) src;
					
					if (e.getClickCount() > 1) {
						AdvancedLabelTool.processLabelEdit(edgeComp);
						return;
					}
					
					mark((EdgeComponent) src, SwingUtilities.isMiddleMouseButton(e), isControlDown(e) || (e.getClickCount() == 1 && e.isShiftDown()), this, true);
					lastPressedPoint = null;
					lastPressed = src;
					
					Edge edge = (Edge) edgeComp.getGraphElement();
					SortedCollectionAttribute bendsColl = (SortedCollectionAttribute) edge
										.getAttribute(GraphicAttributeConstants.BENDS_PATH);
					Collection<?> bends = bendsColl.getCollection().values();
					Point2D coord;
					CoordinateAttribute coordAttr;
					lastBendHit = null;
					
					for (Iterator<?> it = bends.iterator(); it.hasNext();) {
						coordAttr = (CoordinateAttribute) it.next();
						coord = coordAttr.getCoordinate();
						
						if (hit(e.getPoint(), coord)) {
							lastBendHit = coordAttr;
							lastPressedMousePointRel = new Point2D.Double(coord.getX()
												- e.getPoint().getX(), coord.getY()
												- e.getPoint().getY());
							
							break;
						}
					}
				} /*
					 * else
					 * if (src!=null)
					 * System.out.println("SRC: "+src.getClass().getSimpleName());
					 */
	}
	
	protected void postProcessVisibilityChange(GraphElement sourceElementGUIinteraction) {
		// empty
	}
	
	private void checkResizeHit(int mx, int my, NodeComponent nodeComp) {
		int x = nodeComp.getX();
		int y = nodeComp.getY();
		int w = nodeComp.getWidth();
		int h = nodeComp.getHeight();
		resizeHitBl = resizeHit(mx, my, x, y, w, h, false, false, true, false);
		resizeHitBr = resizeHit(mx, my, x, y, w, h, false, false, false, true);
		resizeHitTl = resizeHit(mx, my, x, y, w, h, true, false, false, false);
		resizeHitTr = resizeHit(mx, my, x, y, w, h, false, true, false, false);
		resizeHit = resizeHitTl || resizeHitTr || resizeHitBl || resizeHitBr;
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
		
		/*
		 * do nothing if we've moved the scrollpane with the right mouse button
		 */
		if(selectedView != null && SwingUtilities.isRightMouseButton(e)){
			selectedView = null;
			return;
		}
			
		
		// logger.info("MOUSE RELEASED");
		if (selRectComp != null) {
			SelectionRectangle r = (SelectionRectangle) selRectComp
								.getClientProperty(rkey);
			if (r != null) {
				selRectComp.remove(r);
				selRectComp.revalidate();
				selRectComp.putClientProperty(rkey, null);
				selRectComp.repaint();
				selRectComp = null;
			}
		}
		
//		if (!SwingUtilities.isLeftMouseButton(e) && !SwingUtilities.isMiddleMouseButton(e)) {
//			return;
//		}
//		
		super.mouseReleased(e);
		
		if (dragged && (lastPressedPoint != null) && (lastBendHit == null)) {
			// if selection rectangle has been drawn
			if (!isControlDown(e)) {
				unmarkAll();
			}
			
			markInRectangle(e, lastPressedPoint, lastPressedPoint2);
			lastPressedPoint = null;
			fireSelectionChanged();
			// ((Container) e.getSource()).repaint();
		}
		
		if (resizeHit && selection.getElements().size() == 1) {
			unmarkAll();
		}
		
		if (lastBendHit != null && lastBendHit.getAttributable() instanceof Edge) {
			MainFrame.showMessage("Hint: Move edge bend inside source or target node to delete selected edge bend.", MessageType.INFO);
			Vector2d bp = new Vector2d(lastBendHit.getX(), lastBendHit.getY());
			Edge edge = (Edge) lastBendHit.getAttributable();
			Vector2d sp = AttributeHelper.getPositionVec2d(edge.getSource());
			double r1 = min2(AttributeHelper.getWidth(edge.getSource()), AttributeHelper.getHeight(edge.getSource())) / 2
								+ AttributeHelper.getFrameThickNess(edge.getSource());
			double r2 = min2(AttributeHelper.getWidth(edge.getTarget()), AttributeHelper.getHeight(edge.getTarget())) / 2
								+ AttributeHelper.getFrameThickNess(edge.getTarget());
			Vector2d tp = AttributeHelper.getPositionVec2d(edge.getTarget());
			if (sp.distance(bp) < r1 || tp.distance(bp) < r2) {
				lastBendHit.getParent().remove(lastBendHit);
				MainFrame.showMessage("Edge bend moved to src/tgt --> removed!", MessageType.INFO);
			} else
				if (lastBendAdded != null && e.getWhen() - lastBendAddedTime < 1000) {
					double ddd = distance(lastBendAddedInitX, lastBendAddedInitY, e.getX(), e.getY());
					// System.out.println("UP "+lastBendAddedInitX+" / "+lastBendAddedInitY+" <--> "+e.getX()+" / "+e.getY());
					if (ddd <= 20) {
						lastBendHit.getParent().remove(lastBendHit);
						MainFrame.showMessage("New bend not or only slightly (d=" + (int) ddd + ") moved --> removed!", MessageType.INFO);
					} else {
						// System.out.println("lastBendAdded!=null, d="+ddd);
						lastBendAdded = null;
					}
				} // else System.out.println("lastBendAdded==null");
		}
		
		originalCoordinates = null;
		
		ToolButton.requestToolButtonFocus();
		
	}
	
	private double min2(double a, double b) {
		return a < b ? a : b;
	}
	
	/**
	 * Resets the tool to initial values.
	 */
	public void reset() {
		lastPressedPoint = null;
		lastClickPoint = null;
	}
	
	/**
	 * Adds all coordinates of bends of an edge into a set.
	 * 
	 * @param set
	 *           The set where the bends are put.
	 * @param edge
	 *           The bends of this edge are processed.
	 */
	@SuppressWarnings("unchecked")
	protected void addBends(Set<CoordinateAttribute> set, Edge edge) {
		SortedCollectionAttribute bends = (SortedCollectionAttribute) edge
							.getAttribute(GraphicAttributeConstants.GRAPHICS
												+ Attribute.SEPARATOR + GraphicAttributeConstants.BENDS);
		Collection<CoordinateAttribute> bendCoords = (Collection) bends.getCollection().values();
		set.addAll(bendCoords);
	}
	
	/**
	 * Highlights all nodes / edges that lie entirely inside the rectangle given
	 * by the two points. Those two points must already be zoomed.
	 * 
	 * @param comp
	 *           mouseEvent whose source is searched
	 * @param p1
	 *           first corner of the rectangle
	 * @param p2
	 *           second corner of the rectangle
	 */
	private void displayAsMarkedInRectangle(JComponent comp, Point2D p1,
						Point2D p2) {
		selRect.setFrameFromDiagonal(p1, p2);
		
		// Component[] allComps = comp.getComponents();
		//
		// for (int i = 0; i < allComps.length; i++) {
		// if (!selectedContain(allComps[i])
		// && selRect.contains(allComps[i].getBounds())
		// && allComps[i] instanceof GraphElementComponent) {
		// highlight(allComps[i]);
		// } else if (allComps[i] instanceof GraphElementComponent) {
		// // unmark((GraphElementComponent) allComps[i]);
		// }
		// }
	}
	
	public static boolean hit(Point2D pnt1, Point2D pnt2) {
		if ((Math.abs(pnt1.getX() - pnt2.getX()) < EdgeShape.CLICK_TOLERANCE)
							&& (Math.abs(pnt1.getY() - pnt2.getY()) < EdgeShape.CLICK_TOLERANCE)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Marks all nodes / edges that lie entirely inside the rectangle given by
	 * the two points. Uses me.getSource().
	 * 
	 * @param me
	 *           mouseEvent whose source is searched
	 * @param p1
	 *           first corner of the rectangle
	 * @param p2
	 *           second corner of the rectangle
	 */
	private void markInRectangle(MouseEvent me, Point2D p1, Point2D p2) {
		if (p1 == null || p2 == null || (p1.distance(p2) <= 0))
			return;
		Rectangle selRect = new Rectangle();
		selRect.setFrameFromDiagonal(p1, p2);
		
		Component[] allComps = ((JComponent) me.getSource()).getComponents();
		// // probably could leave that out ... :
		// if (!isControlDown(me)) {
		// selection.clear();
		// }
		
		if (me.isShiftDown())
			for (int i = 0; i < allComps.length; i++) {
				if (selRect.intersects(allComps[i].getBounds())
									&& allComps[i] instanceof GraphElementComponent) {
					mark((GraphElementComponent) allComps[i], false, true, this, false);
				}
			}
		else
			for (int i = 0; i < allComps.length; i++) {
				if (selRect.contains(allComps[i].getBounds())
									&& allComps[i] instanceof GraphElementComponent) {
					mark((GraphElementComponent) allComps[i], false, true, this, false);
				}
			}
	}
	
	/**
	 * Draws a rectangle on the given graphics context.
	 * 
	 * @param comp
	 *           context to draw upon
	 * @param p1
	 *           first corner of the rectangle
	 * @param p2
	 *           second corner of the rectangle
	 */
	private void paintSelectionRectangle(JComponent comp, Point2D p1, Point2D p2) {
		int tlx;
		int tly;
		int w;
		int h;
		tlx = (int) Math.min(p1.getX(), p2.getX());
		tly = (int) Math.min(p1.getY(), p2.getY());
		w = (int) Math.abs(p1.getX() - p2.getX());
		h = (int) Math.abs(p1.getY() - p2.getY());
		
		displayAsMarkedInRectangle(comp, p1, p2);
		
		// g.drawRect(tlx, tly, w, h);
		
		SelectionRectangle r = (SelectionRectangle) comp.getClientProperty(rkey);
		if (r == null) {
			r = new SelectionRectangle();
			comp.add(r, 0);
			comp.revalidate();
			comp.putClientProperty(rkey, r);
			selRectComp = comp;
		}
		
		comp.repaint(r.getBounds());
		r.setBounds(tlx, tly, w, h);
		r.repaint();
	}
	
	public static Vector2d getLastMovementPosition() {
		Vector2d res = new Vector2d(lastDragX, lastDragY);
		res.applyGrid(10, 10);
		return res;
	}
	
	public static MegaMoveTool getInstance() {
		return thisInstance;
	}
	
	public String getToolName() {
		return "MegaMoveTool";
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
