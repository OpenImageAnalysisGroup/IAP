/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.xwg_wilma;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.io.OutputSerializer;

public class XWGSerializerPlugin
					extends GenericPluginAdapter {
	
	public XWGSerializerPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.EXTENDED_FILE_FORMAT)) {
			
			this.outputSerializers = new OutputSerializer[] {
								new XWGSerializer()
			};
			
		}
	}
}
