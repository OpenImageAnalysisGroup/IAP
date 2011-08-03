/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
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

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggGroupPartAttributeEditor
					extends AbstractValueEditComponent
					implements ActionListener {
	protected JComboBox keggIsPartSelection = new JComboBox();
	protected JButton selectOfThisType = new JButton("Select");
	
	protected final String isPartOfGroup = "<html>is part of a group<br>" +
						"<small><font color='gray'>(move element inside corresponding group element)";
	protected final String isNotPartOfGroup = "<html>is not part of a group";
	
	public KeggGroupPartAttributeEditor(final Displayable disp) {
		super(disp);
		String curVal = ((KeggGroupPartAttribute) getDisplayable()).getString();
		keggIsPartSelection.addItem(isPartOfGroup);
		keggIsPartSelection.addItem(isNotPartOfGroup);
		if (curVal != null && curVal.equalsIgnoreCase("yes"))
			keggIsPartSelection.setSelectedItem(isPartOfGroup);
		else
			keggIsPartSelection.setSelectedItem(isNotPartOfGroup);
		selectOfThisType.addActionListener(this);
		keggIsPartSelection.setOpaque(false);
		selectOfThisType.setOpaque(false);
		int defHeight = new JLabel(isPartOfGroup).getPreferredSize().height + 5;
		keggIsPartSelection.setPreferredSize(new Dimension(40, defHeight));
		keggIsPartSelection.setMinimumSize(new Dimension(40, defHeight));
	}
	
	public JComponent getComponent() {
		return TableLayout.getSplit(
							keggIsPartSelection,
							selectOfThisType,
							TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			selectOfThisType.setEnabled(false);
			keggIsPartSelection.addItem(EMPTY_STRING);
			keggIsPartSelection.setSelectedItem(EMPTY_STRING);
		} else {
			keggIsPartSelection.removeItem(EMPTY_STRING);
			selectOfThisType.setEnabled(true);
			String curVal = ((KeggGroupPartAttribute) getDisplayable()).getString();
			if (curVal != null && curVal.equalsIgnoreCase("yes"))
				keggIsPartSelection.setSelectedItem(isPartOfGroup);
			else
				keggIsPartSelection.setSelectedItem(isNotPartOfGroup);
		}
	}
	
	public void setValue() {
		if (!keggIsPartSelection.getSelectedItem().equals(EMPTY_STRING)) {
			Object o = keggIsPartSelection.getSelectedItem();
			if (o == isPartOfGroup)
				((KeggGroupPartAttribute) displayable).setString("yes");
			else
				((KeggGroupPartAttribute) displayable).setString("no");
		}
	}
	
	public void actionPerformed(ActionEvent arg0) {
		String currentSel = (String) keggIsPartSelection.getSelectedItem();
		boolean searchGroupParts = false;
		if (currentSel == isPartOfGroup)
			searchGroupParts = true;
		KeggGroupPartAttribute kta = (KeggGroupPartAttribute) displayable;
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
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			boolean partOfGroup = KeggGmlHelper.getIsPartOfGroup(n);
			if ((partOfGroup && searchGroupParts) ||
								(!partOfGroup && !searchGroupParts))
				nodes.add(n);
		}
		selection.addAll(nodes);
		
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(nodes.size() + " nodes match criteria", MessageType.INFO);
	}
}
