/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 * The Interface a custom ContextManager should implement.
 * 
 * @author $author$
 * @version $Revision: 1.1 $
 */
public interface IContextMenuManager {
	
	/**
	 * This Method returns a Context Menu.
	 * 
	 * @param e
	 *           The MouseEvent to be processed.
	 * @return ContextMenu for the MouseEvent
	 */
	public JPopupMenu getContextMenu(MouseEvent e);
	
	// /**
	// * This Method processes a mouse click.
	// *
	// * @param e The MouseEvent to be processed.
	// *
	// * @return True, if the MousEvent was processed by this method, in this case
	// * the event should not be processed any further by the caller.
	// * False if the MouseEvent was not processed as a context-menu-event, in this case
	// * the mouse event should be processed by the caller.
	// */
	// public boolean processMouseButton(MouseEvent e);
	
	/**
	 * This Method should be called by the editing tools in the mouse entered
	 * event-handler.
	 * It should activate the view and session where the mouse button is over.
	 * 
	 * @param e
	 *           The <code>MouseEvent</code>.
	 */
	public void ensureActiveSession(MouseEvent e);
}
