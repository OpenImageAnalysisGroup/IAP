/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.KeggReactionIdAttributeEditor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;

public class ReactionLabel extends JLabel {
	private Reaction reaction;
	
	public ReactionLabel(
						final Reaction r,
						final HashMap<Entry, Node> entry2graphNode,
						final Pathway p,
						final Collection<Gml2PathwayWarningInformation> warnings,
						final Collection<Gml2PathwayErrorInformation> errors,
						final Graph graph) {
		super(r.toStringWithDetails(true, true));
		this.reaction = r;
		setBackground(null);
		setOpaque(false);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setToolTipText("<html>" +
							"<b>Click to select</b> related graph-elements<br>" +
							"<b>Double-click to modify</b>/delete this reaction<br><br>" +
							"Double-click graph edges to edit reactions");
		
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				HashSet<Node> nodes = new HashSet<Node>();
				for (Entry s : r.getSubstrates())
					nodes.add(entry2graphNode.get(s));
				for (Entry enz : r.getEntriesRepresentingThisReaction(p.getEntries()))
					nodes.add(entry2graphNode.get(enz));
				for (Entry p : r.getProducts())
					nodes.add(entry2graphNode.get(p));
				
				if (nodes.size() > 0) {
					Graph graph = nodes.iterator().next().getGraph();
					EditorSession es = MainFrame.getInstance().getActiveEditorSession();
					if (es == null || es.getGraph() != graph)
						return;
					Selection selection = es.getSelectionModel().getActiveSelection();
					selection.clear();
					selection.addAll(nodes);
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
					MainFrame.showMessage(nodes.size() + " nodes selected", MessageType.INFO);
				}
				if (e.getClickCount() > 1) {
					KeggReactionIdAttributeEditor.editReactions(r, p, warnings, errors, entry2graphNode, graph);
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
	
	public Reaction getReaction() {
		return reaction;
	}
	
	private static final long serialVersionUID = 1L;
}
