/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: HierarchyAlgorithm.java,v 1.1 2011-01-31 08:59:49 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.hierarchy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid.GridLayouterAlgorithm;

/**
 * @author Christian Klukas
 */
public class HierarchyAlgorithm
					extends AbstractAlgorithm
					implements ActionListener, ProvidesNodeContextMenu {
	
	JMenu myMenu;
	JMenuItem m1showLeafNodes;
	
	/**
	 * Constructs a new instance.
	 */
	public HierarchyAlgorithm() {
		myMenu = new JMenu("Hierarchy"); // /Bends
		m1showLeafNodes = new JMenuItem("Show leaf nodes in new window");
		m1showLeafNodes.addActionListener(this);
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		GravistoService.getInstance().algorithmAttachData(this);
		Collection<Node> leafNodes = GraphHelper.getLeafNodes(selection.getNodes());
		for (Node n : leafNodes)
			AttributeHelper.setAttribute(n, "", "leaf", true);
		Graph g = new AdjListGraph(graph, new ListenerManager());
		for (Node n : leafNodes)
			AttributeHelper.deleteAttribute(n, "", "leaf");
		Set<GraphElement> del = new HashSet<GraphElement>();
		for (Node n : g.getNodes()) {
			if (AttributeHelper.hasAttribute(n, "", "leaf"))
				AttributeHelper.deleteAttribute(n, "", "leaf");
			else
				del.add(n);
		}
		g.deleteAll(del);
		int maxWidth = 20;
		int maxHeight = 20;
		for (Node n : g.getNodes()) {
			AttributeHelper.setHidden(false, n);
			Vector2d size = AttributeHelper.getSize(n);
			if (size.x > maxWidth)
				maxWidth = (int) size.x;
			if (size.y > maxHeight)
				maxHeight = (int) size.y;
		}
		GridLayouterAlgorithm.layoutOnGrid(g.getNodes(), 1d, maxWidth + 10, maxHeight + 10);
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		for (Node n : g.getNodes()) {
			Vector2d pos = AttributeHelper.getPositionVec2d(n);
			if (pos.x < minX)
				minX = (int) pos.x;
			if (pos.y < minY)
				minY = (int) pos.y;
		}
		
		int offX = 50 + maxWidth / 2;
		int offY = 50 + maxHeight / 2;
		for (Node n : g.getNodes()) {
			Vector2d pos = AttributeHelper.getPositionVec2d(n);
			AttributeHelper.setPosition(n, pos.x - minX + offX, pos.y - minY + offY);
		}
		g.setName("Leaf nodes");
		g.setModified(false);
		if (g.getNumberOfNodes() > 0)
			MainFrame.getInstance().showGraph(g, null);
		else
			MainFrame.showMessageDialog("No leaf nodes could be found for display!", "Result");
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		graph = null;
		selection = null;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return null; // "Show leaf nodes in new window";
	}
	
	@Override
	public String getCategory() {
		return null; // "Hierarchy";
	}
	
	/**
	 * Sets the selection on which the algorithm works.
	 * 
	 * @param selection
	 *           the selection
	 */
	public void setSelection(Selection selection) {
		this.selection = selection;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m1showLeafNodes) {
			execute();
		}
	}
	
	public JMenuItem[] getCurrentNodeContextMenuItem(
						Collection<Node> selectedNodes) {
		EditorSession session =
							GravistoService
												.getInstance()
												.getMainFrame()
												.getActiveEditorSession();
		if (session != null)
			selection = session.getSelectionModel().getActiveSelection();
		else
			selection = null;
		if (selection == null || selection.isEmpty())
			return null;
		if (selection.getNodes().size() < 1)
			return null;
		return new JMenuItem[] { m1showLeafNodes };
	}
}
