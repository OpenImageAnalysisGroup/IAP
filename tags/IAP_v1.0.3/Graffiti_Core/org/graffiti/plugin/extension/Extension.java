/*
 * Created on 08.07.2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.graffiti.plugin.extension;

import java.util.List;

import javax.swing.JMenuItem;

/**
 * @author Christian Klukas
 */
public interface Extension {
	/**
	 * Override this method in order to provide menu items, that are
	 * added to the main menu bar.
	 * 
	 * @return Return null, if no menus are provided or a <code>List</code> of <code>JMenuItems</code>
	 */
	List<JMenuItem> getMenuItems();
	
	public String getName();
	
	public String getCategory();
}
