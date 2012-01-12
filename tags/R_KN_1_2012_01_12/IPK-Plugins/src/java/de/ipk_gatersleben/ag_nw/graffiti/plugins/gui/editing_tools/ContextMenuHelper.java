/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.HelperClass;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class ContextMenuHelper implements HelperClass {
	public static void createAndShowContextMenuForAlgorithm(ProvidesNodeContextMenu a) {
		Collection<Node> nodes = getActiveNodeSelection();
		if (a instanceof Algorithm) {
			GravistoService.getInstance().algorithmAttachData((Algorithm) a);
		}
		JMenuItem[] cmd = a.getCurrentNodeContextMenuItem(nodes);
		if (cmd == null) {
			MainFrame.showMessage("<html><b>Algorithm can't work with current node selection!", MessageType.INFO);
			return;
		}
		JPopupMenu jp = new JPopupMenu("Test");
		for (int i = 0; i < cmd.length; i++)
			jp.add(cmd[i]);
		View v = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getActiveView();
		
		jp.show(v.getViewComponent(), 0, 0);
	}
	
	/**
	 * @return All nodes of current view, if no node is selected. If nodes are selected,
	 *         a List with that Nodes is returned.
	 */
	public static Collection<Node> getActiveNodeSelection() {
		EditorSession session =
							GravistoService
												.getInstance()
												.getMainFrame()
												.getActiveEditorSession();
		if (session == null) {
			MainFrame.showMessageDialog("No graph loaded or active!", "Error");
			return null;
		} else {
			Selection selection = session.getSelectionModel().getActiveSelection();
			return GraphHelper.getSelectedOrAllNodes(selection, session.getGraph());
		}
	}
	
	public static Collection<GraphElement> getActiveSelection() {
		EditorSession session =
							GravistoService
												.getInstance()
												.getMainFrame()
												.getActiveEditorSession();
		if (session == null) {
			MainFrame.showMessageDialog("No graph loaded or active!", "Error");
			return null;
		} else {
			Selection selection = session.getSelectionModel().getActiveSelection();
			return GraphHelper.getSelectedOrAllGraphElements(selection, session.getGraph());
		}
	}
}