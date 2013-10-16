/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugins.inspectors.defaults.AbstractTab;
import org.graffiti.plugins.inspectors.defaults.EdgeTab;
import org.graffiti.plugins.inspectors.defaults.GraphTab;
import org.graffiti.plugins.inspectors.defaults.NodeTab;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

public class GraphElementSelectionLabel extends JLabel {
	private Graph g = null;
	private Node n = null;
	private Edge e = null;
	private Collection<Attributable> errorSources = null;
	
	public GraphElementSelectionLabel(Attributable causingGraphElement) {
		if (causingGraphElement instanceof Graph) {
			g = (Graph) causingGraphElement;
			setText("Graph: " + g.getName());
		} else
			if (causingGraphElement instanceof Node) {
				n = (Node) causingGraphElement;
				String lbl = AttributeHelper.getLabel(n, "(unnamed node (" + n.getID() + ")");
				String keggId = KeggGmlHelper.getKeggId(n);
				setText("Node: " + lbl + " KEGG ID=" + keggId);
			} else
				if (causingGraphElement instanceof Edge) {
					e = (Edge) causingGraphElement;
					String lblNs = AttributeHelper.getLabel(e.getSource(), "(unnamed node (" + n.getID() + ")");
					String lblNt = AttributeHelper.getLabel(e.getTarget(), "(unnamed node (" + n.getID() + ")");
					String lbl = AttributeHelper.getLabel(e, "");
					setText("Edge: " + lblNs + " --> " + lblNt + (lbl.length() > 0 ? " (" + lbl + ")" : ""));
				}
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setForeground(Color.BLUE);
		setToolTipText("Click here, to select graph-element");
		addMouseListener();
	}
	
	public GraphElementSelectionLabel(Collection<Attributable> causingGraphElements) {
		errorSources = causingGraphElements;
		String lbl = "<html><table border='0'>";
		String pre = "<tr><td>";
		String in = "</td><td>";
		String post = "</td></tr>";
		for (Attributable causingGraphElement : causingGraphElements) {
			if (causingGraphElement instanceof Graph) {
				g = (Graph) causingGraphElement;
				lbl = lbl + pre + "Graph" + in + g.getName() + post;
			} else
				if (causingGraphElement instanceof Node) {
					n = (Node) causingGraphElement;
					String nlbl = AttributeHelper.getLabel(n, "(unnamed node (" + n.getID() + ")");
					String keggId = KeggGmlHelper.getKeggId(n);
					lbl = lbl + pre + "Node" + in + nlbl + post + " KEGG ID=" + keggId;
				} else
					if (causingGraphElement instanceof Edge) {
						e = (Edge) causingGraphElement;
						String lblNs = AttributeHelper.getLabel(e.getSource(), "(unlabeled node (" + n.getID() + ")");
						String lblNt = AttributeHelper.getLabel(e.getTarget(), "(unlabeled node (" + n.getID() + ")");
						String elbl = AttributeHelper.getLabel(e, "");
						lbl = lbl + pre + "Edge" + in + lblNs + " --> " + lblNt + (lbl.length() > 0 ? " (" + elbl + ")" : "") + pre;
					}
		}
		setText(lbl);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setForeground(Color.BLUE);
		setToolTipText("Click here, to select graph-elements");
		addMouseListener();
	}
	
	private void addMouseListener() {
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				boolean selG, selN, selE;
				selG = false;
				selN = false;
				selE = false;
				GraphHelper.clearSelection();
				if (errorSources != null) {
					GraphHelper.selectElements(errorSources);
					for (Attributable g : errorSources) {
						if (g instanceof Node)
							selN = true;
						if (g instanceof Edge)
							selE = true;
						if (g instanceof Graph)
							selG = true;
					}
				}
				if (n != null) {
					GraphHelper.selectGraphElement(n);
					selN = true;
				}
				if (e != null) {
					GraphHelper.selectGraphElement(e);
					selE = true;
				}
				if (g != null) {
					selG = true;
				}
				final boolean sN = selN;
				final boolean sE = selE;
				final boolean sG = selG;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						NodeTab nt = NodeTab.getInstance();
						EdgeTab et = EdgeTab.getInstance();
						GraphTab gt = GraphTab.getInstance();
						if (gt != null)
							processTab(gt, sG, sG);
						if (et != null)
							processTab(et, sE, sE);
						if (nt != null)
							processTab(nt, sN, sN);
					}
				});
			}
			
			public void mouseEntered(MouseEvent arg0) {
			}
			
			public void mouseExited(MouseEvent arg0) {
			}
			
			public void mousePressed(MouseEvent arg0) {
			}
			
			public void mouseReleased(MouseEvent arg0) {
			}
		});
	}
	
	private void processTab(AbstractTab nt, boolean mark, boolean showInFront) {
		nt.getTitle();
		int index = ((JTabbedPane) nt.getParent()).indexOfComponent(nt);
		if (index >= 0) {
			// if (mark)
			// ((JTabbedPane)nt.getParent()).setTitleAt(index, "<html><font color='red'>"+ot);
			// else
			// ((JTabbedPane)nt.getParent()).setTitleAt(index, "<html><font color='black'>"+ot);
			if (showInFront)
				((JTabbedPane) nt.getParent()).setSelectedIndex(index);
		}
	}
	
	private static final long serialVersionUID = 1L;
	
}
