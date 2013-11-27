/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.kegg_xml;

import org.FeatureSet;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class KEGG_XML_ReaderPlugin extends IPK_PluginAdapter {
	public KEGG_XML_ReaderPlugin() {
		super();
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS)) {
			this.inputSerializers = new InputSerializer[] { new KEGG_XML_Reader() };
		}
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS_ENH)) {
			this.inputSerializers = new InputSerializer[] { new KEGG2_XML_Reader() };
			if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
				this.outputSerializers = new OutputSerializer[] { new KEGG2_XML_Writer() };
		}
	}
}
