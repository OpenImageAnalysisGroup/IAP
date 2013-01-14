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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
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
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.reaction_gui.CompoundListEditor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.reaction_gui.MyReactionList;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.reaction_gui.ReactionIdEditor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.reaction_gui.ReactionTypeSelection;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.IndexAndString;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.ReactionType;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggReactionIdAttributeEditor
					extends AbstractValueEditComponent
					implements ActionListener {
	protected JLabel keggReactionIdEditor = new JLabel();
	protected JButton selectOfThisType = new JButton("Select");
	protected JButton editThisReaction = new JButton("Edit");
	
	public KeggReactionIdAttributeEditor(final Displayable disp) {
		super(disp);
		String curVal = ((KeggReactionIdAttribute) getDisplayable()).getString();
		keggReactionIdEditor.setText(curVal);
		selectOfThisType.addActionListener(this);
		editThisReaction.addActionListener(this);
		editThisReaction.setOpaque(false);
		selectOfThisType.setOpaque(false);
		keggReactionIdEditor.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		keggReactionIdEditor.setPreferredSize(new Dimension(20, keggReactionIdEditor.getPreferredSize().height));
	}
	
	public JComponent getComponent() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return TableLayout.getSplit(
								keggReactionIdEditor,
								TableLayout.getSplit(editThisReaction, selectOfThisType,
													TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED),
								TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
		else
			return TableLayout.getSplit(
								keggReactionIdEditor, selectOfThisType,
								TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			selectOfThisType.setEnabled(false);
			editThisReaction.setEnabled(false);
			keggReactionIdEditor.setText(EMPTY_STRING);
		} else {
			selectOfThisType.setEnabled(true);
			editThisReaction.setEnabled(true);
			String curVal = ((KeggReactionIdAttribute) getDisplayable()).getString();
			keggReactionIdEditor.setText(curVal);
		}
	}
	
	public void setValue() {
		if (!keggReactionIdEditor.getText().equals(EMPTY_STRING))
			((KeggReactionIdAttribute) displayable).setString(keggReactionIdEditor.getText());
	}
	
	public void actionPerformed(ActionEvent arg0) {
		String currentReactionId = keggReactionIdEditor.getText();
		if (currentReactionId == null)
			return;
		if (currentReactionId.indexOf(";") >= 0)
			currentReactionId = currentReactionId.substring(0, currentReactionId.indexOf(";"));
		KeggReactionIdAttribute kta = (KeggReactionIdAttribute) displayable;
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
			selectReactions(currentReactionId, graph, selection);
		}
		if (arg0.getSource() == editThisReaction) {
			editReactions(null, currentReactionId, graph, selection);
		}
	}
	
	public static void editReactions(
						GraphElement ge,
						String currentReactionId, Graph graph, Selection selection) {
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
		Collection<Reaction> rl = null;
		if (currentReactionId != null && currentReactionId.length() > 0)
			rl = p.findReaction(currentReactionId);
		else {
			HashSet<String> findReacIds = new HashSet<String>();
			if (ge != null && ge instanceof Node) {
				for (IndexAndString ias : KeggGmlHelper.getKeggReactions((Node) ge)) {
					findReacIds.add(ias.getValue());
				}
			}
			if (ge != null && ge instanceof Edge) {
				for (IndexAndString ias : KeggGmlHelper.getKeggReactionSubstrates((Edge) ge)) {
					findReacIds.add(ias.getValue());
				}
				for (IndexAndString ias : KeggGmlHelper.getKeggReactionProducts((Edge) ge)) {
					findReacIds.add(ias.getValue());
				}
			}
			rl = p.findReaction(findReacIds);
		}
		
		Reaction currReaction = null;
		if (rl != null && rl.size() > 0)
			rl.iterator().next();
		
		ReactionIdEditor reactionIdEditor = new ReactionIdEditor(currReaction, p);
		ReactionTypeSelection reactionTypeSelection = new ReactionTypeSelection(currReaction);
		CompoundListEditor l1 = new CompoundListEditor(currReaction, p, true, false, false, entry2graphNode);
		CompoundListEditor l2 = new CompoundListEditor(currReaction, p, false, true, false, entry2graphNode);
		CompoundListEditor l3 = new CompoundListEditor(currReaction, p, false, false, true, entry2graphNode);
		
		JLabel reacDesc = new JLabel("");
		
		MyReactionList reacList = new MyReactionList(
							rl.toArray(),
							reacDesc,
							reactionIdEditor,
							reactionTypeSelection,
							l1, l2, l3
							);
		
		Object[] input = MyInputHelper.getInput(
							getReactionSelection(currReaction, null, null, reacList, entry2graphNode, p),
							"Edit Reaction",
							new Object[] {
												"Description", reacDesc,
												"Reaction ID", reactionIdEditor,
												"Reaction Type", reactionTypeSelection,
												"Substrates", l1,
												"Enzymes", l2,
												"Products", l3,
												"",
												new JLabel("<html><font color='" + (errors.size() > 0 ? "red" : "gray") + "'><small><br>" +
																	"Information: Interpretation of graph network as KGML data model produced " +
																	errors.size() + " errors and " +
																	warnings.size() + " warnings.<br><br>")
				});
		if (input != null) {
			// update view
			Document d = p.getKgmlDocument();
			Pathway p2 = Pathway.getPathwayFromKGML(d.getRootElement());
			graph.deleteAll(graph.getGraphElements());
			p2.getGraph(graph);
		}
	}
	
	private static JComponent getReactionSelection(
						final Reaction initialReaction,
						final Entry defaultNewSourceEntry, final Entry defaultNewTargetEntry,
						final MyReactionList reacSel,
						final HashMap<Entry, Node> entry2graphNode,
						final Pathway pathway) {
		
		reacSel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		reacSel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Reaction r = (Reaction) reacSel.getSelectedValue();
				reacSel.updateReactionInfo(r);
				if (r != null) {
					HashSet<Node> nodes = new HashSet<Node>();
					for (Entry s : r.getSubstrates())
						nodes.add(entry2graphNode.get(s));
					for (Entry enz : r.getEntriesRepresentingThisReaction(pathway.getEntries()))
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
				}
			}
		});
		
		final JButton add = new JButton("add");
		JButton del = new JButton("del");
		add.addActionListener(getAddReactionListner(reacSel, pathway, defaultNewSourceEntry, defaultNewTargetEntry));
		del.addActionListener(getDeleteReactionListner(reacSel, pathway));
		add.setOpaque(false);
		del.setOpaque(false);
		JComponent addDel = TableLayout.get3Split(
							new JLabel("Reactions "), add, del,
							TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
		
		JComponent result =
							TableLayout.getSplitVertical(
												addDel,
												new JScrollPane(reacSel),
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.FILL);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (initialReaction != null)
					reacSel.setSelectedValue(initialReaction, true);
				else {
					if (((DefaultListModel) reacSel.getModel()).size() > 0)
						reacSel.setSelectedValue(((DefaultListModel) reacSel.getModel()).firstElement(), true);
					
				}
				if (((DefaultListModel) reacSel.getModel()).size() <= 0)
					add.doClick();
			}
		});
		return result;
	}
	
	private static ActionListener getDeleteReactionListner(final MyReactionList reacSel, final Pathway pathway) {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Reaction r = (Reaction) reacSel.getSelectedValue();
				if (r == null) {
					MainFrame.showMessageDialog("No reaction selected!", "Error");
				} else {
					for (Entry e : r.getEntriesRepresentingThisReaction(pathway.getEntries()))
						e.removeReaction(r);
					pathway.getReactions().remove(r);
					r.getEntriesRepresentingThisReaction(pathway.getEntries());
					((DefaultListModel) reacSel.getModel()).removeElement(r);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if (((DefaultListModel) reacSel.getModel()).size() > 0) {
								reacSel.setSelectedValue(reacSel.getModel().getElementAt(0), true);
							}
						}
					});
				}
			}
		};
		return al;
	}
	
	private static ActionListener getAddReactionListner(final MyReactionList reacSel, final Pathway pathway,
						final Entry defaultNewSourceEntry, final Entry defaultNewTargetEntry) {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<Entry> sl = new ArrayList<Entry>();
				ArrayList<Entry> pl = new ArrayList<Entry>();
				
				if (defaultNewSourceEntry != null)
					sl.add(defaultNewSourceEntry);
				
				if (defaultNewTargetEntry != null)
					pl.add(defaultNewTargetEntry);
				
				final Reaction newReaction = new Reaction("R00000", ReactionType.reversible, sl, pl);
				pathway.getReactions().add(newReaction);
				((DefaultListModel) reacSel.getModel()).addElement(newReaction);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (((DefaultListModel) reacSel.getModel()).size() > 0) {
							reacSel.setSelectedValue(newReaction, true);
						}
					}
				});
			}
		};
		return al;
	}
	
	private void selectReactions(String currentReactionId, Graph graph, Selection selection) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			ArrayList<IndexAndString> reacTypeInfos = KeggGmlHelper.getKeggReactions(n);
			boolean match = false;
			for (IndexAndString ias : reacTypeInfos) {
				String reaction_type = ias.getValue();
				if (reaction_type.equals(currentReactionId)) {
					match = true;
					break;
				}
			}
			if (match) {
				nodes.add(n);
			}
		}
		selection.addAll(nodes);
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (Edge e : graph.getEdges()) {
			ArrayList<IndexAndString> reacTypeInfos1 = KeggGmlHelper.getKeggReactionProducts(e);
			ArrayList<IndexAndString> reacTypeInfos2 = KeggGmlHelper.getKeggReactionSubstrates(e);
			ArrayList<IndexAndString> reacTypeInfos = new ArrayList<IndexAndString>();
			reacTypeInfos.addAll(reacTypeInfos1);
			reacTypeInfos.addAll(reacTypeInfos2);
			boolean match = false;
			for (IndexAndString ias : reacTypeInfos) {
				String reaction_type = ias.getValue();
				if (reaction_type.indexOf(currentReactionId) >= 0) {
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
		MainFrame.showMessage(nodes.size() + " nodes and " + edges.size() + "edges do match this criteria", MessageType.INFO);
	}
	
	public static void editReactions(Reaction r, Pathway p, Collection<Gml2PathwayWarningInformation> warnings, Collection<Gml2PathwayErrorInformation> errors,
						HashMap<Entry, Node> entry2graphNode, Graph graph) {
		ReactionIdEditor reactionIdEditor = new ReactionIdEditor(r, p);
		ReactionTypeSelection reactionTypeSelection = new ReactionTypeSelection(r);
		CompoundListEditor l1 = new CompoundListEditor(r, p, true, false, false, entry2graphNode);
		CompoundListEditor l2 = new CompoundListEditor(r, p, false, true, false, entry2graphNode);
		CompoundListEditor l3 = new CompoundListEditor(r, p, false, false, true, entry2graphNode);
		
		JLabel reacDesc = new JLabel("");
		
		MyReactionList reacList = new MyReactionList(
							(p.getReactions() != null ? p.getReactions().toArray() : new Object[] {}),
							reacDesc,
							reactionIdEditor,
							reactionTypeSelection,
							l1, l2, l3
							);
		
		Object[] input = MyInputHelper.getInput(
							getReactionSelection(r, null, null, reacList, entry2graphNode, p),
							"Edit Reaction",
							new Object[] {
												"Description", reacDesc,
												"Reaction ID", reactionIdEditor,
												"Reaction Type", reactionTypeSelection,
												"Substrates", l1,
												"Enzymes", l2,
												"Products", l3,
												"",
												new JLabel("<html><font color='" + (errors.size() > 0 ? "red" : "gray") + "'><small><br>" +
																	"Information: Interpretation of graph network as KGML data model produced " +
																	errors.size() + " errors and " +
																	warnings.size() + " warnings.<br><br>")
				});
		if (input != null) {
			// update view
			Document d = p.getKgmlDocument();
			Pathway p2 = Pathway.getPathwayFromKGML(d.getRootElement());
			graph.deleteAll(graph.getGraphElements());
			p2.getGraph(graph);
		}
	}
}
