/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.reaction_gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.FolderPanel;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.MutableList;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;

public class CompoundListEditor extends JComponent {
	private static final long serialVersionUID = 1L;
	private MutableList subProdSelection = new MutableList(new DefaultListModel());
	private Reaction currReaction;
	private boolean isProductSelection;
	private boolean isEnzymeSelection;
	private boolean isSubstrateSelection;
	private Pathway pathway;
	private MyReactionList list;
	
	public CompoundListEditor(
						Reaction currReaction,
						Pathway p,
						boolean sub, boolean enz, boolean prod,
						HashMap<Entry, Node> entry2graphNode) {
		this.pathway = p;
		this.isProductSelection = prod;
		this.isEnzymeSelection = enz;
		this.isSubstrateSelection = sub;
		JComponent mc = null;
		if (enz) {
			Collection<Entry> validEnzymeEntries = getEntriesAcceptibleForEnzyme(
								p.getEntries());
			mc = getEntryList("Enzymes", validEnzymeEntries, entry2graphNode);
		}
		if (sub) {
			Collection<Entry> validEntries = getEntriesAcceptibleForSubstrateOrProduct(
								p.getEntries());
			mc = getEntryList("Substrates", validEntries, entry2graphNode);
		}
		if (prod) {
			Collection<Entry> validEntries = getEntriesAcceptibleForSubstrateOrProduct(
								p.getEntries());
			mc = getEntryList("Products", validEntries, entry2graphNode);
		}
		setLayout(TableLayout.getLayout(TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED));
		add(mc, "0,0");
		validate();
	}
	
