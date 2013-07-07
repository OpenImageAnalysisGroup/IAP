/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: SelectLeafNodesAlgorithm.java,v 1.1 2011-01-31 08:59:49 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.hierarchy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JMenuItem;

import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author Christian Klukas
 */
@SuppressWarnings({"rawtypes"})
public class SelectLeafNodesAlgorithm
					extends AbstractAlgorithm
					implements ActionListener, ProvidesNodeContextMenu {
	
	JMenuItem m1selectLeafNodes;
	
	/**
	 * Constructs a new instance.
	 */
	public SelectLeafNodesAlgorithm() {
		m1selectLeafNodes = new JMenuItem("Select leaf nodes");
		m1selectLeafNodes.addActionListener(this);
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	@SuppressWarnings("unchecked")
	public void execute() {
		GravistoService.getInstance().algorithmAttachData(this);
		Collection<Node> leafNodes = GraphHelper.getLeafNodes(selection.getNodes());
		if (leafNodes.size() > 0) {
			GraphHelper.clearSelection();
			GraphHelper.selectGraphElements((Collection) leafNodes);
			MainFrame.showMessage(leafNodes.size() + " leaf nodes selected!", MessageType.INFO);
		} else
			MainFrame.showMessage("No leaf nodes could be found!", MessageType.INFO);
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
		if (e.getSource() == m1selectLeafNodes) {
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
		if (session == null)
			return null;
		selection = session.getSelectionModel().getActiveSelection();
		if (selection.isEmpty())
			return null;
		if (selection.getNodes().size() < 1)
			return null;
		return new JMenuItem[] { m1selectLeafNodes };
	}
}
