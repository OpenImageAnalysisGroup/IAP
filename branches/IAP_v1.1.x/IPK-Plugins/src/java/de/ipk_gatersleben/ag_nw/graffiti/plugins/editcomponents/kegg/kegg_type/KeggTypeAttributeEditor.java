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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggTypeAttributeEditor
					extends AbstractValueEditComponent
					implements ActionListener {
	// protected JComboBox keggTypeSelection = new JComboBox();
	protected JButton selectOfThisType = new JButton("Select");
	protected JLabel keggTypeLabel = new JLabel();
	protected boolean showButton = false;
	
	public KeggTypeAttributeEditor(final Displayable disp) {
		super(disp);
		String curVal = ((KeggTypeAttribute) getDisplayable()).getString();
		showButton = disp.getName().equals("kegg_type");
		// for (EntryType et : EntryType.values()) {
		// keggTypeSelection.addItem(et.getDescription());
		// }
		// keggTypeSelection.setSelectedItem(curVal);
		keggTypeLabel.setText(curVal);
		selectOfThisType.addActionListener(this);
		selectOfThisType.setOpaque(false);
		// keggTypeSelection.setOpaque(false);
	}
	
	public JComponent getComponent() {
		if (showButton)
			return TableLayout.getSplit(
								// keggTypeSelection,
					keggTypeLabel,
								selectOfThisType,
								TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
		else
			return keggTypeLabel;
		// return keggTypeSelection; /*
		/*
		 * return TableLayout.getSplit(
		 * keggTypeSelection,
		 * new JLabel(),
		 * TableLayout.FILL, 0);
		 */
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			selectOfThisType.setEnabled(false);
			// keggTypeSelection.addItem(EMPTY_STRING);
			// keggTypeSelection.setSelectedItem(EMPTY_STRING);
			keggTypeLabel.setText(EMPTY_STRING);
		} else {
			// keggTypeSelection.removeItem(EMPTY_STRING);
			selectOfThisType.setEnabled(true);
			String curVal = ((KeggTypeAttribute) getDisplayable()).getString();
			// keggTypeSelection.setSelectedItem(curVal);
			keggTypeLabel.setText(curVal);
		}
	}
	
	public void setValue() {
		// if (!keggTypeSelection.getSelectedItem().equals(EMPTY_STRING))
		// ((KeggTypeAttribute)displayable).setString(keggTypeSelection.getSelectedItem().toString());
	}
	
	public void actionPerformed(ActionEvent arg0) {
		// String currentEntryTypeString = (String) keggTypeSelection.getSelectedItem();
		String currentEntryTypeString = keggTypeLabel.getText();
		if (currentEntryTypeString == null)
			return;
		EntryType et = EntryType.getEntryType(currentEntryTypeString);
		if (et == null)
			return;
		KeggTypeAttribute kta = (KeggTypeAttribute) displayable;
		Attributable a = kta.getAttributable();
		if (a == null && kta.getParent() != null) {
			a = kta.getParent().getAttributable();
		}
		if (a == null && kta.getParent() != null && kta.getParent().getParent() != null) {
			a = kta.getParent().getParent().getAttributable();
		}
		if (a == null)
			return;
		Graph graph = ((Node) a).getGraph();
		EditorSession es = MainFrame.getInstance().getActiveEditorSession();
		if (es == null || es.getGraph() != graph)
			return;
		Selection selection = es.getSelectionModel().getActiveSelection();
		ArrayList<Node> enzymes = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			String kegg_type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", null, new String(""), false);
			if (kegg_type != null && kegg_type.equals(et.getDescription())) {
				enzymes.add(n);
			}
		}
		selection.addAll(enzymes);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(enzymes.size() + " nodes added to selection", MessageType.INFO);
	}
}
