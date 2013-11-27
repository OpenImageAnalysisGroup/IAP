/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.05.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.RelationType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.SubtypeName;

public class SubComponentNameCheckBox extends JCheckBox {
	private static final long serialVersionUID = 1L;
	
	private Relation currentRelation;
	
	private SubtypeName subTypeName;
	
	private MyRelationList list;
	
	private JLabel subComponentTypesHelp;
	
	public SubComponentNameCheckBox(Relation initialRelation, final SubtypeName subTypeName, JLabel subComponentTypesHelp) {
		setOpaque(false);
		this.subTypeName = subTypeName;
		this.subComponentTypesHelp = subComponentTypesHelp;
		updateRelationSelection(initialRelation);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean sel = isSelected();
				if (sel)
					currentRelation.addSubtypeName(subTypeName);
				else
					currentRelation.removeSubtypeName(subTypeName);
				if (list != null)
					list.updateRelationInfo(currentRelation);
			}
		});
	}
	
	public void updateRelationSelection(Relation r) {
		this.currentRelation = r;
		setText(isSupportedText() + subTypeName.toString());
		if (currentRelation == null) {
			setSelected(false);
			setEnabled(false);
		} else {
			setSelected(currentRelation.hasSubtypeName(subTypeName));
			setEnabled(true);
		}
		validate();
		repaint();
	}
	
	private String isSupportedText() {
		boolean supported = false;
		if (currentRelation == null)
			supported = false;
		else
			if (currentRelation.getType() == RelationType.maplink) {
				if (subTypeName == SubtypeName.indirect)
					supported = true;
			} else
				if (currentRelation.getType() == RelationType.ECrel) {
					if (subTypeName == SubtypeName.compound)
						supported = true;
					if (subTypeName == SubtypeName.hiddenCompound)
						supported = true;
				} else
					if (currentRelation.getType() == RelationType.PPrel) {
						if (subTypeName == SubtypeName.compound)
							supported = true;
						if (subTypeName == SubtypeName.activation)
							supported = true;
						if (subTypeName == SubtypeName.inhibition)
							supported = true;
						if (subTypeName == SubtypeName.indirectEffect)
							supported = true;
						if (subTypeName == SubtypeName.indirect)
							supported = true;
						if (subTypeName == SubtypeName.stateChange)
							supported = true;
						if (subTypeName == SubtypeName.state)
							supported = true;
						if (subTypeName == SubtypeName.binding_association)
							supported = true;
						if (subTypeName == SubtypeName.dissociation)
							supported = true;
						if (subTypeName == SubtypeName.phosphorylation)
							supported = true;
						if (subTypeName == SubtypeName.dephosphorylation)
							supported = true;
						if (subTypeName == SubtypeName.glycosylation)
							supported = true;
						if (subTypeName == SubtypeName.ubiquination)
							supported = true;
						if (subTypeName == SubtypeName.methylation)
							supported = true;
						if (subTypeName == SubtypeName.demethylation)
							supported = true;
					} else
						if (currentRelation.getType() == RelationType.GErel) {
							if (subTypeName == SubtypeName.expression)
								supported = true;
							if (subTypeName == SubtypeName.repression)
								supported = true;
							if (subTypeName == SubtypeName.indirectEffect)
								supported = true;
							if (subTypeName == SubtypeName.indirect)
								supported = true;
						}
		if (subTypeName != SubtypeName.compound &&
							subTypeName != SubtypeName.hiddenCompound &&
							subTypeName != SubtypeName.activation &&
							subTypeName != SubtypeName.inhibition &&
							subTypeName != SubtypeName.expression &&
							subTypeName != SubtypeName.repression &&
							subTypeName != SubtypeName.indirectEffect &&
							subTypeName != SubtypeName.indirect &&
							subTypeName != SubtypeName.stateChange &&
							subTypeName != SubtypeName.state &&
							subTypeName != SubtypeName.binding_association &&
							subTypeName != SubtypeName.dissociation &&
							subTypeName != SubtypeName.phosphorylation &&
							subTypeName != SubtypeName.dephosphorylation &&
							subTypeName != SubtypeName.glycosylation &&
							subTypeName != SubtypeName.ubiquination &&
							subTypeName != SubtypeName.methylation &&
							subTypeName != SubtypeName.demethylation)
			return "<html><font color='blue'>";
		if (!supported && isSelected())
			subComponentTypesHelp.setText("An eventually invalid subtype is selected");
		if (supported)
			return "<html><font color='black'>";
		else
			return "<html><font color='gray'>";
	}
	
	public void setCallBack(MyRelationList list) {
		this.list = list;
	}
	
}
