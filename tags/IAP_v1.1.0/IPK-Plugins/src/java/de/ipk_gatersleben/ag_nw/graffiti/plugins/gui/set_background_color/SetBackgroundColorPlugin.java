/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.set_background_color;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.DBE_PluginAdapter;

public class SetBackgroundColorPlugin extends DBE_PluginAdapter {
	
	public SetBackgroundColorPlugin() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			this.algorithms = new Algorithm[] {
								new SetBackgroundColorAlgorithm(),
								new WindowSettings()
			};
	}
}
