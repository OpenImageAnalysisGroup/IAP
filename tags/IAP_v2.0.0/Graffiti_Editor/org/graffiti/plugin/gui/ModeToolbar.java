// ==============================================================================
//
// ModeToolbar.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ModeToolbar.java,v 1.1 2011-01-31 09:04:33 klukas Exp $

package org.graffiti.plugin.gui;

import java.awt.Component;
import java.util.LinkedList;

import javax.swing.SwingConstants;

import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.AbstractTool;
import org.graffiti.plugin.tool.Tool;

/**
 * This toolbar is designed to be used as a representation of <code>ogr.graffiti.plugin.mode.Mode</code>. It handles toolbuttons in a
 * special way.
 * 
 * @version $Revision: 1.1 $
 * @see org.graffiti.plugin.mode.Mode
 */
public class ModeToolbar
					extends GraffitiToolbar {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor that sets the id of this toolbar. The id is set to the name
	 * of the mode. Tools can be added to the mode represented by this toolbar
	 * by adding their ToolButtons to this toolbar. The orientation is set to
	 * vertical by default.
	 * 
	 * @param m
	 *           the mode this toolbar represents.
	 */
	public ModeToolbar(Mode m) {
		super(m.getId());
		this.setOrientation(SwingConstants.VERTICAL);
	}
	
	/**
	 * Constructor that sets the id of this toolbar to the name of the given
	 * mode and the orientation to the given value.
	 * 
	 * @param m
	 *           the mode this toolbar represents.
	 * @param orientation
	 *           the orientation of this toolbar.
	 */
	public ModeToolbar(Mode m, int orientation) {
		super(m.getId());
		this.setOrientation(orientation);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the tool that is selected in this ModeToolbar's button group.
	 * 
	 * @return the tool that is selected in this ModeToolbar's button group.
	 */
	public Tool getActiveTool() {
		return AbstractTool.getActiveTool();
	}
	
	/**
	 * Returns the tools that are represented by buttons in this toolbar.
	 * 
	 * @return the tools that are represented by buttons in this toolbar.
	 */
	public Tool[] getTools() {
		Component[] c = getComponents();
		LinkedList<Tool> ll = new LinkedList<Tool>();
		for (int i = 0; i < c.length; i++) {
			if (c[i] instanceof ToolButton) {
				ToolButton tb = (ToolButton) c[i];
				ll.add(tb.getTool());
			}
		}
		return (Tool[]) ll.toArray();
	}
	
	/**
	 * This function add the specified component to this toolbar. Additionaly,
	 * if the component is of type <code>ToolButton</code> it is also added to
	 * the button group this toolbar contains. If the component is no <code>ToolButton</code> it is added to the end, else it is added at
	 * the end of the <code>ToolButtons</code> already added.
	 * 
	 * @param comp
	 *           the component to be added.
	 * @return the component <code>comp</code>.
	 * @see java.awt.Container#add(Component)
	 */
	@Override
	public Component add(Component comp) {
		if (comp instanceof ToolButton) {
			ToolButton.addKnownModeToolBar(this);
			return super.add(comp);
		} else {
			return super.add(comp);
		}
	}
	
	// /////////////////////////////////////////////////
	// // Missing: overwriting the other add() methods ////
	// /////////////////////////////////////////////////
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
