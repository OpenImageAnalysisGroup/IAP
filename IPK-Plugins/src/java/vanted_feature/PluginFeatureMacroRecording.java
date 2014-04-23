/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package vanted_feature;

import org.FeatureSet;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsFalse;
import org.graffiti.options.GravistoPreferences;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class PluginFeatureMacroRecording
					extends IPK_PluginAdapter {
	
	public PluginFeatureMacroRecording() {
		if (new SettingsHelperDefaultIsFalse().isEnabled("Macro recorder (in development)"))
			ReleaseInfo.enableFeature(FeatureSet.MacroRecorder);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.GenericPlugin#configure(java.util.prefs.Preferences)
	 */
	@Override
	public void configure(GravistoPreferences p) {
		super.configure(p);
	}
}
