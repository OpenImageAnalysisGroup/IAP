/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.07.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SearchOptionEditorGUI extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private SearchOption option;
	
	public SearchOptionEditorGUI(final SearchOption option,
						final Collection<AttributePathNameSearchType> possibleAttributes,
						boolean showAndOrField) {
		this(option, possibleAttributes, showAndOrField, false);
	}
	
	public SearchOptionEditorGUI(final SearchOption option,
						final Collection<AttributePathNameSearchType> possibleAttributes,
						boolean showAndOrField, final boolean isFindReplaceDialog) {
		this.option = option;
		
		final JComboBox andOrSel = new JComboBox(new String[] { "AND", "OR" });
		
		double border = 5;
		double[][] size = {
							{
												border,
												// AND/OR
									isFindReplaceDialog ? 0
																	: andOrSel.getPreferredSize().width + 10, // 1
									// node/edge/edge or node
									TableLayoutConstants.PREFERRED, // 2
									// attribute
									TableLayoutConstants.PREFERRED, // 3
									// is/is not
									TableLayoutConstants.PREFERRED, // 4
									// equals/is smaller/is greater/contains/...
									TableLayoutConstants.PREFERRED, // 5
									// search term
									isFindReplaceDialog ? 0 : 100, // 6
									border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, border } }; // Rows
		setLayout(new TableLayout(size));
		// [AND/OR] Node Attribute | AttributeName | Is / Is Not | Equals | Text
		
		if (option.getLogicalConnection() == LogicConnection.AND)
			andOrSel.setSelectedIndex(0);
		if (option.getLogicalConnection() == LogicConnection.OR)
			andOrSel.setSelectedIndex(1);
		andOrSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (andOrSel.getSelectedIndex() == 0)
					option.setLogicalConnection(LogicConnection.AND);
				else
					option.setLogicalConnection(LogicConnection.OR);
			}
		});
		JLabel emptyLabel = new JLabel();
		if (showAndOrField)
			add(andOrSel, "1,1");
		else {
			add(emptyLabel, "1,1");
			option.setLogicalConnection(LogicConnection.OR);
		}
		final JComboBox matchOrNotSel = new JComboBox(new String[] { "is",
							"is not" });
		if (option.getSearchLogic() == SearchLogic.searchMatched)
			matchOrNotSel.setSelectedIndex(0);
		if (option.getSearchLogic() == SearchLogic.searchNotMatched)
			matchOrNotSel.setSelectedIndex(1);
		matchOrNotSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (matchOrNotSel.getSelectedIndex() == 0)
					option.setSearchLogic(SearchLogic.searchMatched);
				else
					option.setSearchLogic(SearchLogic.searchNotMatched);
			}
		});
		if (isFindReplaceDialog)
			add(new JLabel(), "4,1");
		else
			add(matchOrNotSel, "4,1");
		
		final JCheckBox jCheckBoxSearchBoolean = new JCheckBox("Selected");
		jCheckBoxSearchBoolean.setSelected(option.isSearchAttributeBoolean());
		jCheckBoxSearchBoolean.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				option.setSearchAttributeBoolean(jCheckBoxSearchBoolean
									.isSelected());
			}
		});
		
		final JTextField jTextFieldString = new JTextField(option
							.getSearchAttributeString());
		jTextFieldString.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}
			
			public void focusLost(FocusEvent e) {
				option.setSearchAttributeString(jTextFieldString.getText());
			}
		});
		
		final JTextField jTextFieldInteger = new JTextField(new Integer(option
							.getSearchAttributeInteger()).toString());
		jTextFieldInteger.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}
			
			public void focusLost(FocusEvent e) {
				try {
					int i = Integer.parseInt(jTextFieldInteger.getText());
					option.setSearchAttributeInteger(i);
					jTextFieldInteger.setBackground(Color.WHITE);
				} catch (NumberFormatException nfe) {
					option.setSearchAttributeInteger(Integer.MAX_VALUE);
					jTextFieldInteger.setBackground(Color.RED);
				}
			}
		});
		
		final JTextField jTextFieldDouble = new JTextField(new Double(option
							.getSearchAttributeDouble()).toString());
		jTextFieldDouble.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}
			
			public void focusLost(FocusEvent e) {
				try {
					double d = Double.parseDouble(jTextFieldDouble.getText());
					option.setSearchAttributeDouble(d);
					jTextFieldDouble.setBackground(Color.WHITE);
				} catch (NumberFormatException nfe) {
					option.setSearchAttributeDouble(Double.NaN);
					jTextFieldDouble.setBackground(Color.RED);
				}
			}
		});
		
		final JComponent equalSel = new JComboBox(new String[] {
							"containing text", "greater than", "smaller than", "equal to",
							"ending with", "starting with", "matching reg. expr.",
							"sort asc., top n:", "sort desc., top n:" });
		
		final JComboBox attributeSel = new JComboBox();
		attributeSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object o = attributeSel.getSelectedItem();
				if (o != null && (o instanceof AttributePathNameSearchType)) {
					AttributePathNameSearchType pns = (AttributePathNameSearchType) o;
					
					option.setSearchAttributePath(pns.getAttributePath());
					
					option.setSearchAttributeName(pns.getAttributeName());
					
					option.setSearchAttributeBoolean(jCheckBoxSearchBoolean
										.isSelected());
					
					option.setSearchAttributeString(jTextFieldString.getText());
					
					try {
						int i = Integer.parseInt(jTextFieldInteger.getText());
						option.setSearchAttributeInteger(i);
						jTextFieldInteger.setBackground(Color.WHITE);
					} catch (NumberFormatException nfe) {
						option.setSearchAttributeInteger(Integer.MAX_VALUE);
						jTextFieldInteger.setBackground(Color.RED);
					}
					
					try {
						double d = Double.parseDouble(jTextFieldDouble
											.getText());
						option.setSearchAttributeDouble(d);
						jTextFieldInteger.setBackground(Color.WHITE);
					} catch (NumberFormatException nfe) {
						option.setSearchAttributeDouble(Double.NaN);
						jTextFieldInteger.setBackground(Color.RED);
					}
					
					option.setSearchType(pns.getSearchType());
					
					equalSel.setVisible(true);
					
					updateInputField(option, isFindReplaceDialog, jCheckBoxSearchBoolean, jTextFieldString, jTextFieldInteger, jTextFieldDouble, equalSel, pns);
				}
			}
		});
		add(attributeSel, "3,1");
		
		final JComboBox nodeOrEdgeSel = new JComboBox(new String[] {
							"Node Attribute", "Edge Attribute", "Node Or Edge Attribute" });
		if (option.getSearchNodeOrEdge() == NodeOrEdge.Nodes)
			nodeOrEdgeSel.setSelectedIndex(0);
		else
			if (option.getSearchNodeOrEdge() == NodeOrEdge.Edges)
				nodeOrEdgeSel.setSelectedIndex(1);
			else
				if (option.getSearchNodeOrEdge() == NodeOrEdge.NodesAndEdges)
					nodeOrEdgeSel.setSelectedIndex(2);
				else
					option.setSearchNodeOrEdge(NodeOrEdge.Nodes);
		propagateAttributeList(option, possibleAttributes, attributeSel);
		nodeOrEdgeSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (nodeOrEdgeSel.getSelectedIndex() == 0)
					option.setSearchNodeOrEdge(NodeOrEdge.Nodes);
				if (nodeOrEdgeSel.getSelectedIndex() == 1)
					option.setSearchNodeOrEdge(NodeOrEdge.Edges);
				if (nodeOrEdgeSel.getSelectedIndex() == 2)
					option.setSearchNodeOrEdge(NodeOrEdge.NodesAndEdges);
				propagateAttributeList(option, possibleAttributes, attributeSel);
			}
		});
		add(nodeOrEdgeSel, "2,1");
		
		if (!isFindReplaceDialog) {
			
			if (option.getSearchOperation() == SearchOperation.include)
				((JComboBox) equalSel).setSelectedIndex(0);
			if (option.getSearchOperation() == SearchOperation.greater)
				((JComboBox) equalSel).setSelectedIndex(1);
			if (option.getSearchOperation() == SearchOperation.smaller)
				((JComboBox) equalSel).setSelectedIndex(2);
			if (option.getSearchOperation() == SearchOperation.equals)
				((JComboBox) equalSel).setSelectedIndex(3);
			if (option.getSearchOperation() == SearchOperation.endswith)
				((JComboBox) equalSel).setSelectedIndex(4);
			if (option.getSearchOperation() == SearchOperation.startswith)
				((JComboBox) equalSel).setSelectedIndex(5);
			if (option.getSearchOperation() == SearchOperation.regexpsearch)
				((JComboBox) equalSel).setSelectedIndex(6);
			if (option.getSearchOperation() == SearchOperation.topN)
				((JComboBox) equalSel).setSelectedIndex(7);
			if (option.getSearchOperation() == SearchOperation.bottomN)
				((JComboBox) equalSel).setSelectedIndex(8);
			((JComboBox) equalSel).addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (((JComboBox) equalSel).getSelectedIndex() == 0)
						option.setSearchOperation(SearchOperation.include);
					if (((JComboBox) equalSel).getSelectedIndex() == 1)
						option.setSearchOperation(SearchOperation.greater);
					if (((JComboBox) equalSel).getSelectedIndex() == 2)
						option.setSearchOperation(SearchOperation.smaller);
					if (((JComboBox) equalSel).getSelectedIndex() == 3)
						option.setSearchOperation(SearchOperation.equals);
					if (((JComboBox) equalSel).getSelectedIndex() == 4)
						option.setSearchOperation(SearchOperation.endswith);
					if (((JComboBox) equalSel).getSelectedIndex() == 5)
						option.setSearchOperation(SearchOperation.startswith);
					if (((JComboBox) equalSel).getSelectedIndex() == 6)
						option.setSearchOperation(SearchOperation.regexpsearch);
					if (((JComboBox) equalSel).getSelectedIndex() == 7)
						option.setSearchOperation(SearchOperation.topN);
					if (((JComboBox) equalSel).getSelectedIndex() == 8)
						option.setSearchOperation(SearchOperation.bottomN);
					Object o = attributeSel.getSelectedItem();
					if (o != null && (o instanceof AttributePathNameSearchType)) {
						AttributePathNameSearchType pns = (AttributePathNameSearchType) o;
						updateInputField(option, isFindReplaceDialog, jCheckBoxSearchBoolean, jTextFieldString, jTextFieldInteger, jTextFieldDouble, equalSel, pns);
					}
				}
			});
		}
		
		if (!isFindReplaceDialog)
			add(equalSel, "5,1");
		else
			add(new JLabel(), "5,1");
		processBorder(emptyLabel, andOrSel, matchOrNotSel, nodeOrEdgeSel,
							attributeSel, equalSel);
		processInputBorder(jTextFieldDouble, jTextFieldInteger,
							jTextFieldString, jCheckBoxSearchBoolean);
		
		validate();
		
		if (!showAndOrField) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jTextFieldString.requestFocusInWindow();
				}
			});
		}
	}
	
	private void propagateAttributeList(SearchOption option,
						Collection<AttributePathNameSearchType> possibleAttributes,
						final JComboBox attributeSel) {
		attributeSel.removeAllItems();
		boolean includeEdges = option.getSearchNodeOrEdge() == NodeOrEdge.Edges
							|| option.getSearchNodeOrEdge() == NodeOrEdge.NodesAndEdges;
		boolean includeNodes = option.getSearchNodeOrEdge() == NodeOrEdge.Nodes
							|| option.getSearchNodeOrEdge() == NodeOrEdge.NodesAndEdges;
		AttributePathNameSearchType firstAtt = null;
		AttributePathNameSearchType defaultSelection = null;
		for (AttributePathNameSearchType a : possibleAttributes) {
			boolean added = false;
			if (a.toString().replaceAll(":", "").replaceAll(" ", "").indexOf("LabelText") >= 0) {
				defaultSelection = a;
			}
			if (includeEdges && a.isInEdge()) {
				attributeSel.addItem(a);
				if (firstAtt == null)
					firstAtt = a;
				added = true;
			}
			if (includeNodes && a.isInNode() && !added) {
				attributeSel.addItem(a);
				if (firstAtt == null)
					firstAtt = a;
			}
		}
		if (defaultSelection != null) {
			final AttributePathNameSearchType fS = defaultSelection;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					attributeSel.setSelectedItem(fS);
				}
			});
		}
		if (firstAtt != null) {
			option.setSearchAttributePath(firstAtt.getAttributePath());
			option.setSearchAttributeName(firstAtt.getAttributeName());
			option.setSearchType(firstAtt.getSearchType());
		}
		attributeSel.revalidate();
		Component jct = attributeSel;
		while (jct != null && !(jct instanceof JDialog)) {
			jct = jct.getParent();
		}
		if (jct != null && (jct instanceof JDialog))
			((JDialog) jct).pack();
	}
	
	private void processBorder(JComponent... comps) {
		for (JComponent c : comps) {
			c.setOpaque(false);
			c.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
		}
	}
	
	private void processInputBorder(JComponent... comps) {
		for (JComponent c : comps) {
			c.setOpaque(false);
			c.setBorder(BorderFactory.createEtchedBorder());
		}
	}
	
	public SearchOption getSearchOption() {
		return option;
	}
	
	private void updateInputField(final SearchOption option, final boolean isFindReplaceDialog, final JCheckBox jCheckBoxSearchBoolean,
						final JTextField jTextFieldString, final JTextField jTextFieldInteger, final JTextField jTextFieldDouble, final JComponent equalSel,
						AttributePathNameSearchType pns) {
		remove(jCheckBoxSearchBoolean);
		remove(jTextFieldString);
		remove(jTextFieldInteger);
		remove(jTextFieldDouble);
		if (option.getSearchOperation() == SearchOperation.bottomN || option.getSearchOperation() == SearchOperation.topN) {
			add(jTextFieldInteger, "6,1");
		} else {
			switch (pns.getSearchType()) {
				case searchBoolean:
					add(jCheckBoxSearchBoolean, "6,1");
					equalSel.setVisible(false);
					((JComboBox) equalSel).setSelectedIndex(3);
					option.setSearchOperation(SearchOperation.equals);
					break;
				case searchInteger:
					add(jTextFieldInteger, "6,1");
					break;
				case searchString:
					if (!isFindReplaceDialog)
						add(jTextFieldString, "6,1");
					else
						add(new JLabel(""), "6,1");
					break;
				case searchDouble:
					add(jTextFieldDouble, "6,1");
					break;
			}
		}
		repaint();
		validate();
	}
}
