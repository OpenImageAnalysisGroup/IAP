/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JLabel;

import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_editing.EntryCreator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;

public class EntryLabel extends JLabel {
	private static final long serialVersionUID = 1L;
	private Entry entry;
	
	public EntryLabel(
						final Entry entry,
						final HashMap<Entry, Node> entry2graphNode,
						final Pathway p,
						final Collection<Gml2PathwayWarningInformation> warnings,
						final Collection<Gml2PathwayErrorInformation> errors,
						final Graph graph) {
		super(entry.getVisibleName());
		this.entry = entry;
		setBackground(null);
		setOpaque(false);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setToolTipText("<html>" +
							"<b>Click to select</b> related graph-node<br>" +
							"<b>Double-click to modify</b>/delete this entry<br><br>" +
							"Double-click graph nodes to edit entries, or select graph-<br>" +
							"nodes and use the Node side panel to edit entry-properties!");
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				HashSet<Node> nodes = new HashSet<Node>();
				if (entry2graphNode.containsKey(entry))
					nodes.add(entry2graphNode.get(entry));
				
				if (nodes.size() > 0) {
					Graph graph = nodes.iterator().next().getGraph();
					EditorSession es = MainFrame.getInstance().getActiveEditorSession();
					if (es == null || es.getGraph() != graph)
						return;
					Selection selection = es.getSelectionModel().getActiveSelection();
					selection.clear();
					selection.addAll(nodes);
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
					MainFrame.showMessage(nodes.size() + " node selected", MessageType.INFO);
					if (e.getClickCount() > 1) {
						EntryCreator.processNewOrExistingNode(nodes.iterator().next(), entry.getName().getId());
						ArrayList<Entry> validEntries = new ArrayList<Entry>();
						validEntries.add(entry);
						EntryCreator.processEntryEditOperation(
											nodes.iterator().next(),
											entry.getType() == EntryType.map,
											graph, warnings, errors, entry2graphNode, p, validEntries);
					}
				}
			}
			
			public void mouseEntered(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseReleased(MouseEvent e) {
			}
		});
	}
	
	public Entry getEntry() {
		return entry;
	}
}
