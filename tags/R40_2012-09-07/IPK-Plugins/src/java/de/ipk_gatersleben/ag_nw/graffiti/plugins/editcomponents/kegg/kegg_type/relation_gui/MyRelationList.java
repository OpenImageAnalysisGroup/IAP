/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.05.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;

public class MyRelationList extends JList {
	RelationTypeEditor relationTypeEditor;
	SubComponentTypesEditor subComponentTypesEditor;
	JLabel relationDescription;
	SubtypeCompoundEditor subtypeCompoundEditor;
	SrcTargetEditor srcTargetEditor;
	
	public MyRelationList(
						Object[] objects,
						RelationTypeEditor relationTypeEditor,
						SubComponentTypesEditor subComponentTypesEditor,
						JLabel relationDescription,
						SrcTargetEditor srcTargetEditor,
						SubtypeCompoundEditor subtypeCompoundEditor) {
		super();
		setModel(new DefaultListModel());
		for (Object o : objects)
			((DefaultListModel) getModel()).addElement(o);
		this.relationTypeEditor = relationTypeEditor;
		this.subComponentTypesEditor = subComponentTypesEditor;
		this.relationDescription = relationDescription;
		this.srcTargetEditor = srcTargetEditor;
		this.subtypeCompoundEditor = subtypeCompoundEditor;
		relationTypeEditor.setCallBack(this);
		subComponentTypesEditor.setCallBack(this);
		srcTargetEditor.setCallBack(this);
		subtypeCompoundEditor.setCallBack(this);
		
		setCellRenderer(getRelationCellRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	private static final long serialVersionUID = 1L;
	
	public void updateRelationInfo(Relation r) {
		relationTypeEditor.updateRelationSelection(r);
		subComponentTypesEditor.updateRelationSelection(r);
		if (r != null)
			relationDescription.setText(r.toStringWithShortDesc(true));
		else
			relationDescription.setText("");
		srcTargetEditor.updateRelationSelection(r);
		subtypeCompoundEditor.updateRelationSelection(r);
		repaint();
	}
	
	private ListCellRenderer getRelationCellRenderer() {
		ListCellRenderer res = new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Relation r = (Relation) value;
				JLabel res = new JLabel(r.toStringWithShortDesc(false));
				res.setToolTipText(r.toStringWithShortDesc(true));
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
