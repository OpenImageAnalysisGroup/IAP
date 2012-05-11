/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 11.02.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.selection.Selection;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class DebugSelectionCheck implements ActionListener {
	
	private static ThreadSafeOptions tso = null;
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		System.out.print(".");
		if (tso != null) {
			try {
				Selection s = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
				if (s.getNodes().size() == 1) {
					Node n = s.getNodes().iterator().next();
					NodeCacheEntry nce = (NodeCacheEntry) tso.nodeSearch.get(n);
					if (n == null || nce == null)
						System.err.println("\nUNKNOWN!");
					else {
						String con = (nce.connectedNodes != null) ?
											new Integer(nce.connectedNodes.size()).toString() :
											"null";
						System.err.println("Selection (Node ID=" + n.getID() + "/" + nce.node.getID() + "):\n" +
											"  POS:" + nce.position.x + "/" + nce.position.y + "\n" +
											"  NODE-REF==NODE?:" + (nce.node == n) + "\n" +
											"  CONNECTIONS TO OTHER NODES: " + con);
					}
				}
			} catch (NullPointerException npe) {
				// empty
			}
		}
	}
	
	public static void setCheckThis(ThreadSafeOptions options) {
		tso = options;
	}
	
}
