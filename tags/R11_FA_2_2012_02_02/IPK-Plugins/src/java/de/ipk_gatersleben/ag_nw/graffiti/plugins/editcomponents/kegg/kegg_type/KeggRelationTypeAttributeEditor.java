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
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.IndexAndString;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.RelationType;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggRelationTypeAttributeEditor
					extends AbstractValueEditComponent
					implements ActionListener {
	protected JLabel keggRelationTypeSelection = new JLabel();
	protected JButton selectOfThisType = new JButton("Select");
	
	public KeggRelationTypeAttributeEditor(final Displayable disp) {
		super(disp);
		String curVal = ((KeggRelationTypeAttribute) getDisplayable()).getString();
		keggRelationTypeSelection.setOpaque(false);
		selectOfThisType.setOpaque(false);
		keggRelationTypeSelection.setText(curVal);
		keggRelationTypeSelection.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		selectOfThisType.addActionListener(this);
	}
	
	public JComponent getComponent() {
		return TableLayout.getSplit(
							keggRelationTypeSelection,
							selectOfThisType,
							TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			selectOfThisType.setEnabled(false);
			keggRelationTypeSelection.setText(EMPTY_STRING);
		} else {
			selectOfThisType.setEnabled(true);
			String curVal = ((KeggRelationTypeAttribute) getDisplayable()).getString();
			keggRelationTypeSelection.setText(curVal);
		}
	}
	
	public void setValue() {
		if (!keggRelationTypeSelection.getText().equals(EMPTY_STRING))
			((KeggRelationTypeAttribute) displayable).setString(keggRelationTypeSelection.getText());
	}
	
	public void actionPerformed(ActionEvent arg0) {
		String currentRelationTypeString = keggRelationTypeSelection.getText();
		if (currentRelationTypeString == null)
			return;
		RelationType rt = RelationType.getRelationType(currentRelationTypeString);
		if (rt == null)
			return;
		KeggRelationTypeAttribute kta = (KeggRelationTypeAttribute) displayable;
		Attributable a = kta.getAttributable();
		if (a == null && kta.getParent() != null) {
			a = kta.getParent().getAttributable();
		}
		if (a == null && kta.getParent() != null && kta.getParent().getParent() != null) {
			a = kta.getParent().getParent().getAttributable();
		}
		if (a == null)
			return;
		Graph graph = ((Edge) a).getGraph();
		EditorSession es = MainFrame.getInstance().getActiveEditorSession();
		if (es == null || es.getGraph() != graph)
			return;
		Selection selection = es.getSelectionModel().getActiveSelection();
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (Edge e : graph.getEdges()) {
			ArrayList<IndexAndString> relTypeInfos = KeggGmlHelper.getRelationTypes(e);
			boolean match = false;
			for (IndexAndString ias : relTypeInfos) {
				String relation_type = ias.getValue();
				if (relation_type.equals(currentRelationTypeString)) {
					match = true;
					break;
				}
			}
			if (match) {
				edges.add(e);
			}
		}
		selection.addAll(edges);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(edges.size() + " edges added to selection", MessageType.INFO);
	}
}
