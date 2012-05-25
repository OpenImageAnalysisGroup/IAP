/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.awt.event.MouseEvent;

import org.graffiti.plugins.modes.defaults.AdvancedLabelTool;

/**
 * A modified editing tool
 * 
 * @author Christian Klukas
 * @version $Revision: 1.1 $
 */
public class IPK_AdvancedLabelTool
					extends AdvancedLabelTool {
	
	public IPK_AdvancedLabelTool() {
		super();
	}
	
	/**
	 * instance of DefaultContextMenuManager
	 */
	DefaultContextMenuManager cmm = new DefaultContextMenuManager();
	
	//
	// /**
	// * Override the mouseClicked Method in order to provide a context menu.
	// *
	// * @param e The MouseEvent
	// */
	// public void mouseClicked(MouseEvent e)
	// {
	// // if (!cmm.processMouseButton(e)) {
	// super.mouseClicked(e);
	// // }
	// }
	// /* (non-Javadoc)
	// * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	// */
	// public void mouseEntered(MouseEvent arg0) {
	// // cmm.ensureActiveSession(arg0);
	// super.mouseEntered(arg0);
	// }
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// cmm.ensureActiveSession(e);
		super.mousePressed(e);
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
