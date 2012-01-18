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
import org.Release;
import org.ReleaseInfo;
import org.graffiti.attributes.Attributable;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_editing.EntryCreator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.pathway_kegg_operation.PathwayKeggLoading;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute.LoadPathwayAttributeAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggIdAttributeEditor
					extends AbstractValueEditComponent
					implements ActionListener {
	String loadText = "Load";
	protected JLabel keggIdEditField = new JLabel("");
	protected JButton selectOfThisType = new JButton("Select");
	protected JButton loadMap = new JButton(loadText);
	protected JButton edit = new JButton("Edit");
	
	public KeggIdAttributeEditor(final Displayable disp) {
		super(disp);
		String curVal = ((KeggIdAttribute) getDisplayable()).getString();
		keggIdEditField.setText(curVal);
		selectOfThisType.addActionListener(this);
		selectOfThisType.setOpaque(false);
		loadMap.setOpaque(false);
		edit.setOpaque(false);
		if (curVal != null && curVal.startsWith("path:"))
			loadMap.setEnabled(true);
		else
			loadMap.setEnabled(false);
		loadMap.addActionListener(getActionListener());
		edit.addActionListener(getEditActionListener());
		if (((KeggIdAttribute) getDisplayable()).getAttributable() instanceof Graph)
			edit = null;
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			edit = null;
		updateMapButton();
	}
	
	private void updateMapButton() {
		Attributable aa = ((KeggIdAttribute) getDisplayable()).getAttributable();
		Node n = null;
		if (aa instanceof Node) {
			n = (Node) aa;
		}
		if (n == null)
			return;
		String curVal = KeggGmlHelper.getKeggGraphicsTitle(n);
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR &&
							curVal != null &&
							(curVal.startsWith("TITLE:") || curVal.startsWith("<html>TITLE:")))
			loadMap.setText("Collapse");
		else
			loadMap.setText(loadText);
	}
	
	public ActionListener getEditActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String curVal = ((KeggIdAttribute) getDisplayable()).getString();
				Attributable aa = ((KeggIdAttribute) getDisplayable()).getAttributable();
				if (aa instanceof Node) {
					Node n = (Node) aa;
					EntryCreator.processNewOrExistingNode(n, curVal);
				}
			}
		};
	}
	
	public ActionListener getActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String curVal = ((KeggIdAttribute) getDisplayable()).getString();
				Attributable aa = ((KeggIdAttribute) getDisplayable()).getAttributable();
				Graph g = null;
				Node n = null;
				if (aa instanceof Node) {
					n = (Node) aa;
					g = n.getGraph();
				}
				if (curVal.startsWith("path:")) {
					if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
						if (loadMap.getText().equalsIgnoreCase(loadText))
							PathwayKeggLoading.loadAndMergePathway(g, curVal, n, false);
						else
							PathwayKeggLoading.collapsePathway(g, curVal, n);
					} else
						LoadPathwayAttributeAction.loadMap(curVal, g, n, true);
				} else
					AttributeHelper.showInBrowser(curVal);
			}
		};
	}
	
	public JComponent getComponent() {
		String curVal = ((KeggIdAttribute) getDisplayable()).getString();
		if (curVal != null && curVal.startsWith("path:"))
			return TableLayout.get3Split(
								keggIdEditField,
								TableLayout.getSplit(loadMap, edit, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED),
								selectOfThisType,
								TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
		else
			return TableLayout.get3Split(
								keggIdEditField,
								edit, selectOfThisType,
								TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			if (selectOfThisType != null)
				selectOfThisType.setEnabled(false);
			if (loadMap != null)
				loadMap.setEnabled(false);
			if (edit != null)
				edit.setEnabled(false);
			keggIdEditField.setText(EMPTY_STRING);
		} else {
			if (selectOfThisType != null)
				selectOfThisType.setEnabled(true);
			if (loadMap != null)
				loadMap.setEnabled(true);
			if (edit != null)
				edit.setEnabled(true);
			String curVal = ((KeggIdAttribute) getDisplayable()).getString();
			keggIdEditField.setText(curVal);
		}
		updateMapButton();
	}
	
	public void setValue() {
		if (!keggIdEditField.getText().equals(EMPTY_STRING)) {
			((KeggIdAttribute) displayable).setString(keggIdEditField.getText());
		}
	}
	
	public void actionPerformed(ActionEvent arg0) {
		String currentIdString = keggIdEditField.getText();
		if (currentIdString == null)
			return;
		KeggIdAttribute kta = (KeggIdAttribute) displayable;
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
		if (a instanceof Graph)
			graph = (Graph) a;
		if (graph == null)
			return;
		EditorSession es = MainFrame.getInstance().getActiveEditorSession();
		if (es == null || es.getGraph() != graph)
			return;
		Selection selection = es.getSelectionModel().getActiveSelection();
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			String kegg_type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_name", null, new String(""), false);
			if (kegg_type != null && kegg_type.equals(currentIdString)) {
				nodes.add(n);
			}
		}
		selection.addAll(nodes);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(nodes.size() + " nodes added to selection", MessageType.INFO);
	}
}
