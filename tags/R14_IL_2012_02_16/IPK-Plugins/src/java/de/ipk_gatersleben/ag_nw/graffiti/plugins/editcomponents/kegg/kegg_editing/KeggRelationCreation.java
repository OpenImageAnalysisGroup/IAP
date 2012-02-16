/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_editing;

import java.util.Collection;

import javax.swing.JMenuItem;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.ProvidesEdgeContextMenu;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.KeggReactionIdAttributeEditor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.KeggRelationSrcTgtAttributeEditor;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;

public class KeggRelationCreation extends AbstractAlgorithm
					implements ProvidesEdgeContextMenu {
	
	public JMenuItem[] getCurrentEdgeContextMenuItem(Collection<Edge> selectedEdges) {
		// if (selectedEdges.size()==1) {
		// JMenuItem addRelation = new JMenuItem("Add Relation");
		// ArrayList<JMenuItem> result = new ArrayList<JMenuItem>();
		// /*�
		// HashSet<String> types = new HashSet<String>();
		// for (Edge e : selectedEdges) {
		// for (IndexAndString ias : KeggGmlHelper.getRelationTypes(e)) {
		// types.add(ias.getValue());
		// }
		// }
		// for (String relationType : types) {
		// JMenuItem delRel = new JMenuItem("Delete Relation(s) of Type "+relationType);
		// result.add(delRel);
		// }*/
		// result.add(addRelation);
		// return result.toArray(new JMenuItem[] {});
		// }
		return null;
	}
	
	public void execute() {
		// empty
	}
	
	public String getName() {
		return null;
	}
	
	public static void processNewOrExistingEdge(Edge edge) {
		if (edge == null)
			return;
		
		int res = GUIhelper.showMessageDialog(
							"<html>" +
												"A graph edge may either represent one or more relations<br>" +
												"or may illustrate reactions products and substrates, or both.<br>" +
												"", "Edit Reactions or Relations",
							new String[] {
												"Edit Relation(s)",
												"Edit Reaction(s)",
												"Cancel" });
		EditorSession es = MainFrame.getInstance().getActiveEditorSession();
		if (es == null || es.getGraph() != edge.getGraph())
			return;
		Selection selection = es.getSelectionModel().getActiveSelection();
		if (res == 0) {
			KeggRelationSrcTgtAttributeEditor.
								editRelations(edge, null, edge.getGraph());
		}
		if (res == 1) {
			KeggReactionIdAttributeEditor.editReactions(edge, null, edge.getGraph(), selection);
		}
	}
	
}
