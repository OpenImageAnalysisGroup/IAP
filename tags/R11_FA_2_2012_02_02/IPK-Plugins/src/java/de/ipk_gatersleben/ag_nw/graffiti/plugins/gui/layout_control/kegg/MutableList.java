/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class MutableList extends JList {
	private static final long serialVersionUID = 1L;
	
	public MutableList(DefaultListModel model) {
		super(model);
	}
	
	public DefaultListModel getContents() {
		return (DefaultListModel) getModel();
	}
}