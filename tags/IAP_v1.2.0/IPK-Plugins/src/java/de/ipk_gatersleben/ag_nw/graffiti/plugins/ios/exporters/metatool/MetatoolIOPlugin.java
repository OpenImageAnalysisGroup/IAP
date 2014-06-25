/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.metatool;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class MetatoolIOPlugin
					extends IPK_PluginAdapter {
	public MetatoolIOPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.EXTENDED_FILE_FORMAT)) {
			outputSerializers = new OutputSerializer[] {
								new MetatoolWriter()
			};
		}
		
		inputSerializers = new InputSerializer[] {
				// new MetaToolReader()
				};
		
	}
}
