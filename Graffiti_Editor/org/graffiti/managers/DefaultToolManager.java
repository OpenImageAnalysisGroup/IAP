// ==============================================================================
//
// DefaultToolManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultToolManager.java,v 1.1 2011-01-31 09:04:28 klukas Exp $

package org.graffiti.managers;

import java.util.HashSet;
import java.util.Set;

import org.ErrorMsg;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;

/**
 * Manages the list of tools.
 * 
 * @version $Revision: 1.1 $
 */
public class DefaultToolManager
					implements ToolManager {
	// ~ Instance fields ========================================================
	
	/** List of all available tools. */
	private Set<Tool> tools;
	
	/** mode manager */
	private ModeManager modeManager;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new tool manager.
	 */
	public DefaultToolManager(ModeManager modeManager) {
		tools = new HashSet<Tool>();
		this.modeManager = modeManager;
	}
	
	// ~ Methods ================================================================
	
	/*
	 * @see org.graffiti.managers.ToolManager#addTool(org.graffiti.plugin.tool.Tool)
	 */
	public void addTool(Tool tool) {
		tools.add(tool);
	}
	
	/*
	 * @see org.graffiti.managers.pluginmgr.PluginManagerListener#pluginAdded(org.graffiti.plugin.GenericPlugin,
	 * org.graffiti.managers.pluginmgr.PluginDescription)
	 */
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		if (!(plugin instanceof EditorPlugin))
			return;
		Tool[] theTools = ((EditorPlugin) plugin).getTools();
		
		if (theTools != null) {
			for (int i = theTools.length; --i >= 0;) {
				this.tools.add(theTools[i]);
				
				Mode mm = this.modeManager.getMode("org.graffiti.plugins.modes.defaultEditMode");
				if (mm != null) {
					mm.addTool(theTools[i]);
				} else {
					ErrorMsg.addErrorMessage("Could not properly process tool extension");
				}
			}
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
