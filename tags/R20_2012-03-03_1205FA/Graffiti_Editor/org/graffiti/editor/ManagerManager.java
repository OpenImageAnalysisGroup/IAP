package org.graffiti.editor;

import org.graffiti.managers.AlgorithmManager;
import org.graffiti.managers.AttributeComponentManager;
import org.graffiti.managers.DefaultAlgorithmManager;
import org.graffiti.managers.DefaultIOManager;
import org.graffiti.managers.DefaultURLattributeActionManager;
import org.graffiti.managers.DefaultViewManager;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.managers.IOManager;
import org.graffiti.managers.URLattributeActionManager;
import org.graffiti.managers.ViewManager;
import org.graffiti.managers.pluginmgr.PluginManager;

public class ManagerManager {
	
	private static ManagerManager instance;
	
	private ManagerManager(PluginManager pluginmgr) {
		ioManager = new DefaultIOManager();
		attributeComponentManager = new AttributeComponentManager();
		editComponentManager = new EditComponentManager();
		urlAttributeActionManager = new DefaultURLattributeActionManager();
		algorithmManager = new DefaultAlgorithmManager();
		viewManager = new DefaultViewManager();
		
		pluginmgr.addPluginManagerListener(viewManager);
		pluginmgr.addPluginManagerListener(algorithmManager);
		pluginmgr.addPluginManagerListener(ioManager);
		pluginmgr.addPluginManagerListener(attributeComponentManager);
		pluginmgr.addPluginManagerListener(editComponentManager);
		pluginmgr.addPluginManagerListener(urlAttributeActionManager);
	}
	
	/** Handles the algorithms. */
	AlgorithmManager algorithmManager;
	
	/** The manager for IO serializers. */
	final IOManager ioManager;
	
	/** The manager for URL attribute actions (load map, view URL, ...). */
	final URLattributeActionManager urlAttributeActionManager;
	
	/** Handles the list of attribute components. */
	public final AttributeComponentManager attributeComponentManager;
	
	/** Handles the list of value edit components. */
	final EditComponentManager editComponentManager;
	
	/** The manager, which maps view type names to view types. */
	public final ViewManager viewManager;
	
	public static synchronized ManagerManager getInstance(PluginManager pluginmgr) {
		if (instance == null)
			instance = new ManagerManager(pluginmgr);
		return instance;
	}
}
