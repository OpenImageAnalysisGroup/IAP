// ==============================================================================
//
// StatusBar.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StatusBar.java,v 1.1 2011-01-31 09:04:26 klukas Exp $

package org.graffiti.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.core.StringBundle;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.GraphListener;
import org.graffiti.event.ListenerManager;
import org.graffiti.event.ListenerNotFoundException;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * Represents a status line ui component, which can display info and error
 * messages.
 * 
 * @version $Revision: 1.1 $
 */
public class StatusBar
					extends JPanel
					implements SessionListener, SelectionListener, GraphListener {
	// ~ Static fields/initializers =============================================
	
	private static final long serialVersionUID = 1L;
	
	/** The time, a message is displayed in the status line. */
	private static final int DELAY = 5000;
	
	/** The font, which is used to display an info message. */
	private static final Font PLAIN_FONT = new Font("dialog", Font.PLAIN, 12);
	
	/** The font, which is used to display an error message. */
	private static final Font BOLD_FONT = new Font("dialog", Font.BOLD, 12);
	
	// ~ Instance fields ========================================================
	
	/** The nodes- and edges label in the status bar. */
	private JLabel edgesLabel;
	
	/** The nodes- and edges label in the status bar. */
	private JLabel nodesLabel;
	
	/** The ui component, which contains the status text. */
	JLabel statusLine;
	
	/** The current session, this status bar is listening to. */
	private Session currentSession;
	
	/** The number of edges. */
	private int edges;
	
	/** The number of nodes. */
	private int nodes;
	
	private int ignoreUpdate = 0;
	
	private Selection activeSelection = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new status bar.
	 * 
	 * @param sBundle
	 *           DOCUMENT ME!
	 */
	public StatusBar(StringBundle sBundle) {
		super();
		
		nodes = 0;
		edges = 0;
		
		setLayout(new GridBagLayout());
		
		statusLine = new MyJLabel("");
		statusLine.setBorder(BorderFactory.createEtchedBorder());
		// statusLine.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		statusLine.setToolTipText("<html><small>Click or <b>use F2</b> to view full status text");
		/*
		 * statusLine.setBorder(BorderFactory.createCompoundBorder(
		 * BorderFactory.createLoweredBevelBorder(), statusLine.getBorder()));
		 */
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(1, 1, 1, 1);
		
		add(statusLine, c);
		
		nodesLabel = new JLabel(" ");
		nodesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		// nodesLabel.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// }});
		nodesLabel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				processRightClick(e, true);
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseReleased(MouseEvent e) {
			}
			
			public void mouseEntered(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
			}
		});
		nodesLabel.setToolTipText(sBundle.getString("statusBar.nodes.tooltip"));
		nodesLabel.setBorder(BorderFactory.createEtchedBorder());
		/*
		 * nodesLabel.setBorder(BorderFactory.createCompoundBorder(
		 * BorderFactory.createLoweredBevelBorder(), nodesLabel.getBorder()));
		 */
		nodesLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		
		edgesLabel = new JLabel(" ");
		edgesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		edgesLabel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				processRightClick(e, false);
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseReleased(MouseEvent e) {
			}
			
			public void mouseEntered(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
			}
		});
		edgesLabel.setToolTipText(sBundle.getString("statusBar.edges.tooltip"));
		edgesLabel.setBorder(BorderFactory.createEtchedBorder());
		/*
		 * edgesLabel.setBorder(BorderFactory.createCompoundBorder(
		 * BorderFactory.createLoweredBevelBorder(), edgesLabel.getBorder()));
		 */
		edgesLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		
		c.gridx = 3;
		c.weightx = 0.0;
		
		JLabel memLabel = GravistoService.getMemoryInfoLabel(true);
		memLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		// memLabel.setBorder(BorderFactory.createEtchedBorder());
		memLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		memLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		add(memLabel, c);
		
		c.gridx = 1;
		add(nodesLabel, c);
		
		c.gridx = 2;
		add(edgesLabel, c);
		
		c.gridx = 4;
		JLabel space = new JLabel();
		space.setPreferredSize(new Dimension(15, 5));
		space.setMinimumSize(new Dimension(15, 5));
		if (AttributeHelper.macOSrunning())
			add(space, c);
		
		nodesLabel.setVisible(false);
		edgesLabel.setVisible(false);
		
		updateGraphInfo();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Clears the current text of the status bar.
	 */
	public synchronized void clear() {
		statusLine.setText(" ");
		// setToolTipText(null);
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postEdgeAdded(GraphEvent)
	 */
	public void postEdgeAdded(GraphEvent e) {
		edges++;
		updateGraphInfo();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postEdgeRemoved(GraphEvent)
	 */
	public void postEdgeRemoved(GraphEvent e) {
		edges--;
		updateGraphInfo();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postGraphCleared(GraphEvent)
	 */
	public void postGraphCleared(GraphEvent e) {
		edges = 0;
		nodes = 0;
		activeSelection = null;
		updateGraphInfo();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postNodeAdded(GraphEvent)
	 */
	public void postNodeAdded(GraphEvent e) {
		nodes++;
		updateGraphInfo();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postNodeRemoved(GraphEvent)
	 */
	public void postNodeRemoved(GraphEvent e) {
		nodes--;
		updateGraphInfo();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preEdgeAdded(GraphEvent)
	 */
	public void preEdgeAdded(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preEdgeRemoved(GraphEvent)
	 */
	public void preEdgeRemoved(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preGraphCleared(GraphEvent)
	 */
	public void preGraphCleared(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preNodeAdded(GraphEvent)
	 */
	public void preNodeAdded(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preNodeRemoved(GraphEvent)
	 */
	public void preNodeRemoved(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.selection.SelectionListener#selectionChanged(SelectionEvent)
	 */
	public void selectionChanged(SelectionEvent e) {
		activeSelection = e.getSelection();
		updateGraphInfo();
	}
	
	/**
	 * @see org.graffiti.selection.SelectionListener#selectionListChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionListChanged(SelectionEvent e) {
		activeSelection = e.getSelection();
		updateGraphInfo();
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionChanged(Session)
	 */
	public void sessionChanged(Session session) {
		ListenerManager lm = null;
		
		if (currentSession != null) {
			// remove the status bar from the graph listener list of the
			// old session ...
			if (currentSession.getGraph() != null) {
				lm = currentSession.getGraph().getListenerManager();
				
				try {
					if (lm != null)
						lm.removeGraphListener(this);
				} catch (ListenerNotFoundException lnfe) {
					ErrorMsg.addErrorMessage(lnfe);
				}
			}
		}
		
		// remember the new session
		currentSession = session;
		
		if (session != null) {
			lm = session.getGraph().getListenerManager();
			
			// and add the status bar to the listener list of the new session.
			if (lm != null)
				lm.addDelayedGraphListener(this);
			if (session instanceof EditorSession)
				activeSelection = ((EditorSession) session).getSelectionModel().getActiveSelection();
			else
				activeSelection = null;
			nodes = currentSession.getGraph().getNumberOfNodes();
			edges = currentSession.getGraph().getNumberOfEdges();
			nodesLabel.setVisible(true);
			edgesLabel.setVisible(true);
		} else {
			nodesLabel.setVisible(false);
			edgesLabel.setVisible(false);
			activeSelection = null;
		}
		
		updateGraphInfo();
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(Session)
	 */
	public void sessionDataChanged(Session s) {
		updateGraphInfo();
	}
	
	/**
	 * Shows the given error message in the status bar for <tt>DELAY</tt> seconds.
	 * 
	 * @param status
	 *           the message to display in the status bar.
	 */
	public synchronized void showError(String status) {
		showError(status, DELAY);
	}
	
	/**
	 * Shows the given error message in the status bar for the given interval.
	 * 
	 * @param status
	 *           the message to display in the status bar.
	 * @param timeMillis
	 *           DOCUMENT ME!
	 */
	public synchronized void showError(final String val, int timeMillis) {
		final String status;
		if (val == null)
			status = "";
		else
			status = val;
		Timer timer = new Timer(0,
							new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (isShowing())
				{
					if (statusLine.getText() == null)
						statusLine.setText("");
					// FIXED, CK: This avoids flickering
					if (status == null || statusLine == null || statusLine.getText().equals(status))
						clear();
				}
			}
		});
		
		statusLine.setFont(BOLD_FONT);
		statusLine.setForeground(Color.red);
		statusLine.setText(status);
		timer.setInitialDelay(timeMillis);
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * Shows the given message in the status bar for <tt>DELAY</tt> seconds.
	 * 
	 * @param message
	 *           the message to display in the status bar.
	 */
	public synchronized void showInfo(String message) {
		showInfo(message, DELAY);
	}
	
	/**
	 * Shows the given message in the status bar for the given interval.
	 * 
	 * @param message
	 *           the message to display in the status bar.
	 * @param timeMillis
	 *           DOCUMENT ME!
	 */
	public synchronized void showInfo(final String val, int timeMillis) {
		final String message;
		if (val == null)
			message = "";
		else
			message = val;
		Timer timer = new Timer(timeMillis,
							new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (isShowing())
				{
					// FIXED, CK: This avoids flickering
					if (statusLine != null && statusLine.getText() == null)
						statusLine.setText("");
					if (statusLine != null && statusLine.getText() != null && message != null && statusLine.getText().equals(message))
						clear();
				}
			}
		});
		
		statusLine.setFont(PLAIN_FONT);
		statusLine.setForeground(Color.black);
		statusLine.setText(message);
		timer.setInitialDelay(timeMillis);
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * @see org.graffiti.event.TransactionListener#transactionFinished(TransactionEvent)
	 */
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		// ignoreUpdate--;
		ignoreUpdate = 0;
		
		if (currentSession != null) {
			nodes = currentSession.getGraph().getNumberOfNodes();
			edges = currentSession.getGraph().getNumberOfEdges();
			updateGraphInfo();
		}
	}
	
	/**
	 * @see org.graffiti.event.TransactionListener#transactionStarted(TransactionEvent)
	 */
	public void transactionStarted(TransactionEvent e) {
		ignoreUpdate++;
	}
	
	ThreadSafeOptions tso = new ThreadSafeOptions();
	
	/**
	 * Updates the graph information ui components.
	 */
	private void updateGraphInfo() {
		
		if (!SwingUtilities.isEventDispatchThread()) {
			if (!tso.getBval(0, false)) {
				tso.setBval(0, true);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						tso.setBval(0, false);
						updateGraphInfo();
					}
				});
			}
			return;
		}
		
		if (ignoreUpdate > 0) {
			// System.out.println("some transaction not yet finished");
			return;
		}
		
		boolean changed = false;
		ArrayList<Node> nl = new ArrayList<Node>();
		if (activeSelection != null)
			nl.addAll(activeSelection.getNodes());
		int nodeCnt1 = activeSelection != null ? activeSelection.getNumberOfNodes() : 0;
		if (activeSelection != null && currentSession != null && currentSession instanceof EditorSession)
			for (Node n : nl) {
				if (n.getGraph() == null || !currentSession.getGraph().containsNode(n)) {
					activeSelection.remove(n);
				}
			}
		int nodeCnt2 = activeSelection != null ? activeSelection.getNumberOfNodes() : 0;
		changed = nodeCnt1 != nodeCnt2;
		if (changed) {
			((EditorSession) currentSession).getSelectionModel().selectionChanged();
		}
		
		String selInfo1 = "";
		String selInfo2 = "";
		String selInfoE1 = "";
		String selInfoE2 = "";
		String br = "<br>";
		if (activeSelection != null) {
			if (activeSelection.getNodes().size() > 0) {
				selInfo1 = activeSelection.getNodes().size() + "/";
				selInfo2 = "<br>selected";
				br = " ";
				if (activeSelection.getNodes().size() == nodes)
					selInfo1 = "all ";
			}
			if (activeSelection.getEdges().size() > 0) {
				selInfoE1 = activeSelection.getEdges().size() + "/";
				selInfoE2 = "<br>selected";
				br = " ";
				if (activeSelection.getEdges().size() == edges)
					selInfoE1 = "all ";
			}
		}
		String nodeText = "";
		String edgeText = "";
		if (nodes == 1)
			nodeText = "<html>" + selInfo1 + nodes + "<br><small>node" + selInfo2;
		else
			if (nodes == 0)
				nodeText = "<html><small><br>no nodes";
			else
				nodeText = "<html>" + selInfo1 + nodes + "<small>" + br + "nodes" + selInfo2;
		
		if (edges == 1)
			edgeText = "<html>" + selInfoE1 + edges + "<small>" + br + "edge" + selInfoE2;
		else
			if (edges == 0)
				edgeText = "<html><small><br>no edges";
			else
				edgeText = "<html>" + selInfoE1 + edges + "<small>" + br + "edges" + selInfoE2;
		nodeText = nodeText.replaceAll("all 1<br>", "1 ");
		nodeText = nodeText.replaceAll("all 1<small>", "1");
		nodeText = nodeText.replaceAll(" ", "&nbsp;");
		edgeText = edgeText.replaceAll("all 1<", "1<");
		edgeText = edgeText.replaceAll("&nbsp;", "&nbsp;");
		nodesLabel.setText(nodeText);
		edgesLabel.setText(edgeText);
	}
	
	private void processRightClick(MouseEvent e, final boolean processNodesTrue_otherwiseEdges) {
		if (true) { // SwingUtilities.isRightMouseButton(e) || SwingUtilities.isLeftMouseButton(e)) {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem selAll = new JMenuItem("Select All");
			selAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (currentSession instanceof EditorSession) {
						Selection sel = new Selection("id");
						sel.addAll(((EditorSession) currentSession).getSelectionModel().getActiveSelection().getElements());
						if (processNodesTrue_otherwiseEdges)
							sel.addAll(currentSession.getGraph().getNodes());
						else
							sel.addAll(currentSession.getGraph().getEdges());
						((EditorSession) currentSession).getSelectionModel().setActiveSelection(sel);
						// ((EditorSession)currentSession).getSelectionModel().selectionChanged();
					}
				}
			});
			popup.add(selAll);
			
			JMenuItem selClear = new JMenuItem("Clear Selection");
			selClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Selection sel = new Selection("id");
					if (currentSession instanceof EditorSession) {
						if (processNodesTrue_otherwiseEdges)
							sel.addAll(((EditorSession) currentSession).getSelectionModel().getActiveSelection().getEdges());
						else
							sel.addAll(((EditorSession) currentSession).getSelectionModel().getActiveSelection().getNodes());
						((EditorSession) currentSession).getSelectionModel().setActiveSelection(sel);
					}
					
				}
			});
			popup.add(selClear);
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	public String getCurrentText() {
		String res = statusLine.getText();
		if (res != null)
			return res;
		else
			return "";
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
