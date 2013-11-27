// ==============================================================================
//
// MegaTools.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MegaTools.java,v 1.3 2012-11-07 14:42:19 klukas Exp $

package org.graffiti.plugins.modes.defaults;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.Timer;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.GraffitiFrame;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.tool.AbstractTool;
import org.graffiti.plugin.tool.AbstractUndoableTool;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.EdgeComponent;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.plugins.views.defaults.NodeComponent;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionModel;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;

/**
 * DOCUMENT ME!
 * 
 * @author holleis
 * @version $Revision: 1.3 $
 */
public abstract class MegaTools extends AbstractUndoableTool {
	// ~ Instance fields ========================================================
	
	// protected EditorSession session;
	
	/** DOCUMENT ME! */
	protected SelectionModel selectionModel;
	
	/** DOCUMENT ME! */
	protected final String ACTIVE = "active";
	
	protected Cursor normCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	protected Cursor edgeCursor = new Cursor(Cursor.HAND_CURSOR);
	protected Cursor nodeCursor = new Cursor(Cursor.HAND_CURSOR);
	
	protected Cursor myMoveCursor = new Cursor(Cursor.MOVE_CURSOR);
	
	/** DOCUMENT ME! */
	protected Component lastSelectedComp;
	protected Component selectedView;
	static boolean scrollpanemovement;
	
	// ~ Constructors ===========================================================
	
