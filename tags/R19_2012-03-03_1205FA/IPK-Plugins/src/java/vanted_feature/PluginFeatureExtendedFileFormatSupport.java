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
 * @author Hendrik Rohn
 */
public class PluginFeatureExtendedFileFormatSupport
					extends IPK_PluginAdapter {
	
	public PluginFeatureExtendedFileFormatSupport() {
		if (new SettingsHelperDefaultIsFalse().isEnabled("Extended file format support"))
			ReleaseInfo.enableFeature(FeatureSet.EXTENDED_FILE_FORMAT);
	}
	
	@Override
	public void configure(GravistoPreferences p) {
		super.configure(p);
	}
}
