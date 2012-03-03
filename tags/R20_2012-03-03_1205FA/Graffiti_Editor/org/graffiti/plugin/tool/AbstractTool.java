// ==============================================================================
//
// AbstractTool.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractTool.java,v 1.2 2011-05-13 09:07:25 klukas Exp $

package org.graffiti.plugin.tool;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.gui.ToolButton;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.EdgeComponentInterface;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.GraphElementComponentInterface;
import org.graffiti.plugin.view.NodeComponentInterface;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import scenario.ScenarioService;

/**
 * Provides an abstract implementation of the <code>Tool</code> interface.
 * 
 * @see Tool
 * @see javax.swing.event.MouseInputAdapter
 */
public abstract class AbstractTool
					extends MouseInputAdapter
					implements Tool, SessionListener, SelectionListener {
	// ~ Instance fields ========================================================
	
	protected JComponent mouseComp = null;
	
	/** DOCUMENT ME! */
	/** DOCUMENT ME! */
	protected AffineTransform zoom = View.NO_ZOOM;
	
	/** The current session that this tool should work on / with. */
	protected EditorSession session;
	
	/** The graph this tool works on. */
	// private Graph graph;
	
	/** The preferences of this tool. */
	protected GravistoPreferences prefs;
	
	/** The current selection that this tool should work on / with. */
	protected Selection selection;
	
	protected static Tool lastActiveTool = null;
	
	/** Flag set by <code>activate</code> and <code>deactivate</code>. */
	protected boolean isActive;
	
	/** Used to display marked nodes. */
	/** Size of bullets used to display marked edges. */
	protected static final int BORDERSIZE = 10;
	
	// private final LineBorder border = new LineBorder(java.awt.Color.RED, 4);
	
	/** DOCUMENT ME! */
	private static final Border border = new NodeBorder(Color.RED.brighter().brighter(), BORDERSIZE);
	
	private static List<Tool> knownTools;
	
	private static Timer checkActivationTimer = new Timer(500, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			synchronized (getKnownTools()) {
				boolean isOneActive = false;
				for (Tool t : getKnownTools()) {
					if (t.isActive()) {
						isOneActive = true;
						lastActiveTool = t;
						break;
					}
				}
				if (!isOneActive && lastActiveTool != null) {
					lastActiveTool.activate();
				}
			}
		}
	});
	
	private static final Border edgeBorder = new EdgeBorder(java.awt.Color.RED,
						BORDERSIZE, true);
	
	// private final Border border = new NodeBorder(java.awt.Color.RED, 8);
	// private final Border border = new TitledBorder("Node");
	
	/** Used to temporarily highlight nodes. */
	private static final Border tempBorder = new NodeBorder(java.awt.Color.ORANGE, BORDERSIZE);
	private static final Border tempBorderLINK = new NodeBorder(java.awt.Color.BLUE, BORDERSIZE);
	// private static final Border tempBorderEdge = new EdgeBorder(java.awt.Color.ORANGE, BORDERSIZE, true);
	
	/** Border for unmarked graph elements. */
	private static final EmptyBorder empty = new EmptyBorder(0, 0, 0, 0); // 3, 3, 3, 3 ?
	
	// ~ Methods ================================================================
	
	protected static List<Tool> getKnownTools() {
		if (knownTools == null)
			knownTools = new ArrayList<Tool>();
		return knownTools;
	}
	
	public AbstractTool() {
		synchronized (checkActivationTimer) {
			if (!checkActivationTimer.isRunning()) {
				checkActivationTimer.setRepeats(true);
				checkActivationTimer.start();
			}
		}
	}
	
	protected Graph getGraph() {
		return MainFrame.getInstance().getActiveEditorSession().getGraph();
	}
	
	public static Tool getActiveTool() {
		for (Tool at : getKnownTools()) {
			if (at.isActive())
				return at;
		}
		return null;
	}
	
	/**
	 * Returns true if this tool has been activated and since then not been
	 * deactivated.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isActive() {
		return this.isActive;
	}
	
	/**
	 * Sets the graph of this tool.
	 * 
	 * @param graph
	 *           the graph of this tool.
	 */
	public void setGraph(Graph graph) {
		// empty
	}
	
	/**
	 * States whether this class wants to be registered as a <code>SelectionListener</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isSelectionListener() {
		return true;
	}
	
	/**
	 * States whether this class wants to be registered as a <code>SessionListener</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isSessionListener() {
		return true;
	}
	
	/**
	 * States whether this class wants to be registered as a <code>ViewListener</code>, i.e. if it wants to get informed when
	 * another view in the same session becomes active. This method is not
	 * called when another session is activated. Implement <code>SessionListener</code> if you are interested in session changed
	 * events.
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isViewListener() {
		return false;
	}
	
	public static boolean activateTool(String id) {
		for (Tool t : getKnownTools()) {
			if (t.getToolName().equals(id)) {
				t.activate();
				return t.isActive();
			}
		}
		return false;
	}
	
	/**
	 * Classes that overwrite this method should call super.active first.
	 * 
	 * @see org.graffiti.plugin.tool.Tool#activate()
	 */
	public void activate() {
		
		ScenarioService.postWorkflowStep(
							"Activate " + getToolName(),
							new String[] { "import org.graffiti.plugin.tool.AbstractTool;" },
							new String[] { "AbstractTool.activateTool(\"" + getToolName() + "\");" });
		
		// System.out.println("Activate "+toString());
		
		deactivateAll();
		//
		// Zoomable myView = MainFrame.getInstance().getActiveSession().getActiveView();
		// ZoomListener zoomView = MainFrame.getInstance().getActiveSession().getActiveView();
		// AffineTransform at = new AffineTransform();
		// at.setToScale(1, 1);
		// zoomView.zoomChanged(at);
		
		try {
			mouseComp = MainFrame.getInstance().getActiveSession().getActiveView().getViewComponent();
			this.isActive = true;
			// logger.entering(this.toString(), "  activate");
			displayAsMarked(this.getAllMarkedComps());
			mouseComp.addMouseListener(this);
			mouseComp.addMouseMotionListener(this);
			mouseComp.repaint();
		} catch (Exception e) {
			isActive = false;
		}
	}
	
	public void deactivateAll() {
		for (Iterator<Tool> it = getKnownTools().iterator(); it.hasNext();) {
			Tool t = it.next();
			t.deactivate();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ToolButton.checkStatusForAllToolButtons();
			}
		});
	}
	
	/**
	 * Classes that overwrite this method should call super.deactive first.
	 * 
	 * @see org.graffiti.plugin.tool.Tool#deactivate()
	 */
	public void deactivate() {
		// System.out.println("Deactivate "+toString());
		if (mouseComp != null) {
			mouseComp.removeMouseListener(this);
			mouseComp.removeMouseMotionListener(this);
		}
		if (this.selection != null) {
			unDisplayAsMarked(getCompsForElems(this.selection.getElements()));
		}
		
		this.isActive = false;
		// logger.entering(this.toString(), "deactivate");
	}
	
	/**
	 * Show a graph element component as marked.
	 * 
	 * @param comp
	 *           DOCUMENT ME!
	 */
	public void displayAsMarked(GraphElementComponent comp) {
		if (comp instanceof NodeComponentInterface) {
			displayAsMarked((NodeComponentInterface) comp);
		} else {
			displayAsMarked((EdgeComponentInterface) comp);
		}
	}
	
	/**
	 * Show a node component as marked.
	 * 
	 * @param comp
	 *           DOCUMENT ME!
	 */
	public void displayAsMarked(NodeComponentInterface comp) {
		if (comp != null) {
			((JComponent) comp).setBorder(border);
			((JComponent) comp).repaint();
			// ((JComponent) comp).getParent().repaint();
		}
	}
	
	/**
	 * Show an edge component as marked.
	 * 
	 * @param comp
	 *           DOCUMENT ME!
	 */
	public void displayAsMarked(EdgeComponentInterface comp) {
		if (comp != null) {
			((JComponent) comp).setBorder(edgeBorder);
			// ((JComponent) comp).repaint();
		}
	}
	
	/**
	 * Display a list of graph element components
	 * 
	 * @param comps
	 *           DOCUMENT ME!
	 */
	public void displayAsMarked(List<GraphElementComponent> comps) {
		for (Iterator<GraphElementComponent> it = comps.iterator(); it.hasNext();) {
			displayAsMarked((it.next()));
		}
	}
	
	protected boolean avoidHighlight = false;
	
	/**
	 * Display a component in a special way distinguished from the way <code>displayAsMarked</code> does it. Used for temporarily highlighting
	 * a component, e.g. for a mouseMoved action.
	 * 
	 * @param comp
	 *           DOCUMENT ME!
	 */
	public void highlight(Component comp, MouseEvent e) {
		if (avoidHighlight)
			return;
		boolean processed = false;
		if (comp != null) {
			if (comp instanceof GraphElementComponentInterface) {
				GraphElementComponentInterface nci = (GraphElementComponentInterface) comp;
				GraphElement n = nci.getGraphElement();
				try {
					ReceiveHighlightInfo hi = (ReceiveHighlightInfo) MainFrame.getInstance().getActiveEditorSession().getActiveView();
					hi.isHighlighted(n);
					processed = true;
				} catch (Exception err) {
					// empty
				}
				if (AttributeHelper.hasAttribute(n, "", "url"))
					((JComponent) comp).setBorder(tempBorderLINK);
				else
					((JComponent) comp).setBorder(tempBorder);
				
				List<AttributeComponent> acc = getAttributeCompsForElem(n);
				for (AttributeComponent ac : acc) {
					ac.highlight(true, e);
				}
			} else
				((JComponent) comp).setBorder(tempBorder);
			if (((JComponent) comp).getParent() != null)
				((JComponent) comp).getParent().repaint();
		}
		if (!processed) {
			try {
				ReceiveHighlightInfo hi = (ReceiveHighlightInfo) MainFrame.getInstance().getActiveEditorSession().getActiveView();
				hi.isHighlighted(null);
			} catch (Exception err) {
				// empty
			}
		}
	}
	
	/**
	 * Called when the selection has changed.
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void selectionChanged(SelectionEvent e) {
		Selection sel = e.getSelection();
		
		if (this.isActive()) {
			if (!sel.equals(this.selection) ||
								(sel.getNewUnmarked().isEmpty() &&
								sel.getNewMarked().isEmpty())) {
				// must completely renew selection
				if (selection != null) {
					unDisplayAsMarked(getAllMarkedComps());
				}
				
				displayAsMarked(getCompsForElems(sel.getElements()));
			} else {
				List<GraphElementComponent> list = new LinkedList<GraphElementComponent>();
				
				for (Iterator<?> it = sel.getNewUnmarked().keySet().iterator(); it.hasNext();) {
					list.addAll(getCompsForElem((GraphElement) it.next()));
				}
				
				unDisplayAsMarked(list);
				list = new LinkedList<GraphElementComponent>();
				
				for (Iterator<?> it = sel.getNewMarked().keySet().iterator(); it.hasNext();) {
					list.addAll(getCompsForElem((GraphElement) it.next()));
				}
				
				displayAsMarked(list);
			}
			
			// for(Iterator viewIt = session.getViews().iterator();
			// viewIt.hasNext();)
			// {
			// JComponent view = (JComponent) viewIt.next();
			// view.repaint();
			// }
		}
		
		this.selection = sel;
	}
	
	/**
	 * @see org.graffiti.selection.SelectionListener#selectionListChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionListChanged(SelectionEvent e) {
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionChanged(Session)
	 */
	public void sessionChanged(Session s) {
		session = (EditorSession) s;
	}
	
	/**
	 * Remove anything that specifies a graph element component as being
	 * marked.
	 * 
	 * @param comp
	 *           DOCUMENT ME!
	 */
	public void unDisplayAsMarked(GraphElementComponent comp) {
		if (comp instanceof NodeComponentInterface) {
			unDisplayAsMarked((NodeComponentInterface) comp);
		} else {
			unDisplayAsMarked((EdgeComponentInterface) comp);
		}
		if (comp != null) {
			GraphElement ge = comp.getGraphElement();
			if (ge != null) {
				List<AttributeComponent> acc = getAttributeCompsForElem(ge);
				if (acc != null)
					for (AttributeComponent ac : acc) {
						ac.highlight(false, null);
					}
			}
		}
	}
	
	/**
	 * Remove anything that specifies a node component as being marked.
	 * 
	 * @param comp
	 *           DOCUMENT ME!
	 */
	private void unDisplayAsMarked(NodeComponentInterface comp) {
		if (comp != null && ((JComponent) comp).getBorder() != empty) {
			((JComponent) comp).setBorder(empty);
			if (((JComponent) comp).getParent() != null)
				((JComponent) comp).getParent().repaint();
		}
	}
	
	/**
	 * Remove anything that specifies an edge component as being marked.
	 * 
	 * @param comp
	 *           DOCUMENT ME!
	 */
	private void unDisplayAsMarked(EdgeComponentInterface comp) {
		if (comp != null && ((JComponent) comp).getBorder() != empty) {
			((JComponent) comp).setBorder(empty);
			if (((JComponent) comp).getParent() != null)
				((JComponent) comp).getParent().repaint();
		}
	}
	
	/**
	 * Call <code>unDisplayAsMarked(GraphElementComponent geComp)</code> on
	 * every element of the provided list.
	 * 
	 * @param comps
	 *           DOCUMENT ME!
	 */
	@SuppressWarnings("unchecked")
	public void unDisplayAsMarked(List comps) {
		for (Iterator it = comps.iterator(); it.hasNext();) {
			unDisplayAsMarked((GraphElementComponent) (it.next()));
		}
	}
	
	/**
	 * Returns a list of all <code>GraphElementComponents</code> contained in
	 * this selection.
	 * 
	 * @return DOCUMENT ME!
	 */
	@SuppressWarnings("unchecked")
	protected List<GraphElementComponent> getAllMarkedComps() {
		List<GraphElementComponent> geComps = new LinkedList<GraphElementComponent>();
		
		if (selection == null) {
			return geComps;
		}
		
		geComps.addAll(getCompsForElems((Collection) selection.getNodes()));
		geComps.addAll(getCompsForElems((Collection) selection.getEdges()));
		
		return geComps;
	}
	
	/**
	 * Used method <code>getComponentForElement</code> from the views of the
	 * current session to get the <code>GraphElementComponent</code>s for the
	 * provided <code>GraphElement</code>.
	 * 
	 * @param ge
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected List<GraphElementComponent> getCompsForElem(GraphElement ge) {
		if (session != null) {
			List<View> views = session.getViews();
			
			List<GraphElementComponent> comps = new LinkedList<GraphElementComponent>();
			
			for (View view : views) {
				GraphElementComponent v = view.getComponentForElement(ge);
				if (v != null)
					comps.add(v);
			}
			
			return comps;
		} else
			return new LinkedList<GraphElementComponent>();
	}
	
	protected List<AttributeComponent> getAttributeCompsForElem(GraphElement ge) {
		if (session != null) {
			List<View> views = session.getViews();
			
			List<AttributeComponent> comps = new LinkedList<AttributeComponent>();
			
			for (View view : views) {
				Set<AttributeComponent> acc = view.getAttributeComponentsForElement(ge);
				if (acc != null)
					comps.addAll(acc);
			}
			
			return comps;
		} else
			return new LinkedList<AttributeComponent>();
	}
	
	/**
	 * Used method <code>getComponentForElement</code> from the views of the
	 * current session to convert the provided list of <code>GraphElement</code> elements to a list of <code>GraphElementComponent</code>s.
	 * 
	 * @param elems
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected List<GraphElementComponent> getCompsForElems(Collection<GraphElement> elems) {
		if (session != null) {
			List<View> views = session.getViews();
			
			// View view = session.getActiveView();
			List<GraphElementComponent> comps = new LinkedList<GraphElementComponent>();
			
			for (GraphElement ge : elems) {
				for (View view : views) {
					if (view.getComponentForElement(ge) != null)
						comps.add(view.getComponentForElement(ge));
				}
			}
			
			return comps;
		} else
			
			return new LinkedList<GraphElementComponent>();
	}
	
	public void setPrefs(GravistoPreferences p) {
		prefs = p;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
