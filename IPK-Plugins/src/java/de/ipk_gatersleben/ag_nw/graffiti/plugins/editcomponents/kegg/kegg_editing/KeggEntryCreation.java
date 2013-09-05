/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_editing;

import java.util.Collection;

import javax.swing.JMenuItem;

import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;

public class KeggEntryCreation extends AbstractAlgorithm implements
					ProvidesNodeContextMenu {
	
	public JMenuItem[] getCurrentNodeContextMenuItem(
						final Collection<Node> selectedNodes) {
		return null; /*
						 * if (selectedNodes.size()==1) {
						 * JMenuItem setKeggType = new JMenuItem("Set Entry Type");
						 * setKeggType.addActionListener(new ActionListener() {
						 * public void actionPerformed(ActionEvent arg0) {
						 * setKeggTypeFor(selectedNodes.iterator().next());
						 * }});
						 * return new JMenuItem[] {
						 * setKeggType
						 * };
						 * }
						 * return null;
						 */
	}
	
	public void setKeggTypeFor(Node node) {
		
	}
	
	public void execute() {
		// empty
	}
	
	public String getName() {
		return null;
	}
	
}
