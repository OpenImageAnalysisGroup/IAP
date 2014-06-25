/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.text_list;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sif.SIFWriter;

/**
 * @author Christian Klukas
 */
public class TextListReaderPlugin
					extends IPK_PluginAdapter {
	/**
	 *
	 */
	public TextListReaderPlugin() {
		super();
		this.inputSerializers = new InputSerializer[] {
							new TextListReader(),
							new SIFreader()
		};
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.EXTENDED_FILE_FORMAT)) {
			outputSerializers = new OutputSerializer[] {
					new SIFWriter()
			};
		}
	}
}
