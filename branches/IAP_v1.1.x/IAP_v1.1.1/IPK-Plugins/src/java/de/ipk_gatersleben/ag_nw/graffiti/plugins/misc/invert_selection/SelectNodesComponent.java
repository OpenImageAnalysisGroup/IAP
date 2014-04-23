/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.04.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.FolderPanel;
import org.StringManipulationTools;
import org.SystemInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.gui.ToolButton;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;

// import com.sun.java.swing.SwingUtilities2;

public class SelectNodesComponent extends JToolBar implements GraffitiComponent,
					SessionListener, ViewListener {
	
	private static final long serialVersionUID = 1L;
	private final ArrayList<Node> initSelection = new ArrayList<Node>();
	// private Selection selection;
	private final String prefComp;
	private static Session activeSession;
	
	private final JTextField searchText;
	private final JLabel searchDesc;
	private final JButton okButton;
	
	private static SelectNodesComponent instance;
	
	public static void focus(KeyEvent e) {
		if (instance != null) {
			instance.searchText.requestFocusInWindow();
			if (e.getKeyChar() > 'a') {
				instance.searchText.setText(e.getKeyChar() + "");
				for (KeyListener kl : instance.searchText.getKeyListeners())
					kl.keyTyped(e);
			}
		}
	}
	
	public SelectNodesComponent(String prefComp) {
		super("Select Nodes/Edges");
		instance = this;
		this.prefComp = prefComp;
		MainFrame.getInstance().addViewListener(this);
		
		searchDesc = new JLabel(FolderPanel.getSearchIcon());
		if (!SystemInfo.isMac())
			add(searchDesc);
		
		okButton = new JButton("OK");
		// int s = 0;
		// okButton.setMargin(new Insets(s, s, s, s));
		okButton.setRolloverEnabled(true);
		
		searchText = new JTextField();
		if (SystemInfo.isMac()) {
			searchText.putClientProperty("JTextField.variant", "search");
		}
		searchText.setBackground(new JPanel().getBackground());
		// final JTextField fSearchText = searchText;
		if (!SystemInfo.isMac())
			searchText.setBorder(BorderFactory.createEtchedBorder(Color.WHITE, Color.LIGHT_GRAY));
		searchText.setPreferredSize(new Dimension(120, okButton.getPreferredSize().height));
		searchText.setMaximumSize(new Dimension(120, okButton.getPreferredSize().height));
		add(searchText);
		// add(TableLayout.getSplitVertical(searchText, null, TableLayout.PREFERRED, 0));
		searchText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButton.doClick();
				okButton.requestFocusInWindow();
			}
		});
		searchText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (activeSession != null) {
					searchText.setBackground(Color.WHITE);
					initSelection.clear();
					initSelection.addAll(getActiveSelection());
					MainFrame.showMessage("<html>Search for Node/Edge labels, press <b>Enter</b> to activate selection, <b>ESC</b> to cancel operation. " +
										"Delete typed search text to select all nodes which have a label.", MessageType.PERMANENT_INFO);
				}
			}
			
			private Collection<Node> getActiveSelection() {
				if (activeSession != null) {
					Selection selection = ((EditorSession) activeSession).getSelectionModel().getActiveSelection();
					return selection.getNodes();
				} else
					return null;
			}
			
			public void focusLost(FocusEvent e) {
				if (activeSession != null) {
					if (e.getOppositeComponent() == okButton) {
						setSelection(initSelection, true);
						int sel1 = ((EditorSession) activeSession).getSelectionModel().getActiveSelection().getElements().size();
						int sel0 = initSelection.size();
						initSelection.clear();
						MainFrame.showMessage("Added " + (sel1 - sel0) + " elements to selection", MessageType.INFO);
					} else {
						setSelection(initSelection, false);
						initSelection.clear();
						MainFrame.showMessage("Selection restored", MessageType.INFO);
					}
					searchText.setText("");
					searchText.setBackground(null);
				}
			}
			
			private void setSelection(ArrayList<Node> tSelection, boolean addToSelection) {
				activeSession.getGraph().getListenerManager().transactionStarted(this);
				Selection selection = ((EditorSession) activeSession).getSelectionModel().getActiveSelection();
				Collection<Edge> edgeSel = selection.getEdges();
				Selection newSelection = new Selection("id");
				if (addToSelection)
					newSelection.addAll(selection.getElements());
				newSelection.addAll(edgeSel);
				newSelection.addAll(tSelection);
				((EditorSession) activeSession).getSelectionModel().setActiveSelection(newSelection);
				activeSession.getGraph().getListenerManager().transactionFinished(this);
			}
		});
		
		searchText.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if (activeSession != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// if (searchText.getText().length() > 0) {
							// activeSession.getGraph().getListenerManager().transactionStarted(this);
							// Selection selection = ((EditorSession)activeSession).getSelectionModel().getActiveSelection();
							Selection newSelection = new Selection("id", getFilteredGraphElementList(searchText.getText()));
							((EditorSession) activeSession).getSelectionModel().setActiveSelection(newSelection);
							// activeSession.getGraph().getListenerManager().transactionFinished(this);
							// }
						}
					});
				}
			}
			
			public void keyPressed(KeyEvent e) {
				// enter
				if (e.getKeyCode() == 10) {
					e.consume();
					okButton.requestFocus();
					for (FocusListener fl : searchText.getFocusListeners()) {
						fl.focusLost(new FocusEvent(searchText, 0, false, okButton));
					}
				}
				// esc
				if (e.getKeyCode() == 27) {
					e.consume();
					ToolButton.requestToolButtonFocus();
				}
				
			}
			
			public void keyReleased(KeyEvent e) {
			}
		});
		
		add(okButton);
		// setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		validate();
	}
	
	private Collection<GraphElement> getFilteredGraphElementList(String text) {
		text = text.toUpperCase();
		ArrayList<GraphElement> result = new ArrayList<GraphElement>();
		for (Node n : activeSession.getGraph().getNodes()) {
			String nodeLabel = AttributeHelper.getLabel(n, null);
			if (nodeLabel != null) {
				if (text.length() <= 0)
					result.add(n);
				else
					if (nodeLabel.indexOf(text) >= 0 ||
										StringManipulationTools.stringReplace(nodeLabel.toUpperCase(), "<BR>", "").indexOf(text) >= 0)
						result.add(n);
			}
		}
		for (Edge e : activeSession.getGraph().getEdges()) {
			String edgeLabel = AttributeHelper.getLabel(e, null);
			if (edgeLabel != null) {
				if (text.length() <= 0)
					result.add(e);
				else
					if (edgeLabel.indexOf(text) >= 0 ||
										StringManipulationTools.stringReplace(edgeLabel.toUpperCase(), "<BR>", "").indexOf(text) >= 0)
						result.add(e);
			}
		}
		return result;
	}
	
	// private String getDescLabelText() {
	// return "<html><small>Search:&nbsp;";
	// }
	
	// private Collection<Node> getFilteredSelection() {
	// Graph graph = activeSession.getGraph();
	// Selection selection = ((EditorSession)activeSession).getSelectionModel().getActiveSelection();
	// List<Node> nodes;
	// if (selection == null || selection.isEmpty()) {
	// nodes = graph.getNodes();
	// } else {
	// nodes = selection.getNodes();
	// }
	//
	// ((EditorSession)activeSession).getSelectionModel().setActiveSelection(selection);
	//
	// return nodes;
	// }
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionChanged(org.graffiti.session.Session)
	 */
	public void sessionChanged(Session s) {
		activeSession = s;
		if (s != null)
			viewChanged(s.getActiveView());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	public void sessionDataChanged(Session s) {
		if (s != null)
			viewChanged(s.getActiveView());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.gui.GraffitiComponent#getPreferredComponent()
	 */
	public String getPreferredComponent() {
		return prefComp;
	}
	
	public void viewChanged(View newView) {
		View view = newView;
		if (view == null || !(view.getClass() == IPKGraffitiView.class)) {
			searchText.setVisible(false);
			okButton.setVisible(false);
			searchDesc.setVisible(false);
		} else {
			searchText.setVisible(true);
			okButton.setVisible(true);
			searchDesc.setVisible(true);
		}
	}
}
