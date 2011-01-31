// ==============================================================================
//
// ToolButton.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ToolButton.java,v 1.1 2011-01-31 09:04:33 klukas Exp $

package org.graffiti.plugin.gui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import org.ApplicationStatus;
import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.tool.Tool;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:04:33 $
 */
public class ToolButton
					extends GraffitiToggleButton
					implements GraffitiToolComponent, ActionListener {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The tool this button is identified with. */
	private Tool tool;
	
	private static List<ToolButton> knownTools = new LinkedList<ToolButton>();
	private static List<ModeToolbar> knownToolBars = new LinkedList<ModeToolbar>();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor that sets the buttons tool to the given <code>Tool</code>.
	 * 
	 * @param t
	 *           DOCUMENT ME!
	 */
	public ToolButton(Tool t) {
		this.tool = t;
		addActionListener(this);
		if (!knownTools.contains(this)) {
			knownTools.add(this);
		}
	}
	
	public static void checkStatusForAllToolButtons() {
		if (ErrorMsg.getAppLoadingStatus() == ApplicationStatus.INITIALIZATION)
			return;
		for (Iterator<ToolButton> it = knownTools.iterator(); it.hasNext();) {
			ToolButton t = (ToolButton) it.next();
			// System.out.println(t.tool.isActive());
			t.setSelected(t.tool.isActive());
		}
	}
	
	public static void requestToolButtonFocus() {
		for (Iterator<ToolButton> it = knownTools.iterator(); it.hasNext();) {
			ToolButton t = (ToolButton) it.next();
			if (t.tool.isActive())
				t.requestFocusInWindow();
		}
	}
	
	/**
	 * Creates a new ToolButton object.
	 * 
	 * @param t
	 *           DOCUMENT ME!
	 * @param preferredComponent
	 *           DOCUMENT ME!
	 */
	public ToolButton(Tool t, String preferredComponent) {
		super(preferredComponent);
		this.tool = t;
		addActionListener(this);
		if (!knownTools.contains(this)) {
			knownTools.add(this);
		}
	}
	
	/**
	 * Creates a new ToolButton object.
	 * 
	 * @param t
	 *           DOCUMENT ME!
	 * @param preferredComponent
	 *           DOCUMENT ME!
	 * @param icon
	 *           DOCUMENT ME!
	 */
	public ToolButton(Tool t, String preferredComponent, ImageIcon icon) {
		super(preferredComponent, icon);
		this.tool = t;
		addActionListener(this);
		if (!knownTools.contains(this)) {
			knownTools.add(this);
		}
		setMargin(new Insets(1, 1, 1, 1));
	}
	
	/**
	 * Creates a new ToolButton object.
	 * 
	 * @param t
	 *           DOCUMENT ME!
	 * @param preferredComponent
	 *           DOCUMENT ME!
	 * @param text
	 *           DOCUMENT ME!
	 */
	public ToolButton(Tool t, String preferredComponent, String text) {
		super(text);
		this.tool = t;
		addActionListener(this);
		setMargin(new Insets(1, 1, 1, 1));
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.gui.GraffitiContainer#getId()
	 */
	public String getId() {
		return getClass().getName();
	}
	
	/**
	 * Returns the tool this button is identified with.
	 * 
	 * @return the tool this button is identified with.
	 */
	public Tool getTool() {
		return tool;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		MainFrame.showMessage("", org.graffiti.editor.MessageType.INFO);
		tool.activate();
		checkStatusForAllToolButtons();
	}
	
	/**
	 * @param toolbar
	 */
	public static void addKnownModeToolBar(ModeToolbar toolbar) {
		if (!knownToolBars.contains(toolbar)) {
			knownToolBars.add(toolbar);
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
