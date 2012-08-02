package org.graffiti.managers;

import java.util.Collection;

import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.plugin.actions.URLattributeAction;

public interface URLattributeActionManager extends PluginManagerListener {
	
	public Collection<URLattributeAction> getActions();
	
}
