/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.petri_ina_pnt;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.io.OutputSerializer;

/**
 * @author klukas
 */
public class PNTSerializerPlugin
					extends GenericPluginAdapter {
	public PNTSerializerPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.EXTENDED_FILE_FORMAT)) {
			
			this.outputSerializers = new OutputSerializer[] {
								new PNTSerializer()
			};
			// this.inputSerializers = new InputSerializer[] {
			// new PNTReader()
			// };
		}
	}
}