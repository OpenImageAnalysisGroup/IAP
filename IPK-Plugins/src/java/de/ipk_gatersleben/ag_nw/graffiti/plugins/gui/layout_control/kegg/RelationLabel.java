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

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.KeggRelationSrcTgtAttributeEditor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;

public class RelationLabel extends JLabel {
	private static final long serialVersionUID = 1L;
	private Relation relation;
	
	public RelationLabel(
						final Relation r,
						final HashMap<Entry, Node> entry2graphNode,
						final Pathway pathway,
						final Collection<Gml2PathwayWarningInformation> warnings,
						final Collection<Gml2PathwayErrorInformation> errors,
						final Graph graph) {
		super(r.toStringWithKeggNames());
		this.relation = r;
		setBackground(null);
		setOpaque(false);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setToolTipText("<html>" +
							"<b>Click to select</b> related graph-elements<br>" +
							"<b>Double-click to modify</b>/delete this relation<br><br>" +
							"Double-click graph edges to edit relations");
		
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				HashSet<Node> nodes = new HashSet<Node>();
				Node nS = entry2graphNode.get(r.getSourceEntry());
				if (nS != null)
					nodes.add(nS);
				Node nT = entry2graphNode.get(r.getTargetEntry());
				if (nT != null)
					nodes.add(nT);
				for (IdRef ref : r.getSubtypeRefs()) {
					if (ref.getRef() != null) {
						Node nST = entry2graphNode.get(ref.getRef());
						if (nST != null)
							nodes.add(nST);
					}
				}
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
				if (e.getClickCount() > 1)
					KeggRelationSrcTgtAttributeEditor.editRelations(r, pathway, warnings, errors, entry2graphNode, graph);
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
	
	public Relation getRelation() {
		return relation;
	}
}
