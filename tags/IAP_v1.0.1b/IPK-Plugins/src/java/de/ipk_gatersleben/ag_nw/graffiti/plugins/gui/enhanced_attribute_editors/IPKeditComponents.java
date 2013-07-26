/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.enhanced_attribute_editors;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.GenericPlugin;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;

/**
 * This class is a plugin providing some default value edit components.
 * 
 * @version $Revision: 1.2 $
 * @see org.graffiti.plugin.editcomponent.ValueEditComponent
 * @see org.graffiti.plugin.GenericPlugin
 */
public class IPKeditComponents
					extends IPK_EditorPluginAdapter
					implements PluginManagerListener {
	// ~ Constructors ===========================================================
	
	public IPKeditComponents() {
		super();
		
		// register the ui compoents for the displayable types
		// //// valueEditComponents.put(DoubleAttribute.class, EnhDoubleEditComponent.class);
		
		// register the ui compoents for the parameter types
		// //// valueEditComponents.put(DoubleParameter.class, EnhDoubleEditComponent.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.managers.pluginmgr.PluginManagerListener#pluginAdded(org.graffiti.plugin.GenericPlugin,
	 * org.graffiti.managers.pluginmgr.PluginDescription)
	 */
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		if (plugin == null || GravistoService.getInstance().getMainFrame() == null)
			return;
		try {
			if (plugin instanceof EditorPluginAdapter) {
				EditComponentManager cm = GravistoService.getInstance().getMainFrame().getEditComponentManager();
				cm.pluginAdded(this, null);
			}
			// System.out.println("IPK Parameter Plugin overrides Default Parameter Editors.");
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