	private Collection<Entry> getEntriesAcceptibleForEnzyme(Collection<Entry> entries) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		if (entries != null)
			for (Entry e : entries) {
				EntryType et = e.getType();
				if (et != EntryType.map && et != EntryType.unspecified && et != EntryType.group
									&& et != EntryType.group && et != EntryType.genes && et != EntryType.compound) {
					result.add(e);
				}
			}
		return result;
	}
	
	private Collection<Entry> getEntriesAcceptibleForSubstrateOrProduct(Collection<Entry> entries) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		HashSet<String> knownIds = new HashSet<String>();
		if (entries != null)
			for (Entry e : entries) {
				EntryType et = e.getType();
				if (et != EntryType.map && et != EntryType.unspecified && et != EntryType.group
									&& et != EntryType.group && et != EntryType.genes && et != EntryType.enzyme) {
					if (!knownIds.contains(e.getName().getId())) {
						knownIds.add(e.getName().getId());
						result.add(e);
					}
				}
			}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private JComponent getEntryList(String title,
						final Collection<Entry> entries,
						final HashMap<Entry, Node> entry2graphNode) {
		final MutableList entrySelection = new MutableList(new DefaultListModel());
		Collections.sort((List) entries, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		});
		for (Entry e : entries) {
			entrySelection.getContents().addElement(e);
		}
		if (isEnzymeSelection)
			entrySelection.addListSelectionListener(getEntryGraphSelectionListener(entry2graphNode, entrySelection, false));
		else
			entrySelection.addListSelectionListener(getEntryGraphSelectionListener(entry2graphNode, entrySelection, true));
		
		final JLabel searchResult = new JLabel("<html><small><font color='gray'>" + entries.size() + " entries");
		
		JScrollPane entrySelectionScrollPane = new JScrollPane(entrySelection);
		
		entrySelectionScrollPane.setPreferredSize(new Dimension(300, 100));
		
		// ///////////
		Collections.sort((List) entries, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		});
		
		if (isEnzymeSelection)
			subProdSelection.addListSelectionListener(getEntryGraphSelectionListener(entry2graphNode, subProdSelection, false));
		else
			subProdSelection.addListSelectionListener(getEntryGraphSelectionListener(entry2graphNode, subProdSelection, true));
		
		JScrollPane subProdScrollPane = new JScrollPane(subProdSelection);
		
		subProdScrollPane.setPreferredSize(new Dimension(300, 100));
		// ///////////
		
		final JTextField filter = new JTextField("");
		
		filter.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			
			public void keyReleased(KeyEvent e) {
			}
			
			public void keyTyped(KeyEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String filterText = filter.getText().toUpperCase();
						entrySelection.getContents().clear();
						for (Entry e : entries) {
							if (e.toString().toUpperCase().contains(filterText))
								entrySelection.getContents().addElement(e);
						}
						searchResult.setText("<html><small><font color='gray'>" + entrySelection.getContents().size() + "/" + entries.size() + " entries shown");
					};
				});
			}
		});
		
		JButton addCmd = new JButton();
		JButton delCmd = new JButton();
		
		addCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (entrySelection.getSelectedValues() == null || entrySelection.getSelectedValues().length <= 0)
					MainFrame.showMessageDialog("Please select a item to be added!", "No item selected");
				else {
					for (Object o : entrySelection.getSelectedValues()) {
						Entry e = (Entry) o;
						if (!subProdSelection.getContents().contains(e)) {
							subProdSelection.getContents().addElement(e);
							if (isProductSelection)
								currReaction.getProducts().add(e);
							if (isSubstrateSelection)
								currReaction.getSubstrates().add(e);
							if (isEnzymeSelection) {
								KeggId rr = new KeggId(currReaction.getId());
								rr.setReference(currReaction);
								e.addReaction(rr);
							}
						}
					}
					list.updateReactionInfo(currReaction);
				}
			}
		});
		delCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (subProdSelection.getSelectedValues() == null || subProdSelection.getSelectedValues().length <= 0)
					MainFrame.showMessageDialog("Please select a item to be removed!", "No item selected");
				else {
					for (Object o : subProdSelection.getSelectedValues()) {
						Entry e = (Entry) o;
						if (isSubstrateSelection)
							currReaction.getSubstrates().remove(e);
						if (isEnzymeSelection)
							e.removeReaction(currReaction);
						if (isProductSelection)
							currReaction.getProducts().remove(e);
					}
					list.updateReactionInfo(currReaction);
				}
			}
		});
		
		addCmd.setOpaque(false);
		delCmd.setOpaque(false);
		
		if (isSubstrateSelection) {
			delCmd.setText("Remove Substrate");
			addCmd.setText("<< Add <<");
		}
		if (isEnzymeSelection) {
			delCmd.setText("Remove Element");
			addCmd.setText("<< Add <<");
		}
		if (isProductSelection) {
			delCmd.setText("Remove Product");
			addCmd.setText("<< Add <<");
		}
		JComponent addRemoveCmds = TableLayout.getSplit(delCmd, new JLabel(), TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
		
		JComponent resultPane = TableLayout.getSplitVertical(
							subProdScrollPane,
							addRemoveCmds,
							TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
		
		JComponent searchPane = TableLayout.getSplitVertical(
							entrySelectionScrollPane,
							TableLayout.getSplitVertical(
												TableLayout.get3Split(new JLabel("Search"), new JLabel(), filter,
																	TableLayoutConstants.PREFERRED, 2, TableLayoutConstants.FILL),
												searchResult, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED),
								TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
		
		JComponent addPane = TableLayout.get3SplitVertical(null, addCmd, null,
							0, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
		JComponent result = TableLayout.get3Split(resultPane, addPane, searchPane,
							TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
		final FolderPanel fp = new FolderPanel(title, false, true, false, null);
		fp.setFrameColor(new Color(240, 240, 240), new Color(240, 240, 250), 1, 2);
		fp.addGuiComponentRow(null, result, false);
		fp.layoutRows();
		fp.addCollapseListenerDialogSizeUpdate();
		return fp.getBorderedComponent(5, 0, 0, 0);
	}
	
	private ListSelectionListener getEntryGraphSelectionListener(
						final HashMap<Entry, Node> entry2graphNode,
						final MutableList entrySelection,
						final boolean searchId) {
		return new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						HashSet<Node> nodes = new HashSet<Node>();
						for (Object o : entrySelection.getSelectedValues()) {
							Entry e = (Entry) o;
							Node n = entry2graphNode.get(e);
							if (n != null)
								nodes.add(n);
						}
						if (nodes.size() > 0) {
							Graph graph = nodes.iterator().next().getGraph();
							EditorSession es = MainFrame.getInstance().getActiveEditorSession();
							if (es == null || es.getGraph() != graph)
								return;
							if (searchId) {
								HashSet<String> validIds = new HashSet<String>();
								for (Node validNode : nodes) {
									validIds.add(KeggGmlHelper.getKeggId(validNode));
								}
								for (Node n : graph.getNodes()) {
									String keggId = KeggGmlHelper.getKeggId(n);
									if (validIds.contains(keggId))
										nodes.add(n);
								}
							}
							Selection selection = es.getSelectionModel().getActiveSelection();
							selection.clear();
							selection.addAll(nodes);
							MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
							MainFrame.showMessage(nodes.size() + " nodes selected", MessageType.INFO);
						}
					}
				});
				
			}
		};
	}
	
	public void updateReactionSelection(Reaction r) {
		this.currReaction = r;
		subProdSelection.getContents().clear();
		
		if (isProductSelection && currReaction != null)
			for (Entry e : currReaction.getProducts()) {
				subProdSelection.getContents().addElement(e);
			}
		
		if (isEnzymeSelection && currReaction != null)
			for (Entry e : currReaction.getEntriesRepresentingThisReaction(pathway.getEntries())) {
				subProdSelection.getContents().addElement(e);
			}
		
		if (isSubstrateSelection && currReaction != null)
			for (Entry e : currReaction.getSubstrates()) {
				subProdSelection.getContents().addElement(e);
			}
	}
	
	public void setCallBack(MyReactionList list) {
		this.list = list;
	}
}
