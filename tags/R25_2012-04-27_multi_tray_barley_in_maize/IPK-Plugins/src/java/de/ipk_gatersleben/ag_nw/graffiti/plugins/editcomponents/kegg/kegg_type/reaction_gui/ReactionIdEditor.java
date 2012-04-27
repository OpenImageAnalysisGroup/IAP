/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.reaction_gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.graffiti.editor.MainFrame;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;

public class ReactionIdEditor extends JTextField {
	private static final long serialVersionUID = 1L;
	private Reaction currReaction;
	
	private Pathway currPathway;
	
	private MyReactionList list;
	
	public ReactionIdEditor(Reaction initReaction, Pathway pathway) {
		
		this.currPathway = pathway;
		
		updateReactionSelection(initReaction);
		
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				updateReactionSelection(currReaction);
			}
			
			public void focusLost(FocusEvent e) {
				if (currReaction != null) {
					String newId = getText();
					if (newId.equals(currReaction.getId()))
						return;
					boolean known = false;
					for (Entry entry : currPathway.getEntries()) {
						if (entry.getReactions() != null)
							for (KeggId keggID : entry.getReactions()) {
								if (keggID.getId().equals(newId)) {
									known = true;
									break;
								}
							}
						if (known)
							break;
					}
					if (known)
						MainFrame.showMessageDialog(
											"<html>" +
																"This new reaction ID is already in use.<br>" +
																"Now click Cancel in case merge of reaction information is not desired.<br>" +
																"Now click OK and re-open reaction editing in case further editing of this<br>" +
																"reaction is desired. Substrate and product list display are otherwise not<br>" +
																"updated with information from existing reaction definition.", "Information");
					for (Entry entry : currReaction.getEntriesRepresentingThisReaction(currPathway.getEntries())) {
						for (KeggId keggID : entry.getReactions()) {
							if (keggID.getId().equals(currReaction.getId())) {
								keggID.setId(newId);
							}
						}
					}
					currReaction.setId(newId);
					list.updateReactionInfo(currReaction);
				}
			}
		});
	}
	
	public void setCallBack(MyReactionList list) {
		this.list = list;
	}
	
	public void updateReactionSelection(Reaction r) {
		this.currReaction = r;
		if (currReaction != null)
			setText(currReaction.getId());
		else
			setText("");
	}
	
}
