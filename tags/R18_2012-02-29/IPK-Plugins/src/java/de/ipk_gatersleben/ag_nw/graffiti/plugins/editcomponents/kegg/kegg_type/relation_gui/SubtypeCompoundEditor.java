/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;

public class SubtypeCompoundEditor extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private Relation currentRelation;
	private MyRelationList list;
	private MutableList subProdSelection = new MutableList(new DefaultListModel());
	
	@SuppressWarnings("unchecked")
	public SubtypeCompoundEditor(
						String title,
						Relation initialRelation,
						final Collection<Entry> entries,
						final HashMap<Entry, Node> entry2graphNode) {
		this.currentRelation = initialRelation;
		final MutableList entrySelection = new MutableList(new DefaultListModel());
		Collections.sort((List) entries, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		});
		for (Entry e : entries) {
			entrySelection.getContents().addElement(e);
		}
		entrySelection.addListSelectionListener(getEntryGraphSelectionListener(entry2graphNode, entrySelection));
		
		final JLabel searchResult = new JLabel("<html><small><font color='gray'>" + entries.size() + " entries");
		
		JScrollPane entrySelectionScrollPane = new JScrollPane(entrySelection);
		
		entrySelectionScrollPane.setPreferredSize(new Dimension(300, 100));
		
		// ///////////
		Collections.sort((List) entries, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		});
		
		subProdSelection.addListSelectionListener(getEntryGraphSelectionListener(entry2graphNode, subProdSelection));
		
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
		
		JButton addSubtypeCompoundCmd = new JButton("^^ Set (Hidden) Compound ^^");
		JButton removeSubtypeCompound = new JButton("Remove");
		
		addSubtypeCompoundCmd.setOpaque(false);
		removeSubtypeCompound.setOpaque(false);
		
		addSubtypeCompoundCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (entrySelection.getSelectedValues() == null || entrySelection.getSelectedValues().length <= 0)
					MainFrame.showMessageDialog("Please select a item to be added!", "No item selected");
				else {
					for (int i = 0; i < ((DefaultListModel) subProdSelection.getModel()).size(); i++) {
						Entry e = (Entry) ((DefaultListModel) subProdSelection.getModel()).getElementAt(i);
						currentRelation.removeSubtypeRef(e);
					}
					((DefaultListModel) subProdSelection.getModel()).clear();
					for (Object o : entrySelection.getSelectedValues()) {
						Entry e = (Entry) o;
						currentRelation.addSubtypeRef(e);
					}
					list.updateRelationInfo(currentRelation);
				}
			}
		});
		removeSubtypeCompound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (subProdSelection.getSelectedValues() == null || subProdSelection.getSelectedValues().length <= 0)
					MainFrame.showMessageDialog("Please select a item to be removed!", "No item selected");
				else {
					for (Object o : subProdSelection.getSelectedValues()) {
						Entry e = (Entry) o;
						currentRelation.removeSubtypeRef(e);
					}
					list.updateRelationInfo(currentRelation);
				}
			}
		});
		
		addSubtypeCompoundCmd.setOpaque(false);
		
		JComponent addPane;
		
		addPane = TableLayout.getSplit(removeSubtypeCompound, addSubtypeCompoundCmd,
							TableLayoutConstants.FILL, TableLayoutConstants.FILL);
		
		JComponent searchPane = TableLayout.getSplitVertical(
							entrySelectionScrollPane,
							TableLayout.getSplitVertical(
												TableLayout.get3Split(new JLabel("Search "), new JLabel(), filter,
																	TableLayoutConstants.PREFERRED, 2, TableLayoutConstants.FILL),
												searchResult, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED),
								TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
		
		JComponent result;
		result = TableLayout.get3SplitVertical(subProdSelection, addPane, searchPane,
							TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
		final FolderPanel fp = new FolderPanel(title, true, true, false, null);
		fp.addGuiComponentRow(null, result, false);
		fp.setBackground(null);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 2);
		fp.layoutRows();
		fp.addCollapseListenerDialogSizeUpdate();
		
		this.setLayout(TableLayout.getLayout(TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED));
		add(fp, "0,0");
		validate();
	}
	
	private ListSelectionListener getEntryGraphSelectionListener(
						final HashMap<Entry, Node> entry2graphNode,
						final MutableList entrySelection) {
		return new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent se) {
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
					Selection selection = es.getSelectionModel().getActiveSelection();
					selection.clear();
					selection.addAll(nodes);
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
					MainFrame.showMessage(nodes.size() + " nodes selected", MessageType.INFO);
				}
			}
		};
	}
	
	public void updateRelationSelection(Relation r) {
		this.currentRelation = r;
		subProdSelection.getContents().clear();
		if (currentRelation != null)
			for (IdRef ir : currentRelation.getSubtypeRefs()) {
				subProdSelection.getContents().addElement(ir.getRef());
			}
		
	}
	
	public void setCallBack(MyRelationList list) {
		this.list = list;
	}
}
