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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.attributes.Attributable;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.jdom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_editing.EntryCreator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui.MyRelationList;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui.RelationTypeEditor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui.SrcTargetEditor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui.SubComponentTypesEditor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui.SubtypeCompoundEditor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.IndexAndString;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Subtype;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK-Gatersleben
 */
public class KeggRelationSrcTgtAttributeEditor
					extends AbstractValueEditComponent
					implements ActionListener {
	protected JLabel keggRelationSrcTgtEditor = new JLabel();
	protected JButton selectOfThisType = new JButton("Select");
	protected JButton editThisRelation = new JButton("Edit");
	
	public KeggRelationSrcTgtAttributeEditor(final Displayable disp) {
		super(disp);
		String curVal = ((KeggRelationSrcTgtAttribute) getDisplayable()).getString();
		keggRelationSrcTgtEditor.setText(curVal);
		selectOfThisType.addActionListener(this);
		editThisRelation.addActionListener(this);
		editThisRelation.setOpaque(false);
		selectOfThisType.setOpaque(false);
		keggRelationSrcTgtEditor.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		keggRelationSrcTgtEditor.setPreferredSize(new Dimension(20, keggRelationSrcTgtEditor.getPreferredSize().height));
	}
	
	public JComponent getComponent() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return TableLayout.getSplit(
								keggRelationSrcTgtEditor,
								TableLayout.getSplit(editThisRelation, selectOfThisType,
													TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED),
								TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
		else
			return TableLayout.getSplit(
								keggRelationSrcTgtEditor, selectOfThisType,
								TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
		
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			selectOfThisType.setEnabled(false);
			editThisRelation.setEnabled(false);
			keggRelationSrcTgtEditor.setText(EMPTY_STRING);
		} else {
			selectOfThisType.setEnabled(true);
			editThisRelation.setEnabled(true);
			String curVal = ((KeggRelationSrcTgtAttribute) getDisplayable()).getString();
			keggRelationSrcTgtEditor.setText(curVal);
		}
	}
	
	public void setValue() {
		if (!keggRelationSrcTgtEditor.getText().equals(EMPTY_STRING))
			((KeggRelationSrcTgtAttribute) displayable).setString(keggRelationSrcTgtEditor.getText());
	}
	
	public void actionPerformed(ActionEvent arg0) {
		String currentRelSrcTgtVal = keggRelationSrcTgtEditor.getText();
		if (currentRelSrcTgtVal == null)
			return;
		if (currentRelSrcTgtVal.indexOf(";") >= 0)
			currentRelSrcTgtVal = currentRelSrcTgtVal.substring(0, currentRelSrcTgtVal.indexOf(";"));
		KeggRelationSrcTgtAttribute kta = (KeggRelationSrcTgtAttribute) displayable;
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
		if (arg0.getSource() == selectOfThisType) {
			selectRelations(currentRelSrcTgtVal, graph, selection);
		}
		if (arg0.getSource() == editThisRelation) {
			editRelations((GraphElement) a, currentRelSrcTgtVal, graph);
		}
	}
	
	public static void editRelations(GraphElement ge, String currentRelationSrcTgtIds, Graph graph) {
		Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
		Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
		HashMap<Entry, Node> entry2graphNode = new HashMap<Entry, Node>();
		Pathway p = Pathway.getPathwayFromGraph(graph, warnings, errors, entry2graphNode);
		if (errors.size() > 0) {
			MainFrame.showMessageDialog(
								"<html>" +
													"The current graph can not be error-free interpreted<br>" +
													"as an KEGG Pathway!<br><br>" + errors.size() + " errors have been found<br>" +
													"during graph analysis.<br><br>" +
													"Please choose <b>Cancel</b> in the following edit dialog,<br>" +
													"in order to leave the current graph unmodified.<br>" +
													"Then select the Pathway Editing side panel and locate and fix<br>" +
													"the problematic network elements.<br><br>" +
													"If you want to save the current work without loosing any network<br>" +
													"elements, select and use the <b>GML</b> format available with the " +
													"<b>Save As...</b> command.<br><br>" +
													"In case you select <b>OK</b> in the following edit dialog,<br>" +
													"only the network elements, which are error-free converted<br>" +
													"into a KEGG Pathway model will be conserved.", "Error");
		}
		Collection<Relation> rl = new ArrayList<Relation>();
		if (currentRelationSrcTgtIds != null && currentRelationSrcTgtIds.length() > 0)
			rl = p.findRelations(currentRelationSrcTgtIds);
		
		if (rl.size() < 1) {
			if (currentRelationSrcTgtIds != null && currentRelationSrcTgtIds.length() > 0)
				MainFrame.showMessageDialog(
									"<html>" +
														"No relation does match this criteria.<br>" +
														"(Internal Error)<br>" +
														"All relations are shown.",
									"Error");
			rl = p.getRelations();
		}
		
		Entry defaultNewSourceEntry = null;
		Entry defaultNewTargetEntry = null;
		if (ge != null && ge instanceof Edge) {
			defaultNewSourceEntry = findEntry(((Edge) ge).getSource(), entry2graphNode);
			defaultNewTargetEntry = findEntry(((Edge) ge).getTarget(), entry2graphNode);
		}
		Relation initialRelation = null;
		if (rl.size() > 0) {
			if (ge != null && ge instanceof Edge) {
				HashSet<Relation> validRels = new HashSet<Relation>();
				Node a = ((Edge) ge).getSource();
				Node b = ((Edge) ge).getTarget();
				for (Relation r : rl) {
					Node ta = entry2graphNode.get(r.getSourceEntry());
					Node tb = entry2graphNode.get(r.getTargetEntry());
					if ((ta == a && tb == b) || (ta == b && tb == a))
						validRels.add(r);
				}
				for (Relation r : rl) {
					Node ta = entry2graphNode.get(r.getSourceEntry());
					Node tb = entry2graphNode.get(r.getTargetEntry());
					for (IdRef ir : r.getSubtypeRefs()) {
						Entry ste = ir.getRef();
						Node tst = entry2graphNode.get(ste);
						if ((tst == a || tst == b) && (ta == a || ta == b || tb == a || tb == b))
							validRels.add(r);
					}
				}
				rl = validRels;
			}
			if (initialRelation == null && rl.size() > 0)
				initialRelation = rl.iterator().next();
		}
		JLabel statusLabel = EntryCreator.getErrorStatusLabel(errors, warnings);
		
		Collection<Entry> validEntries = p.getEntries();
		Collection<Entry> validCompoundEntries = getEntriesAcceptibleForSubComponent(validEntries);
		JLabel relationHelp = new JLabel();
		RelationTypeEditor relTypeEditor = new RelationTypeEditor(initialRelation, relationHelp);
		
		String relDesc = "";
		if (initialRelation != null)
			relDesc = initialRelation.toStringWithShortDesc(true);
		JLabel relationDescription = new JLabel(relDesc);
		
		JLabel subComponentTypesHelp = new JLabel();
		SubComponentTypesEditor subComponentTypesEditor = new SubComponentTypesEditor(initialRelation, subComponentTypesHelp);
		SrcTargetEditor srcTargetEditor = new SrcTargetEditor("Edit Source or Target", initialRelation, validEntries, entry2graphNode);
		SubtypeCompoundEditor subtypeCompoundEditor = new SubtypeCompoundEditor("Edit Compound-Subtype", initialRelation, validCompoundEntries, entry2graphNode);
		
		JLabel helpComponent = new JLabel();
		helpComponent.setPreferredSize(new Dimension(50, 150));
		JComponent relationSelection = getRelationSelection(
							defaultNewSourceEntry, defaultNewTargetEntry,
							initialRelation,
							rl, entry2graphNode, helpComponent, relTypeEditor,
							relationDescription, subComponentTypesEditor, srcTargetEditor, subtypeCompoundEditor,
							p);
		
		Object[] input = MyInputHelper.getInput(
							relationSelection,
							"Edit Relation(s)",
							new Object[] {
												"Relation", relationDescription,
												"Type", relTypeEditor,
												"", relationHelp,
												"Source/Target", srcTargetEditor,
												"Subtype(s)", subComponentTypesEditor,
												"Compound-Subtype", subtypeCompoundEditor,
												"Subtype valid?", subComponentTypesHelp,
												// "Help", helpComponent,
									"", null,
												"",
												statusLabel
				});
		if (input != null) {
			// now update view
			Document d = p.getKgmlDocument();
			Pathway p2 = Pathway.getPathwayFromKGML(d.getRootElement());
			graph.deleteAll(graph.getGraphElements());
			p2.getGraph(graph);
		}
	}
	
	private static Entry findEntry(Node n, HashMap<Entry, Node> entry2graphNode) {
		for (Entry e : entry2graphNode.keySet()) {
			if (entry2graphNode.get(e) == n)
				return e;
		}
		return null;
	}
	
	private static JComponent getRelationSelection(
						final Entry defaultNewSourceEntry, final Entry defaultNewTargetEntry,
						final Relation initialRelation,
						final Collection<Relation> relations, final HashMap<Entry, Node> entry2graphNode,
						final JLabel helpComponent, final RelationTypeEditor relationTypeEditor,
						final JLabel relationDescription,
						final SubComponentTypesEditor subComponentTypesEditor,
						SrcTargetEditor srcTargetEditor,
						SubtypeCompoundEditor subtypeCompoundEditor, Pathway pathway) {
		final MyRelationList relSel = new MyRelationList(relations.toArray(),
							relationTypeEditor, subComponentTypesEditor, relationDescription,
							srcTargetEditor, subtypeCompoundEditor
							);
		relSel.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				helpComponent.setText(Relation.getTypeDescription(true));
			}
			
			public void focusLost(FocusEvent arg0) {
			}
		});
		
		relSel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Relation r = (Relation) relSel.getSelectedValue();
				relSel.updateRelationInfo(r);
				if (r != null) {
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
				}
			}
		});
		
		final JButton add = new JButton("add");
		JButton del = new JButton("del");
		add.addActionListener(getAddRelationListner(relSel, pathway, defaultNewSourceEntry, defaultNewTargetEntry));
		del.addActionListener(getDeleteRelationListner(relSel, pathway));
		add.setOpaque(false);
		del.setOpaque(false);
		JComponent addDel = TableLayout.get3Split(
							new JLabel("Relations "), add, del,
							TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
		
		JComponent result =
							TableLayout.getSplitVertical(
												addDel,
												new JScrollPane(relSel),
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.FILL);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				relSel.setSelectedValue(initialRelation, true);
				if ((relations == null || relations.size() <= 0) && (defaultNewSourceEntry != null && defaultNewTargetEntry != null))
					add.doClick();
				
			}
		});
		return result;
	}
	
	private static ActionListener getDeleteRelationListner(final MyRelationList relSel, final Pathway pathway) {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Relation r = (Relation) relSel.getSelectedValue();
				if (r == null) {
					MainFrame.showMessageDialog("No relation selected!", "Error");
				} else {
					pathway.getRelations().remove(r);
					((DefaultListModel) relSel.getModel()).removeElement(r);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if (((DefaultListModel) relSel.getModel()).size() > 0) {
								relSel.setSelectedValue(relSel.getModel().getElementAt(0), true);
							}
						}
					});
				}
			}
		};
		return al;
	}
	
	private static ActionListener getAddRelationListner(final MyRelationList relSel, final Pathway pathway,
						final Entry defaultNewSourceEntry, final Entry defaultNewTargetEntry) {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				IdRef e1 = null;
				IdRef e2 = null;
				if (defaultNewSourceEntry != null)
					e1 = new IdRef(defaultNewSourceEntry, defaultNewSourceEntry.getId().getValue());
				if (defaultNewTargetEntry != null)
					e2 = new IdRef(defaultNewTargetEntry, defaultNewTargetEntry.getId().getValue());
				final Relation newRelation = new Relation(e1, e2, null, new ArrayList<Subtype>());
				pathway.getRelations().add(newRelation);
				((DefaultListModel) relSel.getModel()).addElement(newRelation);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (((DefaultListModel) relSel.getModel()).size() > 0) {
							relSel.setSelectedValue(newRelation, true);
						}
					}
				});
			}
		};
		return al;
	}
	
	private static Collection<Entry> getEntriesAcceptibleForSubComponent(Collection<Entry> entries) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		// HashSet<String> knownIds = new HashSet<String>();
		if (entries != null)
			for (Entry e : entries) {
				EntryType et = e.getType();
				if (et == EntryType.compound || et == EntryType.ortholog || et == EntryType.hiddenCompound) {
					result.add(e);
				}
			}
		return result;
	}
	
	private void selectRelations(String currentSrcTgt, Graph graph, Selection selection) {
		String srcid = currentSrcTgt.substring(0, currentSrcTgt.indexOf("/"));
		String tgtid = currentSrcTgt.substring(currentSrcTgt.indexOf("/") + 1);
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (Edge e : graph.getEdges()) {
			ArrayList<IndexAndString> relations = KeggGmlHelper.getRelationSourceAndTargets(e);
			boolean match = false;
			for (IndexAndString ias : relations) {
				String relationSrcTgt = ias.getValue();
				if (relationSrcTgt.equals(currentSrcTgt)) {
					match = true;
					break;
				}
			}
			if (match) {
				edges.add(e);
			}
		}
		selection.addAll(edges);
		HashSet<Node> checkTheseNodes = new HashSet<Node>();
		for (Edge e : edges) {
			checkTheseNodes.add(e.getSource());
			checkTheseNodes.add(e.getTarget());
		}
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Node n : checkTheseNodes) {
			String id = KeggGmlHelper.getKeggId(n);
			boolean match = srcid.equals(id) || tgtid.equals(id);
			
			if (!match) {
				ArrayList<IndexAndString> iasKEGGids = KeggGmlHelper.getKeggIds(n);
				for (IndexAndString ias : iasKEGGids) {
					if (ias.getValue().equals(id)) {
						match = true;
						break;
					}
				}
			}
			
			if (match) {
				nodes.add(n);
			}
		}
		selection.addAll(nodes);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(nodes.size() + " nodes and " + edges.size() + "edges do match this criteria", MessageType.INFO);
	}
	
	public static void editRelations(Relation initialRelation, Pathway p,
						Collection<Gml2PathwayWarningInformation> warnings,
						Collection<Gml2PathwayErrorInformation> errors,
						HashMap<Entry, Node> entry2graphNode, Graph graph) {
		JLabel statusLabel = EntryCreator.getErrorStatusLabel(errors, warnings);
		
		Collection<Entry> validEntries = p.getEntries();
		Collection<Entry> validCompoundEntries = getEntriesAcceptibleForSubComponent(validEntries);
		JLabel relationHelp = new JLabel();
		RelationTypeEditor relTypeEditor = new RelationTypeEditor(initialRelation, relationHelp);
		
		String relDesc = "";
		if (initialRelation != null)
			relDesc = initialRelation.toStringWithShortDesc(true);
		JLabel relationDescription = new JLabel(relDesc);
		
		JLabel subComponentTypesHelp = new JLabel();
		SubComponentTypesEditor subComponentTypesEditor = new SubComponentTypesEditor(initialRelation, subComponentTypesHelp);
		SrcTargetEditor srcTargetEditor = new SrcTargetEditor("Edit Source or Target", initialRelation, validEntries, entry2graphNode);
		SubtypeCompoundEditor subtypeCompoundEditor = new SubtypeCompoundEditor("Edit Compound-Subtype", initialRelation, validCompoundEntries, entry2graphNode);
		
		JLabel helpComponent = new JLabel();
		helpComponent.setPreferredSize(new Dimension(50, 150));
		Collection<Relation> rl = p.getRelations();
		JComponent relationSelection = getRelationSelection(
							null, null,
							initialRelation,
							rl, entry2graphNode, helpComponent, relTypeEditor,
							relationDescription, subComponentTypesEditor, srcTargetEditor, subtypeCompoundEditor,
							p);
		
		Object[] input = MyInputHelper.getInput(
							relationSelection,
							"Edit Relation(s)",
							new Object[] {
												"Relation", relationDescription,
												"Type", relTypeEditor,
												"", relationHelp,
												"Source/Target", srcTargetEditor,
												"Subtype(s)", subComponentTypesEditor,
												"Compound-Subtype", subtypeCompoundEditor,
												"Subtype valid?", subComponentTypesHelp,
												// "Help", helpComponent,
									"", null,
												"",
												statusLabel
				});
		if (input != null) {
			// now update view
			Document d = p.getKgmlDocument();
			Pathway p2 = Pathway.getPathwayFromKGML(d.getRootElement());
			graph.deleteAll(graph.getGraphElements());
			p2.getGraph(graph);
		}
	}
}
