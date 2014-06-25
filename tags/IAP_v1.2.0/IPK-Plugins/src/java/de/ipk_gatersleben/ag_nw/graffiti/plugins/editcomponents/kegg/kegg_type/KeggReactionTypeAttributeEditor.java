/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.graffiti.attributes.Attributable;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.IndexAndString;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.ReactionType;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggReactionTypeAttributeEditor
					extends AbstractValueEditComponent
					implements ActionListener {
	// protected JComboBox keggReactionTypeSelection = new JComboBox();
	protected JLabel reactionTypeDisplay = new JLabel();
	protected JButton selectOfThisType = new JButton("Select");
	
	public KeggReactionTypeAttributeEditor(final Displayable disp) {
		super(disp);
		String curVal = ((KeggReactionTypeAttribute) getDisplayable()).getString();
		// for (ReactionType rt : ReactionType.values()) {
		// keggReactionTypeSelection.addItem(rt.toString());
		// }
		// keggReactionTypeSelection.setSelectedItem(curVal);
		// keggReactionTypeSelection.setEnabled(false);
		selectOfThisType.addActionListener(this);
		// keggReactionTypeSelection.setOpaque(false);
		reactionTypeDisplay.setText(curVal);
		reactionTypeDisplay.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		selectOfThisType.setOpaque(false);
	}
	
	public JComponent getComponent() {
		return TableLayout.getSplit(
							// keggReactionTypeSelection,
				reactionTypeDisplay,
							selectOfThisType,
							TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			selectOfThisType.setEnabled(false);
			// keggReactionTypeSelection.addItem(EMPTY_STRING);
			// keggReactionTypeSelection.setSelectedItem(EMPTY_STRING);
			reactionTypeDisplay.setText(EMPTY_STRING);
		} else {
			// keggReactionTypeSelection.removeItem(EMPTY_STRING);
			selectOfThisType.setEnabled(true);
			String curVal = ((KeggReactionTypeAttribute) getDisplayable()).getString();
			// keggReactionTypeSelection.setSelectedItem(curVal);
			reactionTypeDisplay.setText(curVal);
		}
	}
	
	public void setValue() {
		// if (!keggReactionTypeSelection.getSelectedItem().equals(EMPTY_STRING))
		// ((KeggReactionTypeAttribute)displayable).setString(keggReactionTypeSelection.getSelectedItem().toString());
	}
	
	public void actionPerformed(ActionEvent arg0) {
		// String currentReactionTypeString = (String) keggReactionTypeSelection.getSelectedItem();
		String currentReactionTypeString = reactionTypeDisplay.getText();
		if (currentReactionTypeString == null)
			return;
		ReactionType rt = ReactionType.getReactiontype(currentReactionTypeString);
		if (rt == null)
			return;
		KeggReactionTypeAttribute kta = (KeggReactionTypeAttribute) displayable;
		Attributable a = kta.getAttributable();
		if (a == null && kta.getParent() != null) {
			a = kta.getParent().getAttributable();
		}
		if (a == null && kta.getParent() != null && kta.getParent().getParent() != null) {
			a = kta.getParent().getParent().getAttributable();
		}
		if (a == null)
			return;
		Graph graph = null;
		if (a instanceof Node)
			graph = ((Node) a).getGraph();
		if (a instanceof Edge)
			graph = ((Edge) a).getGraph();
		if (graph == null)
			return;
		EditorSession es = MainFrame.getInstance().getActiveEditorSession();
		if (es == null || es.getGraph() != graph)
			return;
		Selection selection = es.getSelectionModel().getActiveSelection();
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (Edge e : graph.getEdges()) {
			ArrayList<IndexAndString> reacTypeInfos = KeggGmlHelper.getKeggReactionTypes(e);
			boolean match = false;
			for (IndexAndString ias : reacTypeInfos) {
				String reaction_type = ias.getValue();
				if (reaction_type.equals(currentReactionTypeString)) {
					match = true;
					break;
				}
			}
			if (match) {
				edges.add(e);
			}
		}
		selection.addAll(edges);
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			ArrayList<IndexAndString> reacTypeInfos = KeggGmlHelper.getKeggReactionTypes(n);
			boolean match = false;
			for (IndexAndString ias : reacTypeInfos) {
				String reaction_type = ias.getValue();
				if (reaction_type.equals(currentReactionTypeString)) {
					match = true;
					break;
				}
			}
			if (match) {
				nodes.add(n);
			}
		}
		selection.addAll(nodes);
		
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(nodes.size() + " nodes and " + edges.size() + " edges added to selection", MessageType.INFO);
	}
}