	// protected Component lastSelectedComp;
	// protected List selectedComps;
	public MegaTools() {
		super();
		// //// MegaTools.selection = new Selection(ACTIVE);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 */
	public void fireSelectionChanged() {
		if (selectionModel != null)
			selectionModel.selectionChanged();
		else
			System.out.println("SEL MODEL NULL");
	}
	
	private static String desiredStatusMessage = null;
	private static long desiredSince = Integer.MIN_VALUE;
	private static final long delayStatus = 200;
	
	private static Timer statusCallTimer = new Timer(100, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (desiredSince != Integer.MAX_VALUE &&
					System.currentTimeMillis() - desiredSince > delayStatus) {
				MainFrame.showMessage(desiredStatusMessage, MessageType.PERMANENT_INFO);
				desiredSince = Integer.MAX_VALUE;
			}
		}
	});
	
	private static MouseEvent lastMouseE = null;
	private static Component lastMouseSrc = null;
	
	public static boolean MouseWheelZoomEnabled = !AttributeHelper.macOSrunning();
	
	/**
	 * Temporarily marks the component under cursor.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// MainFrame.showMesssage(e.getX()+" - "+e.getY(), MessageType.PERMANENT_INFO);
		// System.out.println(e.getSource().toString());
		Component src = findComponentAt(e, e.getX(), e.getY());
		setLastMouseComponent(e, src);
		
		informAttributeComponentsAboutMouseEvents(e, src);
		
		boolean useNodeCursor = src instanceof NodeComponent;
		boolean useEdgeCursor = src instanceof EdgeComponent;
		
		if (!statusCallTimer.isRunning()) {
			statusCallTimer.setRepeats(true);
			statusCallTimer.start();
		}
		
		// System.out.println("SRC: "+src.toString());
		if (src != null && !src.equals(lastSelectedComp)) {
			if ((lastSelectedComp != null) && !selectedContain(lastSelectedComp)) {
				if (lastSelectedComp instanceof GraphElementComponent)
					for (GraphElementComponent c : getCompsForElem(((GraphElementComponent) lastSelectedComp).getGraphElement()))
						unDisplayAsMarked(c);
				// src.getParent().repaint();
				desiredStatusMessage = null;
				desiredSince = Integer.MIN_VALUE;
			}
			if ((lastSelectedComp != null) && selectedContain(lastSelectedComp)) {
				if (lastSelectedComp instanceof GraphElementComponent)
					for (GraphElementComponent c : getCompsForElem(((GraphElementComponent) lastSelectedComp).getGraphElement())) {
						displayAsMarked(c);
					}
				// src.getParent().repaint();
				desiredStatusMessage = null;
				desiredSince = Integer.MIN_VALUE;
			}
			
			if (src instanceof View) {
				lastSelectedComp = null;
			} else {
				lastSelectedComp = src;
				
				// if (!selectedContain(lastSelectedComp)) {
				for (Component c : getCompsForElem(((GraphElementComponent) lastSelectedComp).getGraphElement())) {
					highlight(c, e);
					break;
				}
				// }
				if (lastSelectedComp != null && lastSelectedComp.getParent() != null)
					lastSelectedComp.getParent().repaint();
			}
		} else {
			if (lastSelectedComp instanceof GraphElementComponent) {
				GraphElementComponent gec = (GraphElementComponent) lastSelectedComp;
				if ((gec instanceof NodeComponent) ||
						(gec instanceof EdgeComponent)) {
					desiredStatusMessage = gec.getToolTipText();
					desiredSince = System.currentTimeMillis();
				}
			}
		}
		if (src != null) {
			if (useNodeCursor) {
				// System.out.println("NodeCursor!");
				GraffitiFrame gf = MainFrame.getInstance().getActiveDetachedFrame();
				if (src != null && src.getParent() != null)
					src.getParent().setCursor(getNodeCursor());
				if (gf != null)
					gf.setCursor(getNodeCursor());
				// Component src2 = getComponentAt(e);
				// src2.setCursor(getNodeCursor());
			} else
				if (useEdgeCursor) {
					// System.out.println("EdgeCursor!");
					GraffitiFrame gf = MainFrame.getInstance().getActiveDetachedFrame();
					AttributeComponent ac = getAttributeComponentAt(e);
					if (ac != null) {
						// MainFrame.showMessage("Info: Edge Attribute Component Hit", MessageType.INFO);
						if (src != null && src.getParent() != null)
							if (gf == null)
								src.getParent().setCursor(myMoveCursor);
							else
								gf.setCursor(myMoveCursor);
					} else {
						if (src != null && src.getParent() != null)
							src.getParent().setCursor(getEdgeCursor());
						if (gf != null)
							gf.setCursor(getEdgeCursor());
					}
					
					// Component src2 = getComponentAt(e);
					// src2.setCursor(getEdgeCursor());
				} else {
					// System.out.println("NormCursor!");
					GraffitiFrame gf = MainFrame.getInstance().getActiveDetachedFrame();
					if (src != null && src.getParent() != null)
						src.setCursor(getNormCursor());
					if (gf != null)
						gf.setCursor(getNormCursor());
					// Component src2 = getComponentAt(e);
					// src2.setCursor(getNormCursor());
				}
		}
		
		// move the screen if no component is selected
		if (src == null) {
			GraffitiView view = (GraffitiView) session.getActiveView();
		}
	}
	
	private void informAttributeComponentsAboutMouseEvents(MouseEvent e,
			Component src) {
		if (src != null && !(src instanceof View))
			if (src instanceof MouseMotionListener) {
				MouseMotionListener mml = (MouseMotionListener) src;
				mml.mouseMoved(e);
			} else {
				if (src != null && src instanceof GraphElementComponent)
					for (GraphElementComponent c : getCompsForElem(((GraphElementComponent) src).getGraphElement())) {
						for (Object o : c.getAttributeComponents()) {
							if (!(o instanceof AttributeComponent))
								continue;
							AttributeComponent ac = (AttributeComponent) o;
							if (ac instanceof MouseMotionListener) {
								MouseMotionListener mml = (MouseMotionListener) ac;
								mml.mouseMoved(e);
							}
						}
					}
			}
	}
	
	protected static void setLastMouseComponent(MouseEvent e, Component src) {
		MegaTools.lastMouseE = e;
		MegaTools.lastMouseSrc = src;
	}
	
	protected Cursor getNormCursor() {
		return normCursor;
	}
	
	protected Cursor getEdgeCursor() {
		return edgeCursor;
	}
	
	protected Cursor getNodeCursor() {
		return nodeCursor;
	}
	
	/**
	 * Called when the active session is changed.
	 * 
	 * @param s
	 *           DOCUMENT ME!
	 */
	@Override
	public void sessionChanged(Session s) {
		super.sessionChanged(s);
		
		lastMouseSrc = null;
		
		// this.session = (EditorSession) s;
		
		// there is a new active session. Change the selection
		// model therefore
		if (s != null) {
			this.selectionModel = ((EditorSession) s).getSelectionModel();
			this.selection = selectionModel.getActiveSelection();
			unDisplayAsMarked(getCompsForElems(s.getGraph().getGraphElements()));
			displayAsMarked(getAllMarkedComps());
		} else {
			// there is currently no active session.
			// therefore there is no selection model
			this.selectionModel = null;
			this.selection = new Selection(ACTIVE);
			this.lastSelectedComp = null;
		}
	}
	
	/**
	 * Called when the session data (not the session's graph data!) changed.
	 * 
	 * @param s
	 *           DOCUMENT ME!
	 */
	@Override
	public void sessionDataChanged(Session s) {
		super.sessionDataChanged(s);
		this.sessionChanged(s);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param me
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected boolean isControlDown(MouseEvent me) {
		boolean result = me.isControlDown();
		return result;
	}
	
	protected boolean isAltDown(MouseEvent me) {
		return me.isAltDown();
	}
	
	/**
	 * Returns Component found at position and in source indicated by the
	 * given mouse event. Ignores everything but nodes, edges and the view.
	 * 
	 * @param me
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected Component findComponentAt(MouseEvent me, int x, int y) {
		if (session == null || session.getActiveView() == null)
			return null;
		if (!(session.getActiveView() instanceof GraffitiView))
			return null;
		GraffitiView view = (GraffitiView) session.getActiveView();
		Component c = view.findComponentAt(x, y);
		if (c instanceof AttributeComponent) {
			AttributeComponent ac = (AttributeComponent) c;
			return view.getComponentForElement((GraphElement) ac.getAttribute().getAttributable());
		}
		return c;
	}
	
	protected Component getComponentAt(MouseEvent me) {
		GraffitiView view = (GraffitiView) session.getActiveView();
		Component c = view.getComponentAt(me.getX(), me.getY());
		if (c instanceof AttributeComponent) {
			AttributeComponent ac = (AttributeComponent) c;
			return view.getComponentForElement((GraphElement) ac.getAttribute().getAttributable());
		}
		return c;
	}
	
	protected AttributeComponent getAttributeComponentAt(MouseEvent me) {
		Session session = MainFrame.getInstance().getActiveSession();
		GraffitiView view = (GraffitiView) session.getActiveView();
		Component c = view.getComponentOfAnyTypeAt(me.getX(), me.getY());
		if (c instanceof AttributeComponent) {
			AttributeComponent ac = (AttributeComponent) c;
			return ac;
		}
		return null;
	}
	
	/**
	 * Add component to selection.
	 * 
	 * @param geComp
	 *           the comp holding the element to add to the selection.
	 * @param ctrlPressed
	 *           true if the ctrl-key has been pressed
	 * @param caller
	 *           DOCUMENT ME!
	 * @param issueSelectionEventIfSelectionChanged
	 */
	protected void mark(GraphElementComponent geComp, boolean findContainingNodes, boolean ctrlPressed,
			AbstractTool caller, boolean issueSelectionEventIfSelectionChanged) {
		if (geComp != null) {
			GraphElement ge = geComp.getGraphElement();
			ArrayList<Node> gelist = null;
			if (findContainingNodes && ge != null && ge instanceof Node) {
				Node n = (Node) ge;
				gelist = findContainingNodes(n);
			}
			
			if (selection.getNodes().contains(ge)
					|| selection.getEdges().contains(ge)) {
				if (ctrlPressed) {
					// ctrl and marked => unmark node
					unmark(geComp);
					if (gelist != null) {
						for (Node n : gelist) {
							for (GraphElementComponent gec : getCompsForElem(n)) {
								unmark(gec);
							}
						}
					}
				} else {
					if (findContainingNodes) {
						caller.unDisplayAsMarked(getAllMarkedComps());
						selection.clear();
						selection.add(ge);
						caller.displayAsMarked(geComp);
						if (gelist != null) {
							for (Node n : gelist) {
								selection.add(n);
								for (GraphElementComponent gec : getCompsForElem(n)) {
									caller.displayAsMarked(gec);
								}
							}
						}
					}
				}
			} else {
				if (!ctrlPressed) {
					// unselect all previously selected and create new selection
					caller.unDisplayAsMarked(getAllMarkedComps());
					
					// selection = new Selection(ACTIVE);
					// // resetSelectedComps();
					// selectionModel.add(selection);
					// selectionModel.setActiveSelection(ACTIVE);
					selection.clear();
				}
				
				// add graphelement to selection
				// selectedComps.add(geComp);
				selection.add(ge);
				// caller.displayAsMarked(geComp);
				if (gelist != null) {
					for (Node n : gelist) {
						selection.add(n);
						for (GraphElementComponent gec : getCompsForElem(n)) {
							caller.displayAsMarked(gec);
						}
					}
				}
				
				if (issueSelectionEventIfSelectionChanged) {
					fireSelectionChanged();
				} else {
					// System.out.println("SEL CHANGE WITHOUT EVENT");
				}
			}
		} else
			System.out.println("Can't highlight selection!");
	}
	
	private ArrayList<Node> findContainingNodes(Node n) {
		ArrayList<Node> result = new ArrayList<Node>();
		if (n != null && n.getGraph() != null) {
			Graph g = n.getGraph();
			Vector2d pos = AttributeHelper.getPositionVec2d(n);
			Vector2d size = AttributeHelper.getSize(n);
			Rectangle2D a = new Rectangle2D.Double(pos.x - size.x / 2, pos.y - size.y / 2, size.x, size.y);
			if (size.x > 30 && size.y > 30)
				for (Node check : g.getNodes()) {
					Vector2d cs = AttributeHelper.getSize(check);
					if (cs.x >= size.x)
						continue;
					if (cs.y >= size.y)
						continue;
					Vector2d cp = AttributeHelper.getPositionVec2d(check);
					Rectangle2D b = new Rectangle2D.Double(cp.x - cs.x / 2, cp.y - cs.y / 2, cs.x, cs.y);
					if (a.intersects(b) || (cs.x == 0 && a.contains(cp.x, cp.y))) {
						// if (a.contains(b))
						result.add(check);
					}
				}
		}
		return result;
	}
	
	protected ArrayList<Node> findContainingParentNodes(Node n) {
		ArrayList<Node> result = new ArrayList<Node>();
		if (n != null && n.getGraph() != null) {
			Graph g = n.getGraph();
			Vector2d pos = AttributeHelper.getPositionVec2d(n);
			Vector2d size = AttributeHelper.getSize(n);
			Rectangle2D a = new Rectangle2D.Double(pos.x - size.x / 2, pos.y - size.y / 2, size.x, size.y);
			if (size.x > 30 && size.y > 30)
				for (Node check : g.getNodes()) {
					Vector2d cs = AttributeHelper.getSize(check);
					if (cs.x < size.x)
						continue;
					if (cs.y < size.y)
						continue;
					Vector2d cp = AttributeHelper.getPositionVec2d(check);
					Rectangle2D b = new Rectangle2D.Double(cp.x - cs.x / 2, cp.y - cs.y / 2, cs.x, cs.y);
					if (a.intersects(b) || (cs.x == 0 && a.contains(cp.x, cp.y))) {
						// if (a.contains(b))
						result.add(check);
					}
				}
		}
		return result;
	}
	
	/**
	 * Returns true if the current selection contains the given <code>GraphElement</code>.
	 * 
	 * @param ge
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected boolean selectedContain(GraphElement ge) {
		if (selection.getNodes().contains(ge)
				|| selection.getEdges().contains(ge)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true if the current selection contains the given <code>GraphElementComponent</code>.
	 * 
	 * @param gec
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected boolean selectedContain(Component gec) {
		if (gec != null) {
			try {
				GraphElement ge = ((GraphElementComponent) gec).getGraphElement();
				
				if (selection.getNodes().contains(ge)
						|| selection.getEdges().contains(ge)) {
					return true;
				}
			} catch (ClassCastException cce) {
				return false;
			}
		}
		
		return false;
	}
	
	/**
	 * Removes the graphelement from the selection.
	 * 
	 * @param geComp
	 *           the comp holding the element to add to the selection.
	 */
	protected void unmark(GraphElementComponent geComp) {
		if (geComp != null) {
			GraphElement ge = geComp.getGraphElement();
			
			// assert selectedComps.contains(ge);
			selection.remove(ge);
			
			// selectedComps.remove(geComp);
			unDisplayAsMarked(geComp);
			selectionModel.selectionChanged();
		}
	}
	
	/**
	 * Clears the selection. Does not fire a selectionChanged event.
	 */
	public void unmarkAll() {
		// MainFrame.getInstance().getActiveEditorSession().getActiveView()
		// .getViewComponent().repaint();
		unDisplayAsMarked(getAllMarkedComps());
		selection.clear();
	}
	
	public static MouseEvent getLastMouseE() {
		return lastMouseE;
	}
	
	public static boolean wasScrollPaneMovement() {
		boolean ret = scrollpanemovement;
		scrollpanemovement = false; // reset
		return ret;
	}
	
	public static Component getLastMouseSrc() {
		return lastMouseSrc;
	}
	
	@Override
	public void preProcessImageCreation() {
		avoidHighlight = true;
		if (lastSelectedComp != null && !selectedContain(lastSelectedComp)) {
			unDisplayAsMarked((GraphElementComponent) lastSelectedComp);
		}
		unDisplayAsMarked(getAllMarkedComps());
	}
	
	@Override
	public void postProcessImageCreation() {
		avoidHighlight = false;
		if (lastSelectedComp != null && !selectedContain(lastSelectedComp)) {
			highlight(lastSelectedComp, null);
		}
		displayAsMarked(getAllMarkedComps());
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
