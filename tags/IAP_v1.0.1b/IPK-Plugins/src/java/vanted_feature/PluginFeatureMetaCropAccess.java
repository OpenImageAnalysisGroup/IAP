/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package vanted_feature;

import org.FeatureSet;
import org.Release;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsFalse;
import org.graffiti.options.GravistoPreferences;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class PluginFeatureMetaCropAccess
					extends IPK_PluginAdapter {
	
	public PluginFeatureMetaCropAccess() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			// this string needs to be the same as in the FeatureMetaCrop.xml file
			if (new SettingsHelperDefaultIsFalse().isEnabled("MetaCrop and RIMAS database access")) {
				// if SettingsHelperDefaultIsFalse is changed to e.g. SettingsHelperDefaultIsTrue
				// the feature settings XML needs to be changed, too. // CK 31.7.2011
				ReleaseInfo.enableFeature(FeatureSet.MetaCrop_ACCESS);
				ReleaseInfo.enableFeature(FeatureSet.RIMAS_ACCESS);
			}
		}
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
