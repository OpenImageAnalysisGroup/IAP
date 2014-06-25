/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.reaction_gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.ReactionType;

public class ReactionTypeSelection extends JComboBox {
	private static final long serialVersionUID = 1L;
	private Reaction currReaction;
	private MyReactionList list;
	
	public ReactionTypeSelection(Reaction initReaction) {
		updateReactionSelection(initReaction);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (currReaction != null && getSelectedItem() != null) {
					currReaction.setType((ReactionType) getSelectedItem());
					list.updateReactionInfo(currReaction);
				}
			}
		});
	}
	
	public void updateReactionSelection(Reaction r) {
		this.currReaction = r;
		while (getItemCount() > 0)
			removeItemAt(0);
		if (currReaction != null) {
			addItem(ReactionType.reversible);
			addItem(ReactionType.irreversible);
			setSelectedItem(currReaction.getType());
		}
		validate();
	}
	
	public void setCallBack(MyReactionList list) {
		this.list = list;
	}
}
