/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.05.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.RelationType;

public class RelationTypeEditor extends JComboBox {
	private static final long serialVersionUID = 1L;
	private Relation currentRelation;
	private JLabel relationHelp;
	
	private MyRelationList list;
	
	public RelationTypeEditor(Relation initialRelation, final JLabel relationHelp) {
		super(RelationType.values());
		this.relationHelp = relationHelp;
		updateRelationSelection(initialRelation);
		setOpaque(false);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentRelation != null) {
					currentRelation.setType((RelationType) getSelectedItem());
					if (list != null)
						list.updateRelationInfo(currentRelation);
				}
			}
		});
	}
	
	public void updateRelationSelection(Relation r) {
		this.currentRelation = r;
		if (r == null) {
			setSelectedItem(null);
			setToolTipText("No relation selected");
			relationHelp.setText("");
		} else {
			setSelectedItem(r.getType());
			if (r.getType() != null) {
				setToolTipText(r.getType().getExplanation());
				relationHelp.setText(currentRelation.getType().getExplanation());
			} else {
				setToolTipText("");;
				relationHelp.setText("");
			}
		}
	}
	
	public void setCallBack(MyRelationList list) {
		this.list = list;
	}
}
