package org.graffiti.managers;

import java.util.Collection;
import java.util.HashSet;

import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.actions.URLattributeAction;

public class DefaultURLattributeActionManager implements
					URLattributeActionManager {
	
	HashSet<URLattributeAction> actions = new HashSet<URLattributeAction>();
	
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		if (plugin.getURLattributeActions() != null)
			for (URLattributeAction ua : plugin.getURLattributeActions())
				actions.add(ua);
	}
	
	public Collection<URLattributeAction> getActions() {
		return actions;
	}
}
