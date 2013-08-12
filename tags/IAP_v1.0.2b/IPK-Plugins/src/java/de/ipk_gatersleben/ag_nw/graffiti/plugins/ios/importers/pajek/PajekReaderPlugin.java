/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.pajek;

import org.FeatureSet;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.pajek.PajekWriter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.pajek.PovrayWriter;

/**
 * DOCUMENT ME!
 * 
 * @author Christian Klukas
 */
public class PajekReaderPlugin
					extends IPK_PluginAdapter {
	/**
	 *
	 */
	public PajekReaderPlugin() {
		super();
		this.inputSerializers = new InputSerializer[] {
							new PajekReader()
		};
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.EXTENDED_FILE_FORMAT)) {
			
			if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG || ReleaseInfo.getRunningReleaseStatus() == Release.RELEASE_IPK)
				this.outputSerializers = new OutputSerializer[] {
									new PovrayWriter(),
									new PajekWriter()
				};
			else
				this.outputSerializers = new OutputSerializer[] {
									new PajekWriter()
				};
		}
	}
}
