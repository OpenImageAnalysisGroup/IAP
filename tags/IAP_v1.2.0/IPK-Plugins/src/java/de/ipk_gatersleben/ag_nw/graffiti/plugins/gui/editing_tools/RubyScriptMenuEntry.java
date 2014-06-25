/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 13.10.2003
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import javax.swing.JMenuItem;

/**
 * This class provides the menu entries for the jRuby-Script Menu Items.
 * This modified Menu Items provide the additional methods for storing the
 * ruby command file name.
 * 
 * @author klukas
 */
public class RubyScriptMenuEntry
					extends JMenuItem {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The beanshell commando file-name
	 */
	private String cmdFile;
	
	/**
	 * The menu title
	 */
	private String menuTitle;
	
	/**
	 * Creates a new ScriptMenuEntry object.
	 * 
	 * @param title
	 *           Label text for the menu item
	 * @param commandFile
	 *           The name of the beanshell command file
	 */
	public RubyScriptMenuEntry(String title, String commandFile) {
		cmdFile = commandFile;
		menuTitle = title;
	}
	
	/**
	 * Returns the name of the beanshell commando file for this menuitem.
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getCmdFile() {
		return cmdFile;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.AbstractButton#getText()
	 */
	@Override
	public String getText() {
		return menuTitle;
	}
}
