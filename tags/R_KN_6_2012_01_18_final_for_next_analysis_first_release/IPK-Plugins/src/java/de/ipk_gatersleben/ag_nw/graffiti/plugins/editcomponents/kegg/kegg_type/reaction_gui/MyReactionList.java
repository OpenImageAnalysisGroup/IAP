/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.05.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.reaction_gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;

public class MyReactionList extends JList {
	ReactionIdEditor reactionIdEditor;
	ReactionTypeSelection reactionTypeSelection;
	JLabel reactionDescription;
	CompoundListEditor l1;
	CompoundListEditor l2;
	CompoundListEditor l3;
	
	public MyReactionList(
						Object[] objects,
						JLabel reactionDescription,
						ReactionIdEditor reactionIdEditor,
						ReactionTypeSelection reactionTypeSelection,
						CompoundListEditor l1,
						CompoundListEditor l2,
						CompoundListEditor l3) {
		super();
		setModel(new DefaultListModel());
		for (Object o : objects)
			((DefaultListModel) getModel()).addElement(o);
		this.reactionDescription = reactionDescription;
		this.reactionIdEditor = reactionIdEditor;
		this.reactionTypeSelection = reactionTypeSelection;
		this.l1 = l1;
		this.l2 = l2;
		this.l3 = l3;
		
		reactionTypeSelection.setCallBack(this);
		l1.setCallBack(this);
		l2.setCallBack(this);
		l3.setCallBack(this);
		
		reactionIdEditor.setCallBack(this);
		
		setCellRenderer(getReactionCellRenderer());
	}
	
	private static final long serialVersionUID = 1L;
	
	public void updateReactionInfo(Reaction r) {
		if (r != null) {
			reactionDescription.setText(r.toStringWithDetails(true, true));
			// reactionDescription.setToolTipText(r.toStringWithDetails(true, true));
		} else {
			reactionDescription.setText("");
			// reactionDescription.setToolTipText("");
		}
		
		reactionIdEditor.updateReactionSelection(r);
		reactionTypeSelection.updateReactionSelection(r);
		l1.updateReactionSelection(r);
		l2.updateReactionSelection(r);
		l3.updateReactionSelection(r);
		repaint();
		
		JDialog jd = (JDialog) ErrorMsg.findParentComponent(this, JDialog.class);
		if (jd != null)
			jd.pack();
	}
	
	private ListCellRenderer getReactionCellRenderer() {
		ListCellRenderer res = new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Reaction r = (Reaction) value;
				JLabel res = new JLabel(r.toStringWithDetails(true, false));
				res.setToolTipText(r.toStringWithDetails(true, true));
				res.setOpaque(true);
				if (isSelected)
					res.setBackground(new Color(240, 240, 255));
				else
					res.setBackground(Color.WHITE);
				if (cellHasFocus)
					res.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
				else
					res.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
				Color markColor;
				if (isSelected)
					markColor = Color.BLACK;
				else
					markColor = Color.WHITE;
				JLabel b1 = new JLabel();
				b1.setOpaque(true);
				b1.setBackground(markColor);
				JLabel b2 = new JLabel();
				b2.setOpaque(true);
				b2.setBackground(markColor);
				return TableLayout.get3Split(b1, res, b2, 5, TableLayoutConstants.FILL, 5);
			}
		};
		return res;
	}
}
