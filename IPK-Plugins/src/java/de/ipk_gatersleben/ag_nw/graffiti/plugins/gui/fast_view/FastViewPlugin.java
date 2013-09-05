/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fast_view;

import org.Release;
import org.ReleaseInfo;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;

public class FastViewPlugin
					extends IPK_EditorPluginAdapter {
	public FastViewPlugin() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			this.views = new String[1];
			this.views[0] = "de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fast_view.FastView";
		}
	}
	
}
